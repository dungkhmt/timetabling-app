package openerp.openerpresourceserver.examtimetabling.service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.examtimetabling.dtos.ExamSessionDTO;
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
    @Autowired
    private EntityManager entityManager;

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

    @Transactional
    public ExamTimetableSession createSession(ExamSessionDTO sessionDTO) {
        // Validate session collection exists
        if (!sessionCollectionRepository.existsById(sessionDTO.getExamTimetableSessionCollectionId())) {
            throw new RuntimeException("Session collection not found");
        }
        
        // Create new session entity
        ExamTimetableSession session = new ExamTimetableSession();
        session.setId(UUID.randomUUID());
        session.setName(sessionDTO.getName());
        session.setExamTimetableSessionCollectionId(sessionDTO.getExamTimetableSessionCollectionId());
        session.setStartTime(sessionDTO.getStartTime());
        session.setEndTime(sessionDTO.getEndTime());
        
        // Save and return the session
        return sessionRepository.save(session);
    }

    @Transactional
    public ExamTimetableSession updateSession(UUID sessionId, ExamSessionDTO sessionDTO) {
        // Find the session
        ExamTimetableSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Session not found"));
        
        // If collection ID is being changed, validate the new collection exists
        if (sessionDTO.getExamTimetableSessionCollectionId() != null && 
            !session.getExamTimetableSessionCollectionId().equals(sessionDTO.getExamTimetableSessionCollectionId())) {
            
            if (!sessionCollectionRepository.existsById(sessionDTO.getExamTimetableSessionCollectionId())) {
                throw new RuntimeException("Session collection not found");
            }
            session.setExamTimetableSessionCollectionId(sessionDTO.getExamTimetableSessionCollectionId());
        }
        
        // Update fields
        if (sessionDTO.getName() != null) {
            session.setName(sessionDTO.getName());
        }
        
        if (sessionDTO.getStartTime() != null) {
            session.setStartTime(sessionDTO.getStartTime());
        }
        
        if (sessionDTO.getEndTime() != null) {
            session.setEndTime(sessionDTO.getEndTime());
        }
        
        // Save and return the updated session
        return sessionRepository.save(session);
    }
    
    @Transactional
    public void deleteSession(UUID sessionId) {
        // Check if session exists
        if (!sessionRepository.existsById(sessionId)) {
            System.err.println("Session not found");
            throw new RuntimeException("Session not found");
        }

        System.err.println("Session found");

        
        // Check if the session is being used in any assignments
        boolean isSessionInUse = sessionIsInUse(sessionId);
        System.err.println("isSessionInUse: " + isSessionInUse);
        if (isSessionInUse) {
            throw new RuntimeException("Cannot delete session as it is being used in assignments");
        }
        
        // Delete the session
        sessionRepository.deleteById(sessionId);
    }
    
    private boolean sessionIsInUse(UUID sessionId) {
        String sql = "SELECT COUNT(*) FROM exam_timetable_assignment " +
                     "WHERE exam_session_id = :sessionId " +
                     "AND deleted_at IS NULL";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("sessionId", sessionId);
        
        Number count = (Number) query.getSingleResult();
        return count.longValue() > 0;
    }

    @Transactional
    public ExamTimetableSessionCollection createCollection(SessionCollectionDTO collectionDTO) {
        ExamTimetableSessionCollection collection = new ExamTimetableSessionCollection();
        collection.setId(UUID.randomUUID());
        collection.setName(collectionDTO.getName());
        
        return sessionCollectionRepository.save(collection);
    }
    
    @Transactional
    public ExamTimetableSessionCollection updateCollection(UUID collectionId, SessionCollectionDTO collectionDTO) {
        ExamTimetableSessionCollection collection = sessionCollectionRepository.findById(collectionId)
            .orElseThrow(() -> new RuntimeException("Collection not found"));
        
        if (collectionDTO.getName() != null) {
            collection.setName(collectionDTO.getName());
        }
        
        return sessionCollectionRepository.save(collection);
    }
    
    @Transactional
    public void deleteCollection(UUID collectionId) {
        if (!sessionCollectionRepository.existsById(collectionId)) {
            throw new RuntimeException("Collection not found");
        }
        
        // Check if any sessions in this collection are being used
        if (isCollectionInUse(collectionId)) {
            throw new RuntimeException("Cannot delete collection as it contains sessions that are being used in assignments");
        }
        
        // Delete all sessions in this collection
        List<ExamTimetableSession> sessions = sessionRepository.findByExamTimetableSessionCollectionId(collectionId);
        sessionRepository.deleteAll(sessions);
        
        // Delete the collection
        sessionCollectionRepository.deleteById(collectionId);
    }
    
    private boolean isCollectionInUse(UUID collectionId) {
        String sql = "SELECT COUNT(*) FROM exam_timetable_assignment a " +
                     "JOIN exam_timetable_session s ON a.exam_session_id = s.id " +
                     "WHERE s.exam_timetable_session_collection_id = :collectionId " +
                     "AND a.deleted_at IS NULL";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("collectionId", collectionId);
        
        Number count = (Number) query.getSingleResult();
        return count.longValue() > 0;
    }
}
