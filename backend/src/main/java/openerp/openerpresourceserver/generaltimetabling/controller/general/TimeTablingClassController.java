package openerp.openerpresourceserver.generaltimetabling.controller.general;

import java.security.Principal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.common.Constants;
import openerp.openerpresourceserver.generaltimetabling.exception.*;
import openerp.openerpresourceserver.generaltimetabling.model.dto.CreateClassSegmentRequest;
import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelInputAssignSessionToClassesSummer;
import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelResponseTimeTablingClass;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.*;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.general.*;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.general.UpdateGeneralClassRequest;
import openerp.openerpresourceserver.generaltimetabling.model.entity.Classroom;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.Cluster;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.RoomReservation;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingClass;
import openerp.openerpresourceserver.generaltimetabling.model.input.ModelInputAutoScheduleTimeSlotRoom;
import openerp.openerpresourceserver.generaltimetabling.model.input.ModelInputSearchRoom;
import openerp.openerpresourceserver.generaltimetabling.model.response.ModelResponseGeneralClass;
import openerp.openerpresourceserver.generaltimetabling.repo.ClusterRepo;
import openerp.openerpresourceserver.generaltimetabling.service.ClassGroupService;
import openerp.openerpresourceserver.generaltimetabling.service.ExcelService;
import openerp.openerpresourceserver.generaltimetabling.service.GeneralClassService;
import openerp.openerpresourceserver.generaltimetabling.service.TimeTablingClassService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import openerp.openerpresourceserver.generaltimetabling.model.entity.general.GeneralClass;

@RestController
@RequestMapping("/general-classes")
@AllArgsConstructor
@Log4j2
public class TimeTablingClassController {
    private GeneralClassService gService;
    private ExcelService excelService;
    private ClassGroupService classGroupService;
    private ClusterRepo clusterRepo;

    private TimeTablingClassService timeTablingClassService;

    @ExceptionHandler(ConflictScheduleException.class)
    public ResponseEntity resolveScheduleConflict(ConflictScheduleException e) {
        return ResponseEntity.status(410).body(e.getCustomMessage());
    }

    @ExceptionHandler(InvalidFieldException.class)
    public ResponseEntity resolveInvalidFieldException(InvalidFieldException e) {
        return ResponseEntity.status(420).body(e.getErrorMessage());
    }

    @ExceptionHandler(MinimumTimeSlotPerClassException.class)
    public ResponseEntity resolveMiniumTimeSlotException(MinimumTimeSlotPerClassException e) {
        return ResponseEntity.status(410).body(e.getErrorMessage());
    }

