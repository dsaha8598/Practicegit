package com.ey.in.tds.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.SurchargeMaster;
import com.ey.in.tds.common.util.GlobalConstants;
import com.ey.in.tds.dto.SurchargeAndCessRateDTO;

/**
 * Spring Data repository for the SurchargeMaster entity.
 */
@Repository
public interface SurchargeMasterRepository extends JpaRepository<SurchargeMaster, Long> {

	@Override
	// @formatter:off
	@Query(value = "select * from tds.surcharge_master where active=1 " + GlobalConstants.IN_EFFECT_QUERY
			+ "order by id DESC", nativeQuery = true)
	// @formatter:on
	public List<SurchargeMaster> findAll();

	@Override
	// @formatter:off
	@Query(value = "select * from tds.surcharge_master where active = 1 and id= ? "
			+ GlobalConstants.IN_EFFECT_QUERY, nativeQuery = true)
	// @formatter:on
	public Optional<SurchargeMaster> findById(Long id);

	// @formatter:off
	@Query(value="select * from tds.surcharge_master " 
			+ "where active = 1 and is_surcharge_applicable= ?"
			+ GlobalConstants.IN_EFFECT_QUERY + "order by id DESC ", nativeQuery = true)
	// @formatter:on
	public Optional<SurchargeMaster> findByIsSurchargeApplicable(Boolean isSurchargeApplicable);

	// @formatter:off
	@Query(value = "select * from tds.surcharge_master "
			+ "where active = 1 and bos_nature_of_payment= ? and bos_invoice_slab=? and bos_deductee_status= ? and bos_residential_status=? "
			+ GlobalConstants.IN_EFFECT_QUERY + "order by id DESC ", nativeQuery = true)
	// @formatter:on
	public List<SurchargeMaster> findByBasedOnFields(Boolean bosNatureOfPayment, Boolean bosInvoiceSlab,
			Boolean bosDeducteeStatus, Boolean bosResidentialStatus);
	
	// @formatter:off
	@Query(value = "select distinct sm.surcharge_rate as rate, sm.applicable_from as applicableFrom, sm.applicable_to as applicableTo,"
			+ " bs.invoice_slab_from as invoiceSlabFrom, bs.invoice_slab_to as invoiceSlabTo from tds.surcharge_master sm"
			+ " inner join tds.basis_of_surcharge_details bs on sm.id=bs.surcharge_master_id and sm.active=1 and bs.active=1"
			+ " inner join tds.nature_of_payment_master nop on nop.id = bs.nature_of_payment_master_id and nop.section= ? and nop.active=1"
			+ " inner join tds.residential_status rs on rs.id= bs.deductee_residential_status_id and rs.active = 1 and rs.status= ?"
			+ " inner join tds.status s on bs.deductee_status_id = s.id and s.status = ?"
			+ " where sm.applicable_from is not null and CAST(sm.applicable_from AS DATE) <= CONVERT(DATE, GETDATE())"
			+ " and (sm.applicable_to is null or CAST(sm.applicable_to AS DATE) >= CONVERT(DATE, GETDATE()))", nativeQuery = true)
	// @formatter:on
	public List<SurchargeAndCessRateDTO> findBySectionResidentialStatusDeducteeStatus(String section,
			String residentialStatus, String deducteeStatus);
	
	@Query(value = "select distinct bs.rate as rate, sm.applicable_from as applicableFrom, sm.applicable_to as applicableTo,"
			+ " bs.invoice_slab_from as invoiceSlabFrom, bs.invoice_slab_to as invoiceSlabTo from tds.surcharge_master sm"
			+ " inner join tds.basis_of_surcharge_details bs on sm.id=bs.surcharge_master_id and sm.active=1 and bs.active=1"
			+ " inner join tds.residential_status rs on rs.id= bs.deductee_residential_status_id and rs.active = 1 and rs.status= ?"
			+ " inner join tds.status s on bs.deductee_status_id = s.id and s.status = ?"
			+ " where sm.applicable_from is not null and CAST(sm.applicable_from AS DATE) <= CONVERT(DATE, GETDATE())"
			+ " and (sm.applicable_to is null or CAST(sm.applicable_to AS DATE) >= CONVERT(DATE, GETDATE()))", nativeQuery = true)
	public List<SurchargeAndCessRateDTO> findByResidentialStatusDeducteeStatus(String residentialStatus,
			String deducteeStatus);
	
	@Query(value = "select distinct nop.section,s.[status],bs.rate as rate, sm.applicable_from as applicableFrom, sm.applicable_to as applicableTo,"
			+ " bs.invoice_slab_from as invoiceSlabFrom, bs.invoice_slab_to as invoiceSlabTo from tds.surcharge_master sm"
			+ " inner join tds.basis_of_surcharge_details bs on sm.id=bs.surcharge_master_id and sm.active=1 and bs.active=1"
			+ " inner join tds.nature_of_payment_master nop on nop.id = bs.nature_of_payment_master_id and nop.active=1"
			+ " inner join tds.residential_status rs on rs.id= bs.deductee_residential_status_id and rs.active = 1 and rs.status= ?"
			+ " inner join tds.status s on bs.deductee_status_id = s.id and s.active = 1"
			+ " where sm.applicable_from is not null and CAST(sm.applicable_from AS DATE) <= CONVERT(DATE, GETDATE())"
			+ " and (sm.applicable_to is null or CAST(sm.applicable_to AS DATE) >= CONVERT(DATE, GETDATE()))", nativeQuery = true)
	public List<Map<String, Object>> getAllSurchargeDetails(String residentialStatus);
	
}
