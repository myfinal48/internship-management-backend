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
  @Column(length = 1024)
  private String title;
  @Column(columnDefinition = "TEXT")
  private String description;
  @ManyToOne
  @JoinColumn(name = "sector_id")
  private Sector sector;
  @ElementCollection
  @CollectionTable(name = "offer_skills", joinColumns = @JoinColumn(name = "offer_id"))
  @Column(name = "skill", length = 2048)
  private List<String> skills;
  @Column(length = 1024)
  private String location;
  private OfferStatus status = OfferStatus.ACTIVE;
  private Integer length;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_user_id")
  private User company;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
