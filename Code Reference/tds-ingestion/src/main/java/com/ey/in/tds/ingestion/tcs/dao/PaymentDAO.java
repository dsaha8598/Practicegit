package com.ey.in.tds.ingestion.tcs.dao;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import com.ey.in.tcs.common.domain.transactions.jdbc.rowmapper.CollecteeNoiThresholdLedgerRowMapper;
import com.ey.in.tcs.common.domain.transactions.jdbc.rowmapper.PaymentRowMapper;
import com.ey.in.tcs.common.domain.transactions.jdbc.rowmapper.TCSClosingOpeningMatrixRowMapper;
import com.ey.in.tcs.common.domain.transactions.jdbc.rowmapper.TcsMatrixRowMapper;
import com.ey.in.tcs.common.model.payment.PaymentMismatchByBatchIdDTO;
import com.ey.in.tcs.common.model.payment.TcsMatrixDTO;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.MismatchAmountsDto;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.TcsPaymentDTO;
import com.ey.in.tds.common.domain.transactions.jdbc.rowmapper.MismatchAmountsRowMapper;
import com.ey.in.tds.common.dto.invoice.MismatchesFiltersDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.CollecteeNoiThresholdLedger;
import com.ey.in.tds.common.onboarding.jdbc.dto.TCSLccMaster;
import com.ey.in.tds.common.onboarding.tcs.jdbc.rowmapper.TCSLccMasterRowMapper;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.core.util.CommonUtil;

/**
 * class contains the logic for data base operations with ao_master table
 * 
 * @author Amani
 *
 */
@Repository
public class PaymentDAO {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired(required = false)
	private HashMap<String, String> tcsQueries;

	private JdbcTemplate jdbcTemplate;

	private SimpleJdbcInsert simpleJdbcInsert;

	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	private DataSource dataSource;

	@PostConstruct
	private void postConstruct() {
		jdbcTemplate = new JdbcTemplate(dataSource);
		namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("tcs_payment").withSchemaName("Transactions")
				.usingGeneratedKeyColumns("id");

	}

	/**
	 * 
	 * @param dto
	 * @return
	 */
	public TcsPaymentDTO save(TcsPaymentDTO dto) {
		logger.info("insert method execution started  {}");
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(dto);
		dto.setId(simpleJdbcInsert.executeAndReturnKey(namedParameters).intValue());
		logger.info("Record inserted to ao_master table {}");
		return dto;
	}

