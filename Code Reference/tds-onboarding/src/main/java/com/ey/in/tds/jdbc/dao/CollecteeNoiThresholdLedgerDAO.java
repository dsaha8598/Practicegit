package com.ey.in.tds.jdbc.dao;

import java.io.Serializable;
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

import com.ey.in.tcs.common.domain.transactions.jdbc.rowmapper.CollecteeNoiThresholdLedgerRowMapper;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.onboarding.jdbc.dto.CollecteeNoiThresholdLedger;

/**
 * 
 * @author vamsir
 *
 */
@Repository
public class CollecteeNoiThresholdLedgerDAO implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private HashMap<String, String> queries;

	@Autowired
	private DataSource dataSource;

	private JdbcTemplate jdbcTemplate;

	private SimpleJdbcInsert simpleJdbcInsert;

	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@PostConstruct
	private void postConstruct() {
		jdbcTemplate = new JdbcTemplate(dataSource);
		namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("collectee_noi_threshold_ledger")
				.withSchemaName("Transactions").usingGeneratedKeyColumns("id");
	}

	/**
	 * This method for save collectee noi data
	 * 
	 * @param collecteeMaster
	 * @return
	 */
	public CollecteeNoiThresholdLedger save(CollecteeNoiThresholdLedger collecteeNoiThresholdLedger) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		logger.info("insert method execution started  {}");
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(collecteeNoiThresholdLedger);
		collecteeNoiThresholdLedger.setId(simpleJdbcInsert.executeAndReturnKey(namedParameters).intValue());
		logger.info("Record inserted to collectee master table {}");
		return collecteeNoiThresholdLedger;
	}

	/**
	 * 
	 * @param collecteeNoiThresholdLedger
	 * @return
	 */
	public CollecteeNoiThresholdLedger update(CollecteeNoiThresholdLedger collecteeNoiThresholdLedger) {
		logger.info("DAO method executing to update user data ");
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(collecteeNoiThresholdLedger);
		int status = namedParameterJdbcTemplate
				.update(String.format(queries.get("update_collectee_noi_threshold_ledger")), namedParameters);
		if (status != 0) {
			logger.info("collectee master data is updated for ID " + collecteeNoiThresholdLedger.getId());
		} else {
			logger.info("No record found with ID " + collecteeNoiThresholdLedger.getId());
		}
		logger.info("DAO method execution successful {}");
		return collecteeNoiThresholdLedger;
	}

	/**
	 * This method for get Collectee Noi Threshold Ledger data based on collectee
	 * pan
	 * 
	 * @param collecteePan
	 * @param collectorPan
	 * @return
	 */
	public List<CollecteeNoiThresholdLedger> findByCollecteePan(String collecteePan, String collectorPan) {

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("collecteePan", collecteePan);
		parameters.put("collectorPan", collectorPan);
		String query = String.format(queries.get("tcs_find_noi_by_pan"));

		return namedParameterJdbcTemplate.query(query, parameters, new CollecteeNoiThresholdLedgerRowMapper());
	}

	public int insertSelectForNoi(Integer year, String collectorPan) {

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("year", year);
		parameters.put("collectorPan", collectorPan);
		String query = String.format(queries.get("tcs_insert_select_noi"));

		return namedParameterJdbcTemplate.update(query, parameters);
	}

	/**
	 * This method for get Collectee Noi Threshold Ledger data based on collectee
	 * code
	 * 
	 * @param collecteeCode
	 * @param collectorPan
	 * @return
	 */
	public List<CollecteeNoiThresholdLedger> findByCollecteeCode(String collecteeCode, String collectorPan) {

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("collecteeCode", collecteeCode);
		parameters.put("collectorPan", collectorPan);
		String query = String.format(queries.get("tcs_find_noi_by_code"));

		return namedParameterJdbcTemplate.query(query, parameters, new CollecteeNoiThresholdLedgerRowMapper());
	}

	/**
	 * 
	 * @param collecteeCode
	 * @param collectorPan
	 * @param collecteePan
	 * @param year
	 * @return
	 */
	public List<CollecteeNoiThresholdLedger> findByCollecteeCodeOrCollecteePan(String collecteeCode,
			String collectorPan, String collecteePan, Integer year) {
		String query = String.format(queries.get("tcs_find_noi_by_code_or_collectee_pan"));
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("collecteeCode", collecteeCode);
		parameters.put("collectorPan", collectorPan);
		parameters.put("collecteePan", collecteePan);
		if (year > 0) {
			query = query.concat(" AND year = " + year + " ");
		}
		logger.info("query is :{}", query);
		return namedParameterJdbcTemplate.query(query, parameters, new CollecteeNoiThresholdLedgerRowMapper());
	}

	public void updateByCollecteePan(CollecteeNoiThresholdLedger collecteeNoiThresholdLedger) {
		logger.info("DAO method executing to update user data ");
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(collecteeNoiThresholdLedger);
		int status = namedParameterJdbcTemplate
				.update(String.format(queries.get("update_collectee_noi_threshold_ledger_by_pan")), namedParameters);
		if (status != 0) {
			logger.info("collecteeNoiThresholdLedger is updated for ID " + collecteeNoiThresholdLedger.getId());
		} else {
			logger.info("No record found with ID " + collecteeNoiThresholdLedger.getId());
		}
		logger.info("DAO method execution successful {}");

	}
	
	public void updateByCollecteeCode(CollecteeNoiThresholdLedger collecteeNoiThresholdLedger) {
		logger.info("DAO method executing to update user data ");
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(collecteeNoiThresholdLedger);
		int status = namedParameterJdbcTemplate
				.update(String.format(queries.get("update_collectee_noi_threshold_ledger_by_code")), namedParameters);
		if (status != 0) {
			logger.info("collecteeNoiThresholdLedger is updated for ID " + collecteeNoiThresholdLedger.getId());
		} else {
			logger.info("No record found with ID " + collecteeNoiThresholdLedger.getId());
		}
		logger.info("DAO method execution successful {}");

	}

}
