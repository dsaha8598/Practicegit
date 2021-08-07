
ALTER TABLE tds.user_organization ADD tan_id bigint, for_all_tans bit default ((0));

GO

UPDATE tds.user_organization SET for_all_tans = 0 WHERE for_all_tans is NULL; 

GO