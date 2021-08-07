INSERT INTO [tds].[state] ([name],country_id)
   select 'ANDAMAN AND NICOBAR ISLANDS',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'ANDAMAN AND NICOBAR ISLANDS');

INSERT INTO [tds].[state] ([name],country_id)
   select 'ANDHRA PRADESH',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'ANDHRA PRADESH');

INSERT INTO [tds].[state] ([name],country_id)
   select 'ARUNACHAL PRADESH',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'ARUNACHAL PRADESH');

INSERT INTO [tds].[state] ([name],country_id)
   select 'ASSAM',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'ASSAM');

INSERT INTO [tds].[state] ([name],country_id)
   select 'BIHAR',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'BIHAR');

INSERT INTO [tds].[state] ([name],country_id)
   select 'CHANDIGARH',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'CHANDIGARH');

INSERT INTO [tds].[state] ([name],country_id)
   select 'DADRA AND NAGAR HAVELI',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'DADRA AND NAGAR HAVELI');

INSERT INTO [tds].[state] ([name],country_id)
   select 'DAMAN AND DIU',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'DAMAN AND DIU');

INSERT INTO [tds].[state] ([name],country_id)
   select 'DELHI',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'DELHI');

INSERT INTO [tds].[state] ([name],country_id)
   select 'GOA',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'GOA');

INSERT INTO [tds].[state] ([name],country_id)
   select 'GUJARAT',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'GUJARAT');

INSERT INTO [tds].[state] ([name],country_id)
   select 'HARYANA',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'HARYANA');

INSERT INTO [tds].[state] ([name],country_id)
   select 'HIMACHAL PRADESH',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'HIMACHAL PRADESH');

INSERT INTO [tds].[state] ([name],country_id)
   select 'JAMMU and KASHMIR',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'JAMMU and KASHMIR');

INSERT INTO [tds].[state] ([name],country_id)
   select 'KARNATAKA',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'KARNATAKA');

INSERT INTO [tds].[state] ([name],country_id)
   select 'KERALA',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'KERALA');

INSERT INTO [tds].[state] ([name],country_id)
   select 'LAKSHWADEEP',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'LAKSHWADEEP');

INSERT INTO [tds].[state] ([name],country_id)
   select 'MADHYA PRADESH',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'MADHYA PRADESH');

INSERT INTO [tds].[state] ([name],country_id)
   select 'MAHARASHTRA',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'MAHARASHTRA');

INSERT INTO [tds].[state] ([name],country_id)
   select 'MANIPUR',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'MANIPUR');

INSERT INTO [tds].[state] ([name],country_id)
   select 'MEGHALAYA',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'MEGHALAYA');

INSERT INTO [tds].[state] ([name],country_id)
   select 'MIZORAM',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'MIZORAM');

INSERT INTO [tds].[state] ([name],country_id)
   select 'NAGALAND',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'NAGALAND');

INSERT INTO [tds].[state] ([name],country_id)
   select 'ODISHA',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'ODISHA');

INSERT INTO [tds].[state] ([name],country_id)
   select 'PONDICHERRY',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'PONDICHERRY');

INSERT INTO [tds].[state] ([name],country_id)
   select 'PUNJAB',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'PUNJAB');

INSERT INTO [tds].[state] ([name],country_id)
   select 'RAJASTHAN',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'RAJASTHAN');

INSERT INTO [tds].[state] ([name],country_id)
   select 'SIKKIM',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'SIKKIM');

INSERT INTO [tds].[state] ([name],country_id)
   select 'TAMILNADU',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'TAMILNADU');

INSERT INTO [tds].[state] ([name],country_id)
   select 'TRIPURA',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'TRIPURA');

INSERT INTO [tds].[state] ([name],country_id)
   select 'UTTAR PRADESH',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'UTTAR PRADESH');

INSERT INTO [tds].[state] ([name],country_id)
   select 'WEST BENGAL',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'WEST BENGAL');

INSERT INTO [tds].[state] ([name],country_id)
   select 'CHHATISHGARH',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'CHHATISHGARH');

INSERT INTO [tds].[state] ([name],country_id)
   select 'UTTARAKHAND',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'UTTARAKHAND');

INSERT INTO [tds].[state] ([name],country_id)
   select 'JHARKHAND',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'JHARKHAND');

INSERT INTO [tds].[state] ([name],country_id)
   select 'TELANGANA',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'TELANGANA');

INSERT INTO [tds].[state] ([name],country_id)
   select 'LADAKH',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'LADAKH');

INSERT INTO [tds].[state] ([name],country_id)
   select 'OTHERS',(select id from tds.country where [name] = 'India')
   where not exists (SELECT 1 FROM [tds].[state] WHERE [name] = 'OTHERS');
