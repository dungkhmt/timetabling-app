package openerp.openerpresourceserver.generaltimetabling.helper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.RoomOccupationWithModuleCode;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.GeneralClass;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.RoomReservation;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingClass;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingClassSegment;
import openerp.openerpresourceserver.generaltimetabling.model.entity.occupation.OccupationClassPeriod;
import openerp.openerpresourceserver.generaltimetabling.model.entity.occupation.RoomOccupation;
import openerp.openerpresourceserver.generaltimetabling.repo.TimeTablingClassRepo;
import openerp.openerpresourceserver.generaltimetabling.repo.TimeTablingClassSegmentRepo;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import openerp.openerpresourceserver.generaltimetabling.helper.ColumnWidthHelper;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.ss.usermodel.BorderStyle;

@Log4j2
@Component
public class GeneralExcelHelper {
    public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    static String[] HEADERS = { "SL thực", "Loại lớp", "Mã HP","Tên HP", "Tuần học", "Thời lượng", "SL max",
            "Lớp học", "Trạng thái", "Mã lớp", "Mã lớp tham chiếu", "Mã lớp tạm thời", "Mã lớp cha", "Kíp", "Đợt", "Khóa", "Giáo viên nước ngoài"};
    static String SHEET = "Sheet1";
    static String DEFAULT_SHEET = "Sheet1";

    /**
     * Start row in excel file to classes
     */
    private final static int START_ROW_TO_READ_CLASS = 2;
    /**
     * Start column in excel to read class information (Start with column A)
     */
    private final static int START_COL_TO_READ_CLASS_INFO = 0;
    /**
     * End column in excel to read class information (End with column Q)
     */
    private final static int END_COL_TO_READ_CLASS_INFO = 16;
    /**
     * Start column in excel to read class schedule (Start with column R)
     */
    private final static int START_COL_TO_READ_CLASS_SCHEDULE = 17;
    /**
     * End column in excel to read class information (End with column BA)
     */
    private final static int END_COL_TO_READ_CLASS_SCHEDULE = 53;

    public static final Integer NUMBER_PERIODS_PER_DAY = 6;

    public static final Integer DEFAULT_VALUE_CALCULATE_TIME = 12;

    /**
     * @param file
     * @return boolean
     * @functionality return true if file has excel format
     * @example example.xlsx return true, example.docx return false
     */
    public static boolean hasExcelFormat(MultipartFile file) {
        return TYPE.equals(file.getContentType());
    }

