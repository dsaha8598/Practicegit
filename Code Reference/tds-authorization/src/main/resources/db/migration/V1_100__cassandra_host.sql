update tds.tenant_config set config_value='ci-tfo-dv-cdb01'
where config_code='cosmos-config.contact-points' 
and tenant_id=(select tenant_id from tds.tenant where tenant_name='tds.dvtfo.onmicrosoft.com');