UPDATE tds.role_permission  SET active ='1' WHERE active IS NULL;
ALTER TABLE tds.role_permission ADD CONSTRAINT DEF_RP_ACTIVE DEFAULT 1 FOR ACTIVE;
