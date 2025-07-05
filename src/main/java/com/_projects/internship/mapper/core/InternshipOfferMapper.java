package com._projects.internship.mapper.core;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com._projects.internship.dto.core.CreateInternshipOfferRequestDTO;
import com._projects.internship.dto.core.GetInternshipOfferResponseDTO;
import com._projects.internship.model.core.InternshipOffer;

public class InternshipOfferMapper {
  public static InternshipOffer toEntity(CreateInternshipOfferRequestDTO dto) {
    InternshipOffer offer = new InternshipOffer();
    offer.setCreatedAt(LocalDateTime.now());
    offer.setDescription(dto.getDescription());
    offer.setLength(dto.getLength());
    offer.setTitle(dto.getTitle());
    offer.setUpdatedAt(LocalDateTime.now());
    offer.setLocation(dto.getLocation());
    offer.setSector(dto.getSector());
    offer.setSkills(dto.getSkills());
    return offer;
  }

  public static GetInternshipOfferResponseDTO toGetResponseDTO(InternshipOffer offer) {
    GetInternshipOfferResponseDTO dto = new GetInternshipOfferResponseDTO();
    dto.setId(offer.getId());
    dto.setTitle(offer.getTitle());
    dto.setDescription(offer.getDescription());
    dto.setSector(offer.getSector());
    dto.setLocation(offer.getLocation());
    dto.setSkills(offer.getSkills());
    dto.setLength(offer.getLength());
    dto.setStatus(offer.getStatus());
    dto.setCreatedAt(offer.getCreatedAt());
    dto.setUpdatedAt(offer.getUpdatedAt());
    dto.setCompanyName(offer.getCompany().getUsername());
    return dto;
  }

  public static List<GetInternshipOfferResponseDTO> toGetResponseDTOList(List<InternshipOffer> offers) {
    List<GetInternshipOfferResponseDTO> dtoList = new ArrayList<>();
    offers.forEach(offer -> {
      dtoList.add(toGetResponseDTO(offer));
    });
    return dtoList;
  }
}
