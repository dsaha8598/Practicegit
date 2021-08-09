create table tds.tds_month_tracker(
id bigint IDENTITY(1,1) NOT NULL,
year bigint NOT NULL,
month bigint NOT NULL,
due_date_for_challan_payment datetime Null,
month_closure_for_processing datetime NULL,
due_date_for_filing datetime NULL,
created_by text NULL,
created_date datetime NULL,
modified_by text NULL,
modified_date  datetime NULL,
active smallint DEFAULT 1,
primary key(id)
); 
