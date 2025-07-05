package com._projects.internship.service.core;

import com._projects.internship.model.core.CompanyInfo;
import java.util.List;
import java.util.Optional;

public interface CompanyInfoService {
    CompanyInfo save(CompanyInfo companyInfo);
    Optional<CompanyInfo> findById(Long id);
    List<CompanyInfo> findAll();
    void deleteById(Long id);
} 