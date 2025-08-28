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
@Tag(name = "convention-controller", description = "Internship convention management")
public class ConventionController {

    private final ConventionService conventionService;
    private final ConventionStorageService conventionStorageService;

    @PostMapping("/create-from-application/{applicationId}")
    @Operation(
            summary = "Create a convention after application acceptance",
            description = "Allows creating an internship convention after an application has been accepted. Accessible to companies and administrators."
    )
    @ApiResponse(responseCode = "200", description = "Convention created successfully")
    @ApiResponse(responseCode = "404", description = "Application not found")
    @ApiResponse(responseCode = "409", description = "Convention already exists")
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
            summary = "Upload signed convention (PDF)",
            description = "Allows uploading the signed PDF of a convention. Accessible to concerned companies and students."
    )
    @ApiResponse(responseCode = "200", description = "Signed PDF uploaded successfully")
    @ApiResponse(responseCode = "400", description = "Invalid file")
    @ApiResponse(responseCode = "404", description = "Convention not found")
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
            summary = "Retrieve convention by ID",
            description = "Allows retrieving details of a specific convention. Accessible to all authenticated users."
    )
    @ApiResponse(responseCode = "200", description = "Convention found")
    @ApiResponse(responseCode = "404", description = "Convention not found")
    public ResponseEntity<ConventionResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(conventionService.getConventionById(id));
    }

    @GetMapping
    @Operation(
            summary = "Retrieve all conventions",
            description = "Allows retrieving the list of all conventions. Accessible to administrators."
    )
    @ApiResponse(responseCode = "200", description = "List of conventions retrieved")
    public ResponseEntity<List<ConventionResponseDTO>> getAll() {
        return ResponseEntity.ok(conventionService.getAllConventions());
    }

    @GetMapping("/teacher/{teacherId}")
    @Operation(
            summary = "Retrieve all conventions in a teacher's sector",
            description = "Allows a teacher to see all conventions in their field of activity. Required role: TEACHER"
    )
    @ApiResponse(responseCode = "200", description = "Sector conventions retrieved")
    public ResponseEntity<List<ConventionResponseDTO>> getConventionsForTeacher(@PathVariable Long teacherId) {
        List<ConventionResponseDTO> conventions = conventionService.getConventionsForTeacher(teacherId);
        return ResponseEntity.ok(conventions);
    }

    @GetMapping("/company/{companyId}")
    @Operation(
            summary = "Retrieve all conventions of a company",
            description = "Allows a company to see all its internship conventions. Required role: COMPANY"
    )
    @ApiResponse(responseCode = "200", description = "Company conventions retrieved")
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
            summary = "Validate convention by teacher",
            description = "Allows a teacher to validate an internship convention. Required role: TEACHER"
    )
    @ApiResponse(responseCode = "200", description = "Convention validated by teacher")
    public ResponseEntity<ConventionResponseDTO> validate(@PathVariable Long id) {
        return ResponseEntity.ok(conventionService.validateByTeacher(id));
    }

    @PutMapping("/{id}/reject-by-teacher")
    @Operation(
            summary = "Reject convention by teacher",
            description = "Allows a teacher to reject a convention with a reason. Required role: TEACHER"
    )
    @ApiResponse(responseCode = "200", description = "Convention rejected by teacher")
    public ResponseEntity<ConventionResponseDTO> rejectTeacher(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(conventionService.rejectByTeacher(id, body.get("reason")));
    }

    @PutMapping("/{id}/approve-by-admin")
    @Operation(
            summary = "Approve convention by administrator",
            description = "Allows an administrator to definitively approve a convention. Required role: ADMIN"
    )
    @ApiResponse(responseCode = "200", description = "Convention approved by administrator")
    public ResponseEntity<ConventionResponseDTO> approve(@PathVariable Long id) {
        return ResponseEntity.ok(conventionService.approveByAdmin(id));
    }

    @PutMapping("/{id}/reject-by-admin")
    @Operation(
            summary = "Reject convention by administrator",
            description = "Allows an administrator to reject a convention with a reason. Required role: ADMIN"
    )
    @ApiResponse(responseCode = "200", description = "Convention rejected by administrator")
    public ResponseEntity<ConventionResponseDTO> rejectAdmin(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(conventionService.rejectByAdmin(id, body.get("reason")));
    }

    @GetMapping("/{id}/download-pdf")
    @Operation(
            summary = "Download convention PDF",
            description = "Allows downloading the PDF of a convention. Accessible to concerned parties (student, company, teacher, admin)."
    )
    @ApiResponse(responseCode = "200", description = "PDF downloaded successfully")
    @ApiResponse(responseCode = "404", description = "Convention not found")
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
            summary = "Update convention by company before validation",
            description = "Allows a company to modify a convention before it is validated. Required role: COMPANY"
    )
    @ApiResponse(responseCode = "200", description = "Convention updated")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "409", description = "Convention cannot be modified")
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
            summary = "Regenerate convention PDF",
            description = "Allows regenerating the PDF of a convention after modification. Accessible to administrators."
    )
    @ApiResponse(responseCode = "200", description = "PDF regenerated successfully")
    public ResponseEntity<Void> regeneratePdf(@PathVariable Long id) {
        conventionService.regeneratePdfForConvention(id);
        return ResponseEntity.ok().build();
    }
}