-- Create table for update-case queue messages
CREATE TABLE update_case_queue (
    id BIGSERIAL PRIMARY KEY,
    message_id VARCHAR(255) UNIQUE NOT NULL,
    message_body TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    retry_count INTEGER NOT NULL DEFAULT 0,
    error_message TEXT,
    locked_until TIMESTAMP,
    locked_by VARCHAR(255)
);

-- Create indexes for performance
CREATE INDEX idx_update_case_queue_status ON update_case_queue(status);
CREATE INDEX idx_update_case_queue_created_at ON update_case_queue(created_at);
CREATE INDEX idx_update_case_queue_locked_until ON update_case_queue(locked_until);
CREATE INDEX idx_update_case_queue_status_locked ON update_case_queue(status, locked_until);

COMMENT ON TABLE update_case_queue IS 'Queue table for update case messages, replacing Azure Service Bus';
COMMENT ON COLUMN update_case_queue.status IS 'Message status: PENDING, PROCESSING, COMPLETED, FAILED';
COMMENT ON COLUMN update_case_queue.locked_until IS 'Timestamp until which the message is locked for processing';
COMMENT ON COLUMN update_case_queue.locked_by IS 'Identifier of the processor that locked this message';
