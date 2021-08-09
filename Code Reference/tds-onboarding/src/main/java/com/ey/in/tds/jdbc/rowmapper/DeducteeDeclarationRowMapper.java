package com.ey.in.tds.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ey.in.tds.common.dto.onboarding.deductee.DeducteeDeclaration;

/**
 * 
 * @author vamsir
 *
 */
public class DeducteeDeclarationRowMapper implements RowMapper<DeducteeDeclaration> {

	@Override
	public DeducteeDeclaration mapRow(ResultSet rs, int rowNum) throws SQLException {
		DeducteeDeclaration deducteeDeclaration = new DeducteeDeclaration();

		deducteeDeclaration.setId(rs.getInt("id"));
		deducteeDeclaration.setDeducteeId(rs.getInt("deductee_id"));
		deducteeDeclaration.setDeducteeCode(rs.getString("deductee_code"));
		deducteeDeclaration.setRateType(rs.getString("rate_type"));
		deducteeDeclaration.setTdsOrTcs(rs.getString("tds_or_tcs"));
		deducteeDeclaration.setApplicableFrom(rs.getDate("applicable_from"));
		deducteeDeclaration.setApplicableTo(rs.getDate("applicable_to"));
		deducteeDeclaration.setYear(rs.getInt("year"));
		deducteeDeclaration.setDeductorPan(rs.getString("deductor_pan"));
		deducteeDeclaration.setDeductorTan(rs.getString("deductor_tan"));
		deducteeDeclaration.setActive(rs.getBoolean("active"));
		deducteeDeclaration.setCreatedBy(rs.getString("created_by"));
		deducteeDeclaration.setCreatedDate(rs.getDate("created_date"));
		deducteeDeclaration.setModifiedBy(rs.getString("modified_by"));
		deducteeDeclaration.setModifiedDate(rs.getDate("modified_date"));
		deducteeDeclaration.setSpecifiedPerson(rs.getString("specified_person"));
		
		return deducteeDeclaration;
	}

}
