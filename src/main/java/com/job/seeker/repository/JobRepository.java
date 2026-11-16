package com.job.seeker.repository;

import com.job.seeker.domain.Job;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, Long> {

    Optional<Job> findBySourceAndExternalId(String source, String externalId);

    List<Job> findByDedupeHash(String dedupeHash);

    List<Job> findByCompanySourceId(Long companySourceId);
}
