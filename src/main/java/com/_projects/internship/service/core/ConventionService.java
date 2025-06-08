package com._projects.internship.service.core;

import com._projects.internship.dto.core.ConventionRequestDTO;
import com._projects.internship.dto.core.ConventionResponseDTO;

import java.util.List;

public interface ConventionService {

    ConventionResponseDTO createFromApplication(Long applicationId);

    ConventionResponseDTO validateByTeacher(Long id);

    ConventionResponseDTO rejectByTeacher(Long id, String reason);

    ConventionResponseDTO approveByAdmin(Long id);

    ConventionResponseDTO rejectByAdmin(Long id, String reason);

    /**
     * @param id L'ID de la convention
     * @return La convention ou null si non trouvée
     */
    ConventionResponseDTO getConventionById(Long id);

    /**
     * Récupère toutes les conventions
     * @return La liste des conventions
     */
    List<ConventionResponseDTO> getAllConventions();

    /**
     * Met à jour le chemin du PDF signé d'une convention
     * @param id L'ID de la convention
     * @param signedPdfPath Le chemin du PDF signé
     * @return La convention mise à jour
     */
    ConventionResponseDTO updateSignedPdfPath(Long id, String signedPdfPath);

    /**
     * Permet à l'entreprise de mettre à jour une convention avant validation
     * @param dto Les données de mise à jour
     * @param companyId L'ID de l'entreprise qui fait la mise à jour
     * @return La convention mise à jour
     * @throws RuntimeException si la convention n'est pas modifiable ou si l'entreprise n'est pas autorisée
     */
    ConventionResponseDTO updateByCompany(ConventionRequestDTO dto, Long companyId);

    /**
     * Récupère toutes les conventions assignées à un enseignant spécifique
     * @param teacherId L'ID de l'enseignant
     * @return La liste des conventions assignées à cet enseignant
     */
    List<ConventionResponseDTO> getConventionsByTeacher(Long teacherId);

    /**
     * Récupère toutes les conventions d'une entreprise spécifique
     * @param companyId L'ID de l'entreprise
     * @return La liste des conventions de cette entreprise
     */
    List<ConventionResponseDTO> getConventionsByCompany(Long companyId);
}
