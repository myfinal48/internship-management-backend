package com._projects.internship.mapper.core;

import com._projects.internship.dto.core.ConventionRequestDTO;
import com._projects.internship.dto.core.ConventionResponseDTO;
import com._projects.internship.model.core.Convention;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class ConventionMapper {

    public ConventionResponseDTO toResponse(Convention entity) {
        if (entity == null) {
            return null;
        }

        ConventionResponseDTO dto = new ConventionResponseDTO();
        dto.setId(entity.getId());

        // Étudiant
        if (entity.getStudent() != null) {
            String firstName = entity.getStudent().getFirstName() != null ? entity.getStudent().getFirstName() : "";
            String lastName = entity.getStudent().getLastName() != null ? entity.getStudent().getLastName() : "";
            dto.setStudentName((firstName + " " + lastName).trim());
        } else {
            dto.setStudentName("");
        }

        // Entreprise
        if (entity.getCompany() != null) {
            String companyName = entity.getCompany().getFirstName() != null ? entity.getCompany().getFirstName() : "";
            dto.setCompanyName(companyName);
        } else {
            dto.setCompanyName("");
        }

        // Informations convention
        dto.setStatus(entity.getStatus());
        dto.setPdfPath(entity.getPdfPath() != null ? entity.getPdfPath() : "");
        dto.setSignedPdfPath(entity.getSignedPdfPath() != null ? entity.getSignedPdfPath() : "");

        dto.setTitle(entity.getTitle() != null ? entity.getTitle() : "");
        dto.setDescription(entity.getDescription() != null ? entity.getDescription() : "");
        dto.setLocation(entity.getLocation() != null ? entity.getLocation() : "");
        dto.setSkills(entity.getSkills() != null ? entity.getSkills() : new ArrayList<>());
        dto.setLength(entity.getLength() != null ? entity.getLength() : 0);

        // Dates
        dto.setStartDate(entity.getCreationDate());

        if (entity.getCreationDate() != null) {
            int months = entity.getLength() != null && entity.getLength() > 0 ? entity.getLength() : 6;
            dto.setEndDate(entity.getCreationDate().plusMonths(months));
        }

        return dto;
    }

    public ConventionResponseDTO toDto(Convention entity) {
        if (entity == null) {
            return null;
        }

        ConventionResponseDTO dto = new ConventionResponseDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setLocation(entity.getLocation());
        dto.setSkills(entity.getSkills());
        dto.setLength(entity.getLength());
        dto.setCompanyName(entity.getCompany() != null ? entity.getCompany().getFirstName() : "");
        dto.setStudentName(entity.getStudent() != null ? entity.getStudent().getFirstName() + " " + entity.getStudent().getLastName() : "");
        dto.setStartDate(entity.getInternshipStartDate());
        dto.setEndDate(entity.getInternshipEndDate());
        dto.setStatus(entity.getStatus());
        dto.setPdfPath(entity.getPdfPath());
        dto.setSignedPdfPath(entity.getSignedPdfPath());

        return dto;
    }

    public Convention toEntity(ConventionRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Convention entity = new Convention();
        entity.setId(dto.getId());
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setLocation(dto.getLocation());
        entity.setSkills(dto.getSkills());
        entity.setLength(dto.getLength());

        // Note: Assurez-vous que les relations avec les entités `Student` et `Company` sont correctement gérées ailleurs
        // car elles nécessitent probablement des recherches dans la base de données pour obtenir les objets complets.

        return entity;
    }
}
