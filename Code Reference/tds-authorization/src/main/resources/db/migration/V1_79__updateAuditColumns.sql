
ALTER TABLE tds.deductor_onboarding_info DROP COLUMN IF EXISTS is_active;

ALTER TABLE tds.tenant DROP CONSTRAINT IF EXISTS DF__tenant__is_activ__55F4C372;

ALTER TABLE tds.tenant DROP CONSTRAINT IF EXISTS DF__tenant__is_activ__02C769E9;

ALTER TABLE tds.tenant DROP COLUMN IF EXISTS is_active; 

ALTER TABLE tds.tenant_config DROP COLUMN IF EXISTS is_active;


--tds.article_master;
update tds.article_master set created_date = CURRENT_TIMESTAMP where created_date is null;

update tds.article_master set modified_date = CURRENT_TIMESTAMP where modified_date is null;

update tds.article_master set created_by = 'eyAdmin' where created_by is null;

update tds.article_master set modified_by = 'eyAdmin' where modified_by is null;

update tds.article_master set active = 1 where active is null;

--tds.article_master_conditions;
update tds.article_master_conditions set created_date = CURRENT_TIMESTAMP where created_date is null;

update tds.article_master_conditions set modified_date = CURRENT_TIMESTAMP where modified_date is null;

update tds.article_master_conditions set created_by = 'eyAdmin' where created_by is null;

update tds.article_master_conditions set modified_by = 'eyAdmin' where modified_by is null;

update tds.article_master_conditions set active = 1 where active is null;

--tds.article_master_detailed_conditions;
update tds.article_master_detailed_conditions set created_date = CURRENT_TIMESTAMP where created_date is null;

update tds.article_master_detailed_conditions set modified_date = CURRENT_TIMESTAMP where modified_date is null;

update tds.article_master_detailed_conditions set created_by = 'eyAdmin' where created_by is null;

update tds.article_master_detailed_conditions set modified_by = 'eyAdmin' where modified_by is null;

update tds.article_master_detailed_conditions set active = 1 where active is null;

--tds.basis_of_cess_details;
update tds.basis_of_cess_details set created_date = CURRENT_TIMESTAMP where created_date is null;

update tds.basis_of_cess_details set modified_date = CURRENT_TIMESTAMP where modified_date is null;

update tds.basis_of_cess_details set created_by = 'eyAdmin' where created_by is null;

update tds.basis_of_cess_details set modified_by = 'eyAdmin' where modified_by is null;

update tds.basis_of_cess_details set active = 1 where active is null;

--tds.cess_master;
update tds.cess_master set created_date = CURRENT_TIMESTAMP where created_date is null;

update tds.cess_master set modified_date = CURRENT_TIMESTAMP where modified_date is null;

update tds.cess_master set created_by = 'eyAdmin' where created_by is null;

update tds.cess_master set modified_by = 'eyAdmin' where modified_by is null;

update tds.cess_master set active = 1 where active is null;


--tds.cess_type_master;
update tds.cess_type_master set created_date = CURRENT_TIMESTAMP where created_date is null;

update tds.cess_type_master set modified_date = CURRENT_TIMESTAMP where modified_date is null;

update tds.cess_type_master set created_by = 'eyAdmin' where created_by is null;

update tds.cess_type_master set modified_by = 'eyAdmin' where modified_by is null;

update tds.cess_type_master set active = 1 where active is null;

--tds.deductor_tan_details;
update tds.deductor_tan_details set created_date = CURRENT_TIMESTAMP where created_date is null;

update tds.deductor_tan_details set modified_date = CURRENT_TIMESTAMP where modified_date is null;

update tds.deductor_tan_details set created_by = 'eyAdmin' where created_by is null;

update tds.deductor_tan_details set modified_by = 'eyAdmin' where modified_by is null;

update tds.deductor_tan_details set active = 1 where active is null;

--tds.deductor_tenant;
update tds.deductor_tenant set created_date = CURRENT_TIMESTAMP where created_date is null;

update tds.deductor_tenant set modified_date = CURRENT_TIMESTAMP where modified_date is null;

update tds.deductor_tenant set created_by = 'eyAdmin' where created_by is null;

update tds.deductor_tenant set modified_by = 'eyAdmin' where modified_by is null;

update tds.deductor_tenant set active = 1 where active is null;


--tds.fine_rate_master;
update tds.fine_rate_master set created_date = CURRENT_TIMESTAMP where created_date is null;

