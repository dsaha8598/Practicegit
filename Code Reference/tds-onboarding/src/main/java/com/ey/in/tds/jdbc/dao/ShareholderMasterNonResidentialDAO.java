package com.ey.in.tds.jdbc.dao;

import java.math.BigInteger;
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
import com.ey.in.tds.common.dto.onboarding.shareholder.ShareholderMasterNonResidential;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.jdbc.rowmapper.ShareholderMasterNonResidentialRowMapper;
/**
 * the class contains logic for DB operation with shareholder_master_non_residential table
 * @author dipak
 *
 */
@Repository
public class ShareholderMasterNonResidentialDAO {

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
		simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("shareholder_master_non_residential").withSchemaName("Client_Masters")
				.usingGeneratedKeyColumns("shareholder_master_id");

	}
	
	/**
	 * inserts record in shareholder_master_residential table
	 * 
	 * @param dto
	 * @return
	 */
	public ShareholderMasterNonResidential save(ShareholderMasterNonResidential dto) {
		logger.info("insert method execution started  to save data in share_holder_master_non_residential table{}");
		Map<String,Object> parameters=new HashMap<>();
		parameters.put("shareholder_master_id", dto.getId());
		parameters.put("deductor_master_pan", dto.getDeductorPan());
		parameters.put("user_tenant_id", dto.getTenantId());
		parameters.put("assessment_year_dividend_details", dto.getStringAssesmentYearDividendDetails());
		parameters.put("beneficial_ownership_file_address", dto.getBeneficialOwnershipFileAddress());
		parameters.put("iceland_dividend_taxation_rate", dto.getIcelandDividendTaxationRate());
		parameters.put("is_beneficial_owner_of_income", dto.getIsBeneficialOwnerOfIncome());
		parameters.put("is_beneficial_ownership_declaration_available", dto.getIsBeneficialOwnershipDeclarationAvailable());
		parameters.put("is_commercial_indemnity_or_treaty_benefits_without_documents", dto.getIsCommercialIndemnityOrTreatyBenefitsWithoutDocuments());
		parameters.put("is_iceland_rate_less_than_twenty", dto.getIsIcelandRateLessThanTwenty());
		parameters.put("is_mli_slob_satisfaction_declaration_available", dto.getIsMliSlobSatisfactionDeclarationAvailable());
		parameters.put("is_no_pe_declaration_available", dto.getIsNoPeDeclarationAvailable());
		parameters.put("is_no_poem_declaration_available", dto.getIsNoPoemDeclarationAvailable());
		parameters.put("is_pe_available_in_india", dto.getIsPeAvailableInIndia());
		parameters.put("is_poem_of_shareholder_in_india", dto.getIsPoemOfShareholderInIndia());
		parameters.put("is_tenf_available", dto.getIsTenfAvailable());
		parameters.put("is_transaction_gaar_compliant", dto.getIsTransactionGAARCompliant());
		parameters.put("is_trc_available", dto.getIsTrcAvailable());
		parameters.put("is_uk_vehicle_exempt_tax", dto.getIsUKVehicleExemptTax());
		parameters.put("iskuwaitshareholdertype", dto.getIsKuwaitShareholderType());
		parameters.put("last_payout_invoice_id", dto.getLastPayoutId());
		parameters.put("match_score", dto.getMatchScore());
		parameters.put("mli_slob_declaration_file_address", dto.getMliFileAddress());
		parameters.put("name_as_per_traces", dto.getNameAsPerTraces());
		parameters.put("no_pe_declaration_applicable_from", dto.getNoPeDeclarationApplicableFrom());
		parameters.put("no_pe_declaration_applicable_to", dto.getNoPeDeclarationApplicableTo());
		parameters.put("no_pe_declaration_file_address", dto.getNoPeFileAddress());
		parameters.put("no_poem_declaration_file_address", dto.getNoPoemFileAddress());
		parameters.put("no_poem_in_india_applicable_from", dto.getNoPoemDeclarationInIndiaApplicableFrom());
		parameters.put("no_poem_in_india_applicable_to", dto.getNoPoemDeclarationInIndiaApplicableTo());
		parameters.put("pan_as_per_traces", dto.getPanAsPerTraces());
		parameters.put("remarks_as_per_traces", dto.getRemarksAsPerTraces());
		parameters.put("area_locality", dto.getAreaLocality());
		parameters.put("category", dto.getShareholderCategory());
		parameters.put("contact", dto.getContact());
		parameters.put("country", dto.getCountry());
		parameters.put("created_by", dto.getCreatedBy());
		parameters.put("created_date", dto.getCreatedDate());
		parameters.put("demat_account_no", dto.getDematAccountNo());
		parameters.put("email_id", dto.getEmailId());
		parameters.put("flat_door_block_no", dto.getFlatDoorBlockNo());
		parameters.put("folio_no", dto.getFolioNo());
		parameters.put("form15cacb_applicable", dto.getForm15CACBApplicable());
		parameters.put("key_shareholder", dto.getKeyShareholder()==true?1:0);
		parameters.put("modified_by", dto.getModifiedBy());
		parameters.put("modified_date", dto.getModifiedDate());
		parameters.put("name", dto.getShareholderName());
		parameters.put("name_building_village", dto.getNameBuildingVillage());
		parameters.put("pan", dto.getShareholderPan());
		parameters.put("pan_status", dto.getPanStatus());
		parameters.put("pan_verified_date", dto.getPanVerifiedDate());
		parameters.put("percetage_of_share_held", dto.getPercentageSharesHeld());
		parameters.put("pin_code", dto.getPinCode());
		parameters.put("principal_place_of_business", dto.getPrincipalPlaceOfBusiness());
		parameters.put("residential_status", dto.getShareholderResidentialStatus());
		parameters.put("road_street_postoffice", dto.getRoadStreetPostoffice());
		parameters.put("share_held_from_date", dto.getShareHeldFromDate());
		parameters.put("share_held_to_date", dto.getShareHeldToDate());
		parameters.put("share_transfer_agent_name", dto.getShareTransferAgentName());
		parameters.put("source_file_name", dto.getSourceFileName());
		parameters.put("source_identifier", dto.getSourceIdentifier());
		parameters.put("state", dto.getState());
		parameters.put("tin", dto.getShareholderTin());
		parameters.put("total_shares_held", dto.getTotalSharesHeld());
		parameters.put("town_city_district", dto.getTownCityDistrict());
		parameters.put("shareholder_master_type", dto.getShareholderType());
		parameters.put("tenf_applicable_from", dto.getTenfApplicableFrom());
		parameters.put("tenf_applicable_to", dto.getTenfApplicableTo());
		parameters.put("tenf_file_address", dto.getTenfFileAddress());
		parameters.put("transaction_count", dto.getTransactionCount());
		parameters.put("trc_applicable_from", dto.getTrcApplicableFrom());
		parameters.put("trc_applicable_to", dto.getTrcApplicableTo());
		parameters.put("trc_file_address", dto.getTrcFileAddress());
		parameters.put("active", 1);
		parameters.put("version", dto.getVersion());
		parameters.put("unique_shareholder_identification_number", dto.getUniqueIdentificationNumber());
		
		dto.setId(simpleJdbcInsert.executeAndReturnKey(parameters).intValue());

		logger.info("Record inserted to share_holder_master_non_residential table {}");
		return dto;
	}
	
	//gets the count of folio no present for the deductor pan  from non resident table
		 public BigInteger getShareholderByFolioNumberPan(String deductorPan,  String folioNo) {
		        logger.debug("tenant id : {}", MultiTenantContext.getTenantId());
		        logger.info("DAO method executing to get cont of folio no for non resident{}");
				Map<String, Object> parameter = new HashMap<>();
				parameter.put("deductorPan", deductorPan);
				parameter.put("folioNo",folioNo);
				return namedParameterJdbcTemplate.queryForObject(queries.get("get_non_resident_folio_no_count"), parameter, BigInteger.class);
				
		    }
		 /**
		  * find  share holder non residential by pan and name
		  * @param deductorPan
		  * @param shareholderName
		  * @param pagination
		  * @return
		  */
		 public List<ShareholderMasterNonResidential> findAllByPanTenantIdAndName(String deductorPan, String shareholderName,
					Pagination pagination) {
				String tenantId = MultiTenantContext.getTenantId();
				logger.debug("tenant id : {}", tenantId);
				logger.info("DAO method executing to get all non resident share holder records {}");
				String paginationOrder = CommonUtil.getPagination("shareholder_master_id", pagination.getPageNumber(),
						pagination.getPageSize(), "DESC");
				String query = String.format(queries.get("get_non_resident_shareHolder_by_deductorPan"));
				if (!shareholderName.equals("noshareholderfilter")) {
					query = query + " AND name =:name";
				}
				query = query.concat(paginationOrder);
				Map<String, Object> parameter = new HashMap<>();
				parameter.put("deductorPan", deductorPan);
				parameter.put("name", shareholderName);
				return namedParameterJdbcTemplate.query(query, parameter, new ShareholderMasterNonResidentialRowMapper());

			}
		 
		 /**
		  * getting count of non resident share holders
		  * @param deductorPan
		  * @param shareholderName
		  * @return
		  */
		 public BigInteger getCountByPanTenantId(String deductorPan, String shareholderName) {
				String tenantId = MultiTenantContext.getTenantId();
				logger.debug("tenant id : {}", tenantId);
				logger.info("DAO method executing to get count of non resident share holder records count{}");
				String query = String.format(queries.get("get_count_of_non_resident_shareHolder_by_deductorPan"));
				if (!shareholderName.equals("noshareholderfilter")) {
					query = query + " AND name =:name";
				}
				Map<String, Object> parameter = new HashMap<>();
				parameter.put("deductorPan", deductorPan);
				parameter.put("name", shareholderName);
				return namedParameterJdbcTemplate.queryForObject(query, parameter, BigInteger.class);

			}
		 
		 /**
		  * to get the share hholder non residential record based on id
		  * @param id
		  * @return
		  */
		 public List<ShareholderMasterNonResidential> findById(Integer id) {
			 logger.info("Retrieveing ShareholderMasterNonResidential with ID="+id+"{}");
			 Map<String, Object> parameter = new HashMap<>();
				parameter.put("id", id);
				return namedParameterJdbcTemplate.query(queries.get("get_nonresident_share_holder_by_id"), parameter, new ShareholderMasterNonResidentialRowMapper());
			 
		 }
		 
		 public ShareholderMasterNonResidential update(ShareholderMasterNonResidential dto) {
				logger.info("DAO method executing to update ShareholderMasterNonResidential data ");

				SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(dto);
				int status = namedParameterJdbcTemplate.update(String.format(queries.get("update_non_resident_shareholder_master_by_id")), namedParameters);

				if (status != 0) {
					logger.info("ShareholderMasterNonResidential data is updated for ID " + dto.getId());
				} else {
					logger.info("No record found with ID " + dto.getId());
				}
				logger.info("DAO method execution successful {}");
				return dto;
			}
		 
		 public List<ShareholderMasterNonResidential> getAllNonResidentShareholderByPanTenantId(String deductorPan) {
		        String tenantId = MultiTenantContext.getTenantId();
		        logger.debug("tenant id : {}", tenantId);
		        Map<String, Object> parameter = new HashMap<>();
				parameter.put("deductorPan", deductorPan);
				return namedParameterJdbcTemplate.query(queries.get("get_All_Non_Resident_Shareholder_By_Pan_TenantId"), parameter, new ShareholderMasterNonResidentialRowMapper());
		    }
		 
		 public List<ShareholderMasterNonResidential> getNonResidentShareholderByFolioNumberPanTenantId(String deductorPan, String tenantId, String folioNo) {
		        logger.debug("tenant id : {}", tenantId);
		        Map<String, Object> parameter = new HashMap<>();
				parameter.put("deductorPan", deductorPan);
				parameter.put("folioNo", folioNo);
				return namedParameterJdbcTemplate.query(queries.get("get_Non_Resident_Shareholder_By_FolioNumber_PanTenantId"), parameter, new ShareholderMasterNonResidentialRowMapper());
		    }
		 
	public List<ShareholderMasterNonResidential> getNonResidentShareholdersByPan(String deductorPan, String shareHolderPan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameter = new HashMap<>();
		parameter.put("deductorPan", deductorPan);
		parameter.put("shareHolderPan", shareHolderPan);
		return namedParameterJdbcTemplate.query(queries.get("get_non_resident_shareholders_by_pan"), parameter,
				new ShareholderMasterNonResidentialRowMapper());
	}
	
	@org.springframework.transaction.annotation.Transactional
	public Integer batchUpdate(final List<ShareholderMasterNonResidential> shareholder) {
		String updateQuery = "UPDATE Client_Masters.shareholder_master_non_residential set name =:shareholderName,pan =:shareHolderPan,email_id =:emailId,flat_door_block_no =:flatDoorBlockNo,\n"
				+ "road_street_postoffice =:roadStreetPostoffice,area_locality =:areaLocality,town_city_district =:townCityDistrict,country =:country,pin_code =:pinCode where \n"
				+ "shareholder_master_id =:id";
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(shareholder);

		int[] updateCounts = namedParameterJdbcTemplate.batchUpdate(updateQuery, batch);
		logger.info("tcs Invoice line item batch updated successfully {}", updateCounts.length);
		return updateCounts.length;
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
				String.format(queries.get("get_non_resident_shareholder_shareholderPan_deductorPan")), parameters, Long.class);
	}
	
	public List<String> getListOfFolioNo(String deductorPan){
		logger.info("Retrieving list of resident folio numbers {}");
		Map<String, String> map = new HashMap<>();
		map.put("pan", deductorPan);
		return namedParameterJdbcTemplate.queryForList(String.format(queries.get("get_list_of_non_resident_foliono")), map,String.class);
		
	}
	
	@Transactional
	public void batchSaveShareHolderNonResident(List<ShareholderMasterNonResidential> shareholderList) {

		String query="INSERT INTO Client_Masters.shareholder_master_non_residential (deductor_master_pan,user_tenant_id,assessment_year_dividend_details,beneficial_ownership_file_address,\n"
				+ "iceland_dividend_taxation_rate,is_beneficial_owner_of_income,is_beneficial_ownership_declaration_available,is_commercial_indemnity_or_treaty_benefits_without_documents,\n"
				+ "is_iceland_rate_less_than_twenty,is_mli_slob_satisfaction_declaration_available,is_no_pe_declaration_available,is_no_poem_declaration_available,\n"
				+ "is_pe_available_in_india,is_poem_of_shareholder_in_india,is_tenf_available,is_transaction_gaar_compliant,is_trc_available,is_uk_vehicle_exempt_tax,\n"
				+ "iskuwaitshareholdertype,last_payout_invoice_id,match_score,mli_slob_declaration_file_address,name_as_per_traces,no_pe_declaration_applicable_from,\n"
				+ "no_pe_declaration_applicable_to,no_pe_declaration_file_address,no_poem_declaration_file_address,no_poem_in_india_applicable_from,no_poem_in_india_applicable_to,\n"
				+ "pan_as_per_traces,remarks_as_per_traces,area_locality,category,contact,country,created_by,created_date,demat_account_no,email_id,flat_door_block_no,folio_no,\n"
				+ "form15cacb_applicable,key_shareholder,modified_by,modified_date,name,name_building_village,pan,pan_status,pan_verified_date,percetage_of_share_held,pin_code,\n"
				+ "principal_place_of_business,residential_status,road_street_postoffice,share_held_from_date,share_held_to_date,share_transfer_agent_name,source_file_name,\n"
				+ "source_identifier,state,tin,total_shares_held,town_city_district,shareholder_master_type,tenf_applicable_from,tenf_applicable_to,tenf_file_address,transaction_count,\n"
				+ "trc_applicable_from,trc_applicable_to,trc_file_address,version,active,unique_shareholder_identification_number,batch_upload_id) VALUES\n"
				+ "(:deductorPan,:tenantId,:stringAssesmentYearDividendDetails,:beneficialOwnershipFileAddress,\n"
				+ ":icelandDividendTaxationRate,:isBeneficialOwnerOfIncome,:isBeneficialOwnershipDeclarationAvailable,:isCommercialIndemnityOrTreatyBenefitsWithoutDocuments,\n"
				+ ":isIcelandRateLessThanTwenty,:isMliSlobSatisfactionDeclarationAvailable,:isNoPeDeclarationAvailable,:isNoPoemDeclarationAvailable,\n"
				+ ":isPeAvailableInIndia,:isPoemOfShareholderInIndia,:isTenfAvailable,:isTransactionGAARCompliant,:isTrcAvailable,:isUKVehicleExemptTax,\n"
				+ ":isKuwaitShareholderType,:lastPayoutId,:matchScore,:mliFileAddress,:nameAsPerTraces,:noPeDeclarationApplicableFrom,\n"
				+ ":noPeDeclarationApplicableTo,:noPeFileAddress,:noPoemFileAddress,:noPoemDeclarationInIndiaApplicableFrom,:noPoemDeclarationInIndiaApplicableTo,\n"
				+ ":panAsPerTraces,:remarksAsPerTraces,:areaLocality,:shareholderCategory,:contact,:country,:createdBy,:createdDate,:dematAccountNo,:emailId,:flatDoorBlockNo,:folioNo,\n"
				+ ":form15CACBApplicable,:keyShareholder,:modifiedBy,:modifiedDate,:shareholderName,:nameBuildingVillage,:shareholderPan,:panStatus,:panVerifiedDate,:percentageSharesHeld,:pinCode,\n"
				+ ":principalPlaceOfBusiness,:shareholderResidentialStatus,:roadStreetPostoffice,:shareHeldFromDate,:shareHeldToDate,:shareTransferAgentName,:sourceFileName,\n"
				+ ":sourceIdentifier,:state,:shareholderTin,:totalSharesHeld,:townCityDistrict,:shareholderType,:tenfApplicableFrom,:tenfApplicableTo,:tenfFileAddress,:transactionCount,\n"
				+ ":trcApplicableFrom,:trcApplicableTo,:trcFileAddress,:version,1,:uniqueIdentificationNumber,:batchUploadId);";
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(shareholderList);
		namedParameterJdbcTemplate.batchUpdate(query, batch);
		logger.info("shareholder records saved with size {}", shareholderList.size());

	}
	/**
	 * to get non resident  share holder by id 
	 * 
	 * @param id
	 * @param deductorPan
	 * @return
	 */
	public List<ShareholderMasterNonResidential> findShareholderByIdAndPan(String ids, String deductorPan) {
		logger.info("DAO method executing to get non resident invoice share holder record using id {}");
		String query = String.format(queries.get("get_non_resrident__shareholder_by_id_pan"));
		query = query + ids;
		Map<String, Object> parameter = new HashMap<>();
		parameter.put("deductorPan", deductorPan);
		return namedParameterJdbcTemplate.query(query, parameter, new ShareholderMasterNonResidentialRowMapper());

	}
	


}