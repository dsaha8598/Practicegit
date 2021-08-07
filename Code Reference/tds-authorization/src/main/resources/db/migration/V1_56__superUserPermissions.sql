INSERT INTO [tds].role_permission (role_id,permission_id) 
VALUES ((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'KEYWORD_MASTER')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'KEYWORD_MASTER_CREATE')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'KEYWORD_MASTER_UPDATE')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'KEYWORD_MASTER_VIEW')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'ONBOARDING_MASTER')),	
		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'ROLE_MASTER'));
        