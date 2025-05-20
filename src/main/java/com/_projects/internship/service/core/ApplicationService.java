package com._projects.internship.service.core;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com._projects.internship.dto.core.ApplicationRequestDTO;
import com._projects.internship.dto.core.ApplicationResponseDTO;
import com._projects.internship.model.core.ApplicationStatus;

import java.io.OutputStream;
import java.io.IOException;

public interface ApplicationService {
    ApplicationResponseDTO apply(ApplicationRequestDTO dto, MultipartFile cv, MultipartFile coverLetter);

    ApplicationResponseDTO updateStatus(Long id, ApplicationStatus status);

    List<ApplicationResponseDTO> getAll();
    
    List<ApplicationResponseDTO> getByCompanyId(Long companyId);

    List<ApplicationResponseDTO> getByOfferId(Long offerId);

    ApplicationResponseDTO updateApplication(Long id, ApplicationRequestDTO dto, MultipartFile cv, MultipartFile coverLetter);

    void delete(Long id, Long studentId);

    void streamApplicationZip(Long applicationId, OutputStream os) throws IOException;

}
