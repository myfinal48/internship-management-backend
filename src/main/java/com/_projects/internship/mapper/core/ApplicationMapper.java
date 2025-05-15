package com._projects.internship.mapper.core;

import org.springframework.stereotype.Component;

import com._projects.internship.dto.core.ApplicationRequestDTO;
import com._projects.internship.dto.core.ApplicationResponseDTO;
import com._projects.internship.model.core.Application;
import com._projects.internship.model.core.ApplicationStatus;
import com._projects.internship.model.core.InternshipOffer;
import com._projects.internship.model.security.User;

@Component
public class ApplicationMapper {
    public static Application toEntity(ApplicationRequestDTO dto, User student, InternshipOffer offer, String cvPath, String coverLetterPath) {
        return Application.builder()
            .student(student)
            .offer(offer)
            .cvPath(cvPath)
            .coverLetterPath(coverLetterPath)
            .status(ApplicationStatus.PENDING)
            .build();
    }

    public static ApplicationResponseDTO toResponseDto(Application application) {
        return new ApplicationResponseDTO(
            application.getId(),
            application.getStudent().getId(),
            application.getOffer().getId(),
            application.getCvPath(),
            application.getCoverLetterPath(),
            application.getStatus(),
            application.getApplicationDate()
        );
    }
}
