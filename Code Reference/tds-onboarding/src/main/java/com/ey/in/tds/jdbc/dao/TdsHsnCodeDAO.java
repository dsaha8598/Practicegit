package com.ey.in.tds.jdbc.dao;

import java.io.Serializable;
import java.math.BigInteger;
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
import com.ey.in.tds.common.onboarding.jdbc.dto.TdsHsnCode;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.jdbc.rowmapper.TdsHsnCodeRowMapper;

@Repository
public class TdsHsnCodeDAO implements Serializable {

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
		simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("tds_hsn_code")
				.withSchemaName("Client_Masters").usingGeneratedKeyColumns("id");
	}

	/**
	 * 
	 * @param tdsHsnCode
	 * @return
	 */
	public TdsHsnCode save(TdsHsnCode tdsHsnCode) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(tdsHsnCode);
		tdsHsnCode.setId(simpleJdbcInsert.executeAndReturnKey(namedParameters).intValue());
		return tdsHsnCode;
	}

	/**
	 * 
	 * @param pagination
	 * @param deductorPan
	 * @return
	 */
	public List<TdsHsnCode> getAllHsnCode(Pagination pagination, String deductorPan, String tan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String paginationOrder = CommonUtil.getPagination("id", pagination.getPageNumber(), pagination.getPageSize(),
				"DESC");
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("tan", tan);
		String query = String.format(queries.get("get_all_hsn_code"));
		query = query.concat(paginationOrder);
		return namedParameterJdbcTemplate.query(query, parameters, new TdsHsnCodeRowMapper());

	}

	/**
	 * 
	 * @param deductorPan
	 * @return
	 */
	public BigInteger getAllHsnCodeCount(String deductorPan, String tan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("tan", tan);
		return namedParameterJdbcTemplate.queryForObject(String.format(queries.get("get_all_hsn_code_count")),
				parameters, BigInteger.class);

	}

	/**
	 * 
	 * @param tan
	 * @param deductorPan
	 * @return
	 */
	public List<TdsHsnCode> getHsnList(Long hsn, String deductorPan, String tan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("hsn", hsn);
		parameters.put("tan", tan);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_hsn_code")), parameters,
				new TdsHsnCodeRowMapper());

	}

	/**
	 * 
	 * @param id
	 * @param deductorPan
	 * @param tan
	 * @return
	 */
	public List<TdsHsnCode> getById(Integer id, String deductorPan, String tan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("tan", tan);
		parameters.put("id", id);
		return namedParameterJdbcTemplate.query(String.format(queries.get("fine_by_hsn_id")), parameters,
				new TdsHsnCodeRowMapper());

	}

	/**
	 * 
	 * @param hsnList
	 */
	public int update(TdsHsnCode hsnList) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(hsnList);
		int status = namedParameterJdbcTemplate.update(String.format(queries.get("update_hsn")), namedParameters);
		if (status != 0) {
			logger.info("hsn code data is updated for ID " + hsnList.getId());
		} else {
			logger.info("No record found with ID " + hsnList.getId());
		}
		return status;
	}

	/**
	 * 
	 * @param deductorPan
	 * @param tan
	 * @return
	 */
	public List<String> getAllSections(String deductorPan, String tan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("tan", tan);
		return namedParameterJdbcTemplate.queryForList(String.format(queries.get("get_all_hsn_tds_section")),
				parameters, String.class);
	}

	/**
	 * 
	 * @param tdsSection
	 * @param deductorPan
	 * @param tan
	 * @return
	 */
	public List<TdsHsnCode> fetchHsnByTdsSecion(String tdsSection, String deductorPan, String tan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("tan", tan);
		parameters.put("tdsSection", tdsSection);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_all_hsn_by_section")), parameters,
				new TdsHsnCodeRowMapper());
	}

}
