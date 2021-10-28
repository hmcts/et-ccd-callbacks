/* CREATE TABLES */

  -- =============================================
-- Author:		Mohammed Hafejee
--
-- Create date: 14-APR-2020
-- Description:	Script to create base tables used by function fn_ethosMultipleCaseRefGen
-- VERSION	:	14-MAR-2020		1.0  - Initial
--          :   28-OCT-2021     2.0  - CCD Consolidation
-- =============================================

DROP TABLE IF EXISTS multiple_reference_england_wales;
CREATE TABLE multiple_reference_england_wales
(
    counter int

);
DELETE FROM multiple_reference_england_wales ; -- remove any existing values in case the script is ran more than once
INSERT INTO multiple_reference_england_wales VALUES (0);

DROP TABLE IF EXISTS multiple_reference_scotland;
CREATE TABLE multiple_reference_scotland
(
    counter int

);
DELETE FROM multiple_reference_scotland ; -- remove any existing values in case the script is ran more than once
INSERT INTO multiple_reference_scotland VALUES (0);
