package com._projects.internship.model.core;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class ConventionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    private Long studentId;
    private Long companyId;
    private Long teacherId;

    private LocalDate creationDate;

    @Enumerated(EnumType.STRING)
    private ConventionStatus status;

    private String rejectionReason;
    private String pdfPath;
}
