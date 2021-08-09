package com.ey.in.tds.tcs.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ey.in.tds.common.domain.ResidentialStatus;
import com.ey.in.tds.common.domain.Status;
import com.ey.in.tds.common.domain.tcs.TCSNatureOfIncome;
import com.ey.in.tds.common.domain.tcs.TCSSurchargeMaster;
import com.ey.in.tds.common.tcs.repository.TCSNatureOfIncomeRepository;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.repository.ResidentialStatusRepository;
import com.ey.in.tds.repository.StatusRepository;
import com.ey.in.tds.tcs.dto.TCSBasOfSurchargeDetailsDTO;
import com.ey.in.tds.tcs.dto.TCSSurchargeMasterDTO;
import com.ey.in.tds.tcs.repository.TCSSurchargeMasterRepository;

@Service
@Transactional
public class TCSSurchargeMasterService {

	private final Logger logger = LoggerFactory.getLogger(TCSSurchargeMasterService.class);

	@Autowired
	private TCSSurchargeMasterRepository tcsSurchargeMasterRepository;

	@Autowired
	private ResidentialStatusRepository residentialStatusRepository;

	@Autowired
	private StatusRepository statusRepository;

	@Autowired
	private TCSNatureOfIncomeRepository tcsNatureOfIncomeRepository;

