package com.ey.in.tds.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ey.in.tcs.common.domain.CollecteeDeclaration;

/**
 * 
 * @author vamsir
 *
 */
public class CollecteeDeclarationRowMapper implements RowMapper<CollecteeDeclaration> {

	@Override
	public CollecteeDeclaration mapRow(ResultSet rs, int rowNum) throws SQLException {
		CollecteeDeclaration collecteeDeclaration = new CollecteeDeclaration();

		collecteeDeclaration.setId(rs.getInt("id"));
		collecteeDeclaration.setCollecteeId(rs.getInt("collectee_id"));
		collecteeDeclaration.setCollecteeCode(rs.getString("collectee_code"));
		collecteeDeclaration.setRateType(rs.getString("rate_type"));
		collecteeDeclaration.setTdsOrTcs(rs.getString("tds_or_tcs"));
		collecteeDeclaration.setApplicableFrom(rs.getDate("applicable_from"));
		collecteeDeclaration.setApplicableTo(rs.getDate("applicable_to"));
		collecteeDeclaration.setYear(rs.getInt("year"));
		collecteeDeclaration.setDeductorPan(rs.getString("deductor_pan"));
		collecteeDeclaration.setDeductorTan(rs.getString("deductor_tan"));
		collecteeDeclaration.setActive(rs.getBoolean("active"));
		collecteeDeclaration.setCreatedBy(rs.getString("created_by"));
		collecteeDeclaration.setCreatedDate(rs.getDate("created_date"));
		collecteeDeclaration.setModifiedBy(rs.getString("modified_by"));
		collecteeDeclaration.setModifiedDate(rs.getDate("modified_date"));
		collecteeDeclaration.setSpecifiedPerson(rs.getString("specified_person"));

		return collecteeDeclaration;
	}

}
