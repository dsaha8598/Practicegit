CREATE TABLE [tds].[error_code](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[code] [varchar](32) NOT NULL,
    [description] [varchar](2048) NOT NULL,
    created_by text NOT NULL default 'EY Admin',
    created_date datetime NOT NULL default current_timestamp,
    modified_by text NOT NULL default 'EY Admin',
    modified_date  datetime NOT NULL default current_timestamp,
    active smallint NOT NULL DEFAULT 1,
    primary key(id)
);

