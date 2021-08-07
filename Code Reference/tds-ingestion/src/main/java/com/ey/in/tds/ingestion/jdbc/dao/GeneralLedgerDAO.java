package com.ey.in.tds.ingestion.jdbc.dao;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.GeneralLedger;
import com.ey.in.tds.common.domain.transactions.jdbc.rowmapper.GeneralLedgerRowMapper;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.core.util.CommonUtil;

@Repository
public class GeneralLedgerDAO {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired(required = false)
	private HashMap<String, String> queries;

	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	private DataSource dataSource;

	@PostConstruct
	private void postConstruct() {
		namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}

	public List<GeneralLedger> invoicesFoundInGLByBatchId(Integer batchId, boolean tdsExist, boolean notAbleToDetermine,
			boolean isAccountingCode, boolean recordFound, boolean mismatch, boolean amountMismatch,
			Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String paginationOrder = CommonUtil.getPagination("general_ledger_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("batchId", batchId);
		parameter.put("tdsExist", tdsExist ? 1 : 0);
		parameter.put("notAbleToDetermine", notAbleToDetermine ? 1 : 0);
		parameter.put("isAccountingCode", isAccountingCode ? 1 : 0);
		parameter.put("recordFound", recordFound ? 1 : 0);
		parameter.put("mismatch", mismatch ? 1 : 0);
		parameter.put("amountMismatch", amountMismatch ? 1 : 0);
		String query = String.format(queries.get("invoices_Found_In_GLBy_BatchId")).concat(paginationOrder);

		return namedParameterJdbcTemplate.query(query, parameter, new GeneralLedgerRowMapper());

	}

	public BigInteger getCountOFinvoicesFoundInGLByBatchId(Integer batchId, boolean tdsExist,
			boolean notAbleToDetermine, boolean isAccountingCode, boolean recordFound, boolean mismatch,
			boolean amountMismatch) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("batchId", batchId);
		parameter.put("tdsExist", tdsExist ? 1 : 0);
		parameter.put("notAbleToDetermine", notAbleToDetermine ? 1 : 0);
		parameter.put("isAccountingCode", isAccountingCode ? 1 : 0);
		parameter.put("recordFound", recordFound ? 1 : 0);
		parameter.put("mismatch", mismatch ? 1 : 0);
		parameter.put("amountMismatch", amountMismatch ? 1 : 0);
		return namedParameterJdbcTemplate.queryForObject(
				String.format(queries.get("get_count_invoices_Found_In_GLBy_BatchId")), parameter, BigInteger.class);
	}

