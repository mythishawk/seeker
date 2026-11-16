-- Hibernate 7 maps a List<String> field to jsonb by default (the same way experience.bullets works),
-- so the four list columns on preference are stored as jsonb arrays like ["a", "b"] instead of text[].

ALTER TABLE preference ALTER COLUMN target_titles DROP DEFAULT;
ALTER TABLE preference ALTER COLUMN target_titles TYPE jsonb USING to_jsonb(target_titles);
ALTER TABLE preference ALTER COLUMN target_titles SET DEFAULT '[]'::jsonb;

ALTER TABLE preference ALTER COLUMN locations DROP DEFAULT;
ALTER TABLE preference ALTER COLUMN locations TYPE jsonb USING to_jsonb(locations);
ALTER TABLE preference ALTER COLUMN locations SET DEFAULT '[]'::jsonb;

ALTER TABLE preference ALTER COLUMN excluded_title_keywords DROP DEFAULT;
ALTER TABLE preference ALTER COLUMN excluded_title_keywords TYPE jsonb USING to_jsonb(excluded_title_keywords);
ALTER TABLE preference ALTER COLUMN excluded_title_keywords SET DEFAULT '[]'::jsonb;

ALTER TABLE preference ALTER COLUMN deal_breaker_keywords DROP DEFAULT;
ALTER TABLE preference ALTER COLUMN deal_breaker_keywords TYPE jsonb USING to_jsonb(deal_breaker_keywords);
ALTER TABLE preference ALTER COLUMN deal_breaker_keywords SET DEFAULT '[]'::jsonb;
