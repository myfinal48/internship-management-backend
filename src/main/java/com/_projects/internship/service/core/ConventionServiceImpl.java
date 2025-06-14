package com._projects.internship.service.core;

import com._projects.internship.dto.core.ConventionRequestDTO;
import com._projects.internship.dto.core.ConventionResponseDTO;
import com._projects.internship.dto.user.TeacherDTO;
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
    public ConventionResponseDTO createFromApplication(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application non trouvée avec l'ID: " + applicationId));

        if (!ApplicationStatus.ACCEPTED.equals(application.getStatus())) {
            throw new IllegalStateException("Impossible de créer une convention : la candidature n'est pas acceptée.");
        }

        Convention convention = new Convention();
        InternshipOffer  offer = application.getInternshipOffer();

        convention.setTitle("Convention de stage - " + offer.getTitle());
        convention.setDescription(offer.getDescription());
        convention.setLocation(String.valueOf(offer.getLocation()));
        convention.setSkills(offer.getSkills() != null ? new java.util.ArrayList<>(offer.getSkills()) : null);
        convention.setLength(offer.getLength());
        convention.setStatus(ConventionStatus.PENDING);
        convention.setCreationDate(LocalDate.now());

        convention.setStudent(application.getStudent());
        convention.setCompany(offer.getCompany());

        // Stocker la référence à l'offre de stage au lieu d'un enseignant spécifique
        convention.setInternshipOffer(offer);

        Convention saved = repository.save(convention);

        String pdfPath = conventionPdfGenerationService.generatePdf(saved);
        saved.setPdfPath(pdfPath);
        repository.save(saved);

        log.info("Convention créée à partir de la candidature ID {} : convention ID {}", applicationId, saved.getId());

        return mapper.toResponse(saved);
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
        if (signedPdfPath == null || signedPdfPath.isEmpty()) {
            throw new IllegalArgumentException("Le chemin du PDF signé ne peut pas être vide");
        }

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
        convention.setSkills(dto.getSkills() != null ? new java.util.ArrayList<>(dto.getSkills()) : null);
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

    @Override
    public List<ConventionResponseDTO> getConventionsForTeacher(Long teacherId) {
        // Récupérer l'enseignant pour connaître son secteur
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Enseignant non trouvé avec l'ID: " + teacherId));

        if (teacher.getSector() == null) {
            log.warn("L'enseignant ID: {} n'a pas de secteur assigné", teacherId);
            return List.of(); // Retourne une liste vide si l'enseignant n'a pas de secteur
        }

        // Récupérer toutes les conventions dans le secteur de l'enseignant
        List<Convention> conventions = repository.findByInternshipOfferSectorId(teacher.getSector().getId());
        log.info("Récupération de {} conventions dans le secteur '{}' pour l'enseignant ID: {}",
                conventions.size(), teacher.getSector().getName(), teacherId);

        return conventions.stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public List<ConventionResponseDTO> getConventionsByCompany(Long companyId) {
        List<Convention> conventions = repository.findByCompanyId(companyId);
        log.info("Récupération de {} conventions pour l'entreprise ID: {}", conventions.size(), companyId);
        return conventions.stream()
                .map(mapper::toResponse)
                .toList();
    }

    private Convention getConventionOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException(CONVENTION_NOT_FOUND_MESSAGE + id));
    }

    private void regeneratePdf(Convention convention) {
        try {
            if (convention.getPdfPath() != null) {
                conventionPdfGenerationService.deletePdf(convention.getPdfPath());
            }
            String newPdfPath = conventionPdfGenerationService.generatePdf(convention);
            convention.setPdfPath(newPdfPath);
            repository.save(convention);
            log.info("PDF régénéré pour la convention ID: {}", convention.getId());
        } catch (Exception e) {
            log.error("Erreur lors de la régénération du PDF pour la convention ID: {}", convention.getId(), e);
        }
    }


    @Override
    public byte[] getConventionPdf(Long conventionId) {
        Convention convention = getConventionOrThrow(conventionId);

        String filePath = convention.getSignedPdfPath();
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalStateException("Aucun PDF signé n'est disponible pour cette convention");
        }

        return conventionStorageService.getFile(filePath);
    }

    public ConventionStorageService getConventionStorageService() {
        return conventionStorageService;
    }

    @Override
    public List<TeacherDTO> getAvailableTeachers() {
        List<User> teachers = userRepository.findByRole(Role.TEACHER);
        log.info("Récupération de {} enseignants disponibles", teachers.size());

        return teachers.stream()
                .map(teacher -> TeacherDTO.builder()
                        .id(teacher.getId())
                        .firstName(teacher.getFirstName() != null ? teacher.getFirstName() : "")
                        .lastName(teacher.getLastName() != null ? teacher.getLastName() : "")
                        .fullName((teacher.getFirstName() != null ? teacher.getFirstName() : "") + " " +
                                 (teacher.getLastName() != null ? teacher.getLastName() : ""))
                        .email(teacher.getEmail())
                        .build())
                .toList();
    }
}
