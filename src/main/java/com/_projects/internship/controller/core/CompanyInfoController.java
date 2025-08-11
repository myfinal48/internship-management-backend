package com._projects.internship.controller.core;

import com._projects.internship.model.core.CompanyInfo;
import com._projects.internship.service.core.CompanyInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/company-info")
@RequiredArgsConstructor
@Tag(name = "company-info-controller", description = "Gestion des informations d'entreprise")
public class CompanyInfoController {
    private final CompanyInfoService companyInfoService;

    @PostMapping
    @Operation(
            summary = "Créer des informations d'entreprise",
            description = "Permet de créer de nouvelles informations d'entreprise. Accessible à tous les utilisateurs authentifiés."
    )
    @ApiResponse(responseCode = "200", description = "Informations d'entreprise créées")
    public ResponseEntity<CompanyInfo> create(@RequestBody CompanyInfo companyInfo) {
        return ResponseEntity.ok(companyInfoService.save(companyInfo));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Récupérer les informations d'une entreprise",
            description = "Permet de récupérer les informations d'une entreprise par son ID. Accessible à tous."
    )
    @ApiResponse(responseCode = "200", description = "Informations d'entreprise trouvées")
    @ApiResponse(responseCode = "404", description = "Entreprise non trouvée")
    public ResponseEntity<CompanyInfo> getById(@PathVariable Long id) {
        return companyInfoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(
            summary = "Récupérer toutes les informations d'entreprises",
            description = "Permet de récupérer la liste de toutes les informations d'entreprises. Accessible à tous."
    )
    @ApiResponse(responseCode = "200", description = "Liste des informations d'entreprises récupérée")
    public ResponseEntity<List<CompanyInfo>> getAll() {
        return ResponseEntity.ok(companyInfoService.findAll());
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Mettre à jour les informations d'une entreprise",
            description = "Permet de mettre à jour les informations d'une entreprise existante. Accessible à tous les utilisateurs authentifiés."
    )
    @ApiResponse(responseCode = "200", description = "Informations d'entreprise mises à jour")
    @ApiResponse(responseCode = "404", description = "Entreprise non trouvée")
    public ResponseEntity<CompanyInfo> update(@PathVariable Long id, @RequestBody CompanyInfo companyInfo) {
        return companyInfoService.findById(id)
                .map(existing -> {
                    companyInfo.setId(id);
                    return ResponseEntity.ok(companyInfoService.save(companyInfo));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Supprimer les informations d'une entreprise",
            description = "Permet de supprimer les informations d'une entreprise. Accessible à tous les utilisateurs authentifiés."
    )
    @ApiResponse(responseCode = "204", description = "Informations d'entreprise supprimées")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        companyInfoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
} 