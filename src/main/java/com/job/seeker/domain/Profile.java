package com.job.seeker.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** The single user's identity, compensation details and resume file. */
@Entity
@Table(name = "profile")
public class Profile extends AuditedEntity {

    @Column(nullable = false, length = 200)
    private String fullName;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(length = 200)
    private String location;

    @Column(columnDefinition = "text")
    private String summary;

    @Column(length = 100)
    private String workAuth;

    @Column(precision = 6, scale = 2)
    private BigDecimal currentCtcLpa;

    @Column(precision = 6, scale = 2)
    private BigDecimal expectedCtcMinLpa;

    @Column(precision = 6, scale = 2)
    private BigDecimal expectedCtcMaxLpa;

    private Integer noticePeriodDays;

    @Column(nullable = false)
    private boolean willingToRelocate;

    @Column(length = 500)
    private String resumeFilePath;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("startDate DESC")
    private List<Experience> experiences = new ArrayList<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Skill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Education> educations = new ArrayList<>();

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getWorkAuth() {
        return workAuth;
    }

    public void setWorkAuth(String workAuth) {
        this.workAuth = workAuth;
    }

    public BigDecimal getCurrentCtcLpa() {
        return currentCtcLpa;
    }

    public void setCurrentCtcLpa(BigDecimal currentCtcLpa) {
        this.currentCtcLpa = currentCtcLpa;
    }

    public BigDecimal getExpectedCtcMinLpa() {
        return expectedCtcMinLpa;
    }

    public void setExpectedCtcMinLpa(BigDecimal expectedCtcMinLpa) {
        this.expectedCtcMinLpa = expectedCtcMinLpa;
    }

    public BigDecimal getExpectedCtcMaxLpa() {
        return expectedCtcMaxLpa;
    }

    public void setExpectedCtcMaxLpa(BigDecimal expectedCtcMaxLpa) {
        this.expectedCtcMaxLpa = expectedCtcMaxLpa;
    }

    public Integer getNoticePeriodDays() {
        return noticePeriodDays;
    }

    public void setNoticePeriodDays(Integer noticePeriodDays) {
        this.noticePeriodDays = noticePeriodDays;
    }

    public boolean isWillingToRelocate() {
        return willingToRelocate;
    }

    public void setWillingToRelocate(boolean willingToRelocate) {
        this.willingToRelocate = willingToRelocate;
    }

    public String getResumeFilePath() {
        return resumeFilePath;
    }

    public void setResumeFilePath(String resumeFilePath) {
        this.resumeFilePath = resumeFilePath;
    }

    public List<Experience> getExperiences() {
        return experiences;
    }

    public void setExperiences(List<Experience> experiences) {
        this.experiences = experiences;
    }

    public List<Skill> getSkills() {
        return skills;
    }

    public void setSkills(List<Skill> skills) {
        this.skills = skills;
    }

    public List<Education> getEducations() {
        return educations;
    }

    public void setEducations(List<Education> educations) {
        this.educations = educations;
    }
}
