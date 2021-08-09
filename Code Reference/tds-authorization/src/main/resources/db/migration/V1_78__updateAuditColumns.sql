--tds.country;
update tds.country set created_date = CURRENT_TIMESTAMP where created_date is null;

update tds.country set modified_date = CURRENT_TIMESTAMP where modified_date is null;

update tds.country set created_by = 'eyAdmin' where created_by is null;

update tds.country set modified_by = 'eyAdmin' where modified_by is null;

update tds.country set active = 1 where active is null;

--tds.state;
update tds.state set created_date = CURRENT_TIMESTAMP where created_date is null;

update tds.state set modified_date = CURRENT_TIMESTAMP where modified_date is null;

update tds.state set created_by = 'eyAdmin' where created_by is null;

update tds.state set modified_by = 'eyAdmin' where modified_by is null;

update tds.state set active = 1 where active is null;

--tds.status;
update tds.status set created_date = CURRENT_TIMESTAMP where created_date is null;

update tds.status set modified_date = CURRENT_TIMESTAMP where modified_date is null;

update tds.status set created_by = 'eyAdmin' where created_by is null;

update tds.status set modified_by = 'eyAdmin' where modified_by is null;

update tds.status set active = 1 where active is null;

--tds.address;

update tds.address set created_date = CURRENT_TIMESTAMP where created_date is null;

update tds.address set modified_date = CURRENT_TIMESTAMP where modified_date is null;

update tds.address set created_by = 'eyAdmin' where created_by is null;

update tds.address set modified_by = 'eyAdmin' where modified_by is null;

update tds.address set active = 1 where active is null;

--tds.deductor_master;

update tds.deductor_master set created_date = CURRENT_TIMESTAMP where created_date is null;

update tds.deductor_master set modified_date = CURRENT_TIMESTAMP where modified_date is null;

update tds.deductor_master set created_by = 'eyAdmin' where created_by is null;

update tds.deductor_master set modified_by = 'eyAdmin' where modified_by is null;

update tds.deductor_master set active = 1 where active is null;


--tds.deductor_type;

update tds.deductor_type set created_date = CURRENT_TIMESTAMP where created_date is null;

update tds.deductor_type set modified_date = CURRENT_TIMESTAMP where modified_date is null;

update tds.deductor_type set created_by = 'eyAdmin' where created_by is null;

update tds.deductor_type set modified_by = 'eyAdmin' where modified_by is null;

update tds.deductor_type set active = 1 where active is null;

--tds.mode_of_payment;

update tds.mode_of_payment set created_date = CURRENT_TIMESTAMP where created_date is null;

update tds.mode_of_payment set modified_date = CURRENT_TIMESTAMP where modified_date is null;

update tds.mode_of_payment set created_by = 'eyAdmin' where created_by is null;



update tds.mode_of_payment set modified_by = 'eyAdmin' where modified_by is null;

update tds.mode_of_payment set active = 1 where active is null;
