package com.ey.in.tds.service;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ey.in.tds.common.domain.ArticleMaster;
import com.ey.in.tds.common.domain.BasisOfCessDetails;
import com.ey.in.tds.common.domain.CessMaster;
import com.ey.in.tds.common.domain.CessTypeMaster;
import com.ey.in.tds.common.domain.MasterBatchUpload;
import com.ey.in.tds.common.domain.NatureOfPaymentMaster;
import com.ey.in.tds.common.domain.ResidentialStatus;
import com.ey.in.tds.common.domain.Status;
import com.ey.in.tds.common.dto.SurchargeAndCessDTO;
import com.ey.in.tds.common.repository.NatureOfPaymentMasterRepository;
import com.ey.in.tds.common.service.Sha256SumService;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.dto.ArticleRateMasterDTO;
import com.ey.in.tds.dto.ArticleRateMasterErrorReportDTO;
import com.ey.in.tds.dto.BasisOfCessDetailsDTO;
import com.ey.in.tds.dto.CessMasterDTO;
import com.ey.in.tds.dto.SurchargeAndCessRateDTO;
import com.ey.in.tds.repository.BasisOfCessDetailsRepository;
import com.ey.in.tds.repository.CessMasterRepository;
import com.ey.in.tds.repository.CessTypeMasterRepository;
import com.ey.in.tds.repository.MasterBatchUploadRepository;
import com.ey.in.tds.repository.ResidentialStatusRepository;
import com.ey.in.tds.repository.StatusRepository;
import com.ey.in.tds.service.util.excel.CessMasterExcel;
import com.microsoft.azure.storage.StorageException;

@Service
public class CessMasterService {

	private final CessMasterRepository cessMasterRepository;
	private final CessTypeMasterRepository cessTypeMasterRepository;
	private final NatureOfPaymentMasterRepository natureOfPaymentMasterRepository;
	private final BasisOfCessDetailsRepository basisOfCessDetailsRepository;

	@Autowired
	private StatusRepository statusRepository;

	@Autowired
	private ResidentialStatusRepository residentialStatusRepository;
	
	@Autowired
	private Sha256SumService sha256SumService;
	
	@Autowired
	private MasterBatchUploadService masterBatchUploadService;
	
	@Autowired
	private MasterBatchUploadRepository masterBatchUploadRepository;

	private final Logger logger = LoggerFactory.getLogger(CessMasterService.class);

	public CessMasterService(CessMasterRepository cessMasterRepository,
			CessTypeMasterRepository cessTypeMasterRepository,
			NatureOfPaymentMasterRepository natureOfPaymentMasterRepository,
			BasisOfCessDetailsRepository basisOfCessDetailsRepository) {
		this.cessMasterRepository = cessMasterRepository;
		this.cessTypeMasterRepository = cessTypeMasterRepository;
		this.natureOfPaymentMasterRepository = natureOfPaymentMasterRepository;
		this.basisOfCessDetailsRepository = basisOfCessDetailsRepository;
	}

