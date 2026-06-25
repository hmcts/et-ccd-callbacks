CREATE OR REPLACE FUNCTION fn_ScotlandEthosSubMultipleCaseRefGen(p_multref INT, p_numofcases INT)
    RETURNS VARCHAR(100) AS
$$

-- =============================================
-- TEST :		SELECT fn_ScotlandEthosSubMultipleCaseRefGen(243, 1);
--
-- Create date: 14-APR-2020
-- Description:	Function to generate Ethos case reference numbers for submultiple cases for Scotland
-- VERSION	:	14-APR-2020		1.0  - Initial
--        	:	29-APR-2020		1.1  - replaced RIGHT (CONCAT ( '00000', p_multref) ,5) with p_multref to prevent truncation
--          :   28-OCT-2021     2.0  - CCD Consolidation
--          :   06-MAY-2022     3.0  - New method for calculating references
--                                     https://tools.hmcts.net/jira/browse/RET-822
-- =============================================

DECLARE
    c_submultref    integer;
    c_submultrefstr varchar(200);
    c_multrefstr    varchar(20);
BEGIN
    SELECT submultref
    INTO c_submultref
    FROM submultiple_reference_scotland
    WHERE multref = p_multref FOR UPDATE;

    CASE
        WHEN c_submultref IS NULL THEN
            INSERT INTO submultiple_reference_scotland VALUES (p_multref, p_numofcases);

            c_multrefstr = p_multref;
            c_submultref = 1;
            c_submultrefstr = CONCAT(c_multrefstr, '/', c_submultref::text);
        ELSE
            UPDATE submultiple_reference_scotland
            SET submultref = c_submultref + p_numofcases
            WHERE multref = p_multref;

            c_submultref = c_submultref + 1;
            c_multrefstr = p_multref;
            c_submultrefstr = CONCAT(c_multrefstr, '/', c_submultref::text);
        END CASE;

    RETURN c_submultrefstr;
END;
$$ LANGUAGE plpgsql;