    @ExceptionHandler(InvalidClassStudentQuantityException.class)
    public ResponseEntity resolveScheduleConflict(InvalidClassStudentQuantityException e) {
        return ResponseEntity.status(410).body(e.getCustomMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity resolveNotFoundSolution(NotFoundException e) {
        return ResponseEntity.status(410).body(e.getCustomMessage());
    }

    @ExceptionHandler(ParseException.class)
    public ResponseEntity resolveParseException( ParseException e) {
        return ResponseEntity.status(410).body("Mã lớp hoặc mã lớp tạm thời, mã lớp cha không phải là 1 số!");
    }

    @GetMapping("/")
    public List<ModelResponseTimeTablingClass> requestGetClasses(
            @RequestParam("semester") String semester,
            @RequestParam(value = "groupName", required = false) Long groupId,
            @RequestParam(value = "versionId", required = false) Long versionId) {
        log.info("requestGetClasses, group = " + groupId + ", semester = " + semester + ", versionId = " + versionId);
        return timeTablingClassService.getTimeTablingClassDtos(semester, groupId, versionId);
    }

    @GetMapping("/get-by-parent-class")
    public List<ModelResponseTimeTablingClass> getSubClasses(@RequestParam("parentClassId") Long parentClassId){
        return timeTablingClassService.getSubClass(parentClassId);
    }

    @PostMapping("/update-class")
    public ResponseEntity<?> requestUpdateClass(@RequestBody UpdateGeneralClassRequest request) {
        log.info("requestUpdateClass, API /update-class: request classId = " + request.getGeneralClass().getId());
        TimeTablingClass cls = timeTablingClassService.updateClass(request);
        return ResponseEntity.ok().body(cls);
    }

    @PostMapping("/update-class-schedule")
    public ResponseEntity<GeneralClass> requestUpdateClassSchedule(@RequestParam("semester")String semester, @RequestBody UpdateGeneralClassScheduleRequest request ) {
        GeneralClass updatedGeneralClass= gService.updateGeneralClassSchedule(semester, request);
        if(updatedGeneralClass == null) throw new RuntimeException("General Class was null");
        return ResponseEntity.ok().body(updatedGeneralClass);
    }

    @PostMapping("/update-class-schedule-v2")
    public ResponseEntity<List<GeneralClass>> requestUpdateClassScheduleV2(@RequestParam("semester")String semester, @RequestBody UpdateClassScheduleRequest request ) {
        boolean ok = timeTablingClassService.updateTimeTableClassSegment(semester, request.getSaveRequests());
        return ResponseEntity.ok().body(new ArrayList<>());
    }

    @PostMapping("/update-classes-group")
    public ResponseEntity<String> requestUpdateClassesGroup(@RequestBody UpdateClassesToNewGroupRequest request) {
        try {
            gService.addClassesToGroup(request.getIds(), request.getGroupName());
            return ResponseEntity.ok("Updated class groups successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update class groups: " + e.getMessage());
        }
    }


    @PostMapping("/update-class-group")
    public ResponseEntity<String> updateClassGroup(@RequestParam Long classId, @RequestParam Long groupId) {
        try {
            classGroupService.addClassGroup(classId, groupId);
            return ResponseEntity.ok("Class group updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update class group: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete-class-group")
    public ResponseEntity<String> deleteClassGroup(@RequestParam Long classId, @RequestParam Long groupId) {
        try {
            classGroupService.deleteClassGroup(classId, groupId);
            return ResponseEntity.ok("Class group deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete class group: " + e.getMessage());
        }
    }

    @GetMapping("/get-class-groups")
    public ResponseEntity<List<ClassGroupSummary>> getClassGroups(Principal principal, @RequestParam Long classId) {
        log.info("getClassGroups, classId = " + classId);
        List<ClassGroupSummary> classGroups = classGroupService.getAllClassGroup(classId);
        return ResponseEntity.ok(classGroups);
    }

    @GetMapping("/get-class-detail-with-subclasses/{classId}")
    public ResponseEntity<?> getClassDetailWithSubClasses(Principal principal, @PathVariable Long classId){
        ModelResponseGeneralClass cls = gService.getClassDetailWithSubClasses(classId);
        return ResponseEntity.ok().body(cls);
    }

    @PostMapping("/export-excel")
    public ResponseEntity requestExportExcel(@RequestParam("semester") String semester, @RequestBody ExportExcelRequest requestDto) {
        log.info("Controler API -> requestExportExcel start...");
        String filename = String.format("TKB_{}.xlsx", semester);
        InputStreamResource file = new InputStreamResource(excelService.exportGeneralExcel(
                semester,
                requestDto.getVersionId(),
                requestDto.getNumberSlotsPerSession()
        ));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @PostMapping("/export-excel/view-all-session")
    public ResponseEntity requestExportExcelWithAllSession(@RequestParam("semester") String semester, @RequestBody ExportExcelRequest requestDto) {
        log.info("Controler API -> requestExportExcel start...");
        String filename = String.format("TKB_{}.xlsx", semester);
        InputStreamResource file = new InputStreamResource(excelService.exportGeneralExcelWithAllSession(
                semester,
                requestDto.getVersionId(),
                requestDto.getNumberSlotsPerSession()
        ));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @PostMapping("/reset-schedule")
    public ResponseEntity<?> requestResetSchedule(@RequestParam("semester") String semester, @RequestBody ResetScheduleRequest request) {
        log.info("Controler API -> requestResetSchedule start...");
        return ResponseEntity.ok(timeTablingClassService.clearTimeTable(request.getIds()));
    }

    @GetMapping("/get-list-algorithm-names")
    public ResponseEntity<?> getListAlgorithms(){
        List<String> res = new ArrayList<>();
        res.add(Constants.SUMMER_SEMESTER);
        res.add(Constants.MANY_CLASS_PER_COURSE_FULL_SLOTS_SEPARATE_DAYS);
        res.add(Constants.ALGO_BACKTRACKING_ONE_CLUSTER);
        res.add(Constants.MANY_CLASS_PER_COURSE_MAX_REGISTRATION_OPPORTUNITY_GREEDY_1);
        res.add(Constants.ONE_CLASS_PER_COURSE_GREEDY_FIRST_FIT);
        res.add(Constants.ONE_CLASS_PER_COURSE_GREEDY_2);
        res.add(Constants.ONE_CLASS_PER_COURSE_GREEDY_3);

        return ResponseEntity.ok().body(res);
    }
    @PostMapping("/auto-schedule-timeslot-room")
    public ResponseEntity<?> autoScheduleTimeSlotRoom(Principal principal, @RequestBody ModelInputAutoScheduleTimeSlotRoom I){
        return ResponseEntity.ok().body(gService.autoScheduleTimeSlotRoom(I.getSemester(),I.getClassIds(),I.getTimeLimit(),I.getAlgorithm(),I.getMaxDaySchedule(), I.getVersionId()));
    }
    @PostMapping("/auto-schedule-time")
    public ResponseEntity<List<GeneralClass>> requestAutoScheduleTime(
            @RequestParam("semester") String semester,
            @RequestParam("groupName") String groupName,
            @RequestParam("timeLimit") int timeLimit) {
        log.info("Controller API -> requestAutoScheduleTime...");
        return ResponseEntity.ok(gService.autoSchedule(semester, timeLimit*1000));
    }

    @PostMapping("/auto-schedule-room")
    public ResponseEntity<?> requestAutoScheduleRoom(
            @RequestParam("semester") String semester,
            @RequestParam("groupName") String groupName,
            @RequestParam("timeLimit") int timeLimit) {
        log.info("Controler API -> requestAutoScheduleRoom...");
        return ResponseEntity.ok(gService.autoScheduleRoom(semester, groupName, timeLimit));
    }

    @DeleteMapping("/")
    public ResponseEntity<Integer> requestDeleteClass(@RequestParam("generalClassId") Long generalClassId) {
        return ResponseEntity.ok(timeTablingClassService.deleteByIds(List.of(generalClassId)));
    }

    @DeleteMapping("/delete-by-ids")
    public ResponseEntity<String> deleteClassesByIds(@RequestBody List<Long> ids) {
        log.info("deleteClassesByIds, ids = " + ids.size());
        timeTablingClassService.deleteByIds(ids);
        return ResponseEntity.ok("Deleted classes with IDs: " + ids);
    }


    @PostMapping("/{generalClassId}/room-reservations/")
    public ResponseEntity<?> requestAddRoomReservation(
            @PathVariable("generalClassId") Long generalClassId,
            @RequestBody RoomReservationDto request) {
        return ResponseEntity.ok(timeTablingClassService.splitNewClassSegment(generalClassId, request.getParentId(), request.getDuration(), request.getVersionId()));
    }

    @DeleteMapping("/delete-by-semester")
    public ResponseEntity<String> requestDeleteClassesBySemester(@RequestParam("semester")String semester) {
        gService.deleteClassesBySemester(semester);
        return ResponseEntity.ok("Xóa lớp thành công");
    }

    @DeleteMapping("/{timeTablingClassId}/class-segment/{timeTablingClassSegmentId}")
    public ResponseEntity<String> requestDeleteRoomReservation(
            @PathVariable("timeTablingClassId") Long timeTablingClassId,
            @PathVariable("timeTablingClassSegmentId") Long timeTablingClassSegmentId,
            @RequestParam(name = "versionId", required = false) Long versionId 
    ) {
        timeTablingClassService.mergeAndDeleteClassSegments(timeTablingClassId, timeTablingClassSegmentId, versionId);
        return ResponseEntity.ok("Xóa phân đoạn ca học và gộp vào ca cha thành công");
    }
    
    @PostMapping("/compute-class-cluster")
    public ResponseEntity<?> computeClassCluster(Principal principal, @RequestBody ModelInputComputeClassCluster I){
        log.info("computeClassCluster, semester = " + I.getSemester());
        int cnt = timeTablingClassService.computeClassCluster(I);
        log.info("computeClassCluster, semester = " + I.getSemester() + " result cnt = " + cnt);
        return ResponseEntity.ok().body(cnt);
    }

    @GetMapping("/get-by-cluster/{clusterId}")
    public ResponseEntity<?> getGeneralClassesByCluster(
            @PathVariable Long clusterId,
            @RequestParam(required = false) Long versionId) {
        List<ModelResponseTimeTablingClass> classes = timeTablingClassService.getClassByCluster(clusterId, versionId);
        return ResponseEntity.ok(classes);
    }

    @GetMapping("/get-clusters-by-semester")
    public ResponseEntity<List<Cluster>> getClustersBySemester(@RequestParam("semester") String semester) {
        List<Cluster> clusters = clusterRepo.findAllBySemester(semester);
        return ResponseEntity.ok(clusters);
    }

    @PostMapping("/remove-class-segments")
    public ResponseEntity<?> removeClassSegments(Principal principal, @RequestBody CreateClassSegmentRequest I){
        int res = gService.removeClassSegment(I);
        res = timeTablingClassService.removeClassSegment(I);
        return ResponseEntity.ok().body(res);
    }
    @PostMapping("/create-class-segments")
    public ResponseEntity<?> createClassSegments(Principal principal, @RequestBody CreateClassSegmentRequest I){
        List<RoomReservation> res = gService.createClassSegment(I);
        timeTablingClassService.createClassSegment(I);
        return ResponseEntity.ok().body(res);
    }
    @PostMapping("/assign-session-to-classes-for-summer-semester")
    public ResponseEntity<?> assignSessionToClassesSummerSemester(Principal principal, @RequestBody ModelInputAssignSessionToClassesSummer I){
        List<TimeTablingClass> res = timeTablingClassService.assignSessionToClassesSummer(I);
        return ResponseEntity.ok().body(res);
    }

    @PostMapping("/create-class-segments-for-summer-semester")
    public ResponseEntity<?> createClassSegmentsForSummerSemester(Principal principal, @RequestBody CreateClassSegmentRequest I){
        List<RoomReservation> res = gService.createClassSegment(I);
        timeTablingClassService.createClassSegmentForSummerSemester(I);
        return ResponseEntity.ok().body(res);
    }
    @PostMapping("/search-rooms")
    public ResponseEntity<?> searchRoom(Principal principal, @RequestBody ModelInputSearchRoom I){
        log.info("searchRoom, capacity = " + I.getSearchRoomCapacity() + " timeSlots = " + I.getTimeSlots());
        List<Classroom> res = timeTablingClassService.searchRoom(I);
        return ResponseEntity.ok().body(res);
    }
}