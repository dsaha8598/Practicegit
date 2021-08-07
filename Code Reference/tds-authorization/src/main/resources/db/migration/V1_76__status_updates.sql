

update tds.residential_status set status='RES' , active =1, created_by='admin', 
modified_by='admin', created_date=CURRENT_TIMESTAMP, modified_date=CURRENT_TIMESTAMP 
where status='Resident';

update tds.residential_status set status='NR', active =1, created_by='admin', 
modified_by='admin', created_date=CURRENT_TIMESTAMP, modified_date=CURRENT_TIMESTAMP 
where status='Non Resident';

update tds.residential_status set status='RNOR', active =1, created_by='admin', 
modified_by='admin', created_date=CURRENT_TIMESTAMP, modified_date=CURRENT_TIMESTAMP 
where status='Resident or Non Resident';
