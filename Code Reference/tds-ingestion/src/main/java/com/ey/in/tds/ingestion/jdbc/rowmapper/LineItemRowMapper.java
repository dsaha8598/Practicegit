package com.ey.in.tds.ingestion.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.ey.in.tds.common.dto.DeducteeDetailDTO;

public class LineItemRowMapper implements RowMapper<DeducteeDetailDTO> {

	@Override
	public DeducteeDetailDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		DeducteeDetailDTO deducteeDetailDTO = new DeducteeDetailDTO();
		deducteeDetailDTO.setAddress(rs.getString("address"));
		deducteeDetailDTO.setAmount(rs.getBigDecimal("amount"));
		deducteeDetailDTO.setContactNo(rs.getString("contactNo"));
		deducteeDetailDTO.setCountry(rs.getString("country"));
		deducteeDetailDTO.setDeducteeName(rs.getString("deducteeName"));
		deducteeDetailDTO.setDocumentPostingDate(rs.getDate("documentPostingDate"));
		deducteeDetailDTO.setEmail(rs.getString("email"));
		deducteeDetailDTO.setFinalTDSAmount(rs.getBigDecimal("finalTDSAmount"));
		deducteeDetailDTO.setFinalTDSRate(rs.getBigDecimal("finalTDSRate"));
		deducteeDetailDTO.setFinalTDSSection(rs.getString("finalTDSSection"));
		deducteeDetailDTO.setLdcCertificateNo(rs.getString("ldcCertificateNo"));
		deducteeDetailDTO.setNatureOfRemittance(rs.getString("natureOfRemittance"));
		deducteeDetailDTO.setPan(rs.getString("pan"));
		deducteeDetailDTO.setTin(rs.getString("tin"));
		return deducteeDetailDTO;
	}

}
