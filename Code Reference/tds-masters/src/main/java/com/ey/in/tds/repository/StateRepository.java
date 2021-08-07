package com.ey.in.tds.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.State;

/**
 * Spring Data repository for the State entity.
 */
@Repository
public interface StateRepository extends JpaRepository<State, Long> {

	@Override
	// @formatter:off
	@Query(value = "select * from tds.state " 
			+ "where active=1 " 
			+ "order by name ASC", nativeQuery = true)
	// @formatter:on
	public List<State> findAll();

	@Override
	// @formatter:off
	@Query(value = "select * from tds.state " 
			+ "where active = 1 and id= ?", nativeQuery = true)
	// @formatter:on
	public Optional<State> findById(Long id);

}
