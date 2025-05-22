package openerp.openerpresourceserver.generaltimetabling.controller.occupation;

import java.util.List;

import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.model.GetEmptyRoomsRequest;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.RoomOccupationWithModuleCode;
import openerp.openerpresourceserver.generaltimetabling.service.ExcelService;
import openerp.openerpresourceserver.generaltimetabling.service.TimeTablingClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import openerp.openerpresourceserver.generaltimetabling.model.entity.occupation.RoomOccupation;
import openerp.openerpresourceserver.generaltimetabling.service.RoomOccupationService;

@Log4j2
@RestController
@RequestMapping("/room-occupation")
public class RoomOccupationController {
    @Autowired
    private ExcelService excelService;

    @Autowired
    private RoomOccupationService roomOccupationService;

    @Autowired
    private TimeTablingClassService timeTablingClassService;

    @GetMapping("/get-all")
    public ResponseEntity<List<RoomOccupation>> getRoomOccupation (@RequestParam("semester") String semester) {
        try {
            return ResponseEntity.ok(roomOccupationService.getRoomOccupationsBySemester(semester));
        } catch (Exception e) {
            System.err.println(e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/export")
    public ResponseEntity<Resource> exportExcel (@RequestParam("semester") String semester, @RequestParam("week") int week, @RequestParam("versionId") Long versionId) {
        String filename = String.format("room_occupations_S{}_W{}.xlsx", semester, week);
        InputStreamResource file = new InputStreamResource(excelService.exportRoomOccupationExcel(semester, week, versionId));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @GetMapping("/")
    public ResponseEntity<List<RoomOccupationWithModuleCode>> requestGetRoomOccupationsBySemesterAndWeekIndex(
            @RequestParam("semester") String semester, 
            @RequestParam("weekIndex") int weekIndex,
            @RequestParam(name = "versionId", required = false) Long versionId) {
        log.info("requestGetRoomOccupationsBySemesterAndWeekIndex, semester " + semester + " week " + weekIndex + " versionId " + versionId);
        return ResponseEntity.ok(timeTablingClassService.getRoomOccupationsBySemesterAndWeekIndexAndVersionId(semester, weekIndex, versionId));
    }
    
    @PostMapping("/empty-room")
    public ResponseEntity requestGetEmptyRooms(
            @RequestParam("semester") String semester,
            @RequestBody GetEmptyRoomsRequest request) {
        return ResponseEntity.ok(roomOccupationService.getRoomsNotOccupiedBySemesterAndWeekDayCrewStartAndEndSLot(
                semester,
                request.getCrew(),
                request.getWeek(),
                request.getWeekDay(),
                request.getStartTime(),
                request.getEndTime()
        ));
    }
}
