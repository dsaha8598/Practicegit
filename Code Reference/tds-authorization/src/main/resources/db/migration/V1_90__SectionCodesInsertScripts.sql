create table tds.filing_state_code (
id bigint IDENTITY(1,1) NOT NULL,
state_name varchar(255) NOT NULL,
state_code varchar(4) NOT NULL,
created_by varchar(255) DEFAULT 'EY Admin' ,
created_date datetime DEFAULT CURRENT_TIMESTAMP,
modified_by varchar(255) DEFAULT 'EY Admin',
modified_date  datetime DEFAULT CURRENT_TIMESTAMP,
active smallint DEFAULT 1,
primary key(id)
);

create table tds.filing_section_code(
id bigint IDENTITY(1,1) NOT NULL,
section_name varchar(10) NOT NULL,
section_code varchar(10) NOT NULL,
created_by varchar(255) DEFAULT 'EY Admin' ,
created_date datetime DEFAULT CURRENT_TIMESTAMP,
modified_by varchar(255) DEFAULT 'EY Admin',
modified_date  datetime DEFAULT CURRENT_TIMESTAMP,
active smallint DEFAULT 1,
primary key(id)
);

create table tds.filing_ministry_code(
id bigint IDENTITY(1,1) NOT NULL,
ministry_name varchar(255) NOT NULL,
ministry_code varchar(3),
created_by varchar(255) DEFAULT 'EY Admin' ,
created_date datetime DEFAULT CURRENT_TIMESTAMP,
modified_by varchar(255) DEFAULT 'EY Admin',
modified_date  datetime DEFAULT CURRENT_TIMESTAMP,
active smallint DEFAULT 1,
primary key(id)
);

create table tds.filing_deductor_collector(
id bigint IDENTITY(1,1) NOT NULL,
category_description varchar(255) NOT NULL,
category_value varchar(64),
created_by varchar(255) DEFAULT 'EY Admin' ,
created_date datetime DEFAULT CURRENT_TIMESTAMP,
modified_by varchar(255) DEFAULT 'EY Admin',
modified_date  datetime DEFAULT CURRENT_TIMESTAMP,
active smallint DEFAULT 1,
primary key(id)
);

create table tds.filing_minor_head_code(
id bigint IDENTITY(1,1) NOT NULL,
particulars varchar(255) NOT NULL,
code char(4),
created_by varchar(255) DEFAULT 'EY Admin' ,
created_date datetime DEFAULT CURRENT_TIMESTAMP,
modified_by varchar(255) DEFAULT 'EY Admin',
modified_date  datetime DEFAULT CURRENT_TIMESTAMP,
active smallint DEFAULT 1,
primary key(id)
);

