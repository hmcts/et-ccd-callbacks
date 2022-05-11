CREATE OR REPLACE FUNCTION fn_EnglandWalesEthosMultipleCaseRefGen()
    RETURNS VARCHAR(200) AS
$$

-- =============================================
-- TEST :		SELECT fn_EnglandWalesEthosMultipleCaseRefGen();
--
-- Create date: 14-APR-2020
-- Description:	Function to generate Ethos case reference numbers for multiple cases for England/Wales
-- VERSION	:	14-MAR-2020		1.0  - Initial
--          :   28-OCT-2021     2.0  - CCD Consolidation
--          :   06-MAY-2022     3.0  - New method for calculating references
--                                     https://tools.hmcts.net/jira/browse/RET-822
-- =============================================

DECLARE
    currentCounter Integer;
BEGIN
    SELECT counter INTO currentCounter FROM multiple_reference_englandwales FOR UPDATE;

    currentCounter = currentCounter + 1;
    UPDATE multiple_reference_englandwales SET counter = currentCounter;

    RETURN currentCounter;
END;
$$ LANGUAGE plpgsql;
