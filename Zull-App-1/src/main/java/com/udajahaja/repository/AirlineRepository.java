package com.udajahaja.repository;



import java.util.List;

import javax.persistence.Id;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.udajahaja.entity.AeroplaneEntity;
import com.udajahaja.entity.AirlineEntity;

@Repository
public interface AirlineRepository extends JpaRepository<AirlineEntity, Integer>{

	@Query(value = "select * from airline where  ID=?1 and active=1" , nativeQuery = true)
	// @formatter:on
	AirlineEntity findByAirlineId(Long id);
	
	@Query(value = "select airline_name from airline where  active=1" , nativeQuery = true)
	// @formatter:on
	List<String> findAllAirlineName();
}
