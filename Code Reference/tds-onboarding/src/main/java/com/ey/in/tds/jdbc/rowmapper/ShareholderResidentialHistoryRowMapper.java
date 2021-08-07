package com.ey.in.tds.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ey.in.tds.common.onboarding.jdbc.dto.ShareholderMasterResidential;
import com.microsoft.sqlserver.jdbc.StringUtils;

public class ShareholderResidentialHistoryRowMapper implements RowMapper<ShareholderMasterResidential>{

	@Override
	public ShareholderMasterResidential mapRow(ResultSet rs, int rowNum) throws SQLException {
		ShareholderMasterResidential dto=new ShareholderMasterResidential();
		
		dto.setAreaLocality(rs.getString("area_locality"));
		//TODO need to check below line
		//dto.setAssessmentYearDividendDetails(rs.getInt("assessment_year_dividend_details"));
		dto.setContact(rs.getString("contact"));
		dto.setCountry(rs.getString("country"));
		dto.setCreatedBy(rs.getString("created_by"));
		dto.setCreatedDate(rs.getTimestamp("created_date"));
		dto.setDeductorPan(rs.getString("deductor_master_pan"));
		dto.setDematAccountNo(rs.getString("demat_account_no"));
		dto.setEmailId(rs.getString("email_id"));
		dto.setFlatDoorBlockNo(rs.getString("flat_door_block_no"));
		dto.setFolioNo(rs.getString("folio_no"));
		dto.setForm15ghAvailable(rs.getInt("form15gh_available")==1?true:false);
		dto.setForm15ghFileAddress(rs.getString("form15gh_file_address"));
		dto.setForm15ghUniqueIdentificationNo(null);
		dto.setId(rs.getInt("shareholder_master_id"));
		dto.setKeyShareholder(rs.getInt("key_shareholder")==1?true:false);
		dto.setLastPayoutId(rs.getInt("last_payout_invoice_id"));
		dto.setMatchScore(rs.getString("match_score"));
		dto.setModifiedBy(rs.getString("modified_by"));
		dto.setModifiedDate(rs.getTimestamp("modified_date"));
		dto.setNameAsPerTraces(rs.getString("pan_as_per_traces"));
		dto.setNameBuildingVillage(rs.getString("name_building_village"));
		dto.setPanAsPerTraces(rs.getString("pan_as_per_traces"));
		dto.setPanStatus(rs.getString("pan_status")==null?StringUtils.EMPTY:rs.getString("pan_status"));
		dto.setPanVerifiedDate(rs.getTimestamp("pan_verified_date"));
		dto.setPercentageSharesHeld(rs.getBigDecimal("percetage_of_share_held"));
		dto.setPinCode(rs.getString("pin_code"));
		dto.setRemarksAsPerTraces(rs.getString("remarks_as_per_traces"));
		dto.setRoadStreetPostoffice(rs.getString("road_street_postoffice"));
		dto.setShareholderCategory(rs.getString("category"));
		dto.setShareholderName(rs.getString("name"));
		dto.setShareholderPan(rs.getString("pan"));
		dto.setShareholderResidentialStatus(null);
		dto.setSourceFileName(rs.getString("source_file_name"));
		dto.setShareholderType(rs.getString("type"));
		dto.setSourceIdentifier(rs.getString("source_identifier"));
		dto.setShareTransferAgentName(rs.getString("share_transfer_agent_name"));
		dto.setState(rs.getString("state"));
		dto.setTotalSharesHeld(rs.getBigDecimal("total_of_shares_held"));
		dto.setTownCityDistrict(rs.getString("town_city_district"));
		dto.setTransactionCount(rs.getInt("transaction_count"));
		dto.setVersion(rs.getInt("version"));
		
		return dto;
	}
}
