INSERT into [tds].permission (permission_name,active) values 
('SETTINGS',1),
('HOME',1),
('REPORTS',1),
('FILING',1),
('DASHBOARDS',1)

GO

INSERT INTO [tds].role_permission (role_id,permission_id) 

VALUES ((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'HAS_ACCESS')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'MASTERS')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'DEDUCTEE_MASTER')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'DEDUCTEE_CREATE')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'DEDUCTEE_LIST')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'DEDUCTEE_UPDATE')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'DEDUCTEE_IMPORT')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'DEDUCTEE_PAN_REPORT_EXPORT')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'AO_MASTER')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'AO_CREATE')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'AO_LIST')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'AO_UPDATE')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'AO_IMPORT')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'AO_PERIODIC_REPORT_EXPORT')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'LDC_MASTER')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'LDC_CREATE')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'LDC_LIST')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'LDC_IMPORT')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'LDC_PERIODIC_REPORT_EXPORT')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TRANSACTIONS')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TRANSACTIONS_INVOICE_EXCEL')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TRANSACTIONS_INVOICE_EXCEL_RESIDENT_LIST')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TRANSACTIONS_INVOICE_EXCEL_NONRESIDENT_LIST')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TRANSACTIONS_INVOICE_EXCEL_IMPORT')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TRANSACTIONS_INVOICE_EXCEL_STATUS_LIST')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TRANSACTIONS_INVOICE_PDF')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TRANSACTIONS_INVOICE_PDF_RESIDENT_LIST')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TRANSACTIONS_INVOICE_PDF_NONRESIDENT_LIST')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TRANSACTIONS_INVOICE_PDF_IMPORT')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TRANSACTIONS_INVOICE_PDF_STATUS_LIST')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TRANSACTIONS_INVOICE_SAP')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TRANSACTIONS_INVOICE_SAP_RESIDENT_LIST')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TRANSACTIONS_INVOICE_SAP_NONRESIDENT_LIST')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TRANSACTIONS_INVOICE_SAP_STATUS_LIST')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TRANSACTIONS_INVOICE_MISMATCHES')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TRANSACTIONS_INVOICE_MISMATCHES_SUMMARY_LIST')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TRANSACTIONS_INVOICE_MISMATCHES_MOIDFY_ACTION')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TRANSACTIONS_INVOICE_MISMATCHES_RECTIFICATION_EXPORT')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TRANSACTIONS_INVOICE_MISMATCHES_RECTIFICATION_IMPORT')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TRANSACTIONS_INVOICE_MISMATCHES_DETAILED_LIST')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TRANSACTIONS_GL')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TRANSACTIONS_GL_IMPORT')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TRANSACTIONS_GL_EXPORT')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TRANSACTIONS_GL_STATUS_LIST')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TRANSACTIONS_GL_SUMMARY_MATRIX')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TRANSACTIONS_PROVISIONS')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TRANSACTIONS_PROVISIONS_IMPORT')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TRANSACTIONS_PROVISIONS_EXPORT')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TRANSACTIONS_PROVISIONS_STATUS_LIST')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TRANSACTIONS_PROVISIONS_SUMMARY_MATRIX')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TRANSACTIONS_ADVANCES')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TRANSACTIONS_ADVANCES_IMPORT')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TRANSACTIONS_ADVANCES_EXPORT')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TRANSACTIONS_ADVANCES_STATUS_LIST')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TRANSACTIONS_ADVANCES_SUMMARY_MATRIX')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'CHALLANS')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'CHALLANS_TDS_SUMMARY')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'CHALLANS_GENERATE')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'CHALLANS_APPROVE')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'CHALLANS_UPLOAD_RECEIPT')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'CHALLANS_LIST')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'CHALLANS_PENDING_APPROVALS')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'CHALLANS_RECEIPT_STATUS_LIST')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'SETTINGS')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'HOME')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'REPORTS')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'FILING')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'DASHBOARDS')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'SETTINGS_ORGANIZATION')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'SETTINGS_PROFILE_VIEW')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'SETTINGS_NOTIFICATION_VIEW')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'SETTINGS_NOTIFICATION_CREATE')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'SETTINGS_NOTIFICATION_UPDATE')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'SETTINGS_NOTIFICATION_DELETE')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'VALIDATION_AO')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'VALIDATION_AO_REPORT_EXPORT')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'VALIDATION_AO_IMPORT')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'VALIDATION_AO_STATUS')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'VALIDATION_LDC')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'VALIDATION_LDC_REPORT_EXPORT')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'VALIDATION_LDC_IMPORT')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'VALIDATION_LDC_STATUS')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'VALIDATION')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'VALIDATION_PAN')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'VALIDATION_PAN_REPORT_EXPORT')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'VALIDATION_PAN_IMPORT')),
		((SELECT role_id FROM tds.role WHERE role_name= 'Senior Deductor'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'VALIDATION_PAN_STATUS'))


		

		