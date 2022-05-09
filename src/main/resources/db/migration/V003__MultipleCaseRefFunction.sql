CREATE OR REPLACE FUNCTION fn_ethosMultipleCaseRefGen(caseTypeId varchar(200))
    RETURNS VARCHAR(200) AS
$$

    -- =============================================
-- TEST :		SELECT fn_ethosMultipleCaseRefGen('ET_EnglandWales_Multiple');
--      :		SELECT fn_ethosMultipleCaseRefGen('ET_Scotland_Multiple');
--
-- Create date: 14-APR-2020
-- Description:	Function to generate Ethos case reference numbers for multiple cases
-- VERSION	:	14-MAR-2020		1.0  - Initial
--          :   28-OCT-2021     2.0  - CCD Consolidation
--          :   06-MAY-2022     3.0  - New method for calculating references
--                                     https://tools.hmcts.net/jira/browse/RET-822
-- =============================================

DECLARE
    currentCounter Integer;

BEGIN
    IF caseTypeId = 'ET_EnglandWales_Multiple' THEN
        SELECT counter INTO currentCounter FROM multiple_reference_englandwales FOR UPDATE;

        currentCounter = currentCounter + 1;
        UPDATE multiple_reference_englandwales SET counter = currentCounter;

    ELSIF caseTypeId = 'ET_Scotland_Multiple' THEN
        SELECT counter INTO currentCounter FROM multiple_reference_scotland FOR UPDATE;

        currentCounter = currentCounter + 1;
        UPDATE multiple_reference_scotland SET counter = currentCounter;
    ELSE
        RAISE EXCEPTION 'Unexpected caseTypeId %', caseTypeId;
    END IF;

    RETURN currentCounter;

END;
$$ LANGUAGE plpgsql;
