package com.ey.in.tds.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ey.in.tds.common.domain.dividend.ShareholderGstinStatus;

/**
 * 
 * @author vamsir
 *
 */
public class ShareholderGstinStatusRowMapper implements RowMapper<ShareholderGstinStatus> {

	@Override
	public ShareholderGstinStatus mapRow(ResultSet rs, int rowNum) throws SQLException {
		ShareholderGstinStatus shareholderGstinStatus = new ShareholderGstinStatus();

		shareholderGstinStatus.setId(rs.getInt("id"));
		shareholderGstinStatus.setActive(rs.getBoolean("active"));
		shareholderGstinStatus.setShareholderId(rs.getInt("shareholder_id"));
		shareholderGstinStatus.setCreatedBy(rs.getString("created_by"));
		shareholderGstinStatus.setCreatedDate(rs.getDate("created_date"));
		shareholderGstinStatus.setGstnReturnStatus(rs.getString("gstn_return_status"));
		shareholderGstinStatus.setGstnStatus(rs.getString("gstn_status"));
		shareholderGstinStatus.setModifiedBy(rs.getString("modified_by"));
		shareholderGstinStatus.setModifiedDate(rs.getDate("modified_date"));
		shareholderGstinStatus.setYear(rs.getInt("year"));
		shareholderGstinStatus.setLegalNameOfBusiness(rs.getString("legal_name_of_business"));
		shareholderGstinStatus.setDateOfCancellation(rs.getString("date_of_cancellation"));
		shareholderGstinStatus.setLastUpdatedDate(rs.getString("last_updated_date"));
		shareholderGstinStatus.setTradeName(rs.getString("trade_name"));
		shareholderGstinStatus.setGstr1DateOfFiling(rs.getString("gstr1_date_of_filing"));
		shareholderGstinStatus.setGstr1ReturnFiled(rs.getString("gstr1_return_filed"));
		shareholderGstinStatus.setGstr1IsReturnValid(rs.getString("gstr1_is_return_valid"));
		shareholderGstinStatus.setGstr3DateOfFiling(rs.getString("gstr3_date_of_filing"));
		shareholderGstinStatus.setGstr3ReturnFiled(rs.getString("gstr3_return_filed"));
		shareholderGstinStatus.setGstr3IsReturnValid(rs.getString("gstr3_is_return_valid"));
		shareholderGstinStatus.setGstr6DateOfFiling(rs.getString("gstr6_date_of_filing"));
		shareholderGstinStatus.setGstr6ReturnFiled(rs.getString("gstr6_return_filed"));
		shareholderGstinStatus.setGstr6IsReturnValid(rs.getString("gstr6_is_return_valid"));
		shareholderGstinStatus.setGstr9DateOfFiling(rs.getString("gstr9_date_of_filing"));
		shareholderGstinStatus.setGstr9ReturnFiled(rs.getString("gstr9_return_filed"));
		shareholderGstinStatus.setGstr9IsReturnValid(rs.getString("gstr9_is_return_valid"));
		shareholderGstinStatus.setDeductorPan(rs.getString("deductor_pan"));
		shareholderGstinStatus.setDeductorMasterTan(rs.getString("deductor_master_tan"));
		shareholderGstinStatus.setMonth(rs.getInt("month"));

		return shareholderGstinStatus;
	}

}
