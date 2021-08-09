package com.ey.in.tds.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.SubNaturePaymentMaster;

/**
 * Spring Data repository for the SubNaturePaymentMaster entity.
 */
@Repository
public interface SubNaturePaymentMasterRepository extends JpaRepository<SubNaturePaymentMaster, Long> {

	// @formatter:off
	@Query(value = "select * from tds.sub_nature_payment_master " 
			+ "where active=1 and nature = ? " 
			+ "order by id DESC", nativeQuery = true)
	// @formatter:on
	public Optional<SubNaturePaymentMaster> findByNature(String nature);

	@Override
	// @formatter:off
	@Query(value = "select * from tds.sub_nature_payment_master " 
			+ "where active=1 " 
			+ "order by id DESC", nativeQuery = true)
	// @formatter:on
	public List<SubNaturePaymentMaster> findAll();

	@Override
	// @formatter:off
	@Query(value = "select * from tds.sub_nature_payment_master " 
			+ "where active = 1 and id= ?", nativeQuery = true)
	// @formatter:on
	public Optional<SubNaturePaymentMaster> findById(Long id);

}