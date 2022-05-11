CREATE TYPE emp_status AS ENUM('SALARIED', 'FEE_PAID', 'UNKNOWN');

CREATE CAST (CHARACTER VARYING as emp_status) WITH INOUT AS IMPLICIT;

CREATE TABLE judge (
    id SERIAL PRIMARY KEY,
    tribunal_office VARCHAR(100),
    code VARCHAR(100),
    name VARCHAR(100),
    employment_status emp_status
);

CREATE TABLE venue (
    id SERIAL PRIMARY KEY,
    tribunal_office VARCHAR(100),
    code VARCHAR(100),
    name VARCHAR(100)
);

CREATE TABLE room (
    id SERIAL PRIMARY KEY,
    code VARCHAR(100),
    name VARCHAR(100),
    venue_code VARCHAR(100)
);

CREATE TABLE court_worker (
    id SERIAL PRIMARY KEY,
    tribunal_office VARCHAR(100),
    type VARCHAR(20),
    code VARCHAR(100),
    name VARCHAR(100)
);

CREATE TABLE file_location (
    id SERIAL PRIMARY KEY,
    tribunal_office VARCHAR(100),
    code VARCHAR(100),
    name VARCHAR(100)
);
