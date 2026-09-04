-- Local CFTLib seed data for ET case flags.
-- rd-commondata-api owns the flag catalogue; this file adds ET's BHA1 service mappings
-- plus the LOV rows needed by CUI RA's list-backed flags.

WITH seed(id, service_id, hearing_relevant, request_reason, flag_code, default_status, available_externally) AS (
    VALUES
        (900001, 'BHA1', true,  true,  'RA0010', 'Requested', true),
        (900002, 'BHA1', true,  false, 'RA0011', 'Requested', true),
        (900003, 'BHA1', true,  false, 'RA0012', 'Requested', true),
        (900004, 'BHA1', true,  true,  'RA0013', 'Requested', true),
        (900005, 'BHA1', false, false, 'RA0014', 'Requested', true),
        (900006, 'BHA1', false, false, 'RA0015', 'Requested', true),
        (900007, 'BHA1', false, false, 'RA0016', 'Requested', true),
        (900008, 'BHA1', false, false, 'RA0017', 'Requested', false),
        (900009, 'BHA1', false, false, 'RA0018', 'Requested', false),
        (900010, 'BHA1', true,  false, 'RA0019', 'Requested', true),
        (900011, 'BHA1', false, false, 'RA0020', 'Requested', true),
        (900012, 'BHA1', true,  true,  'RA0021', 'Requested', true),
        (900013, 'BHA1', false, false, 'RA0022', 'Requested', true),
        (900014, 'BHA1', false, false, 'RA0023', 'Requested', true),
        (900015, 'BHA1', true,  false, 'RA0024', 'Requested', false),
        (900016, 'BHA1', true,  false, 'RA0025', 'Requested', true),
        (900017, 'BHA1', true,  true,  'RA0026', 'Requested', true),
        (900018, 'BHA1', true,  true,  'RA0027', 'Requested', true),
        (900019, 'BHA1', true,  false, 'RA0028', 'Requested', true),
        (900020, 'BHA1', true,  true,  'RA0029', 'Requested', true),
        (900021, 'BHA1', true,  false, 'RA0030', 'Requested', true),
        (900022, 'BHA1', true,  false, 'RA0031', 'Requested', true),
        (900023, 'BHA1', false, false, 'RA0032', 'Requested', true),
        (900024, 'BHA1', true,  false, 'RA0033', 'Requested', true),
        (900025, 'BHA1', true,  false, 'RA0034', 'Requested', true),
        (900026, 'BHA1', true,  false, 'RA0035', 'Requested', true),
        (900027, 'BHA1', true,  false, 'RA0036', 'Requested', true),
        (900028, 'BHA1', false, false, 'RA0037', 'Requested', true),
        (900029, 'BHA1', true,  false, 'RA0038', 'Requested', true),
        (900030, 'BHA1', true,  false, 'RA0039', 'Requested', false),
        (900031, 'BHA1', false, false, 'RA0040', 'Requested', true),
        (900032, 'BHA1', true,  false, 'RA0041', 'Requested', false),
        (900033, 'BHA1', true,  false, 'RA0042', 'Requested', true),
        (900034, 'BHA1', true,  false, 'RA0043', 'Requested', true),
        (900035, 'BHA1', true,  false, 'RA0044', 'Requested', true),
        (900036, 'BHA1', true,  false, 'RA0045', 'Requested', true),
        (900037, 'BHA1', false, false, 'RA0046', 'Requested', true),
        (900038, 'BHA1', true,  false, 'RA0047', 'Requested', true),
        (900039, 'BHA1', true,  false, 'PF0015', 'Active',    false),
        (900101, 'BHA1', true,  false, 'CF0007', 'Active',    false),
        (900102, 'BHA1', false, false, 'CF0012', 'Active',    false),
        (900103, 'BHA1', true,  false, 'CF0014', 'Active',    false)
),
deleted AS (
    DELETE FROM flag_service existing
    USING seed
    WHERE existing.service_id = seed.service_id
      AND existing.flag_code = seed.flag_code
)
INSERT INTO flag_service (
    id,
    service_id,
    hearing_relevant,
    request_reason,
    flag_code,
    default_status,
    available_externally
)
SELECT
    seed.id,
    seed.service_id,
    seed.hearing_relevant,
    seed.request_reason,
    seed.flag_code,
    seed.default_status,
    seed.available_externally
