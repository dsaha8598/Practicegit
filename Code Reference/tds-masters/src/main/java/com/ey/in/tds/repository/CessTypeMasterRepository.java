package com.ey.in.tds.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.CessTypeMaster;
import com.ey.in.tds.common.util.GlobalConstants;

/**
 * Spring Data repository for the CessTypeMaster entity.
 */
@Repository
public interface CessTypeMasterRepository extends JpaRepository<CessTypeMaster, Long> {

	public Optional<CessTypeMaster> findByCessType(String type);

	@Override
	// @formatter:off
	@Query(value = "select * from tds.cess_type_master " 
			+ "where active=1 " 
			+ GlobalConstants.IN_EFFECT_QUERY
			+ "order by id DESC", nativeQuery = true)
	// @formatter:on
	public List<CessTypeMaster> findAll();

	@Override
	// @formatter:off
	@Query(value = "select * from tds.cess_type_master " 
			+ "where active = 1 and id= ? "
			+ GlobalConstants.IN_EFFECT_QUERY, nativeQuery = true)
	// @formatter:on
	public Optional<CessTypeMaster> findById(Long id);

}
