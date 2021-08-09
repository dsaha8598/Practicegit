package com.ey.in.tds.tcs.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ey.in.tds.common.domain.ResidentialStatus;
import com.ey.in.tds.common.domain.Status;
import com.ey.in.tds.common.domain.tcs.TCSCessMaster;
import com.ey.in.tds.common.domain.tcs.TCSNatureOfIncome;
import com.ey.in.tds.common.tcs.repository.TCSNatureOfIncomeRepository;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.repository.ResidentialStatusRepository;
import com.ey.in.tds.repository.StatusRepository;
import com.ey.in.tds.tcs.dto.TCSBAsisOfCessDetails;
import com.ey.in.tds.tcs.dto.TCSCessMasterDTO;
import com.ey.in.tds.tcs.repository.TCSCessMasterRepository;
import com.ey.in.tds.tcs.repository.TCSCessTypeMasterRepository;

@Service
public class TCSCessMasterService {

	@Autowired
	private TCSCessMasterRepository cessMasterRepository;

	@Autowired
	private TCSCessTypeMasterRepository cessTypeMasterRepository;

	@Autowired
	private TCSNatureOfIncomeRepository tcsNatureOfIncomeRepository;

	@Autowired
	private StatusRepository statusRepository;

	@Autowired
	private ResidentialStatusRepository residentialStatusRepository;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Save a cessMaster.
	 * 
	 * @param userName
	 *
	 * @param cessMaster the entity to save
	 * @return the persisted entity
	 */
	public Long saveTCSCessMaster(TCSCessMasterDTO cessMasterDTO, String userName) { // save
		if (logger.isDebugEnabled()) {
			logger.debug("REST request to save TCSCessMaster : {}", cessMasterDTO);
		}

		TCSCessMaster cessMaster = new TCSCessMaster();

		List<TCSCessMaster> list = saveAndUpdateCommonLogic(cessMaster, cessMasterDTO, userName);
		cessMaster.setActive(true);
		cessMaster.setCreatedBy(userName);
		cessMaster.setCreatedDate(Instant.now());
		cessMaster.setModifiedBy(userName);
		cessMaster.setModifiedDate(Instant.now());
		try {
			for (TCSCessMaster entity : list) {
				cessMaster = cessMasterRepository.save(entity);
			}
		} catch (Exception ex) {
			logger.error("Error occured while saving record", ex);
		}
		return cessMaster.getId();
	}

	/**
	 * common fields in from method
	 * 
	 * @param cessMaster
	 * @param cessMasterDTO
	 */
	public void saveTCSCessMasterCommonFields(TCSCessMaster cessMaster, TCSCessMasterDTO cessMasterDTO,
			String userName) {
		// cessMaster.setIsCessApplicable(cessMasterDTO.getIsCessApplicable());
		cessMaster.setApplicableFrom(cessMasterDTO.getApplicableFrom());
		cessMaster.setApplicableTo(cessMasterDTO.getApplicableTo());
		//cessMaster.setRate(cessMasterDTO.getRate());
		//cessMaster.setAmount(cessMasterDT);
		cessMaster.setActive(true);
		cessMaster.setCreatedBy(userName);
		cessMaster.setCreatedDate(Instant.now());
		cessMaster.setModifiedBy(userName);
		cessMaster.setModifiedDate(Instant.now());

	}

	/**
	 * Get all the cessMasters.
	 *
	 * @param pageable the pagination information
	 * @return the list of entities
	 */
	@Transactional(readOnly = true)
	public List<TCSCessMasterDTO> findAll() {
		List<TCSCessMasterDTO> cessMasterListDTO = null;
		TCSCessMasterDTO cessMasterDTO = null;

		List<TCSCessMaster> cessMasterList = cessMasterRepository.findAll();

		cessMasterListDTO = new ArrayList<>();
		for (TCSCessMaster cessMaster : cessMasterList) {
			cessMasterDTO = getTcsCessMasterDTOFromDomain(cessMaster);
			cessMasterListDTO.add(cessMasterDTO);
		}
		return cessMasterListDTO;
	}

	/***
	 * get TCSCessMasterDTO from cessMaster domain
	 * 
	 * @param cessMaster
	 * @return
	 */

