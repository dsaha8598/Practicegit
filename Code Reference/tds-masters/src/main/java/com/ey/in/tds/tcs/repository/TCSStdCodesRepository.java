package com.ey.in.tds.tcs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.tcs.TCSStdCodes;
import com.ey.in.tds.common.domain.transactions.jdbc.tcs.dto.TCSStdCodesAndCitys;

/**
 * 
 * @author vamsir
 *
 */
@Repository
public interface TCSStdCodesRepository extends JpaRepository<TCSStdCodes, Long> {
	// @formatter:off
	@Query(value = "SELECT S.name AS state,STD.city AS city ,STD.std_code AS stdCode FROM tds.state S "
			+ "INNER JOIN tcs.std_codes STD ON S.id = STD.state_id WHERE S.name = ? ORDER BY city ", nativeQuery = true)
	// @formatter:on
	List<TCSStdCodesAndCitys> findAllCitysAndCodes(String state);
	
	// @formatter:off
	@Query(value = "SELECT S.name AS state FROM tds.country C INNER JOIN tds.state S ON C.id = S.country_id "
			+ "WHERE C.name = ? ORDER BY state ", nativeQuery = true)
	// @formatter:on
	List<String> findAllStates(String country);
}
