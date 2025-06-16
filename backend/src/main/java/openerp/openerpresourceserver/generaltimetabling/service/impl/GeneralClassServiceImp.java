package openerp.openerpresourceserver.generaltimetabling.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import openerp.openerpresourceserver.generaltimetabling.algorithms.ClassSegmentPartitionConfigForSummerSemester;
import openerp.openerpresourceserver.generaltimetabling.algorithms.Util;
import openerp.openerpresourceserver.generaltimetabling.algorithms.V2ClassScheduler;
import openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata.ConnectedComponentRoomReservationSolver;
import openerp.openerpresourceserver.generaltimetabling.exception.ConflictScheduleException;
import openerp.openerpresourceserver.generaltimetabling.exception.InvalidFieldException;
import openerp.openerpresourceserver.generaltimetabling.exception.MinimumTimeSlotPerClassException;
import openerp.openerpresourceserver.generaltimetabling.exception.NotFoundException;
import openerp.openerpresourceserver.generaltimetabling.helper.ClassTimeComparator;
import openerp.openerpresourceserver.generaltimetabling.helper.LearningWeekExtractor;
import openerp.openerpresourceserver.generaltimetabling.mapper.RoomOccupationMapper;
import openerp.openerpresourceserver.generaltimetabling.model.Constant;
import openerp.openerpresourceserver.generaltimetabling.model.dto.CreateClassSegmentRequest;
import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelResponseTimeTablingClass;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.GeneralClassDto;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.ModelInputComputeClassCluster;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.general.UpdateGeneralClassRequest;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.general.UpdateGeneralClassScheduleRequest;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.general.V2UpdateClassScheduleRequest;
import openerp.openerpresourceserver.generaltimetabling.model.entity.*;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.*;
import openerp.openerpresourceserver.generaltimetabling.model.entity.occupation.RoomOccupation;
import openerp.openerpresourceserver.generaltimetabling.model.input.ModelInputAdvancedFilter;
import openerp.openerpresourceserver.generaltimetabling.model.response.ModelResponseGeneralClass;
import openerp.openerpresourceserver.generaltimetabling.repo.*;
import openerp.openerpresourceserver.generaltimetabling.service.GeneralClassService;
import openerp.openerpresourceserver.generaltimetabling.service.TimeTablingClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * GeneralClassOpenedServiceImp
 */
@AllArgsConstructor
@Service
@Log4j2
@Slf4j
public class GeneralClassServiceImp implements GeneralClassService {

    private GeneralClassRepository gcoRepo;

    private GroupRepo groupRepo;

    private RoomOccupationRepo roomOccupationRepo;

    private RoomReservationRepo roomReservationRepo;

    private ClassroomRepo classroomRepo;

    private TimeTablingClassService timeTablingClassService;

    @Autowired
    private TimeTablingClassRepo timeTablingClassRepo;
    @Autowired
    private TimeTablingClassSegmentRepo timeTablingClassSegmentRepo;
    @Autowired
    private TimeTablingConfigParamsRepo timeTablingConfigParamsRepo;

    @Autowired
    private TimeTablingRoomRepo roomRepo;

    @Autowired
    private GroupRoomPriorityRepo groupRoomPriorityRepo;

    @Autowired
    private TimeTablingCourseRepo timeTablingCourseRepo;

    @Autowired
    private ClassGroupRepo classGroupRepo;

    @Autowired
    private ClusterRepo clusterRepo;

    @Autowired
    private ClusterClassRepo clusterClassRepo;

    @Autowired
    private PlanGeneralClassRepo planGeneralClassRepo;

    @Autowired
    private TimeTablingVersionRepo timeTablingVersionRepo;

    @Override
    public ModelResponseGeneralClass getClassDetailWithSubClasses(Long classId) {
        GeneralClass gc = gcoRepo.findById(classId).orElse(null);
        if(gc == null)
            return null;
        List<GeneralClass> subClasses = gcoRepo.findAllByParentClassId(gc.getId());
        ModelResponseGeneralClass res = new ModelResponseGeneralClass(gc);
        List<ModelResponseGeneralClass> resSubClass = new ArrayList<>();
        for(GeneralClass gci: subClasses){
            ModelResponseGeneralClass aSubClass = new ModelResponseGeneralClass(gci);
            resSubClass.add(aSubClass);
        }
        res.setSubClasses(resSubClass);
        return res;
    }

    @Override
    public List<GeneralClassDto> getGeneralClassDtos(String semester, Long groupId) {
        List<GeneralClass> generalClasses = (groupId == null)
                ? gcoRepo.findAllBySemester(semester)
                : gcoRepo.findAllBySemesterAndGroupId(semester, groupId);

        List<Long> classIds = generalClasses.stream().map(GeneralClass::getId).toList();

        List<ClassGroup> classGroups = classGroupRepo.findAllByClassIdIn(classIds);

        List<Long> groupIds = classGroups.stream()
                .map(ClassGroup::getGroupId)
                .distinct()
                .toList();

        Map<Long, String> groupNameMap = groupRepo.findAllById(groupIds)
                .stream()
                .collect(Collectors.toMap(Group::getId, Group::getGroupName));

        Map<Long, List<String>> classGroupMap = classGroups.stream()
                .collect(Collectors.groupingBy(
                        ClassGroup::getClassId,
                        Collectors.mapping(cg -> groupNameMap.getOrDefault(cg.getGroupId(), "Unknown"), Collectors.toList())
                ));

        return generalClasses.stream()
                .map(gc -> GeneralClassDto.builder()
                        .id(gc.getId())
                        .quantity(gc.getQuantity())
                        .quantityMax(gc.getQuantityMax())
                        .moduleCode(gc.getModuleCode())
                        .moduleName(gc.getModuleName())
                        .classType(gc.getClassType())
                        .classCode(gc.getClassCode())
                        .semester(gc.getSemester())
                        .studyClass(gc.getStudyClass())
                        .mass(gc.getMass())
                        .state(gc.getState())
                        .crew(gc.getCrew())
                        .openBatch(gc.getOpenBatch())
                        .course(gc.getCourse())
                        .refClassId(gc.getRefClassId())
                        .parentClassId(gc.getParentClassId())
                        .duration(gc.getDuration())
                        .groupName(gc.getGroupName())
                        .listGroupName(classGroupMap.getOrDefault(gc.getId(), List.of()))
                        .timeSlots(gc.getTimeSlots())
                        .learningWeeks(gc.getLearningWeeks())
                        .foreignLecturer(gc.getForeignLecturer())
                        .build()
                ).toList();
    }

    @Override
    public List<GeneralClassDto> getSubClasses(Long parentClassId){
        String parentId = String.valueOf(parentClassId);
        List<GeneralClass> subClasses = gcoRepo.findSubClassesByParentClassId(parentId);

        if (subClasses.isEmpty()) {
            System.out.println("Không tìm thấy subclass nào với parentClassId = " + parentClassId);
        }

        return subClasses.stream()
                .map(gc -> GeneralClassDto.builder()
                        .id(gc.getId())
                        .quantity(gc.getQuantity())
                        .quantityMax(gc.getQuantityMax())
                        .moduleCode(gc.getModuleCode())
                        .moduleName(gc.getModuleName())
                        .classType(gc.getClassType())
                        .classCode(gc.getClassCode())
                        .semester(gc.getSemester())
                        .studyClass(gc.getStudyClass())
                        .mass(gc.getMass())
                        .state(gc.getState())
                        .crew(gc.getCrew())
                        .openBatch(gc.getOpenBatch())
                        .course(gc.getCourse())
                        .refClassId(gc.getRefClassId())
                        .parentClassId(gc.getParentClassId())
                        .duration(gc.getDuration())
                        .groupName(gc.getGroupName())
                        .timeSlots(gc.getTimeSlots())
                        .learningWeeks(gc.getLearningWeeks())
                        .foreignLecturer(gc.getForeignLecturer())
                        .build()
                ).toList();
    }

