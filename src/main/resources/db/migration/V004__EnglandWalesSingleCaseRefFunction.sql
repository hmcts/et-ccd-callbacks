CREATE OR REPLACE FUNCTION fn_EnglandWalesEthosCaseRefGen(yr integer)
    RETURNS VARCHAR(10) AS
$$

-- =============================================
-- TEST :		SELECT fn_EnglandWalesEthosCaseRefGen (2022);
--
-- Create date: 14-APR-2020
-- Description:	Function to generate Ethos case reference numbers for single cases for England/Wales
-- VERSION	:	14-MAR-2020		1.0  - Initial
--          :   28-OCT-2021     2.0  - CCD Consolidation
--          :   06-MAY-2022     3.0  - New method for calculating references
--                                     https://tools.hmcts.net/jira/browse/RET-822
-- =============================================

DECLARE
    currentYear    smallint;
    currentCounter integer;
BEGIN
    SELECT cyear, counter
    INTO currentYear, currentCounter
    FROM single_reference_englandwales
    WHERE cyear = yr
        FOR UPDATE;

    IF currentYear IS NULL THEN
        currentYear = yr;
        currentCounter = 6000001;

        INSERT INTO single_reference_englandwales
            (CYEAR, COUNTER)
        VALUES (currentYear, currentCounter);
    ELSE
        currentCounter = currentCounter + 1;
        UPDATE single_reference_englandwales SET counter = currentCounter WHERE cyear = currentYear;
    END IF;

    RETURN currentCounter || '/' || currentYear;
END;
$$ LANGUAGE plpgsql;
