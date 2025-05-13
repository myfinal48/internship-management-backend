package com._projects.internship.repository.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com._projects.internship.model.core.InternshipOffer;

public interface InternshipOfferRepository
    extends JpaRepository<InternshipOffer, Long>, JpaSpecificationExecutor<InternshipOffer> {

}
