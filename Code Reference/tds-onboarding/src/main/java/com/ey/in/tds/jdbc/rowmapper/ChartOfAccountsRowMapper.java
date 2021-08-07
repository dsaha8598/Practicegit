package com.ey.in.tds.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ey.in.tds.common.onboarding.jdbc.dto.ChartOfAccounts;

public class ChartOfAccountsRowMapper implements RowMapper<ChartOfAccounts> {

	@Override
	public ChartOfAccounts mapRow(ResultSet rs, int rowNum) throws SQLException {
		ChartOfAccounts dto = new ChartOfAccounts();

		dto.setId(rs.getInt("chart_of_accounts_id"));
		dto.setPan(rs.getString("pan"));
		dto.setAccountCode(rs.getString("account_code"));
		dto.setAccountDescription(rs.getString("account_description"));
		dto.setAccountType(rs.getString("account_type"));
		dto.setActive(rs.getInt("active") == 1 ? true : false);
		dto.setAssessmentYear(rs.getInt("assessment_year"));
		dto.setBatchId(rs.getInt("batch_id"));
		dto.setClassification(rs.getString("classification"));
		dto.setCreatedBy(rs.getString("created_by"));
		dto.setCreatedDate(rs.getDate("created_date"));
		dto.setNatureOfPayment(rs.getString("nature_of_payment"));
		dto.setTdsSection(rs.getString("tds_section"));
		return dto;
	}
}
