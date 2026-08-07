ALTER TABLE notifications
  ADD COLUMN reference_type VARCHAR(20) NULL,
  ADD COLUMN reference_id BIGINT NULL;
