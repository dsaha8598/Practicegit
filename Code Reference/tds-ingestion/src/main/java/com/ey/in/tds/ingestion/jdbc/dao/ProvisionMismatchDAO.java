package com.ey.in.tds.ingestion.jdbc.dao;

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
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.ProvisionDTO;
import com.ey.in.tds.common.domain.transactions.jdbc.rowmapper.ProvisionRowMapper;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.core.util.CommonUtil;

/**
 * 
 * @author scriptbees.
 *
 */
@Repository
public class ProvisionMismatchDAO implements Serializable {

	private static final long serialVersionUID = 1L;

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
		simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("provision").withSchemaName("Transactions")
				.usingGeneratedKeyColumns("provision_id");

	}
	
	/**
	 * 
	 * @param tan
	 * @param type
	 * @return
	 */
	public BigInteger getAdhocCount(String tan, String type) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("tan", tan);
		parameters.put("type", type);
		return namedParameterJdbcTemplate.queryForObject(String.format(queries.get("get_adhoc_count")), parameters, BigInteger.class);

	}
	/**
	 * 
	 * @param tan
	 * @param type
	 * @param pagination
	 * @return
	 */
	public List<ProvisionDTO> getAdhocList(String tan, String type, Pagination pagination) {
		String paginationOrder = CommonUtil.getPagination("provision_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("tan", tan);
		parameters.put("type", type);
		String query = String.format(queries.get("get_adhoc_list"));
		query = query.concat(paginationOrder);
		return namedParameterJdbcTemplate.query(query, parameters, new ProvisionRowMapper());

	}

}
