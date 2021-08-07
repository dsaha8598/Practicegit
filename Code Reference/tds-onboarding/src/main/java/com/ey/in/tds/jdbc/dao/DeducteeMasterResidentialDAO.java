package com.ey.in.tds.jdbc.dao;

import java.io.Serializable;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.AdvanceDTO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.ProvisionDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeducteeDeclarationRateType;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeducteeMasterResidential;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeducteeNopGroup;
import com.ey.in.tds.common.repository.common.PagedData;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.jdbc.rowmapper.DeducteeDeclarationRateTypeRowMapper;
import com.ey.in.tds.jdbc.rowmapper.DeducteeMasterResidentialRowMapper;
import com.ey.in.tds.onboarding.dto.deductee.CustomDeducteesDTO;

/**
 * 
 * @author scriptbees.
 *
 */
@Repository
public class DeducteeMasterResidentialDAO implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private HashMap<String, String> queries;

	@Autowired
	private DataSource dataSource;
	private JdbcTemplate jdbcTemplate;
	private SimpleJdbcInsert simpleJdbcInsert;
	private SimpleJdbcInsert advanceJdbcInsert;
	private SimpleJdbcInsert provisionJdbcInsert;
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@PostConstruct
	private void postConstruct() {
		jdbcTemplate = new JdbcTemplate(dataSource);
		namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("deductee_master_residential")
				.withSchemaName("Client_Masters").usingGeneratedKeyColumns("deductee_master_id");

		advanceJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("advance").withSchemaName("Transactions")
				.usingGeneratedKeyColumns("advance_id");

		provisionJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("provision").withSchemaName("Transactions")
				.usingGeneratedKeyColumns("provision_id");
	}

	/**
	 * 
	 * @param deductorPan
	 * @param deducteeModifiedName
	 * @return
	 */
	public List<DeducteeMasterResidential> findAllByModifiedDeducteeNamePan(String deductorPan,
			String deducteeModifiedName) {
		return jdbcTemplate.query(String.format(queries.get("find_all_by_deductor_pan_and_name")),
				new Object[] { deductorPan, deducteeModifiedName }, new DeducteeMasterResidentialRowMapper());
	}

	public List<DeducteeMasterResidential> findAllByDeducteeKey(String deductorPan, String deducteeKey) {
		return jdbcTemplate.query(String.format(queries.get("find_res_deductee_by_deductee_key")),
				new Object[] { deductorPan, deducteeKey }, new DeducteeMasterResidentialRowMapper());
	}

	/**
	 * 
	 * @param deductorPan
	 * @param pagination
	 * @return
	 */
	public List<DeducteeMasterResidential> findAllByPan(String deductorPan, Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String paginationOrder = CommonUtil.getPagination("deductee_master_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("deductorPan", deductorPan);
		String query = String.format(queries.get("find_all_by_pan"));
		query = query.concat(paginationOrder);
		return namedParameterJdbcTemplate.query(query, parameters, new DeducteeMasterResidentialRowMapper());

	}

	/**
	 * 
	 * @param deductorPan
	 * @param pagination
	 * @param deducteeName
	 * @param pagination
	 * @return
	 */
	public List<DeducteeMasterResidential> findAllByDeducteeNamePan(String deductorPan, String deducteeName,
			Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String paginationOrder = CommonUtil.getPagination("deductee_master_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("deducteeName", "%" + deducteeName + "%");
		String query = String.format(queries.get("find_all_by_pan_name"));
		query = query.concat(paginationOrder);
		return namedParameterJdbcTemplate.query(query, parameters, new DeducteeMasterResidentialRowMapper());
	}

	/**
	 * 
	 * @param deductorPan
	 * @return
	 */
	public BigInteger getAllDeducteeResidentialCount(String deductorPan) {
		return jdbcTemplate.queryForObject(String.format(queries.get("get_all_residential_count")),
				new Object[] { deductorPan }, BigInteger.class);
	}

	/**
	 * This method for save deductee residential.
	 * 
	 * @param deducteeMasterResidential
	 * @return
	 */

	public DeducteeMasterResidential save(DeducteeMasterResidential deducteeMasterResidential) {

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("deductor_master_pan", deducteeMasterResidential.getDeductorPan());
		parameters.put("active", deducteeMasterResidential.getActive());
		parameters.put("advance_transaction_count", deducteeMasterResidential.getAdvanceTransactionCount());
		parameters.put("applicable_from", deducteeMasterResidential.getApplicableFrom());
		parameters.put("applicable_to", deducteeMasterResidential.getApplicableTo());
		parameters.put("area_locality", deducteeMasterResidential.getAreaLocality());
		parameters.put("country", deducteeMasterResidential.getCountry());
		parameters.put("created_by", deducteeMasterResidential.getCreatedBy());
		parameters.put("created_date", deducteeMasterResidential.getCreatedDate());
		parameters.put("deductee_code", deducteeMasterResidential.getDeducteeCode());
		parameters.put("deductee_status", deducteeMasterResidential.getDeducteeStatus());
		parameters.put("deductor_code", deducteeMasterResidential.getDeductorCode());
		parameters.put("default_rate", deducteeMasterResidential.getDefaultRate());
		parameters.put("email", deducteeMasterResidential.getEmailAddress());
		parameters.put("flat_door_block_no", deducteeMasterResidential.getFlatDoorBlockNo());
		parameters.put("invoice_transaction_count", deducteeMasterResidential.getInvoiceTransactionCount());
		parameters.put("is_eligible_for_multiple_sections",
				deducteeMasterResidential.getIsDeducteeHasAdditionalSections());
		parameters.put("additional_sections", deducteeMasterResidential.getAdditionalSections());
		parameters.put("modified_by", deducteeMasterResidential.getModifiedBy());
		parameters.put("modified_date", deducteeMasterResidential.getModifiedDate());
		parameters.put("modified_name", deducteeMasterResidential.getModifiedName());
		parameters.put("deductee_master_name", deducteeMasterResidential.getDeducteeName());
		parameters.put("name_building_village", deducteeMasterResidential.getNameBuildingVillage());
		parameters.put("name_of_the_company_code", deducteeMasterResidential.getNameOfTheCompanyCode());
		parameters.put("name_of_the_deductee", deducteeMasterResidential.getNameOfTheDeductee());
		parameters.put("deductee_master_pan", deducteeMasterResidential.getDeducteePAN());
		parameters.put("pan_status", deducteeMasterResidential.getPanStatus());
		parameters.put("pan_verified_date", deducteeMasterResidential.getPanVerifiedDate());
		parameters.put("phone_number", deducteeMasterResidential.getPhoneNumber());
		parameters.put("pin_code", deducteeMasterResidential.getPinCode());
		parameters.put("provision_transaction_count", deducteeMasterResidential.getProvisionTransactionCount());
		parameters.put("rate", deducteeMasterResidential.getRate());
		parameters.put("residential_status", deducteeMasterResidential.getDeducteeResidentialStatus());
		parameters.put("road_street_postoffice", deducteeMasterResidential.getRoadStreetPostoffice());
		parameters.put("section", deducteeMasterResidential.getSection());
		parameters.put("source_file_name", deducteeMasterResidential.getSourceFileName());
		parameters.put("source_identifier", deducteeMasterResidential.getSourceIdentifier());
		parameters.put("state", deducteeMasterResidential.getState());
		parameters.put("tin_unique_identification", deducteeMasterResidential.getTinUniqueIdentification());
		parameters.put("town_city_district", deducteeMasterResidential.getTownCityDistrict());
		parameters.put("user_defined_field_1", deducteeMasterResidential.getUserDefinedField1());
		parameters.put("user_defined_field_2", deducteeMasterResidential.getUserDefinedField2());
		parameters.put("user_defined_field_3", deducteeMasterResidential.getUserDefinedField3());
		parameters.put("match_score", deducteeMasterResidential.getMatchScore());
		parameters.put("name_as_per_traces", deducteeMasterResidential.getNameAsPerTraces());
		parameters.put("pan_as_per_traces", deducteeMasterResidential.getPanAsPerTraces());
		parameters.put("remarks_as_per_traces", deducteeMasterResidential.getRemarksAsPerTraces());
		parameters.put("nature_of_payment", deducteeMasterResidential.getNatureOfPayment());
		parameters.put("deductee_aadhar_number", deducteeMasterResidential.getDeducteeAadharNumber());
		parameters.put("section_code", deducteeMasterResidential.getSectionCode());
		parameters.put("is_threshold_limit_applicable",
				deducteeMasterResidential.getIsThresholdLimitApplicable() != null
						&& deducteeMasterResidential.getIsThresholdLimitApplicable() == true ? 1 : 0);
		parameters.put("additional_section_code", deducteeMasterResidential.getAdditionalSectionCode());
		parameters.put("deductee_key", deducteeMasterResidential.getDeducteeKey());
		parameters.put("deductee_enrichment_key", deducteeMasterResidential.getDeducteeEnrichmentKey());
		parameters.put("tds_excemption_reason", deducteeMasterResidential.getTdsExcemptionReason());
		parameters.put("tds_excemption_flag", deducteeMasterResidential.getTdsExcemptionFlag());
		parameters.put("deductee_master_balances_of_194q", deducteeMasterResidential.getDeducteeMasterBalancesOf194q());
		parameters.put("advance_balances_of_194q", deducteeMasterResidential.getAdvanceBalancesOf194q());
		parameters.put("provision_balances_of_194q", deducteeMasterResidential.getProvisionBalancesOf194q());
		parameters.put("current_balance_year", deducteeMasterResidential.getCurrentBalanceYear());
		parameters.put("current_balance_month", deducteeMasterResidential.getCurrentBalanceMonth());
		parameters.put("previous_balance_year", deducteeMasterResidential.getPreviousBalanceYear());
		parameters.put("previous_balance_month", deducteeMasterResidential.getPreviousBalanceMonth());
		parameters.put("advances_as_of_march", deducteeMasterResidential.getAdvancesAsOfMarch());
		parameters.put("provisions_as_of_march", deducteeMasterResidential.getProvisionsAsOfMarch());
		parameters.put("additional_section_thresholds", deducteeMasterResidential.getAdditionalSectionThresholds());
		parameters.put("tds_applicability_under_section", deducteeMasterResidential.getTdsApplicabilityUnderSection());
		parameters.put("deductee_tan", deducteeMasterResidential.getDeducteeTan());
		parameters.put("opening_balance_credit_note", deducteeMasterResidential.getOpeningBalanceCreditNote());
		parameters.put("tds_exemption_number", deducteeMasterResidential.getTdsExemptionNumber());
		parameters.put("gstin_number", deducteeMasterResidential.getDeducteeGSTIN());
		parameters.put("gr_or_ir_indicator", deducteeMasterResidential.getGrOrIRIndicator());
		deducteeMasterResidential.setDeducteeMasterId(simpleJdbcInsert.executeAndReturnKey(parameters).intValue());
		logger.info("Record inserted to deductee master residential table {}",
				deducteeMasterResidential.getDeducteeMasterId());
		return deducteeMasterResidential;

	}

	/**
	 * 
	 * @param deductorPan
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public List<DeducteeMasterResidential> getResidentalBasedOnTanAndYear(String deductorPan, Date startDate,
			Date endDate) {
		return jdbcTemplate.query(String.format(queries.get("get_deductees")), new DeducteeMasterResidentialRowMapper(),
				deductorPan, startDate, endDate);
	}

	/**
	 * 
	 * @param deductorPan
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public long countDeducteeResidentialPanStatusInValid(String deductorPan, String startDate, String endDate) {
		return jdbcTemplate.queryForObject(String.format(queries.get("count_residential_pan_status_invalide")),
				new Object[] { deductorPan, startDate, endDate }, Long.class);
	}

	/**
	 * This method is count invalid pan status.
	 * 
	 * @param deductorPan
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public long countDeducteeResidentialPanStatusValid(String deductorPan, String startDate, String endDate) {
		return jdbcTemplate.queryForObject(String.format(queries.get("count_residential_pan_status_valid")),
				new Object[] { deductorPan, startDate, endDate }, Long.class);
	}

	/**
	 * This method is count empty pan status.
	 * 
	 * @param deductorPan
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public long countDeducteeResidentialPanStatusEmpty(String deductorPan, String startDate, String endDate) {
		return jdbcTemplate.queryForObject(String.format(queries.get("count_residential_pan_status_empty")),
				new Object[] { deductorPan, startDate, endDate }, Long.class);
	}

	/**
	 * This method for get all deductee names.
	 * 
	 * @param deductorPan
	 * @return
	 */
	public List<DeducteeMasterResidential> getDeducteesByPan(String deductorPan) {
		return jdbcTemplate.query(String.format(queries.get("get_list_deductee")),
				new DeducteeMasterResidentialRowMapper(), deductorPan);
	}

	public List<DeducteeMasterResidential> getDeducteesByNoPan(String deductorPan) {
		return jdbcTemplate.query(String.format(queries.get("get_list_deductee_no_pan")),
				new DeducteeMasterResidentialRowMapper(), deductorPan);
	}

	/**
	 * This method for get deductee residental recode based on deductor pan and id.
	 * 
	 * @param deductorPan
	 * @param id
	 * @return
	 */
	public List<DeducteeMasterResidential> findById(String deductorPan, Integer id) {
		return jdbcTemplate.query(String.format(queries.get("find_by_id")), new DeducteeMasterResidentialRowMapper(),
				deductorPan, id);
	}

	/**
	 * 
	 * @param deductorPan
	 * @param monthStartDate
	 * @param monthEndDate
	 * @param unpaged
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public PagedData<DeducteeMasterResidential> getDeductees(String deductorPan, Date monthStartDate, Date monthEndDate,
			Pagination unpaged) {
		return (PagedData<DeducteeMasterResidential>) jdbcTemplate.query(String.format(queries.get("get_deductees")),
				new DeducteeMasterResidentialRowMapper(), deductorPan, monthStartDate, monthEndDate);
	}

	/**
	 * This method for update applicable to date based on deductee id.
	 * 
	 * @param deducteeDB
	 */
	public void updateApplicableTo(DeducteeMasterResidential deducteeDB) {
		jdbcTemplate.update(String.format(queries.get("update_applicable_to")), deducteeDB.getApplicableTo(),
				deducteeDB.getDeducteeMasterId());
	}

	/**
	 * 
	 * @param deducteeDB
	 * @return
	 */
	public int excelUpdateApplicableTo(DeducteeMasterResidential deducteeDB) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(deducteeDB);
		int status = namedParameterJdbcTemplate.update(String.format(queries.get("excel_update_applicable_to")),
				namedParameters);
		if (status != 0) {
			logger.info("deductee data is updated for ID: {}", deducteeDB.getDeducteeMasterId());
		} else {
			logger.info("No record found with ID: {}", deducteeDB.getDeducteeMasterId());
		}
		return status;
	}

	/**
	 * This method for update deductee master residential data
	 * 
	 * @param deducteeMasterResidential
	 * @return
	 */
	public DeducteeMasterResidential updateResidential(DeducteeMasterResidential deducteeMasterResidential) {
		jdbcTemplate.execute(String.format(queries.get("update_residential")),
				new PreparedStatementCallback<Boolean>() {
					@Override
					public Boolean doInPreparedStatement(PreparedStatement ps)
							throws SQLException, DataAccessException {
						ps.setDate(1,
								deducteeMasterResidential.getApplicableTo() != null
										? new java.sql.Date(deducteeMasterResidential.getApplicableTo().getTime())
										: null);
						ps.setString(2, deducteeMasterResidential.getAreaLocality());
						ps.setString(3, deducteeMasterResidential.getCountry());
						ps.setString(4, deducteeMasterResidential.getFlatDoorBlockNo());
						ps.setString(5, deducteeMasterResidential.getModifiedBy());
						ps.setDate(6, new java.sql.Date(deducteeMasterResidential.getModifiedDate().getTime()));
						ps.setString(7, deducteeMasterResidential.getNameBuildingVillage());
						ps.setString(8, deducteeMasterResidential.getPinCode());
						ps.setString(9, deducteeMasterResidential.getRoadStreetPostoffice());
						ps.setString(10, deducteeMasterResidential.getState());
						ps.setString(11, deducteeMasterResidential.getTownCityDistrict());
						ps.setString(12, deducteeMasterResidential.getDeductorPan());
						ps.setInt(13, deducteeMasterResidential.getDeducteeMasterId());

						return ps.execute();
					}
				});
		return deducteeMasterResidential;
	}

	/**
	 * 
	 * @param deductorPan
	 * @param unpaged
	 * @return
	 */
	public List<DeducteeMasterResidential> findAllByDeductorPan(String deductorPan) {
		return jdbcTemplate.query(String.format(queries.get("find_all_res_deductees_by_deductorpan")),
				new DeducteeMasterResidentialRowMapper(), deductorPan);
	}

	public List<CustomDeducteesDTO> findAllByDeducteeNamePan(String deductorPan) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		String query = String.format(queries.get("find_all_res_deductee_name_pan"));
		return namedParameterJdbcTemplate.query(query, parameters,
				new BeanPropertyRowMapper<CustomDeducteesDTO>(CustomDeducteesDTO.class));
	}

	/**
	 * returns pan count based on pan status
	 * 
	 * @param panStatus
	 * @param deductorPan
	 * @return
	 */
	public long countPanBasedOnStatus(String panStatus, String deductorPan) {
		return jdbcTemplate.queryForObject(String.format(queries.get("count_pan_status")),
				new Object[] { deductorPan, panStatus }, Long.class);
	}

	/**
	 * to get the deductee names from the invoice table
	 * 
	 * @param tan
	 * @param type
	 * @param year
	 * @param month
	 * @param isMismatch
	 * @return
	 */
	public List<String> getInvoiceDeductees(String tan, String type, int year, int month, boolean isMismatch) {
		logger.info("DAO method executing to get List of Distinct deductee names from invoice_line_item table{}");
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", year);
		parameters.put("month", month);
		parameters.put("tan", tan);
		parameters.put("type", type);
		parameters.put("isMismatch", isMismatch);
		String query = queries.get("get_Invoice_Deductees");
		if ("N".equalsIgnoreCase(type) && (!isMismatch)) {
			query += " AND active = 1 ;";

		} else if (isMismatch) {
			query += " and active = 0 and has_mismatch = 1 ;";

		}
		return namedParameterJdbcTemplate.queryForList(query, parameters, String.class);
	}

	/**
	 * to fetch advance data
	 * 
	 * @param tan
	 * @param type
	 * @param year
	 * @param month
	 * @param isMatch
	 * @return
	 */
	public List<String> getAdvanceDeductees(String tan, String type, int year, int month, boolean isMatch) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.info("DAO method executing to get List of Distinct deductee names from Advance table{}");
		logger.debug("tenant id : {}", tenantId);
		String query = String.format(queries.get("get_advance_deductees"));

		if ("N".equalsIgnoreCase(type) && !isMatch) {
			query = query.concat(" and active = 1");
		}
		if (isMatch) {
			query = query.concat(" and mismatch =1 and active = 0");
		}

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("tan", tan);
		parameters.put("type", type);
		parameters.put("year", year);
		parameters.put("month", month);

		return namedParameterJdbcTemplate.queryForList(query, parameters, String.class);
	}

	/**
	 * getting provision data
	 * 
	 * @param tan
	 * @param type
	 * @param year
	 * @param month
	 * @param isMatch
	 * @return
	 */
	public List<String> getProvisionDeductees(String tan, String type, int year, int month, boolean isMatch) {
		logger.info("DAO method executing to get List of Distinct deductee names from provision table{}");
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String query = String.format(queries.get("get_provision_deductees"));

		if ("N".equalsIgnoreCase(type) && !isMatch) {
			query = query.concat(" and active = 1");
		}
		if (isMatch) {
			query = query.concat(" and mismatch =1 and active = 0");
		}

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("tan", tan);
		parameters.put("type", type);
		parameters.put("year", year);
		parameters.put("month", month);

		return namedParameterJdbcTemplate.queryForList(query, parameters, String.class);
	}

	/**
	 * to get list of pans from residential deductee table
	 * 
	 * @param deducteePan
	 * @param deductorPan
	 * @return
	 */
	public Long getDeducteeBasedOnDeducteePanAndDeductorPan(String deducteePan, String deductorPan) {
		logger.info("DAO method executing to get list of deductee based on pan {}");

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("deducteePan", deducteePan);
		parameters.put("deductorPan", deductorPan);
		return namedParameterJdbcTemplate.queryForObject(
				String.format(queries.get("get_deductee_deducteePan_deductorPan")), parameters, Long.class);
	}

	public List<String> getInvoiceDeducteesBasedOnYEar(String tan, String type, int year, boolean isMismatch) {
		logger.info("DAO method executing to get List of Distinct deductee names from invoice_line_item table{}");
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", year);
		parameters.put("tan", tan);
		parameters.put("type", type);
		parameters.put("isMismatch", isMismatch);
		String query = queries.get("get_Invoice_Deductees_without_month");
		if ("N".equalsIgnoreCase(type) && (!isMismatch)) {
			query += " AND active = 1 ;";

		} else if (isMismatch) {
			query += " and active = 0 and has_mismatch = 1 ;";

		}
		return namedParameterJdbcTemplate.queryForList(query, parameters, String.class);
	}

	public List<DeducteeMasterResidential> findAllByDeductorPanDeducteePan(String deductorPan, String deducteePan) {
		return jdbcTemplate.query(String.format(queries.get("find_all_by_deductor_pan_and_deductee_pan")),
				new Object[] { deductorPan, deducteePan }, new DeducteeMasterResidentialRowMapper());
	}

	public Map<String, Integer> getActiveAndInactiveResidentDeducteeCounts(String deductorPan, String type) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("type", type);
		String query = "select count(1) from (select distinct deductee_key"
				+ " from Client_Masters.deductee_master_residential where deductor_master_pan =:deductorPan and active = 1"
				+ " and applicable_from is not null and CAST(applicable_from AS DATE) <= CONVERT(DATE, GETDATE())"
				+ " and (applicable_to is null or CAST(applicable_to AS DATE) >= CONVERT(DATE, GETDATE()))) as activeCount";
		Integer activeCount = namedParameterJdbcTemplate.queryForObject(query, parameters, Integer.class);
		query = "select count(1) from (select distinct deductee_key"
				+ " from Client_Masters.deductee_master_residential where deductor_master_pan =:deductorPan and active = 1"
				+ " and applicable_from is not null and (CAST(applicable_to AS DATE) < CONVERT(DATE, GETDATE()))) as inactiveCount;";
		Integer inactiveCount = namedParameterJdbcTemplate.queryForObject(query, parameters, Integer.class);
		Map<String, Integer> deducteeCounts = new HashMap<>();
		deducteeCounts.put("active", activeCount);
		deducteeCounts.put("inactive", inactiveCount);
		return deducteeCounts;
	}

	/**
	 * 
	 * @param dto
	 * @return
	 */
	public AdvanceDTO advanceSave(AdvanceDTO dto) {
		logger.info("tenent id: {}", MultiTenantContext.getTenantId());
		logger.info("insert advance method execution started  {}");
		dto.setAssessmentYear(dto.getAssessmentYear());
		dto.setDeductorMasterTan(dto.getDeductorMasterTan());
		dto.setPostingDateOfDocument(dto.getPostingDateOfDocument());
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(dto);
		dto.setId(advanceJdbcInsert.executeAndReturnKey(namedParameters).intValue());
		logger.info("Record inserted to advance table {}");
		return dto;
	}

	/**
	 * 
	 * @param dto
	 * @return
	 */
	public ProvisionDTO provisionSave(ProvisionDTO dto) {
		logger.info("tenent id: {}", MultiTenantContext.getTenantId());
		logger.info("insert provision method execution started  {}");
		dto.setDeductorMasterTan(dto.getDeductorMasterTan());
		dto.setAssessmentYear(dto.getAssessmentYear());
		dto.setPostingDateOfDocument(dto.getPostingDateOfDocument());
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(dto);
		dto.setId(provisionJdbcInsert.executeAndReturnKey(namedParameters).intValue());
		logger.info("Record inserted to provision table {}");
		return dto;
	}

	/**
	 * 
	 * @param deductorPan
	 * @param deducteeName
	 * @param pagination
	 * @return
	 */
	public List<DeducteeMasterResidential> findAllByDeducteeCode(String deductorPan, String deducteeCode,
			Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String paginationOrder = CommonUtil.getPagination("deductee_master_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("deducteeCode", "%" + deducteeCode + "%");
		String query = String.format(queries.get("find_all_by_deductee_code"));
		query = query.concat(paginationOrder);
		return namedParameterJdbcTemplate.query(query, parameters, new DeducteeMasterResidentialRowMapper());
	}

	/**
	 * 
	 * @param deductorPan
	 * @param deducteeName
	 * @param deducteeCode
	 * @param pagination
	 * @return
	 */
	public List<DeducteeMasterResidential> findAllByDeducteeNameAndCode(String deductorPan, String deducteeName,
			String deducteeCode, Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String paginationOrder = CommonUtil.getPagination("deductee_master_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("deducteeCode", "%" + deducteeCode + "%");
		parameters.put("deducteeName", "%" + deducteeName + "%");
		String query = String.format(queries.get("find_all_by_deductee_name_and_code"));
		query = query.concat(paginationOrder);
		return namedParameterJdbcTemplate.query(query, parameters, new DeducteeMasterResidentialRowMapper());
	}

	/**
	 * This method for deductee batch update applicableTo date.
	 * 
	 * @param batchUpdateApplicableTo
	 */
	@org.springframework.transaction.annotation.Transactional
	public void batchUpdateApplicableToDate(List<DeducteeMasterResidential> batchUpdateApplicableTo) {
		String updateQuery = "UPDATE Client_Masters.deductee_master_residential SET applicable_to = :applicableTo, active = 0 WHERE deductee_master_id = :deducteeMasterId ";
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(batchUpdateApplicableTo);
		namedParameterJdbcTemplate.batchUpdate(updateQuery, batch);
		logger.info("deductee batch updated successfully {}", batchUpdateApplicableTo.size());
	}

	/**
	 * This method for deductee batch save.
	 * 
	 * @param deducteeBatchSave
	 */
	@Transactional
	public void deducteeBatchSave(List<DeducteeMasterResidential> deducteeBatchSave, String tenantId) {
		MultiTenantContext.setTenantId(tenantId);
		StopWatch timer = new StopWatch();
		String sql = "INSERT INTO Client_Masters.deductee_master_residential (deductor_master_pan,active,additional_sections,advance_transaction_count,applicable_from,applicable_to, "
				+ "area_locality, country, created_by, created_date, deductee_code, deductee_status, deductor_code, default_rate,email, flat_door_block_no, "
				+ "invoice_transaction_count, is_eligible_for_multiple_sections, modified_by, modified_date, modified_name, deductee_master_name, name_building_village, "
				+ "name_of_the_company_code, name_of_the_deductee, deductee_master_pan, pan_status, pan_verified_date, phone_number, pin_code, provision_transaction_count, "
				+ "rate,residential_status,road_street_postoffice,[section],source_file_name,source_identifier,state,tin_unique_identification,town_city_district, "
				+ "user_defined_field_1,user_defined_field_2,user_defined_field_3,match_score,name_as_per_traces,pan_as_per_traces,remarks_as_per_traces,nature_of_payment, "
				+ "deductee_aadhar_number,section_code,additional_section_code,is_threshold_limit_applicable,deductee_key,deductee_enrichment_key,tds_excemption_reason, "
				+ "tds_excemption_flag,deductee_master_balances_of_194q,advance_balances_of_194q,provision_balances_of_194q,current_balance_year,current_balance_month, "
				+ "previous_balance_year,previous_balance_month,advances_as_of_march,provisions_as_of_march,additional_section_thresholds,tds_applicability_under_section, "
				+ "gstin_number,opening_balance_credit_note,tds_exemption_number,tds_section_description,gr_or_ir_indicator,deductee_tan,deductor_name, "
				+ "user_defined_field_4,user_defined_field_5,user_defined_field_6,user_defined_field_7,user_defined_field_8,user_defined_field_9,user_defined_field_10,batch_upload_id) "
				+ "VALUES (:deductorPan, :active, :additionalSections, :advanceTransactionCount, :applicableFrom, :applicableTo, :areaLocality, :country,:createdBy,:createdDate, "
				+ ":deducteeCode, :deducteeStatus, :deductorCode, :defaultRate, :emailAddress, :flatDoorBlockNo, :invoiceTransactionCount,:isDeducteeHasAdditionalSections, "
				+ ":modifiedBy, :modifiedDate, :modifiedName, :deducteeName, :nameBuildingVillage, :nameOfTheCompanyCode, :nameOfTheDeductee, :deducteePAN,:panStatus, "
				+ ":panVerifiedDate, :phoneNumber, :pinCode, :provisionTransactionCount, :rate, :deducteeResidentialStatus, :roadStreetPostoffice, :section, :sourceFileName,:sourceIdentifier, "
				+ ":state, :tinUniqueIdentification, :townCityDistrict, :userDefinedField1, :userDefinedField2, :userDefinedField3, :matchScore, :nameAsPerTraces,:panAsPerTraces, "
				+ ":remarksAsPerTraces, :natureOfPayment,:deducteeAadharNumber, :sectionCode, :additionalSectionCode, :isThresholdLimitApplicable, :deducteeKey,:deducteeEnrichmentKey, "
				+ ":tdsExcemptionReason, :tdsExcemptionFlag, :deducteeMasterBalancesOf194q, :advanceBalancesOf194q, :provisionBalancesOf194q, :currentBalanceYear,:currentBalanceMonth, "
				+ ":previousBalanceYear, :previousBalanceMonth, :advancesAsOfMarch, :provisionsAsOfMarch, :additionalSectionThresholds, :tdsApplicabilityUnderSection, "
				+ ":deducteeGSTIN, :openingBalanceCreditNote, :tdsExemptionNumber, :tdsSectionDescription, :grOrIRIndicator, :deducteeTan, :deductorName, "
				+ ":userDefinedField4, :userDefinedField5, :userDefinedField6, :userDefinedField7, :userDefinedField8, :userDefinedField9, "
				+ ":userDefinedField10, :batchUploadId) ";
		timer.start();
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(deducteeBatchSave);
		namedParameterJdbcTemplate.batchUpdate(sql, batch);
		logger.info("deductee batch inserted size is {}", deducteeBatchSave.size());
		timer.stop();
		logger.info("deductee batch save -> Total time in seconds: {}", timer.getTime());
		logger.info("inserted deductee total records :{} ", deducteeBatchSave.size());

	}

	/**
	 * This method for deductee batch update.
	 * 
	 * @param deducteeBatchUpdate
	 */
	@org.springframework.transaction.annotation.Transactional
	public void deducteeBatchUpdate(List<DeducteeMasterResidential> deducteeBatchUpdate) {
		String updateQuery = "UPDATE Client_Masters.deductee_master_residential SET deductor_master_pan = :deductorPan, active = :active,"
				+ "	additional_sections = :additionalSections, advance_transaction_count = :advanceTransactionCount,applicable_from = :applicableFrom,"
				+ "	applicable_to = :applicableTo, area_locality = :areaLocality, country = :country, created_by = :createdBy, created_date = :createdDate,"
				+ "	deductee_code = :deducteeCode, deductee_status = :deducteeStatus, deductor_code = :deductorCode, default_rate = :defaultRate,"
				+ "	email = :emailAddress,	flat_door_block_no = :flatDoorBlockNo, invoice_transaction_count = :invoiceTransactionCount, is_eligible_for_multiple_sections = :isDeducteeHasAdditionalSections,"
				+ "	modified_by = :modifiedBy,	modified_date = :modifiedDate, modified_name = :modifiedName, deductee_master_name = :deducteeName,"
				+ "	name_building_village = :nameBuildingVillage, name_of_the_company_code = :nameOfTheCompanyCode, deductee_master_pan = :deducteePAN,"
				+ "	pan_verified_date = :panVerifiedDate, phone_number = :phoneNumber, pin_code = :pinCode, provision_transaction_count = :provisionTransactionCount,"
				+ "	rate = :rate, residential_status = :deducteeResidentialStatus, road_street_postoffice = :roadStreetPostoffice, section = :section, source_file_name = :sourceFileName,"
				+ "	source_identifier = :sourceIdentifier, state = :state, tin_unique_identification = :tinUniqueIdentification, town_city_district = :townCityDistrict, user_defined_field_1 = :userDefinedField1,"
				+ " user_defined_field_2 = :userDefinedField2, user_defined_field_3 = :userDefinedField3, match_score = :matchScore, name_as_per_traces = :nameAsPerTraces, pan_as_per_traces = :panAsPerTraces,"
				+ "	remarks_as_per_traces = :remarksAsPerTraces, nature_of_payment = :natureOfPayment, deductee_aadhar_number = :deducteeAadharNumber, section_code = :sectionCode,"
				+ "	additional_section_code = :additionalSectionCode, is_threshold_limit_applicable = :isThresholdLimitApplicable, deductee_key = :deducteeKey, deductee_enrichment_key = :deducteeEnrichmentKey,"
				+ " tds_excemption_reason = :tdsExcemptionReason, tds_excemption_flag =:tdsExcemptionFlag, additional_section_thresholds =:additionalSectionThresholds,"
				+ " batch_upload_id = :batchUploadId" + " WHERE deductee_master_id = :deducteeMasterId ";

		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(deducteeBatchUpdate);
		namedParameterJdbcTemplate.batchUpdate(updateQuery, batch);

		logger.info("deductee batch updated successfully {}", deducteeBatchUpdate.size());
	}

	/**
	 * This method for deductee nop group master batch save.
	 * 
	 * @param deducteeNopBatchUpdate
	 */
	@Transactional
	public void deducteeNopBatchSave(List<DeducteeNopGroup> deducteeNopBatchSave, String tenantId) {
		MultiTenantContext.setTenantId(tenantId);
		StopWatch timer = new StopWatch();
		String sql = "INSERT INTO Transactions.deductee_nop_group (deductee_key, [year], amount_utilized, threshold_reached, last_updated_date, transaction_id, "
				+ "active, created_by, modified_by, created_date, modified_date, advance_pending, deductor_pan, deductee_pan, group_nop_id, threshold_limit_amount) "
				+ "VALUES (:deducteeKey, :year, :amountUtilized, :thresholdReached, :lastUpdatedDate, :transactionId, "
				+ ":active, :createdBy, :modifiedBy, :createdDate, :modifiedDate, :advancePending, :deductorPan, :deducteePan, :groupNopId, :thresholdLimitAmount) ";

		timer.start();
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(deducteeNopBatchSave);
		namedParameterJdbcTemplate.batchUpdate(sql, batch);
		timer.stop();
		logger.info("deductee nop group batch save -> Total time in seconds: {}", timer.getTime());
		logger.info("inserted deductee nop group total records :{} ", deducteeNopBatchSave.size());

	}

	/**
	 * This method for deductee nop group master batch update.
	 * 
	 * @param deducteeNopBatchUpdate
	 */
	@org.springframework.transaction.annotation.Transactional
	public void deducteeNopBatchUpdate(List<DeducteeNopGroup> deducteeNopBatchUpdate) {
		String updateQuery = "UPDATE Transactions.deductee_nop_group SET last_updated_date= :lastUpdatedDate,modified_by= :modifiedBy, "
				+ "modified_date= :modifiedDate, amount_utilized = :amountUtilized, threshold_reached = :thresholdReached,active = :active ,deductee_key = :deducteeKey, "
				+ "deductee_pan = :deducteePan WHERE id= :id ";
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(deducteeNopBatchUpdate);
		namedParameterJdbcTemplate.batchUpdate(updateQuery, batch);
		logger.info("deductee nop group batch updated successfully {}", deducteeNopBatchUpdate.size());
	}

	/**
	 * This method for deductee batch save.
	 * 
	 * @param deducteeBatchSave
	 */
	@Transactional
	public void deducteeStaggingBatchSave(List<DeducteeMasterResidential> deducteeBatchSave, String tenantId) {
		MultiTenantContext.setTenantId(tenantId);
		StopWatch timer = new StopWatch();
		String sql = "INSERT INTO Client_Masters.deductee_master_stagging (deductor_master_pan,active,additional_sections,advance_transaction_count,applicable_from,applicable_to, "
				+ "area_locality, country, created_by, created_date, deductee_code, deductee_status, deductor_code, default_rate,email, flat_door_block_no, "
				+ "invoice_transaction_count, is_eligible_for_multiple_sections, modified_by, modified_date, modified_name, deductee_master_name, name_building_village, "
				+ "name_of_the_company_code, name_of_the_deductee, deductee_master_pan, pan_status, pan_verified_date, phone_number, pin_code, provision_transaction_count, "
				+ "rate,residential_status,road_street_postoffice,[section],source_file_name,source_identifier,state,tin_unique_identification,town_city_district, "
				+ "user_defined_field_1,user_defined_field_2,user_defined_field_3,match_score,name_as_per_traces,pan_as_per_traces,remarks_as_per_traces,nature_of_payment, "
				+ "deductee_aadhar_number,section_code,additional_section_code,is_threshold_limit_applicable,deductee_key,deductee_enrichment_key,tds_excemption_reason, "
				+ "tds_excemption_flag,deductee_master_balances_of_194q,advance_balances_of_194q,provision_balances_of_194q,current_balance_year,current_balance_month, "
				+ "previous_balance_year,previous_balance_month,advances_as_of_march,provisions_as_of_march,additional_section_thresholds,tds_applicability_under_section, "
				+ "gstin_number,opening_balance_credit_note,tds_exemption_number,tds_section_description,gr_or_ir_indicator,deductee_tan,deductor_name, "
				+ "user_defined_field_4,user_defined_field_5,user_defined_field_6,user_defined_field_7,user_defined_field_8,user_defined_field_9,user_defined_field_10, batch_upload_id) "
				+ "VALUES (:deductorPan, :active, :additionalSections, :advanceTransactionCount, :applicableFrom, :applicableTo, :areaLocality, :country,:createdBy,:createdDate, "
				+ ":deducteeCode, :deducteeStatus, :deductorCode, :defaultRate, :emailAddress, :flatDoorBlockNo, :invoiceTransactionCount,:isDeducteeHasAdditionalSections,"
				+ ":modifiedBy, :modifiedDate, :modifiedName, :deducteeName, :nameBuildingVillage, :nameOfTheCompanyCode, :nameOfTheDeductee, :deducteePAN,:panStatus, "
				+ ":panVerifiedDate, :phoneNumber, :pinCode, :provisionTransactionCount, :rate, :deducteeResidentialStatus, :roadStreetPostoffice, :section, :sourceFileName,:sourceIdentifier, "
				+ ":state, :tinUniqueIdentification, :townCityDistrict, :userDefinedField1, :userDefinedField2, :userDefinedField3, :matchScore, :nameAsPerTraces,:panAsPerTraces, "
				+ ":remarksAsPerTraces, :natureOfPayment,:deducteeAadharNumber, :sectionCode, :additionalSectionCode, :isThresholdLimitApplicable, :deducteeKey,:deducteeEnrichmentKey, "
				+ ":tdsExcemptionReason, :tdsExcemptionFlag, :deducteeMasterBalancesOf194q, :advanceBalancesOf194q, :provisionBalancesOf194q, :currentBalanceYear,:currentBalanceMonth, "
				+ ":previousBalanceYear, :previousBalanceMonth, :advancesAsOfMarch, :provisionsAsOfMarch, :additionalSectionThresholds, :tdsApplicabilityUnderSection, "
				+ ":deducteeGSTIN, :openingBalanceCreditNote, :tdsExemptionNumber, :tdsSectionDescription, :grOrIRIndicator, :deducteeTan, :deductorName, "
				+ ":userDefinedField4, :userDefinedField5, :userDefinedField6, :userDefinedField7, :userDefinedField8, :userDefinedField9, "
				+ ":userDefinedField10, :batchUploadId) ";

		timer.start();
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(deducteeBatchSave);
		namedParameterJdbcTemplate.batchUpdate(sql, batch);
		timer.stop();
		logger.info("batch Stagging Update -> Total time in milli seconds: {}", timer.getTime());
		logger.info("inserted deductee stagging total records :{} ", deducteeBatchSave.size());

	}

	/**
	 * 
	 * @param deductorPan
	 * @param deducteeCode
	 * @param pagination
	 * @return
	 */
	public List<DeducteeMasterResidential> getDeducteeByCode(String deductorPan, String deducteeCode) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("deducteeCode", deducteeCode);
		return namedParameterJdbcTemplate.query(queries.get("tds_get_all_deductee_by_codes"), parameters,
				new DeducteeMasterResidentialRowMapper());
	}

	/**
	 * This method for advance batch save
	 * 
	 * @param advanceBatchSave
	 * @param tenantId
	 */
	public void advanceBatchSave(List<AdvanceDTO> advanceBatchSave) {
		String query = " INSERT INTO Transactions.advance(assessment_year, deductor_master_tan, assessment_month, challan_month, amount, deductor_pan ,deductee_key, deductee_code,"
				+ " deductee_name,document_type, is_resident, deductee_pan,supply_type,document_date,document_number,line_item_number,advance_npid ,advance_groupid ,active,"
				+ " is_parent ,is_exempted ,challan_paid ,approved_for_challan ,is_challan_generated ,created_by ,created_date ,modified_by ,modified_date,"
				+ " posting_date_of_document,mismatch ,under_threshold ,withholding_section ,derived_tds_section ,final_tds_section ,withholding_rate ,derived_tds_rate,"
				+ " final_tds_rate,withholding_amount ,derived_tds_amount ,final_tds_amount, batch_upload_id, is_initial_record)"
				+ " VALUES(:assessmentYear,:deductorMasterTan,:assessmentMonth,:challanMonth, :amount, :deductorPan,:deducteeKey,:deducteeCode,"
				+ " :deducteeName,:documentType,:isResident, :deducteePan,:supplyType,:documentDate,:documentNumber,:lineItemNumber,:advanceNpId,:advanceGroupid,:active,"
				+ " :isParent,:isExempted,:challanPaid,:approvedForChallan,:isChallanGenerated,:createdBy,:createdDate,:modifiedBy,:modifiedDate,"
				+ " :postingDateOfDocument,:mismatch,:underThreshold,:withholdingSection,:derivedTdsSection,:finalTdsSection,:withholdingRate,:derivedTdsRate,"
				+ " :finalTdsRate,:withholdingAmount,:derivedTdsAmount,:finalTdsAmount,:batchUploadId,:isInitialRecord); ";
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(advanceBatchSave);
		namedParameterJdbcTemplate.batchUpdate(query, batch);
		logger.info("deductee advance batch inserted size is {}", advanceBatchSave.size());
	}

	public void provisionBatchSave(List<ProvisionDTO> provisionBatchSave) {
		String query = " INSERT INTO Transactions.provision(assessment_year, deductor_master_tan, assessment_month, challan_month, provisional_amount, deductor_pan ,deductee_key, deductee_code,"
				+ " deductee_name,document_type, is_resident, deductee_pan,supply_type,document_date,document_number,line_item_number,provision_npid ,provision_groupid ,active,"
				+ " is_parent ,is_exempted ,challan_paid ,approved_for_challan ,is_challan_generated ,created_by ,created_date ,modified_by ,modified_date,"
				+ " posting_date_of_document,mismatch ,under_threshold ,withholding_section ,derived_tds_section ,final_tds_section ,withholding_rate ,derived_tds_rate,"
				+ " final_tds_rate,withholding_amount ,derived_tds_amount ,final_tds_amount, batch_upload_id, is_initial_record)"
				+ " VALUES(:assessmentYear,:deductorMasterTan,:assessmentMonth,:challanMonth, :provisionalAmount, :deductorPan,:deducteeKey,:deducteeCode,"
				+ " :deducteeName,:documentType,:isResident, :deducteePan,:supplyType,:documentDate,:documentNumber,:lineItemNumber,:provisionNpId,:provisionGroupid,:active,"
				+ " :isParent,:isExempted,:challanPaid,:approvedForChallan,:isChallanGenerated,:createdBy,:createdDate,:modifiedBy,:modifiedDate,"
				+ " :postingDateOfDocument,:mismatch,:underThreshold,:withholdingSection,:derivedTdsSection,:finalTdsSection,:withholdingRate,:derivedTdsRate,"
				+ " :finalTdsRate,:withholdingAmount,:derivedTdsAmount,:finalTdsAmount,:batchUploadId,:isInitialRecord); ";
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(provisionBatchSave);
		namedParameterJdbcTemplate.batchUpdate(query, batch);
		logger.info("deductee advance batch inserted size is {}", provisionBatchSave.size());
	}

	public int updateAdvanceAncestorId(Integer batchId, String tan) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("batchId", batchId);
		parameters.put("tan", tan);
		String query = "UPDATE Transactions.advance SET ancestor_id = advance_id WHERE batch_upload_id =:batchId and deductor_master_tan =:tan";
		return namedParameterJdbcTemplate.update(query, parameters);
	}

	public int updateProvisionAncestorId(Integer batchId, String tan) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("batchId", batchId);
		parameters.put("tan", tan);
		String query = "UPDATE Transactions.provision SET ancestor_id = provision_id WHERE batch_upload_id =:batchId and deductor_master_tan =:tan";
		return namedParameterJdbcTemplate.update(query, parameters);
	}

	/**
	 * This method for get all deductee based on deducteePan and deductorPan.
	 * 
	 * @param deductorTan
	 * @param type
	 * @return
	 */
	public List<DeducteeMasterResidential> getAllDeducteeMaster(String deductorPan, String deducteePan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("deducteePan", deducteePan);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_all_deductee_master_data")), parameters,
				new DeducteeMasterResidentialRowMapper());
	}

	/**
	 * 
	 * @param deductorPan
	 * @return
	 */
	public List<DeducteeDeclarationRateType> getAllDeducteeByDeclaration(String deductorPan, Integer year,
			Integer month) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("currentDate", new Date());
		parameters.put("year", year);
		parameters.put("month", month - 1);
		return namedParameterJdbcTemplate.query(queries.get("get_deductee_by_declaration"), parameters,
				new DeducteeDeclarationRateTypeRowMapper());
	}

	/**
	 * 
	 * @param deductorTan
	 * @param deductorPan
	 * @return
	 */
	public List<DeducteeMasterResidential> getAllDeducteeGstin(String deductorTan, String deductorPan) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		return namedParameterJdbcTemplate.query(queries.get("get_all_deductee_gstin"), parameters,
				new DeducteeMasterResidentialRowMapper());

	}
}
