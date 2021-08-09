package com.ey.in.tds.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.Pan;

/**
 * Spring Data repository for the Pan entity.
 */
@Repository
public interface PanRepository extends JpaRepository<Pan, Long> {

	// @formatter:off
	@Query(value = "select * from tds.pan " 
			+ "where active=1 and pan = ?", nativeQuery = true)
	// @formatter:on
	public Optional<Pan> findByPan(String pan);

	@Override
	// @formatter:off
	@Query(value = "select * from tds.pan " 
			+ "where active=1 " 
			+ "order by id DESC", nativeQuery = true)
	// @formatter:on
	public List<Pan> findAll();

	@Override
	// @formatter:off
	@Query(value = "select * from tds.pan " 
			+ "where active = 1 and id= ?", nativeQuery = true)
	// @formatter:on
	public Optional<Pan> findById(Long id);
}
