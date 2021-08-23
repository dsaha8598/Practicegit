package com.udajahaja.repository.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.udajahaja.entity.AeroplaneEntity;
import com.udajahaja.entity.AirlineEntity;
import com.udajahaja.repository.AeroplaneRepository;
import com.udajahaja.repository.AirlineRepository;

@SpringBootTest
public class AeroplaneRepositoryTest {

	@Autowired
	private AirlineRepository airlineRepository;
	
	@Autowired
	private AeroplaneRepository aeroplaneRepository;
	
	 @Test
	  public void retrieveAeroplaneByAirlineId() {
	   AirlineEntity airline=new AirlineEntity();
	   airline.setActive(true);
	   airline.setAirlineName("Indigo");
	   airline.setContactAddress("Odsiha");
	   airline.setContactNumber(8763765485l);
	   airline=airlineRepository.save(airline);
	   //assertNotNull(airline.getId());
	   
	   AeroplaneEntity aeroplane=new AeroplaneEntity();
	   aeroplane.setActive(0);
	   aeroplane.setAeroplaneNumber("BOING-101");
	   aeroplane.setAirlineId((long)airline.getId());
	   aeroplane.setBusinessClassCount(100);
	   aeroplane.setEconomyClassCount(500);
	   aeroplane.setEndDate(null);
	   aeroplane.setStartDate(new Date());
	   
	   aeroplane= aeroplaneRepository.save(aeroplane);
	   
	   assertEquals("BOING-101", aeroplaneRepository.findById(aeroplane.getId()).get().getAeroplaneNumber());
	   
	  }
}
