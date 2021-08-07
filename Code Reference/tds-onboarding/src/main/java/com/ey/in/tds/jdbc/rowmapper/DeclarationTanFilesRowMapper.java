package com.ey.in.tds.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ey.in.tds.common.onboarding.jdbc.dto.DeclarationTanFiles;

/**
 * 
 * @author vamsir
 *
 */
public class DeclarationTanFilesRowMapper implements RowMapper<DeclarationTanFiles> {

	@Override
	public DeclarationTanFiles mapRow(ResultSet rs, int rowNum) throws SQLException {
		DeclarationTanFiles kyc = new DeclarationTanFiles();

		kyc.setId(rs.getInt("id"));
		kyc.setKycId(rs.getInt("kyc_id"));
		kyc.setFilePath(rs.getString("file_path"));
		kyc.setKycTanNumber(rs.getString("kyc_tan_number"));
		kyc.setAssesmentYear(rs.getInt("assesment_year"));
		kyc.setDeductorTan(rs.getString("deductor_tan"));
		kyc.setDeductorPan(rs.getString("deductor_pan"));
		kyc.setActive(rs.getBoolean("active"));
		kyc.setCreatedBy(rs.getString("created_by"));
		kyc.setCreatedDate(rs.getTimestamp("created_date"));
		kyc.setModifiedBy(rs.getString("modified_by"));
		kyc.setModifiedDate(rs.getTimestamp("modified_date"));
		kyc.setFileName(rs.getString("file_name"));

		return kyc;
	}

}
