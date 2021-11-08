/* CREATE FUNCTION */

CREATE OR REPLACE FUNCTION fn_ethosSubMultipleCaseRefGen ( p_multref INT, p_numofcases INT, office varchar(200)) RETURNS VARCHAR(100) AS $$

-- =============================================
-- Author:		Mohammed Hafejee

-- TEST :		SELECT fn_ethosSubMultipleCaseRefGen(243, 1, 'Manchester');
--
-- Create date: 14-APR-2020
-- Description:	Function to generate Ethos case reference numbers for submultiple cases
-- VERSION	:	14-APR-2020		1.0  - Initial
--        	:	29-APR-2020		1.1  - replaced RIGHT (CONCAT ( '00000', p_multref) ,5) with p_multref to prevent truncation
--          :   28-OCT-2021     2.0  - CCD Consolidation
-- =============================================

  DECLARE c_submultref integer;
  DECLARE c_submultrefstr varchar(200);
  DECLARE c_multrefstr varchar(20);

BEGIN

CASE

WHEN office = 'EnglandWales' THEN
    SELECT submultref INTO c_submultref FROM submultiple_reference_englandwales WHERE multref  = p_multref FOR UPDATE;

    CASE WHEN c_submultref IS NULL THEN
        INSERT INTO submultiple_reference_englandwales VALUES  (p_multref, p_numofcases);

        c_multrefstr = p_multref;
        c_submultref = 1;
        c_submultrefstr = CONCAT(c_multrefstr,'/', c_submultref::text);
    ELSE
        UPDATE submultiple_reference_englandwales SET submultref = c_submultref + p_numofcases WHERE multref  = p_multref;

        c_submultref = c_submultref + 1;
        c_multrefstr = p_multref;
        c_submultrefstr = CONCAT(c_multrefstr,'/', c_submultref::text);
    END CASE;

    RETURN c_submultrefstr;

WHEN office = 'ET_Scotland' THEN
    SELECT submultref INTO c_submultref FROM submultiple_reference_scotland WHERE multref  = p_multref FOR UPDATE ;

    CASE WHEN c_submultref IS NULL THEN
        INSERT INTO submultiple_reference_scotland VALUES  (p_multref, p_numofcases);

        c_multrefstr = p_multref;
        c_submultref = 1;
        c_submultrefstr = CONCAT(c_multrefstr,'/', c_submultref::text);
    ELSE
        UPDATE submultiple_reference_scotland SET submultref  = c_submultref + p_numofcases WHERE multref  = p_multref;

        c_submultref = c_submultref + 1;
        c_multrefstr = p_multref;
        c_submultrefstr = CONCAT(c_multrefstr,'/', c_submultref::text);
    END CASE;

    RETURN c_submultrefstr;

END CASE;

END;
$$ LANGUAGE plpgsql;


