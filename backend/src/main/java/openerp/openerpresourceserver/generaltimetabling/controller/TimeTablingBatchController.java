package openerp.openerpresourceserver.generaltimetabling.controller;

import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelInputAddRoomToBatchRequest;
import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelInputCreateTimeTablingBatch;
import openerp.openerpresourceserver.generaltimetabling.model.entity.TimeTablingBatch;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.BatchRoom;
import openerp.openerpresourceserver.generaltimetabling.model.response.ModelResposeBatchDetail;
import openerp.openerpresourceserver.generaltimetabling.repo.TimeTablingBatchRepo;
import openerp.openerpresourceserver.generaltimetabling.service.BatchRoomService;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    @GetMapping("/get-batch-detail/{batchId}")
    public ResponseEntity<?> getBatchById(Principal principal, @PathVariable Long batchId){
        TimeTablingBatch batch = timeTablingBatchRepo.findById(batchId).orElse(null);
        List<TimeTablingBatch> batches = timeTablingBatchRepo.findAllByCreatedByUserIdAndSemester(principal.getName(),batch.getSemester());
        ModelResposeBatchDetail res = new ModelResposeBatchDetail();
        res.setBatchId(batchId); res.setBatchName(batch.getName());
        res.setBatchesOfSemester(batches);
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
    public ResponseEntity<?> addBatchRoom(@RequestBody ModelInputAddRoomToBatchRequest request) {
        try {
            Long batchId = request.getBatchId();
            List<String> incomingRoomIds = request.getRoomIds();

//            return null;

            // Bước 1: Lấy danh sách ID các phòng đã có trong batch để kiểm tra trùng lặp
            Set<String> existingRoomIds = batchRoomService.getBathRoomsByBatchId(batchId)
                    .stream()
                    .map(BatchRoom::getRoomId) // Giả sử BatchRoom có phương thức getRoomId() trả về String
                    .collect(Collectors.toSet());

            // Bước 2: Kiểm tra xem có phòng nào trong danh sách gửi lên đã tồn tại hay không
            List<String> duplicatedIds = incomingRoomIds.stream()
                    .filter(existingRoomIds::contains) // Lọc ra những ID đã tồn tại
                    .collect(Collectors.toList());

            // Bước 3: Nếu có bất kỳ phòng nào bị trùng, trả về lỗi
            if (!duplicatedIds.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Thêm thất bại. Một số phòng đã tồn tại trong batch.");
                response.put("duplicatedRoomIds", duplicatedIds);
                // Sử dụng HttpStatus.CONFLICT (409) sẽ ngữ nghĩa hơn badRequest (400)
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            // Bước 4: Nếu không có phòng nào trùng, tiến hành thêm tất cả vào batch
            for (String roomId : incomingRoomIds) {
                BatchRoom newBatchRoom = new BatchRoom();
                newBatchRoom.setBatchId(batchId);
                newBatchRoom.setRoomId(roomId); // SỬA LỖI: Gán trực tiếp String, không cần chuyển đổi
                batchRoomService.addBatchRoom(newBatchRoom);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Thêm " + incomingRoomIds.size() + " phòng vào batch thành công.");
            response.put("addedRoomIds", incomingRoomIds);
            return ResponseEntity.ok(response);

        }
        catch (Exception e) {
            log.error("Error adding rooms to batch: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding rooms to batch");
        }

    }

    @DeleteMapping("/remove-room-from-batch")
    public ResponseEntity<?> removeRoomFromBatch(
            @RequestParam Long batchId,
            @RequestParam String roomId) {

        try {

//            Long batchIdlong = Long.parseLong(batchId);
//            // Xóa bản ghi từ timetabling_batch_room
            batchRoomService.deleteByBatchIdAndRoomId(batchId, roomId);
//
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Xóa phòng khỏi batch thành công"
            ));
//            return null;
        } catch (Exception e) {
            log.error("Error removing room from batch: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error removing room from batch");
        }
    }

    @DeleteMapping("/remove-all-rooms-from-batch")
    public ResponseEntity<?> removeRoomFromBatch(
            @RequestParam Long batchId) {

        try {

//            Long batchIdlong = Long.parseLong(batchId);
//            // Xóa bản ghi từ timetabling_batch_room
            batchRoomService.deleteByBatchId(batchId);
//
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Xóa hết phòng khỏi batch thành công"
            ));
//            return null;
        } catch (Exception e) {
            log.error("Error removing room from batch: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error removing room from batch");
        }
    }
}
