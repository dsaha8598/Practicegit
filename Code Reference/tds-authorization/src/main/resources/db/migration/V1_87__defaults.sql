ALTER TABLE tds.address
    ADD CONSTRAINT address_active  
    DEFAULT 1 FOR active;  

ALTER TABLE tds.address
    ADD CONSTRAINT address_created_by  
    DEFAULT 'EY Admin' FOR created_by;  

ALTER TABLE tds.address
    ADD CONSTRAINT address_modified_by  
    DEFAULT 'EY Admin' FOR modified_by;  

ALTER TABLE tds.address
    ADD CONSTRAINT address_created_date 
    DEFAULT CURRENT_TIMESTAMP FOR created_date;  

ALTER TABLE tds.address
    ADD CONSTRAINT address_modified_date
    DEFAULT CURRENT_TIMESTAMP FOR modified_date;

--tds.ao_master;
ALTER TABLE tds.ao_master
    ADD CONSTRAINT ao_master_active 
    DEFAULT 1 FOR active;  

ALTER TABLE tds.ao_master
    ADD CONSTRAINT ao_master_created_by  
    DEFAULT 'EY Admin' FOR created_by;  

ALTER TABLE tds.ao_master
    ADD CONSTRAINT ao_master_modified_by  
    DEFAULT 'EY Admin' FOR modified_by;  

ALTER TABLE tds.ao_master
    ADD CONSTRAINT ao_master_created_date 
    DEFAULT CURRENT_TIMESTAMP FOR created_date;  

ALTER TABLE tds.ao_master
    ADD CONSTRAINT ao_master_modified_date
    DEFAULT CURRENT_TIMESTAMP FOR modified_date;
--tds.article_master_conditions;
ALTER TABLE tds.article_master_conditions
    ADD CONSTRAINT article_master_conditions_active  
    DEFAULT 1 FOR active;  

ALTER TABLE tds.article_master_conditions
    ADD CONSTRAINT article_master_conditions_created_by  
    DEFAULT 'EY Admin' FOR created_by;  

ALTER TABLE tds.article_master_conditions
    ADD CONSTRAINT article_master_conditions_modified_by  
    DEFAULT 'EY Admin' FOR modified_by;  

ALTER TABLE tds.article_master_conditions
    ADD CONSTRAINT article_master_conditions_created_date 
    DEFAULT CURRENT_TIMESTAMP FOR created_date;  

ALTER TABLE tds.article_master_conditions
    ADD CONSTRAINT article_master_conditions_modified_date
    DEFAULT CURRENT_TIMESTAMP FOR modified_date;
--tds.article_master_detailed_conditions;
ALTER TABLE tds.article_master_detailed_conditions
    ADD CONSTRAINT article_master_detailed_conditions_active  
    DEFAULT 1 FOR active;  

ALTER TABLE tds.article_master_detailed_conditions
    ADD CONSTRAINT article_master_detailed_conditions_created_by  
    DEFAULT 'EY Admin' FOR created_by;  

ALTER TABLE tds.article_master_detailed_conditions
    ADD CONSTRAINT article_master_detailed_conditions_modified_by  
    DEFAULT 'EY Admin' FOR modified_by;  

ALTER TABLE tds.article_master_detailed_conditions
    ADD CONSTRAINT article_master_detailed_conditions_created_date 
    DEFAULT CURRENT_TIMESTAMP FOR created_date;  

ALTER TABLE tds.article_master_detailed_conditions
    ADD CONSTRAINT article_master_detailed_conditions_modified_date
    DEFAULT CURRENT_TIMESTAMP FOR modified_date;
--tds.basis_of_cess_details;
ALTER TABLE tds.basis_of_cess_details
    ADD CONSTRAINT basis_of_cess_details_active  
    DEFAULT 1 FOR active;  

ALTER TABLE tds.basis_of_cess_details
    ADD CONSTRAINT basis_of_cess_details_created_by  
    DEFAULT 'EY Admin' FOR created_by;  

