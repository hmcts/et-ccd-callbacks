CREATE TABLE IF NOT EXISTS public.ai_vetting_pending_result (
    case_reference BIGINT PRIMARY KEY
        REFERENCES ccd.case_data(reference) ON DELETE CASCADE,
    result_json TEXT NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_ai_vetting_pending_result_expires_at
    ON public.ai_vetting_pending_result (expires_at);
