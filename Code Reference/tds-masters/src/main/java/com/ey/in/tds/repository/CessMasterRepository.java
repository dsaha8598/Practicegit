package com.ey.in.tds.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.CessMaster;
import com.ey.in.tds.common.util.GlobalConstants;
import com.ey.in.tds.dto.SurchargeAndCessRateDTO;

/**
 * Spring Data repository for the CessMaster entity.
 */
@Repository
public interface CessMasterRepository extends JpaRepository<CessMaster, Long> {

	@Override
	// @formatter:off
	@Query(value = "select * from tds.cess_master " 
			+ "where active=1 " 
			+ GlobalConstants.IN_EFFECT_QUERY
			+ "order by id DESC", nativeQuery = true)
	// @formatter:on
	public List<CessMaster> findAll();

	@Override
	// @formatter:off
	@Query(value = "select * from tds.cess_master " 
			+ "where active = 1 and id= ?"
			+ GlobalConstants.IN_EFFECT_QUERY, nativeQuery = true)
	// @formatter:on
	public Optional<CessMaster> findById(Long id);
	
	
	@Query(value = "select * from tds.cess_master where active = 1 and cess_type_master_id= ? "
			+ GlobalConstants.IN_EFFECT_QUERY + "order by id DESC ", nativeQuery = true)
	public CessMaster getCessMasterByCessTypeID(Long id);
	
	@Query(value = "select * from tds.cess_master "
			+ "where active = 1 and boc_nature_of_payment= ? and boc_invoice_slab=? and boc_deductee_status= ? and boc_residential_status=? "
			+ GlobalConstants.IN_EFFECT_QUERY + "order by id DESC ", nativeQuery = true)
	public List<CessMaster> findCessMasterByBasedOnFields(Boolean bosNatureOfPayment, Boolean bosInvoiceSlab,
			Boolean bosDeducteeStatus, Boolean bosResidentialStatus);
	
	@Query(value = "select distinct cm.rate as rate, cm.applicable_from as applicableFrom,cm.applicable_to as applicableTo,"
			+ " bc.invoice_slab_from as invoiceSlabFrom, bc.invoice_slab_to as invoiceSlabTo from tds.cess_master cm"
			+ " inner join tds.basis_of_cess_details bc on cm.id = bc.cess_master_id and cm.active = 1 and cm.active = 1 "
			+ " inner join tds.nature_of_payment_master nop on nop.id = bc.nature_of_payment_master_id and nop.section = ? and nop.active = 1"
			+ " inner join tds.residential_status rs on rs.id= bc.deductee_residential_status_id and rs.active = 1 and rs.status = ?"
			+ " inner join tds.status s on bc.deductee_status_id = s.id and s.status = ?"
			+ " where cm.applicable_from is not null and CAST(cm.applicable_from AS DATE) <= CONVERT(DATE, GETDATE())"
			+ " and (cm.applicable_to is null or CAST(cm.applicable_to AS DATE) >= CONVERT(DATE, GETDATE()))", nativeQuery = true)
	public List<SurchargeAndCessRateDTO> findBySectionResidentialStatusDeducteeStatus(String section,
			String residentialStatus, String deducteeStatus);
	
	@Query(value = "select distinct cm.rate as rate, cm.applicable_from as applicableFrom,cm.applicable_to as applicableTo,"
			+ "bc.invoice_slab_from as invoiceSlabFrom, bc.invoice_slab_to as invoiceSlabTo from tds.cess_master cm"
			+ " left join tds.basis_of_cess_details bc on cm.id = bc.cess_master_id and cm.active = 1"
			+ " inner join tds.cess_type_master ctm on ctm.id = cm.cess_type_master_id and ctm.active = 1 and ctm.type like %?%"
			+ " where cm.applicable_from is not null and CAST(cm.applicable_from AS DATE) <= CONVERT(DATE, GETDATE())"
			+ " and (cm.applicable_to is null or CAST(cm.applicable_to AS DATE) >= CONVERT(DATE, GETDATE()))"
			+ " and (cm.applicable_to is null or CAST(cm.applicable_to AS DATE) >= CONVERT(DATE, GETDATE()))"
			+ " and (ctm.applicable_to is null or CAST(ctm.applicable_to AS DATE) >= CONVERT(DATE, GETDATE()))"
			+ " and (ctm.applicable_to is null or CAST(ctm.applicable_to AS DATE) >= CONVERT(DATE, GETDATE()));", nativeQuery = true)
	public List<SurchargeAndCessRateDTO> findByCessType(String cessType);
	
	@Query(value="select * from tds.cess_master " 
			+ "where active = 1 and is_cess_applicable_at_flat_rate= ?"
			+ GlobalConstants.IN_EFFECT_QUERY + "order by id DESC ", nativeQuery = true)
	public Optional<CessMaster> findByIsCessApplicable(Boolean isSurchargeApplicable);

}
