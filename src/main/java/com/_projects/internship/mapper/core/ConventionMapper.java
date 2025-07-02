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
            dto.setStudentId(entity.getStudent().getId());
        } else {
            dto.setStudentName("");
        }

        // Entreprise
        if (entity.getCompany() != null) {
            dto.setCompanyId(entity.getCompany().getId());
        }
        dto.setCompanyName(entity.getCompanyName());
        dto.setCompanyAddress(entity.getCompanyAddress());
        dto.setSupervisorName(entity.getSupervisorName());
        dto.setSupervisorEmail(entity.getSupervisorEmail());
        dto.setObjectives(entity.getObjectives());
        dto.setWeeklyHours(entity.getWeeklyHours());
        
        // Information sur le secteur de l'offre de stage
        if (entity.getInternshipOffer() != null && entity.getInternshipOffer().getSector() != null) {
            dto.setSectorId(entity.getInternshipOffer().getSector().getId());
            dto.setSectorName(entity.getInternshipOffer().getSector().getName());
        }

        // Informations convention
        dto.setStatus(entity.getStatus());
        dto.setPdfPath(entity.getPdfPath() != null ? entity.getPdfPath() : "");
        dto.setSignedPdfPath(entity.getSignedPdfPath() != null ? entity.getSignedPdfPath() : "");
        dto.setRejectionReason(entity.getRejectionReason());

        dto.setTitle(entity.getTitle() != null ? entity.getTitle() : "");
        dto.setDescription(entity.getDescription() != null ? entity.getDescription() : "");
        dto.setLocation(entity.getLocation() != null ? entity.getLocation() : "");
        dto.setSkills(entity.getSkills() != null ? entity.getSkills() : new ArrayList<>());
        dto.setLength(entity.getLength() != null ? entity.getLength() : 0);

        // Dates
        dto.setStartDate(entity.getInternshipStartDate() != null ? entity.getInternshipStartDate() : entity.getCreationDate());
        dto.setEndDate(entity.getInternshipEndDate());

        if (dto.getEndDate() == null && entity.getCreationDate() != null) {
            int months = entity.getLength() != null && entity.getLength() > 0 ? entity.getLength() : 6;
            dto.setEndDate(entity.getCreationDate().plusMonths(months));
        }

        // Application liée
        if (entity.getApplication() != null) {
            dto.setApplicationId(entity.getApplication().getId());
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
        
        // Entreprise
        if (entity.getCompany() != null) {
            dto.setCompanyId(entity.getCompany().getId());
        }
        dto.setCompanyName(entity.getCompanyName());
        dto.setCompanyAddress(entity.getCompanyAddress());
        dto.setSupervisorName(entity.getSupervisorName());
        dto.setSupervisorEmail(entity.getSupervisorEmail());
        dto.setObjectives(entity.getObjectives());
        dto.setWeeklyHours(entity.getWeeklyHours());
        
        // Étudiant
        if (entity.getStudent() != null) {
            dto.setStudentId(entity.getStudent().getId());
            dto.setStudentName(entity.getStudent().getFirstName() + " " + entity.getStudent().getLastName());
        }
        
        // Information sur le secteur de l'offre de stage
        if (entity.getInternshipOffer() != null && entity.getInternshipOffer().getSector() != null) {
            dto.setSectorId(entity.getInternshipOffer().getSector().getId());
            dto.setSectorName(entity.getInternshipOffer().getSector().getName());
        }
        
        dto.setStartDate(entity.getInternshipStartDate());
        dto.setEndDate(entity.getInternshipEndDate());
        dto.setStatus(entity.getStatus());
        dto.setPdfPath(entity.getPdfPath());
        dto.setSignedPdfPath(entity.getSignedPdfPath());
        dto.setRejectionReason(entity.getRejectionReason());

        // Application liée
        if (entity.getApplication() != null) {
            dto.setApplicationId(entity.getApplication().getId());
        }

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


        return entity;
    }
}
