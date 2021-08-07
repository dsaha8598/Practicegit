package com.ey.in.tds.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.TdsMaster;
import com.ey.in.tds.common.util.GlobalConstants;

/**
 * Spring Data repository for the TdsMaster entity.
 */
@Repository
public interface TdsMasterRepository extends JpaRepository<TdsMaster, Long> {
	
	// @formatter:off
	@Query(value = "select top 1 * from tds.tds_master "
			+ "where active=1 and nature_of_payment_id = ? and deductee_status_id= ? "
			+ GlobalConstants.IN_EFFECT_QUERY
			+ "order by applicable_to desc", nativeQuery = true)
	// @formatter:on
	Optional<TdsMaster> findByNOPAndStatus(Long natureOfPaymentId, Long deducteeStatusId);
	
	// @formatter:off
	@Query(value = "select top 1 * from tds.tds_master "
			+ "where active = 1 and sub_nature_of_payment_id = ? and deductee_status_id= ? "
			+ GlobalConstants.IN_EFFECT_QUERY
			+ "order by applicable_to desc", nativeQuery = true)
	// @formatter:on
	Optional<TdsMaster> findBySOPAndDedcuteeStatusId(Long subNatureOfPaymentId, Long deducteeStatusId);

	@Override
	// @formatter:off
	@Query(value = "select * from tds.tds_master " 
			+ "where active=1 order by id DESC", nativeQuery = true)
	// @formatter:on
	public List<TdsMaster> findAll();

	@Override
	// @formatter:off
	@Query(value = "select * from tds.tds_master " 
			+ "where active = 1 and id= ?", nativeQuery = true)
	// @formatter:on
	public Optional<TdsMaster> findById(Long id);
	
	@Query(value = "select * from tds.tds_master " 
			+ "where active = 1 and id= ?", nativeQuery = true)
	// @formatter:on
	public Optional<TdsMaster> findByOnlyId(Long id);
	
	@Query(value = "select distinct tm.* from tds.nature_of_payment_master nopm "
			+ "inner join tds.tds_master tm on tm.nature_of_payment_id = nopm.id and nopm.active=1 and tm.active=1 and nopm.section= ? and nopm.id =?"
			+ " inner join tds.residential_status rs on rs.id = tm.deductee_resident_status_id and rs.active=1 and rs.status= ? "
			+ "inner join tds.status ds on tm.deductee_status_id = ds.id and ds.status = ? ", nativeQuery = true)
	public List<TdsMaster> getTdsMasterBySection(String section, int nop, String residentialStatus, String status);
	
	// @formatter:off
		@Query(value = "select top 1 * from tds.tds_master where active = 1 and nature_of_payment_id = ?"
				+ GlobalConstants.IN_EFFECT_QUERY, nativeQuery = true)
		// @formatter:on
	Optional<TdsMaster> getTdsRateMasterRate(Long natureId);

}
