package com._projects.internship.service.core;

import com._projects.internship.dto.core.ConventionRequestDTO;
import com._projects.internship.dto.core.ConventionResponseDTO;
import com._projects.internship.mapper.core.ConventionMapper;
import com._projects.internship.model.core.Application;
import com._projects.internship.model.core.ApplicationStatus;
import com._projects.internship.model.core.Convention;
import com._projects.internship.model.core.ConventionStatus;
import com._projects.internship.model.security.Role;
import com._projects.internship.model.security.User;
import com._projects.internship.repository.core.ApplicationRepository;
import com._projects.internship.repository.core.ConventionRepository;
import com._projects.internship.repository.security.UserRepository;

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

    private static final String CONVENTION_NOT_FOUND_MESSAGE = "Convention non trouvée avec l'ID: ";
    
    private final ConventionRepository repository;
    private final ConventionMapper mapper;
    private final ConventionPdfGenerationService conventionPdfGenerationService;
    private final ConventionStorageService conventionStorageService;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;

    @Override
    @Transactional
    public ConventionRequestDTO createConvention(ConventionRequestDTO dto) {
        // Implémentation à compléter
        return dto;
    }

    @Override
    @Transactional
    public ConventionRequestDTO updateConvention(ConventionRequestDTO dto) {
        if (dto.getId() == null) {
            throw new RuntimeException("L'ID de la convention est requis pour la mise à jour");
        }
        
        // Récupérer la convention existante
        Convention convention = repository.findById(dto.getId())
            .orElseThrow(() -> new RuntimeException(CONVENTION_NOT_FOUND_MESSAGE + dto.getId()));
        
        // Vérifier si la convention peut être mise à jour (uniquement si elle est en attente ou rejetée par l'enseignant)
        if (convention.getStatus() != ConventionStatus.PENDING && 
            convention.getStatus() != ConventionStatus.REJECTED_BY_TEACHER) {
            throw new RuntimeException("La convention ne peut pas être modifiée dans son état actuel: " + convention.getStatus());
        }
        
        // Mettre à jour les champs modifiables
        // Note: Nous ne modifions pas les relations avec les utilisateurs (student, company, teacher)
        
        // Si des champs supplémentaires sont fournis dans le DTO, les mettre à jour
        if (dto.getTitle() != null) {
            // Ces champs ne sont pas directement dans l'entité Convention, mais pourraient être 
            // stockés dans des métadonnées ou utilisés pour régénérer le PDF
            log.info("Mise à jour des informations supplémentaires pour la convention ID: {}", dto.getId());
        }
        
        // Réinitialiser le statut à PENDING si la convention était rejetée
        if (convention.getStatus() == ConventionStatus.REJECTED_BY_TEACHER) {
            convention.setStatus(ConventionStatus.PENDING);
            convention.setRejectionReason(null);
        }
        
        // Sauvegarder les modifications
        convention = repository.save(convention);
        
        // Régénérer le PDF avec les nouvelles informations
        regeneratePdf(convention);
        
        log.info("Convention ID: {} mise à jour avec succès", dto.getId());
        
        return dto;
    }
    
    @Override
    @Transactional
    public ConventionResponseDTO createFromApplication(Long applicationId) {
        // Récupérer l'application (candidature) par son ID
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Candidature non trouvée avec l'ID: " + applicationId));
        
        // Vérifier que la candidature est acceptée
        if (application.getStatus() != ApplicationStatus.ACCEPTED) {
            throw new RuntimeException("Impossible de créer une convention pour une candidature qui n'est pas acceptée. Statut actuel: " + application.getStatus());
        }
        
        // Création de l'entité convention
        Convention entity = new Convention();
        
        // Récupérer les utilisateurs depuis la candidature
        User student = application.getStudent();
        User company = application.getOffer().getCompany();
        
        // Pour l'enseignant, on peut soit utiliser un enseignant par défaut, soit le récupérer d'une autre manière
        // Ici, nous utilisons un ID par défaut (à adapter selon votre logique métier)
        User teacher = userRepository.findByRole(Role.TEACHER)
            .stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Aucun enseignant trouvé dans le système"));
        
        // Récupérer les informations de l'offre de stage
        InternshipOffer offer = application.getOffer();
        
        entity.setStudent(student);
        entity.setCompany(company);
        entity.setTeacher(teacher);
        entity.setCreationDate(LocalDate.now());
        entity.setStatus(ConventionStatus.PENDING);
        
        // Transférer les informations de l'offre vers la convention
        entity.setTitle(offer.getTitle());
        entity.setDescription(offer.getDescription());
        entity.setLocation(offer.getLocation());
        entity.setSkills(offer.getSkills());
        entity.setLength(offer.getLength());

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
        
        log.info("Convention créée avec succès pour la candidature ID: {}, PDF stocké à: {}", applicationId, pdfPath);
        
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public ConventionResponseDTO validateByTeacher(Long id) {
        Convention entity = repository.findById(id)
            .orElseThrow(() -> new RuntimeException(CONVENTION_NOT_FOUND_MESSAGE + id));
        
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
        Convention entity = repository.findById(id)
            .orElseThrow(() -> new RuntimeException(CONVENTION_NOT_FOUND_MESSAGE + id));
        
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
        Convention entity = repository.findById(id)
            .orElseThrow(() -> new RuntimeException(CONVENTION_NOT_FOUND_MESSAGE + id));
        
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
        Convention entity = repository.findById(id)
            .orElseThrow(() -> new RuntimeException(CONVENTION_NOT_FOUND_MESSAGE + id));
        
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
    
    @Override
    @Transactional
    public ConventionResponseDTO updateSignedPdfPath(Long id, String signedPdfPath) {
        Convention entity = repository.findById(id)
            .orElseThrow(() -> new RuntimeException(CONVENTION_NOT_FOUND_MESSAGE + id));
        
        entity.setSignedPdfPath(signedPdfPath);
        entity = repository.save(entity);
        
        log.info("Convention ID: {} mise à jour avec le chemin du PDF signé: {}", id, signedPdfPath);
        return mapper.toResponse(entity);
    }
    
    /**
     * Régénère le PDF d'une convention et met à jour son chemin de stockage
     * @param entity L'entité convention
     */
    private void regeneratePdf(Convention entity) {
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
    
    @Override
    @Transactional
    public ConventionResponseDTO updateByCompany(ConventionRequestDTO dto, Long companyId) {
        if (dto.getId() == null) {
            throw new RuntimeException("L'ID de la convention est requis pour la mise à jour");
        }
        
        // Récupérer la convention existante
        Convention convention = repository.findById(dto.getId())
            .orElseThrow(() -> new RuntimeException(CONVENTION_NOT_FOUND_MESSAGE + dto.getId()));
        
        // Vérifier que l'entreprise qui fait la mise à jour est bien celle associée à la convention
        if (convention.getCompany() == null || !convention.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier cette convention");
        }
        
        // Vérifier si la convention peut être mise à jour (uniquement si elle est en attente ou rejetée par l'enseignant)
        if (convention.getStatus() != ConventionStatus.PENDING && 
            convention.getStatus() != ConventionStatus.REJECTED_BY_TEACHER) {
            throw new RuntimeException("La convention ne peut pas être modifiée dans son état actuel: " + convention.getStatus());
        }
        
        // Mettre à jour les champs modifiables
        // Note: Ces champs ne sont pas directement dans l'entité Convention, mais pourraient être 
        // stockés dans des métadonnées ou utilisés pour régénérer le PDF
        
        // Réinitialiser le statut à PENDING si la convention était rejetée
        if (convention.getStatus() == ConventionStatus.REJECTED_BY_TEACHER) {
            convention.setStatus(ConventionStatus.PENDING);
            convention.setRejectionReason(null);
            log.info("Statut de la convention ID: {} réinitialisé à PENDING après modification par l'entreprise", dto.getId());
        }
        
        // Sauvegarder les modifications
        convention = repository.save(convention);
        
        // Régénérer le PDF avec les nouvelles informations
        regeneratePdf(convention);
        
        log.info("Convention ID: {} mise à jour avec succès par l'entreprise ID: {}", dto.getId(), companyId);
        
        return mapper.toResponse(convention);
    }
}
