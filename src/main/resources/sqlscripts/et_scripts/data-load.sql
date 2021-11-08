------------------------------------------------------------
-- BRISTOL
------------------------------------------------------------
insert into judge (code, name, tribunal_office, employment_status)
values ('001', 'Judge Bristol 1', 'BRISTOL', 'SALARIED');

insert into judge (code, name, tribunal_office, employment_status)
values ('002', 'Judge Bristol 2', 'BRISTOL', 'SALARIED');

insert into judge (code, name, tribunal_office, employment_status)
values ('003', 'Judge Bristol 3', 'BRISTOL', 'FEE_PAID');

insert into venue (tribunal_office, code, name)
values ('BRISTOL', 'Barnstaple', 'Barnstaple');

insert into venue (tribunal_office, code, name)
values ('BRISTOL', 'Bath Law Courts', 'Bath Law Courts');

insert into venue (tribunal_office, code, name)
values ('BRISTOL', 'Bodmin', 'Bodmin');

insert into room (code, name, venue_code)
values ('* Not Allocated', '* Not Allocated', 'Barnstaple');

insert into room (code, name, venue_code)
values ('Bath Law Courts', 'Bath Law Courts', 'Bath Law Courts');

insert into room (code, name, venue_code)
values ('Bath Law Courts', 'Bath Law Courts', 'Bath Law Courts');

insert into room (code, name, venue_code)
values ('* Not Allocated', '* Not Allocated', 'Bodmin');

insert into room (code, name, venue_code)
values ('Bodmin 2', 'Bodmin 2', 'Bodmin');

insert into room (code, name, venue_code)
values ('Bodmin 3', 'Bodmin 3', 'Bodmin');

insert into room (code, name, venue_code)
values ('Bodmin NA', 'Bodmin NA', 'Bodmin');

insert into court_worker (tribunal_office, type, code, name)
values ('BRISTOL', 'CLERK', 'Mr BRISTOL Clerk', 'Mr BRISTOL Clerk');

insert into court_worker (tribunal_office, type, code, name)
values ('BRISTOL', 'CLERK', 'Mrs BRISTOL Clerk', 'Mrs BRISTOL Clerk');

insert into court_worker (tribunal_office, type, code, name)
values ('BRISTOL', 'CLERK', 'Miss BRISTOL Clerk', 'Miss BRISTOL Clerk');

insert into court_worker (tribunal_office, type, code, name)
values ('BRISTOL', 'EMPLOYEE_MEMBER', 'Mr BRISTOL EE', 'Mr BRISTOL EE');

insert into court_worker (tribunal_office, type, code, name)
values ('BRISTOL', 'EMPLOYEE_MEMBER', 'Mrs BRISTOL EE', 'Mrs BRISTOL EE');

insert into court_worker (tribunal_office, type, code, name)
values ('BRISTOL', 'EMPLOYEE_MEMBER', 'Miss BRISTOL EE', 'Miss BRISTOL EE');

insert into court_worker (tribunal_office, type, code, name)
values ('BRISTOL', 'EMPLOYER_MEMBER', 'Mr BRISTOL ER', 'Mr BRISTOL ER');

insert into court_worker (tribunal_office, type, code, name)
values ('BRISTOL', 'EMPLOYER_MEMBER', 'Mrs BRISTOL ER', 'Mrs BRISTOL ER');

insert into court_worker (tribunal_office, type, code, name)
values ('BRISTOL', 'EMPLOYER_MEMBER', 'Miss BRISTOL ER', 'Miss BRISTOL ER');

insert into file_location (tribunal_office, code, name)
values ('BRISTOL', 'Bristol Desk 1', 'Bristol Desk 1');

insert into file_location (tribunal_office, code, name)
values ('BRISTOL', 'Bristol Desk 2', 'Bristol Desk 2');

insert into file_location (tribunal_office, code, name)
values ('BRISTOL', 'Bristol Desk 3', 'Bristol Desk 3');

------------------------------------------------------------
-- LEEDS
------------------------------------------------------------
insert into judge (code, name, tribunal_office, employment_status)
values ('001', 'Judge Leeds 1', 'LEEDS', 'SALARIED');

