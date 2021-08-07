package com.ey.in.tds.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ey.in.tds.common.model.deductee.DeducteeGstinStatus;

/**
 * 
 * @author vamsir
 *
 */
public class DeducteeGstinStatusRowMapper implements RowMapper<DeducteeGstinStatus> {

	@Override
	public DeducteeGstinStatus mapRow(ResultSet rs, int rowNum) throws SQLException {
		DeducteeGstinStatus deducteeGstinStatus = new DeducteeGstinStatus();

		deducteeGstinStatus.setId(rs.getInt("id"));
		deducteeGstinStatus.setActive(rs.getBoolean("active"));
		deducteeGstinStatus.setDeducteeId(rs.getInt("deductee_id"));
		deducteeGstinStatus.setCreatedBy(rs.getString("created_by"));
		deducteeGstinStatus.setCreatedDate(rs.getDate("created_date"));
		deducteeGstinStatus.setGstnReturnStatus(rs.getString("gstn_return_status"));
		deducteeGstinStatus.setGstnStatus(rs.getString("gstn_status"));
		deducteeGstinStatus.setModifiedBy(rs.getString("modified_by"));
		deducteeGstinStatus.setModifiedDate(rs.getDate("modified_date"));
		deducteeGstinStatus.setYear(rs.getInt("year"));
		deducteeGstinStatus.setLegalNameOfBusiness(rs.getString("legal_name_of_business"));
		deducteeGstinStatus.setDateOfCancellation(rs.getString("date_of_cancellation"));
		deducteeGstinStatus.setLastUpdatedDate(rs.getString("last_updated_date"));
		deducteeGstinStatus.setTradeName(rs.getString("trade_name"));
		deducteeGstinStatus.setGstr1DateOfFiling(rs.getString("gstr1_date_of_filing"));
		deducteeGstinStatus.setGstr1ReturnFiled(rs.getString("gstr1_return_filed"));
		deducteeGstinStatus.setGstr1IsReturnValid(rs.getString("gstr1_is_return_valid"));
		deducteeGstinStatus.setGstr3DateOfFiling(rs.getString("gstr3_date_of_filing"));
		deducteeGstinStatus.setGstr3ReturnFiled(rs.getString("gstr3_return_filed"));
		deducteeGstinStatus.setGstr3IsReturnValid(rs.getString("gstr3_is_return_valid"));
		deducteeGstinStatus.setGstr6DateOfFiling(rs.getString("gstr6_date_of_filing"));
		deducteeGstinStatus.setGstr6ReturnFiled(rs.getString("gstr6_return_filed"));
		deducteeGstinStatus.setGstr6IsReturnValid(rs.getString("gstr6_is_return_valid"));
		deducteeGstinStatus.setGstr9DateOfFiling(rs.getString("gstr9_date_of_filing"));
		deducteeGstinStatus.setGstr9ReturnFiled(rs.getString("gstr9_return_filed"));
		deducteeGstinStatus.setGstr9IsReturnValid(rs.getString("gstr9_is_return_valid"));
		deducteeGstinStatus.setDeductorPan(rs.getString("deductor_pan"));
		deducteeGstinStatus.setDeductorMasterTan(rs.getString("deductor_master_tan"));
		deducteeGstinStatus.setMonth(rs.getInt("month"));
		deducteeGstinStatus.setTaxPeriodByUser(rs.getString("tax_period_by_user"));
		
		return deducteeGstinStatus;
	}

}
