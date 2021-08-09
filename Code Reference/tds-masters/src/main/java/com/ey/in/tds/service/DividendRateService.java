package com.ey.in.tds.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ey.in.tds.common.domain.Country;
import com.ey.in.tds.common.domain.MasterBatchUpload;
import com.ey.in.tds.common.domain.dividend.DividendDeductorType;
import com.ey.in.tds.common.domain.dividend.DividendRateAct;
import com.ey.in.tds.common.domain.dividend.DividendRateTreaty;
import com.ey.in.tds.common.domain.dividend.ShareholderCategory;
import com.ey.in.tds.common.dto.DividendRateActDTO;
import com.ey.in.tds.common.dto.DividendRateTreatyDTO;
import com.ey.in.tds.common.service.Sha256SumService;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.repository.DividendRateRepository;
import com.ey.in.tds.repository.DividendStaticDataRepository;
import com.ey.in.tds.repository.MasterBatchUploadRepository;
import com.ey.in.tds.service.util.excel.DividendRateActExcel;
import com.microsoft.azure.storage.StorageException;


/**
 * 
 * @author dipak
 *
 */
@Service
public class DividendRateService {

	private final Logger logger = LoggerFactory.getLogger(DividendRateService.class);
	@Autowired
	private DividendRateRepository dividendRateRepository;

	@Autowired
	private DividendStaticDataRepository dividendStaticDataRepository;

	@Autowired
	private Sha256SumService sha256SumService;

	@Autowired
	private MasterBatchUploadService masterBatchUploadService;

	@Autowired
	private MasterBatchUploadRepository masterBatchUploadRepository;


	
	@Autowired
	private DividendRateBulkService dividendRateBulkService;


	public static DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	@Transactional
	public DividendRateAct createDividendRateAct(final DividendRateActDTO dividendRateActDTO, final String userName) {
		logger.info("REST request to save RateMasterAct : {}", dividendRateActDTO);
		boolean isDuplicate = this.dividendRateRepository.isDuplicateDividendRateAct(
				dividendRateActDTO.getDividendDeductorTypeId(), dividendRateActDTO.getShareholderCategoryId(),
				dividendRateActDTO.getResidentialStatus(), dividendRateActDTO.getSection(),
				dividendRateActDTO.getApplicableFrom().truncatedTo(ChronoUnit.DAYS),
				dividendRateActDTO.getApplicableTo() != null
						? dividendRateActDTO.getApplicableTo().truncatedTo(ChronoUnit.DAYS)
						: null);
		if (isDuplicate) {
			throw new CustomException("Duplicate Dividend Rate Act already exists");
		}
		DividendDeductorType dividendDeductorType = this.dividendStaticDataRepository
				.getReference(DividendDeductorType.class, dividendRateActDTO.getDividendDeductorTypeId());
		ShareholderCategory shareholderCategory = this.dividendStaticDataRepository
				.getReference(ShareholderCategory.class, dividendRateActDTO.getShareholderCategoryId());
		DividendRateAct dividendRateAct = DividendRateAct.of(dividendDeductorType, shareholderCategory,
				dividendRateActDTO.getResidentialStatus(), dividendRateActDTO.getSection(),
				dividendRateActDTO.getTdsRate(), dividendRateActDTO.getExemptionThreshold(),
				dividendRateActDTO.getApplicableFrom(), dividendRateActDTO.getApplicableTo(), userName);
		this.dividendRateRepository.saveDividendRateAct(dividendRateAct);
		logger.info("SAVED Record :{}", dividendRateActDTO);
		return dividendRateAct;
	}

	@Transactional(readOnly = true)
	public Optional<DividendRateAct> getDividendRateActById(final Long id) {
		return this.dividendRateRepository.findDividendRateActById(id);
	}

	@Transactional(readOnly = true)
	public List<DividendRateAct> getAllDividendRateActs() {
		return this.dividendRateRepository.findAllDividendRateActs();
	}

	@Transactional
	public DividendRateAct updateDividendRateActEndDate(final long id, final Instant applicableTo, String userName) {
		DividendRateAct dividendRateAct = this.dividendRateRepository.findDividendRateActById(id)
				.orElseThrow(() -> new RecordNotFoundException("No DividendRateAct found with id " + id));
		dividendRateAct.updateApplicableTo(applicableTo, userName);
		return dividendRateAct;
	}

