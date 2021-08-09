package com.ey.in.tds.ingestion.jdbc.rowmapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ey.in.tds.common.domain.transactions.jdbc.dto.TcsGstPurchaseRegister;

/**
 * 
 * @author vamsir
 *
 */
public class TcsGstPurchaseRegisterRowMapper implements RowMapper<TcsGstPurchaseRegister> {

	@Override
	public TcsGstPurchaseRegister mapRow(ResultSet rs, int rowNum) throws SQLException {

		TcsGstPurchaseRegister tcsGstPr = new TcsGstPurchaseRegister();
		tcsGstPr.setId(rs.getInt("id"));
		tcsGstPr.setReturnPeriod(rs.getInt("return_period"));
		tcsGstPr.setDocumentType(rs.getString("document_type"));
		tcsGstPr.setSupplyType(rs.getString("supply_type"));
		tcsGstPr.setDocumentNumber(rs.getString("document_number"));
		tcsGstPr.setDocumentDate(rs.getDate("document_date"));
		tcsGstPr.setSupplierGstin(rs.getString("supplier_gstin"));
		tcsGstPr.setPan(rs.getString("pan"));
		tcsGstPr.setSupplierName(rs.getString("supplier_name"));
		tcsGstPr.setHsnOrSac(rs.getString("hsn_or_sac"));
		tcsGstPr.setQuantity(rs.getBigDecimal("quantity"));
		tcsGstPr.setTaxableValue(
				rs.getString("taxable_value") == null ? BigDecimal.ZERO : rs.getBigDecimal("taxable_value"));
		tcsGstPr.setIntegratedTaxamount(rs.getString("integrated_taxamount") == null ? BigDecimal.ZERO
				: rs.getBigDecimal("integrated_taxamount"));
		tcsGstPr.setCentralTaxAmount(rs.getBigDecimal("central_tax_amount") == null ? BigDecimal.ZERO
				: rs.getBigDecimal("central_tax_amount"));
		tcsGstPr.setStateUttaxAmount(rs.getBigDecimal("state_uttax_amount") == null ? BigDecimal.ZERO
				: rs.getBigDecimal("state_uttax_amount"));
		tcsGstPr.setPurchaseVoucherNumber(rs.getString("purchase_voucher_number"));
		tcsGstPr.setInvoiceValue(
				rs.getBigDecimal("invoice_value") == null ? BigDecimal.ZERO : rs.getBigDecimal("invoice_value"));
		tcsGstPr.setPurchaseVoucherDate(rs.getDate("purchase_voucher_date"));
		tcsGstPr.setPaymentDate(rs.getDate("payment_date"));
		tcsGstPr.setExtractorMonth(rs.getString("extractor_month"));
		tcsGstPr.setPrIsMatched(rs.getBoolean("pr_is_matched"));
		tcsGstPr.setPvnMatchWithTcsLedger(rs.getString("pvn_match_with_tcs_ledger"));
		tcsGstPr.setPvnMatch(rs.getBoolean("pvn_match"));
		tcsGstPr.setCollectorPan(rs.getString("collector_pan"));
		tcsGstPr.setCollectorTan(rs.getString("collector_tan"));
		tcsGstPr.setAssessmentYear(rs.getInt("assessment_year"));
		tcsGstPr.setActive(rs.getBoolean("active"));
		tcsGstPr.setCreatedDate(rs.getDate("created_date"));
		tcsGstPr.setModifiedDate(rs.getDate("modified_date"));
		tcsGstPr.setCreatedBy(rs.getString("created_by"));
		tcsGstPr.setModifiedBy(rs.getString("modified_by"));

		return tcsGstPr;

	}

}
