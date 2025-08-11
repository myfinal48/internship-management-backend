package com._projects.internship.controller.core;

import com._projects.internship.dto.core.CreateSectorRequestData;
import com._projects.internship.model.core.Sector;
import com._projects.internship.service.core.SectorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/sectors")
@Tag(name = "sector-controller", description = "Gestion des secteurs d'activité")
public class SectorController {
    private final SectorService sectorService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Créer un nouveau secteur",
            description = "Permet à un administrateur de créer un nouveau secteur d'activité. Rôle requis: ADMIN"
    )
    @ApiResponse(responseCode = "200", description = "Secteur créé avec succès")
    @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis")
    public ResponseEntity<Sector> save(@RequestBody @Valid CreateSectorRequestData dto) throws Exception{
        return ResponseEntity.ok(sectorService.createSector(dto));
    }
    @GetMapping
    @Operation(
            summary = "Récupérer tous les secteurs",
            description = "Permet de récupérer la liste de tous les secteurs d'activité. Accessible à tous."
    )
    @ApiResponse(responseCode = "200", description = "Liste des secteurs récupérée")
    public ResponseEntity<List<Sector>> getAll(){
        return ResponseEntity.ok(sectorService.getAllSectors());
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Modifier un secteur",
            description = "Permet à un administrateur de modifier un secteur d'activité existant. Rôle requis: ADMIN"
    )
    @ApiResponse(responseCode = "200", description = "Secteur modifié avec succès")
    @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis")
    @ApiResponse(responseCode = "404", description = "Secteur non trouvé")
    public ResponseEntity<Sector> update(@PathVariable Long id,@RequestBody @Valid CreateSectorRequestData dto) throws Exception{
        return ResponseEntity.ok(sectorService.updateSector(id,dto));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Récupérer un secteur par ID",
            description = "Permet de récupérer les détails d'un secteur spécifique. Accessible à tous."
    )
    @ApiResponse(responseCode = "200", description = "Secteur trouvé")
    @ApiResponse(responseCode = "404", description = "Secteur non trouvé")
    public ResponseEntity<Sector> get(@PathVariable Long id){
        return ResponseEntity.ok(sectorService.getSectorById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Supprimer un secteur",
            description = "Permet à un administrateur de supprimer un secteur d'activité. Rôle requis: ADMIN"
    )
    @ApiResponse(responseCode = "204", description = "Secteur supprimé avec succès")
    @ApiResponse(responseCode = "403", description = "Accès refusé - rôle ADMIN requis")
    @ApiResponse(responseCode = "409", description = "Secteur utilisé par d'autres entités")
    public void delete(@PathVariable Long id){
        sectorService.deleteSector(id);
    }

}
