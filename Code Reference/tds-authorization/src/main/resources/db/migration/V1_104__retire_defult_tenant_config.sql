update tds.tenant_config set active='0'
where tenant_id=(select tenant_id from tds.tenant where tenant_name='default');
