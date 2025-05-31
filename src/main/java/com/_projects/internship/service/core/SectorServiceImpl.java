package com._projects.internship.service.core;

import com._projects.internship.dto.core.CreateSectorRequestData;
import com._projects.internship.exceptions.core.ResourceNotFoundException;
import com._projects.internship.model.core.Sector;
import com._projects.internship.repository.core.SectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SectorServiceImpl implements SectorService {
  private final SectorRepository sectorRepository;

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
