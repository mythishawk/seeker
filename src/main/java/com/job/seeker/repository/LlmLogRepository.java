package com.job.seeker.repository;

import com.job.seeker.domain.LlmLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LlmLogRepository extends JpaRepository<LlmLog, Long> {

    List<LlmLog> findByApplicationId(Long applicationId);
}
