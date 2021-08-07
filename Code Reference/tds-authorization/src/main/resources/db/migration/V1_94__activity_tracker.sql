--tds.activity;
create table tds.activity(
id bigint IDENTITY(1,1) NOT NULL,
activity_name varchar(255) NOT NULL,
activity_group varchar(150) NOT NULL,
created_by varchar(255) DEFAULT 'EY Admin' ,
created_date datetime DEFAULT CURRENT_TIMESTAMP,
modified_by varchar(255) DEFAULT 'EY Admin',
modified_date  datetime DEFAULT CURRENT_TIMESTAMP,
active smallint DEFAULT 1,
primary key(id)
);

--tds.activity_tracker;
create table tds.activity_tracker(
id bigint IDENTITY(1,1) NOT NULL,
is_duedate_for_activity BIT,
activity_id bigint,
month bigint NOT NULL ,
year bigint NOT NULL,
activity_type bigint,
activity_status varchar(255),
duedate_for_activity datetime,
created_by varchar(255) DEFAULT 'EY Admin' ,
created_date datetime DEFAULT CURRENT_TIMESTAMP,
modified_by varchar(255) DEFAULT 'EY Admin',
modified_date  datetime DEFAULT CURRENT_TIMESTAMP,
active smallint DEFAULT 1,
primary key(id),
FOREIGN KEY(activity_id) REFERENCES tds.activity(id)
);

