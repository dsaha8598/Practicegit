INSERT
    INTO [tds].tenant_config ([tenant_id], [config_code], [config_value], [is_active])
   VALUES ((Select tenant_id from [tds].tenant where tenant_name = 'default'),
			'cosmos-config.contact-points', 'ci-dv-tfo-cosdb002.cassandra.cosmos.azure.com', 1);
GO

INSERT
    INTO [tds].tenant_config ([tenant_id], [config_code], [config_value], [is_active])
	VALUES ((Select tenant_id from [tds].tenant where tenant_name = 'default'),
	        'cosmos-config.keyspace-name', 'tds_payables_dev', 1);
GO

INSERT
    INTO [tds].tenant_config ([tenant_id], [config_code], [config_value], [is_active])
	VALUES ((Select tenant_id from [tds].tenant where tenant_name = 'default'),
			'cosmos-config.schema-action', 'NONE', 1);
GO

INSERT
    INTO [tds].tenant_config ([tenant_id], [config_code], [config_value], [is_active])
	VALUES ((Select tenant_id from [tds].tenant where tenant_name = 'default'),
			'cosmos-config.ssl', 'true', 1);
GO

INSERT
    INTO [tds].tenant_config ([tenant_id], [config_code], [config_value], [is_active])
	VALUES ((Select tenant_id from [tds].tenant where tenant_name = 'default'),
			'cosmos-config.port', '10350', 1);
GO

INSERT
    INTO [tds].tenant_config ([tenant_id], [config_code], [config_value], [is_active])
    VALUES ((Select tenant_id from [tds].tenant where tenant_name = 'default'),
			'cosmos-config.username', 'ci-dv-tfo-cosdb002', 1);
GO

INSERT
    INTO [tds].tenant_config ([tenant_id], [config_code], [config_value], [is_active])
	VALUES ((Select tenant_id from [tds].tenant where tenant_name = 'default'),
			'cosmos-config.password', 'uTRfj1aGSf5D5FenkpPLpDLKEEqoI7o69GLqayU1VoKsLrTRyh0AMT20eBPrTbI13poCryPpYxNZ02Zn7TBBLA==', 1);
GO
