package com.ey.in.tds.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ey.in.tds.common.onboarding.jdbc.dto.KYCDetails;

/**
 * 
 * @author vamsir
 *
 */
public class KYCDetailsRowMapper implements RowMapper<KYCDetails> {

	@Override
	public KYCDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
		KYCDetails kyc = new KYCDetails();

		kyc.setId(rs.getInt("id"));
		kyc.setCustomerName(rs.getString("customer_name"));
		kyc.setCustomerPan(rs.getString("customer_pan"));
		kyc.setCustomerCode(rs.getString("customer_code"));
		kyc.setEmailId(rs.getString("email_id"));
		kyc.setIsPanExists(rs.getBoolean("is_pan_exists"));
		kyc.setKycPan(rs.getString("kyc_pan"));
		kyc.setIsKycSubmitted(rs.getBoolean("is_kyc_submitted"));
		kyc.setAcceptTermsAndConditions(rs.getBoolean("accept_terms_and_conditions"));
		kyc.setApplicableFrom(rs.getTimestamp("applicable_from"));
		kyc.setApplicableTo(rs.getTimestamp("applicable_to"));
		kyc.setDeductorMasterTan(rs.getString("deductor_master_tan"));
		kyc.setDeductorPan(rs.getString("deductor_pan"));
		kyc.setActive(rs.getBoolean("active"));
		kyc.setCreatedBy(rs.getString("created_by"));
		kyc.setCreatedDate(rs.getTimestamp("created_date"));
		kyc.setModifiedBy(rs.getString("modified_by"));
		kyc.setModifiedDate(rs.getTimestamp("modified_date"));
		kyc.setPhoneNumber(rs.getString("phone_number"));
		kyc.setItrAttachmentYear1(rs.getString("itr_attachment_year1"));
		kyc.setItrAttachmentYear2(rs.getString("itr_attachment_year2"));
		kyc.setItrAttachmentYear3(rs.getString("itr_attachment_year3"));
		kyc.setMatchScore(rs.getString("match_score"));
		kyc.setTdsTcsClientFinalResponse(rs.getString("tds_tcs_client_final_response"));
		kyc.setAggregateTcsOrTdsGreaterThan50kForYear1(rs.getString("aggregate_tcs_or_tds_greater_than50k_for_year1"));
		kyc.setAggregateTcsOrTdsGreaterThan50kForYear2(rs.getString("aggregate_tcs_or_tds_greater_than50k_for_year2"));
		kyc.setAggregateTcsOrTdsGreaterThan50kForYear3(rs.getString("aggregate_tcs_or_tds_greater_than50k_for_year3"));
		kyc.setIsHigherTcsRateApplicable(rs.getBoolean("is_higher_tcs_rate_applicable"));
		kyc.setHigherTcsRateApplicableConclusion(rs.getString("higher_tcs_rate_applicable_conclusion"));
		kyc.setItrFinancialYear1(rs.getString("itr_financial_year1"));
		kyc.setItrFinancialYear2(rs.getString("itr_financial_year2"));
		kyc.setItrFinancialYear3(rs.getString("itr_financial_year3"));
		kyc.setAcknowledgementItrYear1(rs.getString("acknowledgement_itr_year1"));
		kyc.setAcknowledgementItrYear2(rs.getString("acknowledgement_itr_year2"));
		kyc.setAcknowledgementItrYear3(rs.getString("acknowledgement_itr_year3"));
		kyc.setIsFormSubmitted(rs.getBoolean("is_form_submitted"));
		kyc.setIsAuthorizedPerson(rs.getBoolean("is_authorized_person"));
		kyc.setTurnoverExceed10cr(rs.getString("turnover_exceed_10cr"));
		kyc.setTdsTcsApplicabilityIndicator(rs.getString("tds_tcs_applicability_indicator"));
		kyc.setTanApplicable(rs.getString("tan_applicable"));
		kyc.setTanNumber(rs.getString("tan_number"));
		kyc.setBasisEmailSent(rs.getTimestamp("basis_email_sent"));
		kyc.setYear(rs.getInt("year"));
		kyc.setIsEmailTriggered(rs.getBoolean("is_email_triggered"));
		kyc.setSignedName(rs.getString("signed_name"));
		kyc.setSignedDesignation(rs.getString("signed_designation"));
		kyc.setSignedEmailId(rs.getString("signed_email_id"));
		kyc.setPanVerificationDate(rs.getTimestamp("pan_verification_date"));
		kyc.setIsPanVerifyStatus(rs.getBoolean("is_pan_verify_status"));
		kyc.setRemarkAsPerTraces(rs.getString("remark_as_per_traces"));
		kyc.setNameAsPerTraces(rs.getString("name_as_per_traces"));
		kyc.setPanAsPerTraces(rs.getString("pan_as_per_traces"));
		kyc.setTcsTdsApplicabilityUserAction(rs.getString("tcs_tds_applicability_user_action"));
		kyc.setType(rs.getString("type"));
		kyc.setRemarks(rs.getString("remarks"));
		kyc.setBatchUploadId(rs.getInt("batch_upload_id"));
		kyc.setRedisKey(rs.getString("redis_key"));
		kyc.setSignedNameforNo(rs.getString("signed_name_for_no"));
		kyc.setSignedMobileNumber(rs.getString("signed_mobile_number"));
		kyc.setKycRemarks(rs.getString("kyc_remarks"));
		kyc.setPanFilePath(rs.getString("pan_file_path"));
		kyc.setFsFilePath(rs.getString("fs_file_path"));
		kyc.setIsTanFiles(rs.getBoolean("is_tan_files"));
		kyc.setIndemnifyDeclare(rs.getBoolean("indemnify_declare"));
		kyc.setFinalRateUserAction(rs.getString("final_rate_user_action"));
		kyc.setYesSignedEmailId(rs.getString("yes_signed_email_id"));
		
		return kyc;
	}

}
