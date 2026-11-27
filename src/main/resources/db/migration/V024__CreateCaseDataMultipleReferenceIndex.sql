CREATE INDEX idx_case_data_multiplereference
    ON ccd.case_data USING btree (
        case_type_id,
        (data ->> 'multipleReference')
    );
