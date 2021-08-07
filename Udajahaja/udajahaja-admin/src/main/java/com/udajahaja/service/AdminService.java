package com.udajahaja.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.udajahaja.dto.AeroplaneSaveDTO;
import com.udajahaja.dto.AirlineSaveDTO;
import com.udajahaja.entity.AeroplaneEntity;
import com.udajahaja.entity.AirlineEntity;
import com.udajahaja.repository.AeroplaneRepository;
import com.udajahaja.repository.AirlineRepository;

@Service
public class AdminService {

	@Autowired
	private AirlineRepository repo;
	@Autowired
	private AeroplaneRepository aeroplaneRepository;
	public void save(AirlineSaveDTO dto) {
		AirlineEntity entity=new AirlineEntity();
		entity.setActive(true);
		entity.setAirlineName(dto.getAirlineName());
		entity.setContactAddress(dto.getContactAddress());
		entity.setContactNumber(dto.getContactNumber());
		entity=repo.save(entity);
		for(AeroplaneSaveDTO aeroplaneSaveDTO:dto.getAeroplanes()) {
			AeroplaneEntity aeroplaneEntity=new AeroplaneEntity();
			aeroplaneEntity.setActive(1);
			aeroplaneEntity.setAeroplaneNumber(aeroplaneSaveDTO.getAeroplaneNumber());
			aeroplaneEntity.setAirlineId((long)entity.getId());
			aeroplaneEntity.setBusinessClassCount(aeroplaneSaveDTO.getBusinessClassCount());
			aeroplaneEntity.setEconomyClassCount(aeroplaneSaveDTO.getEconomyClassCount());
			aeroplaneEntity.setStartDate(aeroplaneSaveDTO.getStartDate());
			aeroplaneEntity.setEndDate(aeroplaneSaveDTO.getEndDate());
			aeroplaneRepository.save(aeroplaneEntity);
		}
		System.out.println("Saved id--->"+entity.getId());
	}
	public List<AirlineEntity> findAllAirlines() {
		return repo.findAll();
	}
}
