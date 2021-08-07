package com.ey.in.tds.jdbc.dao;

import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.dto.onboarding.deductee.DeducteeDeclaration;
import com.ey.in.tds.common.onboarding.jdbc.dto.KYCHistory;

/**
 * 
 * @author vamsir
 *
 */
@Repository
public class KYCHistoryDAO {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private HashMap<String, String> queries;

	@Autowired
	private DataSource dataSource;
	private JdbcTemplate jdbcTemplate;
	private SimpleJdbcInsert jdbcInsert;
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@PostConstruct
	private void postConstruct() {
		jdbcTemplate = new JdbcTemplate(dataSource);
		namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		jdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("kyc_history").withSchemaName("Onboarding")
				.usingGeneratedKeyColumns("id");
	}

	/**
	 * This method for save kyc history
	 *
	 * @param kycDetails
	 */
	public KYCHistory save(KYCHistory kycHistory) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(kycHistory);
		kycHistory.setId(jdbcInsert.executeAndReturnKey(namedParameters).intValue());
		logger.info("Record inserted to kyc history table {}");
		return kycHistory;

	}

	/**
	 * 
	 * @param kycHistoryList
	 * @param tenantId
	 */
	@Transactional
	public void batchSaveKycHistory(List<KYCHistory> kycHistoryList, String tenantId) {
		String query = " INSERT INTO Onboarding.kyc_history (kyc_details_id, email, [year], deductor_pan, deductor_master_tan, active, created_date, modified_date, created_by, modified_by) "
				+ "VALUES( :kycDetailsId, :email, :year, :deductorPan, :deductorMasterTan, :active, :createdDate, :modifiedDate, :createdBy, :modifiedBy)";
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(kycHistoryList);
		namedParameterJdbcTemplate.batchUpdate(query, batch);
		logger.info("kyc history inserted size is {}", kycHistoryList.size());
	}

}
