package com._projects.internship.repository.core;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com._projects.internship.model.core.Application;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByStudentId(Long studentId);
    List<Application> findByOfferCompanyId(Long companyId);
    List<Application> findByOfferId(Long offerId);
    boolean existsByStudentIdAndOfferId(Long studentId, Long offerId);
    
    // Vérifie si un étudiant a postulé à au moins une offre d'une entreprise
    boolean existsByStudentIdAndOfferCompanyId(Long studentId, Long companyId);
}
