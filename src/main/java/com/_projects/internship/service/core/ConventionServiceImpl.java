package com._projects.internship.service.core;

import com._projects.internship.dto.core.ConventionRequestDTO;
import com._projects.internship.dto.core.ConventionResponseDTO;
import com._projects.internship.mapper.core.ConventionMapper;
import com._projects.internship.model.core.*;
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
        // TODO: Implémenter la création d'une convention à partir du DTO
        throw new UnsupportedOperationException("createConvention() n'est pas encore implémenté");
    }

    @Override
    @Transactional
    public ConventionRequestDTO updateConvention(ConventionRequestDTO dto) {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("L'ID de la convention est requis pour la mise à jour");
        }

        Convention convention = repository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException(CONVENTION_NOT_FOUND_MESSAGE + dto.getId()));

        if (convention.getStatus() != ConventionStatus.PENDING &&
                convention.getStatus() != ConventionStatus.REJECTED_BY_TEACHER) {
            throw new IllegalStateException("La convention ne peut pas être modifiée dans son état actuel: " + convention.getStatus());
        }

        // Mise à jour conditionnelle
        if (dto.getTitle() != null) convention.setTitle(dto.getTitle());
        if (dto.getDescription() != null) convention.setDescription(dto.getDescription());
        if (dto.getLocation() != null) convention.setLocation(dto.getLocation());
        if (dto.getSkills() != null) convention.setSkills(dto.getSkills());
        if (dto.getLength() != null) convention.setLength(dto.getLength());

        if (convention.getStatus() == ConventionStatus.REJECTED_BY_TEACHER) {
            convention.setStatus(ConventionStatus.PENDING);
            convention.setRejectionReason(null);
        }

        Convention savedConvention = repository.save(convention);
        regeneratePdf(savedConvention);

        log.info("Convention ID: {} mise à jour avec succès", dto.getId());
        return dto;
    }

    @Override
    @Transactional
    public ConventionResponseDTO createFromApplication(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée avec l'ID: " + applicationId));

        if (application.getStatus() != ApplicationStatus.ACCEPTED) {
            throw new IllegalStateException("Impossible de créer une convention pour une candidature non acceptée. Statut actuel: " + application.getStatus());
        }

        User teacher = userRepository.findByRole(Role.TEACHER).stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Aucun enseignant trouvé"));

        InternshipOffer offer = application.getOffer();
        Convention convention = new Convention();
        convention.setStudent(application.getStudent());
        convention.setCompany(offer.getCompany());
        convention.setTeacher(teacher);
        convention.setCreationDate(LocalDate.now());
        convention.setStatus(ConventionStatus.PENDING);
        convention.setInternshipTitle(offer.getTitle());
        convention.setInternshipDescription(offer.getDescription());
        convention.setInternshipStartDate((LocalDate) offer.getStartDate());
        convention.setInternshipEndDate((LocalDate) offer.getEndDate());
        convention.setLocation(offer.getLocation());
        convention.setSkills(offer.getSkills());
        convention.setLength(offer.getLength());

        Convention saved = repository.save(convention);
        String pdfPath = conventionPdfGenerationService.generatePdf(saved);
        saved.setPdfPath(pdfPath);
        saved = repository.save(saved);

        log.info("Convention créée avec succès pour la candidature ID: {}", applicationId);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public ConventionResponseDTO validateByTeacher(Long id) {
        Convention convention = getConventionOrThrow(id);
        convention.setStatus(ConventionStatus.VALIDATED_BY_TEACHER);
        convention.setRejectionReason(null);
        convention = repository.save(convention);
        regeneratePdf(convention);
        log.info("Convention ID: {} validée par l'enseignant", id);
        return mapper.toResponse(convention);
    }

    @Override
    @Transactional
    public ConventionResponseDTO rejectByTeacher(Long id, String reason) {
        Convention convention = getConventionOrThrow(id);
        convention.setStatus(ConventionStatus.REJECTED_BY_TEACHER);
        convention.setRejectionReason(reason);
        convention = repository.save(convention);
        regeneratePdf(convention);
        log.info("Convention ID: {} rejetée par l'enseignant. Raison: {}", id, reason);
        return mapper.toResponse(convention);
    }

    @Override
    @Transactional
    public ConventionResponseDTO approveByAdmin(Long id) {
        Convention convention = getConventionOrThrow(id);
        convention.setStatus(ConventionStatus.APPROVED_BY_ADMIN);
        convention.setRejectionReason(null);
        convention = repository.save(convention);
        regeneratePdf(convention);
        log.info("Convention ID: {} approuvée par l'administrateur", id);
        return mapper.toResponse(convention);
    }

    @Override
    @Transactional
    public ConventionResponseDTO rejectByAdmin(Long id, String reason) {
        Convention convention = getConventionOrThrow(id);
        convention.setStatus(ConventionStatus.REJECTED_BY_ADMIN);
        convention.setRejectionReason(reason);
        convention = repository.save(convention);
        regeneratePdf(convention);
        log.info("Convention ID: {} rejetée par l'administrateur. Raison: {}", id, reason);
        return mapper.toResponse(convention);
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
        Convention convention = getConventionOrThrow(id);
        convention.setSignedPdfPath(signedPdfPath);
        convention = repository.save(convention);
        log.info("Convention ID: {} mise à jour avec le chemin du PDF signé: {}", id, signedPdfPath);
        return mapper.toResponse(convention);
    }

    @Override
    @Transactional
    public ConventionResponseDTO updateByCompany(ConventionRequestDTO dto, Long companyId) {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("L'ID de la convention est requis pour la mise à jour");
        }

        Convention convention = getConventionOrThrow(dto.getId());

        if (convention.getCompany() == null || !convention.getCompany().getId().equals(companyId)) {
            throw new SecurityException("Vous n'êtes pas autorisé à modifier cette convention");
        }

        if (convention.getStatus() != ConventionStatus.PENDING &&
                convention.getStatus() != ConventionStatus.REJECTED_BY_TEACHER) {
            throw new IllegalStateException("La convention ne peut pas être modifiée dans son état actuel: " + convention.getStatus());
        }

        convention.setTitle(dto.getTitle());
        convention.setDescription(dto.getDescription());
        convention.setLocation(dto.getLocation());
        convention.setSkills(dto.getSkills());
        convention.setLength(dto.getLength());

        if (convention.getStatus() == ConventionStatus.REJECTED_BY_TEACHER) {
            convention.setStatus(ConventionStatus.PENDING);
            convention.setRejectionReason(null);
            log.info("Statut de la convention ID: {} réinitialisé à PENDING après modification par l'entreprise", dto.getId());
        }

        convention = repository.save(convention);
        regeneratePdf(convention);

        log.info("Convention ID: {} mise à jour avec succès par l'entreprise ID: {}", dto.getId(), companyId);
        return mapper.toResponse(convention);
    }

    private Convention getConventionOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException(CONVENTION_NOT_FOUND_MESSAGE + id));
    }

    private void regeneratePdf(Convention convention) {
        try {
            if (convention.getPdfPath() != null && !convention.getPdfPath().isEmpty()) {
                conventionStorageService.deleteFile(convention.getPdfPath());
            }

            byte[] pdfContent = conventionPdfGenerationService.generateConventionPdf(convention);
            String pdfPath = conventionStorageService.storeFile(
                    pdfContent,
                    "convention_" + convention.getId() + ".pdf",
                    "application/pdf"
            );

            convention.setPdfPath(pdfPath);
            repository.save(convention);
            log.info("PDF régénéré pour la convention ID: {}, nouveau chemin: {}", convention.getId(), pdfPath);
        } catch (Exception e) {
            log.error("Erreur lors de la régénération du PDF pour la convention ID: {}", convention.getId(), e);
            throw new RuntimeException("Erreur lors de la régénération du PDF", e);
        }
    }
}
