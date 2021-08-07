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
import com.ey.in.tds.common.domain.BasisOfSurchargeDetails;
import com.ey.in.tds.common.domain.MasterBatchUpload;
import com.ey.in.tds.common.domain.NatureOfPaymentMaster;
import com.ey.in.tds.common.domain.ResidentialStatus;
import com.ey.in.tds.common.domain.Status;
import com.ey.in.tds.common.domain.SurchargeMaster;
import com.ey.in.tds.common.domain.dividend.ShareholderCategory;
import com.ey.in.tds.common.domain.dividend.ShareholderType;
import com.ey.in.tds.common.dto.SurchargeAndCessDTO;
import com.ey.in.tds.common.repository.NatureOfPaymentMasterRepository;
import com.ey.in.tds.common.service.Sha256SumService;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.dto.ArticleRateMasterDTO;
import com.ey.in.tds.dto.ArticleRateMasterErrorReportDTO;
import com.ey.in.tds.dto.BasisOfSurchargeDetailsDTO;
import com.ey.in.tds.dto.SurchargeAndCessRateDTO;
import com.ey.in.tds.dto.SurchargeMasterDTO;
import com.ey.in.tds.repository.BasisOfSurchargeDetailsRepository;
import com.ey.in.tds.repository.DividendStaticDataRepository;
import com.ey.in.tds.repository.MasterBatchUploadRepository;
import com.ey.in.tds.repository.ResidentialStatusRepository;
import com.ey.in.tds.repository.StatusRepository;
import com.ey.in.tds.repository.SurchargeMasterRepository;
import com.ey.in.tds.service.util.excel.SurchargeMasterExcel;
import com.microsoft.azure.storage.StorageException;

@Service
public class SurchargeMasterService {

	private final Logger logger = LoggerFactory.getLogger(SurchargeMasterService.class);

	@Autowired
	private Sha256SumService sha256SumService;

	@Autowired
	private MasterBatchUploadService masterBatchUploadService;

	@Autowired
	private MasterBatchUploadRepository masterBatchUploadRepository;

	@Autowired
	private ResidentialStatusRepository residentialStatusRepository;

	@Autowired
	private StatusRepository statusRepository;
	
	@Autowired
	private DividendStaticDataRepository dividendStaticDataRepository;
	

	private final SurchargeMasterRepository surchargeMasterRepository;

	private final NatureOfPaymentMasterRepository natureOfPaymentMasterRepository;

	private final BasisOfSurchargeDetailsRepository basisOfSurchargeDetailsRepository;

	public SurchargeMasterService(SurchargeMasterRepository surchargeMasterRepository,
			NatureOfPaymentMasterRepository natureOfPaymentMasterRepository,
			BasisOfSurchargeDetailsRepository basisOfSurchargeDetailsRepository) {
		this.surchargeMasterRepository = surchargeMasterRepository;
		this.natureOfPaymentMasterRepository = natureOfPaymentMasterRepository;
		this.basisOfSurchargeDetailsRepository = basisOfSurchargeDetailsRepository;
	}

