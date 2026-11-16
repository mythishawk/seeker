package com.job.seeker.repository;

import com.job.seeker.domain.Preference;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreferenceRepository extends JpaRepository<Preference, Long> {

    Optional<Preference> findByProfileId(Long profileId);
}