	/**
	 * This method is to create TCSSurcharge Master
	 * 
	 * @param surchargeMasterDTO
	 * @param userName
	 * @return
	 */
	public TCSSurchargeMasterDTO createSurchargeMaster(TCSSurchargeMasterDTO surchargeMasterDTO, String userName) {

		logger.info("REST request to save SurchargeMaster : {}", surchargeMasterDTO);

		TCSSurchargeMaster surchargeMaster = new TCSSurchargeMaster();

		surchargeMaster.setApplicableFrom(surchargeMasterDTO.getApplicableFrom());
		surchargeMaster.setApplicableTo(surchargeMasterDTO.getApplicableTo());
		// surchargeMaster.setRate(surchargeMasterDTO.getSurchargeRate());
		// surchargeMaster.setIsSurchargeApplicable(surchargeMasterDTO.isSurchargeApplicable());
		surchargeMaster.setCreatedBy(userName);
		surchargeMaster.setCreatedDate(Instant.now());
		surchargeMaster.setModifiedBy(userName);
		surchargeMaster.setModifiedDate(Instant.now());
		surchargeMaster.setActive(true);

		// TODO need to asak for duplicate logic

		List<TCSBasOfSurchargeDetailsDTO> setBasisOfSurchargeDetails = surchargeMasterDTO.getBasisOfSurchargeDetails();

		// filtering unique records
		List<TCSBasOfSurchargeDetailsDTO> uniqueBasisOfSurchargeDetails = new ArrayList<TCSBasOfSurchargeDetailsDTO>();
		Map<TCSBasOfSurchargeDetailsDTO, Integer> map = new HashMap<TCSBasOfSurchargeDetailsDTO, Integer>();
		for (TCSBasOfSurchargeDetailsDTO surchargeDto : setBasisOfSurchargeDetails) {
			if (map.containsKey(surchargeDto)) {
				throw new CustomException("Duplicate Section Not Allowed", HttpStatus.BAD_REQUEST);
			} else {
				map.put(surchargeDto, 1);
				uniqueBasisOfSurchargeDetails.add(surchargeDto);
			}
		}

		for (TCSBasOfSurchargeDetailsDTO basisOfSurcharge : uniqueBasisOfSurchargeDetails) {

			/*
			 * if (surchargeMasterDTO.getBocCollecteeStatus() != null) { Optional<Status>
			 * statusData =
			 * statusRepository.findById(basisOfSurcharge.getDeducteeStatusId()); //status
			 * id need to be passed if (statusData.isPresent()) {
			 * surchargeMaster.setCollecteeStatus(statusData.get()); } }
			 */
			/*
			 * if (surchargeMasterDTO.getBocCollecteeResidentialStatus() != null) {
			 * Optional<ResidentialStatus> residentialStatusData =
			 * residentialStatusRepository
			 * .findById(surchargeMasterDTO.getBocCollecteeResidentialStatus()); if
			 * (residentialStatusData.isPresent()) {
			 * surchargeMaster.setCollecteeResidentialStatus(residentialStatusData.get()); }
			 * }
			 */

			/*
			 * if (surchargeMasterDTO.getBocNatureOfIncome() != null) {
			 * Optional<TCSNatureOfIncome> tcsNatureOfIncome = tcsNatureOfIncomeRepository
			 * .findById(surchargeMasterDTO.getBocNatureOfIncome()); if
			 * (tcsNatureOfIncome.isPresent()) {
			 * surchargeMaster.setTcsNatureOfIncome(tcsNatureOfIncome.get()); } }
			 */
			surchargeMaster.setApplicableFrom(surchargeMasterDTO.getApplicableFrom());
			surchargeMaster.setApplicableTo(surchargeMasterDTO.getApplicableTo());
			// surchargeMaster.setRate(surchargeMasterDTO.getSurchargeRate());
			// surchargeMaster.setIsSurchargeApplicable(surchargeMasterDTO.isSurchargeApplicable());
			surchargeMaster.setCreatedBy(userName);
			surchargeMaster.setCreatedDate(Instant.now());
			// surchargeMaster.setModifiedBy(userName);
			// surchargeMaster.setModifiedDate(Instant.now());
			surchargeMaster.setActive(true);
			surchargeMaster.setNatureOfIncomeId(basisOfSurcharge.getNatureOfIncomeMasterId());
			surchargeMaster.setCollecteeStatusId(basisOfSurcharge.getCollecteeStatusId());
			surchargeMaster.setCollecteeResidentialStatusId(basisOfSurcharge.getCollecteeResidentialStatusId());
			surchargeMaster.setInvoiceSlabFrom(basisOfSurcharge.getInvoiceSlabFrom());
			surchargeMaster.setInvoiceSlabTo(basisOfSurcharge.getInvoiceSlabTo());
			surchargeMaster.setAmount(new BigDecimal(basisOfSurcharge.getInvoiceSlabFrom()));
			surchargeMaster.setRate(basisOfSurcharge.getRate());

		}

		try {
			surchargeMaster = tcsSurchargeMasterRepository.save(surchargeMaster);
		} catch (Exception ex) {
			logger.error("Error occured while saving record", ex);
		}
		return getTCSSurchargeMasterDTOFrom(surchargeMaster);

	}

	/**
	 * Get all the surchargeMasters.
	 *
	 * @param pageable the pagination information
	 * @return the list of entities
	 */
	@Transactional(readOnly = true)
	public List<TCSSurchargeMasterDTO> findAll() {
		List<TCSSurchargeMasterDTO> surchargeMasterListDTO = new ArrayList<>();
		List<TCSSurchargeMaster> surchargeMasterList = tcsSurchargeMasterRepository.findAll();

		for (TCSSurchargeMaster surchargeMaster : surchargeMasterList) {
			surchargeMasterListDTO.add(getTCSSurchargeMasterDTOFrom(surchargeMaster));
		}
		return surchargeMasterListDTO;

	}

