insert into judge (code, name, tribunal_office, employment_status)
values ('001', 'Judge Yellow', 'BRISTOL', 'SALARIED');

insert into judge (code, name, tribunal_office, employment_status)
values ('002', 'Judge Brown', 'BRISTOL', 'SALARIED');

insert into judge (code, name, tribunal_office, employment_status)
values ('003', 'Judge Pink', 'BRISTOL', 'FEE_PAID');

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

insert into judge (code, name, tribunal_office, employment_status)
values ('001', 'Judge Cream', 'LEEDS', 'SALARIED');

insert into judge (code, name, tribunal_office, employment_status)
values ('002', 'Judge Blue', 'LEEDS', 'SALARIED');

insert into judge (code, name, tribunal_office, employment_status)
values ('003', 'Judge Red', 'LEEDS', 'FEE_PAID');

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