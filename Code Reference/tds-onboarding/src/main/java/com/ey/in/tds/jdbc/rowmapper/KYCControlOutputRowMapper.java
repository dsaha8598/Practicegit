package com.ey.in.tds.jdbc.rowmapper;

import com.ey.in.tds.common.onboarding.jdbc.dto.KYCDetailsControlOutput;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 
 * @author prince gupta
 *
 */
public class KYCControlOutputRowMapper implements RowMapper<KYCDetailsControlOutput> {

	@Override
	public KYCDetailsControlOutput mapRow(ResultSet rs, int rowNum) throws SQLException {
		KYCDetailsControlOutput kyc = new KYCDetailsControlOutput();
		KYCDetailsControlOutput.Input input = kyc.new Input();
		input.setCountProvided(rs.getInt("countProvided") + "");
		input.setCountWithoutEmail(rs.getInt("countWithoutEmail") + "");
		input.setCountWithEmail(rs.getInt("countWithEmail") + "");
		input.setCountKYCResponded(rs.getInt("countKYCResponded") + "");
		input.setCountKYCRespondedNotAuthorised(rs.getInt("countKYCRespondedNotAuthorised") + "");
		input.setCountKYCNotResponded(rs.getInt("countKYCNotResponded") + "");
		input.setCustomersMailNotTriggered(rs.getInt("customersMailNotTriggered") + "");
		input.setCustomersMailTriggered(rs.getInt("customersMailTriggered") + "");
		kyc.setInputStats(input);

		KYCDetailsControlOutput.Output output = kyc.new Output();
		output.setCountCustomerRespondedTDS(rs.getInt("countCustomerRespondedTDS") + "");
		output.setCountCustomerRespondedTCS(rs.getInt("countCustomerRespondedTCS") + "");
		output.setCountNormalRate(rs.getInt("countNormalRate") + "");
		output.setCountNoPANHigherRate(rs.getInt("countNoPANHigherRate") + "");
		output.setCountNoITRHigherRate(rs.getInt("countNoITRHigherRate") + "");
		output.setCountNoPANITRHigherRate(rs.getInt("countNoPANITRHigherRate") + "");
		output.setRemediationDoneTCSApplicable(rs.getInt("remediationDoneTCSApplicable") + "");
		output.setRemediationDoneTDSApplicable(rs.getInt("remediationDoneTDSApplicable") + "");
		output.setRemediationPendingHold(rs.getInt("remediationPendingHold") + "");
		output.setRemediationPendingNew(rs.getInt("remediationPendingNew") + "");
		output.setFinalRemediationPending(rs.getInt("finalRemediationPending") + "");
		output.setLevel1totalCount(rs.getInt("level1totalCount") + "");
		output.setLevel2totalCount(rs.getInt("level2totalCount") + "");

		kyc.setOutputStats(output);

		return kyc;
	}

}
