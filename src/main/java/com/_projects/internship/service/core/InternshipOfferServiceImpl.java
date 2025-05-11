package com._projects.internship.service.core;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com._projects.internship.dto.core.CreateInternshipOfferRequestDTO;
import com._projects.internship.dto.core.UpdateInternshipOfferRequestDTO;
import com._projects.internship.exceptions.core.ResourceNotFoundException;
import com._projects.internship.mapper.core.InternshipOfferMapper;
import com._projects.internship.model.core.InternshipOffer;
import com._projects.internship.model.core.OfferStatus;
import com._projects.internship.model.core.Sector;
import com._projects.internship.model.security.Role;
import com._projects.internship.repository.core.InternshipOfferRepository;
import com._projects.internship.repository.security.UserRepository;
import com._projects.internship.specifications.core.InternshipOfferSpecifications;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InternshipOfferServiceImpl implements InternshipOfferService {
  private final InternshipOfferRepository internshipOfferRepository;
  private final UserRepository userRepository;

  @Override
  public InternshipOffer createInternshipOffer(CreateInternshipOfferRequestDTO dto) {
    InternshipOffer newOffer = InternshipOfferMapper.toEntity(dto);
    newOffer.setCompany(userRepository.findById(dto.getCompanyId()).orElseThrow());
    return internshipOfferRepository.save(newOffer);
  }

  @Override
  public InternshipOffer updateInternshipOffer(UpdateInternshipOfferRequestDTO dto) {
    InternshipOffer offerToUpdate = internshipOfferRepository.findById(dto.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Internship offer not found"));
    offerToUpdate.setTitle(dto.getTitle());
    offerToUpdate.setDescription(dto.getDescription());
    offerToUpdate.setSector(dto.getSector());
    offerToUpdate.setLocation(dto.getLocation());
    offerToUpdate.setSkills(dto.getSkills());
    offerToUpdate.setLength(dto.getLength());
    return internshipOfferRepository.save(offerToUpdate);
  }

  @Override
  public InternshipOffer inactivateInternshipOffer(Long internshipOfferId) {
    InternshipOffer offerToActivate = internshipOfferRepository.findById(internshipOfferId)
        .orElseThrow(() -> new ResourceNotFoundException("Offer not found"));
    offerToActivate.setStatus(OfferStatus.INACTIVE);
    return internshipOfferRepository.save(offerToActivate);
  }

  @Override
  public InternshipOffer activateInternshipOffer(Long internshipOfferId) {
    InternshipOffer offerToActivate = internshipOfferRepository.findById(internshipOfferId)
        .orElseThrow(() -> new ResourceNotFoundException("Offer not found"));
    offerToActivate.setStatus(OfferStatus.ACTIVE);
    return internshipOfferRepository.save(offerToActivate);
  }

  @Override
  public InternshipOffer completeInternshipOffer(Long internshipOfferId) {
    InternshipOffer offerToActivate = internshipOfferRepository.findById(internshipOfferId)
        .orElseThrow(() -> new ResourceNotFoundException("Offer not found"));
    offerToActivate.setStatus(OfferStatus.COMPLETED);
    return internshipOfferRepository.save(offerToActivate);
  }

  @Override
  public InternshipOffer getInternshipById(Long internshipOfferId) {
    InternshipOffer offer = internshipOfferRepository.findById(internshipOfferId)
        .orElseThrow(() -> new ResourceNotFoundException("Offer not found"));
    return offer;
  }

  @Override
  public List<InternshipOffer> filterInternshipOffers(Sector sector, String location, Integer length, OfferStatus status,
      Long companyId) {
    if (companyId != null && !userRepository.existsByIdAndRole(companyId, Role.COMPANY)) {
      throw new ResourceNotFoundException("Company not found");
    }

    Specification<InternshipOffer> spec = Specification
        .where(InternshipOfferSpecifications.withStatus(status))
        .and(InternshipOfferSpecifications.withSector(sector))
        .and(InternshipOfferSpecifications.withLocation(location))
        .and(InternshipOfferSpecifications.withLength(length))
        .and(InternshipOfferSpecifications.withCompany(companyId));

    return internshipOfferRepository.findAll(spec);
  }

}
