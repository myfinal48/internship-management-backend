package com._projects.internship.model.core;

import java.time.LocalDateTime;
import java.util.List;

import com._projects.internship.model.security.User;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class InternshipOffer {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String title;
  private String description;
  @ManyToOne
  @JoinColumn(name = "sector_id")
  private Sector sector;
  private List<String> skills;
  private String location;
  private OfferStatus status = OfferStatus.ACTIVE;
  private Integer length;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_user_id")
  private User company;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
