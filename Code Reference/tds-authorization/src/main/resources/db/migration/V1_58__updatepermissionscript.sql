

update tds.role set role_name='SUPER ADMIN' where role_name='Super Admin';


update tds.permission set permission_name='DEDUCTOR_MASTER' WHERE PERMISSION_NAME='DEDUCTOR-MASTER';

update tds.role_permission set permission_id =(select permission_id from tds.permission where permission_name='DEDUCTOR_MASTER')
WHERE PERMISSION_ID IS NULL;
