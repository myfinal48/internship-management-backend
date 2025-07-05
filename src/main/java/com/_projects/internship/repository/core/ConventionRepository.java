package com._projects.internship.repository.core;

import com._projects.internship.model.core.Convention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConventionRepository extends JpaRepository<Convention, Long> {

    List<Convention> findByInternshipOfferSectorId(Long sectorId);


    List<Convention> findByCompanyId(Long companyId);

    Convention findByApplicationId(Long applicationId);

}
