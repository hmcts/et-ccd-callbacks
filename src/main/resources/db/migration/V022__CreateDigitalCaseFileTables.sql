CREATE TABLE public.digital_case_file (
    case_reference BIGINT PRIMARY KEY
        REFERENCES ccd.case_data(reference) ON DELETE CASCADE,
    data JSONB
        CHECK (data IS NULL OR jsonb_typeof(data) = 'object'),
    pending_bundle_id UUID
);
