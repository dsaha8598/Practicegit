package com.ey.in.tds.jdbc.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.dto.PermissionsDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.RoleDTO;
import com.ey.in.tds.jdbc.rowmapper.RoleRowMapper;

/**
 * Repository to perform data base operation with Role table
 * 
 * @author scriptbees
 *
 */
@Repository
public class RoleDAO {
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
	      jdbcInsert = new SimpleJdbcInsert(dataSource)
	              .withTableName("role")
					.withSchemaName("Onboarding").usingGeneratedKeyColumns("role_id");
	}


	/**
	 * method to insert the data int Role table
	 * 
	 * @param dao
	 */
	public RoleDTO save(RoleDTO dao) {
		logger.info("insert method execution started  {}");
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("role_id", dao.getRoleId());
		parameters.put("name", dao.getRoleName());
		parameters.put("active", dao.getActive());
		parameters.put("permissionnames", dao.getPermissionNames());  
		parameters.put("createdby", dao.getCreatedBy());
		parameters.put("modifiedby", dao.getModifiedBy());
		parameters.put("createddate", new Date());
		parameters.put("modifieddate", new Date());
		parameters.put("deductor_pan", dao.getDeductorPan());
		parameters.put("module_type_id", dao.getModuleType());
		int roleId = jdbcInsert.executeAndReturnKey(parameters).intValue();
		dao.setRoleId(roleId);

		logger.info("Record inserted to Role table {}");
		return dao;
	}

	public List<RoleDTO> getRoleByID(Integer roleKey) {
		return jdbcTemplate.query(String.format(queries.get("role_retriev_by_id")),
				new RoleRowMapper(), roleKey); // TODO : dyanmic value to be assigned

	}

	public List<RoleDTO> getRoleByRoleNameAndDeductorPan(String roleName, String deductorPan,Integer moduleType) {
		return jdbcTemplate.query(String.format(queries.get("role_retriev_by_name_deductorPan")),
				new RoleRowMapper(), roleName, deductorPan,moduleType);
	}

	public List<RoleDTO> findAll(String moduleType) {
		return jdbcTemplate.query(String.format(queries.get("role_allData")), new RoleRowMapper(), moduleType);
	}

	public List<RoleDTO> fetchAllByDeductorPan(String deductorPan) {
		return jdbcTemplate.query(String.format(queries.get("role_by_deductorPan")),
				new RoleRowMapper(), deductorPan);
	}
	
	public List<RoleDTO> fetchAllRolesByDeductorPan(String deductorPan, String moduleType) {
		return jdbcTemplate.query(String.format(queries.get("roles_by_deductorPan")),
				new RoleRowMapper(), deductorPan, moduleType);
	}
	
	public int updatePermissions(String tenantId, PermissionsDTO roleData) {
		logger.debug("tenant id : {}", tenantId);
		String query = String.format(queries.get("update_permissions"));
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("roleId", roleData.getRoleId());
		parameters.put("permissionNames", roleData.getPermissionNames().toString());
		parameters.put("modifiedDate", roleData.getModifiedDate());
		return namedParameterJdbcTemplate.update(query, parameters);
	}

}
