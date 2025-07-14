package openerp.openerpresourceserver.generaltimetabling.service;

import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.Utils;
import openerp.openerpresourceserver.generaltimetabling.exception.NotFoundException;
import openerp.openerpresourceserver.generaltimetabling.helper.*;
import openerp.openerpresourceserver.generaltimetabling.model.dto.CreateTimeTablingClassDto;
import openerp.openerpresourceserver.generaltimetabling.model.entity.*;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.PlanGeneralClass;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingClass;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingClassSegment;
import openerp.openerpresourceserver.generaltimetabling.model.entity.occupation.RoomOccupation;
import openerp.openerpresourceserver.generaltimetabling.repo.*;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.FilterClassOpenedDto;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.GeneralClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Collectors;

@Log4j2
@Service
public class ExcelService {

    public static final String CONFLICT_CLASS = "Trùng lịch với lớp: ";

    @Autowired
    private ScheduleRepo scheduleRepo;

    @Autowired
    private ClassOpenedRepo classOpenedRepo;

    @Autowired
    private GeneralClassRepository gcoRepo;

    @Autowired
    private ClassroomRepo classroomRepo;

    @Autowired
    private RoomOccupationService roomOccupationService;

    @Autowired
    private PlanGeneralClassRepo planGeneralClassRepo;

    @Autowired
    private PlanGeneralClassService planGeneralClassService;

    @Autowired
    private ClassGroupRepo classGroupRepo;

    @Autowired
    private GroupRepo groupRepo;    
    
    @Autowired
    private TimeTablingClassRepo timeTablingClassRepo;
    
    @Autowired 
    private TimeTablingClassSegmentRepo timeTablingClassSegmentRepo;   

    private Map<String, Object> prepareClassDataForExport(String semester, Long versionId, Integer numberSlotsPerSession) {
        int slots = numberSlotsPerSession != null ? numberSlotsPerSession : 6;

        List<TimeTablingClass> classes = timeTablingClassRepo.findAllBySemester(semester)
                .stream()
                .filter(c -> c.getClassCode() != null && !c.getClassCode().isEmpty())
                .collect(Collectors.toCollection(ArrayList::new));
        classes.sort((a, b) -> {
            Comparable fieldValueA = a.getClassCode();
            Comparable fieldValueB = b.getClassCode();
            return fieldValueA.compareTo(fieldValueB);
        });
        //List<Long> classIds = new ArrayList<>();
        //for(TimeTablingClass cls: classes) classIds.add(cls.getId());


        List<Long> classIds = classes.stream().map(c -> c.getId()).toList();

        List<Group> groups = groupRepo.findAll();
        Map<Long, Group> mId2Group = new HashMap<>();
        for(Group g: groups) mId2Group.put(g.getId(),g);
        List<ClassGroup> classGroups = classGroupRepo.findAllByClassIdIn(classIds);
        Map<Long, List<String>> mClassId2GroupNames = new HashMap<>();
        for(Long id: classIds) mClassId2GroupNames.put(id, new ArrayList<>());
        for(ClassGroup cg: classGroups){
            Long classId = cg.getClassId();
            Long gId = cg.getGroupId();
            String groupName = mId2Group.get(gId).getGroupName();
            mClassId2GroupNames.get(classId).add(groupName);
        }

        List<TimeTablingClassSegment> segments = timeTablingClassSegmentRepo.findAllByClassIdInAndVersionId(classIds, versionId);
        List<TimeTablingClassSegment> validSegments = segments.stream()
                .filter(segment -> segment.getStartTime() != null && 
                                 segment.getEndTime() != null && 
                                 segment.getWeekday() != null)
                .collect(Collectors.toList());
        
        log.info("Total segments: {}, Valid segments (not null startTime): {}", 
                segments.size(), validSegments.size());
        
        classes = Utils.sort(classes, validSegments);
        // Tạo map từ class ID -> các segments liên quan (chỉ segments hợp lệ)
        Map<Long, List<TimeTablingClassSegment>> mClassId2ClassSegments = new HashMap<>();
        for (TimeTablingClassSegment segment : validSegments) {

            if (!mClassId2ClassSegments.containsKey(segment.getClassId())) {
                mClassId2ClassSegments.put(segment.getClassId(), new ArrayList<>());
            }
            mClassId2ClassSegments.get(segment.getClassId()).add(segment);
        }
        
        if (classes.isEmpty()) throw new NotFoundException("Kỳ học không có bất kỳ lớp học nào!");
        
        Map<String, Object> result = new HashMap<>();
        result.put("classes", classes);
        result.put("segments", mClassId2ClassSegments);
        result.put("slots", slots);
        result.put("classGroup",mClassId2GroupNames);
        return result;
    }
    
