CREATE UNIQUE INDEX uidx_case_data_ethoscasereference
    ON ccd.case_data USING btree (
        case_type_id,
        btrim(upper((data #>> '{ethosCaseReference}'::text[])))
    );
