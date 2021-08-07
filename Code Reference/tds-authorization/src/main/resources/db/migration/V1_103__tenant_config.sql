update tds.tenant_config set config_value='ci-dv-tfo-client1-cosdb036.cassandra.cosmos.azure.com'
where config_code='cosmos-config.contact-points' 
and tenant_id=(select tenant_id from tds.tenant where tenant_name='tds.dvtfo.onmicrosoft.com');

update tds.tenant_config set config_value='ci-dv-tfo-client1-cosdb036'
where config_code='cosmos-config.username' 
and tenant_id=(select tenant_id from tds.tenant where tenant_name='tds.dvtfo.onmicrosoft.com');

update tds.tenant_config set config_value='IVlIBl3YpPtB9eVwZwcm9TKFqeaEUJ6yzeUOpu7pyGs4YFKLsTeeMQjsCQIBC8hTop9PFYS0QhBgvUSlfrhWvQ=='
where config_code='cosmos-config.password' 
and tenant_id=(select tenant_id from tds.tenant where tenant_name='tds.dvtfo.onmicrosoft.com');

update tds.tenant_config set config_value='10350'
where config_code='cosmos-config.port' 
and tenant_id=(select tenant_id from tds.tenant where tenant_name='tds.dvtfo.onmicrosoft.com');

update tds.tenant_config set config_value='true'
where config_code='cosmos-config.ssl' 
and tenant_id=(select tenant_id from tds.tenant where tenant_name='tds.dvtfo.onmicrosoft.com');

INSERT
    INTO [tds].tenant_config ([tenant_id], [config_code], [config_value], [active])
	VALUES ((Select tenant_id from [tds].tenant where tenant_name = 'tds.dvtfo.onmicrosoft.com'),
	        'cosmos-config.transactions-keyspace-name', 'tds_payables_dev_transactions', 1);
	        
INSERT
    INTO [tds].tenant_config ([tenant_id], [config_code], [config_value], [active])
	VALUES ((Select tenant_id from [tds].tenant where tenant_name = 'tds.dvtfo.onmicrosoft.com'),
	        'cosmos-config.masters-keyspace-name', 'tds_payables_dev', 1);