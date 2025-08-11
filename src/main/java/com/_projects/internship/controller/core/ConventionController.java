package com._projects.internship.controller.core;


import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com._projects.internship.dto.core.ConventionRequestDTO;
import com._projects.internship.dto.core.ConventionResponseDTO;
import com._projects.internship.service.core.ConventionService;
import com._projects.internship.service.core.ConventionStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;


@RequestMapping("${api.prefix}/conventions")
@RestController
@RequiredArgsConstructor
@Tag(name = "convention-controller", description = "Gestion des conventions de stage")
public class ConventionController {

    private final ConventionService conventionService;
    private final ConventionStorageService conventionStorageService;

    @PostMapping("/create-from-application/{applicationId}")
    @Operation(
            summary = "Créer une convention après l'acceptation de la demande",
            description = "Permet de créer une convention de stage après qu'une candidature ait été acceptée. Accessible aux entreprises et administrateurs."
    )
    @ApiResponse(responseCode = "200", description = "Convention créée avec succès")
    @ApiResponse(responseCode = "404", description = "Candidature non trouvée")
    @ApiResponse(responseCode = "409", description = "Convention déjà existante")
    public ResponseEntity<ConventionResponseDTO> createConventionFromApplication(@PathVariable Long applicationId, @RequestBody ConventionRequestDTO dto) {
        try {
            ConventionResponseDTO response = conventionService.createFromApplication(applicationId, dto);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
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
    @Operation(
            summary = "Uploader une convention signée (PDF)",
            description = "Permet d'uploader le PDF signé d'une convention. Accessible aux entreprises et étudiants concernés."
    )
    @ApiResponse(responseCode = "200", description = "PDF signé uploadé avec succès")
    @ApiResponse(responseCode = "400", description = "Fichier invalide")
    @ApiResponse(responseCode = "404", description = "Convention non trouvée")
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
    @Operation(
            summary = "Récupérer une convention par ID",
            description = "Permet de récupérer les détails d'une convention spécifique. Accessible à tous les utilisateurs authentifiés."
    )
    @ApiResponse(responseCode = "200", description = "Convention trouvée")
    @ApiResponse(responseCode = "404", description = "Convention non trouvée")
    public ResponseEntity<ConventionResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(conventionService.getConventionById(id));
    }

    @GetMapping
    @Operation(
            summary = "Récupérer toutes les conventions",
            description = "Permet de récupérer la liste de toutes les conventions. Accessible aux administrateurs."
    )
    @ApiResponse(responseCode = "200", description = "Liste des conventions récupérée")
    public ResponseEntity<List<ConventionResponseDTO>> getAll() {
        return ResponseEntity.ok(conventionService.getAllConventions());
    }

    @GetMapping("/teacher/{teacherId}")
    @Operation(
            summary = "Récupérer toutes les conventions dans le secteur d'un enseignant",
            description = "Permet à un enseignant de voir toutes les conventions dans son secteur d'activité. Rôle requis: TEACHER"
    )
    @ApiResponse(responseCode = "200", description = "Conventions du secteur récupérées")
    public ResponseEntity<List<ConventionResponseDTO>> getConventionsForTeacher(@PathVariable Long teacherId) {
        List<ConventionResponseDTO> conventions = conventionService.getConventionsForTeacher(teacherId);
        return ResponseEntity.ok(conventions);
    }

    @GetMapping("/company/{companyId}")
    @Operation(
            summary = "Récupérer toutes les conventions d'une entreprise",
            description = "Permet à une entreprise de voir toutes ses conventions de stage. Rôle requis: COMPANY"
    )
    @ApiResponse(responseCode = "200", description = "Conventions de l'entreprise récupérées")
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
    @Operation(
            summary = "Valider une convention par l'enseignant",
            description = "Permet à un enseignant de valider une convention de stage. Rôle requis: TEACHER"
    )
    @ApiResponse(responseCode = "200", description = "Convention validée par l'enseignant")
    public ResponseEntity<ConventionResponseDTO> validate(@PathVariable Long id) {
        return ResponseEntity.ok(conventionService.validateByTeacher(id));
    }

    @PutMapping("/{id}/reject-by-teacher")
    @Operation(
            summary = "Rejeter une convention par l'enseignant",
            description = "Permet à un enseignant de rejeter une convention avec une raison. Rôle requis: TEACHER"
    )
    @ApiResponse(responseCode = "200", description = "Convention rejetée par l'enseignant")
    public ResponseEntity<ConventionResponseDTO> rejectTeacher(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(conventionService.rejectByTeacher(id, body.get("reason")));
    }

    @PutMapping("/{id}/approve-by-admin")
    @Operation(
            summary = "Approuver une convention par l'administrateur",
            description = "Permet à un administrateur d'approuver définitivement une convention. Rôle requis: ADMIN"
    )
    @ApiResponse(responseCode = "200", description = "Convention approuvée par l'administrateur")
    public ResponseEntity<ConventionResponseDTO> approve(@PathVariable Long id) {
        return ResponseEntity.ok(conventionService.approveByAdmin(id));
    }

    @PutMapping("/{id}/reject-by-admin")
    @Operation(
            summary = "Rejeter une convention par l'administrateur",
            description = "Permet à un administrateur de rejeter une convention avec une raison. Rôle requis: ADMIN"
    )
    @ApiResponse(responseCode = "200", description = "Convention rejetée par l'administrateur")
    public ResponseEntity<ConventionResponseDTO> rejectAdmin(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(conventionService.rejectByAdmin(id, body.get("reason")));
    }

    @GetMapping("/{id}/download-pdf")
    @Operation(
            summary = "Télécharger le PDF d'une convention",
            description = "Permet de télécharger le PDF d'une convention. Accessible aux parties concernées (étudiant, entreprise, enseignant, admin)."
    )
    @ApiResponse(responseCode = "200", description = "PDF téléchargé avec succès")
    @ApiResponse(responseCode = "404", description = "Convention non trouvée")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable("id") Long conventionId) {
        try {
            byte[] content = conventionService.getConventionPdf(conventionId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("convention_" + conventionId + ".pdf")
                    .build());

            return new ResponseEntity<>(content, headers, HttpStatus.OK);
        } catch (IllegalStateException e) {
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
    @Operation(
            summary = "Mettre à jour une convention par l'entreprise avant validation",
            description = "Permet à une entreprise de modifier une convention avant qu'elle ne soit validée. Rôle requis: COMPANY"
    )
    @ApiResponse(responseCode = "200", description = "Convention mise à jour")
    @ApiResponse(responseCode = "403", description = "Accès refusé")
    @ApiResponse(responseCode = "409", description = "Convention ne peut pas être modifiée")
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
    @Operation(
            summary = "Régénérer le PDF d'une convention",
            description = "Permet de régénérer le PDF d'une convention après modification. Accessible aux administrateurs."
    )
    @ApiResponse(responseCode = "200", description = "PDF régénéré avec succès")
    public ResponseEntity<Void> regeneratePdf(@PathVariable Long id) {
        conventionService.regeneratePdfForConvention(id);
        return ResponseEntity.ok().build();
    }
}