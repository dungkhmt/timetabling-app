package openerp.openerpresourceserver.generaltimetabling.controller;

import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelInputAddRoomToBatchRequest;
import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelInputCreateTimeTablingBatch;
import openerp.openerpresourceserver.generaltimetabling.model.entity.TimeTablingBatch;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.BatchRoom;
import openerp.openerpresourceserver.generaltimetabling.repo.TimeTablingBatchRepo;
import openerp.openerpresourceserver.generaltimetabling.service.BatchRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@RestController
@RequestMapping("/timetabling-batch")
public class TimeTablingBatchController {
    @Autowired
    private TimeTablingBatchRepo timeTablingBatchRepo;

    @Autowired
    private BatchRoomService batchRoomService;

    @PostMapping("/add-batch")
    public ResponseEntity<?> addBatch(Principal principal, @RequestBody ModelInputCreateTimeTablingBatch m){
        TimeTablingBatch batch = new TimeTablingBatch();
        Long id = timeTablingBatchRepo.getNextIdValue();
        batch.setId(id);
        batch.setName(m.getBatchName());
        batch.setCreatedStamp(new Date());
        batch.setCreatedByUserId(principal.getName());
        batch.setSemester(m.getSemester());
        batch = timeTablingBatchRepo.save(batch);
        return ResponseEntity.ok().body(batch);
    }
    @GetMapping("/get-all/{semester}")
    public ResponseEntity<?> getAllBatchByUser(Principal principal, @PathVariable String semester){
        List<TimeTablingBatch> res = timeTablingBatchRepo.findAllByCreatedByUserIdAndSemester(principal.getName(),semester);
        log.info("getAllBatchByUser, user = " + principal.getName() + " res.sz = " + res.size());
        return ResponseEntity.ok().body(res);
    }


    @GetMapping("/get-batch-room/{batchId}")
    public ResponseEntity<?> getBatchRoom(@PathVariable Long batchId){
        log.info("getBatchRoom, id = " + batchId);
        try {
            return ResponseEntity.ok().body(batchRoomService.getBathRoomsByBatchId(batchId));
        } catch (Exception e) {
            log.error("Error getting batch room: ", e);
            return ResponseEntity.status(500).body("Error retrieving batch room");
        }
    }

    @PostMapping("/add-rooms-to-batch")
    public ResponseEntity<?> addBatchRoom(@RequestBody ModelInputAddRoomToBatchRequest m){
        try {
            // Lấy danh sách phòng hiện có trong batch
            List<BatchRoom> existingBatchRooms = batchRoomService.getBathRoomsByBatchId(m.getBatchId());

            // Lấy danh sách ID phòng hiện có
            Set<String> existingRoomIds = existingBatchRooms.stream()
                    .map(BatchRoom::getRoomId)
                    .collect(Collectors.toSet());

            // Lọc ra các phòng mới không trùng với phòng hiện có
            List<Long> newRoomIds = m.getRoomIds().stream()
                    .filter(roomId -> !existingRoomIds.contains(roomId))
                    .collect(Collectors.toList());

            // Nếu có phòng trùng, trả về thông báo lỗi
            if (newRoomIds.size() < m.getRoomIds().size()) {
                List<Long> duplicatedIds = m.getRoomIds().stream()
                        .filter(existingRoomIds::contains)
                        .collect(Collectors.toList());

                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Một số phòng đã tồn tại trong batch");
                response.put("duplicatedRoomIds", duplicatedIds);
                return ResponseEntity.badRequest().body(response);
            }

            // Thêm các phòng mới vào batch
            for (Long roomId : newRoomIds) {
                BatchRoom batchRoom = new BatchRoom();
                batchRoom.setBatchId(m.getBatchId());
                batchRoom.setRoomId(String.valueOf(roomId)); // Giả sử có setter này
                batchRoomService.addBatchRoom(batchRoom);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Thêm phòng vào batch thành công");
            response.put("addedRoomIds", newRoomIds);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            log.error("Error adding rooms to batch: ", e);
            return ResponseEntity.status(500).body("Có lỗi khi thêm phòng vào batch");
        }

    }



}
