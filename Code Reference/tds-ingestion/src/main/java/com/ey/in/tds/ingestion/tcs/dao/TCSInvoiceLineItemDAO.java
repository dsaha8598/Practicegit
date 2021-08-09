package com.ey.in.tds.ingestion.tcs.dao;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Collection;
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
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import com.ey.in.tcs.common.domain.TCSLedger;
import com.ey.in.tcs.common.domain.transactions.jdbc.rowmapper.PaymentRowMapper;
import com.ey.in.tcs.common.domain.transactions.jdbc.rowmapper.TCSLedgerRowMapper;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.tcs.TCSInvoiceLineItem;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.MismatchAmountsDto;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.TcsPaymentDTO;
import com.ey.in.tds.common.domain.transactions.jdbc.rowmapper.MismatchAmountsRowMapper;
import com.ey.in.tds.common.dto.CRDTO;
import com.ey.in.tds.common.dto.invoice.InvoiceMismatchByBatchIdDTO;
import com.ey.in.tds.common.dto.invoice.MismatchesFiltersDTO;
import com.ey.in.tds.common.repository.common.PagedData;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.ingestion.tcs.rowmapper.TCSInvoiceLineItemRowMapper;

/**
 * 
 * @author scriptbees.
 *
 */
@Repository
public class TCSInvoiceLineItemDAO {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private HashMap<String, String> queries;

	@Autowired
	private HashMap<String, String> tcsQueries;

	@Autowired
	private DataSource dataSource;

	private SimpleJdbcInsert simplejdbcInsert;

	private NamedParameterJdbcTemplate namedJdbcTemplate;

	@PostConstruct
	private void postConstruct() {
		namedJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		simplejdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("tcs_invoice_line_item")
				.withSchemaName("Transactions").usingGeneratedKeyColumns("id");
	}

	public void USPReverseLiabilityCaluclation(Integer assessmentYear, Integer challanMonth, String collectorTan,
			String pan) {
		SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource).withProcedureName("USP_Reverse_Liability_caluclation");
		SqlParameterSource in = new MapSqlParameterSource().addValue("challanMonth", challanMonth)
				.addValue("assessmentYear", assessmentYear).addValue("collectorPan", pan)
				.addValue("collectorTan", collectorTan);

