package com._projects.internship.dto.core;

import java.time.LocalDate;

import com._projects.internship.model.core.ConventionStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
  

    public ConventionResponseDTO(Long id, String title, String description, String location, List<String> skills,
            Integer length, String companyName, String studentName, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.skills = skills;
        this.length = length;
        this.companyName = companyName;
        this.studentName = studentName;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
