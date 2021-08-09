package com.ey.in.tds.tcs.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ey.in.tds.common.domain.TcsLookUp;
import com.ey.in.tds.repository.TcsLookUpRepository;

@Service
public class TcsLookUpService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	//@Autowired
	//private TcsLookUpDAO tcsLookUpRepository;
	
	@Autowired
	private TcsLookUpRepository tcsLookUpRepository;

	public List<TcsLookUp> getAllLookUpValue() {

		logger.info("Request to fetch all module lookup values");
		//List<TcsLookUpDTO> lookUpDTO = tcsLookUpRepository.fetchModuleLookUp();
		List<TcsLookUp> tcsLookUp=tcsLookUpRepository.findAllActive();
		return tcsLookUp;
	}
}
