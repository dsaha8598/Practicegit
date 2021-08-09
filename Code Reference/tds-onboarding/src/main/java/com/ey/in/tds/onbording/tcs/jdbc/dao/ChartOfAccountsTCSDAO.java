package com.ey.in.tds.onbording.tcs.jdbc.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.onboarding.tcs.jdbc.dto.ChartOfAccountsTCS;
import com.ey.in.tds.common.onboarding.tcs.jdbc.rowmapper.ChartOfAccountsTCSRowmapper;

@Repository
public class ChartOfAccountsTCSDAO {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private HashMap<String, String> queries;

	@Autowired
	private DataSource dataSource;

	private JdbcTemplate jdbcTemplate;
	private SimpleJdbcInsert jdbcInsert;
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@PostConstruct
	private void postConstruct() {
		jdbcTemplate = new JdbcTemplate(dataSource);
		namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		jdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("tcs_chart_of_accounts")
				.withSchemaName("Client_Masters").usingGeneratedKeyColumns("id");
	}

	/**
	 * saves data in charts of accounts table
	 * 
	 * @param dto
	 * @return
	 */
	public ChartOfAccountsTCS save(ChartOfAccountsTCS dto) {

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("chart_of_accounts_id", dto.getChartOfAccountsID());
		parameters.put("pan", dto.getPan());
		parameters.put("account_code", dto.getAccountCode());
		parameters.put("account_description", dto.getAccountDescription());
		parameters.put("account_type", dto.getAccountType());
		parameters.put("active", dto.getActive());
		parameters.put("assessment_year", dto.getAssessmentYear());
		parameters.put("batch_id", dto.getBatchId());
		parameters.put("classification", dto.getClassification());
		parameters.put("created_by", dto.getCreatedBy());
		parameters.put("created_date", dto.getCreatedDate());
		dto.setChartOfAccountsID(jdbcInsert.executeAndReturnKey(parameters).intValue());
		return dto;
	}

	/**
	 * 
	 * @param deductorPan
	 * @return
	 */
	public List<ChartOfAccountsTCS> findAllTcsChartsOfAccountsByDeductorPan(String deductorPan) {
		return jdbcTemplate.query(String.format(queries.get("find_all_tcs_chartsOfaccounts")),
				new ChartOfAccountsTCSRowmapper(), deductorPan);

	}

	/**
	 * 
	 * @param collectorPan
	 * @param accountCode
	 * @param accountType
	 * @param accountDescription
	 * @param classification
	 * @return
	 */
	public List<ChartOfAccountsTCS> getAccountDetails(String collectorPan, String accountCode, String accountType,
			String accountDescription, String classification) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("collectorPan", collectorPan);
		parameters.put("accountCode", accountCode);
		parameters.put("accountType", accountType);
		parameters.put("accountDescription", accountDescription);
		parameters.put("classification", classification);

		return namedParameterJdbcTemplate.query(String.format(queries.get("get_tcs_charts_of_accounts_details")),
				parameters, new ChartOfAccountsTCSRowmapper());
	}
}
