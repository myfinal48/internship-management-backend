package com._projects.internship.dto.core;

import java.time.LocalDate;
import java.util.List;

import com._projects.internship.model.core.ConventionStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;


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
    
    private Long companyId;
    private String companyName;
    
    private Long studentId;
    private String studentName;
    
    private Long sectorId;
    private String sectorName;
    
    private LocalDate startDate;
    private LocalDate endDate;
    private ConventionStatus status;
    private String pdfPath;
    private String signedPdfPath;
    private String rejectionReason;
}
