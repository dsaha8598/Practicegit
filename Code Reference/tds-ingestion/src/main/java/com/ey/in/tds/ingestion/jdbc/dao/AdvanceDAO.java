
package com.ey.in.tds.ingestion.jdbc.dao;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.AdvanceDTO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.AdvanceStagging;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.MismatchesCountDTO;
import com.ey.in.tds.common.domain.transactions.jdbc.rowmapper.AdvanceRowMapper;
import com.ey.in.tds.common.domain.transactions.jdbc.rowmapper.DeducteeDetailsRowMapper;
import com.ey.in.tds.common.domain.transactions.jdbc.rowmapper.LdcMasterRowMapper;
import com.ey.in.tds.common.domain.transactions.jdbc.rowmapper.LdcMasterUtilizationRowMapper;
import com.ey.in.tds.common.domain.transactions.jdbc.rowmapper.MatrixReportsRowMapper;
import com.ey.in.tds.common.domain.transactions.jdbc.rowmapper.MatrixRowMapper;
import com.ey.in.tds.common.domain.transactions.jdbc.rowmapper.VendorSectionRowMapper;
import com.ey.in.tds.common.dto.invoice.MismatchesFiltersDTO;
import com.ey.in.tds.common.model.advance.AdvanceMismatchByBatchIdDTO;
import com.ey.in.tds.common.model.provision.MatrixDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeducteeDetailsDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.LdcMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.LdcMasterUtilizationDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.VendorSectionDTO;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.ingestion.jdbc.rowmapper.AdvanceStaggingRowMapper;
import com.ey.in.tds.ingestion.jdbc.rowmapper.MismatchCountRowMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * class contains the logic for data base operations with ao_master table
 * 
 * @author Amani
 *
 */
@Repository
public class AdvanceDAO {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired(required = false)
	private HashMap<String, String> queries;

	private SimpleJdbcInsert simpleJdbcInsert;

	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	private DataSource dataSource;

	@PostConstruct
	private void postConstruct() {
		namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("advance").withSchemaName("Transactions")
				.usingGeneratedKeyColumns("advance_id");

	}

	public AdvanceDTO save(AdvanceDTO dto) {
		logger.info("insert advance method execution started  {}");
		dto.setAssessmentYear(dto.getAssessmentYear());
		dto.setDeductorMasterTan(dto.getDeductorMasterTan());
		dto.setPostingDateOfDocument(dto.getPostingDateOfDocument());
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(dto);
		dto.setId(simpleJdbcInsert.executeAndReturnKey(namedParameters).intValue());
		logger.info("Record inserted to advance table {}");
		return dto;
	}

	public AdvanceDTO update(AdvanceDTO dto) {
		logger.info("DAO method executing to save user data ");

		dto.setAssessmentYear(dto.getAssessmentYear());
		dto.setDeductorMasterTan(dto.getDeductorMasterTan());
		dto.setPostingDateOfDocument(dto.getPostingDateOfDocument());
		dto.setId(dto.getId());
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(dto);
		int status = namedParameterJdbcTemplate.update(String.format(queries.get("advance_update")), namedParameters);

		if (status != 0) {
			logger.info("Advance data is updated for ID " + dto.getId());
		} else {
			logger.info("No record found with ID " + dto.getId());
		}
		logger.info("DAO method execution successful {}");
		return dto;
	}

