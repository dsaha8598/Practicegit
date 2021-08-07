package com.ey.in.tds.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ey.in.tds.common.domain.FilingStateCode;
import com.ey.in.tds.repository.FilingStateCodeRepository;

@Service
public class FilingStateCodeService {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private FilingStateCodeRepository filingStateCodeRepository;

	/**
	 * This method for create State Code
	 * 
	 * @param stateCode
	 * @param userName
	 * @return
	 */
	public FilingStateCode saveStateCode(@Valid FilingStateCode stateCode, String userName) {
		stateCode.setActive(true);
		stateCode.setCreatedBy(userName);
		stateCode.setCreatedDate(Instant.now());
		return filingStateCodeRepository.save(stateCode);
	}

	/**
	 * This method for get all State codes
	 * 
	 * @return
	 */
	public List<FilingStateCode> getAllStateCode() {

		List<FilingStateCode> listFilingStateCode=null;
		listFilingStateCode= filingStateCodeRepository.findAll();
		logger.info("list of Filing State Code",listFilingStateCode);
		return listFilingStateCode;
	}

	/**
	 * This method for get State code Based on Id
	 * 
	 * @param id
	 * @return
	 */
	public FilingStateCode getByStateCodeId(Long id) {
		Optional<FilingStateCode> fillingStateCodeObj = filingStateCodeRepository.findById(id);
		if (fillingStateCodeObj.isPresent()) {
			return fillingStateCodeObj.get();
		}
		return new FilingStateCode();
	}

}
