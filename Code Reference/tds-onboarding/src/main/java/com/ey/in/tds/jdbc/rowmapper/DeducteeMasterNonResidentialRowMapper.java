package com.ey.in.tds.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

import com.ey.in.tds.common.onboarding.jdbc.dto.DeducteeMasterNonResidential;

/**
 * 
 * @author scriptbees.
 *
 */
public class DeducteeMasterNonResidentialRowMapper implements RowMapper<DeducteeMasterNonResidential> {

	@Override
	public DeducteeMasterNonResidential mapRow(ResultSet rs, int rowNum) throws SQLException {
		DeducteeMasterNonResidential deducteeMasterNonResidential = new DeducteeMasterNonResidential();
		deducteeMasterNonResidential.setDeducteeMasterId(rs.getInt("deductee_master_id"));
		deducteeMasterNonResidential.setDeductorPan(rs.getString("deductor_master_pan"));
		deducteeMasterNonResidential.setTenFApplicableFrom(rs.getDate("deductee_master_10f_applicable_from"));
		deducteeMasterNonResidential.setTenFApplicableTo(rs.getDate("deductee_master_10f_applicable_to"));
		// deductee_master_10f_file_address
		deducteeMasterNonResidential.setForm10fFileAddress(rs.getString("deductee_master_10f_file_address"));
		deducteeMasterNonResidential.setActive(rs.getBoolean("active"));
		deducteeMasterNonResidential.setAdditionalSections(rs.getString("additional_sections"));
		deducteeMasterNonResidential.setAdvanceTransactionCount(rs.getInt("advance_transaction_count"));
		deducteeMasterNonResidential.setApplicableFrom(rs.getDate("applicable_from"));
		deducteeMasterNonResidential.setApplicableTo(rs.getDate("applicable_to"));
		deducteeMasterNonResidential.setAreaLocality(rs.getString("area_locality"));
		deducteeMasterNonResidential.setCountry(rs.getString("country"));
		deducteeMasterNonResidential.setCountryOfResidence(rs.getString("country_of_residence"));
		deducteeMasterNonResidential.setCreatedBy(rs.getString("created_by"));
		deducteeMasterNonResidential.setCreatedDate(rs.getDate("created_date"));
		deducteeMasterNonResidential.setDeducteeCode(rs.getString("deductee_code"));
		deducteeMasterNonResidential.setDeducteeStatus(rs.getString("deductee_status"));
		deducteeMasterNonResidential.setDeductorCode(rs.getString("deductor_code"));
		deducteeMasterNonResidential.setDeductorName(rs.getString("deductor_name"));
		deducteeMasterNonResidential.setDefaultRate(rs.getBigDecimal("default_rate"));
		deducteeMasterNonResidential.setEmailAddress(rs.getString("email"));
		deducteeMasterNonResidential.setFixedBasedIndia(rs.getString("fixed_based_india"));
		deducteeMasterNonResidential.setFlatDoorBlockNo(rs.getString("flat_door_block_no"));
		deducteeMasterNonResidential.setInvoiceTransactionCount(rs.getInt("invoice_transaction_count"));
		deducteeMasterNonResidential.setIsTenFAvailable(rs.getBoolean("is_10f_available"));
		deducteeMasterNonResidential
				.setIsDeducteeHasAdditionalSections(rs.getBoolean("is_eligible_for_multiple_sections"));
		// is_no_pe_doc_available
		deducteeMasterNonResidential.setNoPEDocumentAvailable(rs.getBoolean("is_no_pe_doc_available"));
		// is_pe_in_india
		deducteeMasterNonResidential.setWhetherPEInIndia(rs.getBoolean("is_pe_in_india"));
		deducteeMasterNonResidential.setIsPOEMavailable(rs.getBoolean("is_poem_available"));
		deducteeMasterNonResidential.setIsTRCAvailable(rs.getBoolean("is_trc_available"));
		deducteeMasterNonResidential.setIsDeducteeTransparent(rs.getBoolean("isdeductee_transparent"));
		deducteeMasterNonResidential.setIsGrossingUp(rs.getBoolean("isgrossingup"));
		deducteeMasterNonResidential.setIstenfFuture(rs.getBoolean("istenf_future"));
		deducteeMasterNonResidential.setIstrcFuture(rs.getBoolean("istrc_future"));
		deducteeMasterNonResidential.setModifiedBy(rs.getString("modified_by"));
		deducteeMasterNonResidential.setModifiedDate(rs.getDate("modified_date"));
		deducteeMasterNonResidential.setModifiedName(rs.getString("modified_name"));
		deducteeMasterNonResidential.setDeducteeName(rs.getString("deductee_master_name"));
		deducteeMasterNonResidential.setNameBuildingVillage(rs.getString("name_building_village"));
		deducteeMasterNonResidential.setNameOfTheCompanyCode(rs.getString("name_of_the_company_code"));
		deducteeMasterNonResidential.setNameOfTheDeductee(rs.getString("name_of_the_deductee"));
		// no_pe_doc_address
		deducteeMasterNonResidential.setNoPeDocAddress(rs.getString("no_pe_doc_address"));
		deducteeMasterNonResidential.setNrRate(rs.getInt("nr_rate"));
		// deductee_master_pan
		deducteeMasterNonResidential.setDeducteePAN(rs.getString("deductee_master_pan"));
		deducteeMasterNonResidential.setPanStatus(rs.getString("pan_status"));
		deducteeMasterNonResidential.setPanVerifiedDate(rs.getDate("pan_verified_date"));
		// pe_applicable_from
		deducteeMasterNonResidential.setNoPEApplicableFrom(rs.getDate("pe_applicable_from"));
		// pe_applicable_to
		deducteeMasterNonResidential.setNoPEApplicableTo(rs.getDate("pe_applicable_to"));
		deducteeMasterNonResidential.setPeFileAddress(rs.getString("pe_file_address"));
		deducteeMasterNonResidential.setPhoneNumber(rs.getString("phone_number"));
		deducteeMasterNonResidential.setPinCode(rs.getString("pin_code"));
		deducteeMasterNonResidential.setPoemApplicableFrom(rs.getDate("poem_applicable_from"));
		deducteeMasterNonResidential.setPoemApplicableTo(rs.getDate("poem_applicable_to"));
		deducteeMasterNonResidential.setProvisionTransactionCount(rs.getInt("provision_transaction_count"));
		deducteeMasterNonResidential.setRate(rs.getBigDecimal("rate"));
		deducteeMasterNonResidential.setRelatedParty(rs.getString("related_party"));
		// residential_status
		deducteeMasterNonResidential.setDeducteeResidentialStatus(rs.getString("residential_status"));
		deducteeMasterNonResidential.setRoadStreetPostoffice(rs.getString("road_street_postoffice"));
		deducteeMasterNonResidential.setSection(rs.getString("section"));
		deducteeMasterNonResidential.setSourceFileName(rs.getString("source_file_name"));
		deducteeMasterNonResidential.setSourceIdentifier(rs.getString("source_identifier"));
		deducteeMasterNonResidential.setState(rs.getString("state"));
		deducteeMasterNonResidential.setTenfFutureDate(rs.getDate("tenf_future_date"));
		// tin_unique_identification
		deducteeMasterNonResidential.setDeducteeTin(rs.getString("tin_unique_identification"));
		deducteeMasterNonResidential.setTownCityDistrict(rs.getString("town_city_district"));
		deducteeMasterNonResidential.setTrcApplicableFrom(rs.getDate("trc_applicable_from"));
		deducteeMasterNonResidential.setTrcApplicableTo(rs.getDate("trc_applicable_to"));
		deducteeMasterNonResidential.setTrcFileAddress(rs.getString("trc_file_address"));
		deducteeMasterNonResidential.setTrcFutureDate(rs.getDate("trc_future_date"));
		deducteeMasterNonResidential.setUserDefinedField1(rs.getString("user_defined_field_1"));
		deducteeMasterNonResidential.setUserDefinedField2(rs.getString("user_defined_field_2"));
		deducteeMasterNonResidential.setUserDefinedField3(rs.getString("user_defined_field_3"));
		deducteeMasterNonResidential
				.setFixedbaseAvailbleIndiaApplicableFrom(rs.getDate("fixedbase_availble_india_applicable_from"));
		deducteeMasterNonResidential
				.setFixedbaseAvailbleIndiaApplicableTo(rs.getDate("fixedbase_availble_india_applicable_to"));
		deducteeMasterNonResidential.setIsAmountConnectedFixedBase(rs.getBoolean("is_amount_connected_fixed_base"));
		// is_business_carried_in_india
		deducteeMasterNonResidential.setIsBusinessCarriedInIndia(rs.getBoolean("is_business_carried_in_india"));
		deducteeMasterNonResidential.setIsFixedbaseAvailbleIndia(rs.getBoolean("is_fixedbase_availble_india"));
		// is_peamount_document
		deducteeMasterNonResidential.setIsPEdocument(rs.getBoolean("is_peamount_document"));
		deducteeMasterNonResidential.setIsPEamountReceived(rs.getBoolean("is_peamount_received"));
		deducteeMasterNonResidential.setIsPEinvoilvedInPurchaseGoods(rs.getBoolean("ispeinvoilvedin_purchase_goods"));
		deducteeMasterNonResidential.setPrinciplesOfBusinessPlace(rs.getString("principles_of_business_place"));
		deducteeMasterNonResidential.setStayPeriodFinancialYear(rs.getString("stay_period_financial_year"));
		deducteeMasterNonResidential
				.setWhetherPEInIndiaApplicableFrom(rs.getDate("weather_pe_in_india_applicable_from"));
		deducteeMasterNonResidential.setWhetherPEInIndiaApplicableTo(rs.getDate("weather_pe_in_india_applicable_to"));
		deducteeMasterNonResidential.setNatureOfPayment(rs.getString("nature_of_payment"));
		deducteeMasterNonResidential.setDeducteeAadharNumber(rs.getString("deductee_aadhar_number"));
		deducteeMasterNonResidential.setIsPoemDeclaration(rs.getBoolean("is_poem_declaration"));
		deducteeMasterNonResidential.setPoemFutureDate(rs.getDate("poem_future_date"));
		deducteeMasterNonResidential.setCountryToRemittance(rs.getString("country_to_remittance"));
		deducteeMasterNonResidential.setBeneficialOwnerOfIncome(rs.getBoolean("beneficial_owner_of_income"));
		deducteeMasterNonResidential
				.setIsBeneficialOwnershipOfDeclaration(rs.getBoolean("is_beneficial_ownership_of_declaration"));
		deducteeMasterNonResidential.setMliPptConditionSatisifed(rs.getBoolean("mli_ppt_condition_satisifed"));
		deducteeMasterNonResidential.setMliSlobConditionSatisifed(rs.getBoolean("mli_slob_condition_satisifed"));
		deducteeMasterNonResidential.setIsMliPptSlob(rs.getBoolean("is_mli_ppt_slob"));
		deducteeMasterNonResidential.setNatureOfRemittance(rs.getString("nature_of_remittance"));
		deducteeMasterNonResidential.setArticleNumberDtaa(rs.getString("article_number_dtaa"));
		deducteeMasterNonResidential.setSectionOfIncometaxAct(rs.getString("section_of_incometax_act"));
		deducteeMasterNonResidential.setAggreementForTransaction(rs.getBoolean("aggreement_for_transaction"));
		deducteeMasterNonResidential.setSectionCode(
				StringUtils.isEmpty(rs.getString("section_code")) ? StringUtils.EMPTY : rs.getString("section_code"));
		deducteeMasterNonResidential.setAdditionalSectionCode(
				StringUtils.isEmpty(rs.getString("additional_section_code")) ? StringUtils.EMPTY
						: rs.getString("additional_section_code"));
		deducteeMasterNonResidential.setDeducteeKey(rs.getString("deductee_key"));
		deducteeMasterNonResidential.setDeducteeEnrichmentKey(rs.getString("deductee_enrichment_key"));
		deducteeMasterNonResidential.setAdditionalSectionThresholds(rs.getString("additional_section_thresholds"));
		deducteeMasterNonResidential.setMatchScore(rs.getString("match_score"));
		deducteeMasterNonResidential.setNameAsPerTraces(rs.getString("name_as_per_traces"));
		deducteeMasterNonResidential.setPanAsPerTraces(rs.getString("pan_as_per_traces"));
		deducteeMasterNonResidential.setRemarksAsPerTraces(rs.getString("remarks_as_per_traces"));
		// new column's
		deducteeMasterNonResidential
				.setIsNoFixedBaseDeclarationAvailable(rs.getBoolean("is_no_fixed_base_declaration_available"));
		deducteeMasterNonResidential.setNoFixedBaseDeclarationAvailableInFuture(
				rs.getBoolean("no_fixed_base_declaration_available_in_future"));
		deducteeMasterNonResidential.setNoFixedBaseDeclarationAvailableFutureDate(
				rs.getDate("no_fixed_base_declaration_available_future_date"));
		deducteeMasterNonResidential
				.setNoFixedBaseDeclarationFromDate(rs.getDate("no_fixed_base_declaration_from_date"));
		deducteeMasterNonResidential.setNoFixedBaseDeclarationToDate(rs.getDate("no_fixed_base_declaration_to_date"));
		deducteeMasterNonResidential.setTdsExemptionFlag(rs.getBoolean("tds_exemption_flag"));
		deducteeMasterNonResidential.setTdsExemptionReason(rs.getString("tds_exemption_reason"));
		deducteeMasterNonResidential.setCharteredAccountantNo(rs.getString("chartered_accountant_no"));
		deducteeMasterNonResidential.setDeducteeGSTIN(rs.getString("deductee_gstin"));
		deducteeMasterNonResidential.setGrOrIRIndicator(rs.getString("gr_or_ir_indicator"));
		deducteeMasterNonResidential.setTdsApplicabilityUnderSection(rs.getString("tds_applicability_under_section"));
		deducteeMasterNonResidential.setPreviousBalanceMonth(rs.getInt("previous_balance_month"));
		deducteeMasterNonResidential.setPreviousBalanceYear(rs.getInt("previous_balance_year"));
		deducteeMasterNonResidential.setCurrentBalanceMonth(rs.getInt("current_balance_month"));
		deducteeMasterNonResidential.setCurrentBalanceYear(rs.getInt("current_balance_year"));
		deducteeMasterNonResidential
				.setDeducteeMasterBalancesOf194q(rs.getBigDecimal("deductee_master_balances_of_194q"));
		deducteeMasterNonResidential.setAdvanceBalancesOf194q(rs.getBigDecimal("advance_balances_of_194q"));
		deducteeMasterNonResidential.setProvisionBalancesOf194q(rs.getBigDecimal("provision_balances_of_194q"));
		deducteeMasterNonResidential.setOpeningBalanceCreditNote(rs.getBigDecimal("opening_balance_creditNote"));
		deducteeMasterNonResidential.setAdvancesAsOfMarch(rs.getBigDecimal("advances_as_of_march"));
		deducteeMasterNonResidential.setProvisionsAsOfMarch(rs.getBigDecimal("provisions_as_of_march"));
		deducteeMasterNonResidential.setDeducteeTan(rs.getString("deductee_tan"));
		deducteeMasterNonResidential.setTdsExemptionNumber(rs.getString("tds_exemption_number"));
		deducteeMasterNonResidential.setTdsSectionDescription(rs.getString("tds_section_description"));
		deducteeMasterNonResidential
				.setNoPEDeclarationAvailableInFuture(rs.getBoolean("no_pe_declaration_available_in_future"));
		deducteeMasterNonResidential
				.setNoPEDeclarationAvailableFutureDate(rs.getDate("no_pe_declaration_available_future_date"));
		deducteeMasterNonResidential.setIsNoPOEMDeclarationAvailable(rs.getBoolean("is_no_poem_declaration_available"));
		deducteeMasterNonResidential
				.setNoPOEMDeclarationApplicableFromDate(rs.getDate("no_poem_declaration_applicable_from_date"));
		deducteeMasterNonResidential
				.setNoPOEMDeclarationApplicableToDate(rs.getDate("no_poem_declaration_applicable_to_date"));
		deducteeMasterNonResidential.setIsThresholdLimitApplicable(rs.getBoolean("is_threshold_limit_applicable"));

		return deducteeMasterNonResidential;

	}
}
