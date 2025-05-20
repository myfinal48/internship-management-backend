package com._projects.internship.dto.core;

import java.time.LocalDate;

import com._projects.internship.model.core.ConventionStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.util.List;
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


