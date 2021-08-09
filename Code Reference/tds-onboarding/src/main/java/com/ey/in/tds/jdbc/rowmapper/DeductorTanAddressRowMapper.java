package com.ey.in.tds.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorTanAddress;

/**
 * 
 * @author scriptbees
 *
 */
public class DeductorTanAddressRowMapper implements RowMapper<DeductorTanAddress> {

	@Override
	public DeductorTanAddress mapRow(ResultSet rs, int rowNum) throws SQLException {
		DeductorTanAddress deductorTanAddress = new DeductorTanAddress();
		deductorTanAddress.setDeductorTanAddressId(rs.getInt("deductor_tan_address_id"));
		deductorTanAddress.setDeductorMasterId(rs.getInt("deductor_master_id"));
		deductorTanAddress.setPan(rs.getString("pan"));
		deductorTanAddress.setTan(rs.getString("tan"));
		deductorTanAddress.setFlatDoorBlockNo(rs.getString("flat_door_block_no"));
		deductorTanAddress.setNameBuildingVillage(rs.getString("name_building_village"));
		deductorTanAddress.setTownCityDistrict(rs.getString("town_city_district"));
		deductorTanAddress.setAreaLocality(rs.getString("area_locality"));
		deductorTanAddress.setRoadStreetPostoffice(rs.getString("road_street_postoffice"));
		deductorTanAddress.setPinCode(rs.getString("pin_code"));
		deductorTanAddress.setStateName(rs.getString("state_name"));
		deductorTanAddress.setCountryName(rs.getString("country_name"));
		deductorTanAddress.setPersonName(rs.getString("person_name"));
		deductorTanAddress.setPersonPan(rs.getString("person_pan"));
		deductorTanAddress.setPersonDesignation(rs.getString("person_designation"));
		deductorTanAddress.setPersonFlatDoorBlockNo(rs.getString("person_flat_door_blockno"));
		deductorTanAddress.setPersonBuildingName(rs.getString("person_building_name"));
		deductorTanAddress.setPersonStreetName(rs.getString("person_street_name"));
		deductorTanAddress.setPersonArea(rs.getString("person_area"));
		deductorTanAddress.setPersonCity(rs.getString("person_city"));
		deductorTanAddress.setPersonState(rs.getString("person_state"));
		deductorTanAddress.setPersonPinCode(rs.getString("person_pin_code"));
		deductorTanAddress.setPersonTelephone(rs.getString("person_telephone"));
		deductorTanAddress.setPersonAlternateTelephone(rs.getString("person_alternate_telephone"));
		deductorTanAddress.setPersonMobileNumber(rs.getString("person_mobile_number"));
		deductorTanAddress.setPersonEmail(rs.getString("person_email"));
		deductorTanAddress.setPersonAlternateEmail(rs.getString("person_alternate_email"));
		deductorTanAddress.setPersonAddressChange(rs.getBoolean("person_address_change"));
		deductorTanAddress.setStdCode(rs.getString("std_code"));
		deductorTanAddress.setPersonStdCode(rs.getString("person_std_code"));
		deductorTanAddress.setActive(rs.getBoolean("active"));
		deductorTanAddress.setCreatedDate(rs.getDate("created_date"));
		deductorTanAddress.setCreatedBy(rs.getString("created_by"));
		deductorTanAddress.setModifiedBy(rs.getString("modified_by"));
		deductorTanAddress.setModifiedDate(rs.getDate("modified_date"));
		
		//dividend 
		deductorTanAddress.setDvndAccountantName(rs.getString("accountant_name"));
		deductorTanAddress.setDvndAreaLocality(rs.getString("dvnd_area_locality"));
		deductorTanAddress.setDvndBranchOfBank(rs.getString("branch_of_bank"));
		deductorTanAddress.setDvndBsrCodeOfBankBranch(rs.getString("bsr_code_of_bank_branch"));
		deductorTanAddress.setDvndCountry(rs.getString("dvnd_country"));
		deductorTanAddress.setDvndFatherOrHusbandName(rs.getString("father_or_husband_name"));
		deductorTanAddress.setDvndFlatDoorBlockNo(rs.getString("dvnd_flat_door_block_no"));
		deductorTanAddress.setDvndMembershipNumber(rs.getString("membership_number"));
		deductorTanAddress.setDvndNameOfBank(rs.getString("name_of_bank"));
		deductorTanAddress.setDvndNameOfPremisesBuildingVillage(rs.getString("name_of_premises_building_village"));
		deductorTanAddress.setDvndNameOfProprietorship(rs.getString("name_of_proprietorship"));
		deductorTanAddress.setDvndOptedFor15CaCb(rs.getInt("opted_for_15_ca_cb")==1?true:false);
		deductorTanAddress.setDvndPinCode(rs.getString("dvnd_pin_code"));
		deductorTanAddress.setDvndPrincipalAreaOfBusiness(rs.getString("principal_area_of_business"));
		deductorTanAddress.setDvndRegistrationNumber(rs.getString("registration_number"));
		deductorTanAddress.setDvndRoadStreetPostOffice(rs.getString("dvnd_road_street_post_office"));
		deductorTanAddress.setDvndState(rs.getString("dvnd_state"));
		deductorTanAddress.setDvndTownCityDistrict(rs.getString("dvnd_town_city_district"));
		deductorTanAddress.setAccountantSalutation(rs.getString("accountant_salutation"));

		return deductorTanAddress;
	}

}
