package com.ey.in.tds.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ey.in.tds.common.config.RedisUtil;
import com.ey.in.tds.common.domain.NatureOfPaymentMaster;
import com.ey.in.tds.common.domain.ResidentialStatus;
import com.ey.in.tds.common.domain.Status;
import com.ey.in.tds.common.domain.SubNaturePaymentMaster;
import com.ey.in.tds.common.domain.TdsMaster;
import com.ey.in.tds.common.dto.DeducteeStatusDTO;
import com.ey.in.tds.common.dto.NatureOfPaymentRate;
import com.ey.in.tds.common.repository.NatureOfPaymentMasterRepository;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.core.util.RedisKeys;
import com.ey.in.tds.dto.TdsMasterDTO;
import com.ey.in.tds.repository.ResidentialStatusRepository;
import com.ey.in.tds.repository.StatusRepository;
import com.ey.in.tds.repository.SubNaturePaymentMasterRepository;
import com.ey.in.tds.repository.TdsMasterRepository;
import com.ey.in.tds.web.rest.util.CommonValidations;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service Implementation for managing TdsMaster.
 */
@Service
@Transactional
public class TdsMasterService {

	@Autowired
	private ResidentialStatusRepository residentialStatusRepository;

	@Autowired
	private StatusRepository statusRepository;

	@Autowired
	private NatureOfPaymentMasterRepository natureOfPaymentMasterRepository;

	@Autowired
	private RedisUtil<Map<String, Object>> redisUtilUserTenantInfo;

	@Autowired
	private SubNaturePaymentMasterRepository subNaturePaymentMasterRepository;

	private final Logger logger = LoggerFactory.getLogger(TdsMasterService.class);
	
	private final TdsMasterRepository tdsMasterRepository;

	public TdsMasterService(TdsMasterRepository tdsMasterRepository) {
		this.tdsMasterRepository = tdsMasterRepository;
	}

