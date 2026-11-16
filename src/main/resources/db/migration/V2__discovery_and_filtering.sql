-- Stage 2 (job discovery) and Stage 3 (cleaning and filtering) additions.

-- Discovery: remember how each watchlist company fared on its last check.
ALTER TABLE company_source
    ADD COLUMN last_checked_at      TIMESTAMPTZ,
    ADD COLUMN last_error           VARCHAR(500),
    ADD COLUMN consecutive_failures INTEGER NOT NULL DEFAULT 0;

-- Discovery: which watchlist row a job came from (so jobs that vanish from a board can be closed),
-- and Filtering: when the cleaning/filter pass last looked at the job.
ALTER TABLE job
    ADD COLUMN company_source_id BIGINT REFERENCES company_source (id) ON DELETE SET NULL,
    ADD COLUMN filtered_at       TIMESTAMPTZ;
CREATE INDEX idx_job_company_source ON job (company_source_id);

-- Filtering: why a job was skipped, for example 'LOCATION_NOT_INDIA: Remote - US'.
ALTER TABLE application
    ADD COLUMN filter_reason VARCHAR(300);

-- Filtering: extra hard-rule inputs on the user's preferences.
ALTER TABLE preference
    ADD COLUMN excluded_title_keywords TEXT[] NOT NULL DEFAULT '{}',
    ADD COLUMN deal_breaker_keywords   TEXT[] NOT NULL DEFAULT '{}';
