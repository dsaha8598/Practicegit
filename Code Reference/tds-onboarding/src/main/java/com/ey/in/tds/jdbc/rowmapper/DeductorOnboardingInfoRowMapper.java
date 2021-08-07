
package com.ey.in.tds.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

import com.ey.in.tds.common.domain.masters.DividendOnboardingInfoMetaData.PrepForm15CaCb;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorOnboardingInformationDTO;

/**
 * 
 * @author scriptbees
 */
public class DeductorOnboardingInfoRowMapper implements RowMapper<DeductorOnboardingInformationDTO> {

	@Override
	public DeductorOnboardingInformationDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		DeductorOnboardingInformationDTO deductorOnboardingInfo = new DeductorOnboardingInformationDTO();

		deductorOnboardingInfo.setDeductorOnboardingInfoId(rs.getInt("deductor_onboarding_info_id"));
		deductorOnboardingInfo.setDeductorMasterId(rs.getInt("deductor_master_id"));
		deductorOnboardingInfo.setPan(rs.getString("pan"));
		deductorOnboardingInfo.setAccountNumber(rs.getString("accountnumber"));
		deductorOnboardingInfo.setActive(rs.getBoolean("active"));
		deductorOnboardingInfo.setApplicableFrom(rs.getDate("applicable_from"));
		deductorOnboardingInfo.setApplicableTo(rs.getDate("applicable_to"));
		deductorOnboardingInfo.setCnp(rs.getBoolean("cnp"));
		deductorOnboardingInfo.setCp(rs.getBoolean("cp"));
		deductorOnboardingInfo.setCrpt(rs.getString("crpt"));
		deductorOnboardingInfo.setCustomJobs(rs.getString("custom_jobs"));
		deductorOnboardingInfo.setIpp(rs.getString("ipp"));
		deductorOnboardingInfo.setPpa(rs.getString("ppa"));
		deductorOnboardingInfo.setPriority(rs.getString("priority"));
		deductorOnboardingInfo.setProvisionProcessing(rs.getString("provisionprocessing"));
		deductorOnboardingInfo.setAdvanceProcessing(rs.getString("advance_processing"));
		deductorOnboardingInfo.setProvisionTracking(rs.getString("provisiontracking"));
		deductorOnboardingInfo.setTif(rs.getString("tif"));
		deductorOnboardingInfo.setCreatedBy(rs.getString("created_by"));
		deductorOnboardingInfo.setModifiedBy(rs.getString("modified_by"));
		deductorOnboardingInfo.setCreatedDate(rs.getDate("created_date"));
		deductorOnboardingInfo.setModifiedDate(rs.getDate("modified_date"));
		deductorOnboardingInfo.setRoundoff(rs.getString("rounding_off"));
		deductorOnboardingInfo.setPertransactionlimit(rs.getString("per_transaction_limit"));
		deductorOnboardingInfo.setInterestCalculationType(rs.getString("intereset_calculation_type"));
		deductorOnboardingInfo.setDvndDdtPaidBeforeEOY(rs.getInt("ddt_paid_before_eoy")==1?true:false);
		deductorOnboardingInfo.setDvndEnabled(rs.getInt("dvnd_enabled")==1?true:false);
		deductorOnboardingInfo.setDvndFileForm15gh(rs.getInt("file_form_15_gh")==1?true:false);
		String prepForm15CaCb=rs.getString("prep_form_15_ca_cb");
		if(StringUtils.isNotBlank(prepForm15CaCb)) {
		deductorOnboardingInfo.setDvndPrepForm15CaCb(PrepForm15CaCb.valueOf(prepForm15CaCb));
		}
		String sections=rs.getString("sections_for_transaction_limit");
		//added null and empty check so that existing onbording info will not break
		if(StringUtils.isNotBlank(sections)){
			sections=sections.replace("[", "").replace("]", "");
			List<String> selectedSections = Arrays.stream(sections.split(","))
				    .map(String::trim)
				    .collect(Collectors.toList());
			deductorOnboardingInfo.setSelectedSectionsForTransactionLimit(selectedSections);
		}
		deductorOnboardingInfo.setStringClientSpecificRules(rs.getString("client_specific_rules"));
		return deductorOnboardingInfo;
	}

}