	/**
	 * This method is to create Surcharge Master
	 * 
	 * @param surchargeMasterDTO
	 * @return
	 */
	/**
	 * @param surchargeMasterDTO
	 * @param userName
	 * @return
	 */
	public SurchargeMasterDTO createSurchargeMaster(SurchargeMasterDTO surchargeMasterDTO, String userName) {

		logger.info("REST request to save SurchargeMaster : {}", surchargeMasterDTO);

		SurchargeMaster surchargeMaster = new SurchargeMaster();

		surchargeMaster.setApplicableFrom(surchargeMasterDTO.getApplicableFrom());
		surchargeMaster.setApplicableTo(surchargeMasterDTO.getApplicableTo());
		surchargeMaster.setRate(surchargeMasterDTO.getSurchargeRate());
		surchargeMaster.setBosDeducteeStatus(surchargeMasterDTO.isBocDeducteeStatus());
		surchargeMaster.setBosInvoiceSlab(surchargeMasterDTO.isBocInvoiceSlab());
		surchargeMaster.setBosNatureOfPayment(surchargeMasterDTO.isBocNatureOfPayment());
		surchargeMaster.setBosResidentialStatus(surchargeMasterDTO.isBocDeducteeResidentialStatus());
		surchargeMaster.setIsSurchargeApplicable(surchargeMasterDTO.isSurchargeApplicable());
		surchargeMaster.setBosShareholderCatagory(surchargeMasterDTO.isBocShareholderCatagory());
		surchargeMaster.setBosShareholderType(surchargeMasterDTO.isBocShareholderType());
		surchargeMaster.setCreatedBy(userName);
		surchargeMaster.setCreatedDate(Instant.now());
		surchargeMaster.setModifiedBy(userName);
		surchargeMaster.setModifiedDate(Instant.now());
		surchargeMaster.setActive(true);

		if (!surchargeMasterDTO.isSurchargeApplicable()) {
			List<SurchargeMaster> dbSurchargeList = surchargeMasterRepository.findByBasedOnFields(
					surchargeMasterDTO.isBocNatureOfPayment(), surchargeMasterDTO.isBocInvoiceSlab(),
					surchargeMasterDTO.isBocDeducteeStatus(), surchargeMasterDTO.isBocDeducteeResidentialStatus());
			SurchargeMaster retrievedSurchargeMaster = null;
			Set<Long> newIds = new HashSet<>();
			for (BasisOfSurchargeDetailsDTO bos : surchargeMasterDTO.getBasisOfSurchargeDetails()) {
				if (bos.getNatureOfPaymentMasterId() != null) {
					newIds.add(bos.getNatureOfPaymentMasterId());
				}
				if (bos.getDeducteeResidentialStatusId() != null) {
					newIds.add(bos.getDeducteeResidentialStatusId());
				}
				if (bos.getDeducteeStatusId() != null) {
					newIds.add(bos.getDeducteeStatusId());
				}
				if (bos.getInvoiceSlabFrom() != null) {
					newIds.add(bos.getInvoiceSlabFrom());
				}
				if (bos.getShareholderCatagoryId() != null) {
					newIds.add(bos.getShareholderCatagoryId());
				}
				if (bos.getShareholderTypeId() != null) {
					newIds.add(bos.getShareholderTypeId());
				}
			}
			for (SurchargeMaster surcharge : dbSurchargeList) {
				Set<Long> dbIds = new HashSet<>();
				for (BasisOfSurchargeDetails bos : surcharge.getBasisOfSurchargeDetails()) {
					if (bos.getNatureOfPaymentMaster() != null) {
						dbIds.add(bos.getNatureOfPaymentMaster().getId());
					}
					if (bos.getDeducteeResidentialStatus() != null) {
						dbIds.add(bos.getDeducteeResidentialStatus().getId());
					}
					if (bos.getDeducteeStatus() != null) {
						dbIds.add(bos.getDeducteeStatus().getId());
					}
					if (bos.getInvoiceSlabFrom() != null) {
						dbIds.add(bos.getInvoiceSlabFrom());
					}
					if (bos.getShareholderCategory() != null) {
						newIds.add(bos.getShareholderCategory().getId());
					}
					if (bos.getShareholderType() != null) {
						newIds.add(bos.getShareholderType().getId());
					}
				}
				List<Long> dbSurchargeIds = new ArrayList<>(dbIds);
				Collections.sort(dbSurchargeIds);
				List<Long> newSurchargeIds = new ArrayList<>(newIds);
				Collections.sort(newSurchargeIds);
				if (dbSurchargeIds.equals(newSurchargeIds)) {
					retrievedSurchargeMaster = surcharge;
					break;
				}
			}
			if (retrievedSurchargeMaster != null && retrievedSurchargeMaster.getApplicableTo() != null
					&& !retrievedSurchargeMaster.getApplicableTo().isBefore(surchargeMasterDTO.getApplicableFrom())) {
				throw new CustomException("Please update previous record Applicable To in order to create new.",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
			// checking and updating the date
			if (retrievedSurchargeMaster != null && retrievedSurchargeMaster.getApplicableTo() == null) {
				retrievedSurchargeMaster
						.setApplicableTo(surchargeMasterDTO.getApplicableFrom().minusMillis(1000L * 60L * 60L * 24));
				surchargeMasterRepository.save(retrievedSurchargeMaster);
			}

			List<BasisOfSurchargeDetailsDTO> setBasisOfSurchargeDetails = surchargeMasterDTO
					.getBasisOfSurchargeDetails();

			// filtering unique records
			List<BasisOfSurchargeDetailsDTO> uniqueBasisOfSurchargeDetails = new ArrayList<BasisOfSurchargeDetailsDTO>();
			Map<String, Integer> map = new HashMap<>();
			for (BasisOfSurchargeDetailsDTO surchargeDto : setBasisOfSurchargeDetails) {
				String dupliateCheck = surchargeDto.getDeducteeStatusId() + "-"
						+ surchargeDto.getNatureOfPaymentMasterId() + "-"
						+ surchargeDto.getDeducteeResidentialStatusId() + "-" + surchargeDto.getInvoiceSlabFrom()+"-"
						+surchargeDto.getShareholderCatagoryId()+"-"+surchargeDto.getShareholderTypeId();
				if (map.containsKey(dupliateCheck)) {
					throw new CustomException("Duplicate Records Not Allowed", HttpStatus.BAD_REQUEST);
				} else {
					map.put(dupliateCheck, 1);
					uniqueBasisOfSurchargeDetails.add(surchargeDto);
				}
			}

			List<BasisOfSurchargeDetails> basisOfSurchargeDetailss = new ArrayList<>();
			for (BasisOfSurchargeDetailsDTO basisOfSurcharge : uniqueBasisOfSurchargeDetails) {
				BasisOfSurchargeDetails basisOfSurchargeDetails = new BasisOfSurchargeDetails();

				if (basisOfSurcharge.getNatureOfPaymentMasterId() != null) {
					Optional<NatureOfPaymentMaster> natureOfPaymentMaster = natureOfPaymentMasterRepository
							.findById(basisOfSurcharge.getNatureOfPaymentMasterId());

					surchargeMaster.setRate(basisOfSurcharge.getRate());
					if (surchargeMasterDTO.isBocNatureOfPayment() && natureOfPaymentMaster.isPresent()) {
						basisOfSurchargeDetails.setNatureOfPaymentMaster(natureOfPaymentMaster.get());
					}
				}

				if (surchargeMasterDTO.isBocInvoiceSlab()) {
					basisOfSurchargeDetails.setInvoiceSlabFrom(basisOfSurcharge.getInvoiceSlabFrom());
					basisOfSurchargeDetails.setInvoiceSlabTo(basisOfSurcharge.getInvoiceSlabTo());
				}

				if (surchargeMasterDTO.isBocDeducteeStatus()) {
					Optional<Status> statusData = statusRepository.findById(basisOfSurcharge.getDeducteeStatusId());
					if (statusData.isPresent()) {
						basisOfSurchargeDetails.setDeducteeStatus(statusData.get());
					}
				}

				if (surchargeMasterDTO.isBocDeducteeResidentialStatus()) {
					Optional<ResidentialStatus> residentialStatusData = residentialStatusRepository
							.findById(basisOfSurcharge.getDeducteeResidentialStatusId());
					if (residentialStatusData.isPresent()) {
						basisOfSurchargeDetails.setDeducteeResidentialStatus(residentialStatusData.get());
					}
				}
				
				if (surchargeMasterDTO.isBocShareholderCatagory()) {
					Optional<ShareholderCategory> shareholderCategoryData = dividendStaticDataRepository
							.findShareholderCategoryById(basisOfSurcharge.getShareholderCatagoryId());
					if (shareholderCategoryData.isPresent()) {
						basisOfSurchargeDetails.setShareholderCategory(shareholderCategoryData.get());
					}
				}
				
				if (surchargeMasterDTO.isBocShareholderType()) {
					Optional<ShareholderType> shareholderTypeData = dividendStaticDataRepository
							.findShareholderTypeById(basisOfSurcharge.getShareholderTypeId());
					if (shareholderTypeData.isPresent()) {
						basisOfSurchargeDetails.setShareholderType(shareholderTypeData.get());
					}
				}

				basisOfSurchargeDetails.setRate(basisOfSurcharge.getRate());
				basisOfSurchargeDetails.setSurchargeMaster(surchargeMaster);
				basisOfSurchargeDetails.setActive(true);
				basisOfSurchargeDetailss.add(basisOfSurchargeDetails);
			} // for
			surchargeMaster.setBasisOfSurchargeDetails(basisOfSurchargeDetailss);
		} else {
			// if isApplicable is true
			Optional<SurchargeMaster> surchargeMasterResponse = surchargeMasterRepository
					.findByIsSurchargeApplicable(true);
			if (surchargeMasterResponse.isPresent()) {
				SurchargeMaster retrievedsurchargeMaster = surchargeMasterResponse.get();
				if (retrievedsurchargeMaster != null && surchargeMasterDTO.getApplicableFrom()
						.equals(retrievedsurchargeMaster.getApplicableFrom())) {
					throw new CustomException("A record is already there with same Applicable From Date",
							HttpStatus.INTERNAL_SERVER_ERROR);
				}
				if (retrievedsurchargeMaster != null && retrievedsurchargeMaster.getApplicableTo() != null
						&& !retrievedsurchargeMaster.getApplicableTo()
								.isBefore(surchargeMasterDTO.getApplicableFrom())) {
					throw new CustomException("Please update previous record Applicable To in order to create new.",
							HttpStatus.INTERNAL_SERVER_ERROR);
				}
				if (retrievedsurchargeMaster != null && retrievedsurchargeMaster.getApplicableTo() == null) {
					retrievedsurchargeMaster.setApplicableTo(
							surchargeMasterDTO.getApplicableFrom().minusMillis(1000L * 60L * 60L * 24));
					surchargeMasterRepository.save(retrievedsurchargeMaster);
				}
			}
		}

		try {
			surchargeMaster = surchargeMasterRepository.save(surchargeMaster);
		} catch (Exception ex) {
			logger.error("Error occured while saving record", ex);
		}
		return surchargeMasterDTO;

	}

	/**
	 * Get all the surchargeMasters.
	 *
	 * @param pageable the pagination information
	 * @return the list of entities
	 */
	@Transactional(readOnly = true)
	public List<SurchargeMasterDTO> findAll() {
		List<SurchargeMasterDTO> surchargeMasterListDTO = null;
		SurchargeMasterDTO surchargeMasterDTO = null;
		List<BasisOfSurchargeDetails> basisOfSurcharge = null;

		List<BasisOfSurchargeDetailsDTO> basisOfSurchargeDetailsListDTO = null;

		BasisOfSurchargeDetailsDTO basisOfSurchargeDetailsDTO = null;

		List<SurchargeMaster> surchargeMasterList = surchargeMasterRepository.findAll();

		surchargeMasterListDTO = new ArrayList<>();
		for (SurchargeMaster surchargeMaster : surchargeMasterList) {
			surchargeMasterDTO = new SurchargeMasterDTO();
			surchargeMasterDTO.setId(surchargeMaster.getId());
			surchargeMasterDTO.setApplicableFrom(surchargeMaster.getApplicableFrom());
			surchargeMasterDTO.setApplicableTo(surchargeMaster.getApplicableTo());
			surchargeMasterDTO.setBocDeducteeResidentialStatus(surchargeMaster.getBosResidentialStatus());
			surchargeMasterDTO.setBocDeducteeStatus(surchargeMaster.getBosDeducteeStatus());
			surchargeMasterDTO.setBocInvoiceSlab(surchargeMaster.getBosInvoiceSlab());
			surchargeMasterDTO.setBocNatureOfPayment(surchargeMaster.getBosNatureOfPayment());
			surchargeMasterDTO.setSurchargeApplicable(surchargeMaster.getIsSurchargeApplicable());
			surchargeMasterDTO.setSurchargeRate(surchargeMaster.getRate());

			basisOfSurcharge = basisOfSurchargeDetailsRepository.findBySurchargeMasterId(surchargeMaster.getId());

			if (!basisOfSurcharge.isEmpty()) {

				basisOfSurchargeDetailsListDTO = new ArrayList<>();

				for (BasisOfSurchargeDetails basisOfSurchargeIterate : basisOfSurcharge) {
					basisOfSurchargeDetailsDTO = new BasisOfSurchargeDetailsDTO();
					basisOfSurchargeDetailsDTO.setId(basisOfSurchargeIterate.getId());

					if (basisOfSurchargeIterate.getDeducteeResidentialStatus() != null) {

						basisOfSurchargeDetailsDTO.setDeducteeResidentialStatusId(
								basisOfSurchargeIterate.getDeducteeResidentialStatus().getId());
						basisOfSurchargeDetailsDTO
								.setResidentStatus(basisOfSurchargeIterate.getDeducteeResidentialStatus().getStatus());
					}

					if (basisOfSurchargeIterate.getDeducteeStatus() != null) {

						basisOfSurchargeDetailsDTO
								.setDeducteeStatusId(basisOfSurchargeIterate.getDeducteeStatus().getId());
						basisOfSurchargeDetailsDTO.setStatus(basisOfSurchargeIterate.getDeducteeStatus().getStatus());
					}

					if (basisOfSurchargeIterate.getNatureOfPaymentMaster() != null) {
						basisOfSurchargeDetailsDTO
								.setNatureOfPaymentMasterId(basisOfSurchargeIterate.getNatureOfPaymentMaster().getId());
						basisOfSurchargeDetailsDTO
								.setNature(basisOfSurchargeIterate.getNatureOfPaymentMaster().getNature());
					}

					basisOfSurchargeDetailsDTO.setInvoiceSlabFrom(basisOfSurchargeIterate.getInvoiceSlabFrom());
					basisOfSurchargeDetailsDTO.setInvoiceSlabTo(basisOfSurchargeIterate.getInvoiceSlabTo());
					basisOfSurchargeDetailsDTO.setRate(basisOfSurchargeIterate.getRate());
					basisOfSurchargeDetailsListDTO.add(basisOfSurchargeDetailsDTO);
				}
				surchargeMasterDTO.setBasisOfSurchargeDetails(basisOfSurchargeDetailsListDTO);
			} else {
				basisOfSurchargeDetailsListDTO = new ArrayList<>();
				basisOfSurchargeDetailsListDTO.add(new BasisOfSurchargeDetailsDTO());
				surchargeMasterDTO.setBasisOfSurchargeDetails(basisOfSurchargeDetailsListDTO);
			}
			surchargeMasterListDTO.add(surchargeMasterDTO);

		}
		return surchargeMasterListDTO;

	}

	/**
	 * This method is to update Surcharge Master
	 * 
	 * @param surchargeMasterDTO
	 * @param userName
	 * @return
	 */

	public Long updateSurchargeMaster(SurchargeMasterDTO surchargeMasterDTO, String userName) {

		logger.info("REST request to save SurchargeMaster : {}", surchargeMasterDTO);
		SurchargeMaster surchargeMaster = null;
		Optional<SurchargeMaster> surchargeMasterData = surchargeMasterRepository.findById(surchargeMasterDTO.getId());

		if (surchargeMasterData.isPresent()) {
			surchargeMaster = surchargeMasterData.get();
			surchargeMaster.setApplicableFrom(surchargeMasterDTO.getApplicableFrom());
			surchargeMaster.setApplicableTo(surchargeMasterDTO.getApplicableTo());
			surchargeMaster.setBosInvoiceSlab(surchargeMasterDTO.isBocInvoiceSlab());
			surchargeMaster.setBosNatureOfPayment(surchargeMasterDTO.isBocNatureOfPayment());
			surchargeMaster.setBosResidentialStatus(surchargeMasterDTO.isBocDeducteeResidentialStatus());
			surchargeMaster.setBosDeducteeStatus(surchargeMasterDTO.isBocDeducteeStatus());
			surchargeMaster.setIsSurchargeApplicable(surchargeMasterDTO.isSurchargeApplicable());
			surchargeMaster.setRate(surchargeMasterDTO.getSurchargeRate());
			surchargeMaster.setModifiedBy(userName);
			surchargeMaster.setModifiedDate(Instant.now());

			if (!surchargeMasterDTO.isSurchargeApplicable()) {

				List<BasisOfSurchargeDetailsDTO> setBasisOfSurchargeDetails = surchargeMasterDTO
						.getBasisOfSurchargeDetails();
				List<BasisOfSurchargeDetails> basisOfSurchargeDetailss = new ArrayList<>();
				for (BasisOfSurchargeDetailsDTO basisOfSurcharge : setBasisOfSurchargeDetails) {

					BasisOfSurchargeDetails basisOfSurchargeDetails = new BasisOfSurchargeDetails();
					basisOfSurchargeDetails.setId(basisOfSurcharge.getId());
					if (basisOfSurcharge.getNatureOfPaymentMasterId() != null) {
						Optional<NatureOfPaymentMaster> natureOfPaymentMaster = natureOfPaymentMasterRepository
								.findById(basisOfSurcharge.getNatureOfPaymentMasterId());
						if (surchargeMasterDTO.isBocNatureOfPayment() && natureOfPaymentMaster.isPresent()) {
							basisOfSurchargeDetails.setNatureOfPaymentMaster(natureOfPaymentMaster.get());
						}
					}

					if (surchargeMasterDTO.isBocInvoiceSlab()) {
						basisOfSurchargeDetails.setInvoiceSlabFrom(basisOfSurcharge.getInvoiceSlabFrom());
						basisOfSurchargeDetails.setInvoiceSlabTo(basisOfSurcharge.getInvoiceSlabTo());
					}

					if (surchargeMasterDTO.isBocDeducteeStatus()) {
						Optional<Status> statusData = statusRepository.findById(basisOfSurcharge.getDeducteeStatusId());
						if (statusData.isPresent()) {
							basisOfSurchargeDetails.setDeducteeStatus(statusData.get());
						}
					}
					if (surchargeMasterDTO.isBocDeducteeResidentialStatus()) {
						Optional<ResidentialStatus> residentialStatusData = residentialStatusRepository
								.findById(basisOfSurcharge.getDeducteeResidentialStatusId());

						if (residentialStatusData.isPresent()) {
							basisOfSurchargeDetails.setDeducteeResidentialStatus(residentialStatusData.get());
						}
					}

					basisOfSurchargeDetails.setRate(basisOfSurcharge.getRate());
					basisOfSurchargeDetails.setSurchargeMaster(surchargeMaster);
					basisOfSurchargeDetailss.add(basisOfSurchargeDetails);
				} // for
				surchargeMaster.setBasisOfSurchargeDetails(basisOfSurchargeDetailss);
			}
		} else {
//			throw new EntityNotFoundException("Surcharge Master Entity not found");
			throw new CustomException("Surcharge Master Entity not found", HttpStatus.INTERNAL_SERVER_ERROR);

		}
		surchargeMaster = surchargeMasterRepository.save(surchargeMaster);
		return surchargeMaster.getId();
	}

	/**
	 * Get one surchargeMaster by id.
	 *
	 * @param id the id of the entity
	 * @return the entity
	 * @throws RecordNotFoundException
	 */
	@Transactional(readOnly = true)
	public SurchargeMasterDTO findOne(Long id) throws RecordNotFoundException {
		logger.info("REST request id to get SurchargeMaster : {}", id);
		Optional<SurchargeMaster> optionalSurchargeMaster = surchargeMasterRepository.findById(id);
		SurchargeMaster surchargeMaster = null;

		SurchargeMasterDTO surchargeMasterDTO = new SurchargeMasterDTO();

		if (optionalSurchargeMaster.isPresent()) {
			surchargeMaster = optionalSurchargeMaster.get();

			surchargeMasterDTO.setId(surchargeMaster.getId());
			surchargeMasterDTO.setApplicableFrom(surchargeMaster.getApplicableFrom());
			surchargeMasterDTO.setApplicableTo(surchargeMaster.getApplicableTo());
			surchargeMasterDTO.setSurchargeApplicable(surchargeMaster.getIsSurchargeApplicable());
			surchargeMasterDTO.setSurchargeRate(surchargeMaster.getRate());
			surchargeMasterDTO.setBocDeducteeResidentialStatus(surchargeMaster.getBosResidentialStatus());
			surchargeMasterDTO.setBocDeducteeStatus(surchargeMaster.getBosDeducteeStatus());
			surchargeMasterDTO.setBocInvoiceSlab(surchargeMaster.getBosInvoiceSlab());
			surchargeMasterDTO.setBocNatureOfPayment(surchargeMaster.getBosNatureOfPayment());
			surchargeMasterDTO.setBocShareholderCatagory(surchargeMaster.getBosShareholderCatagory()==null?false:true);
			surchargeMasterDTO.setBocShareholderType(surchargeMaster.getBosShareholderType()==null?false:true);

			List<BasisOfSurchargeDetails> basisOfSurcharge = basisOfSurchargeDetailsRepository
					.findBySurchargeMasterId(surchargeMaster.getId());
			List<BasisOfSurchargeDetailsDTO> basisOfSurchargeDetailsListDTO = null;
			BasisOfSurchargeDetailsDTO basisOfSurchargeDetailsDTO = null;
			if (!basisOfSurcharge.isEmpty()) {

				basisOfSurchargeDetailsListDTO = new ArrayList<>();
				for (BasisOfSurchargeDetails basisOfSurchargeIterate : basisOfSurcharge) {
					basisOfSurchargeDetailsDTO = new BasisOfSurchargeDetailsDTO();
					basisOfSurchargeDetailsDTO.setId(basisOfSurchargeIterate.getId());

					if (basisOfSurchargeIterate.getDeducteeResidentialStatus() != null) {

						basisOfSurchargeDetailsDTO.setDeducteeResidentialStatusId(
								basisOfSurchargeIterate.getDeducteeResidentialStatus().getId());
						basisOfSurchargeDetailsDTO
								.setResidentStatus(basisOfSurchargeIterate.getDeducteeResidentialStatus().getStatus());
					}

					if (basisOfSurchargeIterate.getDeducteeStatus() != null) {

						basisOfSurchargeDetailsDTO
								.setDeducteeStatusId(basisOfSurchargeIterate.getDeducteeStatus().getId());
						basisOfSurchargeDetailsDTO.setStatus(basisOfSurchargeIterate.getDeducteeStatus().getStatus());
					}

					if (basisOfSurchargeIterate.getNatureOfPaymentMaster() != null) {
						basisOfSurchargeDetailsDTO
								.setNatureOfPaymentMasterId(basisOfSurchargeIterate.getNatureOfPaymentMaster().getId());
						basisOfSurchargeDetailsDTO
								.setNature(basisOfSurchargeIterate.getNatureOfPaymentMaster().getNature());
					}
					if (basisOfSurchargeIterate.getShareholderCategory() != null) {
						basisOfSurchargeDetailsDTO
								.setShareholderCatagoryId(basisOfSurchargeIterate.getShareholderCategory().getId());
					}
					
					if (basisOfSurchargeIterate.getShareholderType() != null) {
						basisOfSurchargeDetailsDTO
								.setShareholderTypeId(basisOfSurchargeIterate.getShareholderType().getId());
					}

					basisOfSurchargeDetailsDTO.setInvoiceSlabFrom(basisOfSurchargeIterate.getInvoiceSlabFrom());
					basisOfSurchargeDetailsDTO.setInvoiceSlabTo(basisOfSurchargeIterate.getInvoiceSlabTo());
					basisOfSurchargeDetailsDTO.setRate(basisOfSurchargeIterate.getRate());
					basisOfSurchargeDetailsListDTO.add(basisOfSurchargeDetailsDTO);
				}

				surchargeMasterDTO.setBasisOfSurchargeDetails(basisOfSurchargeDetailsListDTO);

			} else {
				surchargeMasterDTO.setBasisOfSurchargeDetails(new ArrayList<BasisOfSurchargeDetailsDTO>());
			}
		}
		return surchargeMasterDTO;
	}

	// Feign Client - Get Nature Of payment Rate for Basis Of Surcharge Details
	public BasisOfSurchargeDetails getBasisOfSurchargeRecordBasedOnNatureOfPaymentId(Long id) {
		BasisOfSurchargeDetails basisOfSurchargeDetails = null;
		List<BasisOfSurchargeDetails> basisOfSurcharge = basisOfSurchargeDetailsRepository
				.findListOfSurchargeRateByNatureOfPaymentId(id);
		if (basisOfSurcharge.size() > 0) {
			basisOfSurchargeDetails = basisOfSurcharge.get(0);
		} else {
			basisOfSurchargeDetails = new BasisOfSurchargeDetails();
		}
		return basisOfSurchargeDetails;
	}

	/**
	 * 
	 * @param section
	 * @param deducteeStatus
	 * @param residentialStatus
	 * @return
	 */
	public List<SurchargeAndCessDTO> getSurchargeDetailsBySectionDeducteeStatus(String section, String deducteeStatus,
			String residentialStatus) {
		List<SurchargeAndCessDTO> surchargeList = new ArrayList<>();
		List<SurchargeAndCessRateDTO> surchargeMasterList = surchargeMasterRepository
				.findBySectionResidentialStatusDeducteeStatus(section, residentialStatus, deducteeStatus);
		if (surchargeMasterList.isEmpty()) {
			surchargeMasterList = surchargeMasterRepository.findByResidentialStatusDeducteeStatus(residentialStatus,
					deducteeStatus);
		}
		if (surchargeMasterList != null) {
			logger.info("surcharge applicable at flat rate for all deductee for all nature of payments is false ");
			for (SurchargeAndCessRateDTO surcharge : surchargeMasterList) {
				SurchargeAndCessDTO surchargeAndCessDTO = new SurchargeAndCessDTO();
				surchargeAndCessDTO.setApplicableFrom(surcharge.getApplicableFrom());
				surchargeAndCessDTO.setApplicableTo(surcharge.getApplicableTo());
				surchargeAndCessDTO.setRate(surcharge.getRate());
				surchargeAndCessDTO.setInvoiceSlabFrom(surcharge.getInvoiceSlabFrom());
				surchargeAndCessDTO.setInvoiceSlabTo(surcharge.getInvoiceSlabTo());
				surchargeList.add(surchargeAndCessDTO);
			}
		} else {
			Optional<SurchargeMaster> surchargeMasterResponse = surchargeMasterRepository
					.findByIsSurchargeApplicable(true);
			if (surchargeMasterResponse.isPresent()) {
				SurchargeMaster surchargeMaster = surchargeMasterResponse.get();
				logger.info("surcharge applicable at flat rate for all deductee for all nature of payments is true ");
				SurchargeAndCessDTO surchargeAndCessDTO = new SurchargeAndCessDTO();
				surchargeAndCessDTO.setApplicableFrom(Date.from(surchargeMaster.getApplicableFrom()));
				surchargeAndCessDTO.setApplicableTo(Date.from(surchargeMaster.getApplicableTo()));
				surchargeAndCessDTO.setRate(surchargeMaster.getRate());
				surchargeAndCessDTO.setInvoiceSlabFrom(0L);
				surchargeAndCessDTO.setInvoiceSlabTo(0L);
				surchargeList.add(surchargeAndCessDTO);
			}
		}
		return surchargeList;
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

		String uploadType = UploadTypes.SURCHARGE_MASTER_EXCEL.name();
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
			int headersCount = SurchargeMasterExcel.getNonEmptyCellsCount(headerRow);
			logger.info("Column header count :{}", headersCount);
			MasterBatchUpload masterBatchUpload = new MasterBatchUpload();
			if (headersCount != SurchargeMasterExcel.fieldMappings.size()) {
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
			if (headersCount == SurchargeMasterExcel.fieldMappings.size()) {
				return saveSurchargeMasterData(workbook, file, sha256, assesssmentYear, assessmentMonth, userName,
						masterBatchUpload, uploadType);
			} else {
				throw new CustomException("Data not found ", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to process surcharge master data ", e);
		}
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
	private MasterBatchUpload saveSurchargeMasterData(XSSFWorkbook workbook, MultipartFile file, String sha256,
			Integer assesssmentYear, Integer assessmentMonth, String userName, MasterBatchUpload masterBatchUpload,
			String uploadType) throws InvalidKeyException, URISyntaxException, StorageException, IOException {

		File articleErrorFile = null;
		ArrayList<ArticleRateMasterErrorReportDTO> errorList = new ArrayList<>();
		try {
			SurchargeMasterExcel excelData = new SurchargeMasterExcel(workbook);
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
							// TODO need to changes
							// articleMasterRepository.save(articleMaster);
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

	/**
	 * 
	 * @param sha256Sum
	 * @return
	 */
	private boolean isAlreadyProcessed(String sha256Sum) {
		Optional<MasterBatchUpload> sha256Record = masterBatchUploadRepository.getSha256Records(sha256Sum);
		return sha256Record.isPresent();
	}

	public List<Map<String, Object>> getAllSurchargeDetails(String residentialStatus) {
		return surchargeMasterRepository.getAllSurchargeDetails(residentialStatus);
	}

}
