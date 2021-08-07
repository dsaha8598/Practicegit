package com.ey.in.tds.jdbc.dao;

import java.io.Serializable;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.lang3.StringUtils;
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

import com.ey.in.tds.common.onboarding.jdbc.dto.DeducteeMasterNonResidential;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeducteeMasterResidential;
import com.ey.in.tds.common.repository.common.PagedData;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.jdbc.rowmapper.DeducteeMasterNonResidentialRowMapper;
import com.ey.in.tds.jdbc.rowmapper.DeducteeMasterStaggingRowMapper;
import com.ey.in.tds.onboarding.dto.deductee.CustomDeducteesNonResidentDTO;

/**
 * 
 * @author scriptbees
 *
 */
@Repository
public class DeducteeMasterNonResidentialDAO implements Serializable {

	private static final long serialVersionUID = 1L;

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
		simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("deductee_master_non_residential")
				.withSchemaName("Client_Masters").usingGeneratedKeyColumns("deductee_master_id");
	}

	/**
	 * 
	 * @param deductorPan
	 * @param modifiedName
	 * @return
	 */
	public List<DeducteeMasterNonResidential> findAllByModifiedDeducteeNamePan(String deductorPan, String modifiedName,
			String deducteePAN) {
		deducteePAN = StringUtils.isBlank(deducteePAN) ? "" : deducteePAN;
		return jdbcTemplate.query(String.format(queries.get("deductee_master_modified_name")),
				new DeducteeMasterNonResidentialRowMapper(), deductorPan, modifiedName, deducteePAN);
	}

	/**
	 * 
	 * @param deducteeMasterNonResidential
	 * @return
	 */

	public DeducteeMasterNonResidential save(DeducteeMasterNonResidential deducteeMasterNonResidential) {

		Map<String, Object> parameters = new HashMap<String, Object>();

		parameters.put("deductor_master_pan", deducteeMasterNonResidential.getDeductorPan());
		parameters.put("deductee_master_10f_applicable_from", deducteeMasterNonResidential.getTenFApplicableFrom());
		parameters.put("deductee_master_10f_applicable_to", deducteeMasterNonResidential.getTenFApplicableTo());
		parameters.put("deductee_master_10f_file_address", deducteeMasterNonResidential.getForm10fFileAddress());
		parameters.put("active", deducteeMasterNonResidential.getActive());
		parameters.put("additional_sections", deducteeMasterNonResidential.getAdditionalSections());
		parameters.put("advance_transaction_count", deducteeMasterNonResidential.getAdvanceTransactionCount());
		parameters.put("applicable_from", deducteeMasterNonResidential.getApplicableFrom());
		parameters.put("applicable_to", deducteeMasterNonResidential.getApplicableTo());
		parameters.put("area_locality", deducteeMasterNonResidential.getAreaLocality());
		parameters.put("country", deducteeMasterNonResidential.getCountry());
		parameters.put("country_of_residence", deducteeMasterNonResidential.getCountryOfResidence());
		parameters.put("created_by", deducteeMasterNonResidential.getCreatedBy());
		parameters.put("created_date", deducteeMasterNonResidential.getCreatedDate());
		parameters.put("deductee_code", deducteeMasterNonResidential.getDeducteeCode());
		parameters.put("deductee_status", deducteeMasterNonResidential.getDeducteeStatus());
		parameters.put("deductor_code", deducteeMasterNonResidential.getDeductorCode());
		parameters.put("default_rate", deducteeMasterNonResidential.getDefaultRate());
		parameters.put("email", deducteeMasterNonResidential.getEmailAddress());
		parameters.put("fixed_based_india", deducteeMasterNonResidential.getFixedBasedIndia());
		parameters.put("flat_door_block_no", deducteeMasterNonResidential.getFlatDoorBlockNo());
		parameters.put("invoice_transaction_count", deducteeMasterNonResidential.getInvoiceTransactionCount());
		parameters.put("is_10f_available", deducteeMasterNonResidential.getIsTenFAvailable());
		parameters.put("is_eligible_for_multiple_sections",
				deducteeMasterNonResidential.getIsDeducteeHasAdditionalSections());
		parameters.put("is_no_pe_doc_available", deducteeMasterNonResidential.getNoPEDocumentAvailable());
		parameters.put("is_pe_in_india", deducteeMasterNonResidential.getWhetherPEInIndia());
		parameters.put("is_poem_available", deducteeMasterNonResidential.getIsPOEMavailable());
		parameters.put("is_trc_available", deducteeMasterNonResidential.getIsTRCAvailable());
		parameters.put("isdeductee_transparent", deducteeMasterNonResidential.getIsDeducteeTransparent());
		parameters.put("isgrossingup", deducteeMasterNonResidential.getIsGrossingUp());
		parameters.put("istenf_future", deducteeMasterNonResidential.getIstenfFuture());
		parameters.put("istrc_future", deducteeMasterNonResidential.getIstrcFuture());
		parameters.put("modified_by", deducteeMasterNonResidential.getModifiedBy());
		parameters.put("modified_date", deducteeMasterNonResidential.getModifiedDate());
		parameters.put("modified_name", deducteeMasterNonResidential.getModifiedName());
		parameters.put("deductee_master_name", deducteeMasterNonResidential.getDeducteeName());
		parameters.put("name_building_village", deducteeMasterNonResidential.getNameBuildingVillage());
		parameters.put("name_of_the_company_code", deducteeMasterNonResidential.getNameOfTheCompanyCode());
		parameters.put("name_of_the_deductee", deducteeMasterNonResidential.getNameOfTheDeductee());
		parameters.put("no_pe_doc_address", deducteeMasterNonResidential.getNoPeDocAddress());
		parameters.put("nr_rate", deducteeMasterNonResidential.getNrRate());
		parameters.put("deductee_master_pan", deducteeMasterNonResidential.getDeducteePAN());
		parameters.put("pan_status", deducteeMasterNonResidential.getPanStatus());
		parameters.put("pan_verified_date", deducteeMasterNonResidential.getPanVerifiedDate());
		parameters.put("pe_applicable_from", deducteeMasterNonResidential.getNoPEApplicableFrom());
		parameters.put("pe_applicable_to", deducteeMasterNonResidential.getNoPEApplicableTo());
		parameters.put("pe_file_address", deducteeMasterNonResidential.getPeFileAddress());
		parameters.put("phone_number", deducteeMasterNonResidential.getPhoneNumber());
		parameters.put("pin_code", deducteeMasterNonResidential.getPinCode());
		parameters.put("poem_applicable_from", deducteeMasterNonResidential.getPoemApplicableFrom());
		parameters.put("poem_applicable_to", deducteeMasterNonResidential.getPoemApplicableTo());
		parameters.put("provision_transaction_count", deducteeMasterNonResidential.getProvisionTransactionCount());
		parameters.put("rate", deducteeMasterNonResidential.getRate());
		parameters.put("related_party", deducteeMasterNonResidential.getRelatedParty());
		parameters.put("residential_status", deducteeMasterNonResidential.getDeducteeResidentialStatus());
		parameters.put("road_street_postoffice", deducteeMasterNonResidential.getRoadStreetPostoffice());
		parameters.put("section", deducteeMasterNonResidential.getSection());
		parameters.put("source_file_name", deducteeMasterNonResidential.getSourceFileName());
		parameters.put("source_identifier", deducteeMasterNonResidential.getSourceIdentifier());
		parameters.put("state", deducteeMasterNonResidential.getState());
		parameters.put("tenf_future_date", deducteeMasterNonResidential.getTenfFutureDate());
		parameters.put("tin_unique_identification", deducteeMasterNonResidential.getDeducteeTin());
		parameters.put("town_city_district", deducteeMasterNonResidential.getTownCityDistrict());
		parameters.put("trc_applicable_from", deducteeMasterNonResidential.getTrcApplicableFrom());
		parameters.put("trc_applicable_to", deducteeMasterNonResidential.getTrcApplicableTo());
		parameters.put("trc_file_address", deducteeMasterNonResidential.getTrcFileAddress());
		parameters.put("trc_future_date", deducteeMasterNonResidential.getTrcFutureDate());
		parameters.put("user_defined_field_1", deducteeMasterNonResidential.getUserDefinedField1());
		parameters.put("user_defined_field_2", deducteeMasterNonResidential.getUserDefinedField2());
		parameters.put("user_defined_field_3", deducteeMasterNonResidential.getUserDefinedField3());
		parameters.put("fixedbase_availble_india_applicable_from",
				deducteeMasterNonResidential.getFixedbaseAvailbleIndiaApplicableFrom());
		parameters.put("fixedbase_availble_india_applicable_to",
				deducteeMasterNonResidential.getFixedbaseAvailbleIndiaApplicableTo());
		parameters.put("is_amount_connected_fixed_base", deducteeMasterNonResidential.getIsAmountConnectedFixedBase());
		parameters.put("is_business_carried_in_india", deducteeMasterNonResidential.getIsBusinessCarriedInIndia());
		parameters.put("is_fixedbase_availble_india", deducteeMasterNonResidential.getIsFixedbaseAvailbleIndia());
		parameters.put("is_peamount_document", deducteeMasterNonResidential.getIsPEdocument());
		parameters.put("is_peamount_received", deducteeMasterNonResidential.getIsPEamountReceived());
		parameters.put("ispeinvoilvedin_purchase_goods",
				deducteeMasterNonResidential.getIsPEinvoilvedInPurchaseGoods());
		parameters.put("principles_of_business_place", deducteeMasterNonResidential.getPrinciplesOfBusinessPlace());
		parameters.put("stay_period_financial_year", deducteeMasterNonResidential.getStayPeriodFinancialYear());
		parameters.put("weather_pe_in_india_applicable_from",
				deducteeMasterNonResidential.getWhetherPEInIndiaApplicableFrom());
		parameters.put("weather_pe_in_india_applicable_to",
				deducteeMasterNonResidential.getWhetherPEInIndiaApplicableTo());
		parameters.put("nature_of_payment", deducteeMasterNonResidential.getNatureOfPayment());
		parameters.put("deductee_aadhar_number", deducteeMasterNonResidential.getDeducteeAadharNumber());
		parameters.put("section_code", deducteeMasterNonResidential.getSectionCode());
		parameters.put("is_threshold_limit_applicable",
				deducteeMasterNonResidential.getIsThresholdLimitApplicable() != null
						&& deducteeMasterNonResidential.getIsThresholdLimitApplicable() == true ? 1 : 0);
		parameters.put("additional_section_code", deducteeMasterNonResidential.getAdditionalSectionCode());
		parameters.put("nature_of_remittance", deducteeMasterNonResidential.getNatureOfRemittance());
		parameters.put("is_poem_declaration", deducteeMasterNonResidential.getIsPoemDeclaration());
		parameters.put("poem_future_date", deducteeMasterNonResidential.getPoemFutureDate());
		parameters.put("country_to_remittance", deducteeMasterNonResidential.getCountryToRemittance());
		parameters.put("beneficial_owner_of_income", deducteeMasterNonResidential.getBeneficialOwnerOfIncome());
		parameters.put("is_beneficial_ownership_of_declaration",
				deducteeMasterNonResidential.getIsBeneficialOwnershipOfDeclaration());
		parameters.put("mli_ppt_condition_satisifed", deducteeMasterNonResidential.getMliPptConditionSatisifed());
		parameters.put("mli_slob_condition_satisifed", deducteeMasterNonResidential.getMliSlobConditionSatisifed());
		parameters.put("is_mli_ppt_slob", deducteeMasterNonResidential.getIsMliPptSlob());
		parameters.put("article_number_dtaa", deducteeMasterNonResidential.getArticleNumberDtaa());
		parameters.put("section_of_incometax_act", deducteeMasterNonResidential.getSectionOfIncometaxAct());
		parameters.put("aggreement_for_transaction", deducteeMasterNonResidential.getAggreementForTransaction());
		parameters.put("deductee_key", deducteeMasterNonResidential.getDeducteeKey());
		parameters.put("deductee_enrichment_key", deducteeMasterNonResidential.getDeducteeEnrichmentKey());
		parameters.put("batch_upload_id", deducteeMasterNonResidential.getBatchUploadId());
		// new column's
		parameters.put("is_no_fixed_base_declaration_available",
				deducteeMasterNonResidential.getIsNoFixedBaseDeclarationAvailable());
		parameters.put("no_fixed_base_declaration_available_in_future",
				deducteeMasterNonResidential.getNoFixedBaseDeclarationAvailableInFuture());
		parameters.put("no_fixed_base_declaration_available_future_date",
				deducteeMasterNonResidential.getNoFixedBaseDeclarationAvailableFutureDate());
		parameters.put("no_fixed_base_declaration_from_date",
				deducteeMasterNonResidential.getNoFixedBaseDeclarationFromDate());
		parameters.put("no_fixed_base_declaration_to_date",
				deducteeMasterNonResidential.getNoFixedBaseDeclarationToDate());
		parameters.put("tds_exemption_reason", deducteeMasterNonResidential.getTdsExemptionReason());
		parameters.put("tds_exemption_flag", deducteeMasterNonResidential.getTdsExemptionFlag());
		parameters.put("deductee_master_balances_of_194q",
				deducteeMasterNonResidential.getDeducteeMasterBalancesOf194q());
		parameters.put("advance_balances_of_194q", deducteeMasterNonResidential.getAdvanceBalancesOf194q());
		parameters.put("provision_balances_of_194q", deducteeMasterNonResidential.getProvisionBalancesOf194q());
		parameters.put("current_balance_year", deducteeMasterNonResidential.getCurrentBalanceYear());
		parameters.put("current_balance_month", deducteeMasterNonResidential.getCurrentBalanceMonth());
		parameters.put("previous_balance_year", deducteeMasterNonResidential.getPreviousBalanceYear());
		parameters.put("previous_balance_month", deducteeMasterNonResidential.getPreviousBalanceMonth());
		parameters.put("advances_as_of_march", deducteeMasterNonResidential.getAdvancesAsOfMarch());
		parameters.put("provisions_as_of_march", deducteeMasterNonResidential.getProvisionsAsOfMarch());
		parameters.put("tds_applicability_under_section",
				deducteeMasterNonResidential.getTdsApplicabilityUnderSection());
		parameters.put("deductee_tan", deducteeMasterNonResidential.getDeducteeTan());
		parameters.put("opening_balance_creditNote", deducteeMasterNonResidential.getOpeningBalanceCreditNote());
		parameters.put("tds_exemption_number", deducteeMasterNonResidential.getTdsExemptionNumber());
		parameters.put("deductee_gstin", deducteeMasterNonResidential.getDeducteeGSTIN());
		parameters.put("gr_or_ir_indicator", deducteeMasterNonResidential.getGrOrIRIndicator());
		parameters.put("chartered_accountant_no", deducteeMasterNonResidential.getCharteredAccountantNo());
		parameters.put("tds_section_description", deducteeMasterNonResidential.getTdsSectionDescription());
		parameters.put("no_pe_declaration_available_in_future",
				deducteeMasterNonResidential.getNoPEDeclarationAvailableInFuture());
		parameters.put("no_pe_declaration_available_future_date",
				deducteeMasterNonResidential.getNoPEDeclarationAvailableFutureDate());
		parameters.put("is_no_poem_declaration_available",
				deducteeMasterNonResidential.getIsNoPOEMDeclarationAvailable());
		parameters.put("no_poem_declaration_applicable_from_date",
				deducteeMasterNonResidential.getNoPOEMDeclarationApplicableFromDate());
		parameters.put("no_poem_declaration_applicable_to_date",
				deducteeMasterNonResidential.getNoPOEMDeclarationApplicableToDate());

		deducteeMasterNonResidential.setDeducteeMasterId(simpleJdbcInsert.executeAndReturnKey(parameters).intValue());
		logger.info("Record inserted to deductee master non residential table {}",
				deducteeMasterNonResidential.getDeducteeMasterId());
		return deducteeMasterNonResidential;

	}

	/**
	 * 
	 * @param deductorPan
	 * @param id
	 * @return
	 */
	public List<DeducteeMasterNonResidential> findById(String deductorPan, Integer id) {
		return jdbcTemplate.query(String.format(queries.get("nr_fine_by_id")),
				new DeducteeMasterNonResidentialRowMapper(), deductorPan, id);
	}

	/**
	 * 
	 * @param deductorPan
	 * @param pagination
	 * @return
	 */
	public List<DeducteeMasterNonResidential> findAllByPan(String deductorPan, Pagination pagination) {
		String paginationOrder = CommonUtil.getPagination("deductee_master_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("deductorPan", deductorPan);
		String query = String.format(queries.get("find_all_by_deductor_pan"));
		query = query.concat(paginationOrder);
		return namedParameterJdbcTemplate.query(query, parameters, new DeducteeMasterNonResidentialRowMapper());

	}

	/**
	 * 
	 * @param deductorPan
	 * @param pagination
	 * @param deducteeName
	 * @param pagination
	 * @return
	 */
	public List<DeducteeMasterNonResidential> findAllByDeducteeNamePan(String deductorPan, String deducteeName,
			Pagination pagination) {
		String paginationOrder = CommonUtil.getPagination("deductee_master_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("deducteeName", "%" + deducteeName + "%");
		String query = String.format(queries.get("find_all_by_name_pan"));
		query = query.concat(paginationOrder);
		return namedParameterJdbcTemplate.query(query, parameters, new DeducteeMasterNonResidentialRowMapper());
	}

	/**
	 * 
	 * @param deductorPan
	 * @return
	 */
	public BigInteger getAllDeducteeNonResidentialCount(String deductorPan) {
		return jdbcTemplate.queryForObject(String.format(queries.get("non_residential_count")),
				new Object[] { deductorPan }, BigInteger.class);
	}

	/**
	 * 
	 * @param deductorPan
	 * @param deducteePan
	 * @param unpaged
	 * @return
	 */
	public List<DeducteeMasterNonResidential> getDeducteeByPan(String deductorPan, String deducteePan) {
		return jdbcTemplate.query(String.format(queries.get("get_deductee_by_pans")),
				new DeducteeMasterNonResidentialRowMapper(), deductorPan, deducteePan);
	}

	/**
	 * 
	 * @param deductorPan
	 * @param deducteeName
	 * @param unpaged
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public PagedData<DeducteeMasterNonResidential> getDeducteeByName(String deductorPan, String deducteeName) {
		return (PagedData<DeducteeMasterNonResidential>) jdbcTemplate.query(
				String.format(queries.get("get_deductee_by_name")), new DeducteeMasterNonResidentialRowMapper(),
				deductorPan, deducteeName);
	}

	/**
	 * This method for get deductee names.
	 * 
	 * @param deductorPan
	 * @return
	 */
	public List<DeducteeMasterNonResidential> getDeducteesByPan(String deductorPan) {
		return jdbcTemplate.query(String.format(queries.get("get_deductee_by_pan")),
				new DeducteeMasterNonResidentialRowMapper(), deductorPan);
	}

	/**
	 * This method for update applicable to data.
	 * 
	 * @param deducteeDB
	 */
	public void updateApplicableTo(DeducteeMasterNonResidential deducteeDB) {
		jdbcTemplate.update(String.format(queries.get("update_applicable_to_data")), deducteeDB.getApplicableTo(),
				deducteeDB.getDeducteeMasterId());
	}

	/**
	 * 
	 * @param deducteeDB
	 * @return
	 */
	public int excelUpdateApplicableTo(DeducteeMasterNonResidential deducteeDB) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(deducteeDB);
		int status = namedParameterJdbcTemplate.update(String.format(queries.get("excel_update_applicable_to_data")),
				namedParameters);
		if (status != 0) {
			logger.info("deductee data is updated for ID: {}", deducteeDB.getDeducteeMasterId());
		} else {
			logger.info("No record found with ID: {}", deducteeDB.getDeducteeMasterId());
		}
		return status;
	}

	/**
	 * This method for update non residential recode.
	 * 
	 * @param deducteeMasterNonResidential
	 * @return
	 */
	public DeducteeMasterNonResidential updateNonResidential(
			DeducteeMasterNonResidential deducteeMasterNonResidential) {
		jdbcTemplate.execute(String.format(queries.get("update_non_residential")),
				new PreparedStatementCallback<Boolean>() {
					@Override
					public Boolean doInPreparedStatement(PreparedStatement ps)
							throws SQLException, DataAccessException {
						ps.setDate(1,
								deducteeMasterNonResidential.getApplicableTo() != null
										? new java.sql.Date(deducteeMasterNonResidential.getApplicableTo().getTime())
										: null);
						ps.setString(2, deducteeMasterNonResidential.getAreaLocality());
						ps.setString(3, deducteeMasterNonResidential.getCountry());
						ps.setString(4, deducteeMasterNonResidential.getFlatDoorBlockNo());
						ps.setString(5, deducteeMasterNonResidential.getModifiedBy());
						ps.setDate(6, new java.sql.Date(deducteeMasterNonResidential.getModifiedDate().getTime()));
						ps.setString(7, deducteeMasterNonResidential.getNameBuildingVillage());
						ps.setString(8, deducteeMasterNonResidential.getPinCode());
						ps.setString(9, deducteeMasterNonResidential.getRoadStreetPostoffice());
						ps.setString(10, deducteeMasterNonResidential.getState());
						ps.setString(11, deducteeMasterNonResidential.getTownCityDistrict());
						ps.setString(12, deducteeMasterNonResidential.getDeductorPan());
						ps.setInt(13, deducteeMasterNonResidential.getDeducteeMasterId());
						return ps.execute();
					}
				});
		return deducteeMasterNonResidential;
	}

	/**
	 * 
	 * @param deductorPan
	 * @param modifiedName
	 * @param deducteePAN
	 * @return
	 */
	public List<DeducteeMasterNonResidential> findAllByDeducteePanModifiedName(String deductorPan,
			String modifiedName) {
		return jdbcTemplate.query(String.format(queries.get("find_all_by_deductee_pan_name")),
				new DeducteeMasterNonResidentialRowMapper(), deductorPan, modifiedName);
	}

	/**
	 * 
	 * @param deductorPan
	 * @return
	 */
	public List<DeducteeMasterNonResidential> findAllByDeductorPan(String deductorPan) {
		return jdbcTemplate.query(String.format(queries.get("find_all_nr_deductees_by_deductorpan")),
				new DeducteeMasterNonResidentialRowMapper(), deductorPan);
	}

	public List<CustomDeducteesNonResidentDTO> findAllByDeducteeNamePan(String deductorPan) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		String query = String.format(queries.get("find_all_nr_deductee_name_pan"));
		return namedParameterJdbcTemplate.query(query, parameters,
				new BeanPropertyRowMapper<CustomDeducteesNonResidentDTO>(CustomDeducteesNonResidentDTO.class));
	}

	/**
	 * to get the record count from deductee-non-residential table based on deductee
	 * pan and deductor pan
	 * 
	 * @param deducteePan
	 * @param deductorPan
	 * @return
	 */
	public Long getDeducteeCountBasedOnDeducteePanAndDeductorPan(String deductorPan, String deducteePan) {
		logger.info("DAO method executing to get count of deductee based on pan {}");

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("deducteePan", deducteePan);
		parameters.put("deductorPan", deductorPan);
		return namedParameterJdbcTemplate.queryForObject(
				String.format(queries.get("get_deductee_deducteePan_deductorPan_nonResident")), parameters, Long.class);
	}

	public List<DeducteeMasterNonResidential> findAllByDeductorPanDeducteePan(String deductorPan, String deducteePan) {
		return jdbcTemplate.query(String.format(queries.get("find_all_nr_by_deductor_pan_and_deductee_pan")),
				new Object[] { deductorPan, deducteePan }, new DeducteeMasterNonResidentialRowMapper());
	}

	public Map<String, Integer> getActiveAndInactiveNonResidentDeducteeCounts(String deductorPan, String type) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("type", type);
		String query = "select count(1) from (select distinct deductee_key"
				+ " from Client_Masters.deductee_master_non_residential where deductor_master_pan =:deductorPan and active = 1"
				+ " and applicable_from is not null and CAST(applicable_from AS DATE) <= CONVERT(DATE, GETDATE())"
				+ " and (applicable_to is null or CAST(applicable_to AS DATE) >= CONVERT(DATE, GETDATE()))) as activeCount";
		Integer activeCount = namedParameterJdbcTemplate.queryForObject(query, parameters, Integer.class);
		query = "select count(1) from (select distinct deductee_key"
				+ " from Client_Masters.deductee_master_non_residential where deductor_master_pan =:deductorPan and active = 1"
				+ " and applicable_from is not null and (CAST(applicable_to AS DATE) < CONVERT(DATE, GETDATE()))) as inactiveCount;";
		Integer inactiveCount = namedParameterJdbcTemplate.queryForObject(query, parameters, Integer.class);
		Map<String, Integer> deducteeCounts = new HashMap<>();
		deducteeCounts.put("active", activeCount);
		deducteeCounts.put("inactive", inactiveCount);
		return deducteeCounts;
	}

	/**
	 * 
	 * @param deductorPan
	 * @param deducteeKey
	 * @return
	 */
	public List<DeducteeMasterNonResidential> findAllByDeducteeKey(String deductorPan, String deducteeKey) {
		return jdbcTemplate.query(String.format(queries.get("find_nr_deductee_by_deductee_key")),
				new Object[] { deductorPan, deducteeKey }, new DeducteeMasterNonResidentialRowMapper());
	}

	/**
	 * 
	 * @param deductorPan
	 * @param deducteeCode
	 * @param pagination
	 * @return
	 */
	public List<DeducteeMasterNonResidential> findAllByDeducteeCode(String deductorPan, String deducteeCode,
			Pagination pagination) {
		String paginationOrder = CommonUtil.getPagination("deductee_master_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("deducteeCode", "%" + deducteeCode + "%");
		String query = String.format(queries.get("find_all_by_nr_deductee_code"));
		query = query.concat(paginationOrder);
		return namedParameterJdbcTemplate.query(query, parameters, new DeducteeMasterNonResidentialRowMapper());
	}

	/**
	 * 
	 * @param deductorPan
	 * @param deducteeName
	 * @param deducteeCode
	 * @param pagination
	 * @return
	 */
	public List<DeducteeMasterNonResidential> findAllByDeducteeNameAndCode(String deductorPan, String deducteeName,
			String deducteeCode, Pagination pagination) {
		String paginationOrder = CommonUtil.getPagination("deductee_master_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("deducteeName", "%" + deducteeName + "%");
		parameters.put("deducteeCode", "%" + deducteeCode + "%");
		String query = String.format(queries.get("find_all_by_nr_deductee_name_and_code"));
		query = query.concat(paginationOrder);
		return namedParameterJdbcTemplate.query(query, parameters, new DeducteeMasterNonResidentialRowMapper());
	}

	public List<DeducteeMasterResidential> generateDeducteeNrStaggingFile(String deductorPan) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("deductorPan", deductorPan);
		return namedParameterJdbcTemplate.query(String.format(queries.get("find_all_nr_deductee_stagging_data")),
				parameters, new DeducteeMasterStaggingRowMapper());
	}

	/**
	 * This method for nr batch save.
	 * 
	 * @param nrBatchSave
	 */
	@org.springframework.transaction.annotation.Transactional
	public void nrBatchSave(List<DeducteeMasterNonResidential> nrBatchSave) {
		StopWatch timer = new StopWatch();
		String query = " INSERT INTO Client_Masters.deductee_master_non_residential (deductor_master_pan,deductee_master_10f_applicable_from, deductee_master_10f_applicable_to, deductee_master_10f_file_address, active,"
				+ " additional_sections, advance_transaction_count, applicable_from, applicable_to, area_locality, country, country_of_residence, created_by,created_date,deductee_code,deductee_status,"
				+ " deductor_code, default_rate, email, fixed_based_india, flat_door_block_no, invoice_transaction_count, is_10f_available, is_eligible_for_multiple_sections, is_no_pe_doc_available,"
				+ " is_pe_in_india, is_poem_available, is_trc_available, isdeductee_transparent,isgrossingup, istenf_future, istrc_future,modified_by, modified_date, modified_name, deductee_master_name,"
				+ " name_building_village, name_of_the_company_code, name_of_the_deductee,no_pe_doc_address,nr_rate, deductee_master_pan, pan_status, pan_verified_date, pe_applicable_from, pe_applicable_to,"
				+ " pe_file_address, phone_number, pin_code, poem_applicable_from, poem_applicable_to, provision_transaction_count, rate, related_party, residential_status, road_street_postoffice, [section],"
				+ " source_file_name, source_identifier, state, tenf_future_date, tin_unique_identification, town_city_district, trc_applicable_from, trc_applicable_to, trc_file_address, trc_future_date, user_defined_field_1, user_defined_field_2,"
				+ " user_defined_field_3, fixedbase_availble_india_applicable_from, fixedbase_availble_india_applicable_to, is_amount_connected_fixed_base, is_business_carried_in_india, is_fixedbase_availble_india,"
				+ " is_peamount_document, is_peamount_received, ispeinvoilvedin_purchase_goods, principles_of_business_place, stay_period_financial_year, weather_pe_in_india_applicable_from, weather_pe_in_india_applicable_to, "
				+ " nature_of_payment, deductee_aadhar_number, section_code, additional_section_code, nature_of_remittance, is_poem_declaration, poem_future_date, country_to_remittance, beneficial_owner_of_income, "
				+ " is_beneficial_ownership_of_declaration, mli_ppt_condition_satisifed, mli_slob_condition_satisifed, is_mli_ppt_slob, article_number_dtaa, section_of_incometax_act, aggreement_for_transaction, deductee_key,"
				+ " deductee_enrichment_key, batch_upload_id, deductor_name, is_no_fixed_base_declaration_available, no_fixed_base_declaration_available_in_future, no_fixed_base_declaration_available_future_date,"
				+ " no_fixed_base_declaration_from_date,no_fixed_base_declaration_to_date, tds_exemption_flag, tds_exemption_reason, chartered_accountant_no, gr_or_ir_indicator,tds_applicability_under_section,"
				+ " previous_balance_month,previous_balance_year, current_balance_month, current_balance_year, deductee_master_balances_of_194q, advance_balances_of_194q, provision_balances_of_194q, opening_balance_creditNote, "
				+ " advances_as_of_march, provisions_as_of_march, tds_exemption_number, tds_section_description, deductee_gstin, no_pe_declaration_available_in_future, no_pe_declaration_available_future_date, is_no_poem_declaration_available,"
				+ " no_poem_declaration_applicable_from_date, no_poem_declaration_applicable_to_date, is_threshold_limit_applicable)"
				+ " VALUES (:deductorPan, :tenFApplicableFrom, :tenFApplicableTo, :form10fFileAddress, :active,:additionalSections, :advanceTransactionCount, :applicableFrom, :applicableTo, :areaLocality, :country, :countryOfResidence, :createdBy, "
				+ " :createdDate, :deducteeCode, :deducteeStatus, :deductorCode, :defaultRate, :emailAddress, :fixedBasedIndia, :flatDoorBlockNo, :invoiceTransactionCount, :isTenFAvailable, :isDeducteeHasAdditionalSections, :noPEDocumentAvailable,"
				+ " :whetherPEInIndia, :isPOEMavailable, :isTRCAvailable, :isDeducteeTransparent,:isGrossingUp, :istenfFuture, :istrcFuture, :modifiedBy, :modifiedDate, :modifiedName, :deducteeName, :nameBuildingVillage, :nameOfTheCompanyCode, "
				+ " :nameOfTheDeductee, :noPeDocAddress, :nrRate, :deducteePAN, :panStatus, :panVerifiedDate, :noPEApplicableFrom, :noPEApplicableTo, :peFileAddress, :phoneNumber, :pinCode, :poemApplicableFrom, :poemApplicableTo, :provisionTransactionCount, "
				+ " :rate, :relatedParty, :deducteeResidentialStatus, :roadStreetPostoffice, :section, :sourceFileName, :sourceIdentifier, :state, :tenfFutureDate, :deducteeTin, :townCityDistrict, :trcApplicableFrom, :trcApplicableTo, :trcFileAddress, "
				+ " :trcFutureDate, :userDefinedField1, :userDefinedField2, :userDefinedField3, :fixedbaseAvailbleIndiaApplicableFrom, :fixedbaseAvailbleIndiaApplicableTo, :isAmountConnectedFixedBase, :isBusinessCarriedInIndia, :isFixedbaseAvailbleIndia,"
				+ " :isPEdocument, :isPEamountReceived,  :isPEinvoilvedInPurchaseGoods, :principlesOfBusinessPlace, :stayPeriodFinancialYear, :whetherPEInIndiaApplicableFrom, :whetherPEInIndiaApplicableTo, :natureOfPayment, :deducteeAadharNumber, :sectionCode, "
				+ " :additionalSectionCode, :natureOfRemittance, :isPoemDeclaration, :poemFutureDate, :countryToRemittance, :beneficialOwnerOfIncome, :isBeneficialOwnershipOfDeclaration, :mliPptConditionSatisifed, :mliSlobConditionSatisifed, :isMliPptSlob,"
				+ " :articleNumberDtaa, :sectionOfIncometaxAct, :aggreementForTransaction, :deducteeKey, :deducteeEnrichmentKey, :batchUploadId, :deductorName, :isNoFixedBaseDeclarationAvailable, :noFixedBaseDeclarationAvailableInFuture, "
				+ " :noFixedBaseDeclarationAvailableFutureDate, :noFixedBaseDeclarationFromDate, :noFixedBaseDeclarationToDate, :tdsExemptionFlag, :tdsExemptionReason, :charteredAccountantNo, :grOrIRIndicator, :tdsApplicabilityUnderSection,"
				+ " :previousBalanceMonth, :previousBalanceYear, :currentBalanceMonth, :currentBalanceYear, :deducteeMasterBalancesOf194q, :advanceBalancesOf194q, :provisionBalancesOf194q, :openingBalanceCreditNote, :advancesAsOfMarch, :provisionsAsOfMarch, "
				+ " :tdsExemptionNumber, :tdsSectionDescription, :deducteeGSTIN, :noPEDeclarationAvailableInFuture, :noPEDeclarationAvailableFutureDate, :isNoPOEMDeclarationAvailable, :noPOEMDeclarationApplicableFromDate, :noPOEMDeclarationApplicableToDate,"
				+ " :isThresholdLimitApplicable)";
		timer.start();
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(nrBatchSave);
		namedParameterJdbcTemplate.batchUpdate(query, batch);
		timer.stop();
		logger.info("batchUpdate -> Total time in seconds: {}", timer.getTime());

		logger.info("deductee master non residential batch save size is {}", nrBatchSave.size());

	}

	/**
	 * 
	 * @param batchUpdate
	 */
	@org.springframework.transaction.annotation.Transactional
	public void nrBatchUpdate(List<DeducteeMasterNonResidential> nrBatchUpdate) {
		String updateQuery = " UPDATE Client_Masters.deductee_master_non_residential SET deductee_master_10f_applicable_from = :tenFApplicableFrom, deductee_master_10f_applicable_to = :tenFApplicableTo, "
				+ " deductee_master_10f_file_address = :form10fFileAddress, active = :active, additional_sections = :additionalSections, advance_transaction_count = :advanceTransactionCount, applicable_from = :applicableFrom,"
				+ "	applicable_to = :applicableTo, area_locality = :areaLocality, country = :country, country_of_residence = :countryOfResidence, created_by = :createdBy, default_rate = :defaultRate, email = :emailAddress, "
				+ " fixed_based_india = :fixedBasedIndia, flat_door_block_no = :flatDoorBlockNo, invoice_transaction_count = :invoiceTransactionCount, is_10f_available = :isTenFAvailable, "
				+ " is_eligible_for_multiple_sections = :isDeducteeHasAdditionalSections, is_no_pe_doc_available = :noPEDocumentAvailable, is_pe_in_india = :whetherPEInIndia, is_poem_available = :isPOEMavailable, "
				+ " is_trc_available = :isTRCAvailable, isdeductee_transparent = :isDeducteeTransparent, isgrossingup = :isGrossingUp, istenf_future = :istenfFuture, istrc_future = :istrcFuture, modified_by = :modifiedBy,"
				+ " modified_date = :modifiedDate, modified_name = :modifiedName, name_building_village = :nameBuildingVillage, no_pe_doc_address = :noPeDocAddress, nr_rate = :nrRate, pe_applicable_from = :noPEApplicableFrom,"
				+ " pe_applicable_to = :noPEApplicableTo, pe_file_address = :peFileAddress, phone_number = :phoneNumber, pin_code = :pinCode, poem_applicable_from = :poemApplicableFrom, poem_applicable_to = :poemApplicableTo, "
				+ " provision_transaction_count = :provisionTransactionCount, rate = :rate, related_party = :relatedParty, road_street_postoffice = :roadStreetPostoffice, section = :section, source_file_name = :sourceFileName, "
				+ " source_identifier = :sourceIdentifier, state = :state, tenf_future_date = :tenfFutureDate, tin_unique_identification = :deducteeTin, town_city_district = :townCityDistrict, trc_applicable_from = :trcApplicableFrom, "
				+ " trc_applicable_to = :trcApplicableTo, trc_file_address = :trcFileAddress, trc_future_date = :trcFutureDate, user_defined_field_1 = :userDefinedField1, user_defined_field_2 = :userDefinedField2,"
				+ " user_defined_field_3 = :userDefinedField3, fixedbase_availble_india_applicable_from = :fixedbaseAvailbleIndiaApplicableFrom, fixedbase_availble_india_applicable_to = :fixedbaseAvailbleIndiaApplicableTo,"
				+ "	is_amount_connected_fixed_base = :isAmountConnectedFixedBase, is_business_carried_in_india = :isBusinessCarriedInIndia, is_fixedbase_availble_india = :isFixedbaseAvailbleIndia,"
				+ "	is_peamount_document = :isPEdocument, is_peamount_received = :isPEamountReceived, ispeinvoilvedin_purchase_goods = :isPEinvoilvedInPurchaseGoods, principles_of_business_place = :principlesOfBusinessPlace, "
				+ " stay_period_financial_year = :stayPeriodFinancialYear, weather_pe_in_india_applicable_from = :whetherPEInIndiaApplicableFrom, weather_pe_in_india_applicable_to = :whetherPEInIndiaApplicableTo, "
				+ " nature_of_payment = :natureOfPayment, deductee_aadhar_number = :deducteeAadharNumber, is_poem_declaration = :isPoemDeclaration, poem_future_date = :poemFutureDate, country_to_remittance = :countryToRemittance,"
				+ "	beneficial_owner_of_income = :beneficialOwnerOfIncome, is_beneficial_ownership_of_declaration = :isBeneficialOwnershipOfDeclaration, mli_ppt_condition_satisifed = :mliPptConditionSatisifed,"
				+ "	mli_slob_condition_satisifed = :mliSlobConditionSatisifed, is_mli_ppt_slob = :isMliPptSlob, nature_of_remittance = :natureOfRemittance ,article_number_dtaa = :articleNumberDtaa,"
				+ "	section_of_incometax_act = :sectionOfIncometaxAct, aggreement_for_transaction = :aggreementForTransaction, section_code = :sectionCode, additional_section_code = :additionalSectionCode, "
				+ " deductee_key = :deducteeKey, deductee_enrichment_key = :deducteeEnrichmentKey, batch_upload_id = :batchUploadId, is_no_fixed_base_declaration_available = :isNoFixedBaseDeclarationAvailable, "
				+ " no_fixed_base_declaration_available_in_future = :noFixedBaseDeclarationAvailableInFuture , no_fixed_base_declaration_available_future_date = :noFixedBaseDeclarationAvailableFutureDate, "
				+ " no_fixed_base_declaration_from_date = :noFixedBaseDeclarationFromDate, no_fixed_base_declaration_to_date = :noFixedBaseDeclarationToDate, tds_exemption_flag = :tdsExemptionFlag, tds_exemption_reason = :tdsExemptionReason,"
				+ " chartered_accountant_no = :charteredAccountantNo, gr_or_ir_indicator = :grOrIRIndicator, tds_applicability_under_section = :tdsApplicabilityUnderSection, previous_balance_month = :previousBalanceMonth, "
				+ " previous_balance_year = :previousBalanceYear, current_balance_month = :currentBalanceMonth, current_balance_year = :currentBalanceYear, deductee_master_balances_of_194q = :deducteeMasterBalancesOf194q, "
				+ " advance_balances_of_194q = :advanceBalancesOf194q, provision_balances_of_194q = :provisionBalancesOf194q, opening_balance_creditNote = :openingBalanceCreditNote, advances_as_of_march = :advancesAsOfMarch, "
				+ " provisions_as_of_march = :provisionsAsOfMarch, deductee_tan= :deducteeTan, tds_exemption_number = :tdsExemptionNumber, tds_section_description = :tdsSectionDescription,"
				+ " no_pe_declaration_available_in_future = :noPEDeclarationAvailableInFuture, no_pe_declaration_available_future_date = :noPEDeclarationAvailableFutureDate, is_no_poem_declaration_available = :isNoPOEMDeclarationAvailable,"
				+ " no_poem_declaration_applicable_from_date = :noPOEMDeclarationApplicableFromDate, no_poem_declaration_applicable_to_date = :noPOEMDeclarationApplicableToDate, deductee_gstin = :deducteeGSTIN, is_threshold_limit_applicable = :isThresholdLimitApplicable"
				+ "	WHERE deductee_master_id = :deducteeMasterId ; ";
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(nrBatchUpdate);
		namedParameterJdbcTemplate.batchUpdate(updateQuery, batch);
		logger.info("deductee master non residential batch update size is {}", nrBatchUpdate.size());
	}

	/**
	 * 
	 * @param batchUpdateApplicableTo
	 */
	@org.springframework.transaction.annotation.Transactional
	public void nrBatchUpdateApplicableTo(List<DeducteeMasterNonResidential> nrBatchUpdateApplicableTo) {
		String updateQuery = " UPDATE Client_Masters.deductee_master_non_residential SET applicable_to = :applicableTo, active = 0 WHERE deductee_master_id = :deducteeMasterId ";
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(nrBatchUpdateApplicableTo);
		namedParameterJdbcTemplate.batchUpdate(updateQuery, batch);
		logger.info("deductee master non residential batch update applicableTo size is {}",
				nrBatchUpdateApplicableTo.size());
	}

	public List<DeducteeMasterNonResidential> getNRDeductees(String deductorPan) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		String query = "SELECT * FROM Client_Masters.deductee_master_non_residential WHERE deductor_master_pan =:deductorPan AND active = 1";
		return namedParameterJdbcTemplate.query(query, parameters, new DeducteeMasterNonResidentialRowMapper());
	}

}
