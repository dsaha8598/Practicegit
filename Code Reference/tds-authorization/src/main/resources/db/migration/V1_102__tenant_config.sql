update tds.tenant_config set config_value='ci-dv-tfo-cosdb002.cassandra.cosmos.azure.com'
where config_code='cosmos-config.contact-points' 
and tenant_id=(select tenant_id from tds.tenant where tenant_name='tds.dvtfo.onmicrosoft.com');

update tds.tenant_config set config_value='ci-dv-tfo-cosdb002'
where config_code='cosmos-config.username' 
and tenant_id=(select tenant_id from tds.tenant where tenant_name='tds.dvtfo.onmicrosoft.com');

update tds.tenant_config set config_value='uTRfj1aGSf5D5FenkpPLpDLKEEqoI7o69GLqayU1VoKsLrTRyh0AMT20eBPrTbI13poCryPpYxNZ02Zn7TBBLA=='
where config_code='cosmos-config.password' 
and tenant_id=(select tenant_id from tds.tenant where tenant_name='tds.dvtfo.onmicrosoft.com');

update tds.tenant_config set config_value='10350'
where config_code='cosmos-config.port' 
and tenant_id=(select tenant_id from tds.tenant where tenant_name='tds.dvtfo.onmicrosoft.com');

update tds.tenant_config set config_value='true'
where config_code='cosmos-config.ssl' 
and tenant_id=(select tenant_id from tds.tenant where tenant_name='tds.dvtfo.onmicrosoft.com');

update tds.tenant_config set config_value='tds_payables_dev'
where config_code='cosmos-config.keyspace-name' 
and tenant_id=(select tenant_id from tds.tenant where tenant_name='tds.dvtfo.onmicrosoft.com');
