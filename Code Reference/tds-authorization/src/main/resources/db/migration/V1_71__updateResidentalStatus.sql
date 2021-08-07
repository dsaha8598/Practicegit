

update tds.residential_status set status='Resident' , active =1, created_by='admin', modified_by='admin', created_date=CURRENT_TIMESTAMP, modified_date=CURRENT_TIMESTAMP where status='RES';

update tds.residential_status set status='Non Resident', active =1, created_by='admin', modified_by='admin', created_date=CURRENT_TIMESTAMP, modified_date=CURRENT_TIMESTAMP where status='NR';

update tds.residential_status set status='Resident or Non Resident', active =1, created_by='admin', modified_by='admin', created_date=CURRENT_TIMESTAMP, modified_date=CURRENT_TIMESTAMP where status='RNOR';




