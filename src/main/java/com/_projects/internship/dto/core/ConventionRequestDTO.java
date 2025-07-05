package com._projects.internship.dto.core;

import java.time.LocalDate;
import java.util.List;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConventionRequestDTO{
    private Long id;
    private String title;
    private String description;
    private String location;
    private List<String> skills;
    private Integer length;
    private Long companyId;
    private Long studentId;
    private Long teacherId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String companyName;
    private String companyAddress;
    private String supervisorName;
    private String supervisorEmail;
    private String objectives;
    private Integer weeklyHours;
    private Long offerId;
    private Long applicationId;
    private Long companyInfoId;


}
