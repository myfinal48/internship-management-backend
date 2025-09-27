package com._projects.internship.service.core;

import com._projects.internship.dto.core.ConventionRequestDTO;
import com._projects.internship.dto.core.ConventionResponseDTO;
import com._projects.internship.dto.user.TeacherDTO;
import com._projects.internship.exceptions.core.ResourceNotFoundException;
import com._projects.internship.mapper.core.ConventionMapper;
import com._projects.internship.model.core.*;
import com._projects.internship.model.security.Role;
import com._projects.internship.model.security.User;
import com._projects.internship.repository.core.ApplicationRepository;
import com._projects.internship.repository.core.ConventionRepository;
import com._projects.internship.repository.core.CompanyInfoRepository;
import com._projects.internship.repository.security.UserRepository;
import com._projects.internship.service.notification.NotificationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.context.SecurityContextHolder;
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
    private final CompanyInfoRepository companyInfoRepository;
    private final NotificationHelper notificationHelper;

    @Override
    @Transactional
    public ConventionResponseDTO createFromApplication(Long applicationId, ConventionRequestDTO dto) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application non trouvée avec l'ID: " + applicationId));

        if (!ApplicationStatus.ACCEPTED.equals(application.getStatus())) {
            throw new IllegalStateException("Impossible de créer une convention : la candidature n'est pas acceptée.");
        }

        Convention convention = new Convention();
        InternshipOffer offer = application.getInternshipOffer();

        convention.setTitle("Convention de stage - " + offer.getTitle());
        convention.setDescription(dto.getDescription());
        convention.setLocation(String.valueOf(offer.getLocation()));
        convention.setSkills(offer.getSkills() != null ? new java.util.ArrayList<>(offer.getSkills()) : null);
        convention.setLength(offer.getLength());
        convention.setStatus(ConventionStatus.PENDING);
        convention.setCreationDate(LocalDate.now());
        convention.setObjectives(dto.getObjectives());
        convention.setInternshipStartDate(dto.getStartDate());
        convention.setInternshipEndDate(dto.getEndDate());
        convention.setWeeklyHours(dto.getWeeklyHours());
        convention.setSupervisorName(dto.getSupervisorName());
        convention.setSupervisorEmail(dto.getSupervisorEmail());

        convention.setStudent(application.getStudent());
        convention.setCompany(offer.getCompany());
        convention.setInternshipOffer(offer);
        convention.setApplication(application);

        if (dto.getCompanyInfoId() != null) {
            companyInfoRepository.findById(dto.getCompanyInfoId()).ifPresent(convention::setCompanyInfo);
        } else if (offer.getCompany() != null) {
            CompanyInfo autoInfo = companyInfoRepository.findFirstByCompany(offer.getCompany());
            if (autoInfo != null) {
                convention.setCompanyInfo(autoInfo);
            }
        }

        Convention saved = repository.save(convention);

        String pdfPath = conventionPdfGenerationService.generatePdf(saved);
        saved.setPdfPath(pdfPath);
        repository.save(saved);

        log.info("Convention créée à partir de la candidature ID {} : convention ID {}", applicationId, saved.getId());
        
        // Notify teachers in the sector about the new convention
        notificationHelper.notifyNewConvention(saved, saved.getCompany());

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ConventionResponseDTO validateByTeacher(Long id) {
        Convention convention = getConventionOrThrow(id);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("Utilisateur non authentifié");
        }
        Object principal = authentication.getPrincipal();
        String currentUserEmail;
        if (principal instanceof User u) {
            currentUserEmail = u.getEmail();
        } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            currentUserEmail = ud.getUsername();
        } else {
            currentUserEmail = authentication.getName();
        }

        User teacher = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new SecurityException("Utilisateur non authentifié"));

        if (teacher.getSector() == null) {
            throw new SecurityException("L'enseignant n'a pas de secteur assigné");
        }

        if (convention.getInternshipOffer() == null ||
                convention.getInternshipOffer().getSector() == null ||
                !convention.getInternshipOffer().getSector().getId().equals(teacher.getSector().getId())) {
            throw new SecurityException("Vous n'êtes pas autorisé à valider cette convention");
        }

        convention.setStatus(ConventionStatus.VALIDATED_BY_TEACHER);
        convention.setRejectionReason(null);
        convention = repository.save(convention);
        regeneratePdf(convention);
        log.info("Convention ID: {} validée par l'enseignant", id);
        
        if (teacher != null) {
            notificationHelper.notifyConventionValidatedByTeacher(convention, teacher);
        }
        
        return mapper.toResponse(convention);
    }

    @Override
    @Transactional
    public ConventionResponseDTO rejectByTeacher(Long id, String reason) {
        Convention convention = getConventionOrThrow(id);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("Utilisateur non authentifié");
        }
        Object principal = authentication.getPrincipal();
        String currentUserEmail;
        if (principal instanceof User u) {
            currentUserEmail = u.getEmail();
        } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            currentUserEmail = ud.getUsername();
        } else {
            currentUserEmail = authentication.getName();
        }

        User teacher = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new SecurityException("Utilisateur non authentifié"));

        if (teacher.getSector() == null) {
            throw new SecurityException("L'enseignant n'a pas de secteur assigné");
        }

        if (convention.getInternshipOffer() == null ||
                convention.getInternshipOffer().getSector() == null ||
                !convention.getInternshipOffer().getSector().getId().equals(teacher.getSector().getId())) {
            throw new SecurityException("Vous n'êtes pas autorisé à rejeter cette convention");
        }

        if (convention.getStatus() == ConventionStatus.APPROVED_BY_ADMIN) {
            throw new IllegalStateException("Impossible de rejeter une convention déjà approuvée par l'administrateur");
        }

        convention.setStatus(ConventionStatus.REJECTED_BY_TEACHER);
        convention.setRejectionReason(reason);
        convention = repository.save(convention);
        regeneratePdf(convention);
        log.info("Convention ID: {} rejetée par l'enseignant. Raison: {}", id, reason);
        
        if (teacher != null) {
            notificationHelper.notifyConventionRejectedByTeacher(convention, teacher);
        }
        
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
        
        // Notify company and student about admin approval
        User admin = userRepository.findByRole(Role.ADMIN).stream().findFirst().orElse(null);
        if (admin != null) {
            notificationHelper.notifyConventionAdminDecision(convention, admin);
        }
        
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
        
        // Notify company about admin rejection
        User admin = userRepository.findByRole(Role.ADMIN).stream().findFirst().orElse(null);
        if (admin != null) {
            notificationHelper.notifyConventionAdminDecision(convention, admin);
        }
        
        return mapper.toResponse(convention);
    }

    @Override
    public ConventionResponseDTO getConventionById(Long id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Convention not found with ID: " + id));
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
            throw new IllegalStateException(
                    "La convention ne peut pas être modifiée dans son état actuel: " + convention.getStatus());
        }

        convention.setTitle(dto.getTitle());
        convention.setCompanyAddress(dto.getCompanyAddress());
        convention.setCompanyName(dto.getCompanyName());
        convention.setDescription(dto.getDescription());
        convention.setInternshipEndDate(dto.getEndDate());
        convention.setObjectives(dto.getObjectives());
        convention.setInternshipStartDate(dto.getStartDate());
        convention.setSupervisorEmail(dto.getSupervisorEmail());
        convention.setSupervisorName(dto.getSupervisorName());
        convention.setWeeklyHours(dto.getWeeklyHours());

        if (dto.getCompanyInfoId() != null) {
            companyInfoRepository.findById(dto.getCompanyInfoId()).ifPresent(convention::setCompanyInfo);
        } else if (convention.getCompany() != null) {
            CompanyInfo autoInfo = companyInfoRepository.findFirstByCompany(convention.getCompany());
            if (autoInfo != null) {
                convention.setCompanyInfo(autoInfo);
            }
        }

        if (convention.getStatus() == ConventionStatus.REJECTED_BY_TEACHER) {
            convention.setStatus(ConventionStatus.PENDING);
            convention.setRejectionReason(null);
            log.info("Statut de la convention ID: {} réinitialisé à PENDING après modification par l'entreprise",
                    dto.getId());
        }

        convention = repository.save(convention);
        regeneratePdf(convention);

        log.info("Convention ID: {} mise à jour avec succès par l'entreprise ID: {}", dto.getId(), companyId);
        return mapper.toResponse(convention);
    }

    @Override
    public List<ConventionResponseDTO> getConventionsForTeacher(Long teacherId) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Enseignant non trouvé avec l'ID: " + teacherId));

        if (teacher.getSector() == null) {
            log.warn("L'enseignant ID: {} n'a pas de secteur assigné", teacherId);
            return List.of();
        }

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
                .orElseThrow(() -> new ResourceNotFoundException(CONVENTION_NOT_FOUND_MESSAGE + id));
    }

    private void regeneratePdf(Convention convention) {
        try {
            log.info("Début de la régénération du PDF pour la convention ID: {}", convention.getId());

            if (convention.getPdfPath() != null) {
                log.info("Suppression de l'ancien PDF: {}", convention.getPdfPath());
                conventionPdfGenerationService.deletePdf(convention.getPdfPath());
            }

            log.info("Génération du nouveau PDF...");
            String newPdfPath = conventionPdfGenerationService.generatePdf(convention);
            log.info("Nouveau PDF généré avec le chemin: {}", newPdfPath);

            convention.setPdfPath(newPdfPath);
            Convention savedConvention = repository.save(convention);
            log.info("Convention sauvegardée avec le nouveau pdfPath: {}", savedConvention.getPdfPath());
            log.info("PDF régénéré avec succès pour la convention ID: {}", convention.getId());
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public byte[] getConventionPdf(Long conventionId) {
        Convention convention = getConventionOrThrow(conventionId);

        log.info("Tentative de récupération du PDF pour la convention ID: {}", conventionId);
        log.info("PDF signé: {}", convention.getSignedPdfPath());
        log.info("PDF généré: {}", convention.getPdfPath());

        String filePath = convention.getSignedPdfPath();
        if (filePath == null || filePath.isEmpty()) {
            filePath = convention.getPdfPath();
            if (filePath == null || filePath.isEmpty()) {
                throw new IllegalStateException("Aucun PDF n'est disponible pour cette convention");
            }
        }

        try {
            byte[] fileContent = conventionStorageService.getFile(filePath);
            return fileContent;
        } catch (Exception e) {
            throw e;
        }
    }

    public ConventionStorageService getConventionStorageService() {
        return conventionStorageService;
    }

    @Override
    public List<TeacherDTO> getAvailableTeachers() {
        List<User> teachers = userRepository.findByRole(Role.TEACHER);
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

    @Override
    public void regeneratePdfForConvention(Long id) {
        Convention convention = getConventionOrThrow(id);
        regeneratePdf(convention);
    }
}