insert into judge (code, name, tribunal_office, employment_status)
values ('002', 'Judge Leeds 2', 'LEEDS', 'SALARIED');

insert into judge (code, name, tribunal_office, employment_status)
values ('003', 'Judge Leeds 3', 'LEEDS', 'FEE_PAID');

insert into venue (tribunal_office, code, name)
values ('LEEDS', 'Hull', 'Hull');

insert into venue (tribunal_office, code, name)
values ('LEEDS', 'Hull Combined Court Centre', 'Hull Combined Court Centre');

insert into room (code, name, venue_code)
values ('* Not Allocated', '* Not Allocated', 'Hull');

insert into room (code, name, venue_code)
values ('1', '1', 'Hull');

insert into room (code, name, venue_code)
values ('2', '2', 'Hull Combined Court Centre');

insert into room (code, name, venue_code)
values ('* Not Allocated', '* Not Allocated', 'Hull Combined Court Centre');

insert into court_worker (tribunal_office, type, code, name)
values ('LEEDS', 'CLERK', 'Mr LEEDS Clerk', 'Mr LEEDS Clerk');

insert into court_worker (tribunal_office, type, code, name)
values ('LEEDS', 'CLERK', 'Mrs LEEDS Clerk', 'Mrs LEEDS Clerk');

insert into court_worker (tribunal_office, type, code, name)
values ('LEEDS', 'CLERK', 'Miss LEEDS Clerk', 'Miss LEEDS Clerk');

insert into court_worker (tribunal_office, type, code, name)
values ('LEEDS', 'EMPLOYEE_MEMBER', 'Mr LEEDS EE', 'Mr LEEDS EE');

insert into court_worker (tribunal_office, type, code, name)
values ('LEEDS', 'EMPLOYEE_MEMBER', 'Mrs LEEDS EE', 'Mrs LEEDS EE');

insert into court_worker (tribunal_office, type, code, name)
values ('LEEDS', 'EMPLOYEE_MEMBER', 'Miss LEEDS EE', 'Miss LEEDS EE');

insert into court_worker (tribunal_office, type, code, name)
values ('LEEDS', 'EMPLOYER_MEMBER', 'Mr LEEDS ER', 'Mr LEEDS ER');

insert into court_worker (tribunal_office, type, code, name)
values ('LEEDS', 'EMPLOYER_MEMBER', 'Mrs LEEDS ER', 'Mrs LEEDS ER');

insert into court_worker (tribunal_office, type, code, name)
values ('LEEDS', 'EMPLOYER_MEMBER', 'Miss LEEDS ER', 'Miss LEEDS ER');

insert into file_location (tribunal_office, code, name)
values ('LEEDS', 'Leeds Desk 1', 'Leeds Desk 1');

insert into file_location (tribunal_office, code, name)
values ('LEEDS', 'Leeds Desk 2', 'Leeds Desk 2');

insert into file_location (tribunal_office, code, name)
values ('LEEDS', 'Leeds Desk 3', 'Leeds Desk 3');

------------------------------------------------------------
-- GLASGOW
------------------------------------------------------------
insert into judge (code, name, tribunal_office, employment_status)
values ('001', 'Judge Glasgow 1', 'GLASGOW', 'SALARIED');

insert into judge (code, name, tribunal_office, employment_status)
values ('002', 'Judge Glasgow 2', 'GLASGOW', 'SALARIED');

insert into judge (code, name, tribunal_office, employment_status)
values ('003', 'Judge Glasgow 3', 'GLASGOW', 'FEE_PAID');

insert into court_worker (tribunal_office, type, code, name)
values ('GLASGOW', 'CLERK', 'Mr GLASGOW Clerk', 'Mr GLASGOW Clerk');

insert into court_worker (tribunal_office, type, code, name)
values ('GLASGOW', 'CLERK', 'Mrs GLASGOW Clerk', 'Mrs GLASGOW Clerk');

insert into court_worker (tribunal_office, type, code, name)
values ('GLASGOW', 'CLERK', 'Miss GLASGOW Clerk', 'Miss GLASGOW Clerk');

