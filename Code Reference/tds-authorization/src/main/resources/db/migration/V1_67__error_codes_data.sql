-- Series 100 error codes
insert into tds.error_code(code,description) VALUES ('ERR100','Invalid TAN');
insert into tds.error_code(code,description) VALUES ('ERR101','Blank Deductee PAN');
insert into tds.error_code(code,description) VALUES ('ERR102','Invalid Type of Vendor');
insert into tds.error_code(code,description) VALUES ('ERR103','Invalid Deductor PAN');
insert into tds.error_code(code,description) VALUES ('ERR104','Invalid Deductee PAN');
insert into tds.error_code(code,description) VALUES ('ERR105','Invalid Deductee Name');
insert into tds.error_code(code,description) VALUES ('ERR106','Invalid Taxable Value');
insert into tds.error_code(code,description) VALUES ('ERR107','Taxable value can’t be negative for the document types other than reversals and credit note');
insert into tds.error_code(code,description) VALUES ('ERR108','Blank Line Item Number');
insert into tds.error_code(code,description) VALUES ('ERR109','Invalid Line Item Number');
insert into tds.error_code(code,description) VALUES ('ERR110','Same line item number is populated across one Document');
insert into tds.error_code(code,description) VALUES ('ERR111','Blank Document Number');
insert into tds.error_code(code,description) VALUES ('ERR112','Blank Document Type');
insert into tds.error_code(code,description) VALUES ('ERR113','Invalid Document Type');
insert into tds.error_code(code,description) VALUES ('ERR114','Invalid Document Number');
insert into tds.error_code(code,description) VALUES ('ERR115','Invalid Document Date');
insert into tds.error_code(code,description) VALUES ('ERR116','Blank Document Date');
insert into tds.error_code(code,description) VALUES ('ERR117','More than one Document Date provided for one Document Number');
insert into tds.error_code(code,description) VALUES ('ERR118','Deductee PAN provided for Non Resident Vendor');
insert into tds.error_code(code,description) VALUES ('ERR119','Incase Non-resident Deductee Indicator is Y, Deductee TIN should be provided');
insert into tds.error_code(code,description) VALUES ('ERR120','Blank HSN/SAC Code');
insert into tds.error_code(code,description) VALUES ('ERR121','Invaid HSN/SAC Code');
insert into tds.error_code(code,description) VALUES ('ERR122','Invalid Service Description');
insert into tds.error_code(code,description) VALUES ('ERR123','Blank Service Description');
insert into tds.error_code(code,description) VALUES ('ERR124','Non-Resident Deductee Indicator is Y but TDS section applies for resident vendor');
insert into tds.error_code(code,description) VALUES ('ERR125','Invalid Original Document Number or Original Document Number is missing');
insert into tds.error_code(code,description) VALUES ('ERR126','Invalid Original Document Date or Original Document Date is missing');
insert into tds.error_code(code,description) VALUES ('ERR127','Original Document Number was not reported in earlier tax periods');
insert into tds.error_code(code,description) VALUES ('ERR128','Document Date cannot be prior to Original Document Date');
insert into tds.error_code(code,description) VALUES ('ERR129','Original Document was never reported');
insert into tds.error_code(code,description) VALUES ('ERR130','Posting date cannot be prior to Document Date');

-- Series 100 information codes
insert into tds.error_code(code,description) VALUES ('INF100','Invalid Deductee Address');
insert into tds.error_code(code,description) VALUES ('INF101','Invalid Deductor GSTIN/UIN');
insert into tds.error_code(code,description) VALUES ('INF102','Invalid Deductee GSTIN/UIN');
insert into tds.error_code(code,description) VALUES ('INF103','Blank Deductor GSTIN/UINN');
insert into tds.error_code(code,description) VALUES ('INF104','Invalid creditable Flag');
insert into tds.error_code(code,description) VALUES ('INF105','Invalid TDS Sections');
insert into tds.error_code(code,description) VALUES ('INF106','Invalid TDS Rates');
insert into tds.error_code(code,description) VALUES ('INF107','TDS Amount does not link to the TDS rates provided');
insert into tds.error_code(code,description) VALUES ('INF108','Invalid Posting date');
insert into tds.error_code(code,description) VALUES ('INF109','Invalid TDS section incase of non resident deductee');


-- Series 200 error codes
insert into tds.error_code(code,description) VALUES ('ERR200','Deductor TAN not available as part of Client Master');
insert into tds.error_code(code,description) VALUES ('ERR201','Deductor PAN not available as part of Client Master');
insert into tds.error_code(code,description) VALUES ('ERR202','PAN not available in the Deductee master');
insert into tds.error_code(code,description) VALUES ('ERR203','Vendor status shows Invalid/deleted as per government records');
insert into tds.error_code(code,description) VALUES ('ERR204','TDS Rates provided are not as per the rates recorded in the Vendor Master');
insert into tds.error_code(code,description) VALUES ('ERR205','Same PAN is recorded against multiple Deductees');
insert into tds.error_code(code,description) VALUES ('ERR206','Rates are not as per the rates determined by the tool');
insert into tds.error_code(code,description) VALUES ('ERR207','TDS section is not part of Vendor Master');
insert into tds.error_code(code,description) VALUES ('ERR208','Duplicate line items recorded in one invoice');
insert into tds.error_code(code,description) VALUES ('ERR209','Duplicate invoices');

-- Series 200 information codes
insert into tds.error_code(code,description) VALUES ('INF200','Deductee Name contradicts from the name recorded in the Deductee master');
insert into tds.error_code(code,description) VALUES ('INF201','Vendor name is not as per government records');
insert into tds.error_code(code,description) VALUES ('INF202','20% TDS Rate to be applied for no PAN case');
insert into tds.error_code(code,description) VALUES ('INF203','Certificate available in LDC Master but exemption rates are not applied');
insert into tds.error_code(code,description) VALUES ('INF204','Deductee address contradicts from the address recorded in the Deductee master');
