package com.ey.in.tds.jdbc.dao;

import java.util.HashMap;
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

import com.ey.in.tds.common.dto.onboarding.shareholder.ShareholderMasterNonResidential;
import com.ey.in.tds.common.onboarding.jdbc.dto.ShareholderNonResidentialHistory;
import com.ey.in.tds.common.onboarding.jdbc.dto.ShareholderResidentialHistory;

@Repository
public class ShareholderNonResidentialHistoryDAO {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired(required = false)
	private HashMap<String, String> queries;

	private JdbcTemplate jdbcTemplate;

	private SimpleJdbcInsert simpleJdbcInsert;

	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	private DataSource dataSource;

	@PostConstruct
	private void postConstruct() {
		jdbcTemplate = new JdbcTemplate(dataSource);
		namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("shareholder_non_residential_history").withSchemaName("Transactions")
				.usingGeneratedKeyColumns("shareholder_master_id");

	}
	
	/**
	 * inserts record in shareholder_master_residential table
	 * 
	 * @param dto
	 * @return
	 */
	public ShareholderNonResidentialHistory save(ShareholderNonResidentialHistory dto) {
		logger.info("insert method execution started  {}");

		//SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(dto);
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
		//parameters.put("last_payout_invoice_id", dto.getinv());
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
	//	parameters.put("form15cacb_applicable", dto.getForm15CACBApplicable());
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
		//parameters.put("transaction_count", dto.getTransactionCount());
		parameters.put("trc_applicable_from", dto.getTrcApplicableFrom());
		parameters.put("trc_applicable_to", dto.getTrcApplicableTo());
		parameters.put("trc_file_address", dto.getTrcFileAddress());
		parameters.put("version", dto.getVersion());
		parameters.put("unique_shareholder_identification_number", dto.getUniqueIdentificationNumber());
		dto.setId(simpleJdbcInsert.executeAndReturnKey(parameters).intValue());

		logger.info("Record inserted to ShareholderNonResidentialHistory table {}");
		return dto;
	}//ShareholderNonResidentialHistoryRowMapper
	
	/**
	 * method to update data in ShareholderNonResidentialHistory table
	 * @param dto
	 * @return
	 */
	 public ShareholderNonResidentialHistory update(ShareholderNonResidentialHistory dto) {
			logger.info("DAO method executing to update ShareholderNonResidentialHistory data ");

			SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(dto);
			int status = namedParameterJdbcTemplate.update(String.format(queries.get("update_non_resident_shareholder_history_by_id")), namedParameters);

			if (status != 0) {
				logger.info("ShareholderNonResidentialHistory data is updated for ID " + dto.getId());
			} else {
				logger.info("No record found with ID " + dto.getId());
			}
			logger.info("DAO method execution successful {}");
			return dto;
		}
}
