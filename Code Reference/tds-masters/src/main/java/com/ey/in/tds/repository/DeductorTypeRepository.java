package com.ey.in.tds.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.DeductorType;

/**
 * Spring Data repository for the DeductorType entity.
 */
@Repository
public interface DeductorTypeRepository extends JpaRepository<DeductorType, Long> {

	@Override
	// @formatter:off
	@Query(value = "select * from tds.deductor_type " 
			+ "where active=1 " 
			+ "order by id DESC", nativeQuery = true)
	// @formatter:on
	public List<DeductorType> findAll();

	@Override
	// @formatter:off
	@Query(value = "select * from tds.deductor_type " 
			+ "where active = 1 and id= ?", nativeQuery = true)
	// @formatter:on
	public Optional<DeductorType> findById(Long id);
}
