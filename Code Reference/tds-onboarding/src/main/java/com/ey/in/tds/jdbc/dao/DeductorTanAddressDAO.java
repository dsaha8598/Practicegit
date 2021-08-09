package com.ey.in.tds.jdbc.dao;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorTanAddress;
import com.ey.in.tds.jdbc.rowmapper.DeductorTanAddressRowMapper;

/**
 * 
 * @author Scriptbees.
 *
 */
@Repository
public class DeductorTanAddressDAO implements Serializable {

	private static final long serialVersionUID = 1L;

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
		jdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("deductor_tan_address").withSchemaName("Onboarding")
				.usingGeneratedKeyColumns("deductor_tan_address_id");
	}

	/**
	 * This method for get all deductor tan address based on pan.
	 * 
	 * @param pan
	 * @return
	 */
	public List<DeductorTanAddress> findByDeductorPan(String pan) {
		return jdbcTemplate.query(String.format(queries.get("find_deductor_pan")), new DeductorTanAddressRowMapper(),
				pan);
	}
	
	/**
	 * 
	 * @param deductorTan
	 * @return
	 */
	public List<DeductorTanAddress> findPanNameByTan(String deductorTan) {
		return jdbcTemplate.query(String.format(queries.get("find_by_deductor_tan")),
				new DeductorTanAddressRowMapper(), deductorTan);
	}

	/**
	 * 
	 * @param deductorTanAddress
	 * @param deductorMasterId
	 */
	public void save(DeductorTanAddress deductorTanAddress, Integer deductorMasterId) {

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductor_master_id", deductorMasterId);
		parameters.put("pan", deductorTanAddress.getPan());
		parameters.put("tan", deductorTanAddress.getTan());
		parameters.put("area_locality", deductorTanAddress.getAreaLocality());
		parameters.put("country_name", deductorTanAddress.getCountryName());
		parameters.put("flat_door_block_no", deductorTanAddress.getFlatDoorBlockNo());
		parameters.put("name_building_village", deductorTanAddress.getNameBuildingVillage());
		parameters.put("person_address_change", deductorTanAddress.getPersonAddressChange());
		parameters.put("person_alternate_email", deductorTanAddress.getPersonAlternateEmail());
		parameters.put("person_alternate_telephone", deductorTanAddress.getPersonAlternateTelephone());
		parameters.put("person_area", deductorTanAddress.getPersonArea());
		parameters.put("person_building_name", deductorTanAddress.getPersonBuildingName());
		parameters.put("person_city", deductorTanAddress.getPersonCity());
		parameters.put("person_designation", deductorTanAddress.getPersonDesignation());
		parameters.put("person_email", deductorTanAddress.getPersonEmail());
		parameters.put("person_flat_door_blockno", deductorTanAddress.getPersonFlatDoorBlockNo());
		parameters.put("person_mobile_number", deductorTanAddress.getPersonMobileNumber());
		parameters.put("person_name", deductorTanAddress.getPersonName());
		parameters.put("person_pan", deductorTanAddress.getPersonPan());
		parameters.put("person_pin_code", deductorTanAddress.getPinCode());
		parameters.put("person_state", deductorTanAddress.getPersonState());
		parameters.put("person_street_name", deductorTanAddress.getPersonStreetName());
		parameters.put("person_telephone", deductorTanAddress.getPersonTelephone());
		parameters.put("pin_code", deductorTanAddress.getPinCode());
		parameters.put("road_street_postoffice", deductorTanAddress.getRoadStreetPostoffice());
		parameters.put("state_name", deductorTanAddress.getStateName());
		parameters.put("town_city_district", deductorTanAddress.getTownCityDistrict());
		parameters.put("std_code", deductorTanAddress.getStdCode());
		parameters.put("person_std_code", deductorTanAddress.getPersonStdCode());
		parameters.put("active", deductorTanAddress.getActive());
		parameters.put("created_by", deductorTanAddress.getCreatedBy());
		parameters.put("created_date", deductorTanAddress.getCreatedDate());
		parameters.put("modified_by", deductorTanAddress.getModifiedBy());
		parameters.put("modified_date", deductorTanAddress.getModifiedDate());
		
		parameters.put("accountant_name", deductorTanAddress.getDvndAccountantName());
		parameters.put("dvnd_area_locality", deductorTanAddress.getDvndAreaLocality());
		parameters.put("branch_of_bank", deductorTanAddress.getDvndBranchOfBank());
		parameters.put("bsr_code_of_bank_branch", deductorTanAddress.getDvndBsrCodeOfBankBranch());
		parameters.put("dvnd_country", deductorTanAddress.getDvndCountry());
		parameters.put("father_or_husband_name", deductorTanAddress.getDvndFatherOrHusbandName());
		parameters.put("dvnd_flat_door_block_no", deductorTanAddress.getDvndFlatDoorBlockNo());
		parameters.put("membership_number", deductorTanAddress.getDvndMembershipNumber());
		parameters.put("name_of_bank", deductorTanAddress.getDvndNameOfBank());
		parameters.put("name_of_premises_building_village", deductorTanAddress.getDvndNameOfPremisesBuildingVillage());
		parameters.put("name_of_proprietorship", deductorTanAddress.getDvndNameOfProprietorship());
		parameters.put("opted_for_15_ca_cb", deductorTanAddress.getDvndOptedFor15CaCb());
		parameters.put("dvnd_pin_code", deductorTanAddress.getDvndPinCode());
		parameters.put("principal_area_of_business", deductorTanAddress.getDvndPrincipalAreaOfBusiness());
		parameters.put("registration_number", deductorTanAddress.getDvndRegistrationNumber());
		parameters.put("dvnd_road_street_post_office", deductorTanAddress.getDvndRoadStreetPostOffice());
		parameters.put("dvnd_state", deductorTanAddress.getDvndState());
		parameters.put("dvnd_town_city_district", deductorTanAddress.getDvndTownCityDistrict());
		parameters.put("accountant_salutation", deductorTanAddress.getAccountantSalutation());
		

		int deductorTanAddressId = jdbcInsert.executeAndReturnKey(parameters).intValue();
		deductorTanAddress.setDeductorTanAddressId(deductorTanAddressId);

	}
	
	/**
	 * 
	 * @param deductorTanAddress
	 * @return
	 */
	public DeductorTanAddress update(DeductorTanAddress dto) {
		
		logger.info("DAO method executing to update deductor tan address data ");

		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(dto);
		int status = namedParameterJdbcTemplate.update(String.format(queries.get("update_deductor_tan_address")), namedParameters);

		if (status != 0) {
			logger.info("deductor tan address data is updated for ID " + dto.getDeductorTanAddressId());
		} else {
			logger.info("No deductor tan address record found with ID " + dto.getDeductorTanAddressId());
		}
		logger.info("DAO method execution successful {}");
		return dto;
	}
	
	/**
	 * 
	 * @param pan
	 * @param tan
	 * @return
	 */
	public List<DeductorTanAddress> findByPanAndTan(String pan, String tan) {
		return jdbcTemplate.query(String.format(queries.get("find_by_pan_and_tan")),
				new DeductorTanAddressRowMapper(), pan, tan);
	}
}