	private TCSCessMasterDTO getTcsCessMasterDTOFromDomain(TCSCessMaster cessMaster) {
		TCSCessMasterDTO cessMasterDTO;
		cessMasterDTO = new TCSCessMasterDTO();
		List<TCSBAsisOfCessDetails> basisOfCessDetails=new ArrayList<>();
		TCSBAsisOfCessDetails basis=new TCSBAsisOfCessDetails();
		cessMasterDTO.setBasisOfCessDetails(basisOfCessDetails);
		cessMasterDTO.setId(cessMaster.getId());
		cessMasterDTO.setApplicableFrom(cessMaster.getApplicableFrom());
		cessMasterDTO.setApplicableTo(cessMaster.getApplicableTo());
		// cessMasterDTO.setIsCessApplicable(cessMaster.getIsCessApplicable());
		cessMasterDTO.setRate(cessMaster.getRate());
		cessMasterDTO
				.setAmount(cessMaster.getInvoiceSlabFrom() != null ? new BigDecimal(cessMaster.getInvoiceSlabFrom())
						: BigDecimal.ZERO);
		cessMasterDTO.setActive(cessMaster.isActive());

		cessMasterDTO.setCessTypeId(cessMaster.getTcsCessTypeMasterId());
		if (cessMaster.getCollecteeStatusId() != null) {
			Optional<Status> status = statusRepository.findById(cessMaster.getCollecteeStatusId());
			if (status.isPresent()) {
				cessMasterDTO.setCollecteeStatus(status.get().getStatus());
			}
		}
		if (cessMaster.getCollecteeResidentialId() != null) {
			Optional<ResidentialStatus> residentialStatus = residentialStatusRepository
					.findById(cessMaster.getCollecteeResidentialId());
			if (residentialStatus.isPresent()) {
				if(residentialStatus.get().getStatus().equals("NR")) {
				cessMasterDTO.setCollecteeResidentialStatus("Non-Resident");
				basis.setCollecteeResidentialStatus("Non-Resident");
				}
			}
		}
		if (cessMaster.getNatureOfIncomeId() != null) {
			Optional<TCSNatureOfIncome> natureOfIncome = tcsNatureOfIncomeRepository
					.findById(cessMaster.getNatureOfIncomeId());
			if (natureOfIncome.isPresent()) {
				cessMasterDTO.setNatureOfIncome(natureOfIncome.get().getNature());
			}
		}
		basis.setRate(cessMaster.getRate());
		basis.setCollecteeStatusId(cessMaster.getCollecteeStatusId());
		basis.setCessTypeId(cessMaster.getTcsCessTypeMasterId());
		basis.setCollecteeResidentialStatusId(cessMaster.getCollecteeResidentialId());
		basis.setNatureOfIncomeMasterId(cessMaster.getNatureOfIncomeId());
		basis.setInvoiceSlabFrom(cessMaster.getInvoiceSlabFrom());
		basis.setInvoiceSlabTo(cessMaster.getInvoiceSlabTo());
		basisOfCessDetails.add(basis);
		// cessMasterDTO.setCessTypeName(cessMaster.getTcsCessTypeM);

		// cessMasterDTO.setCollecteeStatusId(cessMaster.getCollecteeStatusId());
		// cessMasterDTO.setCollecteeResidentialStatusId(cessMaster.getCollecteeResidentialId());
		return cessMasterDTO;
	}

	/**
	 * Get one cessMaster by id.
	 *
	 * @param id the id of the entity
	 * @return the entity
	 * @throws RecordNotFoundException
	 */
	@Transactional(readOnly = true)
	public TCSCessMasterDTO findOne(Long id) {
		if (logger.isDebugEnabled()) {
			logger.debug("REST request Id to get TCSCessMaster : {}", id);
		}
		Optional<TCSCessMaster> cessMasterData = cessMasterRepository.findById(id);
		if (cessMasterData.isPresent()) {
			return getTcsCessMasterDTOFromDomain(cessMasterData.get());

		}
		return null;
	}

