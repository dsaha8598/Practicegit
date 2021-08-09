package com.ey.in.tds.returns.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ey.in.tds.common.jdbc.returns.dao.FilingFilesDAO;
import com.ey.in.tds.common.returns.jdbc.dto.FilingFiles;
import com.ey.in.tds.common.returns.jdbc.dto.FilingStatus;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.core.util.FilingFilesType;
import com.ey.in.tds.core.util.ReturnType;
import com.ey.in.tds.feign.client.ChallansClient;
import com.ey.in.tds.feign.client.IngestionClient;
import com.ey.in.tds.feign.client.MastersClient;
import com.ey.in.tds.feign.client.OnboardingClient;
import com.ey.in.tds.jdbc.dao.FilingStatusDAO;
import com.ey.in.tds.returns.domain.FilingChallanDetailBean;
import com.ey.in.tds.returns.domain.FilingDeducteeDetailBean;
import com.ey.in.tds.returns.domain.FilingFileBean;
import com.ey.in.tds.returns.domain.FilingHeaderBean;
import com.ey.in.tds.returns.dto.QuarterDataDTO;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.microsoft.azure.storage.StorageException;

@Service
public class RawFileGenerationService {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	protected BlobStorage blobStorage;

	@Autowired
	protected ChallansClient challansClient;

	@Autowired
	protected MastersClient mastersClient;

	@Autowired
	protected OnboardingClient onboardingClient;

	@Autowired
	protected IngestionClient ingestionClient;

	@Autowired
	protected FilingFilesDAO filingFileDAO;
	
	@Autowired
	protected FilingStatusDAO filingStatusDAO;

	protected static final Map<String, int[]> QUARTER_MONTHS_MAP = new HashMap<>();
	static {
		QUARTER_MONTHS_MAP.put("Q1", new int[] { 4, 5, 6 });
		QUARTER_MONTHS_MAP.put("Q2", new int[] { 7, 8, 9 });
		QUARTER_MONTHS_MAP.put("Q3", new int[] { 10, 11, 12 });
		QUARTER_MONTHS_MAP.put("Q4", new int[] { 1, 2, 3 });
	}

	protected static final Map<String, String> PREVIOUS_QUARTERS_MAP = new HashMap<>();
	static {
		PREVIOUS_QUARTERS_MAP.put("Q1", "Q4");
		PREVIOUS_QUARTERS_MAP.put("Q2", "Q1");
		PREVIOUS_QUARTERS_MAP.put("Q3", "Q2");
		PREVIOUS_QUARTERS_MAP.put("Q4", "Q3");
	}

	protected static final Map<String, String> QUARTER_LAST_DAY = new HashMap<>();
	static {
		QUARTER_LAST_DAY.put("Q1", "3006");
		QUARTER_LAST_DAY.put("Q2", "3009");
		QUARTER_LAST_DAY.put("Q3", "3112");
		QUARTER_LAST_DAY.put("Q4", "3103");
	}

	/**
	 * This method will write bean data to text file
	 * 
	 * @param filingFileBean
	 * @param formType
	 * @return
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 */
	public String generateTextFile(FilingFileBean filingFileBean, String tenantId, String formType,
			boolean isCorrection, int assessmentYear, String quarter)
			throws InvalidKeyException, URISyntaxException, StorageException {
		File file = new File(RandomStringUtils.random(8, true, true) + ".txt");
		// File file = new File("o2p3vwci" + ".txt");
		String uri = null;
		// Create the file
		try {
			if (file.createNewFile()) {
				logger.info("File is created!");
			} else {
				logger.info("File already exists.");
			}

			// Write bean data to text file
			try (FileWriter writer = new FileWriter(file)) {
				// Writing Header data
				writer.write(generateHeader(filingFileBean, isCorrection, assessmentYear, quarter));
				writer.write("\n");
				// Writing Batch header data
				writer.write(filingFileBean.getBatchHeaderBean().toString());

				// Transfer Voucher Detail Record Writing Challan
				List<FilingChallanDetailBean> filingChallanDetails = filingFileBean.getChallanDetailBeanList();
				int index = 3;
				for (FilingChallanDetailBean filingChallanDetail : filingChallanDetails) {
					filingChallanDetail.setLineNo(StringUtils.EMPTY + index++);
					writer.write("\n");
					writer.write(filingChallanDetail.toString());
					for (FilingDeducteeDetailBean filingDeducteeDetailBean : filingChallanDetail
							.getDeducteeDetailBeanList()) {
						writer.write("\n");
						filingDeducteeDetailBean.setLineNo(StringUtils.EMPTY + index++);
						writer.write(filingDeducteeDetailBean.toString());
					}
				}
			}
		} catch (IOException e) {
			logger.error(StringUtils.EMPTY, e);
		}

		try {
			uri = blobStorage.uploadExcelToBlobWithFile(file, tenantId);
		} catch (IOException e1) {
			logger.error("Error occured while creating file", e1);
		}

		return uri;
	}

