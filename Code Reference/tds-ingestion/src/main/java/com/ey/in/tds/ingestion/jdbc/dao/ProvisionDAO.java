package com.ey.in.tds.ingestion.jdbc.dao;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.MismatchesCountDTO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.ProvisionDTO;
import com.ey.in.tds.common.domain.transactions.jdbc.rowmapper.MatrixReportsRowMapper;
import com.ey.in.tds.common.domain.transactions.jdbc.rowmapper.MatrixRowMapper;
import com.ey.in.tds.common.domain.transactions.jdbc.rowmapper.ProvisionRowMapper;
import com.ey.in.tds.common.dto.invoice.MismatchesFiltersDTO;
import com.ey.in.tds.common.model.provision.MatrixDTO;
import com.ey.in.tds.common.model.provision.ProvisionMismatchByBatchIdDTO;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.ingestion.jdbc.rowmapper.MismatchCountRowMapper;

/**
 * 
 * @author scriptbees.
 *
 */
@Repository
public class ProvisionDAO implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired(required = false)
	private HashMap<String, String> queries;

	@Autowired
	private DataSource dataSource;

	private SimpleJdbcInsert simpleJdbcInsert;

	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@PostConstruct
	private void postConstruct() {
		namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("provision").withSchemaName("Transactions")
				.usingGeneratedKeyColumns("provision_id");

	}

	public ProvisionDTO save(ProvisionDTO dto) {
		logger.info("insert provision method execution started  {}");
		dto.setDeductorMasterTan(dto.getDeductorMasterTan());
		dto.setAssessmentYear(dto.getAssessmentYear());
		dto.setPostingDateOfDocument(dto.getPostingDateOfDocument());
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(dto);
		dto.setId(simpleJdbcInsert.executeAndReturnKey(namedParameters).intValue());
		logger.info("Record inserted to provision table {}");
		return dto;
	}

	public int update(ProvisionDTO dto) {
		dto.setDeductorMasterTan(dto.getDeductorMasterTan());
		dto.setAssessmentYear(dto.getAssessmentYear());
		dto.setPostingDateOfDocument(dto.getPostingDateOfDocument());
		dto.setId(dto.getId());
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(dto);
		int status = namedParameterJdbcTemplate.update(String.format(queries.get("provision_update")), namedParameters);
		return status;
	}

	private MismatchesCountDTO getTotalValueAndCountByBatchId(String tan, Integer batchId, String type) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("tan", tan);
		parameters.put("batchId", batchId);
		parameters.put("type", type);

		return namedParameterJdbcTemplate.queryForObject(
				String.format(queries.get("get_provision_total_value_and_count_by_batchid")), parameters,
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
				String.format(queries.get("get_provision_total_value_by_year_month")), parameters,
				new MismatchCountRowMapper());

	}

	public ProvisionMismatchByBatchIdDTO getProvisionMismatchSummary(int assessmentYear, int assessmentMonth,
			String tan, Integer batchId, String type) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		ProvisionMismatchByBatchIdDTO provisionMismatchByBatchIdDTO = new ProvisionMismatchByBatchIdDTO();
		provisionMismatchByBatchIdDTO.setId(batchId);
		provisionMismatchByBatchIdDTO.setMismatchcategory(type);
		MismatchesCountDTO advanceProvisionCountDTO = null;
		if (batchId != null) {
			advanceProvisionCountDTO = getTotalValueAndCountByBatchId(tan, batchId, type);
			provisionMismatchByBatchIdDTO
					.setInvoiceValue(advanceProvisionCountDTO.getAmount().setScale(2, RoundingMode.UP));
			provisionMismatchByBatchIdDTO
					.setTdsSystemAmount(advanceProvisionCountDTO.getDerivedTdsAmount().setScale(2, RoundingMode.UP));
			provisionMismatchByBatchIdDTO
					.setTdsClientAmount(advanceProvisionCountDTO.getActualTdsAmount().setScale(2, RoundingMode.UP));
			provisionMismatchByBatchIdDTO.setTotalRecords(advanceProvisionCountDTO.getTotalCount());
		} else {
			advanceProvisionCountDTO = getTotalValueByYearAndMonth(assessmentYear, assessmentMonth, tan, type);
			provisionMismatchByBatchIdDTO
					.setInvoiceValue(advanceProvisionCountDTO.getAmount().setScale(2, RoundingMode.UP));
			provisionMismatchByBatchIdDTO
					.setTdsSystemAmount(advanceProvisionCountDTO.getDerivedTdsAmount().setScale(2, RoundingMode.UP));
			provisionMismatchByBatchIdDTO
					.setTdsClientAmount(advanceProvisionCountDTO.getActualTdsAmount().setScale(2, RoundingMode.UP));
			provisionMismatchByBatchIdDTO.setTotalRecords(advanceProvisionCountDTO.getTotalCount());
		}

		if (!"NAD".equalsIgnoreCase(type)) {
			if (provisionMismatchByBatchIdDTO.getTdsSystemAmount() != null
					&& provisionMismatchByBatchIdDTO.getTdsClientAmount() != null) {
				if (provisionMismatchByBatchIdDTO.getTdsSystemAmount()
						.compareTo(provisionMismatchByBatchIdDTO.getTdsClientAmount()) < 0) {
					provisionMismatchByBatchIdDTO.setExcessDeduction(provisionMismatchByBatchIdDTO.getTdsSystemAmount()
							.subtract(provisionMismatchByBatchIdDTO.getTdsClientAmount()).setScale(2, RoundingMode.UP));
				} else {
					provisionMismatchByBatchIdDTO.setShortDeduction(provisionMismatchByBatchIdDTO.getTdsSystemAmount()
							.subtract(provisionMismatchByBatchIdDTO.getTdsClientAmount()).setScale(2, RoundingMode.UP));
				}
			}
		} else {
			provisionMismatchByBatchIdDTO.setExcessDeduction(new BigDecimal(0));
			provisionMismatchByBatchIdDTO.setShortDeduction(new BigDecimal(0));
		}
		return provisionMismatchByBatchIdDTO;
	}

	public List<ProvisionDTO> getProvisionDetailsByTanBatchIdAndMismatchCategory(String deductorMasterTan,
			String mismatchCategory, Integer batchUploadId) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("deductorMasterTan", deductorMasterTan);
		parameters.put("mismatchCategory", mismatchCategory);
		parameters.put("batchUploadId", batchUploadId);

		return namedParameterJdbcTemplate.query(String.format(queries.get("get_by_tan_batchId_mismatchCategory")),
				parameters, new ProvisionRowMapper());

	}

	public List<ProvisionDTO> findByYearTanDocumentPostingDateId(int assessmentYear, String tan,
			Date documentPostingDate, Integer id) {

		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("year", assessmentYear);
		parameters.put("tan", tan);
		parameters.put("doucmentPostingDate", documentPostingDate);
		parameters.put("id", id);
		return namedParameterJdbcTemplate.query(
				String.format(queries.get("find_provision_By_Year_Tan_Document_PostingDate_Id")), parameters,
				new ProvisionRowMapper());
	}

	public List<ProvisionDTO> findByYearTanDocumentPostingDateIdAndActive(int assessmentYear, String tan,
			Date documentPostingDate, Integer id, boolean activeFlag) {

		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("year", assessmentYear);
		parameters.put("tan", tan);
		parameters.put("doucmentPostingDate", documentPostingDate);
		parameters.put("id", id);
		parameters.put("activeFlag", activeFlag);
		return namedParameterJdbcTemplate.query(
				String.format(queries.get("find_provision_By_Year_Tan_Document_PostingDate_Id_active")), parameters,
				new ProvisionRowMapper());
	}

	/**
	 * 
	 * @param deductorMasterTan
	 * @param mismatchCategory
	 * @param batchUploadId
	 * @param pagination
	 * @param pagination
	 * @return
	 */
	public List<ProvisionDTO> getAllProvisionMisMatchListByTanYearMismatchCategoryBatchId(String deductorMasterTan,
			String mismatchCategory, Integer batchUploadId, Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String paginationOrder = CommonUtil.getPagination("provision_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("deductorMasterTan", deductorMasterTan);
		parameters.put("mismatchCategory", mismatchCategory);
		parameters.put("batchUploadId", batchUploadId);
		String query = String.format(queries.get("get_provision_by_tan_batchId_mismatchCategory"));
		query = query.concat(paginationOrder);
		return namedParameterJdbcTemplate.query(query, parameters, new ProvisionRowMapper());

	}

	public BigInteger getcountOfProvisionMisMatchListByTanYearMismatchCategoryBatchId(String deductorMasterTan,
			String mismatchCategory, Integer batchUploadId) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("deductorMasterTan", deductorMasterTan);
		parameters.put("mismatchCategory", mismatchCategory);
		parameters.put("batchUploadId", batchUploadId);
		String query = String.format(queries.get("get_count_provision_by_tan_batchId_mismatchCategory"));
		return namedParameterJdbcTemplate.queryForObject(query, parameters, BigInteger.class);

	}

	public long getProvisionMismatchCount(String deductorMasterTan, int year, int month) {

		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("deductorMasterTan", deductorMasterTan);
		parameters.put("year", year);
		parameters.put("month", month);

		return namedParameterJdbcTemplate.queryForObject(String.format(queries.get("get_provision_mismatch_count")),
				parameters, Long.class);
	}

	public List<ProvisionDTO> getAllProvisionMismatches(String deductorMasterTan, Integer year, Integer month) {

		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("deductorMasterTan", deductorMasterTan);
		parameters.put("year", year);
		parameters.put("month", month);

		return namedParameterJdbcTemplate.query(String.format(queries.get("get_provision_By_tan_mimatch_year")),
				parameters, new ProvisionRowMapper());
	}

	public List<MatrixDTO> getProvisionMatrix(String deductorMasterTan, Integer year, String type) {

		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("deductorMasterTan", deductorMasterTan);
		parameters.put("year", year);
		parameters.put("isTdsAmount", type.equals("tdsAmount") ? 1 : 0);

		return namedParameterJdbcTemplate.query(String.format(queries.get("get_provision_matrix")), parameters,
				new MatrixRowMapper());
	}

	public List<ProvisionDTO> getProvisionFTM(String deductorTan, int assessmentYear, int month) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("deductorMasterTan", deductorTan);
		parameters.put("year", assessmentYear);
		parameters.put("month", month);

		return namedParameterJdbcTemplate.query(String.format(queries.get("get_provision_ftm")), parameters,
				new ProvisionRowMapper());

	}

	public List<MatrixDTO> getProvisionClosingMatrixReport(String deductorMasterTan, Integer year, Integer month) {

		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("deductorMasterTan", deductorMasterTan);
		parameters.put("year", year);
		parameters.put("month", month);

		return namedParameterJdbcTemplate.query(
				String.format(queries.get("get_provision_closing_and_opening_matrix_report")), parameters,
				new MatrixReportsRowMapper());
	}

	public List<ProvisionDTO> getProvisionLineItemsByReceiptSerialNoAndBSRCodeAndReceiptDate(Integer assessmentYear,
			String deductorTan, boolean challanPaid, boolean isForNonResidents, String bsrCode, String receiptSerailNo,
			String receiptDate) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("deductorTan", deductorTan);
		parameters.put("challanPaid", challanPaid);
		parameters.put("isForNonResidents", isForNonResidents ? "Y" : "N");
		parameters.put("bsrCode", bsrCode);
		parameters.put("receiptSerailNo", receiptSerailNo);
		parameters.put("receiptDate", receiptDate);
		return namedParameterJdbcTemplate.query(
				String.format(queries.get("get_ProvisionLineItems_ReceiptSerialNo_And_BSRCode_And_ReceiptDate")),
				parameters, new ProvisionRowMapper());

	}

	public List<ProvisionDTO> getProvisionDetailsByTanActiveAndMismatchCategory(int assessmentYear, int month,
			String deductorMasterTan, String mismatchCategory, MismatchesFiltersDTO filters) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("month", month);
		parameters.put("deductorMasterTan", deductorMasterTan);
		parameters.put("mismatchCategory", mismatchCategory);

		String query = String.format(queries.get("get_provision_By_tan_mimatch"));

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

		String paginationOrder = CommonUtil.getPagination("provision_id", filters.getPagination().getPageNumber(),
				filters.getPagination().getPageSize(), "DESC");
		String paginatedQuery = query + paginationOrder;
		return namedParameterJdbcTemplate.query(paginatedQuery, parameters, new ProvisionRowMapper());

	}

	public BigInteger getProvisionDetailsCountByTanMismatchCategory(int assessmentYear, int month,
			String deductorMasterTan, String mismatchCategory, MismatchesFiltersDTO filters) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("month", month);
		parameters.put("deductorMasterTan", deductorMasterTan);
		parameters.put("mismatchCategory", mismatchCategory);

		String query = String.format(queries.get("get_provision_By_tan_mimatch_count"));

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

	/**
	 * 
	 * @param provisionId
	 * @return
	 */
	public List<ProvisionDTO> findByProvisionId(Integer provisionId) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("provisionId", provisionId);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_provision_by_provisinId")), parameters,
				new ProvisionRowMapper());
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param tan
	 * @param id
	 * @return
	 */
	public List<ProvisionDTO> findByYearTanId(int assessmentYear, String tan, Integer id) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("year", assessmentYear);
		parameters.put("tan", tan);
		parameters.put("id", id);
		return namedParameterJdbcTemplate.query(String.format(queries.get("find_provision_By_Year_Tan_Id")), parameters,
				new ProvisionRowMapper());
	}

	/**
	 * 
	 * @param tans
	 * @param firstDate
	 * @param lastDate
	 * @param b
	 * @return
	 */
	public long getCountOfTdsCalculationsOfProvisionForCurrentMonth(String tans, String firstDate, String lastDate,
			boolean mismatch) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("tans", tans);
		parameters.put("firstDate", firstDate);
		parameters.put("lastDate", lastDate);
		parameters.put("mismatch", mismatch == true ? 1 : 0);
		return namedParameterJdbcTemplate.queryForObject(String.format(queries.get("get_count_tds_calations")),
				parameters, Long.class);
	}

	/**
	 * 
	 * @param year
	 * @param month
	 * @param tan
	 * @param residentType
	 * @param pagination
	 * @return
	 */
	public List<ProvisionDTO> findAllResidentAndNonResident(int year, int month, String tan, String residentType,
			Pagination pagination) {
		String paginationOrder = CommonUtil.getPagination("provision_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("tan", tan);
		parameters.put("year", year);
		parameters.put("month", month);
		parameters.put("residentType", residentType);
		String query = StringUtils.EMPTY;
		if ("Y".equalsIgnoreCase(residentType)) {
			query = String.format(queries.get("get_all_res_nr"));
		} else {
			query = String.format(queries.get("active_get_all_res_nr"));

		}
		query = query.concat(paginationOrder);
		return namedParameterJdbcTemplate.query(query, parameters, new ProvisionRowMapper());
	}

	/**
	 * 
	 * @param year
	 * @param month
	 * @param tan
	 * @param residentType
	 * @return
	 */
	public BigInteger findAllResidentAndNonResidentCount(int year, int month, String tan, String residentType) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("year", year);
		parameters.put("month", month);
		parameters.put("tan", tan);
		parameters.put("residentType", residentType);
		String query = StringUtils.EMPTY;
		if ("Y".equalsIgnoreCase(residentType)) {
			query = String.format(queries.get("res_nr_count"));
		} else {
			query = String.format(queries.get("active_res_nr_count"));
		}
		return namedParameterJdbcTemplate.queryForObject(query, parameters, BigInteger.class);

	}

	/**
	 * 
	 * @param year
	 * @param month
	 * @param tan
	 * @param residentType
	 * @param pagination
	 * @param deducteeName
	 * @return
	 */
	public List<ProvisionDTO> findAllResidentAndNonResidentByDeductee(int year, int month, String tan,
			String residentType, Pagination pagination, String deducteeName) {
		String paginationOrder = CommonUtil.getPagination("provision_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("tan", tan);
		parameters.put("year", year);
		parameters.put("month", month);
		parameters.put("residentType", residentType);
		parameters.put("deducteeName", "%" + deducteeName + "%");
		String query = StringUtils.EMPTY;
		if ("Y".equalsIgnoreCase(residentType)) {
			query = String.format(queries.get("get_all_res_nr_deductees"));
		} else {
			query = String.format(queries.get("active_get_all_res_nr_deductees"));

		}
		query = query.concat(paginationOrder);
		return namedParameterJdbcTemplate.query(query, parameters, new ProvisionRowMapper());
	}

	/**
	 * 
	 * @param year
	 * @param month
	 * @param tan
	 * @param residentType
	 * @param deducteeName
	 * @return
	 */
	public BigInteger findAllResidentAndNonResidentByDeducteeCount(int year, int month, String tan, String residentType,
			String deducteeName) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("year", year);
		parameters.put("month", month);
		parameters.put("tan", tan);
		parameters.put("residentType", residentType);
		parameters.put("deducteeName", "%" + deducteeName + "%");
		String query = StringUtils.EMPTY;
		if ("Y".equalsIgnoreCase(residentType)) {
			query = String.format(queries.get("deductee_res_nr_count"));
		} else {
			query = String.format(queries.get("active_deductee_res_nr_count"));
		}
		return namedParameterJdbcTemplate.queryForObject(query, parameters, BigInteger.class);
	}

	/**
	 * 
	 * @param deductorTan
	 * @param deducteeKey
	 * @return
	 */
	public BigInteger findProvisionsByDeducteePanCode(String deductorTan, String deducteeKey) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorTan", deductorTan);
		parameters.put("deducteeKey", deducteeKey);
		String query = String.format(queries.get("provision_pan_code_count"));

		return namedParameterJdbcTemplate.queryForObject(query, parameters, BigInteger.class);
	}

	public BigInteger getProvisionsByYearDeducteeKeyDocumentNo(int assessmentYear, String deductorTan,
			String deducteeKey, String erpDocumentNo, String invoiceNumber, Date documentPostingDate) {
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

		String query = String.format(queries.get("provision_year_pan_name_tan_doc_count"));

		return namedParameterJdbcTemplate.queryForObject(query, parameters, BigInteger.class);

	}

	public List<ProvisionDTO> getProvisionWithInterestComputed(String tan, Integer year, Integer month,
			String deducteeName, String residentType) {
		logger.info("DAO method excuting to get the interest records with year =" + year + ",month=" + month + ",tan="
				+ tan + ",deductee name=" + deducteeName + ",Resident type=" + residentType);
		String query = String.format(queries.get("get_provision_with_interest_computed"));
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
		return namedParameterJdbcTemplate.query(query, parameter, new ProvisionRowMapper());
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
	public List<ProvisionDTO> getProvisionWithInterestComputedWithPagination(String tan, Integer year, Integer month,
			String deducteeName, String residentType, Pagination pagination) {
		logger.info("DAO method excuting to get the interest records with year =" + year + ",month=" + month + ",tan="
				+ tan + ",deductee name=" + deducteeName + ",Resident type=" + residentType);
		String paginationOrder = CommonUtil.getPagination("provision_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");
		String query = String.format(queries.get("get_provision_with_interest_computed"));
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
		return namedParameterJdbcTemplate.query(query, parameter, new ProvisionRowMapper());
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
	public BigInteger getCountOfProvisionWithInterestComputed(String tan, Integer year, Integer month,
			String deducteeName, String residentType) {
		logger.info("DAO method excuting to get the interest records count with year =" + year + ",month=" + month
				+ ",tan=" + tan + ",deductee name=" + deducteeName + ",Resident type=" + residentType);
		String query = String.format(queries.get("get_count_of_provision_with_interest_computed"));
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
	public List<ProvisionDTO> getProvisionsWithInterestById(Integer id) {
		logger.info("DAO method excuting to get the interest records with id =" + id);
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("id", id);
		return namedParameterJdbcTemplate.query(
				String.format(queries.get("get_provision_with_interest_computed_by_provision_id")), parameter,
				new ProvisionRowMapper());
	}

	// to update the advance records
	public int updateProvisionInterest(ProvisionDTO dto) {
		int status = 0;
		logger.info("Updating the Provision for Interest{}");
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(dto);
		status = namedParameterJdbcTemplate.update(queries.get("provision_interest_update"), namedParameters);
		if (status != 0) {
			logger.info("Provision updated successfully with final amount as{}" + dto.getFinalTdsAmount());
		}
		return status;
	}

	/**
	 * This method for provision mismatch batch update
	 * 
	 * @param provisionList
	 */
	@org.springframework.transaction.annotation.Transactional
	public void batchUpdateProvisionMismatch(List<ProvisionDTO> provisionList) {
		String updateQuery = "UPDATE Transactions.provision SET action = :actionType, reason = :reason,"
				+ " final_tds_rate = :finalTdsRate , final_tds_section = :finalTdsSection,"
				+ " final_tds_amount = :finalTdsAmount, mismatch = :mismatch, active =:active ,"
				+ " surcharge = :surcharge, cess_amount = :cessAmount, cess_rate = :cessRate,"
				+ " interest = :interest, error_reason = :errorReason, is_exempted = :isExempted, "
				+ " provision_groupid = :provisionGroupid, provision_npid = :provisionNpId, ldc_certificate_number =:ldcCertificateNumber, "
				+ " has_ldc=:hasLdc WHERE provision_id = :id ; ";

		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(provisionList);
		namedParameterJdbcTemplate.batchUpdate(updateQuery, batch);
		logger.info("provision batch updated successfully {}", provisionList.size());

	}

	/**
	 * to get the error records from invoice line item table
	 * 
	 * @param tan
	 * @param assessemtYear
	 * @param batchUploadId
	 * @return
	 */
	public List<ProvisionDTO> getProvisionErrorRecords(String tan, Integer assessemtYear, Integer batchUploadId) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("year", assessemtYear);
		parameters.put("tan", tan);
		parameters.put("batchUploadId", batchUploadId);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_provision_error_records")), parameters,
				new ProvisionRowMapper());
	}

	/**
	 * 
	 * @param tan
	 * @param assessmentYear
	 * @param assessmentMonth
	 * @param deductorPan
	 * @return
	 */
	public List<ProvisionDTO> getUpdatedProvisionRecords(String tan, int assessmentYear, int assessmentMonth,
			String deductorPan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("year", assessmentYear);
		parameters.put("tan", tan);
		parameters.put("deductorPan", deductorPan);
		parameters.put("month", assessmentMonth);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_update_mismatch_provision_data")),
				parameters, new ProvisionRowMapper());
	}

	/**
	 * 
	 * @param provisionId
	 * @return
	 */
	public List<ProvisionDTO> getProvisionBasedOnIds(List<Integer> provisionIds, String tan, String deductorPan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("provisionIds", provisionIds);
		parameters.put("tan", tan);
		parameters.put("deductorPan", deductorPan);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_provision_by_provisinIds")), parameters,
				new ProvisionRowMapper());
	}

	/**
	 * This method for batch save
	 * 
	 * @param provisionBatchSave
	 */
	@org.springframework.transaction.annotation.Transactional
	public void provisionBatchSave(List<ProvisionDTO> provisionBatchSave) {
		String query = " INSERT INTO Transactions.provision(assessment_year, deductor_master_tan, assessment_month, challan_month, provisional_amount, deductor_pan ,deductee_key, deductee_code,"
				+ " deductee_name,document_type, is_resident, deductee_pan,supply_type,document_date,document_number,line_item_number,provision_npid ,provision_groupid ,active,"
				+ " is_parent ,is_exempted ,challan_paid ,approved_for_challan ,is_challan_generated ,created_by ,created_date ,modified_by ,modified_date,"
				+ " posting_date_of_document,mismatch ,under_threshold ,withholding_section ,derived_tds_section ,final_tds_section ,withholding_rate ,derived_tds_rate,"
				+ " final_tds_rate,withholding_amount ,derived_tds_amount ,final_tds_amount, batch_upload_id, is_initial_record, nr_transactions_meta_id, processed_from,source_identifier,"
				+ " [section], rate, is_error, confidence, service_description, service_description_po, service_description_gl, interest, surcharge, cess_rate, po_date, po_number,"
				+ " deductee_tin, tds_rate, mismatch_category, ldc_certificate_number, has_ldc, tds_deduction_date, payment_date, has_dtaa, action, client_effective_tds_rate)"
				+ " VALUES(:assessmentYear,:deductorMasterTan,:assessmentMonth,:challanMonth, :provisionalAmount, :deductorPan,:deducteeKey,:deducteeCode,"
				+ " :deducteeName,:documentType,:isResident, :deducteePan,:supplyType,:documentDate,:documentNumber,:lineItemNumber,:provisionNpId,:provisionGroupid,:active,"
				+ " :isParent,:isExempted,:challanPaid,:approvedForChallan,:isChallanGenerated,:createdBy,:createdDate,:modifiedBy,:modifiedDate,"
				+ " :postingDateOfDocument,:mismatch,:underThreshold,:withholdingSection,:derivedTdsSection,:finalTdsSection,:withholdingRate,:derivedTdsRate,"
				+ " :finalTdsRate,:withholdingAmount,:derivedTdsAmount,:finalTdsAmount,:batchUploadId,:isInitialRecord, :nrTransactionsMetaId, :processedFrom, :sourceIdentifier,"
				+ " :section, :rate, :isError, :confidence, :serviceDescription, :serviceDescriptionPo, :serviceDescriptionGl, :interest, :surcharge, :cessRate,"
				+ " :poDate, :poNumber, :deducteeTin, :tdsRate, :mismatchCategory, :ldcCertificateNumber, :hasLdc, :tdsDeductionDate, :paymentDate, :hasDtaa, :actionType, :clientEffectiveTdsRate)";
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(provisionBatchSave);
		namedParameterJdbcTemplate.batchUpdate(query, batch);
		logger.info("provision batch inserted size is {}", provisionBatchSave.size());
	}

	public int updateProvisionAncestorId(Integer batchId, String deductorTan) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("batchId", batchId);
		parameters.put("tan", deductorTan);
		String query = "UPDATE Transactions.provision SET ancestor_id = provision_id WHERE batch_upload_id =:batchId and deductor_master_tan =:tan";
		return namedParameterJdbcTemplate.update(query, parameters);
	}
	
	public int updateProvisionMetaNrId(Integer batchId, String deductorTan) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("batchId", batchId);
		parameters.put("tan", deductorTan);
		String query = "UPDATE P" + " SET nr_transactions_meta_id = NM.id"
				+ " FROM Transactions.nr_transactions_meta NM" + " INNER JOIN Transactions.provision P "
				+ " ON P.document_number = NM.erp_document_no AND P.line_item_number = NM.line_item_number AND"
				+ " P.document_type = NM.document_type AND P.supply_type = NM.supply_type AND P.deductor_pan = NM.deductor_pan AND "
				+ " P.deductor_master_tan = NM.deductor_master_tan AND P.posting_date_of_document = NM.document_posting_date AND"
				+ " P.batch_upload_id = NM.batch_upload_id AND P.assessment_year = NM.assessment_year"
				+ " WHERE NM.batch_upload_id = :batchId and NM.deductor_master_tan =:tan";
		return namedParameterJdbcTemplate.update(query, parameters);
	}

}
