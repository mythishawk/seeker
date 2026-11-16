package com.job.seeker.repository;

import com.job.seeker.domain.CompanyBlocklist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyBlocklistRepository extends JpaRepository<CompanyBlocklist, Long> {

    boolean existsByCompanyNameIgnoreCase(String companyName);
}
