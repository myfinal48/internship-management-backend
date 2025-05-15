package com._projects.internship.dto.core;

import java.time.LocalDateTime;
import com._projects.internship.model.core.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplicationResponseDTO {
    private Long id;
    private Long studentId;
    private Long offerId;
    private String cvPath;
    private String coverLetterPath;
    private ApplicationStatus status;
    private LocalDateTime applicationDate;
}
