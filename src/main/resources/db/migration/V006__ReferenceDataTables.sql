CREATE TABLE judge (
    id SERIAL PRIMARY KEY,
    tribunal_office VARCHAR(100),
    code VARCHAR(100),
    name VARCHAR(100),
    employment_status VARCHAR(100)
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
    name VARCHAR(100),
    venue_code VARCHAR(100)
);

CREATE TABLE file_location (
    id SERIAL PRIMARY KEY,
    tribunal_office VARCHAR(100),
    code VARCHAR(100),
    name VARCHAR(100)
);