update tds.fine_rate_master set modified_date = CURRENT_TIMESTAMP where modified_date is null;

update tds.fine_rate_master set created_by = 'eyAdmin' where created_by is null;

update tds.fine_rate_master set modified_by = 'eyAdmin' where modified_by is null;

update tds.fine_rate_master set active = 1 where active is null;

--[tds].[group];
update [tds].[group] set created_date = CURRENT_TIMESTAMP where created_date is null;

update [tds].[group] set modified_date = CURRENT_TIMESTAMP where modified_date is null;

update [tds].[group] set created_by = 'eyAdmin' where created_by is null;

update [tds].[group] set modified_by = 'eyAdmin' where modified_by is null;

update [tds].[group] set active = 1 where active is null;

--tds.nature_of_payment_master;
update tds.nature_of_payment_master set created_date = CURRENT_TIMESTAMP where created_date is null;

update tds.nature_of_payment_master set modified_date = CURRENT_TIMESTAMP where modified_date is null;

update tds.nature_of_payment_master set created_by = 'eyAdmin' where created_by is null;

update tds.nature_of_payment_master set modified_by = 'eyAdmin' where modified_by is null;

update tds.nature_of_payment_master set active = 1 where active is null;

--[tds].[role];
update [tds].[role] set created_date = CURRENT_TIMESTAMP where created_date is null;

update [tds].[role] set modified_date = CURRENT_TIMESTAMP where modified_date is null;

update [tds].[role] set created_by = 'eyAdmin' where created_by is null;

update [tds].[role] set modified_by = 'eyAdmin' where modified_by is null;

update [tds].[role] set active = 1 where active is null;

--tds.role_permission;
update tds.role_permission set created_date = CURRENT_TIMESTAMP where created_date is null;

update tds.role_permission set modified_date = CURRENT_TIMESTAMP where modified_date is null;

update tds.role_permission set created_by = 'eyAdmin' where created_by is null;

update tds.role_permission set modified_by = 'eyAdmin' where modified_by is null;

update tds.role_permission set active = 1 where active is null;

--tds.tan;
update tds.tan set created_date = CURRENT_TIMESTAMP where created_date is null;

update tds.tan set modified_date = CURRENT_TIMESTAMP where modified_date is null;

update tds.tan set created_by = 'eyAdmin' where created_by is null;

update tds.tan set modified_by = 'eyAdmin' where modified_by is null;

update tds.tan set active = 1 where active is null;

--tds.tds_master;
update tds.tds_master set created_date = CURRENT_TIMESTAMP where created_date is null;

update tds.tds_master set modified_date = CURRENT_TIMESTAMP where modified_date is null;

update tds.tds_master set created_by = 'eyAdmin' where created_by is null;

update tds.tds_master set modified_by = 'eyAdmin' where modified_by is null;

update tds.tds_master set active = 1 where active is null;

--tds.tenant;
update tds.tenant set created_date = CURRENT_TIMESTAMP where created_date is null;

update tds.tenant set modified_date = CURRENT_TIMESTAMP where modified_date is null;

update tds.tenant set created_by = 'eyAdmin' where created_by is null;

update tds.tenant set modified_by = 'eyAdmin' where modified_by is null;

update tds.tenant set active = 1 where active is null;

--tds.tenant_config
update tds.tenant_config set created_date = CURRENT_TIMESTAMP where created_date is null;

update tds.tenant_config set modified_date = CURRENT_TIMESTAMP where modified_date is null;

update tds.tenant_config set created_by = 'eyAdmin' where created_by is null;

update tds.tenant_config set modified_by = 'eyAdmin' where modified_by is null;

update tds.tenant_config set active = 1 where active is null;

--[tds].[user];
update [tds].[user] set created_date = CURRENT_TIMESTAMP where created_date is null;

update [tds].[user] set modified_date = CURRENT_TIMESTAMP where modified_date is null;

update [tds].[user] set created_by = 'eyAdmin' where created_by is null;

update [tds].[user] set modified_by = 'eyAdmin' where modified_by is null;

update [tds].[user] set active = 1 where active is null;

--tds.user_role;
update tds.user_role set created_date = CURRENT_TIMESTAMP where created_date is null;

update tds.user_role set modified_date = CURRENT_TIMESTAMP where modified_date is null;

update tds.user_role set created_by = 'eyAdmin' where created_by is null;

update tds.user_role set modified_by = 'eyAdmin' where modified_by is null;

update tds.user_role set active = 1 where active is null;


