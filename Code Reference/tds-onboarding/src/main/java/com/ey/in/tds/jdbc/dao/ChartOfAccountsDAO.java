package com.ey.in.tds.jdbc.dao;

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
import com.ey.in.tds.common.onboarding.jdbc.dto.ChartOfAccounts;
import com.ey.in.tds.jdbc.rowmapper.ChartOfAccountsRowMapper;

@Repository
public class ChartOfAccountsDAO {
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
		jdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("chart_of_accounts")
				.withSchemaName("Client_Masters").usingGeneratedKeyColumns("chart_of_accounts_id");
	}

	/**
	 * inserts record in chart of account table
	 * 
	 * @param dto
	 * @return
	 */
	public ChartOfAccounts save(ChartOfAccounts dto) {
		logger.info("insert method execution started  {}");
		Map<String, Object> parameters = mapParameters(dto);
		dto.setId(jdbcInsert.executeAndReturnKey(parameters).intValue());
		logger.info("Record inserted to chart of account table {}");
		return dto;
	}

	public Map<String, Object> mapParameters(ChartOfAccounts dto) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("chart_of_accounts_id", dto.getId());
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
		parameters.put("tds_section", dto.getTdsSection());
		parameters.put("nature_of_payment", dto.getNatureOfPayment());
		return parameters;
	}

	/**
	 * 
	 * @param deductorPan
	 * @return
	 */
	public List<ChartOfAccounts> findAllByDeductorPan(String deductorPan) {
		return jdbcTemplate.query(String.format(queries.get("find_all_chartsOfaccounts")),
				new ChartOfAccountsRowMapper(), deductorPan);

	}

	/**
	 * 
	 * @param deductorPan
	 * @param accountCode
	 * @param accountType
	 * @param accountDescription
	 * @param classification
	 * @return
	 */
	public List<ChartOfAccounts> getAccountDetails(String deductorPan, String accountCode, String accountType,
			String accountDescription, String classification) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("accountCode", accountCode);
		parameters.put("accountType", accountType);
		parameters.put("accountDescription", accountDescription);
		parameters.put("classification", classification);

		return namedParameterJdbcTemplate.query(String.format(queries.get("get_charts_of_accounts_details")),
				parameters, new ChartOfAccountsRowMapper());
	}
}