	private TCSSurchargeMasterDTO getTCSSurchargeMasterDTOFrom(TCSSurchargeMaster surchargeMaster) {
		List<TCSBasOfSurchargeDetailsDTO> listOfBasis=new ArrayList<>();
		TCSBasOfSurchargeDetailsDTO basis=new TCSBasOfSurchargeDetailsDTO();
		TCSSurchargeMasterDTO surchargeMasterDTO = new TCSSurchargeMasterDTO();
		surchargeMasterDTO.setBasisOfSurchargeDetails(listOfBasis);
		surchargeMasterDTO.setId(surchargeMaster.getId());
		surchargeMasterDTO.setApplicableFrom(surchargeMaster.getApplicableFrom());
		surchargeMasterDTO.setApplicableTo(surchargeMaster.getApplicableTo());
		// surchargeMasterDTO.setSurchargeApplicable(surchargeMaster.getIsSurchargeApplicable());
		// surchargeMasterDTO.setSurchargeRate(surchargeMaster.getRate());
		surchargeMasterDTO.setRate(surchargeMaster.getRate());
		
		surchargeMasterDTO.setBocInvoiceSlab(
				surchargeMaster.getInvoiceSlabFrom() != null ? new BigDecimal(surchargeMaster.getInvoiceSlabFrom())
						: BigDecimal.ZERO);
		if (surchargeMaster.getCollecteeResidentialStatusId() != null) {
			Optional<ResidentialStatus> residentialStatus = residentialStatusRepository
					.findById(surchargeMaster.getCollecteeResidentialStatusId());
			if (residentialStatus.isPresent()) {
				if(residentialStatus.get().getStatus().equals("NR")) {
				surchargeMasterDTO.setCollecteeResidentialStatus("Non-Resident");
				basis.setCollecteeResidentialStatus("Non-Resident");
				}
			}
		}
		if (surchargeMaster.getCollecteeStatusId() != null) {
			Optional<Status> status = statusRepository.findById(surchargeMaster.getCollecteeStatusId());
			if (status.isPresent()) {
				surchargeMasterDTO.setCollecteeStatus(status.get().getStatus());
			}
		}
		if (surchargeMaster.getNatureOfIncomeId() != null) {
			Optional<TCSNatureOfIncome> natureOfIncome = tcsNatureOfIncomeRepository
					.findById(surchargeMaster.getNatureOfIncomeId());
			if (natureOfIncome.isPresent()) {
				surchargeMasterDTO.setNatureOfIncome(natureOfIncome.get().getNature());
			}
		}
		basis.setCollecteeResidentialStatusId(surchargeMaster.getCollecteeResidentialStatusId());
		basis.setNatureOfIncomeMasterId(surchargeMaster.getNatureOfIncomeId());
		basis.setCollecteeStatusId(surchargeMaster.getCollecteeStatusId());
		basis.setRate(surchargeMaster.getRate());
		basis.setInvoiceSlabFrom(surchargeMaster.getInvoiceSlabFrom());
		basis.setInvoiceSlabTo(surchargeMaster.getInvoiceSlabTo());
		listOfBasis.add(basis);
		/*
		 * if (surchargeMaster.getCollecteeResidentialStatus() != null) {
		 * surchargeMasterDTO.setBocCollecteeResidentialStatus(surchargeMaster.
		 * getCollecteeResidentialStatus().getId()); }
		 * 
		 * if (surchargeMaster.getCollecteeStatus() != null) {
		 * surchargeMasterDTO.setBocCollecteeStatus(surchargeMaster.getCollecteeStatus()
		 * .getId()); }
		 * 
		 * if (surchargeMaster.getNatureOfincome() != null) {
		 * surchargeMasterDTO.setBocNatureOfIncome(surchargeMaster.getNatureOfincome().
		 * getId()); }
		 */
		return surchargeMasterDTO;
	}

	/**
	 * This method is to update Surcharge Master
	 * 
	 * @param surchargeMasterDTO
	 * @param userName
	 * @return
	 */

