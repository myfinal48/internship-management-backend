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
@Tag(name = "application-controller", description = "Internship application management")
public class ApplicationController {
    private final ApplicationService applicationService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(
            summary = "Create an application",
            description = "Allows a student to apply for an internship offer with CV and cover letter. Required role: STUDENT"
    )
    @ApiResponse(responseCode = "201", description = "Application created successfully")
    @ApiResponse(responseCode = "403", description = "Access denied - STUDENT role required")
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
            summary = "Retrieve all applications",
            description = "Allows an administrator to view all applications in the system. Required role: ADMIN"
    )
    @ApiResponse(responseCode = "200", description = "List of applications retrieved")
    @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    public ResponseEntity<List<ApplicationResponseDTO>> getAllApplications() {
        List<ApplicationResponseDTO> list = applicationService.getAll();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/my-applications")
    @PreAuthorize("hasRole('STUDENT') or hasRole('COMPANY')")
    @Operation(
            summary = "Retrieve my applications",
            description = "Allows a student to view their applications or a company to view received applications. Required roles: STUDENT or COMPANY"
    )
    @ApiResponse(responseCode = "200", description = "Applications retrieved")
    @ApiResponse(responseCode = "403", description = "Access denied - STUDENT or COMPANY role required")
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
            summary = "Retrieve applications by company",
            description = "Allows a company to retrieve all applications for its offers. Required role: COMPANY"
    )
    @ApiResponse(responseCode = "200", description = "Company applications retrieved")
    @ApiResponse(responseCode = "403", description = "Access denied - COMPANY role required")
    public ResponseEntity<List<ApplicationResponseDTO>> getApplicationsByCompanyId(
        @PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getByCompanyId(id));
    }

    @GetMapping("/offer/{id}")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(
            summary = "Retrieve applications by offer",
            description = "Allows a company to view all applications for a specific offer. Required role: COMPANY"
    )
    @ApiResponse(responseCode = "200", description = "Applications for the offer retrieved")
    @ApiResponse(responseCode = "403", description = "Access denied - COMPANY role required")
    public ResponseEntity<List<ApplicationResponseDTO>> getApplicationsByOfferId(
        @PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getByOfferId(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('COMPANY')")
    @Operation(
            summary = "Update application status",
            description = "Allows a company to change the status of an application (accepted, rejected, pending). Required role: COMPANY"
    )
    @ApiResponse(responseCode = "200", description = "Status updated")
    @ApiResponse(responseCode = "403", description = "Access denied - COMPANY role required")
    public ResponseEntity<ApplicationResponseDTO> updateApplicationStatus(
        @PathVariable Long id,
        @RequestParam ApplicationStatus status) {
        return ResponseEntity.ok(applicationService.updateStatus(id, status));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(
            summary = "Update an application",
            description = "Allows a student to modify their application (CV and cover letter). Required role: STUDENT"
    )
    @ApiResponse(responseCode = "200", description = "Application updated")
    @ApiResponse(responseCode = "403", description = "Access denied - STUDENT role required")
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
             summary = "Delete an application",
             description = "Allows a student to delete their application. Required role: STUDENT"
     )
     @ApiResponse(responseCode = "204", description = "Application deleted")
     @ApiResponse(responseCode = "403", description = "Access denied - STUDENT role required")
     public ResponseEntity<Void> deleteApplication(
        @PathVariable Long id,
        @RequestParam Long studentId) {
        applicationService.delete(id, studentId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{id}/bundle")
    @PreAuthorize("hasRole('COMPANY') or hasRole('STUDENT')")
    @Operation(
            summary = "Download application documents",
            description = "Allows downloading a ZIP containing CV and cover letter. Required roles: COMPANY or STUDENT"
    )
    @ApiResponse(responseCode = "200", description = "Documents downloaded")
    @ApiResponse(responseCode = "403", description = "Access denied - COMPANY or STUDENT role required")
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
            summary = "Retrieve my company's applications",
            description = "Allows a logged-in company to retrieve all its applications. Required role: COMPANY"
    )
    @ApiResponse(responseCode = "200", description = "Company applications retrieved")
    @ApiResponse(responseCode = "403", description = "Access denied - COMPANY role required")
    public ResponseEntity<List<ApplicationResponseDTO>> getApplicationsForCompany(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<ApplicationResponseDTO> applications = applicationService.getByCompanyId(user.getId());
        return ResponseEntity.ok(applications);
    }

}