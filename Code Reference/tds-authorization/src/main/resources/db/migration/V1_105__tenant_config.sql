INSERT
    INTO [tds].tenant_config ([tenant_id], [config_code], [config_value], [active])
	VALUES ((Select tenant_id from [tds].tenant where tenant_name = 'client1.dvtfo.onmicrosoft.com'),
	        'cosmos-config.transactions-keyspace-name', 'tds_payables', 1);
	        
INSERT
    INTO [tds].tenant_config ([tenant_id], [config_code], [config_value], [active])
	VALUES ((Select tenant_id from [tds].tenant where tenant_name = 'client1.dvtfo.onmicrosoft.com'),
	        'cosmos-config.masters-keyspace-name', 'tds_payables', 1);
	        
INSERT
    INTO [tds].tenant_config ([tenant_id], [config_code], [config_value], [active])
	VALUES ((Select tenant_id from [tds].tenant where tenant_name = 'client2.dvtfo.onmicrosoft.com'),
	        'cosmos-config.transactions-keyspace-name', 'tds_payables_dev_client1', 1);
	        
INSERT
    INTO [tds].tenant_config ([tenant_id], [config_code], [config_value], [active])
	VALUES ((Select tenant_id from [tds].tenant where tenant_name = 'client2.dvtfo.onmicrosoft.com'),
	        'cosmos-config.masters-keyspace-name', 'tds_payables_dev_client1', 1);