INSERT INTO tds.tenant_config (tenant_id,config_code,config_value)
SELECT tenant_id,'fileshare-config.account-name','citdsstblbstr01'
FROM tds.tenant 
ORDER BY tenant_id
​
INSERT INTO tds.tenant_config (tenant_id,config_code,config_value)
SELECT tenant_id,'fileshare-config.account-key','**'
FROM tds.tenant 
ORDER BY tenant_id
​
INSERT INTO tds.tenant_config (tenant_id,config_code,config_value)
SELECT tenant_id,'fileshare-config.protocol','https'
FROM tds.tenant 
ORDER BY tenant_id
​
INSERT INTO tds.tenant_config (tenant_id,config_code,config_value)
SELECT tenant_id,'fileshare-config.directorypath','\\citdsstblbstr01.blob.core.windows.net\sftptds\Input'
FROM tds.tenant 
ORDER BY tenant_id
​
INSERT INTO tds.tenant_config (tenant_id,config_code,config_value)
SELECT tenant_id,'fileshare-config.share','sftptds'
FROM tds.tenant 
ORDER BY tenant_id
​
