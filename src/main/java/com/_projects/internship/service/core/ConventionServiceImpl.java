package com._projects.internship.service.core;

import com._projects.internship.dto.core.ConventionRequestDTO;
import com._projects.internship.dto.core.ConventionResponseDTO;
import com._projects.internship.mapper.core.ConventionMapper;
import com._projects.internship.model.core.ConventionEntity;
import com._projects.internship.model.core.ConventionStatus;
import com._projects.internship.repository.core.ConventionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConventionServiceImpl implements ConventionService {

    private final ConventionRepository repository;
    private final ConventionMapper mapper;
    private final ConventionPdfGenerationService conventionPdfGenerationService;
    private final ConventionStorageService conventionStorageService;

    @Override
    @Transactional
    public ConventionRequestDTO createConvention(ConventionRequestDTO dto) {
        // Implémentation à compléter
        return dto;
    }

    @Override
    @Transactional
    public ConventionRequestDTO updateConvention(ConventionRequestDTO dto) {
        // Implémentation à compléter
        return dto;
    }

    @Override
    @Transactional
    public ConventionResponseDTO createFromApplication(Long applicationId) {
        // Création de l'entité convention
        ConventionEntity entity = new ConventionEntity();
        entity.setStudentId(1L); // À remplacer par les vraies données de l'application
        entity.setCompanyId(1L); // À remplacer par les vraies données de l'application
        entity.setTeacherId(1L); // À remplacer par les vraies données de l'application
        entity.setCreationDate(LocalDate.now());
        entity.setStatus(ConventionStatus.PENDING);

        // Sauvegarde initiale pour obtenir l'ID
        entity = repository.save(entity);
        
        // Génération du PDF
        byte[] pdfContent = conventionPdfGenerationService.generateConventionPdf(entity);
        
        // Stockage du PDF
        String pdfPath = conventionStorageService.storeFile(
            pdfContent, 
            "convention_" + entity.getId() + ".pdf", 
            "application/pdf"
        );
        
        // Mise à jour du chemin du PDF dans l'entité
        entity.setPdfPath(pdfPath);
        entity = repository.save(entity);
        
        log.info("Convention créée avec succès pour l'application ID: {}, PDF stocké à: {}", applicationId, pdfPath);
        
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public ConventionResponseDTO validateByTeacher(Long id) {
        ConventionEntity entity = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Convention non trouvée avec l'ID: " + id));
        
        entity.setStatus(ConventionStatus.VALIDATED_BY_TEACHER);
        entity.setRejectionReason(null);
        
        // Mise à jour de l'entité
        entity = repository.save(entity);
        
        // Régénération du PDF avec le nouveau statut
        regeneratePdf(entity);
        
        log.info("Convention ID: {} validée par l'enseignant", id);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public ConventionResponseDTO rejectByTeacher(Long id, String reason) {
        ConventionEntity entity = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Convention non trouvée avec l'ID: " + id));
        
        entity.setStatus(ConventionStatus.REJECTED_BY_TEACHER);
        entity.setRejectionReason(reason);
        
        // Mise à jour de l'entité
        entity = repository.save(entity);
        
        // Régénération du PDF avec le nouveau statut
        regeneratePdf(entity);
        
        log.info("Convention ID: {} rejetée par l'enseignant. Raison: {}", id, reason);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public ConventionResponseDTO approveByAdmin(Long id) {
        ConventionEntity entity = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Convention non trouvée avec l'ID: " + id));
        
        entity.setStatus(ConventionStatus.APPROVED_BY_ADMIN);
        entity.setRejectionReason(null);
        
        // Mise à jour de l'entité
        entity = repository.save(entity);
        
        // Régénération du PDF avec le nouveau statut
        regeneratePdf(entity);
        
        log.info("Convention ID: {} approuvée par l'administrateur", id);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public ConventionResponseDTO rejectByAdmin(Long id, String reason) {
        ConventionEntity entity = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Convention non trouvée avec l'ID: " + id));
        
        entity.setStatus(ConventionStatus.REJECTED_BY_ADMIN);
        entity.setRejectionReason(reason);
        
        // Mise à jour de l'entité
        entity = repository.save(entity);
        
        // Régénération du PDF avec le nouveau statut
        regeneratePdf(entity);
        
        log.info("Convention ID: {} rejetée par l'administrateur. Raison: {}", id, reason);
        return mapper.toResponse(entity);
    }
    
    @Override
    public ConventionResponseDTO getConventionById(Long id) {
        return repository.findById(id)
            .map(mapper::toResponse)
            .orElse(null);
    }
    
    @Override
    public List<ConventionResponseDTO> getAllConventions() {
        return repository.findAll().stream()
            .map(mapper::toResponse)
            .toList();
    }
    
    /**
     * Régénère le PDF d'une convention et met à jour son chemin de stockage
     * @param entity L'entité convention
     */
    private void regeneratePdf(ConventionEntity entity) {
        try {
            // Suppression de l'ancien PDF s'il existe
            if (entity.getPdfPath() != null && !entity.getPdfPath().isEmpty()) {
                conventionStorageService.deleteFile(entity.getPdfPath());
            }
            
            // Génération du nouveau PDF
            byte[] pdfContent = conventionPdfGenerationService.generateConventionPdf(entity);
            
            // Stockage du nouveau PDF
            String pdfPath = conventionStorageService.storeFile(
                pdfContent, 
                "convention_" + entity.getId() + ".pdf", 
                "application/pdf"
            );
            
            // Mise à jour du chemin du PDF dans l'entité
            entity.setPdfPath(pdfPath);
            repository.save(entity);
            
            log.info("PDF régénéré pour la convention ID: {}, nouveau chemin: {}", entity.getId(), pdfPath);
        } catch (Exception e) {
            log.error("Erreur lors de la régénération du PDF pour la convention ID: {}", entity.getId(), e);
            throw new RuntimeException("Erreur lors de la régénération du PDF", e);
        }
    }
}
