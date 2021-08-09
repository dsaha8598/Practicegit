package com.ey.in.tds.ingestion.jdbc.dao;


import java.io.Serializable;
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
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.InvoiceMetaNr;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.ingestion.jdbc.rowmapper.InvoiceMetaNrRowMapper;

/**
 * 
 * @author scriptbees.
 *
 */
@Repository
public class InvoiceMetaNrDAO implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired(required = false)
	private HashMap<String, String> queries;

	@Autowired
	private DataSource dataSource;

	private JdbcTemplate jdbcTemplate;

	private SimpleJdbcInsert simpleJdbcInsert;

	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@PostConstruct
	private void postConstruct() {
		jdbcTemplate = new JdbcTemplate(dataSource);
		namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("invoice_meta_nr").withSchemaName("Transactions")
				.usingGeneratedKeyColumns("invoice_meta_nr_id");

	}

	public InvoiceMetaNr save(InvoiceMetaNr dto) {
		logger.info("method executing to save record to invoice_meta_nr table   {}");
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(dto);
		dto.setInvoiceMetaNrId(simpleJdbcInsert.executeAndReturnKey(namedParameters).intValue());
		if(dto.getInvoiceMetaNrId()!=null) {
		logger.info("record inserted into invoice_meta_nr table   {}");
		}
		else {
			logger.info("record insertion failed for  invoice_meta_nr table   {}");
		}
		return dto;
	}
	public InvoiceMetaNr update(InvoiceMetaNr dto) {
		logger.info("DAO method executing to save user data ");

		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(dto);
		int status = namedParameterJdbcTemplate.update(String.format(queries.get("update_invoiceMeta_Nr")), namedParameters);

		if (status != 0) {
			logger.info("AoMaster data is updated for ID " + dto.getInvoiceMetaNrId());
		} else {
			logger.info("No record found with ID " + dto.getInvoiceMetaNrId());
		}
		logger.info("DAO method execution successful {}");
		return dto;
	}
	public List<InvoiceMetaNr> findByLineItemId(Integer lineItemId) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String,Object> parameters=new HashMap<String,Object>();
		parameters.put("lineItemId", lineItemId);
		return namedParameterJdbcTemplate.query(String.format(queries.get("find_invoiceMetaNr_By_LineItemId")), parameters,new InvoiceMetaNrRowMapper());
		//return Optional.ofNullable(cassandraOperations.selectOne(
		//		query(where("invoice_meta_nr_line_item_id").is(lineItemId)).withAllowFiltering(), InvoiceMetaNr.class));
	}
	
	public List<InvoiceMetaNr> findByDeducteeNameTin(String deducteeName, String deducteeTin,
			Pagination pagination) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String,Object> parameters=new HashMap<String,Object>();
		parameters.put("deducteeName", deducteeName);
		parameters.put("deducteeTin", deducteeTin);
		return namedParameterJdbcTemplate.query(String.format(queries.get("find_invoice_Meta_Nr_By_DeducteeName_Tin")), parameters,new InvoiceMetaNrRowMapper());
		
		//return super.getPaginatedData(query(where("invoice_meta_nr_deductee_name").is(deducteeName))
		//		.and(where("invoice_meta_nr_deductee_tin").is(deducteeTin)).withAllowFiltering(), pagination);
	}
	
	public List<InvoiceMetaNr> findByNatureOfRemittance(Integer invoiceMetaNrId, String natureOfRemittance) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String,Object> parameters=new HashMap<String,Object>();
		parameters.put("invoiceMetaNrId", invoiceMetaNrId);
		parameters.put("natureOfRemittance", natureOfRemittance);
		return namedParameterJdbcTemplate.query(String.format(queries.get("find_invoinceMetaNr_By_NatureOfRemittance")), parameters,new InvoiceMetaNrRowMapper());
		
	/*	return Optional.ofNullable(cassandraOperations.selectOne(query(where("invoice_meta_nr_id").is(invoiceMetaNrId))
				.and(where("invoice_meta_nr_invoice_nature_of_remittance").is(natureOfRemittance)).withAllowFiltering(),
				InvoiceMetaNr.class));   */
	}
	public List<InvoiceMetaNr> findByYearMonthTanAndID( int year, int month,String tan, Integer id) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String,Object> parameters=new HashMap<String,Object>();
		parameters.put("year", year);
		parameters.put("month", month);
		parameters.put("tan", tan);
		parameters.put("id", id);
		return namedParameterJdbcTemplate.query(String.format(queries.get("find_By_Year_Month_Tan_And_ID")), parameters,new InvoiceMetaNrRowMapper());
		
	
	}

}