    /**
     * Export general excel with regular format
     */
    public InputStream exportGeneralExcel(String semester, Long versionId, Integer numberSlotsPerSession) {
        Map<String, Object> data = prepareClassDataForExport(semester, versionId, numberSlotsPerSession);
        return GeneralExcelHelper.convertGeneralClassToExcel(
            (List<TimeTablingClass>) data.get("classes"), 
            (Map<Long, List<TimeTablingClassSegment>>) data.get("segments"),
                (Map<Long, List<String>>) data.get("classGroup"),
            (Integer) data.get("slots")
        );
    }

    /**
     * Export general excel with all sessions format
     */
    public InputStream exportGeneralExcelWithAllSession(String semester, Long versionId, Integer numberSlotsPerSession) {
        Map<String, Object> data = prepareClassDataForExport(semester, versionId, numberSlotsPerSession);
        return GeneralExcelHelper.convertGeneralClassToExcelWithAllSession(
            (List<TimeTablingClass>) data.get("classes"), 
            (Map<Long, List<TimeTablingClassSegment>>) data.get("segments"),
                (Map<Long, List<String>>) data.get("classGroup"),
            (Integer) data.get("slots")
        );
    }



    // public ByteArrayInputStream load() {
    // List<Schedule> schedules = this.getAllSchedules();
    // ByteArrayInputStream in = ExcelHelper.schedulesToExcel(schedules);
    // return in;
    // }

    public ByteArrayInputStream loadExport(FilterClassOpenedDto requestDto) {
        String semester = requestDto.getSemester();
        String groupName = requestDto.getGroupName();
        List<ClassOpened> classOpenedList;
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        if (semester != null) {
            if (groupName != null) {
                classOpenedList = classOpenedRepo.getAllBySemesterAndGroupName(semester, groupName, sort);
            } else
                classOpenedList = classOpenedRepo.getAllBySemester(semester, sort);
        } else {
            if (groupName != null) {
                classOpenedList = classOpenedRepo.getAllByGroupName(groupName, sort);
            } else
                classOpenedList = classOpenedRepo.findAll(sort);
        }
        ByteArrayInputStream in = ExcelHelper.classOpenedToExcelExport(classOpenedList);
        return in;
    }

    public ByteArrayInputStream loadClassConflict(List<ClassOpened> classOpenedList) {
        return ExcelHelper.classOpenedToExcelExport(classOpenedList);
    }