FROM seed
INNER JOIN flag_details details ON details.flag_code = seed.flag_code
ON CONFLICT (id) DO UPDATE SET
    service_id = EXCLUDED.service_id,
    hearing_relevant = EXCLUDED.hearing_relevant,
    request_reason = EXCLUDED.request_reason,
    flag_code = EXCLUDED.flag_code,
    default_status = EXCLUDED.default_status,
    available_externally = EXCLUDED.available_externally;

DELETE FROM list_of_values
WHERE categorykey = 'SignLanguage'
  AND key IN (
    'americanSignLanguage',
    'britishSignLanguage',
    'handsOnSigning',
    'internationalSign',
    'lipspeaker',
    'makaton',
    'deafblindManualAlphabet',
    'notetaker',
    'deafRelay',
    'speechSupportedEnglish',
    'visualFrameSigning',
    'palantypist'
  );

DELETE FROM list_of_values
WHERE categorykey = 'InterpreterLanguage'
  AND key IN (
    'ara-ara',
    'ben-ben',
    'fre-fre',
    'pol-pol',
    'por-por',
    'spa-spa',
    'urd-urd',
    'wel-wel'
  );

INSERT INTO list_of_values (
    categorykey,
    serviceid,
    key,
    value_en,
    value_cy,
    hinttext_en,
    hinttext_cy,
    lov_order,
    parentcategory,
    parentkey,
    active
)
VALUES
    ('SignLanguage', null, 'americanSignLanguage', 'American Sign Language (ASL)', null, null, null, null, null, null, 'Y'),
    ('SignLanguage', null, 'britishSignLanguage', 'British Sign Language (BSL)', null, null, null, null, null, null, 'Y'),
    ('SignLanguage', null, 'handsOnSigning', 'Hands on signing', null, null, null, null, null, null, 'Y'),
    ('SignLanguage', null, 'internationalSign', 'International Sign (IS)', null, null, null, null, null, null, 'Y'),
    ('SignLanguage', null, 'lipspeaker', 'Lipspeaker', null, null, null, null, null, null, 'Y'),
    ('SignLanguage', null, 'makaton', 'Makaton', null, null, null, null, null, null, 'Y'),
    ('SignLanguage', null, 'deafblindManualAlphabet', 'Deafblind manual alphabet', null, null, null, null, null, null, 'Y'),
    ('SignLanguage', null, 'notetaker', 'Notetaker', null, null, null, null, null, null, 'Y'),
    ('SignLanguage', null, 'deafRelay', 'Deaf Relay', null, null, null, null, null, null, 'Y'),
    ('SignLanguage', null, 'speechSupportedEnglish', 'Speech Supported English (SSE)', null, null, null, null, null, null, 'Y'),
    ('SignLanguage', null, 'visualFrameSigning', 'Visual frame signing', null, null, null, null, null, null, 'Y'),
    ('SignLanguage', null, 'palantypist', 'Palantypist / Speech to text', null, null, null, null, null, null, 'Y'),
    ('InterpreterLanguage', null, 'ara-ara', 'Arabic', null, null, null, null, null, null, 'Y'),
    ('InterpreterLanguage', null, 'ben-ben', 'Bengali', null, null, null, null, null, null, 'Y'),
    ('InterpreterLanguage', null, 'fre-fre', 'French', null, null, null, null, null, null, 'Y'),
    ('InterpreterLanguage', null, 'pol-pol', 'Polish', null, null, null, null, null, null, 'Y'),
    ('InterpreterLanguage', null, 'por-por', 'Portuguese', null, null, null, null, null, null, 'Y'),
    ('InterpreterLanguage', null, 'spa-spa', 'Spanish', null, null, null, null, null, null, 'Y'),
    ('InterpreterLanguage', null, 'urd-urd', 'Urdu', null, null, null, null, null, null, 'Y'),
    ('InterpreterLanguage', null, 'wel-wel', 'Welsh', null, null, null, null, null, null, 'Y');
