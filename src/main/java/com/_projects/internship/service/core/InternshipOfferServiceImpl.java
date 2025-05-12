package com._projects.internship.service.core;

import java.util.List;

import com._projects.internship.model.security.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com._projects.internship.dto.core.CreateInternshipOfferRequestDTO;
import com._projects.internship.exceptions.core.ResourceNotFoundException;
import com._projects.internship.mapper.core.InternshipOfferMapper;
import com._projects.internship.model.core.InternshipOffer;
import com._projects.internship.model.core.OfferStatus;
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
    User userToAdd = userRepository.findById(dto.getCompanyId()).orElseThrow();
    if(userToAdd.getRole().equals(Role.ADMIN)) {
      newOffer.setCompany(userToAdd);
      return internshipOfferRepository.save(newOffer);
    }else{
      throw new ResourceNotFoundException("Company does not exist");
    }
  }

  @Override
  public InternshipOffer updateInternshipOffer(Long id,CreateInternshipOfferRequestDTO dto) {
    InternshipOffer offerToUpdate = internshipOfferRepository.findById(id)
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
    InternshipOffer offerToInactivate = internshipOfferRepository.findById(internshipOfferId)
        .orElseThrow(() -> new ResourceNotFoundException("Offer not found"));
    if(offerToInactivate.getStatus().equals(OfferStatus.ACTIVE)) {
      offerToInactivate.setStatus(OfferStatus.INACTIVE);
      return internshipOfferRepository.save(offerToInactivate);
    }else {
      throw new ResourceNotFoundException("Offer is not active");
    }

  }

  @Override
  public InternshipOffer activateInternshipOffer(Long internshipOfferId) {
    InternshipOffer offerToActivate = internshipOfferRepository.findById(internshipOfferId)
        .orElseThrow(() -> new ResourceNotFoundException("Offer not found"));
    if(offerToActivate.getStatus().equals(OfferStatus.INACTIVE) || offerToActivate.getStatus().equals(OfferStatus.COMPLETED)) {
      offerToActivate.setStatus(OfferStatus.ACTIVE);
      return internshipOfferRepository.save(offerToActivate);
    }else {
      throw new ResourceNotFoundException("Offer is not active or completed");
    }
  }

  @Override
  public InternshipOffer completeInternshipOffer(Long internshipOfferId) {
    InternshipOffer offerToComplete = internshipOfferRepository.findById(internshipOfferId)
        .orElseThrow(() -> new ResourceNotFoundException("Offer not found"));
    if(offerToComplete.getStatus().equals(OfferStatus.ACTIVE)) {
      offerToComplete.setStatus(OfferStatus.COMPLETED);
      return internshipOfferRepository.save(offerToComplete);
    }else {
      throw new ResourceNotFoundException("Offer is not active or completed");
    }
  }

  @Override
  public InternshipOffer getInternshipById(Long internshipOfferId) {
    InternshipOffer offer = internshipOfferRepository.findById(internshipOfferId)
        .orElseThrow(() -> new ResourceNotFoundException("Offer not found"));
    return offer;
  }

  @Override
  public List<InternshipOffer> filterInternshipOffers(String sector, String location, Integer length, OfferStatus status,
      Long companyId) {
    if (companyId != null && !userRepository.existsByIdAndRole(companyId, Role.COMPANY)) {
      throw new ResourceNotFoundException("Company not found");
    }

    Specification<InternshipOffer> spec = Specification
        .where(InternshipOfferSpecifications.withStatus(status))
        .and(InternshipOfferSpecifications.withSectorName(sector))
        .and(InternshipOfferSpecifications.withLocation(location))
        .and(InternshipOfferSpecifications.withLength(length))
        .and(InternshipOfferSpecifications.withCompany(companyId));

    return internshipOfferRepository.findAll(spec);
  }

}