    @Override
    public void deleteAllGeneralClasses() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteAllGeneralClasses'");
    }

    @Transactional
    @Override
    public GeneralClass updateGeneralClassSchedule(String semester, UpdateGeneralClassScheduleRequest request) {
        GeneralClass gClassOpened = gcoRepo.findById(Long.parseLong(request.getGeneralClassId())).orElse(null);
        List<GeneralClass> generalClassList = gcoRepo.findAllBySemester(semester);
        RoomReservation rr = roomReservationRepo.findById(request.getRoomReservationId()).orElse(null);
        if (gClassOpened == null || rr == null) throw new NotFoundException("Không tìm thấy lớp hoặc lịch học!");
        if (rr.getStartTime() == null || rr.getEndTime() == null || rr.getWeekday() == null || rr.getRoom() == null) {
            switch (request.getField()) {
                case "startTime":
                    rr.setStartTime(Integer.parseInt(request.getValue()));
                    if (rr.getEndTime() != null && rr.getStartTime() > rr.getEndTime())
                        throw new ConflictScheduleException("Thời gian bắt đầu không thể lớn hơn thời gian kết thúc!");
                    break;
                case "room":
                    rr.setRoom(request.getValue());
                    break;
                case "weekday":
                    rr.setWeekday(request.getValue().equals("Chủ nhật") ? 8 : Integer.parseInt(request.getValue()));
                    break;
                case "endTime":
                    rr.setEndTime(Integer.parseInt(request.getValue()));
                    if (rr.getStartTime() != null && rr.getStartTime() > rr.getEndTime())
                        throw new ConflictScheduleException("Thời gian bắt đầu không thể lớn hơn thời gian kết thúc!");
                    break;
                default:
                    break;
            }
            if (rr.getStartTime() != null && rr.getEndTime() != null && rr.getWeekday() != null && rr.getRoom() != null) {
                if (!ClassTimeComparator.isClassConflict(rr, gClassOpened, generalClassList)) {
                    List<Integer> weeks = LearningWeekExtractor.extractArray(gClassOpened.getLearningWeeks());
                    List<RoomOccupation> roomOccupationList = new ArrayList<>();
                    for (Integer week : weeks) {
                        roomOccupationList.add(new RoomOccupation(
                                rr.getRoom(),
                                gClassOpened.getClassCode(),
                                rr.getStartTime(),
                                rr.getEndTime(),
                                gClassOpened.getCrew(),
                                rr.getWeekday(),
                                week,
                                "study",
                                gClassOpened.getSemester()));
                    }
                    roomOccupationRepo.saveAll(roomOccupationList);
                }
            }
            gcoRepo.save(gClassOpened);
        } else {
            System.out.println(rr);
            List<RoomOccupation> roomOccupationList = roomOccupationRepo.findAllBySemesterAndClassCodeAndDayIndexAndStartPeriodAndEndPeriodAndClassRoom(
                    gClassOpened.getSemester(),
                    gClassOpened.getClassCode(),
                    rr.getWeekday(),
                    rr.getStartTime(),
                    rr.getEndTime(),
                    rr.getRoom());
            switch (request.getField()) {
                case "startTime":
                    rr.setStartTime(Integer.parseInt(request.getValue()));
                    if (rr.getEndTime() != null && rr.getStartTime() > rr.getEndTime())
                        throw new ConflictScheduleException("Thời gian bắt đầu không thể lớn hơn thời gian kết thúc!");
                    if (!ClassTimeComparator.isClassConflict(rr, gClassOpened, generalClassList)) {
                        roomOccupationList.forEach(ro -> {
                            ro.setStartPeriod(rr.getStartTime());
                        });
                    }
                    break;
                case "room":
                    rr.setRoom(request.getValue());
                    if (!ClassTimeComparator.isClassConflict(rr, gClassOpened, generalClassList)) {
                        rr.setRoom(request.getValue());
                        roomOccupationList.forEach(ro -> {
                            ro.setClassRoom(rr.getRoom());
                        });
                    }
                    break;
                case "weekday":
                    rr.setWeekday(request.getValue().equals("Chủ nhật") ? 8 : Integer.parseInt(request.getValue()));
                    if (!ClassTimeComparator.isClassConflict(rr, gClassOpened, generalClassList)) {
                        roomOccupationList.forEach(ro -> {
                            ro.setDayIndex(rr.getWeekday());
                        });
                    }
                    break;
                case "endTime":
                    rr.setEndTime(Integer.parseInt(request.getValue()));
                    if (rr.getStartTime() != null && rr.getStartTime() > rr.getEndTime())
                        throw new ConflictScheduleException("Thời gian bắt đầu không thể lớn hơn thời gian kết thúc!");
                    if (!ClassTimeComparator.isClassConflict(rr, gClassOpened, generalClassList)) {
                        roomOccupationList.forEach(ro -> {
                            ro.setEndPeriod(rr.getEndTime());
                        });
                    }
                    break;
                default:
                    gcoRepo.save(gClassOpened);
                    roomOccupationRepo.saveAll(roomOccupationList);
                    break;
            }
        }
        return gClassOpened;
    }

    @Transactional
    @Override
    public GeneralClass updateGeneralClass(UpdateGeneralClassRequest request) {
        GeneralClass gClass = gcoRepo.findById(request.getGeneralClass().getId()).orElseThrow(()->new NotFoundException("Không tìm thấy lớp!"));
        if(!gClass.getCrew().equals(request.getGeneralClass().getCrew())) {
            List<RoomOccupation> roomOccupationList = roomOccupationRepo.findAllBySemesterAndClassCodeAndCrew(
                    gClass.getSemester(),
                    gClass.getClassCode(),
                    gClass.getCrew());
            roomOccupationList.forEach(ro->{ro.setCrew(request.getGeneralClass().getCrew());});
            roomOccupationRepo.saveAll(roomOccupationList);
        }
        System.out.println(request.getGeneralClass().getLearningWeeks());
        gClass.setInfo(request.getGeneralClass());
        return gcoRepo.save(gClass);
    }

    @Transactional
    @Override
    public List<GeneralClass> addClassesToGroup(List<Long> ids, String groupName) throws Exception {
        Long groupId = null;

        List<Group> existingGroups = groupRepo.getAllByGroupName(groupName);
        if (existingGroups.isEmpty()) {
            Group group = new Group();
            group.setGroupName(groupName);
            groupRepo.save(group);
            groupId = group.getId();
        } else {
            groupId = existingGroups.get(0).getId();
        }

        List<GeneralClass> generalClassList = new ArrayList<>();
        for (Long id : ids) {
            GeneralClass generalClass = gcoRepo.findById(id).orElse(null);
            if (generalClass == null) {
                System.err.println("Class not exist with id =" + id);
                continue;
            }

            generalClass.setGroupName(groupName);
            generalClassList.add(generalClass);

            ClassGroup classGroup = new ClassGroup(id, groupId);
            classGroupRepo.save(classGroup);
        }

        // Lưu tất cả lớp đã được cập nhật
        gcoRepo.saveAll(generalClassList);

        // Trả về danh sách các lớp đã được cập nhật
        return gcoRepo.findAll();
    }


    @Transactional
    @Override
    public void deleteClassesBySemester(String semester) {
        gcoRepo.deleteBySemester(semester);
        roomOccupationRepo.deleteBySemester(semester);
    }

    @Override
    @Transactional
    public void deleteClassesByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("Danh sách ID không được rỗng.");
        }

        List<GeneralClass> classesToDelete = gcoRepo.findAllById(ids);

        if (classesToDelete.isEmpty()) {
            throw new RuntimeException("Không tìm thấy lớp học nào để xóa với danh sách ID đã cung cấp.");
        }

        List<Long> allIdsToDelete = new ArrayList<>(ids);

        List<String> parentClassIds = classesToDelete.stream()
                .filter(generalClass -> generalClass.getParentClassId() != null)
                .map(GeneralClass::getClassCode)
                .collect(Collectors.toList());

        if (!parentClassIds.isEmpty()) {
            List<GeneralClass> subClasses = gcoRepo.findSubClassesByParentClassIds(parentClassIds);
            allIdsToDelete.addAll(subClasses.stream()
                    .map(GeneralClass::getId)
                    .collect(Collectors.toList()));
        }

        roomReservationRepo.deleteByGeneralClassIds(allIdsToDelete);
        gcoRepo.deleteByIds(allIdsToDelete);
    }


    @Transactional
    @Override
    public List<GeneralClass> resetSchedule(List<String> ids, String semester) {
        List<GeneralClass> generalClassList = gcoRepo.findAllBySemester(semester);
        if (generalClassList.isEmpty()) {
            throw new NotFoundException("Không tìm thấy lớp, hãy kiểm tra lại danh sách lớp!");
        } else {
            List<GeneralClass> filteredGeneralClassList = new ArrayList<>();
            for (GeneralClass gClass : generalClassList) {
                for (String idString : ids) {
                    log.info("resetSchedule, idString = " + idString);
                    int gId = 0; int timeSlotIndex = 0;
                    if(idString.contains("-")) {
                        gId = Integer.parseInt(idString.split("-")[0]);
                        timeSlotIndex = Integer.parseInt(idString.split("-")[1]) - 1;
                    }else{
                        gId = Integer.parseInt(idString);
                        timeSlotIndex = 0;
                    }
                    log.info("resetSchedule, gId = " + gId + " timeSlotIndex = " + timeSlotIndex + " gClassId = " + gClass.getId());
                    if (gId == gClass.getId()) {
                        RoomReservation timeSlot = gClass.getTimeSlots().get(timeSlotIndex);
                        log.info("resetSchedule, consider classCode = " + gClass.getClassCode() + " startTime = " + timeSlot.getStartTime() + " endTime = " + timeSlot.getEndTime() + " weekDay = " + timeSlot.getWeekday() + " room = " + timeSlot.getRoom());

                        if (timeSlot.isScheduleNotNull()) {
                            log.info("resetSchedule, delete roomOccupation with classCode = " + gClass.getClassCode() + " startTime = " + timeSlot.getStartTime() + " endTime = " + timeSlot.getEndTime() + " weekDay = " + timeSlot.getWeekday() + " room = " + timeSlot.getRoom());
                            roomOccupationRepo.deleteAllByClassCodeAndStartPeriodAndEndPeriodAndDayIndexAndClassRoom(
                                    gClass.getClassCode(),
                                    timeSlot.getStartTime(),
                                    timeSlot.getEndTime(),
                                    timeSlot.getWeekday(),
                                    timeSlot.getRoom()
                            );
                        }

                        // Đặt lại các giá trị về null
                        timeSlot.setWeekday(null);
                        timeSlot.setStartTime(null);
                        timeSlot.setEndTime(null);
                        timeSlot.setRoom(null);
                        log.info("resetSchedule, class-segment " + timeSlot.getId() + " -> set NULL");

                        // Thêm lớp vào danh sách để lưu lại
                        if (!filteredGeneralClassList.contains(gClass)) {
                            filteredGeneralClassList.add(gClass);
                        }
                    }
                }
            }
            log.info("resetSchedule, filterGeneralClassList = " + filteredGeneralClassList.size());

            gcoRepo.saveAll(filteredGeneralClassList);

            ids.forEach(System.out::println);
            filteredGeneralClassList.forEach(System.out::println);

            return filteredGeneralClassList;
        }
    }


    @Transactional
    @Override
    public List<GeneralClass> autoScheduleRoom(String semester, String groupName, int timeLimit) {
        log.info("autoScheduleRoom start...");
        List<GeneralClass> classes = gcoRepo.findAllBySemesterAndGroupName(semester, groupName);
        if (classes == null) throw new NotFoundException("Không tìm thấy lớp");
        Group group = groupRepo.findByGroupName(groupName).orElse(null);
        if(group == null) throw new NotFoundException("Nhóm không tồn tại!");
        List<Classroom> rooms = classroomRepo
                .getClassRoomByBuildingIn(Arrays.stream(group.getPriorityBuilding().split(",")).toList());
        List<RoomOccupation> roomOccupations = roomOccupationRepo.findAllBySemester(semester);
        List<GeneralClass> updatedClasses = V2ClassScheduler.autoScheduleRoom(classes, rooms, timeLimit, roomOccupations);
        List<String> classCodes = updatedClasses.stream().map(GeneralClass::getClassCode).toList();
        List<RoomOccupation> newRoomOccupations = updatedClasses.stream().map(RoomOccupationMapper::mapFromGeneralClass).flatMap(Collection::stream).toList();
        roomOccupationRepo.deleteAllByClassCodeIn(classCodes);
        roomOccupationRepo.saveAll(newRoomOccupations);
        gcoRepo.saveAll(updatedClasses);
        return updatedClasses;
    }

    @Transactional
    @Override
    public List<GeneralClass> autoScheduleGroup(String semester, String groupName, int timeLimit) {
        /*
        log.debug("autoSchedule START....");
        List<GeneralClass> foundClasses = gcoRepo.findAllBySemesterAndGroupName(semester, groupName);
        //List<GeneralClass> autoScheduleClasses = V2ClassScheduler.autoScheduleTimeSlot(foundClasses, timeLimit);
        V2ClassScheduler optimizer = new V2ClassScheduler();
        //List<GeneralClass> autoScheduleClasses = optimizer.autoScheduleTimeSlotRoom(foundClasses,timeLimit);

        //List<TimeTablingRoom> rooms = roomRepo.findAll();

        gcoRepo.saveAll(autoScheduleClasses);
        roomOccupationRepo.deleteAllByClassCodeIn(foundClasses.stream().map(GeneralClass::getClassCode).toList());
        return autoScheduleClasses;
        */
        return null;
    }

    private void synchronizeCourses(){
        //List<GeneralClass> cls = gcoRepo.findAll();
        List<TimeTablingClass> CLS = timeTablingClassRepo.findAll();
        Set<String> courseCodes = new HashSet<>();
        Map<String, String> mCourseCode2Name = new HashMap<>();
        //for(GeneralClass gc: cls){
        for(TimeTablingClass gc: CLS){
            String courseCode = gc.getModuleCode();
            courseCodes.add(courseCode);
            mCourseCode2Name.put(courseCode,gc.getModuleName());
        }
        for(String courseCode: courseCodes){
            TimeTablingCourse course = timeTablingCourseRepo.findById(courseCode).orElse(null);
            if(course == null) {
                String courseName = mCourseCode2Name.get(courseCode);
                course = new TimeTablingCourse();
                course.setId(courseCode);
                course.setName(courseName);
                course.setMaxTeacherInCharge(50);
                timeTablingCourseRepo.save(course);
                log.info("synchronizeCourses save " + courseCode + "," + courseName);
            }
        }
    }
    /*
    @Transactional
    @Override
    public List<GeneralClass> autoScheduleTimeSlotRoom(String semester, List<Long> classIds, int timeLimit, String algorithm, int maxDaySchedule) {
        synchronizeCourses();
        log.info("autoScheduleTimeSlotRoom START....maxDaySchedule = " + maxDaySchedule + " classIds to be scheduled = " + classIds.size());
        List<TimeTablingConfigParams> params = timeTablingConfigParamsRepo.findAll();

        for(TimeTablingConfigParams p: params){
            if(p.getId().equals(TimeTablingConfigParams.MAX_DAY_SCHEDULED)){
                p.setValue(maxDaySchedule);
            }else{

            }
        }


        //List<GeneralClass> foundClasses = gcoRepo.findAllBySemester(semester);
        List<GeneralClass> foundClasses = gcoRepo.findAllByIdIn(classIds);
        log.info("autoScheduleTimeSlotRoom, nb general classes = " + foundClasses.size());
        List<GeneralClass> allClassesOfSemester = gcoRepo.findAllBySemester(semester);
        List<Long> ids = allClassesOfSemester.stream().map(gc -> gc.getId()).toList();
        //List<GeneralClass> autoScheduleClasses = V2ClassScheduler.autoScheduleTimeSlot(foundClasses, timeLimit);
        List<RoomReservation> roomReservationsOfSemester = roomReservationRepo.findAllByGeneralClassIn(allClassesOfSemester);
        //for(RoomReservation rr: roomReservationsOfSemester){
        //    log.info("autoScheduleTimeSlotRoom, get roomReservation " + rr.getId() + "\t" + rr.getRoom() + "\t" + rr.getStartTime() + "\t" + rr.getEndTime());
        //}
        Map<String, List<RoomReservation>> mId2RoomReservations = new HashMap<>();
        for(RoomReservation rr: roomReservationsOfSemester){
            if(rr.getRoom()!=null) {
                if(mId2RoomReservations.get(rr.getRoom())==null){
                    mId2RoomReservations.put(rr.getRoom(),new ArrayList<>());
                }
                mId2RoomReservations.get(rr.getRoom()).add(rr);
            }

        }
        V2ClassScheduler optimizer = new V2ClassScheduler(params);
        List<Classroom> rooms = classroomRepo.findAll();
        List<TimeTablingCourse> courses = timeTablingCourseRepo.findAll();
        List<Group> groups = groupRepo.findAll();
        List<ClassGroup> classGroups = classGroupRepo.findAllByClassIdIn(classIds);
        //List<GeneralClass> autoScheduleClasses = optimizer.autoScheduleTimeSlotRoom(foundClasses,rooms,mId2RoomReservations,courses, groups,classGroups,timeLimit,algorithm,params);
        List<ModelResponseTimeTablingClass> autoScheduleClasses = optimizer.autoScheduleTimeSlotRoomNew(foundClasses,rooms,mId2RoomReservations,courses, groups,classGroups,timeLimit,algorithm,params);


        gcoRepo.saveAll(autoScheduleClasses);
        roomOccupationRepo.deleteAllByClassCodeIn(foundClasses.stream().map(GeneralClass::getClassCode).toList());

        //List<String> classCodes = autoScheduleClasses.stream().map(GeneralClass::getClassCode).toList();
        //List<String> classCodes = autoScheduleClasses.stream().map(GeneralClass::getClassCode).toList();
        //List<RoomOccupation> newRoomOccupations = autoScheduleClasses.stream().map(RoomOccupationMapper::mapFromGeneralClass).flatMap(Collection::stream).toList();
        //List<RoomOccupation> newRoomOccupations = autoScheduleClasses.stream().map(RoomOccupationMapper::mapFromGeneralClassV2).flatMap(Collection::stream).toList();
        log.info("autoScheduleTimeSlotRoom, resulting schedule classes size = " + autoScheduleClasses.size());
        List<RoomOccupation> newRoomOccupations = new ArrayList<>();
        for(GeneralClass gc: autoScheduleClasses){
            List<RoomOccupation> RO = RoomOccupationMapper.mapFromGeneralClassV2(gc);
            for(RoomOccupation ro: RO) newRoomOccupations.add(ro);
        }
        log.info("autoScheduleTimeSlotRoom, collect room occupations size = " + newRoomOccupations.size());
        //roomOccupationRepo.deleteAllByClassCodeIn(classCodes);

        //roomOccupationRepo.saveAll(newRoomOccupations);
        for(RoomOccupation ro: newRoomOccupations){
            ro = roomOccupationRepo.save(ro);
            //log.info("autoScheduleTimeSlotRoom, saved a new room occupation id = " + ro.getId() + ", room " + ro.getClassRoom() + " semester " + ro.getSemester());
        }
        //gcoRepo.saveAll(updatedClasses);

        return autoScheduleClasses;
    }
    */
    @Transactional
    @Override
    public List<ModelResponseTimeTablingClass> autoScheduleTimeSlotRoom(String semester, List<Long> classIds, int timeLimit, String algorithm, int maxDaySchedule, Long versionId) {
        //synchronizeCourses();
        log.info("autoScheduleTimeSlotRoom START....maxDaySchedule = " + maxDaySchedule + " classIds to be scheduled = " + classIds.size());
        List<TimeTablingConfigParams> params = timeTablingConfigParamsRepo.findAll();
        TimeTablingTimeTableVersion ver = timeTablingVersionRepo.findById(versionId).orElse(null);
        if(ver!=null){
            //Constant.slotPerCrew = ver.getNumberSlotsPerSession();
        }
        String PARAM_ROOM_PRIORITY = "Y";
        for(TimeTablingConfigParams p: params){
            if(p.getId().equals(TimeTablingConfigParams.MAX_DAY_SCHEDULED)){
                p.setValue(maxDaySchedule + "");
            }else if(p.getId().equals(TimeTablingConfigParams.USED_ROOM_PRIORITY)){
                PARAM_ROOM_PRIORITY = p.getValue();
            }
        }

        List<ModelResponseTimeTablingClass> foundClasses = timeTablingClassService.getTimeTablingClassDtos(classIds,versionId);

        log.info("autoScheduleTimeSlotRoom, nb general classes = " + foundClasses.size());
        List<ModelResponseTimeTablingClass> allClassesOfSemester = timeTablingClassService.findAllBySemester(semester);

        List<ModelResponseTimeTablingClass> selectedClassesOfSemester = new ArrayList<>();
        for(ModelResponseTimeTablingClass cls : foundClasses){
            //if(cls.getQuantityMax() >= 150)
                selectedClassesOfSemester.add(cls);
        }
        foundClasses = selectedClassesOfSemester;

        List<Long> ids = allClassesOfSemester.stream().map(gc -> gc.getId()).toList();
        //List<GeneralClass> autoScheduleClasses = V2ClassScheduler.autoScheduleTimeSlot(foundClasses, timeLimit);

        //List<TimeTablingClassSegment> classSegmentsOfSemester = timeTablingClassSegmentRepo.findAllByClassIdIn(ids);
        List<TimeTablingClassSegment> classSegmentsOfSemester = timeTablingClassSegmentRepo.findAllByVersionIdAndClassIdIn(versionId,ids);

        //List<RoomReservation> roomReservationsOfSemester = roomReservationRepo.findAllByGeneralClassIn(allClassesOfSemester);

        //for(RoomReservation rr: roomReservationsOfSemester){
        //    log.info("autoScheduleTimeSlotRoom, get roomReservation " + rr.getId() + "\t" + rr.getRoom() + "\t" + rr.getStartTime() + "\t" + rr.getEndTime());
        //}
        Map<String, List<TimeTablingClassSegment>> mId2RoomReservations = new HashMap<>();
        for(TimeTablingClassSegment cs: classSegmentsOfSemester){
            if(cs.getRoom()!=null) {
                if(mId2RoomReservations.get(cs.getRoom())==null){
                    mId2RoomReservations.put(cs.getRoom(),new ArrayList<>());
                }
                mId2RoomReservations.get(cs.getRoom()).add(cs);
            }
        }
        V2ClassScheduler optimizer = new V2ClassScheduler(params, ver);
        //List<Classroom> rooms = classroomRepo.findAll();
        List<Classroom> rooms = classroomRepo.findAllByStatus("ACTIVE");


        List<TimeTablingCourse> courses = timeTablingCourseRepo.findAll();
        List<Group> groups = groupRepo.findAll();
        List<ClassGroup> classGroups = classGroupRepo.findAllByClassIdIn(classIds);
        //List<GeneralClass> autoScheduleClasses = optimizer.autoScheduleTimeSlotRoom(foundClasses,rooms,mId2RoomReservations,courses, groups,classGroups,timeLimit,algorithm,params);
        List<ModelResponseTimeTablingClass> autoScheduleClasses = optimizer.autoScheduleTimeSlotRoomNew(foundClasses,allClassesOfSemester,rooms,mId2RoomReservations,courses, groups,classGroups,timeLimit,algorithm,params);

        for(ModelResponseTimeTablingClass c: autoScheduleClasses){
            for(TimeTablingClassSegment cs: c.getTimeSlots()){
                timeTablingClassSegmentRepo.save(cs);
            }
        }
        return foundClasses;
    }



    @Override
    public List<GeneralClass> autoSchedule(String semester, int timeLimit) {

        return null;// temporary not used
        /*
        log.info("autoSchedule START....");
        List<GeneralClass> foundClasses = gcoRepo.findAllBySemester(semester);
        //List<GeneralClass> autoScheduleClasses = V2ClassScheduler.autoScheduleTimeSlot(foundClasses, timeLimit);
        V2ClassScheduler optimizer = new V2ClassScheduler();
        List<Classroom> rooms = classroomRepo.findAll();
        List<TimeTablingCourse> courses = timeTablingCourseRepo.findAll();
        List<Group> groups = groupRepo.findAll();
        List<Long> classIds = new ArrayList<>();
        for(GeneralClass gc: foundClasses) classIds.add(gc.getId());
        List<ClassGroup> classGroups = classGroupRepo.findAllByClassIdIn(classIds);

        List<GeneralClass> autoScheduleClasses = optimizer.autoScheduleTimeSlotRoom(foundClasses,rooms,courses, groups, classGroups, timeLimit,"");


        gcoRepo.saveAll(autoScheduleClasses);
        roomOccupationRepo.deleteAllByClassCodeIn(foundClasses.stream().map(GeneralClass::getClassCode).toList());

        List<String> classCodes = autoScheduleClasses.stream().map(GeneralClass::getClassCode).toList();
        //List<RoomOccupation> newRoomOccupations = autoScheduleClasses.stream().map(RoomOccupationMapper::mapFromGeneralClass).flatMap(Collection::stream).toList();
        //List<RoomOccupation> newRoomOccupations = autoScheduleClasses.stream().map(RoomOccupationMapper::mapFromGeneralClassV2).flatMap(Collection::stream).toList();
        List<RoomOccupation> newRoomOccupations = new ArrayList<>();
        for(GeneralClass gc: autoScheduleClasses){
            List<RoomOccupation> RO = RoomOccupationMapper.mapFromGeneralClassV2(gc);
            for(RoomOccupation ro: RO) newRoomOccupations.add(ro);
        }
        //roomOccupationRepo.deleteAllByClassCodeIn(classCodes);
        roomOccupationRepo.saveAll(newRoomOccupations);
        //gcoRepo.saveAll(updatedClasses);

        return autoScheduleClasses;

         */
    }

    @Override
    public List<GeneralClass> v2UpdateClassSchedule(String semester, List<V2UpdateClassScheduleRequest> request) {
        /*Find reference*/
        List<GeneralClass> classes = gcoRepo.findAllBySemester(semester);
        List<RoomReservation> roomReservations = classes.stream().map(GeneralClass::getTimeSlots).flatMap(Collection::stream).toList();

        HashMap<Long, RoomReservation> roomReservationMap = new HashMap<>();
        List<Long> requestIds = request.stream().map(V2UpdateClassScheduleRequest::getRoomReservationId).toList();
        List<RoomOccupation> roomOccupations = new ArrayList<>();

        roomReservations.forEach(roomReservation -> {
            if (requestIds.contains(roomReservation.getId())) {
                roomReservationMap.put(roomReservation.getId(), roomReservation);
            }
        });
        /*Set info*/
        request.forEach(updateRequest -> {
            RoomReservation updateRoomReservation = roomReservationMap.get(updateRequest.getRoomReservationId());

            if ((updateRequest.getStartTime() != null && updateRequest.getEndTime() != null ) && updateRequest.getStartTime() > updateRequest.getEndTime()) throw new InvalidFieldException("Tiết BĐ không thể lớn hơn tiết KT");

            int duration = updateRequest.getEndTime() - updateRequest.getStartTime() + 1;
            updateRoomReservation.setDuration(duration);

            List<RoomOccupation> foundRoomOccupations = roomOccupationRepo.findAllBySemesterAndClassCodeAndDayIndexAndStartPeriodAndEndPeriodAndClassRoom(semester,
                    updateRoomReservation.getGeneralClass().getClassCode(),
                    updateRoomReservation.getWeekday(),
                    updateRoomReservation.getStartTime(),
                    updateRoomReservation.getEndTime(),
                    updateRoomReservation.getRoom());


            updateRoomReservation.setStartTime(updateRequest.getStartTime());
            updateRoomReservation.setEndTime(updateRequest.getEndTime());
            updateRoomReservation.setWeekday(updateRequest.getWeekday());
            updateRoomReservation.setRoom(updateRequest.getRoom());


            if (
                /*After schedule is complete*/
                updateRoomReservation.isScheduleNotNull() &&
                /*Before schedule is not complete*/
                foundRoomOccupations.isEmpty()
            ) {
                List<Integer> weeks = LearningWeekExtractor.extractArray(updateRoomReservation.getGeneralClass().getLearningWeeks());
                List<RoomOccupation> roomOccupationList = new ArrayList<>();
                for (Integer week : weeks) {
                    roomOccupationList.add(new RoomOccupation(
                            updateRequest.getRoom(),
                            updateRoomReservation.getGeneralClass().getClassCode(),
                            updateRoomReservation.getStartTime(),
                            updateRoomReservation.getEndTime(),
                            updateRoomReservation.getGeneralClass().getCrew(),
                            updateRoomReservation.getWeekday(),
                            week,
                            "study",
                            semester));
                    System.out.print(roomOccupationList);
                }
                roomOccupations.addAll(roomOccupationList);
            } else if (
                /*After schedule is complete*/
                updateRoomReservation.isScheduleNotNull() &&
                /*Before schedule is complete*/
                !foundRoomOccupations.isEmpty()
            ) {
                foundRoomOccupations.forEach(roomOccupation -> {
                    roomOccupation.setClassRoom(updateRequest.getRoom());
                    roomOccupation.setStartPeriod(updateRequest.getStartTime());
                    roomOccupation.setEndPeriod(updateRequest.getEndTime());
                    roomOccupation.setDayIndex(updateRequest.getWeekday());
                });
            } else {

            }
            roomOccupations.addAll(foundRoomOccupations);
        });

        try {
            /*Check conflict*/
            for (RoomReservation roomReservation : roomReservationMap.values()) {
                if (roomReservation.isScheduleNotNull()) {
                    ClassTimeComparator.isClassConflict(roomReservation, roomReservation.getGeneralClass(), classes);
                }
            }
        } catch (ConflictScheduleException e) {
            throw new ConflictScheduleException(e.getCustomMessage());
        }

        roomReservationRepo.saveAll(roomReservationMap.values());
        roomOccupationRepo.saveAll(roomOccupations);
        return roomReservationMap.values().stream().map(RoomReservation::getGeneralClass).toList();
    }

    @Transactional
    @Override
    public GeneralClass deleteClassById(Long generalClassId) {
        GeneralClass foundClass = gcoRepo.findById(generalClassId).orElse(null);
        if (foundClass == null) throw new NotFoundException("Không tìm thấy lớp kế hoạch!");
        gcoRepo.deleteById(generalClassId);
        return foundClass;
    }

    @Override
    public GeneralClass addRoomReservation(Long generalClassId, Long parentId, Integer duration) {
        GeneralClass foundGeneralClass = gcoRepo.findById(generalClassId).orElse(null);
        if (foundGeneralClass == null) throw new NotFoundException("Không tìm thấy lớp!");
        RoomReservation parentRoomReservation = roomReservationRepo.findById(parentId).orElse(null);
        if(parentRoomReservation == null) throw new NotFoundException("Không tìm thấy lớp!");
        if(parentRoomReservation.getDuration() <= duration) throw new NotFoundException("Số tiết ca được tạo mới (" + duration + ") phải nhỏ hơn số tiết ca cha (" + parentRoomReservation.getDuration()+ ") !");
        parentRoomReservation.setDuration(parentRoomReservation.getDuration() - duration);

        RoomReservation newRoomReservation = new RoomReservation();
        newRoomReservation.setDuration(duration);
        foundGeneralClass.addTimeSlot(newRoomReservation);
        newRoomReservation.setGeneralClass(foundGeneralClass);
        newRoomReservation.setParentId(parentId);
        newRoomReservation.setCrew(foundGeneralClass.getCrew());
        gcoRepo.save(foundGeneralClass);
        return foundGeneralClass;
    }

    @Transactional
    @Override
    public void deleteRoomReservation(Long generalClassId, Long roomReservationId) {
        GeneralClass foundGeneralClass = gcoRepo.findById(generalClassId)
                .orElseThrow(()->new NotFoundException("Không tìm thấy lớp!"));
        List<RoomReservation> lstRoomReservations = roomReservationRepo.findAllByGeneralClass(foundGeneralClass);

        RoomReservation foundRoomReservation= roomReservationRepo.findById(roomReservationId)
                .orElseThrow(()->new NotFoundException("Không tìm thấy ca học!"));
        if (!foundGeneralClass.getTimeSlots().contains(foundRoomReservation)) {
            throw new NotFoundException("Lớp không tồn tại ca học!");
        }
        if(foundRoomReservation.getParentId() == null)
            throw new NotFoundException("Lớp không tồn tại ca học cha nên không xóa được!");


        if(foundGeneralClass.getTimeSlots().size() == 1) throw new MinimumTimeSlotPerClassException("Lớp cần tối thiểu 1 ca học!");

        int minDuration = Integer.MAX_VALUE;
        RoomReservation sel = null;
        for(RoomReservation r: lstRoomReservations){
            if(r.getId()!= foundRoomReservation.getId() && minDuration > r.getDuration()){
                sel = r; minDuration = r.getDuration();
            }
        }
        sel.setDuration(sel.getDuration() + foundRoomReservation.getDuration());
        sel = roomReservationRepo.save(sel);

        //RoomReservation parentRoomReservation = roomReservationRepo.findById(foundRoomReservation.getParentId()).orElse(null);
        //if(parentRoomReservation == null){
        //    throw new NotFoundException("Lớp không tồn tại ca học cha nên không xóa được !");
        //}
        //parentRoomReservation.setDuration(parentRoomReservation.getDuration() + foundRoomReservation.getDuration());
        //parentRoomReservation = roomReservationRepo.save(parentRoomReservation);

        if (foundRoomReservation.isScheduleNotNull()) {
            List<RoomOccupation> foundRoomOccupations =  roomOccupationRepo.findAllBySemesterAndClassCodeAndDayIndexAndStartPeriodAndEndPeriodAndClassRoom(
                    foundRoomReservation.getGeneralClass().getSemester(),
                    foundGeneralClass.getClassCode(),
                    foundRoomReservation.getWeekday(),
                    foundRoomReservation.getStartTime(),
                    foundRoomReservation.getEndTime(),
                    foundRoomReservation.getRoom()
            );
            roomOccupationRepo.deleteAllById(foundRoomOccupations.stream().map(RoomOccupation::getId).toList());
        }
        foundGeneralClass.getTimeSlots().remove(foundRoomReservation);
        foundRoomReservation.setGeneralClass(null);
        gcoRepo.save(foundGeneralClass);
    }

    @Transactional
    @Override
    public int computeClassCluster(ModelInputComputeClassCluster I) {
        List<GeneralClass> classes = gcoRepo.findAllBySemester(I.getSemester());
        List<Long> classIds = new ArrayList<>();
        for(GeneralClass gc: classes) classIds.add(gc.getId());
        List<ClassGroup> classGroups = classGroupRepo.findAllByClassIdIn(classIds);
        List<Group> groups = groupRepo.findAll();
        Map<Long, Group> mId2Group = new HashMap<>();
        for(Group g: groups) mId2Group.put(g.getId(),g);
        Map<Long, List<Long>> mClassId2GroupIds = new HashMap<>();
        for(ClassGroup cg: classGroups){
            if(mClassId2GroupIds.get(cg.getClassId())==null)
                mClassId2GroupIds.put(cg.getClassId(),new ArrayList<>());
            mClassId2GroupIds.get(cg.getClassId()).add(cg.getGroupId());
        }

        //List<RoomReservation> roomReservations = roomReservationRepo.findAllByGeneralClassIn(classes);
        ConnectedComponentRoomReservationSolver ccSolver = new ConnectedComponentRoomReservationSolver();
        List<List<GeneralClass>> clusters = ccSolver.computeRoomReservationCluster(classes,classGroups);

        List<Cluster> oldClusters = clusterRepo.findAllBySemester(I.getSemester());
        for(Cluster c: oldClusters){
            clusterRepo.delete(c);
        }
        for(List<GeneralClass> cluster: clusters){
            // create a new cluster
            String clusterName = "";
            Set<String> names = new HashSet<>();
            for(GeneralClass gc: cluster){
                Long classId = gc.getId();
                List<Long> gids = mClassId2GroupIds.get(classId);
                for(Long gId: gids){
                    Group g = mId2Group.get(gId);
                    names.add(g.getGroupName());
                }
            }
            int cnt = 0;
            for(String n: names){
                cnt++;
                clusterName = clusterName + n;
                if(cnt < names.size()) clusterName = clusterName + ", ";
            }
            Long nextId = planGeneralClassRepo.getNextReferenceValue();
            Cluster newCluster = new Cluster();
            newCluster.setId(nextId);
            newCluster.setName(clusterName);
            newCluster.setSemester(I.getSemester());
            newCluster = clusterRepo.save(newCluster);

            for(GeneralClass gc: cluster){
                ClusterClass clusterClass = new ClusterClass();
                clusterClass.setClusterId(nextId);
                clusterClass.setClassId(gc.getId());

                clusterClass = clusterClassRepo.save(clusterClass);
            }
        }
        return clusters.size();
    }

    @Override
    public List<GeneralClassDto> getGeneralClassByCluster(Long clusterId) {
        // Find all ClusterClass relationships with the given clusterId
        List<ClusterClass> clusterClasses = clusterClassRepo.findAllByClusterId(clusterId);
        
        if (clusterClasses.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Extract class IDs from the relationships
        List<Long> classIds = clusterClasses.stream()
                .map(ClusterClass::getClassId)
                .collect(Collectors.toList());
        
        // Find all GeneralClass entities with these IDs
        List<GeneralClass> generalClasses = gcoRepo.findAllByIdIn(classIds);
        
        // Find ClassGroup entities for these classes
        List<ClassGroup> classGroups = classGroupRepo.findAllByClassIdIn(classIds);
        
        // Get group information
        List<Long> groupIds = classGroups.stream()
                .map(ClassGroup::getGroupId)
                .distinct()
                .toList();
        
        Map<Long, String> groupNameMap = groupRepo.findAllById(groupIds)
                .stream()
                .collect(Collectors.toMap(Group::getId, Group::getGroupName));
        
        Map<Long, List<String>> classGroupMap = classGroups.stream()
                .collect(Collectors.groupingBy(
                        ClassGroup::getClassId,
                        Collectors.mapping(cg -> groupNameMap.getOrDefault(cg.getGroupId(), "Unknown"), Collectors.toList())
                ));
        
        // Convert GeneralClass entities to GeneralClassDto objects
        return generalClasses.stream()
                .map(gc -> GeneralClassDto.builder()
                        .id(gc.getId())
                        .quantity(gc.getQuantity())
                        .quantityMax(gc.getQuantityMax())
                        .moduleCode(gc.getModuleCode())
                        .moduleName(gc.getModuleName())
                        .classType(gc.getClassType())
                        .classCode(gc.getClassCode())
                        .semester(gc.getSemester())
                        .studyClass(gc.getStudyClass())
                        .mass(gc.getMass())
                        .state(gc.getState())
                        .crew(gc.getCrew())
                        .openBatch(gc.getOpenBatch())
                        .course(gc.getCourse())
                        .refClassId(gc.getRefClassId())
                        .parentClassId(gc.getParentClassId())
                        .duration(gc.getDuration())
                        .groupName(gc.getGroupName())
                        .listGroupName(classGroupMap.getOrDefault(gc.getId(), List.of()))
                        .timeSlots(gc.getTimeSlots())
                        .learningWeeks(gc.getLearningWeeks())
                        .foreignLecturer(gc.getForeignLecturer())
                        .build()
                ).toList();
    }

    @Transactional
    private void createClassSegment(GeneralClass gc, List<Integer> seqSlots){
        log.info("createClassSegments, classId " + gc.getId() + ", crew " + gc.getCrew() + "  course "
                + gc.getModuleCode() + " type " + gc.getClassType() + " slots = " + seqSlots);


        /*
        List<RoomReservation> rr = roomReservationRepo.findAllByGeneralClass(gc);
        //roomReservationRepo.deleteAll(rr);
        for(RoomReservation ri: rr){
            roomReservationRepo.deleteById(ri.getId());
            //roomReservationRepo.flush();
            log.info("createClassSegments, delete room_reservation id = " + ri.getId());
        }
        */
        gc.getTimeSlots().clear();
        for(int s: seqSlots){
            RoomReservation r = new RoomReservation();
            r.setGeneralClass(gc);
            r.setCrew(gc.getCrew());
            r.setDuration(s);
            //r = roomReservationRepo.save(r);
            //gc.getTimeSlots().add(r);
            gc.addTimeSlot(r);
            log.info("createClassSegments, classId " + gc.getId() + ", crew " + gc.getCrew() + "  course "
                    + gc.getModuleCode() + " type " + gc.getClassType() + " slots = " + seqSlots
                    + " add slot " + s + " gc.timeSlots.sz = " + gc.getTimeSlots().size());
        }
        gc = gcoRepo.save(gc);
    }
    private void createClassSegmentsOfASession(List<GeneralClass> LS,
                                            List<List<Integer>> PLT,
                                            List<List<Integer>> PBT,
                                            List<List<Integer>> PLTBT){
        // consider morning classes first
        List<GeneralClass> LLT = new ArrayList<>();
        List<GeneralClass> LBT = new ArrayList<>();
        List<GeneralClass> LLTBT = new ArrayList<>();
        for(GeneralClass gc: LS){
            if(gc.getClassType().equals("LT")) LLT.add(gc);
            else if(gc.getClassType().equals("BT")) LBT.add(gc);
            else if(gc.getClassType().equals("LT+BT")) LLTBT.add(gc);
        }
        // apply round robin
        int idx = 0;

        for(GeneralClass gc: LLT){
            if(PLT != null && PLT.size() > 0){
                List<Integer> seqSlots = PLT.get(idx);
                // create a.size class-segment for class gc
                //
                if(seqSlots.size() >= 1){
                    createClassSegment(gc,seqSlots);
                }
                idx += 1;
                if(idx >= PLT.size()) idx = 0;
            }
        }
        idx = 0;
        for(GeneralClass gc: LBT){
            if(PBT != null && PBT.size() > 0){
                List<Integer> seqSlots = PBT.get(idx);
                // create a.size class-segment for class gc
                //if(seqSlots.size() > 1){
                if(seqSlots.size() >= 1){
                    createClassSegment(gc,seqSlots);
                }
                idx += 1;
                if(idx >= PBT.size()) idx = 0;
            }
        }
        idx = 0;
        for(GeneralClass gc: LLTBT){
            if(PLTBT != null && PLTBT.size() > 0){
                List<Integer> seqSlots = PLTBT.get(idx);
                // create a.size class-segment for class gc
                //if(seqSlots.size() > 1){
                if(seqSlots.size() >= 1){
                    createClassSegment(gc,seqSlots);
                }
                idx += 1;
                if(idx >= PLTBT.size()) idx = 0;
            }
        }
    }


    @Transactional
    @Override
    public List<RoomReservation> createClassSegment(CreateClassSegmentRequest I) {

        //List<GeneralClass> cls = gcoRepo.findAllBySemester(I.getSemester()); NOT work WHY?
        List<GeneralClass> allcls = gcoRepo.findAll();
        List<GeneralClass> cls = new ArrayList<>();
        log.info("createClassSegmen allcls = " + allcls.size());
        for(GeneralClass gc: allcls){
            if(gc.getSemester().equals(I.getSemester()))
                cls.add(gc);
        }

        ClassSegmentPartitionConfigForSummerSemester P = new ClassSegmentPartitionConfigForSummerSemester();
        List<TimeTablingCourse> courses = timeTablingCourseRepo.findAll();

        log.info("createClassSegment, semester = " + I.getSemester() + " number classes = " + cls.size()
        + " number courses = " + courses.size());

        Map<String, List<GeneralClass>> mCourseCode2Class = new HashMap<>();
        for(GeneralClass gc: cls){
            String courseCode = gc.getModuleCode();
            if(mCourseCode2Class.get(courseCode)==null)
                mCourseCode2Class.put(courseCode,new ArrayList<>());
            mCourseCode2Class.get(courseCode).add(gc);
            log.info("createClassSegment -> mCourseCode2Class.get(" + courseCode + ").add(gc.id " + gc.getId() + " gc.code " + gc.getClassCode()+ ")");
        }
        Map<String, List<List<Integer>>> mCourseCodeLT2Partitions = new HashMap<>();
        Map<String, List<List<Integer>>> mCourseCodeBT2Partitions = new HashMap<>();
        Map<String, List<List<Integer>>> mCourseCodeLTBT2Partitions = new HashMap<>();
        for(TimeTablingCourse c: courses){
            if(c.getPartitionLtForSummerSemester()!=null) {
                List<List<Integer>> L1 = Util.extractPartition(c.getPartitionLtForSummerSemester());
                mCourseCodeLT2Partitions.put(c.getId(), L1);
            }
            if(c.getPartitionBtForSummerSemester()!=null) {
                List<List<Integer>> L1 = Util.extractPartition(c.getPartitionBtForSummerSemester());
                mCourseCodeBT2Partitions.put(c.getId(), L1);
            }
            if(c.getPartitionLtBtForSummerSemester()!=null) {
                List<List<Integer>> L1 = Util.extractPartition(c.getPartitionLtBtForSummerSemester());
                mCourseCodeLTBT2Partitions.put(c.getId(), L1);
            }
        }

        for(String courseCode: mCourseCode2Class.keySet()){
            List<GeneralClass> L = mCourseCode2Class.get(courseCode);
            List<GeneralClass> LS = new ArrayList<>();
            List<GeneralClass> LC = new ArrayList<>();
            for(GeneralClass gc: L){
                if(gc.getCrew().equals("S")) LS.add(gc);
                else if(gc.getCrew().equals("C")) LC.add(gc);
            }
            // consider morning classes first
            List<GeneralClass> LLT = new ArrayList<>();
            List<GeneralClass> LBT = new ArrayList<>();
            List<GeneralClass> LLTBT = new ArrayList<>();
            for(GeneralClass gc: L){
                if(gc.getClassType().equals("LT")) LLT.add(gc);
                else if(gc.getClassType().equals("BT")) LBT.add(gc);
                else if(gc.getClassType().equals("LT+BT")) LLTBT.add(gc);
            }

            List<List<Integer>> PLT = mCourseCodeLT2Partitions.get(courseCode);
            List<List<Integer>> PBT = mCourseCodeBT2Partitions.get(courseCode);
            List<List<Integer>> PLTBT = mCourseCodeLTBT2Partitions.get(courseCode);
            // if not configured in DataBase, then use hardcode config
            if(PLT == null){
                if(LLT != null && LLT.size() >= 1){
                    GeneralClass gc = LLT.get(0);
                    if(P.partitions.get(gc.getDuration())!=null)
                        PLT = P.partitions.get(gc.getDuration());
                }
            }
            if(PBT == null){
                if(LBT != null && LBT.size() >= 1){
                    GeneralClass gc = LBT.get(0);
                    if(P.partitions.get(gc.getDuration())!=null)
                        PBT = P.partitions.get(gc.getDuration());
                }
            }
            if(PLTBT == null){
                if(LLTBT != null && LLTBT.size() >= 1){
                    GeneralClass gc = LLTBT.get(0);
                    if(P.partitions.get(gc.getDuration())!=null)
                        PLTBT = P.partitions.get(gc.getDuration());
                }
            }
            log.info("createClassSegment, for course " + courseCode + " morning sz = " + LS.size());
            createClassSegmentsOfASession(LS,PLT,PBT,PLTBT);
            log.info("createClassSegment, for course " + courseCode + " afternoon sz = " + LC.size());
            createClassSegmentsOfASession(LC,PLT,PBT,PLTBT);
            /*
            int idx = 0;
            for(GeneralClass gc: LLT){
                if(PLT != null && PLT.size() > 0){
                    List<Integer> seqSlots = PLT.get(idx);
                    // create a.size class-segment for class gc
                    if(seqSlots.size() > 1){
                        createClassSegment(gc,seqSlots);
                    }
                    idx += 1;
                    if(idx >= PLT.size()) idx = 0;
                }
            }
            idx = 0;
            for(GeneralClass gc: LBT){
                if(PBT != null && PBT.size() > 0){
                    List<Integer> seqSlots = PBT.get(idx);
                    // create a.size class-segment for class gc
                    if(seqSlots.size() > 1){
                        createClassSegment(gc,seqSlots);
                    }
                    idx += 1;
                    if(idx >= PBT.size()) idx = 0;
                }
            }
            idx = 0;
            for(GeneralClass gc: LLTBT){
                if(PLTBT != null && PLTBT.size() > 0){
                    List<Integer> seqSlots = PLTBT.get(idx);
                    // create a.size class-segment for class gc
                    if(seqSlots.size() > 1){
                        createClassSegment(gc,seqSlots);
                    }
                    idx += 1;
                    if(idx >= PLTBT.size()) idx = 0;
                }
            }

            // consider àternoon classes first
            LLT = new ArrayList<>();
            LBT = new ArrayList<>();
            LLTBT = new ArrayList<>();
            for(GeneralClass gc: LC){
                if(gc.getClassType().equals("LT")) LLT.add(gc);
                else if(gc.getClassType().equals("BT")) LBT.add(gc);
                else if(gc.getClassType().equals("LT+BT")) LLTBT.add(gc);
            }
            // apply round robin
            PLT = mCourseCodeLT2Partitions.get(courseCode);
            PBT = mCourseCodeBT2Partitions.get(courseCode);
            PLTBT = mCourseCodeLTBT2Partitions.get(courseCode);
            // if not configured in DataBase, then use hardcode config
            if(PLT == null){
                if(LLT != null && LLT.size() >= 1){
                    GeneralClass gc = LLT.get(0);
                    if(P.partitions.get(gc.getDuration())!=null)
                        PLT = P.partitions.get(gc.getDuration());
                }
            }
            if(PBT == null){
                if(LBT != null && LBT.size() >= 1){
                    GeneralClass gc = LBT.get(0);
                    if(P.partitions.get(gc.getDuration())!=null)
                        PBT = P.partitions.get(gc.getDuration());
                }
            }
            if(PLTBT == null){
                if(LLTBT != null && LLTBT.size() >= 1){
                    GeneralClass gc = LLTBT.get(0);
                    if(P.partitions.get(gc.getDuration())!=null)
                        PLTBT = P.partitions.get(gc.getDuration());
                }
            }
            idx = 0;
            for(GeneralClass gc: LLT){
                if(PLT != null && PLT.size() > 0){
                    List<Integer> seqSlots = PLT.get(idx);
                    // create a.size class-segment for class gc
                    if(seqSlots.size() > 1){
                        createClassSegment(gc,seqSlots);
                    }
                    idx += 1;
                    if(idx >= PLT.size()) idx = 0;
                }
            }
            idx = 0;
            for(GeneralClass gc: LBT){
                if(PBT != null && PBT.size() > 0){
                    List<Integer> seqSlots = PBT.get(idx);
                    // create a.size class-segment for class gc
                    if(seqSlots.size() > 1){
                        createClassSegment(gc,seqSlots);
                    }
                    idx += 1;
                    if(idx >= PBT.size()) idx = 0;
                }
            }
            idx = 0;
            for(GeneralClass gc: LLTBT){
                if(PLTBT != null && PLTBT.size() > 0){
                    List<Integer> seqSlots = PLTBT.get(idx);
                    // create a.size class-segment for class gc
                    if(seqSlots.size() > 1){
                        createClassSegment(gc,seqSlots);
                    }
                    idx += 1;
                    if(idx >= PLTBT.size()) idx = 0;
                }
            }

             */


        }
        return null;

    }

    @Transactional
    @Override
    public int removeClassSegment(CreateClassSegmentRequest I) {
        int cnt = 0;
        List<GeneralClass> cls = gcoRepo.findAllBySemester(I.getSemester());
        log.info("removeClassSegment, number of classes = " + cls.size());
        for(GeneralClass gc: cls) {
            List<RoomReservation> rr = roomReservationRepo.findAllByGeneralClass(gc);
            log.info("removeClassSegment, gc.id = " + gc.getId() + " has " + rr.size() + " class-segments");
            for(RoomReservation r: rr) {
                //roomReservationRepo.delete(r);
                roomReservationRepo.deleteById(r.getId());
                log.info("removeClassSegment, gc.id = " + gc.getId() + " has " + rr.size()
                        + " class-segments -> " + r.getId());
                cnt++;
            }
        }
        return cnt;
    }
}