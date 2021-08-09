
insert into tds.error_code(code,description) 
select 'ERR705','Document type should not be empty' 
where not exists (select 1 from tds.error_code where code= 'ERR705');


