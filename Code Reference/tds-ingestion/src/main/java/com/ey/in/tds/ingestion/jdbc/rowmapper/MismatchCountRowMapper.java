package com.ey.in.tds.ingestion.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import com.ey.in.tds.common.domain.transactions.jdbc.dto.MismatchesCountDTO;
/**
 * 
 * @author scriptbees.
 *
 */
public class MismatchCountRowMapper implements RowMapper<MismatchesCountDTO> {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public MismatchesCountDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		logger.info("rowmapper method  execution started {}");
		MismatchesCountDTO dto = new MismatchesCountDTO();

		dto.setAmount(rs.getBigDecimal("amount"));
		dto.setDerivedTdsAmount(rs.getBigDecimal("derivedTdsAmount"));
		dto.setActualTdsAmount(rs.getBigDecimal("actualTdsAmount"));
		dto.setTotalCount(rs.getBigDecimal("totalCount"));

		logger.info("rowmapper method  execution completed {}");

		return dto;
	}
}
