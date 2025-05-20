package com._projects.internship.controller.core;

import com._projects.internship.model.core.Sector;
import com._projects.internship.service.core.SectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/${api.prefix}/sectors")
public class SectorController {
    private final SectorService sectorService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Sector> save(@RequestBody String name) throws Exception{
        return ResponseEntity.ok(sectorService.createSector(name));
    }
    @GetMapping
    public ResponseEntity<List<Sector>> getAll(){
        return ResponseEntity.ok(sectorService.getAllSectors());
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Sector> update(@PathVariable Long id,@RequestBody String name) throws Exception{
        return ResponseEntity.ok(sectorService.updateSector(id,name));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sector> get(@PathVariable Long id){
        return ResponseEntity.ok(sectorService.getSectorById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id){
        sectorService.deleteSector(id);
    }

}
