-- ============================================================================
-- recommendations table
-- ============================================================================
CREATE TABLE recommendations (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,
    meal_type VARCHAR(20) NOT NULL,
    scheduled_time TIMESTAMP NOT NULL,
    menu_details VARCHAR(1000),
    push_message VARCHAR(500),
    is_sent BOOLEAN NOT NULL DEFAULT FALSE,
    sent_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- Indexes
CREATE INDEX idx_recommendations_member_id ON recommendations(member_id);
CREATE INDEX idx_recommendations_meal_type ON recommendations(meal_type);
CREATE INDEX idx_recommendations_scheduled_time ON recommendations(scheduled_time);
CREATE INDEX idx_recommendations_is_sent ON recommendations(is_sent);
CREATE INDEX idx_recommendations_member_scheduled ON recommendations(member_id, scheduled_time);
CREATE INDEX idx_recommendations_deleted_at ON recommendations(deleted_at);

-- Comments
COMMENT ON TABLE recommendations IS 'AI-powered meal recommendations';
COMMENT ON COLUMN recommendations.member_id IS 'FK to members table';
COMMENT ON COLUMN recommendations.meal_type IS 'BREAKFAST, LUNCH, DINNER, SNACK';
COMMENT ON COLUMN recommendations.scheduled_time IS 'When to send the notification';
COMMENT ON COLUMN recommendations.menu_details IS 'JSON format menu details';
COMMENT ON COLUMN recommendations.push_message IS 'FCM push notification message';
COMMENT ON COLUMN recommendations.is_sent IS 'Whether notification was sent';
COMMENT ON COLUMN recommendations.sent_at IS 'Actual notification send time';
