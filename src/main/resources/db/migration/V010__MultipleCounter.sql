CREATE TABLE IF NOT EXISTS multiple_counter
(
    multipleRef varchar(25),
    counter     integer DEFAULT 1
);

CREATE INDEX IF NOT EXISTS IX_multipleCounter_multipleRef ON multiple_counter(multipleRef);
