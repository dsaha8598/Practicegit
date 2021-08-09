
ALTER TABLE tds.tds_master DROP constraint if exists DF__tds_master__rate__2645B050; 
ALTER TABLE tds.tds_master DROP constraint if exists df_tds_master_rate; 
ALTER TABLE tds.tds_master ALTER COLUMN rate DECIMAL(30,2);
ALTER TABLE tds.tds_master ADD CONSTRAINT df_tds_master_rate DEFAULT 0.0 FOR rate;

ALTER TABLE tds.article_master DROP constraint if exists df_tds_article_rate; 
ALTER TABLE tds.article_master ALTER COLUMN rate DECIMAL(30,2);
ALTER TABLE tds.article_master ADD CONSTRAINT df_tds_article_rate DEFAULT 0.0 FOR rate;

ALTER TABLE tds.basis_of_cess_details DROP constraint if exists df_tds_basis_of_cess_details; 
ALTER TABLE tds.basis_of_cess_details ALTER COLUMN rate DECIMAL(30,2);
ALTER TABLE tds.basis_of_cess_details ADD CONSTRAINT df_tds_basis_of_cess_details DEFAULT 0.0 FOR rate;

ALTER TABLE tds.basis_of_surcharge_details DROP constraint if exists df_tds_basis_of_surcharge_details; 
ALTER TABLE tds.basis_of_surcharge_details ALTER COLUMN rate DECIMAL(30,2);
ALTER TABLE tds.basis_of_surcharge_details ADD CONSTRAINT df_tds_basis_of_surcharge_details DEFAULT 0.0 FOR rate;

ALTER TABLE tds.cess_master DROP constraint if exists DF__cess_maste__rate__25518C17;
ALTER TABLE tds.cess_master DROP constraint if exists DF__cess_maste__rate__123EB7A3;
ALTER TABLE tds.cess_master DROP constraint if exists df_tds_cess_master;
ALTER TABLE tds.cess_master ALTER COLUMN rate DECIMAL(30,2);
ALTER TABLE tds.cess_master ADD CONSTRAINT df_tds_cess_master DEFAULT 0.0 FOR rate;

ALTER TABLE tds.fine_rate_master DROP constraint if exists df_tds_fine_rate_master; 
ALTER TABLE tds.fine_rate_master ALTER COLUMN rate DECIMAL(30,2);
ALTER TABLE tds.fine_rate_master ADD CONSTRAINT df_tds_fine_rate_master DEFAULT 0.0 FOR rate;

ALTER TABLE tds.surcharge_master DROP constraint if exists df_tds_surcharge_master; 
ALTER TABLE tds.surcharge_master ALTER COLUMN surcharge_rate DECIMAL(30,2);
ALTER TABLE tds.surcharge_master ADD CONSTRAINT df_tds_surcharge_master DEFAULT 0.0 FOR surcharge_rate;





