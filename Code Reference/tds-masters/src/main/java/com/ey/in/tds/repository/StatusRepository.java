package com.ey.in.tds.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.Status;

/**
 * Spring Data repository for the Status entity.
 */
@Repository
public interface StatusRepository extends JpaRepository<Status, Long> {

	// @formatter:off
	@Query(value = "select * from tds.status " 
			+ "where active=1 and status = ? " 
			+ "order by id DESC", nativeQuery = true)
	// @formatter:on
	Optional<Status> findByStatus(String status);

	// @formatter:off
	@Query(value = "select * from tds.status " 
			+ "where active=1 and pan_code = ? " 
			+ "order by id DESC", nativeQuery = true)
	// @formatter:on
	Optional<Status> findByPanCode(String panCode);

	@Override
	// @formatter:off
	@Query(value = "select * from tds.status " 
			+ "where active=1 " 
			+ "order by id DESC", nativeQuery = true)
	// @formatter:on
	public List<Status> findAll();

	@Override
	// @formatter:off
	@Query(value = "select * from tds.status " 
			+ "where active = 1 and id= ?", nativeQuery = true)
	// @formatter:on
	public Optional<Status> findById(Long id);

	// @formatter:off
		@Query(value = "SELECT DISTINCT pan_code from tds.status where active=1 ", nativeQuery = true)
	// @formatter:on
	List<String> getAllDeducteeStatus();

}
