package com.ey.in.tds.tcs.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.tcs.TCSHsnCode;

/**
 * 
 * @author scriptbees.
 *
 */
@Repository
public interface TCSHsnCodeRepository extends JpaRepository<TCSHsnCode, Long> {

	@Override
	// @formatter:off
	@Query(value = "select * from tcs.tcs_hsn_code where active = 1 order by id DESC", nativeQuery = true)
	// @formatter:on
	List<TCSHsnCode> findAll();

	// @formatter:on
	Page<TCSHsnCode> findAll(Pageable pageable);

}
