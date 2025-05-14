package com._projects.internship.controller.core;

import org.springframework.web.bind.annotation.*;

import com._projects.internship.dto.core.ConventionResponseDTO;
import com._projects.internship.service.core.ConventionService;
import com._projects.internship.service.core.ConventionStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;


@RequestMapping("${api.prefix}/conventions") // Chemin de base pour toutes les méthodes
@RestController
@RequiredArgsConstructor
@Tag(name = "Convention Management")
public class ConventionController {

    private final ConventionService conventionService;
    private final ConventionStorageService conventionStorageService;

   @PostMapping("/from-application/{applicationId}")
    @Operation(summary = "Create convention after application accepted")
    public ResponseEntity<ConventionResponseDTO> create(@PathVariable Long applicationId) {
        return ResponseEntity.ok(conventionService.createFromApplication(applicationId));
    }
    
    /**
     * Récupère une convention par son ID
     * @param id L'ID de la convention
     * @return La convention
     */
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une convention par ID")
    public ResponseEntity<ConventionResponseDTO> getById(@PathVariable Long id) {
        ConventionResponseDTO convention = conventionService.getConventionById(id);
        if (convention == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(convention);
    }
    
    /**
     * Récupère toutes les conventions
     * @return La liste des conventions
     */
    @GetMapping
    @Operation(summary = "Récupérer toutes les conventions")
    public ResponseEntity<List<ConventionResponseDTO>> getAll() {
        return ResponseEntity.ok(conventionService.getAllConventions());
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
    
    /**
     * Télécharge le PDF d'une convention
     * @param id L'ID de la convention
     * @return Le fichier PDF
     */
    @GetMapping("/{id}/download-pdf")
    @Operation(summary = "Télécharger le PDF d'une convention")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        // Récupérer la convention
        ConventionResponseDTO convention = conventionService.getConventionById(id);
        
        if (convention == null || convention.getPdfPath() == null || convention.getPdfPath().isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // Récupérer le contenu du PDF
        byte[] pdfContent = conventionStorageService.getFile(convention.getPdfPath());
        
        // Configurer les en-têtes de la réponse
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "convention_" + id + ".pdf");
        headers.setContentLength(pdfContent.length);
        
        return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
    }
}
