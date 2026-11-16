package com.job.seeker.domain;



/**
 * Where an application is in its life. Stored as text in application.status.
 * The allowed moves between states are added in a later stage.
 */
public enum ApplicationStatus {
    DISCOVERED,
    MATCHED,
    DRAFTED,
    AWAITING_APPROVAL,
    SUBMITTED,
    INTERVIEW,
    OFFER,
    REJECTED_BY_COMPANY,
    DECLINED,
    SKIPPED,
    NEEDS_INPUT,
    MANUAL,
    FAILED,
    EXPIRED
}
