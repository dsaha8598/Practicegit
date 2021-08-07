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

import com.ey.in.tds.common.onboarding.jdbc.dto.AoMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.ShareholderMasterResidential;
import com.ey.in.tds.common.onboarding.jdbc.dto.ShareholderResidentialHistory;

@Repository
public class ShareholderResidentialHistoryDAO {

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
		simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("shareholder_residential_history").withSchemaName("Transactions")
				.usingGeneratedKeyColumns("shareholder_master_id");

	}
	
	/**
	 * inserts record in shareholder_master_residential table
	 * 
	 * @param dto
	 * @return
	 */
	public ShareholderResidentialHistory save(ShareholderResidentialHistory dto) {
		logger.info("insert method execution started  {}");

		//SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(dto);
		Map<String,Object> namedParameters=new HashMap<>();
		namedParameters.put("deductor_master_pan", dto.getDeductorPan());
		namedParameters.put("shareholder_master_id", dto.getId());
		namedParameters.put("assessment_year_dividend_details", dto.getStringAssesmentYearDividendDetails());
		//namedParameters.put("last_payout_invoice_id", dto.get);
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
		namedParameters.put("residential_status", dto.getShareholderResidentialStatus());
		namedParameters.put("road_street_postoffice", dto.getRoadStreetPostoffice());
		namedParameters.put("share_transfer_agent_name", dto.getShareTransferAgentName());
		namedParameters.put("source_file_name", dto.getSourceFileName());
		namedParameters.put("source_identifier", dto.getSourceIdentifier());
		namedParameters.put("state", dto.getState());
		namedParameters.put("total_of_shares_held", dto.getTotalSharesHeld());
		namedParameters.put("town_city_district", dto.getTownCityDistrict());
		namedParameters.put("type", dto.getShareholderType());
		namedParameters.put("unique_shareholder_identification_number", dto.getUniqueIdentificationNumber());
		dto.setId(simpleJdbcInsert.executeAndReturnKey(namedParameters).intValue());

		logger.info("Record inserted to ShareholderResidentialHistory table {}");
		return dto;
	}
	
	/**
	 * to update the ao
	 * 
	 * @param dto
	 * @return
	 */
	public ShareholderResidentialHistory update(ShareholderResidentialHistory dto) {
		logger.info("DAO method executing to update ShareholderResidentialHistory data ");

		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(dto);
		int status = namedParameterJdbcTemplate.update(String.format(queries.get("update_resident_shareHolder_history_by_id")), namedParameters);

		if (status != 0) {
			logger.info("ShareholderResidentialHistory data is updated for ID " + dto.getId());
		} else {
			logger.info("No record found with ID " + dto.getId());
		}
		logger.info("DAO method execution successful {}");
		return dto;
	}


}