ALTER TABLE tds.basis_of_cess_details
    ADD CONSTRAINT basis_of_cess_details_modified_by  
    DEFAULT 'EY Admin' FOR modified_by;  

ALTER TABLE tds.basis_of_cess_details
    ADD CONSTRAINT basis_of_cess_details_created_date 
    DEFAULT CURRENT_TIMESTAMP FOR created_date;  

ALTER TABLE tds.basis_of_cess_details
    ADD CONSTRAINT basis_of_cess_details_modified_date
    DEFAULT CURRENT_TIMESTAMP FOR modified_date;
--tds.basis_of_surcharge_details;
ALTER TABLE tds.basis_of_surcharge_details
    ADD CONSTRAINT basis_of_surcharge_details_active  
    DEFAULT 1 FOR active;  

ALTER TABLE tds.basis_of_surcharge_details
    ADD CONSTRAINT basis_of_surcharge_details_created_by  
    DEFAULT 'EY Admin' FOR created_by;  

ALTER TABLE tds.basis_of_surcharge_details
    ADD CONSTRAINT basis_of_surcharge_details_modified_by  
    DEFAULT 'EY Admin' FOR modified_by;  

ALTER TABLE tds.basis_of_surcharge_details
    ADD CONSTRAINT basis_of_surcharge_details_created_date 
    DEFAULT CURRENT_TIMESTAMP FOR created_date;  

ALTER TABLE tds.basis_of_surcharge_details
    ADD CONSTRAINT basis_of_surcharge_details_modified_date
    DEFAULT CURRENT_TIMESTAMP FOR modified_date;
--tds.country;
ALTER TABLE tds.country
    ADD CONSTRAINT country_active  
    DEFAULT 1 FOR active;  

ALTER TABLE tds.country
    ADD CONSTRAINT country_created_by  
    DEFAULT 'EY Admin' FOR created_by;  

ALTER TABLE tds.country
    ADD CONSTRAINT country_modified_by  
    DEFAULT 'EY Admin' FOR modified_by;  

ALTER TABLE tds.country
    ADD CONSTRAINT country_created_date 
    DEFAULT CURRENT_TIMESTAMP FOR created_date;  

ALTER TABLE tds.country
    ADD CONSTRAINT country_modified_date
    DEFAULT CURRENT_TIMESTAMP FOR modified_date;
--tds.deductor_onboarding_info;
ALTER TABLE tds.deductor_onboarding_info
    ADD CONSTRAINT deductor_onboarding_info_active  
    DEFAULT 1 FOR active;  

ALTER TABLE tds.deductor_onboarding_info
    ADD CONSTRAINT deductor_onboarding_info_created_by  
    DEFAULT 'EY Admin' FOR created_by;  

ALTER TABLE tds.deductor_onboarding_info
    ADD CONSTRAINT deductor_onboarding_info_modified_by  
    DEFAULT 'EY Admin' FOR modified_by;  

ALTER TABLE tds.deductor_onboarding_info
    ADD CONSTRAINT deductor_onboarding_info_created_date 
    DEFAULT CURRENT_TIMESTAMP FOR created_date;  

ALTER TABLE tds.deductor_onboarding_info
    ADD CONSTRAINT deductor_onboarding_info_modified_date
    DEFAULT CURRENT_TIMESTAMP FOR modified_date;
--tds.deductor_tan_details;
ALTER TABLE tds.deductor_tan_details
    ADD CONSTRAINT deductor_tan_details_active  
    DEFAULT 1 FOR active;  

ALTER TABLE tds.deductor_tan_details
    ADD CONSTRAINT deductor_tan_details_created_by  
    DEFAULT 'EY Admin' FOR created_by;  

ALTER TABLE tds.deductor_tan_details
    ADD CONSTRAINT deductor_tan_details_modified_by  
    DEFAULT 'EY Admin' FOR modified_by;  

ALTER TABLE tds.deductor_tan_details
    ADD CONSTRAINT deductor_tan_details_created_date 
    DEFAULT CURRENT_TIMESTAMP FOR created_date;  

ALTER TABLE tds.deductor_tan_details
    ADD CONSTRAINT deductor_tan_details_modified_date
    DEFAULT CURRENT_TIMESTAMP FOR modified_date;