	public List<GeneralLedger> invoicesFoundInGLByYearMonthTan(Integer year, List<Integer> months, String tan,
			boolean tdsExist, boolean notAbleToDetermine, boolean isAccountingCode, boolean recordFound,
			boolean mismatch, boolean amountMismatch, Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String paginationOrder = CommonUtil.getPagination("general_ledger_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("year", year);
		parameter.put("tan", tan);
		parameter.put("months", months);
		parameter.put("tdsExist", tdsExist ? 1 : 0);
		parameter.put("notAbleToDetermine", notAbleToDetermine ? 1 : 0);
		parameter.put("isAccountingCode", isAccountingCode ? 1 : 0);
		parameter.put("recordFound", recordFound ? 1 : 0);
		parameter.put("mismatch", mismatch ? 1 : 0);
		parameter.put("amountMismatch", amountMismatch ? 1 : 0);
		//
		String query = String.format(queries.get("invoices_Found_In_GL_By_Year_Month_Tan")).concat(paginationOrder);

		return namedParameterJdbcTemplate.query(query, parameter, new GeneralLedgerRowMapper());
	}

	public BigInteger getCountOFinvoicesFoundInGLByYearMonthTan(Integer year, List<Integer> months, String tan,
			boolean tdsExist, boolean notAbleToDetermine, boolean isAccountingCode, boolean recordFound,
			boolean mismatch, boolean amountMismatch, Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		;
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("year", year);
		parameter.put("tan", tan);
		parameter.put("months", months);
		parameter.put("tdsExist", tdsExist ? 1 : 0);
		parameter.put("notAbleToDetermine", notAbleToDetermine ? 1 : 0);
		parameter.put("isAccountingCode", isAccountingCode ? 1 : 0);
		parameter.put("recordFound", recordFound ? 1 : 0);
		parameter.put("mismatch", mismatch ? 1 : 0);
		parameter.put("amountMismatch", amountMismatch ? 1 : 0);
		//
		String query = String.format(queries.get("get_count_invoices_Found_In_GL_By_Year_Month_Tan"));

		return namedParameterJdbcTemplate.queryForObject(query, parameter, BigInteger.class);
	}

	public List<GeneralLedger> invoicesFoundInGLByYear(Integer year, String tan, boolean tdsExist,
			boolean notAbleToDetermine, boolean isAccountingCode, boolean recordFound, boolean mismatch,
			boolean amountMismatch, Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String paginationOrder = CommonUtil.getPagination("general_ledger_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("year", year);
		parameter.put("tan", tan);
		parameter.put("tdsExist", tdsExist ? 1 : 0);
		parameter.put("notAbleToDetermine", notAbleToDetermine ? 1 : 0);
		parameter.put("isAccountingCode", isAccountingCode ? 1 : 0);
		parameter.put("recordFound", recordFound ? 1 : 0);
		parameter.put("mismatch", mismatch ? 1 : 0);
		parameter.put("amountMismatch", amountMismatch ? 1 : 0);
		String query = String.format(queries.get("invoices_Found_InGL_ByYear")).concat(paginationOrder);

		return namedParameterJdbcTemplate.query(query, parameter, new GeneralLedgerRowMapper());

	}

	public BigInteger getCountOFinvoicesFoundInGLByYear(Integer year, String tan, boolean tdsExist,
			boolean notAbleToDetermine, boolean isAccountingCode, boolean recordFound, boolean mismatch,
			boolean amountMismatch, Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("year", year);
		parameter.put("tan", tan);
		parameter.put("tdsExist", tdsExist ? 1 : 0);
		parameter.put("notAbleToDetermine", notAbleToDetermine ? 1 : 0);
		parameter.put("isAccountingCode", isAccountingCode ? 1 : 0);
		parameter.put("recordFound", recordFound ? 1 : 0);
		parameter.put("mismatch", mismatch ? 1 : 0);
		parameter.put("amountMismatch", amountMismatch ? 1 : 0);
		String query = String.format(queries.get("get_count_invoices_Found_InGL_ByYear"));

		return namedParameterJdbcTemplate.queryForObject(query, parameter, BigInteger.class);

	}

	public BigInteger findByBatchUploadIdAndTdsFoundCount(Integer batchId, boolean tdsExist) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("batchId", batchId);
		parameter.put("tdsExist", tdsExist);
		return namedParameterJdbcTemplate.queryForObject(
				String.format(queries.get("find_By_BatchUploadId_And_Tds_Found_Count")), parameter, BigInteger.class);
	}

	public List<GeneralLedger> findByBatchUploadIdAndTdsFound(Integer batchId, boolean tdsExist,
			Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String paginationOrder = CommonUtil.getPagination("general_ledger_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("batchId", batchId);
		parameter.put("tdsExist", tdsExist);
		String query = String.format(queries.get("find_By_BatchUploadId_And_Tds_Found")).concat(paginationOrder);

		return namedParameterJdbcTemplate.query(query, parameter, new GeneralLedgerRowMapper());
	}

	public List<GeneralLedger> findByYearMonthTanAndTdsFound(Integer year, List<Integer> months, String tan,
			boolean tdsExist, Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String paginationOrder = CommonUtil.getPagination("general_ledger_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("year", year);
		parameter.put("tdsExist", tdsExist);
		parameter.put("months", months);
		parameter.put("tan", tan);
		String query = String.format(queries.get("find_By_Year_Month_Tan_And_Tds_Found")).concat(paginationOrder);

		return namedParameterJdbcTemplate.query(query, parameter, new GeneralLedgerRowMapper());

	}

	public BigInteger getCountOffindByYearMonthTanAndTdsFound(Integer year, List<Integer> months, String tan,
			boolean tdsExist) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("year", year);
		parameter.put("tdsExist", tdsExist);
		parameter.put("months", months);
		parameter.put("tan", tan);

		return namedParameterJdbcTemplate.queryForObject(
				String.format(queries.get("find_By_Year_Month_Tan_And_Tds_Found_COUNT")), parameter, BigInteger.class);

	}

	public List<GeneralLedger> findByYearAndTdsFound(Integer year, String tan, boolean tdsExist,
			Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String paginationOrder = CommonUtil.getPagination("general_ledger_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("year", year);
		parameter.put("tdsExist", tdsExist);
		parameter.put("tan", tan); //
		String query = String.format(queries.get("find_By_Year_And_Tds_Found")).concat(paginationOrder);
		return namedParameterJdbcTemplate.query(query, parameter, new GeneralLedgerRowMapper());

	}

	public BigInteger getCountOffindByYearAndTdsFound(Integer year, String tan, boolean tdsExist) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("year", year);
		parameter.put("tdsExist", tdsExist);
		parameter.put("tan", tan); //
		return namedParameterJdbcTemplate.queryForObject(String.format(queries.get("find_By_Year_And_Tds_Found_count")),
				parameter, BigInteger.class);

	}

	public List<GeneralLedger> nadAndAccountCodeExists(Integer batchId, boolean notAbleToDetermine,
			boolean isAccountingCode, Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String paginationOrder = CommonUtil.getPagination("general_ledger_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("batchId", batchId);
		parameter.put("notAbleToDetermine", notAbleToDetermine ? 1 : 0);
		parameter.put("isAccountingCode", isAccountingCode ? 1 : 0); //
		String query = String.format(queries.get("nad_And_Account_Code_Exists")).concat(paginationOrder);
		return namedParameterJdbcTemplate.query(query, parameter, new GeneralLedgerRowMapper());
	}

	public BigInteger getCountOfNadAndAccountCodeExists(Integer batchId, boolean notAbleToDetermine,
			boolean isAccountingCode) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("batchId", batchId);
		parameter.put("notAbleToDetermine", notAbleToDetermine ? 1 : 0);
		parameter.put("isAccountingCode", isAccountingCode ? 1 : 0); //
		return namedParameterJdbcTemplate.queryForObject(
				String.format(queries.get("get_count_of_nad_And_Account_Code_Exists")), parameter, BigInteger.class);
	}

	public List<GeneralLedger> nadAndAccountCodeExistsByYearMonthTan(Integer year, List<Integer> months, String tan,
			boolean notAbleToDetermine, boolean isAccountingCode, Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String paginationOrder = CommonUtil.getPagination("general_ledger_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("year", year);
		if (!months.isEmpty()) {
			parameter.put("months", months);
		}
		parameter.put("tan", tan);
		parameter.put("notAbleToDetermine", notAbleToDetermine ? 1 : 0);
		parameter.put("isAccountingCode", isAccountingCode ? 1 : 0); //
		String query = String.format(queries.get("nad_And_Account_Code_Exists_By_Year_Month_Tan"))
				.concat(paginationOrder);
		return namedParameterJdbcTemplate.query(query, parameter, new GeneralLedgerRowMapper());

	}

	public BigInteger getCountOfNadAndAccountCodeExistsByYearMonthTan(Integer year, List<Integer> months, String tan,
			boolean notAbleToDetermine, boolean isAccountingCode) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("year", year);
		if (!months.isEmpty()) {
			parameter.put("months", months);
		}
		parameter.put("tan", tan);
		parameter.put("notAbleToDetermine", notAbleToDetermine ? 1 : 0);
		parameter.put("isAccountingCode", isAccountingCode ? 1 : 0); //
		return namedParameterJdbcTemplate.queryForObject(
				String.format(queries.get("get_count_of_nad_And_Account_Code_Exists_By_Year_Month_Tan")), parameter,
				BigInteger.class);

	}

	public List<GeneralLedger> nadAndAccountCodeExistsByYear(Integer year, String tan, boolean notAbleToDetermine,
			boolean isAccountingCode, Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		String paginationOrder = CommonUtil.getPagination("general_ledger_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("year", year);
		parameter.put("tan", tan);
		parameter.put("notAbleToDetermine", notAbleToDetermine ? 1 : 0);
		parameter.put("isAccountingCode", isAccountingCode ? 1 : 0); //

		String query = String.format(queries.get("nad_And_Account_Code_Exists_By_Year")).concat(paginationOrder);
		return namedParameterJdbcTemplate.query(query, parameter, new GeneralLedgerRowMapper());
	}

	public BigInteger getCountOfNadAndAccountCodeExistsByYear(Integer year, String tan, boolean notAbleToDetermine,
			boolean isAccountingCode) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("year", year);
		parameter.put("tan", tan);
		parameter.put("notAbleToDetermine", notAbleToDetermine ? 1 : 0);
		parameter.put("isAccountingCode", isAccountingCode ? 1 : 0); //

		return namedParameterJdbcTemplate.queryForObject(
				String.format(queries.get("get_count_of_nad_And_Account_Code_Exists_By_Year")), parameter,
				BigInteger.class);
	}

	public List<GeneralLedger> getGlSummary(String deductorTan, int year, int month, String glColumns, Integer batchId,
			List<Integer> monthValues, boolean mismatch, boolean amountMismatch, boolean recordFound) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);

		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("year", year);
		parameter.put("tan", deductorTan);
		parameter.put("month", month);
		parameter.put("glColumns", glColumns);
		parameter.put("batchId", batchId);
		if (!monthValues.isEmpty()) {
			parameter.put("months", monthValues);
		}
		parameter.put("mismatch", mismatch ? 1 : 0);
		parameter.put("amountMismatch", amountMismatch ? 1 : 0);
		parameter.put("recordFound", recordFound ? 1 : 0);
		String query = "";

		if (batchId != null) {
			query = String.format(queries.get("gl_with_year_mismatch_amountMismatch_recordFound"));
			return namedParameterJdbcTemplate.query(query, parameter, new GeneralLedgerRowMapper());

		} else if (month > 0 && year > 0 && StringUtils.isNotBlank(deductorTan) && monthValues.isEmpty()) {

			query = String
					.format(queries.get("gl_with_year_tan_month_mismatch_amountMismatch_recordFound_nad_tdsExist"));
			return namedParameterJdbcTemplate.query(query, parameter, new GeneralLedgerRowMapper());

		} else if (year > 0 && StringUtils.isNotBlank(deductorTan) && monthValues.isEmpty()) {
			query = String.format(queries.get("gl_with_year_tan_month_mismatch_amountMismatch_recordFound_nad"));
			return namedParameterJdbcTemplate.query(query, parameter, new GeneralLedgerRowMapper());
		} else {
			query = String
					.format(queries.get("gl_with_year_tan_month_mismatch_amountMismatch_recordFound_nad_month_values"));
			return namedParameterJdbcTemplate.query(query, parameter, new GeneralLedgerRowMapper());
		}

	}

