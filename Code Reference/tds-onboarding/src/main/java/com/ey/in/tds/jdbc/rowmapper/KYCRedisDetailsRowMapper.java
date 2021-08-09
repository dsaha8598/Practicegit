package com.ey.in.tds.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ey.in.tds.common.onboarding.jdbc.dto.KYCRedisDetails;

/**
 * 
 * @author vamsir
 *
 */
public class KYCRedisDetailsRowMapper implements RowMapper<KYCRedisDetails> {

	@Override
	public KYCRedisDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
		KYCRedisDetails kycEmailDetails = new KYCRedisDetails();

		kycEmailDetails.setId(rs.getInt("id"));
		kycEmailDetails.setKycDetailsId(rs.getInt("kyc_details_id"));
		kycEmailDetails.setRedisKey(rs.getString("redis_key"));
		kycEmailDetails.setYear(rs.getInt("year"));
		kycEmailDetails.setDeductorMasterTan(rs.getString("deductor_master_tan"));
		kycEmailDetails.setDeductorPan(rs.getString("deductor_pan"));
		kycEmailDetails.setActive(rs.getBoolean("active"));
		kycEmailDetails.setCreatedBy(rs.getString("created_by"));
		kycEmailDetails.setCreatedDate(rs.getTimestamp("created_date"));
		kycEmailDetails.setModifiedBy(rs.getString("modified_by"));
		kycEmailDetails.setModifiedDate(rs.getTimestamp("modified_date"));
		kycEmailDetails.setEmail(rs.getString("email"));

		return kycEmailDetails;
	}

}
