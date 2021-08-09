package com.ey.in.tds.tcs.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ey.in.tds.common.domain.tcs.TCSCessTypeMaster;
import com.ey.in.tds.core.errors.FieldValidator;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.dto.CessTypeMasterDTO;
import com.ey.in.tds.tcs.repository.TCSCessTypeMasterRepository;
import com.ey.in.tds.web.rest.util.CommonValidations;

/**
 * Service Implementation for managing CessTypeMaster.
 */
/**
 * @author Admin
 *
 */
@Service
@Transactional
public class TCSCessTypeMasterService {

	private final Logger logger = LoggerFactory.getLogger(TCSCessTypeMasterService.class);

	private final TCSCessTypeMasterRepository cessTypeMasterRepository;

	public TCSCessTypeMasterService(TCSCessTypeMasterRepository cessTypeMasterRepository) {
		this.cessTypeMasterRepository = cessTypeMasterRepository;
	}

	/**
	 * This will create CESS Master Record.
	 * 
	 * @param cessTypeMasterDTO
	 * @param userName 
	 * @return
	 * @throws FieldValidator
	 */
	public TCSCessTypeMaster save(CessTypeMasterDTO cessTypeMasterDTO, String userName) {
		logger.info("REST request to save CessTypeMaster : {}", cessTypeMasterDTO);
		Optional<TCSCessTypeMaster> cessTypeMasterDb = cessTypeMasterRepository.findByCessType(cessTypeMasterDTO.getCessType());
		if(cessTypeMasterDb.isPresent()) {
			CommonValidations.validateApplicableFields(cessTypeMasterDb.get().getApplicableTo(), cessTypeMasterDTO.getApplicableFrom());
		}
		TCSCessTypeMaster cessTypeMaster = new TCSCessTypeMaster();
		cessTypeMaster.setCessType(cessTypeMasterDTO.getCessType());
		cessTypeMaster.setApplicableFrom(cessTypeMasterDTO.getApplicableFrom());
		cessTypeMaster.setApplicableTo(cessTypeMasterDTO.getApplicableTo());
		cessTypeMaster.setActive(true);
		cessTypeMaster.setCreatedBy(userName);
		cessTypeMaster.setCreatedDate(Instant.now());
		cessTypeMaster.setModifiedBy(userName);
		cessTypeMaster.setModifiedDate(Instant.now());
		cessTypeMaster = cessTypeMasterRepository.save(cessTypeMaster);
		return cessTypeMaster;
	}

	/**
	 * This will update CESS Type Master.
	 * 
	 * @param cessTypeMasterDTO
	 * @param userName 
	 * @return
	 * @throws RecordNotFoundException
	 */
	public TCSCessTypeMaster update(CessTypeMasterDTO cessTypeMasterDTO, String userName) {
		logger.info("REST request to update CessTypeMaster : {}", cessTypeMasterDTO);
		Optional<TCSCessTypeMaster> optionalCessTypeMaster = cessTypeMasterRepository.findById(cessTypeMasterDTO.getId());
		TCSCessTypeMaster cessTypeMaster = new TCSCessTypeMaster();

		if (optionalCessTypeMaster.isPresent()) {
			cessTypeMaster.setId(cessTypeMasterDTO.getId());
			cessTypeMaster.setApplicableFrom(cessTypeMasterDTO.getApplicableFrom());
			cessTypeMaster.setApplicableTo(cessTypeMasterDTO.getApplicableTo());
			cessTypeMaster.setCessType(cessTypeMasterDTO.getCessType());
			cessTypeMaster.setActive(true);
			cessTypeMaster.setModifiedBy(userName);
			cessTypeMaster.setModifiedDate(Instant.now());
			cessTypeMaster = cessTypeMasterRepository.save(cessTypeMaster);
		} else {
			throw new CustomException("No record with this id " + cessTypeMasterDTO.getId() + " to update", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return cessTypeMaster;
	}

	/**
	 * Get all the cessTypeMasters.
	 *
	 * @param pageable the pagination information
	 * @return the list of entities
	 */
	@Transactional(readOnly = true)
	public List<TCSCessTypeMaster> findAll() {
		List<TCSCessTypeMaster> listCessType = cessTypeMasterRepository.findAll();
		return listCessType;
	}

	/**
	 * Get one cessTypeMaster by id.
	 *
	 * @param id the id of the entity
	 * @return the entity
	 * @throws RecordNotFoundException
	 */
	@Transactional(readOnly = true)
	public Optional<TCSCessTypeMaster> findOne(Long id) throws RecordNotFoundException {

		Optional<TCSCessTypeMaster> cessTypeMaster = null;
		logger.info("REST request id to get CessTypeMaster : {}", id);
		cessTypeMaster = cessTypeMasterRepository.findById(id);
		if (cessTypeMaster.isPresent()) {
			return cessTypeMaster;
		}
		return cessTypeMaster; 
	}

	/**
	 * Delete the cessTypeMaster by id.
	 *
	 * @param id the id of the entity
	 */
	public void delete(Long id) {
		logger.info("REST request id to delete CessTypeMaster : {}", id);
		cessTypeMasterRepository.deleteById(id);
	}
}
