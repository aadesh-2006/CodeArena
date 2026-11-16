-- =============================================================================
-- Migration: V2__add_display_name_to_users.sql
-- Description: Add optional display_name to users table
-- =============================================================================

ALTER TABLE users ADD COLUMN IF NOT EXISTS display_name VARCHAR(100);
