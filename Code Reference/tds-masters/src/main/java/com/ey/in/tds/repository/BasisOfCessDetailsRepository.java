package com.ey.in.tds.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.BasisOfCessDetails;

/**
 * Spring Data repository for the BasisOfCessDetails entity.
 */
@Repository
public interface BasisOfCessDetailsRepository extends JpaRepository<BasisOfCessDetails, Long> {

	List<BasisOfCessDetails> findByCessMasterId(Long id);
	
	// @formatter:off
	@Query(value ="select * from tds.basis_of_cess_details "
			+ "where active = 1 and nature_of_payment_master_id = ?",nativeQuery = true)
	// @formatter:on
	List<BasisOfCessDetails> findByNatureOfPaymentId(Long natureOfPaymentId);

	@Override
	// @formatter:off
	@Query(value = "select * from tds.basis_of_cess_details " 
			+ "where active=1 " 
			+ "order by id DESC", nativeQuery = true)
	// @formatter:on
	public List<BasisOfCessDetails> findAll();

	@Override
	// @formatter:off
	@Query(value = "select * from tds.basis_of_cess_details " 
			+ "where active = 1 and id= ?", nativeQuery = true)
	// @formatter:on
	public Optional<BasisOfCessDetails> findById(Long id);
	
	@Query(value = "select * from tds.basis_of_cess_details where isnull(nature_of_payment_master_id,0) = ?1" + 
			"and isnull(deductee_status_id,0) = ?2" + 
			"and isnull(deductee_residential_status_id,0) = ?3" + 
			"and isnull(invoice_slab_from,0) = ?4" + 
			"and isnull(invoice_slab_to,0) = ?5" + 
			"and isnull(rate,0) = ?6 and active = 0", nativeQuery = true)
	public Optional<BasisOfCessDetails> findUniqueRecord(long natureId, long deducteeStatusId,
			long deducteeResidentialStatusId, long invoiceSlabFrom, long invoiceSlabTo, long rate);
}
