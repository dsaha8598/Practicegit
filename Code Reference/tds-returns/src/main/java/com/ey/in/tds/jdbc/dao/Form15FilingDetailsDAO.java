package com.ey.in.tds.jdbc.dao;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
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

import com.ey.in.tcs.common.domain.dividend.Form15FileFormat;
import com.ey.in.tcs.common.domain.dividend.Form15FileType;
import com.ey.in.tcs.common.domain.dividend.Form15FilingStatus;
import com.ey.in.tcs.common.domain.dividend.InvoiceShareholderNonResident;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.dividend.InvoiceShareholderResident;
import com.ey.in.tds.common.domain.transactions.jdbc.rowmapper.Form15FilingDetailsRowMapper;
import com.ey.in.tds.common.domain.transactions.jdbc.rowmapper.InvoiceShareholderNonResidentRowMapper;
import com.ey.in.tds.common.domain.transactions.jdbc.rowmapper.InvoiceShareholderResidentRowMapper;
import com.ey.in.tds.common.dto.onboarding.shareholder.ShareholderMasterNonResidential;
import com.ey.in.tds.common.returns.jdbc.dto.Form15FilingDetails;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.ReturnType;

@Repository
public class Form15FilingDetailsDAO {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private HashMap<String, String> queries;

	@Autowired
	private DataSource dataSource;

	private JdbcTemplate jdbcTemplate;

	private SimpleJdbcInsert simplejdbcInsert;

	private NamedParameterJdbcTemplate namedJdbcTemplate;

	@PostConstruct
	private void postConstruct() {
		namedJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		jdbcTemplate = new JdbcTemplate(dataSource);
		simplejdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("form_15_filing_details")
				.withSchemaName("Transactions").usingGeneratedKeyColumns("filing_files_id");
	}

	public List<Form15FilingDetails> findFilingFiles(String deductorTan, Integer assessmentYear, String dateOfPosting,
			Form15FileType fileType, Form15FileFormat fileFormat) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		Map<String, Object> parameter = new HashMap<>();
		parameter.put("deductorTan", deductorTan);
		parameter.put("assessmentYear", assessmentYear);
		parameter.put("dateOfPosting", dateOfPosting);
		parameter.put("fileType", fileType.name());
		parameter.put("fileFormat", fileFormat.name());

