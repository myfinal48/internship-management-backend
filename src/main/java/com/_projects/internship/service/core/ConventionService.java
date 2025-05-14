package com._projects.internship.service.core;

import com._projects.internship.dto.core.ConventionRequestDTO;
import com._projects.internship.dto.core.ConventionResponseDTO;


import java.util.List;

public interface ConventionService {

    ConventionRequestDTO createConvention(ConventionRequestDTO dto);

    ConventionRequestDTO updateConvention(ConventionRequestDTO dto);

    ConventionResponseDTO createFromApplication(Long applicationId);
    ConventionResponseDTO validateByTeacher(Long id);
    ConventionResponseDTO rejectByTeacher(Long id, String reason);
    ConventionResponseDTO approveByAdmin(Long id);
    ConventionResponseDTO rejectByAdmin(Long id, String reason);
    
    /**
     * Récupère une convention par son ID
     * @param id L'ID de la convention
     * @return La convention ou null si non trouvée
     */
    ConventionResponseDTO getConventionById(Long id);
    
    /**
     * Récupère toutes les conventions
     * @return La liste des conventions
     */
    List<ConventionResponseDTO> getAllConventions();
}
