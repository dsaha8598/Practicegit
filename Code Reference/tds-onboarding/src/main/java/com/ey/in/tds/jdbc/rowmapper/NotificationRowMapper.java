package com.ey.in.tds.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import com.ey.in.tds.common.onboarding.jdbc.dto.EmailNotificationConfiguration;
/**
 * author : Dipak
 */
public class NotificationRowMapper implements RowMapper<EmailNotificationConfiguration>{
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public EmailNotificationConfiguration mapRow(ResultSet rs, int rowNum) throws SQLException {
		logger.info("Rowmapper method executing {}");
		EmailNotificationConfiguration dto=new EmailNotificationConfiguration();
		dto.setActive(rs.getInt("active")==1?true:false);
		dto.setCreatedBy(rs.getString("created_by"));
		dto.setCreatedDate(rs.getTimestamp("created_date"));
		dto.setId(rs.getInt("id"));
		dto.setModifiedBy(rs.getString("modified_by"));
		dto.setModifiedDate(rs.getTimestamp("modified_date"));
		dto.setPercentage(rs.getBigDecimal("percentage"));
		dto.setType(rs.getString("type"));
		return dto;
	}

}
