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

import com.ey.in.tds.common.onboarding.jdbc.dto.CollectorOnBoardingInformationDTO;
import com.ey.in.tds.jdbc.rowmapper.CollectorOnBoardingInfoRowMapper;

@Repository
public class CollectorOnBoardingInfoDAO implements Serializable {

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
		jdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("collector_onboarding_info")
				.withSchemaName("Onboarding").usingGeneratedKeyColumns("id");
	}

	/**
	 * 
	 * @param collectiorOnboardingInformation
	 * @return
	 */
	public CollectorOnBoardingInformationDTO save(CollectorOnBoardingInformationDTO collectorOnboardingInformation) {

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("collector_master_id", collectorOnboardingInformation.getCollectorMasterId());
		parameters.put("pan", collectorOnboardingInformation.getPan());
		parameters.put("accountnumber", collectorOnboardingInformation.getAccountNumber());
		parameters.put("active", collectorOnboardingInformation.getActive());
		parameters.put("applicable_from", collectorOnboardingInformation.getApplicableFrom());
		parameters.put("applicable_to", collectorOnboardingInformation.getApplicableTo());
		parameters.put("section_determination_id", collectorOnboardingInformation.getSectionDetermination());
		parameters.put("credit_notes_id", collectorOnboardingInformation.getCreditNotes());
		parameters.put("challan_generation_id", collectorOnboardingInformation.getChallanGeneration());
		parameters.put("scope_process", collectorOnboardingInformation.getScopeProcess());
		parameters.put("invoice_process_scope", collectorOnboardingInformation.getInvoiceProcessScope());
		parameters.put("priority", collectorOnboardingInformation.getPriority());
		parameters.put("lcc_tracking_notification", collectorOnboardingInformation.getLccTrackingNotification());
		parameters.put("tcs_applicability_id", collectorOnboardingInformation.getTcsApplicability());
		parameters.put("tds_transaction_id", collectorOnboardingInformation.getTdsTransaction());
		parameters.put("gst_implication_id", collectorOnboardingInformation.getGstImplication());
		parameters.put("buyer_threshold_computation_id", collectorOnboardingInformation.getBuyerThresholdComputation());
		parameters.put("collection_reference_id", collectorOnboardingInformation.getCollectionReferenceId());
		parameters.put("document_or_posting_date", collectorOnboardingInformation.getDocumentOrPostingDate());
		int collectorOnboardingInfoId = jdbcInsert.executeAndReturnKey(parameters).intValue();
		collectorOnboardingInformation.setCollectorMasterId(collectorOnboardingInfoId);
		logger.info("Record inserted into collector onboarding info table {}");
		return collectorOnboardingInformation;
	}

	/**
	 * 
	 * @param collectorOnboardingInformation
	 * @return
	 */
	public Boolean update(CollectorOnBoardingInformationDTO collectorOnboardingInformation) {
		return jdbcTemplate.execute(queries.get("collector_onboarding_info_update"),
				new PreparedStatementCallback<Boolean>() {
					@Override
					public Boolean doInPreparedStatement(PreparedStatement ps)
							throws SQLException, DataAccessException {
						ps.setBoolean(1, collectorOnboardingInformation.getActive());
						ps.setString(2, collectorOnboardingInformation.getPan());
						return ps.execute();
					}
				});
	}

	/**
	 * 
	 * @param collectorPan
	 * @return
	 */
	public List<CollectorOnBoardingInformationDTO> findByCollectorPan(String collectorPan) {
		return jdbcTemplate.query(queries.get("find_by_collector_pan_onboarding_info"),
				new CollectorOnBoardingInfoRowMapper(), collectorPan);
	}
	
	/**
	 * 
	 * @param deductorPan
	 * @return
	 */
	public Long getDeductorId(String deductorPan) {
		return jdbcTemplate.queryForObject(String.format(queries.get("get_collector_master_id")),
				new Object[] { deductorPan }, Long.class);
	}
	
	/**
	 * This method for get collection_reference_id based on collector pan
	 * 
	 * @param deductorPan
	 * @return
	 */
	public String getCollectionReferenceId(String collectorPan) {
		return jdbcTemplate.queryForObject(String.format(queries.get("get_collection_reference_id")),
				new Object[] { collectorPan }, String.class);
	}

	/**
	 * This method for get buyer threshold computation id based on collector pan
	 * 
	 * @param collectorPan
	 * @return
	 */
	public String getBuyerThresholdComputationId(String collectorPan) {
		return jdbcTemplate.queryForObject(String.format(queries.get("get_buyer_threshold_computation_id")),
				new Object[] { collectorPan }, String.class);
	}
}
