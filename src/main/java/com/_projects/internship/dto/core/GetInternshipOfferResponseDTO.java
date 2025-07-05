package com._projects.internship.dto.core;

import java.time.LocalDateTime;
import java.util.List;

import com._projects.internship.model.core.OfferStatus;
import com._projects.internship.model.core.Sector;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetInternshipOfferResponseDTO {
  private Long id;
  private String title;
  private String description;
  private Sector sector;
  private String location;
  private List<String> skills;
  private OfferStatus status;
  private Integer length;
  private String companyName;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
