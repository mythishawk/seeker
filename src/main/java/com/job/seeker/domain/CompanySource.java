package com.job.seeker.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;

/** A company job board to watch (the watchlist). */
@Entity
@Table(name = "company_source")
public class CompanySource extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String company;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SourceType sourceType;

    @Column(nullable = false, length = 200)
    private String boardSlug;

    @Column(nullable = false)
    private boolean active = true;

    private Instant lastCheckedAt;

    @Column(length = 500)
    private String lastError;

    @Column(nullable = false)
    private int consecutiveFailures;

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public String getBoardSlug() {
        return boardSlug;
    }

    public void setBoardSlug(String boardSlug) {
        this.boardSlug = boardSlug;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getLastCheckedAt() {
        return lastCheckedAt;
    }

    public void setLastCheckedAt(Instant lastCheckedAt) {
        this.lastCheckedAt = lastCheckedAt;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public int getConsecutiveFailures() {
        return consecutiveFailures;
    }

    public void setConsecutiveFailures(int consecutiveFailures) {
        this.consecutiveFailures = consecutiveFailures;
    }
}
