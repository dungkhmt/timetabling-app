package openerp.openerpresourceserver.examtimetabling.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.examtimetabling.dtos.SessionCollectionDTO;
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
}
