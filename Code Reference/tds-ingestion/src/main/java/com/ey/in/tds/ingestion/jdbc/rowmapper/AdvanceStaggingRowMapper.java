package com.ey.in.tds.ingestion.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ey.in.tds.common.domain.transactions.jdbc.dto.AdvanceStagging;

public class AdvanceStaggingRowMapper implements RowMapper<AdvanceStagging> {

	@Override
	public AdvanceStagging mapRow(ResultSet rs, int rowNum) throws SQLException {
		AdvanceStagging dto = new AdvanceStagging();
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
		dto.setDeducteeTin(rs.getString("deductee_tin"));
		dto.setDeducteeGstin(rs.getString("deductee_gstin"));
		dto.setErpDocumentNumber(rs.getString("erp_document_number"));
		dto.setDocumentDate(rs.getDate("document_date"));
		dto.setPostingDate(rs.getDate("posting_date"));
		dto.setTdsDeductionDate(rs.getDate("tds_deduction_date"));
		dto.setDocumentType(rs.getString("document_type"));
		dto.setSupplyType(rs.getString("supply_type"));
		dto.setErpDocumentType(rs.getString("erp_document_type"));
		dto.setLineItemNumber(rs.getInt("line_item_number"));
		dto.setHsnOrSac(rs.getString("hsn_or_sac"));
		dto.setHsnOrSacDesc(rs.getString("hsn_or_sac_desc"));
		dto.setAdvanceDesc(rs.getString("advance_desc"));
		dto.setGlAccountCode(rs.getString("gl_account_code"));
		dto.setGlAccountName(rs.getString("gl_account_name"));
		dto.setPoNumber(rs.getString("po_number"));
		dto.setPoDate(rs.getDate("po_date"));
		dto.setPoDesc(rs.getString("po_desc"));
		dto.setTaxablevalue(rs.getBigDecimal("taxable_value"));
		dto.setTdsTaxCodeErp(rs.getString("tds_tax_code_erp"));
		dto.setTdsSection(rs.getString("tds_section"));
		dto.setTdsRate(rs.getBigDecimal("tds_rate"));
		dto.setTdsAmount(rs.getBigDecimal("tds_amount"));
		dto.setLinkingofInvoicewithPO(rs.getString("linking_of_invoice_with_po"));
		dto.setChallanPaid(rs.getInt("challan_paid") == 1 ? "Y" : "N");
		dto.setChallanProcessingDate(rs.getDate("challan_processing_date"));
		dto.setGrossUpIndicator(rs.getInt("gross_up_indicator") == 1 ? "Y" : "N");
		dto.setOriginalDocumentNumber(rs.getString("original_document_number"));
		dto.setOriginalDocumentPostingDate(rs.getDate("original_document_posting_date"));
		dto.setBusinessPlace(rs.getString("business_place"));
		dto.setBusinessArea(rs.getString("business_area"));
		dto.setPlant(rs.getString("plant"));
		dto.setProfitCenter(rs.getString("profit_center"));
		dto.setAssignmentNumber(rs.getString("assignment_number"));
		dto.setTdsBaseValue(rs.getBigDecimal("tds_base_value"));
		dto.setPoLineItemNumber(rs.getString("po_line_item_number"));
		dto.setUserName(rs.getString("user_name"));
		dto.setNrIndicator(rs.getString("nr_indicator"));
		dto.setTdsRemittancedate(rs.getDate("tds_remittance_date"));
		dto.setDebitCreditIndicator(rs.getString("debit_credit_indicator"));
		dto.setCessRate(rs.getBigDecimal("cess_rate"));
		dto.setCessAmount(rs.getBigDecimal("cess_amount"));
		dto.setAmountForeignCurrency(rs.getBigDecimal("amount_foreign_currency"));
		dto.setExchangeRate(rs.getBigDecimal("exchange_rate"));
		dto.setCurrency(rs.getString("currency"));
		dto.setUserDefinedField1(rs.getString("user_defined_field1"));
		dto.setUserDefinedField2(rs.getString("user_defined_field2"));
		dto.setUserDefinedField3(rs.getString("user_defined_field3"));
		dto.setUserDefinedField4(rs.getString("user_defined_field_4"));
		dto.setUserDefinedField5(rs.getString("user_defined_field_5"));
		dto.setUserDefinedField6(rs.getString("user_defined_field_6"));
		dto.setUserDefinedField7(rs.getString("user_defined_field_7"));
		dto.setUserDefinedField8(rs.getString("user_defined_field_8"));
		dto.setUserDefinedField9(rs.getString("user_defined_field_9"));
		dto.setUserDefinedField10(rs.getString("user_defined_field_10"));
		dto.setActive(rs.getInt("active") == 1 ? true : false);
		dto.setCreatedBy(rs.getString("created_by"));
		dto.setCreatedDate(rs.getDate("created_date"));
		return dto;
	}

}
