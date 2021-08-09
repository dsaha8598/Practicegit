---CREATED_BY DEFAULT CONSTRAINT

ALTER TABLE tds.surcharge_master
ADD CONSTRAINT DF_surcharge_master_created_by  
DEFAULT 'eyadmin' FOR created_by;  

ALTER TABLE tds.tds_master
ADD CONSTRAINT DF_tds_master_created_by  
DEFAULT 'eyadmin' FOR created_by;  

ALTER TABLE tds.tds_month_tracker
ADD CONSTRAINT DF_tds_month_tracker_created_by  
DEFAULT 'eyadmin' FOR created_by;  

ALTER TABLE tds.article_master
ADD CONSTRAINT DF_article_master_created_by  
DEFAULT 'eyadmin' FOR created_by;  

ALTER TABLE tds.cess_master
ADD CONSTRAINT DF_cess_master_created_by  
DEFAULT 'eyadmin' FOR created_by;  

ALTER TABLE tds.cess_type_master
ADD CONSTRAINT DF_cess_type_master_created_by  
DEFAULT 'eyadmin' FOR created_by;  

ALTER TABLE tds.deductor_master
ADD CONSTRAINT DF_deductor_master_created_by  
DEFAULT 'eyadmin' FOR created_by;  

ALTER TABLE tds.fine_rate_master
ADD CONSTRAINT DF_fine_rate_master_created_by  
DEFAULT 'eyadmin' FOR created_by; 

ALTER TABLE tds.group_tenants
ADD CONSTRAINT DF_group_tenants_created_by  
DEFAULT 'eyadmin' FOR created_by; 

ALTER TABLE tds.nature_of_payment_master
ADD CONSTRAINT DF_nature_of_payment_master_created_by  
DEFAULT 'eyadmin' FOR created_by; 

ALTER TABLE tds.role
ADD CONSTRAINT DF_role_created_by  
DEFAULT 'eyadmin' FOR created_by;

ALTER TABLE tds.role_permission
ADD CONSTRAINT DF_role_permission_created_by  
DEFAULT 'eyadmin' FOR created_by;

--MODIFIED_BY DEFAULT CONSTRAINT

ALTER TABLE tds.surcharge_master
ADD CONSTRAINT DF_surcharge_master_modified_by  
DEFAULT 'eyadmin' FOR modified_by;  

ALTER TABLE tds.tds_master
ADD CONSTRAINT DF_tds_master_modified_by  
DEFAULT 'eyadmin' FOR modified_by;  

ALTER TABLE tds.tds_month_tracker
ADD CONSTRAINT DF_tds_month_tracker_modified_by  
DEFAULT 'eyadmin' FOR modified_by; 

ALTER TABLE tds.tenant_ad_domain_config
ADD CONSTRAINT DF_tenant_ad_domain_config_modified_by  
DEFAULT 'eyadmin' FOR modified_by; 

ALTER TABLE tds.article_master
ADD CONSTRAINT DF_article_master_modified_by  
DEFAULT 'eyadmin' FOR modified_by; 

ALTER TABLE tds.cess_master
ADD CONSTRAINT DF_cess_master_modified_by  
DEFAULT 'eyadmin' FOR modified_by;

ALTER TABLE tds.cess_type_master
ADD CONSTRAINT DF_cess_type_master_modified_by  
DEFAULT 'eyadmin' FOR modified_by;

ALTER TABLE tds.deductor_master
ADD CONSTRAINT DF_deductor_master_modified_by  
DEFAULT 'eyadmin' FOR modified_by;

ALTER TABLE tds.fine_rate_master
ADD CONSTRAINT DF_fine_rate_master_modified_by  
DEFAULT 'eyadmin' FOR modified_by;

ALTER TABLE tds.group_tenants
ADD CONSTRAINT DF_group_tenants_modified_by  
DEFAULT 'eyadmin' FOR modified_by;

ALTER TABLE tds.nature_of_payment_master
ADD CONSTRAINT DF_nature_of_payment_master_modified_by  
DEFAULT 'eyadmin' FOR modified_by;

ALTER TABLE tds.role
ADD CONSTRAINT DF_role_modified_by  
DEFAULT 'eyadmin' FOR modified_by;

ALTER TABLE tds.role_permission
ADD CONSTRAINT DF_role_permission_modified_by  
DEFAULT 'eyadmin' FOR modified_by;

--CREATED_DATE DEFAULT CONSTRAINT

