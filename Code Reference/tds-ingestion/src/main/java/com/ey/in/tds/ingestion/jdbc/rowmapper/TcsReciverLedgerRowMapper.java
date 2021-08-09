package com.ey.in.tds.ingestion.jdbc.rowmapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ey.in.tds.common.domain.transactions.jdbc.dto.TcsReciverLedger;

/**
 * 
 * @author vamsir
 *
 */
public class TcsReciverLedgerRowMapper implements RowMapper<TcsReciverLedger> {

	@Override
	public TcsReciverLedger mapRow(ResultSet rs, int rowNum) throws SQLException {

		TcsReciverLedger tcsReciverLedger = new TcsReciverLedger();

		tcsReciverLedger.setId(rs.getInt("id"));
		tcsReciverLedger.setGlAccount(rs.getString("gl_account"));
		tcsReciverLedger.setDocumentHeaderText(rs.getString("document_header_text"));
		tcsReciverLedger.setDocumentNumber(rs.getString("document_number"));
		tcsReciverLedger.setCustomer(rs.getString("customer"));
		tcsReciverLedger.setVendor(rs.getString("vendor"));
		tcsReciverLedger.setPostingDate(rs.getDate("posting_date"));
		tcsReciverLedger.setDocumentDate(rs.getDate("document_date"));
		tcsReciverLedger.setReference(rs.getString("reference"));
		tcsReciverLedger.setBusinessPlace(rs.getString("business_place"));
		tcsReciverLedger.setOrders(rs.getString("orders"));
		tcsReciverLedger.setDocumentType(rs.getString("document_type"));
		tcsReciverLedger.setBusinessArea(rs.getString("business_area"));
		tcsReciverLedger.setProfitCenter(
				rs.getBigDecimal("profit_center") == null ? BigDecimal.ZERO : rs.getBigDecimal("profit_center"));
		tcsReciverLedger.setAmountInLocalCurrency(rs.getBigDecimal("amount_in_local_currency") == null ? BigDecimal.ZERO
				: rs.getBigDecimal("amount_in_local_currency"));
		tcsReciverLedger.setLocalCurrency(rs.getString("local_currency"));
		tcsReciverLedger.setText(rs.getString("text"));
		tcsReciverLedger.setOffsettingAccount(rs.getString("offsetting_account"));
		tcsReciverLedger.setNameOfOffsettingAccount(rs.getString("name_of_offsetting_account"));
		tcsReciverLedger.setCostCenter(rs.getBigDecimal("cost_center"));
		tcsReciverLedger.setPlant(rs.getString("plant"));
		tcsReciverLedger.setUserName(rs.getString("user_name"));
		tcsReciverLedger.setOffsettAccountType(rs.getString("offsett_account_type"));
		tcsReciverLedger.setMaterial(rs.getString("material"));
		tcsReciverLedger.setInvoiceReference(rs.getString("invoice_reference"));
		tcsReciverLedger.setCollectiveInvoice(rs.getBoolean("collective_invoice"));
		tcsReciverLedger.setYearMonth(rs.getString("year_month"));
		tcsReciverLedger.setBillingDocument(rs.getString("billing_document"));
		tcsReciverLedger.setPurchaseVoucherNumber(rs.getString("purchase_voucher_number"));
		tcsReciverLedger.setRlIsMatched(rs.getBoolean("rl_is_matched"));
		tcsReciverLedger.setMatch(rs.getBoolean("is_match"));
		tcsReciverLedger.setCollectorPan(rs.getString("collector_pan"));
		tcsReciverLedger.setCollectorTan(rs.getString("collector_tan"));
		tcsReciverLedger.setAssessmentYear(rs.getInt("assessment_year"));
		tcsReciverLedger.setActive(rs.getBoolean("active"));
		tcsReciverLedger.setCreatedDate(rs.getDate("created_date"));
		tcsReciverLedger.setModifiedDate(rs.getDate("modified_date"));
		tcsReciverLedger.setCreatedBy(rs.getString("created_by"));
		tcsReciverLedger.setModifiedBy(rs.getString("modified_by"));

		return tcsReciverLedger;

	}

}
