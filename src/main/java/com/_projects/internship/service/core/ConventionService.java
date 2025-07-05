package com._projects.internship.service.core;

import com._projects.internship.dto.core.ConventionRequestDTO;
import com._projects.internship.dto.core.ConventionResponseDTO;

import java.util.List;

public interface ConventionService {

    ConventionResponseDTO createFromApplication(Long applicationId, ConventionRequestDTO dto);

    ConventionResponseDTO validateByTeacher(Long id);

    ConventionResponseDTO rejectByTeacher(Long id, String reason);

    ConventionResponseDTO approveByAdmin(Long id);

    ConventionResponseDTO rejectByAdmin(Long id, String reason);


    ConventionResponseDTO getConventionById(Long id);

    List<ConventionResponseDTO> getAllConventions();

    ConventionResponseDTO updateSignedPdfPath(Long id, String signedPdfPath);

    ConventionResponseDTO updateByCompany(ConventionRequestDTO dto, Long companyId);


    List<ConventionResponseDTO> getConventionsForTeacher(Long teacherId);

    List<ConventionResponseDTO> getConventionsByCompany(Long companyId);

    byte[] getConventionPdf(Long conventionId);
    

    List<com._projects.internship.dto.user.TeacherDTO> getAvailableTeachers();

    void regeneratePdfForConvention(Long id);
}
