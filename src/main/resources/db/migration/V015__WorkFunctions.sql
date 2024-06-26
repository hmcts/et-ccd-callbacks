-- Add Work
CREATE OR REPLACE FUNCTION add_work(p_case_id VARCHAR(255), p_json_data JSON) RETURNS VOID AS $$
BEGIN
    INSERT INTO work_queue(case_id, json_data, status, created_datetime)
    VALUES (p_case_id, p_json_data, 'PENDING', CURRENT_TIMESTAMP);
END;
$$ LANGUAGE plpgsql;

-- Pick Up Work
CREATE OR REPLACE FUNCTION pick_up_work(p_batch_size INT) RETURNS TABLE(id INT, case_id VARCHAR(255), json_data JSON) AS $$
BEGIN
    RETURN QUERY
    WITH cte AS (
        SELECT work_queue.id AS work_queue_id
        FROM work_queue
        WHERE status = 'PENDING'
        ORDER BY created_datetime
        LIMIT p_batch_size
        FOR UPDATE
    )
    UPDATE work_queue
    SET status = 'INPROGRESS', last_updated_datetime = CURRENT_TIMESTAMP
    WHERE work_queue.id IN (SELECT work_queue_id FROM cte)
    RETURNING work_queue.id, work_queue.case_id, work_queue.json_data;
END;
$$ LANGUAGE plpgsql;

-- Complete Work
CREATE OR REPLACE FUNCTION complete_work(p_ids INT[]) RETURNS VOID AS $$
BEGIN
    UPDATE work_queue
    SET status = 'COMPLETED', last_updated_datetime = CURRENT_TIMESTAMP
    WHERE id = ANY(p_ids);
END;
$$ LANGUAGE plpgsql;

-- Errored Work
CREATE OR REPLACE FUNCTION errored_work(p_ids INT[]) RETURNS VOID AS $$
BEGIN
    UPDATE work_queue
    SET status = 'ERROR', last_updated_datetime = CURRENT_TIMESTAMP
    WHERE id = ANY(p_ids);
END;
$$ LANGUAGE plpgsql;