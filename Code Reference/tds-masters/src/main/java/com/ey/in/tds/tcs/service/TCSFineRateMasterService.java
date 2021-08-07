package com.ey.in.tds.tcs.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ey.in.tds.common.domain.tcs.TCSFineRateMaster;
import com.ey.in.tds.core.exceptions.BadRequestAlertException;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.tcs.repository.TCSFineRateMasterRepository;
import com.ey.in.tds.web.rest.util.CommonValidations;

/**
 * 
 * @author scriptbees.
 *
 */
@Service
@Transactional
public class TCSFineRateMasterService {

	private final Logger logger = LoggerFactory.getLogger(TCSFineRateMasterService.class);

	@Autowired
	private TCSFineRateMasterRepository tcsFineRateMasterRepository;

	/**
	 * This method is used to create new Fine Rate Master record
	 * 
	 * @param userName
	 * 
	 * @param fineRateMasterfromui
	 * @return
	 * @throws BadRequestAlertException
	 */
	public TCSFineRateMaster save(TCSFineRateMaster fineRateMaster, String userName) {
		logger.info("REST request to save a FineRateMaster : {} ", fineRateMaster);
		Optional<TCSFineRateMaster> fineRateMasterDb = tcsFineRateMasterRepository
				.findByInterestTypeAndInterestCalculation(fineRateMaster.getInterestType(),
						fineRateMaster.getTypeOfIntrestCalculation());
		if (fineRateMasterDb.isPresent()) {
			CommonValidations.validateApplicableFields(fineRateMasterDb.get().getApplicableTo(),
					fineRateMaster.getApplicableFrom());
		}
		TCSFineRateMaster fineRateMasters = new TCSFineRateMaster();
		fineRateMasters.setApplicableFrom(fineRateMaster.getApplicableFrom());
		fineRateMasters.setApplicableTo(fineRateMaster.getApplicableTo());
		fineRateMasters.setTypeOfIntrestCalculation(fineRateMaster.getTypeOfIntrestCalculation());

		if (("Late Filing").equalsIgnoreCase(fineRateMaster.getInterestType())
				&& (fineRateMaster.getFinePerDay() == null || fineRateMaster.getFinePerDay() == 0)) {
			throw new CustomException("If Late Filing Selected, Fine Per Day should not be empty",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		if (!("Late Filing").equalsIgnoreCase(fineRateMaster.getInterestType())
				&& (fineRateMaster.getRate() == null || fineRateMaster.getRate().compareTo(BigDecimal.ZERO) == 0)) {
			throw new CustomException("If Late Filing Selected, Rate should not be empty",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		fineRateMasters.setRate(fineRateMaster.getRate());
		fineRateMasters.setFinePerDay(fineRateMaster.getFinePerDay());
		fineRateMasters.setInterestType(fineRateMaster.getInterestType());
		fineRateMasters.setActive(true);
		fineRateMasters.setCreatedBy(userName);
		fineRateMasters.setCreatedDate(Instant.now());
		fineRateMasters.setModifiedBy(userName);
		fineRateMasters.setModifiedDate(Instant.now());
		fineRateMasters = tcsFineRateMasterRepository.save(fineRateMasters);
		return fineRateMasters;
	}

	/**
	 * This method is used to get all Fine Rate Master record
	 * 
	 * @param pageable
	 * @return
	 * @throws RecordNotFoundException
	 */
	@Transactional(readOnly = true)
	public List<TCSFineRateMaster> findAll() {
		List<TCSFineRateMaster> fineRateList = tcsFineRateMasterRepository.findAll();
		return fineRateList;
	}

	/**
	 * This method is used to get Fine Rate Master record based on id
	 * 
	 * @param id
	 * @return
	 * @throws RecordNotFoundException
	 */
	@Transactional(readOnly = true)
	public TCSFineRateMaster findOne(Long id) {
		logger.info("REST request id to get a record of FineRateMaster : {}", id);
		TCSFineRateMaster fineRateMasters = null;
		Optional<TCSFineRateMaster> fineRateMaster = tcsFineRateMasterRepository.findById(id);
		if (fineRateMaster.isPresent()) {
			fineRateMasters = fineRateMaster.get();
		}
		return fineRateMasters;
	}

	/**
	 * This method is used to get Fine Rate Master record based on Late Filing
	 * 
	 * @param id
	 * @return
	 * @throws RecordNotFoundException
	 */
	@Transactional(readOnly = true)
	public TCSFineRateMaster findRecordBasedonLateFiling(String filing) {
		logger.info("REST request id to get a record of FineRateMaster : {}", filing);
		TCSFineRateMaster fineRateMasters = null;
		Optional<TCSFineRateMaster> fineRateMaster = tcsFineRateMasterRepository.findByInterestType(filing);
		if (fineRateMaster.isPresent()) {
			fineRateMasters = fineRateMaster.get();
		}
		return fineRateMasters;
	}

	/**
	 * This method is used to delete Fine Rate Master record based on id
	 * 
	 * @param id
	 * @return
	 * @throws RecordNotFoundException
	 */
	public String delete(Long id) {
		logger.info("REST request id to delete a FineRateMaster record : {}", id);
		Optional<TCSFineRateMaster> fineRateMasters = tcsFineRateMasterRepository.findById(id);
		if (fineRateMasters.isPresent()) {
			tcsFineRateMasterRepository.deleteById(id);
		} else {
			throw new CustomException("No record found  with id " + id, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return "The deletion request has been succeded";
	}

	/**
	 * This method is used to update Fine Rate Master record
	 * 
	 * @param userName
	 * 
	 * @param fineRateMasterfromui
	 * @return
	 * @throws RecordNotFoundException
	 */
	public TCSFineRateMaster updateFineRateMaster(@Valid TCSFineRateMaster fineRateMaster, String userName)
			throws RecordNotFoundException {
		logger.info("REST request to update a FineRateMaster : {} ", fineRateMaster);
		TCSFineRateMaster response = null;
		Optional<TCSFineRateMaster> fineRateMasters = tcsFineRateMasterRepository.findById(fineRateMaster.getId());
		if (fineRateMasters.isPresent()) {
			TCSFineRateMaster fineRateMaste = fineRateMasters.get();
			fineRateMaste.setApplicableFrom(fineRateMaster.getApplicableFrom());
			fineRateMaste.setApplicableTo(fineRateMaster.getApplicableTo());
			fineRateMaste.setId(fineRateMaster.getId());
			fineRateMaste.setTypeOfIntrestCalculation(fineRateMaster.getTypeOfIntrestCalculation());
			fineRateMaste.setModifiedBy(userName);
			fineRateMaste.setModifiedDate(Instant.now());
			if (fineRateMaste.getInterestType().equalsIgnoreCase("Late filing")) {
				fineRateMaste.setRate(fineRateMaster.getRate());
			} else {
				fineRateMaste.setFinePerDay(fineRateMaster.getFinePerDay());
			}
			fineRateMaste.setInterestType(fineRateMaster.getInterestType());
			response = tcsFineRateMasterRepository.save(fineRateMaste);
		} else {
			throw new CustomException("No record found  with id " + fineRateMaster.getId(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

}
