package com.ey.in.tds.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ey.in.tds.common.onboarding.jdbc.dto.CollectorOnBoardingInformationDTO;

/**
 * 
 * @author scriptbees
 */
public class CollectorOnBoardingInfoRowMapper implements RowMapper<CollectorOnBoardingInformationDTO> {

	@Override
	public CollectorOnBoardingInformationDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		CollectorOnBoardingInformationDTO collectorOnboardingInfo = new CollectorOnBoardingInformationDTO();

		collectorOnboardingInfo.setCollectorOnboardingInfoId(rs.getInt("id"));
		collectorOnboardingInfo.setCollectorMasterId(rs.getInt("collector_master_id"));
		collectorOnboardingInfo.setPan(rs.getString("pan"));
		collectorOnboardingInfo.setActive(rs.getBoolean("active"));
		collectorOnboardingInfo.setApplicableFrom(rs.getDate("applicable_from"));
		collectorOnboardingInfo.setApplicableTo(rs.getDate("applicable_to"));
		collectorOnboardingInfo.setCreditNotes(rs.getString("credit_notes_id"));
		collectorOnboardingInfo.setChallanGeneration(rs.getString("challan_generation_id"));
		collectorOnboardingInfo.setScopeProcess(rs.getString("scope_process"));
		collectorOnboardingInfo.setInvoiceProcessScope(rs.getString("invoice_process_scope"));
		collectorOnboardingInfo.setPriority(rs.getString("priority"));
		collectorOnboardingInfo.setBuyerThresholdComputation(rs.getString("buyer_threshold_computation_id"));
		collectorOnboardingInfo.setSectionDetermination(rs.getString("section_determination_id"));
		collectorOnboardingInfo.setLccTrackingNotification(rs.getString("lcc_tracking_notification"));
		collectorOnboardingInfo.setTcsApplicability(rs.getString("tcs_applicability_id"));
		collectorOnboardingInfo.setTdsTransaction(rs.getString("tds_transaction_id"));
		collectorOnboardingInfo.setGstImplication(rs.getString("gst_implication_id"));
		collectorOnboardingInfo.setCollectionReferenceId(rs.getString("collection_reference_id"));
		collectorOnboardingInfo.setDocumentOrPostingDate(rs.getString("document_or_posting_date"));

		return collectorOnboardingInfo;
	}

}
