-- Stage 1: initial schema. JPA only reads and writes these tables; it never creates them.

-- ---------- Profile side ----------

CREATE TABLE profile (
    id                    BIGSERIAL PRIMARY KEY,
    full_name             VARCHAR(200)  NOT NULL,
    email                 VARCHAR(254)  NOT NULL,
    phone                 VARCHAR(30),
    location              VARCHAR(200),
    summary               TEXT,
    work_auth             VARCHAR(100),
    current_ctc_lpa       NUMERIC(6, 2),
    expected_ctc_min_lpa  NUMERIC(6, 2),
    expected_ctc_max_lpa  NUMERIC(6, 2),
    notice_period_days    INTEGER,
    willing_to_relocate   BOOLEAN       NOT NULL DEFAULT FALSE,
    resume_file_path      VARCHAR(500),
    created_at            TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TABLE experience (
    id          BIGSERIAL PRIMARY KEY,
    profile_id  BIGINT       NOT NULL REFERENCES profile (id) ON DELETE CASCADE,
    company     VARCHAR(200) NOT NULL,
    title       VARCHAR(200) NOT NULL,
    start_date  DATE         NOT NULL,
    end_date    DATE,
    bullets     JSONB        NOT NULL DEFAULT '[]'::jsonb
);
CREATE INDEX idx_experience_profile ON experience (profile_id);

CREATE TABLE skill (
    id          BIGSERIAL PRIMARY KEY,
    profile_id  BIGINT       NOT NULL REFERENCES profile (id) ON DELETE CASCADE,
    name        VARCHAR(100) NOT NULL,
    years       NUMERIC(4, 1)
);
CREATE UNIQUE INDEX uq_skill_profile_name ON skill (profile_id, lower(name));

CREATE TABLE education (
    id          BIGSERIAL PRIMARY KEY,
    profile_id  BIGINT       NOT NULL REFERENCES profile (id) ON DELETE CASCADE,
    school      VARCHAR(200) NOT NULL,
    degree      VARCHAR(200),
    year        INTEGER
);
CREATE INDEX idx_education_profile ON education (profile_id);

CREATE TABLE preference (
    id                    BIGSERIAL PRIMARY KEY,
    profile_id            BIGINT  NOT NULL UNIQUE REFERENCES profile (id) ON DELETE CASCADE,
    target_titles         TEXT[]  NOT NULL DEFAULT '{}',
    locations             TEXT[]  NOT NULL DEFAULT '{}',
    remote_ok             BOOLEAN NOT NULL DEFAULT FALSE,
    min_experience_years  INTEGER,
    max_experience_years  INTEGER,
    min_score             INTEGER NOT NULL DEFAULT 70
);

CREATE TABLE answer_bank (
    id           BIGSERIAL PRIMARY KEY,
    profile_id   BIGINT       NOT NULL REFERENCES profile (id) ON DELETE CASCADE,
    question_key VARCHAR(100) NOT NULL,
    answer       TEXT         NOT NULL,
    CONSTRAINT uq_answer_bank_profile_key UNIQUE (profile_id, question_key)
);

CREATE TABLE company_blocklist (
    id            BIGSERIAL PRIMARY KEY,
    company_name  VARCHAR(200) NOT NULL,
    reason        VARCHAR(500)
);
CREATE UNIQUE INDEX uq_company_blocklist_name ON company_blocklist (lower(company_name));

-- ---------- Job and application side ----------

CREATE TABLE company_source (
    id           BIGSERIAL PRIMARY KEY,
    company      VARCHAR(200) NOT NULL,
    source_type  VARCHAR(32)  NOT NULL,
    board_slug   VARCHAR(200) NOT NULL,
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT ck_company_source_type
        CHECK (source_type IN ('GREENHOUSE', 'LEVER', 'ASHBY', 'WORKABLE', 'SMARTRECRUITERS')),
    CONSTRAINT uq_company_source UNIQUE (source_type, board_slug)
);

CREATE TABLE job (
    id                  BIGSERIAL PRIMARY KEY,
    source              VARCHAR(50)   NOT NULL,
    external_id         VARCHAR(200)  NOT NULL,
    dedupe_hash         VARCHAR(64)   NOT NULL,
    company             VARCHAR(200)  NOT NULL,
    title               VARCHAR(300)  NOT NULL,
    location            VARCHAR(300),
    location_normalized VARCHAR(200),
    min_years           INTEGER,
    max_years           INTEGER,
    description         TEXT,
    description_hash    VARCHAR(64),
    url                 VARCHAR(1000),
    posted_at           TIMESTAMPTZ,
    first_seen_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    last_seen_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),
    closed_at           TIMESTAMPTZ,
    CONSTRAINT uq_job_source_external UNIQUE (source, external_id)
);
CREATE INDEX idx_job_dedupe_hash ON job (dedupe_hash);
CREATE INDEX idx_job_description_hash ON job (description_hash);
CREATE INDEX idx_job_last_seen ON job (last_seen_at);

CREATE TABLE application (
    id                    BIGSERIAL PRIMARY KEY,
    job_id                BIGINT       NOT NULL UNIQUE REFERENCES job (id),
    match_score           INTEGER,
    match_details         JSONB,
    status                VARCHAR(32)  NOT NULL DEFAULT 'DISCOVERED',
    tailored_resume_path  VARCHAR(500),
    cover_letter          TEXT,
    form_answers          JSONB,
    screenshot_path       VARCHAR(500),
    submitted_at          TIMESTAMPTZ,
    outcome_notes         TEXT,
    version               BIGINT       NOT NULL DEFAULT 0,  -- optimistic locking: stops a double approval from submitting twice
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT ck_application_status CHECK (status IN (
        'DISCOVERED', 'MATCHED', 'DRAFTED', 'AWAITING_APPROVAL', 'SUBMITTED', 'INTERVIEW', 'OFFER',
        'REJECTED_BY_COMPANY', 'DECLINED', 'SKIPPED', 'NEEDS_INPUT', 'MANUAL', 'FAILED', 'EXPIRED'))
);
CREATE INDEX idx_application_status ON application (status);

CREATE TABLE llm_log (
    id              BIGSERIAL PRIMARY KEY,
    application_id  BIGINT       REFERENCES application (id) ON DELETE SET NULL,
    step            VARCHAR(50)  NOT NULL,
    model           VARCHAR(100) NOT NULL,
    prompt_version  VARCHAR(50),
    prompt          TEXT,
    response        TEXT,
    input_tokens    INTEGER,
    output_tokens   INTEGER,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_llm_log_application ON llm_log (application_id);
