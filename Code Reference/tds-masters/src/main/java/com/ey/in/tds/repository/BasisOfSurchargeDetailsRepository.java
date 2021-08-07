package com.ey.in.tds.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.BasisOfSurchargeDetails;

/**
 * Spring Data repository for the BasisOfSurchargeDetails entity.
 */
@Repository
public interface BasisOfSurchargeDetailsRepository extends JpaRepository<BasisOfSurchargeDetails, Long> {

	List<BasisOfSurchargeDetails> findBySurchargeMasterId(Long id);

	// @formatter:off
	@Query(value ="select * from tds.basis_of_surcharge_details "
			+ "where active = 1 and nature_of_payment_master_id = ?",nativeQuery = true)
	// @formatter:on
	List<BasisOfSurchargeDetails> findListOfSurchargeRateByNatureOfPaymentId(Long id);

	@Override
	// @formatter:off
	@Query(value = "select * from tds.basis_of_surcharge_details " 
			+ "where active=1 " 
			+ "order by id DESC", nativeQuery = true)
	// @formatter:on
	public List<BasisOfSurchargeDetails> findAll();

	@Override
	// @formatter:off
	@Query(value = "select * from tds.basis_of_surcharge_details " 
			+ "where active = 1 and id= ?", nativeQuery = true)
	// @formatter:on
	public Optional<BasisOfSurchargeDetails> findById(Long id);
	
	@Query(value = "select * from tds.basis_of_surcharge_details where isnull(nature_of_payment_master_id,0) = ?" + 
			"and isnull(deductee_status_id,0) = ?" + 
			"and isnull(deductee_residential_status_id,0) = ?" + 
			"and isnull(invoice_slab_from,0) = ?" + 
			"and isnull(invoice_slab_to,0) = ?" + 
			"and isnull(rate,0) = ? and active = 1", nativeQuery = true)
	public Optional<BasisOfSurchargeDetails> findUniqueRecord(long natureId, long deducteeStatusId,
			long deducteeResidentialStatusId, long invoiceSlabFrom, long invoiceSlabTo, long rate);
	
}
