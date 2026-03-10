ALTER TABLE bet_templates
    ALTER COLUMN over_under_threshold DROP NOT NULL,
    ALTER COLUMN over_under_threshold DROP DEFAULT;
