ALTER TABLE users
    ADD COLUMN password_changed_at TIMESTAMP;

UPDATE users
SET password_changed_at = created_at
WHERE password_changed_at IS NULL;

ALTER TABLE users
    ALTER COLUMN password_changed_at SET NOT NULL;

COMMENT ON COLUMN users.password_changed_at IS 'Timestamp of the last password change, used to invalidate old JWT tokens';