package com.ey.in.tds.jdbc.dao;

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
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.onboarding.jdbc.dto.ShareholderDeclarationRateType;
import com.ey.in.tds.common.onboarding.jdbc.dto.ShareholderMasterResidential;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.jdbc.rowmapper.ShareholderDeclarationRateTypeRowMapper;
import com.ey.in.tds.jdbc.rowmapper.ShareholderMasterResidentialRowMapper;

/**
 * class contains logic for db operation with shareholder_master_residential
 * table
 * 
 * @author dipak
 *
 */
@Repository
public class ShareholderMasterResidentialDAO {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired(required = false)
	private HashMap<String, String> queries;

	private SimpleJdbcInsert simpleJdbcInsert;

	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	private DataSource dataSource;

	@PostConstruct
	private void postConstruct() {
		namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("shareholder_master_residential")
				.withSchemaName("Client_Masters").usingGeneratedKeyColumns("shareholder_master_id");

	}

	/**
	 * inserts record in shareholder_master_residential table
	 * 
	 * @param dto
	 * @return
	 */
	public ShareholderMasterResidential save(ShareholderMasterResidential dto) {
		logger.info("insert method execution started  {}");

		Map<String, Object> namedParameters = new HashMap<>();
		namedParameters.put("deductor_master_pan", dto.getDeductorPan());
		// namedParameters.put("shareholder_master_id", dto);
		namedParameters.put("assessment_year_dividend_details", dto.getStringAssesmentYearDividendDetails());
		namedParameters.put("last_payout_invoice_id", dto.getLastPayoutId());
		namedParameters.put("match_score", dto.getMatchScore());
		namedParameters.put("name_as_per_traces", dto.getNameAsPerTraces());
		namedParameters.put("pan_as_per_traces", dto.getPanAsPerTraces());
		namedParameters.put("remarks_as_per_traces", dto.getRemarksAsPerTraces());
		namedParameters.put("area_locality", dto.getAreaLocality());
		namedParameters.put("category", dto.getShareholderCategory());
		namedParameters.put("contact", dto.getContact());
		namedParameters.put("country", dto.getCountry());
		namedParameters.put("created_by", dto.getCreatedBy());
		namedParameters.put("created_date", dto.getCreatedDate());
		namedParameters.put("demat_account_no", dto.getDematAccountNo());
		namedParameters.put("email_id", dto.getEmailId());
		namedParameters.put("flat_door_block_no", dto.getFlatDoorBlockNo());
		namedParameters.put("folio_no", dto.getFolioNo());
		namedParameters.put("form15gh_available", dto.getForm15ghAvailable());
		namedParameters.put("form15gh_file_address", dto.getForm15ghFileAddress());
		namedParameters.put("form15gh_unique_identification_no", dto.getForm15ghUniqueIdentificationNo());
		namedParameters.put("key_shareholder", dto.getKeyShareholder());
		namedParameters.put("modified_by", dto.getModifiedBy());
		namedParameters.put("modified_date", dto.getModifiedDate());
		namedParameters.put("name", dto.getShareholderName());
		namedParameters.put("name_building_village", dto.getNameBuildingVillage());
		namedParameters.put("pan", dto.getShareholderPan());
		namedParameters.put("pan_status", dto.getPanStatus());
		namedParameters.put("pan_verified_date", dto.getPanVerifiedDate());
		namedParameters.put("percetage_of_share_held", dto.getPercentageSharesHeld());
		namedParameters.put("pin_code", dto.getPinCode());
		namedParameters.put("road_street_postoffice", dto.getRoadStreetPostoffice());
		namedParameters.put("share_transfer_agent_name", dto.getShareTransferAgentName());
		namedParameters.put("source_file_name", dto.getSourceFileName());
		namedParameters.put("source_identifier", dto.getSourceIdentifier());
		namedParameters.put("state", dto.getState());
		namedParameters.put("total_of_shares_held", dto.getTotalSharesHeld());
		namedParameters.put("town_city_district", dto.getTownCityDistrict());
		namedParameters.put("type", dto.getShareholderType());
		namedParameters.put("transaction_count", dto.getTransactionCount());
		namedParameters.put("residential_status", dto.getShareholderResidentialStatus());
		namedParameters.put("active", dto.getActive()==true?1:0);
		namedParameters.put("aadhar_number", dto.getAadharNumber());
		namedParameters.put("unique_shareholder_identification_number", dto.getUniqueIdentificationNumber());
		dto.setId(simpleJdbcInsert.executeAndReturnKey(namedParameters).intValue());

		logger.info("Record inserted to shareholder_master_residential table {}");
		return dto;
	}

