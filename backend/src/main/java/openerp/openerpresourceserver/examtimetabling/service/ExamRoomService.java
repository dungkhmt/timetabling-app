package openerp.openerpresourceserver.examtimetabling.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.examtimetabling.dtos.ExamRoom;
import openerp.openerpresourceserver.examtimetabling.repository.ClassroomRepository;

@Service
@RequiredArgsConstructor
public class ExamRoomService {
    private final ClassroomRepository examRoomRepository;
    
    public List<ExamRoom> getAllRooms() {
        return examRoomRepository.findAllAsExamRoomDTO();
    }
}
