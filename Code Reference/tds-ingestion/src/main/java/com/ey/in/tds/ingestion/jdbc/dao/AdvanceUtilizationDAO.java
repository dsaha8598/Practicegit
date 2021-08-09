package com.ey.in.tds.ingestion.jdbc.dao;

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
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.AdvanceUtilizationDTO;
import com.ey.in.tds.common.domain.transactions.jdbc.rowmapper.AdvanceUtilizationRowMapper;
/**
 * 
 * @author Scriptbees.
 *
 */
@Repository
public class AdvanceUtilizationDAO implements Serializable {

	
	private static final long serialVersionUID = 1L;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired(required = false)
	private HashMap<String, String> queries;

	private JdbcTemplate jdbcTemplate;

	private SimpleJdbcInsert simpleJdbcInsert;

	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	private DataSource dataSource;

	@PostConstruct
	private void postConstruct() {
		jdbcTemplate = new JdbcTemplate(dataSource);
		namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("advance_utilization").withSchemaName("Transactions")
				.usingGeneratedKeyColumns("advance_utilization_id");

	}
	
	public List<AdvanceUtilizationDTO> getAdvanceAdjustment(String deductorTan, int assessmentYear, int month) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("deductorMasterTan", deductorTan);
		parameters.put("year", assessmentYear);
		parameters.put("month", month);

		return namedParameterJdbcTemplate.query(String.format(queries.get("get_advance_adjusted")), parameters,
				new AdvanceUtilizationRowMapper());

	}
	
	public List<AdvanceUtilizationDTO> getAdvanceAdjustmentBasedOnTanAndYear(String deductorTan, int assessmentYear) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("deductorMasterTan", deductorTan);
		parameters.put("year", assessmentYear);

		return namedParameterJdbcTemplate.query(String.format(queries.get("get_advance_adjusted_based_on_year")), parameters,
				new AdvanceUtilizationRowMapper());

	}
	


}
