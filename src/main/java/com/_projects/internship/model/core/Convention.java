package com._projects.internship.model.core;

import java.time.LocalDate;
import java.util.List;

import com._projects.internship.model.security.User;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.ElementCollection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Convention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private User company;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private User teacher;
    
    private String title;
    private String description;
    private String location;
    
    @ElementCollection
    private List<String> skills;
    
    private Integer length;
  
    private LocalDate creationDate;

    @Enumerated(EnumType.STRING)
    private ConventionStatus status;

    private String rejectionReason;
    private String pdfPath;
    private String signedPdfPath;
}
