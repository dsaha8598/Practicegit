ALTER TABLE tds.address
ADD
active smallint,
created_by [varchar](256),
modified_by [varchar](256),
created_date [datetime],
modified_date [datetime];

ALTER TABLE tds.ao_master
ADD
active smallint,
created_by [varchar](256),
modified_by [varchar](256),
created_date [datetime],
modified_date [datetime];

ALTER TABLE tds.article_master_conditions
ADD
active smallint,
created_by [varchar](256),
modified_by [varchar](256),
created_date [datetime],
modified_date [datetime];

ALTER TABLE tds.article_master_detailed_conditions
ADD
active smallint,
created_by [varchar](256),
modified_by [varchar](256),
created_date [datetime],
modified_date [datetime];

ALTER TABLE tds.basis_of_cess_details
ADD
active smallint,
created_by [varchar](256),
modified_by [varchar](256),
created_date [datetime],
modified_date [datetime];

ALTER TABLE tds.basis_of_surcharge_details
ADD
active smallint,
created_by [varchar](256),
modified_by [varchar](256),
created_date [datetime],
modified_date [datetime];

ALTER TABLE tds.country
ADD
active smallint,
created_by [varchar](256),
modified_by [varchar](256),
created_date [datetime],
modified_date [datetime];


ALTER TABLE tds.deductor_onboarding_info
ADD
active smallint,
created_by [varchar](256),
modified_by [varchar](256),
created_date [datetime],
modified_date [datetime];


ALTER TABLE tds.deductor_tan_details
ADD
active smallint,
created_by [varchar](256),
modified_by [varchar](256),
created_date [datetime],
modified_date [datetime];


ALTER TABLE tds.deductor_tenant
ADD
active smallint;


ALTER TABLE tds.deductor_type
ADD
active smallint,
created_by [varchar](256),
modified_by [varchar](256),
created_date [datetime],
modified_date [datetime];


ALTER TABLE tds.[group]
ADD
active smallint;

ALTER TABLE tds.group_tenants
ADD
active smallint;

ALTER TABLE tds.ldc_master
ADD
active smallint,
created_by [varchar](256),
modified_by [varchar](256),
created_date [datetime],
modified_date [datetime];


ALTER TABLE tds.mode_of_payment
ADD
active smallint,
created_by [varchar](256),
modified_by [varchar](256),
created_date [datetime],
modified_date [datetime];


ALTER TABLE tds.pan
ADD
active smallint,
created_by [varchar](256),
modified_by [varchar](256),
created_date [datetime],
modified_date [datetime];


ALTER TABLE tds.provision
ADD
active smallint,
created_by [varchar](256),
modified_by [varchar](256),
created_date [datetime],
modified_date [datetime];

ALTER TABLE tds.residential_status
ADD
active smallint,
created_by [varchar](256),
modified_by [varchar](256),
created_date [datetime],
modified_date [datetime];

ALTER TABLE tds.state
ADD
active smallint,
created_by [varchar](256),
modified_by [varchar](256),
created_date [datetime],
modified_date [datetime];

ALTER TABLE tds.status
ADD
active smallint,
created_by [varchar](256),
modified_by [varchar](256),
created_date [datetime],
modified_date [datetime];

ALTER TABLE tds.sub_nature_payment_master
ADD
active smallint,
created_by [varchar](256),
modified_by [varchar](256),
created_date [datetime],
modified_date [datetime];

ALTER TABLE tds.tan
ADD
active smallint,
created_by [varchar](256),
modified_by [varchar](256),
created_date [datetime],
modified_date [datetime];

ALTER TABLE tds.tenant
ADD
active smallint,
modified_by [varchar](256),
modified_date [datetime];

ALTER TABLE tds.tenant_ad_config
ADD
active smallint,
created_by [varchar](256),
modified_by [varchar](256),
created_date [datetime],
modified_date [datetime];


ALTER TABLE tds.tenant_ad_domain_config
ADD
active smallint,
created_by [varchar](256),
modified_by [varchar](256),
created_date [datetime],
modified_date [datetime];


ALTER TABLE tds.tenant_config
ADD
active smallint,
created_by [varchar](256),
modified_by [varchar](256),
created_date [datetime],
modified_date [datetime];


ALTER TABLE tds.[user]
ADD
active smallint,
created_by [varchar](256),
modified_by [varchar](256),
created_date [datetime],
modified_date [datetime];


ALTER TABLE tds.user_organization
ADD
active smallint,
created_by [varchar](256),
modified_by [varchar](256),
created_date [datetime],
modified_date [datetime];



ALTER TABLE tds.user_role
ADD
active smallint,
created_by [varchar](256),
modified_by [varchar](256),
created_date [datetime],
modified_date [datetime];



ALTER TABLE tds.user_tenant
ADD
active smallint,
created_by [varchar](256),
modified_by [varchar](256),
created_date [datetime],
modified_date [datetime];


