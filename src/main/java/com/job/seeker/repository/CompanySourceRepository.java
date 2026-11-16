package com.job.seeker.repository;

import com.job.seeker.domain.CompanySource;
import com.job.seeker.domain.SourceType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanySourceRepository extends JpaRepository<CompanySource, Long> {

    List<CompanySource> findByActiveTrue();

    boolean existsBySourceTypeAndBoardSlug(SourceType sourceType, String boardSlug);
}
