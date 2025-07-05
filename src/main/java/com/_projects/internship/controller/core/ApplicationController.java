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

import org.springframework.http.HttpHeaders;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/applications")
@RequiredArgsConstructor
public class ApplicationController {
    private final ApplicationService applicationService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('STUDENT')")
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
    public ResponseEntity<List<ApplicationResponseDTO>> getAllApplications() {
        List<ApplicationResponseDTO> list = applicationService.getAll();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/my-applications")
    @PreAuthorize("hasRole('STUDENT') or hasRole('COMPANY')")
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
    public ResponseEntity<List<ApplicationResponseDTO>> getApplicationsByCompanyId(
        @PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getByCompanyId(id));
    }

    @GetMapping("/offer/{id}")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<List<ApplicationResponseDTO>> getApplicationsByOfferId(
        @PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getByOfferId(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<ApplicationResponseDTO> updateApplicationStatus(
        @PathVariable Long id,
        @RequestParam ApplicationStatus status) {
        return ResponseEntity.ok(applicationService.updateStatus(id, status));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('STUDENT')")
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
    public ResponseEntity<Void> deleteApplication(
        @PathVariable Long id,
        @RequestParam Long studentId) {
        applicationService.delete(id, studentId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{id}/bundle")
    @PreAuthorize("hasRole('COMPANY') or hasRole('STUDENT')")
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
    public ResponseEntity<List<ApplicationResponseDTO>> getApplicationsForCompany(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<ApplicationResponseDTO> applications = applicationService.getByCompanyId(user.getId());
        return ResponseEntity.ok(applications);
    }

}