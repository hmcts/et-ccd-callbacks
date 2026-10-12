CREATE TABLE public.notification_view (
    case_reference BIGINT NOT NULL
        REFERENCES ccd.case_data(reference) ON DELETE CASCADE,
    item_id TEXT NOT NULL,
    viewed_at TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'UTC'),
    PRIMARY KEY (case_reference, item_id)
);
