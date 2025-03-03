package openerp.openerpresourceserver.examtimetabling.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.examtimetabling.entity.ExamRoom;
import openerp.openerpresourceserver.examtimetabling.repository.ExamRoomRepository;

@Service
@RequiredArgsConstructor
public class ExamRoomService {
    private final ExamRoomRepository examRoomRepository;
    
    public List<ExamRoom> getAllRooms() {
        return examRoomRepository.findAll();
    }
}
