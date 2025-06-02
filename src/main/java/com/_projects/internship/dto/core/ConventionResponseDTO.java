package com._projects.internship.dto.core;

import java.time.LocalDate;
import java.util.List;

import com._projects.internship.model.core.ConventionStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

/**
 * DTO pour la réponse de la convention.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConventionResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String location;
    private List<String> skills;
    private Integer length;
    private String companyName;
    private String studentName;
    private LocalDate startDate;
    private LocalDate endDate;
    private ConventionStatus status;
    private String pdfPath;
    private String signedPdfPath;
}
