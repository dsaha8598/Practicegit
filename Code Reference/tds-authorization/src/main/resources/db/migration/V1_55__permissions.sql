ALTER TABLE tds.permission ADD CONSTRAINT permission_created_by DEFAULT 'EY Admin' FOR created_by;

ALTER TABLE tds.permission ADD CONSTRAINT permission_modified_by DEFAULT 'EY Admin' FOR modified_by;

ALTER TABLE tds.permission ADD CONSTRAINT permission_created_date DEFAULT GETDATE() FOR created_date;

ALTER TABLE tds.permission ADD CONSTRAINT permission_modified_date DEFAULT GETDATE() FOR modified_date;

delete from tds.role_permission;
delete from tds.permission;


insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'HAS_ACCESS', 'Access', 'Access', '', '' where not exists (select 1 from tds.permission where permission_name=  'HAS_ACCESS');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'MASTERS', 'Masters', 'Masters', '', '' where not exists (select 1 from tds.permission where permission_name=  'MASTERS');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'NOP_MASTER', 'Nature of payment', 'Masters', 'Nop-Masters', '' where not exists (select 1 from tds.permission where permission_name=  'NOP_MASTER');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'NOP_CREATE', 'Create', 'Masters', 'Nop-Masters', 'Nature-of-payment-create' where not exists (select 1 from tds.permission where permission_name=  'NOP_CREATE');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'NOP_LIST', 'List', 'Masters', 'Nop-Masters', 'Nature-of-payment-list' where not exists (select 1 from tds.permission where permission_name=  'NOP_LIST');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'NOP_UPDATE', 'Update', 'Masters', 'Nop-Masters', 'Nature-of-payment-update' where not exists (select 1 from tds.permission where permission_name=  'NOP_UPDATE');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'CESS_MASTER', 'Cess', 'Masters', 'Cess-Masters', '' where not exists (select 1 from tds.permission where permission_name=  'CESS_MASTER');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'CESS_CREATE', 'Create', 'Masters', 'Cess-Masters', 'Cess-create' where not exists (select 1 from tds.permission where permission_name=  'CESS_CREATE');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'CESS_LIST', 'List', 'Masters', 'Cess-Masters', 'Cess-list' where not exists (select 1 from tds.permission where permission_name=  'CESS_LIST');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'CESS_UPDATE', 'Update', 'Masters', 'Cess-Masters', 'Cess-update' where not exists (select 1 from tds.permission where permission_name=  'CESS_UPDATE');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'CESS_TYPE_MASTER', 'Cess type', 'Masters', 'Cess-Type-Masters', '' where not exists (select 1 from tds.permission where permission_name=  'CESS_TYPE_MASTER');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'CESS_TYPE_CREATE', 'Create', 'Masters', 'Cess-Type-Masters', 'Cess-type-create' where not exists (select 1 from tds.permission where permission_name=  'CESS_TYPE_CREATE');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'CESS_TYPE_LIST', 'List', 'Masters', 'Cess-Type-Masters', 'Cess-type-list' where not exists (select 1 from tds.permission where permission_name=  'CESS_TYPE_LIST');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'CESS_TYPE_UPDATE', 'Update', 'Masters', 'Cess-Type-Masters', 'Cess-type-update' where not exists (select 1 from tds.permission where permission_name=  'CESS_TYPE_UPDATE');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'SURCHARGE_MASTER', 'Surcharge', 'Masters', 'Surcharge-Masters', '' where not exists (select 1 from tds.permission where permission_name=  'SURCHARGE_MASTER');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'SURCHARGE_CREATE', 'Create', 'Masters', 'Surcharge-Masters', 'Surcharge-create' where not exists (select 1 from tds.permission where permission_name=  'SURCHARGE_CREATE');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'SURCHARGE_LIST', 'List', 'Masters', 'Surcharge-Masters', 'Surcharge-list' where not exists (select 1 from tds.permission where permission_name=  'SURCHARGE_LIST');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'SURCHARGE_UPDATE', 'Update', 'Masters', 'Surcharge-Masters', 'Surcharge-update' where not exists (select 1 from tds.permission where permission_name=  'SURCHARGE_UPDATE');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TDS_RATE_MASTER', 'Tds rate', 'Masters', 'Tds-Rate-Masters', '' where not exists (select 1 from tds.permission where permission_name=  'TDS_RATE_MASTER');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TDS_RATE_CREATE', 'Create', 'Masters', 'Tds-Rate-Masters', 'Tds-rate-create' where not exists (select 1 from tds.permission where permission_name=  'TDS_RATE_CREATE');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TDS_RATE_LIST', 'List', 'Masters', 'Tds-Rate-Masters', 'Tds-rate-list' where not exists (select 1 from tds.permission where permission_name=  'TDS_RATE_LIST');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TDS_RATE_UPDATE', 'Update', 'Masters', 'Tds-Rate-Masters', 'Tds-rate-update' where not exists (select 1 from tds.permission where permission_name=  'TDS_RATE_UPDATE');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'IPFM_MASTER', 'Intrest and Penalty / Fine rate', 'Masters', 'Ipfm-Masters', '' where not exists (select 1 from tds.permission where permission_name=  'IPFM_MASTER');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'IPFM_CREATE', 'Create', 'Masters', 'Ipfm-Masters', 'Ipfm-create' where not exists (select 1 from tds.permission where permission_name=  'IPFM_CREATE');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'IPFM_LIST', 'List', 'Masters', 'Ipfm-Masters', 'Ipfm-list' where not exists (select 1 from tds.permission where permission_name=  'IPFM_LIST');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'IPFM_UPDATE', 'Update', 'Masters', 'Ipfm-Masters', 'Ipfm-update' where not exists (select 1 from tds.permission where permission_name=  'IPFM_UPDATE');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'DEDUCTOR-MASTER', 'Deductor', 'Masters', 'Deductor-Masters', '' where not exists (select 1 from tds.permission where permission_name=  'DEDUCTOR-MASTER');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'DEDUCTOR_CREATE', 'Create', 'Masters', 'Deductor-Masters', 'Deductor-create' where not exists (select 1 from tds.permission where permission_name=  'DEDUCTOR_CREATE');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'DEDUCTOR_LIST', 'List', 'Masters', 'Deductor-Masters', 'Deductor-list' where not exists (select 1 from tds.permission where permission_name=  'DEDUCTOR_LIST');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'DEDUCTOR_UPDATE', 'Update', 'Masters', 'Deductor-Masters', 'Deductor-update' where not exists (select 1 from tds.permission where permission_name=  'DEDUCTOR_UPDATE');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'ARTICLE_MASTER', 'Article', 'Masters', 'Article-Masters', '' where not exists (select 1 from tds.permission where permission_name=  'ARTICLE_MASTER');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'ARTICLE_CREATE', 'Create', 'Masters', 'Article-Masters', 'Article-create' where not exists (select 1 from tds.permission where permission_name=  'ARTICLE_CREATE');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'ARTICLE_LIST', 'List', 'Masters', 'Article-Masters', 'Article-list' where not exists (select 1 from tds.permission where permission_name=  'ARTICLE_LIST');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'ARTICLE_UPDATE', 'Update', 'Masters', 'Article-Masters', 'Article-update' where not exists (select 1 from tds.permission where permission_name=  'ARTICLE_UPDATE');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'DEDUCTEE_MASTER', 'Deductee', 'Masters', 'Deductee-Masters', '' where not exists (select 1 from tds.permission where permission_name=  'DEDUCTEE_MASTER');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'DEDUCTEE_CREATE', 'Create', 'Masters', 'Deductee-Masters', 'Deductee-create' where not exists (select 1 from tds.permission where permission_name=  'DEDUCTEE_CREATE');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'DEDUCTEE_LIST', 'List', 'Masters', 'Deductee-Masters', 'Deductee-list' where not exists (select 1 from tds.permission where permission_name=  'DEDUCTEE_LIST');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'DEDUCTEE_UPDATE', 'Update', 'Masters', 'Deductee-Masters', 'Deductee-update' where not exists (select 1 from tds.permission where permission_name=  'DEDUCTEE_UPDATE');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'DEDUCTEE_IMPORT', 'Import', 'Masters', 'Deductee-Masters', 'Deductee-import' where not exists (select 1 from tds.permission where permission_name=  'DEDUCTEE_IMPORT');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'DEDUCTEE_PAN_REPORT_EXPORT', 'Periodic report export', 'Masters', 'Deductee-Masters', 'Deductee-pan-report-export' where not exists (select 1 from tds.permission where permission_name=  'DEDUCTEE_PAN_REPORT_EXPORT');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'AO_MASTER', 'Ao', 'Masters', 'Ao-Masters', '' where not exists (select 1 from tds.permission where permission_name=  'AO_MASTER');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'AO_CREATE', 'Create', 'Masters', 'Ao-Masters', 'Ao-create' where not exists (select 1 from tds.permission where permission_name=  'AO_CREATE');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'AO_LIST', 'List', 'Masters', 'Ao-Masters', 'Ao-list' where not exists (select 1 from tds.permission where permission_name=  'AO_LIST');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'AO_UPDATE', 'Update', 'Masters', 'Ao-Masters', 'Ao-update' where not exists (select 1 from tds.permission where permission_name=  'AO_UPDATE');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'AO_IMPORT', 'Import', 'Masters', 'Ao-Masters', 'Ao-import' where not exists (select 1 from tds.permission where permission_name=  'AO_IMPORT');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'LDC_MASTER', 'Ldc', 'Masters', 'Ldc-Masters', '' where not exists (select 1 from tds.permission where permission_name=  'LDC_MASTER');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'LDC_CREATE', 'Create', 'Masters', 'Ldc-Masters', 'Ldc-create' where not exists (select 1 from tds.permission where permission_name=  'LDC_CREATE');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'LDC_LIST', 'List', 'Masters', 'Ldc-Masters', 'Ldc-list' where not exists (select 1 from tds.permission where permission_name=  'LDC_LIST');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'LDC_UPDATE', 'Update', 'Masters', 'Ldc-Masters', 'Ldc-update' where not exists (select 1 from tds.permission where permission_name=  'LDC_UPDATE');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'LDC_IMPORT', 'Import', 'Masters', 'Ldc-Masters', 'Ldc-import' where not exists (select 1 from tds.permission where permission_name=  'LDC_IMPORT');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'LDC_PERIODIC_REPORT_EXPORT', 'Periodic report export', 'Masters', 'Ldc-Masters', 'Ldc-periodic-report-export' where not exists (select 1 from tds.permission where permission_name=  'LDC_PERIODIC_REPORT_EXPORT');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TRANSACTIONS', 'Transactions', 'Transactions', '', '' where not exists (select 1 from tds.permission where permission_name=  'TRANSACTIONS');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TRANSACTIONS_INVOICE_EXCEL', 'Invoice excel', 'Transactions', 'Transactions-invoice-excel', '' where not exists (select 1 from tds.permission where permission_name=  'TRANSACTIONS_INVOICE_EXCEL');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TRANSACTIONS_INVOICE_EXCEL_RESIDENT_LIST', 'Resident list', 'Transactions', 'Transactions-invoice-excel', 'Transactions-invoice-excel-resident-list' where not exists (select 1 from tds.permission where permission_name=  'TRANSACTIONS_INVOICE_EXCEL_RESIDENT_LIST');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TRANSACTIONS_INVOICE_EXCEL_NONRESIDENT_LIST', 'Nonresident list', 'Transactions', 'Transactions-invoice-excel', 'Transactions-invoice-excel-nonresident-list' where not exists (select 1 from tds.permission where permission_name=  'TRANSACTIONS_INVOICE_EXCEL_NONRESIDENT_LIST');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TRANSACTIONS_INVOICE_EXCEL_IMPORT', 'Import', 'Transactions', 'Transactions-invoice-excel', 'Transactions-invoice-excel-import' where not exists (select 1 from tds.permission where permission_name=  'TRANSACTIONS_INVOICE_EXCEL_IMPORT');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TRANSACTIONS_INVOICE_EXCEL_STATUS_LIST', 'Imported file view', 'Transactions', 'Transactions-invoice-excel', 'Transactions-invoice-excel-status-list' where not exists (select 1 from tds.permission where permission_name=  'TRANSACTIONS_INVOICE_EXCEL_STATUS_LIST');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TRANSACTIONS_INVOICE_PDF', 'Invoice pdf ', 'Transactions', 'Transactions-invoice-pdf', '' where not exists (select 1 from tds.permission where permission_name=  'TRANSACTIONS_INVOICE_PDF');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TRANSACTIONS_INVOICE_PDF_RESIDENT_LIST', 'Resident list', 'Transactions', 'Transactions-invoice-pdf', 'Transactions-invoice-pdf-resident-list' where not exists (select 1 from tds.permission where permission_name=  'TRANSACTIONS_INVOICE_PDF_RESIDENT_LIST');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TRANSACTIONS_INVOICE_PDF_NONRESIDENT_LIST', 'Nonresident list', 'Transactions', 'Transactions-invoice-pdf', 'Transactions-invoice-pdf-nonresident-list' where not exists (select 1 from tds.permission where permission_name=  'TRANSACTIONS_INVOICE_PDF_NONRESIDENT_LIST');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TRANSACTIONS_INVOICE_PDF_IMPORT', 'Import', 'Transactions', 'Transactions-invoice-pdf', 'Transactions-invoice-pdf-import' where not exists (select 1 from tds.permission where permission_name=  'TRANSACTIONS_INVOICE_PDF_IMPORT');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TRANSACTIONS_INVOICE_PDF_STATUS_LIST', 'Imported file view', 'Transactions', 'Transactions-invoice-pdf', 'Transactions-invoice-pdf-status-list' where not exists (select 1 from tds.permission where permission_name=  'TRANSACTIONS_INVOICE_PDF_STATUS_LIST');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TRANSACTIONS_INVOICE_SAP', 'Invoice sap', 'Transactions', 'Transactions-invoice-sap', '' where not exists (select 1 from tds.permission where permission_name=  'TRANSACTIONS_INVOICE_SAP');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TRANSACTIONS_INVOICE_SAP_RESIDENT_LIST', 'Resident list', 'Transactions', 'Transactions-invoice-sap', 'Transactions-invoice-sap-resident-list' where not exists (select 1 from tds.permission where permission_name=  'TRANSACTIONS_INVOICE_SAP_RESIDENT_LIST');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TRANSACTIONS_INVOICE_SAP_NONRESIDENT_LIST', 'Nonresident list', 'Transactions', 'Transactions-invoice-sap', 'Transactions-invoice-sap-nonresident-list' where not exists (select 1 from tds.permission where permission_name=  'TRANSACTIONS_INVOICE_SAP_NONRESIDENT_LIST');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TRANSACTIONS_INVOICE_SAP_STATUS_LIST', 'Imported file view', 'Transactions', 'Transactions-invoice-sap', 'Transactions-invoice-sap-status-list' where not exists (select 1 from tds.permission where permission_name=  'TRANSACTIONS_INVOICE_SAP_STATUS_LIST');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TRANSACTIONS_INVOICE_MISMATCHES', 'Mismatches', 'Transactions', 'Transactions-invoice-mismatches', '' where not exists (select 1 from tds.permission where permission_name=  'TRANSACTIONS_INVOICE_MISMATCHES');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TRANSACTIONS_INVOICE_MISMATCHES_SUMMARY_LIST', 'Mismatch Summary View', 'Transactions', 'Transactions-invoice-mismatches', 'Transactions-invoices-mismatches-summary-list' where not exists (select 1 from tds.permission where permission_name=  'TRANSACTIONS_INVOICE_MISMATCHES_SUMMARY_LIST');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TRANSACTIONS_INVOICE_MISMATCHES_MOIDFY_ACTION', 'On screen Remediation', 'Transactions', 'Transactions-invoice-mismatches', 'Transactions-invoices-mismatches-modify-action' where not exists (select 1 from tds.permission where permission_name=  'TRANSACTIONS_INVOICE_MISMATCHES_MOIDFY_ACTION');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TRANSACTIONS_INVOICE_MISMATCHES_RECTIFICATION_EXPORT', 'Off Screen Remediation Export', 'Transactions', 'Transactions-invoice-mismatches', 'Transactions-invoices-mismatches-rectification-export' where not exists (select 1 from tds.permission where permission_name=  'TRANSACTIONS_INVOICE_MISMATCHES_RECTIFICATION_EXPORT');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TRANSACTIONS_INVOICE_MISMATCHES_RECTIFICATION_IMPORT', 'Off Screen Remediation Import', 'Transactions', 'Transactions-invoice-mismatches', 'Transactions-invoices-mismatches-rectification-import' where not exists (select 1 from tds.permission where permission_name=  'TRANSACTIONS_INVOICE_MISMATCHES_RECTIFICATION_IMPORT');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TRANSACTIONS_INVOICE_MISMATCHES_DETAILED_LIST', 'Remediation File Views', 'Transactions', 'Transactions-invoice-mismatches', 'Transactions-invoices-mismatches-detailed-list' where not exists (select 1 from tds.permission where permission_name=  'TRANSACTIONS_INVOICE_MISMATCHES_DETAILED_LIST');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TRANSACTIONS_GL', 'Gl', 'Transactions', 'Transactions-gl', '' where not exists (select 1 from tds.permission where permission_name=  'TRANSACTIONS_GL');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TRANSACTIONS_GL_IMPORT', 'Gl import', 'Transactions', 'Transactions-gl', 'Transactions-gl-import' where not exists (select 1 from tds.permission where permission_name=  'TRANSACTIONS_GL_IMPORT');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TRANSACTIONS_GL_EXPORT', 'Gl export', 'Transactions', 'Transactions-gl', 'Transactions-gl-export' where not exists (select 1 from tds.permission where permission_name=  'TRANSACTIONS_GL_EXPORT');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TRANSACTIONS_GL_STATUS_LIST', 'Gl status list', 'Transactions', 'Transactions-gl', 'Transactions-gl-status-list' where not exists (select 1 from tds.permission where permission_name=  'TRANSACTIONS_GL_STATUS_LIST');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TRANSACTIONS_GL_SUMMARY_MATRIX', 'Gl summary matrix', 'Transactions', 'Transactions-gl', 'Transactions-gl-summary-matrix' where not exists (select 1 from tds.permission where permission_name=  'TRANSACTIONS_GL_SUMMARY_MATRIX');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TRANSACTIONS_PROVISIONS', 'Provisions', 'Transactions', 'Transactions-provisions', '' where not exists (select 1 from tds.permission where permission_name=  'TRANSACTIONS_PROVISIONS');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TRANSACTIONS_PROVISIONS_IMPORT', 'Provisions import', 'Transactions', 'Transactions-provisions', 'Transactions-provisions-import' where not exists (select 1 from tds.permission where permission_name=  'TRANSACTIONS_PROVISIONS_IMPORT');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TRANSACTIONS_PROVISIONS_EXPORT', 'Provisions export', 'Transactions', 'Transactions-provisions', 'Transactions-provisions-export' where not exists (select 1 from tds.permission where permission_name=  'TRANSACTIONS_PROVISIONS_EXPORT');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TRANSACTIONS_PROVISIONS_STATUS_LIST', 'Provisions status list', 'Transactions', 'Transactions-provisions', 'Transactions-provisions-status-list' where not exists (select 1 from tds.permission where permission_name=  'TRANSACTIONS_PROVISIONS_STATUS_LIST');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TRANSACTIONS_PROVISIONS_SUMMARY_MATRIX', 'Provisions summary matrix', 'Transactions', 'Transactions-provisions', 'Transactions-provisions-summary-matrix' where not exists (select 1 from tds.permission where permission_name=  'TRANSACTIONS_PROVISIONS_SUMMARY_MATRIX');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TRANSACTIONS_ADVANCES', 'Advances', 'Transactions', 'Transactions-advances', '' where not exists (select 1 from tds.permission where permission_name=  'TRANSACTIONS_ADVANCES');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TRANSACTIONS_ADVANCES_IMPORT', 'Advances import', 'Transactions', 'Transactions-advances', 'Transactions-advances-import' where not exists (select 1 from tds.permission where permission_name=  'TRANSACTIONS_ADVANCES_IMPORT');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TRANSACTIONS_ADVANCES_EXPORT', 'Advances export', 'Transactions', 'Transactions-advances', 'Transactions-advances-export' where not exists (select 1 from tds.permission where permission_name=  'TRANSACTIONS_ADVANCES_EXPORT');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TRANSACTIONS_ADVANCES_STATUS_LIST', 'Advances status list', 'Transactions', 'Transactions-advances', 'Transactions-advances-status-list' where not exists (select 1 from tds.permission where permission_name=  'TRANSACTIONS_ADVANCES_STATUS_LIST');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'TRANSACTIONS_ADVANCES_SUMMARY_MATRIX', 'Advances summary list', 'Transactions', 'Transactions-advances', 'Transactions-advances-summary-list' where not exists (select 1 from tds.permission where permission_name=  'TRANSACTIONS_ADVANCES_SUMMARY_MATRIX');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'CHALLANS', 'Challans', 'Challans', '', '' where not exists (select 1 from tds.permission where permission_name=  'CHALLANS');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'CHALLANS_TDS_SUMMARY', 'Challans tds summary', 'Challans', 'Challans-tds-summary', '' where not exists (select 1 from tds.permission where permission_name=  'CHALLANS_TDS_SUMMARY');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'CHALLANS_GENERATE', 'Challans generate', 'Challans', 'Challans-generate', '' where not exists (select 1 from tds.permission where permission_name=  'CHALLANS_GENERATE');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'CHALLANS_APPROVE', 'Challans approve ', 'Challans', 'Challans-approve', '' where not exists (select 1 from tds.permission where permission_name=  'CHALLANS_APPROVE');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'CHALLANS_UPLOAD_RECEIPT', 'Challans upload receipt', 'Challans', 'Challans-upload-receipt', '' where not exists (select 1 from tds.permission where permission_name=  'CHALLANS_UPLOAD_RECEIPT');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'CHALLANS_LIST', 'Challans list', 'Challans', 'Challans-list', '' where not exists (select 1 from tds.permission where permission_name=  'CHALLANS_LIST');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'CHALLANS_PENDING_APPROVALS', 'Challans pending approvals', 'Challans', 'Challans-pending-approvals', '' where not exists (select 1 from tds.permission where permission_name=  'CHALLANS_PENDING_APPROVALS');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'CHALLANS_RECEIPT_STATUS_LIST', 'Challans receipt status list', 'Challans', 'Challans-receipt-status-list', '' where not exists (select 1 from tds.permission where permission_name=  'CHALLANS_RECEIPT_STATUS_LIST');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'SETTINGS', 'Settings', 'Settings', '', '' where not exists (select 1 from tds.permission where permission_name=  'SETTINGS');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'SETTINGS_ORGANIZATION', 'organization view', 'Settings', 'Settings-organization', '' where not exists (select 1 from tds.permission where permission_name=  'SETTINGS_ORGANIZATION');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'SETTINGS_PROFILE_VIEW', 'profile view', 'Settings', 'Settings-profile-view', '' where not exists (select 1 from tds.permission where permission_name=  'SETTINGS_PROFILE_VIEW');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'SETTINGS_NOTIFICATION_VIEW', 'notification view', 'Settings', 'Settings-notification-view', '' where not exists (select 1 from tds.permission where permission_name=  'SETTINGS_NOTIFICATION_VIEW');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'SETTINGS_NOTIFICATION_CREATE', 'notification create', 'Settings', 'Settings-notification-create', '' where not exists (select 1 from tds.permission where permission_name=  'SETTINGS_NOTIFICATION_CREATE');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'SETTINGS_NOTIFICATION_UPDATE', 'notification update', 'Settings', 'Settings-notification-update', '' where not exists (select 1 from tds.permission where permission_name=  'SETTINGS_NOTIFICATION_UPDATE');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'SETTINGS_NOTIFICATION_DELETE', 'notification delete', 'Settings', 'Settings-notification-delete', '' where not exists (select 1 from tds.permission where permission_name=  'SETTINGS_NOTIFICATION_DELETE');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'VALIDATION_AO', 'Ao Validation', 'Validation', 'Validation-ao', '' where not exists (select 1 from tds.permission where permission_name=  'VALIDATION_AO');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'VALIDATION_AO_REPORT_EXPORT', 'export', 'Validation', 'Validation-ao', 'Validation-ao-report-export' where not exists (select 1 from tds.permission where permission_name=  'VALIDATION_AO_REPORT_EXPORT');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'VALIDATION_AO_IMPORT', 'Validation Ao import', 'Validation', 'Validation-ao', 'Validation-ao-import' where not exists (select 1 from tds.permission where permission_name=  'VALIDATION_AO_IMPORT');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'VALIDATION_AO_STATUS', 'Validation Ao status', 'Validation', 'Validation-ao', 'Validation-ao-status' where not exists (select 1 from tds.permission where permission_name=  'VALIDATION_AO_STATUS');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'VALIDATION_LDC', 'Validation Ldc', 'Validation', 'Validation -ldc', 'Validation-Ldc' where not exists (select 1 from tds.permission where permission_name=  'VALIDATION_LDC');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'VALIDATION_LDC_REPORT_EXPORT', 'Validation Ldc report export', 'Validation', 'Validation -ldc', 'Validation-ldc-report-export' where not exists (select 1 from tds.permission where permission_name=  'VALIDATION_LDC_REPORT_EXPORT');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'VALIDATION_LDC_IMPORT', 'Validation Ldc import', 'Validation', 'Validation -ldc', 'Validation-ldc-import' where not exists (select 1 from tds.permission where permission_name=  'VALIDATION_LDC_IMPORT');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'VALIDATION_LDC_STATUS', 'Validation Ldc status', 'Validation', 'Validation -ldc', 'Validation-ldc-status' where not exists (select 1 from tds.permission where permission_name=  'VALIDATION_LDC_STATUS');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'VALIDATION', 'Validation ', 'Validation', '', '' where not exists (select 1 from tds.permission where permission_name=  'VALIDATION');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'VALIDATION_PAN', 'PAN Validation', 'Validation', 'Validation-pan', 'Validation-pan' where not exists (select 1 from tds.permission where permission_name=  'VALIDATION_PAN');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'VALIDATION_PAN_REPORT_EXPORT', 'export', 'Validation', 'Validation-pan', 'Validation-pan-report-export' where not exists (select 1 from tds.permission where permission_name=  'VALIDATION_PAN_REPORT_EXPORT');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'VALIDATION_PAN_IMPORT', 'import', 'Validation', 'Validation-pan', 'Validation-pan-import' where not exists (select 1 from tds.permission where permission_name=  'VALIDATION_PAN_IMPORT');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'VALIDATION_PAN_STATUS', 'status', 'Validation', 'Validation-pan', 'Validation-pan-status' where not exists (select 1 from tds.permission where permission_name=  'VALIDATION_PAN_STATUS');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'FILING', 'Filing', 'Filing', '', '' where not exists (select 1 from tds.permission where permission_name=  'FILING');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'FILING_RETURNS', 'Returns', 'Filing', 'Filing-Returns', '' where not exists (select 1 from tds.permission where permission_name=  'FILING_RETURNS');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'FILING_JUSTIFICATION', 'Justification', 'Filing', 'Filing-Justification', '' where not exists (select 1 from tds.permission where permission_name=  'FILING_JUSTIFICATION');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'FILING_FORM16', 'Form 16', 'Filing', 'Filing-Form16', '' where not exists (select 1 from tds.permission where permission_name=  'FILING_FORM16');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'FILING_REVISED', 'Revised Returns', 'Filing', 'Filing-Revised', '' where not exists (select 1 from tds.permission where permission_name=  'FILING_REVISED');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'FILING_DELAYED', 'Delayed Filing', 'Filing', 'Filing-delayed', '' where not exists (select 1 from tds.permission where permission_name=  'FILING_DELAYED');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'DASHBOARD', 'Dashboard', 'Dashboard', '', '' where not exists (select 1 from tds.permission where permission_name=  'DASHBOARD');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'DASHBOARD_LDC', 'Dashboard', 'Dashboard', 'Dashboard-ldc', '' where not exists (select 1 from tds.permission where permission_name=  'DASHBOARD_LDC');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'DASHBOARD_SECTION', 'Dashboard', 'Dashboard', 'Dashboard-section', '' where not exists (select 1 from tds.permission where permission_name=  'DASHBOARD_SECTION');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'DASHBOARD_DEDUCTEE', 'Dashboard', 'Dashboard', 'Dashboard-deductee', '' where not exists (select 1 from tds.permission where permission_name=  'DASHBOARD_DEDUCTEE');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'REPORTS', 'Reports', 'Reports', '', '' where not exists (select 1 from tds.permission where permission_name=  'REPORTS');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'REPORTS', 'Reports', 'Reports', 'Reports-view', '' where not exists (select 1 from tds.permission where permission_name=  'REPORTS');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'REPORTS', 'Reports', 'Reports', 'Reports-export', '' where not exists (select 1 from tds.permission where permission_name=  'REPORTS');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'KEYWORD_MASTER', 'Keywords', 'Masters', 'Master-Keyword', '' where not exists (select 1 from tds.permission where permission_name=  'KEYWORD_MASTER');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'KEYWORD_MASTER_CREATE', 'Keywords', 'Masters', 'Master-Keyword', 'Master-keyword-create' where not exists (select 1 from tds.permission where permission_name=  'KEYWORD_MASTER_CREATE');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'KEYWORD_MASTER_UPDATE', 'Keywords', 'Masters', 'Master-Keyword', 'Master-keyword-view' where not exists (select 1 from tds.permission where permission_name=  'KEYWORD_MASTER_UPDATE');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'KEYWORD_MASTER_VIEW', 'Keywords', 'Masters', 'Master-Keyword', 'Master-keyword-update' where not exists (select 1 from tds.permission where permission_name=  'KEYWORD_MASTER_VIEW');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'ONBOARDING_MASTER', 'Onboarding', 'Masters', 'Master-onboarding', '' where not exists (select 1 from tds.permission where permission_name=  'ONBOARDING_MASTER');
insert into tds.permission (permission_name, permission_display_name, tab_name, grouping_name, label_name) select 'ROLE_MASTER', 'Roles and Permissions', 'Masters', 'Master-role-permission', '' 'ROLE_MASTER'  where not exists (select 1 from tds.permission where permission_name=  'ROLE_MASTER');



