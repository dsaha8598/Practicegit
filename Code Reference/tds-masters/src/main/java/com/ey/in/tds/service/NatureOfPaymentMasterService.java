package com.ey.in.tds.service;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.validation.Valid;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.time.DateUtils;
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

import com.aspose.cells.AutoFilter;
import com.aspose.cells.BackgroundType;
import com.aspose.cells.Cell;
import com.aspose.cells.CellsHelper;
import com.aspose.cells.Color;
import com.aspose.cells.ImportTableOptions;
import com.aspose.cells.Range;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Style;
import com.aspose.cells.TextAlignmentType;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.ey.in.tds.common.domain.BasisOfCessDetails;
import com.ey.in.tds.common.domain.BasisOfSurchargeDetails;
import com.ey.in.tds.common.domain.MasterBatchUpload;
import com.ey.in.tds.common.domain.NatureOfPaymentMaster;
import com.ey.in.tds.common.domain.ResidentialStatus;
import com.ey.in.tds.common.domain.Status;
import com.ey.in.tds.common.domain.SubNaturePaymentMaster;
import com.ey.in.tds.common.domain.TdsMaster;
import com.ey.in.tds.common.dto.SectionNatureDTO;
import com.ey.in.tds.common.repository.NatureOfPaymentMasterRepository;
import com.ey.in.tds.common.service.Sha256SumService;
import com.ey.in.tds.core.exceptions.BadRequestException;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.dto.NOPExcelErrorDTO;
import com.ey.in.tds.dto.NatureOfPaymentMasterDTO;
import com.ey.in.tds.dto.NatureOfPaymentMasterExcelDTO;
import com.ey.in.tds.dto.NopCessRateSurageRateDTO;
import com.ey.in.tds.dto.SubNaturePaymentMasterDTO;
import com.ey.in.tds.repository.BasisOfCessDetailsRepository;
import com.ey.in.tds.repository.BasisOfSurchargeDetailsRepository;
import com.ey.in.tds.repository.MasterBatchUploadRepository;
import com.ey.in.tds.repository.ResidentialStatusRepository;
import com.ey.in.tds.repository.StatusRepository;
import com.ey.in.tds.repository.SubNaturePaymentMasterRepository;
import com.ey.in.tds.repository.TdsMasterRepository;
import com.ey.in.tds.service.util.excel.MasterExcel;
import com.ey.in.tds.service.util.excel.NatureOfPaymentExcel;
import com.ey.in.tds.web.rest.util.CommonValidations;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.microsoft.azure.storage.StorageException;

/**
 * Service Implementation for managing NatureOfPaymentMaster.
 */
