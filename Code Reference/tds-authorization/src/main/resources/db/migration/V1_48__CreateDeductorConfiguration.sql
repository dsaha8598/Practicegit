/****** Object:  Table [tds].[deductor_onboarding_info]   ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[deductor_onboarding_info](
    [deductor_onboarding_info_id] [int] IDENTITY(1,1) NOT NULL,
    [deductor_master_id] [bigint] NULL,
    [config_code] [varchar](50) NOT NULL,
    [config_value] [varchar](255) NOT NULL,
    [is_active] [bit] NOT NULL,
    [deleted_on] [datetime] NULL,
 CONSTRAINT [PK__deductor_master__6A974A447D825559] PRIMARY KEY CLUSTERED 
(
    [deductor_onboarding_info_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
ALTER TABLE [tds].[deductor_onboarding_info]  WITH CHECK 
ADD  CONSTRAINT [FK__deductor_masterC__Deductor_Master__17C286CF] 
FOREIGN KEY([deductor_master_id])
REFERENCES [tds].[deductor_master] ([id])
GO
ALTER TABLE [tds].[deductor_onboarding_info] CHECK CONSTRAINT [FK__deductor_masterC__Deductor_Master__17C286CF]
GO