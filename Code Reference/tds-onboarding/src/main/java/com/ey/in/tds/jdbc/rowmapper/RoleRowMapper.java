package com.ey.in.tds.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;

import com.ey.in.tds.common.onboarding.jdbc.dto.RoleDTO;



/**
 * rowmapper class to map the fields from resultset
 * @author scriptbees
 *
 */
public class RoleRowMapper implements RowMapper<RoleDTO>{

	@Override
	public RoleDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
		RoleDTO dao=new RoleDTO();
	    dao.setRoleId(rs.getInt("roleId"));
	    dao.setRoleName(rs.getString("name"));
	    dao.setDeductorPan(rs.getString("deductorPan"));
	    dao.setActive(rs.getInt("active")==1?true:false);
	    dao.setCreatedBy(rs.getString("createdBy"));
	    dao.setCreatedDate(rs.getDate("createdDate"));
	    dao.setModifiedBy(rs.getString("modifiedBy"));
	    dao.setModifiedDate(rs.getDate("modifiedDate"));	
	    dao.setModuleType(rs.getInt("moduleType"));
	    String[] permissonArray=rs.getString("permisson_Names").replace("[", "").replace("]", "").split(",");
	    List<String> list=new ArrayList<String>();
	    for(int index=0;index<permissonArray.length;index++) {
	    	list.add(permissonArray[index].trim());
	    }
	    dao.setPermissionNames(list);
	    

		return dao;
	}
}
