package com.ey.in.tds.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ey.in.tds.common.onboarding.jdbc.dto.KYCDeclarationEmailDetails;

/**
 * 
 * @author vamsir
 *
 */
public class KYCDeclarationEmailDetailsRowMapper implements RowMapper<KYCDeclarationEmailDetails> {

	@Override
	public KYCDeclarationEmailDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
		KYCDeclarationEmailDetails kyc = new KYCDeclarationEmailDetails();

		kyc.setId(rs.getInt("id"));
		kyc.setLogo(rs.getString("logo"));
		kyc.setPrimaryColor(rs.getString("primary_color"));
		kyc.setSecondaryColor(rs.getString("secondary_color"));
		kyc.setCustomEmailContent(rs.getString("custom_email_content"));
		kyc.setSubject(rs.getString("subject"));
		kyc.setDeductorMasterTan(rs.getString("deductor_master_tan"));
		kyc.setDeductorPan(rs.getString("deductor_pan"));
		kyc.setActive(rs.getBoolean("active"));
		kyc.setCreatedBy(rs.getString("created_by"));
		kyc.setCreatedDate(rs.getDate("created_date"));
		kyc.setModifiedBy(rs.getString("modified_by"));
		kyc.setModifiedDate(rs.getDate("modified_date"));
		kyc.setIndemnifyDeclare(rs.getBoolean("indemnify_declare"));
		kyc.setIsEmailCc(rs.getBoolean("is_email_cc"));
		kyc.setIsApproved(rs.getBoolean("is_approved"));
		kyc.setType(rs.getString("type"));
		
		return kyc;
	}

}
