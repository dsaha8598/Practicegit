package com.udajahaja.repository;



import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.udajahaja.entity.AeroplaneEntity;
import com.udajahaja.entity.AirlineEntity;

@Repository
public interface AeroplaneRepository extends JpaRepository<AeroplaneEntity, Integer>{

	
	@Query(value = "select * from aeroplane where  AIRLINE_ID=?1 and active=1 ", nativeQuery = true)
	// @formatter:on
	List<AeroplaneEntity> getAeroplaneByAirlineId(Long id);
}
