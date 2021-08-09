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
import com.ey.in.tds.common.onboarding.jdbc.dto.ShareholderDeclaration;
import com.ey.in.tds.common.onboarding.jdbc.dto.ShareholderMasterResidential;
import com.ey.in.tds.jdbc.rowmapper.ShareholderDeclarationRowMapper;

/**
 * 
 * @author vamsir
 *
 */
@Repository
public class ShareholderDeclarationDAO implements Serializable {

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
		simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("shareholder_declaration")
				.withSchemaName("Onboarding").usingGeneratedKeyColumns("id");
	}

	/**
	 * This method for save shareholder Declaration data
	 * 
	 * @param collecteeMaster
	 * @return
	 */
	public ShareholderDeclaration save(ShareholderDeclaration shareholderDeclaration) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		logger.info("insert method execution started  {}");
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(shareholderDeclaration);
		shareholderDeclaration.setId(simpleJdbcInsert.executeAndReturnKey(namedParameters).intValue());
		logger.info("Record inserted to shareholder Declaration table {}");
		return shareholderDeclaration;
	}

	/**
	 * This method for batch save.
	 * 
	 * @param shareholderDeclarationList
	 * @param tenantId
	 */
	@Transactional
	public void batchSaveShareholderDeclaration(List<ShareholderDeclaration> shareholderList, String tenantId) {
		String query = " INSERT INTO Onboarding.shareholder_declaration (shareholder_id, shareholder_code, rate_type, tds_or_tcs, applicable_from, "
				+ "applicable_to ,[year], deductor_pan, deductor_tan, active, created_date, modified_date, created_by, modified_by, specified_person) "
				+ "VALUES(:shareholderId, :shareholderCode, :rateType, :tdsOrTcs, :applicableFrom, :applicableTo, :year, "
				+ ":deductorPan, :deductorTan, :active, :createdDate, :modifiedDate, :createdBy, :modifiedBy, :specifiedPerson)";
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(shareholderList);
		namedParameterJdbcTemplate.batchUpdate(query, batch);
		logger.info("Shareholder Declaration inserted size is {}", shareholderList.size());
	}

	/**
	 * This method for get all records
	 * 
	 * @param deductorPan
	 * @param deductorTan
	 * @param year
	 * @return
	 */
	public List<ShareholderDeclaration> findAll(String deductorPan, String deductorTan, int year) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("deductorTan", deductorTan);
		parameters.put("year", year);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_all_shareholder_declaration")),
				parameters, new ShareholderDeclarationRowMapper());
	}

	/**
	 * 
	 * @param deductorPan
	 * @param deductorTan
	 * @param year
	 * @return
	 */
	public List<ShareholderDeclaration> getAllShareholderDeclarationByCode(String deductorPan, String deductorTan,
			int shareholderId, int year) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("deductorTan", deductorTan);
		parameters.put("shareholderId", shareholderId);
		parameters.put("year", year);
		parameters.put("currentDate", new Date());
		return namedParameterJdbcTemplate.query(
				String.format(queries.get("get_all_shareholder_declaration_shareholderId")), parameters,
				new ShareholderDeclarationRowMapper());
	}

	/**
	 * 
	 * @param kycDetailsList
	 */
	@org.springframework.transaction.annotation.Transactional
	public void batchUpdateApplicableTo(List<ShareholderDeclaration> shareholderDeclarationList) {
		String updateQuery = "UPDATE Onboarding.shareholder_declaration SET applicable_to = :applicableTo, "
				+ "modified_date = :modifiedDate, modified_by = :modifiedBy, active = :active WHERE id = :id";
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(shareholderDeclarationList);
		namedParameterJdbcTemplate.batchUpdate(updateQuery, batch);
		logger.info("shareholder Declaration batch updated successfully {}", shareholderDeclarationList.size());
	}

	/**
	 * 
	 * @param shareholderList
	 */
	@org.springframework.transaction.annotation.Transactional
	public void batchUpdateShareholderPanAadhaarLinkStatus(List<ShareholderMasterResidential> shareholderList) {
		String updateQuery = "UPDATE Client_Masters.shareholder_master_residential SET pan_aadhaar_link_status = :panAadhaarLinkStatus, "
				+ "modified_by = :modifiedBy, modified_date = :modifiedDate, name_as_per_traces = :nameAsPerTraces , pan_status = :panStatus, "
				+ "pan_verified_date = :panVerifiedDate, pan_allotment_date = :panAllotmentDate WHERE shareholder_master_id = :id ";
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(shareholderList);
		namedParameterJdbcTemplate.batchUpdate(updateQuery, batch);
		logger.info("shareholder master batch updated successfully {}", shareholderList.size());
	}

}
