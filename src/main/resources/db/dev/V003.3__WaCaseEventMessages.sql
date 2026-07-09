DO $$ BEGIN
    CREATE TYPE message_state_enum AS ENUM ('NEW', 'READY', 'PROCESSED', 'UNPROCESSABLE');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
    CREATE CAST (character varying AS message_state_enum) WITH INOUT AS IMPLICIT;
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

CREATE TABLE IF NOT EXISTS public.wa_case_event_messages
(
    message_id         text               NOT NULL PRIMARY KEY,
    sequence           serial,
    case_id            text,
    event_timestamp    timestamp,
    from_dlq           boolean            NOT NULL DEFAULT false,
    state              message_state_enum NOT NULL,
    message_properties jsonb,
    message_content    text,
    received           timestamp          NOT NULL,
    delivery_count     integer            NOT NULL DEFAULT 1,
    hold_until         timestamp,
    retry_count        integer            NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_wa_case_event_messages_case_id_event_timestamp on public.wa_case_event_messages (case_id, event_timestamp);

CREATE INDEX if not exists idx_wa_case_event_messages_case_id_state_event_timestamp on public.wa_case_event_messages (case_id, state, event_timestamp);

CREATE INDEX if not exists idx_wa_case_event_messages_state_from_dlq_case_id_event_timestamp on public.wa_case_event_messages (state, from_dlq, case_id, event_timestamp);
