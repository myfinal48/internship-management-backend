package com._projects.internship.service.core;

import com._projects.internship.dto.core.CreateSectorRequestData;
import com._projects.internship.exceptions.core.ResourceNotFoundException;
import com._projects.internship.exceptions.core.SectorInUseException;
import com._projects.internship.model.core.Sector;
import com._projects.internship.repository.core.InternshipOfferRepository;
import com._projects.internship.repository.core.SectorRepository;
import com._projects.internship.repository.security.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SectorServiceImpl implements SectorService {
  private final SectorRepository sectorRepository;
  private final InternshipOfferRepository internshipOfferRepository;
  private final UserRepository userRepository;

  @Override
  public Sector createSector(CreateSectorRequestData dto) {
    Sector sector = new Sector();
    sector.setName(dto.getName());
    return sectorRepository.save(sector);
  }

  @Override
  public Sector updateSector(Long id, CreateSectorRequestData dto) {
    Sector sector = sectorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Sector not found"));
    sector.setName(dto.getName());
    return sectorRepository.save(sector);
  }

  @Override
  public void deleteSector(Long id) {
    Sector sector = sectorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Sector not found"));
    
    if (internshipOfferRepository.existsBySector(sector) || userRepository.existsBySector(sector)) {
      throw new SectorInUseException("Cannot delete sector because it is being used by internship offers or users");
    }
    
    sectorRepository.delete(sector);
  }

  @Override
  public List<Sector> getAllSectors() {
    return sectorRepository.findAll();
  }

  @Override
  public Sector getSectorById(Long id) {
    return sectorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Sector not found"));
  }
}
