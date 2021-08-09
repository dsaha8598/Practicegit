package com.ey.in.tds.ingestion.tcs.dao;

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

import com.ey.in.tcs.common.domain.transactions.jdbc.rowmapper.PaymentUtilizationRowMapper;
import com.ey.in.tcs.common.model.payment.TcsPaymentUtilization;
import com.ey.in.tds.common.config.MultiTenantContext;
/**
 * 
 * @author Scriptbees.
 *
 */
@Repository
public class PaymentUtilizationDAO implements Serializable {

	
	private static final long serialVersionUID = 1L;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired(required = false)
	private HashMap<String, String> tcsQueries;

	private JdbcTemplate jdbcTemplate;

	private SimpleJdbcInsert simpleJdbcInsert;

	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	private DataSource dataSource;

	@PostConstruct
	private void postConstruct() {
		jdbcTemplate = new JdbcTemplate(dataSource);
		namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("payment_utilization").withSchemaName("Transactions")
				.usingGeneratedKeyColumns("id");

	}
	
	public List<TcsPaymentUtilization> getPaymentAdjustment(String collectorTan, int assessmentYear, int month) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("collectorTan", collectorTan);
		parameters.put("year", assessmentYear);
		parameters.put("month", month);

		return namedParameterJdbcTemplate.query(String.format(tcsQueries.get("get_payment_adjusted")), parameters,
				new PaymentUtilizationRowMapper());

	}

}
