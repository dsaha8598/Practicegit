package com.ey.in.tds.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.ResidentialStatus;

/**
 * Spring Data repository for the ResidentialStatus entity.
 */
@Repository
public interface ResidentialStatusRepository extends JpaRepository<ResidentialStatus, Long> {

	@Override
	// @formatter:off
	@Query(value = "select * from tds.residential_status " + "where active=1 " + "order by id DESC", nativeQuery = true)
	// @formatter:on
	public List<ResidentialStatus> findAll();

	@Override
	// @formatter:off
	@Query(value = "select * from tds.residential_status " + "where active = 1 and id= ?", nativeQuery = true)
	// @formatter:on
	public Optional<ResidentialStatus> findById(Long id);

	// @formatter:off
	@Query(value = "select * from tds.residential_status where status = ? and active = 1", nativeQuery = true)
	// @formatter:on
	public Optional<ResidentialStatus> findByStatus(String status);

}
