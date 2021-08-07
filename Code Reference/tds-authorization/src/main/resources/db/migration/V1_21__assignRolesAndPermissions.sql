DELETE FROM [tds].permission WHERE permission_name IN ('CESS_TYPE_LIST','CESS_TYPE_UPDATE','CESS_TYPE_CREATE');
	GO	
	

Insert into [tds].permission (permission_name,active) 
		values ('IPFM_MASTER',1) , ('HAS_ACCESS',1),('CESS_TYPE_LIST',1),('CESS_TYPE_UPDATE',1),('CESS_TYPE_CREATE',1)
		
	GO	

Insert into tds.role (role_name,is_default,is_system,active) 
values ('Super Admin',1,1,1),
	   ('Senior Deductor',1,1,1)

	GO
		
INSERT INTO [tds].role_permission (role_id,permission_id) 

VALUES ((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'HAS_ACCESS')),
		
		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'MASTERS')),
		
		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'NOP_MASTER')),
		
		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'NOP_CREATE')),
		
		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'NOP_LIST')),
		
		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'NOP_UPDATE')),
		
		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'CESS_MASTER')),
		
		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'CESS_CREATE')),
		
		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'CESS_LIST')),
		
		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'CESS_UPDATE')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'CESS_TYPE_MASTER')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'CESS_TYPE_CREATE')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'CESS_TYPE_LIST')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'CESS_TYPE_UPDATE')),
		
		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'SURCHARGE_MASTER')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'SURCHARGE_CREATE')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'SURCHARGE_LIST')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'SURCHARGE_UPDATE')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TDS_RATE_MASTER')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TDS_RATE_CREATE')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TDS_RATE_LIST')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TDS_RATE_UPDATE')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'IPFM_MASTER')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'IPFM_CREATE')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'IPFM_LIST')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'IPFM_UPDATE')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'DEDUCTOR_MASTER')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'DEDUCTOR_CREATE')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'DEDUCTOR_LIST')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'DEDUCTOR_UPDATE')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'ARTICLE_MASTER')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'ARTICLE_CREATE')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'ARTICLE_LIST')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'ARTICLE_UPDATE'));
		
		