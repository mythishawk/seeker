package com.job.seeker.repository;

import com.job.seeker.domain.AnswerBankEntry;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerBankRepository extends JpaRepository<AnswerBankEntry, Long> {

    List<AnswerBankEntry> findByProfileId(Long profileId);
}
