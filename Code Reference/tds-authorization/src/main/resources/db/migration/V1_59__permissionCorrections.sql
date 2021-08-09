
update tds.permission set permission_display_name ='Create' where permission_name='KEYWORD_MASTER_CREATE';
update tds.permission set permission_display_name ='Update' where permission_name='KEYWORD_MASTER_UPDATE';
update tds.permission set permission_display_name ='View' where permission_name='KEYWORD_MASTER_VIEW';
update tds.permission set permission_display_name ='DEDUCTEE' where permission_name='DASHBOARD_DEDUCTEE';
update tds.permission set permission_display_name ='LDC' where permission_name='DASHBOARD_LDC';
update tds.permission set permission_display_name ='SECTION' where permission_name='DASHBOARD_SECTION';
update tds.permission set label_name =null where permission_name='VALIDATION_PAN';
update tds.permission set label_name =null where permission_name='VALIDATION_LDC';
