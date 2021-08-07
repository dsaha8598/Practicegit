package com.ey.in.tds.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorMaster;
/**
 * 
 * @author scriptbees.
 *
 */
public class DeductorMasterRowMapper implements RowMapper<DeductorMaster> {

	@Override
	public DeductorMaster mapRow(ResultSet rs, int rowNum) throws SQLException {
		DeductorMaster deductorMaster = new DeductorMaster();
		deductorMaster.setDeductorMasterId(rs.getInt("deductor_master_id"));
		deductorMaster.setActive(rs.getBoolean("active"));
		deductorMaster.setApplicableFrom(rs.getDate("applicable_from"));
		deductorMaster.setApplicableTo(rs.getDate("applicable_to"));
		deductorMaster.setCode(rs.getString("code"));
		deductorMaster.setCreatedBy(rs.getString("created_by"));
		deductorMaster.setCreatedDate(rs.getDate("created_date"));
		deductorMaster.setDueDateOfTaxPayment(rs.getDate("due_date_of_tax_payment"));
		deductorMaster.setEmail(rs.getString("email"));
		deductorMaster.setHaveMoreThanOneBranch(rs.getBoolean("have_more_than_one_branch"));
		deductorMaster.setModeOfPayment(rs.getString("mode_of_payment"));
		deductorMaster.setModifiedBy(rs.getString("modified_by"));
		deductorMaster.setModifiedDate(rs.getDate("modified_date"));
		deductorMaster.setName(rs.getString("name"));
		deductorMaster.setPanField(rs.getString("deductor_master_pan"));
		deductorMaster.setPhoneNumber(rs.getString("phone_number"));
		deductorMaster.setResidentialStatus(rs.getString("residential_status"));
		deductorMaster.setStatus(rs.getString("status"));
		deductorMaster.setType(rs.getString("type"));
		deductorMaster.setGstin(rs.getString("gstin"));
		deductorMaster.setEmailAlternate(rs.getString("alternate_email"));
		deductorMaster.setPhoneNumberAlternate(rs.getString("alternate_phone_number"));
		deductorMaster.setModuleType(rs.getString("module_type"));
		deductorMaster.setDvndDeductorTypeName(rs.getString("deductor_type_name"));
		deductorMaster.setDeductorSalutation(rs.getString("salutation"));
		
		return deductorMaster;
	}

}
