package com._projects.internship.controller.core;


import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com._projects.internship.dto.core.ConventionRequestDTO;
import com._projects.internship.dto.core.ConventionResponseDTO;
import com._projects.internship.service.core.ConventionService;
import com._projects.internship.service.core.ConventionStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;


@RequestMapping("${api.prefix}/conventions")
@RestController
@RequiredArgsConstructor
@Tag(name = "convention-controller")
public class ConventionController {

    private final ConventionService conventionService;
    private final ConventionStorageService conventionStorageService;

    @PostMapping("/create-from-application/{applicationId}")
    @Operation(summary = "Créer une convention après l'acceptation de la demande")
    public ResponseEntity<ConventionResponseDTO> createConventionFromApplication(@PathVariable Long applicationId, @RequestBody ConventionRequestDTO dto) {
        try {
            ConventionResponseDTO response = conventionService.createFromApplication(applicationId, dto);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            // Erreur de validation (candidature non acceptée)
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Application non trouvée")) {
                return ResponseEntity.notFound().build();
            }
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping(value = "/{id}/upload-signed-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Uploader une convention signée (PDF)")
    public ResponseEntity<ConventionResponseDTO> uploadSignedPdf(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            String signedPdfPath = conventionStorageService.saveSignedConvention(id, file);
            ConventionResponseDTO updatedConvention = conventionService.updateSignedPdfPath(id, signedPdfPath);
            return ResponseEntity.ok(updatedConvention);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Convention non trouvée")) {
                return ResponseEntity.notFound().build();
            }
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une convention par ID")
    public ResponseEntity<ConventionResponseDTO> getById(@PathVariable Long id) {
        ConventionResponseDTO convention = conventionService.getConventionById(id);
        if (convention == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(convention);
    }

    @GetMapping
    @Operation(summary = "Récupérer toutes les conventions")
    public ResponseEntity<List<ConventionResponseDTO>> getAll() {
        return ResponseEntity.ok(conventionService.getAllConventions());
    }

    @GetMapping("/teacher/{teacherId}")
    @Operation(summary = "Récupérer toutes les conventions dans le secteur d'un enseignant")
    public ResponseEntity<List<ConventionResponseDTO>> getConventionsForTeacher(@PathVariable Long teacherId) {
        List<ConventionResponseDTO> conventions = conventionService.getConventionsForTeacher(teacherId);
        return ResponseEntity.ok(conventions);
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Récupérer toutes les conventions d'une entreprise")
    public ResponseEntity<List<ConventionResponseDTO>> getConventionsByCompany(@PathVariable Long companyId) {
        try {
            List<ConventionResponseDTO> conventions = conventionService.getConventionsByCompany(companyId);
            return ResponseEntity.ok(conventions);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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

    @GetMapping("/{id}/download-pdf")
    @Operation(summary = "Télécharger le PDF d'une convention")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable("id") Long conventionId) {
        try {
            // Les vérifications sont maintenant dans le service
            byte[] content = conventionService.getConventionPdf(conventionId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("convention_" + conventionId + ".pdf")
                    .build());

            return new ResponseEntity<>(content, headers, HttpStatus.OK);
        } catch (IllegalStateException e) {
            // Aucun PDF disponible
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Convention non trouvée")) {
                return ResponseEntity.notFound().build();
            }
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/update-by-company/{companyId}")
    @Operation(summary = "Mettre à jour une convention par l'entreprise avant validation")
    public ResponseEntity<ConventionResponseDTO> updateByCompany(
            @PathVariable Long id,
            @PathVariable Long companyId,
            @RequestBody ConventionRequestDTO dto) {

        if (!id.equals(dto.getId())) {
            return ResponseEntity.badRequest().build();
        }

        try {
            ConventionResponseDTO updatedConvention = conventionService.updateByCompany(dto, companyId);
            return ResponseEntity.ok(updatedConvention);
        } catch (RuntimeException e) {
            e.printStackTrace();

            // Retourner une réponse appropriée en fonction du message d'erreur
            if (e.getMessage().contains("n'êtes pas autorisé")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } else if (e.getMessage().contains("ne peut pas être modifiée")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
    }

    @PutMapping("/{id}/regenerate-pdf")
    public ResponseEntity<Void> regeneratePdf(@PathVariable Long id) {
        conventionService.regeneratePdfForConvention(id);
        return ResponseEntity.ok().build();
    }
}