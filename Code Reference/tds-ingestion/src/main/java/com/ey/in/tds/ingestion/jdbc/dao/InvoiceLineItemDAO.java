package com.ey.in.tds.ingestion.jdbc.dao;

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
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.AdvanceDTO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.InvoiceLineItem;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.InvoiceStagging;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.MismatchesCountDTO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.ProvisionDTO;
import com.ey.in.tds.common.domain.transactions.jdbc.rowmapper.AdvanceRowMapper;
import com.ey.in.tds.common.domain.transactions.jdbc.rowmapper.InvoiceLineItemRowMapper;
import com.ey.in.tds.common.domain.transactions.jdbc.rowmapper.NrTransactionsMetaRowMapper;
import com.ey.in.tds.common.domain.transactions.jdbc.rowmapper.ProvisionRowMapper;
import com.ey.in.tds.common.dto.CommonDTO;
import com.ey.in.tds.common.dto.invoice.InvoiceMismatchByBatchIdDTO;
import com.ey.in.tds.common.dto.invoice.MismatchesFiltersDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeducteeMasterNonResidential;
import com.ey.in.tds.common.onboarding.jdbc.dto.NrTransactionsMeta;
import com.ey.in.tds.common.repository.common.PagedData;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.ingestion.jdbc.rowmapper.InvoiceStaggingRowMapper;
import com.ey.in.tds.ingestion.jdbc.rowmapper.MismatchCountRowMapper;

/**
 * contains logic to perform DB operations with invoice_line_item table
 * 
 * @author scriptbees
 *
 */
@Repository
public class InvoiceLineItemDAO {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private HashMap<String, String> queries;

	@Autowired
	private DataSource dataSource;

	private SimpleJdbcInsert simplejdbcInsert;

	private NamedParameterJdbcTemplate namedJdbcTemplate;

	@PostConstruct
	private void postConstruct() {
		namedJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		simplejdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("invoice_line_item")
				.withSchemaName("Transactions").usingGeneratedKeyColumns("invoice_line_item_id");
	}

	/**
	 * This method for update invoice_line_item table
	 * 
	 * @param dto
	 * @return
	 */
	// TODO: need to check the method is working properly or not
	public int update(InvoiceLineItem dto) {
		int status = 0;
		logger.info("Updating the Invoice {}");
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(dto);
		status = namedJdbcTemplate.update(queries.get("update_Invoice_line_item"), namedParameters);
		if (status != 0) {
			logger.info("Invoice updated successfully {}");
		}
		return status;
	}

	// to update the invoice records
	public int updateInvoiceMismatch(InvoiceLineItem dto) {
		int status = 0;
		logger.info("Updating the Invoice {}");
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(dto);
		status = namedJdbcTemplate.update(queries.get("invoice_mismatch_update"), namedParameters);
		if (status != 0) {
			logger.info("Invoice updated successfully {}");
		}
		return status;
	}

	// to update the invoice records
	public int updateInvoiceInterest(InvoiceLineItem dto) {
		int status = 0;
		logger.info("Updating the Invoice for Interest{}");
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(dto);
		status = namedJdbcTemplate.update(queries.get("invoice_interest_update"), namedParameters);
		if (status != 0) {
			logger.info("Invoice updated successfully with final amount as{}" + dto.getFinalTdsAmount());
		}
		return status;
	}

	/**
	 * This method for save invoice_line_item table
	 * 
	 * @param dto
	 * @return
	 */
	public InvoiceLineItem save(InvoiceLineItem dto) {
		logger.info("insert invoice method execution started  {}");

		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(dto);
		dto.setId(simplejdbcInsert.executeAndReturnKey(namedParameters).intValue());

		logger.info("Record inserted to invoice line item table {}");
		return dto;
	}

