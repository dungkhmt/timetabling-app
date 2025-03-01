package openerp.openerpresourceserver.examtimetabling.service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.examtimetabling.dtos.SessionCollectionDTO;
import openerp.openerpresourceserver.examtimetabling.dtos.SessionDTO;
import openerp.openerpresourceserver.examtimetabling.entity.ExamTimetableSession;
import openerp.openerpresourceserver.examtimetabling.entity.ExamTimetableSessionCollection;
import openerp.openerpresourceserver.examtimetabling.repository.ExamTimetableSessionCollectionRepository;
import openerp.openerpresourceserver.examtimetabling.repository.ExamTimetableSessionRepository;

@Service
@RequiredArgsConstructor
public class SessionCollectionService {
    private final ExamTimetableSessionCollectionRepository sessionCollectionRepository;
    private final ExamTimetableSessionRepository sessionRepository;
    
    public List<SessionCollectionDTO> getAllSessionCollections() {
        // Get all collections
        List<ExamTimetableSessionCollection> collections = sessionCollectionRepository.findAll();
        
        // Map collections to DTOs with their sessions
        return collections.stream().map(collection -> {
            SessionCollectionDTO dto = new SessionCollectionDTO();
            dto.setId(collection.getId());
            dto.setName(collection.getName());
            
            // Get all sessions for this collection
            List<ExamTimetableSession> sessions = sessionRepository
                .findByExamTimetableSessionCollectionId(collection.getId());
                
            // Map sessions to DTOs
            List<SessionDTO> sessionDTOs = sessions.stream().map(session -> {
                SessionDTO sessionDTO = new SessionDTO();
                sessionDTO.setId(session.getId());
                sessionDTO.setName(session.getName());
                
                // Format times for display
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                String startTime = session.getStartTime().format(formatter);
                String endTime = session.getEndTime().format(formatter);
                
                sessionDTO.setStartTime(session.getStartTime());
                sessionDTO.setEndTime(session.getEndTime());
                sessionDTO.setDisplayName(session.getName() + " (" + startTime + " - " + endTime + ")");
                
                return sessionDTO;
            }).collect(Collectors.toList());
            
            dto.setSessions(sessionDTOs);
            return dto;
        }).collect(Collectors.toList());
    }
}
