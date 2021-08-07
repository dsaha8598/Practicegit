create table tds.currency(
id bigint IDENTITY(1,1) NOT NULL,
currency_name varchar(255) NOT NULL,
country_name varchar(255) NOT NULL,
currency_value decimal(30,2) NOT NULL,
symbol varchar(255) NOT NULL,
created_by varchar(255) DEFAULT 'EY ADMIN' ,
created_date datetime DEFAULT CURRENT_TIMESTAMP,
modified_by varchar(255) DEFAULT 'EY ADMIN',
modified_date  datetime DEFAULT CURRENT_TIMESTAMP,
active smallint DEFAULT 1,
primary key(id)
);