	/**
	 * This method is used to create new TDS Rate Master record
	 * 
	 * @param tdsMasterDto
	 * @param userName
	 * @return
	 * @throws RecordNotFoundException
	 * @throws JsonProcessingException
	 */
	public Long createTdsMaster(TdsMasterDTO tdsMasterDto, String userName)
			throws RecordNotFoundException, JsonProcessingException {

		for (DeducteeStatusDTO deducteeStatusDTO : tdsMasterDto.getDeducteeStatus()) {
			TdsMaster tdsMasters = new TdsMaster();
			logger.info("REST request to save TdsMaster : {}", tdsMasterDto);
			tdsMasters.setIsSubNaturePaymentMaster(tdsMasterDto.getIsSubNaturePaymentMaster());

			if (!tdsMasterDto.getIsSubNaturePaymentMaster()) {
				Optional<TdsMaster> tdsMasterDb = tdsMasterRepository
						.findByNOPAndStatus(tdsMasterDto.getNatureOfPaymentId(), deducteeStatusDTO.getId());
				if (tdsMasterDb.isPresent()) {
					CommonValidations.validateApplicableFields(tdsMasterDb.get().getApplicableTo(),
							tdsMasterDto.getApplicableFrom());
				}
				NatureOfPaymentMaster natureOfPayment = null;

				Optional<NatureOfPaymentMaster> nature = natureOfPaymentMasterRepository
						.findById(tdsMasterDto.getNatureOfPaymentId());
				if (nature.isPresent()) {
					natureOfPayment = nature.get();
					tdsMasters.setNatureOfPayment(natureOfPayment);
					tdsMasters.setSaccode(tdsMasterDto.getSaccode());
					tdsMasters.setRate(tdsMasterDto.getRate());
					tdsMasters.setActive(true);
					tdsMasters.setCreatedBy(userName);
				}

			} else {
				Optional<TdsMaster> tdsMasterDb = tdsMasterRepository.findBySOPAndDedcuteeStatusId(
						tdsMasterDto.getSubNatureOfPaymentId(), deducteeStatusDTO.getId());
				if (tdsMasterDb.isPresent()) {
					CommonValidations.validateApplicableFields(tdsMasterDb.get().getApplicableTo(),
							tdsMasterDto.getApplicableFrom());
				}
				Optional<SubNaturePaymentMaster> subNature = subNaturePaymentMasterRepository
						.findById(tdsMasterDto.getSubNatureOfPaymentId());
				SubNaturePaymentMaster subNatureOfPayment = null;
				if (subNature.isPresent()) {
					subNatureOfPayment = subNature.get();
					tdsMasters.setSubNatureOfPayment(subNatureOfPayment);
					tdsMasters.setRate(tdsMasterDto.getRate());
					tdsMasters.setActive(true);
				}
			}

			if (tdsMasterDto.getDeducteeStatus() != null) {

				Optional<Status> statusData = statusRepository.findById(deducteeStatusDTO.getId());
				Status status = null;
				if (statusData.isPresent()) {
					status = statusData.get();
					tdsMasters.setDeducteeStatus(status);
				} else {
					throw new CustomException("No record to save Deductee Status Object with Deductee Status ID"
							+ tdsMasterDto.getDeducteeStatusId(), HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}

			//ignored transaction limit
			//if (tdsMasterDto.getIsAnnualTransactionLimitApplicable()
				//	&& (tdsMasterDto.getAnnualTransactionLimit() == null
					//		|| tdsMasterDto.getAnnualTransactionLimit() == 0)) {
				//throw new FieldValidator("If Annnual Limit is true Annual Transaction should not be null");
			//}

			tdsMasters.setIsOverAllTransactionLimit(tdsMasterDto.getIsAnnualTransactionLimitApplicable());
			tdsMasters.setAnnualTransaction(tdsMasterDto.getAnnualTransactionLimit());

			//ignored transaction limit
			//if (tdsMasterDto.getIsPerTransactionLimitApplicable()
				//	&& (tdsMasterDto.getPerTransactionLimit() == null || tdsMasterDto.getPerTransactionLimit() == 0)) {
				//throw new FieldValidator(
					//	"If Transaction Limit Applicable is true Transaction Limit should not be null");
			//}

			tdsMasters.setIsPerTransactionLimit(tdsMasterDto.getIsPerTransactionLimitApplicable());
			tdsMasters.setPerTransactionLimit(tdsMasterDto.getPerTransactionLimit());
			if (tdsMasterDto.getDeducteeResidentialStatusId() != null) {
				Optional<ResidentialStatus> residentialStatusData = residentialStatusRepository
						.findById(tdsMasterDto.getDeducteeResidentialStatusId());

				ResidentialStatus residentialStatus = null;
				if (residentialStatusData.isPresent()) {
					residentialStatus = residentialStatusData.get();
					tdsMasters.setDeducteeResidentStatus(residentialStatus);
				} else {
					throw new RecordNotFoundException(
							"No record to save Deductee Resiential Status Object with Deductee Residential Status ID"
									+ tdsMasterDto.getDeducteeResidentialStatusId());
				}
			}
			tdsMasters.setApplicableFrom(tdsMasterDto.getApplicableFrom());
			tdsMasters.setApplicableTo(tdsMasterDto.getApplicableTo());
			logger.info("ID for Deuductee: {}", deducteeStatusDTO.getId());
			
			Optional<Status> statusData = statusRepository.findById(deducteeStatusDTO.getId());
			if(statusData.isPresent()) {
				tdsMasters.setDeducteeStatus(statusData.get());	
			}
			
			tdsMasters.setNoPanRate(tdsMasterDto.getRateForNoPan());
			tdsMasters.setNoItrRate(tdsMasterDto.getNoItrRate());
			tdsMasters.setNoPanRateAndNoItrRate(tdsMasterDto.getNoPanRateAndNoItrRate());
			tdsMasterRepository.save(tdsMasters);
			logger.info("SAVED Record : {}", tdsMasters);

			if (tdsMasters.getId() != null) {
				ObjectMapper mapper = new ObjectMapper();
				List<NatureOfPaymentRate> natureOfPaymentRates = natureOfPaymentMasterRepository
						.findNatureOfPaymentRates();
				for (NatureOfPaymentRate natureOfPaymentRate : natureOfPaymentRates) {
					String jsonString = mapper.writeValueAsString(natureOfPaymentRate);
					redisUtilUserTenantInfo.putMap(RedisKeys.NATUREOFPAYMENT.name(),
							natureOfPaymentRate.getNatureOfPaymentId().toString() + "_"
									+ natureOfPaymentRate.getDeducteeStatus(),
							jsonString);
				}
			}
		}
		return 1L;
	}

	/**
	 * This method is used to get all TDS Rate Master record
	 * 
	 * @param pageable
	 * @return
	 * @throws RecordNotFoundException
	 */
	@Transactional(readOnly = true)
	public List<TdsMasterDTO> findAll() {

		List<TdsMasterDTO> tdsMasterList = new ArrayList<>();

		List<TdsMaster> listTds = tdsMasterRepository.findAll();

		for (TdsMaster tdsMaster : listTds) {

			TdsMasterDTO tdsMasterDTO = new TdsMasterDTO();
			tdsMasterDTO.setId(tdsMaster.getId());
			tdsMasterDTO.setApplicableFrom(tdsMaster.getApplicableFrom());
			tdsMasterDTO.setApplicableTo(tdsMaster.getApplicableTo());
			tdsMasterDTO.setIsAnnualTransactionLimitApplicable(tdsMaster.getIsOverAllTransactionLimit());
			tdsMasterDTO.setAnnualTransactionLimit(tdsMaster.getAnnualTransaction());
			tdsMasterDTO.setIsPerTransactionLimitApplicable(tdsMaster.getIsPerTransactionLimit());
			tdsMasterDTO.setPerTransactionLimit(tdsMaster.getPerTransactionLimit());
			tdsMasterDTO.setIsSubNaturePaymentMaster(tdsMaster.getIsSubNaturePaymentMaster());
			tdsMasterDTO.setSaccode(tdsMaster.getSaccode());
			tdsMasterDTO.setRate(tdsMaster.getRate());
			tdsMasterDTO.setRateForNoPan(tdsMaster.getNoPanRate());
			tdsMasterDTO.setNoItrRate(tdsMaster.getNoItrRate());
			tdsMasterDTO.setNoPanRateAndNoItrRate(tdsMaster.getNoPanRateAndNoItrRate());
			
			if (tdsMaster.getNatureOfPayment() != null) {
				tdsMasterDTO.setNatureOfPaymentId(tdsMaster.getNatureOfPayment().getId());
				tdsMasterDTO.setNatureOfPaymentMaster(tdsMaster.getNatureOfPayment().getNature());
			}
			if (tdsMaster.getSubNatureOfPayment() != null) {
				tdsMasterDTO.setSubNatureOfPaymentId(tdsMaster.getSubNatureOfPayment().getId());
				tdsMasterDTO.setSubNaturePaymentMaster(tdsMaster.getSubNatureOfPayment().getNature());
			}

			if (tdsMaster.getDeducteeResidentStatus() != null) {
				tdsMasterDTO.setDeducteeResidentialStatusId(tdsMaster.getDeducteeResidentStatus().getId());
				tdsMasterDTO.setResidentialStatusName(tdsMaster.getDeducteeResidentStatus().getStatus());
			}

			if (tdsMaster.getDeducteeStatus() != null) {
				Optional<Status> status = statusRepository.findById(tdsMaster.getDeducteeStatus().getId());
				if (status.isPresent()) {
					tdsMasterDTO.setStatusName(status.get().getStatus());
					tdsMasterDTO.setDeducteeStatusId(status.get().getId());
				}
			}
			tdsMasterList.add(tdsMasterDTO);
		}
		return tdsMasterList;
	}

	/**
	 * This method is used to get TDS Rate Master record based on id
	 * 
	 * @param id
	 * @return
	 * @throws RecordNotFoundException
	 */
	@Transactional(readOnly = true)
	public TdsMasterDTO findOne(Long id) {
		logger.info("REST request id to get TdsMaster : {}", id);
		TdsMaster tdsmasters = null;
		Optional<TdsMaster> tdsmaster = tdsMasterRepository.findById(id);
		TdsMasterDTO tdsMasterDTO = new TdsMasterDTO();
		if (tdsmaster.isPresent()) {

			tdsmasters = tdsmaster.get();
			tdsMasterDTO.setId(tdsmasters.getId());
			tdsMasterDTO.setApplicableFrom(tdsmasters.getApplicableFrom());
			tdsMasterDTO.setApplicableTo(tdsmasters.getApplicableTo());
			tdsMasterDTO.setIsAnnualTransactionLimitApplicable(tdsmasters.getIsOverAllTransactionLimit());
			tdsMasterDTO.setAnnualTransactionLimit(tdsmasters.getAnnualTransaction());
			tdsMasterDTO.setIsPerTransactionLimitApplicable(tdsmasters.getIsPerTransactionLimit());
			tdsMasterDTO.setPerTransactionLimit(tdsmasters.getPerTransactionLimit());
			tdsMasterDTO.setIsSubNaturePaymentMaster(tdsmasters.getIsSubNaturePaymentMaster());
			tdsMasterDTO.setRate(tdsmasters.getRate());
			tdsMasterDTO.setSaccode(tdsmasters.getSaccode());
			tdsMasterDTO.setRateForNoPan(tdsmasters.getNoPanRate());
			tdsMasterDTO.setNoItrRate(tdsmasters.getNoItrRate());
			tdsMasterDTO.setNoPanRateAndNoItrRate(tdsmasters.getNoPanRateAndNoItrRate());
			
			if (tdsmasters.getNatureOfPayment() != null) {
				tdsMasterDTO.setNatureOfPaymentId(tdsmasters.getNatureOfPayment().getId());
				tdsMasterDTO.setNatureOfPaymentMaster(tdsmasters.getNatureOfPayment().getNature());
			}
			if (tdsmasters.getSubNatureOfPayment() != null) {
				tdsMasterDTO.setSubNatureOfPaymentId(tdsmasters.getSubNatureOfPayment().getId());
				tdsMasterDTO.setSubNaturePaymentMaster(tdsmasters.getSubNatureOfPayment().getNature());
			}

			if (tdsmasters.getDeducteeResidentStatus() != null) {
				tdsMasterDTO.setDeducteeResidentialStatusId(tdsmasters.getDeducteeResidentStatus().getId());
				tdsMasterDTO.setResidentialStatusName(tdsmasters.getDeducteeResidentStatus().getStatus());
			}

			if (tdsmasters.getDeducteeStatus() != null) {
				tdsMasterDTO.setDeducteeStatusId(tdsmasters.getDeducteeStatus().getId());
				tdsMasterDTO.setStatusName(tdsmasters.getDeducteeStatus().getStatus());
			}

		}
		return tdsMasterDTO;
	}

	/**
	 * This method is used to delete TDS Rate Master record based on id
	 * 
	 * @param id
	 * @throws RecordNotFoundException
	 */
	public void delete(Long id) throws RecordNotFoundException {
		logger.info("REST request id to delete TdsMaster : {}", id);
		Optional<TdsMaster> tdsMaster = tdsMasterRepository.findById(id);
		if (tdsMaster.isPresent()) {
			tdsMasterRepository.deleteById(id);
		} else {
			throw new RecordNotFoundException("No record found  with id " + id);
		}
	}

	/**
	 * This method is used to update TDS Rate Master record
	 * 
	 * @param tdsMasterDto
	 * @param userName
	 * @return
	 * @throws RecordNotFoundException
	 */
	public TdsMaster updateTdsMaster(@Valid TdsMasterDTO tdsMasterDto, String userName) throws RecordNotFoundException {
		logger.info("REST request to update TdsMaster : {}", tdsMasterDto);
		Optional<TdsMaster> tdsMasters = tdsMasterRepository.findById(tdsMasterDto.getId());
		TdsMaster tdsMasterss = null;
		if (tdsMasters.isPresent()) {
			tdsMasterss = tdsMasters.get();
			if (!tdsMasterDto.getIsSubNaturePaymentMaster()) {
				NatureOfPaymentMaster natureOfPayment = null;

				Optional<NatureOfPaymentMaster> nature = natureOfPaymentMasterRepository
						.findById(tdsMasterDto.getNatureOfPaymentId());
				if (nature.isPresent()) {
					natureOfPayment = nature.get();
					tdsMasterss.setNatureOfPayment(natureOfPayment);
					tdsMasterss.setSaccode(tdsMasterDto.getSaccode());
					tdsMasterss.setRate(tdsMasterDto.getRate());
					tdsMasterss.setModifiedBy(userName);
					tdsMasterss.setModifiedDate(Instant.now());
				}
			} else {
				Optional<SubNaturePaymentMaster> subNature = subNaturePaymentMasterRepository
						.findById(tdsMasterDto.getSubNatureOfPaymentId());
				SubNaturePaymentMaster subNatureOfPayment = null;
				if (subNature.isPresent()) {
					subNatureOfPayment = subNature.get();
					tdsMasterss.setSubNatureOfPayment(subNatureOfPayment);
					tdsMasterss.setRate(tdsMasterDto.getRate());
				}
			}
			tdsMasterss.setApplicableFrom(tdsMasterDto.getApplicableFrom());
			tdsMasterss.setApplicableTo(tdsMasterDto.getApplicableTo());

			Optional<Status> statusData = statusRepository.findById(tdsMasterDto.getDeducteeStatusId());
			Status status = null;
			if (statusData.isPresent()) {
				status = statusData.get();
			} else {
				throw new RecordNotFoundException("No record to save Deductee Status Object with Deductee Status ID"
						+ tdsMasterDto.getDeducteeStatusId());
			}
			tdsMasterss.setDeducteeStatus(status);

			tdsMasterss.setId(tdsMasterDto.getId());

			if (tdsMasterDto.getDeducteeResidentialStatusId() != null) {

				Optional<ResidentialStatus> residentialStatusData = residentialStatusRepository
						.findById(tdsMasterDto.getDeducteeResidentialStatusId());

				ResidentialStatus residentialStatus = null;
				if (residentialStatusData.isPresent()) {
					residentialStatus = residentialStatusData.get();
				} else {
					throw new RecordNotFoundException(
							"No record to save Deductee Resiential Status Object with Deductee Residential Status ID"
									+ tdsMasterDto.getDeducteeResidentialStatusId());
				}

				tdsMasterss.setDeducteeResidentStatus(residentialStatus);
			}

			/*
			 * if (tdsMasterDto.getIsAnnualTransactionLimitApplicable()) {
			 * tdsMasterss.setAnnualTransaction(tdsMasterDto.getAnnualTransactionLimit()); }
			 */
			if (tdsMasterDto.getIsPerTransactionLimitApplicable()) {
				tdsMasterss.setPerTransactionLimit(tdsMasterDto.getPerTransactionLimit());
			}

		} else {
			throw new RecordNotFoundException("No record found  for TDS RATE Master with id " + tdsMasterDto.getId());
		}

		tdsMasterss.setNoPanRate(tdsMasterDto.getRateForNoPan());
		tdsMasterss.setNoItrRate(tdsMasterDto.getNoItrRate());
		tdsMasterss.setNoPanRateAndNoItrRate(tdsMasterDto.getNoPanRateAndNoItrRate());
		return tdsMasterRepository.save(tdsMasterss);

	}

	public List<TdsMaster> getTdsMasterBySection(String section, int nopId, String residentialStatus, String status) {
		return tdsMasterRepository.getTdsMasterBySection(section, nopId, residentialStatus, status);
	}
	
	public String getTdsResidentialStatusBySection(String section) {
		String residentialStatus = StringUtils.EMPTY;
		List<String> statusList = natureOfPaymentMasterRepository.getResidentialStatusBasedOnSection(section);
		if (!statusList.isEmpty()) {
			residentialStatus = statusList.get(0);
		}
		return residentialStatus;
	}

	/**
	 * 
	 * @param natureId
	 * @return
	 */
	public Optional<TdsMaster> getTdsRateBasedOnNatureId(Long natureId) {
		Optional<TdsMaster> tdsRateMasterOptional = tdsMasterRepository.getTdsRateMasterRate(natureId);
		if (tdsRateMasterOptional.isPresent()) {
			return tdsRateMasterOptional;
		} else {
			return Optional.empty();
		}
	}
	
	public Map<String, String> getResidentialStatusForSections() {
		Map<String, String> residentialStatusMap = new HashMap<>();
		List<Map<String, Object>> residentialStatusList = natureOfPaymentMasterRepository
				.getResidentialStatusForSections();
		for (Map<String, Object> map : residentialStatusList) {
			String section = (String) map.get("section");
			String rs = (String) map.get("status");
			residentialStatusMap.put(section, rs);
		}
		return residentialStatusMap;
	}
	
	
}
