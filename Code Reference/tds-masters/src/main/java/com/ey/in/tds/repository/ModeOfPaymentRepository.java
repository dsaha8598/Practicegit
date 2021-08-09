package com.ey.in.tds.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.ModeOfPayment;

/**
 * Spring Data repository for the ModeOfPayment entity.
 */
@Repository
public interface ModeOfPaymentRepository extends JpaRepository<ModeOfPayment, Long> {
	
	@Override
	// @formatter:off
	@Query(value = "select * from tds.mode_of_payment " 
			+ "where active=1 " 
			+ "order by id DESC", nativeQuery = true)
	// @formatter:on
	public List<ModeOfPayment> findAll();

	@Override
	// @formatter:off
	@Query(value = "select * from tds.mode_of_payment " 
			+ "where active = 1 and id= ?", nativeQuery = true)
	// @formatter:on
	public Optional<ModeOfPayment> findById(Long id);
}
