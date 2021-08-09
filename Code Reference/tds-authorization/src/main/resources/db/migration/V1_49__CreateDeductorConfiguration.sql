/* default tenant config */

INSERT
    INTO [tds].tenant_config ([tenant_id], [config_code], [config_value], [is_active])
   VALUES ((Select tenant_id from [tds].tenant where tenant_name = 'default'),
            'power-bi.uri.access', 'https://login.windows.net/common/oauth2/token', 1);
  
INSERT
    INTO [tds].tenant_config ([tenant_id], [config_code], [config_value], [is_active])
   VALUES ((Select tenant_id from [tds].tenant where tenant_name = 'default'),
            'power-bi.uri.embed', 'https://api.powerbi.com/v1.0/myorg/groups/', 1);

INSERT
    INTO [tds].tenant_config ([tenant_id], [config_code], [config_value], [is_active])
   VALUES ((Select tenant_id from [tds].tenant where tenant_name = 'default'),
            'power-bi.grant.type', 'password', 1);

INSERT
    INTO [tds].tenant_config ([tenant_id], [config_code], [config_value], [is_active])
   VALUES ((Select tenant_id from [tds].tenant where tenant_name = 'default'),
            'power-bi.resource', 'https://analysis.windows.net/powerbi/api', 1);

INSERT
    INTO [tds].tenant_config ([tenant_id], [config_code], [config_value], [is_active])
   VALUES ((Select tenant_id from [tds].tenant where tenant_name = 'default'),
            'power-bi.client.id', '8199f359-ae96-4eb6-ba7e-28a6954f2f3f', 1);

INSERT
    INTO [tds].tenant_config ([tenant_id], [config_code], [config_value], [is_active])
   VALUES ((Select tenant_id from [tds].tenant where tenant_name = 'default'),
            'power-bi.client.secret', 'Bwuh5RUo57e/VL9YwmF@p?rXY-siR.:u', 1);

INSERT
    INTO [tds].tenant_config ([tenant_id], [config_code], [config_value], [is_active])
   VALUES ((Select tenant_id from [tds].tenant where tenant_name = 'default'),
            'power-bi.user.name', 'pbi_tfo_uat@EnY.onmicrosoft.com', 1);

INSERT
    INTO [tds].tenant_config ([tenant_id], [config_code], [config_value], [is_active])
   VALUES ((Select tenant_id from [tds].tenant where tenant_name = 'default'),
            'power-bi.password', 'cT8mdWhGTXNDYHo0bU0=', 1);

INSERT
    INTO [tds].tenant_config ([tenant_id], [config_code], [config_value], [is_active])
   VALUES ((Select tenant_id from [tds].tenant where tenant_name = 'default'),
            'power-bi.group.id', '9698d557-ca05-4f0b-9d66-743ee341b508', 1);
            
INSERT
    INTO [tds].tenant_config ([tenant_id], [config_code], [config_value], [is_active])
   VALUES ((Select tenant_id from [tds].tenant where tenant_name = 'default'),
            'power-bi.section.report.id', 'b9dce4da-61a8-466f-8a8c-795edfcb5232', 1);
            
INSERT
    INTO [tds].tenant_config ([tenant_id], [config_code], [config_value], [is_active])
   VALUES ((Select tenant_id from [tds].tenant where tenant_name = 'default'),
            'power-bi.deductee.report.id', 'xyz', 1);
            
INSERT
    INTO [tds].tenant_config ([tenant_id], [config_code], [config_value], [is_active])
   VALUES ((Select tenant_id from [tds].tenant where tenant_name = 'default'),
            'power-bi.ldc.report.id', 'xyz', 1);



