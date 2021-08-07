package com.ey.in.tds.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ey.in.tds.common.onboarding.jdbc.dto.ShareholderDeclaration;

/**
 * 
 * @author vamsir
 *
 */
public class ShareholderDeclarationRowMapper implements RowMapper<ShareholderDeclaration> {

	@Override
	public ShareholderDeclaration mapRow(ResultSet rs, int rowNum) throws SQLException {
		ShareholderDeclaration shareholderDeclaration = new ShareholderDeclaration();

		shareholderDeclaration.setId(rs.getInt("id"));
		shareholderDeclaration.setShareholderId(rs.getInt("shareholder_id"));
		shareholderDeclaration.setShareholderCode(rs.getString("shareholder_code"));
		shareholderDeclaration.setRateType(rs.getString("rate_type"));
		shareholderDeclaration.setTdsOrTcs(rs.getString("tds_or_tcs"));
		shareholderDeclaration.setApplicableFrom(rs.getDate("applicable_from"));
		shareholderDeclaration.setApplicableTo(rs.getDate("applicable_to"));
		shareholderDeclaration.setYear(rs.getInt("year"));
		shareholderDeclaration.setDeductorPan(rs.getString("deductor_pan"));
		shareholderDeclaration.setDeductorTan(rs.getString("deductor_tan"));
		shareholderDeclaration.setActive(rs.getBoolean("active"));
		shareholderDeclaration.setCreatedBy(rs.getString("created_by"));
		shareholderDeclaration.setCreatedDate(rs.getDate("created_date"));
		shareholderDeclaration.setModifiedBy(rs.getString("modified_by"));
		shareholderDeclaration.setModifiedDate(rs.getDate("modified_date"));
		shareholderDeclaration.setSpecifiedPerson(rs.getString("specified_person"));

		return shareholderDeclaration;
	}

}
