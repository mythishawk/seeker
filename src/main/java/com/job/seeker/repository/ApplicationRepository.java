package com.job.seeker.repository;

import com.job.seeker.domain.Application;
import com.job.seeker.domain.ApplicationStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Optional<Application> findByJobId(Long jobId);

    List<Application> findByStatus(ApplicationStatus status);
}
