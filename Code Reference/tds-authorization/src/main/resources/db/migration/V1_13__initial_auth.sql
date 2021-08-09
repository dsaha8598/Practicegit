GO
/****** Object:  Schema [tds]    Script Date: 7/1/2019 4:23:29 AM ******/
/****** Object:  Table [tds].[tenant]    Script Date: 7/1/2019 4:23:29 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[tenant](
	[tenant_id] [bigint] IDENTITY(1,1) NOT NULL,
	[tenant_name] [varchar](60) NULL,
	[is_active] [bit] NULL,
	[created_by] [varchar](60) NULL,
	[created_date] [datetime] NULL,
	[updated_by] [varchar](60) NULL,
	[updated_date] [datetime] NULL,
	[tenant_code] [varchar](10) NULL,
PRIMARY KEY CLUSTERED 
(
	[tenant_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [uk_tenant_code] UNIQUE NONCLUSTERED 
(
	[tenant_code] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [tds].[tenant_ad_config]    Script Date: 7/1/2019 4:23:29 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[tenant_ad_config](
	[tenant_ad_conf_id] [bigint] IDENTITY(1,1) NOT NULL,
	[domain_host] [varchar](255) NULL,
	[ad_tenant] [varchar](255) NULL,
	[client_id] [varchar](255) NULL,
	[client_secret] [varchar](255) NULL,
	[tenant_id] [bigint] NULL,
	[user_domains] [varchar](max) NULL,
 CONSTRAINT [master_tenantconfig_ad_PK] PRIMARY KEY CLUSTERED 
(
	[tenant_ad_conf_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [tds].[tenant_ad_domain_config]    Script Date: 7/1/2019 4:23:29 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[tenant_ad_domain_config](
	[tenant_ad_domain_config_id] [bigint] IDENTITY(1,1) NOT NULL,
	[tenant_id] [bigint] NOT NULL,
	[user_domain] [varchar](255) NOT NULL,
 CONSTRAINT [master_tenant_ad_domain_config_PK] PRIMARY KEY CLUSTERED 
(
	[tenant_ad_domain_config_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [tds].[tenant_config]    Script Date: 7/1/2019 4:23:29 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[tenant_config](
	[tenant_config_id] [int] IDENTITY(1,1) NOT NULL,
	[tenant_id] [bigint] NULL,
	[config_code] [varchar](50) NOT NULL,
	[config_value] [varchar](255) NOT NULL,
	[is_active] [bit] NOT NULL,
	[deleted_on] [datetime] NULL,
 CONSTRAINT [PK__tenant__6A974A447D825559] PRIMARY KEY CLUSTERED 
(
	[tenant_config_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [tds].[user]    Script Date: 7/1/2019 4:23:29 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[user](
	[user_id] [bigint] IDENTITY(1,1) NOT NULL,
	[user_username] [varchar](255) NULL,
	[user_firstname] [varchar](255) NULL,
	[user_middlename] [varchar](255) NULL,
	[user_surname] [varchar](255) NULL,
	[user_email] [varchar](255) NULL,
	[user_createddate] [datetime] NULL,
	[user_createduser] [varchar](255) NULL,
	[user_activeflag] [bit] NULL,
	[user_modifieddate] [datetime] NULL,
	[user_modifieduser] [varchar](60) NULL,
	[user_source_type] [varchar](60) NULL,
 CONSTRAINT [PK_user] PRIMARY KEY CLUSTERED 
(
	[user_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

/****** Object:  Table [tds].[permission]    Script Date: 7/1/2019 4:23:29 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[permission](
	[permission_id] [bigint] IDENTITY(1,1) NOT NULL,
	[permission_name] [varchar](255) NULL,
	[permission_display_name] [varchar](255) NULL,
	[permission_type] [varchar](255) NULL,
	[tab_name] [varchar](255) NULL,
	[grouping_name] [varchar](255) NULL,
	[label_name] [varchar](255) NULL,
	[level_id] [bigint] NULL,
	[sort_order] [bigint] NULL,
	[active] [bit] NULL,
	[created_by] [varchar](255) NULL,
	[modified_by] [varchar](255) NULL,
	[created_date] [datetime] NULL,
	[modified_date] [datetime] NULL,
 CONSTRAINT [PK_permission] PRIMARY KEY CLUSTERED 
(
	[permission_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [tds].[role]    Script Date: 7/1/2019 4:23:29 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[role](
	[role_id] [bigint] IDENTITY(1,1) NOT NULL,
	[role_name] [varchar](64) NULL,
	[is_default] [bit] NOT NULL,
	[is_system] [bit] NOT NULL,
	[active] [bit] NULL,
	[created_by] [varchar](255) NULL,
	[modified_by] [varchar](255) NULL,
	[created_date] [datetime] NULL,
	[modified_date] [datetime] NULL,
 CONSTRAINT [PK_role] PRIMARY KEY CLUSTERED 
(
	[role_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [tds].[role_permission]    Script Date: 7/1/2019 4:23:29 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[role_permission](
	[role_permission_id] [bigint] IDENTITY(1,1) NOT NULL,
	[role_id] [bigint] NULL,
	[permission_id] [bigint] NULL,
	[active] [bit] NULL,
	[created_by] [varchar](255) NULL,
	[modified_by] [varchar](255) NULL,
	[created_date] [datetime] NULL,
	[modified_date] [datetime] NULL,
 CONSTRAINT [PK_role_permission] PRIMARY KEY CLUSTERED 
(
	[role_permission_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [tds].[user_role]    Script Date: 7/1/2019 4:23:29 AM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[user_role](
	[user_role_id] [bigint] IDENTITY(1,1) NOT NULL,
	[user_id] [bigint] NULL,
	[role_id] [bigint] NULL,
 CONSTRAINT [PK_user_role] PRIMARY KEY CLUSTERED 
(
	[user_role_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
ALTER TABLE [tds].[tenant] ADD  DEFAULT ((1)) FOR [is_active]
GO
ALTER TABLE [tds].[tenant] ADD  DEFAULT (getdate()) FOR [created_date]
GO
ALTER TABLE [tds].[tenant] ADD  DEFAULT (getdate()) FOR [updated_date]
GO
ALTER TABLE [tds].[role] ADD  CONSTRAINT [DF_role_is_default]  DEFAULT ((0)) FOR [is_default]
GO
ALTER TABLE [tds].[role] ADD  CONSTRAINT [DF_role_is_system]  DEFAULT ((0)) FOR [is_system]
GO
ALTER TABLE [tds].[tenant_ad_config]  WITH CHECK ADD FOREIGN KEY([tenant_id])
REFERENCES [tds].[tenant] ([tenant_id])
GO
ALTER TABLE [tds].[tenant_ad_domain_config]  WITH CHECK ADD FOREIGN KEY([tenant_id])
REFERENCES [tds].[tenant] ([tenant_id])
GO
ALTER TABLE [tds].[tenant_config]  WITH CHECK ADD  CONSTRAINT [FK__tenantC__Tenant__17C286CF] FOREIGN KEY([tenant_id])
REFERENCES [tds].[tenant] ([tenant_id])
GO
ALTER TABLE [tds].[tenant_config] CHECK CONSTRAINT [FK__tenantC__Tenant__17C286CF]
GO
ALTER TABLE [tds].[role_permission]  WITH CHECK ADD  CONSTRAINT [FK_role_permission_permission] FOREIGN KEY([permission_id])
REFERENCES [tds].[permission] ([permission_id])
GO
ALTER TABLE [tds].[role_permission] CHECK CONSTRAINT [FK_role_permission_permission]
GO
ALTER TABLE [tds].[role_permission]  WITH CHECK ADD  CONSTRAINT [FK_role_permission_role] FOREIGN KEY([role_id])
REFERENCES [tds].[role] ([role_id])
GO
ALTER TABLE [tds].[role_permission] CHECK CONSTRAINT [FK_role_permission_role]
GO
ALTER TABLE [tds].[user_role]  WITH CHECK ADD  CONSTRAINT [FK_user_role_role] FOREIGN KEY([role_id])
REFERENCES [tds].[role] ([role_id])
GO
ALTER TABLE [tds].[user_role] CHECK CONSTRAINT [FK_user_role_role]
GO
ALTER TABLE [tds].[user_role]  WITH CHECK ADD  CONSTRAINT [FK_user_role_user] FOREIGN KEY([user_id])
REFERENCES [tds].[user] ([user_id])
GO
ALTER TABLE [tds].[user_role] CHECK CONSTRAINT [FK_user_role_user]
GO
