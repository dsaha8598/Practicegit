package com.ey.in.tds.jdbc.dao;

import java.io.Serializable;
import java.util.HashMap;

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

import com.ey.in.tds.onboarding.dto.collector.CollecteeSectionThresholdLedgerDTO;

@Repository
public class CollecteeSectionThresholdLedgerDAO implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
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
		simpleJdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("collectee_section_threshold_ledger")
				.withSchemaName("Transactions").usingGeneratedKeyColumns("id");
	}
	
	public CollecteeSectionThresholdLedgerDTO save(CollecteeSectionThresholdLedgerDTO collecteeSection) {
		logger.info("insert method execution started  {}");
		SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(collecteeSection);
		collecteeSection.setId(simpleJdbcInsert.executeAndReturnKey(namedParameters).intValue());
		logger.info("Record inserted to collectee_section_threshold_ledger table {}");
		return collecteeSection;
	}

}
