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
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.transactions.jdbc.dto.TcsGstPurchaseRegister;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.TcsGstPurchaseRegisterReportDTO;
import com.ey.in.tds.ingestion.jdbc.rowmapper.TcsGstPurchaseRegisterReportRowMapper;
import com.ey.in.tds.ingestion.jdbc.rowmapper.TcsGstPurchaseRegisterRowMapper;

/**
 * 
 * @author vamsir
 *
 */
@Repository
public class TcsGstPurchaseRegisterDAO implements Serializable {

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
		simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("tcs_gst_purchase_register")
				.withSchemaName("Transactions").usingGeneratedKeyColumns("id");

	}

	/**
	 * 
	 * @param tcsPr
	 */
	public TcsGstPurchaseRegister save(TcsGstPurchaseRegister tcsPr) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(tcsPr);
		tcsPr.setId(simpleJdbcInsert.executeAndReturnKey(namedParameters).intValue());
		logger.info("Record inserted to tcs gst purchase Register table {}");
		return tcsPr;
	}

	/**
	 * 
	 * @param collectorTan
	 * @param assesmentYear
	 * @param collectorPan
	 * @param isMatched
	 * @return
	 */
	public List<TcsGstPurchaseRegister> getGstPurchasedData(String collectorTan, Integer assesmentYear,
			String collectorPan, boolean isMatched) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assesmentYear", assesmentYear);
		parameters.put("collectorPan", collectorPan);
		parameters.put("collectorTan", collectorTan);
		parameters.put("isMatched", isMatched == false ? 0 : 1);
		return namedParameterJdbcTemplate.query(String.format(tcsQueries.get("get_tcs_pr_data")), parameters,
				new TcsGstPurchaseRegisterRowMapper());

	}

	/**
	 * 
	 * @param collectorTan
	 * @param assesmentYear
	 * @param collectorPan
	 * @param isMatched
	 * @return
	 */
	public List<TcsGstPurchaseRegisterReportDTO> getGstPurchasedAndRlData(String collectorTan, Integer assesmentYear,
			String collectorPan) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("year", assesmentYear);
		parameters.put("collectorPan", collectorPan);
		parameters.put("collectorTan", collectorTan);
		return namedParameterJdbcTemplate.query(String.format(tcsQueries.get("get_tcs_pr_rl_matched_records")), parameters,
				new TcsGstPurchaseRegisterReportRowMapper());

	}

	/**
	 * 
	 * @param assesmentYear
	 * @param collectorTan
	 * @param collectorPan
	 */
	public void USP26ASReconciliationForm(Integer assesmentYear, String collectorTan, String collectorPan) {
		SimpleJdbcCall jdbcCallInvoice = new SimpleJdbcCall(dataSource).withProcedureName("USP_26AS_reconcilation");
		SqlParameterSource in = new MapSqlParameterSource().addValue("assessment_year", assesmentYear)
				.addValue("collectorPan", collectorPan).addValue("collectorTan", collectorTan);
		Map<String, Object> outInvoice = jdbcCallInvoice.execute(in);
		logger.info("Status of 26A Reconcilation: " + outInvoice);
	}

}
