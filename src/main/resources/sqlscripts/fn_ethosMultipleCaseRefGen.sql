/* CREATE FUNCTION */

CREATE OR REPLACE FUNCTION fn_ethosMultipleCaseRefGen (numofcases INT, office varchar(200)) RETURNS VARCHAR(200) AS $$

-- =============================================
-- Author:		Mohammed Hafejee

-- TEST :		SELECT fn_ethosMultipleCaseRefGen(2,'Manchester');
--
-- Create date: 14-APR-2020
-- Description:	Function to generate Ethos case reference numbers for multiple cases
-- VERSION	:	14-MAR-2020		1.0  - Initial
--          :   28-OCT-2021     2.0  - CCD Consolidation
-- =============================================

  DECLARE currentval Integer;
  DECLARE currentvalstr Varchar(200);

BEGIN

CASE
    WHEN office = 'EnglandWales' THEN
        SELECT counter INTO currentval FROM multiple_reference_englandwales FOR UPDATE;

        CASE
            WHEN currentval = 99999 OR (currentval + numofcases) > 99999 THEN
                currentval = NULL;
            ELSE
                UPDATE  multiple_reference_englandwales SET counter = counter + numofcases ;
        END CASE;

        IF currentval IS NOT NULL THEN
            currentval = currentval + 1 ;
            currentvalstr = RIGHT(CONCAT ('00000', currentval) ,5);
        ELSE
            currentvalstr = CONCAT ('Exception - Not enough multiple reference numbers available in index multipleReference', Office, ' to service requests');
        END IF;

        RETURN  currentvalstr;

    WHEN office = 'ET_Scotland' THEN
        SELECT counter INTO currentval FROM multiple_reference_scotland FOR UPDATE ;

        CASE
            WHEN currentval = 99999 OR (currentval + numofcases) > 99999 THEN
                currentval = NULL;
            ELSE
                UPDATE  multiple_reference_scotland SET counter = counter + numofcases ;
        END CASE;

        IF currentval IS NOT NULL THEN
            currentval = currentval + 1 ;
            currentvalstr = RIGHT(CONCAT ('00000', currentval) ,5);
        ELSE
            currentvalstr = CONCAT ('Exception - Not enough multiple reference numbers available in index multipleReference', Office, ' to service requests');
        END IF;

        RETURN  currentvalstr;
END CASE;
END;
$$ LANGUAGE plpgsql;