insert into court_worker (tribunal_office, type, code, name)
values ('GLASGOW', 'EMPLOYEE_MEMBER', 'Mr GLASGOW EE', 'Mr GLASGOW EE');

insert into court_worker (tribunal_office, type, code, name)
values ('GLASGOW', 'EMPLOYEE_MEMBER', 'Mrs GLASGOW EE', 'Mrs GLASGOW EE');

insert into court_worker (tribunal_office, type, code, name)
values ('GLASGOW', 'EMPLOYEE_MEMBER', 'Miss GLASGOW EE', 'Miss GLASGOW EE');

insert into court_worker (tribunal_office, type, code, name)
values ('GLASGOW', 'EMPLOYER_MEMBER', 'Mr GLASGOW ER', 'Mr GLASGOW ER');

insert into court_worker (tribunal_office, type, code, name)
values ('GLASGOW', 'EMPLOYER_MEMBER', 'Mrs GLASGOW ER', 'Mrs GLASGOW ER');

insert into court_worker (tribunal_office, type, code, name)
values ('GLASGOW', 'EMPLOYER_MEMBER', 'Miss GLASGOW ER', 'Miss GLASGOW ER');

insert into file_location (tribunal_office, code, name)
values ('GLASGOW', 'GLASGOW Desk 1', 'GLASGOW Desk 1');

insert into file_location (tribunal_office, code, name)
values ('GLASGOW', 'GLASGOW Desk 2', 'GLASGOW Desk 2');

insert into file_location (tribunal_office, code, name)
values ('GLASGOW', 'GLASGOW Desk 3', 'GLASGOW Desk 3');

insert into venue (tribunal_office, code, name)
values ('GLASGOW', 'Glasgow COET', 'Glasgow COET');

insert into room (code, name, venue_code)
values ('1', '1', 'Glasgow COET');

insert into room (code, name, venue_code)
values ('2', '2', 'Glasgow COET');

insert into venue (tribunal_office, code, name)
values ('GLASGOW', 'GTC', 'GTC');

insert into room (code, name, venue_code)
values ('201', '201', 'GTC');

insert into room (code, name, venue_code)
values ('202', '202', 'GTC');

------------------------------------------------------------
-- ABERDEEN
------------------------------------------------------------
insert into judge (code, name, tribunal_office, employment_status)
values ('001', 'Judge Aberdeen 1', 'ABERDEEN', 'SALARIED');

insert into judge (code, name, tribunal_office, employment_status)
values ('002', 'Judge Aberdeen 2', 'ABERDEEN', 'SALARIED');

insert into judge (code, name, tribunal_office, employment_status)
values ('003', 'Judge Aberdeen 3', 'ABERDEEN', 'FEE_PAID');

insert into court_worker (tribunal_office, type, code, name)
values ('ABERDEEN', 'CLERK', 'Mr ABERDEEN Clerk', 'Mr ABERDEEN Clerk');

insert into court_worker (tribunal_office, type, code, name)
values ('ABERDEEN', 'CLERK', 'Mrs ABERDEEN Clerk', 'Mrs ABERDEEN Clerk');

insert into court_worker (tribunal_office, type, code, name)
values ('ABERDEEN', 'CLERK', 'Miss ABERDEEN Clerk', 'Miss ABERDEEN Clerk');

insert into court_worker (tribunal_office, type, code, name)
values ('ABERDEEN', 'EMPLOYEE_MEMBER', 'Mr ABERDEEN EE', 'Mr ABERDEEN EE');

insert into court_worker (tribunal_office, type, code, name)
values ('ABERDEEN', 'EMPLOYEE_MEMBER', 'Mrs ABERDEEN EE', 'Mrs ABERDEEN EE');

insert into court_worker (tribunal_office, type, code, name)
values ('ABERDEEN', 'EMPLOYEE_MEMBER', 'Miss ABERDEEN EE', 'Miss ABERDEEN EE');

insert into court_worker (tribunal_office, type, code, name)
values ('ABERDEEN', 'EMPLOYER_MEMBER', 'Mr ABERDEEN ER', 'Mr ABERDEEN ER');

insert into court_worker (tribunal_office, type, code, name)
values ('ABERDEEN', 'EMPLOYER_MEMBER', 'Mrs ABERDEEN ER', 'Mrs ABERDEEN ER');

