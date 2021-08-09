package com.ey.in.tds.jdbc.dao;

import java.io.Serializable;
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
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.dto.onboarding.deductee.DeducteeDeclaration;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeducteeMasterResidential;
import com.ey.in.tds.jdbc.rowmapper.DeducteeDeclarationRowMapper;

/**
 * 
 * @author vamsir
 *
 */
@Repository
public class DeducteeDeclarationDAO implements Serializable {

	private static final long serialVersionUID = 8210090110701968786L;

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
		simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("deductee_declaration")
				.withSchemaName("Onboarding").usingGeneratedKeyColumns("id");
	}

	/**
	 * This method for save deductee Declaration data
	 * 
	 * @param collecteeMaster
	 * @return
	 */
	public DeducteeDeclaration save(DeducteeDeclaration deducteeDeclaration) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		logger.info("insert method execution started  {}");
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(deducteeDeclaration);
		deducteeDeclaration.setId(simpleJdbcInsert.executeAndReturnKey(namedParameters).intValue());
		logger.info("Record inserted to deductee Declaration table {}");
		return deducteeDeclaration;
	}

	/**
	 * This method for batch save.
	 * 
	 * @param deducteeDeclarationList
	 * @param tenantId
	 */
	@Transactional
	public void batchSaveDeducteeDeclaration(List<DeducteeDeclaration> deducteeList, String tenantId) {
		String query = " INSERT INTO Onboarding.deductee_declaration (deductee_id, deductee_code, rate_type, tds_or_tcs, applicable_from, "
				+ "applicable_to ,[year], deductor_pan, deductor_tan, active, created_date, modified_date, created_by, modified_by, specified_person) "
				+ "VALUES(:deducteeId, :deducteeCode, :rateType, :tdsOrTcs, :applicableFrom, :applicableTo, :year, "
				+ ":deductorPan, :deductorTan, :active, :createdDate, :modifiedDate, :createdBy, :modifiedBy, :specifiedPerson)";
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(deducteeList);
		namedParameterJdbcTemplate.batchUpdate(query, batch);
		logger.info("deductee declaration inserted size is {}", deducteeList.size());
	}

	/**
	 * This method for get all records
	 * 
	 * @param deductorPan
	 * @param deductorTan
	 * @param year
	 * @return
	 */
	public List<DeducteeDeclaration> findAll(String deductorPan, String deductorTan, int year) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("deductorTan", deductorTan);
		parameters.put("year", year);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_all_tds_declaration")), parameters,
				new DeducteeDeclarationRowMapper());
	}

	/**
	 * 
	 * @param deductorPan
	 * @param deductorTan
	 * @param year
	 * @return
	 */
	public List<DeducteeDeclaration> getAllDeducteeDeclarationByCode(String deductorPan, String deductorTan,
			int deducteeId, int year) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("deductorTan", deductorTan);
		parameters.put("deducteeId", deducteeId);
		parameters.put("year", year);
		parameters.put("currentDate", new Date());
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_all_tds_declaration_by_deducteeId")),
				parameters, new DeducteeDeclarationRowMapper());
	}

	/**
	 * 
	 * @param kycDetailsList
	 */
	@org.springframework.transaction.annotation.Transactional
	public void batchUpdateApplicableTo(List<DeducteeDeclaration> deducteeDeclarationList) {
		String updateQuery = "UPDATE Onboarding.deductee_declaration SET applicable_to = :applicableTo, modified_date = :modifiedDate, "
				+ "modified_by = :modifiedBy, active = :active WHERE id = :id";
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(deducteeDeclarationList);
		namedParameterJdbcTemplate.batchUpdate(updateQuery, batch);
		logger.info("deductee declaration batch updated successfully {}", deducteeDeclarationList.size());
	}

	/**
	 * 
	 * @param deducteeList
	 */
	@org.springframework.transaction.annotation.Transactional
	public void batchUpdateDeducteePanAadhaarLinkStatus(List<DeducteeMasterResidential> deducteeList) {
		String updateQuery = "UPDATE Client_Masters.deductee_master_residential SET pan_aadhaar_link_status = :panAadhaarLinkStatus, "
				+ "modified_by = :modifiedBy, modified_date = :modifiedDate, pan_status = :panStatus, name_as_per_traces = :nameAsPerTraces, "
				+ "pan_verified_date = :panVerifiedDate, pan_allotment_date = :panAllotmentDate WHERE deductee_master_id = :deducteeMasterId ";
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(deducteeList);
		namedParameterJdbcTemplate.batchUpdate(updateQuery, batch);
		logger.info("deductee batch updated successfully {}", deducteeList.size());
	}

}
