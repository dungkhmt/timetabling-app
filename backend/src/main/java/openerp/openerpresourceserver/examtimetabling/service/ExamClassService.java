package openerp.openerpresourceserver.examtimetabling.service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.examtimetabling.entity.ExamClass;
import openerp.openerpresourceserver.examtimetabling.repository.ExamClassRepository;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder.In;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.sql.DataSource;

@Service
@RequiredArgsConstructor
public class ExamClassService {
    private final ExamClassRepository examClassRepository;
    private final EntityManager entityManager;
    @Autowired
    private DataSource dataSource;
    private static final int BATCH_SIZE = 1000;

    public List<ExamClass> getExamClassesByPlanId(UUID examPlanId) {
        return examClassRepository.findByExamPlanId(examPlanId);
    }

    @Transactional
    public void deleteClasses(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        
        for (int i = 0; i < ids.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, ids.size());
            List<UUID> batchIds = ids.subList(i, end);
            
            String idParams = batchIds.stream()
                .map(id -> "'" + id + "'")
                .collect(Collectors.joining(","));
            
            String deleteAssignmentsSql = 
                "DELETE FROM exam_timetable_assignment " +
                "WHERE exam_timtabling_class_id IN (" +
                "SELECT id FROM exam_timetabling_class WHERE id IN (" + idParams + ")" +
                ")";
            
            entityManager.createNativeQuery(deleteAssignmentsSql).executeUpdate();
        }
        
