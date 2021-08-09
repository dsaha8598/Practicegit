package com.ey.in.tds.tcs.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.tcs.TCSCessMaster;
import com.ey.in.tds.common.util.GlobalConstants;

/**
 * Spring Data repository for the TCSCessMaster entity.
 */
@Repository
public interface TCSCessMasterRepository extends JpaRepository<TCSCessMaster, Long> {

	@Override
	// @formatter:off
	@Query(value = "select * from tcs.tcs_cess_master " 
			+ "where active=1 " 
			+ GlobalConstants.IN_EFFECT_QUERY
			+ "order by id DESC", nativeQuery = true)
	// @formatter:on
	public List<TCSCessMaster> findAll();

	@Override
	// @formatter:off
	@Query(value = "select * from tcs.tcs_cess_master " 
			+ "where active = 1 and id= ?"
			+ GlobalConstants.IN_EFFECT_QUERY, nativeQuery = true)
	// @formatter:on
	public Optional<TCSCessMaster> findById(Long id);
	
	
	@Query(value="select TOP 1 * from tcs.tcs_cess_master " 
			+ "where active = 1 and tcs_cess_type_master_id= ? "
			+ "order by id DESC ", nativeQuery = true)
	public TCSCessMaster getTcsCessMasterByCessTypeID(Long id);
	
//	@Query(value="select TOP 1 * from tcs.cess_master " 
//			+ "where active = 1 and boc_nature_of_payment= ? and boc_invoice_slab=? and boc_deductee_status= ? and boc_residential_status=? "
//			+ GlobalConstants.IN_EFFECT_QUERY
//			+ "order by id DESC ", nativeQuery = true)
//    public TCSCessMaster findCessMasterByBasedOnFields(Boolean bosNatureOfPayment,Boolean bosInvoiceSlab,Boolean bosDeducteeStatus,Boolean bosResidentialStatus);

}
