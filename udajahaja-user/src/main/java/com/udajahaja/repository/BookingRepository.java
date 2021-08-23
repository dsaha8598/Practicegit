package com.udajahaja.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.udajahaja.entity.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer>{

	@Query(value = "select * from booking where  email=?1 and active=1 ", nativeQuery = true)
	// @formatter:on
	List<Booking> getBookedTicketsByEmail(String email);
	
	@Query(value = "select * from booking where  airline_id=?1 and active=1 ", nativeQuery = true)
	// @formatter:on
	List<Booking> deactivateRecordsByAirlineId(Integer id);
	
}
