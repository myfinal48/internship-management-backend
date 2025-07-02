package com._projects.internship.service.core;

import com._projects.internship.model.core.CompanyInfo;
import com._projects.internship.repository.core.CompanyInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CompanyInfoServiceImpl implements CompanyInfoService {
    private final CompanyInfoRepository companyInfoRepository;

    @Override
    public CompanyInfo save(CompanyInfo companyInfo) {
        return companyInfoRepository.save(companyInfo);
    }

    @Override
    public Optional<CompanyInfo> findById(Long id) {
        return companyInfoRepository.findById(id);
    }

    @Override
    public List<CompanyInfo> findAll() {
        return companyInfoRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        companyInfoRepository.deleteById(id);
    }
} 