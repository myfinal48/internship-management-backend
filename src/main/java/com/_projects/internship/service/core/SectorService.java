package com._projects.internship.service.core;

import com._projects.internship.dto.core.CreateSectorRequestData;
import com._projects.internship.model.core.Sector;

import java.util.List;

public interface SectorService {
    Sector createSector(CreateSectorRequestData dto);

    Sector updateSector(Long id, CreateSectorRequestData dto);

    void deleteSector(Long id);

    List<Sector> getAllSectors();

    Sector getSectorById(Long id);
}
