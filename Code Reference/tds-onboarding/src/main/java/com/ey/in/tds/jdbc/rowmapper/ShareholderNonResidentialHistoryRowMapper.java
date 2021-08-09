package com.ey.in.tds.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ey.in.tds.common.onboarding.jdbc.dto.ShareholderNonResidentialHistory;
import com.microsoft.sqlserver.jdbc.StringUtils;

public class ShareholderNonResidentialHistoryRowMapper implements RowMapper<ShareholderNonResidentialHistory> {

	@Override
	public ShareholderNonResidentialHistory mapRow(ResultSet rs, int rowNum) throws SQLException {
		ShareholderNonResidentialHistory dto = new ShareholderNonResidentialHistory();
		dto.setAreaLocality(rs.getString("area_locality"));
		// dto.setAssessmentYearDividendDetails(rs.get(""));
		dto.setContact(rs.getString("contact"));
		dto.setCountry(rs.getString("country"));
		dto.setCreatedBy(rs.getString("created_by"));
		dto.setCreatedDate(rs.getTimestamp("created_date"));
		dto.setDeductorPan(rs.getString("deductor_master_pan"));
		dto.setDematAccountNo(rs.getString("demat_account_no"));
		dto.setEmailId(rs.getString("email_id"));
		dto.setFlatDoorBlockNo(rs.getString("flat_door_block_no"));
		dto.setFolioNo(rs.getString("folio_no"));
		dto.setIcelandDividendTaxationRate(rs.getBigDecimal("iceland_dividend_taxation_rate"));
		dto.setId(rs.getInt("shareholder_master_id"));
		dto.setIsBeneficialOwnerOfIncome(rs.getInt("is_beneficial_owner_of_income") == 1 ? true : false);
		dto.setIsBeneficialOwnershipDeclarationAvailable(
				rs.getInt("is_beneficial_ownership_declaration_available") == 1 ? true : false);
		dto.setIsCommercialIndemnityOrTreatyBenefitsWithoutDocuments(
				rs.getInt("is_commercial_indemnity_or_treaty_benefits_without_documents") == 1 ? true : false);
		dto.setIsIcelandRateLessThanTwenty(rs.getInt("is_iceland_rate_less_than_twenty") == 1 ? true : false);
		dto.setIsKuwaitShareholderType(rs.getInt("iskuwaitshareholdertype") == 1 ? true : false);
		dto.setIsMliSlobSatisfactionDeclarationAvailable(
				rs.getInt("is_mli_slob_satisfaction_declaration_available") == 1 ? true : false);
		dto.setIsNoPeDeclarationAvailable(rs.getInt("is_no_pe_declaration_available") == 1 ? true : false);
		dto.setIsPoemOfShareholderInIndia(rs.getInt("is_poem_of_shareholder_in_india") == 1 ? true : false);
		dto.setIsPeAvailableInIndia(rs.getInt("is_pe_available_in_india") == 1 ? true : false);
		dto.setIsTenfAvailable(rs.getInt("is_tenf_available") == 1 ? true : false);
		dto.setIsTransactionGAARCompliant(rs.getInt("is_transaction_gaar_compliant") == 1 ? true : false);
		dto.setIsTrcAvailable(rs.getInt("is_trc_available") == 1 ? true : false);
		dto.setIsUKVehicleExemptTax(rs.getInt("is_uk_vehicle_exempt_tax") == 1 ? true : false);
		dto.setKeyShareholder(rs.getInt("key_shareholder") == 1 ? true : false);
		dto.setMatchScore(rs.getString("match_score"));
		dto.setMliFileAddress(rs.getString("mli_slob_declaration_file_address"));
		dto.setModifiedBy(rs.getString("modified_by"));
		dto.setModifiedDate(rs.getTimestamp("modified_date"));
		dto.setNameAsPerTraces(rs.getString("name_as_per_traces"));
		dto.setNameBuildingVillage(rs.getString("name_building_village"));
		dto.setNoPeDeclarationApplicableFrom(rs.getTimestamp("no_pe_declaration_applicable_from"));
		dto.setNoPeDeclarationApplicableTo(rs.getTimestamp("no_pe_declaration_applicable_to"));
		dto.setNoPeFileAddress(rs.getString("no_pe_declaration_file_address"));
		dto.setNoPoemDeclarationInIndiaApplicableFrom(rs.getTimestamp("no_poem_in_india_applicable_from"));
		dto.setNoPoemDeclarationInIndiaApplicableTo(rs.getTimestamp("no_poem_in_india_applicable_to"));
		dto.setNoPoemFileAddress(rs.getString("no_poem_declaration_file_address"));
		dto.setPanAsPerTraces(rs.getString("pan_as_per_traces"));
		dto.setPanStatus(rs.getString("pan_status") == null ? StringUtils.EMPTY : rs.getString("pan_status"));
		dto.setPanVerifiedDate(rs.getTimestamp("pan_verified_date"));
		dto.setPercentageSharesHeld(rs.getBigDecimal("percetage_of_share_held"));
		dto.setPinCode(rs.getString("pin_code"));
		dto.setPrincipalPlaceOfBusiness(rs.getString("principal_place_of_business"));
		dto.setRemarksAsPerTraces(rs.getString("remarks_as_per_traces"));
		dto.setRoadStreetPostoffice(rs.getString("road_street_postoffice"));
		dto.setShareHeldFromDate(rs.getTimestamp("share_held_from_date"));
		dto.setShareHeldToDate(rs.getTimestamp("share_held_to_date"));
		dto.setShareholderCategory(rs.getString("category"));
		dto.setShareholderName(rs.getString("name"));
		dto.setShareholderPan(rs.getString("pan"));
		dto.setShareholderResidentialStatus(rs.getString("residential_status"));
		dto.setShareholderTin(rs.getString("tin"));
		dto.setShareholderType(rs.getString("type"));
		dto.setShareTransferAgentName(rs.getString("share_transfer_agent_name"));
		dto.setSourceFileName(rs.getString("source_file_name"));
		dto.setSourceIdentifier(rs.getString("source_identifier"));
		dto.setState(rs.getString("state"));
		dto.setTenfApplicableFrom(rs.getTimestamp("tenf_applicable_from"));
		dto.setTenfApplicableTo(rs.getTimestamp("tenf_applicable_to"));
		dto.setTenfFileAddress(rs.getString("tenf_file_address"));
		dto.setTotalSharesHeld(rs.getBigDecimal("total_shares_held"));
		dto.setTownCityDistrict(rs.getString("town_city_district"));
		dto.setTrcApplicableFrom(rs.getTimestamp("trc_applicable_from"));
		dto.setTrcApplicableTo(rs.getTimestamp("trc_applicable_to"));
		dto.setTrcFileAddress(rs.getString("trc_file_address"));
		dto.setVersion(rs.getInt("version"));
		dto.setTenantId(rs.getString("user_tenant_id"));
		dto.setBeneficialOwnershipFileAddress(rs.getString("beneficial_ownership_file_address"));
		dto.setIsNoPoemDeclarationAvailable(rs.getBoolean("is_no_poem_declaration_available"));
		dto.setShareholderType(rs.getString("shareholder_master_type"));
		dto.setActive(rs.getBoolean("active"));
		dto.setTransactionCount(rs.getInt("transaction_count"));
		dto.setForm15cacbApplicable(rs.getString("form15cacb_applicable"));
		dto.setLastPayoutInvoiceId(rs.getInt("last_payout_invoice_id"));
		
		return dto;
	}
	
	
}
