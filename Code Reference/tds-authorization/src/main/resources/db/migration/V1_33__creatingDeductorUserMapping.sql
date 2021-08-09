CREATE TABLE [tds].[user_organization](
	[user_organization_id] [bigint] IDENTITY(1,1) NOT NULL,
	[user_id] [bigint] NULL,
	[organization_id] [bigint] NULL,
	 CONSTRAINT [PK_user_organization] PRIMARY KEY CLUSTERED 
(
	[user_organization_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
	
GO

INSERT INTO [tds].[address] (flat_door_block_no,name_building_village,road_street_postoffice,area_locality,town_city_district,state_id,pin_code,applicable_from,applicable_to) 
 VALUES ('1a2b3c4d5e','Nandana Residency','County Road','Alwal','Hyderabad',(SELECT id FROM [tds].[state] WHERE name ='Telangana'),'500010','',''),
 ('1a2b3c4d5e6f','Nandana Residency','County Road','Alwal','Hyderabad',(SELECT id FROM [tds].[state] WHERE name ='Telangana'),'500010','',''),
 ('1a2b3c4d5e6f7g','Nandana Residency','County Road','Alwal','Hyderabad',(SELECT id FROM [tds].[state] WHERE name ='Telangana'),'500010','','')
 

 GO

 INSERT INTO [tds].[deductor_master] (code,name,residential_status_id,have_more_than_one_branch,
								status_id,type_id,mode_of_payment_id,due_date_of_tax_payment,provision_id,
								email,address_id,phone_number,applicable_from,applicable_to,pan,pan_id,
								provision)
VALUES ('EY!#%1','Ashish',
		(SELECT id FROM [tds].[residential_status] WHERE status = 'RES'),
		1,(SELECT id FROM [tds].[status] WHERE status = 'Company'),
		(SELECT id FROM [tds].[deductor_type] WHERE type = 'Government'),
		(SELECT id FROM [tds].[mode_of_payment] WHERE mode = 'Cash'),
		'',
		'',
		'ashish_gaikwad@abc.com',
		(SELECT id FROM [tds].[address] WHERE flat_door_block_no = '1a2b3c4d5e'),
		'9876543210',
		'',
		'',
		'EKYPK9019R',
		'',
		''),
	
		('EY!#%2','Debhashish',
		(SELECT id FROM [tds].[residential_status] WHERE status = 'RES'),
		1,(SELECT id FROM [tds].[status] WHERE status = 'Company'),
		(SELECT id FROM [tds].[deductor_type] WHERE type = 'Government'),
		(SELECT id FROM [tds].[mode_of_payment] WHERE mode = 'Cash'),
		'',
		'',
		'debashish_gaikwad@abc.com',
		(SELECT id FROM [tds].[address] WHERE flat_door_block_no = '1a2b3c4d5e'),
		'9876543120',
		'',
		'',
		'EAZPP5572F',
		'',
		'')
		
		GO

		INSERT INTO [tds].[tan] (tan,address_id)
		VALUES ('CACC7854K',(SELECT id FROM [tds].[address] WHERE flat_door_block_no = '1a2b3c4d5e')),
			   ('MACK1343H',(SELECT id FROM [tds].[address] WHERE flat_door_block_no = '1a2b3c4d5e6f')),
			   ('MACK9087Q',(SELECT id FROM [tds].[address] WHERE flat_door_block_no = '1a2b3c4d5e6f7g'))

			   GO
			   
		INSERT INTO [tds].[deductor_tan_details] (deductor_id,tan_id)
		VALUES ((SELECT id FROM [tds].[deductor_master] WHERE code = 'EY!#%2'),(SELECT id FROM [tds].[tan] WHERE tan = 'CACC7854K')),
		((SELECT id FROM [tds].[deductor_master] WHERE code = 'EY!#%2'),(SELECT id FROM [tds].[tan] WHERE tan = 'MACK1343H')),
		((SELECT id FROM [tds].[deductor_master] WHERE code = 'EY!#%1'),(SELECT id FROM [tds].[tan] WHERE tan = 'MACK9087Q'))
		
		GO
		