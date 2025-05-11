package com._projects.internship.dto.core;

import java.util.List;

import com._projects.internship.model.core.Sector;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateInternshipOfferRequestDTO {
  private Long id;
  private String title;
  private String description;
  private Sector sector;
  private String location;
  private List<String> skills;
  private Integer length;
}
