package com._projects.internship.service.core;

import java.time.LocalDateTime;
import java.util.List;
import java.util.zip.ZipOutputStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com._projects.internship.dto.core.ApplicationRequestDTO;
import com._projects.internship.dto.core.ApplicationResponseDTO;
import com._projects.internship.exceptions.core.DuplicateApplicationException;
import com._projects.internship.exceptions.core.ResourceNotFoundException;
import com._projects.internship.exceptions.core.StorageException;
import com._projects.internship.model.core.Application;
import com._projects.internship.model.core.ApplicationStatus;
import com._projects.internship.model.security.Role;
import com._projects.internship.model.security.User;
import com._projects.internship.repository.core.ApplicationRepository;
import com._projects.internship.repository.core.InternshipOfferRepository;
import com._projects.internship.repository.security.UserRepository;
import com._projects.internship.service.storage.ApplicationStorageService;

import java.io.OutputStream;
import java.io.IOException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final InternshipOfferRepository internshipOfferRepository;
    private final ApplicationStorageService appStorage;  // ← utilisation du service de stockage

    @Override
    @Transactional
    public ApplicationResponseDTO apply(ApplicationRequestDTO dto,
                                        MultipartFile cv,
                                        MultipartFile coverLetter) {
        // 1. Validation utilisateur & offre (inchangé)
        User student = userRepository.findById(dto.getStudentId())
            .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + dto.getStudentId()));
        if (!Role.STUDENT.equals(student.getRole())) {
            throw new IllegalArgumentException("User is not a student");
        }
        var offer = internshipOfferRepository.findById(dto.getOfferId())
            .orElseThrow(() -> new ResourceNotFoundException("Offer not found with id " + dto.getOfferId()));


        // 2. Pas de doublon (inchangé)
        if (applicationRepository.existsByStudentIdAndOfferId(dto.getStudentId(), dto.getOfferId())) {
            throw new DuplicateApplicationException("Application already exists");
        } 
    

       // 3. Création initiale pour obtenir l'ID
        Application app = Application.builder()
            .student(student)
            .offer(offer)
            .status(ApplicationStatus.PENDING)
            .applicationDate(LocalDateTime.now())
            .cvPath("")             // ← Valeur non‐nulle temporaire pour éviter NOT NULL
            .coverLetterPath("")    // ← Idem
            .build();
        app = applicationRepository.save(app);  // ← SAUVEGARDE 1


        // 4. Upload des fichiers en MinIO
        String cvPath   = appStorage.store(cv, app.getId());
        String coverPath = appStorage.store(coverLetter, app.getId());

        // 5. Mise à jour des chemins et sauvegarde finale
        app.setCvPath(cvPath);                     // ← A présent non-null
        app.setCoverLetterPath(coverPath);         // ← A présent non-null
        Application saved = applicationRepository.save(app);  // ← SAUVEGARDE 2

        // 6. Retour DTO
        return mapToResponseDTO(saved);
    }


    @Override
    @Transactional
    public ApplicationResponseDTO updateStatus(Long id, ApplicationStatus status) {
        Application app = applicationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Application not found with id " + id));

        app.setStatus(status);
        Application updated = applicationRepository.save(app);
        return mapToResponseDTO(updated);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponseDTO> getAll() {
        return applicationRepository.findAll()
            .stream()
            .map(this::mapToResponseDTO)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponseDTO> getByCompanyId(Long companyId) {
        return applicationRepository.findByOfferCompanyId(companyId)
            .stream()
            .map(this::mapToResponseDTO)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponseDTO> getByOfferId(Long offerId) {
        return applicationRepository.findByOfferId(offerId)
            .stream()
            .map(this::mapToResponseDTO)
            .toList();
    }


    @Override
    @Transactional
    public ApplicationResponseDTO updateApplication(Long id,
                    ApplicationRequestDTO dto,
                    MultipartFile cv,
                    MultipartFile coverLetter) {

        // 1) Récupère l’application et lève 404 si pas trouvée
        Application app = applicationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found with id " + id));

        // 2) Ne laisse modifier que si statut == PENDING
        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new IllegalStateException("Cannot update an application that is already processed.");
        }

        // 3) Sécurité : vérifie que l’étudiant modifie sa propre candidature
        if (!app.getStudent().getId().equals(dto.getStudentId())) {
            throw new SecurityException("You can only update your own application.");
        }

        // 4) Si on fournit un nouveau CV, supprime l’ancien dans MinIO et upload le nouveau
        if (cv != null && !cv.isEmpty()) {
            appStorage.delete(app.getCvPath());                // ← suppression de l’ancien CV
            String newCvPath = appStorage.store(cv, id);       // ← upload du nouveau
            app.setCvPath(newCvPath);                          // ← mise à jour du chemin
        }

        // 5) Même logique pour la lettre de motivation
        if (coverLetter != null && !coverLetter.isEmpty()) {
            appStorage.delete(app.getCoverLetterPath());       // ← suppression de l’ancienne lettre
            String newCoverPath = appStorage.store(coverLetter, id);  
            app.setCoverLetterPath(newCoverPath);
        }

        // 6) Sauvegarde finale
        Application updated = applicationRepository.save(app);
        return mapToResponseDTO(updated);
    }


    @Override
    @Transactional
    public void delete(Long id, Long studentId) {
        Application app = applicationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found with id " + id));

        if (!app.getStudent().getId().equals(studentId)) {
            throw new SecurityException("You are not authorized to delete this application.");
        }

        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new IllegalStateException("Cannot delete an application that has already been processed.");
        }

        // Supprimer les fichiers associés si besoin
        appStorage.delete(app.getCvPath());           // ← Nouvelle ligne : supprime le CV stocké
        appStorage.delete(app.getCoverLetterPath());  // ← Nouvelle ligne : supprime la lettre

        applicationRepository.delete(app);
    }


    @Override
    @Transactional(readOnly = true)
    public void streamApplicationZip(Long applicationId, OutputStream os) throws IOException {
        // 1) Récupère l'application
        Application app = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found " + applicationId));

        // 2) Charge les deux fichiers depuis MinIO
       byte[] cvBytes  = appStorage.load(app.getCvPath());
       byte[] letBytes = appStorage.load(app.getCoverLetterPath());

        // 3) Crée un ZipOutputStream
        try (ZipOutputStream zip = new ZipOutputStream(os)) {
            // Entrée pour le CV
            java.util.zip.ZipEntry cvEntry = new java.util.zip.ZipEntry("cv_" + applicationId + ".pdf");
            zip.putNextEntry(cvEntry);
            zip.write(cvBytes);
            zip.closeEntry();

            // Entrée pour la lettre
            java.util.zip.ZipEntry letEntry = new java.util.zip.ZipEntry("cover_letter_" + applicationId + ".pdf");
            zip.putNextEntry(letEntry);
            zip.write(letBytes);
            zip.closeEntry();

            zip.finish();
        } catch (IOException e) {
            throw new StorageException("Error while streaming ZIP bundle", e);
        }
    }


    private ApplicationResponseDTO mapToResponseDTO(Application app) {
        return ApplicationResponseDTO.builder()
            .id(app.getId())
            .studentId(app.getStudent().getId())
            .offerId(app.getOffer().getId())
            .cvPath(app.getCvPath())
            .coverLetterPath(app.getCoverLetterPath())
            .status(app.getStatus())
            .applicationDate(app.getApplicationDate())
            .build();
    }
}