--tds.deductor_tenant;
ALTER TABLE tds.deductor_tenant
    ADD CONSTRAINT deductor_tenant_active  
    DEFAULT 1 FOR active;  

ALTER TABLE tds.deductor_tenant
    ADD CONSTRAINT deductor_tenant_created_by  
    DEFAULT 'EY Admin' FOR created_by;  

ALTER TABLE tds.deductor_tenant
    ADD CONSTRAINT deductor_tenant_modified_by  
    DEFAULT 'EY Admin' FOR modified_by;  

ALTER TABLE tds.deductor_tenant
    ADD CONSTRAINT deductor_tenant_created_date 
    DEFAULT CURRENT_TIMESTAMP FOR created_date;  

ALTER TABLE tds.deductor_tenant
    ADD CONSTRAINT deductor_tenant_modified_date
    DEFAULT CURRENT_TIMESTAMP FOR modified_date;
--tds.deductor_type;
ALTER TABLE tds.deductor_type
    ADD CONSTRAINT deductor_type_active  
    DEFAULT 1 FOR active;  

ALTER TABLE tds.deductor_type
    ADD CONSTRAINT deductor_type_created_by  
    DEFAULT 'EY Admin' FOR created_by;  

ALTER TABLE tds.deductor_type
    ADD CONSTRAINT deductor_type_modified_by  
    DEFAULT 'EY Admin' FOR modified_by;  

ALTER TABLE tds.deductor_type
    ADD CONSTRAINT deductor_type_created_date 
    DEFAULT CURRENT_TIMESTAMP FOR created_date;  

ALTER TABLE tds.deductor_type
    ADD CONSTRAINT deductor_type_modified_date
    DEFAULT CURRENT_TIMESTAMP FOR modified_date;
--tds.[group];
ALTER TABLE tds.[group]
    ADD CONSTRAINT group_active  
    DEFAULT 1 FOR active;  

ALTER TABLE tds.[group]
    ADD CONSTRAINT group_created_by  
    DEFAULT 'EY Admin' FOR created_by;  

ALTER TABLE tds.[group]
    ADD CONSTRAINT group_modified_by  
    DEFAULT 'EY Admin' FOR modified_by;  

ALTER TABLE tds.[group]
    ADD CONSTRAINT group_created_date 
    DEFAULT CURRENT_TIMESTAMP FOR created_date;  

ALTER TABLE tds.[group]
    ADD CONSTRAINT group_modified_date
    DEFAULT CURRENT_TIMESTAMP FOR modified_date;
--tds.ldc_master;
ALTER TABLE tds.ldc_master
    ADD CONSTRAINT ldc_master_active  
    DEFAULT 1 FOR active;  

ALTER TABLE tds.ldc_master
    ADD CONSTRAINT ldc_master_created_by  
    DEFAULT 'EY Admin' FOR created_by;  

ALTER TABLE tds.ldc_master
    ADD CONSTRAINT ldc_master_modified_by  
    DEFAULT 'EY Admin' FOR modified_by;  

ALTER TABLE tds.ldc_master
    ADD CONSTRAINT ldc_master_created_date 
    DEFAULT CURRENT_TIMESTAMP FOR created_date;  

ALTER TABLE tds.ldc_master
    ADD CONSTRAINT ldc_master_modified_date
    DEFAULT CURRENT_TIMESTAMP FOR modified_date;
--tds.mode_of_payment;
ALTER TABLE tds.mode_of_payment
    ADD CONSTRAINT mode_of_payment_active  
    DEFAULT 1 FOR active;  

ALTER TABLE tds.mode_of_payment
    ADD CONSTRAINT mode_of_payment_created_by  
    DEFAULT 'EY Admin' FOR created_by;  

ALTER TABLE tds.mode_of_payment
    ADD CONSTRAINT mode_of_payment_modified_by  
    DEFAULT 'EY Admin' FOR modified_by;  

