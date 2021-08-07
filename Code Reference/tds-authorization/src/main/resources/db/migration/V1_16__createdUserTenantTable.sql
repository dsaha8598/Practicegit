GO

/****** Object:  Table [tds].[user_role]    Script Date: 7/19/2019 7:08:56 PM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [tds].[user_tenant](
	[user_tenant_id] [bigint] IDENTITY(1,1) NOT NULL,
	[user_id] [bigint] NULL,
	[tenant_id] [bigint] NULL,
 CONSTRAINT [PK_user_tenant] PRIMARY KEY CLUSTERED 
(
	[user_tenant_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

ALTER TABLE [tds].[user_tenant]  WITH CHECK ADD  CONSTRAINT [FK_user_tenant_tenant] FOREIGN KEY([tenant_id])
REFERENCES [tds].[tenant] ([tenant_id])
GO

ALTER TABLE [tds].[user_tenant] CHECK CONSTRAINT [FK_user_tenant_tenant]
GO

ALTER TABLE [tds].[user_tenant]  WITH CHECK ADD  CONSTRAINT [FK_user_tenant_user] FOREIGN KEY([user_id])
REFERENCES [tds].[user] ([user_id])
GO

ALTER TABLE [tds].[user_tenant] CHECK CONSTRAINT [FK_user_tenant_user]
GO



