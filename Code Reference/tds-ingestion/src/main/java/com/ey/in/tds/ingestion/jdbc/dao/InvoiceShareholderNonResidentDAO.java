package com.ey.in.tds.ingestion.jdbc.dao;

import java.math.BigInteger;
import java.time.LocalDate;
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
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import com.ey.in.tcs.common.domain.dividend.InvoiceShareholderNonResident;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.dividend.InvoiceShareholderResident;
import com.ey.in.tds.common.domain.transactions.jdbc.rowmapper.InvoiceShareholderNonResidentRowMapper;
import com.ey.in.tds.common.domain.transactions.jdbc.rowmapper.InvoiceShareholderResidentRowMapper;
import com.ey.in.tds.common.dto.DeducteeDetailDTO;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.ingestion.jdbc.rowmapper.LineItemRowMapper;

@Repository
public class InvoiceShareholderNonResidentDAO {

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
		simplejdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("invoice_shareholder_non_resident")
				.withSchemaName("Transactions").usingGeneratedKeyColumns("invoice_id");
	}

	/**
	 * to get the non resident invoice share holder by year,month and pan with
	 * pagination
	 * 
	 * @param assessmentYear
	 * @param assessmentMonth
	 * @param deductorMasterPan
	 * @param pagination
	 * @return
	 */
	public List<InvoiceShareholderNonResident> findAllNonResident(int assessmentYear, int assessmentMonth,
			String deductorMasterPan, Pagination pagination, String section) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		logger.info("DAO method executng to get the resident invoice share holder{}");
		String paginationOrder = CommonUtil.getPagination("invoice_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");
		String query = String.format(queries.get("get_non_resrident_invoice_shareholder_by_pan_year_and_month"));
		if (!section.equalsIgnoreCase("nosectionselected")) {
			query = query + " AND tds_section=:section ";
		}
		query = query.concat(paginationOrder);
		Map<String, Object> parameter = new HashMap<>();
		parameter.put("assessmentYear", assessmentYear);
		parameter.put("assessmentMonth", assessmentMonth);
		parameter.put("deductorMasterPan", deductorMasterPan);
		parameter.put("section", section);
		return namedJdbcTemplate.query(query, parameter, new InvoiceShareholderNonResidentRowMapper());
	}

	/**
	 * to get count of the non resident invoice share holder by year,month and pan
	 * with pagination
	 * 
	 * @param assessmentYear
	 * @param assessmentMonth
	 * @param deductorMasterPan
	 * @param pagination
	 * @return
	 */
	public BigInteger findCountOfAllNonResident(int assessmentYear, int assessmentMonth, String deductorMasterPan,
			String section) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		logger.info("DAO method executng to get the resident invoice count share holder{}");
		String query = String
				.format(queries.get("get_non_resrident_count_of_invoice_shareholder_by_pan_year_and_month"));
		if (!section.equalsIgnoreCase("nosectionselected")) {
			query = query + " AND tds_section=:section ";
		}
		Map<String, Object> parameter = new HashMap<>();
		parameter.put("assessmentYear", assessmentYear);
		parameter.put("assessmentMonth", assessmentMonth);
		parameter.put("deductorMasterPan", deductorMasterPan);
		parameter.put("section", section);
		return namedJdbcTemplate.queryForObject(query, parameter, BigInteger.class);
	}

	/**
	 * to get non resident invoice share holder by assesment year
	 * 
	 * @param assessmentYear
	 * @return
	 */
	public List<InvoiceShareholderNonResident> findAllNonResidentByDateOfPosting(int assessmentYear) {
		logger.info("DAO method executing to get non reident invoice share holder  {}");
		Map<String, Object> parameter = new HashMap<>();
		parameter.put("assessmentYear", assessmentYear);
		return namedJdbcTemplate.query(
				String.format(queries.get("get_non_resrident__invoice_shareholder_by_assesment_year")), parameter,
				new InvoiceShareholderNonResidentRowMapper());
	}

	/**
	 * to get non resident invoice share holder by id and pan
	 * 
	 * @param id
	 * @param deductorPan
	 * @return
	 */
	public List<InvoiceShareholderNonResident> findByIdPan(Integer id, String deductorPan) {
		logger.info("DAO method executing to get non resident invoice share holder record using id {}");
		Map<String, Object> parameter = new HashMap<>();
		parameter.put("id", id);
		parameter.put("deductorPan", deductorPan);
		return namedJdbcTemplate.query(String.format(queries.get("get_non_resrident__invoice_shareholder_by_id_pan")),
				parameter, new InvoiceShareholderNonResidentRowMapper());

	}

	/**
	 * to perform batch update in InvoiceShareholderNonResident table
	 * 
	 * @param invoiceShareholderNonResident
	 */
	public void batchUpdateInvoiceShareholderNonResident(
			final List<InvoiceShareholderNonResident> invoiceShareholderNonResident) {
		logger.info("DAO method executing to perform batch update {}");
		String query = String.format(queries.get("get_non_resrident__invoice_shareholder_by_id_pan"));
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(invoiceShareholderNonResident.toArray());
		int[] updateCounts = namedJdbcTemplate.batchUpdate(query, batch);
		if (updateCounts.length > 0) {
			logger.info("invoiceShareholderNonResident Records updated {}");
		} else {
			logger.info("failed to update invoiceShareholderNonResident Records {}");

		}
	}

	/**
	 * to update the InvoiceShareholderNonResident
	 * 
	 * @param dto
	 * @return
	 */
	public InvoiceShareholderNonResident updateInvoiceShareholderNonResident(InvoiceShareholderNonResident dto) {
		logger.info("DAO method executing to update InvoiceShareholderNonResident data ");
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(dto);
		int status = namedJdbcTemplate
				.update(String.format(queries.get("update_invoice_shareholder_non_resident_for_approve")), namedParameters);
		if (status != 0) {
			logger.info("INVOICE SHARE HOLDER NON RESIDENT data is updated for ID " + dto.getId());
		} else {
			logger.info("No record found with ID " + dto.getId());
		}
		logger.info("DAO method execution successful TO UPDATE INVOICE SHARE HOLDER NON RESIDENT{}");
		return dto;
	}

	/**
	 * to get all non resident by date of posting
	 * 
	 * @param deductorPan
	 * @param dateOfPosting
	 * @param year
	 * @return
	 */
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

	public List<LocalDate> findDistinctDate(String deductorPan) {
		logger.info("DAO method executing to get document posting date{}");
		Map<String, Object> parameter = new HashMap<>();
		parameter.put("pan", deductorPan);
		return namedJdbcTemplate.queryForList(
				String.format(queries.get("get_distinct_dateOfPosting_non_resident_invoice_shareolder")), parameter,
				LocalDate.class);
	}

	public List<InvoiceShareholderNonResident> findByYearTanId(int assessmentYear, String tan, Integer id) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("year", assessmentYear);
		parameters.put("tan", tan);
		parameters.put("id", id);
		return namedJdbcTemplate.query(
				String.format(queries.get("find_non_resident_invoice_shareholder_By_Year_Tan_Id")), parameters,
				new InvoiceShareholderNonResidentRowMapper());
	}

	public List<DeducteeDetailDTO> findByReceiptDetails(String receiptSerailNo, String bsrCode, String receiptDate,
			Integer year, String tan) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("assessmentYear", year);
		parameters.put("deductorTan", tan);
		parameters.put("bsrCode", bsrCode);
		parameters.put("receiptSerailNo", receiptSerailNo);
		parameters.put("receiptDate", receiptDate);
		parameters.put("challanPaid", 1);
		return namedJdbcTemplate.query(String.format(queries.get("get_non_resident_invoice_shareholder_by_receipt")),
				parameters, new LineItemRowMapper());
	}

	 public List<InvoiceShareholderNonResident> findAllByPanAndYear(String deductorPan, Integer year) {
		 logger.debug("tenant id : {}", MultiTenantContext.getTenantId());
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("assessmentYear", year);
			parameters.put("deductorPan", deductorPan);
			return namedJdbcTemplate.query(String.format(queries.get("non_resident_find_All_By_Pan_And_Year_and_isParent")),
					parameters, new InvoiceShareholderNonResidentRowMapper());

	    }
	 
	 public List<InvoiceShareholderNonResident> findAllApprovedRecordsByPanAndYear(String deductorPan, Integer year) {
		 logger.debug("tenant id : {}", MultiTenantContext.getTenantId());
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("assessmentYear", year);
			parameters.put("deductorPan", deductorPan);
			return namedJdbcTemplate.query(String.format(queries.get("non_resident_find_All_By_Pan_And_Year_and_isParent_processed")),
					parameters, new InvoiceShareholderNonResidentRowMapper());

	    }
	 
	 public List<InvoiceShareholderNonResident> findByTransactionCountPanFolioNo(Integer transactionCount, String folioNumber, String deductorPan) {
		 Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("transactionCount", transactionCount);
			parameters.put("folioNumber", folioNumber);
			parameters.put("deductorPan", deductorPan);//
			return namedJdbcTemplate.query(String.format(queries.get("non_resident_find_By_Transaction_Count_Pan_FolioNo")),
					parameters, new InvoiceShareholderNonResidentRowMapper());
	    }
	 public Map<String, Integer> getActiveAndInactiveNonResidentShareHoldersCounts(String deductorPan, String type) {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("deductorPan", deductorPan);
			String query = "select count(1) as activeCount from Transactions.invoice_shareholder_non_resident where deductor_master_pan =:deductorPan AND active =1 ";
			Integer activeCount = namedJdbcTemplate.queryForObject(query, parameters, Integer.class);
			query = "select count(1) as inactiveCount from Transactions.invoice_shareholder_non_resident where deductor_master_pan =:deductorPan AND active =0";
			Integer inactiveCount = namedJdbcTemplate.queryForObject(query, parameters, Integer.class);
			Map<String, Integer> deducteeCounts = new HashMap<>();
			deducteeCounts.put("active", activeCount);
			deducteeCounts.put("inactive", inactiveCount);
			return deducteeCounts;
		}
		
}
