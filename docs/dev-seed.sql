-- Example starter data so stages 2 and 3 can be tried end to end.
-- Run it once against the app's database (for example with psql or your IDE's database tool),
-- after the app has started once so Flyway has created the tables.
-- Replace every value below with your own. Profile loading through the API comes in a later stage.

-- 1. You, plus the preferences the hard filters use.
WITH new_profile AS (
    INSERT INTO profile (full_name, email, phone, location, work_auth,
                         current_ctc_lpa, expected_ctc_min_lpa, expected_ctc_max_lpa,
                         notice_period_days, willing_to_relocate)
    VALUES ('Your Name', 'you@example.com', '0000000000', 'Pune, Maharashtra', 'Indian citizen',
            10.0, 14.0, 18.0, 60, TRUE)
    RETURNING id
)
INSERT INTO preference (profile_id, target_titles, locations, remote_ok,
                        min_experience_years, max_experience_years, min_score,
                        excluded_title_keywords, deal_breaker_keywords, required_title_keywords)
SELECT id,
       to_jsonb(ARRAY['Java Backend Developer', 'Software Engineer']),   -- used for scoring later, not for hard filtering
       to_jsonb(ARRAY['Pune']),                                           -- preferred cities; ignored for jobs when willing_to_relocate is TRUE
       TRUE,                                                    -- remote jobs welcome
       1, 5,                                                    -- apply only to jobs asking for 1 to 5 years of experience
       70,
       to_jsonb(ARRAY['intern', 'internship', 'principal', 'staff', 'director', 'head of', 'sales', 'recruiter']),
       to_jsonb(ARRAY['security clearance', 'must relocate abroad']),
       -- a job title must contain at least one of these words, or the job is skipped as TITLE_NOT_RELEVANT
       to_jsonb(ARRAY['backend', 'back end', 'software engineer', 'software developer', 'software development', 'sde', 'java'])
FROM new_profile;

-- 2. Companies you never want to apply to (your current employer, for example).
INSERT INTO company_blocklist (company_name, reason) VALUES
    ('Example Current Employer', 'current employer');

-- 3. The watchlist. These four are public boards used only as examples; replace them with companies you care
--    about. The slug is the last part of the board URL: boards.greenhouse.io/<slug> or jobs.lever.co/<slug>.
INSERT INTO company_source (company, source_type, board_slug) VALUES
    ('Stripe',    'GREENHOUSE', 'stripe'),
    ('Airbnb',    'GREENHOUSE', 'airbnb'),
    ('Anthropic', 'GREENHOUSE', 'anthropic'),
    ('Spotify',   'LEVER',      'spotify');
