package com.ey.in.tds.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ey.in.tds.common.onboarding.jdbc.dto.UserAccessDetailsJdbcDTO;

public class UserAccessDetailsRowMapper implements RowMapper<UserAccessDetailsJdbcDTO>{

	@Override
	public UserAccessDetailsJdbcDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		UserAccessDetailsJdbcDTO dto=new UserAccessDetailsJdbcDTO();
		dto.setUserAccessDetailsId(rs.getInt("user_access_details_id"));
		dto.setUserId(rs.getInt("user_id"));
		dto.setUserName(rs.getString("user_name"));
		dto.setPan(rs.getString("user_pan"));
		dto.setTan(rs.getString("user_tan"));
		dto.setRoleId(rs.getInt("role_id"));
		dto.setActive(rs.getInt("active")==1?true:false);
		dto.setCreateddate(rs.getDate("created_date"));
		dto.setCreateduser(rs.getString("created_user"));
		return dto;
	}
}
