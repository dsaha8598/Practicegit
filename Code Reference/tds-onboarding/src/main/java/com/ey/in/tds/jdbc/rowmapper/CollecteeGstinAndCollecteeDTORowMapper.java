package com.ey.in.tds.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ey.in.tcs.common.domain.CollecteeGstinAndCollecteeDTO;

/**
 * 
 * @author vamsir
 *
 */
public class CollecteeGstinAndCollecteeDTORowMapper implements RowMapper<CollecteeGstinAndCollecteeDTO> {

	@Override
	public CollecteeGstinAndCollecteeDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		CollecteeGstinAndCollecteeDTO collecteeGstinStatus = new CollecteeGstinAndCollecteeDTO();

		collecteeGstinStatus.setId(rs.getInt("id"));
		collecteeGstinStatus.setActive(rs.getBoolean("active"));
		collecteeGstinStatus.setCollecteeId(rs.getInt("collectee_id"));
		collecteeGstinStatus.setCreatedBy(rs.getString("created_by"));
		collecteeGstinStatus.setCreatedDate(rs.getDate("created_date"));
		collecteeGstinStatus.setGstnReturnStatus(rs.getString("gstn_return_status"));
		collecteeGstinStatus.setGstnStatus(rs.getString("gstn_status"));
		collecteeGstinStatus.setModifiedBy(rs.getString("modified_by"));
		collecteeGstinStatus.setModifiedDate(rs.getDate("modified_date"));
		collecteeGstinStatus.setYear(rs.getInt("year"));
		collecteeGstinStatus.setLegalNameOfBusiness(rs.getString("legal_name_of_business"));
		collecteeGstinStatus.setDateOfCancellation(rs.getString("date_of_cancellation"));
		collecteeGstinStatus.setLastUpdatedDate(rs.getString("last_updated_date"));
		collecteeGstinStatus.setTradeName(rs.getString("trade_name"));
		collecteeGstinStatus.setGstr1DateOfFiling(rs.getString("gstr1_date_of_filing"));
		collecteeGstinStatus.setGstr1ReturnFiled(rs.getString("gstr1_return_filed"));
		collecteeGstinStatus.setGstr1IsReturnValid(rs.getString("gstr1_is_return_valid"));
		collecteeGstinStatus.setGstr3DateOfFiling(rs.getString("gstr3_date_of_filing"));
		collecteeGstinStatus.setGstr3ReturnFiled(rs.getString("gstr3_return_filed"));
		collecteeGstinStatus.setGstr3IsReturnValid(rs.getString("gstr3_is_return_valid"));
		collecteeGstinStatus.setGstr6DateOfFiling(rs.getString("gstr6_date_of_filing"));
		collecteeGstinStatus.setGstr6ReturnFiled(rs.getString("gstr6_return_filed"));
		collecteeGstinStatus.setGstr6IsReturnValid(rs.getString("gstr6_is_return_valid"));
		collecteeGstinStatus.setGstr9DateOfFiling(rs.getString("gstr9_date_of_filing"));
		collecteeGstinStatus.setGstr9ReturnFiled(rs.getString("gstr9_return_filed"));
		collecteeGstinStatus.setGstr9IsReturnValid(rs.getString("gstr9_is_return_valid"));
		collecteeGstinStatus.setDeductorPan(rs.getString("deductor_pan"));
		collecteeGstinStatus.setDeductorMasterTan(rs.getString("deductor_master_tan"));
		collecteeGstinStatus.setMonth(rs.getInt("month"));
		collecteeGstinStatus.setTaxPeriodByUser(rs.getString("tax_period_by_user"));
		//collectee master 
		collecteeGstinStatus.setNameOfTheCollector(rs.getString("name_of_the_collector"));
		collecteeGstinStatus.setCollectorPan(rs.getString("collector_pan"));
		collecteeGstinStatus.setCollecteeCode(rs.getString("collectee_code"));
		collecteeGstinStatus.setNameOfTheCollectee(rs.getString("name_of_the_collectee"));
		collecteeGstinStatus.setCollecteePan(rs.getString("collectee_pan"));
		collecteeGstinStatus.setGstinNumber(rs.getString("gstin_number"));
		
		return collecteeGstinStatus;
	}

}
