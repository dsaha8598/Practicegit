CREATE SCHEMA [tds]
GO
/****** Object:  Table [tds].[address]    Script Date: 6/9/2019 6:37:59 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[address](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[flat_door_block_no] [varchar](256) NOT NULL,
	[name_building_village] [varchar](256) NULL,
	[road_street_postoffice] [varchar](256) NULL,
	[area_locality] [varchar](256) NOT NULL,
	[town_city_district] [varchar](256) NOT NULL,
	[state_id] [bigint] NOT NULL,
	[pin_code] [varchar](64) NOT NULL,
	[applicable_from] [datetime] NOT NULL,
	[applicable_to] [datetime] NULL,
 CONSTRAINT [PK_address_id] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [address$id_UNIQUE] UNIQUE NONCLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [tds].[ao_master]    Script Date: 6/9/2019 6:37:59 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[ao_master](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[ao_certificate_number] [varchar](100) NOT NULL,
	[deductor_name] [varchar](500) NOT NULL,
	[deductor_tan] [varchar](100) NOT NULL,
	[deductee_name] [varchar](500) NOT NULL,
	[deductee_pan] [varchar](50) NOT NULL,
	[section] [varchar](50) NOT NULL,
	[nature_of_payment_id] [bigint] NOT NULL,
	[amount] [bigint] NULL,
	[ao_rate] [bigint] NULL,
	[limit_utilised] [bigint] NULL,
	[from_date] [datetime] NULL,
	[to_date] [datetime] NULL
) ON [PRIMARY]
GO
/****** Object:  Table [tds].[article_master]    Script Date: 6/9/2019 6:37:59 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[article_master](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[number] [varchar](64) NOT NULL,
	[name] [varchar](128) NOT NULL,
	[is_inclusion_or_exclusion] [smallint] NOT NULL,
	[rate] [int] NOT NULL,
	[applicable_from] [datetime] NOT NULL,
	[applicable_to] [datetime] NULL,
	[country] [varchar](50) NULL,
 CONSTRAINT [PK_article_master_id] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [article_master$id_UNIQUE] UNIQUE NONCLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [tds].[article_master_conditions]    Script Date: 6/9/2019 6:37:59 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[article_master_conditions](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[condition] [varchar](256) NOT NULL,
	[is_inclusion] [smallint] NOT NULL,
	[article_master_id] [bigint] NOT NULL,
	[is_detailed_condition_applicable] [smallint] NOT NULL,
 CONSTRAINT [PK_article_master_conditions_id] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [article_master_conditions$id_UNIQUE] UNIQUE NONCLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [tds].[article_master_detailed_conditions]    Script Date: 6/9/2019 6:37:59 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[article_master_detailed_conditions](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[condition] [varchar](256) NOT NULL,
	[article_master_condition_id] [bigint] NOT NULL,
 CONSTRAINT [PK_article_master_detailed_conditions_id] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [article_master_detailed_conditions$id_UNIQUE] UNIQUE NONCLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [tds].[basis_of_cess_details]    Script Date: 6/9/2019 6:37:59 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[basis_of_cess_details](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[cess_master_id] [bigint] NOT NULL,
	[nature_of_payment_master_id] [bigint] NULL,
	[invoice_slab_from] [datetime] NULL,
	[invoice_slab_to] [datetime] NULL,
	[deductee_status_id] [bigint] NULL,
	[deductee_residential_status_id] [bigint] NULL,
	[rate] [int] NULL,
 CONSTRAINT [PK_basis_of_cess_details_id] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [basis_of_cess_details$id_UNIQUE] UNIQUE NONCLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [tds].[basis_of_surcharge_details]    Script Date: 6/9/2019 6:37:59 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[basis_of_surcharge_details](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[surcharge_master_id] [bigint] NOT NULL,
	[nature_of_payment_master_id] [bigint] NULL,
	[invoice_slab_from] [datetime] NULL,
	[invoice_slab_to] [datetime] NULL,
	[deductee_status_id] [bigint] NULL,
	[rate] [int] NULL,
	[deductee_residential_status_id] [bigint] NULL,
 CONSTRAINT [id] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [tds].[cess_master]    Script Date: 6/9/2019 6:37:59 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[cess_master](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[boc_nature_of_payment] [smallint] NULL,
	[boc_invoice_slab] [smallint] NULL,
	[boc_deductee_status] [smallint] NULL,
	[boc_residential_status] [smallint] NULL,
	[rate] [int] NULL,
	[applicable_from] [datetime] NOT NULL,
	[applicable_to] [datetime] NULL,
	[cess_type_master_id] [bigint] NOT NULL,
	[is_cess_applicable_at_flat_rate] [smallint] NULL,
 CONSTRAINT [PK_cess_master_id] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [cess_master$id_UNIQUE] UNIQUE NONCLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [tds].[cess_type_master]    Script Date: 6/9/2019 6:37:59 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[cess_type_master](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[type] [varchar](128) NOT NULL,
	[applicable_from] [datetime] NOT NULL,
	[applicable_to] [datetime] NULL,
 CONSTRAINT [PK_cess_type_master_id] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [cess_type_master$id_UNIQUE] UNIQUE NONCLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [tds].[country]    Script Date: 6/9/2019 6:37:59 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[country](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[name] [varchar](128) NOT NULL,
 CONSTRAINT [PK_country_id] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [country$id_UNIQUE] UNIQUE NONCLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [tds].[deductor_master]    Script Date: 6/9/2019 6:37:59 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[deductor_master](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[code] [varchar](32) NOT NULL,
	[name] [varchar](128) NOT NULL,
	[residential_status_id] [bigint] NOT NULL,
	[have_more_than_one_branch] [smallint] NOT NULL,
	[status_id] [bigint] NOT NULL,
	[type_id] [bigint] NOT NULL,
	[mode_of_payment_id] [bigint] NOT NULL,
	[due_date_of_tax_payment] [datetime] NULL,
	[provision_id] [bigint] NULL,
	[email] [varchar](256) NULL,
	[address_id] [bigint] NOT NULL,
	[phone_number] [varchar](32) NULL,
	[applicable_from] [datetime] NULL,
	[applicable_to] [datetime] NULL,
	[pan] [varchar](50) NULL,
	[pan_id] [bigint] NULL,
	[provision] [varchar](50) NULL,
 CONSTRAINT [PK_deductor_master_id] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [deductor_master$id_UNIQUE] UNIQUE NONCLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [tds].[deductor_tan_details]    Script Date: 6/9/2019 6:37:59 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[deductor_tan_details](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[deductor_id] [bigint] NOT NULL,
	[tan_id] [bigint] NOT NULL,
 CONSTRAINT [PK_deductor_tan_details_id] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [deductor_tan_details$id_UNIQUE] UNIQUE NONCLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [tds].[deductor_type]    Script Date: 6/9/2019 6:37:59 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[deductor_type](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[type] [varchar](64) NOT NULL,
 CONSTRAINT [PK_deductor_type_id] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [deductor_type$id_UNIQUE] UNIQUE NONCLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [tds].[fine_rate_master]    Script Date: 6/9/2019 6:37:59 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[fine_rate_master](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[interest_type] [varchar](128) NOT NULL,
	[rate] [int] NULL,
	[fine_per_day] [bigint] NULL,
	[applicable_from] [datetime] NOT NULL,
	[applicable_to] [datetime] NULL,
	[type_of_intrest_calculation] [varchar](50) NULL,
 CONSTRAINT [PK_fine_rate_master_id] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [fine_rate_master$id_UNIQUE] UNIQUE NONCLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [tds].[ldc_master]    Script Date: 6/9/2019 6:37:59 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[ldc_master](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[ldc_certificate_number] [varchar](100) NOT NULL,
	[deductor_name] [varchar](500) NOT NULL,
	[deductor_tan] [varchar](100) NOT NULL,
	[deductee_name] [varchar](500) NOT NULL,
	[deductee_pan] [varchar](50) NOT NULL,
	[section] [varchar](50) NOT NULL,
	[amount] [bigint] NULL,
	[ldc_rate] [bigint] NULL,
	[limit_utilised] [bigint] NULL,
	[from_date] [datetime] NULL,
	[to_date] [datetime] NULL,
	[nature_of_payment_id] [bigint] NULL
) ON [PRIMARY]
GO
/****** Object:  Table [tds].[mode_of_payment]    Script Date: 6/9/2019 6:37:59 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[mode_of_payment](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[mode] [varchar](64) NOT NULL,
 CONSTRAINT [PK_mode_of_payment_id] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [mode_of_payment$id_UNIQUE] UNIQUE NONCLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [tds].[nature_of_payment_master]    Script Date: 6/9/2019 6:37:59 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[nature_of_payment_master](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[section] [varchar](64) NOT NULL,
	[nature] [varchar](128) NOT NULL,
	[display_value] [varchar](64) NOT NULL,
	[keywords] [varchar](128) NOT NULL,
	[applicable_from] [datetime] NOT NULL,
	[applicable_to] [datetime] NULL,
	[is_sub_nature_payment_applies] [smallint] NOT NULL,
 CONSTRAINT [PK_nature_of_payment_master_id] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [nature_of_payment_master$id_UNIQUE] UNIQUE NONCLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [tds].[pan]    Script Date: 6/9/2019 6:37:59 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[pan](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[pan] [varchar](32) NOT NULL,
 CONSTRAINT [PK_pan_id] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [pan$id_UNIQUE] UNIQUE NONCLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [pan$pan_UNIQUE] UNIQUE NONCLUSTERED 
(
	[pan] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [tds].[provision]    Script Date: 6/9/2019 6:37:59 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[provision](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[code] [varchar](32) NOT NULL,
 CONSTRAINT [PK_provision_id] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [provision$id_UNIQUE] UNIQUE NONCLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [tds].[residential_status]    Script Date: 6/9/2019 6:37:59 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[residential_status](
	[id] [bigint] NOT NULL,
	[status] [varchar](64) NOT NULL,
 CONSTRAINT [PK_residential_status_id] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [residential_status$id_UNIQUE] UNIQUE NONCLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [residential_status$status_UNIQUE] UNIQUE NONCLUSTERED 
(
	[status] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [tds].[state]    Script Date: 6/9/2019 6:37:59 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[state](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[name] [varchar](256) NOT NULL,
	[country_id] [bigint] NOT NULL,
 CONSTRAINT [PK_state_id] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [state$id_UNIQUE] UNIQUE NONCLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [tds].[status]    Script Date: 6/9/2019 6:37:59 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[status](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[status] [varchar](64) NOT NULL,
	[pan_code] [char](1) NOT NULL,
 CONSTRAINT [PK_status_id] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [status$id_UNIQUE] UNIQUE NONCLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [status$pan_code_UNIQUE] UNIQUE NONCLUSTERED 
(
	[pan_code] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [status$status_UNIQUE] UNIQUE NONCLUSTERED 
(
	[status] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [tds].[sub_nature_payment_master]    Script Date: 6/9/2019 6:37:59 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[sub_nature_payment_master](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[nature] [varchar](128) NOT NULL,
	[nature_payment_master_id] [bigint] NOT NULL,
 CONSTRAINT [PK_sub_nature_payment_master_id] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [sub_nature_payment_master$id_UNIQUE] UNIQUE NONCLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [sub_nature_payment_master$nature_UNIQUE] UNIQUE NONCLUSTERED 
(
	[nature] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [tds].[surcharge_master]    Script Date: 6/9/2019 6:37:59 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[surcharge_master](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[bos_nature_of_payment] [smallint] NULL,
	[bos_invoice_slab] [smallint] NULL,
	[bos_deductee_status] [smallint] NULL,
	[bos_residential_status] [smallint] NULL,
	[applicable_from] [datetime] NOT NULL,
	[applicable_to] [datetime] NULL,
	[is_surcharge_applicable] [bit] NULL,
	[surcharge_rate] [int] NULL,
 CONSTRAINT [PK_surcharge_master_id] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [surcharge_master$id_UNIQUE] UNIQUE NONCLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [tds].[tan]    Script Date: 6/9/2019 6:37:59 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[tan](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[tan] [varchar](10) NOT NULL,
	[address_id] [bigint] NULL,
 CONSTRAINT [PK_tan_id] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [tan$id_UNIQUE] UNIQUE NONCLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [tan$tan_UNIQUE] UNIQUE NONCLUSTERED 
(
	[tan] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [tds].[tds_master]    Script Date: 6/9/2019 6:37:59 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [tds].[tds_master](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[nature_of_payment_id] [bigint] NULL,
	[sub_nature_of_payment_id] [bigint] NULL,
	[rate] [int] NULL,
	[overall_transaction_limit] [bigint] NOT NULL,
	[per_transaction_limit] [bigint] NOT NULL,
	[applicable_from] [datetime] NOT NULL,
	[applicable_to] [datetime] NULL,
	[deductee_resident_status_id] [bigint] NULL,
	[deductee_status_id] [bigint] NULL,
	[sac_code] [varchar](50) NULL,
	[is_overall_transaction_limit] [bigint] NULL,
	[is_per_transaction_limit] [bigint] NULL,
 CONSTRAINT [PK_tds_master_id] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
 CONSTRAINT [tds_master$id_UNIQUE] UNIQUE NONCLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
ALTER TABLE [tds].[address] ADD  DEFAULT (NULL) FOR [name_building_village]
GO
ALTER TABLE [tds].[address] ADD  DEFAULT (NULL) FOR [road_street_postoffice]
GO
ALTER TABLE [tds].[address] ADD  DEFAULT (getdate()) FOR [applicable_from]
GO
ALTER TABLE [tds].[address] ADD  DEFAULT (NULL) FOR [applicable_to]
GO
ALTER TABLE [tds].[article_master] ADD  CONSTRAINT [DF__article_m__is_in__02084FDA]  DEFAULT ((0)) FOR [is_inclusion_or_exclusion]
GO
ALTER TABLE [tds].[article_master] ADD  CONSTRAINT [DF__article_m__appli__02FC7413]  DEFAULT (getdate()) FOR [applicable_from]
GO
ALTER TABLE [tds].[article_master] ADD  CONSTRAINT [DF__article_m__appli__03F0984C]  DEFAULT (NULL) FOR [applicable_to]
GO
ALTER TABLE [tds].[article_master_conditions] ADD  DEFAULT ((1)) FOR [is_inclusion]
GO
ALTER TABLE [tds].[article_master_conditions] ADD  DEFAULT ((0)) FOR [is_detailed_condition_applicable]
GO
ALTER TABLE [tds].[basis_of_cess_details] ADD  CONSTRAINT [DF__basis_of___natur__09A971A2]  DEFAULT (NULL) FOR [nature_of_payment_master_id]
GO
ALTER TABLE [tds].[basis_of_cess_details] ADD  CONSTRAINT [DF__basis_of___invoi__0A9D95DB]  DEFAULT (NULL) FOR [invoice_slab_from]
GO
ALTER TABLE [tds].[basis_of_cess_details] ADD  CONSTRAINT [DF__basis_of___invoi__0B91BA14]  DEFAULT (NULL) FOR [invoice_slab_to]
GO
ALTER TABLE [tds].[cess_master] ADD  DEFAULT (NULL) FOR [boc_nature_of_payment]
GO
ALTER TABLE [tds].[cess_master] ADD  DEFAULT (NULL) FOR [boc_invoice_slab]
GO
ALTER TABLE [tds].[cess_master] ADD  DEFAULT (NULL) FOR [boc_deductee_status]
GO
ALTER TABLE [tds].[cess_master] ADD  DEFAULT (NULL) FOR [boc_residential_status]
GO
ALTER TABLE [tds].[cess_master] ADD  DEFAULT (NULL) FOR [rate]
GO
ALTER TABLE [tds].[cess_master] ADD  DEFAULT (getdate()) FOR [applicable_from]
GO
ALTER TABLE [tds].[cess_master] ADD  DEFAULT (NULL) FOR [applicable_to]
GO
ALTER TABLE [tds].[cess_type_master] ADD  DEFAULT (getdate()) FOR [applicable_from]
GO
ALTER TABLE [tds].[cess_type_master] ADD  DEFAULT (NULL) FOR [applicable_to]
GO
ALTER TABLE [tds].[deductor_master] ADD  CONSTRAINT [DF__deductor___have___123EB7A3]  DEFAULT ((0)) FOR [have_more_than_one_branch]
GO
ALTER TABLE [tds].[deductor_master] ADD  CONSTRAINT [DF__deductor___due_d__1332DBDC]  DEFAULT (NULL) FOR [due_date_of_tax_payment]
GO
ALTER TABLE [tds].[deductor_master] ADD  CONSTRAINT [DF__deductor___email__14270015]  DEFAULT (NULL) FOR [email]
GO
ALTER TABLE [tds].[deductor_master] ADD  CONSTRAINT [DF__deductor___phone__151B244E]  DEFAULT (NULL) FOR [phone_number]
GO
ALTER TABLE [tds].[fine_rate_master] ADD  CONSTRAINT [DF__fine_rate__fine___160F4887]  DEFAULT ((0)) FOR [fine_per_day]
GO
ALTER TABLE [tds].[fine_rate_master] ADD  CONSTRAINT [DF__fine_rate__appli__17036CC0]  DEFAULT (getdate()) FOR [applicable_from]
GO
ALTER TABLE [tds].[fine_rate_master] ADD  CONSTRAINT [DF__fine_rate__appli__17F790F9]  DEFAULT (NULL) FOR [applicable_to]
GO
ALTER TABLE [tds].[nature_of_payment_master] ADD  CONSTRAINT [DF__nature_of__appli__18EBB532]  DEFAULT (getdate()) FOR [applicable_from]
GO
ALTER TABLE [tds].[nature_of_payment_master] ADD  CONSTRAINT [DF__nature_of__appli__19DFD96B]  DEFAULT (getdate()) FOR [applicable_to]
GO
ALTER TABLE [tds].[nature_of_payment_master] ADD  CONSTRAINT [DF__nature_of__is_su__1AD3FDA4]  DEFAULT ((0)) FOR [is_sub_nature_payment_applies]
GO
ALTER TABLE [tds].[surcharge_master] ADD  CONSTRAINT [DF__surcharge__bos_n__1BC821DD]  DEFAULT (NULL) FOR [bos_nature_of_payment]
GO
ALTER TABLE [tds].[surcharge_master] ADD  CONSTRAINT [DF__surcharge__bos_i__1CBC4616]  DEFAULT (NULL) FOR [bos_invoice_slab]
GO
ALTER TABLE [tds].[surcharge_master] ADD  CONSTRAINT [DF__surcharge__bos_d__1DB06A4F]  DEFAULT (NULL) FOR [bos_deductee_status]
GO
ALTER TABLE [tds].[surcharge_master] ADD  CONSTRAINT [DF__surcharge__bos_r__1EA48E88]  DEFAULT (NULL) FOR [bos_residential_status]
GO
ALTER TABLE [tds].[surcharge_master] ADD  CONSTRAINT [DF__surcharge__appli__208CD6FA]  DEFAULT (getdate()) FOR [applicable_from]
GO
ALTER TABLE [tds].[surcharge_master] ADD  CONSTRAINT [DF__surcharge__appli__2180FB33]  DEFAULT (NULL) FOR [applicable_to]
GO
ALTER TABLE [tds].[tds_master] ADD  CONSTRAINT [DF__tds_maste__natur__245D67DE]  DEFAULT (NULL) FOR [nature_of_payment_id]
GO
ALTER TABLE [tds].[tds_master] ADD  CONSTRAINT [DF__tds_maste__sub_n__25518C17]  DEFAULT (NULL) FOR [sub_nature_of_payment_id]
GO
ALTER TABLE [tds].[tds_master] ADD  CONSTRAINT [DF__tds_master__rate__2645B050]  DEFAULT (NULL) FOR [rate]
GO
ALTER TABLE [tds].[tds_master] ADD  CONSTRAINT [DF__tds_maste__overa__2739D489]  DEFAULT ((0)) FOR [overall_transaction_limit]
GO
ALTER TABLE [tds].[tds_master] ADD  CONSTRAINT [DF__tds_maste__per_t__282DF8C2]  DEFAULT ((0)) FOR [per_transaction_limit]
GO
ALTER TABLE [tds].[tds_master] ADD  CONSTRAINT [DF__tds_maste__appli__29221CFB]  DEFAULT (getdate()) FOR [applicable_from]
GO
ALTER TABLE [tds].[tds_master] ADD  CONSTRAINT [DF__tds_maste__appli__2A164134]  DEFAULT (NULL) FOR [applicable_to]
GO
ALTER TABLE [tds].[address]  WITH CHECK ADD  CONSTRAINT [address$fk_address_state] FOREIGN KEY([state_id])
REFERENCES [tds].[state] ([id])
GO
ALTER TABLE [tds].[address] CHECK CONSTRAINT [address$fk_address_state]
GO
ALTER TABLE [tds].[ao_master]  WITH CHECK ADD  CONSTRAINT [fk_ao_nature_of_payment_master] FOREIGN KEY([nature_of_payment_id])
REFERENCES [tds].[nature_of_payment_master] ([id])
GO
ALTER TABLE [tds].[ao_master] CHECK CONSTRAINT [fk_ao_nature_of_payment_master]
GO
ALTER TABLE [tds].[article_master_conditions]  WITH CHECK ADD  CONSTRAINT [article_master_conditions$fk_condition_article_master] FOREIGN KEY([article_master_id])
REFERENCES [tds].[article_master] ([id])
GO
ALTER TABLE [tds].[article_master_conditions] CHECK CONSTRAINT [article_master_conditions$fk_condition_article_master]
GO
ALTER TABLE [tds].[article_master_detailed_conditions]  WITH CHECK ADD  CONSTRAINT [article_master_detailed_conditions$fk_detailed_condition_condition] FOREIGN KEY([article_master_condition_id])
REFERENCES [tds].[article_master_conditions] ([id])
GO
ALTER TABLE [tds].[article_master_detailed_conditions] CHECK CONSTRAINT [article_master_detailed_conditions$fk_detailed_condition_condition]
GO
ALTER TABLE [tds].[basis_of_cess_details]  WITH CHECK ADD  CONSTRAINT [basis_of_cess_details$fk_boc_cess_id] FOREIGN KEY([cess_master_id])
REFERENCES [tds].[cess_master] ([id])
GO
ALTER TABLE [tds].[basis_of_cess_details] CHECK CONSTRAINT [basis_of_cess_details$fk_boc_cess_id]
GO
ALTER TABLE [tds].[basis_of_cess_details]  WITH CHECK ADD  CONSTRAINT [basis_of_cess_details$fk_boc_nature_of_payment] FOREIGN KEY([nature_of_payment_master_id])
REFERENCES [tds].[nature_of_payment_master] ([id])
GO
ALTER TABLE [tds].[basis_of_cess_details] CHECK CONSTRAINT [basis_of_cess_details$fk_boc_nature_of_payment]
GO
ALTER TABLE [tds].[basis_of_cess_details]  WITH CHECK ADD  CONSTRAINT [fk_basis_of_cess_residential_status] FOREIGN KEY([deductee_residential_status_id])
REFERENCES [tds].[residential_status] ([id])
GO
ALTER TABLE [tds].[basis_of_cess_details] CHECK CONSTRAINT [fk_basis_of_cess_residential_status]
GO
ALTER TABLE [tds].[basis_of_cess_details]  WITH CHECK ADD  CONSTRAINT [fk_basis_of_cess_status] FOREIGN KEY([deductee_status_id])
REFERENCES [tds].[status] ([id])
GO
ALTER TABLE [tds].[basis_of_cess_details] CHECK CONSTRAINT [fk_basis_of_cess_status]
GO
ALTER TABLE [tds].[basis_of_surcharge_details]  WITH CHECK ADD  CONSTRAINT [fk_basis_of_surcharge_residential_status] FOREIGN KEY([deductee_residential_status_id])
REFERENCES [tds].[status] ([id])
GO
ALTER TABLE [tds].[basis_of_surcharge_details] CHECK CONSTRAINT [fk_basis_of_surcharge_residential_status]
GO
ALTER TABLE [tds].[basis_of_surcharge_details]  WITH CHECK ADD  CONSTRAINT [fk_nature_of_payment_master] FOREIGN KEY([nature_of_payment_master_id])
REFERENCES [tds].[nature_of_payment_master] ([id])
GO
ALTER TABLE [tds].[basis_of_surcharge_details] CHECK CONSTRAINT [fk_nature_of_payment_master]
GO
ALTER TABLE [tds].[basis_of_surcharge_details]  WITH CHECK ADD  CONSTRAINT [fk_surcharge_master] FOREIGN KEY([surcharge_master_id])
REFERENCES [tds].[surcharge_master] ([id])
GO
ALTER TABLE [tds].[basis_of_surcharge_details] CHECK CONSTRAINT [fk_surcharge_master]
GO
ALTER TABLE [tds].[cess_master]  WITH CHECK ADD  CONSTRAINT [cess_master$fk_cess_type] FOREIGN KEY([cess_type_master_id])
REFERENCES [tds].[cess_type_master] ([id])
GO
ALTER TABLE [tds].[cess_master] CHECK CONSTRAINT [cess_master$fk_cess_type]
GO
ALTER TABLE [tds].[deductor_tan_details]  WITH CHECK ADD  CONSTRAINT [deductor_tan_details$fk_tan_deductor] FOREIGN KEY([deductor_id])
REFERENCES [tds].[deductor_master] ([id])
GO
ALTER TABLE [tds].[deductor_tan_details] CHECK CONSTRAINT [deductor_tan_details$fk_tan_deductor]
GO
ALTER TABLE [tds].[deductor_tan_details]  WITH CHECK ADD  CONSTRAINT [deductor_tan_details$fk_tan_details] FOREIGN KEY([tan_id])
REFERENCES [tds].[tan] ([id])
GO
ALTER TABLE [tds].[deductor_tan_details] CHECK CONSTRAINT [deductor_tan_details$fk_tan_details]
GO
ALTER TABLE [tds].[ldc_master]  WITH CHECK ADD  CONSTRAINT [fk_ldc_nature_of_payment_master] FOREIGN KEY([nature_of_payment_id])
REFERENCES [tds].[nature_of_payment_master] ([id])
GO
ALTER TABLE [tds].[ldc_master] CHECK CONSTRAINT [fk_ldc_nature_of_payment_master]
GO
ALTER TABLE [tds].[state]  WITH CHECK ADD  CONSTRAINT [state$fk_state_country] FOREIGN KEY([country_id])
REFERENCES [tds].[country] ([id])
GO
ALTER TABLE [tds].[state] CHECK CONSTRAINT [state$fk_state_country]
GO
ALTER TABLE [tds].[sub_nature_payment_master]  WITH CHECK ADD  CONSTRAINT [sub_nature_payment_master$fk_snp_parent] FOREIGN KEY([nature_payment_master_id])
REFERENCES [tds].[nature_of_payment_master] ([id])
GO
ALTER TABLE [tds].[sub_nature_payment_master] CHECK CONSTRAINT [sub_nature_payment_master$fk_snp_parent]
GO
ALTER TABLE [tds].[tan]  WITH CHECK ADD FOREIGN KEY([address_id])
REFERENCES [tds].[address] ([id])
GO
ALTER TABLE [tds].[tds_master]  WITH CHECK ADD  CONSTRAINT [fk_residential_status] FOREIGN KEY([deductee_resident_status_id])
REFERENCES [tds].[residential_status] ([id])
GO
ALTER TABLE [tds].[tds_master] CHECK CONSTRAINT [fk_residential_status]
GO
ALTER TABLE [tds].[tds_master]  WITH CHECK ADD  CONSTRAINT [fk_status] FOREIGN KEY([deductee_status_id])
REFERENCES [tds].[status] ([id])
GO
ALTER TABLE [tds].[tds_master] CHECK CONSTRAINT [fk_status]
GO
ALTER TABLE [tds].[tds_master]  WITH CHECK ADD  CONSTRAINT [tds_master$fk_tds_nature_of_payment] FOREIGN KEY([nature_of_payment_id])
REFERENCES [tds].[nature_of_payment_master] ([id])
GO
ALTER TABLE [tds].[tds_master] CHECK CONSTRAINT [tds_master$fk_tds_nature_of_payment]
GO
ALTER TABLE [tds].[tds_master]  WITH CHECK ADD  CONSTRAINT [tds_master$fk_tds_sub_nature_of_payment] FOREIGN KEY([sub_nature_of_payment_id])
REFERENCES [tds].[sub_nature_payment_master] ([id])
GO
ALTER TABLE [tds].[tds_master] CHECK CONSTRAINT [tds_master$fk_tds_sub_nature_of_payment]
GO
EXEC sys.sp_addextendedproperty @name=N'MS_SSMA_SOURCE', @value=N'tds.address' , @level0type=N'SCHEMA',@level0name=N'tds', @level1type=N'TABLE',@level1name=N'address'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_SSMA_SOURCE', @value=N'tds.article_master' , @level0type=N'SCHEMA',@level0name=N'tds', @level1type=N'TABLE',@level1name=N'article_master'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_SSMA_SOURCE', @value=N'tds.article_master_conditions' , @level0type=N'SCHEMA',@level0name=N'tds', @level1type=N'TABLE',@level1name=N'article_master_conditions'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_SSMA_SOURCE', @value=N'tds.basis_of_cess_details' , @level0type=N'SCHEMA',@level0name=N'tds', @level1type=N'TABLE',@level1name=N'basis_of_cess_details'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_SSMA_SOURCE', @value=N'tds.cess_master' , @level0type=N'SCHEMA',@level0name=N'tds', @level1type=N'TABLE',@level1name=N'cess_master'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_SSMA_SOURCE', @value=N'tds.cess_type_master' , @level0type=N'SCHEMA',@level0name=N'tds', @level1type=N'TABLE',@level1name=N'cess_type_master'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_SSMA_SOURCE', @value=N'tds.country' , @level0type=N'SCHEMA',@level0name=N'tds', @level1type=N'TABLE',@level1name=N'country'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_SSMA_SOURCE', @value=N'tds.deductor_master' , @level0type=N'SCHEMA',@level0name=N'tds', @level1type=N'TABLE',@level1name=N'deductor_master'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_SSMA_SOURCE', @value=N'tds.deductor_tan_details' , @level0type=N'SCHEMA',@level0name=N'tds', @level1type=N'TABLE',@level1name=N'deductor_tan_details'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_SSMA_SOURCE', @value=N'tds.deductor_type' , @level0type=N'SCHEMA',@level0name=N'tds', @level1type=N'TABLE',@level1name=N'deductor_type'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_SSMA_SOURCE', @value=N'tds.fine_rate_master' , @level0type=N'SCHEMA',@level0name=N'tds', @level1type=N'TABLE',@level1name=N'fine_rate_master'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_SSMA_SOURCE', @value=N'tds.mode_of_payment' , @level0type=N'SCHEMA',@level0name=N'tds', @level1type=N'TABLE',@level1name=N'mode_of_payment'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_SSMA_SOURCE', @value=N'tds.nature_of_payment_master' , @level0type=N'SCHEMA',@level0name=N'tds', @level1type=N'TABLE',@level1name=N'nature_of_payment_master'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_SSMA_SOURCE', @value=N'tds.pan' , @level0type=N'SCHEMA',@level0name=N'tds', @level1type=N'TABLE',@level1name=N'pan'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_SSMA_SOURCE', @value=N'tds.provision' , @level0type=N'SCHEMA',@level0name=N'tds', @level1type=N'TABLE',@level1name=N'provision'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_SSMA_SOURCE', @value=N'tds.residential_status' , @level0type=N'SCHEMA',@level0name=N'tds', @level1type=N'TABLE',@level1name=N'residential_status'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_SSMA_SOURCE', @value=N'tds.state' , @level0type=N'SCHEMA',@level0name=N'tds', @level1type=N'TABLE',@level1name=N'state'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_SSMA_SOURCE', @value=N'tds.status' , @level0type=N'SCHEMA',@level0name=N'tds', @level1type=N'TABLE',@level1name=N'status'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_SSMA_SOURCE', @value=N'tds.sub_nature_payment_master' , @level0type=N'SCHEMA',@level0name=N'tds', @level1type=N'TABLE',@level1name=N'sub_nature_payment_master'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_SSMA_SOURCE', @value=N'tds.surcharge_master' , @level0type=N'SCHEMA',@level0name=N'tds', @level1type=N'TABLE',@level1name=N'surcharge_master'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_SSMA_SOURCE', @value=N'tds.`tan`' , @level0type=N'SCHEMA',@level0name=N'tds', @level1type=N'TABLE',@level1name=N'tan'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_SSMA_SOURCE', @value=N'tds.tds_master' , @level0type=N'SCHEMA',@level0name=N'tds', @level1type=N'TABLE',@level1name=N'tds_master'
GO
