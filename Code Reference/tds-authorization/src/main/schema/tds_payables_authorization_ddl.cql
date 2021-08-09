
CREATE TABLE tenant(
    tenant_id                   TIMEUUID,
    tenant_name                 TEXT,
    tenant_active               BOOLEAN,
    tenant_created_by           TEXT,
    tenant_modified_by          TEXT,
    tenant_created_date         TIMESTAMP,
    tenant_modified_date        TIMESTAMP,
    PRIMARY KEY(tenant_name, tenant_id)
);

CREATE TABLE tenant_ad_config(
    tenant_ad_config_id             TIMEUUID,
    tenant_ad_config_host           TEXT,
    tenant_ad_config_tenant         TEXT,
    tenant_ad_config_client_id      TEXT,
    tenant_ad_config_client_secret  TEXT,
    tenant_ad_config_user_domains   TEXT,
    tenant_ad_config_created_by     TEXT,
    tenant_ad_config_modified_by    TEXT,
    tenant_ad_config_created_date   TIMESTAMP,
    tenant_ad_config_modified_date  TIMESTAMP,
    tenant_ad_config_active         BOOLEAN,
    PRIMARY KEY(tenant_ad_config_tenant, tenant_ad_config_id)
);

CREATE TABLE tenant_config (
    tenant_config_id              TIMEUUID,
    tenant_config_tenant          TEXT,
    tenant_config_code            TEXT,
    tenant_config_value           TEXT,
    tenant_config_created_by      TEXT,
    tenant_config_modified_by     TEXT,
    tenant_config_created_date    TIMESTAMP,
    tenant_config_modified_date   TIMESTAMP,
    tenant_config_active          BOOLEAN,
    PRIMARY KEY(tenant_config_tenant, tenant_config_id)
);

CREATE TABLE user(
    user_id            TIMEUUID,
    user_username      TEXT,
    user_firstname     TEXT,
    user_middlename    TEXT,
    user_surname       TEXT,
    user_email         TEXT,
    user_createddate   TIMESTAMP,
    user_createduser   INT,
    user_activeflag    BOOLEAN,
    user_modifieddate  TIMESTAMP,
    user_modifieduser  INT ,
    user_source_type   TEXT,
    PRIMARY KEY (user_username, user_id)
);


CREATE TABLE role (
    role_id             TIMEUUID,
    user_id             TIMEUUID,
    role_tenant         TEXT,
    role_code           TEXT,
    role_name           TEXT,
    role_is_default     TEXT,
    role_createddate    TIMESTAMP,
    role_createduser    INT,
    role_activeflag     BOOLEAN,
    role_modifieddate   TIMESTAMP,
    role_modifieduser   INT ,
    role_source_type    TEXT,
    role_is_system      BOOLEAN,
    PRIMARY KEY(role_tenant, user_id, role_id)
);

CREATE TABLE permission(
    permission_id            TIMEUUID,
    permission_role_id       TIMEUUID,
    permission_name          TEXT,
    permission_displayname   TEXT,
    permission_type          TEXT,
    permission_created_by    TEXT,
    permission_modified_by   TEXT,
    permission_created_date  TIMESTAMP,
    permission_modified_date TIMESTAMP,
    permission_tab_name      TEXT,
    permission_grouping_name TEXT,
    permission_label_name    TEXT,
    permission_level         BIGINT,
    permission_sort_order    BIGINT,
    PRIMARY KEY(permission_role_id, permission_id)
);


CREATE TABLE role_permissions
(
  role_permissions_id            TIMEUUID,
  tenant                         TEXT,
  role_id                        TIMEUUID,
  permission_id                  TIMEUUID,
  role_permissions_created_date  TIMESTAMP,
  role_permissions_created_by    TEXT,
  role_permissions_active        BOOLEAN,
  role_permissions_modified_date TIMESTAMP,
  role_permissions_modified_by   TEXT,
  PRIMARY KEY(tenant, role_id, permission_id, role_permissions_id)
);




-- Do we need HSN Masters?
-- Purpose of distribution register table?
-- purpose of isd staging table?
