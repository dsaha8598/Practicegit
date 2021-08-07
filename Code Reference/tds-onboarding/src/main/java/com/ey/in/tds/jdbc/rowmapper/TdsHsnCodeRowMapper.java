package com.ey.in.tds.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ey.in.tds.common.onboarding.jdbc.dto.TdsHsnCode;

public class TdsHsnCodeRowMapper implements RowMapper<TdsHsnCode> {

	@Override
	public TdsHsnCode mapRow(ResultSet rs, int rowNum) throws SQLException {
		TdsHsnCode tdsHsnCode = new TdsHsnCode();

		tdsHsnCode.setId(rs.getInt("id"));
		tdsHsnCode.setHsnCode(rs.getLong("hsn_code"));
		tdsHsnCode.setDescription(rs.getString("description"));
		tdsHsnCode.setActive(rs.getInt("active") == 1 ? true : false);
		tdsHsnCode.setCreatedBy(rs.getString("created_by"));
		tdsHsnCode.setCreatedDate(rs.getDate("created_date"));
		tdsHsnCode.setModifiedBy(rs.getString("modified_by"));
		tdsHsnCode.setModifiedDate(rs.getDate("modified_date"));
		tdsHsnCode.setNatureOfPayment(rs.getString("nature_of_payment"));
		tdsHsnCode.setTdsSection(rs.getString("tds_section"));
		tdsHsnCode.setDeductorMasterTan(rs.getString("deductor_master_tan"));
		tdsHsnCode.setDeductorPan(rs.getString("deductor_pan"));

		return tdsHsnCode;
	}

}
