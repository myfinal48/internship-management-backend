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
@Tag(name = "internship-offer-controller", description = "Gestion des offres de stage")
public class InternshipOfferController {
  private final InternshipOfferService internshipOfferService;

  @GetMapping("/{id}")
  @Operation(
          summary = "Récupérer une offre de stage par ID",
          description = "Permet de récupérer les détails d'une offre de stage spécifique. Accessible à tous."
  )
  @ApiResponse(responseCode = "200", description = "Offre de stage trouvée")
  @ApiResponse(responseCode = "404", description = "Offre de stage non trouvée")
  public ResponseEntity<GetInternshipOfferResponseDTO> getInternshipOfferById(@PathVariable Long id)
      throws Exception {
    return ResponseEntity.ok(InternshipOfferMapper.toGetResponseDTO(internshipOfferService.getInternshipById(id)));
  }

  @PostMapping
  @PreAuthorize("hasRole('COMPANY')")
  @Operation(
          summary = "Créer une offre de stage",
          description = "Permet à une entreprise de créer une nouvelle offre de stage. Rôle requis: COMPANY"
  )
  @ApiResponse(responseCode = "200", description = "Offre de stage créée avec succès")
  @ApiResponse(responseCode = "403", description = "Accès refusé - rôle COMPANY requis")
  public ResponseEntity<GetInternshipOfferResponseDTO> createInternshipOffer(
      @Valid @RequestBody CreateInternshipOfferRequestDTO dto) throws Exception {
    return ResponseEntity.ok(InternshipOfferMapper.toGetResponseDTO(internshipOfferService.createInternshipOffer(dto)));
  }

  @PutMapping
  @PreAuthorize("hasRole('COMPANY')")
  @Operation(
          summary = "Modifier une offre de stage",
          description = "Permet à une entreprise de modifier une offre de stage existante. Rôle requis: COMPANY"
  )
  @ApiResponse(responseCode = "200", description = "Offre de stage modifiée avec succès")
  @ApiResponse(responseCode = "403", description = "Accès refusé - rôle COMPANY requis")
  public ResponseEntity<GetInternshipOfferResponseDTO> updateInternshipOffer(@RequestParam Long id,
      @Valid @RequestBody CreateInternshipOfferRequestDTO dto) throws Exception {
    return ResponseEntity.ok(InternshipOfferMapper.toGetResponseDTO(internshipOfferService.updateInternshipOffer(id,dto)));
  }

  @PostMapping("/{id}/activate")
  @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
  @Operation(
          summary = "Activer une offre de stage",
          description = "Permet d'activer une offre de stage pour la rendre visible aux étudiants. Rôles requis: COMPANY ou ADMIN"
  )
  @ApiResponse(responseCode = "200", description = "Offre de stage activée")
  @ApiResponse(responseCode = "403", description = "Accès refusé - rôle COMPANY ou ADMIN requis")
  public ResponseEntity<GetInternshipOfferResponseDTO> activateInternshipOffer(@PathVariable Long id)
      throws Exception {
    return ResponseEntity
        .ok(InternshipOfferMapper.toGetResponseDTO(internshipOfferService.activateInternshipOffer(id)));
  }

  @PostMapping("/{id}/inactivate")
  @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
  @Operation(
          summary = "Désactiver une offre de stage",
          description = "Permet de désactiver une offre de stage pour la retirer de la liste visible. Rôles requis: COMPANY ou ADMIN"
  )
  @ApiResponse(responseCode = "200", description = "Offre de stage désactivée")
  @ApiResponse(responseCode = "403", description = "Accès refusé - rôle COMPANY ou ADMIN requis")
  public ResponseEntity<GetInternshipOfferResponseDTO> inactivateInternshipOffer(@PathVariable Long id)
      throws Exception {
    return ResponseEntity
        .ok(InternshipOfferMapper.toGetResponseDTO(internshipOfferService.inactivateInternshipOffer(id)));
  }

  @PostMapping("/{id}/complete")
  @PreAuthorize("hasAnyRole('COMPANY', 'ADMIN')")
  @Operation(
          summary = "Marquer une offre de stage comme complétée",
          description = "Permet de marquer une offre de stage comme complétée (poste pourvu). Rôles requis: COMPANY ou ADMIN"
  )
  @ApiResponse(responseCode = "200", description = "Offre de stage marquée comme complétée")
  @ApiResponse(responseCode = "403", description = "Accès refusé - rôle COMPANY ou ADMIN requis")
  public ResponseEntity<GetInternshipOfferResponseDTO> completeInternshipOffer(@PathVariable Long id)
      throws Exception {
    return ResponseEntity
        .ok(InternshipOfferMapper.toGetResponseDTO(internshipOfferService.completeInternshipOffer(id)));
  }

  @GetMapping
  @Operation(
          summary = "Rechercher et filtrer les offres de stage",
          description = "Permet de rechercher des offres de stage avec des filtres (secteur, localisation, durée, statut, entreprise). Accessible à tous."
  )
  @ApiResponse(responseCode = "200", description = "Liste des offres filtrées récupérée")
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
