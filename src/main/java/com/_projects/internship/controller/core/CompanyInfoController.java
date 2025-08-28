package com._projects.internship.controller.core;

import com._projects.internship.model.core.CompanyInfo;
import com._projects.internship.service.core.CompanyInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/company-info")
@RequiredArgsConstructor
@Tag(name = "company-info-controller", description = "Company information management")
public class CompanyInfoController {
    private final CompanyInfoService companyInfoService;

    @PostMapping
    @Operation(
            summary = "Create company information",
            description = "Allows creating new company information. Accessible to all authenticated users."
    )
    @ApiResponse(responseCode = "200", description = "Company information created")
    public ResponseEntity<CompanyInfo> create(@RequestBody CompanyInfo companyInfo) {
        return ResponseEntity.ok(companyInfoService.save(companyInfo));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Retrieve company information",
            description = "Allows retrieving company information by ID. Accessible to everyone."
    )
    @ApiResponse(responseCode = "200", description = "Company information found")
    @ApiResponse(responseCode = "404", description = "Company not found")
    public ResponseEntity<CompanyInfo> getById(@PathVariable Long id) {
        return companyInfoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(
            summary = "Retrieve all company information",
            description = "Allows retrieving the list of all company information. Accessible to everyone."
    )
    @ApiResponse(responseCode = "200", description = "List of company information retrieved")
    public ResponseEntity<List<CompanyInfo>> getAll() {
        return ResponseEntity.ok(companyInfoService.findAll());
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update company information",
            description = "Allows updating existing company information. Accessible to all authenticated users."
    )
    @ApiResponse(responseCode = "200", description = "Company information updated")
    @ApiResponse(responseCode = "404", description = "Company not found")
    public ResponseEntity<CompanyInfo> update(@PathVariable Long id, @RequestBody CompanyInfo companyInfo) {
        return companyInfoService.findById(id)
                .map(existing -> {
                    companyInfo.setId(id);
                    return ResponseEntity.ok(companyInfoService.save(companyInfo));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete company information",
            description = "Allows deleting company information. Accessible to all authenticated users."
    )
    @ApiResponse(responseCode = "204", description = "Company information deleted")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        companyInfoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
} 