		String query = String.format(
				queries.get("get_form_15_filing_details_by_assessment_year_and_deductor_tan_fileType_fileFormat"));
		if (StringUtils.isNotEmpty(dateOfPosting)) {
			query = query + "and date_of_posting=:dateOfPosting ";
		}
		return namedJdbcTemplate.query(query, parameter, new Form15FilingDetailsRowMapper());

	}

	public Form15FilingDetails save(Form15FilingDetails dto) {
		logger.info("insert method execution started  to save data in share_holder_master_non_residential table{}");
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("assessment_year", dto.getAssessmentYear());
		parameters.put("date_of_posting", dto.getDateOfPosting());
		parameters.put("deductor_tan", dto.getDeductorTan());
		parameters.put("error", dto.getError());
		parameters.put("error_file_url", dto.getErrorFileUrl());
		parameters.put("file_format", dto.getFormFifteenFileFormat());
		parameters.put("file_status", dto.getStatus());
		parameters.put("file_type", dto.getFormFifteenFileType());
		parameters.put("file_url", dto.getFileUrl());
		parameters.put("filing_type", dto.getFilingType());
		parameters.put("generated_date", dto.getGeneratedDate());
		parameters.put("is_requested", dto.getIsRequested());
		parameters.put("request_number", dto.getRequestNumber());
		parameters.put("active", dto.getActive() == true ? 1 : 0);
		parameters.put("created_by", dto.getCreatedBy());
		parameters.put("created_date", dto.getCreatedDate());
		parameters.put("updated_by", dto.getUpdatedBy());
		parameters.put("updated_date", dto.getUpdatedDate());
		dto.setId(simplejdbcInsert.executeAndReturnKey(parameters).intValue());

		logger.info("Record inserted to share_holder_master_non_residential table {}");
		return dto;
	}

	// TODO write the Update query
	public Form15FilingDetails update(Form15FilingDetails dto) {
		logger.info("DAO method executing to update Form15FilingDetails data ");

		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(dto);
		int status = namedJdbcTemplate.update(String.format(queries.get("update_form_15_filing_details")),
				namedParameters);

		if (status != 0) {
			logger.info("Form15FilingDetails data is updated for ID " + dto.getId());
		} else {
			logger.info("No record found with ID " + dto.getId());
		}
		logger.info("DAO method execution successful {}");
		return dto;
	}

	public Form15FilingDetails createFilingReportStatus(String deductorTan, Integer assessmentYear,
			String dateOfPosting, Form15FileType fileType, Form15FileFormat fileFormat, ReturnType returnType,
			String userName) {

		List<Form15FilingDetails> existingFilings = findFilingFiles(deductorTan, assessmentYear, dateOfPosting,
				fileType, fileFormat);
		Form15FilingDetails filingReportStatus = existingFilings.isEmpty() ? null : existingFilings.get(0);

		Date today = new Date();
		if (filingReportStatus == null) {
			filingReportStatus = new Form15FilingDetails();
			filingReportStatus.setGeneratedDate(null);
			filingReportStatus.setUpdatedBy(null);
			filingReportStatus.setUpdatedDate(null);
			filingReportStatus.setFilingFilesDeductorTan(deductorTan);
			filingReportStatus.setActive(true);
			filingReportStatus.setFilingType(returnType);
			filingReportStatus.setFormFifteenFileType(fileType);
			filingReportStatus.setFormFifteenFileFormat(fileFormat);
			filingReportStatus.setStatus(Form15FilingStatus.PROCESSING);
			filingReportStatus.setIsRequested(true);
			filingReportStatus.setCreatedBy(userName);
			filingReportStatus.setCreatedDate(today);
			filingReportStatus.setAssessmentYear(assessmentYear);
			filingReportStatus.setDateOfPosting(dateOfPosting);
			return save(filingReportStatus);
		} else {
			filingReportStatus.setFilingFilesDeductorTan(deductorTan);
			filingReportStatus.setActive(true);
			filingReportStatus.setFilingType(returnType);
			filingReportStatus.setFormFifteenFileType(fileType);
			filingReportStatus.setFormFifteenFileFormat(fileFormat);
			filingReportStatus.setStatus(Form15FilingStatus.PROCESSING);
			filingReportStatus.setIsRequested(true);
			filingReportStatus.setCreatedBy(userName);
			filingReportStatus.setCreatedDate(today);
			filingReportStatus.setStringFilingType(returnType.name());
			filingReportStatus.setStringFileStatus(Form15FilingStatus.PROCESSING.name());
			filingReportStatus.setStringFileFormat(fileFormat.name());
			filingReportStatus.setStringFilingType(returnType.name());
			return update(filingReportStatus);
		}
	}

	public Form15FilingDetails updateFilingReportStatus(String deductorTan, Integer assessmentYear,
			String dateOfPosting, Form15FileType fileType, Form15FileFormat fileFormat, ReturnType returnType,
			Form15FilingStatus filingStatus, String fileBlobUrl, String errorDescription, String userName) {

		List<Form15FilingDetails> existingFilings = findFilingFiles(deductorTan, assessmentYear, dateOfPosting,
				fileType, fileFormat);
		Form15FilingDetails filingReportStatus = existingFilings.isEmpty() ? null : existingFilings.get(0);

		if (filingReportStatus == null) {
			throw new IllegalStateException("FilingFiles15CACB record not found for deductorTan: " + deductorTan
					+ ", assessmentYear: " + assessmentYear + ", dateOfPosting: " + dateOfPosting + ", fileType: "
					+ fileType + ", fileFormat: " + fileFormat);
		}
		try {
			filingReportStatus.setStringFileStatus(filingStatus.name());
			filingReportStatus.setFileUrl(fileBlobUrl);
			filingReportStatus.setError(errorDescription);
			filingReportStatus.setUpdatedBy(userName);
			filingReportStatus.setUpdatedDate(new Timestamp(System.currentTimeMillis()));
			if (filingStatus.isGenerated()) {
				filingReportStatus.setGeneratedDate(new Timestamp(System.currentTimeMillis()));
			}
			filingReportStatus = update(filingReportStatus);
		} catch (Exception e) {
			logger.info("Exception occured while updating user table " + e + "{}");
		}
		return filingReportStatus;
	}

	public Form15FilingDetails updateErrorFileURL(String deductorTan, Integer assessmentYear, String dateOfPosting,
			Form15FileType fileType, Form15FileFormat fileFormat, String errorFileUrl) {

		List<Form15FilingDetails> existingFilings = findFilingFiles(deductorTan, assessmentYear, dateOfPosting,
				fileType, fileFormat);
		Form15FilingDetails filingReportStatus = existingFilings.isEmpty() ? null : existingFilings.get(0);

		if (filingReportStatus == null) {
			throw new IllegalStateException("FilingFiles15 record not found for deductorTan: " + deductorTan
					+ ", assessmentYear: " + assessmentYear + ", dateOfPosting: " + dateOfPosting + ", fileType: "
					+ fileType + ", fileFormat: " + fileFormat);
		}

		filingReportStatus.setErrorFileUrl(errorFileUrl);
		filingReportStatus.setStringFileStatus(Form15FilingStatus.ERROR.name());

		return this.update(filingReportStatus);
	}

	@Transactional
	public Integer batchUpdate(final List<ShareholderMasterNonResidential> shareholder) {
		logger.info("Updating list of invoice share holders {}");
		String updateQuery = "UPDATE Client_Masters.shareholder_master_non_residential set name =:shareholderName,flat_door_block_no =:flatDoorBlockNo,\n"
				+ "road_street_postoffice =:roadStreetPostoffice,area_locality =:areaLocality,town_city_district =:townCityDistrict,country =:country,pin_code =:pinCode \n"
				+ ",state=:state,name_building_village=:nameBuildingVillage where shareholder_master_id =:id";
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(shareholder);

		int[] updateCounts = namedJdbcTemplate.batchUpdate(updateQuery, batch);
		logger.info("non resident share holder batch updated successfully {}", updateCounts.length);
		return updateCounts.length;
	}

	public List<InvoiceShareholderNonResident> findAllNonResidentByDateOfPosting(String deductorPan,
			LocalDate dateOfPosting, Integer year) {
		logger.info("DAO method executing to get InvoiceShareholderNonResident with deductorPan,dateOfPosting,year as "
				+ deductorPan, dateOfPosting, year + "{}");
		Map<String, Object> parameter = new HashMap<>();
		parameter.put("dateOfPosting", dateOfPosting);
		parameter.put("deductorPan", deductorPan);
		parameter.put("year", year);
		return namedJdbcTemplate.query(
				String.format(queries.get("get_invoice_shareholder_non_resident_by_pan_dateOfPosting_year")), parameter,
				new InvoiceShareholderNonResidentRowMapper());
	}

	public List<InvoiceShareholderResident> findAllResidentByQuarter(String deductorTan, int assessmentYear, int month1,
			int month2, int month3) {
		logger.info("DAO method executing to get InvoiceShareholderResident data ");
		Map<String, Object> parameter = new HashMap<>();
		parameter.put("deductorTan", deductorTan);
		parameter.put("month1", month1);
		parameter.put("month2", month2);
		parameter.put("month3", month3);
		parameter.put("assessmentYear", assessmentYear);
		return namedJdbcTemplate.query(
				String.format(queries.get("get_invoice_shareholder_resident_by_tan_year_document_posting_month")),
				parameter, new InvoiceShareholderResidentRowMapper());

	}

	/**
	 * to get non resident invoice share holder by id and pan
	 * 
	 * @param id
	 * @param deductorPan
	 * @return
	 */
	public List<InvoiceShareholderNonResident> findByIdPan(String ids, String deductorPan) {
		logger.info("DAO method executing to get non resident invoice share holder record using id {}");
		String query = String.format(queries.get("get_non_resrident__invoice_shareholder_by_id_pan"));
		query = query + ids;
		Map<String, Object> parameter = new HashMap<>();
		parameter.put("deductorPan", deductorPan);
		return namedJdbcTemplate.query(query, parameter, new InvoiceShareholderNonResidentRowMapper());

	}
	
	@Transactional
	public Integer batchUpdateInvoiceShareHolder(final List<InvoiceShareholderNonResident> shareholder) {
		logger.info("Updating list of invoice share holders {}");
		String updateQuery = "update Transactions.invoice_shareholder_non_resident set remittance_made_country =:remittanceMadeCountry,principal_place_of_business =:principalPlaceOfBusiness\n"
				+ ",form15cb_acknowledgement_no =:form15CBAcknowledgementNo,form15cb_generation_date =:form15CBGenartionDate,date_of_posting =:dateOfPosting,\n"
				+ "actual_remmitance_amount_after_tds_foreign_currency =:actualRemmitanceAmountForeignCurrency,dividend_amount_foreign_currency =:dividendAmountForeignCurrency,\n"
				+ "proposed_date_of_remmitence =:proposedDateOfRemmitence,dividend_amount =:dividendAmountRs\n"
				+ "where invoice_id =:id";
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(shareholder);

		int[] updateCounts = namedJdbcTemplate.batchUpdate(updateQuery, batch);
		logger.info("non resident Invoice shareholder batch updated successfully {}", updateCounts.length);
		return updateCounts.length;
	}
	
	
}
