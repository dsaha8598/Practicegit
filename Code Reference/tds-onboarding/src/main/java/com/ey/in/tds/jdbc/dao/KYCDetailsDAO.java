package com.ey.in.tds.jdbc.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.onboarding.jdbc.dto.KYCDetails;
import com.ey.in.tds.common.onboarding.jdbc.dto.KYCDetailsControlOutput;
import com.ey.in.tds.jdbc.rowmapper.KYCControlOutputRowMapper;
import com.ey.in.tds.jdbc.rowmapper.KYCDetailsRowMapper;

/**
 *
 * @author vamsir
 *
 */
@Repository
public class KYCDetailsDAO {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private HashMap<String, String> queries;

	@Autowired
	private DataSource dataSource;
	private JdbcTemplate jdbcTemplate;
	private SimpleJdbcInsert jdbcInsert;
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@PostConstruct
	private void postConstruct() {
		jdbcTemplate = new JdbcTemplate(dataSource);
		namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		jdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("kyc_details").withSchemaName("Onboarding")
				.usingGeneratedKeyColumns("id");
	}

	/**
	 * This method for save kyc deatails
	 *
	 * @param kycDetails
	 */
	public KYCDetails save(KYCDetails kycDetails) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(kycDetails);
		kycDetails.setId(jdbcInsert.executeAndReturnKey(namedParameters).intValue());
		logger.info("Record inserted to kyc details table {}");
		return kycDetails;

	}

	/**
	 * This method for get kyc details based on id
	 *
	 * @param deductorPan
	 * @param id
	 * @return
	 */
	public List<KYCDetails> getKycDetails(String deductorPan, String tan, Integer id, String type) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("tan", tan);
		parameters.put("id", id);
		parameters.put("type", type);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_kyc_by_id")), parameters,
				new KYCDetailsRowMapper());
	}

	/**
	 *
	 * @param deductorPan
	 * @param type
	 * @return
	 */
	public List<KYCDetails> getKycDetailsList(String deductorPan, String tan, String type) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("tan", tan);
		parameters.put("type", type);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_all_kyc")), parameters,
				new KYCDetailsRowMapper());
	}

	/**
	 * @param deductorPan
	 * @param tan
	 * @param type
	 * @return
	 */
	public List<KYCDetails> getKycDetailsListForZip(String deductorPan, String tan, String type) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("tan", tan);
		parameters.put("type", type);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_all_kyc_zip")), parameters,
				new KYCDetailsRowMapper());
	}

	/**
	 *
	 * @param deductorPan
	 * @param year
	 * @param type
	 * @return
	 */
	public List<KYCDetails> getKycActionList(String deductorPan, String tan, int year, String type) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("tan", tan);
		parameters.put("year", year);
		parameters.put("type", type);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_kyc_tds_vs_tcs_action_list")),
				parameters, new KYCDetailsRowMapper());
	}

	/**
	 * 
	 * @param deductorPan
	 * @param tan
	 * @param year
	 * @param type
	 * @return
	 */
	public List<KYCDetails> getKycActionFinalReportList(String deductorPan, String tan, int year, String type) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("tan", tan);
		parameters.put("year", year);
		parameters.put("type", type);
		return namedParameterJdbcTemplate.query(
				String.format(queries.get("get_kyc_tds_vs_tcs_action_final_respone_list")), parameters,
				new KYCDetailsRowMapper());
	}

	/**
	 *
	 * @param deductorPan
	 * @param type
	 * @return
	 */
	public List<KYCDetails> getKycByTanAndPanAndYear(String deductorPan, String tan, int year, String type) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("tan", tan);
		parameters.put("year", year);
		parameters.put("type", type);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_kyc_by_year")), parameters,
				new KYCDetailsRowMapper());
	}

	/**
	 *
	 * @param deductorPan
	 * @param tan
	 * @param year
	 * @param kycType
	 * @return
	 */
	public List<KYCDetails> getKycSurvyReportList(String deductorPan, String tan, int year, String type) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("tan", tan);
		parameters.put("year", year);
		parameters.put("type", type);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_kyc_survy_report_list")), parameters,
				new KYCDetailsRowMapper());
	}

	/**
	 * This method for update kyc details
	 *
	 * @param kycDetails
	 */
	public int update(KYCDetails kycDetails) {
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(kycDetails);
		int status = namedParameterJdbcTemplate.update(String.format(queries.get("kyc_update")), namedParameters);
		return status;
	}

	/**
	 *
	 * @param deductorPan
	 * @param tan
	 * @param deducteePan
	 * @return
	 */
	public List<KYCDetails> getKycDetailsBasedOnPan(String deductorPan, String tan, String pan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("tan", tan);
		parameters.put("pan", pan);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_kyc_based_on_deductee_pan")), parameters,
				new KYCDetailsRowMapper());
	}

	/**
	 * This method for batch update in kyc details
	 *
	 * @param kycDetailsList
	 */
	@org.springframework.transaction.annotation.Transactional
	public void batchUpdate(List<KYCDetails> kycDetailsList) {
		String updateQuery = "UPDATE Onboarding.kyc_details SET tds_tcs_client_final_response = :tdsTcsClientFinalResponse,"
				+ " tcs_tds_applicability_user_action = :tcsTdsApplicabilityUserAction WHERE id = :id ";
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(kycDetailsList);
		namedParameterJdbcTemplate.batchUpdate(updateQuery, batch);
		logger.info("kyc details batch updated successfully {}", kycDetailsList.size());

	}

	/**
	 * This method for batch update in kyc details
	 *
	 * @param kycDetailsList
	 */
	@org.springframework.transaction.annotation.Transactional
	public void batchUpdateFinalResponse(List<KYCDetails> kycDetailsList) {
		String updateQuery = "UPDATE Onboarding.kyc_details SET higher_tcs_rate_applicable_conclusion = :higherTcsRateApplicableFinalConclusion,"
				+ " final_rate_user_action = :finalRateUserAction WHERE id = :id ";
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(kycDetailsList);
		namedParameterJdbcTemplate.batchUpdate(updateQuery, batch);
		logger.info("kyc details batchUpdateFinalResponse updated successfully {}", kycDetailsList.size());

	}

	/**
	 *
	 * @param deductorPan
	 * @param tan
	 * @param kycType
	 * @return
	 */
	public List<KYCDetails> getFailedResponseReport(String deductorPan, String tan, int year, String kycType) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("tan", tan);
		parameters.put("year", year);
		parameters.put("type", kycType);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_failed_response_report")), parameters,
				new KYCDetailsRowMapper());
	}

	/**
	 *
	 * @param kycIdList
	 * @param deductorTan
	 * @param deductorPan
	 */
	public int batchUpdateBasicEmailSent(List<Integer> kycIdList, String deductorPan, String deductorTan, String type) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String query = String.format(queries.get("batch_update_basic_email_sent"));
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("kycIdList", kycIdList);
		parameters.addValue("deductorPan", deductorPan);
		parameters.addValue("deductorTan", deductorTan);
		parameters.addValue("type", type);
		return namedParameterJdbcTemplate.update(query, parameters);
	}

	/**
	 *
	 * @param customerName
	 * @param customerCode
	 * @param deductorPan
	 * @param type
	 * @param deductorTan
	 * @return
	 */
	public List<KYCDetails> getKycListByNameAndCode(String customerName, String customerCode, String deductorPan,
			String tan, String type) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("tan", tan);
		parameters.put("customerName", customerName);
		parameters.put("customerCode", customerCode);
		parameters.put("type", type);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_kyc_by_name_code")), parameters,
				new KYCDetailsRowMapper());
	}

	/**
	 * 
	 * @param customerName
	 * @param customerCode
	 * @param deductorPan
	 * @param tan
	 * @param type
	 * @return
	 */
	public List<KYCDetails> getKycListByAndCode(String customerCode, String deductorPan, String tan, String type) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("tan", tan);
		parameters.put("customerCode", customerCode);
		parameters.put("type", type);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_kyc_by_code")), parameters,
				new KYCDetailsRowMapper());
	}

	/**
	 * 
	 * @param deductorPan
	 * @param tan
	 * @param type
	 * @return
	 */

	public List<KYCDetailsControlOutput> getKYCControlOutput(String deductorPan, String tan, int year, String type) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("pan", deductorPan);
		parameters.put("tan", tan);
		parameters.put("year", year);
		parameters.put("type", type);
		if ("VENDOR".equalsIgnoreCase(type)) {
			return namedParameterJdbcTemplate.query(String.format(queries.get("get_kyc_control_output_tds")),
					parameters, new KYCControlOutputRowMapper());
		} else {
			return namedParameterJdbcTemplate.query(String.format(queries.get("get_kyc_control_output_tcs")),
					parameters, new KYCControlOutputRowMapper());
		}
	}

	/**
	 *
	 * @param deductorPan
	 * @param year
	 * @param type
	 * @return
	 */
	public List<KYCDetails> getKycTCSOrTDSList(String deductorPan, String tan, int year, String flag, String type) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("tan", tan);
		parameters.put("year", year);
		parameters.put("flag", flag);
		parameters.put("type", type);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_kyc_tcs_tds_list")), parameters,
				new KYCDetailsRowMapper());
	}

	/**
	 *
	 * @param deductorPan
	 * @param year
	 * @param type
	 * @return
	 */
	public List<KYCDetails> getKycListTypeWise(String deductorPan, String tan, int year, String type) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("tan", tan);
		parameters.put("year", year);
		parameters.put("type", type);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_kyc_type_wise_list")), parameters,
				new KYCDetailsRowMapper());
	}

	/**
	 * 
	 * @param kycList
	 * @param tenantId
	 */
	public void batchSaveKycDetails(List<KYCDetails> kycList, String tenantId) {
		MultiTenantContext.setTenantId(tenantId);
		StopWatch timer = new StopWatch();
		String sql = "INSERT INTO Onboarding.kyc_details (customer_name, customer_pan, email_id ,is_kyc_submitted,deductor_master_tan, deductor_pan, active, created_by,"
				+ " created_date, modified_by, modified_date, phone_number, customer_code,is_form_submitted, [year] ,is_email_triggered,[type],batch_upload_id)"
				+ " VALUES (:customerName, :customerPan, :emailId ,:isKycSubmitted,:deductorMasterTan, :deductorPan, :active, :createdBy,"
				+ " :createdDate, :modifiedBy, :modifiedDate, :phoneNumber, :customerCode, :isFormSubmitted, :year, :isEmailTriggered, :type, :batchUploadId);";

		List<MapSqlParameterSource> params = new ArrayList<MapSqlParameterSource>();

		for (KYCDetails kyc : kycList) {
			MapSqlParameterSource source = new MapSqlParameterSource();
			source.addValue("customerName", kyc.getCustomerName());
			source.addValue("customerPan", kyc.getCustomerPan());
			source.addValue("emailId", kyc.getEmailId());
			source.addValue("isKycSubmitted", kyc.getIsKycSubmitted());
			source.addValue("deductorMasterTan", kyc.getDeductorMasterTan());
			source.addValue("deductorPan", kyc.getDeductorPan());
			source.addValue("active", kyc.getActive());
			source.addValue("createdBy", kyc.getCreatedBy());
			source.addValue("createdDate", kyc.getCreatedDate());
			source.addValue("modifiedBy", kyc.getModifiedBy());
			source.addValue("modifiedDate", kyc.getModifiedDate());
			source.addValue("phoneNumber", kyc.getPhoneNumber());
			source.addValue("customerCode", kyc.getCustomerCode());
			source.addValue("isFormSubmitted", kyc.getIsFormSubmitted());
			source.addValue("year", kyc.getYear());
			source.addValue("isEmailTriggered", kyc.getIsEmailTriggered());
			source.addValue("type", kyc.getType());
			source.addValue("batchUploadId", kyc.getBatchUploadId());
			params.add(source);
		}
		timer.start();
		namedParameterJdbcTemplate.batchUpdate(sql, params.stream().toArray(MapSqlParameterSource[]::new));
		timer.stop();
		logger.info("batchUpdate -> Total time in seconds: {}", timer.getTime());
		logger.info("inserted kyc total records :{} ", kycList.size());

	}

	/**
	 * This method for batch update in kyc details
	 *
	 * @param kycDetailsList
	 */
	@org.springframework.transaction.annotation.Transactional
	public void batchUpdateKycDetails(List<KYCDetails> kycDetailsList) {
		String updateQuery = "UPDATE Onboarding.kyc_details SET email_id = :emailId, is_email_triggered = :isEmailTriggered,"
				+ " batch_upload_id = :batchUploadId, [type] = :type , phone_number = :phoneNumber, customer_name = :customerName, "
				+ "customer_pan = :customerPan ,"
				+ " accept_terms_and_conditions = null, acknowledgement_itr_year1 = null, "
				+ "	acknowledgement_itr_year2 = null, acknowledgement_itr_year3 = null, aggregate_tcs_or_tds_greater_than50k_for_year1 = null, "
				+ "	aggregate_tcs_or_tds_greater_than50k_for_year2 = null , aggregate_tcs_or_tds_greater_than50k_for_year3 = null, "
				+ "	is_authorized_person = null , is_form_submitted = 0, is_kyc_submitted = 0, is_pan_exists = null, "
				+ " kyc_pan = null, tan_applicable = null, tan_number = null, itr_attachment_year1 = null,itr_financial_year1  = null, "
				+ "	itr_financial_year2 = null, itr_financial_year3 = null, turnover_exceed_10cr = null, tds_tcs_client_final_response = null, "
				+ "	tds_tcs_applicability_indicator = null, tcs_tds_applicability_user_action = null, itr_attachment_year2  = null, "
				+ "	itr_attachment_year3 = null, signed_designation = null, signed_email_id = null, "
				+ "	signed_name = null, signed_name_for_no = null, signed_mobile_number = null, kyc_remarks = null, "
				+ " pan_file_path = null, fs_file_path = null, is_tan_files = 0, indemnify_declare = 0, yes_signed_email_id = null WHERE id = :id ";

		String updateTanFilesQuery = "UPDATE Onboarding.declaration_tan_files set active = 0 where kyc_id = :id";

		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(kycDetailsList);
		namedParameterJdbcTemplate.batchUpdate(updateQuery, batch);

		SqlParameterSource[] tanFilesbatch = SqlParameterSourceUtils.createBatch(kycDetailsList);
		namedParameterJdbcTemplate.batchUpdate(updateTanFilesQuery, tanFilesbatch);

		logger.info("kyc details batch updated successfully {}", kycDetailsList.size());
	}

	/**
	 * 
	 * @param batchUploadId
	 * @param tenantId
	 * @param deductorPan
	 * @param deductorTan
	 * @param string
	 * @return
	 */
	public List<KYCDetails> getKycListBasedOnBatchId(int batchUploadId, String type, String deductorTan,
			String deductorPan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("batchUploadId", batchUploadId);
		parameters.put("deductorPan", deductorPan);
		parameters.put("deductorTan", deductorTan);
		parameters.put("type", type);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_all_kyc_based_on_batch_id")), parameters,
				new KYCDetailsRowMapper());
	}

	/**
	 * 
	 * @param kyc
	 * @param deductorTan
	 * @param deductorPan
	 */
	public int updateRediesKey(KYCDetails kyc, String deductorPan, String deductorTan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String query = String.format(queries.get("update_redies_kyc_and_basic_sent_date"));
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("id", kyc.getId());
		parameters.addValue("redisKey", kyc.getRedisKey());
		parameters.addValue("basisEmailSent", kyc.getBasisEmailSent());
		parameters.addValue("deductorPan", deductorPan);
		parameters.addValue("deductorTan", deductorTan);
		return namedParameterJdbcTemplate.update(query, parameters);

	}

	/**
	 * 
	 * @param kycDetailsList
	 */
	@org.springframework.transaction.annotation.Transactional
	public void batchUpdateForRedisKeys(List<KYCDetails> kycDetailsList) {
		String updateQuery = "UPDATE Onboarding.kyc_details SET is_email_triggered = 1, basis_email_sent = :basisEmailSent WHERE id = :id";
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(kycDetailsList);
		namedParameterJdbcTemplate.batchUpdate(updateQuery, batch);
		logger.info("key details basic send date batch updated successfully {}", kycDetailsList.size());
	}

	/**
	 * 
	 * @param customerName
	 * @param customerCode
	 * @param deductorPan
	 * @param tan
	 * @param type
	 * @return
	 */
	public List<KYCDetails> getKycListByCustomerCode(List<String> customerCodes, String deductorPan, String tan,
			String type) {
		String tenantId = MultiTenantContext.getTenantId();
		String querie = String.format(queries.get("get_kyc_list_basedon_codes"));
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("tan", tan);
		parameters.put("customerCodes", customerCodes);
		parameters.put("type", type);
		return namedParameterJdbcTemplate.query(querie, parameters, new KYCDetailsRowMapper());
	}

	/**
	 * 
	 * @param deductorPan
	 * @param deductorTan
	 * @param type
	 * @return
	 */
	public Integer getPanMismatchCount(String deductorPan, String deductorTan, String type) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("deductorTan", deductorTan);
		parameters.put("type", type);
		return namedParameterJdbcTemplate.queryForObject(String.format(queries.get("get_pan_mismatch_count")),
				parameters, Integer.class);
	}

	/**
	 *
	 * @param deductorPan
	 * @param type
	 * @return
	 */
	public List<KYCDetails> getAllMailTriggeredReport(String deductorPan, String tan, int year, String type) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("tan", tan);
		parameters.put("year", year);
		parameters.put("type", type);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_all_kyc_mail_triggered")), parameters,
				new KYCDetailsRowMapper());
	}

	/**
	 *
	 * @param deductorPan
	 * @param type
	 * @return
	 */
	public List<KYCDetails> getAllMailNotTriggeredReport(String deductorPan, String tan, int year, String type) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("tan", tan);
		parameters.put("year", year);
		parameters.put("type", type);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_all_kyc_mail_not_triggered")),
				parameters, new KYCDetailsRowMapper());
	}

	/**
	 * 
	 * @param deductorPan
	 * @param tan
	 * @param year
	 * @param type
	 * @return
	 */
	public List<KYCDetails> getKycHoldAndNewControlReport(String deductorPan, String tan, int year, String type) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("tan", tan);
		parameters.put("year", year);
		parameters.put("type", type);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_kyc_tds_vs_tcs_action_list")),
				parameters, new KYCDetailsRowMapper());
	}

}
