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
import com._projects.internship.dto.core.GetInternshipOfferResponseDTO;
import com._projects.internship.mapper.core.InternshipOfferMapper;
import com._projects.internship.model.core.OfferStatus;
import com._projects.internship.service.core.InternshipOfferService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/offers")
@RequiredArgsConstructor
public class InternshipOfferController {
  private final InternshipOfferService internshipOfferService;

  @GetMapping("/{id}")
  public ResponseEntity<GetInternshipOfferResponseDTO> getInternshipOfferById(@PathVariable Long id)
      throws Exception {
    return ResponseEntity.ok(InternshipOfferMapper.toGetResponseDTO(internshipOfferService.getInternshipById(id)));
  }

  @PostMapping
  @PreAuthorize("hasRole('COMPANY')")
  public ResponseEntity<GetInternshipOfferResponseDTO> createInternshipOffer(
      @RequestBody CreateInternshipOfferRequestDTO dto) throws Exception {
    return ResponseEntity.ok(InternshipOfferMapper.toGetResponseDTO(internshipOfferService.createInternshipOffer(dto)));
  }

  @PutMapping
  @PreAuthorize("hasRole('COMPANY')")
  public ResponseEntity<GetInternshipOfferResponseDTO> updateInternshipOffer(@RequestParam Long id,
      @RequestBody CreateInternshipOfferRequestDTO dto) throws Exception {
    return ResponseEntity.ok(InternshipOfferMapper.toGetResponseDTO(internshipOfferService.updateInternshipOffer(id,dto)));
  }

  @PostMapping("/{id}/activate")
  @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
  public ResponseEntity<GetInternshipOfferResponseDTO> activateInternshipOffer(@PathVariable Long id)
      throws Exception {
    return ResponseEntity
        .ok(InternshipOfferMapper.toGetResponseDTO(internshipOfferService.activateInternshipOffer(id)));
  }

  @PostMapping("/{id}/inactivate")
  @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
  public ResponseEntity<GetInternshipOfferResponseDTO> inactivateInternshipOffer(@PathVariable Long id)
      throws Exception {
    return ResponseEntity
        .ok(InternshipOfferMapper.toGetResponseDTO(internshipOfferService.inactivateInternshipOffer(id)));
  }

  @PostMapping("/{id}/complete")
  @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
  public ResponseEntity<GetInternshipOfferResponseDTO> completeInternshipOffer(@PathVariable Long id)
      throws Exception {
    return ResponseEntity
        .ok(InternshipOfferMapper.toGetResponseDTO(internshipOfferService.completeInternshipOffer(id)));
  }

  @GetMapping
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
