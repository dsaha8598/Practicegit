update tds.tenant_config set active='1'
where tenant_id=(select tenant_id from tds.tenant where tenant_name='default');
