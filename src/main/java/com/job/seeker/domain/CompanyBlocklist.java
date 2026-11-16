package com.job.seeker.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** A company that must never be applied to. */
@Entity
@Table(name = "company_blocklist")
public class CompanyBlocklist extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String companyName;

    @Column(length = 500)
    private String reason;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