ALTER TABLE tds.mode_of_payment
    ADD CONSTRAINT mode_of_payment_created_date 
    DEFAULT CURRENT_TIMESTAMP FOR created_date;  

ALTER TABLE tds.mode_of_payment
    ADD CONSTRAINT mode_of_payment_modified_date
    DEFAULT CURRENT_TIMESTAMP FOR modified_date;
--tds.pan;
ALTER TABLE tds.pan
    ADD CONSTRAINT pan_active  
    DEFAULT 1 FOR active;  

ALTER TABLE tds.pan
    ADD CONSTRAINT pan_created_by  
    DEFAULT 'EY Admin' FOR created_by;  

ALTER TABLE tds.pan
    ADD CONSTRAINT pan_modified_by  
    DEFAULT 'EY Admin' FOR modified_by;  

ALTER TABLE tds.pan
    ADD CONSTRAINT pan_created_date 
    DEFAULT CURRENT_TIMESTAMP FOR created_date;  

ALTER TABLE tds.pan
    ADD CONSTRAINT pan_modified_date
    DEFAULT CURRENT_TIMESTAMP FOR modified_date;
--tds.provision;
ALTER TABLE tds.provision
    ADD CONSTRAINT provision_active  
    DEFAULT 1 FOR active;  

ALTER TABLE tds.provision
    ADD CONSTRAINT provision_created_by  
    DEFAULT 'EY Admin' FOR created_by;  

ALTER TABLE tds.provision
    ADD CONSTRAINT provision_modified_by  
    DEFAULT 'EY Admin' FOR modified_by;  

ALTER TABLE tds.provision
    ADD CONSTRAINT provision_created_date 
    DEFAULT CURRENT_TIMESTAMP FOR created_date;  

ALTER TABLE tds.provision
    ADD CONSTRAINT provision_modified_date
    DEFAULT CURRENT_TIMESTAMP FOR modified_date;
--tds.residential_status;
ALTER TABLE tds.residential_status
    ADD CONSTRAINT residential_status_active  
    DEFAULT 1 FOR active;  

ALTER TABLE tds.residential_status
    ADD CONSTRAINT residential_status_created_by  
    DEFAULT 'EY Admin' FOR created_by;  

ALTER TABLE tds.residential_status
    ADD CONSTRAINT residential_status_modified_by  
    DEFAULT 'EY Admin' FOR modified_by;  

ALTER TABLE tds.residential_status
    ADD CONSTRAINT residential_status_created_date 
    DEFAULT CURRENT_TIMESTAMP FOR created_date;  

ALTER TABLE tds.residential_status
    ADD CONSTRAINT residential_status_modified_date
    DEFAULT CURRENT_TIMESTAMP FOR modified_date;
--tds.state;
ALTER TABLE tds.state
    ADD CONSTRAINT state_active  
    DEFAULT 1 FOR active;  

ALTER TABLE tds.state
    ADD CONSTRAINT state_created_by  
    DEFAULT 'EY Admin' FOR created_by;

ALTER TABLE tds.state
    ADD CONSTRAINT state_modified_by  
    DEFAULT 'EY Admin' FOR modified_by;  

ALTER TABLE tds.state
    ADD CONSTRAINT state_created_date 
    DEFAULT CURRENT_TIMESTAMP FOR created_date;  

ALTER TABLE tds.state
    ADD CONSTRAINT state_modified_date
    DEFAULT CURRENT_TIMESTAMP FOR modified_date;

--tds.status;
ALTER TABLE tds.status
    ADD CONSTRAINT status_active  
    DEFAULT 1 FOR active;  

ALTER TABLE tds.status
    ADD CONSTRAINT status_created_by  
    DEFAULT 'EY Admin' FOR created_by;

ALTER TABLE tds.status
    ADD CONSTRAINT status_modified_by  
    DEFAULT 'EY Admin' FOR modified_by;  

ALTER TABLE tds.status
    ADD CONSTRAINT status_created_date 
    DEFAULT CURRENT_TIMESTAMP FOR created_date;  

ALTER TABLE tds.status
    ADD CONSTRAINT status_modified_date
    DEFAULT CURRENT_TIMESTAMP FOR modified_date;
