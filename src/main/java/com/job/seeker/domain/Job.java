package com.job.seeker.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;

/** A job posting found on some source. */
@Entity
@Table(name = "job")
public class Job extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String source;

    @Column(nullable = false, length = 200)
    private String externalId;

    @Column(nullable = false, length = 64)
    private String dedupeHash;

    @Column(nullable = false, length = 200)
    private String company;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(length = 300)
    private String location;

    @Column(length = 200)
    private String locationNormalized;

    private Integer minYears;

    private Integer maxYears;

    @Column(columnDefinition = "text")
    private String description;

    @Column(length = 64)
    private String descriptionHash;

    @Column(length = 1000)
    private String url;

    private Instant postedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant firstSeenAt;

    @Column(nullable = false)
    private Instant lastSeenAt = Instant.now();

    private Instant closedAt;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getDedupeHash() {
        return dedupeHash;
    }

    public void setDedupeHash(String dedupeHash) {
        this.dedupeHash = dedupeHash;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocationNormalized() {
        return locationNormalized;
    }

    public void setLocationNormalized(String locationNormalized) {
        this.locationNormalized = locationNormalized;
    }

    public Integer getMinYears() {
        return minYears;
    }

    public void setMinYears(Integer minYears) {
        this.minYears = minYears;
    }

    public Integer getMaxYears() {
        return maxYears;
    }

    public void setMaxYears(Integer maxYears) {
        this.maxYears = maxYears;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionHash() {
        return descriptionHash;
    }

    public void setDescriptionHash(String descriptionHash) {
        this.descriptionHash = descriptionHash;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Instant getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(Instant postedAt) {
        this.postedAt = postedAt;
    }

    public Instant getFirstSeenAt() {
        return firstSeenAt;
    }

    public void setFirstSeenAt(Instant firstSeenAt) {
        this.firstSeenAt = firstSeenAt;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(Instant lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }
}
