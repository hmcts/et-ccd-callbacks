DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'emp_status') THEN
        CREATE TYPE emp_status AS ENUM('SALARIED', 'FEE_PAID', 'UNKNOWN');
    END IF;
END$$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_cast WHERE castsource = 'character varying'::regtype AND casttarget = 'emp_status'::regtype) THEN
        CREATE CAST (CHARACTER VARYING as emp_status) WITH INOUT AS IMPLICIT;
    END IF;
END$$;

CREATE TABLE IF NOT EXISTS judge (
    id SERIAL PRIMARY KEY,
    tribunal_office VARCHAR(100),
    code VARCHAR(100),
    name VARCHAR(100),
    employment_status emp_status
);

CREATE TABLE IF NOT EXISTS venue (
    id SERIAL PRIMARY KEY,
    tribunal_office VARCHAR(100),
    code VARCHAR(100),
    name VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS room (
    id SERIAL PRIMARY KEY,
    code VARCHAR(100),
    name VARCHAR(100),
    venue_code VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS court_worker (
    id SERIAL PRIMARY KEY,
    tribunal_office VARCHAR(100),
    type VARCHAR(20),
    code VARCHAR(100),
    name VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS file_location (
    id SERIAL PRIMARY KEY,
    tribunal_office VARCHAR(100),
    code VARCHAR(100),
    name VARCHAR(100)
);
