package com.ey.in.tds.ingestion.jdbc.rowmapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ey.in.tds.common.domain.transactions.jdbc.dto.TcsGstPurchaseRegisterReportDTO;
/**
 * 
 * @author vamsir
 *
 */
public class TcsGstPurchaseRegisterReportRowMapper implements RowMapper<TcsGstPurchaseRegisterReportDTO> {

	@Override
	public TcsGstPurchaseRegisterReportDTO mapRow(ResultSet rs, int rowNum) throws SQLException {

		TcsGstPurchaseRegisterReportDTO tcsGstPrAndRlDto = new TcsGstPurchaseRegisterReportDTO();
		tcsGstPrAndRlDto.setReturnPeriod(rs.getInt("return_period"));
		tcsGstPrAndRlDto.setDocumentType(rs.getString("document_type"));
		tcsGstPrAndRlDto.setSupplyType(rs.getString("supply_type"));
		tcsGstPrAndRlDto.setDocumentNumber(rs.getString("document_number"));
		tcsGstPrAndRlDto.setDocumentDate(rs.getDate("document_date"));
		tcsGstPrAndRlDto.setSupplierGstin(rs.getString("supplier_gstin"));
		tcsGstPrAndRlDto.setPan(rs.getString("pan"));
		tcsGstPrAndRlDto.setSupplierName(rs.getString("supplier_name"));
		tcsGstPrAndRlDto.setHsnOrSac(rs.getString("hsn_or_sac"));
		tcsGstPrAndRlDto.setQuantity(rs.getBigDecimal("quantity"));
		tcsGstPrAndRlDto.setTaxableValue(
				rs.getString("taxable_value") == null ? BigDecimal.ZERO : rs.getBigDecimal("taxable_value"));
		tcsGstPrAndRlDto.setIntegratedTaxamount(rs.getString("integrated_taxamount") == null ? BigDecimal.ZERO
				: rs.getBigDecimal("integrated_taxamount"));
		tcsGstPrAndRlDto.setCentralTaxAmount(rs.getBigDecimal("central_tax_amount") == null ? BigDecimal.ZERO
				: rs.getBigDecimal("central_tax_amount"));
		tcsGstPrAndRlDto.setStateUttaxAmount(rs.getBigDecimal("state_uttax_amount") == null ? BigDecimal.ZERO
				: rs.getBigDecimal("state_uttax_amount"));
		tcsGstPrAndRlDto.setPurchaseVoucherNumber(rs.getString("purchase_voucher_number"));
		tcsGstPrAndRlDto.setInvoiceValue(
				rs.getBigDecimal("invoice_value") == null ? BigDecimal.ZERO : rs.getBigDecimal("invoice_value"));
		tcsGstPrAndRlDto.setPurchaseVoucherDate(rs.getDate("purchase_voucher_date"));
		tcsGstPrAndRlDto.setPaymentDate(rs.getDate("payment_date"));
		tcsGstPrAndRlDto.setExtractorMonth(rs.getString("extractor_month"));
		tcsGstPrAndRlDto.setPrIsMatched(rs.getBoolean("pr_is_matched"));
		tcsGstPrAndRlDto.setPvnMatchWithTcsLedger(rs.getString("pvn_match_with_tcs_ledger"));
		tcsGstPrAndRlDto.setPvnMatch(rs.getBoolean("pvn_match"));
		tcsGstPrAndRlDto.setAmountInLocalCurrency(rs.getBigDecimal("rl_amount_in_local_currency"));
		tcsGstPrAndRlDto.setRlPurchaseVoucherNumber(rs.getString("rl_purchase_voucher_number"));
		tcsGstPrAndRlDto.setRlDocumentDate(rs.getDate("rl_document_date"));
		return tcsGstPrAndRlDto;
	}

}