ALTER TABLE tds.surcharge_master
ADD CONSTRAINT DF_surcharge_master_created_date 
DEFAULT CURRENT_TIMESTAMP FOR created_date; 

ALTER TABLE tds.tds_master
ADD CONSTRAINT DF_tds_master_created_date 
DEFAULT CURRENT_TIMESTAMP FOR created_date; 

ALTER TABLE tds.tds_month_tracker
ADD CONSTRAINT DF_tds_month_tracker_created_date 
DEFAULT CURRENT_TIMESTAMP FOR created_date; 

ALTER TABLE tds.article_master
ADD CONSTRAINT DF_article_master_created_date 
DEFAULT CURRENT_TIMESTAMP FOR created_date; 

ALTER TABLE tds.cess_master
ADD CONSTRAINT DF_cess_master_created_date 
DEFAULT CURRENT_TIMESTAMP FOR created_date; 

ALTER TABLE tds.cess_type_master
ADD CONSTRAINT DF_cess_type_master_created_date 
DEFAULT CURRENT_TIMESTAMP FOR created_date; 

ALTER TABLE tds.deductor_master
ADD CONSTRAINT DF_deductor_master_created_date 
DEFAULT CURRENT_TIMESTAMP FOR created_date; 

ALTER TABLE tds.fine_rate_master
ADD CONSTRAINT DF_fine_rate_master_created_date 
DEFAULT CURRENT_TIMESTAMP FOR created_date; 

ALTER TABLE tds.group_tenants
ADD CONSTRAINT DF_group_tenants_created_date 
DEFAULT CURRENT_TIMESTAMP FOR created_date; 

ALTER TABLE tds.nature_of_payment_master
ADD CONSTRAINT DF_nature_of_payment_master_created_date 
DEFAULT CURRENT_TIMESTAMP FOR created_date; 

ALTER TABLE tds.role
ADD CONSTRAINT DF_role_created_date 
DEFAULT CURRENT_TIMESTAMP FOR created_date; 

ALTER TABLE tds.role_permission
ADD CONSTRAINT DF_role_permission_created_date 
DEFAULT CURRENT_TIMESTAMP FOR created_date; 

---MODIFIED_DATE DEFAULT CONSTRAINT

ALTER TABLE tds.surcharge_master
ADD CONSTRAINT DF_surcharge_master_modified_date
DEFAULT CURRENT_TIMESTAMP FOR modified_date;

ALTER TABLE tds.tds_master
ADD CONSTRAINT DF_tds_master_modified_date
DEFAULT CURRENT_TIMESTAMP FOR modified_date;

ALTER TABLE tds.tds_month_tracker
ADD CONSTRAINT DF_tds_month_tracker_modified_date
DEFAULT CURRENT_TIMESTAMP FOR modified_date;

ALTER TABLE tds.article_master
ADD CONSTRAINT DF_article_master_modified_date
DEFAULT CURRENT_TIMESTAMP FOR modified_date;

ALTER TABLE tds.cess_master
ADD CONSTRAINT DF_cess_master_modified_date
DEFAULT CURRENT_TIMESTAMP FOR modified_date;

ALTER TABLE tds.cess_type_master
ADD CONSTRAINT DF_cess_type_master_modified_date
DEFAULT CURRENT_TIMESTAMP FOR modified_date;

ALTER TABLE tds.deductor_master
ADD CONSTRAINT DF_deductor_master_modified_date
DEFAULT CURRENT_TIMESTAMP FOR modified_date;

ALTER TABLE tds.fine_rate_master
ADD CONSTRAINT DF_fine_rate_master_modified_date
DEFAULT CURRENT_TIMESTAMP FOR modified_date;

ALTER TABLE tds.group_tenants
ADD CONSTRAINT DF_group_tenants_modified_date
DEFAULT CURRENT_TIMESTAMP FOR modified_date;

ALTER TABLE tds.nature_of_payment_master
ADD CONSTRAINT DF_nature_of_payment_master_modified_date
DEFAULT CURRENT_TIMESTAMP FOR modified_date;

ALTER TABLE tds.role
ADD CONSTRAINT DF_role_modified_date
DEFAULT CURRENT_TIMESTAMP FOR modified_date;

ALTER TABLE tds.role_permission
ADD CONSTRAINT DF_role_permission_modified_date
DEFAULT CURRENT_TIMESTAMP FOR modified_date;
