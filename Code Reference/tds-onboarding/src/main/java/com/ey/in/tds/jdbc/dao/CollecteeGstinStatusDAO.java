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

import com.ey.in.tcs.common.domain.CollecteeGstinAndCollecteeDTO;
import com.ey.in.tcs.common.domain.CollecteeGstinStatus;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.jdbc.rowmapper.CollecteeGstinAndCollecteeDTORowMapper;
import com.ey.in.tds.jdbc.rowmapper.CollecteeGstinStatusRowMapper;

/**
 * 
 * @author vamsir
 *
 */
@Repository
public class CollecteeGstinStatusDAO {

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
		simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("collectee_gstin_status")
				.withSchemaName("Client_Masters").usingGeneratedKeyColumns("id");
	}

	/**
	 * This method for save collectee gstin status
	 * 
	 * @param collecteeGstinStatus
	 * @return
	 */
	public CollecteeGstinStatus save(CollecteeGstinStatus collecteeGstinStatus) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(collecteeGstinStatus);
		collecteeGstinStatus.setId(simpleJdbcInsert.executeAndReturnKey(namedParameters).intValue());
		logger.info("Record inserted to collectee Gstin Status table {}");
		return collecteeGstinStatus;
	}

	/**
	 * 
	 * @param year
	 * @param deductorPan
	 * @param tan
	 * @return
	 */
	public List<CollecteeGstinAndCollecteeDTO> getAllCollecteeGstin(int year, String deductorPan, String tan, int month) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("tan", tan);
		parameters.put("year", year);
		parameters.put("month", month);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_all_collectee_gstin_report")),
				parameters, new CollecteeGstinAndCollecteeDTORowMapper());
	}

	/**
	 * 
	 * @param deductorTan
	 * @param deductorPan
	 * @param month
	 * @param year
	 * @param collecteeId
	 * @return
	 */
	public List<CollecteeGstinStatus> getAllGstinBasedOnCollecteeId(String deductorTan, String deductorPan, int month,
			Integer year, Integer collecteeId) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("deductorTan", deductorTan);
		parameters.put("year", year);
		parameters.put("month", month);
		parameters.put("collecteeId", collecteeId);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_all_collectee_based_on_collecteeId")),
				parameters, new CollecteeGstinStatusRowMapper());
	}

	/**
	 * 
	 * @param collecteeGstinStatus
	 * @return
	 */
	public int collecteeGstinInActive(CollecteeGstinStatus collecteeGstinStatus) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(collecteeGstinStatus);
		int status = namedParameterJdbcTemplate.update(String.format(queries.get("collectee_gstin_inactive")),
				namedParameters);
		logger.info("inActive in collectee Gstin Status table {}");
		return status;

	}

}
