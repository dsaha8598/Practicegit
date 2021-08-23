package com.udajahaja.repository.test;

import static org.junit.Assert.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.udajahaja.entity.AirlineEntity;
import com.udajahaja.repository.AirlineRepository;

//@RunWith(SpringRunner.class)
@SpringBootTest
public class AirlineRepositoryTest {
	
	@Autowired
	private AirlineRepository airlineRepository;

	 @Test
	  public void saveAirline() {
	   AirlineEntity airline=new AirlineEntity();
	   airline.setActive(true);
	   airline.setAirlineName("Indigo");
	   airline.setContactAddress("Odsiha");
	   airline.setContactNumber(8763765485l);
	   airline=airlineRepository.save(airline);
	   assertNotNull(airline.getId());
	  }
}
