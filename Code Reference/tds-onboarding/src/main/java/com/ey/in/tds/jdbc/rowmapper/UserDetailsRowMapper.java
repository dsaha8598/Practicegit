package com.ey.in.tds.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import com.ey.in.tds.common.onboarding.jdbc.dto.UserCassandraDTO;

/**
 * class to recieve data from resultset
 * 
 * @author scriptbees
 *
 */
public class UserDetailsRowMapper implements RowMapper<UserCassandraDTO> {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * mapping the data from resultset to respective field
	 */
	@Override
	public UserCassandraDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		logger.info("rowmapper method executing  {}");
		UserCassandraDTO dao = new UserCassandraDTO();
		dao.setConsent(rs.getInt("Consonent") == 1 ? true : false);
		dao.setConsentDate(rs.getDate("Consentdate"));
		String pans = rs.getString("DeductorPans").replace("[", "").replace("]", ""); 
		dao.setDeductorPans(pans);
		dao.setUserforAllTans(rs.getInt("userForAllTans") == 1 ? true : false);
		dao.setActiveflag(rs.getInt("activeFlag") == 1 ? true : false);
		dao.setCreateddate(rs.getDate("CreatedDate"));
		dao.setCreateduser(rs.getString("CreateduCser"));
		dao.setEmail(rs.getString("Email"));
		dao.setUserId(rs.getInt("userId"));
		dao.setModifieddate(rs.getDate("ModifiedDate"));
		dao.setModifieduser(rs.getString("ModifiedUser"));
		dao.setSourceType(rs.getString("SourceType"));
		dao.setUsername(rs.getString("UserName"));
		logger.info("rowmapper method execution succeded  {}");
		return dao;

	}
}
