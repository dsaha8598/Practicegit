package com.ey.in.tds.tcs.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.tcs.TCSRateMaster;
import com.ey.in.tds.common.util.GlobalConstants;

/**
 * Spring Data repository for the TCSMaster entity.
 */
@Repository
public interface TCSMasterRepository extends JpaRepository<TCSRateMaster, Long> {

	// @formatter:off
	@Query(value = "select top 1 * from tcs.tcs_rate_master " + "where active=1 and nature_of_income_id = ? "
			+ GlobalConstants.IN_EFFECT_QUERY + "order by applicable_to desc", nativeQuery = true)
	// @formatter:on
	Optional<TCSRateMaster> findByNOPAndStatus(Long natureOfPaymentId, Long deducteeStatusId);

	// @formatter:off
	@Query(value = "select top 1 * from tcs.tcs_rate_master where active = 1 " + GlobalConstants.IN_EFFECT_QUERY
			+ "order by applicable_to desc", nativeQuery = true)
	// @formatter:on
	Optional<TCSRateMaster> findBySOPAndDedcuteeStatusId(Long subNatureOfPaymentId, Long deducteeStatusId);

	@Override
	// @formatter:off
	@Query(value = "select * from tcs.tcs_rate_master where active=1 order by id DESC", nativeQuery = true)
	// @formatter:on
	public List<TCSRateMaster> findAll();

	@Override
	// @formatter:off
	@Query(value = "select * from tcs.tcs_rate_master where active = 1 and id= ?", nativeQuery = true)
	// @formatter:on
	public Optional<TCSRateMaster> findById(Long id);

	@Query(value = "select distinct tm.* from tcs.nature_of_income noi "
			+ " join tcs.tcs_rate_master tm on tm.nature_of_income_id = noi.id and noi.active=1 and tm.active=1 and noi.section= ?1"
			+ " where noi.applicable_from is not null and "
			+ " CAST(noi.applicable_from AS DATE) <= CONVERT(DATE, GETDATE()) and (noi.applicable_to is null or "
			+ " CAST(noi.applicable_to AS DATE) >= CONVERT(DATE, GETDATE())) and tm.applicable_from is not null and "
			+ " CAST(tm.applicable_from AS DATE) <= CONVERT(DATE, GETDATE()) and (tm.applicable_to is null or "
			+ " CAST(tm.applicable_to AS DATE) >= CONVERT(DATE, GETDATE()))", nativeQuery = true)
	public List<TCSRateMaster> getTCSMasterBySection(String section);

	// @formatter:off
	@Query(value = "select top 1 * from tcs.tcs_rate_master where active = 1 and nature_of_income_id = ?"
			+ GlobalConstants.IN_EFFECT_QUERY, nativeQuery = true)
	// @formatter:on
	TCSRateMaster getTcsRateMasterRate(Long natureOfId);

}
