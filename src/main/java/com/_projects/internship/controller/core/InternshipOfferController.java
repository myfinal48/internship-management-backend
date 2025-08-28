package com._projects.internship.controller.core;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com._projects.internship.dto.core.CreateInternshipOfferRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com._projects.internship.dto.core.GetInternshipOfferResponseDTO;
import com._projects.internship.mapper.core.InternshipOfferMapper;
import com._projects.internship.model.core.OfferStatus;
import com._projects.internship.service.core.InternshipOfferService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/offers")
@RequiredArgsConstructor
@Tag(name = "internship-offer-controller", description = "Internship offer management")
public class InternshipOfferController {
  private final InternshipOfferService internshipOfferService;

  @GetMapping("/{id}")
  @Operation(
          summary = "Retrieve internship offer by ID",
          description = "Allows retrieving details of a specific internship offer. Accessible to everyone."
  )
  @ApiResponse(responseCode = "200", description = "Internship offer found")
  @ApiResponse(responseCode = "404", description = "Internship offer not found")
  public ResponseEntity<GetInternshipOfferResponseDTO> getInternshipOfferById(@PathVariable Long id)
      throws Exception {
    return ResponseEntity.ok(InternshipOfferMapper.toGetResponseDTO(internshipOfferService.getInternshipById(id)));
  }

  @PostMapping
  @PreAuthorize("hasRole('COMPANY')")
  @Operation(
          summary = "Create an internship offer",
          description = "Allows a company to create a new internship offer. Required role: COMPANY"
  )
  @ApiResponse(responseCode = "200", description = "Internship offer created successfully")
  @ApiResponse(responseCode = "403", description = "Access denied - COMPANY role required")
  public ResponseEntity<GetInternshipOfferResponseDTO> createInternshipOffer(
      @Valid @RequestBody CreateInternshipOfferRequestDTO dto) throws Exception {
    return ResponseEntity.ok(InternshipOfferMapper.toGetResponseDTO(internshipOfferService.createInternshipOffer(dto)));
  }

  @PutMapping
  @PreAuthorize("hasRole('COMPANY')")
  @Operation(
          summary = "Update an internship offer",
          description = "Allows a company to modify an existing internship offer. Required role: COMPANY"
  )
  @ApiResponse(responseCode = "200", description = "Internship offer updated successfully")
  @ApiResponse(responseCode = "403", description = "Access denied - COMPANY role required")
  public ResponseEntity<GetInternshipOfferResponseDTO> updateInternshipOffer(@RequestParam Long id,
      @Valid @RequestBody CreateInternshipOfferRequestDTO dto) throws Exception {
    return ResponseEntity.ok(InternshipOfferMapper.toGetResponseDTO(internshipOfferService.updateInternshipOffer(id,dto)));
  }

  @PostMapping("/{id}/activate")
  @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
  @Operation(
          summary = "Activate an internship offer",
          description = "Allows activating an internship offer to make it visible to students. Required roles: COMPANY or ADMIN"
  )
  @ApiResponse(responseCode = "200", description = "Internship offer activated")
  @ApiResponse(responseCode = "403", description = "Access denied - COMPANY or ADMIN role required")
  public ResponseEntity<GetInternshipOfferResponseDTO> activateInternshipOffer(@PathVariable Long id)
      throws Exception {
    return ResponseEntity
        .ok(InternshipOfferMapper.toGetResponseDTO(internshipOfferService.activateInternshipOffer(id)));
  }

  @PostMapping("/{id}/inactivate")
  @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
  @Operation(
          summary = "Deactivate an internship offer",
          description = "Allows deactivating an internship offer to remove it from the visible list. Required roles: COMPANY or ADMIN"
  )
  @ApiResponse(responseCode = "200", description = "Internship offer deactivated")
  @ApiResponse(responseCode = "403", description = "Access denied - COMPANY or ADMIN role required")
  public ResponseEntity<GetInternshipOfferResponseDTO> inactivateInternshipOffer(@PathVariable Long id)
      throws Exception {
    return ResponseEntity
        .ok(InternshipOfferMapper.toGetResponseDTO(internshipOfferService.inactivateInternshipOffer(id)));
  }

  @PostMapping("/{id}/complete")
  @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
  @Operation(
          summary = "Mark an internship offer as completed",
          description = "Allows marking an internship offer as completed (position filled). Required roles: COMPANY or ADMIN"
  )
  @ApiResponse(responseCode = "200", description = "Internship offer marked as completed")
  @ApiResponse(responseCode = "403", description = "Access denied - COMPANY or ADMIN role required")
  public ResponseEntity<GetInternshipOfferResponseDTO> completeInternshipOffer(@PathVariable Long id)
      throws Exception {
    return ResponseEntity
        .ok(InternshipOfferMapper.toGetResponseDTO(internshipOfferService.completeInternshipOffer(id)));
  }

  @GetMapping
  @Operation(
          summary = "Search and filter internship offers",
          description = "Allows searching for internship offers with filters (sector, location, duration, status, company). Accessible to everyone."
  )
  @ApiResponse(responseCode = "200", description = "List of filtered offers retrieved")
  public ResponseEntity<List<GetInternshipOfferResponseDTO>> filterOffers(
      @RequestParam(required = false) String sector,
      @RequestParam(required = false) String location,
      @RequestParam(required = false) Integer length,
      @RequestParam(required = false) OfferStatus status,
      @RequestParam(required = false) Long companyId) throws Exception {
    return ResponseEntity.ok(InternshipOfferMapper
        .toGetResponseDTOList(
            internshipOfferService.filterInternshipOffers(sector, location, length, status, companyId)));
  }
}
