package com._projects.internship.controller.core;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com._projects.internship.dto.core.ApplicationRequestDTO;
import com._projects.internship.dto.core.ApplicationResponseDTO;
import com._projects.internship.model.core.ApplicationStatus;
import com._projects.internship.model.security.User;
import com._projects.internship.service.core.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpHeaders;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/applications")
@RequiredArgsConstructor
@Tag(name = "application-controller", description = "Gestion des candidatures de stage")
public class ApplicationController {
    private final ApplicationService applicationService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(
            summary = "Créer une candidature",
            description = "Permet à un étudiant de postuler à une offre de stage avec CV et lettre de motivation. Rôle requis: STUDENT"
    )
    @ApiResponse(responseCode = "201", description = "Candidature créée avec succès")
    @ApiResponse(responseCode = "403", description = "Accès refusé - rôle STUDENT requis")
    public ResponseEntity<ApplicationResponseDTO> createApplication(
        @RequestParam("studentId") Long studentId,
        @RequestParam("offerId")  Long offerId,
        @RequestPart("cv")        MultipartFile cv,
        @RequestPart("coverLetter") MultipartFile coverLetter
    ) {
        ApplicationRequestDTO dto = new ApplicationRequestDTO(studentId, offerId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(applicationService.apply(dto, cv, coverLetter));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Récupérer toutes les candidatures",
            description = "Permet à un administrateur de voir toutes les candidatures du système. Rôle requis: ADMIN"
    )
    @ApiResponse(responseCode = "200", description = "Liste des candidatures récupérée")
    @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis")
    public ResponseEntity<List<ApplicationResponseDTO>> getAllApplications() {
        List<ApplicationResponseDTO> list = applicationService.getAll();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/my-applications")
    @PreAuthorize("hasRole('STUDENT') or hasRole('COMPANY')")
    @Operation(
            summary = "Récupérer mes candidatures",
            description = "Permet à un étudiant de voir ses candidatures ou à une entreprise de voir les candidatures reçues. Rôles requis: STUDENT ou COMPANY"
    )
    @ApiResponse(responseCode = "200", description = "Candidatures récupérées")
    @ApiResponse(responseCode = "403", description = "Accès refusé - rôle STUDENT ou COMPANY requis")
    public ResponseEntity<List<ApplicationResponseDTO>> getMyApplications(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        if (user.getRole().name().equals("STUDENT")) {
            return ResponseEntity.ok(applicationService.getByStudentId(user.getId()));
        } else if (user.getRole().name().equals("COMPANY")) {
            return ResponseEntity.ok(applicationService.getByCompanyId(user.getId()));
        } else {
            return ResponseEntity.status(403).build();
        }
    }

    @GetMapping("/company/{id}")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(
            summary = "Récupérer les candidatures par entreprise",
            description = "Permet à une entreprise de récupérer toutes les candidatures pour ses offres. Rôle requis: COMPANY"
    )
    @ApiResponse(responseCode = "200", description = "Candidatures de l'entreprise récupérées")
    @ApiResponse(responseCode = "403", description = "Accès refusé - rôle COMPANY requis")
    public ResponseEntity<List<ApplicationResponseDTO>> getApplicationsByCompanyId(
        @PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getByCompanyId(id));
    }

    @GetMapping("/offer/{id}")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(
            summary = "Récupérer les candidatures par offre",
            description = "Permet à une entreprise de voir toutes les candidatures pour une offre spécifique. Rôle requis: COMPANY"
    )
    @ApiResponse(responseCode = "200", description = "Candidatures pour l'offre récupérées")
    @ApiResponse(responseCode = "403", description = "Accès refusé - rôle COMPANY requis")
    public ResponseEntity<List<ApplicationResponseDTO>> getApplicationsByOfferId(
        @PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getByOfferId(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(
            summary = "Mettre à jour le statut d'une candidature",
            description = "Permet à une entreprise de changer le statut d'une candidature (acceptée, refusée, en attente). Rôle requis: COMPANY"
    )
    @ApiResponse(responseCode = "200", description = "Statut mis à jour")
    @ApiResponse(responseCode = "403", description = "Accès refusé - rôle COMPANY requis")
    public ResponseEntity<ApplicationResponseDTO> updateApplicationStatus(
        @PathVariable Long id,
        @RequestParam ApplicationStatus status) {
        return ResponseEntity.ok(applicationService.updateStatus(id, status));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(
            summary = "Modifier une candidature",
            description = "Permet à un étudiant de modifier sa candidature (CV et lettre de motivation). Rôle requis: STUDENT"
    )
    @ApiResponse(responseCode = "200", description = "Candidature modifiée")
    @ApiResponse(responseCode = "403", description = "Accès refusé - rôle STUDENT requis")
    public ResponseEntity<ApplicationResponseDTO> updateApplication(
        @PathVariable Long id,
        @RequestParam("studentId") Long studentId,   
        @RequestParam("offerId") Long offerId,      
        @RequestPart(name = "cv",          required = false) MultipartFile cv,
        @RequestPart(name = "coverLetter", required = false) MultipartFile coverLetter
    ) {
        ApplicationRequestDTO dto = new ApplicationRequestDTO(studentId, offerId);

        ApplicationResponseDTO updated = applicationService
            .updateApplication(id, dto, cv, coverLetter);

        return ResponseEntity.ok(updated);
    }

   @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(
            summary = "Supprimer une candidature",
            description = "Permet à un étudiant de supprimer sa candidature. Rôle requis: STUDENT"
    )
    @ApiResponse(responseCode = "204", description = "Candidature supprimée")
    @ApiResponse(responseCode = "403", description = "Accès refusé - rôle STUDENT requis")
    public ResponseEntity<Void> deleteApplication(
        @PathVariable Long id,
        @RequestParam Long studentId) {
        applicationService.delete(id, studentId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{id}/bundle")
    @PreAuthorize("hasRole('COMPANY') or hasRole('STUDENT')")
    @Operation(
            summary = "Télécharger les documents d'une candidature",
            description = "Permet de télécharger un ZIP contenant CV et lettre de motivation. Rôles requis: COMPANY ou STUDENT"
    )
    @ApiResponse(responseCode = "200", description = "Documents téléchargés")
    @ApiResponse(responseCode = "403", description = "Accès refusé - rôle COMPANY ou STUDENT requis")
    public ResponseEntity<StreamingResponseBody> downloadApplicationBundle(@PathVariable Long id) {
        StreamingResponseBody stream = outputStream -> {
            applicationService.streamApplicationZip(id, outputStream);
        };
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"application_" + id + "_documents.zip\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(stream);
    }

    @GetMapping("/company-applications")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(
            summary = "Récupérer les candidatures de mon entreprise",
            description = "Permet à une entreprise connectée de récupérer toutes ses candidatures. Rôle requis: COMPANY"
    )
    @ApiResponse(responseCode = "200", description = "Candidatures de l'entreprise récupérées")
    @ApiResponse(responseCode = "403", description = "Accès refusé - rôle COMPANY requis")
    public ResponseEntity<List<ApplicationResponseDTO>> getApplicationsForCompany(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<ApplicationResponseDTO> applications = applicationService.getByCompanyId(user.getId());
        return ResponseEntity.ok(applications);
    }

}