	/**
	 * Save a cessMaster.
	 * 
	 * @param userName
	 *
	 * @param cessMaster the entity to save
	 * @return the persisted entity
	 */
	public Long saveCessMaster(CessMasterDTO cessMasterDTO, String userName) { // save
		if (logger.isDebugEnabled()) {
			logger.debug("REST request to save CessMaster : {}", cessMasterDTO);
		}

		CessMaster cessMaster = new CessMaster();

		saveAndUpdateCommonLogic(cessMaster, cessMasterDTO);
		cessMaster.setActive(true);
		cessMaster.setCreatedBy(userName);
		cessMaster.setCreatedDate(Instant.now());
		cessMaster.setModifiedBy(userName);
		cessMaster.setModifiedDate(Instant.now());
		try {
			cessMaster = cessMasterRepository.save(cessMaster);
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
	public void saveCessMasterCommonFields(CessMaster cessMaster, CessMasterDTO cessMasterDTO) {
		cessMaster.setIsCessApplicable(cessMasterDTO.getIsCessApplicable());
		cessMaster.setApplicableFrom(cessMasterDTO.getApplicableFrom());
		cessMaster.setApplicableTo(cessMasterDTO.getApplicableTo());
		cessMaster.setBocDeducteeStatus(cessMasterDTO.getBocDeducteeStatus());
		cessMaster.setBocInvoiceSlab(cessMasterDTO.getBocInvoiceSlab());
		cessMaster.setBocNatureOfPayment(cessMasterDTO.getBocNatureOfPayment());
		cessMaster.setBocResidentialStatus(cessMasterDTO.getBocDeducteeResidentialStatus());
	}

	/**
	 * Get all the cessMasters.
	 *
	 * @param pageable the pagination information
	 * @return the list of entities
	 */
	@Transactional(readOnly = true)
	public List<CessMasterDTO> findAll() {
		List<CessMasterDTO> cessMasterListDTO = null;
		CessMasterDTO cessMasterDTO = null;
		List<BasisOfCessDetails> basisOfCess = null;
		List<BasisOfCessDetailsDTO> basisOfCessDetailsListDTO = null;
		BasisOfCessDetailsDTO basisOfCessDetailsDTO = null;

		List<CessMaster> cessMasterList = cessMasterRepository.findAll();

		cessMasterListDTO = new ArrayList<>();
		for (CessMaster cessMaster : cessMasterList) {
			cessMasterDTO = new CessMasterDTO();
			cessMasterDTO.setId(cessMaster.getId());
			cessMasterDTO.setApplicableFrom(cessMaster.getApplicableFrom());
			cessMasterDTO.setApplicableTo(cessMaster.getApplicableTo());
			cessMasterDTO.setIsCessApplicable(cessMaster.getIsCessApplicable());
			cessMasterDTO.setBocDeducteeResidentialStatus(cessMaster.getBocResidentialStatus());
			cessMasterDTO.setBocDeducteeStatus(cessMaster.getBocDeducteeStatus());
			cessMasterDTO.setBocInvoiceSlab(cessMaster.getBocInvoiceSlab());
			cessMasterDTO.setBocNatureOfPayment(cessMaster.getBocNatureOfPayment());
			cessMasterDTO.setRate(cessMaster.getRate());
			cessMasterDTO.setActive(cessMaster.isActive());
			if (cessMaster.getCessTypeMaster() != null && cessMaster.getIsCessApplicable()) {
				cessMasterDTO.setCessTypeId(cessMaster.getCessTypeMaster().getId());
				cessMasterDTO.setCessTypeName(cessMaster.getCessTypeMaster().getCessType());
			}

			if (!cessMasterDTO.getIsCessApplicable()) {
				basisOfCess = basisOfCessDetailsRepository.findByCessMasterId(cessMaster.getId());

				if (!basisOfCess.isEmpty()) {
					basisOfCessDetailsListDTO = new ArrayList<>();

					for (BasisOfCessDetails basisOfCessMaster : basisOfCess) {
						basisOfCessDetailsDTO = new BasisOfCessDetailsDTO();
						basisOfCessDetailsDTO.setId(basisOfCessMaster.getId());

						if (basisOfCessMaster.getDeducteeStatus() != null) {
							basisOfCessDetailsDTO.setDeducteeStatusId(basisOfCessMaster.getDeducteeStatus().getId());
							basisOfCessDetailsDTO.setDeducteeStatus(basisOfCessMaster.getDeducteeStatus().getStatus());
						}
						if (basisOfCessMaster.getDeducteeResidentialStatus() != null) {
							basisOfCessDetailsDTO.setDeducteeResidentialStatusId(
									basisOfCessMaster.getDeducteeResidentialStatus().getId());
							basisOfCessDetailsDTO.setDeducteeResidentialStatus(
									basisOfCessMaster.getDeducteeResidentialStatus().getStatus());
						}
						if (basisOfCessMaster.getNatureOfPaymentMaster() != null) {
							basisOfCessDetailsDTO
									.setNatureOfPaymentMasterId(basisOfCessMaster.getNatureOfPaymentMaster().getId());
							basisOfCessDetailsDTO.setNature(basisOfCessMaster.getNatureOfPaymentMaster().getNature());
						}
						basisOfCessDetailsDTO.setInvoiceSlabFrom(basisOfCessMaster.getInvoiceSlabFrom());
						basisOfCessDetailsDTO.setInvoiceSlabTo(basisOfCessMaster.getInvoiceSlabTo());
						basisOfCessDetailsDTO.setCessTypeId(cessMaster.getCessTypeMaster().getId());
						basisOfCessDetailsDTO.setCessTypeName(cessMaster.getCessTypeMaster().getCessType());
						basisOfCessDetailsDTO.setRate(basisOfCessMaster.getRate());
						basisOfCessDetailsListDTO.add(basisOfCessDetailsDTO);

					}
					cessMasterDTO.setBasisOfCessDetails(basisOfCessDetailsListDTO);
				}
			} else {
				basisOfCessDetailsListDTO = new ArrayList<>();
				basisOfCessDetailsListDTO.add(new BasisOfCessDetailsDTO());
				cessMasterDTO.setBasisOfCessDetails(basisOfCessDetailsListDTO);
			}
			cessMasterListDTO.add(cessMasterDTO);

		}
		return cessMasterListDTO;
	}

	/**
	 * Get one cessMaster by id.
	 *
	 * @param id the id of the entity
	 * @return the entity
	 * @throws RecordNotFoundException
	 */
	@Transactional(readOnly = true)
	public CessMasterDTO findOne(Long id) {
		if (logger.isDebugEnabled()) {
			logger.debug("REST request Id to get CessMaster : {}", id);
		}
		Optional<CessMaster> cessMasterData = cessMasterRepository.findById(id);
		CessMaster cessMaster = null;
		CessMasterDTO cessMasterDTO = new CessMasterDTO();
		if (cessMasterData.isPresent()) {
			cessMaster = cessMasterData.get();

			cessMasterDTO.setId(cessMaster.getId());
			cessMasterDTO.setIsCessApplicable(cessMaster.getIsCessApplicable());
			cessMasterDTO.setApplicableFrom(cessMaster.getApplicableFrom());
			cessMasterDTO.setApplicableTo(cessMaster.getApplicableTo());
			cessMasterDTO.setActive(cessMaster.isActive());

			if (cessMaster.getIsCessApplicable()) {
				cessMasterDTO.setRate(cessMaster.getRate());
				cessMasterDTO.setCessTypeId(cessMaster.getCessTypeMaster().getId());
				cessMasterDTO.setCessTypeName(cessMaster.getCessTypeMaster().getCessType());

			} else {
				cessMasterDTO.setBocDeducteeResidentialStatus(cessMaster.getBocResidentialStatus());
				cessMasterDTO.setBocDeducteeStatus(cessMaster.getBocDeducteeStatus());
				cessMasterDTO.setBocInvoiceSlab(cessMaster.getBocInvoiceSlab());
				cessMasterDTO.setBocNatureOfPayment(cessMaster.getBocNatureOfPayment());
				List<BasisOfCessDetails> basisOfCess = basisOfCessDetailsRepository
						.findByCessMasterId(cessMaster.getId());
				List<BasisOfCessDetailsDTO> basisOfCessDetailsListDTO = null;
				BasisOfCessDetailsDTO basisOfCessDetailsDTO = null;
				if (!basisOfCess.isEmpty()) {

					basisOfCessDetailsListDTO = new ArrayList<>();
					for (BasisOfCessDetails basisOfCessMaster : basisOfCess) {
						basisOfCessDetailsDTO = new BasisOfCessDetailsDTO();
						basisOfCessDetailsDTO.setId(basisOfCessMaster.getId());
						if (basisOfCessMaster.getDeducteeResidentialStatus() != null
								&& basisOfCessMaster.getDeducteeResidentialStatus().getId() != null) {
							basisOfCessDetailsDTO.setDeducteeResidentialStatusId(
									basisOfCessMaster.getDeducteeResidentialStatus().getId());
						}
						if (basisOfCessMaster.getDeducteeStatus() != null
								&& basisOfCessMaster.getDeducteeStatus().getId() != null) {
							basisOfCessDetailsDTO.setDeducteeStatusId(basisOfCessMaster.getDeducteeStatus().getId());
						}
						if (basisOfCessMaster.getNatureOfPaymentMaster() != null
								&& basisOfCessMaster.getNatureOfPaymentMaster().getId() != null) {
							basisOfCessDetailsDTO
									.setNatureOfPaymentMasterId(basisOfCessMaster.getNatureOfPaymentMaster().getId());
							basisOfCessDetailsDTO.setNature(basisOfCessMaster.getNatureOfPaymentMaster().getNature());
						}
						basisOfCessDetailsDTO.setInvoiceSlabFrom(basisOfCessMaster.getInvoiceSlabFrom());
						basisOfCessDetailsDTO.setInvoiceSlabTo(basisOfCessMaster.getInvoiceSlabTo());
						basisOfCessDetailsDTO.setRate(basisOfCessMaster.getRate());
						basisOfCessDetailsDTO.setCessTypeId(cessMaster.getCessTypeMaster().getId());
						basisOfCessDetailsDTO.setCessTypeName(cessMaster.getCessTypeMaster().getCessType());
						basisOfCessDetailsListDTO.add(basisOfCessDetailsDTO);
					}

					cessMasterDTO.setBasisOfCessDetails(basisOfCessDetailsListDTO);

				} else {
					basisOfCessDetailsListDTO = new ArrayList<>();
					basisOfCessDetailsListDTO.add(new BasisOfCessDetailsDTO());
					cessMasterDTO.setBasisOfCessDetails(basisOfCessDetailsListDTO);
				}
			}
			return cessMasterDTO;
		}
		return cessMasterDTO;
	}

	public CessMaster updateCessMaster(@Valid CessMasterDTO cessMasterDTO, String userName) {
		logger.info("REST request to update CessMaster : {}", cessMasterDTO);
		CessMaster cessMaster = null;
		Optional<CessMaster> cessMasterData = cessMasterRepository.findById(cessMasterDTO.getId());

		if (cessMasterData.isPresent()) {
			cessMaster = cessMasterData.get();
			saveAndUpdateCommonLogic(cessMaster, cessMasterDTO);
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
	public void saveAndUpdateCommonLogic(CessMaster cessMaster, @Valid CessMasterDTO cessMasterDTO) {
		// If cess rate applicable
		if (cessMasterDTO.getIsCessApplicable()) {
			// Getting data from CessType master table
			Optional<CessTypeMaster> cessTypeMaster = cessTypeMasterRepository.findById(cessMasterDTO.getCessTypeId());
			if (cessTypeMaster.isPresent()) {
				cessMaster.setCessTypeMaster(cessTypeMaster.get());
			}
			CessMaster retrievedCessMaster = cessMasterRepository
					.getCessMasterByCessTypeID(cessMasterDTO.getCessTypeId());

			if(retrievedCessMaster != null && cessMasterDTO.getApplicableFrom().equals(retrievedCessMaster.getApplicableFrom())) {
				throw new CustomException("A record is already there with same Applicable From Date",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
			if (retrievedCessMaster != null && retrievedCessMaster.getRate().equals(cessMasterDTO.getRate())
					&& retrievedCessMaster.getApplicableTo() != null
					&& !retrievedCessMaster.getApplicableTo().isBefore(cessMasterDTO.getApplicableFrom())) {
				throw new CustomException("Please update previous record Applicable To in order to create new.",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
			if (retrievedCessMaster != null && retrievedCessMaster.getApplicableTo() == null) {
				retrievedCessMaster
						.setApplicableTo(cessMasterDTO.getApplicableFrom().minusMillis(1000L * 60L * 60L * 24));
				cessMasterRepository.save(retrievedCessMaster);
			}
			cessMaster.setRate(cessMasterDTO.getRate());
			// common logic for yes and no functionality
			saveCessMasterCommonFields(cessMaster, cessMasterDTO);

		} else {
			//getting previous record data if exist and updating applicable to record
			List<CessMaster> cessMasterList = cessMasterRepository.findCessMasterByBasedOnFields(
					cessMasterDTO.getBocNatureOfPayment(), cessMasterDTO.getBocInvoiceSlab(),
					cessMasterDTO.getBocDeducteeStatus(), cessMasterDTO.getBocDeducteeResidentialStatus());
			CessMaster retrievedCessMaster = null;
			Set<Long> newIds = new HashSet<>();
			for (BasisOfCessDetailsDTO boc : cessMasterDTO.getBasisOfCessDetails()) {
				if (boc.getNatureOfPaymentMasterId() != null) {
					newIds.add(boc.getNatureOfPaymentMasterId());
				}
				if (boc.getDeducteeResidentialStatusId() != null) {
					newIds.add(boc.getDeducteeResidentialStatusId());
				}
				if (boc.getDeducteeStatusId() != null) {
					newIds.add(boc.getDeducteeStatusId());
				}
				if (boc.getCessTypeId() != null) {
					newIds.add(boc.getCessTypeId());
				}
				if (boc.getInvoiceSlabFrom() != null) {
					newIds.add(boc.getInvoiceSlabFrom());
				}
			}
			for (CessMaster cessMasterData : cessMasterList) {
				Set<Long> dbIds = new HashSet<>();
				for (BasisOfCessDetails boc : cessMasterData.getBasisOfCessDetails()) {
					if (boc.getNatureOfPaymentMaster() != null) {
						dbIds.add(boc.getNatureOfPaymentMaster().getId());
					}
					if (boc.getDeducteeResidentialStatus() != null) {
						dbIds.add(boc.getDeducteeResidentialStatus().getId());
					}
					if (boc.getDeducteeStatus() != null) {
						dbIds.add(boc.getDeducteeStatus().getId());
					}
					if (boc.getCessMaster().getId() != null) {
						dbIds.add(boc.getCessMaster().getId());
					}
					if (boc.getInvoiceSlabFrom() != null) {
						dbIds.add(boc.getInvoiceSlabFrom());
					}
				}
				List<Long> dbCessIds = new ArrayList<>(dbIds);
				Collections.sort(dbCessIds);
				List<Long> newCessIds = new ArrayList<>(newIds);
				Collections.sort(newCessIds);
				if (dbCessIds.equals(newCessIds)) {
					retrievedCessMaster = cessMaster;
					break;
				}
			}
			if (retrievedCessMaster != null && retrievedCessMaster.getApplicableTo() != null
					&& retrievedCessMaster.getApplicableTo().isBefore(cessMasterDTO.getApplicableFrom())) {
				throw new CustomException("Please update previous record Applicable To in order to create new.",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
			if (retrievedCessMaster != null && retrievedCessMaster.getApplicableTo() == null) {
				retrievedCessMaster
						.setApplicableTo(cessMasterDTO.getApplicableFrom().minusMillis(1000L * 60L * 60L * 24));
				cessMasterRepository.save(retrievedCessMaster);
			}
			// common logic for yes and no functionality
			saveCessMasterCommonFields(cessMaster, cessMasterDTO);

			List<BasisOfCessDetailsDTO> setBasisOfCessDetails = cessMasterDTO.getBasisOfCessDetails();
			List<BasisOfCessDetailsDTO> uniqeBasisOfCessDetails = new ArrayList<BasisOfCessDetailsDTO>();
			Map<String, Integer> map = new HashMap<>();
			for (BasisOfCessDetailsDTO cessDto : setBasisOfCessDetails) {
				String dupliateCheck = cessDto.getDeducteeStatusId() + "-" + cessDto.getNatureOfPaymentMasterId() + "-"
						+ cessDto.getDeducteeResidentialStatusId() + "-" + cessDto.getInvoiceSlabFrom();
				if (map.containsKey(dupliateCheck)) {
					throw new CustomException("Duplicate Records Not Allowed", HttpStatus.BAD_REQUEST);
				} else {
					map.put(dupliateCheck, 1);
					uniqeBasisOfCessDetails.add(cessDto);
				}
			}

			List<BasisOfCessDetails> basisOfCessDetailss = new ArrayList<>();
			for (BasisOfCessDetailsDTO basisOfCess : uniqeBasisOfCessDetails) {
				BasisOfCessDetails basisOfCessDetails = new BasisOfCessDetails();
				Optional<CessTypeMaster> cessTypeMaster = null;
				Optional<NatureOfPaymentMaster> natureOfPaymentMaster = null;

				// Getting data from CessType master table
				if (basisOfCess.getCessTypeId() != null) {
					cessTypeMaster = cessTypeMasterRepository.findById(basisOfCess.getCessTypeId());
					if (cessTypeMaster.isPresent()) {
						cessMaster.setCessTypeMaster(cessTypeMaster.get());
					}
				}
				// Getting data from NatureOfPayment master table
				if (basisOfCess.getNatureOfPaymentMasterId() != null && cessMasterDTO.getBocNatureOfPayment()) {
					natureOfPaymentMaster = natureOfPaymentMasterRepository
							.findById(basisOfCess.getNatureOfPaymentMasterId());
					if (natureOfPaymentMaster.isPresent()) {
						basisOfCessDetails.setNatureOfPaymentMaster(natureOfPaymentMaster.get());
					}
				}
				cessMaster.setRate(basisOfCess.getRate());
				if (cessMasterDTO.getBocInvoiceSlab() != null && cessMasterDTO.getBocInvoiceSlab()) {
					basisOfCessDetails.setInvoiceSlabFrom(basisOfCess.getInvoiceSlabFrom());
					basisOfCessDetails.setInvoiceSlabTo(basisOfCess.getInvoiceSlabTo());
				}

				// setting deductee status
				if (cessMasterDTO.getBocDeducteeStatus() != null && cessMasterDTO.getBocDeducteeStatus()) {
					Optional<Status> statusData = statusRepository.findById(basisOfCess.getDeducteeStatusId());
					if (statusData.isPresent()) {
						basisOfCessDetails.setDeducteeStatus(statusData.get());
					}
				}
				// setting boc deductee residential
				if (cessMasterDTO.getBocDeducteeResidentialStatus() != null
						&& cessMasterDTO.getBocDeducteeResidentialStatus()) {
					Optional<ResidentialStatus> residentialStatusData = residentialStatusRepository
							.findById(basisOfCess.getDeducteeResidentialStatusId());
					if (residentialStatusData.isPresent()) {
						basisOfCessDetails.setDeducteeResidentialStatus(residentialStatusData.get());
					}
				}
				basisOfCessDetails.setRate(basisOfCess.getRate());
				basisOfCessDetails.setCessMaster(cessMaster);
				basisOfCessDetailss.add(basisOfCessDetails);
				cessMaster.setBasisOfCessDetails(basisOfCessDetailss);
			} // for
		}
	}
	/**
	 * 
	 * @param natureOfPaymentId
	 * @return
	 */
	public BasisOfCessDetails getBasisOfCessDetails(Long natureOfPaymentId) {
		BasisOfCessDetails basisOfCessRecord = null;
		List<BasisOfCessDetails> getBasisOfCess = basisOfCessDetailsRepository
				.findByNatureOfPaymentId(natureOfPaymentId);
		if (getBasisOfCess.size() > 0) {
			basisOfCessRecord = getBasisOfCess.get(0);
		} else {
			basisOfCessRecord = new BasisOfCessDetails();
		}
		return basisOfCessRecord;
	}
	
	/**
	 * 
	 * @param section
	 * @param deducteeStatus
	 * @param residentialStatus
	 * @return
	 */
	public List<SurchargeAndCessDTO> getCessDetailsBySectionDeducteeStatus(String section, String deducteeStatus,
			String residentialStatus) {
		List<SurchargeAndCessDTO> cessList = new ArrayList<>();
		SurchargeAndCessDTO surchargeAndCessDTO = new SurchargeAndCessDTO();
		List<SurchargeAndCessRateDTO> cessMasterList = cessMasterRepository
				.findBySectionResidentialStatusDeducteeStatus(section, residentialStatus, deducteeStatus);
		if (cessMasterList != null) {
			logger.info("cess applicable at flat rate for all deductee for all nature of payments is false ");
			for (SurchargeAndCessRateDTO cess : cessMasterList) {
				surchargeAndCessDTO.setApplicableFrom(cess.getApplicableFrom());
				surchargeAndCessDTO.setApplicableTo(cess.getApplicableTo());
				surchargeAndCessDTO.setRate(cess.getRate());
				surchargeAndCessDTO.setInvoiceSlabFrom(cess.getInvoiceSlabFrom());
				surchargeAndCessDTO.setInvoiceSlabTo(cess.getInvoiceSlabTo());
				cessList.add(surchargeAndCessDTO);
			}
		} else {
			Optional<CessMaster> cessMasterResponse = cessMasterRepository.findByIsCessApplicable(true);
			if (cessMasterResponse.isPresent()) {
				CessMaster cessMaster = cessMasterResponse.get();
				logger.info("cess applicable at flat rate for all deductee for all nature of payments is true ");
				surchargeAndCessDTO.setApplicableFrom(Date.from(cessMaster.getApplicableFrom()));
				surchargeAndCessDTO.setApplicableTo(Date.from(cessMaster.getApplicableTo()));
				surchargeAndCessDTO.setRate(cessMaster.getRate());
				surchargeAndCessDTO.setInvoiceSlabFrom(0L);
				surchargeAndCessDTO.setInvoiceSlabTo(0L);
				cessList.add(surchargeAndCessDTO);
			}
		}
		return cessList;
	}
	
	public List<SurchargeAndCessDTO> getCessDetailsByCessType(String cessType) {
		List<SurchargeAndCessDTO> cessList = new ArrayList<>();
		SurchargeAndCessDTO surchargeAndCessDTO = new SurchargeAndCessDTO();
		List<SurchargeAndCessRateDTO> cessMasterList = cessMasterRepository.findByCessType(cessType);
		if (cessMasterList != null) {
			for (SurchargeAndCessRateDTO cess : cessMasterList) {
				surchargeAndCessDTO.setApplicableFrom(cess.getApplicableFrom());
				surchargeAndCessDTO.setApplicableTo(cess.getApplicableTo());
				surchargeAndCessDTO.setRate(cess.getRate());
				surchargeAndCessDTO.setInvoiceSlabFrom(cess.getInvoiceSlabFrom());
				surchargeAndCessDTO.setInvoiceSlabTo(cess.getInvoiceSlabTo());
				cessList.add(surchargeAndCessDTO);
			}
		}
		return cessList;
	}

	/**
	 * 
	 * @param file
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @param userName
	 * @return
	 * @throws IOException 
	 * @throws StorageException 
	 * @throws URISyntaxException 
	 * @throws InvalidKeyException 
	 */
	@Transactional
	public MasterBatchUpload saveFileData(MultipartFile file, Integer assesssmentYear, Integer assessmentMonth,
			String userName) throws InvalidKeyException, URISyntaxException, StorageException, IOException {

		String uploadType = UploadTypes.CESS_MASTER_EXCEL.name();
		String sha256 = sha256SumService.getSHA256Hash(file);
		if (isAlreadyProcessed(sha256)) {
			MasterBatchUpload masterBatchUpload = new MasterBatchUpload();
			masterBatchUpload.setCreatedDate(Instant.now());
			masterBatchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			masterBatchUpload.setCreatedBy(userName);
			masterBatchUpload.setSuccessCount(0L);
			masterBatchUpload.setFailedCount(0L);
			masterBatchUpload.setRowsCount(0L);
			masterBatchUpload.setProcessed(0);
			masterBatchUpload.setMismatchCount(0L);
			masterBatchUpload.setSha256sum(sha256);
			masterBatchUpload.setStatus("Duplicate");
			masterBatchUpload.setNewStatus("Duplicate");
			masterBatchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			masterBatchUpload = masterBatchUploadService.masterBatchUpload(masterBatchUpload, file, assesssmentYear,
					assessmentMonth, userName, null, uploadType);
			return masterBatchUpload;
		}
		try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());) {
			XSSFSheet worksheet = workbook.getSheetAt(0);
			XSSFRow headerRow = worksheet.getRow(0);
			int headersCount = CessMasterExcel.getNonEmptyCellsCount(headerRow);
			logger.info("Column header count :{}", headersCount);
			MasterBatchUpload masterBatchUpload = new MasterBatchUpload();
			if (headersCount != CessMasterExcel.fieldMappings.size()) {
				masterBatchUpload.setCreatedDate(Instant.now());
				masterBatchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
				masterBatchUpload.setSuccessCount(0L);
				masterBatchUpload.setFailedCount(0L);
				masterBatchUpload.setRowsCount(0L);
				masterBatchUpload.setProcessed(0);
				masterBatchUpload.setMismatchCount(0L);
				masterBatchUpload.setSha256sum(sha256);
				masterBatchUpload.setStatus("Failed");
				masterBatchUpload.setCreatedBy(userName);
				masterBatchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
				return masterBatchUploadService.masterBatchUpload(masterBatchUpload, file, assesssmentYear,
						assessmentMonth, userName, null, uploadType);
			} else {
				masterBatchUpload.setCreatedDate(Instant.now());
				masterBatchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
				masterBatchUpload.setStatus("Processing");
				masterBatchUpload.setSuccessCount(0L);
				masterBatchUpload.setFailedCount(0L);
				masterBatchUpload.setRowsCount(0L);
				masterBatchUpload.setProcessed(0);
				masterBatchUpload.setMismatchCount(0L);
				masterBatchUpload = masterBatchUploadService.masterBatchUpload(masterBatchUpload, file, assesssmentYear,
						assessmentMonth, userName, null, uploadType);
			}
			if (headersCount == CessMasterExcel.fieldMappings.size()) {
				return saveCessMasterData(workbook, file, sha256, assesssmentYear, assessmentMonth, userName,
						masterBatchUpload, uploadType);
			} else {
				throw new CustomException("Data not found ", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to process cess master data ", e);
		}
	}
	

	/**
	 * 
	 * @param sha256Sum
	 * @return
	 */
	private boolean isAlreadyProcessed(String sha256Sum) {
		Optional<MasterBatchUpload> sha256Record = masterBatchUploadRepository.getSha256Records(sha256Sum);
		return sha256Record.isPresent();
	}
	
	/**
	 * 
	 * @param workbook
	 * @param file
	 * @param sha256
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @param userName
	 * @param masterBatchUpload
	 * @param uploadType
	 * @return
	 * @throws IOException 
	 * @throws StorageException 
	 * @throws URISyntaxException 
	 * @throws InvalidKeyException 
	 */
	@Async
	private MasterBatchUpload saveCessMasterData(XSSFWorkbook workbook, MultipartFile file, String sha256,
			Integer assesssmentYear, Integer assessmentMonth, String userName, MasterBatchUpload masterBatchUpload,
			String uploadType) throws InvalidKeyException, URISyntaxException, StorageException, IOException {

		File articleErrorFile = null;
		ArrayList<ArticleRateMasterErrorReportDTO> errorList = new ArrayList<>();
		try {
			CessMasterExcel excelData = new CessMasterExcel(workbook);
			masterBatchUpload.setSha256sum(sha256);
			masterBatchUpload.setMismatchCount(0L);
			long dataRowsCount = excelData.getDataRowsCount();
			masterBatchUpload.setRowsCount(dataRowsCount);
			Long errorCount = 0L;
			Long duplicateCount = 0L;
			List<ArticleMaster> articleList = new ArrayList<>();
			for (int rowIndex = 1; rowIndex <= dataRowsCount; rowIndex++) {
				boolean isNotValid = false;
				Optional<ArticleRateMasterErrorReportDTO> errorDTO = null;
				try {
					errorDTO = excelData.validate(rowIndex);
				} catch (Exception e) {
					logger.error("Unable validate row number " + rowIndex + " due to "
							+ ExceptionUtils.getRootCauseMessage(e), e);
					++errorCount;
					continue;
				}
				if (errorDTO.isPresent()) {
					++errorCount;
					errorList.add(errorDTO.get());
				} else {
					try {
						ArticleRateMasterDTO articleMasterDTO = excelData.get(rowIndex);

						
						try {
							//TODO need to changes 
							//articleMasterRepository.save(articleMaster);
						} catch (Exception e) {
							logger.error("Exception occurred while inserting data:", e);
						}

					} catch (Exception e) {
						logger.error("Unable to process row number " + rowIndex + " due to " + e.getMessage(), e);
						ArticleRateMasterErrorReportDTO problematicDataError = excelData.getErrorDTO(rowIndex);
						if (StringUtils.isBlank(problematicDataError.getReason())) {
							problematicDataError.setReason(
									"Unable to process row number " + rowIndex + " due to : " + e.getMessage());
						}
						errorList.add(problematicDataError);
						++errorCount;
					}
				}
			} // for 1
				// int duplicateRecordsCount = processCollecteeMasterRecords(articleList,
				// tcsBatchUpload, userName);
				// increment duplicate count if same deductee in database exists with same
				// sections and pan then
				// marking as duplicate.
				// duplicateCount += duplicateRecordsCount;
			int processedRecordsCount = articleList.size();
			masterBatchUpload.setSuccessCount(dataRowsCount);
			masterBatchUpload.setFailedCount(errorCount);
			masterBatchUpload.setProcessed(processedRecordsCount);
			masterBatchUpload.setDuplicateCount(0L);
			masterBatchUpload.setStatus("Processed");
			masterBatchUpload.setCreatedDate(Instant.now());
			masterBatchUpload.setProcessEndTime(new Date());
			masterBatchUpload.setCreatedBy(userName);
			/*
			 * if (!errorList.isEmpty()) { deducteeErrorFile =
			 * prepareResidentDeducteesErrorFile(uploadedFile.getOriginalFilename(),
			 * errorList, new ArrayList<>(excelData.getHeaders())); }
			 */
		} catch (Exception e) {
			masterBatchUpload.setStatus("Process failed");
			logger.error("Exception occurred :", e);

		}
		return masterBatchUploadService.masterBatchUpload(masterBatchUpload, file, assesssmentYear, assessmentMonth,
				userName, articleErrorFile, uploadType);
	}

}
