package com._projects.internship.dto.core;

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
    private String startDate;
    private String endDate;
}
