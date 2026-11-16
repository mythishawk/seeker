package com.job.seeker.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** What kind of jobs to look for. One row per profile. */
@Entity
@Table(name = "preference")
public class Preference extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false, unique = true)
    private Profile profile;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]", nullable = false)
    private List<String> targetTitles = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]", nullable = false)
    private List<String> locations = new ArrayList<>();

    @Column(nullable = false)
    private boolean remoteOk;

    private Integer minExperienceYears;

    private Integer maxExperienceYears;

    @Column(nullable = false)
    private int minScore = 70;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]", nullable = false)
    private List<String> excludedTitleKeywords = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]", nullable = false)
    private List<String> dealBreakerKeywords = new ArrayList<>();

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public List<String> getTargetTitles() {
        return targetTitles;
    }

    public void setTargetTitles(List<String> targetTitles) {
        this.targetTitles = targetTitles;
    }

    public List<String> getLocations() {
        return locations;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }

    public boolean isRemoteOk() {
        return remoteOk;
    }

    public void setRemoteOk(boolean remoteOk) {
        this.remoteOk = remoteOk;
    }

    public Integer getMinExperienceYears() {
        return minExperienceYears;
    }

    public void setMinExperienceYears(Integer minExperienceYears) {
        this.minExperienceYears = minExperienceYears;
    }

    public Integer getMaxExperienceYears() {
        return maxExperienceYears;
    }

    public void setMaxExperienceYears(Integer maxExperienceYears) {
        this.maxExperienceYears = maxExperienceYears;
    }

    public int getMinScore() {
        return minScore;
    }

    public void setMinScore(int minScore) {
        this.minScore = minScore;
    }

    public List<String> getExcludedTitleKeywords() {
        return excludedTitleKeywords;
    }

    public void setExcludedTitleKeywords(List<String> excludedTitleKeywords) {
        this.excludedTitleKeywords = excludedTitleKeywords;
    }

    public List<String> getDealBreakerKeywords() {
        return dealBreakerKeywords;
    }

    public void setDealBreakerKeywords(List<String> dealBreakerKeywords) {
        this.dealBreakerKeywords = dealBreakerKeywords;
    }
}
