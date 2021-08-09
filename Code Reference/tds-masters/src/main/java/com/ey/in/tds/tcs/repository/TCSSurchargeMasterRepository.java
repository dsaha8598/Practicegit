package com.ey.in.tds.tcs.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.tcs.TCSSurchargeMaster;
import com.ey.in.tds.common.util.GlobalConstants;

/**
 * Spring Data repository for the SurchargeMaster entity.
 */
@Repository
public interface TCSSurchargeMasterRepository extends JpaRepository<TCSSurchargeMaster, Long> {

	@Override
	// @formatter:off
	@Query(value = "select * from tcs.tcs_surcharge_master " 
			+ "where active=1 " 
			+ GlobalConstants.IN_EFFECT_QUERY
			+ "order by id DESC", nativeQuery = true)
	// @formatter:on
	public List<TCSSurchargeMaster> findAll();

	@Override
	// @formatter:off
	@Query(value = "select * from tcs.tcs_surcharge_master " 
			+ "where active = 1 and id= ? "
			+ GlobalConstants.IN_EFFECT_QUERY, nativeQuery = true)
	// @formatter:on
	public Optional<TCSSurchargeMaster> findById(Long id);
	
	
	@Query(value="select TOP 1 * from tcs.tcs_surcharge_master " 
			+ "where active = 1 and is_surcharge_applicable= ?  "
			+ "order by id DESC ", nativeQuery = true)
	public TCSSurchargeMaster findByIsSurchargeApplicable(Boolean isSurchargeApplicable);
	
}
