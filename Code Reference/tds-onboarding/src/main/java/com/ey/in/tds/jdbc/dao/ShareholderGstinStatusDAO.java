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
import com.ey.in.tds.common.domain.dividend.ShareholderGstinStatus;
import com.ey.in.tds.jdbc.rowmapper.ShareholderGstinStatusRowMapper;

/**
 * 
 * @author vamsir
 *
 */
@Repository
public class ShareholderGstinStatusDAO {

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
		simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("shareholder_gstin_status")
				.withSchemaName("Client_Masters").usingGeneratedKeyColumns("id");
	}

	/**
	 * This method for save ShareholderGstinStatus
	 * 
	 * @param ShareholderGstinStatus
	 * @return
	 */
	public ShareholderGstinStatus save(ShareholderGstinStatus shareholderGstinStatus) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(shareholderGstinStatus);
		shareholderGstinStatus.setId(simpleJdbcInsert.executeAndReturnKey(namedParameters).intValue());
		logger.info("Record inserted to shareholder Gstin Status table {}");
		return shareholderGstinStatus;
	}

	/**
	 * 
	 * @param year
	 * @param deductorPan
	 * @param tan
	 * @return
	 */
	public List<ShareholderGstinStatus> getAllShareholderGstin(int year, String deductorPan, String tan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("tan", tan);
		parameters.put("year", year);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_all_shareholder_gstin_report")),
				parameters, new ShareholderGstinStatusRowMapper());
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
	public List<ShareholderGstinStatus> getAllGstinBasedOnShareholderId(String deductorTan, String deductorPan,
			int month, Integer year, Integer shareholderId) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("deductorTan", deductorTan);
		parameters.put("year", year);
		parameters.put("month", month);
		parameters.put("shareholderId", shareholderId);
		return namedParameterJdbcTemplate.query(
				String.format(queries.get("get_all_shareholder_based_on_shareholderId")), parameters,
				new ShareholderGstinStatusRowMapper());
	}

	/**
	 * 
	 * @param shareholderGstinStatus
	 * @return
	 */
	public int shareholderGstinInActive(ShareholderGstinStatus shareholderGstinStatus) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(shareholderGstinStatus);
		int status = namedParameterJdbcTemplate.update(String.format(queries.get("shareholder_gstin_inactive")),
				namedParameters);
		return status;

	}
	
}
