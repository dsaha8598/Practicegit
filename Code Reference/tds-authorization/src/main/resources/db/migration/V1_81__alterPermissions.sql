Update [tds].[permission] set active=0 where permission_name in ('DASHBOARD_LDC','DASHBOARD_DEDUCTEE','DASHBOARD_SECTION');
GO
Update [tds].[permission] set permission_display_name = 'Cfo' where permission_display_name='cfo';
GO
Update [tds].[permission] set permission_display_name = 'Taxhead' where permission_display_name='taxhead';
GO
Update [tds].[permission] set permission_display_name = 'Functional' where permission_display_name='functional';
GO