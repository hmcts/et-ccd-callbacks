CREATE TABLE public.hub_link_status (
    case_reference BIGINT PRIMARY KEY
        REFERENCES ccd.case_data(reference) ON DELETE CASCADE,
    data JSONB NOT NULL
        CHECK (jsonb_typeof(data) = 'object')
);