--tds.sub_nature_payment_master;
ALTER TABLE tds.sub_nature_payment_master
    ADD CONSTRAINT sub_nature_payment_master_active  
    DEFAULT 1 FOR active;  

ALTER TABLE tds.sub_nature_payment_master
    ADD CONSTRAINT sub_nature_payment_master_created_by  
    DEFAULT 'EY Admin' FOR created_by;

ALTER TABLE tds.sub_nature_payment_master
    ADD CONSTRAINT sub_nature_payment_master_modified_by  
    DEFAULT 'EY Admin' FOR modified_by;  

ALTER TABLE tds.sub_nature_payment_master
    ADD CONSTRAINT sub_nature_payment_master_created_date 
    DEFAULT CURRENT_TIMESTAMP FOR created_date;  

ALTER TABLE tds.sub_nature_payment_master
    ADD CONSTRAINT sub_nature_payment_master_modified_date
    DEFAULT CURRENT_TIMESTAMP FOR modified_date;
--tds.tan;
ALTER TABLE tds.tan
    ADD CONSTRAINT tan_active  
    DEFAULT 1 FOR active;  

ALTER TABLE tds.tan
    ADD CONSTRAINT tan_created_by  
    DEFAULT 'EY Admin' FOR created_by;

ALTER TABLE tds.tan
    ADD CONSTRAINT tan_modified_by  
    DEFAULT 'EY Admin' FOR modified_by;  

ALTER TABLE tds.tan
    ADD CONSTRAINT tan_created_date 
    DEFAULT CURRENT_TIMESTAMP FOR created_date;  

ALTER TABLE tds.tan
    ADD CONSTRAINT tan_modified_date
    DEFAULT CURRENT_TIMESTAMP FOR modified_date;
--tds.tenant;
ALTER TABLE tds.tenant
    ADD CONSTRAINT tenant_active  
    DEFAULT 1 FOR active;  

ALTER TABLE tds.tenant
    ADD CONSTRAINT tenant_created_by  
    DEFAULT 'EY Admin' FOR created_by;

ALTER TABLE tds.tenant
    ADD CONSTRAINT tenant_modified_by  
    DEFAULT 'EY Admin' FOR modified_by;  

ALTER TABLE tds.tenant
    ADD CONSTRAINT tenant_modified_date
    DEFAULT CURRENT_TIMESTAMP FOR modified_date;

ALTER TABLE tds.tenant_ad_config
    ADD CONSTRAINT tenant_ad_config_active  
    DEFAULT 1 FOR active;  

ALTER TABLE tds.tenant_ad_config
    ADD CONSTRAINT tenant_ad_config_created_by  
    DEFAULT 'EY Admin' FOR created_by;

ALTER TABLE tds.tenant_ad_config
    ADD CONSTRAINT tenant_ad_config_modified_by  
    DEFAULT 'EY Admin' FOR modified_by;  

ALTER TABLE tds.tenant_ad_config
    ADD CONSTRAINT tenant_ad_config_created_date 
    DEFAULT CURRENT_TIMESTAMP FOR created_date;  

ALTER TABLE tds.tenant_ad_config
    ADD CONSTRAINT tenant_ad_config_modified_date
    DEFAULT CURRENT_TIMESTAMP FOR modified_date;

ALTER TABLE tds.tenant_ad_domain_config
    ADD CONSTRAINT tenant_ad_domain_config_active  
    DEFAULT 1 FOR active;  

ALTER TABLE tds.tenant_ad_domain_config
    ADD CONSTRAINT tenant_ad_domain_config_created_by  
    DEFAULT 'EY Admin' FOR created_by;

ALTER TABLE tds.tenant_ad_domain_config
    ADD CONSTRAINT tenant_ad_domain_config_created_date 
    DEFAULT CURRENT_TIMESTAMP FOR created_date;  

ALTER TABLE tds.tenant_ad_domain_config
    ADD CONSTRAINT tenant_ad_domain_config_modified_date
    DEFAULT CURRENT_TIMESTAMP FOR modified_date;

