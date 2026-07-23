CREATE TABLE single_reference_englandwales
(
    cyear smallint PRIMARY KEY,
    counter int
);

CREATE TABLE single_reference_scotland
(
    cyear smallint PRIMARY KEY,
    counter int
);

CREATE TABLE multiple_reference_englandwales
(
    counter int
);

CREATE TABLE multiple_reference_scotland
(
    counter int
);

CREATE TABLE submultiple_reference_englandwales
(
    multref int,
    submultref int
);
CREATE INDEX IX_subMultipleReferenceEnglandWales_multref ON submultiple_reference_englandwales(multref);

CREATE TABLE submultiple_reference_scotland
(
    multref int,
    submultref int
);
CREATE INDEX IX_subMultipleReferenceScotland_multref ON submultiple_reference_scotland(multref);