	public String generateHeader(FilingFileBean tDS27QFileBean, boolean isCorrection, int assessmentYear,
			String quarter) {
		FilingHeaderBean filingHeaderBean = new FilingHeaderBean();

		filingHeaderBean.setLineNo(1);
		filingHeaderBean.setRecordType("FH");
		filingHeaderBean.setFileType("NS1");
		if (isCorrection) {
			filingHeaderBean.setUploadType("C");
		} else {
			filingHeaderBean.setUploadType("R");
		}
		SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy");
		String dateString = formatter.format(new Date());
		filingHeaderBean.setFileDate(dateString);

		// should be unique across all files? (all files meaning?)
		filingHeaderBean.setFileSeq(1);
		filingHeaderBean.setUploaderType("D");

		filingHeaderBean.setTanOfDeductor(tDS27QFileBean.getBatchHeaderBean().getTanOfDeductor());
		filingHeaderBean.setNoOfBatches(1);

		filingHeaderBean.setRpuName("NSDL RPU 3.5");

		filingHeaderBean.setRecordHash(StringUtils.EMPTY);
		filingHeaderBean.setFvuVersion(StringUtils.EMPTY);
		filingHeaderBean.setFileHash(StringUtils.EMPTY);
		filingHeaderBean.setSamVersion(StringUtils.EMPTY);
		filingHeaderBean.setSamHash(StringUtils.EMPTY);
		filingHeaderBean.setScmVersion(StringUtils.EMPTY);
		filingHeaderBean.setScmHash(StringUtils.EMPTY);
		filingHeaderBean.setFileHash(StringUtils.EMPTY);
		filingHeaderBean.setConsHash(StringUtils.EMPTY);
		if (isCorrection) {
			List<FilingFiles> filingFilesList = filingFileDAO.findByYearQuarterTanFileType(assessmentYear,
					tDS27QFileBean.getBatchHeaderBean().getTanOfDeductor(), quarter, "Conso");
			if (!filingFilesList.isEmpty()) {
				filingHeaderBean.setConsHash(filingFilesList.get(0).getConsolidatedFileHash());
			}
		}
		return filingHeaderBean.toString();
	}

