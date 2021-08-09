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

import com.ey.in.tcs.common.domain.transactions.jdbc.rowmapper.DeducteeNopGroupRowMapper;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeducteeNopGroup;

/**
 * 
 * @author vamsir
 *
 */
@Repository
public class DeducteeNopGroupDAO {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired(required = false)
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
		simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("deductee_nop_group")
				.withSchemaName("Transactions").usingGeneratedKeyColumns("id");
	}

	/**
	 * This method for save deductee noi data
	 * 
	 * @param DeducteeNopGroup
	 * @return
	 */
	public DeducteeNopGroup save(DeducteeNopGroup deducteeNopGroup) {
		String tenantId = MultiTenantContext.getTenantId();
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(deducteeNopGroup);
		deducteeNopGroup.setId(simpleJdbcInsert.executeAndReturnKey(namedParameters).intValue());
		return deducteeNopGroup;
	}

	/**
	 * 
	 * @param deducteeNopGropu
	 * @return
	 */
	public DeducteeNopGroup update(DeducteeNopGroup deducteeNopGropu) {
		logger.info("DAO method executing to update user data ");
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(deducteeNopGropu);
		int status = namedParameterJdbcTemplate
				.update(String.format(queries.get("update_deductee_noi_threshold_ledger")), namedParameters);
		if (status != 0) {
			logger.info("deductee noi data is updated for ID " + deducteeNopGropu.getId());
		} else {
			logger.info("No record found with ID " + deducteeNopGropu.getId());
		}
		logger.info("DAO method execution successful {}");
		return deducteeNopGropu;
	}

	/**
	 * 
	 * @param deducteeKey
	 * @param deductorPan
	 * @param deducteePan
	 * @param year
	 * @return
	 */
	public List<DeducteeNopGroup> findByDeducteeKeyOrDeducteePan(String deducteeKey, String deductorPan,
			String deducteePan, Integer year) {
		String query = String.format(queries.get("find_noi_by_code_or_deductee_pan"));
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deducteeKey", deducteeKey);
		parameters.put("deductorPan", deductorPan);
		parameters.put("deducteePan", deducteePan);
		parameters.put("year", year);
		return namedParameterJdbcTemplate.query(query, parameters, new DeducteeNopGroupRowMapper());
	}

	/**
	 * 
	 * @param deducteePan
	 * @param deductorPancollectee_pan
	 * @return
	 */
	public List<DeducteeNopGroup> findByDeducteePan(String deducteePan, String deductorPan) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("deducteePan", deducteePan);
		parameters.put("deductorPan", deductorPan);
		String query = String.format(queries.get("get_deductee_nop_by_pans"));
		return namedParameterJdbcTemplate.query(query, parameters, new DeducteeNopGroupRowMapper());
	}

	/**
	 * 
	 * @param deducteeNopGroup
	 */
	public void updateByDeducteePan(DeducteeNopGroup deducteeNopGroup) {
		logger.info("DAO method executing to update user data ");
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(deducteeNopGroup);
		int status = namedParameterJdbcTemplate.update(String.format(queries.get("update_deductee_nop_group_by_pan")),
				namedParameters);
		if (status != 0) {
			logger.info("deductee nop group is updated for ID :{} ", deducteeNopGroup.getId());
		} else {
			logger.info("No record found with ID :{} ", deducteeNopGroup.getId());
		}
		logger.info("DAO method execution successful {}");

	}

	/**
	 * 
	 * @param deducteeNopGroup
	 */
	public void updateByDeducteeKey(DeducteeNopGroup deducteeNopGroup) {
		logger.info("DAO method executing to update user data ");
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(deducteeNopGroup);
		int status = namedParameterJdbcTemplate.update(String.format(queries.get("update_deductee_nop_group_by_key")),
				namedParameters);
		if (status != 0) {
			logger.info("deductee nop group is updated for ID:{} ", deducteeNopGroup.getId());
		} else {
			logger.info("No record found with ID: {} ", deducteeNopGroup.getId());
		}
		logger.info("DAO method execution successful {}");

	}

}