	@Transactional
	public DividendRateTreaty createDividendRateTreaty(final DividendRateTreatyDTO dividendRateTreatytDTO,
			final String userName) {
		Country country = this.dividendStaticDataRepository.getReference(Country.class,
				dividendRateTreatytDTO.getCountryId());
		boolean isDuplicate = this.dividendRateRepository.isDuplicateDividendRateTreaty(country,
				dividendRateTreatytDTO.getTaxTreatyClause(), dividendRateTreatytDTO.getMfnClauseExists(),
				dividendRateTreatytDTO.getMliArticle8Applicable(), dividendRateTreatytDTO.getMliPptConditionSatisfied(),
				dividendRateTreatytDTO.getMliSlobConditionSatisfied(),
				dividendRateTreatytDTO.getForeignCompShareholdingInIndComp(), null,
				dividendRateTreatytDTO.getApplicableFrom().truncatedTo(ChronoUnit.DAYS),
				dividendRateTreatytDTO.getApplicableTo() != null
						? dividendRateTreatytDTO.getApplicableTo().truncatedTo(ChronoUnit.DAYS)
						: null,dividendRateTreatytDTO.getMfnNotAvailedCompanyTaxRate(),dividendRateTreatytDTO.getMfnAvailedCompanyTaxRate());
		if (isDuplicate) {
			throw new CustomException("Duplicate Dividend Rate Treaty already exists");
		}
		if (dividendRateTreatytDTO.getMfnClauseExists().booleanValue()) {
			DividendRateTreaty dividendRateTreaty = DividendRateTreaty.of(country)
					.applicableDateRange(dividendRateTreatytDTO.getApplicableFrom(),
							dividendRateTreatytDTO.getApplicableTo())
					.treatyClause(dividendRateTreatytDTO.getTaxTreatyClause())
					.mli(dividendRateTreatytDTO.getMliArticle8Applicable(),
							dividendRateTreatytDTO.getMliPptConditionSatisfied(),
							dividendRateTreatytDTO.getMliSlobConditionSatisfied())
					.mfnClauseExist()
					.mfnClauseAvailed(dividendRateTreatytDTO.getMfnAvailedCompanyTaxRate(),
							dividendRateTreatytDTO.getMfnAvailedNonCompanyTaxRate())
					.mfnClauseNotAvailed(dividendRateTreatytDTO.getMfnNotAvailedCompanyTaxRate(),
							dividendRateTreatytDTO.getMfnNotAvailedNonCompanyTaxRate())
					.foreignCompShareholdingInIndComp(dividendRateTreatytDTO.getForeignCompShareholdingInIndComp())
					.countrySpecificRules(dividendRateTreatytDTO.getCountrySpecificRules()).build(userName);
			return this.dividendRateRepository.saveDividendRateTreaty(dividendRateTreaty);
		} else {
			DividendRateTreaty dividendRateTreaty = DividendRateTreaty.of(country)
					.applicableDateRange(dividendRateTreatytDTO.getApplicableFrom(),
							dividendRateTreatytDTO.getApplicableTo())
					.treatyClause(dividendRateTreatytDTO.getTaxTreatyClause())
					.mli(dividendRateTreatytDTO.getMliArticle8Applicable(),
							dividendRateTreatytDTO.getMliPptConditionSatisfied(),
							dividendRateTreatytDTO.getMliSlobConditionSatisfied())
					.mfnClauseDoesNotExist()
					.mfnClauseNotAvailed(dividendRateTreatytDTO.getMfnNotAvailedCompanyTaxRate(),
							dividendRateTreatytDTO.getMfnNotAvailedNonCompanyTaxRate())
					.foreignCompShareholdingInIndComp(dividendRateTreatytDTO.getForeignCompShareholdingInIndComp())
					.countrySpecificRules(dividendRateTreatytDTO.getCountrySpecificRules()).build(userName);
			return this.dividendRateRepository.saveDividendRateTreaty(dividendRateTreaty);
		}
	}

	@Transactional(readOnly = true)
	public Optional<DividendRateTreaty> getDividendRateTreatyById(final Long id) {
		return this.dividendRateRepository.findDividendRateTreatyById(id);
	}

	@Transactional(readOnly = true)
	public List<DividendRateTreaty> getAllDividendRateTreaties() {
		List<DividendRateTreaty> list=this.dividendRateRepository.findAllDividendRateTreaties();
		return list.stream().sorted((n1,n2)->n2.getId().compareTo(n1.getId())).collect(Collectors.toList());
	}

	@Transactional
	public DividendRateTreaty updateDividendRateTreatyEndDate(final long id, final Instant applicableTo,
			String userName) {
		DividendRateTreaty dividendRateTreaty = this.dividendRateRepository.findDividendRateTreatyById(id)
				.orElseThrow(() -> new RecordNotFoundException("No DividendRateTreaty found with id " + id));
		dividendRateTreaty.updateApplicableTo(applicableTo, userName);
		return dividendRateTreaty;
	}

	@Transactional
	public MasterBatchUpload saveFileData(MultipartFile file, Integer assesssmentYear, Integer assessmentMonth,
			String userName) throws InvalidKeyException, URISyntaxException, StorageException, IOException {

		String uploadType = UploadTypes.DIVIDEND_RATE_ACT_EXCEL.name();
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
			int headersCount = DividendRateActExcel.getNonEmptyCellsCount(headerRow);
			logger.info("Column header count :{}", headersCount);
			MasterBatchUpload masterBatchUpload = new MasterBatchUpload();
			if (headersCount != DividendRateActExcel.fieldMappings.size()) {
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
			if (headersCount == DividendRateActExcel.fieldMappings.size()) {
				dividendRateBulkService.saveDividendRateActData(workbook, file, sha256, assesssmentYear, assessmentMonth, userName,
						masterBatchUpload, uploadType);
				return masterBatchUpload;
			} else {
				throw new CustomException("Data not found ", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to process Dividend Rate Act master data ", e);
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
	public MasterBatchUpload saveDividendRateTreatyFileData(MultipartFile file, Integer assesssmentYear,
			Integer assessmentMonth, String userName)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException {

		String uploadType = UploadTypes.DIVIDEND_RATE_TREATY_EXCEL.name();
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
			int headersCount = headerRow.getLastCellNum();
			logger.info("Column header count :{}", headersCount);
			MasterBatchUpload masterBatchUpload = new MasterBatchUpload();
			if (headersCount != 19) {
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
			if (headersCount == 19) {
				 dividendRateBulkService.saveDividendRateTreatyData(workbook, file, sha256, assesssmentYear, assessmentMonth, userName,
						masterBatchUpload, uploadType);
				 return masterBatchUpload;
			} else {
				throw new CustomException("Data not found ", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to process Dividend Rate Treaty master data ", e);
		}
	}

	

}