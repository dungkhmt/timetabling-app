package openerp.openerpresourceserver.examtimetabling.service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
        List<ExamTimetableSessionCollection> collections = sessionCollectionRepository.findAll();
        
        // First, fetch all sessions used in assignments in a single query
        String usedSessionsSql = "SELECT DISTINCT exam_session_id FROM exam_timetable_assignment " +
                                "WHERE exam_session_id IS NOT NULL";
        
        Query query = entityManager.createNativeQuery(usedSessionsSql);
        
        // Convert the result to a Set of UUIDs
        @SuppressWarnings("unchecked")
        List<Object> resultList = query.getResultList();
        Set<UUID> usedSessionIds = resultList.stream()
            .filter(Objects::nonNull)
            .map(id -> UUID.fromString(id.toString()))
            .collect(Collectors.toSet());
        
        // Now map collections with this information
        return collections.stream().map(collection -> {
            SessionCollectionDTO dto = new SessionCollectionDTO();
            dto.setId(collection.getId());
            dto.setName(collection.getName());
            
            List<ExamTimetableSession> sessions = sessionRepository
                .findByExamTimetableSessionCollectionId(collection.getId());
            
            List<SessionDTO> sessionDTOs = sessions.stream().map(session -> {
                SessionDTO sessionDTO = new SessionDTO();
                sessionDTO.setId(session.getId());
                sessionDTO.setName(session.getName());
                
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                String startTime = session.getStartTime().format(formatter);
                String endTime = session.getEndTime().format(formatter);
                
                sessionDTO.setStartTime(session.getStartTime());
                sessionDTO.setEndTime(session.getEndTime());
                sessionDTO.setDisplayName(session.getName() + " (" + startTime + " - " + endTime + ")");
                
                // Set isUsing based on whether this session ID is in the used sessions set
                sessionDTO.setUsing(usedSessionIds.contains(session.getId()));
                
                return sessionDTO;
            }).collect(Collectors.toList());
            
            dto.setSessions(sessionDTOs);
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public ExamTimetableSession createSession(ExamSessionDTO sessionDTO) {
        if (!sessionCollectionRepository.existsById(sessionDTO.getExamTimetableSessionCollectionId())) {
            throw new RuntimeException("Session collection not found");
        }
        
        ExamTimetableSession session = new ExamTimetableSession();
        session.setId(UUID.randomUUID());
        session.setName(sessionDTO.getName());
        session.setExamTimetableSessionCollectionId(sessionDTO.getExamTimetableSessionCollectionId());
        session.setStartTime(sessionDTO.getStartTime());
        session.setEndTime(sessionDTO.getEndTime());
        
        return sessionRepository.save(session);
    }

    @Transactional
    public ExamTimetableSession updateSession(UUID sessionId, ExamSessionDTO sessionDTO) {
        // Find the session
        ExamTimetableSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Session not found"));
        
        if (sessionDTO.getExamTimetableSessionCollectionId() != null && 
            !session.getExamTimetableSessionCollectionId().equals(sessionDTO.getExamTimetableSessionCollectionId())) {
            
            if (!sessionCollectionRepository.existsById(sessionDTO.getExamTimetableSessionCollectionId())) {
                throw new RuntimeException("Session collection not found");
            }
            session.setExamTimetableSessionCollectionId(sessionDTO.getExamTimetableSessionCollectionId());
        }
        
        if (sessionDTO.getName() != null) {
            session.setName(sessionDTO.getName());
        }
        
        if (sessionDTO.getStartTime() != null) {
            session.setStartTime(sessionDTO.getStartTime());
        }
        
        if (sessionDTO.getEndTime() != null) {
            session.setEndTime(sessionDTO.getEndTime());
        }
        
        return sessionRepository.save(session);
    }
    
    @Transactional
    public void deleteSession(UUID sessionId) {
        if (!sessionRepository.existsById(sessionId)) {
            System.err.println("Session not found");
            throw new RuntimeException("Session not found");
        }

        boolean isSessionInUse = sessionIsInUse(sessionId);
        System.err.println("isSessionInUse: " + isSessionInUse);
        if (isSessionInUse) {
            throw new RuntimeException("Cannot delete session as it is being used in assignments");
        }
        
        sessionRepository.deleteById(sessionId);
    }
    
    private boolean sessionIsInUse(UUID sessionId) {
        String sql = "SELECT COUNT(*) FROM exam_timetable_assignment " +
                     "WHERE exam_session_id = :sessionId ";
        
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
        
        if (isCollectionInUse(collectionId)) {
            throw new RuntimeException("Cannot delete collection as it contains sessions that are being used in assignments");
        }
        
        List<ExamTimetableSession> sessions = sessionRepository.findByExamTimetableSessionCollectionId(collectionId);
        sessionRepository.deleteAll(sessions);
        
        sessionCollectionRepository.deleteById(collectionId);
    }
    
    private boolean isCollectionInUse(UUID collectionId) {
        String sql = "SELECT COUNT(*) FROM exam_timetable_assignment a " +
                     "JOIN exam_timetable_session s ON a.exam_session_id = s.id " +
                     "WHERE s.exam_timetable_session_collection_id = :collectionId ";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("collectionId", collectionId);
        
        Number count = (Number) query.getSingleResult();
        return count.longValue() > 0;
    }
}
