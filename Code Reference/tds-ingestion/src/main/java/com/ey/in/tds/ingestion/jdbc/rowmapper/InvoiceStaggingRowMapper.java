package com.ey.in.tds.ingestion.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ey.in.tds.common.domain.transactions.jdbc.dto.InvoiceStagging;

public class InvoiceStaggingRowMapper implements RowMapper<InvoiceStagging> {

	@Override
	public InvoiceStagging mapRow(ResultSet rs, int rowNum) throws SQLException {
		InvoiceStagging dto = new InvoiceStagging();
		dto.setId(rs.getInt("id"));
		dto.setSourceIdentifier(rs.getString("source_identifier"));
		dto.setSourceFilename(rs.getString("source_filename"));
		dto.setDeductorCode(rs.getString("deductor_code"));
		dto.setDeductorName(rs.getString("deductor_name"));
		dto.setDeductorPan(rs.getString("deductor_pan"));
		dto.setDeductorTan(rs.getString("deductor_tan"));
		dto.setDeductorGstin(rs.getString("deductor_gstin"));
		dto.setDeducteeCode(rs.getString("deductee_code"));
		dto.setDeducteeName(rs.getString("deductee_name"));
		dto.setDeducteePan(rs.getString("deductee_pan"));
		dto.setAssessmentYear(rs.getInt("assessment_year"));
		dto.setChallanMonth(rs.getInt("challan_month"));
		dto.setDeducteeTin(rs.getString("deductee_tin"));
		dto.setDeducteeGstin(rs.getString("deductee_gstin"));
		dto.setVendorInvoiceNumber(rs.getString("vendor_invoice_number"));
		dto.setDocumentDate(rs.getDate("document_date"));
		dto.setDocumentNumber(rs.getString("document_number"));
		dto.setPostingDate(rs.getDate("posting_date"));
		dto.setPaymentDate(rs.getDate("payment_date"));
		dto.setTdsDeductionDate(rs.getDate("tds_deduction_date"));
		dto.setDocumentType(rs.getString("document_type"));
		dto.setSupplyType(rs.getString("supply_type"));
		dto.setMigoNumber(rs.getString("migo_number"));
		dto.setMiroNumber(rs.getString("miro_number"));
		dto.setErpDocumentType(rs.getString("erp_document_type"));
		dto.setLineItemNumber(rs.getInt("line_item_number"));
		dto.setHsnOrSac(rs.getString("hsn_or_sac"));
		dto.setHsnOrSacDesc(rs.getString("hsn_or_sac_desc"));
		dto.setInvoiceDesc(rs.getString("invoice_desc"));
		dto.setGlAccountCode(rs.getString("gl_account_code"));
		dto.setGlAccountName(rs.getString("gl_account_name"));
		dto.setPoNumber(rs.getString("po_number"));
		dto.setPoDate(rs.getDate("po_date"));
		dto.setPoDesc(rs.getString("po_desc"));
		dto.setTaxablevalue(rs.getBigDecimal("taxable_value"));
		dto.setIgstRate(rs.getBigDecimal("igst_rate"));
		dto.setIgstAmount(rs.getBigDecimal("igst_amount"));
		dto.setCgstRate(rs.getBigDecimal("cgst_rate"));
		dto.setCgstAmount(rs.getBigDecimal("cgst_amount"));
		dto.setSgstRate(rs.getBigDecimal("sgst_rate"));
		dto.setSgstAmount(rs.getBigDecimal("sgst_amount"));
		dto.setCessRate(rs.getBigDecimal("cess_rate"));
		dto.setCessAmount(rs.getBigDecimal("cess_amount"));
		dto.setPos(rs.getString("pos"));
		dto.setTdsTaxCodeErp(rs.getString("tds_tax_code_erp"));
		dto.setTdsSection(rs.getString("tds_section"));
		dto.setTdsRate(rs.getBigDecimal("tds_rate"));
		dto.setTdsAmount(rs.getBigDecimal("tds_amount"));
		dto.setLinkedAdvanceIndicator(rs.getString("linked_advance_indicator"));
		dto.setLinkedProvisionIndicator(rs.getString("linked_provision_indicator"));
		dto.setProvisionAdjustmentFlag(rs.getInt("provision_adjustment_flag") == 1 ? "Y" : "N");
		dto.setAdvanceAdjustmentFlag(rs.getInt("advance_adjustment_flag") == 1 ? "Y" : "N");
		dto.setChallanPaid(rs.getInt("challan_paid") == 1 ? "Y" : "N");
		dto.setChallanProcessingDate(rs.getDate("challan_processing_date"));
		dto.setGrossUpIndicator(rs.getInt("gross_up_indicator") == 1 ? "Y" : "N");
		dto.setOriginalDocumentNumber(rs.getString("original_document_number"));
		dto.setOriginalDocumentDate(rs.getDate("original_document_date"));
		dto.setRefKey3(rs.getString("ref_key3"));
		dto.setBusinessPlace(rs.getString("business_place"));
		dto.setBusinessArea(rs.getString("business_area"));
		dto.setPlant(rs.getString("plant"));
		dto.setProfitCenter(rs.getString("profit_center"));
		dto.setAssignmentNumber(rs.getString("assignment_number"));
		dto.setTdsBaseValue(rs.getBigDecimal("tds_base_value"));
		dto.setPoItemNo(rs.getString("po_item_no"));
		dto.setTypeOfTransaction(rs.getString("type_of_transaction"));
		dto.setUserName(rs.getString("user_name"));
		dto.setAmountForeignCurrency(rs.getBigDecimal("amount_foreign_currency"));
		dto.setExchangeRate(rs.getBigDecimal("exchange_rate"));
		dto.setCurrency(rs.getString("currency"));
		dto.setItemCode(rs.getString("item_code"));
		dto.setInvoiceValue(rs.getBigDecimal("invoice_value"));
		dto.setSaaNumber(rs.getInt("saa_number"));
		dto.setNrIndicator(rs.getString("nr_indicator"));
		dto.setTdsRemittancedate(rs.getDate("tds_remittance_date"));
		dto.setDebitCreditIndicator(rs.getString("debit_credit_indicator"));
		dto.setUserDefinedField1(rs.getString("user_defined_field1"));
		dto.setUserDefinedField2(rs.getString("user_defined_field2"));
		dto.setUserDefinedField3(rs.getString("user_defined_field3"));
		dto.setUserDefinedField4(rs.getString("user_defined_field4"));
		dto.setUserDefinedField5(rs.getString("user_defined_field5"));
		dto.setUserDefinedField6(rs.getString("user_defined_field6"));
		dto.setUserDefinedField7(rs.getString("user_defined_field7"));
		dto.setUserDefinedField8(rs.getString("user_defined_field8"));
		dto.setUserDefinedField9(rs.getString("user_defined_field9"));
		dto.setUserDefinedField10(rs.getString("user_defined_field10"));
		dto.setActive(rs.getInt("active") == 1 ? true : false);
		dto.setCreatedBy(rs.getString("created_by"));
		dto.setCreatedDate(rs.getDate("created_date"));

		return dto;
	}

}
