package com.ey.in.tds.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ey.in.tds.common.domain.FilingMinorHeadCode;
import com.ey.in.tds.repository.FilingMinorHeadCodeRepository;

@Service
public class FilingMinorHeadCodeService {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private FilingMinorHeadCodeRepository filingMinorHeadCodeRepository;

	/**
	 * This method for create Minor Head Code
	 * 
	 * @param minorHeadCode
	 * @param userName
	 * @return
	 */
	public FilingMinorHeadCode saveMinorHeadCode(@Valid FilingMinorHeadCode minorHeadCode, String userName) {
		minorHeadCode.setActive(true);
		minorHeadCode.setCreatedBy(userName);
		minorHeadCode.setCreatedDate(Instant.now());
		return filingMinorHeadCodeRepository.save(minorHeadCode);
	}

	/**
	 * This method for get all Minor Head Code.
	 * 
	 * @return
	 */
	public List<FilingMinorHeadCode> getAllMinorCodeCode() {
		List<FilingMinorHeadCode> listFilingMinorHeadCode=null;
		 listFilingMinorHeadCode= filingMinorHeadCodeRepository.findAll();
		 logger.info("list of Filing Minor Head Code", listFilingMinorHeadCode);
		 return listFilingMinorHeadCode;
	}

	/**
	 * This method for get Minor Head Code Based on Id
	 * 
	 * @param id
	 * @return
	 */
	public FilingMinorHeadCode getByMinistryCodeId(Long id) {
		Optional<FilingMinorHeadCode> filingMinistryCodeObj = filingMinorHeadCodeRepository.findById(id);
		if (filingMinistryCodeObj.isPresent()) {
			return filingMinistryCodeObj.get();
		}
		return new FilingMinorHeadCode();
	}

}
