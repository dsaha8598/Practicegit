package com.udajahaja.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.udajahaja.entity.Scheduler;

public interface SchedulerRepository extends JpaRepository<Scheduler, Integer>{

	@Query(value = "select * from schedule where  airline_id=?1  ", nativeQuery = true)
	// @formatter:on
	public List<Scheduler> getSchedulesByAirlineId(int id);
	
	@Query(value = "select * from schedule where  from_place=?1  and to_place=?2", nativeQuery = true)
	// @formatter:on
	public List<Scheduler> getSchedulesByFromAndToPlace(String from,String to);
}