INSERT INTO [tds].role_permission (role_id,permission_id) 

VALUES ((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'HAS_ACCESS')),
		
		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'MASTERS')),
		
		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'NOP_MASTER')),
		
		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'NOP_CREATE')),
		
		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'NOP_LIST')),
		
		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'NOP_UPDATE')),
		
		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'CESS_MASTER')),
		
		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'CESS_CREATE')),
		
		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'CESS_LIST')),
		
		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'CESS_UPDATE')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'CESS_TYPE_MASTER')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'CESS_TYPE_CREATE')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'CESS_TYPE_LIST')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'CESS_TYPE_UPDATE')),
		
		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'SURCHARGE_MASTER')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'SURCHARGE_CREATE')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'SURCHARGE_LIST')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'SURCHARGE_UPDATE')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TDS_RATE_MASTER')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TDS_RATE_CREATE')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TDS_RATE_LIST')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'TDS_RATE_UPDATE')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'IPFM_MASTER')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'IPFM_CREATE')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'IPFM_LIST')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'IPFM_UPDATE')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'DEDUCTOR_MASTER')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'DEDUCTOR_CREATE')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'DEDUCTOR_LIST')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'DEDUCTOR_UPDATE')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'ARTICLE_MASTER')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'ARTICLE_CREATE')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'ARTICLE_LIST')),

		((SELECT role_id FROM tds.role WHERE role_name= 'Super Admin'),
		(SELECT permission_id FROM tds.permission WHERE permission_name= 'ARTICLE_UPDATE'));
		
		