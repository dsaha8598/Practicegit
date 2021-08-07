package com.ey.in.tds.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ey.in.tds.common.onboarding.jdbc.dto.KYCHistory;

/**
 * 
 * @author vamsir
 *
 */
public class KYCHistoryRowMapper implements RowMapper<KYCHistory> {

	@Override
	public KYCHistory mapRow(ResultSet rs, int rowNum) throws SQLException {
		KYCHistory kycHistory = new KYCHistory();

		kycHistory.setId(rs.getInt("id"));
		kycHistory.setKycDetailsId(rs.getInt("kyc_details_id"));
		kycHistory.setEmail(rs.getString("email"));
		kycHistory.setYear(rs.getInt("year"));
		kycHistory.setDeductorMasterTan(rs.getString("deductor_master_tan"));
		kycHistory.setDeductorPan(rs.getString("deductor_pan"));
		kycHistory.setActive(rs.getBoolean("active"));
		kycHistory.setCreatedBy(rs.getString("created_by"));
		kycHistory.setCreatedDate(rs.getTimestamp("created_date"));
		kycHistory.setModifiedBy(rs.getString("modified_by"));
		kycHistory.setModifiedDate(rs.getTimestamp("modified_date"));

		return kycHistory;
	}

}
