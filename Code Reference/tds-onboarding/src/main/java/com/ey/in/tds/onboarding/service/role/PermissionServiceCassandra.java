package com.ey.in.tds.onboarding.service.role;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ey.in.tds.common.dto.PermissionsDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.RoleDTO;
import com.ey.in.tds.common.onboarding.response.dto.RoleResponseDTO;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.jdbc.dao.RoleDAO;
import com.ey.in.tds.onboarding.dto.role.RolePermisionDTO;

@Service
public class PermissionServiceCassandra {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	RoleDAO repository;

	public RoleDTO saveRolePermisionInCassandra(RolePermisionDTO rolePermisionDTO, String userName) {
		if (rolePermisionDTO.getRoleId() != null) {
			// Optional<RoleCassandra> roleData =
			// roleCassandraRepository.findByRoleId(rolePermisionDTO.getRoleId());
			List<RoleDTO> listDao = repository.getRoleByID(rolePermisionDTO.getRoleId());
			if (!listDao.isEmpty()) {
				listDao.get(0).setActive(false);
				repository.save(listDao.get(0));
			}
		}
		if (rolePermisionDTO.getRoleId() == null && rolePermisionDTO.getRoleName() != null) {
			List<RoleDTO> listDao = repository.getRoleByRoleNameAndDeductorPan(rolePermisionDTO.getRoleName(),
					rolePermisionDTO.getDeductorPan(),rolePermisionDTO.getModuleType());
			if (!listDao.isEmpty()) {
				logger.error("Role name should be unique" + listDao.get(0).toString());
				throw new CustomException("Role name should be unique", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		RoleDTO role = new RoleDTO();
		// RoleCassandra.Key roleKey = new RoleCassandra.Key(UUID.randomUUID());
		// role.setRoleId(UUID.randomUUID());
		role.setRoleName(rolePermisionDTO.getRoleName());
		role.setCreatedDate(new Timestamp(new Date().getTime()));
		role.setModifiedDate(new Timestamp(new Date().getTime()));
		role.setCreatedBy(userName);
		role.setModifiedBy(userName);
		role.setActive(true);
		role.setDeductorPan(rolePermisionDTO.getDeductorPan());
		role.setPermissionNames(rolePermisionDTO.getPermissionNames());
		role.setModuleType(rolePermisionDTO.getModuleType());
		role = repository.save(role);

		return role;
	}

	@Transactional
	public List<RoleDTO> fetchRolePermision(Integer id) {
		List<RoleDTO> roleData = repository.getRoleByID(id);
		return roleData;
	}

	public List<RoleDTO> fetchAllRoles(String moduleType) {
		return repository.findAll(moduleType);
	}

	public List<RoleDTO> getRolesByDeductorPan(String deductorPan) {
		return repository.fetchAllByDeductorPan(deductorPan);
	}

	public List<RoleDTO> getAllRolesByDeductorPan(String deductorPan, String moduleType) {
		return repository.fetchAllRolesByDeductorPan(deductorPan, moduleType);
	}

	/**
	 * to copy values from RoleDTO to RoleResponseDTOs
	 * 
	 * @param dto
	 * @return
	 */
	public RoleResponseDTO copyToEntity(RoleDTO dto) {
		RoleResponseDTO response = new RoleResponseDTO();
		response.setRoleId(String.valueOf(dto.getRoleId()));
		response.setActive(dto.getActive());
		response.setCreatedBy(dto.getCreatedBy());
		response.setCreatedDate(dto.getCreatedDate());
		response.setDeductorPan(dto.getDeductorPan());
		response.setModifiedBy(dto.getModifiedBy());
		response.setModifiedDate(dto.getModifiedDate());
		response.setPermissionNames(dto.getPermissionNames());
		response.setRoleName(dto.getRoleName());
		response.setModuleType(dto.getModuleType());
		return response;
	}

	public List<RoleResponseDTO> copyToListEntity(List<RoleDTO> listdto) {
		List<RoleResponseDTO> listResponse = new ArrayList<RoleResponseDTO>();
		for (RoleDTO dto : listdto) {
			RoleResponseDTO response = new RoleResponseDTO();
			response.setRoleId(String.valueOf(dto.getRoleId()));
			response.setActive(dto.getActive());
			response.setCreatedBy(dto.getCreatedBy());
			response.setCreatedDate(dto.getCreatedDate());
			response.setDeductorPan(dto.getDeductorPan());
			response.setModifiedBy(dto.getModifiedBy());
			response.setModifiedDate(dto.getModifiedDate());
			response.setPermissionNames(dto.getPermissionNames());
			response.setRoleName(dto.getRoleName());
			response.setModuleType(dto.getModuleType());
			listResponse.add(response);
		}
		return listResponse;

	}
	
	public Boolean updatePermissions(String tenantId, PermissionsDTO rolePermissionDTO) {
		logger.info("Updating user role : {}", rolePermissionDTO.getRoleName());
		repository.updatePermissions(tenantId, rolePermissionDTO);
		return true;
	}
}
