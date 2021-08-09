INSERT INTO [tds].role_permission (role_id,permission_id) 

VALUES ((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'NOP_MASTER')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'NOP_LIST')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'CESS_MASTER')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'CESS_LIST')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'CESS_TYPE_MASTER')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'CESS_TYPE_LIST')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'SURCHARGE_MASTER')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'SURCHARGE_LIST')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TDS_RATE_MASTER')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TDS_RATE_LIST')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'IPFM_MASTER')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'IPFM_LIST')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'DEDUCTOR_MASTER')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'DEDUCTOR_LIST')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'ARTICLE_MASTER')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'ARTICLE_LIST'));

