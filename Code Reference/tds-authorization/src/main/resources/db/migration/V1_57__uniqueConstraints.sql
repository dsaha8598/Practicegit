/*
ALTER TABLE tds.role ADD CONSTRAINT UQ_ROLE UNIQUE (role_name);
ALTER TABLE tds.permission ADD CONSTRAINT UQ_PERMISSION UNIQUE (permission_name);
ALTER TABLE tds.role_permission ADD CONSTRAINT UQ_ROLE_PERMISSION UNIQUE (role_id, permission_id);
ALTER TABLE tds.deductor_master ADD CONSTRAINT UQ_DEDUCTOR_CODE UNIQUE (code);
ALTER TABLE tds.tan ADD CONSTRAINT UQ_TAN UNIQUE (tan);
ALTER TABLE tds.deductor_tan_details ADD CONSTRAINT UQ_DEDUCTOR_TAN UNIQUE (deductor_id,tan_id);
ALTER TABLE tds.tenant ADD CONSTRAINT UQ_TENANT_NAME UNIQUE (tenant_name);
ALTER TABLE tds.[user] ADD CONSTRAINT UQ_USER_NAME UNIQUE (user_username);
ALTER TABLE tds.[user] ADD CONSTRAINT UQ_USER_EMAIL UNIQUE (user_email);
ALTER TABLE tds.user_tenant ADD CONSTRAINT UQ_TENANT_USER UNIQUE (tenant_id, user_id);
ALTER TABLE tds.user_organization ADD CONSTRAINT UQ_USER_ORGANIZATION UNIQUE (user_id,organization_id);
*/