package openerp.openerpresourceserver.examtimetabling.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.examtimetabling.dtos.ExamRoom;
import openerp.openerpresourceserver.examtimetabling.service.ExamRoomService;

@RestController
@RequestMapping("/exam-room")
@RequiredArgsConstructor
public class ExamRoomController {
    private final ExamRoomService examRoomService;
    
    @GetMapping
    public ResponseEntity<List<ExamRoom>> getAllRooms() {
        List<openerp.openerpresourceserver.examtimetabling.dtos.ExamRoom> rooms = examRoomService.getAllRooms();
        return ResponseEntity.ok(rooms);
    }
}
