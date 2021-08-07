INSERT
    INTO [tds].tenant_config ([tenant_id], [config_code], [config_value], [active])
	VALUES ((Select tenant_id from [tds].tenant where tenant_name = 'default'),
	        'cosmos-config.transactions-keyspace-name', 'tds_payables', 1);
	        
INSERT
    INTO [tds].tenant_config ([tenant_id], [config_code], [config_value], [active])
	VALUES ((Select tenant_id from [tds].tenant where tenant_name = 'default'),
	        'cosmos-config.masters-keyspace-name', 'tds_payables', 1);
