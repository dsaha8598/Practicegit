UPDATE [tds].[user] SET user_email = 'user1@dvtfo.onmicrosoft.com' where user_id = 2;

GO

UPDATE [tds].tenant_config SET config_code = 'user1 tds_payables' where tenant_config_id = 2;

GO

UPDATE [tds].tenant_config SET config_value = 'tds_payables' where tenant_config_id = 2;