package com.ey.in.tds.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ey.in.tds.common.domain.FilingMinistryCode;
import com.ey.in.tds.repository.FilingMinistryCodeRepository;

@Service
public class FilingMinistryCodeService {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private FilingMinistryCodeRepository  filingMinistryCodeRepository;
	
	/**
	 * This method for create Ministry code
	 * 
	 * @param ministryCode
	 * @param userName
	 * @return
	 */
	public FilingMinistryCode saveMinistryCode(@Valid FilingMinistryCode ministryCode, String userName) {
		ministryCode.setActive(true);
		ministryCode.setCreatedBy(userName);
		ministryCode.setCreatedDate(Instant.now());
		return filingMinistryCodeRepository.save(ministryCode);
	}

	/**
	 * This method for get all Ministry codes.
	 * 
	 * @return
	 */
	public List<FilingMinistryCode> getAllMinistryCode() {
		List<FilingMinistryCode> listFilingMinistryCode=null;
		listFilingMinistryCode= filingMinistryCodeRepository.findAll();
		logger.info("list of Filing Ministry Code",listFilingMinistryCode);
		return listFilingMinistryCode;
	}

	/**
	 * This method for get Ministry code Based on Id
	 * 
	 * @param id
	 * @return
	 */
	public FilingMinistryCode getByMinistryCodeId(Long id) {
		Optional<FilingMinistryCode> filingMinistryCodeObj = filingMinistryCodeRepository.findById(id);
		if (filingMinistryCodeObj.isPresent()) {
			return filingMinistryCodeObj.get();
		}
		return new FilingMinistryCode();
	}	

}
