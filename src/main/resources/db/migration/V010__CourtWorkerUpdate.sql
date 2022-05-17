CREATE OR REPLACE FUNCTION fn_CourtWorkerUpdate(name varchar, id integer)
    returns void as

$$

begin
    update court_worker c
    set c.name = name
    where c.id = id;
end;

$$ language plpgsql