--tds.tenant_config;
ALTER TABLE tds.tenant_config
    ADD CONSTRAINT tenant_config_active  
    DEFAULT 1 FOR active;  

ALTER TABLE tds.tenant_config
    ADD CONSTRAINT tenant_config_created_by  
    DEFAULT 'EY Admin' FOR created_by;

ALTER TABLE tds.tenant_config
    ADD CONSTRAINT tenant_config_modified_by  
    DEFAULT 'EY Admin' FOR modified_by;  

ALTER TABLE tds.tenant_config
    ADD CONSTRAINT tenant_config_created_date 
    DEFAULT CURRENT_TIMESTAMP FOR created_date;  

ALTER TABLE tds.tenant_config
    ADD CONSTRAINT tenant_config_modified_date
    DEFAULT CURRENT_TIMESTAMP FOR modified_date;
--tds.[user];
ALTER TABLE tds.[user]
    ADD CONSTRAINT user_active  
    DEFAULT 1 FOR active;  

ALTER TABLE tds.[user]
    ADD CONSTRAINT user_created_by  
    DEFAULT 'EY Admin' FOR created_by;

ALTER TABLE tds.[user]
    ADD CONSTRAINT user_modified_by  
    DEFAULT 'EY Admin' FOR modified_by;  

ALTER TABLE tds.[user]
    ADD CONSTRAINT user_created_date 
    DEFAULT CURRENT_TIMESTAMP FOR created_date;  

ALTER TABLE tds.[user]
    ADD CONSTRAINT user_modified_date
    DEFAULT CURRENT_TIMESTAMP FOR modified_date;
--tds.user_organization;
ALTER TABLE tds.user_organization
    ADD CONSTRAINT user_organization_active  
    DEFAULT 1 FOR active;  

ALTER TABLE tds.user_organization
    ADD CONSTRAINT user_organization_created_by  
    DEFAULT 'EY Admin' FOR created_by;

ALTER TABLE tds.user_organization
    ADD CONSTRAINT user_organization_modified_by  
    DEFAULT 'EY Admin' FOR modified_by;  

ALTER TABLE tds.user_organization
    ADD CONSTRAINT user_organization_created_date 
    DEFAULT CURRENT_TIMESTAMP FOR created_date;  

ALTER TABLE tds.user_organization
    ADD CONSTRAINT user_organization_modified_date
    DEFAULT CURRENT_TIMESTAMP FOR modified_date;

--tds.user_role;
ALTER TABLE tds.user_role
    ADD CONSTRAINT user_role_active  
    DEFAULT 1 FOR active;  

ALTER TABLE tds.user_role
    ADD CONSTRAINT user_role_created_by  
    DEFAULT 'EY Admin' FOR created_by;

ALTER TABLE tds.user_role
    ADD CONSTRAINT user_role_modified_by  
    DEFAULT 'EY Admin' FOR modified_by;  

ALTER TABLE tds.user_role
    ADD CONSTRAINT user_role_created_date 
    DEFAULT CURRENT_TIMESTAMP FOR created_date;  

ALTER TABLE tds.user_role
    ADD CONSTRAINT user_role_modified_date
    DEFAULT CURRENT_TIMESTAMP FOR modified_date;

--tds.user_tenant;
ALTER TABLE tds.user_tenant
    ADD CONSTRAINT user_tenant_active  
    DEFAULT 1 FOR active;  

ALTER TABLE tds.user_tenant
    ADD CONSTRAINT user_tenant_created_by  
    DEFAULT 'EY Admin' FOR created_by;

ALTER TABLE tds.user_tenant
    ADD CONSTRAINT user_tenant_modified_by  
    DEFAULT 'EY Admin' FOR modified_by;  

ALTER TABLE tds.user_tenant
    ADD CONSTRAINT user_tenant_created_date 
    DEFAULT CURRENT_TIMESTAMP FOR created_date;  

ALTER TABLE tds.user_tenant
    ADD CONSTRAINT user_tenant_modified_date
    DEFAULT CURRENT_TIMESTAMP FOR modified_date;