	@Transactional
	public void filingLogic(String fileType, String quarter, Integer assessmentYear, String tanNumber, String userName,
			String textUrl, String formType) throws InvalidKeyException, URISyntaxException, StorageException, IOException {
		logger.info("Entered in filingLogic method.");
		// check if the record exists or not if exists update status column
		// check with the file record
		// of insert new record in Table1
		// FilingFiles.Key key = new FilingFiles.Key(assessmentYear, quarter);

		// storing in blob excel
		// need confirmation on storing the excel uploads
		// String fileUrl = blobStorage.uploadExcelToBlob(multipartFile);

		List<FilingFiles> filingFileList=filingFileDAO.findByYearQuarterDeductorTanAndFormType(assessmentYear, quarter, tanNumber, formType);
		Date today = new Date();
		if (!filingFileList.isEmpty()) {
			FilingFiles filingFile = filingFileList.get(0);
			filingFile.setActive(true);
			filingFile.setFileType(ReturnType.REGULAR.name());
			filingFile.setActive(true);
			filingFile.setFilingType(ReturnType.REGULAR.name());
			filingFile.setFormType(formType);
			filingFile.setFileType(FilingFilesType.getType(textUrl, formType));
			filingFile.setFileStatus(FilingFiles.FilingFilesStatus.getStatus(textUrl));
			filingFile.setFileUrl(textUrl);
			filingFile.setIsRequested(true);
			filingFile.setUpdatedby(userName);
			filingFile.setUpdatedDate(new Timestamp(today.getTime()));
			filingFile.setGeneratedDate(new Timestamp(today.getTime()));
			filingFileDAO.update(filingFile);
		} else {
			FilingFiles filingFiles=new FilingFiles();
			filingFiles.setAssesmentYear(assessmentYear);
			filingFiles.setQuarter(quarter);
			filingFiles.setActive(true);
			filingFiles.setDeductorTan(tanNumber);
			// submitted,available ,failed,not available, not available-no deductee
			// records,not available pan not present in master
			
			filingFiles.setFormType(formType);
			filingFiles.setFileType(FilingFilesType.getType(textUrl, formType));
			filingFiles.setFileStatus(FilingFiles.FilingFilesStatus.getStatus(textUrl));
			filingFiles.setFilingType(ReturnType.REGULAR.name());
			filingFiles.setFileUrl(textUrl);
			// revision or current regular
			filingFiles.setIsRequested(true);
			filingFiles.setGeneratedDate(new Timestamp(today.getTime()));
			filingFiles.setCreatedDate(new Timestamp(today.getTime()));
			filingFiles.setCreatedBy(userName);
			filingFileDAO.save(filingFiles);
			logger.info("Filing Files Record Inserted {}");
		}

		// Clear all output files if any
		List<FilingFiles> outputFiles=filingFileDAO.findByYearQuarterDeductorTanAndFormType(assessmentYear, quarter, 
				tanNumber, formType);
				outputFiles.stream()		
				.filter(filingFile -> (filingFile.getFileType().equals(FilingFilesType.LOG.name())
						|| filingFile.getFileType().equals(FilingFilesType.HTML.name())
						|| filingFile.getFileType().equals(FilingFilesType.FVU.name())
						|| filingFile.getFileType().endsWith(FilingFilesType.PDF.name())
						|| filingFile.getFileType().equals("27A")
						|| filingFile.getFileType().equals("PNG")
						|| filingFile.getFileType().equals(FilingFilesType.IMAGE.name())
						|| filingFile.getFileType().endsWith(FilingFilesType.ERR.name())))
				.forEach(filingFile -> {
					filingFileDAO.deleteById(filingFile.getFilingFilesId()); 
				});
		logger.info("Completed in filingLogic method.");
	}
	
	@Transactional
	public void saveInFilingStatus(Integer assessmentYear, String quarter, String deductorPan, String tanNumber,
			String filingType, String tenantId, String userName, String formType)
			throws JsonParseException, JsonMappingException, IOException {
		logger.info("Entered in saveInFilingStatus method.");

		QuarterDataDTO identifyQuarter = quarterData(assessmentYear, quarter);

		List<FilingStatus> filingStatusList=filingStatusDAO.findByYearAndQuarterAndTan(assessmentYear, quarter,
				tanNumber,formType);
		
		if (!filingStatusList.isEmpty()) {
			FilingStatus filingStatus = filingStatusList.get(0);
			filingStatus.setDeductorPan(deductorPan);
			filingStatus.setDeductorTan(tanNumber);
			// due
			filingStatus.setDueDateForFiling(identifyQuarter.getFilingStatusDuedateForFiling());
			filingStatus.setFilingType(filingType);
			filingStatus.setQuarterEndDate(identifyQuarter.getFilingStatusQuarterEndDate());
			filingStatus.setQuarterStartDate(identifyQuarter.getFilingStatusQuarterStartDate());
			filingStatus.setRevisionExists(false);
			filingStatus.setUpdatedBy(userName);
			filingStatus.setUpdatedDate(new Timestamp(new Date().getTime()));
			filingStatus.setPnrOrTokenNumber(StringUtils.EMPTY);
			filingStatus.setFileType(formType);
			filingStatus.setFormType(formType);
			filingStatusDAO.update(filingStatus);
			
		} else {
			
			FilingStatus filingStatus=new FilingStatus();
			filingStatus.setAssesmentYear(assessmentYear);
			filingStatus.setQuarter(quarter);
			filingStatus.setActive(true);
			// get deductor pan
			filingStatus.setDeductorPan(deductorPan);
			filingStatus.setDeductorTan(tanNumber);
			// due
			filingStatus.setDueDateForFiling(identifyQuarter.getFilingStatusDuedateForFiling());
			filingStatus.setFilingType(filingType);
			filingStatus.setQuarterEndDate(identifyQuarter.getFilingStatusQuarterEndDate());
			filingStatus.setQuarterPeriod("3 Months");
			filingStatus.setQuarterStartDate(identifyQuarter.getFilingStatusQuarterStartDate());
			filingStatus.setRevisionExists(false);
			filingStatus.setStatus("submitted");
			filingStatus.setCreatedBy(userName);
			filingStatus.setCreatedDate(new Timestamp(new Date().getTime()));
			filingStatus.setPnrOrTokenNumber(StringUtils.EMPTY);
			filingStatus.setFileType(formType);
			filingStatus.setFormType(formType);
			filingStatusDAO.save(filingStatus);
		}
		logger.info("Completed saveInFilingStatus method.");
	}
	
