DELETE FROM [tds].[user_role] WHERE user_id IN ((select user_id from [tds].[user] where user_email = 'user1@tds.dvtfo.onmicrosoft.com'))

GO 

Insert into [tds].[user_role] (user_id,role_id)
values ((select user_id from [tds].[user] where user_email = 'user1@tds.dvtfo.onmicrosoft.com'),
(select role_id from [tds].[role] where role_name = 'Senior Deductor'));