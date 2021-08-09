package com.ey.in.tds.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.NatureOfPaymentMaster;
import com.ey.in.tds.common.dto.ExemptionDTO;

/**
 * reporsitory for exemption
 * @author dipak
 *
 */
@Repository
public interface ExemptionRepository extends JpaRepository<NatureOfPaymentMaster, Long>{
	
	@Query(value="select sec.dividend_deductor_type_id AS deductorTypeId,sec.shareholder_category_id AS shareHolderCatagoryId, dim.residential_status AS residentialStatus,\n"
			+ "dim.[section] AS [section],sec.exempted AS isExempted,sc.name AS shareHolderCatagory,ddt.name AS deductorType\n"
			+ "from tds.shareholder_exempted_category sec inner join tds.dividend_instruments_mappings dim on sec.dividend_deductor_type_id =dim.dividend_deductor_type_id\n"
			+ "and sec.shareholder_category_id =dim.shareholder_category_id INNER JOIN tds.shareholder_category sc on sec.shareholder_category_id =sc.id INNER JOIN \n"
			+ "tds.dividend_deductor_type ddt ON sec.dividend_deductor_type_id =ddt.id ", nativeQuery = true)
	 public List<ExemptionDTO> getAllShareholderExemption();

}
