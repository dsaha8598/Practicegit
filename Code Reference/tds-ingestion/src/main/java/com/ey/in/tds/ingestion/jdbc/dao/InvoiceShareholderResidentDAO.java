package com.ey.in.tds.ingestion.jdbc.dao;

import java.math.BigInteger;
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

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.dividend.InvoiceShareholderResident;
import com.ey.in.tds.common.domain.transactions.jdbc.rowmapper.InvoiceShareholderResidentRowMapper;
import com.ey.in.tds.common.dto.DeducteeDetailDTO;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.ingestion.jdbc.rowmapper.LineItemRowMapper;

@Repository
public class InvoiceShareholderResidentDAO {

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
		simplejdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("invoice_shareholder_resident")
				.withSchemaName("Transactions").usingGeneratedKeyColumns("invoice_id");
	}

	/**
	 * finding resident invoice share holder using year,assesment month, deductor
	 * master pan AND section
	 * 
	 * @param assessmentYear
	 * @param assessmentMonth
	 * @param deductorMasterPan
	 * @param pagination
	 * @return
	 */
	public List<InvoiceShareholderResident> findAllResident(int assessmentYear, int assessmentMonth,
			String deductorMasterPan, Pagination pagination, String section) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		logger.info("DAO method executng to get the resident invoice share holder{}");
		String paginationOrder = CommonUtil.getPagination("invoice_id", pagination.getPageNumber(),
				pagination.getPageSize(), "DESC");
		String query = String.format(queries.get("get_resrident_invoice_shareholder_by_pan_year_and_month"));
		if (!section.equalsIgnoreCase("nosectionselected")) {
			query = query + " AND tds_section=:section ";
		}
		query = query.concat(paginationOrder);
		Map<String, Object> parameter = new HashMap<>();
		parameter.put("assessmentYear", assessmentYear);
		parameter.put("assessmentMonth", assessmentMonth);
		parameter.put("deductorMasterPan", deductorMasterPan);
		parameter.put("section", section);
		return namedJdbcTemplate.query(query, parameter, new InvoiceShareholderResidentRowMapper());

	}

	/**
	 * finding count of resident invoice share holder using year,assesment month,
	 * deductor master pan AND section
	 * 
	 * @param assessmentYear
	 * @param assessmentMonth
	 * @param deductorMasterPan
	 * @param pagination
	 * @return
	 */
	public BigInteger findAllResidentCount(int assessmentYear, int assessmentMonth, String deductorMasterPan,
			String section) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		logger.info("DAO method executng to get the resident invoice share holder count{}");
		String query = String.format(queries.get("get_countOf_resrident_invoice_shareholder_by_pan_year_and_month"));
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
	 * to get the resident invoice share holder using id and pan
	 * 
	 * @param id
	 * @param deductorPan
	 * @return
	 */
	public List<InvoiceShareholderResident> findByIdPan(Integer id, String deductorPan) {
		logger.info("DAO method executing to get the resident invoice share holder by id =" + id + "{}");
		Map<String, Object> parameter = new HashMap<>();
		parameter.put("id", id);
		parameter.put("deductorPan", deductorPan);
		return namedJdbcTemplate.query(String.format(queries.get("get_resrident__invoice_shareholder_by_id_pan")),
				parameter, new InvoiceShareholderResidentRowMapper());
	}

	/**
	 * to perform batch update in InvoiceShareholderNonResident table
	 * 
	 * @param invoiceShareholderNonResident
	 */
	public void batchUpdateInvoiceShareholderResident(
			final List<InvoiceShareholderResident> invoiceShareholderResident) {
		logger.info("DAO method executing to perform batch update {}");
		String query = String.format(queries.get("update_invoice_shareholder_resident_by_id"));
		SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(invoiceShareholderResident.toArray());
		int[] updateCounts = namedJdbcTemplate.batchUpdate(query, batch);
		if (updateCounts.length > 0) {
			logger.info("invoiceShareholderResident Records updated {}");
		} else {
			logger.info("failed to update invoiceShareholderResident Records {}");
		}
	}

	/**
	 * to update the InvoiceShareholderNonResident
	 * 
	 * @param dto
	 * @return
	 */
	public InvoiceShareholderResident updateInvoiceShareholderResident(InvoiceShareholderResident dto) {
		logger.info("DAO method executing to update InvoiceShareholderResident data ");
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(dto);
		int status = namedJdbcTemplate.update(String.format(queries.get("update_invoice_shareholder_resident_by_id")),
				namedParameters);
		if (status != 0) {
			logger.info("Resident Invoice share holder  data is updated for ID " + dto.getId());
		} else {
			logger.info("No record found with ID " + dto.getId());
		}
		logger.info("DAO method execution successful to Update Resident Invoice share holder{}");
		return dto;
	}

	/**
	 * to get the resident invoice sharec holder by year and document posting month
	 * 
	 * @param deductorTan
	 * @param assessmentYear
	 * @param month1
	 * @param month2
	 * @param month3
	 * @return
	 */
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

	public List<InvoiceShareholderResident> findByYearTanId(int assessmentYear, String tan, Integer id) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("year", assessmentYear);
		parameters.put("tan", tan);
		parameters.put("id", id);
		return namedJdbcTemplate.query(String.format(queries.get("find_resident_invoice_shareholder_By_Year_Tan_Id")),
				parameters, new InvoiceShareholderResidentRowMapper());
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
		return namedJdbcTemplate.query(String.format(queries.get("get_resident_invoice_shareholder_by_receipt")),
				parameters, new LineItemRowMapper());
	}
	
	 public List<InvoiceShareholderResident> findAllByPanAndYear(String deductorPan, Integer year) {
		 logger.debug("tenant id : {}", MultiTenantContext.getTenantId());
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("assessmentYear", year);
			parameters.put("deductorPan", deductorPan);
			return namedJdbcTemplate.query(String.format(queries.get("resident_find_All_By_Pan_And_Year_and_isParent")),
					parameters, new InvoiceShareholderResidentRowMapper());

	    }
	 public List<InvoiceShareholderResident> findAllApprovedRecordsByPanAndYear(String deductorPan, Integer year) {
		 logger.debug("tenant id : {}", MultiTenantContext.getTenantId());
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("assessmentYear", year);
			parameters.put("deductorPan", deductorPan);
			return namedJdbcTemplate.query(String.format(queries.get("resident_find_All_By_Pan_And_Year_and_isParent_processed")),
					parameters, new InvoiceShareholderResidentRowMapper());

	    }
	 
	 public List<InvoiceShareholderResident> findByTransactionCountPanFolioNo(Integer transactionCount, String folioNumber, String deductorPan) {
		 Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("transactionCount", transactionCount);
			parameters.put("folioNumber", folioNumber);
			parameters.put("deductorPan", deductorPan);//
			return namedJdbcTemplate.query(String.format(queries.get("resident_find_By_Transaction_Count_Pan_FolioNo")),
					parameters, new InvoiceShareholderResidentRowMapper());
	    }
	 public Map<String, Integer> getActiveAndInactiveResidentShareHoldersCounts(String deductorPan, String type) {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("deductorPan", deductorPan);
			String query = "select count(1) as activeCount from Transactions.invoice_shareholder_resident where deductor_master_pan =:deductorPan AND active =1 ";
			Integer activeCount = namedJdbcTemplate.queryForObject(query, parameters, Integer.class);
			query = "select count(1) as inactiveCount from Transactions.invoice_shareholder_resident where deductor_master_pan =:deductorPan AND active =0";
			Integer inactiveCount = namedJdbcTemplate.queryForObject(query, parameters, Integer.class);
			Map<String, Integer> deducteeCounts = new HashMap<>();
			deducteeCounts.put("active", activeCount);
			deducteeCounts.put("inactive", inactiveCount);
			return deducteeCounts;
		}

}
