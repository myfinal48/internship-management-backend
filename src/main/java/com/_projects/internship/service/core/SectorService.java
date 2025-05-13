package com._projects.internship.service.core;

import com._projects.internship.model.core.Sector;

import java.util.List;

public interface SectorService {
    Sector createSector(String name);
    Sector updateSector(Long id,String name);
    void deleteSector(Long id);
    List<Sector> getAllSectors();
    Sector getSectorById(Long id);
}
