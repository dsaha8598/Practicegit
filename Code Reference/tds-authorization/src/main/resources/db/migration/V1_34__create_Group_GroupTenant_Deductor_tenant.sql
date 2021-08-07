SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[group](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[group_code] [varchar] (100) NULL,
	[group_name] [varchar](100) NULL,
	[description] [varchar](255) NULL,	
	[created_by] [varchar](100) NULL,
	[created_date] [datetime] NULL,
	[modified_by] [varchar](100) NULL,
	[modified_date] [datetime] NULL,
 CONSTRAINT [PK_group_id] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [group$id_UNIQUE] UNIQUE NONCLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO


SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[group_tenants](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[group_id] [bigint] NOT NULL,
	[tenant_id] [bigint] NOT NULL,
	[created_by] [varchar](100) NULL,
	[created_date] [datetime] NULL,
	[modified_by] [varchar](100) NULL,
	[modified_date] [datetime] NULL,
 CONSTRAINT [PK_group_tenants_id] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [group_tenants$id_UNIQUE] UNIQUE NONCLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[deductor_tenant](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[deductor_id] [bigint] NOT NULL,
	[tenant_id] [bigint] NOT NULL,
	[created_by] [varchar](100) NULL,
	[created_date] [datetime] NULL,
	[modified_by] [varchar](100) NULL,
	[modified_date] [datetime] NULL,
 CONSTRAINT [PK_deductor_tenant_id] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [deductor_tenant$id_UNIQUE] UNIQUE NONCLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

alter table tds.group_tenants
add constraint group_tenants_tenant_id_FK FOREIGN KEY ( tenant_id ) references [tds].[tenant](tenant_id)

alter table tds.group_tenants
add constraint group_tenants_group_id_FK FOREIGN KEY ( group_id ) references [tds].[group](id)

alter table tds.deductor_tenant
add constraint deductor_tenant_tenant_id_FK FOREIGN KEY ( tenant_id ) references [tds].[tenant](tenant_id)

alter table tds.deductor_tenant
add constraint deductor_tenant_deductor_id_FK FOREIGN KEY ( deductor_id ) references [tds].[deductor_master](id)