	public void USPMismatchPaymentAdjustments(Integer batchId) {
		SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource).withProcedureName("USP_Mismatch_Payment_Adjustments");
		SqlParameterSource in = new MapSqlParameterSource().addValue("batch_id", batchId);
		Map<String, Object> out = jdbcCall.execute(in);
		logger.info("Status of mismatcg payment adjustments: " + out);
	}

	public void USPCanMismatchPaymentAdjustments(Integer batchId) {
		SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource).withProcedureName("USP_CAN_Mismatch_Adjustments");
		SqlParameterSource in = new MapSqlParameterSource().addValue("batch_id", batchId);
		Map<String, Object> out = jdbcCall.execute(in);
		logger.info("Status of mismatcg payment adjustments: " + out);
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param challanMonth
	 * @param collectorTan
	 * @param pan
	 */
	public void USPPaymentUtilization(Integer assessmentYear, Integer challanMonth, String collectorTan, String pan) {
		SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource).withProcedureName("USP_Payment_Utilization");
		SqlParameterSource in = new MapSqlParameterSource().addValue("assessmentYear", assessmentYear)
				.addValue("challanMonth", challanMonth).addValue("collectorTan", collectorTan)
				.addValue("collectorPan", pan);
		Map<String, Object> out = jdbcCall.execute(in);
		logger.info("Status of payment adjustments: " + out);
	}

	/**
	 * 
	 * @param dto
	 * @return
	 */
	public TcsPaymentDTO update(TcsPaymentDTO dto) {
		logger.info("DAO method executing to save user data ");
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(dto);
		int status = namedParameterJdbcTemplate.update(String.format(tcsQueries.get("payment_update")),
				namedParameters);

		if (status != 0) {
			logger.info("Payment data is updated for ID " + dto.getId());
		} else {
			logger.info("No record found with ID " + dto.getId());
		}
		logger.info("DAO method execution successful {}");
		return dto;
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param challanMonth
	 * @param tan
	 * @param approvedForChallan
	 * @return
	 */
	public List<TcsPaymentDTO> getPaymenteByYearMonthTan(int assessmentYear, int challanMonth, String tan,
			boolean approvedForChallan) {

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("challanMonth", challanMonth);
		parameters.put("tan", tan);
		parameters.put("approvedForChallan", approvedForChallan);
		return namedParameterJdbcTemplate.query(String.format(tcsQueries.get("get_payment_by_year_month_tan")),
				parameters, new PaymentRowMapper());

	}

	/**
	 * 
	 * @param assessmentYear
	 * @param challanMonth
	 * @param tan
	 * @param approvedForChallan
	 * @return
	 */
	public Long getPaymenteByYearMonthTanCount(int assessmentYear, int challanMonth, String tan,
			boolean approvedForChallan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("challanMonth", challanMonth);
		parameters.put("tan", tan);
		parameters.put("approvedForChallan", approvedForChallan);

		return namedParameterJdbcTemplate.queryForObject(
				String.format(tcsQueries.get("get_payment_by_year_month_tan_count")), parameters, Long.class);
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param collectorTan
	 * @param challanPaid
	 * @param isForNonResidents
	 * @param bsrCode
	 * @param receiptSerailNo
	 * @param receiptDate
	 * @return
	 */
	public List<TcsPaymentDTO> getAdvacnceLineItemsByReceiptSerialNoAndBSRCodeAndReceiptDate(Integer assessmentYear,
			String collectorTan, boolean challanPaid, boolean isForNonResidents, String bsrCode, String receiptSerailNo,
			String receiptDate, List<Integer> months, String section) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		// String resident = isForNonResidents ? "Y" : "N";
		String query = String.format(tcsQueries.get("get_payment_by_serialNo_bsrCode_date"));
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("collectorTan", collectorTan);
		parameters.put("challanPaid", challanPaid);
		// parameters.put("resident", resident);
		parameters.put("bsrCode", bsrCode);
		parameters.put("receiptSerailNo", receiptSerailNo);
		parameters.put("receiptDate", receiptDate);
		parameters.put("months", months);

		if (StringUtils.isNotBlank(section) && section.split("-").length > 1) {
			query = query + String.format(tcsQueries.get("get_payment_by_serialNo_bsrCode_date_count_union"));
			query = query + "and tp.final_tcs_section ='" + section.split("-")[0] + "'";
			if (section.split("-")[1].equalsIgnoreCase("company")) {
				query = query + "and SUBSTRING(tp.collectee_pan,4,1) in ('C')";
			} else {
				query = query + "and SUBSTRING(tp.collectee_pan,4,1) not in ('C')";
			}
		}

		query = query + "order by created_date desc";

		return namedParameterJdbcTemplate.query(query, parameters, new PaymentRowMapper());
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param collectorTan
	 * @param challanPaid
	 * @param isForNonResidents
	 * @param bsrCode
	 * @param receiptSerailNo
	 * @param receiptDate
	 * @return
	 */
	public BigInteger getTotalCountByAssessmentYearAndBSRCode(Integer assessmentYear, String collectorTan,
			boolean challanPaid, boolean isForNonResidents, String bsrCode, String receiptSerailNo,
			String receiptDate) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		// String resident = isForNonResidents ? "Y" : "N";

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("collectorTan", collectorTan);
		parameters.put("challanPaid", challanPaid);
		// parameters.put("resident", resident);
		parameters.put("bsrCode", bsrCode);
		parameters.put("receiptSerailNo", receiptSerailNo);
		parameters.put("receiptDate", receiptDate);

		return namedParameterJdbcTemplate.queryForObject(
				String.format(tcsQueries.get("get_payment_by_serialNo_bsrCode_date_count")), parameters,
				BigInteger.class);
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param assessmentMonth
	 * @param tan
	 * @param systemChallanSerailNo
	 * @return
	 */
	public BigDecimal getTotalValueBySystemChallanSerialNo(int assessmentYear, int assessmentMonth, String tan,
			String systemChallanSerailNo) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("assessmentMonth", assessmentMonth);
		parameters.put("tan", tan);
		parameters.put("systemChallanSerailNo", systemChallanSerailNo);

		BigDecimal response = namedParameterJdbcTemplate.queryForObject(
				String.format(tcsQueries.get("get_payment_total_by_serialNo")), parameters, BigDecimal.class);
		return response == null ? BigDecimal.ZERO : response.setScale(2, RoundingMode.UP);

	}

	/**
	 * 
	 * @param assessmentYear
	 * @param challanMonth
	 * @param tan
	 * @param approvedForChallan
	 * @param section
	 * @param isChallanGenerated
	 * @param isVerifyLiability
	 * @param isChallanPaid
	 * @return
	 */
	public BigDecimal getTotalTdsAmount(int assessmentYear, int challanMonth, String tan, boolean approvedForChallan,
			String section, boolean isChallanGenerated, boolean isVerifyLiability, boolean isChallanPaid) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String queryStr = "";
		if (!isVerifyLiability) {
			queryStr += "AND is_challan_generated = " + isChallanGenerated;
		}

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("challanMonth", challanMonth);
		parameters.put("tan", tan);
		parameters.put("approvedForChallan", approvedForChallan);
		parameters.put("section", section);
		parameters.put("isChallanGenerated", isChallanGenerated);
		parameters.put("isChallanPaid", isChallanPaid);
		parameters.put("queryStr", queryStr);

		BigDecimal response = namedParameterJdbcTemplate.queryForObject(
				String.format(tcsQueries.get("get_payment_totalTdsAmount")), parameters, BigDecimal.class);
		return response == null ? BigDecimal.ZERO : response.setScale(2, RoundingMode.UP);

	}

	/**
	 * 
	 * @param assessmentYear
	 * @param challanMonth
	 * @param tan
	 * @param approvedForChallan
	 * @param section
	 * @return
	 */
	public BigDecimal getTotalInterestAmount(int assessmentYear, int challanMonth, String tan,
			boolean approvedForChallan, String section) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("challanMonth", challanMonth);
		parameters.put("tan", tan);
		parameters.put("approvedForChallan", approvedForChallan);
		parameters.put("section", section);

		BigDecimal response = namedParameterJdbcTemplate.queryForObject(
				String.format(tcsQueries.get("get_payment_totalInterstAmount")), parameters, BigDecimal.class);
		return response == null ? BigDecimal.ZERO : response.setScale(2, RoundingMode.UP);

	}

	/**
	 * 
	 * @param assessmentYear
	 * @param challanMonth
	 * @param tan
	 * @param approvedForChallan
	 * @param section
	 * @return
	 */
	public BigDecimal getTotalPenaltyAmount(int assessmentYear, int challanMonth, String tan,
			boolean approvedForChallan, String section) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("challanMonth", challanMonth);
		parameters.put("tan", tan);
		parameters.put("approvedForChallan", approvedForChallan);
		parameters.put("section", section);

		BigDecimal response = namedParameterJdbcTemplate.queryForObject(
				String.format(tcsQueries.get("get_payment_totalPenaltyAmount")), parameters, BigDecimal.class);
		return response == null ? BigDecimal.ZERO : response.setScale(2, RoundingMode.UP);

	}

	/**
	 * 
	 * @param assessmentYear
	 * @param challanMonth
	 * @param tan
	 * @param approvedForChallan
	 * @return
	 */
	public BigDecimal getTotalTdsAmountAggregated(int assessmentYear, int challanMonth, String tan,
			boolean approvedForChallan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("challanMonth", challanMonth);
		parameters.put("tan", tan);
		parameters.put("approvedForChallan", approvedForChallan);

		BigDecimal response = namedParameterJdbcTemplate.queryForObject(
				String.format(tcsQueries.get("get_payment_totalTdsAmount_Aggregated")), parameters, BigDecimal.class);
		return response == null ? BigDecimal.ZERO : response.setScale(2, RoundingMode.UP);

	}

	/**
	 * 
	 * @param assessmentYear
	 * @param challanMonth
	 * @param tan
	 * @param section
	 * @param approvedForChallan
	 * @param isChallanGenerated
	 * @param isVerifyLiability
	 * @param systemChallanSerialNumber
	 * @param isChallanPaid
	 * @return
	 */
	public List<TcsPaymentDTO> getPaymenteByYearMonthTanAndSection(int assessmentYear, int challanMonth, String tan,
			String section, boolean approvedForChallan, boolean isChallanGenerated, boolean isVerifyLiability,
			String systemChallanSerialNumber, boolean isChallanPaid) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		String query = String.format(tcsQueries.get("get_payment_by_year_month_tan_section"));
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

		return namedParameterJdbcTemplate.query(query, parameters, new PaymentRowMapper());
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param challanMonth
	 * @param tan
	 * @param approvedForChallan
	 * @param isChallanGenerated
	 * @param isVerifyLiability
	 * @param isChallanPaid
	 * @return
	 */
	public List<TcsPaymentDTO> getPaymenteByYearMonthTanWithoutSection(int assessmentYear, int challanMonth, String tan,
			boolean approvedForChallan, boolean isChallanGenerated, boolean isVerifyLiability, boolean isChallanPaid) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		String query = String.format(tcsQueries.get("get_payment_by_year_month_tan_withOutSection"));
		if (!isVerifyLiability) {
			query = query.concat("is_challan_generated =" + isChallanGenerated);
		}

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("challanMonth", challanMonth);
		parameters.put("tan", tan);
		parameters.put("approvedForChallan", approvedForChallan);
		parameters.put("isChallanPaid", isChallanPaid);

		return namedParameterJdbcTemplate.query(query, parameters, new PaymentRowMapper());
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param challanMonth
	 * @param tan
	 * @param approvedForChallan
	 * @param isChallanGenerated
	 * @param systemChallanSerialNumber
	 * @return
	 */
	public List<TcsPaymentDTO> getPaymenteByYearMonthTanAggregated(int assessmentYear, int challanMonth,
			List<String> tan, boolean approvedForChallan, boolean isChallanGenerated,
			String systemChallanSerialNumber) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		String query = String.format(tcsQueries.get("get_payment_by_year_month_tan_aggregated"));
		if (systemChallanSerialNumber != null) {
			query = query.concat("system_challan_serial_no =" + systemChallanSerialNumber);
		}

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("challanMonth", challanMonth);
		parameters.put("tan", tan);
		parameters.put("approvedForChallan", approvedForChallan);

		return namedParameterJdbcTemplate.query(query, parameters, new PaymentRowMapper());
	}

	/**
	 * 
	 * @param tan
	 * @param type
	 * @param year
	 * @param month
	 * @param isMatch
	 * @return
	 */
	public List<TcsPaymentDTO> getPaymentDeductees(String tan, String type, int year, int month, boolean isMatch) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String query = String.format(tcsQueries.get("get_payment_collectees"));
		/*
		 * if ("N".equalsIgnoreCase(type) && !isMatch) { query =
		 * query.concat(" and active = 1"); } if (isMatch) { query =
		 * query.concat(" and mismatch =1 and active = 0"); }
		 */
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("tan", tan);
		// parameters.put("type", type);
		parameters.put("year", year);
		parameters.put("month", month);

		return namedParameterJdbcTemplate.query(query, parameters, new PaymentRowMapper());
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param assessmentMonth
	 * @param tan
	 * @param resType
	 * @param pagination
	 * @return
	 */
	public List<TcsPaymentDTO> findAllResidentAndNonResident(int assessmentYear, int assessmentMonth, String tan,
			String resType, Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String paginationOrder = CommonUtil.getPagination("id", pagination.getPageNumber(), pagination.getPageSize(),
				"DESC");
		String query = String.format(tcsQueries.get("get_payment_resident_nonresident"));

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("assessmentMonth", assessmentMonth);
		parameters.put("tan", tan);
		query = query.concat(paginationOrder);
		return namedParameterJdbcTemplate.query(query, parameters, new PaymentRowMapper());
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param assessmentMonth
	 * @param tan
	 * @param resType
	 * @param collecteeName
	 * @param pagination
	 * @return
	 */
	public List<TcsPaymentDTO> findAllResidentAndNonResidentByDeductee(int assessmentYear, int assessmentMonth,
			String tan, String resType, String collecteeName, Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String paginationOrder = CommonUtil.getPagination("id", pagination.getPageNumber(), pagination.getPageSize(),
				"DESC");
		String query = String.format(tcsQueries.get("get_payment_resident_nonresident_collectee"));
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("assessmentMonth", assessmentMonth);
		parameters.put("tan", tan);
		// parameters.put("resType", resType);
		parameters.put("collecteeName", collecteeName);
		query = query.concat(paginationOrder);
		return namedParameterJdbcTemplate.query(query, parameters, new PaymentRowMapper());
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
	public PaymentMismatchByBatchIdDTO getPaymentMismatchSummary(int assessmentYear, int assessmentMonth, String tan,
			Integer batchId, String type) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		PaymentMismatchByBatchIdDTO paymentMismatchByBatchIdDTO = new PaymentMismatchByBatchIdDTO();
		paymentMismatchByBatchIdDTO.setId(batchId);
		paymentMismatchByBatchIdDTO.setMismatchcategory(type);

		List<MismatchAmountsDto> mismatchAmountsDtoList = getTotalValueAndCountByBatchId(tan, batchId, type,
				assessmentYear, assessmentMonth);
		if (!mismatchAmountsDtoList.isEmpty()) {
			MismatchAmountsDto mismatchAmountsDto = mismatchAmountsDtoList.get(0);
			paymentMismatchByBatchIdDTO.setInvoiceValue(mismatchAmountsDto.getTotalSum().setScale(2, RoundingMode.UP));
			paymentMismatchByBatchIdDTO
					.setTdsSystemAmount(mismatchAmountsDto.getDerivedTcsSum().setScale(2, RoundingMode.UP));
			paymentMismatchByBatchIdDTO
					.setTdsClientAmount(mismatchAmountsDto.getActualTcsSum().setScale(2, RoundingMode.UP));
			paymentMismatchByBatchIdDTO
					.setTotalRecords((mismatchAmountsDto.getMismatchCount() == null) ? new BigDecimal(0)
							: mismatchAmountsDto.getMismatchCount());
		}
		if (!"NAD".equalsIgnoreCase(type)) {
			if (paymentMismatchByBatchIdDTO.getTdsSystemAmount() != null
					&& paymentMismatchByBatchIdDTO.getTdsClientAmount() != null) {
				if (paymentMismatchByBatchIdDTO.getTdsSystemAmount()
						.compareTo(paymentMismatchByBatchIdDTO.getTdsClientAmount()) < 0) {
					paymentMismatchByBatchIdDTO.setExcessDeduction(paymentMismatchByBatchIdDTO.getTdsSystemAmount()
							.subtract(paymentMismatchByBatchIdDTO.getTdsClientAmount()).setScale(2, RoundingMode.UP));
				} else {
					paymentMismatchByBatchIdDTO.setShortDeduction(paymentMismatchByBatchIdDTO.getTdsSystemAmount()
							.subtract(paymentMismatchByBatchIdDTO.getTdsClientAmount()).setScale(2, RoundingMode.UP));
				}
			}
		} else {
			paymentMismatchByBatchIdDTO.setExcessDeduction(new BigDecimal(0));
			paymentMismatchByBatchIdDTO.setShortDeduction(new BigDecimal(0));
		}
		return paymentMismatchByBatchIdDTO;
	}

	/**
	 * 
	 * @param tan
	 * @param batchId
	 * @param type
	 * @return
	 */
	private List<MismatchAmountsDto> getTotalValueAndCountByBatchId(String tan, Integer batchId, String type,
			Integer year, Integer month) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String query = String.format(tcsQueries.get("get_payment_total_value_and_count_by_batchid"));
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("tan", tan);
		parameters.put("batchId", batchId);
		parameters.put("type", type);

		if (batchId != null) {
			query = query.concat(" AND batch_upload_id = :batchId");
		}

		if (year != 0 && month != 0) {
			query = query.concat(" AND assessment_year=" + year);
			query = query.concat(" AND challan_month=" + month);
		}
		return namedParameterJdbcTemplate.query(query, parameters, new MismatchAmountsRowMapper());
	}

	/**
	 * 
	 * @param collectorTan
	 * @param mismatchCategory
	 * @param batchUploadId
	 * @param pagination
	 * @return
	 */
	public List<TcsPaymentDTO> getPaymentDetailsByTanBatchIdAndMismatchCategory(String collectorTan,
			String mismatchCategory, Integer batchUploadId, Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String paginationOrder = CommonUtil.getPagination("id", pagination.getPageNumber(), pagination.getPageSize(),
				"DESC");
		String query = String.format(tcsQueries.get("get_payment_by_tan_batchId_mismatchCategory"));
		query = query.concat(paginationOrder);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("collectorTan", collectorTan);
		parameters.put("mismatchCategory", mismatchCategory);
		parameters.put("batchUploadId", batchUploadId);

		return namedParameterJdbcTemplate.query(query, parameters, new PaymentRowMapper());

	}

	/**
	 * 
	 * @param assessmentYear
	 * @param tan
	 * @param documentPostingDate
	 * @param id
	 * @param activeFlag
	 * @return
	 */
	public List<TcsPaymentDTO> findByYearTanDocumentPostingDateIdActive(int assessmentYear, String tan,
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
				String.format(tcsQueries.get("find_payment_By_Year_Tan_Document_PostingDate_Id_Active")), parameters,
				new PaymentRowMapper());
	}

	/**
	 * 
	 * @param collectorTan
	 * @param deducteePan
	 * @param finalTdsSection
	 * @param documentDate
	 * @return
	 */
	public BigDecimal getLccRemainigBalance(String collectorTan, String collecteePan, String finalTdsSection,
			long documentDate) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("collectorTan", collectorTan);
		parameters.put("collecteePan", collecteePan);
		parameters.put("finalTdsSection", finalTdsSection);
		parameters.put("documentDate", documentDate);
		BigDecimal amount = namedParameterJdbcTemplate.queryForObject(
				String.format(tcsQueries.get("find_lcc_remainingBalance")), parameters, BigDecimal.class);
		return amount == null ? BigDecimal.ZERO : amount.setScale(2, RoundingMode.UP);

	}

	/**
	 * 
	 * @param collectorTan
	 * @param deducteePan
	 * @param finalTdsSection
	 * @param documentDate
	 * @return
	 */
	public List<TCSLccMaster> getLccRecordByTanPanSectionDocumentDate(String collectorTan, String collecteePan,
			String finalTdsSection, long documentDate) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("collectorTan", collectorTan);
		parameters.put("collecteePan", collecteePan);
		parameters.put("finalTdsSection", finalTdsSection);
		parameters.put("documentDate", documentDate);
		return namedParameterJdbcTemplate.query(
				String.format(tcsQueries.get("find_lcc_by_tan_pan_section_docuemntDate")), parameters,
				new TCSLccMasterRowMapper());

	}

	/**
	 * 
	 * @param collectorTan
	 * @param year
	 * @return
	 */
	public List<TcsMatrixDTO> getPaymentMatrix(String collectorTan, Integer year) {

		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("collectorTan", collectorTan);
		parameters.put("year", year);

		return namedParameterJdbcTemplate.query(String.format(tcsQueries.get("get_payment_matrix")), parameters,
				new TcsMatrixRowMapper());
	}

	/**
	 * 
	 * @param collectorTan
	 * @param year
	 * @param month
	 * @return
	 */
	public List<TcsPaymentDTO> getAllPaymentMismatches(String collectorTan, Integer year, Integer month) {

		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("collectorTan", collectorTan);
		parameters.put("year", year);
		parameters.put("month", month);

		return namedParameterJdbcTemplate.query(String.format(tcsQueries.get("get_payment_mismatches_By_year_tan")),
				parameters, new PaymentRowMapper());
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param month
	 * @param collectorTan
	 * @param mismatchCategory
	 * @param filters
	 * @return
	 */
	public List<TcsPaymentDTO> getPaymentDetailsByTanActiveAndMismatchCategory(int assessmentYear, int month,
			String collectorTan, String mismatchCategory, MismatchesFiltersDTO filters) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String paginationOrder = CommonUtil.getPagination("id", filters.getPagination().getPageNumber(),
				filters.getPagination().getPageSize(), "DESC");
		Map<String, Object> parameters = new HashMap<>();

		parameters.put("assessmentYear", assessmentYear);
		parameters.put("month", month);
		parameters.put("collectorTan", collectorTan);
		parameters.put("mismatchCategory", mismatchCategory);

		String query = String.format(tcsQueries.get("get_payment_By_tan_mimatch"));
		query = query.concat(paginationOrder);

		if (StringUtils.isNotBlank(filters.getDeducteeName())
				&& !"undefined".equalsIgnoreCase(filters.getDeducteeName())) {
			query = query.concat(" and collectee_name = '" + filters.getDeducteeName() + "' ");
		}
		if (StringUtils.isNotBlank(filters.getConfidence()) && !"undefined".equalsIgnoreCase(filters.getConfidence())) {
			query = query.concat(" and confidence = '" + filters.getConfidence() + "' ");
		}
		if (StringUtils.isNotBlank(filters.getSection()) && !"undefined".equalsIgnoreCase(filters.getSection())) {
			query = query.concat(" and actual_tcs_section = '" + filters.getSection() + "' ");
		}
		if (StringUtils.isNotBlank(filters.getDerivedSection())
				&& !"undefined".equalsIgnoreCase(filters.getDerivedSection())) {
			query = query.concat(" and derived_tcs_section = '" + filters.getDerivedSection() + "' ");
		}

		return namedParameterJdbcTemplate.query(query, parameters, new PaymentRowMapper());

	}

	/**
	 * 
	 * @param collectorTan
	 * @param year
	 * @param month
	 * @return
	 */
	public long getPaymentMismatchCount(String collectorTan, int year, int month) {

		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("collectorTan", collectorTan);
		parameters.put("year", year);
		parameters.put("month", month);

		return namedParameterJdbcTemplate.queryForObject(String.format(tcsQueries.get("get_payment_mismatch_count")),
				parameters, Long.class);
	}

	/**
	 * 
	 * @param collectorTan
	 * @param assessmentYear
	 * @param month
	 * @return
	 */
	public List<TcsPaymentDTO> getPaymentFTM(String collectorTan, int assessmentYear, int month) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("collectorTan", collectorTan);
		parameters.put("year", assessmentYear);
		parameters.put("month", month);

		return namedParameterJdbcTemplate.query(String.format(tcsQueries.get("get_payment_ftm")), parameters,
				new PaymentRowMapper());

	}

	/**
	 * 
	 * @param collectorTan
	 * @param year
	 * @param month
	 * @return
	 */
	public List<TcsMatrixDTO> getPaymentClosingMatrixReport(String collectorTan, Integer year, Integer month) {

		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("collectorTan", collectorTan);
		parameters.put("year", year);
		parameters.put("month", month);

		return namedParameterJdbcTemplate.query(String.format(tcsQueries.get("get_payment_closing_matrix_report")),
				parameters, new TCSClosingOpeningMatrixRowMapper());
	}

	/**
	 * 
	 * @param advanceId
	 * @return
	 */
	public List<TcsPaymentDTO> findByPaymentId(Integer advanceId) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("id", advanceId);
		return namedParameterJdbcTemplate.query(String.format(tcsQueries.get("get_payment_by_paymentId")), parameters,
				new PaymentRowMapper());
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param tan
	 * @param id
	 * @return
	 */
	public List<TcsPaymentDTO> findByYearTanDocumentPostingDateId(int assessmentYear, String tan, Integer id) {

		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("year", assessmentYear);
		parameters.put("tan", tan);
		parameters.put("iD", id);
		return namedParameterJdbcTemplate.query(
				String.format(tcsQueries.get("find_payment_By_Year_Tan_Document_PostingDate_Id")), parameters,
				new PaymentRowMapper());
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
	public List<TcsPaymentDTO> getTcsPaymentCollectees(String collectorTan, int year, int month, boolean isMismatch) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("year", year);
		parameters.put("month", month);
		parameters.put("collectorTan", collectorTan);
		parameters.put("isMismatch", isMismatch);
		String query = tcsQueries.get("get_payment_By_year_tan_mimatch");

		return namedParameterJdbcTemplate.query(query, parameters, new PaymentRowMapper());
	}

	public List<TcsPaymentDTO> findByAdvanceId(Integer id) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 
	 * @param collectorTan
	 * @param mismatchCategory
	 * @param batchUploadId
	 * @return
	 */
	public BigInteger getPaymentBatchIdAndMismatchCategoryCount(String collectorTan, String mismatchCategory,
			Integer batchUploadId) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("collectorTan", collectorTan);
		parameters.put("mismatchCategory", mismatchCategory);
		parameters.put("batchUploadId", batchUploadId);
		String query = String.format(tcsQueries.get("get_payment_by_tan_batchId_mismatchCategory_count"));
		return namedParameterJdbcTemplate.queryForObject(query, parameters, BigInteger.class);
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param month
	 * @param collectorTan
	 * @param mismatchCategory
	 * @param filters
	 * @return
	 */
	public BigInteger getPaymentMismatchCategoryCount(int assessmentYear, int month, String collectorTan,
			String mismatchCategory, MismatchesFiltersDTO filters) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("month", month);
		parameters.put("collectorTan", collectorTan);
		parameters.put("mismatchCategory", mismatchCategory);

		String query = String.format(tcsQueries.get("get_payment_By_tan_mimatch_count"));
		if (StringUtils.isNotBlank(filters.getDeducteeName())
				&& !"undefined".equalsIgnoreCase(filters.getDeducteeName())) {
			query = query.concat(" and collectee_name = '" + filters.getDeducteeName() + "' ");
		}
		/*
		 * if (StringUtils.isNotBlank(filters.getResidentType()) &&
		 * !"undefined".equalsIgnoreCase(filters.getResidentType())) { query =
		 * query.concat(" and is_resident = ' " + filters.getResidentType() + "' "); }
		 */
		if (StringUtils.isNotBlank(filters.getConfidence()) && !"undefined".equalsIgnoreCase(filters.getConfidence())) {
			query = query.concat(" and confidence = '" + filters.getConfidence() + "' ");
		}
		if (StringUtils.isNotBlank(filters.getSection()) && !"undefined".equalsIgnoreCase(filters.getSection())) {
			query = query.concat(" and actual_tcs_section = '" + filters.getSection() + "' ");
		}
		if (StringUtils.isNotBlank(filters.getDerivedSection())
				&& !"undefined".equalsIgnoreCase(filters.getDerivedSection())) {
			query = query.concat(" and derived_tcs_section = '" + filters.getDerivedSection() + "' ");
		}

		return namedParameterJdbcTemplate.queryForObject(query, parameters, BigInteger.class);
	}

	/**
	 * 
	 * @param year
	 * @param month
	 * @param tan
	 * @param residentType
	 * @return
	 */
	public BigInteger findAllPaymentCount(int assessmentYear, int assessmentMonth, String tan, String residentType) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("assessmentMonth", assessmentMonth);
		parameters.put("tan", tan);
		// parameters.put("resType", resType);
		/*
		 * if ("Y".equalsIgnoreCase(resType)) { return
		 * namedParameterJdbcTemplate.query(String.format(tcsQueries.get(
		 * "get_payment_resident_nonresident_Y")), parameters, new PaymentRowMapper());
		 * } else { return
		 * namedParameterJdbcTemplate.query(String.format(tcsQueries.get(
		 * "get_payment_resident_nonresident_N")), parameters, new PaymentRowMapper());
		 * }
		 */
		String query = String.format(tcsQueries.get("get_payment_resident_nonresident_count"));
		return namedParameterJdbcTemplate.queryForObject(query, parameters, BigInteger.class);
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
	public BigInteger findAllPaymentNamesCount(int assessmentYear, int assessmentMonth, String tan, String residentType,
			String collecteeName) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("assessmentYear", assessmentYear);
		parameters.put("assessmentMonth", assessmentMonth);
		parameters.put("tan", tan);
		// parameters.put("resType", resType);
		parameters.put("collecteeName", collecteeName);
		/*
		 * if ("Y".equalsIgnoreCase(resType)) { return namedParameterJdbcTemplate.query(
		 * String.format(tcsQueries.get("get_payment_resident_nonresident_byDeductee_Y")
		 * ), parameters, new PaymentRowMapper()); } else { return
		 * namedParameterJdbcTemplate.query(
		 * String.format(tcsQueries.get("get_payment_resident_nonresident_byDeductee_N")
		 * ), parameters, new PaymentRowMapper()); }
		 */
		String query = String.format(tcsQueries.get("get_payment_resident_nonresident_name_count"));
		BigInteger count = namedParameterJdbcTemplate.queryForObject(query, parameters, BigInteger.class);
		return count == null ? BigInteger.ZERO : count;
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param tan
	 * @param list
	 * @param activeFlag
	 * @return
	 */
	public List<TcsPaymentDTO> findPaymentByYearAndTanAndId(int assessmentYear, String tan, Integer id,
			boolean activeFlag) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("year", assessmentYear);
		parameters.put("tan", tan);
		parameters.put("id", id);
		parameters.put("activeFlag", activeFlag == true ? 1 : 0);
		return namedParameterJdbcTemplate.query(String.format(tcsQueries.get("find_payment_by_year_tan_ids")),
				parameters, new PaymentRowMapper());
	}

	/**
	 * DAO method to get collectee residential status using collectee code
	 * 
	 * @param collecteeCode
	 * @param collectorTan
	 * @return
	 */
	public Integer getCollecteeResidentialStatus(String collecteeCode, String collectorTan) {
		logger.info("DAO method executing to get collectee residential status with collectee code {}", collecteeCode);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("collecteeCode", collecteeCode);
		parameters.put("tan", collectorTan);
		return namedParameterJdbcTemplate.queryForObject(
				String.format(tcsQueries.get("get_Collectee_ResidentialStatus")), parameters, Integer.class);
	}

	/**
	 * 
	 * @param tan
	 * @param assessemtYear
	 * @param batchUploadId
	 * @return
	 */
	public List<TcsPaymentDTO> getCrErrorRecords(String tan, Integer assessemtYear, Integer batchUploadId) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("year", assessemtYear);
		parameters.put("tan", tan);
		parameters.put("batchUploadId", batchUploadId);
		return namedParameterJdbcTemplate.query(String.format(tcsQueries.get("tcs_get_payment_cr_error_records")),
				parameters, new PaymentRowMapper());
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param deductorTan
	 * @param invoiceLineItemId
	 * @return
	 */
	public List<TcsPaymentDTO> findByYearTanId(int assessmentYear, String deductorTan, Integer invoiceLineItemId) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("year", assessmentYear);
		parameters.put("tan", deductorTan);
		parameters.put("id", invoiceLineItemId);
		return namedParameterJdbcTemplate.query(String.format(tcsQueries.get("find_tcs_payment_by_year_tan_id")),
				parameters, new PaymentRowMapper());
	}

	/**
	 * This method for batch update in payment table.
	 * 
	 * @param tcsPaymentList
	 */
	@org.springframework.transaction.annotation.Transactional
	public void batchUpdate(List<TcsPaymentDTO> tcsPaymentList) {
		String updateQuery = "UPDATE Transactions.tcs_payment SET "
				+ "	surcharge_amount = :surchargeAmount, itcess_amount = :itcessAmount, action = :action, "
				+ "	final_tcs_amount = :finalTcsAmount, final_tcs_rate = :finalTcsRate, final_tcs_section = :finalTcsSection, "
				+ "	has_mismatch = :hasMismatch, active = :active, is_exempted = :isExempted, nature_of_income = :natureOfIncome,"
				+ " noi_id = :noiId, final_reason = :finalReason, error_reason= :errorReason, consumed_amount = :consumedAmount , "
				+ " under_threshold = :underThreshold WHERE id = :id ";
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(tcsPaymentList);

		int[] updateCounts = namedParameterJdbcTemplate.batchUpdate(updateQuery, batch);
		logger.info("payment batch updated successfully {}", tcsPaymentList.size());
	}

	/**
	 * This method for get all non resident collectee based on tan
	 * 
	 * @param tan
	 * @return
	 */
	public List<String> getCollecteeNonResidentialStatus(String tan) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tan", tan);
		return namedParameterJdbcTemplate.queryForList(
				String.format(tcsQueries.get("get_collectee_non_residential_status")), parameters, String.class);
	}

	/**
	 * 
	 * @param collectorPan
	 * @param year
	 * @return
	 */
	public List<CollecteeNoiThresholdLedger> getCollecteeNoiThresholdLedger(String collectorPan, int year) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("year", year);
		parameters.put("collectorPan", collectorPan);
		return namedParameterJdbcTemplate.query(String.format(tcsQueries.get("get_collectee_noi_threshold")),
				parameters, new CollecteeNoiThresholdLedgerRowMapper());
	}

	/**
	 * This method for batch update for collectee noi theshold ledger table
	 * 
	 * @param listNoiThesholdLedger
	 */
	public void batchUpdateCollecteeNoiThesholdLedger(List<CollecteeNoiThresholdLedger> listNoiThesholdLedger) {
		String updateQuery = "UPDATE Transactions.collectee_noi_threshold_ledger SET "
				+ "	amount_utilized = :amountUtilized, threshold_reached = :thresholdReached, modified_by = :modifiedBy, "
				+ "	modified_date = :modifiedDate WHERE id = :id ";
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(listNoiThesholdLedger);

		int[] updateCounts = namedParameterJdbcTemplate.batchUpdate(updateQuery, batch);
		logger.info("colletee noi batch updated successfully {}", updateCounts);
	}

	/**
	 * 
	 * @param paymentIds
	 * @param tan
	 * @param collectorPan
	 * @return
	 */
	public List<TcsPaymentDTO> getPaymentList(List<Integer> paymentIds, String tan, String collectorPan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("tan", tan);
		parameters.put("collectorPan", collectorPan);
		parameters.put("paymentIds", paymentIds);
		return namedParameterJdbcTemplate.query(String.format(tcsQueries.get("get_tcs_payment_basedon_ids")),
				parameters, new PaymentRowMapper());
	}

}
