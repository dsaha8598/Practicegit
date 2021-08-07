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

import com.ey.in.tds.common.domain.transactions.jdbc.dto.TcsReciverLedger;
import com.ey.in.tds.ingestion.jdbc.rowmapper.TcsReciverLedgerRowMapper;

/**
 * 
 * @author vamsir
 *
 */
@Repository
public class TcsReciverLedgerDAO {

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
		simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("tcs_reciver_ledger")
				.withSchemaName("Transactions").usingGeneratedKeyColumns("id");

	}

	/**
	 * 
	 * @param tcsRl
	 * @return
	 */
	public TcsReciverLedger save(TcsReciverLedger tcsRl) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(tcsRl);
		tcsRl.setId(simpleJdbcInsert.executeAndReturnKey(namedParameters).intValue());
		logger.info("Record inserted to tcs reciver ledger table {}");
		return tcsRl;
	}

	/**
	 * 
	 * @param collectorTan
	 * @param assesmentYear
	 * @param collectorPan
	 * @param isMatched
	 * @return
	 */
	public List<TcsReciverLedger> getTcsReciverLedgerData(String collectorTan, Integer assesmentYear,
			String collectorPan, boolean isMatched) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assesmentYear", assesmentYear);
		parameters.put("collectorPan", collectorPan);
		parameters.put("collectorTan", collectorTan);
		parameters.put("isMatched", isMatched == false ? 0 : 1);
		return namedParameterJdbcTemplate.query(String.format(tcsQueries.get("get_tcs_rl_data")), parameters,
				new TcsReciverLedgerRowMapper());

	}

}