    // public void save(MultipartFile file) {
    // try {
    // List<Schedule> tutorials =
    // ExcelHelper.excelToSchedules(file.getInputStream());
    // tutorials.forEach(el -> {
    // if (el != null && !el.getState().equals("Huỷ lớp")) {
    // scheduleRepo.save(el);
    // }
    // });
    // } catch (IOException e) {
    // throw new RuntimeException("fail to store excel data: " + e.getMessage());
    // }
    // }    @Transactional
    public List<GeneralClass> saveGeneralClasses(MultipartFile file, String semester) {
        gcoRepo.deleteAllBySemester(semester);
        try {
            List<GeneralClass> generalClassList = GeneralExcelHelper
                    .convertFromExcelToGeneralClassOpened(file.getInputStream(), semester);
            if (generalClassList == null) {
                return new ArrayList<>();
            }
            List<RoomOccupation> roomOccupations = new ArrayList<>();
            generalClassList.forEach(generalClass -> {
                generalClass.getTimeSlots().forEach(timeSlot -> {
                    List<String> learningWeekStrings = Arrays.asList(generalClass.getLearningWeeks().trim().split(","));
                    learningWeekStrings.forEach(learningWeek -> {
                        if(
                            LearningWeekValidator.isCorrectFormat(learningWeek)
                        ) {
                            LearningWeekExtractor.extract(learningWeek).forEach(weekInt->{
                                roomOccupations.add(new RoomOccupation(
                                    timeSlot.getRoom(),
                                    generalClass.getClassCode(),
                                    timeSlot.getStartTime(),
                                    timeSlot.getEndTime(),
                                    generalClass.getCrew(),
                                    timeSlot.getWeekday(),
                                    weekInt,
                                    "study",
                                    generalClass.getSemester()
                                ));
                            });
                        }
                    });
                });
            });
            gcoRepo.saveAll(generalClassList);
            roomOccupationService.saveAll(roomOccupations);
            return generalClassList;
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<GeneralClass>();
        }
    }
    
//    @Transactional
//    public Map<String, Object> saveTimeTablingClasses(MultipartFile file, String semester, Long versionId) {
//        try {
//            // Nếu cần, có thể xóa các lớp và segments hiện có cho kỳ học này
//            // timeTablingClassRepo.deleteAllBySemester(semester);
//            // timeTablingClassSegmentRepo.deleteAllByVersionId(versionId);
//
//            Map<String, Object> result = GeneralExcelHelper
//                    .saveTimeTablingClassAndSegmentsFromExcel(file.getInputStream(), semester,
//                                                             timeTablingClassRepo, timeTablingClassSegmentRepo);
//
//            if (result == null) {
//                return new HashMap<>();
//            }
//
//            // Cập nhật versionId cho tất cả các segments
//            if (result.containsKey("segments")) {
//                @SuppressWarnings("unchecked")
//                Map<Long, List<TimeTablingClassSegment>> segmentsMap =
//                    (Map<Long, List<TimeTablingClassSegment>>)result.get("segments");
//
//                for (List<TimeTablingClassSegment> segments : segmentsMap.values()) {
//                    for (TimeTablingClassSegment segment : segments) {
//                        segment.setVersionId(versionId);
//                        timeTablingClassSegmentRepo.save(segment);
//                    }
//                }
//            }
//
//            // Tạo RoomOccupation từ các TimeTablingClassSegment
//            List<RoomOccupation> roomOccupations = new ArrayList<>();
//            if (result.containsKey("classes") && result.containsKey("segments")) {
//                @SuppressWarnings("unchecked")
//                List<TimeTablingClass> classes = (List<TimeTablingClass>) result.get("classes");
//
//                @SuppressWarnings("unchecked")
//                Map<Long, List<TimeTablingClassSegment>> segmentsMap =
//                    (Map<Long, List<TimeTablingClassSegment>>) result.get("segments");
//
//                for (TimeTablingClass cls : classes) {
//                    List<TimeTablingClassSegment> segments = segmentsMap.get(cls.getId());
//                    if (segments != null) {
//                        for (TimeTablingClassSegment segment : segments) {
//                            List<String> learningWeekStrings = Arrays.asList(cls.getLearningWeeks().trim().split(","));
//                            learningWeekStrings.forEach(learningWeek -> {
//                                if (LearningWeekValidator.isCorrectFormat(learningWeek)) {
//                                    LearningWeekExtractor.extract(learningWeek).forEach(weekInt -> {
//                                        roomOccupations.add(new RoomOccupation(
//                                            segment.getRoom(),
//                                            cls.getClassCode(),
//                                            segment.getStartTime(),
//                                            segment.getEndTime(),
//                                            cls.getCrew(),
//                                            segment.getWeekday(),
//                                            weekInt,
//                                            "study",
//                                            cls.getSemester()
//                                        ));
//                                    });
//                                }
//                            });
//                        }
//                    }
//                }
//
//                // Lưu các RoomOccupation
//                roomOccupationService.saveAll(roomOccupations);
//            }
//
//            return result;
//        } catch (IOException e) {
//            e.printStackTrace();
//            log.error("Không thể lưu các lớp từ Excel: " + e.getMessage());
//            return new HashMap<>();
//        }
//    }

