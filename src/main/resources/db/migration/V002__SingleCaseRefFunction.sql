CREATE OR REPLACE FUNCTION fn_ethosCaseRefGen(yr integer, caseTypeId varchar(200))
    RETURNS VARCHAR(10) AS
$$

    -- =============================================
-- TEST :		SELECT fn_ethosCaseRefGen (2022,'ET_EnglandWales');
--      		SELECT fn_ethosCaseRefGen (2022,'ET_Scotland');
--
-- Create date: 14-APR-2020
-- Description:	Function to generate Ethos case reference numbers for single cases
-- VERSION	:	14-MAR-2020		1.0  - Initial
--          :   28-OCT-2021     2.0  - CCD Consolidation
--          :   06-MAY-2022     3.0  - New method for calculating references
--                                     https://tools.hmcts.net/jira/browse/RET-822
-- =============================================

DECLARE
    currentYear    smallint;
    currentCounter integer;
BEGIN
    IF caseTypeId = 'ET_EnglandWales' THEN
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
    ELSIF caseTypeId = 'ET_Scotland' THEN
        SELECT cyear, counter
        INTO currentYear, currentCounter
        FROM single_reference_scotland
        WHERE cyear = yr
            FOR UPDATE;

        IF currentYear IS NULL THEN
            currentYear = yr;
            currentCounter = 8000001;

            INSERT INTO single_reference_scotland
                (CYEAR, COUNTER)
            VALUES (currentYear, currentCounter);
        ELSE
            currentCounter = currentCounter + 1;
            UPDATE single_reference_scotland SET counter = currentCounter WHERE cyear = currentYear;
        END IF;
    ELSE
        RAISE EXCEPTION 'Unexpected caseTypeId %', caseTypeId;
    END IF;

    RETURN currentCounter || '/' || currentYear;
END;
$$ LANGUAGE plpgsql;
