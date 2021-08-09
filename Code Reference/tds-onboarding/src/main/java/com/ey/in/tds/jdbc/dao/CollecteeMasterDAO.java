package com.ey.in.tds.jdbc.dao;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
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

import com.ey.in.tcs.common.domain.transactions.jdbc.rowmapper.PaymentRowMapper;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.TcsPaymentDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.CollecteeDeclarationRateType;
import com.ey.in.tds.common.onboarding.jdbc.dto.CollecteeMaster;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.jdbc.rowmapper.CollecteeDeclarationRateTypeRowMapper;
import com.ey.in.tds.jdbc.rowmapper.CollecteeMasterRowMapper;

/**
 * 
 * @author vamsir
 *
 */
@Repository
public class CollecteeMasterDAO implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private HashMap<String, String> queries;

	@Autowired
	private DataSource dataSource;

	private JdbcTemplate jdbcTemplate;

	private SimpleJdbcInsert simpleJdbcInsert;

	private SimpleJdbcInsert simplePaymentJdbcInsert;

	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@PostConstruct
	private void postConstruct() {
		jdbcTemplate = new JdbcTemplate(dataSource);
		namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("collectee_master")
				.withSchemaName("Client_Masters").usingGeneratedKeyColumns("id");

		simplePaymentJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("tcs_payment")
				.withSchemaName("Transactions").usingGeneratedKeyColumns("id");
	}

	/**
	 * This method for save collectee master data
	 * 
	 * @param collecteeMaster
	 * @return
	 */
	public CollecteeMaster save(CollecteeMaster collecteeMaster) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		logger.info("insert method execution started  {}");
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(collecteeMaster);
		collecteeMaster.setId(simpleJdbcInsert.executeAndReturnKey(namedParameters).intValue());
		logger.info("Record inserted to collectee master table {}");
		return collecteeMaster;
	}

	public TcsPaymentDTO paymentSave(TcsPaymentDTO dto) {
		logger.info("insert method execution started  {}");
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(dto);
		dto.setId(simplePaymentJdbcInsert.executeAndReturnKey(namedParameters).intValue());
		logger.info("Record inserted to ao_master table {}");
		return dto;
	}

	/**
	 * 
	 * @param collectorPan
	 * @param id
	 * @return
	 */
	public List<CollecteeMaster> getCollectee(String collectorPan, Integer id) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("collectorPan", collectorPan);
		parameters.put("id", id);

		return namedParameterJdbcTemplate.query(String.format(queries.get("tcs_find_by_id")), parameters,
				new CollecteeMasterRowMapper());
	}

	public Long getPaymentbyIntialBalance(String collectorPan, Integer year, Integer month, String documentNumber,
			String collectorTan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("collectorPan", collectorPan);
		parameters.put("collectorTan", collectorTan);
		parameters.put("year", year);
		parameters.put("month", month);
		parameters.put("documentNumber", documentNumber);

		return namedParameterJdbcTemplate.queryForObject(
				String.format(queries.get("tcs_find_payment_by_intial_balance")), parameters, Long.class);
	}

	/**
	 * This method for get all collectee master data.
	 * 
	 * @param collectorPan
	 * @param pagination
	 * @return
	 */
	public List<CollecteeMaster> findAllCollectee(String collectorPan, Pagination pagination) {
		String paginationOrder = CommonUtil.getPagination("id", pagination.getPageNumber(), pagination.getPageSize(),
				"DESC");
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("collectorPan", collectorPan);
		String query = String.format(queries.get("tcs_get_all_collectee"));
		query = query.concat(paginationOrder);
		return namedParameterJdbcTemplate.query(query, parameters, new CollecteeMasterRowMapper());

	}

	/**
	 * 
	 * @param collectorPan
	 * @return
	 */
	public BigInteger getAllCollecteeCount(String collectorPan) {
		return jdbcTemplate.queryForObject(String.format(queries.get("get_all_collectee_count")),
				new Object[] { collectorPan }, BigInteger.class);
	}

	/**
	 * This method for update collectee master table.
	 * 
	 * @param collecteeMaster
	 * @return
	 */
	public CollecteeMaster update(CollecteeMaster collecteeMaster) {
		logger.info("DAO method executing to update user data ");
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(collecteeMaster);
		int status = namedParameterJdbcTemplate.update(String.format(queries.get("update_collectee_master_dto")),
				namedParameters);
		if (status != 0) {
			logger.info("collectee master data is updated for ID " + collecteeMaster.getId());
		} else {
			logger.info("No record found with ID " + collecteeMaster.getId());
		}
		logger.info("DAO method execution successful {}");
		return collecteeMaster;
	}

	/**
	 * This method for update collectee master table.
	 * 
	 * @param collecteeMaster
	 * @return
	 */
	public CollecteeMaster updateCollecteeExcel(CollecteeMaster collecteeMaster) {
		logger.info("DAO method executing to update user data ");
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(collecteeMaster);
		int status = namedParameterJdbcTemplate.update(String.format(queries.get("update_collectee_master_excel_data")),
				namedParameters);
		if (status != 0) {
			logger.info("collectee master data is updated for ID " + collecteeMaster.getId());
		} else {
			logger.info("No record found with ID " + collecteeMaster.getId());
		}
		logger.info("DAO method execution successful {}");
		return collecteeMaster;
	}

	/**
	 * 
	 * @param collectorPan
	 * @param pagination
	 * @return
	 */
	public List<CollecteeMaster> findAllByPan(String collectorPan, Pagination pagination) {
		String paginationOrder = CommonUtil.getPagination("id", pagination.getPageNumber(), pagination.getPageSize(),
				"DESC");
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("collectorPan", collectorPan);
		String query = String.format(queries.get("tcs_find_all_by_pan"));
		query = query.concat(paginationOrder);
		return namedParameterJdbcTemplate.query(query, parameters, new CollecteeMasterRowMapper());
	}

	/**
	 * 
	 * @param collectorPan
	 * @param collecteeName
	 * @param pagination
	 * @return
	 */
	public List<CollecteeMaster> findAllByCollecteeNamePan(String collectorPan, String collecteeName,
			Pagination pagination) {
		String paginationOrder = CommonUtil.getPagination("id", pagination.getPageNumber(), pagination.getPageSize(),
				"DESC");
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("collectorPan", collectorPan);
		parameters.put("collecteeName", collecteeName);
		String query = String.format(queries.get("tcs_find_all_by_pan_name"));
		query = query.concat(paginationOrder);
		return namedParameterJdbcTemplate.query(query, parameters, new CollecteeMasterRowMapper());
	}

	public BigInteger getAllCollecteeMasterCount(String collectorPan) {
		return jdbcTemplate.queryForObject(String.format(queries.get("tcs_get_all_collectee_count")),
				new Object[] { collectorPan }, BigInteger.class);
	}

	/**
	 * 
	 * @param collectorPan
	 * @return
	 */
	public List<CollecteeMaster> getCollectorPan(String collectorPan) {
		return jdbcTemplate.query(String.format(queries.get("tcs_get_all_collectee_names_codes")),
				new CollecteeMasterRowMapper(), collectorPan);
	}

	/**
	 * 
	 * @param collectorPan
	 * @param nameOfTheCollectee
	 * @param collecteeCode
	 * @return
	 */
	public List<CollecteeMaster> getColleteeData(String collectorPan, String collecteeCode) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("collectorPan", collectorPan);
		parameters.put("collecteeCode", collecteeCode);
		return namedParameterJdbcTemplate.query(String.format(queries.get("tcs_get_all_collectees")), parameters,
				new CollecteeMasterRowMapper());

	}

	/**
	 * 
	 * @param collectorPan
	 * @return
	 */
	public List<CollecteeMaster> findAllByCollecteePans(String collectorPan) {
		return jdbcTemplate.query(String.format(queries.get("tcs_get_all_collectee_pan")),
				new CollecteeMasterRowMapper(), collectorPan);
	}

	/**
	 * 
	 * @param collectorPan
	 * @param collecteeCode
	 * @param pagination
	 * @return
	 */
	public List<CollecteeMaster> findAllByCollecteeCode(String collectorPan, String collecteeCode,
			Pagination pagination) {
		String paginationOrder = CommonUtil.getPagination("id", pagination.getPageNumber(), pagination.getPageSize(),
				"DESC");
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("collectorPan", collectorPan);
		parameters.put("collecteeCode", collecteeCode);
		String query = String.format(queries.get("tcs_find_all_by_pan_code"));
		query = query.concat(paginationOrder);
		return namedParameterJdbcTemplate.query(query, parameters, new CollecteeMasterRowMapper());
	}

	/**
	 * 
	 * @param collecteeName
	 * @param collectorPan
	 * @param collecteeCode
	 * @param pagination
	 * @return
	 */
	public List<CollecteeMaster> findAllByCollecteeNameAndCode(String collecteeName, String collectorPan,
			String collecteeCode, Pagination pagination) {
		String paginationOrder = CommonUtil.getPagination("id", pagination.getPageNumber(), pagination.getPageSize(),
				"DESC");
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("collectorPan", collectorPan);
		parameters.put("collecteeCode", collecteeCode);
		parameters.put("collecteeName", collecteeName);
		String query = String.format(queries.get("tcs_find_all_by_pan_name_code"));
		query = query.concat(paginationOrder);
		return namedParameterJdbcTemplate.query(query, parameters, new CollecteeMasterRowMapper());
	}

	/**
	 * gets collectee status based on collectee code
	 * 
	 * @param collectorCode
	 * @return
	 */
	public List<String> getCollecteeType(String collecteeCode, String tan, String pan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("collecteeCode", collecteeCode);
		parameters.put("tan", tan);
		parameters.put("pan", pan);

		return namedParameterJdbcTemplate.queryForList(String.format(queries.get("tcs_get_collectee_type")), parameters,
				String.class);
	}

	/**
	 * 
	 * @param collectorPan
	 * @param keyenetered
	 * @return
	 */
	public List<CollecteeMaster> getCollecteeMasterBasedOnKeyEnteredName(String collectorPan, String keyenetered) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("collectorPan", collectorPan);
		String query = String.format(queries.get("tcs_collectee_based_on_name_and_code"));
		query = query.concat("AND name_of_the_collectee LIKE '%" + keyenetered + "%' ");
		query = query.concat("ORDER BY name_of_the_collectee");
		return namedParameterJdbcTemplate.query(query, parameters, new CollecteeMasterRowMapper());

	}

	/**
	 * 
	 * @param collectorPan
	 * @param keyenetered
	 * @return
	 */
	public List<CollecteeMaster> getCollecteeMasterBasedOnKeyEnteredCode(String collectorPan, String keyenetered) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("collectorPan", collectorPan);
		String query = String.format(queries.get("tcs_collectee_based_on_name_and_code"));
		query = query.concat("AND collectee_code LIKE '%" + keyenetered + "%' ");
		query = query.concat("ORDER BY collectee_code");
		return namedParameterJdbcTemplate.query(query, parameters, new CollecteeMasterRowMapper());

	}

	/**
	 * 
	 * @param assessmentYear
	 * @param collecteeCode
	 * @return
	 */
	public List<TcsPaymentDTO> findPaymentByCollecteeCode(int assessmentYear, String collecteeCode) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("year", assessmentYear);
		parameters.put("collecteeCode", collecteeCode);
		return namedParameterJdbcTemplate.query(String.format(queries.get("find_payment_by_collectee_code")),
				parameters, new PaymentRowMapper());
	}

	/**
	 * 
	 * @param lccMasterPan
	 * @param deductorPan
	 * @return
	 */
	public Double getCollecteeBasedOnPanAndDeductorPan(String lccMasterPan, String deductorPan) {
		return jdbcTemplate.queryForObject(String.format(queries.get("tcs_collectee_count")),
				new Object[] { deductorPan, lccMasterPan }, Double.class);
	}

	/**
	 * 
	 * @param deductorPan
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public long countCollecteeMasterPanStatusValid(String collectorPan, String startDate, String endDate) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("collectorPan", collectorPan);
		parameters.put("startDate", startDate);
		parameters.put("endDate", endDate);
		return namedParameterJdbcTemplate.queryForObject(queries.get("collectee_pan_status_valid_count"), parameters,
				Long.class);
	}

	/**
	 * 
	 * @param deductorPan
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public long countCollecteeMasterPanStatusInValid(String collectorPan, String startDate, String endDate) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("collectorPan", collectorPan);
		parameters.put("startDate", startDate);
		parameters.put("endDate", endDate);
		return namedParameterJdbcTemplate.queryForObject(queries.get("collectee_pan_status_invalid_count"), parameters,
				Long.class);
	}

	public Integer getCountByCollecteeCodeAndPan(String collecteeCode, String collecteePan, String collectorPan) {
		logger.info("Fetching collectee record count with collectee code and collectee pan as {}" + collecteeCode + ","
				+ collecteePan);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("collectorPan", collectorPan);
		parameters.put("collecteeCode", collecteeCode);
		parameters.put("collecteePan", collecteePan);
		return namedParameterJdbcTemplate.queryForObject(
				queries.get("collectee_count_by_collecteeCode_collecteePan_and_collectorPan"), parameters,
				Integer.class);

	}

	/**
	 * 
	 * @param deductorPan
	 * @return
	 */
	public List<CollecteeMaster> getAllCollectees(String collectorPan, String collectorTan) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("collectorPan", collectorPan);
		parameters.put("collectorTan", collectorTan);
		String query = String.format(queries.get("tcs_find_all_by_pan_tan"));
		return namedParameterJdbcTemplate.query(query, parameters, new CollecteeMasterRowMapper());
	}

	/**
	 * 
	 * @param collectorTan
	 * @param collectorPan
	 * @param collecteeCodes
	 * @return
	 */
	public List<CollecteeMaster> getAllCollecteeByCodes(String collectorPan, String collecteeCode) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("collectorPan", collectorPan);
		parameters.put("collecteeCode", collecteeCode);
		return namedParameterJdbcTemplate.query(queries.get("tcs_get_all_collectee_by_codes"), parameters,
				new CollecteeMasterRowMapper());
	}

	/**
	 * 
	 * @param collectorTan
	 * @param collectorPan
	 * @param collecteeCodes
	 * @return
	 */
	public List<CollecteeMaster> getAllCollecteeByPan(String collectorPan, String collecteePan) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("collectorPan", collectorPan);
		parameters.put("collecteePan", collecteePan);
		return namedParameterJdbcTemplate.query(queries.get("tcs_get_all_collectee_by_pan"), parameters,
				new CollecteeMasterRowMapper());
	}

	/**
	 * @param collectorPan
	 * @param collecteePan
	 * @return
	 */
	public List<CollecteeDeclarationRateType> getAllCollecteeByDeclaration(String collectorPan, Integer year,
			Integer month) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("collectorPan", collectorPan);
		parameters.put("currentDate", new Date());
		parameters.put("year", year);
		parameters.put("month", month-1);
		return namedParameterJdbcTemplate.query(queries.get("get_collectees_by_declaration"), parameters,
				new CollecteeDeclarationRateTypeRowMapper());
	}

	/**
	 * 
	 * @param collectorPan
	 * @return
	 */
	public List<CollecteeMaster> getCollectorNoPan(String collectorPan) {
		return jdbcTemplate.query(String.format(queries.get("tcs_get_all_collectee_no_pan")),
				new CollecteeMasterRowMapper(), collectorPan);
	}

	/**
	 * 
	 * @param deductorPan
	 * @return
	 */
	public List<CollecteeMaster> getAllCollecteeGstin(String collectorPan, String collectorTan) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("collectorPan", collectorPan);
		parameters.put("collectorTan", collectorTan);
		return namedParameterJdbcTemplate.query(queries.get("get_all_collectee_gstin"), parameters,
				new CollecteeMasterRowMapper());

	}

}
