package com.ey.in.tds.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.RowMapper;

import com.ey.in.tds.common.onboarding.jdbc.dto.ShareholderDeclarationRateType;

/**
 * 
 * @author vamsir
 *
 */
public class ShareholderDeclarationRateTypeRowMapper implements RowMapper<ShareholderDeclarationRateType> {

	@Override
	public ShareholderDeclarationRateType mapRow(ResultSet rs, int rowNum) throws SQLException {
		ShareholderDeclarationRateType shareholder = new ShareholderDeclarationRateType();

		shareholder.setAreaLocality(rs.getString("area_locality"));
		shareholder.setContact(rs.getString("contact"));
		shareholder.setCountry(rs.getString("country"));
		shareholder.setCreatedBy(rs.getString("created_by"));
		shareholder.setCreatedDate(rs.getTimestamp("created_date"));
		shareholder.setDeductorPan(rs.getString("deductor_master_pan"));
		shareholder.setDematAccountNo(rs.getString("demat_account_no"));
		shareholder.setEmailId(rs.getString("email_id"));
		shareholder.setFlatDoorBlockNo(rs.getString("flat_door_block_no"));
		shareholder.setFolioNo(rs.getString("folio_no"));
		shareholder.setForm15ghAvailable(rs.getInt("form15gh_available") == 1 ? true : false);
		shareholder.setForm15ghFileAddress(rs.getString("form15gh_file_address"));
		shareholder.setForm15ghUniqueIdentificationNo(rs.getString("form15gh_unique_identification_no"));
		shareholder.setId(rs.getInt("shareholder_master_id"));
		shareholder.setKeyShareholder(rs.getInt("key_shareholder") == 1 ? true : false);
		shareholder.setMatchScore(rs.getString("match_score"));
		shareholder.setModifiedBy(rs.getString("modified_by"));
		shareholder.setModifiedDate(rs.getTimestamp("modified_date"));
		shareholder.setNameAsPerTraces(rs.getString("name_as_per_traces"));
		shareholder.setNameBuildingVillage(rs.getString("name_building_village"));
		shareholder.setPanAsPerTraces(rs.getString("pan_as_per_traces"));
		shareholder.setPanStatus(rs.getString("pan_status") == null ? StringUtils.EMPTY : rs.getString("pan_status"));
		shareholder.setPanVerifiedDate(rs.getTimestamp("pan_verified_date"));
		shareholder.setPercentageSharesHeld(rs.getBigDecimal("percetage_of_share_held"));
		shareholder.setPinCode(rs.getString("pin_code"));
		shareholder.setRemarksAsPerTraces(rs.getString("remarks_as_per_traces"));
		shareholder.setRoadStreetPostoffice(rs.getString("road_street_postoffice"));
		shareholder.setShareholderCategory(rs.getString("category"));
		shareholder.setShareholderName(rs.getString("name"));
		shareholder.setShareholderPan(rs.getString("pan"));
		shareholder.setShareholderResidentialStatus(rs.getString("residential_status"));
		shareholder.setShareholderType(rs.getString("type"));
		shareholder.setShareTransferAgentName(rs.getString("share_transfer_agent_name"));
		shareholder.setSourceFileName(rs.getString("source_file_name"));
		shareholder.setState(rs.getString("state"));
		shareholder.setTotalSharesHeld(rs.getBigDecimal("total_of_shares_held"));
		shareholder.setTownCityDistrict(rs.getString("town_city_district"));
		shareholder.setVersion(rs.getInt("version"));
		shareholder.setSourceIdentifier(rs.getString("source_identifier"));
		shareholder.setTransactionCount(rs.getInt("transaction_count"));
		shareholder.setLastPayoutId(rs.getInt("last_payout_invoice_id"));
		shareholder.setTenantId(rs.getString("user_tenant_id"));
		shareholder.setActive(rs.getBoolean("active"));
		shareholder.setAadharNumber(rs.getString("aadhar_number"));
		shareholder.setUniqueIdentificationNumber(rs.getString("unique_shareholder_identification_number"));
		shareholder.setPanAadhaarLinkStatus(rs.getString("pan_aadhaar_link_status"));
		shareholder.setBatchUploadId(rs.getInt("batch_upload_id"));
		shareholder.setRateType(rs.getString("rate_type"));
		shareholder.setPanAllotmentDate(rs.getDate("pan_allotment_date"));
		shareholder.setTdsOrTcs(rs.getString("tds_or_tcs"));
		shareholder.setSpecifiedPerson(rs.getString("specified_person"));
		shareholder.setDeclarationApplicableFrom(rs.getDate("declarationApplicableFrom"));

		return shareholder;
	}

}
