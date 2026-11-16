-- A "must look like my kind of role" word list, and a review status for jobs a person should check.

ALTER TABLE preference
    ADD COLUMN required_title_keywords jsonb NOT NULL DEFAULT '[]'::jsonb;

ALTER TABLE application DROP CONSTRAINT ck_application_status;
ALTER TABLE application ADD CONSTRAINT ck_application_status CHECK (status IN (
    'DISCOVERED', 'MATCHED', 'DRAFTED', 'AWAITING_APPROVAL', 'SUBMITTED', 'INTERVIEW', 'OFFER',
    'REJECTED_BY_COMPANY', 'DECLINED', 'SKIPPED', 'NEEDS_REVIEW', 'NEEDS_INPUT', 'MANUAL', 'FAILED', 'EXPIRED'));
