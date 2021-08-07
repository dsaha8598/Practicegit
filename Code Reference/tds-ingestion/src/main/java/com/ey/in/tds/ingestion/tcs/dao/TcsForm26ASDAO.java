package com.ey.in.tds.ingestion.tcs.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.transactions.jdbc.dto.Tcs26AsInputDto;
import com.ey.in.tds.ingestion.jdbc.rowmapper.TcsForm26ASRowMapper;

/**
 * 
 * @author vamsir
 *
 */
@Repository
public class TcsForm26ASDAO {

	private static final long serialVersionUID = 1L;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired(required = false)
	private HashMap<String, String> tcsQueries;

	private JdbcTemplate jdbcTemplate;

	private SimpleJdbcInsert simpleJdbcInsert;

	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	private DataSource dataSource;

	@PostConstruct
	private void postConstruct() {
		jdbcTemplate = new JdbcTemplate(dataSource);
		namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("tcs_form_26as")
				.withSchemaName("Transactions").usingGeneratedKeyColumns("id");

	}
	
	/**
	 * 
	 * @param tcs26As
	 * @return
	 */
	public Tcs26AsInputDto save(Tcs26AsInputDto tcs26As) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(tcs26As);
		tcs26As.setId(simpleJdbcInsert.executeAndReturnKey(namedParameters).intValue());
		logger.info("Record inserted to tcs form 26AS table {}");
		return tcs26As;
	}
	
	/**
	 * 
	 * @param collectorTan
	 * @param assesmentYear
	 * @param collectorPan
	 * @param isMatched
	 * @return
	 */
	public List<Tcs26AsInputDto> getAllTcsForm26AS(String collectorTan, Integer assesmentYear,
			String collectorPan, boolean isMatched) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assesmentYear", assesmentYear);
		parameters.put("collectorPan", collectorPan);
		parameters.put("collectorTan", collectorTan);
		parameters.put("isMatched", isMatched == false ? 0 : 1);
		return namedParameterJdbcTemplate.query(String.format(tcsQueries.get("")), parameters,
				new TcsForm26ASRowMapper());

	}
}