	/**
	 * to get share resident share holder records count
	 * 
	 * @param deductorPan
	 * @param pagination
	 * @return
	 */
	public List<ShareholderMasterResidential> findAllByPanTenantIdAndName(String deductorPan, String shareholderName,
			Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		logger.info("DAO method executing to get all resident share holder records {}");
		String paginationOrder = CommonUtil.getPagination("shareholder_master_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");
		String query = String.format(queries.get("get_resident_shareHolder_by_deductorPan"));
		if (!shareholderName.equals("noshareholderfilter")) {
			query = query + " AND name =:name";
		}
		query = query.concat(paginationOrder);
		Map<String, Object> parameter = new HashMap<>();
		parameter.put("deductorPan", deductorPan);
		parameter.put("name", shareholderName);
		return namedParameterJdbcTemplate.query(query, parameter, new ShareholderMasterResidentialRowMapper());

	}

	/**
	 * to get the count by deductor pan
	 * 
	 * @param deductorPan
	 * @param pagination
	 * @return
	 */
	public BigInteger getCountByPanTenantId(String deductorPan, String shareholderName) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		logger.info("DAO method executing to get all resident share holder records count{}");
		String query = String.format(queries.get("get_count_of_resident_shareHolder_by_deductorPan"));
		if (!shareholderName.equals("noshareholderfilter")) {
			query = query + " AND name =:name";
		}
		Map<String, Object> parameter = new HashMap<>();
		parameter.put("deductorPan", deductorPan);
		parameter.put("name", shareholderName);
		return namedParameterJdbcTemplate.queryForObject(query, parameter, BigInteger.class);

	}

	/**
	 * to get share holder master based on id
	 * 
	 * @param id
	 * @param deductorPan
	 * @return
	 */
	public ShareholderMasterResidential findById(Integer id, String deductorPan) {
		logger.info("DAO method executing to get share holder master with id=" + id + "{}");
		Map<String, Object> parameter = new HashMap<>();
		parameter.put("deductorPan", deductorPan);
		parameter.put("id", id);
		return namedParameterJdbcTemplate.queryForObject(
				String.format(queries.get("get_resident_shareHolder_by_deductorPan_and_id")), parameter,
				new ShareholderMasterResidentialRowMapper());

	}

	/**
	 * to update the shareholder_master_residential
	 * 
	 * @param dto
	 * @return
	 */
	public ShareholderMasterResidential update(ShareholderMasterResidential dto) {
		logger.info("DAO method executing to update user data ");

		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(dto);
		int status = namedParameterJdbcTemplate.update(String.format(queries.get("update_resident_shareHolder_by_id")),
				namedParameters);

		if (status != 0) {
			logger.info("shareholder_master_residential data is updated for ID " + dto.getId());
		} else {
			logger.info("No record found with ID " + dto.getId());
		}
		logger.info("DAO method execution successful to update ShareholderMasterResidential {}");
		return dto;
	}

	// gets the count of folio no present for the deductor pan
	public BigInteger getShareholderByFolioNumberPan(String deductorPan, String folioNo) {
		logger.debug("tenant id : {}", MultiTenantContext.getTenantId());
		logger.info("DAO method executing to get cont of folio no for resident{}");
		Map<String, Object> parameter = new HashMap<>();
		parameter.put("deductorPan", deductorPan);
		parameter.put("folioNo", folioNo);
		return namedParameterJdbcTemplate.queryForObject(queries.get("get_resident_folio_no_count"), parameter,
				BigInteger.class);

	}

	public List<ShareholderMasterResidential> getAllResidentShareholderByPanTenantId(String deductorPan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameter = new HashMap<>();
		parameter.put("deductorPan", deductorPan);
		return namedParameterJdbcTemplate.query(queries.get("get_All_Resident_Shareholder_By_Pan_TenantId"), parameter,
				new ShareholderMasterResidentialRowMapper());
	}

	public List<ShareholderMasterResidential> getResidentShareholdersByPan(String deductorPan, String shareHolderPan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameter = new HashMap<>();
		parameter.put("deductorPan", deductorPan);
		parameter.put("shareHolderPan", shareHolderPan);
		return namedParameterJdbcTemplate.query(queries.get("get_resident_shareholders_by_pan"), parameter,
				new ShareholderMasterResidentialRowMapper());
	}

	public List<ShareholderMasterResidential> getResidentShareholderByFolioNumberPanTenantId(String deductorPan,
			String tenantId, String folioNo) {
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameter = new HashMap<>();
		parameter.put("deductorPan", deductorPan);
		parameter.put("folioNo", folioNo);
		return namedParameterJdbcTemplate.query(queries.get("get_Resident_Shareholder_By_FolioNumber_PanTenantId"),
				parameter, new ShareholderMasterResidentialRowMapper());
	}

	/**
	 * to get list of pans from residential Shareholder table
	 * 
	 * @param deducteePan
	 * @param deductorPan
	 * @return
	 */
	public Long getShareholderBasedOnShareholderPanAndDeductorPan(String shareholderPan, String deductorPan) {
		logger.info("DAO method executing to get list of Shareholder based on pan {}");

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("shareholderPan", shareholderPan);
		parameters.put("deductorPan", deductorPan);
		return namedParameterJdbcTemplate.queryForObject(
				String.format(queries.get("get_resident_shareholder_shareholderPan_deductorPan")), parameters,
				Long.class);
	}

	public Map<String, Integer> getActiveAndInactiveResidentShareHoldersCounts(String deductorPan, String type) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		String query = "select count(1) as activeCount from Client_Masters.shareholder_master_residential where deductor_master_pan =:deductorPan AND active =1 ";
		Integer activeCount = namedParameterJdbcTemplate.queryForObject(query, parameters, Integer.class);
		query = "select count(1) as inactiveCount from Client_Masters.shareholder_master_residential where deductor_master_pan =:deductorPan AND active =0";
		Integer inactiveCount = namedParameterJdbcTemplate.queryForObject(query, parameters, Integer.class);
		Map<String, Integer> deducteeCounts = new HashMap<>();
		deducteeCounts.put("active", activeCount);
		deducteeCounts.put("inactive", inactiveCount);
		return deducteeCounts;
	}

	@Transactional
	public void batchSaveShareHolderResident(List<ShareholderMasterResidential> shareholderList) {

		try {
		String query = "INSERT INTO Client_Masters.shareholder_master_residential (deductor_master_pan,user_tenant_id,assessment_year_dividend_details,\n"
				+ "last_payout_invoice_id,match_score,name_as_per_traces,pan_as_per_traces,remarks_as_per_traces,area_locality,category,contact,country,created_by,\n"
				+ "created_date,demat_account_no,email_id,flat_door_block_no,folio_no,form15gh_available,form15gh_file_address,form15gh_unique_identification_no,\n"
				+ "key_shareholder,modified_by,modified_date,name,name_building_village,pan,pan_status,pan_verified_date,percetage_of_share_held,pin_code,residential_status,\n"
				+ "road_street_postoffice,share_transfer_agent_name,source_file_name,source_identifier,state,total_of_shares_held,town_city_district,[type],transaction_count,\n"
				+ "version,active,aadhar_number,unique_shareholder_identification_number,batch_upload_id) VALUES(:deductorPan,:tenantId,:stringAssesmentYearDividendDetails,\n"
				+ ":lastPayoutId,:matchScore,:nameAsPerTraces,:panAsPerTraces,:remarksAsPerTraces,:areaLocality,:shareholderCategory,:contact,:country,:createdBy,\n"
				+ ":createdDate,:dematAccountNo,:emailId,:flatDoorBlockNo,:folioNo,:form15ghAvailable,:form15ghFileAddress,:form15ghUniqueIdentificationNo,\n"
				+ ":keyShareholder,:modifiedBy,:modifiedDate,:shareholderName,:nameBuildingVillage,:shareholderPan,:panStatus,:panVerifiedDate,:percentageSharesHeld,:pinCode,:shareholderResidentialStatus,\n"
				+ ":roadStreetPostoffice,:shareTransferAgentName,:sourceFileName,:sourceIdentifier,:state,:totalSharesHeld,:townCityDistrict,:shareholderType,:transactionCount,\n"
				+ ":version,1,:aadharNumber,:uniqueIdentificationNumber,:batchUploadId)";
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(shareholderList);
		namedParameterJdbcTemplate.batchUpdate(query, batch);
		logger.info("shareholder records saved with size {}", shareholderList.size());
		}catch(Exception e) {
			logger.info("Exception occured while saving record "+e);
			throw new RuntimeException();
		}

	}

	public List<String> getListOfFolioNo(String deductorPan) {
		logger.info("Retrieving list of resident folio numbers {}");
		Map<String, String> map = new HashMap<>();
		map.put("pan", deductorPan);
		return namedParameterJdbcTemplate.queryForList(String.format(queries.get("get_list_of_resident_foliono")), map,
				String.class);

	}

	/**
	 * 
	 * @param deductorPan
	 * @param shareHolderPan
	 * @return
	 */
	public List<ShareholderMasterResidential> getAllResidentShareholdersByPan(String deductorPan,
			String shareHolderPan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameter = new HashMap<>();
		parameter.put("deductorPan", deductorPan);
		parameter.put("shareHolderPan", shareHolderPan);
		return namedParameterJdbcTemplate.query(queries.get("get_all_resident_shareholders_by_pan_and_active"),
				parameter, new ShareholderMasterResidentialRowMapper());
	}

	/**
	 * 
	 * @param deductorPan
	 * @return
	 */
	public List<ShareholderDeclarationRateType> getAllShareholderByDeclaration(String deductorPan) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("currentDate", new Date());
		return namedParameterJdbcTemplate.query(queries.get("get_shareholder_by_declaration"), parameters,
				new ShareholderDeclarationRateTypeRowMapper());
	}

	/**
	 * 
	 * @param deductorPan
	 * @return
	 */
	public List<ShareholderMasterResidential> getAllShareholderNoPan(String deductorPan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameter = new HashMap<>();
		parameter.put("deductorPan", deductorPan);
		return namedParameterJdbcTemplate.query(queries.get("get_all_shareholder_no_pan"), parameter,
				new ShareholderMasterResidentialRowMapper());
	}
}
