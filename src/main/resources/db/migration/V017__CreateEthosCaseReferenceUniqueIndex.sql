CREATE UNIQUE INDEX uidx_case_data_ethoscasereference
    ON public.case_data USING btree (
        case_type_id,
        btrim(upper((data #>> '{ethosCaseReference}'::text[])))
    );
