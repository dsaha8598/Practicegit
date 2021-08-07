--Insert Permission

insert into tds.role_permission 
(role_id,permission_id,active,created_by,modified_by,created_date,modified_date) 
select (select role_id from tds.role where role_name ='SUPER ADMIN' and active = 1),
(select permission_id from tds.permission where permission_name = 'DEDUCTEE_LIST' and active =1),
1,'EY Admin','EY Admin',current_timestamp,current_timestamp
where not exists (select 1 from tds.role_permission where 
role_id= (select role_id from tds.role where role_name ='SUPER ADMIN' and active = 1) and 
permission_id=(select permission_id from tds.permission where permission_name = 'DEDUCTEE_LIST' and active =1));

insert into tds.role_permission 
(role_id,permission_id,active,created_by,modified_by,created_date,modified_date) 
select (select role_id from tds.role where role_name ='CFO' and active = 1),
(select permission_id from tds.permission where permission_name = 'DEDUCTEE_LIST' and active =1),
1,'EY Admin','EY Admin',current_timestamp,current_timestamp
where not exists (select 1 from tds.role_permission where 
role_id= (select role_id from tds.role where role_name ='CFO' and active = 1) and 
permission_id=(select permission_id from tds.permission where permission_name = 'DEDUCTEE_LIST' and active =1));

insert into tds.role_permission 
(role_id,permission_id,active,created_by,modified_by,created_date,modified_date) 
select (select role_id from tds.role where role_name ='Tax Head' and active = 1),
(select permission_id from tds.permission where permission_name = 'DEDUCTEE_LIST' and active =1),
1,'EY Admin','EY Admin',current_timestamp,current_timestamp
where not exists (select 1 from tds.role_permission where 
role_id= (select role_id from tds.role where role_name ='Tax Head' and active = 1) and 
permission_id=(select permission_id from tds.permission where permission_name = 'DEDUCTEE_LIST' and active =1));

insert into tds.role_permission 
(role_id,permission_id,active,created_by,modified_by,created_date,modified_date) 
select (select role_id from tds.role where role_name ='Partner' and active = 1),
(select permission_id from tds.permission where permission_name = 'DEDUCTEE_LIST' and active =1),
1,'EY Admin','EY Admin',current_timestamp,current_timestamp
where not exists (select 1 from tds.role_permission where 
role_id= (select role_id from tds.role where role_name ='Partner' and active = 1) and 
permission_id=(select permission_id from tds.permission where permission_name = 'DEDUCTEE_LIST' and active =1));

insert into tds.role_permission 
(role_id,permission_id,active,created_by,modified_by,created_date,modified_date) 
select (select role_id from tds.role where role_name ='Processor' and active = 1),
(select permission_id from tds.permission where permission_name = 'DEDUCTEE_LIST' and active =1),
1,'EY Admin','EY Admin',current_timestamp,current_timestamp
where not exists (select 1 from tds.role_permission where 
role_id= (select role_id from tds.role where role_name ='Processor' and active = 1) and 
permission_id=(select permission_id from tds.permission where permission_name = 'DEDUCTEE_LIST' and active =1));

insert into tds.role_permission 
(role_id,permission_id,active,created_by,modified_by,created_date,modified_date) 
select (select role_id from tds.role where role_name ='Engagement Team' and active = 1),
(select permission_id from tds.permission where permission_name = 'DEDUCTEE_LIST' and active =1),
1,'EY Admin','EY Admin',current_timestamp,current_timestamp
where not exists (select 1 from tds.role_permission where 
role_id= (select role_id from tds.role where role_name ='Engagement Team' and active = 1) and 
permission_id=(select permission_id from tds.permission where permission_name = 'DEDUCTEE_LIST' and active =1));

insert into tds.role_permission 
(role_id,permission_id,active,created_by,modified_by,created_date,modified_date) 
select (select role_id from tds.role where role_name ='Manager' and active = 1),
(select permission_id from tds.permission where permission_name = 'DEDUCTEE_LIST' and active =1),
1,'EY Admin','EY Admin',current_timestamp,current_timestamp
where not exists (select 1 from tds.role_permission where 
role_id= (select role_id from tds.role where role_name ='Manager' and active = 1) and 
permission_id=(select permission_id from tds.permission where permission_name = 'DEDUCTEE_LIST' and active =1));

insert into tds.role_permission 
(role_id,permission_id,active,created_by,modified_by,created_date,modified_date) 
select (select role_id from tds.role where role_name ='Tax Head' and active = 1),
(select permission_id from tds.permission where permission_name = 'MASTERS' and active =1),
1,'EY Admin','EY Admin',current_timestamp,current_timestamp
where not exists (select 1 from tds.role_permission where 
role_id= (select role_id from tds.role where role_name ='Tax Head' and active = 1) 
and permission_id=(select permission_id from tds.permission where permission_name = 'MASTERS' and active =1));