        for (int i = 0; i < ids.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, ids.size());
            List<UUID> batchIds = ids.subList(i, end);
            examClassRepository.deleteByExamClassIdIn(batchIds);
        }
    }

    public boolean validateExamClass(String examClassId, UUID examPlanId) {
        return examClassRepository.existsByExamClassIdAndExamPlanId(examClassId, examPlanId);
    }

    @Transactional
    public ExamClass createExamClass(ExamClass examClass) {
        ExamClass savedClass = examClassRepository.save(examClass);
        
        String bulkInsertSql = 
            "INSERT INTO exam_timetable_assignment " +
            "(id, exam_timetable_id, exam_timtabling_class_id) " +
            "SELECT uuid_generate_v4(), id, :examClassId " +
            "FROM exam_timetable " + 
            "WHERE exam_plan_id = :examPlanId";
        
        Query query = entityManager.createNativeQuery(bulkInsertSql);
        query.setParameter("examClassId", savedClass.getId());
        query.setParameter("examPlanId", examClass.getExamPlanId());
        query.setParameter("now", LocalDateTime.now());
        
        query.executeUpdate();
        
        return savedClass;
    }

    public ExamClass updateExamClass(ExamClass examClass) {
        // Check if exists
        if (!examClassRepository.existsById(examClass.getId())) {
            throw new RuntimeException("Exam class not found with id: " + examClass.getExamClassId());
        }
        
        return examClassRepository.save(examClass);
    }

    @Transactional
    public List<ExamClass> bulkCreateFromExcel(MultipartFile file, UUID examPlanId, Integer groupDescriptionId, String groupName) throws IOException, EncryptedDocumentException, InvalidFormatException {
        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        
        // For conflicts check
        Set<String> excelExamClassIds = new HashSet<>();
        
        List<Object[]> batchParams = new ArrayList<>();
        List<UUID> newClassIds = new ArrayList<>(); // Store IDs of new classes for assignment creation
        
        // Skip header row and process Excel data
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            
            try {
                String examClassId = getStringValue(row.getCell(14));
                
                // Skip if examClassId is null or empty
                if (examClassId == null || examClassId.trim().isEmpty()) continue;
                
                excelExamClassIds.add(examClassId);
                
                UUID id = UUID.randomUUID();
                newClassIds.add(id); // Store the ID for later use
                
                String classId = getStringValue(row.getCell(0));
                String courseId = getStringValue(row.getCell(2));
                String groupId = getStringValue(row.getCell(5));
                String courseName = getStringValue(row.getCell(3));
                String description = groupName;
                Integer numberOfStudents = getIntValue(row.getCell(8));
                String period = getStringValue(row.getCell(9));
                String managementCode = getStringValue(row.getCell(11));
                String school = getStringValue(row.getCell(13));
                Integer examClassGroupId = groupDescriptionId;
                
                batchParams.add(new Object[]{
                    id, examClassId, classId, courseId, groupId, courseName, 
                    description, numberOfStudents, period, managementCode, 
                    school, examPlanId, examClassGroupId
                });
            } catch (Exception e) {
                System.out.println("Error processing row " + i + ": " + e.getMessage());
            }
        }
        workbook.close();
        
        if (excelExamClassIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<ExamClass> conflictClasses = examClassRepository.findByExamPlanIdAndExamClassIdIn(
            examPlanId, new ArrayList<>(excelExamClassIds));
        
        // If conflicts found, return them
        if (!conflictClasses.isEmpty()) {
            return conflictClasses;
        }
        
        // No conflicts - insert exam classes
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        
        String sql = "INSERT INTO exam_timetabling_class (id, exam_class_id, class_id, course_id, " +
            "group_id, course_name, description, number_students, period, management_code, " +
            "school, exam_plan_id, exam_group_id) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Object[] params = batchParams.get(i);
                ps.setObject(1, params[0]); 
                ps.setString(2, (String)params[1]); 
                ps.setString(3, (String)params[2]);  
                ps.setString(4, (String)params[3]);  
                ps.setString(5, (String)params[4]);  
                ps.setString(6, (String)params[5]);  
                ps.setString(7, (String)params[6]);  
                ps.setObject(8, params[7]);  
                ps.setString(9, (String)params[8]);  
                ps.setString(10, (String)params[9]);
                ps.setString(11, (String)params[10]); 
                ps.setObject(12, params[11]); 
                ps.setObject(13, params[12]); 
            }

            @Override
            public int getBatchSize() {
                return batchParams.size();
            }
        });
        
        if (!newClassIds.isEmpty()) {
            List<UUID> timetableIds = getTimetableIdsForExamPlan(examPlanId);
            
            if (!timetableIds.isEmpty()) {
                LocalDateTime now = LocalDateTime.now();
                
                for (UUID timetableId : timetableIds) {
                    createAssignmentsForTimetable(timetableId, newClassIds, now);
                }
            }
        }
        
        return new ArrayList<>();
    }

    private List<UUID> getTimetableIdsForExamPlan(UUID examPlanId) {
        String sql = "SELECT id FROM exam_timetable WHERE exam_plan_id = :examPlanId";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("examPlanId", examPlanId);
        
        @SuppressWarnings("unchecked")
        List<Object> results = query.getResultList();
        
        List<UUID> timetableIds = new ArrayList<>();
        for (Object result : results) {
            if (result != null) {
                timetableIds.add(UUID.fromString(result.toString()));
            }
        }
        
        return timetableIds;
    }

    private void createAssignmentsForTimetable(UUID timetableId, List<UUID> classIds, LocalDateTime timestamp) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        
        String insertSql = "INSERT INTO exam_timetable_assignment " +
                        "(id, exam_timetable_id, exam_timtabling_class_id, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?)";
        
        List<Object[]> batchParams = classIds.stream()
            .map(classId -> new Object[] {
                UUID.randomUUID(),  
                timetableId,        
                classId,            
                timestamp,         
                timestamp           
            })
            .collect(Collectors.toList());
        
        jdbcTemplate.batchUpdate(insertSql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Object[] params = batchParams.get(i);
                ps.setObject(1, params[0]);  
                ps.setObject(2, params[1]);  
                ps.setObject(3, params[2]);  
                ps.setObject(4, params[3]);  
                ps.setObject(5, params[4]);  
            }

            @Override
            public int getBatchSize() {
                return batchParams.size();
            }
        });
    }
    
   public ByteArrayInputStream loadExamClasses(List<UUID> ids) {
        List<ExamClass> examClasses = examClassRepository.findAllById(ids);

        try (Workbook workbook = new XSSFWorkbook()) { 
            Sheet sheet = workbook.createSheet("Exam Classes");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "Mã lớp QT", "Mã lớp LT", "Mã học phần", "Tên học phần", "Ghi chú",
                "studyGroupID", "Nhóm", "sessionid", "SL", "Đợt mở",
                "ManagerID", "Mã_QL", "TeachUnitID", "Tên trường/khoa", "Mã lớp thi"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Create data rows
            int rowNum = 1;
            for (ExamClass examClass : examClasses) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(examClass.getClassId());           // Mã lớp QT
                row.createCell(1).setCellValue("");                               // Mã lớp LT  
                row.createCell(2).setCellValue(examClass.getCourseId());         // Mã học phần
                row.createCell(3).setCellValue(examClass.getCourseName());       // Tên học phần
                row.createCell(4).setCellValue(examClass.getDescription());      // Ghi chú
                row.createCell(5).setCellValue(examClass.getGroupId());          // studyGroupID
                row.createCell(6).setCellValue("");                              // Nhóm
                row.createCell(7).setCellValue("");                              // sessionid
                row.createCell(8).setCellValue(examClass.getNumberOfStudents()); // SL
                row.createCell(9).setCellValue(examClass.getPeriod());           // Đợt mở
                row.createCell(10).setCellValue("");                             // ManagerID
                row.createCell(11).setCellValue(examClass.getManagementCode());  // Mã_QL
                row.createCell(12).setCellValue("");                             // TeachUnitID
                row.createCell(13).setCellValue(examClass.getSchool());          // Tên trường/khoa
                row.createCell(14).setCellValue(examClass.getExamClassId());     // Mã lớp thi
            }

            // Autosize columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Failed to export exam classes data to Excel file: " + e.getMessage());
        }
    }

    public ByteArrayInputStream generateTemplate() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Exam Classes Template");
    
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "Mã lớp QT", "Mã lớp LT", "Mã học phần", "Tên học phần", "Ghi chú",
                "studyGroupID", "Nhóm", "sessionid", "SL", "Đợt mở",
                "ManagerID", "Mã_QL", "TeachUnitID", "Tên trường/khoa", "Mã lớp thi"
            };
    
            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
    
            // Set headers
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
    
            // Add sample data - Row 1
            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("158783");      // Mã lớp QT
            row1.createCell(1).setCellValue("158783");      // Mã lớp LT
            row1.createCell(2).setCellValue("AC2030");      // Mã học phần
            row1.createCell(3).setCellValue("Khai thác thông tin đa phương tiện"); // Tên học phần
            row1.createCell(4).setCellValue("CN giáo dục"); // Ghi chú
            row1.createCell(5).setCellValue("01-K68S");     // studyGroupID
            row1.createCell(6).setCellValue("365");         // Nhóm
            row1.createCell(7).setCellValue("TC");          // sessionid
            row1.createCell(8).setCellValue(650);           // SL
            row1.createCell(9).setCellValue("51");          // Đợt mở
            row1.createCell(10).setCellValue("AB");         // ManagerID
            row1.createCell(11).setCellValue("671");        // Mã_QL
            row1.createCell(12).setCellValue("CT CHUẨN");   // TeachUnitID
            row1.createCell(13).setCellValue("Trường Điện - Điện tử"); // Tên trường/khoa
            row1.createCell(14).setCellValue("182568");     // Mã lớp thi
    
            // Add sample data - Row 2
            Row row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue("158784");
            row2.createCell(1).setCellValue("158784");
            row2.createCell(2).setCellValue("AC2031");
            row2.createCell(3).setCellValue("Lập trình web nâng cao");
            row2.createCell(4).setCellValue("CN giáo dục");
            row2.createCell(5).setCellValue("02-K68S");
            row2.createCell(6).setCellValue("366");
            row2.createCell(7).setCellValue("TC");
            row2.createCell(8).setCellValue(45);
            row2.createCell(9).setCellValue("51");
            row2.createCell(10).setCellValue("CD");
            row2.createCell(11).setCellValue("672");
            row2.createCell(12).setCellValue("CT CHUẨN");
            row2.createCell(13).setCellValue("Trường Điện - Điện tử");
            row2.createCell(14).setCellValue("182569");
    
            // Autosize columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
    
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
    
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel template", e);
        }
    }

    private String getStringValue(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            case Cell.CELL_TYPE_NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            default:
                return null;
        }
    }

    private Integer getIntValue(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC:
                return (int) cell.getNumericCellValue();
            case Cell.CELL_TYPE_STRING:
                try {
                    return Integer.parseInt(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    return null;
                }
            default:
                return null;
        }
    }
}