    public List<ClassOpened> saveClassOpened(MultipartFile file, String semester) {
        try {
            List<ClassOpened> tutorials = ExcelHelper.excelToClassOpened(file.getInputStream());
            List<ClassOpened> classOpenedConflictList = new ArrayList<>();
            for (ClassOpened el : tutorials) {
                if (el != null && !el.getCourse().isEmpty() && !el.getStudyClass().isEmpty()) {
                    el.setSemester(semester);
                    if (el.getStartPeriod() != null && el.getClassroom() != null && el.getWeekday() != null) {
                        String crew = el.getCrew();
                        Long startPeriod = Long.parseLong(el.getStartPeriod());
                        String weekday = el.getWeekday();
                        String classroom = el.getClassroom();
                        long currentFinish = this.calculateFinishPeriod(el.getMass(), startPeriod,
                                el.getIsSeparateClass());

                        String conflictClass = null;
                        String conflictSecondClass = null;
                        List<ClassOpened> existedClassOpened = classOpenedRepo
                                .getAllBySemesterAndClassroomAndWeekdayAndCrewAndStartPeriodIsNotNull(semester,
                                        classroom, weekday, crew);
                        List<ClassOpened> existedClassOpenedSecond = classOpenedRepo
                                .getAllBySemesterAndSecondClassroomAndSecondWeekdayAndCrewAndSecondStartPeriodIsNotNull(
                                        semester, classroom, weekday, crew);

                        // Kiểm tra trùng lịch với danh sách lớp đơn hoặc lớp thứ nhất của lớp tách
                        conflictClass = this.checkConflictTimeForListFirstClass(existedClassOpened, startPeriod,
                                currentFinish);

                        // Kiểm tra trùng lịch với danh sách lớp thứ hai của lớp tách
                        conflictSecondClass = this.checkConflictTimeForListSecondClass(existedClassOpenedSecond,
                                startPeriod, currentFinish);

                        if (conflictClass != null || conflictSecondClass != null) {
                            classOpenedConflictList.add(el);
                            String stateConflict = conflictClass != null ? conflictClass + ","
                                    : "" + " "
                                            + conflictSecondClass != null ? conflictSecondClass + "," : "";
                            el.setState(CONFLICT_CLASS + stateConflict);
                            el.setClassroom(null);
                            el.setWeekday(null);
                            el.setStartPeriod(null);
                        }

                        classOpenedRepo.save(el);

                        if (el.getIsSeparateClass()) {
                            startPeriod = Long.parseLong(el.getSecondStartPeriod());
                            weekday = el.getSecondWeekday();
                            classroom = el.getSecondClassroom();
                            currentFinish = this.calculateFinishPeriod(el.getMass(), startPeriod,
                                    el.getIsSeparateClass());
                            String conflictClassSecond = null;
                            String conflictSecondClassSecond = null;
                            List<ClassOpened> listClassOpened = classOpenedRepo
                                    .getAllBySemesterAndClassroomAndWeekdayAndCrewAndStartPeriodIsNotNull(semester,
                                            classroom, weekday, crew);
                            List<ClassOpened> listSecondClassOpened = classOpenedRepo
                                    .getAllBySemesterAndSecondClassroomAndSecondWeekdayAndCrewAndSecondStartPeriodIsNotNullAndIdNot(
                                            semester, classroom, weekday, crew, el.getId());

                            // Kiểm tra trùng lịch với danh sách lớp đơn hoặc lớp thứ nhất của lớp tách
                            conflictClassSecond = this.checkConflictTimeForListFirstClass(listClassOpened, startPeriod,
                                    currentFinish);

                            // Kiểm tra trùng lịch với danh sách lớp thứ hai của lớp tách
                            conflictSecondClassSecond = this.checkConflictTimeForListSecondClass(listSecondClassOpened,
                                    startPeriod, currentFinish);

                            if (conflictClassSecond != null || conflictSecondClassSecond != null) {
                                boolean isExisted = classOpenedConflictList.contains(el);
                                String stateConflict = conflictClassSecond != null ? conflictClassSecond + ","
                                        : "" + " "
                                                + conflictSecondClassSecond != null ? conflictSecondClassSecond + ","
                                                        : "";
                                if (isExisted) {
                                    String stateExisted = el.getState();
                                    el.setState(stateExisted + " " + stateConflict);
                                } else {
                                    classOpenedConflictList.add(el);
                                    el.setState(CONFLICT_CLASS + stateConflict);
                                }
                                el.setSecondClassroom(null);
                                el.setSecondStartPeriod(null);
                                el.setSecondWeekday(null);
                            }
                            classOpenedRepo.save(el);
                        }
                    } else
                        classOpenedRepo.save(el);

                    // el.setSemester(semester);
                    // classOpenedRepo.save(el);
                }
            }
            return classOpenedConflictList;
        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
    }

    private String checkConflictTimeForListFirstClass(List<ClassOpened> listClassOpened, long currentStartPeriod,
            long currentFinish) {
        for (ClassOpened el : listClassOpened) {
            String supMass = el.getMass();
            String moduleName = el.getModuleName();
            Boolean isSeparateClassExisted = el.getIsSeparateClass() != null ? el.getIsSeparateClass() : false;
            long existedStartPeriod = Long.parseLong(el.getStartPeriod());
            long existedFinishPeriod = this.calculateFinishPeriod(supMass, existedStartPeriod, isSeparateClassExisted);

            if (!this.compareTimeForSetClassroom(currentStartPeriod, currentFinish, existedStartPeriod,
                    existedFinishPeriod)) {
                return moduleName;
            }
        }
        return null;
    }

    private String checkConflictTimeForListSecondClass(List<ClassOpened> listSecondClassOpened, long currentStartPeriod,
            long currentFinish) {
        for (ClassOpened el : listSecondClassOpened) {
            String supMass = el.getMass();
            String moduleName = el.getModuleName();
            Boolean isSeparateClassExisted = el.getIsSeparateClass() != null ? el.getIsSeparateClass() : false;
            long existedStartPeriod = Long.parseLong(el.getSecondStartPeriod());
            long existedFinishPeriod = this.calculateFinishPeriod(supMass, existedStartPeriod, isSeparateClassExisted);

            if (!this.compareTimeForSetClassroom(currentStartPeriod, currentFinish, existedStartPeriod,
                    existedFinishPeriod)) {
                return moduleName;
            }
        }
        return null;
    }

    private Boolean compareTimeForSetClassroom(long currentStartPeriod, long currentFinish,
            long existedStartPeriod, long existedFinishPeriod) {
        if (currentStartPeriod > existedStartPeriod) {
            return currentStartPeriod > existedFinishPeriod;
        } else {
            return currentFinish < existedStartPeriod;
        }
    }

    private Long calculateFinishPeriod(String mass, Long startPeriod, Boolean isSeparateClass) {
        long totalPeriod = this.calculateTotalPeriod(mass);
        long finishPeriod = isSeparateClass ? (startPeriod + (totalPeriod / 2) - 1) : startPeriod + totalPeriod - 1;
        return finishPeriod;
    }

    private Long calculateTotalPeriod(String mass) {
        // a(b-c-d-e) => b-c-d-e => b,c,d,e => b+c
        String numbersString = mass.trim().substring(2, mass.indexOf(')'));
        String[] numbersArray = numbersString.split("-");
        return Long.parseLong(numbersArray[0]) + Long.parseLong(numbersArray[1]);
    }

    public void saveClassroom(MultipartFile file) {
        try {
            List<Classroom> tutorials = ExcelHelper.excelToClassroom(file.getInputStream());
            tutorials.forEach(el -> {
                if (el != null && !el.getClassroom().isEmpty() && el.getQuantityMax() != null) {
                    classroomRepo.save(el);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
    }

    public List<Schedule> getAllSchedules() {
        return scheduleRepo.findAll();
    }

    public ByteArrayInputStream exportRoomOccupationExcel(String semester, int week, Long versionId, int numberSlotsPerSession) {
        return roomOccupationService.exportExcel(semester, week, versionId, numberSlotsPerSession);
    }


    @Transactional
    //public List<PlanGeneralClass> savePlanClasses(MultipartFile file, String semester, boolean createClass, String groupName) {
    public List<PlanGeneralClass> savePlanClasses(MultipartFile file, String semester, boolean createClass, Long groupId) {
        log.info("savePlanClasses... START");
        try {
            //planGeneralClassRepository.deleteAllBySemester(semester);
            List<PlanGeneralClass> planClasses = PlanGeneralClassExcelHelper
                    .convertExcelToPlanGeneralClasses(file.getInputStream(), semester);
            log.info("savePlanClasses, extract from excel GOT " + planClasses.size() + " items");
            //return planGeneralClassRepository.saveAll(planClasses);
            for(PlanGeneralClass pl: planClasses) pl.setGroupId(groupId);
            planGeneralClassRepo.saveAll(planClasses);
            log.info("savePlanClasse planCLasses CREATED!");

            //createClass = false;
            if(createClass){
                
                // create classes from planClasses
                for(PlanGeneralClass p: planClasses) {
                    log.info("savePlanClasses, start to create class for plan " + p.getModuleCode() + " nbClasses = " + p.getNumberOfClasses());
                    for(int i = 1;i <= p.getNumberOfClasses();i++) {
                        CreateTimeTablingClassDto req = new CreateTimeTablingClassDto();
                        req.setId(p.getId());
                        req.setNbClasses(p.getNumberOfClasses());
                        req.setClassType(p.getClassType());
                        req.setDuration(p.getDuration());
                        req.setCrew(p.getCrew());
                        req.setMass(p.getMass());
                        req.setLearningWeeks(p.getLearningWeeks());
                        req.setModuleCode(p.getModuleCode());
                        req.setSemester(p.getSemester());
                        req.setModuleName(p.getModuleName());
                        req.setExerciseMaxQuantity(p.getExerciseMaxQuantity());
                        req.setLectureMaxQuantity(p.getLectureMaxQuantity());
                        req.setLectureExerciseMaxQuantity(p.getLectureExerciseMaxQuantity());
                        req.setProgramName(p.getProgramName());
                        req.setWeekType(p.getWeekType());
                        //planGeneralClassService.makeClass(req, groupName);
                        planGeneralClassService.makeClass(req, groupId);

                        // create ONE class-segment for the new class
                        //TimeTablingClassSegment cs = new TimeTablingClassSegment();

                    }
                }
            }
            planClasses = planGeneralClassRepo.findAllBySemester(semester);
            return planClasses;
        } catch (IOException e) {
            log.error(e.getMessage());
            return new ArrayList<>();
        }
    }

    @Transactional
    public Map<String, Object> saveTimeTablingClasses(MultipartFile file, String semester, Long versionId) {
        try {
            // Delete existing classes and segments for this semester if necessary
            // Note: You may want to add a confirmation step before deleting data in production
            // timeTablingClassRepo.deleteAllBySemester(semester);
            // timeTablingClassSegmentRepo.deleteAllByVersionId(versionId);
            
            Map<String, Object> result = GeneralExcelHelper
                    .saveTimeTablingClassAndSegmentsFromExcel(file.getInputStream(), semester, 
                                                             timeTablingClassRepo, timeTablingClassSegmentRepo);
            
            if (result == null) {
                return new HashMap<>();
            }
            
            // Update version ID for all segments
            if (result.containsKey("segments")) {
                @SuppressWarnings("unchecked")
                Map<Long, List<TimeTablingClassSegment>> segmentsMap = 
                    (Map<Long, List<TimeTablingClassSegment>>)result.get("segments");
                
                for (List<TimeTablingClassSegment> segments : segmentsMap.values()) {
                    for (TimeTablingClassSegment segment : segments) {
                        segment.setVersionId(versionId);
                        timeTablingClassSegmentRepo.save(segment);
                    }
                }
            }
            
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Failed to save timetabling classes from Excel: " + e.getMessage());
            return new HashMap<>();
        }
    }


}