		Map<String, Object> out = jdbcCall.execute(in);
		logger.info("Status of payment adjustments: " + out);
	}

	public void USPCANAdjustments(Integer assessmentYear, Integer challanMonth, String collectorTan, String pan) {
		SimpleJdbcCall jdbcCall1 = new SimpleJdbcCall(dataSource).withProcedureName("USP_CAN_Adjustments");
		SimpleJdbcCall jdbcCall2 = new SimpleJdbcCall(dataSource).withProcedureName("USP_RNV_Adjustments");
		SimpleJdbcCall jdbcCall3 = new SimpleJdbcCall(dataSource).withProcedureName("USP_Payment_CAN_Adjustment");

		SqlParameterSource in = new MapSqlParameterSource().addValue("challanMonth", challanMonth)
				.addValue("assessmentYear", assessmentYear).addValue("collectorPan", pan)
				.addValue("collectorTan", collectorTan);

		Map<String, Object> out1 = jdbcCall1.execute(in);
		Map<String, Object> out2 = jdbcCall2.execute(in);
		Map<String, Object> out3 = jdbcCall3.execute(in);
		logger.info("Status of CAN adjustments: " + out1);
		logger.info("Status of CAN adjustments: " + out2);
		logger.info("Status of CAN adjustments: " + out3);
	}

	public void USPCRAdjustments(Integer assessmentYear, Integer challanMonth, String collectorTan, String pan) {
		SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource).withProcedureName("USP_CR_Adjustments");
		SqlParameterSource in = new MapSqlParameterSource().addValue("challanMonth", challanMonth)
				.addValue("assessmentYear", assessmentYear).addValue("collectorPan", pan)
				.addValue("collectorTan", collectorTan);

		Map<String, Object> out = jdbcCall.execute(in);
		logger.info("Status of payment adjustments: " + out);
	}

	public void USPPaymentUtilization(Integer assessmentYear, Integer challanMonth, String collectorTan, String pan) {
		SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource).withProcedureName("USP_Payment_Utilization");
		SqlParameterSource in = new MapSqlParameterSource().addValue("challanMonth", challanMonth)
				.addValue("assessmentYear", assessmentYear).addValue("collectorPan", pan)
				.addValue("collectorTan", collectorTan);

		Map<String, Object> out = jdbcCall.execute(in);
		logger.info("Status of payment adjustments: " + out);
	}

	public void USPLCCInvoiceAdjustments(Integer assessmentYear, Integer challanMonth, String collectorTan,
			String pan) {
		SimpleJdbcCall jdbcCallInvoice = new SimpleJdbcCall(dataSource).withProcedureName("USP_LCC_Adjustment");
		SqlParameterSource in = new MapSqlParameterSource().addValue("challanMonth", challanMonth)
				.addValue("assessmentYear", assessmentYear).addValue("collectorPan", pan)
				.addValue("collectorTan", collectorTan);

		Map<String, Object> outInvoice = jdbcCallInvoice.execute(in);
		logger.info("Status of payment adjustments: " + outInvoice);
	}

	public void USPMismatchInvoiceAdjustments(Integer batchId) {
		SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource).withProcedureName("USP_Mismatch_Invoice_Adjustments");
		SqlParameterSource in = new MapSqlParameterSource().addValue("batchId", batchId);
		Map<String, Object> out = jdbcCall.execute(in);
		logger.debug("Status of mismatch payment adjustments: " + out);
	}

	public void adjustCR(Integer batchId) {
		SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource).withProcedureName("USP_CR_Mismatch_Adjustments");
		SqlParameterSource in = new MapSqlParameterSource().addValue("batchId", batchId);
		Map<String, Object> out = jdbcCall.execute(in);
		logger.debug("Status of CR Adjustments : " + out);
	}

	/**
	 * 
	 * @param batchId
	 * @param mismatchType
	 * @param tan
	 * @param pagination
	 * @return
	 */
	public List<TCSInvoiceLineItem> getInvoiceMismatchesByAssessmentYearAssessmentMonthBatchIdAndMismatchcategory(
			Integer batchId, String mismatchType, String tan, Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		String paginationOrder = CommonUtil.getPagination("id", pagination.getPageNumber(), pagination.getPageSize(),
				"DESC");
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("batchId", batchId);
		parameters.put("mismatchType", mismatchType);
		parameters.put("tan", tan);
		String query = String.format(queries.get("tcs_find_batchId_tan_mismatch"));
		query = query.concat(paginationOrder);
		return namedJdbcTemplate.query(query, parameters, new TCSInvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param collector_tan
	 * @param type
	 * @param year
	 * @param month
	 * @param pagination
	 * @return
	 */
	public List<TCSInvoiceLineItem> getInvoiceByType(String tan, String type, int year, int month,
			Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String paginationOrder = CommonUtil.getPagination("id", pagination.getPageNumber(), pagination.getPageSize(),
				"DESC");
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("tan", tan);
		parameters.put("type", type);
		parameters.put("year", year);
		parameters.put("month", month);
		String query = String.format(queries.get("tcs_get_invoice_by_type"));
		query = query.concat(paginationOrder);
		return namedJdbcTemplate.query(query, parameters, new TCSInvoiceLineItemRowMapper());
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
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("year", year);
		parameters.put("tan", tan);
		parameters.put("type", type);
		parameters.put("month", month);
		return namedJdbcTemplate.queryForObject(String.format(queries.get("tcs_get_invoce_by_type_count")), parameters,
				BigInteger.class);
	}

	/**
	 * 
	 * @param year
	 * @param tan
	 * @return
	 */
	public List<TCSInvoiceLineItem> invoiceLineItemBasedOnTanAndYear(Integer year, String tan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("year", year);
		parameters.put("tan", tan);
		return namedJdbcTemplate.query(String.format(queries.get("tcs_get_invoice_based_on_tan_year")), parameters,
				new TCSInvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param year
	 * @param tan
	 * @return
	 */
	public List<TCSInvoiceLineItem> getAllInvoiceTanAndYear(Integer year, String tan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("year", year);
		parameters.put("tan", tan);
		return namedJdbcTemplate.query(String.format(queries.get("tcs_get_all_invoice_based_on_tan_year")), parameters,
				new TCSInvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param tan
	 * @param firstDate
	 * @param lastDate
	 * @param isMismatch
	 * @return
	 */
	public long getTdsCalculationStatus(Integer assessmentYear, String tan, String firstDate, String lastDate,
			boolean isMismatch) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("previousYear", assessmentYear - 1);
		parameters.put("currentYear", assessmentYear);
		parameters.put("nextYear", assessmentYear + 1);
		parameters.put("tan", tan);
		parameters.put("isMismatch", isMismatch == true ? 1 : 0);
		parameters.put("startDate", firstDate);
		parameters.put("endDate", lastDate);
		return namedJdbcTemplate.queryForObject(String.format(queries.get("tcs_get_tcs_calculation_status")),
				parameters, Long.class);

	}

	public PagedData<TCSInvoiceLineItem> getInvoicesByCRData(CRDTO crData, Pagination pagination,
			Date originalDocumentDate) {
		return null;
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param deductorTan
	 * @param documentPostingDate
	 * @param id
	 * @param activeFlag
	 * @return
	 */
	public List<TCSInvoiceLineItem> findById(Integer assessmentYear, String deductorTan, Date documentPostingDate,
			Integer id, boolean activeFlag) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("tan", deductorTan);
		parameters.put("id", id);
		parameters.put("activeFlag", activeFlag ? 1 : 0);
		return namedJdbcTemplate.query(String.format(queries.get("tcs_find_by_id_active")), parameters,
				new TCSInvoiceLineItemRowMapper());

	}

	public BigInteger getInvoicesCountByCRData(CRDTO crData, String orginalDocumentDate) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 
	 * @param collectorTan
	 * @param assessmentYear
	 * @return
	 */
	public List<TCSInvoiceLineItem> findAll(String collectorTan, Integer assessmentYear) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("collectorTan", collectorTan);
		parameters.put("assessmentYear", assessmentYear);
		return namedJdbcTemplate.query(String.format(queries.get("tcs_find_all_tan_year")), parameters,
				new TCSInvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param collectorTan
	 * @param collecteeType
	 * @param year
	 * @param month
	 * @param isMismatch
	 * @return
	 */
	public Collection<TCSInvoiceLineItem> getInvoiceCollectees(String collectorTan, int year, int month,
			boolean isMismatch) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", year);
		parameters.put("month", month);
		parameters.put("tan", collectorTan);
		parameters.put("isMismatch", isMismatch);
		String query = queries.get("tcs_invoice_collectees");
		/*
		 * Boolean residentType = null; if("Y".equalsIgnoreCase(collecteeType)) {
		 * residentType = true; }else { residentType = false; } if ((residentType) &&
		 * (!isMismatch)) { query +=
		 * " AND active = 1 AND non_resident_collectee_indicator = 1 ;"; } else if
		 * (isMismatch) { query +=
		 * " AND active = 0 AND has_mismatch = 1 AND non_resident_collectee_indicator = 0 ;"
		 * ; }
		 */
		return namedJdbcTemplate.query(query, parameters, new TCSInvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param listChallanMonth
	 * @param collectorTan
	 * @param challanPaid
	 * @param isForNonResidents
	 * @return
	 */
	public List<TCSInvoiceLineItem> getInvoiceLineItemsByAssessmentYearChallanMonthDeductorTan(Integer assessmentYear,
			List<Integer> listChallanMonth, List<String> collectorTan, boolean challanPaid, boolean isForNonResidents) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("listChallanMonth", listChallanMonth.toString().replace("[", "").replace("]", ""));
		parameters.put("collectorTan", collectorTan.toString().replace("[", "").replace("]", ""));
		parameters.put("challanPaid", challanPaid == true ? 1 : 0);
		// parameters.put("isForNonResidents", isForNonResidents);
		return namedJdbcTemplate.query(String.format(queries.get("tcs_get_invoice_challan_month_tan_year")), parameters,
				new TCSInvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param tcsInvoiceLineItem
	 * @return
	 */
	public int update(TCSInvoiceLineItem tcsInvoiceLineItem) {
		int status = 0;
		logger.info("Updating the tcs Invoice line item {}");
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(tcsInvoiceLineItem);
		status = namedJdbcTemplate.update(queries.get("tcs_update_Invoice_line_item"), namedParameters);
		if (status != 0) {
			logger.info("tcs Invoice line item updated successfully {}");
		}
		return status;
	}

	@org.springframework.transaction.annotation.Transactional
	public void batchUpdate(final List<TCSInvoiceLineItem> tcsInvoiceLineItems) {
		String updateQuery = "UPDATE Transactions.tcs_invoice_line_item SET "
				+ "	surcharge_amount = :surchargeAmount, itcess_amount = :itcessAmount, action = :action, "
				+ "	final_tcs_amount = :finalTcsAmount, final_tcs_rate = :finalTcsRate, final_tcs_section = :finalTcsSection, "
				+ "	has_mismatch = :hasMismatch, active = :active, is_exempted = :isExempted, nature_of_income=:natureOfIncome,"
				+ " noi_id=:noiId, final_reason=:finalReason, error_reason=:errorReason WHERE id = :id ";
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(tcsInvoiceLineItems);

		int[] updateCounts = namedJdbcTemplate.batchUpdate(updateQuery, batch);
		logger.info("tcs Invoice line item batch updated successfully {}", tcsInvoiceLineItems.size());
	}

	/**
	 * 
	 * @param id
	 * @param tan
	 * @param postingDate
	 * @param lineNumber
	 * @param b
	 * @return
	 */
	public List<TCSInvoiceLineItem> findByYearTanDocumentPostingDateIdActive(int year, String tan, Date postingDate,
			Integer id, boolean activeFlag) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("year", year);
		parameters.put("tan", tan);
		parameters.put("postingDate", postingDate);
		parameters.put("id", id);
		parameters.put("activeFlag", activeFlag == true ? 1 : 0);
		return namedJdbcTemplate.query(String.format(queries.get("tcs_find_posting_date_tan_year_active")), parameters,
				new TCSInvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param trim
	 * @param year
	 * @param month
	 * @return
	 */
	public long getAllInvoiceMismatchesCount(String tan, int year, int month) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("year", year);
		parameters.put("tan", tan);
		parameters.put("month", month);
		return namedJdbcTemplate.queryForObject(String.format(queries.get("")), parameters, Long.class);
	}

	/**
	 * 
	 * @param trim
	 * @param year
	 * @param month
	 * @param pagination
	 * @return
	 */
	public List<TCSInvoiceLineItem> getAllInvoiceMismatchesPage(String collectorTan, int year, int month,
			Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("year", year);
		parameters.put("tan", collectorTan);
		parameters.put("month", month);
		return namedJdbcTemplate.query(String.format(queries.get("get_all_invoice_mismatch_page")), parameters,
				new TCSInvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param invoiceLineItem
	 * @return
	 */
	public TCSInvoiceLineItem save(TCSInvoiceLineItem invoiceLineItem) {
		logger.info("insert method execution started  {}");
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(invoiceLineItem);
		invoiceLineItem.setId(simplejdbcInsert.executeAndReturnKey(namedParameters).intValue());
		logger.info("Record inserted to tcs invoice line table {}");
		return invoiceLineItem;
	}

	/**
	 * 
	 * @param year
	 * @param month
	 * @param tan
	 * @param batchId
	 * @param type
	 * @return
	 */
	public InvoiceMismatchByBatchIdDTO getInvoiceMismatchSummary(int year, int month, String tan, Integer batchId,
			String type) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		InvoiceMismatchByBatchIdDTO invoiceMismatchByBatchIdDTO = new InvoiceMismatchByBatchIdDTO();
		invoiceMismatchByBatchIdDTO.setId(batchId);
		invoiceMismatchByBatchIdDTO.setMismatchcategory(type);

		List<MismatchAmountsDto> mismatchAmountsDtoList = getMismatchAmounts(tan, batchId, type, year, month);
		if (!mismatchAmountsDtoList.isEmpty()) {
			MismatchAmountsDto mismatchAmountsDto = mismatchAmountsDtoList.get(0);

			invoiceMismatchByBatchIdDTO.setInvoiceValue(mismatchAmountsDto.getTotalSum().setScale(2, RoundingMode.UP));
			invoiceMismatchByBatchIdDTO
					.setTdsSystemAmount(mismatchAmountsDto.getDerivedTcsSum().setScale(2, RoundingMode.UP));
			invoiceMismatchByBatchIdDTO
					.setTdsClientAmount(mismatchAmountsDto.getActualTcsSum().setScale(2, RoundingMode.UP));
			invoiceMismatchByBatchIdDTO
					.setTotalRecords((mismatchAmountsDto.getMismatchCount() == null) ? new BigDecimal(0)
							: mismatchAmountsDto.getMismatchCount());

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
	 * @param year
	 * @param month
	 * @param collectorTan
	 * @param residentType
	 * @param filetype
	 * @param pagination
	 * @return
	 */
	public List<TCSInvoiceLineItem> findAllResidentAndNonResident(int year, int month, String collectorTan,
			String collecteeType, String filetype, Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String paginationOrder = CommonUtil.getPagination("id", pagination.getPageNumber(), pagination.getPageSize(),
				"DESC");
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("year", year);
		parameters.put("month", month);
		parameters.put("collectorTan", collectorTan);
		// parameters.put("collecteeType", collecteeType);
		parameters.put("fileType", filetype);
		String query = String.format(queries.get("tcs_find_all_resident"));
		query = query.concat(paginationOrder);
		return namedJdbcTemplate.query(query, parameters, new TCSInvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param year
	 * @param month
	 * @param collectorTan
	 * @param residentType
	 * @param filetype
	 * @return
	 */
	public BigInteger findAllResidentAndNonResidentCount(int year, int month, String collectorTan, String residentType,
			String filetype) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("year", year);
		parameters.put("month", month);
		parameters.put("collectorTan", collectorTan);
		parameters.put("filetype", filetype);
		return namedJdbcTemplate.queryForObject(String.format(queries.get("tcs_find_resident_count")), parameters,
				BigInteger.class);
	}

	/**
	 * 
	 * @param year
	 * @param month
	 * @param collectorTan
	 * @param residentType
	 * @param filetype
	 * @param collecteeName
	 * @param pagination
	 * @return
	 */
	public List<TCSInvoiceLineItem> findAllResidentAndNonResidentByDeductee(int year, int month, String collectorTan,
			String residentType, String filetype, String collecteeName, Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String paginationOrder = CommonUtil.getPagination("id", pagination.getPageNumber(), pagination.getPageSize(),
				"DESC");
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("year", year);
		parameters.put("month", month);
		parameters.put("collectorTan", collectorTan);
		parameters.put("filetype", filetype);
		parameters.put("collecteeName", collecteeName);
		String query = String.format(queries.get("tcs_find_resident_by_collecctee"));
		query = query.concat(paginationOrder);
		return namedJdbcTemplate.query(query, parameters, new TCSInvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param year
	 * @param month
	 * @param collectorTan
	 * @param residentType
	 * @param collecteeName
	 * @param filetype
	 * @return
	 */
	public BigInteger findAllResidentAndNonResidentByDeducteeCount(int year, int month, String collectorTan,
			String residentType, String collecteeName, String filetype) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("year", year);
		parameters.put("month", month);
		parameters.put("collectorTan", collectorTan);
		parameters.put("filetype", filetype);
		parameters.put("collecteeName", collecteeName);
		return namedJdbcTemplate.queryForObject(String.format(queries.get("tcs_find_resident_name_count")), parameters,
				BigInteger.class);
	}

	/**
	 * 
	 * @param year
	 * @param challanMonth
	 * @param tan
	 * @param mismatchCategory
	 * @param filters
	 * @return
	 */
	public List<TCSInvoiceLineItem> getInvoicesByAssessmentYearChallanMonthByTanMismatchCategory(int year,
			int challanMonth, String tan, String mismatchCategory, MismatchesFiltersDTO filters) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String paginationOrder = CommonUtil.getPagination("id", filters.getPagination().getPageNumber(),
				filters.getPagination().getPageSize(), "DESC");
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("year", year);
		parameters.put("challanMonth", challanMonth);
		parameters.put("tan", tan);
		parameters.put("mismatchCategory", mismatchCategory);

		String sqlQuery = "";

		if (StringUtils.isNotBlank(filters.getDeducteeName())
				&& !"undefined".equalsIgnoreCase(filters.getDeducteeName())) {
			parameters.put("deducteeName", filters.getDeducteeName());
			sqlQuery += " AND collectee_name = :deducteeName ";
		}
		if (StringUtils.isNotBlank(filters.getConfidence()) && !"undefined".equalsIgnoreCase(filters.getConfidence())) {
			parameters.put("confidence", filters.getConfidence());
			sqlQuery += " AND confidence = :confidence ";
		}
		if (StringUtils.isNotBlank(filters.getSection()) && !"undefined".equalsIgnoreCase(filters.getSection())) {
			parameters.put("section", filters.getSection());
			sqlQuery += " AND tcs_section = :section ";
		}
		if (StringUtils.isNotBlank(filters.getDerivedSection())
				&& !"undefined".equalsIgnoreCase(filters.getDerivedSection())) {
			parameters.put("derivedTdsSection", filters.getDerivedSection());
			sqlQuery += " AND derived_tcs_section = :derivedTdsSection ";
		}
		sqlQuery = sqlQuery.concat(paginationOrder);
		return namedJdbcTemplate.query(
				String.format(queries.get("get_tcs_invoices_by_year_month_by_tan_mismatch_category"), sqlQuery),
				parameters, new TCSInvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param year
	 * @param challanMonth
	 * @param tan
	 * @param mismatchCategory
	 * @param filters
	 * @return
	 */
	public BigInteger getInvoicesCountByYearMonthAndTanMismatchCategory(int year, int challanMonth, String tan,
			String mismatchCategory, MismatchesFiltersDTO filters) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		// String filtersStr = "";
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("year", year);
		parameters.put("challanMonth", challanMonth);
		parameters.put("tan", tan);
		parameters.put("mismatchCategory", mismatchCategory);
		String sqlQuery = String.format(queries.get("get_tcs_invoices_by_year_month_by_tan_mismatch_category_count"));
		if (StringUtils.isNotBlank(filters.getDeducteeName())
				&& !"undefined".equalsIgnoreCase(filters.getDeducteeName())) {
			parameters.put("deducteeName", filters.getDeducteeName());
			sqlQuery += " AND collectee_name = :deducteeName ";
		}
		if (StringUtils.isNotBlank(filters.getConfidence()) && !"undefined".equalsIgnoreCase(filters.getConfidence())) {
			parameters.put("confidence", filters.getConfidence());
			sqlQuery += " AND confidence = :confidence ";
		}
		if (StringUtils.isNotBlank(filters.getSection()) && !"undefined".equalsIgnoreCase(filters.getSection())) {
			parameters.put("section", filters.getSection());
			sqlQuery += " AND tcs_section = :section ";
		}
		if (StringUtils.isNotBlank(filters.getDerivedSection())
				&& !"undefined".equalsIgnoreCase(filters.getDerivedSection())) {
			parameters.put("derrivedTdsSection", filters.getDerivedSection());
			sqlQuery += "AND derived_tcs_section = :derrivedTdsSection ";
		}
		return namedJdbcTemplate.queryForObject(sqlQuery, parameters, BigInteger.class);

	}

	/**
	 * 
	 * @param year
	 * @param tan
	 * @param id
	 * @param postingDate
	 * @return
	 */
	public List<TCSInvoiceLineItem> findByYearPanInvoiceId(int year, String tan, Integer id, Long postingDate) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String query = String.format(queries.get("tcs_fine_year_pan_id"));
		if (postingDate != null) {
			query = query.concat(" AND posting_date = '" + postingDate + "' ");
		}
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("year", year);
		parameters.put("tan", tan);
		parameters.put("id", id);
		return namedJdbcTemplate.query(query, parameters, new TCSInvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param tan
	 * @param postingDate
	 * @param id
	 * @return
	 */
	public List<TCSInvoiceLineItem> findByYearTanDocumentPostingDateId(Integer year, String tan, Date postingDate,
			Integer id) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("year", year);
		parameters.put("tan", tan);
		parameters.put("postingDate", postingDate);
		parameters.put("id", id);
		return namedJdbcTemplate.query(String.format(queries.get("tcs_find_invoice_by_year_tan_posting_date")),
				parameters, new TCSInvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param month
	 * @param tan
	 * @param collecteeName
	 * @param pagination
	 * @return
	 */
	public List<TCSInvoiceLineItem> getInvoicesByCurrentMonth(int assessmentYear, int month, String tan,
			String collecteeName, Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String paginationOrder = CommonUtil.getPagination("id", pagination.getPageNumber(), pagination.getPageSize(),
				"DESC");
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("month", month);
		parameters.put("tan", tan);
		parameters.put("collecteeName", collecteeName);
		String query = String.format(queries.get("tcs_get_invoice_current_month"));
		query = query.concat(paginationOrder);
		return namedJdbcTemplate.query(query, parameters, new TCSInvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param month
	 * @param tan
	 * @param collecteeName
	 * @return
	 */
	public BigInteger getInvoicesCountByCurrentMonth(int assessmentYear, int month, String tan, String collecteeName) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("month", month);
		parameters.put("tan", tan);
		parameters.put("collecteeName", collecteeName);
		return namedJdbcTemplate.queryForObject(String.format(queries.get("tcs_get_invoice_current_month_count")),
				parameters, BigInteger.class);
	}

	/**
	 * 
	 * @param tan
	 * @param batchId
	 * @param type
	 * @return
	 */
	private List<MismatchAmountsDto> getMismatchAmounts(String tan, Integer batchId, String type, Integer year,
			Integer month) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String query = String.format(queries.get("tcs_get_invoice_mismatch_amounts"));

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("batchId", batchId);
		parameters.put("tan", tan);
		parameters.put("type", type);
		parameters.put("year", year);
		parameters.put("month", month);
		if (batchId != null) {
			query = query.concat(" AND batch_upload_id=:batchId");
		}
		if (year != 0 && month != 0) {
			query = query.concat(" AND assessment_year=" + year);
			query = query.concat(" AND challan_month=" + month);
		}

		return namedJdbcTemplate.query(query, parameters, new MismatchAmountsRowMapper());

	}

	// all mismatches query based on TAN and Type
	public BigDecimal getTotalValueByYearAndMonth(String columnName, int assessmentYear, int assessmentMonth,
			String tan, String type) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("columnName", columnName);
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("assessmentMonth", assessmentMonth);
		parameters.put("tan", tan);
		parameters.put("type", type);
		Double sum = namedJdbcTemplate.queryForObject(
				String.format(queries.get("tcs_get_total_value_year_month"), columnName), parameters, Double.class);
		return sum == null ? BigDecimal.ZERO : new BigDecimal(sum).setScale(2, BigDecimal.ROUND_UP);

	}

	/**
	 * 
	 * @param assessmentYear
	 * @param deductorTan
	 * @param id
	 * @param b
	 * @return
	 */
	public List<TCSInvoiceLineItem> findTcsInvoiceId(Integer id) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("lineItemId", id);

		return namedJdbcTemplate.query(String.format(queries.get("tcs_invoice_line_item_by_id")), parameters,
				new TCSInvoiceLineItemRowMapper());
	}

	/**
	 * TO get the records which are exempted
	 * 
	 * @param assessmentYear
	 * @param assessmentMonth
	 * @param collectorTan
	 * @param collectorPan
	 * @return
	 */
	public List<TCSInvoiceLineItem> getInvoiceByExempt(int assessmentYear, int assessmentMonth, String collectorTan,
			String collectorPan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("assessmentMonth", assessmentMonth);
		parameters.put("collectorTan", collectorTan);
		parameters.put("collectorPan", collectorPan);
		return namedJdbcTemplate.query(String.format(queries.get("tcs_by_tan_pan_exempted")), parameters,
				new TCSInvoiceLineItemRowMapper());

	}

	public List<TcsPaymentDTO> getPaymentByExempt(int assessmentYear, int assessmentMonth, String collectorTan,
			String collectorPan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("assessmentMonth", assessmentMonth);
		parameters.put("collectorTan", collectorTan);
		parameters.put("collectorPan", collectorPan);
		return namedJdbcTemplate.query(String.format(queries.get("tcs_payment_by_tan_pan_exempted")), parameters,
				new PaymentRowMapper());

	}

	/**
	 * 
	 * @param batchId
	 * @param mismatchType
	 * @param tan
	 * @return
	 */
	public BigInteger getTcsInvoiceMismatchesBatchIdAndMismatchcategoryCount(Integer batchId, String mismatchType,
			String tan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("batchId", batchId);
		parameters.put("mismatchType", mismatchType);
		parameters.put("tan", tan);
		String query = String.format(queries.get("tcs_find_batchId_tan_mismatch_count"));
		return namedJdbcTemplate.queryForObject(query, parameters, BigInteger.class);
	}

	/**
	 * 
	 * @param year
	 * @param tan
	 * @param postingDate
	 * @param ids
	 * @param activeFlag
	 * @return
	 */
	public List<TCSInvoiceLineItem> findTcsInvoiceByYearAndTanAndId(int year, String tan, Integer id,
			boolean activeFlag) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("year", year);
		parameters.put("tan", tan);
		parameters.put("id", id);
		parameters.put("activeFlag", activeFlag == true ? 1 : 0);
		return namedJdbcTemplate.query(String.format(queries.get("tcs_find_invoice_year_tan_ids")), parameters,
				new TCSInvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param collecteeCode
	 * @param collectorTan
	 * @return
	 */
	public String getCollecteeNmeByCollecteeCode(String collecteeCode, String collectorTan) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("collecteeCode", collecteeCode);
		parameters.put("collectorTan", collectorTan);
		return namedJdbcTemplate.queryForObject(String.format(queries.get("get_collecteeName_by_CollecteeCode")),
				parameters, String.class);
	}

	public List<TCSInvoiceLineItem> getCrErrorRecords(String tan, Integer assessemtYear, Integer batchUploadId) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("year", assessemtYear);
		parameters.put("tan", tan);
		parameters.put("batchUploadId", batchUploadId);
		return namedJdbcTemplate.query(String.format(queries.get("tcs_get_cr_error_records")), parameters,
				new TCSInvoiceLineItemRowMapper());
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param deductorTan
	 * @param invoiceLineItemId
	 * @return
	 */
	public List<TCSInvoiceLineItem> findByLineItemId(int assessmentYear, String deductorTan,
			Integer invoiceLineItemId) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("year", assessmentYear);
		parameters.put("tan", deductorTan);
		parameters.put("id", invoiceLineItemId);
		return namedJdbcTemplate.query(String.format(queries.get("find_tcs_line_item_id")), parameters,
				new TCSInvoiceLineItemRowMapper());
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
		return namedJdbcTemplate.queryForObject(String.format(queries.get("get_tcs_pdf_transaction_status")),
				parameters, Long.class);

	}

	public List<TCSLedger> getInvoiceLineItemsByReceiptSerialNoAndBSRCodeAndReceiptDate(Integer assessmentYear,
			String deductorTan, boolean challanPaid, boolean isForNonResidents, String bsrCode, String receiptSerailNo,
			String receiptDate, List<Integer> months, String section) {

		String query = String.format(queries.get("tcs_invoice_By_ReceiptSerialNo__dBSRCode_ReceiptDate"));

		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("deductorTan", deductorTan);
		parameters.put("bsrCode", bsrCode);
		parameters.put("receiptSerailNo", receiptSerailNo);
		parameters.put("receiptDate", receiptDate);
		// parameters.put("isForNonResidents", isForNonResidents);
		parameters.put("challanPaid", challanPaid);
		parameters.put("months", months);

		if (StringUtils.isNotBlank(section) && section.split("-").length > 1) {
			query = query + String.format(queries.get("tcs_invoice_By_ReceiptSerialNo__dBSRCode_ReceiptDate_union"));
			
			query = query + "and l.final_tcs_section ='" + section.split("-")[0] + "'";
			if (section.split("-")[1].equalsIgnoreCase("company")) {
				query = query + "and SUBSTRING(l.collectee_pan,4,1) in ('C')";
			} else {
				query = query + "and SUBSTRING(l.collectee_pan,4,1) not in ('C')";
			}
		}

		query = query + "order by create_date desc";

		return namedJdbcTemplate.query(query, parameters, new TCSLedgerRowMapper());

	}

	/**
	 * 
	 * @param year
	 * @param month
	 * @param tan
	 * @param deductorPan
	 */
	public void invoiceMismatchCountUpdate(int year, Integer month, String tan, String deductorPan) {

		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("year", year);
		parameters.put("tan", tan);
		parameters.put("deductorPan", deductorPan);
		parameters.put("month", month);
		namedJdbcTemplate.update(queries.get("tcs_invoice_mismatch_count_update"), parameters);

	}

	/**
	 * 
	 * @param year
	 * @param month
	 * @param tan
	 * @param deductorPan
	 */
	public void paymentMismatchCountUpdate(int year, Integer month, String tan, String deductorPan) {
		String tenantId = MultiTenantContext.getTenantId();

		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("year", year);
		parameters.put("tan", tan);
		parameters.put("deductorPan", deductorPan);
		parameters.put("month", month);
		namedJdbcTemplate.update(queries.get("tcs_payment_mismatch_count_update"), parameters);

	}

	/**
	 * 
	 * @param invoiceIds
	 * @param tan
	 * @param collectorPan
	 * @return
	 */
	public List<TCSInvoiceLineItem> getInvoiceBasedOnIds(List<Integer> invoiceIds, String tan, String collectorPan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("invoiceIds", invoiceIds);
		parameters.put("tan", tan);
		parameters.put("collectorPan", collectorPan);
		return namedJdbcTemplate.query(tcsQueries.get("get_tcs_invoice_basedon_ids"), parameters,
				new TCSInvoiceLineItemRowMapper());

	}

}