insert into court_worker (tribunal_office, type, code, name)
values ('ABERDEEN', 'EMPLOYER_MEMBER', 'Miss ABERDEEN ER', 'Miss ABERDEEN ER');

insert into file_location (tribunal_office, code, name)
values ('ABERDEEN', 'ABERDEEN Desk 1', 'ABERDEEN Desk 1');

insert into file_location (tribunal_office, code, name)
values ('ABERDEEN', 'ABERDEEN Desk 2', 'ABERDEEN Desk 2');

insert into file_location (tribunal_office, code, name)
values ('ABERDEEN', 'ABERDEEN Desk 3', 'ABERDEEN Desk 3');

insert into venue (tribunal_office, code, name)
values ('ABERDEEN', 'Aberdeen', 'Aberdeen');

insert into room (code, name, venue_code)
values ('1', '1', 'Aberdeen');

insert into room (code, name, venue_code)
values ('2', '2', 'Aberdeen');

insert into venue (tribunal_office, code, name)
values ('ABERDEEN', 'I J C', 'I J C');

insert into room (code, name, venue_code)
values ('1', '1', 'I J C');

------------------------------------------------------------
-- DUNDEE
------------------------------------------------------------
insert into judge (code, name, tribunal_office, employment_status)
values ('001', 'Judge DUNDEE 1', 'DUNDEE', 'SALARIED');

insert into judge (code, name, tribunal_office, employment_status)
values ('002', 'Judge DUNDEE 2', 'DUNDEE', 'SALARIED');

insert into judge (code, name, tribunal_office, employment_status)
values ('003', 'Judge DUNDEE 3', 'DUNDEE', 'FEE_PAID');

insert into court_worker (tribunal_office, type, code, name)
values ('DUNDEE', 'CLERK', 'Mr DUNDEE Clerk', 'Mr DUNDEE Clerk');

insert into court_worker (tribunal_office, type, code, name)
values ('DUNDEE', 'CLERK', 'Mrs DUNDEE Clerk', 'Mrs DUNDEE Clerk');

insert into court_worker (tribunal_office, type, code, name)
values ('DUNDEE', 'CLERK', 'Miss DUNDEE Clerk', 'Miss DUNDEE Clerk');

insert into court_worker (tribunal_office, type, code, name)
values ('DUNDEE', 'EMPLOYEE_MEMBER', 'Mr DUNDEE EE', 'Mr DUNDEE EE');

insert into court_worker (tribunal_office, type, code, name)
values ('DUNDEE', 'EMPLOYEE_MEMBER', 'Mrs DUNDEE EE', 'Mrs DUNDEE EE');

insert into court_worker (tribunal_office, type, code, name)
values ('DUNDEE', 'EMPLOYEE_MEMBER', 'Miss DUNDEE EE', 'Miss DUNDEE EE');

insert into court_worker (tribunal_office, type, code, name)
values ('DUNDEE', 'EMPLOYER_MEMBER', 'Mr DUNDEE ER', 'Mr DUNDEE ER');

insert into court_worker (tribunal_office, type, code, name)
values ('DUNDEE', 'EMPLOYER_MEMBER', 'Mrs DUNDEE ER', 'Mrs DUNDEE ER');

insert into court_worker (tribunal_office, type, code, name)
values ('DUNDEE', 'EMPLOYER_MEMBER', 'Miss DUNDEE ER', 'Miss DUNDEE ER');

insert into file_location (tribunal_office, code, name)
values ('DUNDEE', 'DUNDEE Desk 1', 'DUNDEE Desk 1');

insert into file_location (tribunal_office, code, name)
values ('DUNDEE', 'DUNDEE Desk 2', 'DUNDEE Desk 2');

insert into file_location (tribunal_office, code, name)
values ('DUNDEE', 'DUNDEE Desk 3', 'DUNDEE Desk 3');

insert into venue (tribunal_office, code, name)
values ('DUNDEE', 'Dundee', 'Dundee');

insert into room (code, name, venue_code)
values ('1', '1', 'DUNDEE');

insert into room (code, name, venue_code)
values ('2', '2', 'DUNDEE');

