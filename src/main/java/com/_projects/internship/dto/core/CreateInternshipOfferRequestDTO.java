package com._projects.internship.dto.core;

import java.util.List;

import com._projects.internship.model.core.Sector;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateInternshipOfferRequestDTO {
  @NotBlank(message = "Title can not be empty")
  @Size(min = 7, message = "Title must be at least 7 characters long")
  private String title;
  @NotBlank(message = "Description can not be empty")
  @Size(min = 10, message = "Description must be at least 10 characters long")
  private String description;
  @NotNull(message = "sector can not be empty")
  private Sector sector;
  @NotBlank(message = "Location can not be empty")
  @Size(min = 4, message = "Location must be at least 4 characters long")
  private String location;
  private List<String> skills;
  @Min(value = 1, message = "Length must be greater than 0")
  private Integer length;
  private Long companyId;
}
