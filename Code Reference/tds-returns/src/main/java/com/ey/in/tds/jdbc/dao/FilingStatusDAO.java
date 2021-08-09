package com.ey.in.tds.jdbc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
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
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.transactions.jdbc.rowmapper.FilingStatusRowMapper;
import com.ey.in.tds.common.returns.jdbc.dto.FilingStatus;

@Repository
public class FilingStatusDAO {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private HashMap<String, String> queries;

	private JdbcTemplate jdbcTemplate;

	private SimpleJdbcInsert simpleJdbcInsert;

	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	private DataSource dataSource;
	
	@PostConstruct
	private void postConstruct() {
		jdbcTemplate = new JdbcTemplate(dataSource);
		namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("filing_status")
				.withSchemaName("Transactions").usingGeneratedKeyColumns("filing_status_id");
	}


	public Map<String, Object> mapParameters(FilingStatus dto) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("filing_status_id", dto.getFilingStatusId());
		parameters.put("assesment_year", dto.getAssesmentYear());
		parameters.put("quarter", dto.getQuarter());
		parameters.put("deductor_id", dto.getDeductorID());
		parameters.put("deductor_pan", dto.getDeductorPan());
		parameters.put("deductor_tan", dto.getDeductorTan());
		parameters.put("duedate_for_filing", dto.getDueDateForFiling());
		parameters.put("file_type", dto.getFileType());
		parameters.put("filing_type", dto.getFilingType());
		parameters.put("pnr_or_token_number", dto.getPnrOrTokenNumber());
		parameters.put("quarter_end_date", dto.getQuarterEndDate());
		parameters.put("quarter_period", dto.getQuarterPeriod());
		parameters.put("quarter_start_date", dto.getQuarterStartDate());
		parameters.put("request_number", dto.getRequestNumber());
		parameters.put("revision_exists", dto.getRevisionExists());
		parameters.put("status", dto.getStatus());
		parameters.put("active", dto.getActive() == true ? 1 : 0);
		parameters.put("created_by", dto.getCreatedBy());
		parameters.put("created_date", dto.getCreatedDate());
		parameters.put("updated_by", dto.getUpdatedBy());
		parameters.put("updated_date", dto.getUpdatedDate());

		return parameters;
	}

	/**
	 * inserts record in ldc_masater table
	 * @param dto
	 * @return
	 */
	public FilingStatus save(FilingStatus dto) {
		logger.info("insert method execution started  {}");

		//Map<String, Object> parameters = mapParameters(dto);
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(dto);
		dto.setFilingStatusId(simpleJdbcInsert.executeAndReturnKey(namedParameters).intValue());

		logger.info("Record inserted to filing_status table {}");
		return dto;
	}

	/**
	 * retrieves  filing status based on assessmentYear, String quarter, String tanNumber
	 * @param assessmentYear
	 * @param quarter
	 * @param tanNumber
	 * @return
	 */
	public List<FilingStatus> findByYearAndQuarterAndTan(Integer assessmentYear, String quarter, String tanNumber,String formType) {
		Map<String ,Object> parameters=new HashMap<String,Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("quarter", quarter);
		parameters.put("tanNumber", tanNumber);
		parameters.put("formType", formType);
		return namedParameterJdbcTemplate.query(String.format(queries.get("filingStatus_by_assesmentyear_quarter_tan")),
				parameters,new FilingStatusRowMapper());
	}

	

	public FilingStatus update(FilingStatus dto) {
		jdbcTemplate.update(new PreparedStatementCreator() {

			@Override
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				PreparedStatement ps = connection.prepareStatement(String.format(queries.get("update_filingStatus")),
						Statement.RETURN_GENERATED_KEYS);
				ps.setInt(1, dto.getAssesmentYear());
				ps.setString(2, dto.getQuarter());
				ps.setInt(3, dto.getDeductorID());
				ps.setString(4, dto.getDeductorPan());
				ps.setString(5, dto.getDeductorTan());
				ps.setDate(6, new java.sql.Date(dto.getDueDateForFiling().getTime()));
				ps.setString(7, dto.getFileType());
				ps.setString(8, dto.getFilingType());
				ps.setString(9, dto.getPnrOrTokenNumber());
				if (dto.getQuarterEndDate() != null) {
					ps.setDate(10, new java.sql.Date(dto.getQuarterEndDate().getTime()));
				}
				ps.setString(11, dto.getQuarterPeriod());
				if (dto.getQuarterStartDate() != null) {
					ps.setDate(12, new java.sql.Date(dto.getQuarterStartDate().getTime()));
				}
				ps.setInt(13, dto.getRequestNumber());
				ps.setInt(14, dto.getRevisionExists() == true ? 1 : 0);
				ps.setString(15, dto.getStatus());
				ps.setInt(16, dto.getActive() == true ? 1 : 0);
				ps.setString(17, dto.getUpdatedBy());
				ps.setDate(18, new java.sql.Date(dto.getUpdatedDate().getTime()));
				ps.setInt(19, dto.getFilingStatusId());
				return ps;
			}
		});
		logger.info("DAO method execution successful {}");
		return dto;
	}
	/**
	 * retrieves by id
	 * @param id
	 * @return
	 */
	public List<FilingStatus> findById(Integer id) {
		Map<String ,Object> parameters=new HashMap<String,Object>();
		parameters.put("id", id);
		return namedParameterJdbcTemplate.query(String.format(queries.get("filingStatus_by_id")),
				parameters,new FilingStatusRowMapper());
	}
	
	public List<FilingStatus> findByYearAndQuarterAndFileType(Integer assessmentYear, String quarter, String deductorTan,String formType) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String ,Object> parameters=new HashMap<String,Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("quarter", quarter.toUpperCase());
		parameters.put("deductorTan", deductorTan);
		parameters.put("formType", formType);
		return namedParameterJdbcTemplate.query(String.format(queries.get("filingStatus_By_Year_And_Quarter_And_FileType")),
				parameters,new FilingStatusRowMapper());
	}
	
	public List<FilingStatus> findByYearAndQuarterAndId(Integer assessmentYear, String quarter, Integer id) {
		Map<String ,Object> parameters=new HashMap<String,Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("quarter", quarter);
		parameters.put("id", id);
		return namedParameterJdbcTemplate.query(String.format(queries.get("filingStatus_by_assesmentyear_quarter_id")),
				parameters,new FilingStatusRowMapper());
	}
	
	
}