@Service
public class NatureOfPaymentMasterService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final NatureOfPaymentMasterRepository natureOfPaymentMasterRepository;

	public NatureOfPaymentMasterService(NatureOfPaymentMasterRepository natureOfPaymentMasterRepository) {
		this.natureOfPaymentMasterRepository = natureOfPaymentMasterRepository;
	}

	@Autowired
	private SubNaturePaymentMasterRepository subNaturePaymentMasterRepository;

	@Autowired
	private BasisOfSurchargeDetailsRepository basisOfSurchargeDetailsRepository;

	@Autowired
	private BasisOfCessDetailsRepository basisOfCessDetailsRepository;

	@Autowired
	private TdsMasterRepository tdsMasterRepository;

	@Autowired
	private StatusRepository statusRepository;

	@Autowired
	private Sha256SumService sha256SumService;

	@Autowired
	private MasterBatchUploadRepository masterBatchUploadRepository;

	@Autowired
	private MasterBatchUploadService masterBatchUploadService;

	@Autowired
	private ResidentialStatusRepository residentialStatusRepository;

	/**
	 * This method is used to create new Nature of Payment record
	 * 
	 * @param userName
	 * 
	 * @param natureOfPaymentMastersfromui
	 * @return
	 * @throws JsonProcessingException
	 */

	public NatureOfPaymentMaster save(NatureOfPaymentMaster natureOfPayment, String userName)
			throws JsonProcessingException {
		logger.info("REST request to save NatureOfPaymentMaster : {} ", natureOfPayment);

		/**
		 * Below logic written for checking the unique subnature
		 */
		if (natureOfPayment.getIsSubNaturePaymentApplies()) {
			Set<SubNaturePaymentMaster> unique = natureOfPayment.getSubNaturePaymentMasters().stream()
					.collect(collectingAndThen(
							toCollection(() -> new TreeSet<>(comparing(SubNaturePaymentMaster::getNature))),
							HashSet::new));
			if (unique.size() < natureOfPayment.getSubNaturePaymentMasters().size()) {
				throw new BadRequestException("Subnature of payment name should not be same");
			}
		}
		Optional<NatureOfPaymentMaster> nopDb = natureOfPaymentMasterRepository
				.findBySectionAndNOP(natureOfPayment.getSection(), natureOfPayment.getNature());
		if (nopDb.isPresent()) {
			CommonValidations.validateApplicableFields(nopDb.get().getApplicableTo(),
					natureOfPayment.getApplicableFrom());
		}
		NatureOfPaymentMaster response = null;
		NatureOfPaymentMaster natureOfPaymentMaster = new NatureOfPaymentMaster();
		natureOfPaymentMaster.setApplicableFrom(natureOfPayment.getApplicableFrom());
		natureOfPaymentMaster.setApplicableTo(natureOfPayment.getApplicableTo());
		natureOfPaymentMaster.setDisplayValue(natureOfPayment.getDisplayValue());
		natureOfPaymentMaster.setNature(natureOfPayment.getNature());
		natureOfPaymentMaster.setSection(natureOfPayment.getSection());
		natureOfPaymentMaster.setIsSubNaturePaymentApplies(natureOfPayment.getIsSubNaturePaymentApplies());
		natureOfPaymentMaster.setCreatedBy(userName);
		natureOfPaymentMaster.setCreatedDate(Instant.now());
		natureOfPaymentMaster.setModifiedBy(userName);
		natureOfPaymentMaster.setModifiedDate(Instant.now());
		natureOfPaymentMaster.setActive(true);
		natureOfPaymentMaster.setConsiderDateofPayment(natureOfPayment.getConsiderDateofPayment());
		Set<SubNaturePaymentMaster> set = new HashSet<>();
		SubNaturePaymentMaster subSet = null;
		if (natureOfPayment.getIsSubNaturePaymentApplies()) {
			Set<SubNaturePaymentMaster> subSets = natureOfPayment.getSubNaturePaymentMasters();
			for (SubNaturePaymentMaster setOfSubNature : subSets) {
				Optional<SubNaturePaymentMaster> subNaturePaymentMasterOptional = subNaturePaymentMasterRepository
						.findByNature(setOfSubNature.getNature());
				if (!subNaturePaymentMasterOptional.isPresent()) {
					subSet = new SubNaturePaymentMaster();
					subSet.setNature(setOfSubNature.getNature());
					subSet.setNaturePaymentMaster(natureOfPaymentMaster);
					subSet.setActive(true);
					subSet.setCreatedDate(Instant.now());
					subSet.setCreatedBy(userName);
					set.add(subSet);
				} else {
					throw new CustomException(setOfSubNature.getNature() + " is already present  ",
							HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}
			natureOfPaymentMaster.setSubNaturePaymentMasters(set);
		}
		response = natureOfPaymentMasterRepository.save(natureOfPaymentMaster);
		return response;
	}

	/**
	 * This method is used to get all Nature of Payment record
	 * 
	 * @return
	 * @throws RecordNotFoundException
	 */
	@Transactional(readOnly = true)
	public List<NatureOfPaymentMasterDTO> findAll() {
		List<NatureOfPaymentMasterDTO> arrayList = new ArrayList<>();
		List<SubNaturePaymentMasterDTO> getSetData = null;
		NatureOfPaymentMasterDTO natureList = null;
		SubNaturePaymentMasterDTO setSubnature = null;
		Set<SubNaturePaymentMaster> set = null;
		List<NatureOfPaymentMaster> list = natureOfPaymentMasterRepository.findAll();
		if (!list.isEmpty()) {
			for (NatureOfPaymentMaster nature : list) {
				natureList = new NatureOfPaymentMasterDTO();
				getSetData = new ArrayList<>();
				natureList.setApplicableFrom(nature.getApplicableFrom());
				natureList.setApplicableTo(nature.getApplicableTo());
				natureList.setDisplayValue(nature.getDisplayValue());
				natureList.setId(nature.getId());
				natureList.setIsSubNaturePaymentApplies(nature.getIsSubNaturePaymentApplies());
				natureList.setNature(nature.getNature());
				natureList.setSection(nature.getSection());

				set = nature.getSubNaturePaymentMasters();
				for (SubNaturePaymentMaster sets : set) {
					setSubnature = new SubNaturePaymentMasterDTO();
					setSubnature.setId(sets.getId());
					setSubnature.setNature(sets.getNature());
					setSubnature.setNaturePaymentMaster(nature);
					getSetData.add(setSubnature);
				}
				natureList.setSubNaturePaymentMasters(getSetData);
				arrayList.add(natureList);
			}
		}
		return arrayList;
	}

	/**
	 * This method is used to get Nature of Payment record
	 * 
	 * @param id
	 * @return
	 * @throws RecordNotFoundException
	 */
	@Transactional(readOnly = true)
	public NatureOfPaymentMasterDTO findOne(Long id) {
		logger.info("REST request id to get a NatureOfPaymentMaster : {} ", id);
		NatureOfPaymentMasterDTO natureOfPaymentMasterDTO = new NatureOfPaymentMasterDTO();
		List<SubNaturePaymentMasterDTO> getSetData = null;
		SubNaturePaymentMasterDTO setSubnature = null;
		Set<SubNaturePaymentMaster> set = null;
		Optional<NatureOfPaymentMaster> natureOfpayment = natureOfPaymentMasterRepository.findById(id);

		NatureOfPaymentMaster nature = null;
		if (natureOfpayment.isPresent()) {
			getSetData = new ArrayList<>();
			nature = natureOfpayment.get();
			natureOfPaymentMasterDTO.setApplicableFrom(nature.getApplicableFrom());
			natureOfPaymentMasterDTO.setApplicableTo(nature.getApplicableTo());
			natureOfPaymentMasterDTO.setDisplayValue(nature.getDisplayValue());
			natureOfPaymentMasterDTO.setId(nature.getId());
			natureOfPaymentMasterDTO.setIsSubNaturePaymentApplies(nature.getIsSubNaturePaymentApplies());
			natureOfPaymentMasterDTO.setNature(nature.getNature());
			natureOfPaymentMasterDTO.setSection(nature.getSection());
			natureOfPaymentMasterDTO.setConsiderDateofPayment(nature.getConsiderDateofPayment());

			set = nature.getSubNaturePaymentMasters();
			for (SubNaturePaymentMaster sets : set) {
				setSubnature = new SubNaturePaymentMasterDTO();
				setSubnature.setId(sets.getId());
				setSubnature.setNature(sets.getNature());
				setSubnature.setNaturePaymentMaster(nature);
				getSetData.add(setSubnature);
			}
			natureOfPaymentMasterDTO.setSubNaturePaymentMasters(getSetData);
		}
		return natureOfPaymentMasterDTO;
	}

	/**
	 * This method is used to update Nature of Payment record
	 * 
	 * @param natureOfPaymentMaster
	 * @param userName
	 * @return
	 * @throws JsonProcessingException
	 * @throws RecordNotFoundException
	 */
	public NatureOfPaymentMaster updateNatureOfPaymentMaster(@Valid NatureOfPaymentMaster natureOfPaymentMaster,
			String userName) throws JsonProcessingException {
		logger.info("REST request to update NatureOfPaymentMaster : {} ", natureOfPaymentMaster);
		NatureOfPaymentMaster response = null;
		Optional<NatureOfPaymentMaster> natureOfPaymentMasters = natureOfPaymentMasterRepository
				.findById(natureOfPaymentMaster.getId());
		Set<SubNaturePaymentMaster> set = new HashSet<>();
		SubNaturePaymentMaster subSetUpdate = null;

		if (natureOfPaymentMasters.isPresent()) {
			NatureOfPaymentMaster natureOfPayment = natureOfPaymentMasters.get();
			natureOfPayment.setId(natureOfPaymentMaster.getId());
			natureOfPayment.setApplicableFrom(natureOfPaymentMaster.getApplicableFrom());
			natureOfPayment.setApplicableTo(natureOfPaymentMaster.getApplicableTo());
			natureOfPayment.setDisplayValue(natureOfPaymentMaster.getDisplayValue());
			natureOfPayment.setIsSubNaturePaymentApplies(natureOfPaymentMaster.getIsSubNaturePaymentApplies());
			natureOfPayment.setSection(natureOfPaymentMaster.getSection());
			natureOfPayment.setNature(natureOfPaymentMaster.getNature());
			natureOfPayment.setModifiedBy(userName);
			natureOfPayment.setModifiedDate(Instant.now());
			natureOfPayment.setConsiderDateofPayment(natureOfPaymentMaster.getConsiderDateofPayment());
			if (natureOfPayment.getSubNaturePaymentMasters() != null
					|| !natureOfPayment.getSubNaturePaymentMasters().isEmpty()) {
				for (SubNaturePaymentMaster subSet : natureOfPayment.getSubNaturePaymentMasters()) {
					subSetUpdate = new SubNaturePaymentMaster();
					subSetUpdate.setId(subSet.getId());
					subSetUpdate.setNature(subSet.getNature());
					subSetUpdate.setNaturePaymentMaster(natureOfPaymentMaster);
					set.add(subSetUpdate);
				}
			}
			natureOfPayment.setSubNaturePaymentMasters(set);
			response = natureOfPaymentMasterRepository.save(natureOfPayment);
		} else {
			throw new CustomException("No record found  with id " + natureOfPaymentMaster.getId(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	public NopCessRateSurageRateDTO findCessRateSurchargeRateNOPIdBasedonSection(String section) {

		NopCessRateSurageRateDTO nopCessSurchargeIds = new NopCessRateSurageRateDTO();
		List<NatureOfPaymentMaster> natureOfpayment = natureOfPaymentMasterRepository.findListBySection(section);
		NatureOfPaymentMaster nature = null;
		BasisOfSurchargeDetails basisOfSurcharge = null;
		BasisOfCessDetails basisOfCess = null;
		if (!natureOfpayment.isEmpty()) {
			nature = natureOfpayment.get(0);
			if (nature.getId() != null && nature.getId() != 0) {
				nopCessSurchargeIds.setId(nature.getId());
				// Get Basis Of Surcharge rate
				List<BasisOfSurchargeDetails> basisOfSurchargeDetails = basisOfSurchargeDetailsRepository
						.findListOfSurchargeRateByNatureOfPaymentId(nature.getId());
				if (!basisOfSurchargeDetails.isEmpty()) {
					basisOfSurcharge = basisOfSurchargeDetails.get(0);
					nopCessSurchargeIds.setSurchargeRate(basisOfSurcharge.getRate());
				}
				List<BasisOfCessDetails> basisOfCessDetails = basisOfCessDetailsRepository
						.findByNatureOfPaymentId(nature.getId());
				if (!basisOfCessDetails.isEmpty()) {
					basisOfCess = basisOfCessDetails.get(0);
					nopCessSurchargeIds.setCessRate(basisOfCess.getRate());
				}
			}
		}

		return nopCessSurchargeIds;
	}

	public Boolean getNOPBasedOnStatusAndSectionResidentStatus(String residentalStatus, String section,
			String deducteeStatus) {
		Optional<SectionNatureDTO> response = natureOfPaymentMasterRepository
				.getNOPBasedOnStatusAndSectionResidentStatus(residentalStatus, section, deducteeStatus);
		Boolean isSectionFound = false;
		if (response.isPresent()) {
			isSectionFound = true;
		}
		return isSectionFound;
	}

	/**
	 * 
	 * @param section
	 * @param nature
	 * @return
	 */
	public Optional<NatureOfPaymentMaster> findBySectionAndNOP(String section, String nature) {
		Optional<NatureOfPaymentMaster> nopDb = natureOfPaymentMasterRepository.findBySectionAndNOP(section, nature);
		if (nopDb.isPresent()) {
			return nopDb;
		} else {
			return Optional.empty();
		}
	}
	
	/**
	 * 
	 * @param section
	 * @param nature
	 * @return
	 */
	public Optional<NatureOfPaymentMaster> findByNOP(String nature) {
		Optional<NatureOfPaymentMaster> nopDb = natureOfPaymentMasterRepository.findByNOP(nature);
		if (nopDb.isPresent()) {
			return nopDb;
		} else {
			return Optional.empty();
		}
	}

	/**
	 * 
	 * @param section
	 * @param deducteeStatus
	 * @param residentStatus
	 * @return
	 */
	public List<Map<String, Object>> getListOfNatureAndRate(String section, String deducteeStatus,
			String residentStatus) {
		return natureOfPaymentMasterRepository.getListOfNatureAndRate(section, deducteeStatus, residentStatus);
	}

	/**
	 * 
	 * @param file
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @param userName
	 * @return
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 */
	public MasterBatchUpload saveFileData(MultipartFile file, Integer assesssmentYear, Integer assessmentMonth,
			String userName) throws IOException, InvalidKeyException, URISyntaxException, StorageException {

		String uploadType = UploadTypes.NATURE_OF_PAYMENT_EXCEL.name();

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
			int headersCount = NatureOfPaymentExcel.getNonEmptyCellsCount(headerRow);
			logger.info("Column header count :{}", headersCount);
			MasterBatchUpload masterBatchUpload = new MasterBatchUpload();
			if (headersCount != NatureOfPaymentExcel.fieldMappings.size()) {
				masterBatchUpload.setCreatedDate(Instant.now());
				masterBatchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
				masterBatchUpload.setSuccessCount(0L);
				masterBatchUpload.setFailedCount(0L);
				masterBatchUpload.setRowsCount(0L);
				masterBatchUpload.setProcessed(0);
				masterBatchUpload.setMismatchCount(0L);
				masterBatchUpload.setSha256sum(sha256);
				masterBatchUpload.setStatus("File Error");
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
			if (headersCount == NatureOfPaymentExcel.fieldMappings.size()) {
				return processNOPFile(workbook, file, sha256, assesssmentYear, assessmentMonth, userName,
						masterBatchUpload, uploadType);
			} else {
				throw new CustomException("Data not found ", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to process nop data ", e);
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
	
	@Transactional
	public MasterBatchUpload processNOPFile(XSSFWorkbook workbook, MultipartFile file, String sha256,
			Integer assesssmentYear, Integer assessmentMonth, String userName, MasterBatchUpload masterBatchUpload,
			String uploadType) throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		return processNOPExcelFile(workbook, file, sha256, assesssmentYear, assessmentMonth, userName,
				masterBatchUpload, uploadType);
	}
	/**
	 * processing the NOP excel file
	 * 
	 * @param file
	 * @throws IOException
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws InvalidKeyException
	 */
	@Async
	public MasterBatchUpload processNOPExcelFile(XSSFWorkbook workbook, MultipartFile file, String sha256,
			Integer assesssmentYear, Integer assessmentMonth, String userName, MasterBatchUpload masterBatchUpload,
			String uploadType) throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		File nopErrorFile = null;
		try {
			NatureOfPaymentExcel excel = new NatureOfPaymentExcel(workbook);
			long rowsWithDataCount = excel.getDataRowsCount();
			masterBatchUpload.setSha256sum(sha256);
			masterBatchUpload.setMismatchCount(0L);
			masterBatchUpload.setRowsCount(rowsWithDataCount);
			int duplicateCount = 0;
			int errorCount = 0;
			Long successCount = 0L;
			Map<String, NatureOfPaymentMasterExcelDTO> filter = new HashMap<>();
			List<NOPExcelErrorDTO> errorList = new ArrayList<>();
			logger.info("Number of records present in excel sheet {}", rowsWithDataCount);
			// reading rows from excel and filtering unique records
			for (int rowIndex = 1; rowIndex <= rowsWithDataCount; rowIndex++) {
				Optional<NOPExcelErrorDTO> errorDTO = null;
				try {
					errorDTO = excel.validate(rowIndex);
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
					NatureOfPaymentMasterExcelDTO nopDTO = excel.get(rowIndex);
					NOPExcelErrorDTO nopExcelErrorDTO = excel.getErrorDTO(rowIndex);
					if (StringUtils.isBlank(nopExcelErrorDTO.getReason())) {
						nopExcelErrorDTO.setReason("");
					}
					logger.info("Record fetched from excel for row {} is {}", rowIndex, nopDTO);
					String key = nopDTO.getNatureOfPayment() + "_" + nopDTO.getSection() + "_"
							+ nopDTO.getDeducteeStatus() + "_" + nopDTO.getResidentialStatus() + "_"
							+ nopDTO.getApplicableFrom() + "_" + nopDTO.getApplicableTo() + "_" + nopDTO.getRate();
					if (filter.containsKey(key)) {
						++duplicateCount;
					} else {
						// processing the unique records
						boolean isValid = true;
						boolean isNotValid = false;
						TdsMaster tdsMaster = new TdsMaster();
						NatureOfPaymentMaster nopEntity = new NatureOfPaymentMaster();
						// Date validation
						if (nopDTO.getApplicableTo() != null
								&& (nopDTO.getApplicableFrom().equals(nopDTO.getApplicableTo())
										|| nopDTO.getApplicableFrom().after(nopDTO.getApplicableTo()))) {
							nopExcelErrorDTO.setReason(nopExcelErrorDTO.getReason()
									+ "Applicable To cannot be Greater than Applicable From" + "\n");
							isNotValid = true;
						} else {
							nopEntity.setApplicableFrom(nopDTO.getApplicableFrom().toInstant());
							tdsMaster.setApplicableFrom(nopDTO.getApplicableFrom().toInstant());
							if (nopDTO.getApplicableTo() != null) {
								tdsMaster.setApplicableTo(nopDTO.getApplicableTo().toInstant());
							}
						}
						// checking for the duplicate record DB level along with date values
						List<TdsMaster> dbTdsMasterOptional = new ArrayList<>();
						try {
							dbTdsMasterOptional = natureOfPaymentMasterRepository
									.getFromTdsMasterUsingNOPSectionAndStatus(nopDTO.getNatureOfPayment(),
											nopDTO.getSection(), nopDTO.getDeducteeStatus());
						} catch (Exception e) {
							logger.error("Exception occured while processing NOP file and saving record in DB {}",
									e.getMessage());
						}
						if (!dbTdsMasterOptional.isEmpty()) {
							TdsMaster dbTdsMaster = dbTdsMasterOptional.get(0);
							if (dbTdsMaster.getRate().compareTo(nopDTO.getRate()) != 0
									&& (dbTdsMaster.getApplicableTo() == null || !dbTdsMaster.getApplicableTo()
											.isBefore(nopDTO.getApplicableFrom().toInstant()))) {
								isValid = false;
								duplicateCount++;
							} else if (dbTdsMaster.getApplicableTo() == null || !dbTdsMaster.getApplicableTo()
									.isBefore(nopDTO.getApplicableFrom().toInstant())) {
								dbTdsMaster
										.setApplicableTo(DateUtils.addDays(nopDTO.getApplicableFrom(), -1).toInstant());
								dbTdsMaster.setModifiedDate(new Date().toInstant());
								tdsMasterRepository.save(dbTdsMaster);
							}
						}

						if (isValid) {
							// presence of NOP record check in DB
							List<NatureOfPaymentMaster> nopList = natureOfPaymentMasterRepository
									.findActiveRecordsBySectionAndNOP(nopDTO.getSection(), nopDTO.getNatureOfPayment());
							if (!nopList.isEmpty()) {
								nopEntity = nopList.get(0);
								logger.info("Existing nop record retrieved from DB {}", nopEntity.getId());
							} else {
								// nature of payment
								nopEntity.setActive(true);
								nopEntity.setDisplayValue(nopDTO.getDisplayValue());
								nopEntity.setSection(nopDTO.getSection());
								nopEntity.setNature(nopDTO.getNatureOfPayment());
								nopEntity.setCreatedBy(userName);
								nopEntity.setCreatedDate(new Date().toInstant());
								nopEntity.isSubNaturePaymentApplies(false);
								nopEntity.setConsiderDateofPayment(
										nopDTO.getConsiderDateofPayment().equalsIgnoreCase("Y") ? true : false);
								nopEntity = natureOfPaymentMasterRepository.save(nopEntity);
								logger.info("Record inserted into nop table {}", nopEntity.getId());
							}
							// tds master
							tdsMaster.setActive(true);
							tdsMaster.setIsSubNaturePaymentMaster(false);
							tdsMaster.setRate(nopDTO.getRate());
							tdsMaster.setSaccode(nopDTO.getSacCode());
							tdsMaster.setNatureOfPayment(nopEntity);
							tdsMaster.setNoPanRate(nopDTO.getNoPanRate());
							tdsMaster.setNoItrRate(nopDTO.getNoItrRate());
							tdsMaster.setNoPanRateAndNoItrRate(nopDTO.getNoPanRateAndNoItrRate());
							tdsMaster.setIsPerTransactionLimit(isNull(nopDTO.getIsPerTransactionLimitApplicable()));
							if (tdsMaster.getIsPerTransactionLimit()) {
								try {
									tdsMaster.setPerTransactionLimit(nopDTO.getPerTansactionLimitAmount() == null ? 0l
											: nopDTO.getPerTansactionLimitAmount().longValue());
								} catch (Exception e) {
									logger.error("Per Transaction limit is not a number", e.getMessage());
								}
								
							} else {
								tdsMaster.setPerTransactionLimit(0L);
							}
							tdsMaster.setIsOverAllTransactionLimit(
									isNull(nopDTO.getIsAnnualTransactionLimitApplicable()));
							if (tdsMaster.getIsOverAllTransactionLimit()) {
								try {
									tdsMaster.setAnnualTransaction(nopDTO.getAnnualTransactionLimit() == null ? 0L
											: nopDTO.getAnnualTransactionLimit().longValue());
								} catch (Exception e) {
									logger.error("Annual Transaction limit is not a number", e.getMessage());
								}
							} else {
								tdsMaster.setAnnualTransaction(0L);
							}
							// check for deductee residential status
							if (StringUtils.isNotBlank(nopDTO.getResidentialStatus())) {
								Optional<ResidentialStatus> residentalStatus = residentialStatusRepository
										.findByStatus(nopDTO.getResidentialStatus());
								if (residentalStatus.isPresent() && isValid == true) {
									tdsMaster.setDeducteeResidentStatus(residentalStatus.get());
								} else {
									nopExcelErrorDTO.setReason(nopExcelErrorDTO.getReason() + "Residential Status"
											+ nopDTO.getResidentialStatus() + " is not valid." + "\n");
									isNotValid = true;
								}
							}
							if (StringUtils.isNotBlank(nopDTO.getDeducteeStatus())) {
								Optional<Status> statusResponse = statusRepository
										.findByStatus(nopDTO.getDeducteeStatus());
								if (statusResponse.isPresent() && isValid == true) {
									tdsMaster.setDeducteeStatus(statusResponse.get());
									tdsMaster = tdsMasterRepository.save(tdsMaster);
									logger.info("Record inserted into tds_master table {}", tdsMaster);
									++successCount;
								} else {
									nopExcelErrorDTO.setReason(nopExcelErrorDTO.getReason() + "Deductee Status "
											+ nopDTO.getDeducteeStatus() + " is not valid." + "\n");
									isNotValid = true;
								}
							}
							if (isNotValid) {
								++errorCount;
								errorList.add(nopExcelErrorDTO);
							}
						}
					}
				}
			}
		
			int processedRecordsCount = (int) (rowsWithDataCount - duplicateCount - errorCount);
			masterBatchUpload.setSuccessCount(successCount);
			masterBatchUpload.setFailedCount(Long.valueOf(errorCount));
			masterBatchUpload.setProcessed(processedRecordsCount);
			masterBatchUpload.setDuplicateCount(Long.valueOf(duplicateCount));
			masterBatchUpload.setStatus("Processed");
			masterBatchUpload.setCreatedDate(Instant.now());
			masterBatchUpload.setProcessEndTime(new Date());
			masterBatchUpload.setCreatedBy(userName);
			
			if (errorList.size() > 0) {
				nopErrorFile = prepareNopErrorFile(file.getOriginalFilename(), errorList,
						new ArrayList<>(excel.getHeaders()));
			}
		} catch (Exception e) {
			logger.error("Exception occured while processing NOP file and saving record in DB {}", e.getMessage());
			throw new CustomException("Exception occured while processing NOP file.");
		}
		return masterBatchUploadService.masterBatchUpload(masterBatchUpload, file, assesssmentYear, assessmentMonth,
				userName, nopErrorFile, uploadType);
	}

	/**
	 * 
	 * @param originalFilename
	 * @param errorList
	 * @param arrayList
	 * @return
	 * @throws Exception 
	 */
	private File prepareNopErrorFile(String originalFilename, List<NOPExcelErrorDTO> errorList,
			ArrayList<String> headers) throws Exception {
		try {
			headers.addAll(0, MasterExcel.STANDARD_ADDITIONAL_HEADERS);
			Workbook wkBook = nopXlsxReport(errorList, headers);
			ByteArrayOutputStream baout = new ByteArrayOutputStream();
			wkBook.save(baout, SaveFormat.XLSX);
			File errorFile = new File(
					FilenameUtils.getBaseName(originalFilename) + "_" + UUID.randomUUID() + "_Error_Report.xlsx");
			FileUtils.writeByteArrayToFile(errorFile, baout.toByteArray());
			baout.close();
			return errorFile;
		} catch (Exception e) {
			logger.error("Encountered an error while preparing error file", e);
			throw e;
		}
	}

	/**
	 * 
	 * @param errorList
	 * @param headers
	 * @return
	 * @throws Exception 
	 */
	private Workbook nopXlsxReport(List<NOPExcelErrorDTO> errorList, ArrayList<String> headers) throws Exception {

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);

		worksheet.getCells().importArrayList(headers, 5, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		setExtractDataForNop(errorList, worksheet, headers);

		// Style for A6 to B6 headers
		Style style1 = workbook.createStyle();
		style1.setForegroundColor(Color.fromArgb(169, 209, 142));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange1 = worksheet.getCells().createRange("A6:B6");
		headerColorRange1.setStyle(style1);

		// Style for C6 to S6 headers
		Style style2 = workbook.createStyle();
		style2.setForegroundColor(Color.fromArgb(252, 199, 155));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		style2.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange2 = worksheet.getCells().createRange("C6:S6");
		headerColorRange2.setStyle(style2);

		worksheet.autoFitColumns();
		worksheet.autoFitRows();
		worksheet.setGridlinesVisible(false);
		worksheet.freezePanes(0, 2, 0, 2);

		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

		String date = simpleDateFormat.format(new Date());

		Cell cellA1 = worksheet.getCells().get("A1");
		cellA1.setValue("TDS Payables Error Report (Dated: " + date + ")");
		Style a1Style = cellA1.getStyle();
		a1Style.getFont().setName("Cambria");
		a1Style.getFont().setSize(14);
		a1Style.getFont().setBold(true);
		cellA1.setStyle(a1Style);

		Cell cellA2 = worksheet.getCells().get("A2");
		Style a2Style = cellA2.getStyle();
		a2Style.getFont().setName("Cambria");
		a2Style.getFont().setSize(14);
		a2Style.getFont().setBold(true);
		cellA2.setStyle(a2Style);

		// column B5 column
		Cell cellB5 = worksheet.getCells().get("A5");
		cellB5.setValue("Error/Information");
		Style b5Style = cellB5.getStyle();
		b5Style.setForegroundColor(Color.fromArgb(180, 199, 231));
		b5Style.setPattern(BackgroundType.SOLID);
		b5Style.getFont().setBold(true);
		cellB5.setStyle(b5Style);

		int maxdatacol = worksheet.getCells().getMaxDataColumn();
		int maxdatarow = worksheet.getCells().getMaxDataRow();

		String firstHeaderCellName = "A6";
		String lastHeaderCellName = "S6";
		String firstDataCellName = "A7";
		String lastDataCellName = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);

		CommonUtil.setBoardersForAsposeXlsx(worksheet, firstHeaderCellName, lastHeaderCellName, firstDataCellName,
				lastDataCellName);
		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A6:S6");
		worksheet.autoFitColumn(1);
		worksheet.autoFitRows();
		return workbook;
	}

	/**
	 * 
	 * @param errorList
	 * @param worksheet
	 * @param headers
	 * @throws Exception
	 */
	private void setExtractDataForNop(List<NOPExcelErrorDTO> errorList, Worksheet worksheet,
			ArrayList<String> headers) throws Exception {
		if (!errorList.isEmpty()) {
			int dataRowsStartIndex = 6;
			int serialNumber = 1;
			for (int i = 0; i < errorList.size(); i++) {
				NOPExcelErrorDTO errorDTO = errorList.get(i);
				ArrayList<String> rowData = MasterExcel.getValues(errorDTO, NatureOfPaymentExcel.fieldMappings, headers);
				rowData.set(0, StringUtils.isBlank(errorDTO.getReason()) ? StringUtils.EMPTY : errorDTO.getReason());
				rowData.set(1, String.valueOf(serialNumber));
				worksheet.getCells().importArrayList(rowData, i + dataRowsStartIndex, 0, false);
				worksheet.autoFitRow(i + dataRowsStartIndex);
				worksheet.autoFitColumn(i);
				MasterExcel.colorCodeErrorCells(worksheet, i + dataRowsStartIndex, headers, errorDTO.getReason());
				serialNumber++;
			}
		}
	}

	public boolean isNull(Boolean isValue) {
		return isValue != null ? isValue : false;
	}

	
	public List<Map<String, Object>> getSectionAndRate() {
		return natureOfPaymentMasterRepository.getSectionAndRate();
	}

	public List<Map<String, Object>> getNOPAndSectionsResidentialStatus(String residentalStatus) {
		return natureOfPaymentMasterRepository.getNopBasedOnResidentialStatus(residentalStatus);
	}
	
	public List<Map<String, Object>> getSectionAndDeducteeStatusBasedOnStatus(String residentalStatus) {
		return natureOfPaymentMasterRepository.getSectionAndDeducteeStatusBasedOnStatus(residentalStatus);
	}

}
