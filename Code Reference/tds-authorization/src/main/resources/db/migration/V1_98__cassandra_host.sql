update tds.tenant_config set config_value='ci-tfo-dv-cdb01, ci-tfo-dv-cdb02'
where config_code='cosmos-config.contact-points' 
and tenant_id=(select tenant_id from tds.tenant where tenant_name='tds.dvtfo.onmicrosoft.com');

update tds.tenant_config set config_value='9042'
where config_code='cosmos-config.port' 
and tenant_id=(select tenant_id from tds.tenant where tenant_name='tds.dvtfo.onmicrosoft.com');

update tds.tenant_config set config_value='cassandra'
where config_code='cosmos-config.username' 
and tenant_id=(select tenant_id from tds.tenant where tenant_name='tds.dvtfo.onmicrosoft.com');

update tds.tenant_config set config_value='cassandra'
where config_code='cosmos-config.password' 
and tenant_id=(select tenant_id from tds.tenant where tenant_name='tds.dvtfo.onmicrosoft.com');

update tds.tenant_config set config_value='cassandra'
where config_code='cosmos-config.password' 
and tenant_id=(select tenant_id from tds.tenant where tenant_name='tds.dvtfo.onmicrosoft.com');

update tds.tenant_config set config_value='false'
where config_code='cosmos-config.ssl' 
and tenant_id=(select tenant_id from tds.tenant where tenant_name='tds.dvtfo.onmicrosoft.com');

update tds.tenant_config set config_value='ZXlSRVBPUlRTQDM0NQ=='
where config_code='power-bi.password' 
and tenant_id=(select tenant_id from tds.tenant where tenant_name='tds.dvtfo.onmicrosoft.com');
