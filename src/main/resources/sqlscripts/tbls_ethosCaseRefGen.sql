/* CREATE TABLES */

-- =============================================
-- Author:		Mohammed Hafejee
--
-- Create date: 14-APR-2020
-- Description:	Script to create base tables used by function fn_ethosCaseRefGen
-- VERSION	:	14-MAR-2020		1.0  - Initial
--          :   28-OCT-2021     2.0  - CCD Consolidation
-- =============================================

DROP TABLE IF EXISTS single_reference_englandwales;
CREATE TABLE single_reference_englandwales
(
    counter int,
    cyear varchar(10)
);
DELETE FROM single_reference_englandwales; -- remove any existing values in case the script is ran more than once
INSERT INTO single_reference_englandwales VALUES (0,EXTRACT(YEAR FROM CURRENT_DATE));

DROP TABLE IF EXISTS single_reference_scotland;
CREATE TABLE single_reference_scotland
(
    counter int,
    cyear varchar(10)
);
DELETE FROM single_reference_scotland ; -- remove any existing values in case the script is ran more than once
INSERT INTO single_reference_scotland VALUES (0,EXTRACT(YEAR FROM CURRENT_DATE));
