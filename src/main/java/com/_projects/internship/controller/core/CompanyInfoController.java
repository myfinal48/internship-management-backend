package com._projects.internship.controller.core;

import com._projects.internship.model.core.CompanyInfo;
import com._projects.internship.service.core.CompanyInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/company-info")
@RequiredArgsConstructor
public class CompanyInfoController {
    private final CompanyInfoService companyInfoService;

    @PostMapping
    public ResponseEntity<CompanyInfo> create(@RequestBody CompanyInfo companyInfo) {
        return ResponseEntity.ok(companyInfoService.save(companyInfo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyInfo> getById(@PathVariable Long id) {
        return companyInfoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<CompanyInfo>> getAll() {
        return ResponseEntity.ok(companyInfoService.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyInfo> update(@PathVariable Long id, @RequestBody CompanyInfo companyInfo) {
        return companyInfoService.findById(id)
                .map(existing -> {
                    companyInfo.setId(id);
                    return ResponseEntity.ok(companyInfoService.save(companyInfo));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        companyInfoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
} 