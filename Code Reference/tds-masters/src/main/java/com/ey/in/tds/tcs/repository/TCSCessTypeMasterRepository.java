package com.ey.in.tds.tcs.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.tcs.TCSCessTypeMaster;
import com.ey.in.tds.common.util.GlobalConstants;

/**
 * Spring Data repository for the CessTypeMaster entity.
 */
@Repository
public interface TCSCessTypeMasterRepository extends JpaRepository<TCSCessTypeMaster, Long> {

	public Optional<TCSCessTypeMaster> findByCessType(String type);

	@Override
	// @formatter:off	
	@Query(value = "select * from tcs.tcs_cess_type_master " 
			+ "where active=1 " 
			+ GlobalConstants.IN_EFFECT_QUERY
			+ "order by id DESC", nativeQuery = true)
	// @formatter:on
	public List<TCSCessTypeMaster> findAll();

	@Override
	// @formatter:off
	@Query(value = "select * from tcs.tcs_cess_type_master " 
			+ "where active = 1 and id= ? "
			+ GlobalConstants.IN_EFFECT_QUERY, nativeQuery = true)
	// @formatter:on
	public Optional<TCSCessTypeMaster> findById(Long id);

}
