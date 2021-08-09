package com.ey.in.tds.jdbc.rowmapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ey.in.tds.common.onboarding.jdbc.dto.CollecteeMaster;

/**
 * 
 * @author vamsir
 *
 */
public class CollecteeMasterRowMapper implements RowMapper<CollecteeMaster> {

	@Override
	public CollecteeMaster mapRow(ResultSet rs, int rowNum) throws SQLException {
		CollecteeMaster collecteeMaster = new CollecteeMaster();

		collecteeMaster.setId(rs.getInt("id"));
		collecteeMaster.setSourceIdentifier(rs.getString("source_identifier"));
		collecteeMaster.setSourceFileName(rs.getString("source_file_name"));
		collecteeMaster.setCollectorCode(rs.getString("collector_code"));
		collecteeMaster.setNameOfTheCollector(rs.getString("name_of_the_collector"));
		collecteeMaster.setCollectorPan(rs.getString("collector_pan"));
		collecteeMaster.setCollectorTan(rs.getString("collector_tan"));
		collecteeMaster.setCollectorCode(rs.getString("collector_code"));
		collecteeMaster.setCollecteeCode(rs.getString("collectee_code"));
		collecteeMaster.setNameOfTheCollectee(rs.getString("name_of_the_collectee"));
		collecteeMaster.setNonResidentCollecteeIndicator(rs.getBoolean("non_resident_collectee_indicator"));
		collecteeMaster.setCollecteePan(rs.getString("collectee_pan"));
		collecteeMaster.setCollecteeAadharNumber(rs.getString("collectee_aadhar_number"));
		collecteeMaster.setCollecteeStatus(rs.getString("collectee_status"));
		collecteeMaster.setDistributionChannel(rs.getString("distribution_channel"));
		collecteeMaster.setEmailAddress(rs.getString("email_address"));
		collecteeMaster.setPhoneNumber(rs.getString("phone_number"));
		collecteeMaster.setFlatDoorBlockNo(rs.getString("flat_door_block_no"));
		collecteeMaster.setNameOfTheBuildingVillage(rs.getString("name_of_the_building_village"));
		collecteeMaster.setRoadStreetPostoffice(rs.getString("road_street_postoffice"));
		collecteeMaster.setAreaLocality(rs.getString("area_locality"));
		collecteeMaster.setTownCityDistrict(rs.getString("town_city_district"));
		collecteeMaster.setState(rs.getString("state"));
		collecteeMaster.setCountry(rs.getString("country"));
		collecteeMaster.setPinCode(rs.getString("pin_code"));
		collecteeMaster.setNatureOfIncome(rs.getString("nature_of_income"));
		collecteeMaster.setTcsSection(rs.getString("tcs_section"));
		collecteeMaster.setTcsRate(rs.getBigDecimal("tcs_rate") == null ? BigDecimal.ZERO
				: rs.getBigDecimal("tcs_rate").setScale(4, BigDecimal.ROUND_HALF_DOWN));
		collecteeMaster.setTdsSection(rs.getString("tds_section"));
		collecteeMaster.setTdsRate(rs.getBigDecimal("tcs_rate") == null ? BigDecimal.ZERO
				: rs.getBigDecimal("tds_rate").setScale(4, BigDecimal.ROUND_HALF_DOWN));
		collecteeMaster.setApplicableFrom(rs.getDate("applicable_from"));
		collecteeMaster.setApplicableTo(rs.getDate("applicable_to"));
		collecteeMaster
				.setNoCollectionDeclarationAsPerForm27c(rs.getBoolean("no_collection_declaration_as_per_form_27c"));
		collecteeMaster
				.setBalancesForSection206c(rs.getBigDecimal("balances_for_section_206c") == null ? BigDecimal.ZERO
						: rs.getBigDecimal("balances_for_section_206c").setScale(2, BigDecimal.ROUND_UP));
		collecteeMaster.setUserDefinedField1(rs.getString("user_defined_field1"));
		collecteeMaster.setUserDefinedField2(rs.getString("user_defined_field2"));
		collecteeMaster.setUserDefinedField3(rs.getString("user_defined_field3"));
		collecteeMaster.setUserDefinedField4(rs.getString("user_defined_field4"));
		collecteeMaster.setUserDefinedField5(rs.getString("user_defined_field5"));
		collecteeMaster.setActive(rs.getBoolean("active"));
		collecteeMaster.setCreatedBy(rs.getString("created_by"));
		collecteeMaster.setCreatedDate(rs.getDate("created_date"));
		collecteeMaster.setModifiedBy(rs.getString("modified_by"));
		collecteeMaster.setModifiedDate(rs.getDate("modified_date"));
		collecteeMaster.setCollecteeType(rs.getString("collectee_type"));
		collecteeMaster.setPanVerifyStatus(rs.getBoolean("pan_verify_status"));
		collecteeMaster.setPanVerificationDate(rs.getDate("pan_verification_date"));
		collecteeMaster.setRemarkAsPerTraces(rs.getString("remark_as_per_traces"));
		collecteeMaster.setNameAsPerTraces(rs.getString("name_as_per_traces"));
		collecteeMaster.setMatchScore(rs.getInt("match_score"));
		collecteeMaster.setPanAsPerTraces(rs.getString("pan_as_per_traces"));
		collecteeMaster.setAdvanceBalancesForSection206c(
				rs.getBigDecimal("advance_balances_for_section206c") == null ? BigDecimal.ZERO
						: rs.getBigDecimal("advance_balances_for_section206c").setScale(2, BigDecimal.ROUND_UP));
		collecteeMaster.setCollectionsBalancesForSection206c(rs.getBigDecimal("collections_balances_for_section206c"));
		collecteeMaster.setIsEligibleForMultipleSections(rs.getBoolean("is_eligible_for_multiple_sections"));
		collecteeMaster.setAdditionalSections(rs.getString("additional_sections"));
		collecteeMaster.setTdsCode(rs.getString("tds_code"));
		collecteeMaster.setTdsIndicator(rs.getBoolean("tds_indicator"));
		collecteeMaster.setAdvancesAsOfMarch(rs.getBigDecimal("advances_as_of_march"));
		collecteeMaster.setNccCertificateUrl(rs.getString("ncc_certificate_url"));
		collecteeMaster.setCurrentBalanceMonth(rs.getInt("current_balance_month"));
		collecteeMaster.setCurrentBalanceYear(rs.getInt("current_balance_year"));
		collecteeMaster.setPreviousBalanceMonth(rs.getInt("previous_balance_month"));
		collecteeMaster.setPreviousBalanceYear(rs.getInt("previous_balance_year"));
		collecteeMaster.setPanAadhaarLinkStatus(rs.getString("pan_aadhaar_link_status"));
		collecteeMaster.setPanAllotmentDate(rs.getDate("pan_allotment_date"));
		collecteeMaster.setBatchUploadId(rs.getInt("batch_upload_id"));
		collecteeMaster.setGstinNumber(rs.getString("gstin_number"));
		
		return collecteeMaster;
	}

}
