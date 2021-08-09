package com.ey.in.tds.jdbc.dao;

import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.jdbc.rowmapper.TcsLookUpRowMapper;
import com.ey.in.tds.onboarding.dto.lookup.TcsLookUpDTO;

@Repository
public class TcsLookUpDAO {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private HashMap<String, String> queries;
	
	@Autowired
	private DataSource dataSource;
	
	private JdbcTemplate jdbcTemplate;
	
    private SimpleJdbcInsert jdbcInsert;
	  	
	@PostConstruct
	private void postConstruct() {
		jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("module_lookup").withSchemaName("Onboarding")
				.usingGeneratedKeyColumns("id");
	}
	
	public List<TcsLookUpDTO> fetchModuleLookUp() {
		return jdbcTemplate.query(String.format(queries.get("get_all_module_lookup")),
				new TcsLookUpRowMapper()); 
	}
	
}
