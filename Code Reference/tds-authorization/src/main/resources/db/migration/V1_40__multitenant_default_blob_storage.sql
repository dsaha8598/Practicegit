
INSERT
    INTO [tds].tenant_config ([tenant_id], [config_code], [config_value], [is_active])
   VALUES ((Select tenant_id from [tds].tenant where tenant_name = 'default'),
			'blob-storage.protocol', 'https', 1);
GO

INSERT
    INTO [tds].tenant_config ([tenant_id], [config_code], [config_value], [is_active])
	VALUES ((Select tenant_id from [tds].tenant where tenant_name = 'default'),
	        'blob-storage.account-name', 'citdsdvblbstr01', 1);
GO

INSERT
    INTO [tds].tenant_config ([tenant_id], [config_code], [config_value], [is_active])
	VALUES ((Select tenant_id from [tds].tenant where tenant_name = 'default'),
			'blob-storage.account-key', 'vdk3d83UIQuvW3oqLcSHNsFNGP+a3o8gGsvykx/g9ebxxY+V1rhSYNZSV87Fa4p9Z6WxjpUPF4M/u49wBXa0+w==', 1);
GO

INSERT
    INTO [tds].tenant_config ([tenant_id], [config_code], [config_value], [is_active])
	VALUES ((Select tenant_id from [tds].tenant where tenant_name = 'default'),
			'blob-storage.container', 'tdspayables', 1);
GO