	public TCSCessMaster updateTCSCessMaster(@Valid TCSCessMasterDTO cessMasterDTO, String userName) { // ==========================
		logger.info("REST request to update TCSCessMaster : {}", cessMasterDTO);
		TCSCessMaster cessMaster = null;
		Optional<TCSCessMaster> cessMasterData = cessMasterRepository.findById(cessMasterDTO.getId());

		if (cessMasterData.isPresent()) {
			cessMaster = cessMasterData.get();
			//saveAndUpdateCommonLogic(cessMaster, cessMasterDTO, userName);
			cessMaster.setApplicableTo(cessMasterDTO.getApplicableTo());
			cessMaster.setModifiedBy(userName);
			cessMaster.setModifiedDate(Instant.now());
		} else {
			throw new CustomException("Data not found for Cess Master", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		try {
			cessMaster = cessMasterRepository.save(cessMaster);
		} catch (Exception ex) {
			logger.error("Error occured while updating record", ex);
		}
		return cessMaster;
	}

	/**
	 * below method has common logic for save and update
	 * 
	 * @param cessMaster
	 * @param cessMasterDTO
	 */
	public List<TCSCessMaster> saveAndUpdateCommonLogic(TCSCessMaster cessMaster, @Valid TCSCessMasterDTO cessMasterDTO,
			String userName) {
		List<TCSCessMaster> listCess = new ArrayList<>();
		// If cess rate applicable
		// if (cessMasterDTO.getIsCessApplicable()) {

		// Getting data from CessType master table
		/*
		 * Optional<TCSCessTypeMaster> cessTypeMaster =
		 * cessTypeMasterRepository.findById(cessMasterDTO.getCessTypeId()); if
		 * (cessTypeMaster.isPresent()) {
		 * cessMaster.setTcsCessTypeMaster(cessTypeMaster.get()); }
		 */
		/*
		 * cessMaster.setTcsCessTypeMasterId(cessMasterDTO.getCessTypeId());
		 * TCSCessMaster retrievedTCSCessMaster = cessMasterRepository
		 * .getTcsCessMasterByCessTypeID(cessMasterDTO.getCessTypeId());
		 * 
		 * if(retrievedTCSCessMaster != null &&
		 * cessMasterDTO.getApplicableFrom().equals(retrievedTCSCessMaster.
		 * getApplicableFrom())) { throw new
		 * CustomException("A record is already there with same Applicable From Date",
		 * HttpStatus.INTERNAL_SERVER_ERROR); } if (retrievedTCSCessMaster != null &&
		 * retrievedTCSCessMaster.getRate().equals(cessMasterDTO.getRate()) &&
		 * retrievedTCSCessMaster.getApplicableTo() != null &&
		 * !retrievedTCSCessMaster.getApplicableTo().isBefore(cessMasterDTO.
		 * getApplicableFrom())) { throw new
		 * CustomException("Please update previous record Applicable To in order to create new."
		 * , HttpStatus.INTERNAL_SERVER_ERROR); } if (retrievedTCSCessMaster != null &&
		 * retrievedTCSCessMaster.getApplicableTo() == null) { retrievedTCSCessMaster
		 * .setApplicableTo(cessMasterDTO.getApplicableFrom().minusMillis(1000L * 60L *
		 * 60L * 24)); cessMasterRepository.save(retrievedTCSCessMaster); } // common
		 * logic for basic functionality saveTCSCessMasterCommonFields(cessMaster,
		 * cessMasterDTO,userName); listCess.add(cessMaster);
		 */

		// } else { */
		/*
		 * Optional<ResidentialStatus> residentialStatusData =
		 * residentialStatusRepository
		 * .findById(cessMasterDTO.getCollecteeResidentialStatusId()); if
		 * (residentialStatusData.isPresent()) {
		 * cessMaster.setCollecteeResidentialStatus(residentialStatusData.get()); }
		 * 
		 * Optional<Status> statusData =
		 * statusRepository.findById(cessMasterDTO.getCollecteeStatusId()); if
		 * (statusData.isPresent()) { cessMaster.setCollecteeStatus(statusData.get()); }
		 */
		for (TCSBAsisOfCessDetails basis : cessMasterDTO.getBasisOfCessDetails()) {
			cessMaster.setNatureOfIncomeId(basis.getNatureOfIncomeMasterId());
			cessMaster.setCollecteeResidentialId(basis.getCollecteeResidentialStatusId());
			cessMaster.setCollecteeStatusId(basis.getCollecteeStatusId());
			cessMaster.setInvoiceSlabFrom(basis.getInvoiceSlabFrom());
			cessMaster.setInvoiceSlabTo(basis.getInvoiceSlabTo());
			cessMaster.setRate(basis.getRate());
			cessMaster.setTcsCessTypeMasterId(basis.getCessTypeId());
			cessMaster.setAmount(new BigDecimal(basis.getInvoiceSlabFrom()));
			saveTCSCessMasterCommonFields(cessMaster, cessMasterDTO, userName);
			listCess.add(cessMaster);

		}
		// }
		return listCess;
	}

}
