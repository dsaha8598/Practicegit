INSERT into [tds].[user] (user_username,user_firstname,user_middlename,user_surname,user_email) 
values ('adminPan','adminPan','adminPan','Pan','user1@ey.dvtfo.onmicrosoft.com');
	GO

INSERT into tds.role (role_name,is_default,is_system,active) 
values ('Pan Validator',1,1,1)
	GO

	INSERT into tds.user_role (user_id , role_id) 
values ((Select user_id from [tds].[user] where user_username = 'adminPan'),
(select role_id from tds.role where role_name='Pan Validator'))

	GO

	INSERT into tds.role_permission (role_id,permission_id) 
values ((select role_id from tds.role where role_name = 'Pan Validator') ,
(select permission_id from tds.permission where permission_name = 'SuperAdminPermission'));

	GO

INSERT INTO [tds].role_permission (role_id,permission_id) 
	VALUES ((SELECT role_id FROM tds.role WHERE role_name= 'Pan Validator'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'VALIDATION_AO')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Pan Validator'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'VALIDATION_AO_REPORT_EXPORT')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Pan Validator'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'VALIDATION_AO_IMPORT')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Pan Validator'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'VALIDATION_AO_STATUS')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Pan Validator'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'VALIDATION_LDC')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Pan Validator'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'VALIDATION_LDC_REPORT_EXPORT')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Pan Validator'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'VALIDATION_LDC_IMPORT')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Pan Validator'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'VALIDATION_LDC_STATUS')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Pan Validator'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'VALIDATION')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Pan Validator'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'VALIDATION_PAN')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Pan Validator'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'VALIDATION_PAN_REPORT_EXPORT')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Pan Validator'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'VALIDATION_PAN_IMPORT')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Pan Validator'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'VALIDATION_PAN_STATUS'))



