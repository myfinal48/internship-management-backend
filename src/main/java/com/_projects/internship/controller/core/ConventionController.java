package com._projects.internship.controller.core;

import org.springframework.web.bind.annotation.*;

import com._projects.internship.dto.core.ConventionResponseDTO;
import com._projects.internship.service.core.ConventionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import java.util.Map;


@RequestMapping("${api.prefix}/conventions") // Chemin de base pour toutes les méthodes
@RestController
@RequiredArgsConstructor
@Tag(name = "Convention Management")
public class ConventionController {

    private final ConventionService conventionService;

   @PostMapping("/from-application/{applicationId}")
    @Operation(summary = "Create convention after application accepted")
    public ResponseEntity<ConventionResponseDTO> create(@PathVariable Long applicationId) {
        return ResponseEntity.ok(conventionService.createFromApplication(applicationId));
    }

    @PutMapping("/{id}/validate-by-teacher")
    public ResponseEntity<ConventionResponseDTO> validate(@PathVariable Long id) {
        return ResponseEntity.ok(conventionService.validateByTeacher(id));
    }

    @PutMapping("/{id}/reject-by-teacher")
    public ResponseEntity<ConventionResponseDTO> rejectTeacher(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(conventionService.rejectByTeacher(id, body.get("reason")));
    }

    @PutMapping("/{id}/approve-by-admin")
    public ResponseEntity<ConventionResponseDTO> approve(@PathVariable Long id) {
        return ResponseEntity.ok(conventionService.approveByAdmin(id));
    }

    @PutMapping("/{id}/reject-by-admin")
    public ResponseEntity<ConventionResponseDTO> rejectAdmin(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(conventionService.rejectByAdmin(id, body.get("reason")));
    }
}
