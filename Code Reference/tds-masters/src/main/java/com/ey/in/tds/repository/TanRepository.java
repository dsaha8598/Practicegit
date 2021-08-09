package com.ey.in.tds.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.Tan;

/**
 * Spring Data repository for the Tan entity.
 */
@Repository
public interface TanRepository extends JpaRepository<Tan, Long> {

	// @formatter:off
	@Query(value = "Select top 1 * from tds.tan "
			+ "where active = 1 and tan = ?", nativeQuery = true)
	// @formatter:on
	public Optional<Tan> findByTan(String tan);

	// @formatter:off
	@Query(value = "select * from tds.tan "
			+ "where active = 1 and id = ? "
			+ "order by id desc", nativeQuery = true)
	// @formatter:on
	public List<Tan> findTansById(Long deductorId);
	
	@Override
	// @formatter:off
	@Query(value = "select * from tds.tan " 
			+ "where active=1 " 
			+ "order by id DESC", nativeQuery = true)
	// @formatter:on
	public List<Tan> findAll();

	@Override
	// @formatter:off
	@Query(value = "select * from tds.tan " 
			+ "where active = 1 and id= ?", nativeQuery = true)
	// @formatter:on
	public Optional<Tan> findById(Long id);

}