	/**
	 * This method identifies the quarter and returns the required dates accordingly
	 * 
	 * @param assessmentYear
	 * @param quarter
	 * @return
	 */
	private QuarterDataDTO quarterData(Integer assessmentYear, String quarter) {

		QuarterDataDTO quarterDataDTO = new QuarterDataDTO();
		Year y = Year.of(assessmentYear);
		Date dueDate = java.sql.Date.valueOf(y.atDay(90));

		if (quarter.equalsIgnoreCase("Q1")) {
			java.util.Date startDateQuarter1 = java.sql.Date.valueOf(y.atDay(91));
			java.util.Date endDateQuarter1 = java.sql.Date.valueOf(y.atDay(181));
			setQuarterData(quarterDataDTO, startDateQuarter1, endDateQuarter1, dueDate);
		}
		if (quarter.equalsIgnoreCase("Q2")) {
			java.util.Date startDateQuarter2 = java.sql.Date.valueOf(y.atDay(182));
			java.util.Date endDateQuarter2 = java.sql.Date.valueOf(y.atDay(273));
			setQuarterData(quarterDataDTO, startDateQuarter2, endDateQuarter2, dueDate);
		}
		if (quarter.equalsIgnoreCase("Q3")) {
			java.util.Date startDateQuarter3 = java.sql.Date.valueOf(y.atDay(274));
			java.util.Date endDateQuarter3 = java.sql.Date.valueOf(y.atDay(365));
			setQuarterData(quarterDataDTO, startDateQuarter3, endDateQuarter3, dueDate);
		}
		if (quarter.equalsIgnoreCase("Q4")) {
			java.util.Date startDateQuarter4 = java.sql.Date.valueOf(y.atDay(1));
			java.util.Date endDateQuarter4 = java.sql.Date.valueOf(y.atDay(90));
			setQuarterData(quarterDataDTO, startDateQuarter4, endDateQuarter4, dueDate);
		}
		return quarterDataDTO;
	}
	
	public void setQuarterData(QuarterDataDTO quarterDataDTO, Date startDateQuarter, Date endDateQuarter,
			Date dueDate) {
		quarterDataDTO.setFilingStatusQuarterStartDate(startDateQuarter);
		quarterDataDTO.setFilingStatusQuarterEndDate(endDateQuarter);
		quarterDataDTO.setFilingStatusDuedateForFiling(dueDate);
	}
	
	public String getNullSafeStateCode(String stateName, Map<String, String> stateCodeMap) {
		String stateCode = stateCodeMap.get(stateName.trim().toUpperCase());
		if (StringUtils.isBlank(stateCode)) {
			return StringUtils.EMPTY;
		} else {
			return stateCode;
		}
	}
	
