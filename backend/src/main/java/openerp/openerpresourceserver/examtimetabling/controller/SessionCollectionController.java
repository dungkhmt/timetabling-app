package openerp.openerpresourceserver.examtimetabling.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.examtimetabling.dtos.ExamSessionDTO;
import openerp.openerpresourceserver.examtimetabling.dtos.SessionCollectionDTO;
import openerp.openerpresourceserver.examtimetabling.entity.ExamTimetableSession;
import openerp.openerpresourceserver.examtimetabling.entity.ExamTimetableSessionCollection;
import openerp.openerpresourceserver.examtimetabling.service.SessionCollectionService;

@RestController
@RequestMapping("/exam-session")
@RequiredArgsConstructor
public class SessionCollectionController {
    private final SessionCollectionService sessionCollectionService;
    
    @GetMapping
    public ResponseEntity<List<SessionCollectionDTO>> getAllSessionCollections() {
        List<SessionCollectionDTO> collections = sessionCollectionService.getAllSessionCollections();
        return ResponseEntity.ok(collections);
    }

    @PostMapping("/session/create")
    public ResponseEntity<ExamTimetableSession> createSession(@Valid @RequestBody ExamSessionDTO sessionDTO) {
        try {
            ExamTimetableSession createdSession = sessionCollectionService.createSession(sessionDTO);
            return ResponseEntity.ok(createdSession);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/session/update/{id}")
    public ResponseEntity<ExamTimetableSession> updateSession(
            @PathVariable UUID id,
            @Valid @RequestBody ExamSessionDTO sessionDTO) {
        try {
            ExamTimetableSession updatedSession = sessionCollectionService.updateSession(id, sessionDTO);
            return ResponseEntity.ok(updatedSession);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/session/delete/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable UUID id) {
        try {
            sessionCollectionService.deleteSession(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Cannot delete")) {
                return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .header("X-Error-Message", e.getMessage())
                    .build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/collection/create")
    public ResponseEntity<ExamTimetableSessionCollection> createCollection(@Valid @RequestBody SessionCollectionDTO collectionDTO) {
        try {
            ExamTimetableSessionCollection createdCollection = sessionCollectionService.createCollection(collectionDTO);
            return ResponseEntity.ok(createdCollection);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/collection/update/{id}")
    public ResponseEntity<ExamTimetableSessionCollection> updateCollection(
            @PathVariable UUID id,
            @Valid @RequestBody SessionCollectionDTO collectionDTO) {
        try {
            ExamTimetableSessionCollection updatedCollection = sessionCollectionService.updateCollection(id, collectionDTO);
            return ResponseEntity.ok(updatedCollection);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/collection/delete/{id}")
    public ResponseEntity<Void> deleteCollection(@PathVariable UUID id) {
        try {
            sessionCollectionService.deleteCollection(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Cannot delete")) {
                return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .header("X-Error-Message", e.getMessage())
                    .build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
