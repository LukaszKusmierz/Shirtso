-- Drop the column if it exists from the failed migration
ALTER TABLE users DROP COLUMN IF EXISTS password_changed_at;

-- Add the column with NOT NULL and a default value in one step
ALTER TABLE users
    ADD COLUMN password_changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Update existing users to use their created_at timestamp instead of CURRENT_TIMESTAMP
UPDATE users
SET password_changed_at = created_at
WHERE created_at IS NOT NULL;
