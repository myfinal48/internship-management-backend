package com._projects.internship.repository.core;

import com._projects.internship.model.core.CompanyInfo;
import com._projects.internship.model.security.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyInfoRepository extends JpaRepository<CompanyInfo, Long> {
    CompanyInfo findFirstByCompany(User company);
} 