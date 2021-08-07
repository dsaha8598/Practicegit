package com.ey.in.tds.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.FineRateMaster;
import com.ey.in.tds.common.util.GlobalConstants;

/**
 * Spring Data repository for the FineRateMaster entity.
 */
@Repository
public interface FineRateMasterRepository extends JpaRepository<FineRateMaster, Long> {

	// @formatter:off
	@Query(value = "select * from tds.fine_rate_master "
			+ "where active = 1 and interest_type = ? "
			+ GlobalConstants.IN_EFFECT_QUERY, nativeQuery = true)
	// @formatter:on
	Optional<FineRateMaster> findByInterestType(String filing);

	// @formatter:off
	@Query(value = "select top 1 * from tds.fine_rate_master "
			+ "where active = 1 and interest_type = ? and type_of_intrest_calculation =? "
			+ GlobalConstants.IN_EFFECT_QUERY
			+ "order by applicable_to desc", nativeQuery = true)
	// @formatter:on
	Optional<FineRateMaster> findByInterestTypeAndInterestCalculation(String interestType, String interestCalculation);

	@Override
	// @formatter:off
	@Query(value = "select * from tds.fine_rate_master " 
			+ "where active=1 " 
			+ GlobalConstants.IN_EFFECT_QUERY
			+ "order by id DESC", nativeQuery = true)
	// @formatter:on
	public List<FineRateMaster> findAll();

	@Override
	// @formatter:off
	@Query(value = "select * from tds.fine_rate_master " 
			+ "where active = 1 and id= ?"
			+ GlobalConstants.IN_EFFECT_QUERY, nativeQuery = true)
	// @formatter:on
	public Optional<FineRateMaster> findById(Long id);
}
