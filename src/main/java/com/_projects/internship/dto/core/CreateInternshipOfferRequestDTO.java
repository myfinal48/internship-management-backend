package com._projects.internship.dto.core;

import java.util.List;

import com._projects.internship.model.core.Sector;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateInternshipOfferRequestDTO {
  @NotBlank(message = "Title can not be empty")
  private String title;
  @NotBlank(message = "Description can not be empty")
  private String description;
  @NotBlank(message = "sector can not be empty")
  private Sector sector;
  @NotBlank(message = "Location can not be empty")
  private String location;
  private List<String> skills;
  private Integer length;
  private Long companyId;
}
