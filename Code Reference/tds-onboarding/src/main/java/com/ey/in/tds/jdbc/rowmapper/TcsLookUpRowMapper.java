package com.ey.in.tds.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ey.in.tds.onboarding.dto.lookup.TcsLookUpDTO;

/**
 * rowmapper class to map the fields from resultset
 * 
 * @author scriptbees
 *
 */
public class TcsLookUpRowMapper implements RowMapper<TcsLookUpDTO> {

	@Override
	public TcsLookUpDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		TcsLookUpDTO dao = new TcsLookUpDTO();
		dao.setId(rs.getInt("id"));
		dao.setSelectedValue(rs.getString("selectionValue"));
		dao.setModule(rs.getString("module"));
		dao.setIsMultiSelection(rs.getInt("multiSelection"));

		return dao;
	}
}
