package com.ey.in.tds.tcs.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ey.in.tcs.common.TCSMasterDTO;
import com.ey.in.tds.common.config.RedisUtil;
import com.ey.in.tds.common.domain.ResidentialStatus;
import com.ey.in.tds.common.domain.Status;
import com.ey.in.tds.common.domain.tcs.TCSNatureOfIncome;
import com.ey.in.tds.common.domain.tcs.TCSRateMaster;
import com.ey.in.tds.common.tcs.repository.TCSNatureOfIncomeRepository;
import com.ey.in.tds.core.errors.FieldValidator;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.repository.ResidentialStatusRepository;
import com.ey.in.tds.repository.StatusRepository;
import com.ey.in.tds.tcs.repository.TCSMasterRepository;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Service Implementation for managing TCSMaster.
 */
@Service
@Transactional
public class TCSMasterService {

	@Autowired
	private ResidentialStatusRepository residentialStatusRepository;

	@Autowired
	private StatusRepository statusRepository;

	@Autowired
	private TCSNatureOfIncomeRepository tcsNatureOfIncomeRepository;

	@Autowired
	private RedisUtil<Map<String, Object>> redisUtilUserTenantInfo;

	@Autowired
	private TCSMasterRepository tcsMasterRepository;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * This method is used to create new TDS Rate Master record
	 * 
	 * @param tcsMasterDto
	 * @param userName
	 * @return
	 * @throws RecordNotFoundException
	 * @throws JsonProcessingException
	 */
	public Long createTCSMaster(TCSMasterDTO tcsMasterDto, String userName) throws JsonProcessingException {

		Long noOfRecordCreated=0L;
		TCSRateMaster tcsMasters = new TCSRateMaster();
		logger.info("REST request to save TCSMaster : {}", tcsMasterDto);

		tcsMasters.setRate(tcsMasterDto.getRate());
		tcsMasters.setActive(true);
		tcsMasters.setCreatedBy(userName);
		tcsMasters.setIsPerTransactionLimit(tcsMasterDto.getIsPerTransactionLimitApplicable());
		tcsMasters.setPerTransactionLimit(tcsMasterDto.getPerTransactionLimit());
		tcsMasters.setApplicableFrom(tcsMasterDto.getApplicableFrom());
		tcsMasters.setApplicableTo(tcsMasterDto.getApplicableTo());
		tcsMasters.setIsAnnualTransactionLimitApplicable(tcsMasterDto.getIsAnnualTransactionLimitApplicable());
		tcsMasters.setAnnualTransactionLimit(tcsMasterDto.getAnnualTransactionLimit());
		tcsMasters.setRateForNoPan(tcsMasterDto.getRateForNoPan());
		tcsMasters.setNoItrRate(tcsMasterDto.getNoItrRate());
		tcsMasters.setNoPanRateAndNoItrRate(tcsMasterDto.getNoPanRateAndNoItrRate());
		if (tcsMasterDto.getNatureOfIncomeId() != null) {
			Optional<TCSNatureOfIncome> nature = tcsNatureOfIncomeRepository
					.findById(tcsMasterDto.getNatureOfIncomeId());
			if (nature.isPresent()) {
				tcsMasters.setNatureOfIncomeId(tcsMasterDto.getNatureOfIncomeId());
			} else {
				throw new CustomException("No record to save Nature of Income Object with Nature of Income ID"
						+ tcsMasterDto.getNatureOfIncomeId(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		

		if (tcsMasterDto.getIsPerTransactionLimitApplicable()!=null && tcsMasterDto.getIsPerTransactionLimitApplicable()==true
				&& (tcsMasterDto.getPerTransactionLimit() == null || tcsMasterDto.getPerTransactionLimit().intValue() == 0)) {
			throw new FieldValidator("If Transaction Limit Applicable is true Transaction Limit should not be null");
		}

		if (tcsMasterDto.getCollecteeResidentialStatusId() != null) {
			Optional<ResidentialStatus> residentialStatusData = residentialStatusRepository
					.findById(tcsMasterDto.getCollecteeResidentialStatusId());

			if (residentialStatusData.isPresent()) {
				tcsMasters.setCollecteeResidentStatusId(tcsMasterDto.getCollecteeResidentialStatusId());
			} else {
				throw new RecordNotFoundException(
						"No record to save Deductee Resiential Status Object with Deductee Residential Status ID"
								+ tcsMasterDto.getCollecteeResidentialStatusId());
			}
		}
		
		/*
		 * if (status.getId() != null) { Optional<Status> statusData =
		 * statusRepository.findById(status.getId()); if (statusData.isPresent()) {
		 * tcsMasters.setCollecteeStatusId(status.getId()); } else { throw new
		 * CustomException("No record to save Deductee Status Object with Deductee Status ID"
		 * + tcsMasterDto.getCollecteeStatusId(), HttpStatus.INTERNAL_SERVER_ERROR); } }
		 */
			//setting id as null so that new record will be created for more than iteration
			//instead of updating the record
			tcsMasters.setId(null);
			tcsMasters = tcsMasterRepository.save(tcsMasters);
			logger.info("SAVED Record : {}", tcsMasters);
			noOfRecordCreated++;
		
	
		// TODO: need to change the code
//		if (tcsMasters.getId() != null) {
//			ObjectMapper mapper = new ObjectMapper();
//			List<NatureOfPaymentRate> natureOfPaymentRates = tcsNatureOfIncomeRepository.findNatureOfPaymentRates();
//			for (NatureOfPaymentRate natureOfPaymentRate : natureOfPaymentRates) {
//				String jsonString = mapper.writeValueAsString(natureOfPaymentRate);
//				redisUtilUserTenantInfo.putMap(RedisKeys.NATUREOFPAYMENT.name(),
//						natureOfPaymentRate.getNatureOfPaymentId().toString() + "_"
//								+ natureOfPaymentRate.getDeducteeStatus(),
//						jsonString);
//			}
//		}
		return noOfRecordCreated;
	}

	/**
	 * This method is used to get all TDS Rate Master record
	 * 
	 * @param pageable
	 * @return
	 * @throws RecordNotFoundException
	 */
	@Transactional(readOnly = true)
	public List<TCSMasterDTO> findAll() {
		List<TCSMasterDTO> tcsMasterList = new ArrayList<>();
		List<TCSRateMaster> listTds = tcsMasterRepository.findAll();
		for (TCSRateMaster tcsMaster : listTds) {
			TCSMasterDTO tcsMasterDTO = getTCSMastersDTOFromDomainObject(tcsMaster);
			tcsMasterList.add(tcsMasterDTO);
		}
		return tcsMasterList;
	}

	/**
	 * 
	 * @param tcsMaster
	 * @return
	 */
	private TCSMasterDTO getTCSMastersDTOFromDomainObject(TCSRateMaster tcsMaster) {
		TCSMasterDTO tcsMasterDTO = new TCSMasterDTO();
		tcsMasterDTO.setId(tcsMaster.getId());
		tcsMasterDTO.setApplicableFrom(tcsMaster.getApplicableFrom());
		tcsMasterDTO.setApplicableTo(tcsMaster.getApplicableTo());
		tcsMasterDTO.setIsPerTransactionLimitApplicable(tcsMaster.getIsPerTransactionLimit());
		tcsMasterDTO.setPerTransactionLimit(tcsMaster.getPerTransactionLimit()==null ? BigDecimal.ZERO :tcsMaster.getPerTransactionLimit());
		tcsMasterDTO.setRate(tcsMaster.getRate());
		tcsMasterDTO.setCollecteeResidentialStatusId(tcsMaster.getCollecteeResidentStatusId());
		tcsMasterDTO.setCollecteeStatusId(tcsMaster.getCollecteeStatusId());
		tcsMasterDTO.setNatureOfIncomeId(tcsMaster.getNatureOfIncomeId());
		tcsMasterDTO.setResidentialStatusName("NR");
		tcsMasterDTO.setIsAnnualTransactionLimitApplicable(tcsMaster.getIsAnnualTransactionLimitApplicable());
		tcsMasterDTO.setAnnualTransactionLimit(tcsMaster.getAnnualTransactionLimit());
		tcsMasterDTO.setRateForNoPan(tcsMaster.getRateForNoPan());
		tcsMasterDTO.setNoItrRate(tcsMaster.getNoItrRate());
		tcsMasterDTO.setNoPanRateAndNoItrRate(tcsMaster.getNoPanRateAndNoItrRate());
		
		Optional<ResidentialStatus> residentialStatusData = residentialStatusRepository
				.findById(tcsMaster.getCollecteeResidentStatusId());

		if (residentialStatusData.isPresent()) {
			tcsMasterDTO.setResidentialStatusName(residentialStatusData.get().getStatus());
		}
		
		Optional<TCSNatureOfIncome> nature = tcsNatureOfIncomeRepository
				.findById(tcsMaster.getNatureOfIncomeId());
		if (nature.isPresent()) {
			tcsMasterDTO.setNatureOfIncomeMaster(nature.get().getNature());
		}  
		Optional<Status> statusData = statusRepository.findById(tcsMaster.getCollecteeStatusId());
		if (statusData.isPresent()) {
			tcsMasterDTO.setStatusName(statusData.get().getStatus());
		}
		return tcsMasterDTO;
	}

	/**
	 * This method is used to get TDS Rate Master record based on id
	 * 
	 * @param id
	 * @return
	 * @throws RecordNotFoundException
	 */
	@Transactional(readOnly = true)
	public TCSMasterDTO findOne(Long id) {
		logger.info("REST request id to get TCSMaster : {}", id);
		Optional<TCSRateMaster> tcsmaster = tcsMasterRepository.findById(id);
		if (tcsmaster.isPresent()) {
			return getTCSMastersDTOFromDomainObject(tcsmaster.get());
		}
		throw new RecordNotFoundException("No record found  with id " + id);
	}
	
	
	@Transactional(readOnly = true)
	public TCSMasterDTO finByNoiId(Long id) {
		logger.info("REST request id to get TCSMaster : {}", id);
		TCSRateMaster tcsmaster = tcsMasterRepository.getTcsRateMasterRate(id);
		if (tcsmaster != null) {
			return getTCSMastersDTOFromDomainObject(tcsmaster);
		}
		throw new RecordNotFoundException("No record found  with id " + id);
	}

	/**
	 * This method is used to delete TDS Rate Master record based on id
	 * 
	 * @param id
	 * @throws RecordNotFoundException
	 */
	public void delete(Long id) throws RecordNotFoundException {
		logger.info("REST request id to delete TCSMaster : {}", id);
		Optional<TCSRateMaster> tcsMaster = tcsMasterRepository.findById(id);
		if (tcsMaster.isPresent()) {
			tcsMasterRepository.deleteById(id);
		} else {
			throw new RecordNotFoundException("No record found  with id " + id);
		}
	}

	/**
	 * This method is used to update TDS Rate Master record
	 * 
	 * @param tcsMasterDto
	 * @param userName
	 * @return
	 * @throws RecordNotFoundException
	 */
	public TCSRateMaster updateTCSMaster(@Valid TCSMasterDTO tcsMasterDto, String userName)
			throws RecordNotFoundException {
		logger.info("REST request to update TCSMaster : {}", tcsMasterDto);
		Optional<TCSRateMaster> tcsMastersOpt = tcsMasterRepository.findById(tcsMasterDto.getId());
		TCSRateMaster tcsMasters = null;
		if (tcsMastersOpt.isPresent()) {
			tcsMasters = tcsMastersOpt.get();
			tcsMasters.setRate(tcsMasterDto.getRate());
			tcsMasters.setActive(true);
			tcsMasters.setCreatedBy(userName);
			tcsMasters.setIsPerTransactionLimit(tcsMasterDto.getIsPerTransactionLimitApplicable());
			tcsMasters.setPerTransactionLimit(tcsMasterDto.getPerTransactionLimit());
			tcsMasters.setApplicableFrom(tcsMasterDto.getApplicableFrom());
			tcsMasters.setApplicableTo(tcsMasterDto.getApplicableTo());
			tcsMasters.setRateForNoPan(tcsMasterDto.getRateForNoPan());
			tcsMasters.setNoItrRate(tcsMasterDto.getNoItrRate());
			tcsMasters.setNoPanRateAndNoItrRate(tcsMasterDto.getNoPanRateAndNoItrRate());
			/*if (tcsMasterDto.getNatureOfIncomeId() != null) {
				Optional<TCSNatureOfIncome> nature = tcsNatureOfIncomeRepository
						.findById(tcsMasterDto.getNatureOfIncomeId());
				if (nature.isPresent()) {
					tcsMasters.setNatureOfIncome(nature.get());
				} else {
					throw new CustomException("No record to save Nature of Income Object with Nature of Income ID"
							+ tcsMasterDto.getNatureOfIncomeId(), HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}  */
		/*	if (tcsMasterDto.getCollecteeStatusId() != null) {
				Optional<Status> statusData = statusRepository.findById(tcsMasterDto.getCollecteeStatusId());
				Status status = null;
				if (statusData.isPresent()) {
					status = statusData.get();
					tcsMasters.setDeducteeStatus(status);
				} else {
					throw new CustomException("No record to save Deductee Status Object with Deductee Status ID"
							+ tcsMasterDto.getCollecteeStatusId(), HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} */

			if (tcsMasterDto.getIsPerTransactionLimitApplicable()
					&& (tcsMasterDto.getPerTransactionLimit() == null || tcsMasterDto.getPerTransactionLimit().intValue() == 0)) {
				throw new FieldValidator(
						"If Transaction Limit Applicable is true Transaction Limit should not be null");
			}

		/*	if (tcsMasterDto.getCollecteeResidentStatusId() != null) {
				Optional<ResidentialStatus> residentialStatusData = residentialStatusRepository
						.findById(tcsMasterDto.getCollecteeResidentStatusId());

				ResidentialStatus residentialStatus = null;
				if (residentialStatusData.isPresent()) {
					residentialStatus = residentialStatusData.get();
					tcsMasters.setDeducteeResidentStatus(residentialStatus);
				} else {
					throw new RecordNotFoundException(
							"No record to save Deductee Resiential Status Object with Deductee Residential Status ID"
									+ tcsMasterDto.getCollecteeResidentStatusId());
				}
			}  */

			logger.info("ID for Deuductee: {}", tcsMasterDto.getCollecteeStatusId());
			return tcsMasterRepository.save(tcsMasters);
		} else {
			throw new RecordNotFoundException("No record found  for TDS RATE Master with id " + tcsMasterDto.getId());
		}
		
	}

	public List<TCSRateMaster> getTCSMasterBySection(String section, String residentialStatus, String status) {
		return tcsMasterRepository.getTCSMasterBySection(section);
	}
}