	public List<GeneralLedger> getGlSummaryTdsFound(String deductorTan, int year, int month, String glColumns,
			Integer batchId, List<Integer> monthValues) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("year", year);
		parameter.put("tan", deductorTan);
		parameter.put("month", month);
		parameter.put("glColumns", glColumns);
		parameter.put("batchId", batchId);
		if (!monthValues.isEmpty()) {
			parameter.put("months", monthValues);
		}

		if (batchId != null) {
			return namedParameterJdbcTemplate.query(String.format(queries.get("gl_with_batchUploadId_tds_exist_true")),
					parameter, new GeneralLedgerRowMapper());
		} else if (month > 0 && year > 0 && StringUtils.isNotBlank(deductorTan) && monthValues.isEmpty()) {
			return namedParameterJdbcTemplate.query(String.format(queries.get("gl_with_tan_year_month_tds_exist_true")),
					parameter, new GeneralLedgerRowMapper());

		} else if (year > 0 && StringUtils.isNotBlank(deductorTan) && monthValues.isEmpty()) {
			return namedParameterJdbcTemplate.query(String.format(queries.get("gl_with_tan_year_tds_exist_true")),
					parameter, new GeneralLedgerRowMapper());
		} else {
			return namedParameterJdbcTemplate.query(
					String.format(queries.get("gl_with_tan_year_multiplemonth_tds_exist_true")), parameter,
					new GeneralLedgerRowMapper());
		}
	}

	public List<GeneralLedger> getGlSummaryCodeExists(String deductorTan, int year, int month, String glColumns,
			Integer batchId, List<Integer> monthValues, boolean codeExists) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("year", year);
		parameter.put("tan", deductorTan);
		parameter.put("month", month);
		parameter.put("glColumns", glColumns);
		parameter.put("batchId", batchId);
		if (!monthValues.isEmpty()) {
			parameter.put("months", monthValues);
		}
		parameter.put("codeExists", codeExists ? 1 : 0);
		if (batchId != null) {

			return namedParameterJdbcTemplate.query(
					String.format(queries.get("gl_with_codeExists_batch_upload_id_is_nad_true")), parameter,
					new GeneralLedgerRowMapper());
		} else if (month > 0 && year > 0 && StringUtils.isNotBlank(deductorTan) && monthValues.isEmpty()) {
			return namedParameterJdbcTemplate.query(
					String.format(queries.get("gl_with_codeExists_tan_year_month_is_nad_true")), parameter,
					new GeneralLedgerRowMapper());
		} else if (year > 0 && StringUtils.isNotBlank(deductorTan) && monthValues.isEmpty()) {
			return namedParameterJdbcTemplate.query(
					String.format(queries.get("gl_with_codeExists_tan_year_is_nad_true")), parameter,
					new GeneralLedgerRowMapper());
		} else {//
			return namedParameterJdbcTemplate.query(
					String.format(queries.get("gl_with_codeExists_tan_year_is_nad_true_multipleMonths")), parameter,
					new GeneralLedgerRowMapper());
		}
	}
	
	/**
	 * to get the error records from general ledger table
	 * @param tan
	 * @param assessemtYear
	 * @param batchUploadId
	 * @return
	 */
	public List<GeneralLedger> getGLErrorRecords(String tan, Integer assessemtYear, Integer batchUploadId) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("year", assessemtYear);
		parameters.put("tan", tan);
		parameters.put("batchUploadId", batchUploadId);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_invocice_error_records")), parameters,
				new GeneralLedgerRowMapper());
	}
}
