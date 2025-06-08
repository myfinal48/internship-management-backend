package com._projects.internship.repository.core;

import com._projects.internship.model.core.Convention;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConventionRepository extends JpaRepository<Convention, Long> {
    
    /**
     * Récupère toutes les conventions assignées à un enseignant spécifique
     * @param teacherId L'ID de l'enseignant
     * @return La liste des conventions assignées à cet enseignant
     */
    List<Convention> findByTeacherId(Long teacherId);

    /**
     * Récupère toutes les conventions d'une entreprise spécifique
     * @param companyId L'ID de l'entreprise
     * @return La liste des conventions de cette entreprise
     */
    List<Convention> findByCompanyId(Long companyId);



}
