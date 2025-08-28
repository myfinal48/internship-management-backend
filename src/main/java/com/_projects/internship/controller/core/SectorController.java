package com._projects.internship.controller.core;

import com._projects.internship.dto.core.CreateSectorRequestData;
import com._projects.internship.model.core.Sector;
import com._projects.internship.service.core.SectorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/sectors")
@Tag(name = "sector-controller", description = "Sector management")
public class SectorController {
    private final SectorService sectorService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create a new sector",
            description = "Allows an administrator to create a new sector of activity. Required role: ADMIN"
    )
    @ApiResponse(responseCode = "200", description = "Sector created successfully")
    @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    public ResponseEntity<Sector> save(@RequestBody @Valid CreateSectorRequestData dto) throws Exception{
        return ResponseEntity.ok(sectorService.createSector(dto));
    }
    @GetMapping
    @Operation(
            summary = "Retrieve all sectors",
            description = "Allows retrieving the list of all sectors of activity. Accessible to everyone."
    )
    @ApiResponse(responseCode = "200", description = "List of sectors retrieved")
    public ResponseEntity<List<Sector>> getAll(){
        return ResponseEntity.ok(sectorService.getAllSectors());
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update a sector",
            description = "Allows an administrator to modify an existing sector of activity. Required role: ADMIN"
    )
    @ApiResponse(responseCode = "200", description = "Sector updated successfully")
    @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    @ApiResponse(responseCode = "404", description = "Sector not found")
    public ResponseEntity<Sector> update(@PathVariable Long id,@RequestBody @Valid CreateSectorRequestData dto) throws Exception{
        return ResponseEntity.ok(sectorService.updateSector(id,dto));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Retrieve sector by ID",
            description = "Allows retrieving details of a specific sector. Accessible to everyone."
    )
    @ApiResponse(responseCode = "200", description = "Sector found")
    @ApiResponse(responseCode = "404", description = "Sector not found")
    public ResponseEntity<Sector> get(@PathVariable Long id){
        return ResponseEntity.ok(sectorService.getSectorById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete a sector",
            description = "Allows an administrator to delete a sector of activity. Required role: ADMIN"
    )
    @ApiResponse(responseCode = "204", description = "Sector deleted successfully")
    @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    @ApiResponse(responseCode = "409", description = "Sector used by other entities")
    public void delete(@PathVariable Long id){
        sectorService.deleteSector(id);
    }

}
