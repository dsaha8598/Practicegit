package com.ey.in.tds.ingestion.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ey.in.tds.common.domain.transactions.jdbc.dto.Tcs26AsInputDto;
/**
 * 
 * @author vamsir
 *
 */
public class TcsForm26ASRowMapper implements RowMapper<Tcs26AsInputDto> {

	@Override
	public Tcs26AsInputDto mapRow(ResultSet rs, int rowNum) throws SQLException {

		Tcs26AsInputDto tcs26ASDto = new Tcs26AsInputDto();
		
		tcs26ASDto.setId(rs.getInt("id"));
		tcs26ASDto.setPart(rs.getString("part"));
		tcs26ASDto.setHeader(rs.getInt("header"));
		tcs26ASDto.setChild(rs.getInt("child"));
		tcs26ASDto.setCollectorName(rs.getString("collector_name"));
		tcs26ASDto.setTotalAmountPaid(rs.getBigDecimal("total_amount_paid"));
		tcs26ASDto.setTotalTaxDeducted(rs.getBigDecimal("total_tax_deducted"));
		tcs26ASDto.setTotalTdsDeposited(rs.getBigDecimal("total_tds_deposited"));
		tcs26ASDto.setSection(rs.getString("section"));
		tcs26ASDto.setTransactionDate(rs.getDate("transaction_date"));
		tcs26ASDto.setStatusOfBooking(rs.getString("status_of_booking"));
		tcs26ASDto.setDateOfBooking(rs.getDate("date_of_booking"));
		tcs26ASDto.setRemarks(rs.getString("remarks"));
		tcs26ASDto.setAmountPaid(rs.getBigDecimal("amount_paid"));
		tcs26ASDto.setTaxDeducted(rs.getBigDecimal("tax_deducted"));
		tcs26ASDto.setTdsDeposited(rs.getBigDecimal("tds_deposited"));
		tcs26ASDto.setCollectorPan(rs.getString("collector_pan"));
		tcs26ASDto.setCollectorTan(rs.getString("collector_tan"));
		tcs26ASDto.setAssessmentYear(rs.getInt("assessment_year"));
		tcs26ASDto.setActive(rs.getBoolean("active"));
		tcs26ASDto.setCreatedDate(rs.getDate("created_date"));
		tcs26ASDto.setModifiedDate(rs.getDate("modified_date"));
		tcs26ASDto.setCreatedBy(rs.getString("created_by"));
		tcs26ASDto.setModifiedBy(rs.getString("modified_by"));

		return tcs26ASDto;

	}

}
