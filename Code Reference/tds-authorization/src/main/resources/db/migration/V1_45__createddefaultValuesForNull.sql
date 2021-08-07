UPDATE tds.surcharge_master SET bos_nature_of_payment = 0 WHERE bos_nature_of_payment = NULL; 

GO

UPDATE tds.surcharge_master SET bos_invoice_slab = 0 WHERE bos_invoice_slab = NULL;

GO

UPDATE tds.surcharge_master SET bos_residential_status = 0 WHERE bos_residential_status = NULL;

GO