    /**
     * @param inputStream
     * @functionality this function is for get data from excel file and convert it
     *                to GCO.
     * @return
     */
    public static List<GeneralClass> convertFromExcelToGeneralClassOpened(InputStream inputStream,
                                                                          String semester) {
        try {
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheet(SHEET);
            List<GeneralClass> convertedList = new ArrayList<GeneralClass>();
            if (sheet == null) {
                sheet = workbook.getSheet(DEFAULT_SHEET);
            }
            int totalRowsNum = sheet.getLastRowNum();
            System.out.println(totalRowsNum);
            for (int i = totalRowsNum; i >= START_ROW_TO_READ_CLASS; i--) {
                Row classRow = sheet.getRow(i);
                // skip if not exist classs code
                if (classRow != null && classRow.getCell(9) != null) {
                    Cell classCodeCell = classRow.getCell(9);
                    switch (classCodeCell.getCellType()) {
                        case Cell.CELL_TYPE_BLANK:
                            System.out.println("Cell blank, skip!");
                            continue;
                    }
                } else {
                    continue;
                }
                GeneralClass generalClass = new GeneralClass();
                RoomReservation timeSlot = null;
                int duration = 0;
                for (int j = START_COL_TO_READ_CLASS_INFO; j <= END_COL_TO_READ_CLASS_SCHEDULE; j++) {
                    Cell classInfoCell = classRow.getCell(j);
                    if (classInfoCell != null) {
                        String cellValue = "";
                        switch (classInfoCell.getCellType()) {
                            case 1:
                                cellValue = classInfoCell.getStringCellValue();
                                break;
                            case 0:
                                cellValue = String.valueOf((int) classInfoCell.getNumericCellValue());
                                break;
                            default:
                                break;
                        }
                        if (classInfoCell.getColumnIndex() >= START_COL_TO_READ_CLASS_SCHEDULE
                                && classInfoCell.getCellStyle() != null) {
                            XSSFColor bgColor = (XSSFColor) classInfoCell.getCellStyle().getFillBackgroundColorColor();
                            if (bgColor != null && "FFFFC000".equals(bgColor.getARGBHex())) {
                                if (cellValue != null && !cellValue.isEmpty()) {
                                    timeSlot = new RoomReservation();
                                    timeSlot.setGeneralClass(generalClass);
                                    timeSlot.setStartTime(
                                            (classInfoCell.getColumnIndex() - END_COL_TO_READ_CLASS_INFO) % 6);
                                    timeSlot.setWeekday(
                                            (classInfoCell.getColumnIndex() - END_COL_TO_READ_CLASS_INFO) / 6 + 2);
                                    timeSlot.setRoom(cellValue);
                                }

                                duration++;
                            } else {
                                if (timeSlot != null) {
                                    timeSlot.setEndTime(timeSlot.getStartTime() + duration - 1);
                                    generalClass.addTimeSlot(timeSlot);
                                    timeSlot = null;
                                    duration = 0;
                                }
                            }
                        } else {
                            switch (classInfoCell.getColumnIndex()) {
                                case 0:
                                    if (cellValue.isEmpty() || cellValue.trim().isEmpty()) {
                                        generalClass.setQuantity(null);
                                    } else {
                                        generalClass.setQuantity(Integer.valueOf(cellValue));
                                    }
                                    break;
                                case 1:
                                    generalClass.setClassType(cellValue);
                                    break;
                                case 2:
                                    generalClass.setModuleCode(cellValue);
                                    break;
                                case 3:
                                    generalClass.setModuleName(cellValue);
                                    break;
                                case 4:
                                    generalClass.setLearningWeeks(cellValue);
                                    break;
                                case 5:
                                    generalClass.setMass(cellValue);
                                    break;
                                case 6:
                                    generalClass.setQuantityMax(Integer.valueOf(cellValue));
                                    break;
                                case 7:
                                    generalClass.setStudyClass(cellValue);
                                    break;
                                case 8:
                                    generalClass.setState(cellValue);
                                    break;
                                case 9:
                                    generalClass.setClassCode(cellValue);
                                    break;
                                case 10:
                                    if (cellValue.isEmpty()) {
                                        generalClass.setRefClassId(null);
                                    } else {
                                        generalClass.setRefClassId(Long.valueOf(cellValue));
                                    }
                                    break;
                                case 12:
                                    if (cellValue.isEmpty()) {
                                        generalClass.setParentClassId(null);
                                    } else {
                                        generalClass.setParentClassId(Long.valueOf(cellValue));
                                    }
                                    break;
                                case 13:
                                    generalClass.setCrew(cellValue);
                                    break;
                                case 14:
                                    generalClass.setOpenBatch(cellValue);
                                    break;
                                case 15:
                                    generalClass.setCourse(cellValue);
                                    break;
                                case 16:
                                    generalClass.setForeignLecturer(cellValue);
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }
                generalClass.setSemester(semester);
                convertedList.add(generalClass);
            }
            workbook.close();
            return convertedList;
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(ex);
            return null;
        }
    }


    public static ByteArrayInputStream convertRoomOccupationToExcel(List<RoomOccupationWithModuleCode> rooms) {
        /*Init the data to map*/
        HashMap<String, List<OccupationClassPeriod>> periodMap = new HashMap<>();
        HashMap<OccupationClassPeriod, List<OccupationClassPeriod>> conflictMap = new HashMap<>();
        for(RoomOccupationWithModuleCode room : rooms) {
            if (room.getStartPeriod() == null || room.getEndPeriod() == null || 
                room.getDayIndex() == null || room.getClassRoom() == null) {
                continue;
            }
            
            String classRoom = room.getClassRoom();
            String moduleCode = room.getModuleCode();
            String displayText = room.getClassCode() + (moduleCode != null ? " (" + moduleCode + ")" : "");
            long crewPeriod = "S".equals(room.getCrew()) ? 0 : 6;
            long startPeriodIndex = room.getStartPeriod().intValue() + 12L * (room.getDayIndex().intValue()-2) + crewPeriod;
            long endPeriodIndex = room.getEndPeriod().intValue() + 12L * (room.getDayIndex().intValue()-2) + crewPeriod;
            OccupationClassPeriod period = new OccupationClassPeriod(startPeriodIndex, endPeriodIndex, displayText, classRoom);
            if(periodMap.get(classRoom) == null) {
                List<OccupationClassPeriod> initList = new ArrayList<>();
                initList.add(period);
                periodMap.put(classRoom, initList);
            } else {
                periodMap.get(classRoom).add(period);
            }
            if(conflictMap.get(period) == null) {
                List<OccupationClassPeriod> initList = new ArrayList<>();
                conflictMap.put(period, initList);
            }
        }

        /*Handle Excel write*/
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            /* Init the cell style*/
            /*Bold style*/
            CellStyle boldStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);
            boldStyle.setBorderBottom((short) 1);
            boldStyle.setBorderLeft((short) 1);
            boldStyle.setBorderRight((short) 1);
            boldStyle.setBorderTop((short) 1);
            /*Error style*/
            CellStyle errorStyle=  workbook.createCellStyle();
            errorStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
            errorStyle.setFillPattern((short) 1);
            errorStyle.setFont(boldFont);
            errorStyle.setBorderBottom((short) 1);
            errorStyle.setBorderLeft((short) 1);            errorStyle.setBorderRight((short) 1);
            errorStyle.setBorderTop((short) 1);
            /*Room style*/
            CellStyle roomStyle=  workbook.createCellStyle();
            roomStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            roomStyle.setFillPattern((short) 1);
            roomStyle.setBorderBottom((short) 1);
            roomStyle.setBorderLeft((short) 1);
            roomStyle.setBorderRight((short) 1);
            roomStyle.setBorderTop((short) 1);
            roomStyle.setAlignment(CellStyle.ALIGN_CENTER); // Center horizontally
            roomStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER); // Center vertically
            /*Week index style*/
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setAlignment(CellStyle.ALIGN_CENTER);
            headerStyle.setBorderBottom((short) 1);
            headerStyle.setBorderLeft((short) 1);
            headerStyle.setBorderRight((short) 1);
            headerStyle.setBorderTop((short) 1);
            Sheet sheet = workbook.createSheet(SHEET);            int rowIndex = 0;
            
            // Set column widths for schedule display - half width
            for(int i = 1; i <= 84; i++) {
                sheet.setColumnWidth(i, 256 * 6); // 6 characters width (half of standard)
            }

            /*Header*/
            /*Week index row*/
            Row dayIndexRow = sheet.createRow(rowIndex);
            for (int i = 0; i < 84; i+=12) {
                sheet.addMergedRegion(new CellRangeAddress(rowIndex,rowIndex,i+1,i+12));
                Cell c = dayIndexRow.createCell(i+1);
                String weekIndexString = "" + ((i%84)/12 +2);
                c.setCellValue(weekIndexString);
                c.setCellStyle(headerStyle);
            }
            rowIndex++;
            /*Period row*/
            Row periodRow = sheet.createRow(rowIndex);
            for (int i = 0; i < 84; i++) {
                Cell c = periodRow.createCell(i+1);
                String periodIndexString = "" + ((i%84)%12+1);
                c.setCellValue(periodIndexString);
                c.setCellStyle(headerStyle);
            }
            rowIndex++;


            /*Start write data*/
            for (String room : periodMap.keySet()) {
                if(room != null && !room.isEmpty()) {
                    Row roomRow = sheet.createRow(rowIndex);
                    Cell roomNameCell = roomRow.createCell(0);
                    roomNameCell.setCellValue(room);
                    roomNameCell.setCellStyle(headerStyle);
                    for (int cellIndex = 1; cellIndex <= 84; cellIndex++) {
                        Cell c = roomRow.createCell(cellIndex);
                        c.setCellStyle(boldStyle);
                        for (OccupationClassPeriod roomPeriod : periodMap.get(room)) {
                            sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, (int) roomPeriod.getStartPeriodIndex(), (int) roomPeriod.getEndPeriodIndex()));
                            if(cellIndex >=  roomPeriod.getStartPeriodIndex() && cellIndex <= roomPeriod.getEndPeriodIndex()) {
                                // If cell value is not empty, append class code with comma
                                if (c.getStringCellValue() != null && !c.getStringCellValue().isEmpty()) {
                                    Set<String> classCodeSet = new HashSet<>(Arrays.stream(c.getStringCellValue().split(",")).toList());
                                    classCodeSet.add(roomPeriod.getClassCode());
                                    c.setCellValue(String.join(",", classCodeSet));
                                    c.setCellStyle(errorStyle);
                                } else {
                                    c.setCellValue(roomPeriod.getClassCode());
                                    c.setCellStyle(roomStyle);
                                }
                            }
                        }
                    }
                    rowIndex++;
                }
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }    /**
     * Overload of convertGeneralClassToExcel that accepts a map of class segments and numberSlotsPerSession
     * This version creates a timetable with morning and afternoon sessions combined in one continuous day view
     */
    public static ByteArrayInputStream convertGeneralClassToExcelWithAllSession(List<TimeTablingClass> classes, Map<Long, List<TimeTablingClassSegment>> mClassId2ClassSegments, Integer numberSlotsPerSession) {
        /*Handle Excel write*/
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            /* Init the cell style*/
            /*Bold style*/
            CellStyle boldStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);
            boldStyle.setBorderBottom((short) 1);
            boldStyle.setBorderLeft((short) 1);
            boldStyle.setBorderRight((short) 1);
            boldStyle.setBorderTop((short) 1);            
            