insert into room (code, name, venue_code)
values ('Judges Own Room', 'Judges Own Room', 'DUNDEE');

insert into venue (tribunal_office, code, name)
values ('DUNDEE', 'Dundee Tribunal', 'Tribunal');

insert into room (code, name, venue_code)
values ('1', '1', 'Dundee Tribunal');

insert into room (code, name, venue_code)
values ('2', '2', 'Dundee Tribunal');

------------------------------------------------------------
-- EDINBURGH
------------------------------------------------------------
insert into judge (code, name, tribunal_office, employment_status)
values ('001', 'Judge EDINBURGH 1', 'EDINBURGH', 'SALARIED');

insert into judge (code, name, tribunal_office, employment_status)
values ('002', 'Judge EDINBURGH 2', 'EDINBURGH', 'SALARIED');

insert into judge (code, name, tribunal_office, employment_status)
values ('003', 'Judge EDINBURGH 3', 'EDINBURGH', 'FEE_PAID');

insert into court_worker (tribunal_office, type, code, name)
values ('EDINBURGH', 'CLERK', 'Mr EDINBURGH Clerk', 'Mr EDINBURGH Clerk');

insert into court_worker (tribunal_office, type, code, name)
values ('EDINBURGH', 'CLERK', 'Mrs EDINBURGH Clerk', 'Mrs EDINBURGH Clerk');

insert into court_worker (tribunal_office, type, code, name)
values ('EDINBURGH', 'CLERK', 'Miss EDINBURGH Clerk', 'Miss EDINBURGH Clerk');

insert into court_worker (tribunal_office, type, code, name)
values ('EDINBURGH', 'EMPLOYEE_MEMBER', 'Mr EDINBURGH EE', 'Mr EDINBURGH EE');

insert into court_worker (tribunal_office, type, code, name)
values ('EDINBURGH', 'EMPLOYEE_MEMBER', 'Mrs EDINBURGH EE', 'Mrs EDINBURGH EE');

insert into court_worker (tribunal_office, type, code, name)
values ('EDINBURGH', 'EMPLOYEE_MEMBER', 'Miss EDINBURGH EE', 'Miss EDINBURGH EE');

insert into court_worker (tribunal_office, type, code, name)
values ('EDINBURGH', 'EMPLOYER_MEMBER', 'Mr EDINBURGH ER', 'Mr EDINBURGH ER');

insert into court_worker (tribunal_office, type, code, name)
values ('EDINBURGH', 'EMPLOYER_MEMBER', 'Mrs EDINBURGH ER', 'Mrs EDINBURGH ER');

insert into court_worker (tribunal_office, type, code, name)
values ('EDINBURGH', 'EMPLOYER_MEMBER', 'Miss EDINBURGH ER', 'Miss EDINBURGH ER');

insert into file_location (tribunal_office, code, name)
values ('EDINBURGH', 'EDINBURGH Desk 1', 'EDINBURGH Desk 1');

insert into file_location (tribunal_office, code, name)
values ('EDINBURGH', 'EDINBURGH Desk 2', 'EDINBURGH Desk 2');

insert into file_location (tribunal_office, code, name)
values ('EDINBURGH', 'EDINBURGH Desk 3', 'EDINBURGH Desk 3');

insert into venue (tribunal_office, code, name)
values ('EDINBURGH', 'Edinburgh', 'Edinburgh');

insert into room (code, name, venue_code)
values ('1', '1', 'Edinburgh');

insert into room (code, name, venue_code)
values ('2', '2', 'Edinburgh');

insert into room (code, name, venue_code)
values ('3', '3', 'Edinburgh');

insert into room (code, name, venue_code)
values ('4', '4', 'Edinburgh');

insert into room (code, name, venue_code)
values ('Judges Own Room', 'Judges Own Room', 'EDINBURGH');

insert into venue (tribunal_office, code, name)
values ('EDINBURGH', 'EDINBURGH Tribunal', 'Tribunal');

insert into room (code, name, venue_code)
values ('1', '1', 'EDINBURGH Tribunal');

insert into room (code, name, venue_code)
values ('2', '2', 'EDINBURGH Tribunal');