	private String replaceUnnecessaryValueWithSpace(String value) {
		if(value.split("\\^")[1].equals("CD")) {
			return value.replace("lineNo", "")
					.replace("filler4", "")
					.replace("filler6", "")
					.replace("filler7", "")
					.replace("filler5", "")
					.replace("filler", "")
					.replace("recType", "")
					.replace("chBatchNo", "")
					.replace("challanDetailRecordNo", "")
					.replace("countOfDeducteeDetail", "")
					.replace("nillChallanIndicator", "")
					.replace("challanUpdationIndicator", "")
					.replace("lastBankChallanNo", "")
					.replace("bankChallanNo", "")
					.replace("lastTransferVoucherNo", "")
					.replace("ddoSerialNumberForm24G", "")
					.replace("lastBankBranchCode", "")
					.replace("bankBranchCode", "")
					.replace("lastDateOfBankChallanNo", "")
					.replace("dateOfBankChallanNo", "")
					.replace("section", "")
					.replace("oltasIncomeTax", "")
					.replace("oltasSurcharge", "")
					.replace("oltasCess", "")
					.replace("oltasInterest", "")
					.replace("oltasOthers", "")
					.replace("totalOfDepositAmountAsPerChallan", "")
					.replace("lastTotalOfDepositAmountAsPerChallan", "")
					.replace("totalTaxDepositedAsPerDeducteeAnex", "")
					.replace("tdsIncomeTaxC", "")
					.replace("tdsSurchargeC", "")
					.replace("tdsCessC", "")
					.replace("sumTotalIncTaxDedAtSource", "")
					.replace("tdsInterest", "")
					.replace("tdsOthers", "")
					.replace("chequeDDNo", "")
					.replace("bookCash", "")
					.replace("remark", "")
					.replace("lateFee", "")
					.replace("minorHeadCodeChallan", "")
					.replace("challanHash", "");
					
		}else {
			return value.replace("lineNo", "")
			.replace("recType", "").replace("ddBatchNo", "").replace("ChallanRecordNo", "")
			.replace("deducteeDetailRecNo", "").replace("mode", "").replace("deducteeSerialNo", "")
			.replace("deducteeCode", "").replace("lastDeducteePan", "").replace("deducteePan", "")
			.replace("lastDeducteeRefNo", "").replace("lastTotalIncomeTaxDeductedAtSource", "").replace("dateOfDeposit", "").replace("rateAtWhichTaxDeducted", "")
			.replace("deducteeRefNo", "").replace("totalTaxDeposited", "").replace("grossingUpIndicator", "").replace("bookCashEntry", "")
			.replace("deducteeName", "").replace("lastTotalTaxDeposited", "").replace("dateOfFurnishingTaxDeductionCertificate", "").replace("remark1", "")
			.replace("tdsIncomeTaxDD", "").replace("totalValueofPurchase", "").replace("remark2", "").replace("remark3", "")
			.replace("tdsSurchargeDD", "").replace("amountOfPayment", "").replace("sectionCode", "").replace("certNumAo", "")
			.replace("tdsCessDD", "").replace("dateOnWhichAmountPaid", "").replace("filler2", "")
			.replace("totalIncomeTaxDeductedAtSource", "").replace("dateOnWhichTaxDeducted", "").replace("filler3", "")
			.replace("filler4", "")
			.replace("filler5", "")
			.replace("filler6", "")
			.replace("filler7", "")
			.replace("filler8", "")
			.replace("isDTAA", "")
			.replace("natureOfRemittance", "")
			.replace("uniqueAck15CA", "")
			.replace("countryOfDeductee", "")
			.replace("emailOfDeductee", "")
			.replace("contactNumberOfDeductee", "")
			.replace("addressOfDeducteeInCountry", "")
			.replace("tinOfDeductee", "")
			.replace("excess94NAmount", "")
			.replace("filler9", "")
			.replace("filler10", "")
			.replace("filler11", "")
			.replace("filler12", "")
			.replace("deducteeHash", "")
			.replace("filler1", "");
		}
	}
	public String generateTextFileWithStringData(FilingFileBean filingFileBean, String tenantId, String formType,
			boolean isCorrection, int assessmentYear, String quarter)
			throws InvalidKeyException, URISyntaxException, StorageException {
		File file = new File(RandomStringUtils.random(8, true, true) + ".txt");
		// File file = new File("o2p3vwci" + ".txt");
		String uri = null;
		// Create the file
		try {
			if (file.createNewFile()) {
				logger.info("File is created!");
			} else {
				logger.info("File already exists.");
			}

			// Write bean data to text file
			try (FileWriter writer = new FileWriter(file)) {
				// Writing Header data
				writer.write(generateHeader(filingFileBean, isCorrection, assessmentYear, quarter));
				writer.write("\n");
				// Writing Batch header data
				writer.write(filingFileBean.getBatchHeaderBean().toString());

				// Transfer Voucher Detail Record Writing Challan
				List<FilingChallanDetailBean> filingChallanDetails = filingFileBean.getChallanDetailBeanList();
				int index = 3;
				for (String filingChallanAndDeducteeDetail : filingFileBean.getChallanDetailsStringList()) {
					//filingChallanDetail.setLineNo(StringUtils.EMPTY + index++);
					writer.write("\n");
					writer.write(replaceUnnecessaryValueWithSpace(filingChallanAndDeducteeDetail));
					
				}
			}
		} catch (IOException e) {
			logger.error(StringUtils.EMPTY, e);
		}

		try {
			uri = blobStorage.uploadExcelToBlobWithFile(file, tenantId);
		} catch (IOException e1) {
			logger.error("Error occured while creating file", e1);
		}

		return uri;
	}}
