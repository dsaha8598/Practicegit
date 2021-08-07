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

import com.ey.in.tcs.common.domain.CollecteeDeclaration;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.onboarding.jdbc.dto.CollecteeMaster;
import com.ey.in.tds.jdbc.rowmapper.CollecteeDeclarationRowMapper;

/**
 * 
 * @author vamsir
 *
 */
@Repository
public class CollecteeDeclarationDAO implements Serializable {

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
		simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("collectee_declaration")
				.withSchemaName("Onboarding").usingGeneratedKeyColumns("id");
	}

	/**
	 * This method for save collectee Declaration data
	 * 
	 * @param collecteeMaster
	 * @return
	 */
	public CollecteeDeclaration save(CollecteeDeclaration collecteeDeclaration) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		logger.info("insert method execution started  {}");
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(collecteeDeclaration);
		collecteeDeclaration.setId(simpleJdbcInsert.executeAndReturnKey(namedParameters).intValue());
		logger.info("Record inserted to collectee Declaration table {}");
		return collecteeDeclaration;
	}

	/**
	 * This method for batch save.
	 * 
	 * @param collecteeList
	 * @param tenantId
	 */
	@Transactional
	public void batchSaveCollecteeDeclaration(List<CollecteeDeclaration> collecteeList, String tenantId) {
		String query = " INSERT INTO Onboarding.collectee_declaration (collectee_id, collectee_code, rate_type, tds_or_tcs, applicable_from, "
				+ "applicable_to ,[year], deductor_pan, deductor_tan, active, created_date, modified_date, created_by, modified_by, specified_person) "
				+ "VALUES(:collecteeId, :collecteeCode, :rateType, :tdsOrTcs, :applicableFrom, :applicableTo, :year, "
				+ ":deductorPan, :deductorTan, :active, :createdDate, :modifiedDate, :createdBy, :modifiedBy, :specifiedPerson)";
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(collecteeList);
		namedParameterJdbcTemplate.batchUpdate(query, batch);
		logger.info("collectee declaration inserted size is {}", collecteeList.size());
	}

	/**
	 * This method for get all recodes.
	 * 
	 * @param deductorPan
	 * @param deductorTan
	 * @param year
	 * @return
	 */
	public List<CollecteeDeclaration> findAll(String deductorPan, String deductorTan, int year) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("deductorTan", deductorTan);
		parameters.put("year", year);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_all_tcs_declaration")), parameters,
				new CollecteeDeclarationRowMapper());
	}

	/**
	 * 
	 * @param deductorPan
	 * @param deductorTan
	 * @param year
	 * @return
	 */
	public List<CollecteeDeclaration> getAllCollecteeDeclarationByCode(String deductorPan, String deductorTan,
			int collecteeId, int year) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("deductorTan", deductorTan);
		parameters.put("collecteeId", collecteeId);
		parameters.put("year", year);
		parameters.put("currentDate", new Date());
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_all_tcs_declaration_by_colecteeId")),
				parameters, new CollecteeDeclarationRowMapper());
	}

	/**
	 * 
	 * @param kycDetailsList
	 */
	@org.springframework.transaction.annotation.Transactional
	public void batchUpdateApplicableTo(List<CollecteeDeclaration> collecteeDeclarationList) {
		String updateQuery = "UPDATE Onboarding.collectee_declaration SET applicable_to = :applicableTo, "
				+ "modified_date = :modifiedDate, modified_by = :modifiedBy, active = :active WHERE id = :id";
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(collecteeDeclarationList);
		namedParameterJdbcTemplate.batchUpdate(updateQuery, batch);
		logger.info("collectee declaration batch updated successfully {}", collecteeDeclarationList.size());
	}

	/**
	 * 
	 * @param collecteeList
	 */
	@org.springframework.transaction.annotation.Transactional
	public void batchUpdateCollecteePanAadhaarLinkStatus(List<CollecteeMaster> collecteeList) {
		String updateQuery = "UPDATE Client_Masters.collectee_master SET pan_aadhaar_link_status = :panAadhaarLinkStatus, "
				+ "modified_by = :modifiedBy, modified_date = :modifiedDate, pan_verification_date = :panVerificationDate, pan_verify_status = :panVerifyStatus,"
				+ "name_as_per_traces = :nameAsPerTraces, pan_allotment_date = :panAllotmentDate WHERE id = :id ";
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(collecteeList);
		namedParameterJdbcTemplate.batchUpdate(updateQuery, batch);
		logger.info("collectee master batch updated successfully {}", collecteeList.size());
	}

}
