/* CREATE TABLES */

-- =============================================
-- Author:		Mohammed Hafejee
--
-- Create date: 14-APR-2020
-- Description:	Script to create base tables used by function fn_ethosSubMultipleCaseRefGen
-- VERSION	:	14-APR-2020		1.0  - Initial
--          :   28-OCT-2021     2.0  - CCD Consolidation
-- =============================================

DROP TABLE IF EXISTS submultiple_reference_englandwales;
CREATE TABLE submultiple_reference_englandwales
(
    multref int,
    submultref int
);
CREATE INDEX IX_subMultipleReferenceEnglandWales_multref ON submultiple_reference_englandwales(multref);

DROP TABLE IF EXISTS submultiple_reference_scotland;
CREATE TABLE submultiple_reference_scotland
(
    multref int,
    submultref int
);
CREATE INDEX IX_subMultipleReferenceScotland_multref ON submultiple_reference_scotland(multref);
