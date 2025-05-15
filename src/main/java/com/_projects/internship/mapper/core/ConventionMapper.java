package com._projects.internship.mapper.core;

import org.springframework.stereotype.Component;
import com._projects.internship.model.core.Convention;
import com._projects.internship.dto.core.ConventionResponseDTO;

import java.util.ArrayList;

@Component
public class ConventionMapper {
    public ConventionResponseDTO toResponse(Convention entity) {
        if (entity == null) {
            return null;
        }
        
        ConventionResponseDTO responseDTO = new ConventionResponseDTO();
        responseDTO.setId(entity.getId());
        
        // Set student name from User entity if available
        if (entity.getStudent() != null) {
            String firstName = entity.getStudent().getFirstName() != null ? entity.getStudent().getFirstName() : "";
            String lastName = entity.getStudent().getLastName() != null ? entity.getStudent().getLastName() : "";
            String studentName = firstName + " " + lastName;
            responseDTO.setStudentName(studentName.trim());
        } else {
            responseDTO.setStudentName("");
        }
        
        // Set company name from User entity if available
        if (entity.getCompany() != null) {
            String companyName = entity.getCompany().getFirstName() != null ? entity.getCompany().getFirstName() : "";
            responseDTO.setCompanyName(companyName);
        } else {
            responseDTO.setCompanyName("");
        }
        
        // Set status and paths
        responseDTO.setStatus(entity.getStatus());
        responseDTO.setPdfPath(entity.getPdfPath());
        responseDTO.setSignedPdfPath(entity.getSignedPdfPath());
        
        // Set default values for other fields to avoid nulls
        responseDTO.setTitle("");
        responseDTO.setDescription("");
        responseDTO.setLocation("");
        responseDTO.setSkills(new ArrayList<>());
        responseDTO.setLength(0);
        
        // Ensure PDF paths are not null
        if (responseDTO.getPdfPath() == null) {
            responseDTO.setPdfPath("");
        }
        
        if (responseDTO.getSignedPdfPath() == null) {
            responseDTO.setSignedPdfPath("");
        }
        
        // Set dates
        responseDTO.setStartDate(entity.getCreationDate());
        
        // Calculate end date (for example, 6 months after creation date)
        if (entity.getCreationDate() != null) {
            responseDTO.setEndDate(entity.getCreationDate().plusMonths(6));
        }
        
        return responseDTO;
    }
}
