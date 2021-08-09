package com.ey.in.tds.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ey.in.tds.common.domain.FilingSectionCode;
import com.ey.in.tds.repository.FilingSectionCodeRepository;

@Service
public class FilingSectionCodeService {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private FilingSectionCodeRepository filingSectionCodeRepository;

	/**
	 * This method for create Section code
	 * 
	 * @param sectionCode
	 * @param userName
	 * @return
	 */
	public FilingSectionCode saveSectionCode(@Valid FilingSectionCode sectionCode, String userName) {
		sectionCode.setActive(true);
		sectionCode.setCreatedBy(userName);
		sectionCode.setCreatedDate(Instant.now());
		return filingSectionCodeRepository.save(sectionCode);
	}

	/**
	 * This method for get all Section codes.
	 * 
	 * @return
	 */
	public List<FilingSectionCode> getAllSectionCode() {
		List<FilingSectionCode> listFilingSectionCode=null;
		listFilingSectionCode= filingSectionCodeRepository.findAll();
		logger.info("list of FilingSectionCode",listFilingSectionCode);
		return listFilingSectionCode;
	}

	/**
	 * This method for get Section code Based on Id
	 * 
	 * @param id
	 * @return
	 */
	public FilingSectionCode getBySectionCodeId(Long id) {
		Optional<FilingSectionCode> filingSectionCodeObj = filingSectionCodeRepository.findById(id);
		if (filingSectionCodeObj.isPresent()) {
			return filingSectionCodeObj.get();
		}
		return new FilingSectionCode();
	}

}
