package com.udajahaja.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.udajahaja.entity.AeroplaneEntity;
import com.udajahaja.entity.AirlineEntity;

@Repository
public interface AeroplaneRepository extends JpaRepository<AeroplaneEntity, Long>{

}