	/**
	 * 
	 * @param deductorMasterTan
	 * @param assessmentYear
	 * @param assessmentMonth
	 * @return
	 */
	public List<InvoiceLineItem> findAll(String deductorMasterTan, Integer assessmentYear, Integer assessmentMonth) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("deductorMasterTan", deductorMasterTan);
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("assessmentMonth", assessmentMonth);
		return namedJdbcTemplate.query(String.format(queries.get("find_by_tan_asssesmentYear")), parameters,
				new InvoiceLineItemRowMapper());
	}

	/**
	 * Method to fetch all records based for update and should not be changed to
	 * restricted projections
	 * 
	 * @param deductorName
	 * @param pagination
	 * @return Invoice line items paged data
	 */
	public List<InvoiceLineItem> getInvoicesByDeducteeNamePan(String deducteeName, String deducteePan,
			Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("deducteeName", deducteeName);
		parameters.put("deducteePan", deducteePan);
		return namedJdbcTemplate.query(String.format(queries.get("find_by_deducteename_pan")), parameters,
				new InvoiceLineItemRowMapper());

	}

	/**
	 * All Records based on Tan
	 * 
	 * @param assessmentYear
	 * @param challanMonths
	 * @param deductorTan
	 * @param challanPaid
	 * @param isForNonResidents
	 * @param pagination
	 * @return
	 */
	public List<InvoiceLineItem> getInvoiceLineItemsByAssessmentYearChallanMonthDeductorTan(Integer assessmentYear,
			List<Integer> challanMonths, List<String> deductorTan, boolean challanPaid, boolean isForNonResidents,
			Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("challanMonths", challanMonths.toString().replace("[", "").replace("]", ""));
		parameters.put("deductorTan", deductorTan.toString().replace("[", "").replace("]", ""));
		parameters.put("challanPaid", challanPaid == true ? 1 : 0);
		parameters.put("isForNonResidents", isForNonResidents == true ? "Y" : "N");

		return namedJdbcTemplate.query(String.format(queries.get("find_by_year_tan-challanmontj_isresident_active")),
				parameters, new InvoiceLineItemRowMapper());

	}

	/**
	 * 
	 * @param batchId
	 * @param mismatchType
	 * @param tan
	 * @param pagination
	 * @return
	 */
	public List<InvoiceLineItem> getInvoiceMismatchesByAssessmentYearAssessmentMonthBatchIdAndMismatchcategory(
			Integer batchId, String mismatchType, List<String> tan, Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String paginationOrder = CommonUtil.getPagination("invoice_line_item_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("batchId", batchId);
		parameters.put("mismatchType", mismatchType);
		parameters.put("tan", tan.toString().replace("[", "").replace("]", ""));
		String query = String.format(queries.get("find_by_year_tan_batchId_mismatch_keyDuplicate_isParent"));
		query = query.concat(paginationOrder);
		return namedJdbcTemplate.query(query, parameters, new InvoiceLineItemRowMapper());

	}

	public BigInteger getCountOfInvoiceMismatchesByAssessmentYearAssessmentMonthBatchIdAndMismatchcategory(
			Integer batchId, String mismatchType, List<String> tan, Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("batchId", batchId);
		parameters.put("mismatchType", mismatchType);
		parameters.put("tan", tan.toString().replace("[", "").replace("]", ""));
		return namedJdbcTemplate.queryForObject(
				String.format(queries.get("count_find_by_year_tan_batchId_mismatch_keyDuplicate_isParent")), parameters,
				BigInteger.class);

	}

	/**
	 * 
	 * @param tan
	 * @param year
	 * @param id
	 * @param activeFlag
	 * @return
	 */
	public List<InvoiceLineItem> findById(String tan, Integer year, Integer id, Boolean activeFlag) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("tan", tan.trim());
		parameters.put("year", year);
		parameters.put("id", id);
		parameters.put("activeFlag", activeFlag ? 1 : 0);

		return namedJdbcTemplate.query(String.format(queries.get("find_by_year_tan_id_active")), parameters,
				new InvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param deductorTan
	 * @param challanPaid
	 * @param isForNonResidents
	 * @param bsrCode
	 * @param receiptSerailNo
	 * @param receiptDate
	 * @param isForm16
	 * @return
	 */
	public List<InvoiceLineItem> getInvoiceLineItemsByReceiptSerialNoAndBSRCodeAndReceiptDate(Integer assessmentYear,
			String deductorTan, boolean challanPaid, boolean isForNonResidents, String bsrCode, String receiptSerailNo,
			String receiptDate, boolean isForm16) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("deductorTan", deductorTan);
		parameters.put("bsrCode", bsrCode);
		parameters.put("receiptSerailNo", receiptSerailNo);
		parameters.put("receiptDate", receiptDate);
		parameters.put("isForNonResidents", isForNonResidents == true ? "Y" : "N");
		parameters.put("challanPaid", challanPaid);
		String query = "";
		if (isForm16) {
			query = String.format(queries.get("top_3_invoices_by_serialNo_bsrcode_receiptdate"));
		} else {
			query = String.format(queries.get("invoice_By_ReceiptSerialNo__dBSRCode_ReceiptDate"));
		}
		return namedJdbcTemplate.query(query, parameters, new InvoiceLineItemRowMapper());

	}

	/**
	 * This method to get the count
	 * 
	 * @param assessmentYear
	 * @param deductorTan
	 * @param challanPaid
	 * @param isForNonResidents
	 * @param bsrCode
	 * @param receiptSerailNo
	 * @param receiptDate
	 * @return
	 */
	public BigInteger getTotalCountByAssessmentYearAndBSRCode(Integer assessmentYear, String deductorTan,
			boolean challanPaid, boolean isForNonResidents, String bsrCode, String receiptSerailNo,
			String receiptDate) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String residentIndicator = isForNonResidents ? "Y" : "N";
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("deductorTan", deductorTan);
		parameters.put("bsrCode", bsrCode);
		parameters.put("receiptSerailNo", receiptSerailNo);
		parameters.put("receiptDate", receiptDate);
		parameters.put("residentIndicator", residentIndicator);
		parameters.put("challanPaid", challanPaid);
		return namedJdbcTemplate.queryForObject(
				String.format(queries.get("invoice_By_ReceiptSerialNo__dBSRCode_ReceiptDate")), parameters,
				BigInteger.class);

	}

	/**
	 * 
	 * @param assessmentYear
	 * @param challanMonths
	 * @param deductorTan
	 * @param challanPaid
	 * @param isForNonResidents
	 * @return
	 */
	public BigInteger getTotalCountByyAssessmentYearChallanMonthDeductorTan(Integer assessmentYear,
			List<Integer> challanMonths, String deductorTan, boolean challanPaid, boolean isForNonResidents) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String residentIndicator = isForNonResidents ? "Y" : "N";
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("deductorTan", deductorTan);
		parameters.put("challanMonths", challanMonths.toString().replace("[", "").replace("]", ""));
		parameters.put("residentIndicator", residentIndicator);
		parameters.put("challanPaid", challanPaid);
		return namedJdbcTemplate.queryForObject(
				String.format(queries.get("invoice_count_By_ReceiptSerialNo__challanMonth")), parameters,
				BigInteger.class);

	}

	/**
	 * All Mismatch Records based on Tan
	 * 
	 * @param tan
	 * @param year
	 * @param month
	 * @return
	 */
	public List<InvoiceLineItem> getAllInvoiceMismatches(String tan, int year, int month) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("year", year);
		parameters.put("tan", tan);
		parameters.put("month", month);
		return namedJdbcTemplate.query(String.format(queries.get("get_All_Invoice_Mismatches")), parameters,
				new InvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param tan
	 * @param year
	 * @param month
	 * @return
	 */
	public long getAllInvoiceMismatchesCount(String tan, int year, int month) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("year", year);
		parameters.put("tan", tan);
		parameters.put("month", month);
		return namedJdbcTemplate.queryForObject(String.format(queries.get("get_count_Invoice_Mismatches")), parameters,
				Long.class);
	}

	/**
	 * All Mismatch Records based on Tan
	 * 
	 * @param deductorTan
	 * @param year
	 * @param month
	 * @param pagination
	 * @return
	 */
	public List<InvoiceLineItem> getAllInvoiceMismatchesPage(String deductorTan, int year, int month,
			Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("year", year);
		parameters.put("tan", deductorTan);
		parameters.put("month", month);
		return namedJdbcTemplate.query(String.format(queries.get("get_All_InvoiceMismatches_Page")), parameters,
				new InvoiceLineItemRowMapper());
	}

	/**
	 * all mismatches query based on TAN and Type
	 * 
	 * @param columnName
	 * @param assessmentYear
	 * @param assessmentMonth
	 * @param tan
	 * @param type
	 * @return
	 */
	public MismatchesCountDTO getTotalValueByYearAndMonth(int assessmentYear, int assessmentMonth, String tan,
			String type) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("assessmentMonth", assessmentMonth);
		parameters.put("tan", tan);
		parameters.put("type", type);
		return namedJdbcTemplate.queryForObject(String.format(queries.get("get_mismatch_count_by_year_and_month")),
				parameters, new MismatchCountRowMapper());
	}

	private MismatchesCountDTO getTotalValueAndCountByBatchId(String tan, Integer batchId, String type) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("tan", tan);
		parameters.put("batchId", batchId);
		parameters.put("type", type);

		return namedJdbcTemplate.queryForObject(String.format(queries.get("get_mismatch_count_by_batchid")), parameters,
				new MismatchCountRowMapper());

	}

	/**
	 * 
	 * @param assessmentYear
	 * @param assessmentMonth
	 * @param tan
	 * @param systemChallanSerailNo
	 * @return
	 */
	public double getTotalValueBySystemChallanSerialNo(int assessmentYear, int assessmentMonth, String tan,
			String systemChallanSerailNo) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("assessmentMonth", assessmentMonth);
		parameters.put("tan", tan);
		parameters.put("systemChallanSerailNo", systemChallanSerailNo);
		return namedJdbcTemplate.queryForObject(String.format(queries.get("get_TotalValue_By_SystemChallanSerialNo")),
				parameters, Double.class);
	}

	/**
	 * 
	 * @param tan
	 * @param type
	 * @param year
	 * @param month
	 * @param isMismatch
	 * @return
	 */
	public List<InvoiceLineItem> getInvoiceDeductees(String tan, String type, int year, int month, boolean isMismatch) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("assessmentYear", year);
		parameters.put("month", month);
		parameters.put("tan", tan);
		parameters.put("type", type);
		parameters.put("isMismatch", isMismatch);
		String query = queries.get("get_Invoice_Deductees");
		if ("N".equalsIgnoreCase(type) && (!isMismatch)) {
			query += " AND active = 1 ;";

		} else if (isMismatch) {
			query += " and active = 0 and has_mismatch = 1 ;";

		}
		return namedJdbcTemplate.query(query, parameters, new InvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param assessmentMonth
	 * @param tan
	 * @param batchId
	 * @param type
	 * @return
	 */
	public InvoiceMismatchByBatchIdDTO getInvoiceMismatchSummary(int assessmentYear, int assessmentMonth, String tan,
			Integer batchId, String type) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		InvoiceMismatchByBatchIdDTO invoiceMismatchByBatchIdDTO = new InvoiceMismatchByBatchIdDTO();

		invoiceMismatchByBatchIdDTO.setId(batchId);
		invoiceMismatchByBatchIdDTO.setMismatchcategory(type);
		MismatchesCountDTO mismatchesCountDTO = null;
		if (batchId != null) {
			mismatchesCountDTO = getTotalValueAndCountByBatchId(tan, batchId, type);
			invoiceMismatchByBatchIdDTO.setInvoiceValue(mismatchesCountDTO.getAmount().setScale(2, RoundingMode.UP));
			invoiceMismatchByBatchIdDTO
					.setTdsSystemAmount(mismatchesCountDTO.getDerivedTdsAmount().setScale(2, RoundingMode.UP));
			invoiceMismatchByBatchIdDTO
					.setTdsClientAmount(mismatchesCountDTO.getActualTdsAmount().setScale(2, RoundingMode.UP));
			invoiceMismatchByBatchIdDTO.setTotalRecords(mismatchesCountDTO.getTotalCount());
		} else {
			mismatchesCountDTO = getTotalValueByYearAndMonth(assessmentYear, assessmentMonth, tan, type);
			invoiceMismatchByBatchIdDTO.setInvoiceValue(mismatchesCountDTO.getAmount().setScale(2, RoundingMode.UP));
			invoiceMismatchByBatchIdDTO
					.setTdsSystemAmount(mismatchesCountDTO.getDerivedTdsAmount().setScale(2, RoundingMode.UP));
			invoiceMismatchByBatchIdDTO
					.setTdsClientAmount(mismatchesCountDTO.getActualTdsAmount().setScale(2, RoundingMode.UP));
			invoiceMismatchByBatchIdDTO.setTotalRecords(mismatchesCountDTO.getTotalCount());
		}

		if (!"NAD".equalsIgnoreCase(type)) {
			if (invoiceMismatchByBatchIdDTO.getTdsSystemAmount() != null
					&& invoiceMismatchByBatchIdDTO.getTdsClientAmount() != null) {
				if (invoiceMismatchByBatchIdDTO.getTdsSystemAmount()
						.compareTo(invoiceMismatchByBatchIdDTO.getTdsClientAmount()) < 0) {
					invoiceMismatchByBatchIdDTO.setExcessDeduction(invoiceMismatchByBatchIdDTO.getTdsSystemAmount()
							.subtract(invoiceMismatchByBatchIdDTO.getTdsClientAmount()).setScale(2, RoundingMode.UP));
				} else {
					invoiceMismatchByBatchIdDTO.setShortDeduction(invoiceMismatchByBatchIdDTO.getTdsSystemAmount()
							.subtract(invoiceMismatchByBatchIdDTO.getTdsClientAmount()).setScale(2, RoundingMode.UP));
				}
			}
		} else {
			invoiceMismatchByBatchIdDTO.setExcessDeduction(new BigDecimal(0));
			invoiceMismatchByBatchIdDTO.setShortDeduction(new BigDecimal(0));
		}
		return invoiceMismatchByBatchIdDTO;
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param assessmentMonth
	 * @param tan
	 * @param resType
	 * @param fileType
	 * @param deducteeName
	 * @param pagination
	 * @return
	 */
	public List<InvoiceLineItem> findAllResidentAndNonResidentByDeductee(int assessmentYear, int assessmentMonth,
			String tan, String resType, String fileType, String deducteeName, Pagination pagination) {
		String paginationOrder = CommonUtil.getPagination("invoice_line_item_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("assessmentMonth", assessmentMonth);
		parameters.put("tan", tan);
		parameters.put("resType", resType);
		parameters.put("fileType", fileType);
		parameters.put("deducteeName", deducteeName);
		String query = String.format(queries.get("find_all_invoices"));
		if ("N".equalsIgnoreCase(resType)) {
			query = query.concat(" AND active = 1");
		}
		if (!"nodeducteefilter".equalsIgnoreCase(deducteeName)) {
			query = query.concat(" AND deductee_name like '%" + deducteeName + "%'");
		}
		query = query.concat(paginationOrder);
		return namedJdbcTemplate.query(query, parameters, new InvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param assessmentMonth
	 * @param tan
	 * @param resType
	 * @param deducteeName
	 * @param fileType
	 * @return
	 */
	public BigInteger findAllResidentAndNonResidentByDeducteeCount(int assessmentYear, int assessmentMonth, String tan,
			String resType, String deducteeName, String fileType) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("assessmentMonth", assessmentMonth);
		parameters.put("tan", tan);
		parameters.put("resType", resType);
		parameters.put("fileType", fileType);
		parameters.put("deducteeName", deducteeName);
		String query = String.format(queries.get("find_all_invoices_count"));
		if ("N".equalsIgnoreCase(resType)) {
			query = query.concat(" AND active = 1");
		}
		if (!"nodeducteefilter".equalsIgnoreCase(deducteeName)) {
			query = query.concat(" AND deductee_name like '%" + deducteeName + "%'");
		}
		return namedJdbcTemplate.queryForObject(query, parameters, BigInteger.class);
	}

	/**
	 * 
	 * @param tan
	 * @param batchId
	 * @param processedFrom
	 * @param type
	 * @return
	 */
	public List<InvoiceLineItem> getSAPInvoiceMismatchesByBatchIdType(String tan, Integer batchId, String processedFrom,
			String type) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tan", tan);
		parameters.put("batchId", batchId);
		parameters.put("processedFrom", processedFrom);
		parameters.put("type", type);
		return namedJdbcTemplate.query(String.format(queries.get("get_SAP_InvoiceMismatches_By_BatchIdType")),
				parameters, new InvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param assessmentMonth
	 * @param tan
	 * @param processedFrom
	 * @param type
	 * @return
	 */
	public List<InvoiceLineItem> getSAPInvoiceMismatchesByAssessmentYearAssessmentMonthTanType(Integer assessmentYear,
			Integer assessmentMonth, String tan, String processedFrom, String type) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("assessmentMonth", assessmentMonth);
		parameters.put("tan", tan);
		parameters.put("processedFrom", processedFrom);
		parameters.put("type", type);
		return namedJdbcTemplate.query(
				String.format(queries.get("get_SAP_InvoiceMismatches_By_AssessmentYear_AssessmentMonth_Tan_Type")),
				parameters, new InvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param batchId
	 * @param mismatchType
	 * @param tan
	 * @param processedFrom
	 * @return
	 */
	public List<InvoiceLineItem> getSAPInvoiceMismatchesByAssessmentYearAssessmentMonthBatchIdAndMismatchcategory(
			Integer batchId, String mismatchType, String tan, String processedFrom) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("batchId", batchId);
		parameters.put("mismatchType", mismatchType);
		parameters.put("tan", tan);
		parameters.put("processedFrom", processedFrom);
		return namedJdbcTemplate.query(
				String.format(queries.get(
						"get_SAP_InvoiceMismatches_By_AssessmentYear_AssessmentMonth_BatchId_And_Mismatchcategory")),
				parameters, new InvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param assessmentMonth
	 * @param tan
	 * @param category
	 * @param processedFrom
	 * @return
	 */
	public List<InvoiceLineItem> getSAPInvoicesByAssessmentYearChallanMonthByTanMismatchCategory(Integer assessmentYear,
			Integer assessmentMonth, String tan, String category, String processedFrom) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("assessmentMonth", assessmentMonth);
		parameters.put("tan", tan);
		parameters.put("category", category);
		parameters.put("processedFrom", processedFrom);
		return namedJdbcTemplate.query(
				String.format(queries.get("get_SAP_Invoices_By_AssessmentYear_ChallanMonth_By_TanMismatchCategory")),
				parameters, new InvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param challanMonth
	 * @param tan
	 * @param category
	 * @param filters
	 * @return
	 */
	public List<InvoiceLineItem> getInvoicesByAssessmentYearChallanMonthByTanMismatchCategory(Integer assessmentYear,
			Integer challanMonth, String tan, String category, MismatchesFiltersDTO filters) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("challanMonth", challanMonth);
		parameters.put("tan", tan);
		parameters.put("category", category);

		String sqlQuery = "";
		if (StringUtils.isNotBlank(filters.getDeducteeName())
				&& !"undefined".equalsIgnoreCase(filters.getDeducteeName())) {
			parameters.put("deducteeName", filters.getDeducteeName());
			sqlQuery += " AND deductee_name = :deducteeName ";
			// query.and(where("invoice_line_item_deductee_name").is(filters.getDeducteeName()));
		}
		if (StringUtils.isNotBlank(filters.getResidentType())
				&& !"undefined".equalsIgnoreCase(filters.getResidentType())) {
			// query =
			// query.and(where("invoice_line_item_resident").is(filters.getResidentType()));
			parameters.put("residentType", filters.getResidentType());
			sqlQuery += " AND resident = :residentType ";
		}
		if (StringUtils.isNotBlank(filters.getConfidence()) && !"undefined".equalsIgnoreCase(filters.getConfidence())) {
			// query =
			// query.and(where("invoice_line_item_confidence").is(filters.getConfidence()));
			parameters.put("confidence", filters.getConfidence());
			sqlQuery += " AND confidence = :confidence ";
		}
		if (StringUtils.isNotBlank(filters.getSection()) && !"undefined".equalsIgnoreCase(filters.getSection())) {
			// query =
			// query.and(where("invoice_line_item_actual_tds_section").is(filters.getSection()));
			parameters.put("section", filters.getSection());
			sqlQuery += " AND tds_section = :section ";
		}
		if (StringUtils.isNotBlank(filters.getDerivedSection())
				&& !"undefined".equalsIgnoreCase(filters.getDerivedSection())) {
			// query =
			// query.and(where("invoice_line_item_derived_tds_section").is(filters.getDerivedSection()));
			parameters.put("derrivedTdsSection", filters.getDerivedSection());
			sqlQuery += " AND derived_tds_section = :derrivedTdsSection ";
		}
		// query = query.withAllowFiltering();
		// return super.getPaginatedData(query, filters.getPagination());
		String paginationOrder = CommonUtil.getPagination("invoice_line_item_id",
				filters.getPagination().getPageNumber(), filters.getPagination().getPageSize(), "DESC");
		String paginatedQuery = queries.get("get_Invoices_By_AssessmentYear_ChallanMonth_By_TanMismatchCategory")
				+ paginationOrder;
		return namedJdbcTemplate.query(String.format(paginatedQuery, sqlQuery), parameters,
				new InvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param challanMonth
	 * @param tan
	 * @param category
	 * @param filters
	 * @return
	 */
	public BigInteger getInvoicesCountByYearMonthAndTanMismatchCategory(Integer assessmentYear, Integer challanMonth,
			String tan, String category, MismatchesFiltersDTO filters) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		// String filtersStr = "";
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("challanMonth", challanMonth);
		parameters.put("tan", tan);
		parameters.put("category", category);
		String sqlQuery = String
				.format(queries.get("get_Invoices_count_By_AssessmentYear_ChallanMonth_By_TanMismatchCategory"));
		if (StringUtils.isNotBlank(filters.getDeducteeName())
				&& !"undefined".equalsIgnoreCase(filters.getDeducteeName())) {
			// filtersStr += " and invoice_line_item_deductee_name='" +
			// filters.getDeducteeName() + "'";
			parameters.put("deducteeName", filters.getDeducteeName());
			sqlQuery += " AND deductee_name = :deducteeName ";
		}
		if (StringUtils.isNotBlank(filters.getResidentType())
				&& !"undefined".equalsIgnoreCase(filters.getResidentType())) {
			// filtersStr += " and invoice_line_item_resident='" + filters.getResidentType()
			// + "'";
			parameters.put("residentType", filters.getResidentType());
			sqlQuery += " AND resident = :residentType ";
		}
		if (StringUtils.isNotBlank(filters.getConfidence()) && !"undefined".equalsIgnoreCase(filters.getConfidence())) {
			// filtersStr += " and invoice_line_item_confidence='" + filters.getConfidence()
			// + "'";
			parameters.put("confidence", filters.getConfidence());
			sqlQuery += " AND confidence = :confidence ";
		}
		if (StringUtils.isNotBlank(filters.getSection()) && !"undefined".equalsIgnoreCase(filters.getSection())) {
			// filtersStr += " and invoice_line_item_actual_tds_section='" +
			// filters.getSection() + "'";
			parameters.put("section", filters.getSection());
			sqlQuery += " AND tds_section = :section ";
		}
		if (StringUtils.isNotBlank(filters.getDerivedSection())
				&& !"undefined".equalsIgnoreCase(filters.getDerivedSection())) {
			// filtersStr += " and invoice_line_item_derived_tds_section='" +
			// filters.getDerivedSection() + "'";
			parameters.put("derrivedTdsSection", filters.getDerivedSection());
			sqlQuery += "AND derived_tds_section = :derrivedTdsSection ";
		}
		return namedJdbcTemplate.queryForObject(sqlQuery, parameters, BigInteger.class);
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param deductorTan
	 * @param lineItemId
	 * @return
	 */
	public List<InvoiceLineItem> findByLineItemId(int assessmentYear, String deductorTan, Integer lineItemId) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("lineItemId", lineItemId);
		parameters.put("tan", deductorTan);

		return namedJdbcTemplate.query(String.format(queries.get("find_By_LineItemId")), parameters,
				new InvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param tan
	 * @param type
	 * @param year
	 * @param month
	 * @return
	 */
	public List<InvoiceLineItem> getInvoiceByType(String tan, String type, int year, int month, Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		logger.info("DAO method executing to get invoices by type  {}");
		String paginationOrder = CommonUtil.getPagination("invoice_line_item_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tan", tan);
		parameters.put("type", type);
		parameters.put("year", year);
		parameters.put("month", month);
		String query = String.format(queries.get("get_Invoice_by_Type"));
		query = query.concat(paginationOrder);

		return namedJdbcTemplate.query(query, parameters, new InvoiceLineItemRowMapper());
	}

	public List<InvoiceLineItem> getCRInvoices(String tan, String type, int year, int month, Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		logger.info("DAO method executing to get invoices by type  {}");
		String paginationOrder = CommonUtil.getPagination("invoice_line_item_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tan", tan);
		parameters.put("type", type);
		parameters.put("year", year);
		parameters.put("month", month);
		String query = String.format(queries.get("get_Invoices_by_document_Type"));
		query = query.concat(paginationOrder);

		return namedJdbcTemplate.query(query, parameters, new InvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param batchId
	 * @param glFound
	 * @param pagination
	 * @return
	 */
	public List<InvoiceLineItem> findByBatchUploadIdAndGLFound(Integer batchId, boolean glFound,
			Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("batchId", batchId);
		parameters.put("glFound", glFound == true ? 1 : 0);
		return namedJdbcTemplate.query(String.format(queries.get("find_By_BatchUploadId_And_GLFound")), parameters,
				new InvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param batchId
	 * @param glFound
	 * @return
	 */
	public BigInteger findByBatchUploadIdAndGLFoundCount(Integer batchId, boolean glFound) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("batchId", batchId);
		parameters.put("glFound", glFound == true ? 1 : 0);
		return namedJdbcTemplate.queryForObject(String.format(queries.get("find_By_BatchUploadId_And_GLFound_count")),
				parameters, BigInteger.class);
	}

	/**
	 * 
	 * @param year
	 * @param months
	 * @param tan
	 * @param glFound
	 * @param pagination
	 * @return
	 */
	public List<InvoiceLineItem> findByYearMonthTanAndGLFound(Integer year, List<Integer> months, String tan,
			boolean glFound, Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String paginationOrder = CommonUtil.getPagination("invoice_line_item_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("year", year);
		parameters.put("months", months);
		parameters.put("tan", tan);
		parameters.put("glFound", glFound == true ? 1 : 0);
		String query = String.format(queries.get("find_By_Year_Month_Tan_And_GLFound"));
		query = query.concat(paginationOrder);
		return namedJdbcTemplate.query(query, parameters, new InvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param year
	 * @param tan
	 * @param glFound
	 * @param pagination
	 * @return
	 */
	public List<InvoiceLineItem> findByYearTanAndGLFound(Integer year, String tan, boolean glFound,
			Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String paginationOrder = CommonUtil.getPagination("invoice_line_item_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("year", year);
		parameters.put("tan", tan);
		parameters.put("glFound", glFound == true ? 1 : 0);
		String query = String.format(queries.get("find_By_Year_Tan_And_GLFound"));
		query = query.concat(paginationOrder);
		return namedJdbcTemplate.query(query, parameters, new InvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param tan
	 * @param type
	 * @param year
	 * @param month
	 * @return
	 */
	public BigInteger getInvoceCountByType(String tan, String type, int year, int month) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("year", year);
		parameters.put("tan", tan);
		parameters.put("type", type);
		parameters.put("month", month);

		return namedJdbcTemplate.queryForObject(String.format(queries.get("get_Invoce_Count_By_Type")), parameters,
				BigInteger.class);

	}

	/**
	 * 
	 * @param year
	 * @param tan
	 * @return
	 */
	public List<InvoiceLineItem> invoiceLineItemBasedOnTanAndYear(Integer year, String tan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("year", year);
		parameters.put("tan", tan);
		return namedJdbcTemplate.query(String.format(queries.get("invoiceLineItem_Based_On_Tan_And_Year")), parameters,
				new InvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param year
	 * @param tan
	 * @param pagination
	 * @return
	 */
	public List<InvoiceLineItem> getAllInvoiceTanAndYear(Integer year, String tan, Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("year", year);
		parameters.put("tan", tan);
		return namedJdbcTemplate.query(String.format(queries.get("invoiceLineItem_Based_On_Tan_And_Year")), parameters,
				new InvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param tan
	 * @param year
	 * @param months
	 * @param glFound
	 * @return
	 */
	public BigInteger invoicesNotFoundInGLCountByTanYearMonth(String tan, Integer year, List<Integer> months,
			boolean glFound) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("year", year);
		parameters.put("tan", tan);
		parameters.put("glFound", glFound == true ? 1 : 0);
		if (months.isEmpty()) {
			return namedJdbcTemplate.queryForObject(
					String.format(queries.get("invoices_Not_Found_In_GLCount_By_Tan_Year")), parameters,
					BigInteger.class);
		} else {
			parameters.put("months", months);
			return namedJdbcTemplate.queryForObject(
					String.format(queries.get("invoices_Not_Found_In_GLCount_By_Tan_Year_month")), parameters,
					BigInteger.class);
		}
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param type
	 * @param tan
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public long getPdfTransactionStatus(Integer assessmentYear, String type, String tan, String startDate,
			String endDate) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("previousYear", assessmentYear - 1);
		parameters.put("currentYear", assessmentYear);
		parameters.put("nextYear", assessmentYear + 1);
		parameters.put("tan", tan);
		parameters.put("type", type);
		parameters.put("startDate", startDate);
		parameters.put("endDate", endDate);
		return namedJdbcTemplate.queryForObject(String.format(queries.get("get_Pdf_Transaction_Status")), parameters,
				Long.class);

	}

	/**
	 * 
	 * @param assessmentYear
	 * @param tan
	 * @param startDate
	 * @param endDate
	 * @param isMismatch
	 * @return
	 */
	public long getTdsCalculationStatus(Integer assessmentYear, String tan, String startDate, String endDate,
			boolean isMismatch) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("previousYear", assessmentYear - 1);
		parameters.put("currentYear", assessmentYear);
		parameters.put("nextYear", assessmentYear + 1);
		parameters.put("tan", tan);
		parameters.put("isMismatch", isMismatch == true ? 1 : 0);
		parameters.put("startDate", startDate);
		parameters.put("endDate", endDate);
		return namedJdbcTemplate.queryForObject(String.format(queries.get("get_Tds_Calculation_Status")), parameters,
				Long.class);

	}

	/**
	 * changes for invoice line item table
	 * 
	 * @param assessmentYear
	 * @param challanMonth
	 * @param tan
	 * @return
	 */
	public List<InvoiceLineItem> findInvoicesWithoutSection(int assessmentYear, int challanMonth, String tan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("tan", tan);
		parameters.put("challanMonth", challanMonth);
		return namedJdbcTemplate.query(String.format(queries.get("get_Tds_Calculation_Status")), parameters,
				new InvoiceLineItemRowMapper());

	}

	/**
	 * 
	 * @param assessmentYear
	 * @param challanMonth
	 * @param tan
	 * @param section
	 * @param approvedForChallan
	 * @param pagination
	 * @param isChallanGenerated
	 * @param isVerifyLiability
	 * @param systemChallanSerialNumber
	 * @param isChallanPaid
	 * @return
	 */
	public List<InvoiceLineItem> getInvoiceByYearMonthTanAndSection(int assessmentYear, int challanMonth, String tan,
			String section, boolean approvedForChallan, Pagination pagination, boolean isChallanGenerated,
			boolean isVerifyLiability, String systemChallanSerialNumber, boolean isChallanPaid) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("tan", tan);
		parameters.put("challanMonth", challanMonth);
		parameters.put("approvedForChallan", approvedForChallan == true ? 1 : 0);
		parameters.put("isChallanPaid", isChallanPaid == true ? 1 : 0);

		String sqlQuery = String.format(queries.get("get_Invoice_By_Year_Month_Tan_And_Section"));

		if (systemChallanSerialNumber != null) {
			parameters.put("systemChallanSerialNumber", systemChallanSerialNumber);
			sqlQuery += "AND challan_serial_no= :systemChallanSerialNumber;";
		}
		if (!isVerifyLiability) {
			parameters.put("isChallanGenerated", isChallanGenerated == true ? 1 : 0);
			sqlQuery += " AND is_challan_generated =:isChallanGenerated ;";
		}
		return namedJdbcTemplate.query(sqlQuery, parameters, new InvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param challanMonth
	 * @param tan
	 * @param approvedForChallan
	 * @param pagination
	 * @param isChallanGenerated
	 * @param isVerifyLiability
	 * @param isChallanPaid
	 * @return
	 */
	public List<InvoiceLineItem> getInvoiceRecordsWithoutSection(int assessmentYear, int challanMonth, String tan,
			boolean approvedForChallan, Pagination pagination, boolean isChallanGenerated, boolean isVerifyLiability,
			boolean isChallanPaid) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("tan", tan);
		parameters.put("challanMonth", challanMonth);
		parameters.put("approvedForChallan", approvedForChallan == true ? 1 : 0);
		parameters.put("isChallanPaid", isChallanPaid == true ? 1 : 0);

		parameters.put("isVerifyLiability", isVerifyLiability == true ? 1 : 0);

		String sqlQuery = String.format(queries.get("get_Invoice_By_Year_Month_Tan_And_Section"));

		if (!isVerifyLiability) {
			parameters.put("isChallanGenerated", isChallanGenerated == true ? 1 : 0);
			sqlQuery = sqlQuery.replace(";", "") + " AND challan_generated =:isChallanGenerated ;";
		}
		return namedJdbcTemplate.query(sqlQuery, parameters, new InvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param year
	 * @param deductorTan
	 * @param invoiceLineItemId
	 * @param documentPostingDate
	 * @return
	 */
	public List<InvoiceLineItem> findByYearPanInvoiceId(Integer year, String deductorTan, Integer invoiceLineItemId,
			Long documentPostingDate) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("year", year);
		parameters.put("deductorTan", deductorTan);
		parameters.put("invoiceLineItemId", invoiceLineItemId);

		String sqlQuery = "SELECT * FROM Transactions.invoice_line_item WHERE assessment_year=:year AND deductor_master_tan=:deductorTan AND "
				+ "invoice_line_item_id =:invoiceLineItemId ;";

		if (documentPostingDate != null) {
			parameters.put("documentPostingDate", new Date(documentPostingDate));
			sqlQuery = "SELECT * FROM Transactions.invoice_line_item WHERE assessment_year=:year AND deductor_master_tan=:deductorTan AND "
					+ "invoice_line_item_id =:invoiceLineItemId AND document_posting_date=:documentPostingDate ;";

		}
		return namedJdbcTemplate.query(sqlQuery, parameters, new InvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param year
	 * @param tan
	 * @param doucmentPostingDate
	 * @param iD
	 * @param activeFlag
	 * @return
	 */
	public List<InvoiceLineItem> findByYearTanDocumentPostingDateIdActive(Integer year, String tan,
			Date doucmentPostingDate, Integer iD, Boolean activeFlag) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("year", year);
		parameters.put("tan", tan);
		parameters.put("doucmentPostingDate", doucmentPostingDate);
		parameters.put("iD", iD);
		parameters.put("activeFlag", activeFlag == true ? 1 : 0);
		return namedJdbcTemplate.query(
				String.format(queries.get("find_invoice_By_Year_Tan_Document_PostingDate_Id_Active")), parameters,
				new InvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param challanMonth
	 * @param tan
	 * @param deducteeName
	 * @param pagination
	 * @param section
	 * @return
	 */
	public List<InvoiceLineItem> getInvoicesByCurrentMonth(int assessmentYear, int challanMonth, String tan,
			String deducteeName, Pagination pagination, String section) {
		String paginationOrder = CommonUtil.getPagination("invoice_line_item_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("challanMonth", challanMonth);
		parameters.put("tan", tan);
		parameters.put("deducteeName", deducteeName);
		String query = String.format(queries.get("get_Invoices_By_CurrentMonth"));
		if (StringUtils.isNotBlank(section)) {
			query = query.concat(" AND final_tds_section = '" + section + "'");
		}
		query = query.concat(paginationOrder);
		return namedJdbcTemplate.query(query, parameters, new InvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param challanMonth
	 * @param tan
	 * @param deducteeName
	 * @param section
	 * @return
	 */
	public BigInteger getInvoicesCountByCurrentMonth(int assessmentYear, int challanMonth, String tan,
			String deducteeName, String section) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("challanMonth", challanMonth);
		parameters.put("tan", tan);
		parameters.put("deducteeName", deducteeName);
		String query = String.format(queries.get("get_Invoices_count_by_current_month"));
		if (StringUtils.isNotBlank(section)) {
			query = query.concat(" AND final_tds_section = '" + section + "'");
		}
		return namedJdbcTemplate.queryForObject(query, parameters, BigInteger.class);
	}

	/**
	 * 
	 * @param year
	 * @param tan
	 * @param doucmentPostingDate
	 * @param iD
	 * @return
	 */
	public List<InvoiceLineItem> findByYearTanDocumentPostingDateId(Integer year, String tan, Date doucmentPostingDate,
			Integer iD) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("year", year);
		parameters.put("tan", tan);
		parameters.put("doucmentPostingDate", doucmentPostingDate);
		parameters.put("iD", iD);

		return namedJdbcTemplate.query(String.format(queries.get("find_invoice_By_Year_Tan_Document_PostingDate_Id")),
				parameters, new InvoiceLineItemRowMapper());

	}

	/**
	 * 
	 * @param assessmentYear
	 * @param deductorTan
	 * @param lineItemIds
	 * @return
	 */
	public List<InvoiceLineItem> findByLineItemIds(int assessmentYear, String deductorTan, List<Integer> lineItemIds) {

		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tan", deductorTan);
		parameters.put("year", assessmentYear);
		parameters.put("activeFlag", 1);
		parameters.put("lineItemIds", lineItemIds);

		return namedJdbcTemplate.query(String.format(queries.get("find_by_year_tan_ids")), parameters,
				new InvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param crInvoiceData
	 * @param pagination
	 * @return
	 */
	public CommonDTO<InvoiceLineItem> getInvoicesByCRData(InvoiceLineItem crInvoiceData, Pagination pagination) {
		Date date = crInvoiceData.getOriginalDocumentDate();
		String documentDate = new SimpleDateFormat("yyyy-MM-dd").format(date);

		String paginationOrder = CommonUtil.getPagination("invoice_line_item_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("tan", crInvoiceData.getDeductorMasterTan());
		parameters.put("documentPostingDate", documentDate);
		parameters.put("documentNumber", crInvoiceData.getOriginalDocumentNumber());

		String query = String.format(queries.get("find_All_invoices_by_cr_data"));
		query = query.concat(paginationOrder);

		String countQuery = String.format(queries.get("find_All_invoices_count_by_cr_data"));
		BigInteger invoiceCount = namedJdbcTemplate.queryForObject(countQuery, parameters, BigInteger.class);

		List<InvoiceLineItem> invoiceList = namedJdbcTemplate.query(query, parameters, new InvoiceLineItemRowMapper());
		CommonDTO<InvoiceLineItem> invoiceData = new CommonDTO<>();
		PagedData<InvoiceLineItem> pagedData = new PagedData<>(invoiceList, invoiceList.size(),
				pagination.getPageNumber(),
				invoiceCount.intValue() > (pagination.getPageSize() * pagination.getPageNumber()) ? false : true);
		invoiceData.setResultsSet(pagedData);
		invoiceData.setCount(BigInteger.valueOf(invoiceList.size()));
		return invoiceData;
	}

	/**
	 * 
	 * @param crInvoiceData
	 * @return
	 */
	public List<InvoiceLineItem> getInvoicesByCRData(InvoiceLineItem crInvoiceData) {
		Date date = crInvoiceData.getOriginalDocumentDate();
		String documentDate = new SimpleDateFormat("yyyy-MM-dd").format(date);

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tan", crInvoiceData.getDeductorMasterTan());
		parameters.put("documentPostingDate", documentDate);
		parameters.put("documentNumber", crInvoiceData.getOriginalDocumentNumber());

		String query = String.format(queries.get("get_invoices_key_by_cr_data"));

		return namedJdbcTemplate.query(query, parameters, new InvoiceLineItemRowMapper());
	}

	/**
	 * This method for delete recode based on id.
	 * 
	 * @param id
	 * @return
	 */
	public Integer deleteById(Integer id) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("id", id);
		String query = String.format(queries.get("delete_by_id"));
		return namedJdbcTemplate.update(query, parameters);
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param deductorTan
	 * @param deducteePan
	 * @param deducteename
	 * @param erpDocumentNo
	 * @param invoiceNumber
	 * @param documentPostingDate
	 * @return
	 */
	public BigInteger getInvoicesByYearDeducteeKeyDocumentNo(int assessmentYear, String deductorTan, String deducteeKey,
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

		String query = String.format(queries.get("invoice_year_pan_name_tan_doc_count"));

		return namedJdbcTemplate.queryForObject(query, parameters, BigInteger.class);

	}

	/**
	 * 
	 * @param deductorTan
	 * @param assessmentYear
	 * @param deducteeCode
	 * @param deducteeName
	 * @param natureOfRemittance
	 * @param country
	 * @return
	 */
	public List<NrTransactionsMeta> findByNatureOfRemittance(String deductorTan, Integer assessmentYear,
			String deducteeCode, String deducteeName, String natureOfRemittance, String country) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		deducteeCode = StringUtils.isBlank(deducteeCode) ? "" : deducteeCode;
		country = StringUtils.isBlank(country) ? "" : country;
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("deductorTan", deductorTan);
		parameters.put("deducteeCode", deducteeCode);
		parameters.put("deducteeName", deducteeName);
		parameters.put("natureOfRemittance", natureOfRemittance);
		parameters.put("country", country);

		return namedJdbcTemplate.query(String.format(queries.get("get_nature_of_remittance")), parameters,
				new NrTransactionsMetaRowMapper());
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param deductorTan
	 * @param documentPostingdate
	 * @param nrTransactionsMetaId
	 * @return
	 */
	public List<InvoiceLineItem> findByNRTransactionsMetaId(int assessmentYear, String deductorTan,
			Date documentPostingdate, Integer nrTransactionsMetaId) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("deductorTan", deductorTan);
		parameters.put("documentPostingdate", documentPostingdate);
		parameters.put("nrTransactionsMetaId", nrTransactionsMetaId);

		return namedJdbcTemplate.query(queries.get("get_nr_transaction_meta"), parameters,
				new InvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param deductorPan
	 * @param deducteekey
	 * @return
	 */
	public BigInteger getDeducteeCountByDeducteeKey(String deductorPan, String deducteeKey) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("deducteeKey", deducteeKey);
		String query = String.format(queries.get("deductee_key_count"));

		return namedJdbcTemplate.queryForObject(query, parameters, BigInteger.class);
	}

	/**
	 * This method for update Deductee Master Non Residential
	 * 
	 * @param nrTransactionsMeta
	 * @return
	 */
	public int updateDeducteeMasterNonResidential(DeducteeMasterNonResidential deducteeMasterNonResidential) {
		logger.info("DAO method executing to update user data ");
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(deducteeMasterNonResidential);
		int status = namedJdbcTemplate.update(queries.get("update_deductee_master_non_residential"), namedParameters);
		if (status != 0) {
			logger.info("Deductee Master Non Residential data is updated for ID {} ",
					deducteeMasterNonResidential.getDeducteeMasterId());
		} else {
			logger.info("Deductee Master Non Residential record found with ID {}",
					deducteeMasterNonResidential.getDeducteeMasterId());
		}
		logger.info("DAO method execution successful {}");
		return status;
	}

	public List<InvoiceLineItem> findByOnlyId(Integer id) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("id", id);
		return namedJdbcTemplate.query(queries.get("get__Invoice_by_id"), parameters, new InvoiceLineItemRowMapper());
	}

	public List<InvoiceLineItem> getGlSummaryFoundInGl(String deductorTan, int year, int month, String invoiceColumns,
			Integer batchId, List<Integer> monthValues) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("year", year);
		parameter.put("tan", deductorTan);
		parameter.put("month", month);
		parameter.put("invoiceColumns", invoiceColumns);
		parameter.put("batchId", batchId);
		if (!monthValues.isEmpty()) {
			parameter.put("months", monthValues);
		}
		if (batchId != null) {
			return namedJdbcTemplate.query(queries.get("get_invoice_by_batchId_gl_found"), parameter,
					new InvoiceLineItemRowMapper());
		} else if (month > 0 && year > 0 && StringUtils.isNotBlank(deductorTan) && monthValues.isEmpty()) {
			return namedJdbcTemplate.query(queries.get("get_invoice_by_year_month_tan_gl_found"), parameter,
					new InvoiceLineItemRowMapper());
		} else if (year > 0 && StringUtils.isNotBlank(deductorTan) && monthValues.isEmpty()) {//
			return namedJdbcTemplate.query(queries.get("get_invoice_by_year__tan_gl_found"), parameter,
					new InvoiceLineItemRowMapper());
		} else {//
			return namedJdbcTemplate.query(queries.get("get_invoice_by_year__tan_gl_found_multipleMonth"), parameter,
					new InvoiceLineItemRowMapper());
		}

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
	public List<InvoiceLineItem> getInvoicesWithInterestComputed(String tan, Integer year, Integer month) {
		logger.info("DAO method excuting to get the interest records with year =" + year + ",month=" + month + ",tan="
				+ tan);
		String query = String.format(queries.get("get_invoices_with_interest_computed"));

		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("year", year);
		parameter.put("month", month);
		parameter.put("tan", tan);
		return namedJdbcTemplate.query(query, parameter, new InvoiceLineItemRowMapper());
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
	public List<InvoiceLineItem> getInvoicesWithInterestComputedWithPagination(String tan, Integer year, Integer month,
			String deducteeName, String residentType, Pagination pagination) {
		logger.info("DAO method excuting to get the interest records with year =" + year + ",month=" + month + ",tan="
				+ tan + ",deductee name=" + deducteeName + ",Resident type=" + residentType);
		String paginationOrder = CommonUtil.getPagination("invoice_line_item_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");
		String query = String.format(queries.get("get_invoices_with_interest_computed"));
		if (StringUtils.isNotBlank(deducteeName)) {
			query = query + " AND deductee_name='" + deducteeName + "' ";
		}
		if (StringUtils.isNotBlank(residentType)) {
			query = query + " AND resident='" + residentType + "' ";
		}
		query = query.concat(paginationOrder);
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("year", year);
		parameter.put("month", month);
		parameter.put("tan", tan);
		return namedJdbcTemplate.query(query, parameter, new InvoiceLineItemRowMapper());
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
	public BigInteger getCountOfInvoicesWithInterestComputed(String tan, Integer year, Integer month,
			String deducteeName, String residentType) {
		logger.info("DAO method excuting to get the interest records count with year =" + year + ",month=" + month
				+ ",tan=" + tan + ",deductee name=" + deducteeName + ",Resident type=" + residentType);
		String query = String.format(queries.get("get_count_of_invoices_with_interest_computed"));
		if (deducteeName != null) {
			query = query + " AND deductee_name='" + deducteeName + "' ";
		}
		if (residentType != null) {
			query = query + " AND resident='" + residentType + "' ";
		}
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("year", year);
		parameter.put("month", month);
		parameter.put("tan", tan);
		return namedJdbcTemplate.queryForObject(query, parameter, BigInteger.class);
	}

	/**
	 * to get the invoice interest records based on the id,interest amount and
	 * active
	 * 
	 * @param id
	 * @return
	 */
	public List<InvoiceLineItem> getInvoicesWithInterestById(Integer id) {
		logger.info("DAO method excuting to get the interest records with id =" + id);
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("id", id);
		return namedJdbcTemplate.query(String.format(queries.get("get_invoices_with_interest_computed_by_invoice_id")),
				parameter, new InvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param challanMonth
	 * @param deductorTan
	 * @param pan
	 */
	public void USPReverseLiabilityCaluclation(Integer assessmentYear, Integer challanMonth, String deductorTan,
			String pan) {
		SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource)
				.withProcedureName("USP_TDS_Reverse_Liability_caluclation");
		SqlParameterSource in = new MapSqlParameterSource().addValue("challanMonth", challanMonth)
				.addValue("assessmentYear", assessmentYear).addValue("deductorPan", pan)
				.addValue("deductorTan", deductorTan);

		Map<String, Object> out = jdbcCall.execute(in);
		logger.info("Status of payment adjustments: " + out);
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param challanMonth
	 * @param deductorTan
	 * @param pan
	 */
	public void nrUSPReverseLiabilityCalculation(Integer assessmentYear, Integer challanMonth, String deductorTan,
			String pan) {
		SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource)
				.withProcedureName("USP_TDS_Reverse_Liability_caluclation_NR");
		SqlParameterSource in = new MapSqlParameterSource().addValue("challanMonth", challanMonth)
				.addValue("assessmentYear", assessmentYear).addValue("deductorPan", pan)
				.addValue("deductorTan", deductorTan);

		Map<String, Object> out = jdbcCall.execute(in);
		logger.info("Status of payment adjustments: " + out);
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param challanMonth
	 * @param deductorTan
	 * @param pan
	 */
	public void USPCANAdjustments(Integer assessmentYear, Integer challanMonth, String deductorTan, String pan) {
		SimpleJdbcCall jdbcCall1 = new SimpleJdbcCall(dataSource).withProcedureName("USP_TDS_CAN_Adjustments");
		SimpleJdbcCall jdbcCall2 = new SimpleJdbcCall(dataSource).withProcedureName("USP_TDS_RNV_Adjustments");
		SimpleJdbcCall jdbcCall3 = new SimpleJdbcCall(dataSource).withProcedureName("USP_TDS_Advance_CAN_Adjustment");
		SimpleJdbcCall jdbcCall4 = new SimpleJdbcCall(dataSource).withProcedureName("USP_TDS_Provision_CAN_Adjustment");

		SqlParameterSource in = new MapSqlParameterSource().addValue("challan_month", challanMonth)
				.addValue("assessment_year", assessmentYear).addValue("deductor_pan", pan)
				.addValue("deductor_master_tan", deductorTan);

		Map<String, Object> out1 = jdbcCall1.execute(in);
		Map<String, Object> out2 = jdbcCall2.execute(in);
		Map<String, Object> out3 = jdbcCall3.execute(in);
		Map<String, Object> out4 = jdbcCall4.execute(in);
		logger.info("Status of CAN adjustments: " + out1);
		logger.info("Status of RNV adjustments: " + out2);
		logger.info("Status of Advance CAN adjustments: " + out3);
		logger.info("Status of Provision CAN adjustments: " + out4);
	}

	public void nrUSPCANAdjustments(Integer assessmentYear, Integer challanMonth, String deductorTan, String pan) {
		SimpleJdbcCall jdbcCall1 = new SimpleJdbcCall(dataSource).withProcedureName("USP_TDS_CAN_Adjustments_NR");
		SimpleJdbcCall jdbcCall2 = new SimpleJdbcCall(dataSource)
				.withProcedureName("USP_TDS_Provision_CAN_Adjustment_NR");

		SqlParameterSource in = new MapSqlParameterSource().addValue("challan_month", challanMonth)
				.addValue("assessment_year", assessmentYear).addValue("deductor_pan", pan)
				.addValue("deductor_master_tan", deductorTan);

		Map<String, Object> out1 = jdbcCall1.execute(in);
		Map<String, Object> out2 = jdbcCall2.execute(in);
		logger.info("Status of NR CAN adjustments: " + out1);
		logger.info("Status of NR Provision CAN adjustments: " + out2);
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param challanMonth
	 * @param deductorTan
	 * @param pan
	 */
	public void USPCRAdjustments(Integer assessmentYear, Integer challanMonth, String deductorTan, String pan) {
		SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource).withProcedureName("USP_TDS_CR_Adjustments");
		SqlParameterSource in = new MapSqlParameterSource().addValue("challanMonth", challanMonth)
				.addValue("assessmentYear", assessmentYear).addValue("deductorPan", pan)
				.addValue("deductorMasterTan", deductorTan);
		Map<String, Object> out = jdbcCall.execute(in);
		logger.info("Status of cr adjustments: " + out);
	}

	public void nrUSPCRAdjustments(Integer assessmentYear, Integer challanMonth, String deductorTan, String pan) {
		SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource).withProcedureName("USP_TDS_CR_Adjustments_NR");
		SqlParameterSource in = new MapSqlParameterSource().addValue("challanMonth", challanMonth)
				.addValue("assessmentYear", assessmentYear).addValue("deductorPan", pan)
				.addValue("deductorMasterTan", deductorTan);
		Map<String, Object> out = jdbcCall.execute(in);
		logger.info("Status of nr cr adjustments: " + out);
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param challanMonth
	 * @param deductorTan
	 * @param pan
	 */
	public void USPAdvanceUtilization(Integer assessmentYear, Integer challanMonth, String deductorTan, String pan) {
		SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource).withProcedureName("USP_TDS_Advance_Utilization");
		SqlParameterSource in = new MapSqlParameterSource().addValue("assessment_year", assessmentYear)
				.addValue("challan_month", challanMonth).addValue("deductor_master_tan", deductorTan)
				.addValue("deductor_pan", pan);

		Map<String, Object> out = jdbcCall.execute(in);
		logger.info("Status of advance adjustments: " + out);
	}

	public void nrUSPAdvanceUtilization(Integer assessmentYear, Integer challanMonth, String deductorTan, String pan) {
		SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource).withProcedureName("USP_TDS_Advance_Utilization_NR");
		SqlParameterSource in = new MapSqlParameterSource().addValue("assessment_year", assessmentYear)
				.addValue("challan_month", challanMonth).addValue("deductor_master_tan", deductorTan)
				.addValue("deductor_pan", pan);

		Map<String, Object> out = jdbcCall.execute(in);
		logger.info("Status of nr advance adjustments: " + out);
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param challanMonth
	 * @param deductorTan
	 * @param pan
	 */
	public void USPProvisionUtilization(Integer assessmentYear, Integer challanMonth, String deductorTan, String pan) {
		SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource).withProcedureName("USP_TDS_Provision_Utilization");
		SqlParameterSource in = new MapSqlParameterSource().addValue("assessment_year", assessmentYear)
				.addValue("challan_month", challanMonth).addValue("deductor_master_tan", deductorTan)
				.addValue("deductor_pan", pan);

		Map<String, Object> out = jdbcCall.execute(in);
		logger.info("Status of provision adjustments: " + out);
	}

	public void nrUSPProvisionUtilization(Integer assessmentYear, Integer challanMonth, String deductorTan,
			String pan) {
		SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource).withProcedureName("USP_TDS_Provision_Utilization_NR");
		SqlParameterSource in = new MapSqlParameterSource().addValue("assessment_year", assessmentYear)
				.addValue("challan_month", challanMonth).addValue("deductor_master_tan", deductorTan)
				.addValue("deductor_pan", pan);

		Map<String, Object> out = jdbcCall.execute(in);
		logger.info("Status of nr provision adjustments: " + out);
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param challanMonth
	 * @param deductorTan
	 * @param pan
	 */
	public void USPLDCAdjustmentsProcess(Integer assessmentYear, Integer challanMonth, String deductorTan, String pan) {
		SimpleJdbcCall jdbcCallLdcAdjustments = new SimpleJdbcCall(dataSource)
				.withProcedureName("USP_TDS_LDC_Adjustment");
		SqlParameterSource in = new MapSqlParameterSource().addValue("challan_month", challanMonth)
				.addValue("assessment_year", assessmentYear).addValue("deductor_pan", pan)
				.addValue("deductor_master_tan", deductorTan);

		Map<String, Object> outLdcAdjustments = jdbcCallLdcAdjustments.execute(in);
		logger.info("Status of ldc invoice adjustments: " + outLdcAdjustments);

	}

	public void nrUSPLDCAdjustmentsProcess(Integer assessmentYear, Integer challanMonth, String deductorTan,
			String pan) {
		SimpleJdbcCall jdbcCallLdcAdjustments = new SimpleJdbcCall(dataSource)
				.withProcedureName("USP_TDS_LDC_Adjustment_NR");
		SqlParameterSource in = new MapSqlParameterSource().addValue("challan_month", challanMonth)
				.addValue("assessment_year", assessmentYear).addValue("deductor_pan", pan)
				.addValue("deductor_master_tan", deductorTan);

		Map<String, Object> outLdcAdjustments = jdbcCallLdcAdjustments.execute(in);
		logger.info("Status of nr ldc invoice adjustments: " + outLdcAdjustments);

	}

	/**
	 * This method for batch update invoice mismatch records.
	 * 
	 * @param invoiceList
	 */
	@org.springframework.transaction.annotation.Transactional
	public void batchUpdateInvoiceMismatch(List<InvoiceLineItem> invoiceList) {
		String updateQuery = "UPDATE Transactions.invoice_line_item SET action=:actionType,reason=:finalReason,final_tds_rate=:finalTdsRate ,"
				+ " final_tds_section=:finalTdsSection, final_tds_amount=:finalTdsAmount,has_mismatch=:hasMismatch,active=:active ,"
				+ " surcharge=:surcharge,cess_amount=:cessAmount,interest=:interest,cess_rate=:cessRate, "
				+ " error_reason = :errorReason, is_exempted = :isExempted, invoice_npid = :invoiceNpId, "
				+ " invoice_groupid =:groupId, ldc_certificate_number =:ldcCertificateNumber, has_ldc=:hasLdc, "
				+ " invoice_amount=:invoiceAmount, client_taxable_amount =:clientTaxableAmount "
				+ " WHERE invoice_line_item_id=:id ; ";

		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(invoiceList);
		namedJdbcTemplate.batchUpdate(updateQuery, batch);
		logger.info("invoice batch updated successfully {}", invoiceList.size());

	}

	/**
	 * to get the error records from invoice line item table
	 * 
	 * @param tan
	 * @param assessemtYear
	 * @param batchUploadId
	 * @return
	 */
	public List<InvoiceLineItem> getInvoiceErrorRecords(String tan, Integer assessemtYear, Integer batchUploadId) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("year", assessemtYear);
		parameters.put("tan", tan);
		parameters.put("batchUploadId", batchUploadId);
		return namedJdbcTemplate.query(String.format(queries.get("get_invocice_error_records")), parameters,
				new InvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param batchId
	 * @param tan
	 * @param year
	 * @return
	 */
	public List<InvoiceLineItem> getInvoiceBasedOnBatchId(Integer batchId, String tan, Integer year) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("batchId", batchId);
		parameters.put("tan", tan);
		parameters.put("year", year);
		return namedJdbcTemplate.query(String.format(queries.get("get_invoice_based_on_batchid")), parameters,
				new InvoiceLineItemRowMapper());
	}
	
	public List<Integer> getInvoiceChallanMonthsBasedOnBatchId(Integer batchId, String tan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("batchId", batchId);
		parameters.put("tan", tan);
		return namedJdbcTemplate.queryForList(String.format(queries.get("get_invoice_challan_months_by_batchid")),
				parameters, Integer.class);
	}

	/**
	 * 
	 * @param batchId
	 * @param tan
	 * @param assessmentYear
	 * @return
	 */
	public Integer deleteByBatchId(int batchId, String tan, Integer assessmentYear) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("batchId", batchId);
		parameters.put("tan", tan);
		parameters.put("year", assessmentYear);
		String query = String.format(queries.get("delete_by_batch_id"));
		return namedJdbcTemplate.update(query, parameters);
	}
	
	public Integer deleteNrMetaTransactionsByBatchId(int batchId, String tan) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("batchId", batchId);
		parameters.put("tan", tan);
		String query = String.format(queries.get("delete_nr_transactions_meta_by_batch_id"));
		return namedJdbcTemplate.update(query, parameters);
	}

	/**
	 * 
	 * @param batchId
	 * @param tan
	 * @param year
	 * @return
	 */
	public List<ProvisionDTO> getProvisionBasedOnBatchId(int batchId, String tan, Integer year) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("batchId", batchId);
		parameters.put("tan", tan);
		parameters.put("year", year);
		return namedJdbcTemplate.query(String.format(queries.get("get_provision_based_on_batchid")), parameters,
				new ProvisionRowMapper());
	}
	
	public List<Integer> getProvisionMonthsByBatchId(int batchId, String tan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("batchId", batchId);
		parameters.put("tan", tan);
		return namedJdbcTemplate.queryForList(String.format(queries.get("get_provision_challan_months_by_batchid")),
				parameters, Integer.class);
	}

	/**
	 * 
	 * @param batchId
	 * @param tan
	 * @param assessmentYear
	 * @return
	 */
	public int deleteProvisionBasedOnBatchId(int batchId, String tan, Integer assessmentYear) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("batchId", batchId);
		parameters.put("tan", tan);
		parameters.put("year", assessmentYear);
		String query = String.format(queries.get("delete_provision_by_batch_id"));
		return namedJdbcTemplate.update(query, parameters);
	}

	/**
	 * 
	 * @param batchId
	 * @param tan
	 * @param year
	 * @return
	 */
	public List<AdvanceDTO> getAdvacneBasedOnBatchId(int batchId, String tan, Integer year) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("batchId", batchId);
		parameters.put("tan", tan);
		parameters.put("year", year);
		return namedJdbcTemplate.query(String.format(queries.get("get_advance_based_on_batchid")), parameters,
				new AdvanceRowMapper());
	}
	
	public List<Integer> getAdvanceMonthsBasedOnBatchId(int batchId, String tan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("batchId", batchId);
		parameters.put("tan", tan);
		return namedJdbcTemplate.queryForList(String.format(queries.get("get_advance_challan_months_by_batchid")), parameters,
				Integer.class);
	}

	/**
	 * 
	 * @param batchId
	 * @param tan
	 * @param assessmentYear
	 * @return
	 */
	public int deleteAdvacneBasedOnBatchId(int batchId, String tan, Integer assessmentYear) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("batchId", batchId);
		parameters.put("tan", tan);
		parameters.put("year", assessmentYear);
		String query = String.format(queries.get("delete_advance_by_batch_id"));
		return namedJdbcTemplate.update(query, parameters);
	}

	/**
	 * 
	 * @param batchId
	 * @param tan
	 * @param year
	 * @return
	 */
	public List<InvoiceLineItem> getUpdateInvoiceRecords(String tan, int assessmentYear, int assessmentMonth,
			String deductorPan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("year", assessmentYear);
		parameters.put("tan", tan);
		parameters.put("deductorPan", deductorPan);
		parameters.put("month", assessmentMonth);
		return namedJdbcTemplate.query(String.format(queries.get("get_update_mismatch_invoice_data")), parameters,
				new InvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param deductorTan
	 * @param deductorPan
	 * @param lineItemIds
	 * @return
	 */
	public List<InvoiceLineItem> getInvoiceList(String deductorTan, String deductorPan, List<Integer> lineItemIds) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("lineItemIds", lineItemIds);
		parameters.put("tan", deductorTan);
		return namedJdbcTemplate.query(String.format(queries.get("find_by_tan_invoiceIds")), parameters,
				new InvoiceLineItemRowMapper());
	}

	public List<InvoiceStagging> getInvoiceStaggingRecords(String deductorTan, String deductorPan, Integer year,
			Integer month) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("year", year);
		parameters.put("month", month);
		parameters.put("tan", deductorTan);
		return namedJdbcTemplate.query(String.format(queries.get("find_invoice_stagging_records")), parameters,
				new InvoiceStaggingRowMapper());
	}

	/**
	 * This method for batch save
	 * 
	 * @param provisionBatchSave
	 */
	@org.springframework.transaction.annotation.Transactional
	public void invoiceBatchSave(List<InvoiceLineItem> invoiceLineItemBatchSave) {
		String query = " INSERT INTO Transactions.invoice_line_item (assessment_year, deductor_master_tan, assessment_month, challan_month, invoice_amount, deductor_pan ,deductee_key, deductee_code,"
				+ " deductee_name,document_type, resident, pan, supply_type,document_date,document_number,line_item_number,invoice_npid ,invoice_groupid ,active,"
				+ " is_parent ,is_exempted ,challan_paid ,approved_for_challan ,is_challan_generated ,created_by ,created_date ,modified_by ,modified_date,"
				+ " document_posting_date ,has_mismatch, under_threshold ,actual_tds_section, derived_tds_section ,final_tds_section ,actual_tds_rate, derived_tds_rate,"
				+ " final_tds_rate,actual_tds_amount, derived_tds_amount ,final_tds_amount, batch_upload_id, nr_transactions_meta_id, processed_from,source_identifier,"
				+ " [section], is_error, confidence, service_description_invoice , service_description_po, service_description_gl, interest, surcharge, cess_rate, po_date, po_number,"
				+ " deductee_tin, tds_rate, mismatch_category, ldc_certificate_number, has_ldc, tds_deduction_date, payment_date, has_dtaa, source_file_name, invoice_value, saa_number, tds_remittance_date,"
				+ " debit_credit_indicator, item_code, type_of_transaction, po_line_item_number, tds_base_Value, assignment_number, profit_center, plant, business_area, business_place, "
				+ " ref_key3, tds_tax_code_erp, pos, sgst_rate, sgst_amount, cgst_rate, cgst_amount, igst_rate, igst_amount, gl_account_code, migo_number, miro_number, gstin, deductor_gstin, "
				+ " original_document_number, user_defined_field1, user_defined_field2, tds_rate_ldc, action, advance_can_adjust, provision_can_adjust, vendor_invoice_number, client_effective_tds_rate)"
				+ " VALUES(:assessmentYear,:deductorMasterTan,:assessmentMonth,:challanMonth, :invoiceAmount, :deductorPan,:deducteeKey,:deducteeCode,"
				+ " :deducteeName,:documentType,:resident, :pan, :supplyType,:documentDate,:documentNumber,:lineItemNumber,:invoiceNpId, :groupId,:active,"
				+ " :isParent,:isExempted,:challanPaid,:approvedForChallan,:isChallanGenerated,:createdBy,:createdDate,:modifiedBy,:modifiedDate,"
				+ " :documentPostingDate,:hasMismatch,:underThreshold,:actualTdsSection,:derivedTdsSection,:finalTdsSection,:actualTdsRate,:derivedTdsRate,"
				+ " :finalTdsRate,:actualTdsAmount,:derivedTdsAmount,:finalTdsAmount,:batchUploadId, :nrTransactionsMetaId, :processedFrom, :sourceIdentifier,"
				+ " :section, :isError, :confidence, :serviceDescriptionInvoice, :serviceDescriptionPo, :serviceDescriptionGl, :interest, :surcharge, :cessRate,"
				+ " :poDate, :poNumber, :deducteeTin, :tdsRate, :mismatchCategory, :ldcCertificateNumber, :hasLdc, :tdsDeductionDate, :paymentDate, :hasDtaa,"
				+ " :sourceFileName, :invoiceValue, :saaNumber, :tdsRemittancedate, :debitCreditIndicator, :itemCode, :typeOfTransaction, :poItemNo, :tdsBaseValue,"
				+ " :assignmentNumber, :profitCenter, :plant, :businessArea, :businessPlace, :refKey3, :tdsTaxCodeErp, :pos, :sgstRate, :sgstAmount, :cgstRate,"
				+ " :cgstAmount, :igstRate, :igstAmount, :glAccountCode, :migoNumber, :miroNumber, :gstin, :deductorGstin, :originalDocumentNumber,"
				+ " :userDefinedField1, :userDefinedField2, :tdsRateLdc, :actionType, :advanceCanAdjust, :provisionCanAdjust, :vendorInvoiceNumber, :clientEffectiveTdsRate)";
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(invoiceLineItemBatchSave);
		namedJdbcTemplate.batchUpdate(query, batch);
		logger.info("Invoice lineItem  batch inserted size is {}", invoiceLineItemBatchSave.size());
	}

	/**
	 * This method for batch save
	 * 
	 * @param provisionBatchSave
	 */
	@org.springframework.transaction.annotation.Transactional
	public void nrDeducteeBatchUpdate(List<DeducteeMasterNonResidential> nrDeducteeBatchUpdate) {
		String query = "UPDATE Client_Masters.deductee_master_non_residential SET deductee_master_10f_applicable_from = :tenFApplicableFrom, "
				+ " deductee_master_10f_applicable_to = :tenFApplicableTo, deductee_master_10f_file_address = :form10fFileAddress,"
				+ "	active = :active, additional_sections = :additionalSections, advance_transaction_count = :advanceTransactionCount, applicable_from = :applicableFrom,"
				+ "	applicable_to = :applicableTo, area_locality = :areaLocality, country = :country, country_of_residence = :countryOfResidence, created_by = :createdBy,"
				+ "	default_rate = :defaultRate, email = :emailAddress, fixed_based_india = :fixedBasedIndia, flat_door_block_no = :flatDoorBlockNo, invoice_transaction_count = :invoiceTransactionCount,"
				+ "	is_10f_available = :isTenFAvailable, is_eligible_for_multiple_sections = :isDeducteeHasAdditionalSections, is_no_pe_doc_available = :noPEDocumentAvailable,"
				+ "	is_pe_in_india = :whetherPEInIndia, is_poem_available = :isPOEMavailable, is_trc_available = :isTRCAvailable, isdeductee_transparent = :isDeducteeTransparent, isgrossingup = :isGrossingUp,"
				+ "	istenf_future = :istenfFuture, istrc_future = :istrcFuture, modified_by = :modifiedBy, modified_date = :modifiedDate, modified_name = :modifiedName,"
				+ "	name_building_village = :nameBuildingVillage, no_pe_doc_address = :noPeDocAddress, nr_rate = :nrRate,"
				+ "	pe_applicable_from = :noPEApplicableFrom, pe_applicable_to = :noPEApplicableTo, pe_file_address = :peFileAddress, phone_number = :phoneNumber, pin_code = :pinCode,"
				+ " poem_applicable_from = :poemApplicableFrom, poem_applicable_to = :poemApplicableTo, provision_transaction_count = :provisionTransactionCount,"
				+ "	rate = :rate, related_party = :relatedParty, road_street_postoffice = :roadStreetPostoffice, section = :section,"
				+ "	source_file_name = :sourceFileName, source_identifier = :sourceIdentifier, state = :state, tenf_future_date = :tenfFutureDate, tin_unique_identification = :deducteeTin,"
				+ "	town_city_district = :townCityDistrict, trc_applicable_from = :trcApplicableFrom, trc_applicable_to = :trcApplicableTo,"
				+ "	trc_file_address = :trcFileAddress, trc_future_date = :trcFutureDate, user_defined_field_1 = :userDefinedField1, user_defined_field_2 = :userDefinedField2, user_defined_field_3 = :userDefinedField3,"
				+ "	fixedbase_availble_india_applicable_from = :fixedbaseAvailbleIndiaApplicableFrom, fixedbase_availble_india_applicable_to = :fixedbaseAvailbleIndiaApplicableTo,"
				+ " is_amount_connected_fixed_base = :isAmountConnectedFixedBase, is_business_carried_in_india = :isBusinessCarriedInIndia, is_fixedbase_availble_india = :isFixedbaseAvailbleIndia,"
				+ "	is_peamount_document = :isPEdocument, is_peamount_received = :isPEamountReceived, ispeinvoilvedin_purchase_goods = :isPEinvoilvedInPurchaseGoods,"
				+ "	principles_of_business_place = :principlesOfBusinessPlace, stay_period_financial_year = :stayPeriodFinancialYear,"
				+ "	weather_pe_in_india_applicable_from = :whetherPEInIndiaApplicableFrom, weather_pe_in_india_applicable_to = :whetherPEInIndiaApplicableTo, nature_of_payment = :natureOfPayment,"
				+ "	deductee_aadhar_number = :deducteeAadharNumber, is_poem_declaration = :isPoemDeclaration, poem_future_date = :poemFutureDate, country_to_remittance = :countryToRemittance,"
				+ "	beneficial_owner_of_income = :beneficialOwnerOfIncome, is_beneficial_ownership_of_declaration = :isBeneficialOwnershipOfDeclaration, mli_ppt_condition_satisifed = :mliPptConditionSatisifed,"
				+ "	mli_slob_condition_satisifed = :mliSlobConditionSatisifed, is_mli_ppt_slob = :isMliPptSlob, nature_of_remittance = :natureOfRemittance ,article_number_dtaa = :articleNumberDtaa,"
				+ "	section_of_incometax_act = :sectionOfIncometaxAct, aggreement_for_transaction = :aggreementForTransaction, deductee_gstin = :deducteeGSTIN, is_no_poem_declaration_available = :isNoPOEMDeclarationAvailable,"
				+ " no_poem_declaration_applicable_from_date = :noPOEMDeclarationApplicableFromDate, no_poem_declaration_applicable_to_date = :noPOEMDeclarationApplicableToDate WHERE deductee_master_id = :deducteeMasterId ; ";
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(nrDeducteeBatchUpdate);
		namedJdbcTemplate.batchUpdate(query, batch);
		logger.info("nr deductee master  batch update size is {}", nrDeducteeBatchUpdate.size());
	}

	public List<DeducteeMasterNonResidential> getDeducteesByPan(String deductorPan) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);

		String query = "SELECT * FROM Client_Masters.deductee_master_non_residential WHERE deductor_master_pan = :deductorPan AND active = 1 ";
		return namedJdbcTemplate.query(query, parameters,
				new BeanPropertyRowMapper<DeducteeMasterNonResidential>(DeducteeMasterNonResidential.class));
	}

	public int updateInvoiceAncestorId(Integer batchId, String deductorTan) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("batchId", batchId);
		parameters.put("tan", deductorTan);
		String query = "UPDATE Transactions.invoice_line_item SET ancestor_id = invoice_line_item_id WHERE batch_upload_id =:batchId and deductor_master_tan =:tan";
		return namedJdbcTemplate.update(query, parameters);
	}
	
	public int updateInvoiceMetaNrId(Integer batchId, String deductorTan) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("batchId", batchId);
		parameters.put("tan", deductorTan);
		String query = "UPDATE IT SET nr_transactions_meta_id = NM.id"
				+ " FROM Transactions.nr_transactions_meta NM" + " INNER JOIN Transactions.invoice_line_item IT"
				+ " ON IT.document_number = NM.erp_document_no AND IT.line_item_number = NM.line_item_number AND"
				+ " IT.document_type = NM.document_type AND IT.supply_type = NM.supply_type AND"
				+ " IT.vendor_invoice_number = NM.vendor_document_no AND IT.deductor_pan = NM.deductor_pan AND"
				+ " IT.deductor_master_tan = NM.deductor_master_tan AND IT.document_posting_date = NM.document_posting_date AND"
				+ " IT.batch_upload_id = NM.batch_upload_id AND IT.assessment_year = NM.assessment_year"
				+ " WHERE NM.batch_upload_id = :batchId and NM.deductor_master_tan =:tan";
		return namedJdbcTemplate.update(query, parameters);
	}

	public void USPTDSLDCInvoiceRateMappingNR(Integer batchUploadId) {
		SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource)
				.withProcedureName("USP_TDS_LDC_Invoice_Rate_Mapping_NR");
		SqlParameterSource in = new MapSqlParameterSource().addValue("batchUploadId", batchUploadId);

		Map<String, Object> out = jdbcCall.execute(in);
		logger.info("Status of USPTDSLDCInvoiceRateMappingNR adjustments: {}", out);
	}

	public void USPTDSLDCProvisionRateMappingNR(Integer batchUploadId) {
		SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource)
				.withProcedureName("USP_TDS_LDC_Provision_Rate_Mapping_NR");
		SqlParameterSource in = new MapSqlParameterSource().addValue("batchUploadId", batchUploadId);
		Map<String, Object> out = jdbcCall.execute(in);
		logger.info("Status of USPTDSLDCProvisionRateMappingNR adjustments: {}", out);
	}

	public void USPTDSLDCAdvanceRateMappingNR(Integer batchUploadId) {
		SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource)
				.withProcedureName("USP_TDS_LDC_Advance_Rate_Mapping_NR");
		SqlParameterSource in = new MapSqlParameterSource().addValue("batchUploadId", batchUploadId);

		Map<String, Object> out = jdbcCall.execute(in);
		logger.info("Status of USPTDSLDCAdvanceRateMapping_NR adjustments: {}", out);
	}

}
