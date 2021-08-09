insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'DASHBOARD_CFO', 'cfo', 'Dashboard', 'cfo', '' where not exists (select 1 from tds.permission where permission_name=  'DASHBOARD_TAXHEAD');

insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'DASHBOARD_TAXHEAD', 'taxhead', 'Dashboard', 'taxhead', '' where not exists (select 1 from tds.permission where permission_name=  'DASHBOARD_TAXHEAD');

insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'DASHBOARD_FUNCTIONAL', 'functional', 'Dashboard', 'functional', '' where not exists (select 1 from tds.permission where permission_name=  'DASHBOARD_FUNCTIONAL');

