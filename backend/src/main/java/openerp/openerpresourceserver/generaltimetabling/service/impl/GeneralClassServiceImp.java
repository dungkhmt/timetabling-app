package openerp.openerpresourceserver.generaltimetabling.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import openerp.openerpresourceserver.generaltimetabling.algorithms.V2ClassScheduler;
import openerp.openerpresourceserver.generaltimetabling.exception.ConflictScheduleException;
import openerp.openerpresourceserver.generaltimetabling.exception.InvalidFieldException;
import openerp.openerpresourceserver.generaltimetabling.exception.MinimumTimeSlotPerClassException;
import openerp.openerpresourceserver.generaltimetabling.exception.NotFoundException;
import openerp.openerpresourceserver.generaltimetabling.helper.ClassTimeComparator;
import openerp.openerpresourceserver.generaltimetabling.helper.LearningWeekExtractor;
import openerp.openerpresourceserver.generaltimetabling.mapper.RoomOccupationMapper;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.GeneralClassDto;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.general.UpdateGeneralClassRequest;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.general.UpdateGeneralClassScheduleRequest;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.general.V2UpdateClassScheduleRequest;
import openerp.openerpresourceserver.generaltimetabling.model.entity.*;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.GeneralClass;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.RoomReservation;
import openerp.openerpresourceserver.generaltimetabling.model.entity.occupation.RoomOccupation;
import openerp.openerpresourceserver.generaltimetabling.model.response.ModelResponseGeneralClass;
import openerp.openerpresourceserver.generaltimetabling.repo.*;
import openerp.openerpresourceserver.generaltimetabling.service.GeneralClassService;
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
    public List<GeneralClassDto> getGeneralClassDtos(String semester, String groupName) {
        List<GeneralClass> generalClasses = (groupName == null || groupName.isEmpty())
                ? gcoRepo.findAllBySemester(semester)
                : gcoRepo.findAllBySemesterAndGroupName(semester, groupName);

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

    @Transactional
    @Override
    public List<GeneralClass> autoScheduleTimeSlotRoom(String semester, List<Long> classIds, int timeLimit, String algorithm, int maxDaySchedule) {

        log.info("autoScheduleTimeSlotRoom START....maxDaySchedule = " + maxDaySchedule);
        List<TimeTablingConfigParams> params = timeTablingConfigParamsRepo.findAll();
        /*
        for(TimeTablingConfigParams p: params){
            if(p.getId().equals(TimeTablingConfigParams.MAX_DAY_SCHEDULED)){
                p.setValue(maxDaySchedule);
            }else{

            }
        }
        */
        
        //List<GeneralClass> foundClasses = gcoRepo.findAllBySemester(semester);
        List<GeneralClass> foundClasses = gcoRepo.findAllByIdIn(classIds);
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
        List<GeneralClass> autoScheduleClasses = optimizer.autoScheduleTimeSlotRoom(foundClasses,rooms,mId2RoomReservations,courses, groups,classGroups,timeLimit,algorithm,params);

        /*Save the scheduled timeslot of the classes*/
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
            //log.info("autoScheduleTimeSlotRoom, saved a new room occupation id = " + ro.getId());
        }
        //gcoRepo.saveAll(updatedClasses);

        return autoScheduleClasses;
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
}