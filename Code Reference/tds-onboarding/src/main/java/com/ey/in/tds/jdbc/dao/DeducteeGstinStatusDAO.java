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
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.model.deductee.DeducteeGstinAndDeducteeResDTO;
import com.ey.in.tds.common.model.deductee.DeducteeGstinStatus;
import com.ey.in.tds.jdbc.rowmapper.DeducteeGstinAndDeducteeResDTORowMapper;
import com.ey.in.tds.jdbc.rowmapper.DeducteeGstinStatusRowMapper;

/**
 * 
 * @author vamsir
 *
 */
@Repository
public class DeducteeGstinStatusDAO {

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
		simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("deductee_gstin_status")
				.withSchemaName("Client_Masters").usingGeneratedKeyColumns("id");
	}

	/**
	 * This method for save deductee Gstin Status
	 * 
	 * @param deducteeGstinStatus
	 * @return
	 */
	public DeducteeGstinStatus save(DeducteeGstinStatus deducteeGstinStatus) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(deducteeGstinStatus);
		deducteeGstinStatus.setId(simpleJdbcInsert.executeAndReturnKey(namedParameters).intValue());
		logger.info("Record inserted to deductee Gstin Status table {}");
		return deducteeGstinStatus;
	}

	/**
	 * 
	 * @param year
	 * @param deductorPan
	 * @param tan
	 * @return
	 */
	public List<DeducteeGstinAndDeducteeResDTO> getAllDeducteeGstin(int year, String deductorPan, String tan, int month) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("tan", tan);
		parameters.put("year", year);
		parameters.put("month", month);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_all_deductee_gstin_report")), parameters,
				new DeducteeGstinAndDeducteeResDTORowMapper());
	}

	/**
	 * 
	 * @param deductorTan
	 * @param deductorPan
	 * @param month
	 * @param assessmentYear
	 * @param deducteeMasterId
	 * @return
	 */
	public List<DeducteeGstinStatus> getAllGstinBasedOnDeducteeId(String deductorTan, String deductorPan, int month,
			Integer year, Integer deducteeId) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("deductorTan", deductorTan);
		parameters.put("year", year);
		parameters.put("month", month);
		parameters.put("deducteeId", deducteeId);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_all_deductee_based_on_deducteeId")),
				parameters, new DeducteeGstinStatusRowMapper());
	}

	/**
	 * 
	 * @param deducteeGstinStatus
	 */
	public int deducteeGstinInActive(DeducteeGstinStatus deducteeGstinStatus) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(deducteeGstinStatus);
		int status = namedParameterJdbcTemplate.update(String.format(queries.get("deductee_gstin_inactive")),
				namedParameters);
		logger.info("inacvitve to deductee Gstin Status table {}");
		return status;

	}

}
