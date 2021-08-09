package com.ey.in.tds.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

import com.ey.in.tds.common.onboarding.jdbc.dto.DeducteeMasterResidential;

public class DeducteeMasterStaggingRowMapper implements RowMapper<DeducteeMasterResidential>{

	@Override
	public DeducteeMasterResidential mapRow(ResultSet rs, int rowNum) throws SQLException {
		DeducteeMasterResidential deducteeMasterResidential = new DeducteeMasterResidential();

		deducteeMasterResidential.setDeducteeMasterId(rs.getInt("deductee_master_id"));
		deducteeMasterResidential.setDeductorPan(rs.getString("deductor_master_pan"));
		deducteeMasterResidential.setActive(rs.getBoolean("active"));
		deducteeMasterResidential.setAdditionalSections(rs.getString("additional_sections"));
		deducteeMasterResidential.setAdvanceTransactionCount(rs.getInt("advance_transaction_count"));
		deducteeMasterResidential.setApplicableFrom(rs.getDate("applicable_from"));
		deducteeMasterResidential.setApplicableTo(rs.getDate("applicable_to"));
		deducteeMasterResidential.setAreaLocality(rs.getString("area_locality"));
		deducteeMasterResidential.setCountry(rs.getString("country"));
		deducteeMasterResidential.setCreatedBy(rs.getString("created_by"));
		deducteeMasterResidential.setCreatedDate(rs.getDate("created_date"));
		deducteeMasterResidential.setDeducteeCode(rs.getString("deductee_code"));
		deducteeMasterResidential.setDeducteeStatus(rs.getString("deductee_status"));
		deducteeMasterResidential.setDeductorCode(rs.getString("deductor_code"));
		deducteeMasterResidential.setDefaultRate(rs.getBigDecimal("default_rate"));
		deducteeMasterResidential.setEmailAddress(rs.getString("email"));
		deducteeMasterResidential.setFlatDoorBlockNo(rs.getString("flat_door_block_no"));
		deducteeMasterResidential.setInvoiceTransactionCount(rs.getInt("invoice_transaction_count"));
		deducteeMasterResidential
				.setIsDeducteeHasAdditionalSections(rs.getBoolean("is_eligible_for_multiple_sections"));
		deducteeMasterResidential.setModifiedBy(rs.getString("modified_by"));
		deducteeMasterResidential.setModifiedDate(rs.getDate("modified_date"));
		deducteeMasterResidential.setModifiedName(rs.getString("modified_name"));
		deducteeMasterResidential.setDeducteeName(rs.getString("deductee_master_name"));
		deducteeMasterResidential.setNameBuildingVillage(rs.getString("name_building_village"));
		deducteeMasterResidential.setNameOfTheCompanyCode(rs.getString("name_of_the_company_code"));
		deducteeMasterResidential.setNameOfTheDeductee(rs.getString("name_of_the_deductee"));
		deducteeMasterResidential.setDeducteePAN(rs.getString("deductee_master_pan"));
		deducteeMasterResidential.setPanStatus(rs.getString("pan_status"));
		deducteeMasterResidential.setPanVerifiedDate(rs.getDate("pan_verified_date"));
		deducteeMasterResidential.setPhoneNumber(rs.getString("phone_number"));
		deducteeMasterResidential.setPinCode(rs.getString("pin_code"));
		deducteeMasterResidential.setProvisionTransactionCount(rs.getInt("provision_transaction_count"));
		deducteeMasterResidential.setRate(rs.getBigDecimal("rate"));
		deducteeMasterResidential.setDeducteeResidentialStatus(rs.getString("residential_status"));
		deducteeMasterResidential.setRoadStreetPostoffice(rs.getString("road_street_postoffice"));
		deducteeMasterResidential.setSection(rs.getString("section"));
		deducteeMasterResidential.setSourceFileName(rs.getString("source_file_name"));
		deducteeMasterResidential.setSourceIdentifier(rs.getString("source_identifier"));
		deducteeMasterResidential.setState(rs.getString("state"));
		deducteeMasterResidential.setTinUniqueIdentification(rs.getString("tin_unique_identification"));
		deducteeMasterResidential.setTownCityDistrict(rs.getString("town_city_district"));
		deducteeMasterResidential.setUserDefinedField1(rs.getString("user_defined_field_1"));
		deducteeMasterResidential.setUserDefinedField2(rs.getString("user_defined_field_2"));
		deducteeMasterResidential.setUserDefinedField3(rs.getString("user_defined_field_3"));
		deducteeMasterResidential.setMatchScore(rs.getString("match_score"));
		deducteeMasterResidential.setNameAsPerTraces(rs.getString("name_as_per_traces"));
		deducteeMasterResidential.setPanAsPerTraces(rs.getString("pan_as_per_traces"));
		deducteeMasterResidential.setRemarksAsPerTraces(rs.getString("remarks_as_per_traces"));
		deducteeMasterResidential.setNatureOfPayment(rs.getString("nature_of_payment"));
		deducteeMasterResidential.setDeducteeAadharNumber(rs.getString("deductee_aadhar_number"));
		deducteeMasterResidential
				.setIsThresholdLimitApplicable(rs.getInt("is_threshold_limit_applicable") == 1 ? true : false);
		deducteeMasterResidential.setSectionCode(
				StringUtils.isEmpty(rs.getString("section_code")) ? StringUtils.EMPTY : rs.getString("section_code"));
		deducteeMasterResidential.setAdditionalSectionCode(rs.getString("additional_section_code"));
		deducteeMasterResidential.setDeducteeKey(rs.getString("deductee_key"));
		deducteeMasterResidential.setDeducteeEnrichmentKey(rs.getString("deductee_enrichment_key"));
		deducteeMasterResidential.setPreviousBalanceMonth(rs.getInt("previous_balance_month"));
		deducteeMasterResidential.setPreviousBalanceYear(rs.getInt("previous_balance_year"));
		deducteeMasterResidential.setCurrentBalanceMonth(rs.getInt("current_balance_month"));
		deducteeMasterResidential.setCurrentBalanceYear(rs.getInt("current_balance_year"));
		deducteeMasterResidential.setDeducteeMasterBalancesOf194q(rs.getBigDecimal("deductee_master_balances_of_194q"));
		deducteeMasterResidential.setAdvanceBalancesOf194q(rs.getBigDecimal("advance_balances_of_194q"));
		deducteeMasterResidential.setProvisionBalancesOf194q(rs.getBigDecimal("provision_balances_of_194q"));
		deducteeMasterResidential.setAdvancesAsOfMarch(rs.getBigDecimal("advances_as_of_march"));
		deducteeMasterResidential.setProvisionsAsOfMarch(rs.getBigDecimal("provisions_as_of_march"));
		deducteeMasterResidential.setAdditionalSectionThresholds(rs.getString("additional_section_thresholds"));
		deducteeMasterResidential.setTdsApplicabilityUnderSection(rs.getString("tds_applicability_under_section"));
		deducteeMasterResidential.setDeducteeGSTIN(rs.getString("gstin_number"));
		deducteeMasterResidential.setOpeningBalanceCreditNote(rs.getBigDecimal("opening_balance_credit_note"));
		deducteeMasterResidential.setTdsExemptionNumber(rs.getString("tds_exemption_number"));
		deducteeMasterResidential.setGrOrIRIndicator(rs.getString("gr_or_ir_indicator"));
		deducteeMasterResidential.setDeducteeTan(rs.getString("deductee_tan"));
		
		return deducteeMasterResidential;
	}

}
