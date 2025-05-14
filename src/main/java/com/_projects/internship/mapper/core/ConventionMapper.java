package com._projects.internship.mapper.core;

import org.springframework.stereotype.Component;
import com._projects.internship.model.core.ConventionEntity;
import com._projects.internship.dto.core.ConventionResponseDTO;
import java.util.Collections;

@Component
public class ConventionMapper {
  public ConventionResponseDTO toResponse(ConventionEntity entity) {
        ConventionResponseDTO responseDTO = new ConventionResponseDTO();
        responseDTO.setId(entity.getId());
        responseDTO.setStudentName(""); // This needs to be fetched from student service
        responseDTO.setCompanyName(""); // This needs to be fetched from company service
        responseDTO.setStatus(entity.getStatus());
        responseDTO.setPdfPath(entity.getPdfPath());
        // Other fields need to be set based on your business logic
        
        return responseDTO;
    }
    
}
