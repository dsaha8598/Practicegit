CREATE TABLE [tds].[utility_version](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[name] [varchar](128) NOT NULL,
	[version] [varchar](128) NOT NULL,
	[utility_effective_from] [date] NOT NULL,
	[applicable_from] [datetime] NOT NULL,
	[applicable_to] [datetime] NULL,
	CONSTRAINT [PK_utility_version_id] PRIMARY KEY CLUSTERED ( [id] ASC)
	WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY],
    CONSTRAINT [utility_version$id_UNIQUE] UNIQUE NONCLUSTERED ( [id] ASC)
    WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY])
    ON [PRIMARY]
GO

INSERT INTO [tds].[utility_version] ([name], [version], [utility_effective_from], [applicable_from])
    VALUES ('FVU','6.3', '2019-06-28', CURRENT_TIMESTAMP);

INSERT INTO [tds].[utility_version] ([name], [version], [utility_effective_from], [applicable_from])
    VALUES ('FVU','6.4', '2019-10-16', CURRENT_TIMESTAMP);

