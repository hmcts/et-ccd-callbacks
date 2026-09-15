SET LOCAL statement_timeout = '10min';

INSERT INTO public.hub_link_status (case_reference, data)
SELECT reference, data -> 'hubLinksStatuses'
FROM ccd.case_data
WHERE jsonb_typeof(data -> 'hubLinksStatuses') = 'object'
ON CONFLICT (case_reference) DO NOTHING;
