package com.ey.in.tds.jdbc.dao;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorOnboardingInformationDTO;
import com.ey.in.tds.jdbc.rowmapper.DeductorOnboardingInfoRowMapper;

/**
 * 
 * @author scriptbees
 *
 */
@Repository
public class DeductorOnboardingInfoDAO implements Serializable {

	private static final long serialVersionUID = 1L;

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
	      jdbcInsert = new SimpleJdbcInsert(dataSource)
	    		  .withTableName("deductor_onboarding_info")
					.withSchemaName("Onboarding").usingGeneratedKeyColumns("deductor_onboarding_info_id");
	}
	/**
	 * 
	 * @param deductorOnboardingInformation
	 * @return
	 */
	public DeductorOnboardingInformationDTO save(DeductorOnboardingInformationDTO deductorOnboardingInformation) {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("deductor_master_id", deductorOnboardingInformation.getDeductorMasterId());
		parameters.put("pan", deductorOnboardingInformation.getPan());
		parameters.put("accountnumber", deductorOnboardingInformation.getAccountNumber());
		parameters.put("active", deductorOnboardingInformation.getActive());
		parameters.put("applicable_from", deductorOnboardingInformation.getApplicableFrom());
		parameters.put("applicable_to", deductorOnboardingInformation.getApplicableTo());
		parameters.put("cnp", deductorOnboardingInformation.getCnp());
		parameters.put("cp", deductorOnboardingInformation.getCp());
		parameters.put("crpt", deductorOnboardingInformation.getCrpt());
		parameters.put("custom_jobs", deductorOnboardingInformation.getCustomJobs());
		parameters.put("ipp", deductorOnboardingInformation.getIpp());
		parameters.put("ppa", deductorOnboardingInformation.getPpa());
		parameters.put("priority", deductorOnboardingInformation.getPriority());
		parameters.put("provisionprocessing", deductorOnboardingInformation.getProvisionProcessing());
		parameters.put("provisiontracking", deductorOnboardingInformation.getProvisionTracking());
		parameters.put("tif", deductorOnboardingInformation.getTif());
		parameters.put("created_date", deductorOnboardingInformation.getCreatedDate());
		parameters.put("modified_date", deductorOnboardingInformation.getModifiedDate());
		parameters.put("created_by", deductorOnboardingInformation.getCreatedBy());
		parameters.put("modified_by", deductorOnboardingInformation.getModifiedBy());
		parameters.put("rounding_off", deductorOnboardingInformation.getRoundoff());
		parameters.put("per_transaction_limit", deductorOnboardingInformation.getPertransactionlimit());
		parameters.put("sections_for_transaction_limit", deductorOnboardingInformation.getSelectedSectionsForTransactionLimit());
		parameters.put("intereset_calculation_type", deductorOnboardingInformation.getInterestCalculationType());
		parameters.put("dvnd_enabled", deductorOnboardingInformation.getDvndEnabled());
		parameters.put("client_specific_rules", deductorOnboardingInformation.getStringClientSpecificRules());
		parameters.put("prep_form_15_ca_cb", deductorOnboardingInformation.getDvndPrepForm15CaCb());
		parameters.put("ddt_paid_before_eoy", deductorOnboardingInformation.getDvndDdtPaidBeforeEOY());
		parameters.put("file_form_15_gh", deductorOnboardingInformation.getDvndFileForm15gh());
		parameters.put("advance_processing", deductorOnboardingInformation.getAdvanceProcessing());
		
		int deductorOnboardingInfoId = jdbcInsert.executeAndReturnKey(parameters).intValue();
		deductorOnboardingInformation.setDeductorMasterId(deductorOnboardingInfoId);
		logger.info("Record inserted into deductor onboarding info table {}");
		return deductorOnboardingInformation;
	}
	/**
	 * 
	 * @param deductorOnboardingInformation
	 * @return
	 */
	public Boolean update(DeductorOnboardingInformationDTO deductorOnboardingInformation) {
		return jdbcTemplate.execute(queries.get("deductor_onboarding_info_update"),
				new PreparedStatementCallback<Boolean>() {
					@Override
					public Boolean doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
						ps.setBoolean(1, deductorOnboardingInformation.getActive());
						ps.setString(2, deductorOnboardingInformation.getPan());
						return ps.execute();
					}
				});
	}
	/**
	 * 
	 * @param deductorPan
	 * @return
	 */
	public List<DeductorOnboardingInformationDTO> findByDeductorPan(String deductorPan) {
		logger.info("finding deductor onbording details by deductor pan {}");
		return jdbcTemplate.query(queries.get("find_by_deductor_pan_onboarding_info"), new DeductorOnboardingInfoRowMapper(),
				deductorPan);
	}
	/**
	 * 
	 * @param deductorOnboardingInformation
	 * @return
	 */
	public Boolean updateCustomJobs(DeductorOnboardingInformationDTO deductorOnboardingInformation) {
		return jdbcTemplate.execute(queries.get("update_custom_jobs"), new PreparedStatementCallback<Boolean>() {
			@Override
			public Boolean doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
				ps.setString(1, deductorOnboardingInformation.getCustomJobs());
				ps.setString(2, deductorOnboardingInformation.getPan());
				ps.setInt(3, deductorOnboardingInformation.getDeductorOnboardingInfoId());
				return ps.execute();
			}
		});
	}
}
