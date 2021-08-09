package com.ey.in.tds.jdbc.dao;

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
import com.ey.in.tds.common.onboarding.jdbc.dto.DeclarationTanFiles;
import com.ey.in.tds.jdbc.rowmapper.DeclarationTanFilesRowMapper;

/**
 * 
 * @author vamsir
 *
 */
@Repository
public class DeclarationTanFilesDAO {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private HashMap<String, String> queries;

	@Autowired
	private DataSource dataSource;
	private JdbcTemplate jdbcTemplate;
	private SimpleJdbcInsert jdbcInsert;
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@PostConstruct
	private void postConstruct() {
		jdbcTemplate = new JdbcTemplate(dataSource);
		namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		jdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("declaration_tan_files")
				.withSchemaName("Onboarding").usingGeneratedKeyColumns("id");
	}

	/**
	 * This method for save Declaration Tan Files
	 *
	 * @param kycDetails
	 */
	public DeclarationTanFiles save(DeclarationTanFiles declarationTanFiles) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(declarationTanFiles);
		declarationTanFiles.setId(jdbcInsert.executeAndReturnKey(namedParameters).intValue());
		logger.info("Record inserted to Declaration Tan Files table {}");
		return declarationTanFiles;

	}

	/**
	 * 
	 * @param deductorPan
	 * @param tan
	 * @param kycId
	 * @return
	 */
	public List<DeclarationTanFiles> getDeclarationTanFileDetails(String deductorPan, String tan, int kycId) {
		String tenantId = MultiTenantContext.getTenantId();
		logger.debug("tenant id : {}", tenantId);
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("deductorPan", deductorPan);
		parameters.put("tan", tan);
		parameters.put("kycId", kycId);
		return namedParameterJdbcTemplate.query(String.format(queries.get("get_declaration_by_kycid")), parameters,
				new DeclarationTanFilesRowMapper());
	}

}
