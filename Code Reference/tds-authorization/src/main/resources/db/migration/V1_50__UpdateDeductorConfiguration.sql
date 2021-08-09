/* default tenant config */

UPDATE [tds].tenant_config SET [config_value] = 'https://app.powerbi.com/reportEmbed'
	WHERE [tenant_id] = (Select tenant_id from [tds].tenant where tenant_name = 'default')
	AND [config_code] = 'power-bi.uri.embed';

UPDATE [tds].tenant_config SET [config_value] = '39fe2d18-d5b6-4e6c-b14c-68669ccb09c8'
	WHERE [tenant_id] = (Select tenant_id from [tds].tenant where tenant_name = 'default')
	AND [config_code] = 'power-bi.section.report.id';

UPDATE [tds].tenant_config SET [config_value] = 'b9dce4da-61a8-466f-8a8c-795edfcb5232'
	WHERE [tenant_id] = (Select tenant_id from [tds].tenant where tenant_name = 'default')
	AND [config_code] = 'power-bi.deductee.report.id';
	
UPDATE [tds].tenant_config SET [config_value] = '9a5a2c93-4dbe-468a-8a40-776c27ed2250'
	WHERE [tenant_id] = (Select tenant_id from [tds].tenant where tenant_name = 'default')
	AND [config_code] = 'power-bi.ldc.report.id';


