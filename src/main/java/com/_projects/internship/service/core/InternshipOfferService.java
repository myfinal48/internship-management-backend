package com._projects.internship.service.core;

import java.util.List;

import com._projects.internship.dto.core.CreateInternshipOfferRequestDTO;
import com._projects.internship.dto.core.UpdateInternshipOfferRequestDTO;
import com._projects.internship.model.core.InternshipOffer;
import com._projects.internship.model.core.OfferStatus;
import com._projects.internship.model.core.Sector;

public interface InternshipOfferService {
  InternshipOffer createInternshipOffer(CreateInternshipOfferRequestDTO dto);

  InternshipOffer updateInternshipOffer(UpdateInternshipOfferRequestDTO dto);

  InternshipOffer inactivateInternshipOffer(Long internshipOfferId);

  InternshipOffer activateInternshipOffer(Long internshipOfferId);

  InternshipOffer completeInternshipOffer(Long internshipOfferId);

  InternshipOffer getInternshipById(Long internshipOfferId);

  List<InternshipOffer> filterInternshipOffers(Sector sector, String location, Integer length, OfferStatus status, Long companyId);
}
