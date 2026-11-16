package com.job.seeker.repository;

import com.job.seeker.domain.Application;
import com.job.seeker.domain.ApplicationStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Optional<Application> findByJobId(Long jobId);

    List<Application> findByStatus(ApplicationStatus status);

    @Query("select a from Application a join fetch a.job j where j.companySource.id = :sourceId")
    List<Application> findByCompanySourceIdFetchJob(@Param("sourceId") Long sourceId);

    /** New jobs that have not been through the filters yet, after the given application id, in id order. */
    @Query("""
            select a from Application a join fetch a.job j
            where a.id > :afterId and a.status = :status and j.filteredAt is null
            order by a.id""")
    List<Application> findPendingFilter(@Param("afterId") long afterId,
                                        @Param("status") ApplicationStatus status,
                                        Pageable pageable);

    /** Every DISCOVERED job, plus jobs skipped by a filter (not because they closed), for a second look. */
    @Query("""
            select a from Application a join fetch a.job j
            where a.id > :afterId
              and (a.status = :discovered
                   or (a.status = :skipped and a.filterReason is not null and a.filterReason not like 'JOB_CLOSED%'))
            order by a.id""")
    List<Application> findRefilterCandidates(@Param("afterId") long afterId,
                                             @Param("discovered") ApplicationStatus discovered,
                                             @Param("skipped") ApplicationStatus skipped,
                                             Pageable pageable);

    /** Is there an earlier, still-open posting with the same fingerprint that has not been skipped? */
    @Query("""
            select count(a) > 0 from Application a
            where a.job.dedupeHash = :hash and a.job.id < :jobId
              and a.job.closedAt is null and a.status <> :skipped""")
    boolean existsLiveTwin(@Param("hash") String hash,
                           @Param("jobId") Long jobId,
                           @Param("skipped") ApplicationStatus skipped);

    @Query("select a from Application a join fetch a.job j where a.status = :status order by a.id desc")
    List<Application> findByStatusWithJob(@Param("status") ApplicationStatus status, Pageable pageable);

    @Query("select a from Application a join fetch a.job j order by a.id desc")
    List<Application> findAllWithJob(Pageable pageable);
}
