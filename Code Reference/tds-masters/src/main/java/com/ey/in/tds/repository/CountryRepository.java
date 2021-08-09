package com.ey.in.tds.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.Country;

/**
 * Spring Data repository for the Country entity.
 */
@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {

	@Override
	// @formatter:off
	@Query(value = "select id, UPPER(name) as name, currency, symbol from tds.country " 
			+ "where active=1 " 
			+ "order by name ASC", nativeQuery = true)
	// @formatter:on
	public List<Country> findAll();

	@Override
	// @formatter:off
	@Query(value = "select * from tds.country " 
			+ "where active = 1 and id= ?", nativeQuery = true)
	// @formatter:on
	public Optional<Country> findById(Long id);

	@Query(value = "select DISTINCT symbol from tds.country where active=1 and symbol is not null", nativeQuery = true)
	public List<String> getCountryCodes();
}
