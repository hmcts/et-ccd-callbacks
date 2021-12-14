CREATE OR REPLACE FUNCTION fn_ethosCaseRefGen (numofcases INT, yr INT , office varchar(200))
RETURNS VARCHAR(10) AS $$

-- =============================================
-- Author:		Mohammed Hafejee

-- TEST :		SELECT fn_ethosCaseRefGen (2,2020,'Manchester');
--
-- Create date: 14-APR-2020
-- Description:	Function to generate Ethos case reference numbers for single cases
-- VERSION	:	14-MAR-2020		1.0  - Initial
--          :   28-OCT-2021     2.0  - CCD Consolidation
-- =============================================

DECLARE currentval integer;
DECLARE currentyr varchar(10);
DECLARE currentvalstr varchar(20);

BEGIN
    CASE
        WHEN office = 'ET_EnglandWales' THEN
            SELECT counter, cyear INTO currentval,currentyr FROM single_reference_englandwales FOR UPDATE ;

            CASE
                WHEN currentyr <> yr::text AND RIGHT(currentyr, 2) <> RIGHT(yr::text, 2) THEN
                    UPDATE  single_reference_englandwales SET counter = numofcases, cyear = yr ;
                    currentval := 0;
                    currentyr = yr;
                WHEN (currentval + numofcases) > 99999  THEN
                    UPDATE  single_reference_englandwales SET counter = (numofcases + currentval) - 99999,
                    cyear = RIGHT(currentyr, 2);
                    IF (currentval + 1)  > 99999 THEN
                        currentval := 0;
                        currentyr = CONCAT('00',RIGHT(currentyr, 2));
                    END IF;
                ELSE
                    UPDATE single_reference_englandwales SET counter = counter + numofcases;
            END CASE;

            currentval = currentval + 1 ;
            currentvalstr = RIGHT(CONCAT ('00000', currentval) ,5);
            currentyr =  RIGHT(CONCAT('00',currentyr),4);
            currentvalstr = CONCAT(currentvalstr,'/',currentyr);

            RETURN  currentvalstr;

        WHEN office = 'ET_Scotland' THEN
            SELECT counter, cyear INTO currentval,currentyr FROM single_reference_scotland FOR UPDATE;

            CASE
                WHEN currentyr <> yr::text AND RIGHT(currentyr, 2) <> RIGHT(yr::text, 2) THEN
                    UPDATE  single_reference_scotland SET counter = numofcases, cyear = yr;
                    currentval := 0;
                    currentyr = yr;
                WHEN (currentval + numofcases) > 99999  THEN
                    UPDATE  single_reference_scotland SET counter = (numofcases + currentval) - 99999,
                    cyear = RIGHT(currentyr, 2);
                    IF (currentval + 1)  > 99999 THEN
                        currentval := 0;
                        currentyr = CONCAT('00',RIGHT(currentyr, 2));
                    END IF;
                ELSE
                    UPDATE  single_reference_scotland SET counter = counter + numofcases;
            END CASE;

            currentval = currentval + 1;
            currentvalstr = RIGHT(CONCAT ('00000', currentval) ,5);
            currentyr = RIGHT(CONCAT('00',currentyr),4);
            currentvalstr = CONCAT(currentvalstr,'/',currentyr);

            RETURN  currentvalstr;
    END CASE;
END;
$$ LANGUAGE plpgsql;
