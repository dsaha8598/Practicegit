package com.ey.in.tds.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.NatureOfRemittanceTreaty;

@Repository
public interface NatureOfRemittanceTreatyRepository extends JpaRepository<NatureOfRemittanceTreaty, Long> {

	// @formatter:off
	@Query(value = "select * from tds.nature_of_remittance_treaty "
			+ "where active = 1 and nature_of_remittance= ? and treaty_type = ?", nativeQuery = true)
	// @formatter:on
	public Optional<NatureOfRemittanceTreaty> findByNatureOfRemittanceTreatyType(String natureOfRemittance,
			String treatyType);

}
