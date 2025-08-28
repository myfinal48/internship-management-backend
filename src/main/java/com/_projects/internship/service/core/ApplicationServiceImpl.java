package com._projects.internship.service.core;

import java.time.LocalDateTime;
import java.util.List;
import java.util.zip.ZipOutputStream;
import java.io.OutputStream;
import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com._projects.internship.dto.core.ApplicationRequestDTO;
import com._projects.internship.dto.core.ApplicationResponseDTO;
import com._projects.internship.exceptions.core.DuplicateApplicationException;
import com._projects.internship.exceptions.core.ResourceNotFoundException;
import com._projects.internship.exceptions.core.StorageException;
import com._projects.internship.mapper.core.ApplicationMapper;
import com._projects.internship.model.core.Application;
import com._projects.internship.model.core.ApplicationStatus;
import com._projects.internship.model.security.Role;
import com._projects.internship.model.security.User;
import com._projects.internship.repository.core.ApplicationRepository;
import com._projects.internship.repository.core.InternshipOfferRepository;
import com._projects.internship.repository.security.UserRepository;
import com._projects.internship.service.notification.NotificationHelper;
import com._projects.internship.service.storage.ApplicationStorageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final InternshipOfferRepository internshipOfferRepository;
    private final ApplicationStorageService appStorage;
    private final NotificationHelper notificationHelper;

    @Override
    @Transactional
    public ApplicationResponseDTO apply(ApplicationRequestDTO dto,
            MultipartFile cv,
            MultipartFile coverLetter) {
        User student = userRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + dto.getStudentId()));
        if (!Role.STUDENT.equals(student.getRole())) {
            throw new IllegalArgumentException("User is not a student");
        }
        var offer = internshipOfferRepository.findById(dto.getOfferId())
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found with id " + dto.getOfferId()));

        if (applicationRepository.existsByStudentIdAndOfferId(dto.getStudentId(), dto.getOfferId())) {
            throw new DuplicateApplicationException("Application already exists");
        }

        Application app = Application.builder()
                .student(student)
                .offer(offer)
                .status(ApplicationStatus.PENDING)
                .applicationDate(LocalDateTime.now())
                .cvPath("")
                .coverLetterPath("")
                .build();
        app = applicationRepository.save(app);

        String cvPath = appStorage.store(cv, app.getId());
        String coverPath = appStorage.store(coverLetter, app.getId());

        app.setCvPath(cvPath);
        app.setCoverLetterPath(coverPath);
        Application saved = applicationRepository.save(app);
        
        // Send notification to the company
        notificationHelper.notifyNewApplication(saved, student);

        return mapToResponseDTO(saved);
    }

    @Override
    @Transactional
    public ApplicationResponseDTO updateStatus(Long id, ApplicationStatus status) {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id " + id));

        app.setStatus(status);
        Application updated = applicationRepository.save(app);
        
        // Send notification to the student about the application decision
        User company = updated.getInternshipOffer().getCompany();
        notificationHelper.notifyApplicationDecision(updated, company);
        
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
    public List<ApplicationResponseDTO> getByStudentId(Long studentId) {
        User student = userRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + studentId));
        if (!Role.STUDENT.equals(student.getRole())) {
            throw new IllegalArgumentException("User is not a student");
        }
        return applicationRepository.findByStudentId(studentId)
            .stream()
            .map(this::mapToResponseDTO)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponseDTO> getByCompanyId(Long companyId) {
        User company = userRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id " + companyId));
        if (!Role.COMPANY.equals(company.getRole())) {
            throw new SecurityException("User with id " + companyId + " is not a company");
        }
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
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id " + id));
        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new IllegalStateException("Cannot update an application that is already processed.");
        }
        if (!app.getStudent().getId().equals(dto.getStudentId())) {
            throw new SecurityException("You can only update your own application.");
        }
        if (cv != null && !cv.isEmpty()) {
            appStorage.delete(app.getCvPath());
            String newCvPath = appStorage.store(cv, id);
            app.setCvPath(newCvPath);
        }
        if (coverLetter != null && !coverLetter.isEmpty()) {
            appStorage.delete(app.getCoverLetterPath());
            String newCoverPath = appStorage.store(coverLetter, id);
            app.setCoverLetterPath(newCoverPath);
        }
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
        appStorage.delete(app.getCvPath());
        appStorage.delete(app.getCoverLetterPath());
        applicationRepository.delete(app);
    }

    @Override
    @Transactional(readOnly = true)
    public void streamApplicationZip(Long applicationId, OutputStream os) throws IOException {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found " + applicationId));
        byte[] cvBytes = appStorage.load(app.getCvPath());
        byte[] letBytes = appStorage.load(app.getCoverLetterPath());

        try (ZipOutputStream zip = new ZipOutputStream(os)) {
            java.util.zip.ZipEntry cvEntry = new java.util.zip.ZipEntry("cv_" + applicationId + ".pdf");
            zip.putNextEntry(cvEntry);
            zip.write(cvBytes);
            zip.closeEntry();

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
        return ApplicationMapper.toResponseDto(app);
    }
}
