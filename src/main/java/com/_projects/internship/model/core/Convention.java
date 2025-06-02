package com._projects.internship.model.core;

import com._projects.internship.model.security.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Convention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    // Chemin du PDF original (généré)
    private String pdfPath;

    // Chemin du PDF signé
    private String signedPdfPath;

    private LocalDate internshipStartDate;
    private LocalDate internshipEndDate;

    public void setInternshipTitle(String title) {

    }

    public void setInternshipDescription(String description) {

    }
}
