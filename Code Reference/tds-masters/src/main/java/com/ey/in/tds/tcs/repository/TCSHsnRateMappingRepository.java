package com.ey.in.tds.tcs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.tcs.TCSHsnRateMapping;
/**
 * 
 * @author scriptbees.
 *
 */
@Repository
public interface TCSHsnRateMappingRepository extends JpaRepository<TCSHsnRateMapping, Long> {
	
	

}
