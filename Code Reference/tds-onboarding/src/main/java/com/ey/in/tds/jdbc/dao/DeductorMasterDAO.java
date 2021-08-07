package com.ey.in.tds.jdbc.dao;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorMaster;
import com.ey.in.tds.jdbc.rowmapper.DeductorMasterRowMapper;

/**
 * 
 * @author scriptbees.
 *
 */
@Repository
public class DeductorMasterDAO implements Serializable {

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
		jdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("deductor_master").withSchemaName("Onboarding")
				.usingGeneratedKeyColumns("deductor_master_id");
	}


	public DeductorMaster insert(DeductorMaster deductorMaster) {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("deductor_master_pan", deductorMaster.getPanField());
		parameters.put("code", deductorMaster.getCode());
		parameters.put("name", deductorMaster.getName());
		parameters.put("phone_number", deductorMaster.getPhoneNumber());
		parameters.put("residential_status", deductorMaster.getResidentialStatus());
		parameters.put("status", deductorMaster.getStatus());
		parameters.put("type", deductorMaster.getType());
		parameters.put("applicable_from", deductorMaster.getApplicableFrom());
		parameters.put("applicable_to", deductorMaster.getApplicableTo());
		parameters.put("due_date_of_tax_payment", deductorMaster.getDueDateOfTaxPayment());
		parameters.put("email", deductorMaster.getEmail());
		parameters.put("have_more_than_one_branch", deductorMaster.getHaveMoreThanOneBranch());
		parameters.put("mode_of_payment", deductorMaster.getModeOfPayment());
		parameters.put("active", deductorMaster.getActive() != null ? deductorMaster.getActive() : 1);
		parameters.put("created_by", deductorMaster.getCreatedBy());
		parameters.put("created_date", new Date());
		parameters.put("modified_by", deductorMaster.getModifiedBy());
		parameters.put("modified_date", deductorMaster.getModifiedDate());
		parameters.put("alternate_email", deductorMaster.getEmailAlternate());
		parameters.put("alternate_phone_number", deductorMaster.getPhoneNumberAlternate());
		parameters.put("gstin", deductorMaster.getGstin());
		parameters.put("module_type", deductorMaster.getModuleType());
		parameters.put("deductor_type_name", deductorMaster.getDvndDeductorTypeName());
		parameters.put("salutation", deductorMaster.getDeductorSalutation());
		
		int deductorMasterId = jdbcInsert.executeAndReturnKey(parameters).intValue();
		deductorMaster.setDeductorMasterId(deductorMasterId);
		logger.info("Record inserted to deductor master table {}");
		return deductorMaster;
	}

	/**
	 * 
	 * @param deductorMaster
	 * @return
	 */
	public DeductorMaster update(DeductorMaster deductorMaster, String userName) {
		
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(deductorMaster);
		int status = namedParameterJdbcTemplate.update(String.format(queries.get("deductor_master_update")), namedParameters);

		if (status != 0) {
			logger.info("Deductor master data is updated for ID " + deductorMaster.getDeductorMasterId());
		} else {
			logger.info("No record found with ID " + deductorMaster.getDeductorMasterId());
		}
		logger.info("DAO method execution successful {}");
		return deductorMaster;
	}

	/**
	 * This method for get all recode from deductor master table.
	 * 
	 * @return
	 */
	public List<DeductorMaster> findAll() {
		return jdbcTemplate.query(String.format(queries.get("get_all_deductor")), new DeductorMasterRowMapper());
	}

	/**
	 * This method for get data based on list of pans.
	 * 
	 * @param pans
	 * @return
	 */
	public List<DeductorMaster> findAllDeductorsByPans(String pans) {
		return jdbcTemplate.query(String.format(queries.get("find_all_deductor_pan")), new DeductorMasterRowMapper(),
				pans);
	}

	/**
	 * This method for get data based on pan.
	 * 
	 * @param pan
	 * @return
	 */
	public List<DeductorMaster> findBasedOnDeductorPan(String pan) {
		return jdbcTemplate.query(String.format(queries.get("find_based_on_deductor_pan")),
				new DeductorMasterRowMapper(), pan);
	}
	/**
	 * This method for get code and name from deductor master table.
	 * @param deductorPan
	 * @return
	 */
	public List<DeductorMaster> findByDeductorPan(String deductorPan) {
		return jdbcTemplate.query(String.format(queries.get("find_by_deductor_pan")), new DeductorMasterRowMapper(),
				deductorPan);
	}
	/**
	 * to get the deductor based on deductor code
	 * @param deductorCode
	 * @return
	 */
	public List<DeductorMaster> findBasedOnDeductorCode(String deductorCode) {
		logger.info("DAO method executing to get deductor details based on deductor code {}");
		return jdbcTemplate.query(String.format(queries.get("find_based_on_deductor_code")),
				new DeductorMasterRowMapper(), deductorCode);
	}

	public String getDeductorName(String deductorPan) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("pan", deductorPan);
		return namedParameterJdbcTemplate.queryForObject(String.format(queries.get("getdeductor_name_by_pan")),
				parameters, String.class);
	}
	
	public Boolean getDeductorCountByDeductorPan(String deductorPan) {
		logger.info("DAO method executing to get deductor count based on pan {}");
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("pan", deductorPan);
		Integer count= namedParameterJdbcTemplate.queryForObject(String.format(queries.get("getdeductor_count_by_pan")),
				parameters, Integer.class);
		return count==0?false:true;
	}
}