            int rowIndex = START_ROW_TO_READ_CLASS;
            Sheet sheet = workbook.createSheet(SHEET);
            
            /*Room style*/
            CellStyle roomStyle=  workbook.createCellStyle();
            roomStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            roomStyle.setFillPattern((short) 1);
            roomStyle.setBorderBottom((short) 1);
            roomStyle.setBorderLeft((short) 1);
            roomStyle.setBorderRight((short) 1);
            roomStyle.setBorderTop((short) 1);
            roomStyle.setAlignment(CellStyle.ALIGN_CENTER); // Center horizontally
            roomStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER); // Center vertically

            // Tạo style riêng cho buổi sáng (màu vàng)
            CellStyle morningStyle = workbook.createCellStyle();
            morningStyle.cloneStyleFrom(roomStyle);
            
            // Tạo style riêng cho buổi chiều (màu xanh nhạt)
            CellStyle afternoonStyle = workbook.createCellStyle();
            afternoonStyle.cloneStyleFrom(roomStyle);
            afternoonStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            afternoonStyle.setFillPattern((short) 1);

            /*Header style*/
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(boldFont);
            headerStyle.setAlignment(CellStyle.ALIGN_CENTER);
            headerStyle.setBorderBottom((short) 1);
            headerStyle.setBorderLeft((short) 1);
            headerStyle.setBorderRight((short) 1);
            headerStyle.setBorderTop((short) 1); // Ensure top border is applied

            /*Create default cell style with borders*/
            CellStyle defaultStyle = workbook.createCellStyle();
            defaultStyle.setBorderBottom((short) 1);
            defaultStyle.setBorderLeft((short) 1);
            defaultStyle.setBorderRight((short) 1);
            defaultStyle.setBorderTop((short) 1);
            
            // Tạo header style cho buổi sáng (màu vàng nhạt)
            CellStyle morningHeaderStyle = workbook.createCellStyle();
            morningHeaderStyle.cloneStyleFrom(headerStyle);
            morningHeaderStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            morningHeaderStyle.setFillPattern((short) 1);
            
            // Tạo header style cho buổi chiều (màu xanh nhạt)
            CellStyle afternoonHeaderStyle = workbook.createCellStyle();
            afternoonHeaderStyle.cloneStyleFrom(headerStyle);
            afternoonHeaderStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
            afternoonHeaderStyle.setFillPattern((short) 1);
            
            /*Header*/
            /*Handle create header info*/
            
            // Số tiết mỗi ngày là số tiết mỗi session * 2 (sáng + chiều)
            int slotsPerDay = numberSlotsPerSession * 2;
            
            // Set column widths - make schedule columns half width
            ColumnWidthHelper.setTimeTableColumnWidths(sheet, END_COL_TO_READ_CLASS_INFO, 
                                                     START_COL_TO_READ_CLASS_SCHEDULE, 
                                                     slotsPerDay, 7); // 7 days (Mon-Sun)
            
            Row weekIndexRow = sheet.createRow(rowIndex);
            for (int i = 0; i < HEADERS.length; i += 1) {
                sheet.addMergedRegion(new CellRangeAddress(rowIndex,rowIndex+1,i,i));
                Cell c = weekIndexRow.createCell(i);
                String classInfoString = HEADERS[i];
                c.setCellValue(classInfoString);
                c.setCellStyle(headerStyle);
            }
            
            /*Handle create header schedule info */
            int totalColumns = 7 * slotsPerDay; // 7 days (Mon-Sun) * (morning + afternoon) slots per day
            
            // Day row
            for (int i = START_COL_TO_READ_CLASS_SCHEDULE; i < START_COL_TO_READ_CLASS_SCHEDULE+totalColumns; i += slotsPerDay) {
                CellRangeAddress region = new CellRangeAddress(rowIndex, rowIndex, i, i+slotsPerDay-1);
                sheet.addMergedRegion(region);
                
                // Explicitly set borders for the merged region
                RegionUtil.setBorderTop((short) 1, region, sheet, workbook);
                RegionUtil.setBorderBottom((short) 1, region, sheet, workbook);
                RegionUtil.setBorderLeft((short) 1, region, sheet, workbook);
                RegionUtil.setBorderRight((short) 1, region, sheet, workbook);
                
                Cell c = weekIndexRow.createCell(i);
                int dayIndex = (i-START_COL_TO_READ_CLASS_SCHEDULE)/slotsPerDay + 2;
                String weekString = dayIndex < 8 ? "Thứ " + dayIndex : "Chủ nhật";
                c.setCellValue(weekString);
                c.setCellStyle(headerStyle);
            }
            
            // Session row (below day row)
            Row sessionRow = sheet.createRow(rowIndex+1);
            for (int i = START_COL_TO_READ_CLASS_SCHEDULE; i < START_COL_TO_READ_CLASS_SCHEDULE+totalColumns; i += slotsPerDay) {
                // Sáng (Morning)
                CellRangeAddress morningRegion = new CellRangeAddress(rowIndex+1, rowIndex+1, i, i+numberSlotsPerSession-1);
                sheet.addMergedRegion(morningRegion);
                
                // Set borders
                RegionUtil.setBorderTop((short) 1, morningRegion, sheet, workbook);
                RegionUtil.setBorderBottom((short) 1, morningRegion, sheet, workbook);
                RegionUtil.setBorderLeft((short) 1, morningRegion, sheet, workbook);
                RegionUtil.setBorderRight((short) 1, morningRegion, sheet, workbook);
                
                Cell morningCell = sessionRow.createCell(i);
                morningCell.setCellValue("Sáng");
                morningCell.setCellStyle(morningHeaderStyle);
                
                // Chiều (Afternoon)
                CellRangeAddress afternoonRegion = new CellRangeAddress(rowIndex+1, rowIndex+1, 
                                                   i+numberSlotsPerSession, i+slotsPerDay-1);
                sheet.addMergedRegion(afternoonRegion);
                
                // Set borders
                RegionUtil.setBorderTop((short) 1, afternoonRegion, sheet, workbook);
                RegionUtil.setBorderBottom((short) 1, afternoonRegion, sheet, workbook);
                RegionUtil.setBorderLeft((short) 1, afternoonRegion, sheet, workbook);
                RegionUtil.setBorderRight((short) 1, afternoonRegion, sheet, workbook);
                
                Cell afternoonCell = sessionRow.createCell(i+numberSlotsPerSession);
                afternoonCell.setCellValue("Chiều");
                afternoonCell.setCellStyle(afternoonHeaderStyle);
            }
            
            // Period row (below session row)
            Row periodRow = sheet.createRow(rowIndex+2);
            for (int i = START_COL_TO_READ_CLASS_SCHEDULE; i < START_COL_TO_READ_CLASS_SCHEDULE+totalColumns; i++) {
                Cell c = periodRow.createCell(i);
                // Calculate the period number (1-based) within its session
                int columnPosition = i - START_COL_TO_READ_CLASS_SCHEDULE;
                // int dayIndex = columnPosition / slotsPerDay; // Not strictly needed for period string logic
                int periodInDay = columnPosition % slotsPerDay; // 0-indexed period within the combined day

                // Determine if this is morning or afternoon slot based on periodInDay
                boolean isMorning = periodInDay < numberSlotsPerSession;
                
                int periodInSession = periodInDay;
                
                // For afternoon session, adjust period number to be 0-indexed within the afternoon session
                if (!isMorning) {
                    periodInSession = periodInDay - numberSlotsPerSession;
                }
                
                // Period numbers should always start from 1
                String periodString = "" + (periodInSession + 1);
                
                c.setCellValue(periodString);
                c.setCellStyle(isMorning ? morningHeaderStyle : afternoonHeaderStyle);
            }

            rowIndex+=3; // Increase row index for data rows (after day, session, and period rows)
            
            /*Handle write class info and schedule*/
            for (TimeTablingClass timeTablingClass : classes) {
                Row classRow = sheet.createRow(rowIndex);
                /*Write the class info*/
                for (int i = 0 ; i <= END_COL_TO_READ_CLASS_INFO; i++ ) {
                    Cell c = classRow.createCell(i);
                    c.setCellStyle(defaultStyle); // Add default style with borders
                    switch (i) {
                        case 0:
                            if (timeTablingClass.getQuantity() != null) {
                                c.setCellValue(timeTablingClass.getQuantity());
                            }
                            break;
                        case 1:
                            c.setCellValue(timeTablingClass.getClassType());
                            break;
                        case 2:
                            c.setCellValue(timeTablingClass.getModuleCode());
                            break;
                        case 3:
                            c.setCellValue(timeTablingClass.getModuleName());
                            break;
                        case 4:
                            c.setCellValue(timeTablingClass.getLearningWeeks());
                            break;
                        case 5:
                            c.setCellValue(timeTablingClass.getMass());
                            break;
                        case 6:
                            if (timeTablingClass.getQuantityMax() != null) {
                                c.setCellValue(timeTablingClass.getQuantityMax());
                            }
                            break;
                        case 7:
                            c.setCellValue(timeTablingClass.getStudyClass());
                            break;
                        case 8:
                            c.setCellValue(timeTablingClass.getState());
                            break;
                        case 9:
                            c.setCellValue(timeTablingClass.getClassCode());
                            break;
                        case 10:
                            if (timeTablingClass.getRefClassId() != null) {
                                c.setCellValue(timeTablingClass.getRefClassId());
                            }
                            break;
                        case 11:
                            if (timeTablingClass.getId()!= null) {
                                c.setCellValue(timeTablingClass.getId());
                            }
                            break;
                        case 12:
                            if (timeTablingClass.getParentClassId() != null) {
                                c.setCellValue(timeTablingClass.getParentClassId());
                            }
                            break;
                        case 13:
                            c.setCellValue(timeTablingClass.getCrew());
                            break;
                        case 14:
                            c.setCellValue(timeTablingClass.getOpenBatch());
                            break;
                        case 15:
                            c.setCellValue(timeTablingClass.getCourse());
                            break;
                        case 16:
                            c.setCellValue(timeTablingClass.getForeignLecturer());
                            break;
                        default:
                            break;
                    }
                }
                
                /*Write the class schedule using segments*/
                for (int j = START_COL_TO_READ_CLASS_SCHEDULE; j < START_COL_TO_READ_CLASS_SCHEDULE + totalColumns; j++) {
                    Cell c = classRow.createCell(j);
                    c.setCellStyle(defaultStyle); // Add default style with borders
                    
                    // Get segments for current class from the map
                    List<TimeTablingClassSegment> segments = mClassId2ClassSegments.getOrDefault(timeTablingClass.getId(), new ArrayList<>());
                    for (TimeTablingClassSegment segment : segments) {
                        if (segment.getRoom() != null && segment.getWeekday() != null && segment.getStartTime() != null && segment.getEndTime() != null) {
                            // Điều chỉnh công thức để lùi 1 cột sang trái
                            int dayIndex = segment.getWeekday();
                            // Chuyển đổi chỉ số ngày: 8 -> 7 (chủ nhật)
                            if (dayIndex == 8) dayIndex = 7;
                            else if (dayIndex > 8) continue; // Bỏ qua nếu nằm ngoài phạm vi
                            
                            // Xác định kíp học (S = sáng, C = chiều)
                            String crew = timeTablingClass.getCrew();
                            boolean isMorning = "S".equals(crew);
                            
                            // Tính toán vị trí cột dựa vào ngày, kíp và thời gian
                            int sessionOffset = isMorning ? 0 : numberSlotsPerSession;
                            int startCol = (dayIndex-2)*slotsPerDay + sessionOffset + segment.getStartTime() - 1 + START_COL_TO_READ_CLASS_SCHEDULE;
                            int endCol = (dayIndex-2)*slotsPerDay + sessionOffset + segment.getEndTime() - 1 + START_COL_TO_READ_CLASS_SCHEDULE;
                            
                            sheet.addMergedRegion(new CellRangeAddress(
                                rowIndex, rowIndex, 
                                startCol, 
                                endCol
                            ));
                            
                            if (j >= startCol && j <= endCol) {
                                // Hiển thị phòng học
                                c.setCellValue(segment.getRoom());
                                
                                // Sử dụng style tương ứng với kíp
                                CellStyle cellStyle = isMorning ? morningStyle : afternoonStyle;
                                c.setCellStyle(cellStyle);
                            }
                        }
                    }
                }
                rowIndex++;
            }
            
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    public static ByteArrayInputStream convertGeneralClassToExcel(List<TimeTablingClass> classes, Map<Long, List<TimeTablingClassSegment>> mClassId2ClassSegments, Integer numberSlotsPerSession) {
        /*Handle Excel write*/
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            /* Init the cell style*/
            /*Bold style*/
            CellStyle boldStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);
            boldStyle.setBorderBottom((short) 1);
            boldStyle.setBorderLeft((short) 1);
            boldStyle.setBorderRight((short) 1);
            boldStyle.setBorderTop((short) 1);            int rowIndex = START_ROW_TO_READ_CLASS;
            Sheet sheet = workbook.createSheet(SHEET);
            /*Room style*/
            CellStyle roomStyle=  workbook.createCellStyle();
            roomStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            roomStyle.setFillPattern((short) 1);
            roomStyle.setBorderBottom((short) 1);
            roomStyle.setBorderLeft((short) 1);
            roomStyle.setBorderRight((short) 1);
            roomStyle.setBorderTop((short) 1);
            roomStyle.setAlignment(CellStyle.ALIGN_CENTER); // Center horizontally
            roomStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER); // Center vertically

            /*Header style*/
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(boldFont);
            headerStyle.setAlignment(CellStyle.ALIGN_CENTER);
            headerStyle.setBorderBottom((short) 1);
            headerStyle.setBorderLeft((short) 1);
            headerStyle.setBorderRight((short) 1);
            headerStyle.setBorderTop((short) 1); // Ensure top border is applied

            /*Create default cell style with borders*/
            CellStyle defaultStyle = workbook.createCellStyle();
            defaultStyle.setBorderBottom((short) 1);
            defaultStyle.setBorderLeft((short) 1);
            defaultStyle.setBorderRight((short) 1);
            defaultStyle.setBorderTop((short) 1);            /*Header*/
            /*Handle create header info*/
            
            // Set column widths - make schedule columns half width
            ColumnWidthHelper.setTimeTableColumnWidths(sheet, END_COL_TO_READ_CLASS_INFO, 
                                                     START_COL_TO_READ_CLASS_SCHEDULE, 
                                                     numberSlotsPerSession, 7); // 7 days (Mon-Sun)
            
            Row weekIndexRow = sheet.createRow(rowIndex);
            for (int i = 0; i < HEADERS.length; i += 1) {
                sheet.addMergedRegion(new CellRangeAddress(rowIndex,rowIndex+1,i,i));
                Cell c = weekIndexRow.createCell(i);
                String classInfoString = HEADERS[i];
                c.setCellValue(classInfoString);
                c.setCellStyle(headerStyle);
            }
            /*Handle create header schedule info */            Row periodIndexRow = sheet.createRow(rowIndex+1);
            int totalColumns = 7 * numberSlotsPerSession; // 7 days (Mon-Sun) * slots per day
            for (int i = START_COL_TO_READ_CLASS_SCHEDULE; i < START_COL_TO_READ_CLASS_SCHEDULE+totalColumns; i += numberSlotsPerSession) {
                CellRangeAddress region = new CellRangeAddress(rowIndex, rowIndex, i, i+numberSlotsPerSession-1);
                sheet.addMergedRegion(region);
                
                // Explicitly set borders for the merged region - with all 4 required parameters
                RegionUtil.setBorderTop((short) 1, region, sheet, workbook);     // 1 = THIN border
                RegionUtil.setBorderBottom((short) 1, region, sheet, workbook);
                RegionUtil.setBorderLeft((short) 1, region, sheet, workbook);
                RegionUtil.setBorderRight((short) 1, region, sheet, workbook);
                
                Cell c = weekIndexRow.createCell(i);
                int dayIndex = (i-START_COL_TO_READ_CLASS_SCHEDULE)/numberSlotsPerSession + 2;
                String weekString = dayIndex < 8 ? "Thứ " + dayIndex : "Chủ nhật";
                c.setCellValue(weekString);
                c.setCellStyle(headerStyle);
            }            for (int i = START_COL_TO_READ_CLASS_SCHEDULE; i < START_COL_TO_READ_CLASS_SCHEDULE+totalColumns; i++) {
                Cell c = periodIndexRow.createCell(i);
                String periodString = "" + ((i-START_COL_TO_READ_CLASS_SCHEDULE)%numberSlotsPerSession + 1);
                c.setCellValue(periodString);
                c.setCellStyle(headerStyle);
            }

            rowIndex+=2;
            /*Handle write class info and schedule*/
            for (TimeTablingClass timeTablingClass : classes) {
                Row classRow = sheet.createRow(rowIndex);
                /*Write the class info*/
                for (int i = 0 ; i <= END_COL_TO_READ_CLASS_INFO; i++ ) {
                    Cell c = classRow.createCell(i);
                    c.setCellStyle(defaultStyle); // Add default style with borders
                    switch (i) {
                        case 0:
                            if (timeTablingClass.getQuantity() != null) {
                                c.setCellValue(timeTablingClass.getQuantity());
                            }
                            break;
                        case 1:
                            c.setCellValue(timeTablingClass.getClassType());
                            break;
                        case 2:
                            c.setCellValue(timeTablingClass.getModuleCode());
                            break;
                        case 3:
                            c.setCellValue(timeTablingClass.getModuleName());
                            break;
                        case 4:
                            c.setCellValue(timeTablingClass.getLearningWeeks());
                            break;
                        case 5:
                            c.setCellValue(timeTablingClass.getMass());
                            break;
                        case 6:
                            if (timeTablingClass.getQuantityMax() != null) {
                                c.setCellValue(timeTablingClass.getQuantityMax());
                            }
                            break;
                        case 7:
                            c.setCellValue(timeTablingClass.getStudyClass());
                            break;
                        case 8:
                            c.setCellValue(timeTablingClass.getState());
                            break;
                        case 9:
                            c.setCellValue(timeTablingClass.getClassCode());
                            break;
                        case 10:
                            if (timeTablingClass.getRefClassId() != null) {
                                c.setCellValue(timeTablingClass.getRefClassId());
                            }
                            break;
                        case 11:
                            if (timeTablingClass.getId()!= null) {
                                c.setCellValue(timeTablingClass.getId());
                            }
                            break;
                        case 12:
                            if (timeTablingClass.getParentClassId() != null) {
                                c.setCellValue(timeTablingClass.getParentClassId());
                            }
                            break;
                        case 13:
                            c.setCellValue(timeTablingClass.getCrew());
                            break;
                        case 14:
                            c.setCellValue(timeTablingClass.getOpenBatch());
                            break;
                        case 15:
                            c.setCellValue(timeTablingClass.getCourse());
                            break;
                        case 16:
                            c.setCellValue(timeTablingClass.getForeignLecturer());
                            break;
                        default:
                            break;
                    }
                }
                  /*Write the class schedule using segments*/
                for (int j = START_COL_TO_READ_CLASS_SCHEDULE; j < START_COL_TO_READ_CLASS_SCHEDULE + totalColumns; j++) {
                    Cell c = classRow.createCell(j);
                    c.setCellStyle(defaultStyle); // Add default style with borders
                    
                    // Get segments for current class from the map
                    List<TimeTablingClassSegment> segments = mClassId2ClassSegments.getOrDefault(timeTablingClass.getId(), new ArrayList<>());
                    for (TimeTablingClassSegment segment : segments) {
                        if (segment.getRoom() != null && segment.getWeekday() != null && segment.getStartTime() != null && segment.getEndTime() != null) {
                            // Điều chỉnh công thức để lùi 1 cột sang trái
                            int dayIndex = segment.getWeekday();
                            // Chuyển đổi chỉ số ngày: 8 -> 7 (chủ nhật)
                            if (dayIndex == 8) dayIndex = 7;
                            else if (dayIndex > 8) continue; // Bỏ qua nếu nằm ngoài phạm vi
                            
                            int startCol = (dayIndex-2)*numberSlotsPerSession + segment.getStartTime() - 1 + START_COL_TO_READ_CLASS_SCHEDULE;
                            int endCol = START_COL_TO_READ_CLASS_SCHEDULE + (dayIndex-2)*numberSlotsPerSession + segment.getEndTime() - 1;                            
                            sheet.addMergedRegion(new CellRangeAddress(
                                rowIndex, rowIndex, 
                                startCol, 
                                endCol
                            ));
                            
                            if (j - START_COL_TO_READ_CLASS_SCHEDULE >= (dayIndex-2)*numberSlotsPerSession + segment.getStartTime() - 1 && 
                                j - START_COL_TO_READ_CLASS_SCHEDULE <= (dayIndex-2)*numberSlotsPerSession + segment.getEndTime() - 1) {
                                c.setCellValue(segment.getRoom());
                                c.setCellStyle(roomStyle);                                CellStyle specificStyle = workbook.createCellStyle();
                                specificStyle.cloneStyleFrom(roomStyle);
                                specificStyle.setAlignment(CellStyle.ALIGN_CENTER);
                                specificStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
                                specificStyle.setBorderTop((short) 1); // Adding top border
                                c.setCellStyle(specificStyle);
                            }


                        }
                    }
                }
                rowIndex++;
            }
            
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    public static Map<String, Object> saveTimeTablingClassAndSegmentsFromExcel(
            InputStream inputStream, 
            String semester,
            TimeTablingClassRepo classRepo,
            TimeTablingClassSegmentRepo segmentRepo) {
        try {
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheet(SHEET);
            List<TimeTablingClass> classes = new ArrayList<>();
            Map<Long, List<TimeTablingClassSegment>> mClassId2ClassSegments = new HashMap<>();
            
            if (sheet == null) {
                sheet = workbook.getSheet(DEFAULT_SHEET);
            }
            
            int totalRowsNum = sheet.getLastRowNum();
            for (int i = totalRowsNum; i >= START_ROW_TO_READ_CLASS; i--) {
                Row classRow = sheet.getRow(i);
                // Skip if class code doesn't exist
                if (classRow != null && classRow.getCell(9) != null) {
                    Cell classCodeCell = classRow.getCell(9);
                    if (classCodeCell.getCellType() == Cell.CELL_TYPE_BLANK) {
                        continue;
                    }
                } else {
                    continue;
                }
                
                // Create the class object from the row
                TimeTablingClass timeTablingClass = new TimeTablingClass();
                timeTablingClass.setSemester(semester);
                
                // Process each cell in the row
                for (int j = START_COL_TO_READ_CLASS_INFO; j <= END_COL_TO_READ_CLASS_INFO; j++) {
                    Cell cell = classRow.getCell(j);
                    if (cell != null) {
                        String value = "";
                        if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                            value = cell.getStringCellValue();
                        } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                            value = String.valueOf((int) cell.getNumericCellValue());
                        }
                        
                        switch (j) {
                            case 0: // quantity
                                if (!value.isEmpty()) {
                                    timeTablingClass.setQuantity(Integer.valueOf(value));
                                }
                                break;
                            case 1: // class type
                                timeTablingClass.setClassType(value);
                                break;
                            case 2: // module code
                                timeTablingClass.setModuleCode(value);
                                break;
                            case 3: // module name
                                timeTablingClass.setModuleName(value);
                                break;
                            case 4: // learning weeks
                                timeTablingClass.setLearningWeeks(value);
                                break;
                            case 5: // mass
                                timeTablingClass.setMass(value);
                                break;
                            case 6: // quantity max
                                if (!value.isEmpty()) {
                                    timeTablingClass.setQuantityMax(Integer.valueOf(value));
                                }
                                break;
                            case 7: // study class
                                timeTablingClass.setStudyClass(value);
                                break;
                            case 8: // state
                                timeTablingClass.setState(value);
                                break;
                            case 9: // class code
                                timeTablingClass.setClassCode(value);
                                break;
                            case 10: // ref class id
                                if (!value.isEmpty()) {
                                    timeTablingClass.setRefClassId(Long.valueOf(value));
                                }
                                break;
                            case 12: // parent class id
                                if (!value.isEmpty()) {
                                    timeTablingClass.setParentClassId(Long.valueOf(value));
                                }
                                break;
                            case 13: // crew
                                timeTablingClass.setCrew(value);
                                break;
                            case 14: // open batch
                                timeTablingClass.setOpenBatch(value);
                                break;
                            case 15: // course
                                timeTablingClass.setCourse(value);
                                break;
                            case 16: // foreign lecturer
                                timeTablingClass.setForeignLecturer(value);
                                break;
                        }
                    }
                }
                
                // Save the TimeTablingClass to get an ID
                if (timeTablingClass.getId() == null) {
                    Long nextId = classRepo.getNextReferenceValue();
                    timeTablingClass.setId(nextId);
                }
                TimeTablingClass savedClass = classRepo.save(timeTablingClass);
                classes.add(savedClass);
                
                // Now process the schedule cells to create TimeTablingClassSegment objects
                List<TimeTablingClassSegment> segments = new ArrayList<>();
                
                for (int j = START_COL_TO_READ_CLASS_SCHEDULE; j < START_COL_TO_READ_CLASS_SCHEDULE + 42; j++) {
                    Cell cell = classRow.getCell(j);
                    if (cell != null && cell.getCellStyle() != null && cell.getStringCellValue() != null && !cell.getStringCellValue().isEmpty()) {
                        // Check if this is a cell with room information (by checking cell style or value)
                        XSSFColor bgColor = (XSSFColor) cell.getCellStyle().getFillForegroundColorColor();
                        if (bgColor != null && bgColor.getARGBHex() != null && bgColor.getARGBHex().equals("FFFF9900")) {
                            String room = cell.getStringCellValue();
                            
                            // Calculate weekday and period from cell position
                            int colOffset = j - START_COL_TO_READ_CLASS_SCHEDULE;
                            int weekday = (colOffset / 6) + 2; // 2=Monday, 3=Tuesday, etc.
                            int startTime = (colOffset % 6);   // 0-5 represents periods 1-6
                            
                            // Find how many cells are merged horizontally (represents duration)
                            int duration = 1;
                            for (int k = 0; k < sheet.getNumMergedRegions(); k++) {
                                CellRangeAddress merge = sheet.getMergedRegion(k);
                                if (merge.getFirstRow() == i && merge.getLastRow() == i &&
                                    merge.getFirstColumn() <= j && merge.getLastColumn() >= j) {
                                    duration = merge.getLastColumn() - merge.getFirstColumn() + 1;
                                    break;
                                }
                            }
                            
                            // Create segment
                            TimeTablingClassSegment segment = new TimeTablingClassSegment();
                            segment.setClassId(savedClass.getId());
                            segment.setRoom(room);
                            segment.setWeekday(weekday);
                            segment.setStartTime(startTime);
                            segment.setEndTime(startTime + duration - 1);
                            segment.setDuration(duration);
                            segment.setCrew(savedClass.getCrew());
                            
                            // Save segment
                            Long nextSegmentId = segmentRepo.getNextReferenceValue();
                            segment.setId(nextSegmentId);
                            TimeTablingClassSegment savedSegment = segmentRepo.save(segment);
                            segments.add(savedSegment);
                            
                            // Skip ahead to avoid processing the same merged cell multiple times
                            j += (duration - 1);
                        }
                    }
                }
                
                if (!segments.isEmpty()) {
                    mClassId2ClassSegments.put(savedClass.getId(), segments);
                }
            }
            
            workbook.close();
            
            Map<String, Object> result = new HashMap<>();
            result.put("classes", classes);
            result.put("segments", mClassId2ClassSegments);
            
            return result;
            
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(ex);
            return null;
        }
    }
}