	public Long updateTCSSurchargeMaster(TCSSurchargeMasterDTO surchargeMasterDTO, String userName) {

		logger.info("REST request to save TCSSurchargeMaster : {}", surchargeMasterDTO);
		TCSSurchargeMaster surchargeMaster = null;
		Optional<TCSSurchargeMaster> surchargeMasterData = tcsSurchargeMasterRepository
				.findById(surchargeMasterDTO.getId());

		if (surchargeMasterData.isPresent()) {

			surchargeMaster = surchargeMasterData.get();
			// surchargeMaster.setRate(surchargeMasterDTO.getSurchargeRate());
		//	surchargeMaster.setAmount(surchargeMasterDTO.getBocInvoiceSlab());
		//	surchargeMaster.setApplicableFrom(surchargeMasterDTO.getApplicableFrom());
			surchargeMaster.setApplicableTo(surchargeMasterDTO.getApplicableTo());
			// surchargeMaster.setIsSurchargeApplicable(surchargeMasterDTO.isSurchargeApplicable());
			// surchargeMaster.setRate(surchargeMasterDTO.getSurchargeRate());
			surchargeMaster.setModifiedBy(userName);
			surchargeMaster.setModifiedDate(Instant.now());

			/*
			 * if (surchargeMasterDTO.getBocNatureOfIncome() != null) {
			 * Optional<TCSNatureOfIncome> natureOfPaymentMaster =
			 * tcsNatureOfIncomeRepository
			 * .findById(surchargeMasterDTO.getBocNatureOfIncome()); if
			 * (natureOfPaymentMaster.isPresent()) { //
			 * surchargeMaster.setNatureOfincome(natureOfPaymentMaster.get()); } } if
			 * (surchargeMasterDTO.getBocCollecteeStatus() != null) { Optional<Status>
			 * statusData =
			 * statusRepository.findById(surchargeMasterDTO.getBocCollecteeStatus()); if
			 * (statusData.isPresent()) {
			 * //surchargeMaster.setCollecteeStatus(statusData.get()); } } if
			 * (surchargeMasterDTO.getBocCollecteeResidentialStatus() != null) {
			 * Optional<ResidentialStatus> residentialStatusData =
			 * residentialStatusRepository
			 * .findById(surchargeMasterDTO.getBocCollecteeResidentialStatus());
			 * 
			 * if (residentialStatusData.isPresent()) {
			 * //surchargeMaster.setCollecteeResidentialStatus(residentialStatusData.get());
			 * } }
			 */
		} else {
			throw new CustomException("Surcharge Master Entity not found", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		surchargeMaster = tcsSurchargeMasterRepository.save(surchargeMaster);
		return surchargeMaster.getId();
	}

	/**
	 * Get one tcsSurchargeMaster by id.
	 *
	 * @param id the id of the entity
	 * @return the entity
	 * @throws RecordNotFoundException
	 */
	@Transactional(readOnly = true)
	public TCSSurchargeMasterDTO findOne(Long id) throws RecordNotFoundException {
		logger.info("REST request id to get TCSSurchargeMaster : {}", id);
		Optional<TCSSurchargeMaster> optionalTCSSurchargeMaster = tcsSurchargeMasterRepository.findById(id);

		if (optionalTCSSurchargeMaster.isPresent()) {
			return getTCSSurchargeMasterDTOFrom(optionalTCSSurchargeMaster.get());
		} else {
			throw new CustomException("TCS Surcharge Master id not found", HttpStatus.NOT_FOUND);
		}

	}

//	// Feign Client - Get Nature Of payment Rate for Basis Of Surcharge Details
//	public BasisOfSurchargeDetails getBasisOfSurchargeRecordBasedOnNatureOfPaymentId(Long id) {
//		BasisOfSurchargeDetails basisOfSurchargeDetails = null;
//		List<BasisOfSurchargeDetails> basisOfSurcharge = basisOfSurchargeDetailsRepository
//				.findListOfSurchargeRateByNatureOfPaymentId(id);
//		if (basisOfSurcharge.size() > 0) {
//			basisOfSurchargeDetails = basisOfSurcharge.get(0);
//		} else {
//			basisOfSurchargeDetails = new BasisOfSurchargeDetails();
//		}
//		return basisOfSurchargeDetails;
//	}

}
