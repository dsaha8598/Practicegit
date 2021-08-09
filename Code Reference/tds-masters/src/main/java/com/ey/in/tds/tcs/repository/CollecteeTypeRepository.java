package com.ey.in.tds.tcs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.tcs.CollecteeType;
/**
 * 
 * @author scriptbees.
 *
 */
@Repository
public interface CollecteeTypeRepository extends JpaRepository<CollecteeType, Long> {

	// @formatter:off
		@Query(value = "select status from tcs.collectee_type where status = ? and active = 1 ;", nativeQuery = true)
	// @formatter:on
	String getCollecteeType(String collecteeStatus);
		
	// @formatter:off
	@Query(value = "SELECT * FROM tcs.collectee_type where active = 1;",nativeQuery = true)
	// @formatter:on
	List<CollecteeType> getCollecteeTypeStatus();

}