	public List<AdvanceDTO> getAdavanceByYearMonthTan(int assessmentYear, int challanMonth, String tan,
			boolean approvedForChallan) {

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("challanMonth", challanMonth);
		parameters.put("tan", tan);
		parameters.put("approvedForChallan", approvedForChallan);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_adavance_by_year_month_tan")),
				parameters, new AdvanceRowMapper());

	}

	public Long getAdavanceByYearMonthTanCount(int assessmentYear, int challanMonth, String tan,
			boolean approvedForChallan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("challanMonth", challanMonth);
		parameters.put("tan", tan);
		parameters.put("approvedForChallan", approvedForChallan);

		return namedParameterJdbcTemplate.queryForObject(
				String.format(queries.get("get_adavance_by_year_month_tan_count")), parameters, Long.class);
	}

	public List<AdvanceDTO> getAdvacnceLineItemsByReceiptSerialNoAndBSRCodeAndReceiptDate(Integer assessmentYear,
			String deductorTan, boolean challanPaid, boolean isForNonResidents, String bsrCode, String receiptSerailNo,
			String receiptDate) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String resident = isForNonResidents ? "Y" : "N";

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("deductorTan", deductorTan);
		parameters.put("challanPaid", challanPaid);
		parameters.put("resident", resident);
		parameters.put("bsrCode", bsrCode);
		parameters.put("receiptSerailNo", receiptSerailNo);
		parameters.put("receiptDate", receiptDate);

		return namedParameterJdbcTemplate.query(String.format(queries.get("get_adavance_by_serialNo_bsrCode_date")),
				parameters, new AdvanceRowMapper());
	}

	public BigInteger getTotalCountByAssessmentYearAndBSRCode(Integer assessmentYear, String deductorTan,
			boolean challanPaid, boolean isForNonResidents, String bsrCode, String receiptSerailNo,
			String receiptDate) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String resident = isForNonResidents ? "Y" : "N";

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("deductorTan", deductorTan);
		parameters.put("challanPaid", challanPaid);
		parameters.put("resident", resident);
		parameters.put("bsrCode", bsrCode);
		parameters.put("receiptSerailNo", receiptSerailNo);
		parameters.put("receiptDate", receiptDate);

		return namedParameterJdbcTemplate.queryForObject(
				String.format(queries.get("get_adavance_by_serialNo_bsrCode_date_count")), parameters,
				BigInteger.class);
	}

	public Double getTotalValueBySystemChallanSerialNo(int assessmentYear, int assessmentMonth, String tan,
			String systemChallanSerailNo) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("assessmentMonth", assessmentMonth);
		parameters.put("tan", tan);
		parameters.put("systemChallanSerailNo", systemChallanSerailNo);

		return namedParameterJdbcTemplate.queryForObject(String.format(queries.get("get_adavance_total_by_serialNo")),
				parameters, Double.class);
	}

	public Double getTotalTdsAmount(int assessmentYear, int challanMonth, String tan, boolean approvedForChallan,
			String section, boolean isChallanGenerated, boolean isVerifyLiability, boolean isChallanPaid) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String queryStr = "";
		if (!isVerifyLiability) {
			queryStr += "AND is_challan_generated = " + isChallanGenerated;
		}

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("challanMonth", challanMonth);
		parameters.put("tan", tan);
		parameters.put("approvedForChallan", approvedForChallan);
		parameters.put("section", section);
		parameters.put("isChallanGenerated", isChallanGenerated);
		parameters.put("isChallanPaid", isChallanPaid);
		parameters.put("queryStr", queryStr);

		return namedParameterJdbcTemplate.queryForObject(String.format(queries.get("get_adavance_totalTdsAmount")),
				parameters, Double.class);

	}

	public Double getTotalInterestAmount(int assessmentYear, int challanMonth, String tan, boolean approvedForChallan,
			String section) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("challanMonth", challanMonth);
		parameters.put("tan", tan);
		parameters.put("approvedForChallan", approvedForChallan);
		parameters.put("section", section);

		return namedParameterJdbcTemplate.queryForObject(String.format(queries.get("get_adavance_totalInterstAmount")),
				parameters, Double.class);

	}

	public Double getTotalPenaltyAmount(int assessmentYear, int challanMonth, String tan, boolean approvedForChallan,
			String section) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("challanMonth", challanMonth);
		parameters.put("tan", tan);
		parameters.put("approvedForChallan", approvedForChallan);
		parameters.put("section", section);

		return namedParameterJdbcTemplate.queryForObject(String.format(queries.get("get_adavance_totalPenaltyAmount")),
				parameters, Double.class);

	}

	public Double getTotalTdsAmountAggregated(int assessmentYear, int challanMonth, String tan,
			boolean approvedForChallan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("challanMonth", challanMonth);
		parameters.put("tan", tan);
		parameters.put("approvedForChallan", approvedForChallan);

		return namedParameterJdbcTemplate.queryForObject(
				String.format(queries.get("get_adavance_totalTdsAmount_Aggregated")), parameters, Double.class);

	}

	public List<AdvanceDTO> getAdavanceByYearMonthTanAndSection(int assessmentYear, int challanMonth, String tan,
			String section, boolean approvedForChallan, boolean isChallanGenerated, boolean isVerifyLiability,
			String systemChallanSerialNumber, boolean isChallanPaid) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		String query = String.format(queries.get("get_adavance_by_year_month_tan_section"));
		if (!isVerifyLiability) {
			query = query.concat("is_challan_generated =" + isChallanGenerated);
		}
		if (systemChallanSerialNumber != null) {
			query = query.concat("system_challan_serial_no =" + systemChallanSerialNumber);
		}

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("challanMonth", challanMonth);
		parameters.put("tan", tan);
		parameters.put("section", section);
		parameters.put("approvedForChallan", approvedForChallan);
		parameters.put("isChallanPaid", isChallanPaid);

		return namedParameterJdbcTemplate.query(query, parameters, new AdvanceRowMapper());
	}

	public List<AdvanceDTO> getAdavanceByYearMonthTanWithoutSection(int assessmentYear, int challanMonth, String tan,
			boolean approvedForChallan, boolean isChallanGenerated, boolean isVerifyLiability, boolean isChallanPaid) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		String query = String.format(queries.get("get_adavance_by_year_month_tan_withOutSection"));
		if (!isVerifyLiability) {
			query = query.concat("is_challan_generated =" + isChallanGenerated);
		}

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("challanMonth", challanMonth);
		parameters.put("tan", tan);
		parameters.put("approvedForChallan", approvedForChallan);
		parameters.put("isChallanPaid", isChallanPaid);

		return namedParameterJdbcTemplate.query(query, parameters, new AdvanceRowMapper());
	}

	public List<AdvanceDTO> getAdavanceByYearMonthTanAggregated(int assessmentYear, int challanMonth, List<String> tan,
			boolean approvedForChallan, boolean isChallanGenerated, String systemChallanSerialNumber) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		String query = String.format(queries.get("get_adavance_by_year_month_tan_aggregated"));
		if (systemChallanSerialNumber != null) {
			query = query.concat("system_challan_serial_no =" + systemChallanSerialNumber);
		}

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("challanMonth", challanMonth);
		parameters.put("tan", tan);
		parameters.put("approvedForChallan", approvedForChallan);

		return namedParameterJdbcTemplate.query(query, parameters, new AdvanceRowMapper());
	}

	public List<AdvanceDTO> getAdvanceDeductees(String tan, String type, int year, int month, boolean isMatch) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String query = String.format(queries.get("get_advance_deductees"));

		if ("N".equalsIgnoreCase(type) && !isMatch) {
			query = query.concat(" and active = 1");
		}
		if (isMatch) {
			query = query.concat(" and mismatch =1 and active = 0");
		}

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("tan", tan);
		parameters.put("type", type);
		parameters.put("year", year);
		parameters.put("month", month);

		return namedParameterJdbcTemplate.query(query, parameters, new AdvanceRowMapper());
	}

	public List<AdvanceDTO> findAllResidentAndNonResident(int assessmentYear, int assessmentMonth, String tan,
			String resType, Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		String paginationOrder = CommonUtil.getPagination("advance_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("assessmentMonth", assessmentMonth);
		parameters.put("tan", tan);
		parameters.put("resType", resType);
		String query = "";
		if ("Y".equalsIgnoreCase(resType)) {
			query = String.format(queries.get("get_advance_resident_nonresident_Y")).concat(paginationOrder);
			return namedParameterJdbcTemplate.query(query, parameters, new AdvanceRowMapper());
		} else {
			query = String.format(queries.get("get_advance_resident_nonresident_N")).concat(paginationOrder);
			return namedParameterJdbcTemplate.query(query, parameters, new AdvanceRowMapper());
		}
	}

	public List<AdvanceDTO> findAllResidentAndNonResidentByDeductee(int assessmentYear, int assessmentMonth, String tan,
			String resType, String deducteeName, Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		String paginationOrder = CommonUtil.getPagination("advance_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("assessmentMonth", assessmentMonth);
		parameters.put("tan", tan);
		parameters.put("resType", resType);
		parameters.put("deducteeName", "%" + deducteeName + "%");
		String query = "";
		if ("Y".equalsIgnoreCase(resType)) {
			query = String.format(queries.get("get_advance_resident_nonresident_byDeductee_Y")).concat(paginationOrder);
			return namedParameterJdbcTemplate.query(query, parameters, new AdvanceRowMapper());
		} else {
			query = String.format(queries.get("get_advance_resident_nonresident_byDeductee_N")).concat(paginationOrder);
			return namedParameterJdbcTemplate.query(query, parameters, new AdvanceRowMapper());
		}
	}

	public AdvanceMismatchByBatchIdDTO getAdvanceMismatchSummary(int assessmentYear, int assessmentMonth, String tan,
			Integer batchId, String type) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		AdvanceMismatchByBatchIdDTO advanceMismatchByBatchIdDTO = new AdvanceMismatchByBatchIdDTO();
		advanceMismatchByBatchIdDTO.setId(batchId);
		advanceMismatchByBatchIdDTO.setMismatchcategory(type);
		MismatchesCountDTO advanceProvisionCountDTO = null;
		if (batchId != null) {
			advanceProvisionCountDTO = getTotalValueAndCountByBatchId(tan, batchId, type);
			advanceMismatchByBatchIdDTO
					.setInvoiceValue(advanceProvisionCountDTO.getAmount().setScale(2, RoundingMode.UP));
			advanceMismatchByBatchIdDTO
					.setTdsSystemAmount(advanceProvisionCountDTO.getDerivedTdsAmount().setScale(2, RoundingMode.UP));
			advanceMismatchByBatchIdDTO
					.setTdsClientAmount(advanceProvisionCountDTO.getActualTdsAmount().setScale(2, RoundingMode.UP));
			advanceMismatchByBatchIdDTO.setTotalRecords(advanceProvisionCountDTO.getTotalCount());
		} else {
			advanceProvisionCountDTO = getTotalValueByYearAndMonth(assessmentYear, assessmentMonth, tan, type);
			advanceMismatchByBatchIdDTO
					.setInvoiceValue(advanceProvisionCountDTO.getAmount().setScale(2, RoundingMode.UP));
			advanceMismatchByBatchIdDTO
					.setTdsSystemAmount(advanceProvisionCountDTO.getDerivedTdsAmount().setScale(2, RoundingMode.UP));
			advanceMismatchByBatchIdDTO
					.setTdsClientAmount(advanceProvisionCountDTO.getActualTdsAmount().setScale(2, RoundingMode.UP));
			advanceMismatchByBatchIdDTO.setTotalRecords(advanceProvisionCountDTO.getTotalCount());
		}

		if (!"NAD".equalsIgnoreCase(type)) {
			if (advanceMismatchByBatchIdDTO.getTdsSystemAmount() != null
					&& advanceMismatchByBatchIdDTO.getTdsClientAmount() != null) {
				if (advanceMismatchByBatchIdDTO.getTdsSystemAmount()
						.compareTo(advanceMismatchByBatchIdDTO.getTdsClientAmount()) < 0) {
					advanceMismatchByBatchIdDTO.setExcessDeduction(advanceMismatchByBatchIdDTO.getTdsSystemAmount()
							.subtract(advanceMismatchByBatchIdDTO.getTdsClientAmount()).setScale(2, RoundingMode.UP));
				} else {
					advanceMismatchByBatchIdDTO.setShortDeduction(advanceMismatchByBatchIdDTO.getTdsSystemAmount()
							.subtract(advanceMismatchByBatchIdDTO.getTdsClientAmount()).setScale(2, RoundingMode.UP));
				}
			}
		} else {
			advanceMismatchByBatchIdDTO.setExcessDeduction(new BigDecimal(0));
			advanceMismatchByBatchIdDTO.setShortDeduction(new BigDecimal(0));
		}
		return advanceMismatchByBatchIdDTO;
	}

	private MismatchesCountDTO getTotalValueAndCountByBatchId(String tan, Integer batchId, String type) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("tan", tan);
		parameters.put("batchId", batchId);
		parameters.put("type", type);

		return namedParameterJdbcTemplate.queryForObject(
				String.format(queries.get("get_advance_total_value_and_count_by_batchid")), parameters,
				new MismatchCountRowMapper());

	}

	private MismatchesCountDTO getTotalValueByYearAndMonth(int assessmentYear, int assessmentMonth, String tan,
			String type) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("assessmentMonth", assessmentMonth);
		parameters.put("tan", tan);
		parameters.put("type", type);
		return namedParameterJdbcTemplate.queryForObject(
				String.format(queries.get("get_advance_total_value_by_year_month")), parameters,
				new MismatchCountRowMapper());

	}

	/**
	 * 
	 * @param deductorMasterTan
	 * @param mismatchCategory
	 * @param batchUploadId
	 * @param pagination
	 * @return
	 */
	public List<AdvanceDTO> getAdvanceDetailsByTanBatchIdAndMismatchCategory(String deductorMasterTan,
			String mismatchCategory, Integer batchUploadId, Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String paginationOrder = CommonUtil.getPagination("advance_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");
		String query = String.format(queries.get("get_advance_by_tan_batchId_mismatchCategory"));
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("deductorMasterTan", deductorMasterTan);
		parameters.put("mismatchCategory", mismatchCategory);
		parameters.put("batchUploadId", batchUploadId);
		query = query.concat(paginationOrder);

		return namedParameterJdbcTemplate.query(query, parameters, new AdvanceRowMapper());

	}

	public BigInteger getCountoFAdvanceDetailsByTanBatchIdAndMismatchCategory(String deductorMasterTan,
			String mismatchCategory, Integer batchUploadId) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String query = String.format(queries.get("get_count_advance_by_tan_batchId_mismatchCategory"));
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("deductorMasterTan", deductorMasterTan);
		parameters.put("mismatchCategory", mismatchCategory);
		parameters.put("batchUploadId", batchUploadId);
		return namedParameterJdbcTemplate.queryForObject(query, parameters, BigInteger.class);

	}

	public List<AdvanceDTO> findByYearTanDocumentPostingDateIdActive(int assessmentYear, String tan,
			Date documentPostingDate, Integer id, boolean activeFlag) {

		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("year", assessmentYear);
		parameters.put("tan", tan);
		parameters.put("doucmentPostingDate", documentPostingDate);
		parameters.put("id", id);
		parameters.put("activeFlag", activeFlag == true ? 1 : 0);
		return namedParameterJdbcTemplate.query(
				String.format(queries.get("find_advance_By_Year_Tan_Document_PostingDate_Id_Active")), parameters,
				new AdvanceRowMapper());
	}

	public Double getLdcRemainigBalance(String deductorMasterTan, String deducteePan, String finalTdsSection,
			Date postingDocumentDate) {

		Double balance = 0.0;
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		try {
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-mm-dd");
			postingDocumentDate = df2.parse(df2.format(postingDocumentDate));
		} catch (Exception e) {
			logger.error("Exception occured while parsing date ");
			throw new RuntimeException();
		}
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("deductorMasterTan", deductorMasterTan);
		parameters.put("deducteePan", deducteePan);
		parameters.put("finalTdsSection", finalTdsSection);
		parameters.put("documentDate", postingDocumentDate);
		balance = namedParameterJdbcTemplate.queryForObject(String.format(queries.get("find_ldc_remainingBalance")),
				parameters, Double.class);
		return balance == null ? 0 : balance;
	}

	public List<LdcMaster> getLdcRecordByTanPanSectionDocumentDate(String deductorMasterTan, String deducteePan,
			String finalTdsSection, Date postingDocumentDate) {

		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		try {
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-mm-dd");
			postingDocumentDate = df2.parse(df2.format(postingDocumentDate));
		} catch (Exception e) {
			logger.error("Exception occured while parsing date ");
			throw new RuntimeException();
		}
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("deductorMasterTan", deductorMasterTan);
		parameters.put("deducteePan", deducteePan);
		parameters.put("finalTdsSection", finalTdsSection);
		parameters.put("documentDate", postingDocumentDate);
		return namedParameterJdbcTemplate.query(String.format(queries.get("find_ldc_By_Tan_pan_section_docuemntDate")),
				parameters, new LdcMasterRowMapper());
	}

	public List<MatrixDTO> getAdvanceMatrix(String deductorMasterTan, Integer year, String type) {

		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("deductorMasterTan", deductorMasterTan);
		parameters.put("year", year);
		parameters.put("isTdsAmount", type.equals("tdsAmount") ? 1 : 0);

		return namedParameterJdbcTemplate.query(String.format(queries.get("get_advance_matrix")), parameters,
				new MatrixRowMapper());
	}

	public List<AdvanceDTO> getAllAdvanceMismatches(String deductorMasterTan, Integer year, Integer month) {

		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("deductorMasterTan", deductorMasterTan);
		parameters.put("year", year);
		parameters.put("month", month);

		return namedParameterJdbcTemplate.query(String.format(queries.get("get_advance_By_tan_mimatch_year")),
				parameters, new AdvanceRowMapper());
	}

	public List<AdvanceDTO> getAdvanceDetailsByTanActiveAndMismatchCategory(int assessmentYear, int month,
			String deductorMasterTan, String mismatchCategory, MismatchesFiltersDTO filters) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("month", month);
		parameters.put("deductorMasterTan", deductorMasterTan);
		parameters.put("mismatchCategory", mismatchCategory);

		String query = String.format(queries.get("get_advance_By_tan_mimatch"));

		if (StringUtils.isNotBlank(filters.getDeducteeName())
				&& !"undefined".equalsIgnoreCase(filters.getDeducteeName())) {
			query = query.concat(" and deductee_name = " + filters.getDeducteeName());
		}
		if (StringUtils.isNotBlank(filters.getResidentType())
				&& !"undefined".equalsIgnoreCase(filters.getResidentType())) {
			query = query.concat(" and is_resident = " + filters.getResidentType());
		}
		if (StringUtils.isNotBlank(filters.getConfidence()) && !"undefined".equalsIgnoreCase(filters.getConfidence())) {
			query = query.concat(" and confidence = " + filters.getConfidence());
		}
		if (StringUtils.isNotBlank(filters.getSection()) && !"undefined".equalsIgnoreCase(filters.getSection())) {
			query = query.concat(" and withholding_section = " + filters.getSection());
		}
		if (StringUtils.isNotBlank(filters.getDerivedSection())
				&& !"undefined".equalsIgnoreCase(filters.getDerivedSection())) {
			query = query.concat(" and derived_tds_section = " + filters.getDerivedSection());
		}

		String paginationOrder = CommonUtil.getPagination("advance_id", filters.getPagination().getPageNumber(),
				filters.getPagination().getPageSize(), filters.getPagination().isNext() ? "ASC" : "DESC");
		String paginatedQuery = query + paginationOrder;
		return namedParameterJdbcTemplate.query(paginatedQuery, parameters, new AdvanceRowMapper());

	}

	public BigInteger getAdvanceDetailsCountByTanMismatchCategory(int assessmentYear, int month,
			String deductorMasterTan, String mismatchCategory, MismatchesFiltersDTO filters) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("month", month);
		parameters.put("deductorMasterTan", deductorMasterTan);
		parameters.put("mismatchCategory", mismatchCategory);

		String query = String.format(queries.get("get_advance_By_tan_mimatch_count"));

		if (StringUtils.isNotBlank(filters.getDeducteeName())
				&& !"undefined".equalsIgnoreCase(filters.getDeducteeName())) {
			query = query.concat(" and deductee_name = " + filters.getDeducteeName());
		}
		if (StringUtils.isNotBlank(filters.getResidentType())
				&& !"undefined".equalsIgnoreCase(filters.getResidentType())) {
			query = query.concat(" and is_resident = " + filters.getResidentType());
		}
		if (StringUtils.isNotBlank(filters.getConfidence()) && !"undefined".equalsIgnoreCase(filters.getConfidence())) {
			query = query.concat(" and confidence = " + filters.getConfidence());
		}
		if (StringUtils.isNotBlank(filters.getSection()) && !"undefined".equalsIgnoreCase(filters.getSection())) {
			query = query.concat(" and withholding_section = " + filters.getSection());
		}
		if (StringUtils.isNotBlank(filters.getDerivedSection())
				&& !"undefined".equalsIgnoreCase(filters.getDerivedSection())) {
			query = query.concat(" and derived_tds_section = " + filters.getDerivedSection());
		}

		return namedParameterJdbcTemplate.queryForObject(query, parameters, BigInteger.class);

	}

	public long getAdvanceMismatchCount(String deductorMasterTan, int year, int month) {

		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("deductorMasterTan", deductorMasterTan);
		parameters.put("year", year);
		parameters.put("month", month);

		return namedParameterJdbcTemplate.queryForObject(String.format(queries.get("get_advance_mismatch_count")),
				parameters, Long.class);
	}

	public List<AdvanceDTO> getAdvanceFTM(String deductorTan, int assessmentYear, int month) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("deductorMasterTan", deductorTan);
		parameters.put("year", assessmentYear);
		parameters.put("month", month);

		return namedParameterJdbcTemplate.query(String.format(queries.get("get_advance_ftm")), parameters,
				new AdvanceRowMapper());

	}

	/**
	 * 
	 * @param deductorMasterTan
	 * @param year
	 * @param month
	 * @return
	 */
	public List<MatrixDTO> getAdvanceClosingMatrixReport(String deductorMasterTan, Integer year, Integer month) {

		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("deductorMasterTan", deductorMasterTan);
		parameters.put("year", year);
		parameters.put("month", month);

		return namedParameterJdbcTemplate.query(
				String.format(queries.get("get_advance_closing_and_opening_matrix_report")), parameters,
				new MatrixReportsRowMapper());
	}

	/**
	 * 
	 * @param advanceId
	 * @return
	 */
	public List<AdvanceDTO> findByAdvanceId(Integer advanceId) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("advanceId", advanceId);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_advance_by_advanceId")), parameters,
				new AdvanceRowMapper());
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param tan
	 * @param id
	 * @return
	 */
	public List<AdvanceDTO> findByYearTanDocumentPostingDateId(int assessmentYear, String tan, Integer id) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("year", assessmentYear);
		parameters.put("tan", tan);
		parameters.put("id", id);
		return namedParameterJdbcTemplate.query(
				String.format(queries.get("find_advance_By_Year_Tan_Document_PostingDate_Id")), parameters,
				new AdvanceRowMapper());
	}

	/**
	 * 
	 * @param tan
	 * @param deducteePan
	 * @param deducteeCode
	 * @param deducteeName
	 * @return
	 */
	public BigInteger findAdvancesByDeducteePanCode(String deductorTan, String deducteeKey) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorTan", deductorTan);
		parameters.put("deducteeKey", deducteeKey);
		String query = String.format(queries.get("advance_pan_code_count"));

		return namedParameterJdbcTemplate.queryForObject(query, parameters, BigInteger.class);

	}

	public List<String> getVendorSections(String deducteeKey, String residentialStatus)
			throws JsonMappingException, JsonProcessingException {
		List<VendorSectionDTO> list = null;
		List<String> sectionList = new ArrayList<>();
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("name", deducteeKey);
		if ("Y".equalsIgnoreCase(residentialStatus)) {

			list = namedParameterJdbcTemplate.query(queries.get("deductee_non_resident_status"), parameters,
					new VendorSectionRowMapper());
		} else {
			list = namedParameterJdbcTemplate.query(queries.get("deductee_resident_status"), parameters,
					new VendorSectionRowMapper());
		}
		for (VendorSectionDTO dto : list) {
			if (StringUtils.isNotBlank(dto.getSection())) {
				sectionList.add(dto.getSection().trim());
			}
			if (dto.getAdditionalSection() != null) {
				sectionList.addAll(getAdditionlSection(dto.getAdditionalSection(), sectionList));
			}
		}
		Set<String> set = new LinkedHashSet<>();
		set.addAll(sectionList);
		sectionList.clear();
		sectionList.addAll(set);

		return sectionList;
	}

	public List<String> getVendorSectionsAll(String deducteeKey, String residentialStatus,
			List<DeducteeDetailsDTO> vendorSectionNR, List<DeducteeDetailsDTO> vendorSectionR)
			throws JsonMappingException, JsonProcessingException {
		List<DeducteeDetailsDTO> list = new ArrayList<>();
		List<String> sectionList = new ArrayList<>();
		if ("Y".equalsIgnoreCase(residentialStatus)) {
			for (DeducteeDetailsDTO vs : vendorSectionNR) {
				if (vs.getDeducteeKey().equals(deducteeKey)) {
					list.add(vs);
				}
			}
		} else {
			for (DeducteeDetailsDTO vs : vendorSectionR) {
				if (vs.getDeducteeKey().equals(deducteeKey)) {
					list.add(vs);
				}
			}
		}
		for (DeducteeDetailsDTO dto : list) {
			if (StringUtils.isNotBlank(dto.getSection())) {
				sectionList.add(dto.getSection().trim());
			}
			if (dto.getAdditionalSection() != null) {
				sectionList.addAll(getAdditionlSection(dto.getAdditionalSection(), sectionList));
			}
		}
		Set<String> set = new LinkedHashSet<>();
		set.addAll(sectionList);
		sectionList.clear();
		sectionList.addAll(set);

		return sectionList;
	}

	public Set<String> getDeducteeSections(String residentialStatus, DeducteeDetailsDTO deducteeData)
			throws JsonMappingException, JsonProcessingException {
		Set<String> sectionList = new HashSet<>();
		if ("Y".equalsIgnoreCase(residentialStatus)) {
			sectionList.add(deducteeData.getSection());
			if (deducteeData.getAdditionalSection() != null) {
				sectionList.addAll(getAdditionlSection(deducteeData.getAdditionalSection(), sectionList));
			}
		} else {
			sectionList.add(deducteeData.getSection());
			if (deducteeData.getAdditionalSection() != null) {
				sectionList.addAll(getAdditionlSection(deducteeData.getAdditionalSection(), sectionList));
			}
		}
		Set<String> set = new LinkedHashSet<>();
		set.addAll(sectionList);
		sectionList.clear();
		sectionList.addAll(set);

		return sectionList;
	}

	private List<String> getAdditionlSection(String section, List<String> list)
			throws JsonMappingException, JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Float> additionalSections = null;
		if (section != null) {
			additionalSections = objectMapper.readValue(section, new TypeReference<Map<String, Float>>() {
			});
		}
		if (additionalSections != null) {
			for (Map.Entry<String, Float> entry : additionalSections.entrySet()) {
				String additionalSection = StringUtils.substringBefore(entry.getKey(), "-");
				list.add(additionalSection);
			}
		}

		return list;
	}

	private Set<String> getAdditionlSection(String section, Set<String> list)
			throws JsonMappingException, JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Float> additionalSections = null;
		if (section != null) {
			additionalSections = objectMapper.readValue(section, new TypeReference<Map<String, Float>>() {
			});
		}
		if (additionalSections != null) {
			for (Map.Entry<String, Float> entry : additionalSections.entrySet()) {
				String additionalSection = StringUtils.substringBefore(entry.getKey(), "-");
				list.add(additionalSection);
			}
		}

		return list;
	}

	public BigInteger getAdvancesByYearDeducteeKeyDocumentNo(int assessmentYear, String deductorTan, String deducteeKey,
			String erpDocumentNo, String invoiceNumber, Date documentPostingDate) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String postingDate = new SimpleDateFormat("yyyy-MM-dd").format(documentPostingDate);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("deductorTan", deductorTan);
		parameters.put("deducteeKey", deducteeKey);
		parameters.put("erpDocumentNo", erpDocumentNo);
		parameters.put("invoiceNumber", invoiceNumber);
		parameters.put("documentPostingDate", postingDate);

		String query = String.format(queries.get("advance_year_pan_name_tan_doc_count"));

		return namedParameterJdbcTemplate.queryForObject(query, parameters, BigInteger.class);

	}

	/**
	 * to get the invoice interest records with out pagination
	 * 
	 * @param tan
	 * @param year
	 * @param month
	 * @param deducteeName
	 * @param residentType
	 * @return
	 */
	public List<AdvanceDTO> getAdvancesWithInterestComputed(String tan, Integer year, Integer month) {
		logger.info("DAO method excuting to get the interest records with year =" + year + ",month=" + month + ",tan="
				+ tan);
		String query = String.format(queries.get("get_advances_with_interest_computed"));

		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("year", year);
		parameter.put("month", month);
		parameter.put("tan", tan);
		return namedParameterJdbcTemplate.query(query, parameter, new AdvanceRowMapper());
	}

	/**
	 * to get the interest records with pagination
	 * 
	 * @param tan
	 * @param year
	 * @param deducteeName
	 * @param residentType
	 * @param pagination
	 * @return
	 */
	public List<AdvanceDTO> getAdvancecesWithInterestComputedWithPagination(String tan, Integer year, Integer month,
			String deducteeName, String residentType, Pagination pagination) {
		logger.info("DAO method excuting to get the interest records with year =" + year + ",month=" + month + ",tan="
				+ tan + ",deductee name=" + deducteeName + ",Resident type=" + residentType);
		String paginationOrder = CommonUtil.getPagination("advance_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");
		String query = String.format(queries.get("get_advances_with_interest_computed"));
		if (StringUtils.isNotBlank(deducteeName)) {
			query = query + " AND deductee_name='" + deducteeName + "' ";
		}
		if (StringUtils.isNotBlank(residentType)) {
			query = query + " AND is_resident='" + residentType + "' ";
		}
		query = query.concat(paginationOrder);
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("year", year);
		parameter.put("month", month);
		parameter.put("tan", tan);
		return namedParameterJdbcTemplate.query(query, parameter, new AdvanceRowMapper());
	}

	/**
	 * to get the count of invoice interest records
	 * 
	 * @param tan
	 * @param year
	 * @param month
	 * @param deducteeName
	 * @param residentType
	 * @return
	 */
	public BigInteger getCountOfAdvancesWithInterestComputed(String tan, Integer year, Integer month,
			String deducteeName, String residentType) {
		logger.info("DAO method excuting to get the interest records count with year =" + year + ",month=" + month
				+ ",tan=" + tan + ",deductee name=" + deducteeName + ",Resident type=" + residentType);
		String query = String.format(queries.get("get_count_of_invoices_with_interest_computed"));
		if (StringUtils.isNotBlank(deducteeName)) {
			query = query + " AND deductee_name='" + deducteeName + "' ";
		}
		if (StringUtils.isNotBlank(residentType)) {
			query = query + " AND is_resident='" + residentType + "' ";
		}
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("year", year);
		parameter.put("month", month);
		parameter.put("tan", tan);
		return namedParameterJdbcTemplate.queryForObject(query, parameter, BigInteger.class);
	}

	/**
	 * to get the invoice interest records based on the id,interest amount and
	 * active
	 * 
	 * @param id
	 * @return
	 */
	public List<AdvanceDTO> getAdvancsWithInterestById(Integer id) {
		logger.info("DAO method excuting to get the interest records with id =" + id);
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("id", id);
		return namedParameterJdbcTemplate.query(
				String.format(queries.get("get_advance_with_interest_computed_by_advance_id")), parameter,
				new AdvanceRowMapper());
	}

	public List<DeducteeDetailsDTO> getDeducteeNonResidentStatusAll(String deductorPan) {
		logger.info("DAO method excuting to get the ideductee_non_resident_status_all");
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("deductorPan", deductorPan);
		List<DeducteeDetailsDTO> list = namedParameterJdbcTemplate
				.query(queries.get("deductee_non_resident_status_all"), parameter, new DeducteeDetailsRowMapper());
		return list;
	}

	public List<DeducteeDetailsDTO> getDeducteeResidentStatusAll(String deductorPan) {
		logger.info("DAO method excuting to get the ideductee_non_resident_status_all");
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("deductorPan", deductorPan);
		List<DeducteeDetailsDTO> list = namedParameterJdbcTemplate.query(queries.get("deductee_resident_status_all"),
				parameter, new DeducteeDetailsRowMapper());
		return list;
	}

	// to update the advance records
	public int updateAdvanceInterest(AdvanceDTO dto) {
		int status = 0;
		logger.info("Updating the Advance for Interest{}");
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(dto);
		status = namedParameterJdbcTemplate.update(queries.get("advance_interest_update"), namedParameters);
		if (status != 0) {
			logger.info("Advance updated successfully with final amount as{}" + dto.getFinalTdsAmount());
		}
		return status;
	}

	/**
	 * This method for batch update advance mismatch recodes.
	 * 
	 * @param advanceList
	 */
	@org.springframework.transaction.annotation.Transactional
	public void batchUpdateAdvacneMismatch(List<AdvanceDTO> advanceList) {
		String updateQuery = "UPDATE Transactions.advance SET action = :action, reason = :reason,"
				+ " final_tds_rate = :finalTdsRate , final_tds_section = :finalTdsSection,"
				+ " final_tds_amount = :finalTdsAmount, mismatch = :mismatch, active =:active ,"
				+ " surcharge = :surcharge, cess_amount = :cessAmount, cess_rate = :cessRate,"
				+ " interest = :interest, error_reason = :errorReason, is_exempted = :isExempted, "
				+ " advance_npid = :advanceNpId, advance_groupid = :advanceGroupid, ldc_certificate_number =:ldcCertificateNumber, "
				+ " has_ldc=:hasLdc, amount= :amount, client_taxable_amount =:clientTaxableAmount WHERE advance_id = :id ; ";
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(advanceList);
		namedParameterJdbcTemplate.batchUpdate(updateQuery, batch);
		logger.info("advance batch updated successfully {}", advanceList.size());

	}

	/**
	 * 
	 * @param deductorMasterTan
	 * @param deducteePan
	 * @param finalTdsSection
	 * @param postingDocumentDate
	 * @return
	 */
	public List<LdcMaster> getLdcRecordByTan(String deductorMasterTan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorMasterTan", deductorMasterTan);
		return namedParameterJdbcTemplate.query(String.format(queries.get("find_ldc_by_tan")), parameters,
				new LdcMasterRowMapper());
	}

	/**
	 * 
	 * @param deductorMasterTan
	 * @return
	 */
	public List<LdcMasterUtilizationDTO> getLdcUtilizationAmountByTan(String deductorMasterTan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorMasterTan", deductorMasterTan);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_ldc_utilized_amount_by_tan")),
				parameters, new LdcMasterUtilizationRowMapper());
	}

	/**
	 * to get the error records from invoice line item table
	 * 
	 * @param tan
	 * @param assessemtYear
	 * @param batchUploadId
	 * @return
	 */
	public List<AdvanceDTO> getAdvanceErrorRecords(String tan, Integer assessemtYear, Integer batchUploadId) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("year", assessemtYear);
		parameters.put("tan", tan);
		parameters.put("batchUploadId", batchUploadId);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_advance_error_records")), parameters,
				new AdvanceRowMapper());
	}

	/**
	 * 
	 * @param tan
	 * @param assessmentYear
	 * @param assessmentMonth
	 * @param deductorPan
	 * @return
	 */
	public List<AdvanceDTO> getUpdatedAdvanceRecords(String tan, int assessmentYear, int assessmentMonth,
			String deductorPan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("year", assessmentYear);
		parameters.put("tan", tan);
		parameters.put("deductorPan", deductorPan);
		parameters.put("month", assessmentMonth);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_update_mismatch_advance_data")),
				parameters, new AdvanceRowMapper());
	}

	/**
	 * 
	 * @param advanceIds
	 * @param tan
	 * @param deductorPan
	 * @return
	 */
	public List<AdvanceDTO> getAdvanceListBasedOnIds(List<Integer> advanceIds, String tan, String deductorPan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("advanceIds", advanceIds);
		parameters.put("tan", tan);
		parameters.put("deductorPan", deductorPan);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_advance_by_advanceIds")), parameters,
				new AdvanceRowMapper());
	}

	/**
	 * 
	 * @param deductorPan
	 * @param deductorTan
	 * @param isResident 
	 * @return
	 */
	public List<AdvanceDTO> getAllAdvancePoData(String deductorPan, String deductorTan, String isResident) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorTan", deductorTan);
		parameters.put("deductorPan", deductorPan);
		parameters.put("isResident", isResident);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_all_advance_po_data")), parameters,
				new AdvanceRowMapper());
	}

	/**
	 * 
	 * @param advanceList
	 */
	@org.springframework.transaction.annotation.Transactional
	public void batchUpdateAdvacnePoRecords(List<AdvanceDTO> advanceList) {
		String updateQuery = "UPDATE Transactions.advance SET po_number = :poNumber WHERE advance_id = :id ; ";
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(advanceList);
		namedParameterJdbcTemplate.batchUpdate(updateQuery, batch);
		logger.info("advance po number batch updated successfully {}", advanceList.size());

	}

	public List<AdvanceStagging> getAdvanceStaggingRecords(String deductorTan, String deductorPan, Integer year,
			Integer month) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("year", year);
		parameters.put("month", month);
		parameters.put("tan", deductorTan);
		return namedParameterJdbcTemplate.query(String.format(queries.get("find_advance_stagging_records")), parameters,
				new AdvanceStaggingRowMapper());
	}

	public DeductorMaster findByDeductorPan(String deductorPan) {
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("deductorPan", deductorPan);
		String query = "SELECT name, deductor_master_pan, code FROM Onboarding.deductor_master WHERE deductor_master_pan =:deductorPan "
				+ "	AND active = 1 ";
		return namedParameterJdbcTemplate.queryForObject(query, parameter,
				new BeanPropertyRowMapper<DeductorMaster>(DeductorMaster.class));
	}

	public String findOnboardingDetailsByDeductorPan(String deductorPan) {
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("deductorPan", deductorPan);
		String query = "select priority from Onboarding.deductor_onboarding_info where active = 1 and pan =:deductorPan ";
		return namedParameterJdbcTemplate.queryForObject(query, parameter, String.class);
	}

	/**
	 * This method for advance batch save
	 * 
	 * @param advanceBatchSave
	 * @param tenantId
	 */
	@org.springframework.transaction.annotation.Transactional
	public void advanceBatchSave(List<AdvanceDTO> advanceBatchSave) {
		String query = " INSERT INTO Transactions.advance(assessment_year, deductor_master_tan, assessment_month, challan_month, amount, deductor_pan ,deductee_key, deductee_code,"
				+ " deductee_name,document_type, is_resident, deductee_pan,supply_type,document_date,document_number,line_item_number,advance_npid ,advance_groupid ,active,"
				+ " is_parent ,is_exempted ,challan_paid ,approved_for_challan ,is_challan_generated ,created_by ,created_date ,modified_by ,modified_date,"
				+ " posting_date_of_document,mismatch ,under_threshold ,withholding_section ,derived_tds_section ,final_tds_section ,withholding_rate ,derived_tds_rate,"
				+ " final_tds_rate,withholding_amount ,derived_tds_amount ,final_tds_amount, batch_upload_id, is_initial_record,"
				+ " nr_transactions_meta_id, processed_from, source_identifiers, [section], is_error, confidence, service_description, service_description_po,"
				+ " service_description_gl, surcharge, cess_rate, po_date, po_number, deductee_tin, tds_rate, mismatch_category, ldc_certificate_number, has_ldc,"
				+ " tds_deduction_date, payment_date, has_dtaa, tds_remittance_date, debit_credit_indicator, tds_base_value, assignment_Number, profit_center, plant, "
				+ " business_area, business_place, gl_account_code, deductor_gstin, linking_of_invoice_with_po, user_defined_field_1, user_defined_field_2, user_defined_field_3,"
				+ " user_defined_field_4, user_defined_field_5, action, client_effective_tds_rate) "
				+ " VALUES(:assessmentYear,:deductorMasterTan,:assessmentMonth,:challanMonth, :amount, :deductorPan,:deducteeKey,:deducteeCode,"
				+ " :deducteeName,:documentType,:isResident, :deducteePan,:supplyType,:documentDate,:documentNumber,:lineItemNumber,:advanceNpId,:advanceGroupid,:active,"
				+ " :isParent,:isExempted,:challanPaid,:approvedForChallan,:isChallanGenerated,:createdBy,:createdDate,:modifiedBy,:modifiedDate,"
				+ " :postingDateOfDocument,:mismatch,:underThreshold,:withholdingSection,:derivedTdsSection,:finalTdsSection,:withholdingRate,:derivedTdsRate,"
				+ " :finalTdsRate,:withholdingAmount,:derivedTdsAmount,:finalTdsAmount,:batchUploadId,:isInitialRecord,"
				+ " :nrTransactionsMetaId, :processedFrom, :sourceIdentifiers, :section, :isError, :confidence, :serviceDescription, :serviceDescriptionPo,"
				+ " :serviceDescriptionGl,:surcharge ,:cessRate,:poDate,:poNumber,:deducteeTin,:tdsRate,:mismatchCategory,:ldcCertificateNumber,"
				+ " :hasLdc, :tdsDeductionDate, :paymentDate, :hasDtaa, :tdsRemittancedate, :debitCreditIndicator, :tdsBaseValue, :assignmentNumber, :profitCenter, :plant,"
				+ " :businessArea, :businessPlace, :glAccountCode, :deductorGstin, :linkingOfInvoiceWithPo, :userDefinedField1, :userDefinedField2, :userDefinedField3,"
				+ " :userDefinedField4, :userDefinedField5, :action, :clientEffectiveTdsRate); ";
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(advanceBatchSave);
		namedParameterJdbcTemplate.batchUpdate(query, batch);
		logger.info("deductee advance batch inserted size is {}", advanceBatchSave.size());
	}

	public int updateAdvanceAncestorId(Integer batchId, String deductorTan) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("batchId", batchId);
		parameters.put("tan", deductorTan);
		String query = "UPDATE Transactions.advance SET ancestor_id = advance_id WHERE batch_upload_id =:batchId and deductor_master_tan =:tan";
		return namedParameterJdbcTemplate.update(query, parameters);
	}
	
	public int updateAdvanceMetaNrId(Integer batchId, String deductorTan) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("batchId", batchId);
		parameters.put("tan", deductorTan);
		String query = "UPDATE A SET nr_transactions_meta_id = NM.id"
				+ " FROM Transactions.nr_transactions_meta NM" + " INNER JOIN Transactions.advance A"
				+ " ON A.document_number = NM.erp_document_no AND A.line_item_number = NM.line_item_number AND"
				+ " A.document_type = NM.document_type AND A.supply_type = NM.supply_type AND A.deductor_pan = NM.deductor_pan AND "
				+ " A.deductor_master_tan = NM.deductor_master_tan AND A.posting_date_of_document = NM.document_posting_date AND"
				+ " A.batch_upload_id = NM.batch_upload_id AND A.assessment_year = NM.assessment_year"
				+ " WHERE NM.batch_upload_id = :batchId and NM.deductor_master_tan =:tan";
		return namedParameterJdbcTemplate.update(query, parameters);
	}

}
