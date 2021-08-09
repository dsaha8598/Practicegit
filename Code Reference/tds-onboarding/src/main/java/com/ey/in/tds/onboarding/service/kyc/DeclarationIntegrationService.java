package com.ey.in.tds.onboarding.service.kyc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
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
import com.ey.in.tcs.common.domain.CollecteeDeclaration;
import com.ey.in.tcs.common.domain.CollecteeThresholdUpdate;
import com.ey.in.tcs.common.domain.DeducteeThresholdUpdate;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.NatureOfPaymentMaster;
import com.ey.in.tds.common.domain.ThresholdGroupAndNopMapping;
import com.ey.in.tds.common.domain.tcs.TCSBatchUpload;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.BatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.TCSBatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.dto.onboarding.deductee.DeducteeDeclaration;
import com.ey.in.tds.common.model.deductee.CollecteeThresholdUpdateErrorDTO;
import com.ey.in.tds.common.model.deductee.DeclarationErrorDTO;
import com.ey.in.tds.common.model.deductee.DeducteeThresholdUpdateErrorDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.CollecteeMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.CollecteeNoiThresholdLedger;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeducteeMasterResidential;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeducteeNopGroup;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.ShareholderDeclaration;
import com.ey.in.tds.common.onboarding.jdbc.dto.ShareholderMasterResidential;
import com.ey.in.tds.common.service.BlobStorageService;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.feign.client.MastersClient;
import com.ey.in.tds.jdbc.dao.CollecteeDeclarationDAO;
import com.ey.in.tds.jdbc.dao.CollecteeMasterDAO;
import com.ey.in.tds.jdbc.dao.CollecteeNoiThresholdLedgerDAO;
import com.ey.in.tds.jdbc.dao.DeducteeDeclarationDAO;
import com.ey.in.tds.jdbc.dao.DeducteeMasterResidentialDAO;
import com.ey.in.tds.jdbc.dao.DeducteeNopGroupDAO;
import com.ey.in.tds.jdbc.dao.DeductorMasterDAO;
import com.ey.in.tds.jdbc.dao.ShareholderDeclarationDAO;
import com.ey.in.tds.jdbc.dao.ShareholderMasterResidentialDAO;
import com.ey.in.tds.onboarding.service.util.excel.deductee.CollecteeDeclarationExcel;
import com.ey.in.tds.onboarding.service.util.excel.deductee.CollecteeThresholdExcel;
import com.ey.in.tds.onboarding.service.util.excel.deductee.DeducteeDeclarationExcel;
import com.ey.in.tds.onboarding.service.util.excel.deductee.DeducteeThresholdExcel;
import com.ey.in.tds.onboarding.service.util.excel.deductee.ShareholderDeclarationExcel;
import com.microsoft.azure.storage.StorageException;

import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;

/**
 * 
 * @author vamsir
 *
 */
@Service
public class DeclarationIntegrationService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private BatchUploadDAO batchUploadDAO;

	@Autowired
	private BlobStorage blob;

	@Autowired
	private DeductorMasterDAO deductorMasterDAO;

	@Autowired
	private BlobStorageService blobStorageService;

	@Autowired
	private MastersClient mastersClient;

	@Autowired
	private CollecteeDeclarationDAO collecteeDeclarationDAO;

	@Autowired
	private DeducteeDeclarationDAO deducteeDeclarationDAO;

	@Autowired
	private CollecteeMasterDAO collecteeMasterDAO;

	@Autowired
	private DeducteeMasterResidentialDAO deducteeMasterResidentialDAO;

	@Autowired
	private TCSBatchUploadDAO tcsBatchUploadDAO;

	@Autowired
	private CollecteeNoiThresholdLedgerDAO collecteeNoiThresholdLedgerDAO;

	@Autowired
	private ShareholderMasterResidentialDAO shareholderMasterResidentialDAO;

	@Autowired
	private ShareholderDeclarationDAO shareholderDeclarationDAO;

	@Autowired
	private DeducteeNopGroupDAO deducteeNopGroupDAO;

	/**
	 * 
	 * @param userName
	 * @param tenantId
	 * @param deductorPan
	 * @param batchUpload
	 * @throws InvalidKeyException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws StorageException
	 */
	@Async
	public void asyncProcessCollecteeDeclaration(String userName, String tenantId, String deductorPan,
			TCSBatchUpload batchUpload) throws InvalidKeyException, IOException, URISyntaxException, StorageException {
		MultiTenantContext.setTenantId(tenantId);
		processCollecteeDeclaration(userName, tenantId, deductorPan, batchUpload);
	}

	/**
	 * 
	 * @param deductorTan
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @param userName
	 * @param tenantId
	 * @param deductorPan
	 * @param batchUpload
	 * @param type
	 * @throws InvalidKeyException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws StorageException
	 */
	private void processCollecteeDeclaration(String userName, String tenantId, String deductorPan,
			TCSBatchUpload batchUpload) throws InvalidKeyException, IOException, URISyntaxException, StorageException {

		MultiTenantContext.setTenantId(tenantId);
		ArrayList<DeclarationErrorDTO> errorList = new ArrayList<>();
		File collecteeDeclarationErrorFile = null;
		File file = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getFilePath());
		XSSFWorkbook workbook = new XSSFWorkbook(file.getAbsolutePath());
		try {
			CollecteeDeclarationExcel collecteeData = new CollecteeDeclarationExcel(workbook);
			long dataRowsCount = collecteeData.getDataRowsCount();

			batchUpload.setMismatchCount(0L);
			batchUpload.setRowsCount(dataRowsCount);
			Long errorCount = 0L;
			Long duplicateCount = 0L;

			List<CollecteeDeclaration> collecteeList = new ArrayList<>();
			List<CollecteeDeclaration> batchUpdateCollecteeDeclarationList = new ArrayList<>();
			Set<String> duplicationRecords = new HashSet<>();
			Set<String> collecteeCodeList = new HashSet<>();
			for (int rowIndex = 1; rowIndex <= dataRowsCount; rowIndex++) {
				Optional<DeclarationErrorDTO> errorDTO = Optional.empty();
				boolean isValid = true;
				try {
					errorDTO = collecteeData.validate(rowIndex);
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
						CollecteeDeclaration collecteeDeclaration = collecteeData.get(rowIndex);
						DeclarationErrorDTO collecteeErrorDTO = collecteeData.getErrorDTO(rowIndex);
						if (StringUtils.isBlank(collecteeErrorDTO.getReason())) {
							collecteeErrorDTO.setReason("");
						}
						// get all collectee master records.
						List<CollecteeMaster> collecteeMasterList = collecteeMasterDAO
								.getAllCollecteeByCodes(deductorPan, collecteeDeclaration.getCollecteeCode().trim());
						if (collecteeMasterList.isEmpty()) {
							collecteeErrorDTO.setReason(
									collecteeErrorDTO.getReason() + "Collectee code not avalible in collectee master "
											+ collecteeDeclaration.getCollecteeCode() + "\n");
							isValid = false;
						}

						if (isValid) {

							List<CollecteeDeclaration> collecteeDeclarationList = collecteeDeclarationDAO
									.getAllCollecteeDeclarationByCode(deductorPan, batchUpload.getCollectorMasterTan(),
											collecteeMasterList.get(0).getId(), batchUpload.getAssessmentYear());
							if (!collecteeDeclarationList.isEmpty()) {
								collecteeDeclaration
										.setSpecifiedPerson(collecteeDeclarationList.get(0).getSpecifiedPerson());
								if (StringUtils.isEmpty(collecteeDeclaration.getRateType())) {
									collecteeDeclaration.setRateType(collecteeDeclarationList.get(0).getRateType());
								} else {
									collecteeDeclaration.setRateType(collecteeDeclaration.getRateType());
								}
								for (CollecteeDeclaration declaration : collecteeDeclarationList) {
									declaration.setActive(false);
									declaration.setApplicableTo(subtractDay(new Date()));
									declaration.setModifiedBy(userName);
									declaration.setModifiedDate(new Date());
									batchUpdateCollecteeDeclarationList.add(declaration);
								}
							}
							collecteeDeclaration.setActive(true);
							collecteeDeclaration.setCollecteeCode(collecteeDeclaration.getCollecteeCode().trim());
							collecteeDeclaration.setCollecteeId(collecteeMasterList.get(0).getId());
							collecteeDeclaration.setTdsOrTcs(collecteeDeclaration.getTdsOrTcs());
							collecteeDeclaration.setCreatedDate(new Timestamp(new Date().getTime()));
							collecteeDeclaration.setCreatedBy(userName);
							collecteeDeclaration.setModifiedBy(userName);
							collecteeDeclaration.setModifiedDate(new Timestamp(new Date().getTime()));
							collecteeDeclaration.setDeductorTan(batchUpload.getCollectorMasterTan());
							collecteeDeclaration.setDeductorPan(deductorPan);
							collecteeDeclaration.setApplicableFrom(new Date());
							collecteeDeclaration.setYear(batchUpload.getAssessmentYear());
							// duplicate check
							String uniqueRecord = collecteeDeclaration.getCollecteeCode() + "_"
									+ collecteeDeclaration.getRateType() + "_" + collecteeDeclaration.getTdsOrTcs();
							if (duplicationRecords.contains(uniqueRecord)) {
								duplicateCount++;
							} else {
								duplicationRecords.add(uniqueRecord);
								// add collectee declaration details list
								collecteeList.add(collecteeDeclaration);
								collecteeCodeList.add(collecteeDeclaration.getCollecteeCode());
							}
						}

						if (!isValid) {
							++errorCount;
							errorList.add(collecteeErrorDTO);
						}
					} catch (Exception e) {
						logger.error("Unable to process row number " + rowIndex + " due to " + e.getMessage(), e);
						DeclarationErrorDTO problematicDataError = collecteeData.getErrorDTO(rowIndex);
						if (StringUtils.isBlank(problematicDataError.getReason())) {
							problematicDataError.setReason(
									"Unable to save data for row number " + rowIndex + " due to : " + e.getMessage());
						}
						errorList.add(problematicDataError);
						++errorCount;
					}
				}
			} // for 1

			// inactivating collecte declaration records
			if (!batchUpdateCollecteeDeclarationList.isEmpty()) {
				collecteeDeclarationDAO.batchUpdateApplicableTo(batchUpdateCollecteeDeclarationList);
			}

			if (!collecteeList.isEmpty()) {
				// batch save
				collecteeDeclarationDAO.batchSaveCollecteeDeclaration(collecteeList, tenantId);
			}

			if (!errorList.isEmpty()) {
				collecteeDeclarationErrorFile = prepareDeclarationErrorFile(file.getName(),
						batchUpload.getCollectorMasterTan(), deductorPan, errorList,
						new ArrayList<>(collecteeData.getHeaders()), batchUpload.getUploadType());
			}

			batchUpload.setSuccessCount(dataRowsCount);
			batchUpload.setFailedCount(errorCount);
			batchUpload.setProcessed(collecteeList.size());
			batchUpload.setDuplicateCount(duplicateCount);
			batchUpload.setStatus("Processed");
			batchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
			batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			batchUpload.setModifiedBy(userName);

			kycDetailsTcsBatchUpload(batchUpload, null, batchUpload.getCollectorMasterTan(),
					batchUpload.getAssessmentYear(), batchUpload.getAssessmentMonth(), userName,
					collecteeDeclarationErrorFile, tenantId, batchUpload.getUploadType());

		} catch (Exception e) {
			logger.error("Exception occurred :{}", e.getMessage());
		}
	}

	/**
	 * 
	 * @param originalFileName
	 * @param deductorTan
	 * @param deductorPan
	 * @param errorList
	 * @param headers
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public File prepareDeclarationErrorFile(String originalFileName, String deductorTan, String deductorPan,
			ArrayList<DeclarationErrorDTO> errorList, ArrayList<String> headers, String type) throws Exception {
		try {
			headers.addAll(0, Excel.STANDARD_ADDITIONAL_HEADERS);
			Workbook wkBook = declarationXlsxReport(errorList, deductorTan, deductorPan, headers, type);
			ByteArrayOutputStream baout = new ByteArrayOutputStream();
			wkBook.save(baout, SaveFormat.XLSX);
			File errorFile = new File(
					FilenameUtils.getBaseName(originalFileName) + "_" + UUID.randomUUID() + "_Error_Report.xlsx");
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
	 * @param deductorTan
	 * @param deductorPan
	 * @param headers
	 * @return
	 * @throws Exception
	 */
	private Workbook declarationXlsxReport(ArrayList<DeclarationErrorDTO> errorList, String deductorTan,
			String deductorPan, ArrayList<String> headers, String type) throws Exception {

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);

		worksheet.getCells().importArrayList(headers, 5, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		setExtractDataForDeclaration(errorList, worksheet, deductorTan, headers, type);

		// Style for A6 to B6 headers
		Style style1 = workbook.createStyle();
		style1.setForegroundColor(Color.fromArgb(169, 209, 142));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange1 = worksheet.getCells().createRange("A6:B6");
		headerColorRange1.setStyle(style1);

		Cell cellD6 = worksheet.getCells().get("C6");
		Style styleD6 = cellD6.getStyle();
		styleD6.setForegroundColor(Color.fromArgb(0, 0, 0));
		styleD6.setPattern(BackgroundType.SOLID);
		styleD6.getFont().setBold(true);
		styleD6.getFont().setColor(Color.getWhite());
		styleD6.setHorizontalAlignment(TextAlignmentType.CENTER);
		cellD6.setStyle(styleD6);

		// Style for D6 to G6 headers
		Style style2 = workbook.createStyle();
		style2.setForegroundColor(Color.fromArgb(252, 199, 155));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		style2.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange2 = worksheet.getCells().createRange("D6:F6");
		headerColorRange2.setStyle(style2);

		worksheet.autoFitColumns();
		worksheet.autoFitRows();
		worksheet.setGridlinesVisible(false);
		worksheet.freezePanes(0, 2, 0, 2);

		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

		String date = simpleDateFormat.format(new Date());

		List<DeductorMaster> response = deductorMasterDAO.findByDeductorPan(deductorPan);

		Cell cellA1 = worksheet.getCells().get("A1");
		cellA1.setValue("TDS Payables Error Report (Dated: " + date + ")");
		Style a1Style = cellA1.getStyle();
		a1Style.getFont().setName("Cambria");
		a1Style.getFont().setSize(14);
		a1Style.getFont().setBold(true);
		cellA1.setStyle(a1Style);

		Cell cellA2 = worksheet.getCells().get("A2");
		if (!response.isEmpty()) {
			cellA2.setValue("Client Name:" + response.get(0).getName());
		} else {
			cellA2.setValue("Client Name:" + StringUtils.EMPTY);
		}

		Style a2Style = cellA2.getStyle();
		a2Style.getFont().setName("Cambria");
		a2Style.getFont().setSize(14);
		a2Style.getFont().setBold(true);
		cellA2.setStyle(a2Style);

		// column B5 column
		Cell cellB5 = worksheet.getCells().get("B5");
		cellB5.setValue("Error/Information");
		Style b5Style = cellB5.getStyle();
		b5Style.setForegroundColor(Color.fromArgb(180, 199, 231));
		b5Style.setPattern(BackgroundType.SOLID);
		b5Style.getFont().setBold(true);
		cellB5.setStyle(b5Style);

		int maxdatacol = worksheet.getCells().getMaxDataColumn();
		int maxdatarow = worksheet.getCells().getMaxDataRow();

		String firstHeaderCellName = "A6";
		String lastHeaderCellName = "F6";
		String firstDataCellName = "A7";
		String lastDataCellName = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);

		CommonUtil.setBoardersForAsposeXlsx(worksheet, firstHeaderCellName, lastHeaderCellName, firstDataCellName,
				lastDataCellName);
		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A6:F6");
		worksheet.autoFitColumn(1);
		worksheet.autoFitRows();
		return workbook;
	}

	/**
	 * 
	 * @param batchUpload
	 * @param mFile
	 * @param tan
	 * @param assesssmentYear
	 * @param assessmentMonthPlusOne
	 * @param userName
	 * @param file
	 * @param tenant
	 * @param type
	 * @return
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 */
	public TCSBatchUpload kycDetailsTcsBatchUpload(TCSBatchUpload batchUpload, MultipartFile mFile, String tan,
			Integer assesssmentYear, Integer assessmentMonthPlusOne, String userName, File file, String tenant,
			String type) throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		logger.info("batch", batchUpload);
		String errorFp = null;
		String path = null;
		if (file != null) {
			errorFp = blob.uploadExcelToBlobWithFile(file, tenant);
			batchUpload.setErrorFilePath(errorFp);
		}
		if (mFile != null) {
			path = blob.uploadExcelToBlob(mFile);
			batchUpload.setFileName(mFile.getOriginalFilename());
			batchUpload.setFilePath(path);
		}
		int month = assessmentMonthPlusOne;
		batchUpload.setAssessmentMonth(month);
		batchUpload.setAssessmentYear(assesssmentYear);
		batchUpload.setCollectorMasterTan(tan);
		if ("COLLECTEE_DECLARATION".equalsIgnoreCase(type)) {
			batchUpload.setUploadType(UploadTypes.COLLECTEE_DECLARATION.name());
		} else if ("COLLECTEE_THRESHOLD_UPDATE".equalsIgnoreCase(type)) {
			batchUpload.setUploadType(UploadTypes.COLLECTEE_THRESHOLD_UPDATE.name());
		}
		batchUpload.setCreatedBy(userName);
		batchUpload.setActive(true);
		if (batchUpload.getId() != null) {
			batchUpload.setId(batchUpload.getId());
			batchUpload = tcsBatchUploadDAO.update(batchUpload);
		} else {
			batchUpload = tcsBatchUploadDAO.save(batchUpload);
		}
		return batchUpload;
	}

	/**
	 * 
	 * @param errorList
	 * @param worksheet
	 * @param deductorTan
	 * @param headers
	 * @param type
	 * @throws Exception
	 */
	private void setExtractDataForDeclaration(ArrayList<DeclarationErrorDTO> errorList, Worksheet worksheet,
			String deductorTan, ArrayList<String> headers, String type) throws Exception {
		if (!errorList.isEmpty()) {
			int dataRowsStartIndex = 6;
			int serialNumber = 0;
			for (int i = 0; i < errorList.size(); i++) {
				DeclarationErrorDTO errorDTO = errorList.get(i);
				ArrayList<String> rowData = new ArrayList<>();
				if ("COLLECTEE_DECLARATION".equals(type)) {
					rowData = Excel.getValues(errorDTO, CollecteeDeclarationExcel.fieldMappings, headers);
				} else if ("DEDUCTEE_DECLARATION".equals(type)) {
					rowData = Excel.getValues(errorDTO, DeducteeDeclarationExcel.fieldMappings, headers);
				} else if ("SHAREHOLDER_DECLARATION".equals(type)) {
					rowData = Excel.getValues(errorDTO, ShareholderDeclarationExcel.fieldMappings, headers);
				}
				rowData.set(0, StringUtils.isBlank(deductorTan) ? StringUtils.EMPTY : deductorTan);
				rowData.set(1, StringUtils.isBlank(errorDTO.getReason()) ? StringUtils.EMPTY : errorDTO.getReason());
				rowData.set(2, String.valueOf(++serialNumber));
				worksheet.getCells().importArrayList(rowData, i + dataRowsStartIndex, 0, false);
				worksheet.autoFitRow(i + dataRowsStartIndex);
				worksheet.autoFitColumn(i);
				Excel.colorCodeErrorCells(worksheet, i + dataRowsStartIndex, headers, errorDTO.getReason());
			}
		}
	}

	/**
	 * 
	 * @param userName
	 * @param tenantId
	 * @param deductorPan
	 * @param batchUpload
	 * @throws IOException
	 */
	@Async
	public void asyncProcessDeducteeDeclaration(String userName, String tenantId, String deductorPan,
			BatchUpload batchUpload) throws IOException {
		MultiTenantContext.setTenantId(tenantId);
		processDeducteeDeclaration(userName, tenantId, deductorPan, batchUpload);
	}

	/**
	 * 
	 * @param userName
	 * @param tenantId
	 * @param deductorPan
	 * @param batchUpload
	 * @throws IOException
	 */
	private void processDeducteeDeclaration(String userName, String tenantId, String deductorPan,
			BatchUpload batchUpload) throws IOException {
		MultiTenantContext.setTenantId(tenantId);
		ArrayList<DeclarationErrorDTO> errorList = new ArrayList<>();
		File declarationErrorFile = null;
		File file = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getFilePath());
		XSSFWorkbook workbook = new XSSFWorkbook(file.getAbsolutePath());
		try {
			DeducteeDeclarationExcel deducteeExcelData = new DeducteeDeclarationExcel(workbook);
			long dataRowsCount = deducteeExcelData.getDataRowsCount();

			batchUpload.setMismatchCount(0L);
			batchUpload.setRowsCount(dataRowsCount);
			Long errorCount = 0L;
			Long duplicateCount = 0L;

			List<DeducteeDeclaration> deducteeDeclarationList = new ArrayList<>();
			Set<String> duplicationRecords = new HashSet<>();
			List<DeducteeDeclaration> batchUpdateDeducteeDeclarationList = new ArrayList<>();

			for (int rowIndex = 1; rowIndex <= dataRowsCount; rowIndex++) {
				Optional<DeclarationErrorDTO> errorDTO = Optional.empty();
				boolean isValid = true;
				try {
					errorDTO = deducteeExcelData.validate(rowIndex);
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
						DeducteeDeclaration deducteeDeclaration = deducteeExcelData.get(rowIndex);
						DeclarationErrorDTO collecteeErrorDTO = deducteeExcelData.getErrorDTO(rowIndex);
						if (StringUtils.isBlank(collecteeErrorDTO.getReason())) {
							collecteeErrorDTO.setReason("");
						}
						String deducteeKey = StringUtils.EMPTY;
						// deducteee key
						if (StringUtils.isNotBlank(deducteeDeclaration.getDeducteeCode())) {
							deducteeKey = deducteeDeclaration.getDeducteeCode().trim();
						} else {
							String name = deducteeDeclaration.getDeducteeName().trim().toLowerCase();
							name = name.replaceAll("[^a-z0-9 ]", "").replace(" ", "-");
							if (StringUtils.isNotBlank(deducteeDeclaration.getDeducteePan())) {
								deducteeKey = name.concat(deducteeDeclaration.getDeducteePan().trim());
							} else {
								deducteeKey = name;
							}
						}
						
						List<DeducteeMasterResidential> deducteeMasterList = deducteeMasterResidentialDAO
								.findAllByDeducteeKey(deductorPan, deducteeKey);
						if (deducteeMasterList.isEmpty()) {
							collecteeErrorDTO.setReason(collecteeErrorDTO.getReason()
									+ "Deductee not avalible in deductee master residential "
									+ deducteeDeclaration.getDeducteeCode() + "\n");
							isValid = false;
						}
						
						if (isValid) {
							List<DeducteeDeclaration> deducteeDeclarationInactivateList = deducteeDeclarationDAO
									.getAllDeducteeDeclarationByCode(deductorPan, batchUpload.getDeductorMasterTan(),
											deducteeMasterList.get(0).getDeducteeMasterId(),
											batchUpload.getAssessmentYear());

							if (!deducteeDeclarationInactivateList.isEmpty()) {
								deducteeDeclaration.setSpecifiedPerson(
										deducteeDeclarationInactivateList.get(0).getSpecifiedPerson());
								// rate type check
								if (StringUtils.isEmpty(deducteeDeclaration.getRateType())) {
									deducteeDeclaration
											.setRateType(deducteeDeclarationInactivateList.get(0).getRateType());
								} else {
									deducteeDeclaration.setRateType(deducteeDeclaration.getRateType());
								}
								for (DeducteeDeclaration declaration : deducteeDeclarationInactivateList) {
									declaration.setActive(false);
									declaration.setApplicableTo(subtractDay(new Date()));
									declaration.setModifiedBy(userName);
									declaration.setModifiedDate(new Date());
									batchUpdateDeducteeDeclarationList.add(declaration);
								}
							}

							deducteeDeclaration.setActive(true);
							deducteeDeclaration.setDeducteeCode(deducteeKey);
							deducteeDeclaration.setDeducteeId(deducteeMasterList.get(0).getDeducteeMasterId());
							deducteeDeclaration.setTdsOrTcs("TDS");
							deducteeDeclaration.setCreatedDate(new Timestamp(new Date().getTime()));
							deducteeDeclaration.setCreatedBy(userName);
							deducteeDeclaration.setModifiedBy(userName);
							deducteeDeclaration.setModifiedDate(new Timestamp(new Date().getTime()));
							deducteeDeclaration.setDeductorTan(batchUpload.getDeductorMasterTan());
							deducteeDeclaration.setDeductorPan(deductorPan);
							deducteeDeclaration.setYear(batchUpload.getAssessmentYear());
							deducteeDeclaration.setApplicableFrom(new Date());
							String uniqueRecord = deducteeDeclaration.getDeducteeCode() + "_"
									+ deducteeDeclaration.getRateType() + "_" + deducteeDeclaration.getTdsOrTcs();

							if (duplicationRecords.contains(uniqueRecord)) {
								duplicateCount++;
							} else {
								duplicationRecords.add(uniqueRecord);
								// add deductee declaration
								deducteeDeclarationList.add(deducteeDeclaration);
							}
						}

						if (!isValid) {
							++errorCount;
							errorList.add(collecteeErrorDTO);
						}
					} catch (Exception e) {
						logger.error("Unable to process row number " + rowIndex + " due to " + e.getMessage(), e);
						DeclarationErrorDTO problematicDataError = deducteeExcelData.getErrorDTO(rowIndex);
						if (StringUtils.isBlank(problematicDataError.getReason())) {
							problematicDataError.setReason(
									"Unable to save data for row number " + rowIndex + " due to : " + e.getMessage());
						}
						errorList.add(problematicDataError);
						++errorCount;
					}
				}
			} // for 1

			// Inactivating old records
			if (!batchUpdateDeducteeDeclarationList.isEmpty()) {
				deducteeDeclarationDAO.batchUpdateApplicableTo(batchUpdateDeducteeDeclarationList);
			}

			if (!deducteeDeclarationList.isEmpty()) {
				// batch save
				deducteeDeclarationDAO.batchSaveDeducteeDeclaration(deducteeDeclarationList, tenantId);
			}

			if (!errorList.isEmpty()) {
				declarationErrorFile = prepareDeclarationErrorFile(file.getName(), batchUpload.getDeductorMasterTan(),
						deductorPan, errorList, new ArrayList<>(deducteeExcelData.getHeaders()),
						batchUpload.getUploadType());
			}

			batchUpload.setSuccessCount(dataRowsCount);
			batchUpload.setFailedCount(errorCount);
			batchUpload.setProcessedCount(deducteeDeclarationList.size());
			batchUpload.setDuplicateCount(duplicateCount);
			batchUpload.setStatus("Processed");
			batchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
			batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			batchUpload.setModifiedBy(userName);

			KycDetailsBatchUpload(batchUpload, null, batchUpload.getDeductorMasterTan(),
					batchUpload.getAssessmentYear(), batchUpload.getAssessmentMonth(), userName, declarationErrorFile,
					tenantId, batchUpload.getUploadType());

		} catch (Exception e) {
			logger.error("Exception occurred :{}", e.getMessage());
		}
	}

	/**
	 *
	 * @param batchUpload
	 * @param mFile
	 * @param tan
	 * @param assesssmentYear
	 * @param assessmentMonthPlusOne
	 * @param userName
	 * @param file
	 * @param tenant
	 * @return
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 */
	public BatchUpload KycDetailsBatchUpload(BatchUpload batchUpload, MultipartFile mFile, String tan,
			Integer assesssmentYear, Integer assessmentMonthPlusOne, String userName, File file, String tenant,
			String type) throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		logger.info("batch", batchUpload);
		String errorFp = null;
		String path = null;
		if (file != null) {
			errorFp = blob.uploadExcelToBlobWithFile(file, tenant);
			batchUpload.setErrorFilePath(errorFp);
		}
		if (mFile != null) {
			path = blob.uploadExcelToBlob(mFile);
			batchUpload.setFileName(mFile.getOriginalFilename());
			batchUpload.setFilePath(path);
		}
		int month = assessmentMonthPlusOne;
		batchUpload.setAssessmentMonth(month);
		batchUpload.setAssessmentYear(assesssmentYear);
		batchUpload.setDeductorMasterTan(tan);
		if ("CUSTOMER".equalsIgnoreCase(type)) {
			batchUpload.setUploadType(UploadTypes.CUSTOMER_KYC_DETAILS_UPLOAD.name());
		} else if ("VENDOR".equalsIgnoreCase(type)) {
			batchUpload.setUploadType(UploadTypes.VENDOR_KYC_DETAILS_UPLOAD.name());
		} else if ("SHAREHOLDER".equalsIgnoreCase(type)) {
			batchUpload.setUploadType(UploadTypes.SHAREHOLDER_KYC_DETAILS_UPLOAD.name());
		} else if ("DEDUCTEE_THRESHOLD_UPDATE".equalsIgnoreCase(type)) {
			batchUpload.setUploadType(UploadTypes.DEDUCTEE_THRESHOLD_UPDATE.name());
		} else if ("DEDUCTEE_DECLARATION".equalsIgnoreCase(type)) {
			batchUpload.setUploadType(UploadTypes.DEDUCTEE_DECLARATION.name());
		} else if ("SHAREHOLDER_DECLARATION".equalsIgnoreCase(type)) {
			batchUpload.setUploadType(UploadTypes.SHAREHOLDER_DECLARATION.name());
		}
		batchUpload.setCreatedBy(userName);
		batchUpload.setActive(true);
		if (batchUpload.getBatchUploadID() != null) {
			batchUpload.setBatchUploadID(batchUpload.getBatchUploadID());
			batchUpload = batchUploadDAO.update(batchUpload);
		} else {
			batchUpload = batchUploadDAO.save(batchUpload);
		}
		return batchUpload;
	}

	/**
	 * @param userName
	 * @param tenantId
	 * @param deductorPan
	 * @param batchUpload
	 * @throws InvalidKeyException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws StorageException
	 */
	@Async
	public void asyncProcessColleteeThresholdUpdate(String userName, String tenantId, String deductorPan,
			TCSBatchUpload batchUpload) throws InvalidKeyException, IOException, URISyntaxException, StorageException {
		MultiTenantContext.setTenantId(tenantId);
		processColleteeThresholdUpdate(userName, tenantId, deductorPan, batchUpload);
	}

	private void processColleteeThresholdUpdate(String userName, String tenantId, String deductorPan,
			TCSBatchUpload batchUpload) throws InvalidKeyException, IOException, URISyntaxException, StorageException {

		MultiTenantContext.setTenantId(tenantId);
		ArrayList<CollecteeThresholdUpdateErrorDTO> errorList = new ArrayList<>();
		File collecteeDeclarationErrorFile = null;
		File file = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getFilePath());
		XSSFWorkbook workbook = new XSSFWorkbook(file.getAbsolutePath());
		try {
			CollecteeThresholdExcel collecteeData = new CollecteeThresholdExcel(workbook);
			Long dataRowsCount = collecteeData.getDataRowsCount();

			batchUpload.setMismatchCount(0L);
			batchUpload.setRowsCount(dataRowsCount);
			Long errorCount = 0L;
			Long duplicateCount = 0L;

			for (int rowIndex = 1; rowIndex <= dataRowsCount; rowIndex++) {
				Optional<CollecteeThresholdUpdateErrorDTO> errorDTO = Optional.empty();
				boolean isValid = true;
				try {
					errorDTO = collecteeData.validate(rowIndex);
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
						CollecteeThresholdUpdate collecteethresholdData = collecteeData.get(rowIndex);
						CollecteeThresholdUpdateErrorDTO collecteeErrorDTO = collecteeData.getErrorDTO(rowIndex);
						if (StringUtils.isBlank(collecteeErrorDTO.getReason())) {
							collecteeErrorDTO.setReason("");
						}
						// get all collectee master records.
						List<CollecteeMaster> collecteeMasterList = collecteeMasterDAO
								.getAllCollecteeByCodes(deductorPan, collecteethresholdData.getCollecteeCode().trim());
						if (collecteeMasterList.isEmpty()) {
							collecteeErrorDTO.setReason(
									collecteeErrorDTO.getReason() + "Collectee code not avalible in collectee master "
											+ collecteethresholdData.getCollecteeCode() + "\n");
							isValid = false;
						}

						if (isValid) {
							CollecteeNoiThresholdLedger collecteeNoiThresholdLedger = new CollecteeNoiThresholdLedger();
							collecteeNoiThresholdLedger.setAmountUtilized(collecteethresholdData.getThresholdAmount());
							collecteeNoiThresholdLedger.setThresholdReached(false);
							if (collecteethresholdData.getThresholdAmount()
									.compareTo(BigDecimal.valueOf(5000000)) >= 0) {
								collecteeNoiThresholdLedger.setThresholdReached(true);
							}
							collecteeNoiThresholdLedger.setModifiedDate(new Date());
							collecteeNoiThresholdLedger.setModifiedBy(userName);
							collecteeNoiThresholdLedger.setCorrectedBy(userName);
							collecteeNoiThresholdLedger.setCorrectedDate(new Date());
							collecteeNoiThresholdLedger.setYear(CommonUtil.getAssessmentYear(null));
							collecteeNoiThresholdLedger.setCollectorPan(deductorPan);
							if (StringUtils.isNotBlank(collecteethresholdData.getCollecteePan())) {
								collecteeNoiThresholdLedger.setCollecteePan(collecteethresholdData.getCollecteePan());
								collecteeNoiThresholdLedgerDAO.updateByCollecteePan(collecteeNoiThresholdLedger);
							} else {
								collecteeNoiThresholdLedger.setCollecteeCode(collecteethresholdData.getCollecteeCode());
								collecteeNoiThresholdLedgerDAO.updateByCollecteeCode(collecteeNoiThresholdLedger);
							}
						}

						if (!isValid) {
							++errorCount;
							errorList.add(collecteeErrorDTO);
						}
					} catch (Exception e) {
						logger.error("Unable to process row number " + rowIndex + " due to " + e.getMessage(), e);
						CollecteeThresholdUpdateErrorDTO problematicDataError = collecteeData.getErrorDTO(rowIndex);
						if (StringUtils.isBlank(problematicDataError.getReason())) {
							problematicDataError.setReason(
									"Unable to save data for row number " + rowIndex + " due to : " + e.getMessage());
						}
						errorList.add(problematicDataError);
						++errorCount;
					}
				}
			} // for 1

			if (!errorList.isEmpty()) {
				collecteeDeclarationErrorFile = prepareThresholdUpdateErrorFile(file.getName(),
						batchUpload.getCollectorMasterTan(), deductorPan, errorList,
						new ArrayList<>(collecteeData.getHeaders()));
			}

			batchUpload.setSuccessCount(dataRowsCount);
			batchUpload.setFailedCount(errorCount);
			batchUpload.setProcessed(dataRowsCount.intValue() - errorCount.intValue());
			batchUpload.setDuplicateCount(duplicateCount);
			batchUpload.setStatus("Processed");
			batchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
			batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			batchUpload.setModifiedBy(userName);

			kycDetailsTcsBatchUpload(batchUpload, null, batchUpload.getCollectorMasterTan(),
					batchUpload.getAssessmentYear(), batchUpload.getAssessmentMonth(), userName,
					collecteeDeclarationErrorFile, tenantId, batchUpload.getUploadType());

		} catch (Exception e) {
			logger.error("Exception occurred :{}", e.getMessage());
		}

	}

	/**
	 * 
	 * @param name
	 * @param collectorMasterTan
	 * @param deductorPan
	 * @param errorList
	 * @param arrayList
	 * @return
	 * @throws Exception
	 */
	private File prepareThresholdUpdateErrorFile(String originalFileName, String collectorMasterTan, String deductorPan,
			ArrayList<CollecteeThresholdUpdateErrorDTO> errorList, ArrayList<String> headers) throws Exception {
		try {
			headers.addAll(0, Excel.STANDARD_ADDITIONAL_HEADERS);
			Workbook wkBook = thresholderColletcteeXlsxReport(errorList, collectorMasterTan, deductorPan, headers);
			ByteArrayOutputStream baout = new ByteArrayOutputStream();
			wkBook.save(baout, SaveFormat.XLSX);
			File errorFile = new File(
					FilenameUtils.getBaseName(originalFileName) + "_" + UUID.randomUUID() + "_Error_Report.xlsx");
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
	 * @param deductorTan
	 * @param deductorPan
	 * @param headers
	 * @return
	 * @throws Exception
	 */
	private Workbook thresholderColletcteeXlsxReport(ArrayList<CollecteeThresholdUpdateErrorDTO> errorList,
			String deductorTan, String deductorPan, ArrayList<String> headers) throws Exception {

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);

		worksheet.getCells().importArrayList(headers, 5, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		setExtractDataForThreholder(errorList, worksheet, deductorTan, headers);

		// Style for A6 to B6 headers
		Style style1 = workbook.createStyle();
		style1.setForegroundColor(Color.fromArgb(169, 209, 142));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange1 = worksheet.getCells().createRange("A6:B6");
		headerColorRange1.setStyle(style1);

		Cell cellD6 = worksheet.getCells().get("C6");
		Style styleD6 = cellD6.getStyle();
		styleD6.setForegroundColor(Color.fromArgb(0, 0, 0));
		styleD6.setPattern(BackgroundType.SOLID);
		styleD6.getFont().setBold(true);
		styleD6.getFont().setColor(Color.getWhite());
		styleD6.setHorizontalAlignment(TextAlignmentType.CENTER);
		cellD6.setStyle(styleD6);

		// Style for D6 to G6 headers
		Style style2 = workbook.createStyle();
		style2.setForegroundColor(Color.fromArgb(252, 199, 155));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		style2.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange2 = worksheet.getCells().createRange("D6:F6");
		headerColorRange2.setStyle(style2);

		worksheet.autoFitColumns();
		worksheet.autoFitRows();
		worksheet.setGridlinesVisible(false);
		worksheet.freezePanes(0, 2, 0, 2);

		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

		String date = simpleDateFormat.format(new Date());

		List<DeductorMaster> response = deductorMasterDAO.findByDeductorPan(deductorPan);

		Cell cellA1 = worksheet.getCells().get("A1");
		cellA1.setValue("TDS Payables Error Report (Dated: " + date + ")");
		Style a1Style = cellA1.getStyle();
		a1Style.getFont().setName("Cambria");
		a1Style.getFont().setSize(14);
		a1Style.getFont().setBold(true);
		cellA1.setStyle(a1Style);

		Cell cellA2 = worksheet.getCells().get("A2");
		if (!response.isEmpty()) {
			cellA2.setValue("Client Name:" + response.get(0).getName());
		} else {
			cellA2.setValue("Client Name:" + StringUtils.EMPTY);
		}

		Style a2Style = cellA2.getStyle();
		a2Style.getFont().setName("Cambria");
		a2Style.getFont().setSize(14);
		a2Style.getFont().setBold(true);
		cellA2.setStyle(a2Style);

		// column B5 column
		Cell cellB5 = worksheet.getCells().get("B5");
		cellB5.setValue("Error/Information");
		Style b5Style = cellB5.getStyle();
		b5Style.setForegroundColor(Color.fromArgb(180, 199, 231));
		b5Style.setPattern(BackgroundType.SOLID);
		b5Style.getFont().setBold(true);
		cellB5.setStyle(b5Style);

		int maxdatacol = worksheet.getCells().getMaxDataColumn();
		int maxdatarow = worksheet.getCells().getMaxDataRow();

		String firstHeaderCellName = "A6";
		String lastHeaderCellName = "F6";
		String firstDataCellName = "A7";
		String lastDataCellName = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);

		CommonUtil.setBoardersForAsposeXlsx(worksheet, firstHeaderCellName, lastHeaderCellName, firstDataCellName,
				lastDataCellName);
		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A6:F6");
		worksheet.autoFitColumn(1);
		worksheet.autoFitRows();
		return workbook;
	}

	/**
	 * 
	 * @param errorList
	 * @param worksheet
	 * @param deductorTan
	 * @param headers
	 * @throws Exception
	 */
	private void setExtractDataForThreholder(ArrayList<CollecteeThresholdUpdateErrorDTO> errorList, Worksheet worksheet,
			String deductorTan, ArrayList<String> headers) throws Exception {
		if (!errorList.isEmpty()) {
			int dataRowsStartIndex = 6;
			int serialNumber = 0;
			for (int i = 0; i < errorList.size(); i++) {
				CollecteeThresholdUpdateErrorDTO errorDTO = errorList.get(i);
				ArrayList<String> rowData = new ArrayList<>();
				rowData = Excel.getValues(errorDTO, CollecteeThresholdExcel.fieldMappings, headers);
				rowData.set(0, StringUtils.isBlank(deductorTan) ? StringUtils.EMPTY : deductorTan);
				rowData.set(1, StringUtils.isBlank(errorDTO.getReason()) ? StringUtils.EMPTY : errorDTO.getReason());
				rowData.set(2, String.valueOf(++serialNumber));
				worksheet.getCells().importArrayList(rowData, i + dataRowsStartIndex, 0, false);
				worksheet.autoFitRow(i + dataRowsStartIndex);
				worksheet.autoFitColumn(i);
				Excel.colorCodeErrorCells(worksheet, i + dataRowsStartIndex, headers, errorDTO.getReason());
			}
		}
	}

	/**
	 * 
	 * @param userName
	 * @param tenantId
	 * @param deductorPan
	 * @param batchUpload
	 * @throws IOException
	 */
	@Async
	public void asyncProcessDeducteeThreholdExcel(String userName, String tenantId, String deductorPan,
			BatchUpload batchUpload) throws IOException {
		MultiTenantContext.setTenantId(tenantId);
		processDeducteeThreholdExcel(userName, tenantId, deductorPan, batchUpload);
	}

	/**
	 * 
	 * @param userName
	 * @param tenantId
	 * @param deductorPan
	 * @param batchUpload
	 * @throws IOException
	 */
	private void processDeducteeThreholdExcel(String userName, String tenantId, String deductorPan,
			BatchUpload batchUpload) throws IOException {
		MultiTenantContext.setTenantId(tenantId);
		ArrayList<DeducteeThresholdUpdateErrorDTO> errorList = new ArrayList<>();
		File deducteeDeclarationErrorFile = null;
		File file = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getFilePath());
		XSSFWorkbook workbook = new XSSFWorkbook(file.getAbsolutePath());
		try {
			DeducteeThresholdExcel deducteeData = new DeducteeThresholdExcel(workbook);
			Long dataRowsCount = deducteeData.getDataRowsCount();

			batchUpload.setMismatchCount(0L);
			batchUpload.setRowsCount(dataRowsCount);
			Long errorCount = 0L;
			Long duplicateCount = 0L;

			for (int rowIndex = 1; rowIndex <= dataRowsCount; rowIndex++) {
				Optional<DeducteeThresholdUpdateErrorDTO> errorDTO = Optional.empty();
				boolean isValid = true;
				try {
					errorDTO = deducteeData.validate(rowIndex);
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
						DeducteeThresholdUpdate deducteeThresholdData = deducteeData.get(rowIndex);
						DeducteeThresholdUpdateErrorDTO deducteeErrorDTO = deducteeData.getErrorDTO(rowIndex);
						if (StringUtils.isBlank(deducteeErrorDTO.getReason())) {
							deducteeErrorDTO.setReason("");
						}
						String deducteeKey = StringUtils.EMPTY;
						// deducteee key
						if (StringUtils.isNotBlank(deducteeThresholdData.getDeducteeCode())) {
							deducteeKey = deducteeThresholdData.getDeducteeCode().trim();
						} else {
							String name = deducteeThresholdData.getDeducteeName().trim().toLowerCase();
							name = name.replaceAll("[^a-z0-9 ]", "").replace(" ", "-");
							if (StringUtils.isNotBlank(deducteeThresholdData.getDeducteePan())) {
								deducteeKey = name.concat(deducteeThresholdData.getDeducteePan().trim());
							} else {
								deducteeKey = name;
							}
						}

						// get all deductee master records based on deductee key
						List<DeducteeMasterResidential> deducteeMasterList = deducteeMasterResidentialDAO
								.findAllByDeducteeKey(deductorPan, deducteeKey);
						if (deducteeMasterList.isEmpty()) {
							deducteeErrorDTO.setReason(deducteeErrorDTO.getReason()
									+ "Deductee not avalible in deductee master residential "
									+ deducteeThresholdData.getDeducteeCode() + "\n");
							isValid = false;
						}

						// get nature of payment records.
						Map<String, String> nopMap = new HashMap<>();
						nopMap.put("nop", deducteeThresholdData.getNatureOfPayment());
						List<ThresholdGroupAndNopMapping> threholdList = new ArrayList<>();
						Optional<NatureOfPaymentMaster> nop = mastersClient.getNOPBasedOnNature(nopMap).getBody()
								.getData();
						if (!nop.isPresent()) {
							deducteeErrorDTO.setReason(
									deducteeErrorDTO.getReason() + "Nature not avalible in nature of payment "
											+ deducteeThresholdData.getNatureOfPayment() + "\n");
							isValid = false;
						} else {
							threholdList = mastersClient.getThresholdNopGroupData(nop.get().getId()).getBody()
									.getData();
							if (threholdList.isEmpty()) {
								deducteeErrorDTO.setReason(deducteeErrorDTO.getReason()
										+ "Threshold limit group master not avalible " + "\n");
								isValid = false;
							}
						}

						if (isValid) {
							DeducteeNopGroup deducteeNopGroup = new DeducteeNopGroup();
							deducteeNopGroup.setAmountUtilized(deducteeThresholdData.getThresholdAmount());
							deducteeNopGroup.setThresholdReached(false);
							if (deducteeThresholdData.getThresholdAmount().compareTo(
									threholdList.get(0).getThresholdLimitGroupMaster().getThresholdAmount()) >= 0) {
								deducteeNopGroup.setThresholdReached(true);
							}
							deducteeNopGroup.setModifiedDate(new Date());
							deducteeNopGroup.setModifiedBy(userName);
							deducteeNopGroup.setCorrectedBy(userName);
							deducteeNopGroup.setCorrectedDate(new Date());
							deducteeNopGroup.setGroupNopId(
									threholdList.get(0).getThresholdLimitGroupMaster().getId().intValue());
							deducteeNopGroup.setYear(CommonUtil.getAssessmentYear(null));
							deducteeNopGroup.setDeductorPan(deductorPan);
							if (StringUtils.isNotBlank(deducteeThresholdData.getDeducteePan())) {
								deducteeNopGroup.setDeducteePan(deducteeThresholdData.getDeducteePan());
								deducteeNopGroupDAO.updateByDeducteePan(deducteeNopGroup);
							} else {
								deducteeNopGroup.setDeducteeKey(deducteeKey);
								deducteeNopGroupDAO.updateByDeducteeKey(deducteeNopGroup);
							}
						}
						if (!isValid) {
							++errorCount;
							errorList.add(deducteeErrorDTO);
						}
					} catch (Exception e) {
						logger.error("Unable to process row number " + rowIndex + " due to " + e.getMessage(), e);
						DeducteeThresholdUpdateErrorDTO problematicDataError = deducteeData.getErrorDTO(rowIndex);
						if (StringUtils.isBlank(problematicDataError.getReason())) {
							problematicDataError.setReason(
									"Unable to save data for row number " + rowIndex + " due to : " + e.getMessage());
						}
						errorList.add(problematicDataError);
						++errorCount;
					}
				}
			} // for 1

			if (!errorList.isEmpty()) {
				deducteeDeclarationErrorFile = deducteeThresholdUpdateErrorFile(file.getName(),
						batchUpload.getDeductorMasterTan(), deductorPan, errorList,
						new ArrayList<>(deducteeData.getHeaders()));
			}

			batchUpload.setSuccessCount(dataRowsCount);
			batchUpload.setFailedCount(errorCount);
			batchUpload.setProcessedCount(dataRowsCount.intValue() - errorCount.intValue());
			batchUpload.setDuplicateCount(duplicateCount);
			batchUpload.setStatus("Processed");
			batchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
			batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			batchUpload.setModifiedBy(userName);

			KycDetailsBatchUpload(batchUpload, null, batchUpload.getDeductorMasterTan(),
					batchUpload.getAssessmentYear(), batchUpload.getAssessmentMonth(), userName,
					deducteeDeclarationErrorFile, tenantId, batchUpload.getUploadType());

		} catch (Exception e) {
			logger.error("Exception occurred :{}", e.getMessage());
		}

	}

	/**
	 * 
	 * @param originalFileName
	 * @param deductorMasterTan
	 * @param deductorPan
	 * @param errorList
	 * @param headers
	 * @return
	 * @throws Exception
	 */
	private File deducteeThresholdUpdateErrorFile(String originalFileName, String deductorMasterTan, String deductorPan,
			ArrayList<DeducteeThresholdUpdateErrorDTO> errorList, ArrayList<String> headers) throws Exception {
		try {
			headers.addAll(0, Excel.STANDARD_ADDITIONAL_HEADERS);
			Workbook wkBook = thresholderDeducteeXlsxReport(errorList, deductorMasterTan, deductorPan, headers);
			ByteArrayOutputStream baout = new ByteArrayOutputStream();
			wkBook.save(baout, SaveFormat.XLSX);
			File errorFile = new File(
					FilenameUtils.getBaseName(originalFileName) + "_" + UUID.randomUUID() + "_Error_Report.xlsx");
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
	 * @param deductorMasterTan
	 * @param deductorPan
	 * @param headers
	 * @return
	 * @throws Exception
	 */
	private Workbook thresholderDeducteeXlsxReport(ArrayList<DeducteeThresholdUpdateErrorDTO> errorList,
			String deductorMasterTan, String deductorPan, ArrayList<String> headers) throws Exception {

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);

		worksheet.getCells().importArrayList(headers, 5, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		setExtractDataForDeducteeThreholder(errorList, worksheet, deductorMasterTan, headers);

		// Style for A6 to B6 headers
		Style style1 = workbook.createStyle();
		style1.setForegroundColor(Color.fromArgb(169, 209, 142));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange1 = worksheet.getCells().createRange("A6:B6");
		headerColorRange1.setStyle(style1);

		Cell cellD6 = worksheet.getCells().get("C6");
		Style styleD6 = cellD6.getStyle();
		styleD6.setForegroundColor(Color.fromArgb(0, 0, 0));
		styleD6.setPattern(BackgroundType.SOLID);
		styleD6.getFont().setBold(true);
		styleD6.getFont().setColor(Color.getWhite());
		styleD6.setHorizontalAlignment(TextAlignmentType.CENTER);
		cellD6.setStyle(styleD6);

		// Style for D6 to G6 headers
		Style style2 = workbook.createStyle();
		style2.setForegroundColor(Color.fromArgb(252, 199, 155));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		style2.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange2 = worksheet.getCells().createRange("D6:H6");
		headerColorRange2.setStyle(style2);

		worksheet.autoFitColumns();
		worksheet.autoFitRows();
		worksheet.setGridlinesVisible(false);
		worksheet.freezePanes(0, 2, 0, 2);

		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

		String date = simpleDateFormat.format(new Date());

		List<DeductorMaster> response = deductorMasterDAO.findByDeductorPan(deductorPan);

		Cell cellA1 = worksheet.getCells().get("A1");
		cellA1.setValue("TDS Payables Error Report (Dated: " + date + ")");
		Style a1Style = cellA1.getStyle();
		a1Style.getFont().setName("Cambria");
		a1Style.getFont().setSize(14);
		a1Style.getFont().setBold(true);
		cellA1.setStyle(a1Style);

		Cell cellA2 = worksheet.getCells().get("A2");
		if (!response.isEmpty()) {
			cellA2.setValue("Client Name:" + response.get(0).getName());
		} else {
			cellA2.setValue("Client Name:" + StringUtils.EMPTY);
		}

		Style a2Style = cellA2.getStyle();
		a2Style.getFont().setName("Cambria");
		a2Style.getFont().setSize(14);
		a2Style.getFont().setBold(true);
		cellA2.setStyle(a2Style);

		// column B5 column
		Cell cellB5 = worksheet.getCells().get("B5");
		cellB5.setValue("Error/Information");
		Style b5Style = cellB5.getStyle();
		b5Style.setForegroundColor(Color.fromArgb(180, 199, 231));
		b5Style.setPattern(BackgroundType.SOLID);
		b5Style.getFont().setBold(true);
		cellB5.setStyle(b5Style);

		int maxdatacol = worksheet.getCells().getMaxDataColumn();
		int maxdatarow = worksheet.getCells().getMaxDataRow();

		String firstHeaderCellName = "A6";
		String lastHeaderCellName = "H6";
		String firstDataCellName = "A7";
		String lastDataCellName = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);

		CommonUtil.setBoardersForAsposeXlsx(worksheet, firstHeaderCellName, lastHeaderCellName, firstDataCellName,
				lastDataCellName);
		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A6:H6");
		worksheet.autoFitColumn(1);
		worksheet.autoFitRows();
		return workbook;
	}

	/**
	 * 
	 * @param errorList
	 * @param worksheet
	 * @param deductorMasterTan
	 * @param headers
	 * @throws Exception
	 */
	private void setExtractDataForDeducteeThreholder(ArrayList<DeducteeThresholdUpdateErrorDTO> errorList,
			Worksheet worksheet, String deductorMasterTan, ArrayList<String> headers) throws Exception {
		if (!errorList.isEmpty()) {
			int dataRowsStartIndex = 6;
			int serialNumber = 0;
			for (int i = 0; i < errorList.size(); i++) {
				DeducteeThresholdUpdateErrorDTO errorDTO = errorList.get(i);
				ArrayList<String> rowData = new ArrayList<>();
				rowData = Excel.getValues(errorDTO, DeducteeThresholdExcel.fieldMappings, headers);
				rowData.set(0, StringUtils.isBlank(deductorMasterTan) ? StringUtils.EMPTY : deductorMasterTan);
				rowData.set(1, StringUtils.isBlank(errorDTO.getReason()) ? StringUtils.EMPTY : errorDTO.getReason());
				rowData.set(2, String.valueOf(++serialNumber));
				worksheet.getCells().importArrayList(rowData, i + dataRowsStartIndex, 0, false);
				worksheet.autoFitRow(i + dataRowsStartIndex);
				worksheet.autoFitColumn(i);
				Excel.colorCodeErrorCells(worksheet, i + dataRowsStartIndex, headers, errorDTO.getReason());
			}
		}
	}

	/**
	 * 
	 * @param deductorPan
	 * @param deductorTan
	 * @param year
	 * @return
	 */
	public List<CollecteeDeclaration> getAllTcsDeclaration(String deductorPan, String deductorTan, int year) {
		return collecteeDeclarationDAO.findAll(deductorPan, deductorTan, year);
	}

	/**
	 * 
	 * @param deductorPan
	 * @param deductorTan
	 * @param year
	 * @return
	 */
	public List<DeducteeDeclaration> getAllTdsDeclaration(String deductorPan, String deductorTan, int year) {
		return deducteeDeclarationDAO.findAll(deductorPan, deductorTan, year);
	}

	/**
	 * 
	 * @param deductorPan
	 * @param deductorTan
	 * @param year
	 * @return
	 */
	public List<ShareholderDeclaration> getAllShareholderDeclaration(String deductorPan, String deductorTan, int year) {
		return shareholderDeclarationDAO.findAll(deductorPan, deductorTan, year);
	}

	/**
	 * 
	 * @param deductorPan
	 * @param deductorTan
	 * @param tenantId
	 * @param type
	 * @param blodUrl
	 * @throws IOException
	 */
	public void declaration(String deductorPan, String deductorTan, String tenantId, String type, String blobUrl,
			String userName) throws IOException {
		int year = CommonUtil.getAssessmentYear(null);
		Biff8EncryptionKey.setCurrentUserPassword("password");
		Workbook workbook;
		try {
			logger.info("blobUrl file path : {}", blobUrl);
			File file = blobStorageService.getFileFromBlobUrl(tenantId, blobUrl);
			workbook = new Workbook(file.getAbsolutePath());
			Worksheet worksheet = workbook.getWorksheets().get(0);
			worksheet.getCells().deleteRows(0, 4);
			ByteArrayOutputStream baout = new ByteArrayOutputStream();
			workbook.save(baout, SaveFormat.CSV);
			File xlsxInvoiceFile = new File("TestCsvFile");
			FileUtils.writeByteArrayToFile(xlsxInvoiceFile, baout.toByteArray());
			CsvReader csvReader = new CsvReader();
			csvReader.setContainsHeader(true);
			CsvContainer csv = csvReader.read(xlsxInvoiceFile, StandardCharsets.UTF_8);

			List<DeducteeDeclaration> batchSaveDeducteeDeclarationList = new ArrayList<>();
			List<CollecteeDeclaration> batchSaveCollecteeDeclarationList = new ArrayList<>();
			List<ShareholderDeclaration> batchSaveShareholderDeclarationList = new ArrayList<>();
			List<DeducteeDeclaration> batchUpdateDeducteeDeclarationList = new ArrayList<>();
			List<CollecteeDeclaration> batchUpdateCollecteeDeclarationList = new ArrayList<>();
			List<ShareholderDeclaration> batchUpdateShareholderDeclarationList = new ArrayList<>();
			List<DeducteeMasterResidential> batchUpdateDeductee = new ArrayList<>();
			List<CollecteeMaster> batchUpdateCollectee = new ArrayList<>();
			List<ShareholderMasterResidential> batchUpdateShareholder = new ArrayList<>();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
			Map<String, List<DeducteeMasterResidential>> deducteeMasterMap = new HashMap<>();
			Map<Integer, List<DeducteeDeclaration>> deducteeDeclarationMap = new HashMap<>();
			Map<String, List<CollecteeMaster>> collecteeMasterMap = new HashMap<>();
			Map<Integer, List<CollecteeDeclaration>> collecteeDeclarationMap = new HashMap<>();
			Map<String, List<ShareholderMasterResidential>> shareholderMasterMap = new HashMap<>();
			Map<Integer, List<ShareholderDeclaration>> shareholderDeclarationMap = new HashMap<>();

			// All the deductee list
			List<DeducteeMasterResidential> deducteeTotalList = deducteeMasterResidentialDAO
					.findAllByDeductorPan(deductorPan);
			for (DeducteeMasterResidential deductee : deducteeTotalList) {
				String pan = deductee.getDeducteePAN();
				List<DeducteeMasterResidential> deductees = new ArrayList<>();
				if (deducteeMasterMap.get(pan) != null) {
					deductees = deducteeMasterMap.get(pan);
				}
				deductees.add(deductee);
				deducteeMasterMap.put(pan, deductees);

			}
			// Deductee declaration Map
			List<DeducteeDeclaration> deducteeDeclarationTotalList = deducteeDeclarationDAO.findAll(deductorPan,
					deductorTan, year);
			for (DeducteeDeclaration deductee : deducteeDeclarationTotalList) {
				Integer deducteeId = deductee.getDeducteeId();
				List<DeducteeDeclaration> deductees = new ArrayList<>();
				if (deducteeDeclarationMap.get(deducteeId) != null) {
					deductees = deducteeDeclarationMap.get(deducteeId);
				}
				deductees.add(deductee);
				deducteeDeclarationMap.put(deducteeId, deductees);

			}
			// All the collectee list
			List<CollecteeMaster> collecteeTotalList = collecteeMasterDAO.getCollectorPan(deductorPan);
			for (CollecteeMaster collectee : collecteeTotalList) {
				String pan = collectee.getCollecteePan();
				List<CollecteeMaster> collectees = new ArrayList<>();
				if (collecteeMasterMap.get(pan) != null) {
					collectees = collecteeMasterMap.get(pan);
				}
				collectees.add(collectee);
				collecteeMasterMap.put(pan, collectees);

			}
			// collectee declaration Map
			List<CollecteeDeclaration> collecteeDeclarationTotalList = collecteeDeclarationDAO.findAll(deductorPan,
					deductorTan, year);
			for (CollecteeDeclaration collectee : collecteeDeclarationTotalList) {
				Integer collecteeId = collectee.getCollecteeId();
				List<CollecteeDeclaration> collectees = new ArrayList<>();
				if (collecteeDeclarationMap.get(collecteeId) != null) {
					collectees = collecteeDeclarationMap.get(collecteeId);
				}
				collectees.add(collectee);
				collecteeDeclarationMap.put(collecteeId, collectees);

			}

			// All the shareholder list
			List<ShareholderMasterResidential> shareholderTotalList = shareholderMasterResidentialDAO
					.getAllResidentShareholderByPanTenantId(deductorPan);
			for (ShareholderMasterResidential shareholder : shareholderTotalList) {
				String pan = shareholder.getShareholderPan();
				List<ShareholderMasterResidential> shareholders = new ArrayList<>();
				if (shareholderMasterMap.get(pan) != null) {
					shareholders = shareholderMasterMap.get(pan);
				}
				shareholders.add(shareholder);
				shareholderMasterMap.put(pan, shareholders);

			}
			// shareholder declaration Map
			List<ShareholderDeclaration> shareholderDeclarationTotalList = shareholderDeclarationDAO
					.findAll(deductorPan, deductorTan, year);
			for (ShareholderDeclaration shareholder : shareholderDeclarationTotalList) {
				Integer shareholderId = shareholder.getShareholderId();
				List<ShareholderDeclaration> shareholders = new ArrayList<>();
				if (shareholderDeclarationMap.get(shareholderId) != null) {
					shareholders = shareholderDeclarationMap.get(shareholderId);
				}
				shareholders.add(shareholder);
				shareholderDeclarationMap.put(shareholderId, shareholders);

			}

			for (CsvRow row : csv.getRows()) {
				if (StringUtils.isNotBlank(StringUtils.join(row.getFields(), ", ").replace(",", ""))) {
					// pan
					String pan = row.getField("PAN");
					// Name
					String name = row.getField("Name");
					// PAN Allotment Date
					String panAllotmentDate = row.getField("PAN Allotment Date");
					// PAN-Aadhaar Link Status
					String panAadhaarLinkStatus = StringUtils.isNotBlank(row.getField("PAN-Aadhaar Link Status"))
							? row.getField("PAN-Aadhaar Link Status")
							: null;
					// Specified Person u/s 206AB & 206CCA
					String specifiedPerson = row.getField("Specified Person u/s 206AB & 206CCA");

					if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(pan)) {
						String rateType = StringUtils.EMPTY;
						if ("Invalid PAN".equalsIgnoreCase(name)) {
							// Yes- Pan Invalid - No Pan No ITR
							rateType = "NO PAN AND ITR";
						} else if (StringUtils.isNotBlank(specifiedPerson) && "No".equalsIgnoreCase(specifiedPerson)
								&& !"Invalid PAN".equalsIgnoreCase(name)) {
							rateType = "NORMAL RATE";
						} // else if ("No".equalsIgnoreCase(specifiedPerson) && "Invalid
							// PAN".equalsIgnoreCase(name)) {
							// NO- Pan invalid - No PAN rate
							// rateType = "TCS - Higher rate - No PAN";
							// }
						else if (StringUtils.isNotBlank(specifiedPerson) && "Yes".equalsIgnoreCase(specifiedPerson)
								&& !"Invalid PAN".equalsIgnoreCase(name)) {
							// Yes - pan Valid - No ITR
							rateType = "NO ITR";
						}
						if ("VENDOR".equalsIgnoreCase(type)) {
							// get deductee master details based on pan.
							List<DeducteeMasterResidential> deducteeList = deducteeMasterMap.get(pan);

							if (deducteeList != null && !deducteeList.isEmpty()) {
								for (DeducteeMasterResidential deductee : deducteeList) {
									DeducteeMasterResidential deducteeMaser = new DeducteeMasterResidential();
									deducteeMaser.setDeducteeMasterId(deductee.getDeducteeMasterId());
									deducteeMaser.setPanAadhaarLinkStatus(panAadhaarLinkStatus);
									deducteeMaser.setModifiedBy(userName);
									deducteeMaser.setModifiedDate(new Date());
									deducteeMaser.setNameAsPerTraces(name.trim());
									if (StringUtils.isNotBlank(panAllotmentDate) && !panAllotmentDate.equals("-")) {
										String date = panAllotmentDate;// .replace("/", "-");
										Date panVerifiedDate = simpleDateFormat.parse(date);
										deducteeMaser.setPanVerifiedDate(new Date());
										deducteeMaser.setPanAllotmentDate(panVerifiedDate);
									}
									if ("Invalid PAN".equalsIgnoreCase(name)) {
										deducteeMaser.setPanStatus("invalid");
									} else {
										deducteeMaser.setPanStatus("valid");
									}
									batchUpdateDeductee.add(deducteeMaser);

									List<DeducteeDeclaration> deducteeDeclarationList = deducteeDeclarationMap
											.get(deductee.getDeducteeMasterId());
									if (deducteeDeclarationList != null && !deducteeDeclarationList.isEmpty()) {
										for (DeducteeDeclaration declaration : deducteeDeclarationList) {
											declaration.setActive(false);
											declaration.setApplicableTo(subtractDay(new Date()));
											declaration.setModifiedBy(userName);
											declaration.setModifiedDate(new Date());
											batchUpdateDeducteeDeclarationList.add(declaration);
										}
									}
									DeducteeDeclaration deducteeDeclaration = new DeducteeDeclaration();
									deducteeDeclaration.setActive(true);
									deducteeDeclaration.setCreatedBy(userName);
									deducteeDeclaration.setCreatedDate(new Timestamp(new Date().getTime()));
									deducteeDeclaration.setModifiedBy(userName);
									deducteeDeclaration.setModifiedDate(new Timestamp(new Date().getTime()));
									deducteeDeclaration.setDeductorPan(deductorPan);
									deducteeDeclaration.setDeductorTan(deductorTan);
									deducteeDeclaration.setRateType(rateType);
									deducteeDeclaration.setTdsOrTcs("TDS");
									deducteeDeclaration.setYear(year);
									deducteeDeclaration.setDeducteeCode(deductee.getDeducteeCode());
									deducteeDeclaration.setDeducteeId(deductee.getDeducteeMasterId());
									deducteeDeclaration.setSpecifiedPerson(specifiedPerson);
									deducteeDeclaration.setApplicableFrom(new Date());
									batchSaveDeducteeDeclarationList.add(deducteeDeclaration);
								}
							}
						} else if ("CUSTOMER".equalsIgnoreCase(type)) {
							// get collectee master details based on pan.
							List<CollecteeMaster> collecteeList = collecteeMasterMap.get(pan);

							if (collecteeList != null && !collecteeList.isEmpty()) {
								for (CollecteeMaster collectee : collecteeList) {
									CollecteeMaster collecteeMaster = new CollecteeMaster();
									collecteeMaster.setId(collectee.getId());
									collecteeMaster.setPanAadhaarLinkStatus(panAadhaarLinkStatus);
									collecteeMaster.setModifiedBy(userName);
									collecteeMaster.setModifiedDate(new Date());
									collecteeMaster.setNameAsPerTraces(name.trim());
									if (StringUtils.isNotBlank(panAllotmentDate) && !panAllotmentDate.equals("-")) {
										String date = panAllotmentDate;// .replace("/", "-");
										Date panVerifiedDate = simpleDateFormat.parse(date);
										collecteeMaster.setPanVerificationDate(new Date());
										collecteeMaster.setPanAllotmentDate(panVerifiedDate);
									}
									if ("Invalid PAN".equalsIgnoreCase(name)) {
										collecteeMaster.setPanVerifyStatus(false);
									} else {
										collecteeMaster.setPanVerifyStatus(true);
									}
									batchUpdateCollectee.add(collecteeMaster);

									List<CollecteeDeclaration> collecteeDeclarationList = collecteeDeclarationMap
											.get(collectee.getId());
									CollecteeDeclaration collecteeDeclaration = new CollecteeDeclaration();
									if (collecteeDeclarationList != null && !collecteeDeclarationList.isEmpty()) {
										collecteeDeclaration.setTdsOrTcs(collecteeDeclarationList.get(0).getTdsOrTcs());
										for (CollecteeDeclaration declaration : collecteeDeclarationList) {
											declaration.setActive(false);
											declaration.setApplicableTo(subtractDay(new Date()));
											declaration.setModifiedBy(userName);
											declaration.setModifiedDate(new Date());
											batchUpdateCollecteeDeclarationList.add(declaration);
										}
									}
									collecteeDeclaration.setActive(true);
									collecteeDeclaration.setCreatedBy(userName);
									collecteeDeclaration.setCreatedDate(new Timestamp(new Date().getTime()));
									collecteeDeclaration.setModifiedBy(userName);
									collecteeDeclaration.setModifiedDate(new Timestamp(new Date().getTime()));
									collecteeDeclaration.setDeductorPan(deductorPan);
									collecteeDeclaration.setDeductorTan(deductorTan);
									collecteeDeclaration.setRateType(rateType);
									// collecteeDeclaration.setTdsOrTcs("TCS");
									collecteeDeclaration.setYear(year);
									collecteeDeclaration.setCollecteeCode(collectee.getCollecteeCode());
									collecteeDeclaration.setCollecteeId(collectee.getId());
									collecteeDeclaration.setSpecifiedPerson(specifiedPerson);
									collecteeDeclaration.setApplicableFrom(new Date());
									batchSaveCollecteeDeclarationList.add(collecteeDeclaration);
								}
							}
						} else if ("SHAREHOLDER".equalsIgnoreCase(type)) {
							// get Shareholder Master Residential details based on pan.
							List<ShareholderMasterResidential> shareholderList = shareholderMasterMap.get(pan);
							if (shareholderList != null && !shareholderList.isEmpty()) {
								for (ShareholderMasterResidential shareholder : shareholderList) {
									ShareholderMasterResidential shareholderMaster = new ShareholderMasterResidential();
									shareholderMaster.setId(shareholder.getId());
									shareholderMaster.setPanAadhaarLinkStatus(panAadhaarLinkStatus);
									shareholderMaster.setModifiedBy(userName);
									shareholderMaster.setModifiedDate(new Date());
									shareholderMaster.setNameAsPerTraces(name.trim());
									if (StringUtils.isNotBlank(panAllotmentDate) && !panAllotmentDate.equals("-")) {
										String date = panAllotmentDate;// .replace("/", "-");
										Date panVerifiedDate = simpleDateFormat.parse(date);
										shareholderMaster.setPanVerifiedDate(new Date());
										shareholderMaster.setPanAllotmentDate(panVerifiedDate);
									}
									if ("Invalid PAN".equalsIgnoreCase(name)) {
										shareholderMaster.setPanStatus("invalid");
									} else {
										shareholderMaster.setPanStatus("valid");
									}
									batchUpdateShareholder.add(shareholderMaster);

									List<ShareholderDeclaration> shareholderDeclarationList = shareholderDeclarationMap
											.get(shareholder.getId());
									if (shareholderDeclarationList != null && !shareholderDeclarationList.isEmpty()) {
										for (ShareholderDeclaration declaration : shareholderDeclarationList) {
											declaration.setActive(false);
											declaration.setApplicableTo(subtractDay(new Date()));
											declaration.setModifiedBy(userName);
											declaration.setModifiedDate(new Date());
											batchUpdateShareholderDeclarationList.add(declaration);
										}
									}
									ShareholderDeclaration shareholderDeclaration = new ShareholderDeclaration();
									shareholderDeclaration.setActive(true);
									shareholderDeclaration.setCreatedBy(userName);
									shareholderDeclaration.setCreatedDate(new Timestamp(new Date().getTime()));
									shareholderDeclaration.setModifiedBy(userName);
									shareholderDeclaration.setModifiedDate(new Timestamp(new Date().getTime()));
									shareholderDeclaration.setDeductorPan(deductorPan);
									shareholderDeclaration.setDeductorTan(deductorTan);
									shareholderDeclaration.setRateType(rateType);
									shareholderDeclaration.setTdsOrTcs("SHAREHOLDER");
									shareholderDeclaration.setYear(year);
									shareholderDeclaration.setShareholderCode(shareholder.getFolioNo());
									shareholderDeclaration.setShareholderId(shareholder.getId());
									shareholderDeclaration.setSpecifiedPerson(specifiedPerson);
									shareholderDeclaration.setApplicableFrom(new Date());
									batchSaveShareholderDeclarationList.add(shareholderDeclaration);
								}
							}
						}
					} else {
						logger.error("specified person and name and pan is mandatory");
					}
				}
			}

			// batch save deductee declaration List
			if (!batchSaveDeducteeDeclarationList.isEmpty()) {
				deducteeDeclarationDAO.batchSaveDeducteeDeclaration(batchSaveDeducteeDeclarationList, tenantId);
			}
			// batch save collectee declaration List
			if (!batchSaveCollecteeDeclarationList.isEmpty()) {
				collecteeDeclarationDAO.batchSaveCollecteeDeclaration(batchSaveCollecteeDeclarationList, tenantId);
			}
			// batch save shareholder Declaration List
			if (!batchSaveShareholderDeclarationList.isEmpty()) {
				shareholderDeclarationDAO.batchSaveShareholderDeclaration(batchSaveShareholderDeclarationList,
						tenantId);
			}
			// batch update deductee declaration List
			if (!batchUpdateDeducteeDeclarationList.isEmpty()) {
				deducteeDeclarationDAO.batchUpdateApplicableTo(batchUpdateDeducteeDeclarationList);
			}
			// batch update collectee declaration List
			if (!batchUpdateCollecteeDeclarationList.isEmpty()) {
				collecteeDeclarationDAO.batchUpdateApplicableTo(batchUpdateCollecteeDeclarationList);
			}
			// batch update shareholder Declaration List
			if (!batchUpdateShareholderDeclarationList.isEmpty()) {
				shareholderDeclarationDAO.batchUpdateApplicableTo(batchUpdateShareholderDeclarationList);
			}
			// batch update deductee master List
			if (!batchUpdateDeductee.isEmpty()) {
				deducteeDeclarationDAO.batchUpdateDeducteePanAadhaarLinkStatus(batchUpdateDeductee);
			}
			// batch update collectee master List
			if (!batchUpdateCollectee.isEmpty()) {
				collecteeDeclarationDAO.batchUpdateCollecteePanAadhaarLinkStatus(batchUpdateCollectee);
			}
			// batch update shareholder master List
			if (!batchUpdateShareholder.isEmpty()) {
				shareholderDeclarationDAO.batchUpdateShareholderPanAadhaarLinkStatus(batchUpdateShareholder);
			}
			logger.info("batch inserts done");

			// calling No PAN rate method
			logger.info("About to start No pan insertion");
			declarationNoPan(deductorPan, deductorTan, tenantId, type, userName);
			logger.info("No PAN inserion done in declaration tale");

		} catch (Exception e) {
			logger.error("Failed in processing Master and Declaration Update Process : {}", e.getMessage());
		}
	}

	public static Date subtractDay(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_MONTH, -1);
		return cal.getTime();
	}

	@Async
	public void asyncProcessShareholderDeclaration(String userName, String tenantId, String deductorPan,
			BatchUpload batchUpload) throws IOException {
		MultiTenantContext.setTenantId(tenantId);
		processShareholderDeclaration(userName, tenantId, deductorPan, batchUpload);
	}

	/**
	 * 
	 * @param userName
	 * @param tenantId
	 * @param deductorPan
	 * @param batchUpload
	 * @throws IOException
	 */
	private void processShareholderDeclaration(String userName, String tenantId, String deductorPan,
			BatchUpload batchUpload) throws IOException {
		MultiTenantContext.setTenantId(tenantId);
		ArrayList<DeclarationErrorDTO> errorList = new ArrayList<>();
		File declarationErrorFile = null;
		File file = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getFilePath());
		XSSFWorkbook workbook = new XSSFWorkbook(file.getAbsolutePath());
		try {
			ShareholderDeclarationExcel shareholderExcelData = new ShareholderDeclarationExcel(workbook);
			long dataRowsCount = shareholderExcelData.getDataRowsCount();

			batchUpload.setMismatchCount(0L);
			batchUpload.setRowsCount(dataRowsCount);
			Long errorCount = 0L;
			Long duplicateCount = 0L;

			List<ShareholderDeclaration> shareholderDeclarationList = new ArrayList<>();
			Set<String> duplicationRecords = new HashSet<>();
			List<ShareholderDeclaration> batchUpdateSharehoderDeclarationList = new ArrayList<>();
			for (int rowIndex = 1; rowIndex <= dataRowsCount; rowIndex++) {
				Optional<DeclarationErrorDTO> errorDTO = Optional.empty();
				boolean isValid = true;
				try {
					errorDTO = shareholderExcelData.validate(rowIndex);
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
						ShareholderDeclaration shareholder = shareholderExcelData.get(rowIndex);
						DeclarationErrorDTO collecteeErrorDTO = shareholderExcelData.getErrorDTO(rowIndex);
						if (StringUtils.isBlank(collecteeErrorDTO.getReason())) {
							collecteeErrorDTO.setReason("");
						}
						// get shareholder master records based on folio number.
						List<ShareholderMasterResidential> shareholderMasterList = shareholderMasterResidentialDAO
								.getResidentShareholderByFolioNumberPanTenantId(deductorPan, tenantId,
										shareholder.getFolioNumber().trim());
						if (shareholderMasterList.isEmpty()) {
							collecteeErrorDTO.setReason(collecteeErrorDTO.getReason()
									+ "Shareholder Folio Number not avalible in Shareholder master residential "
									+ shareholder.getFolioNumber() + "\n");
							isValid = false;
						}

						if (isValid) {
							List<ShareholderDeclaration> shareholderDeclarationInactivateList = shareholderDeclarationDAO
									.getAllShareholderDeclarationByCode(deductorPan, batchUpload.getDeductorMasterTan(),
											shareholderMasterList.get(0).getId(), batchUpload.getAssessmentYear());
							if (!shareholderDeclarationInactivateList.isEmpty()) {
								shareholder.setSpecifiedPerson(
										shareholderDeclarationInactivateList.get(0).getSpecifiedPerson());
								// rate type check
								if (StringUtils.isEmpty(shareholder.getRateType())) {
									shareholder.setRateType(shareholderDeclarationInactivateList.get(0).getRateType());
								} else {
									shareholder.setRateType(shareholder.getRateType());
								}
								for (ShareholderDeclaration declaration : shareholderDeclarationInactivateList) {
									declaration.setActive(false);
									declaration.setApplicableTo(subtractDay(new Date()));
									declaration.setModifiedBy(userName);
									declaration.setModifiedDate(new Date());
									batchUpdateSharehoderDeclarationList.add(declaration);
								}
							}
							shareholder.setActive(true);
							shareholder.setShareholderCode(shareholder.getFolioNumber().trim());
							shareholder.setShareholderId(shareholderMasterList.get(0).getId());
							shareholder.setTdsOrTcs("TDS");
							shareholder.setCreatedDate(new Timestamp(new Date().getTime()));
							shareholder.setCreatedBy(userName);
							shareholder.setModifiedBy(userName);
							shareholder.setModifiedDate(new Timestamp(new Date().getTime()));
							shareholder.setDeductorTan(batchUpload.getDeductorMasterTan());
							shareholder.setDeductorPan(deductorPan);
							shareholder.setYear(batchUpload.getAssessmentYear());
							shareholder.setApplicableFrom(new Date());
							String uniqueRecord = shareholder.getShareholderCode() + "_" + shareholder.getRateType()
									+ "_" + shareholder.getTdsOrTcs();

							if (duplicationRecords.contains(uniqueRecord)) {
								duplicateCount++;
							} else {
								duplicationRecords.add(uniqueRecord);
								// add deductee declaration
								shareholderDeclarationList.add(shareholder);
							}
						}

						if (!isValid) {
							++errorCount;
							errorList.add(collecteeErrorDTO);
						}
					} catch (Exception e) {
						logger.error("Unable to process row number " + rowIndex + " due to " + e.getMessage(), e);
						DeclarationErrorDTO problematicDataError = shareholderExcelData.getErrorDTO(rowIndex);
						if (StringUtils.isBlank(problematicDataError.getReason())) {
							problematicDataError.setReason(
									"Unable to save data for row number " + rowIndex + " due to : " + e.getMessage());
						}
						errorList.add(problematicDataError);
						++errorCount;
					}
				}
			} // for 1

			// Inactivating old records
			if (!batchUpdateSharehoderDeclarationList.isEmpty()) {
				shareholderDeclarationDAO.batchUpdateApplicableTo(batchUpdateSharehoderDeclarationList);
			}
			if (!shareholderDeclarationList.isEmpty()) {
				// batch save
				shareholderDeclarationDAO.batchSaveShareholderDeclaration(shareholderDeclarationList, tenantId);
			}

			if (!errorList.isEmpty()) {
				declarationErrorFile = prepareDeclarationErrorFile(file.getName(), batchUpload.getDeductorMasterTan(),
						deductorPan, errorList, new ArrayList<>(shareholderExcelData.getHeaders()),
						batchUpload.getUploadType());
			}

			batchUpload.setSuccessCount(dataRowsCount);
			batchUpload.setFailedCount(errorCount);
			batchUpload.setProcessedCount(shareholderDeclarationList.size());
			batchUpload.setDuplicateCount(duplicateCount);
			batchUpload.setStatus("Processed");
			batchUpload.setModifiedDate(new Timestamp(new Date().getTime()));
			batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			batchUpload.setModifiedBy(userName);

			KycDetailsBatchUpload(batchUpload, null, batchUpload.getDeductorMasterTan(),
					batchUpload.getAssessmentYear(), batchUpload.getAssessmentMonth(), userName, declarationErrorFile,
					tenantId, batchUpload.getUploadType());

		} catch (Exception e) {
			logger.error("Exception occurred :{}", e.getMessage());
		}
	}

	/**
	 * 
	 * @param deductorPan
	 * @param deductorTan
	 * @param tenantId
	 * @param type
	 * @param blobUrl
	 * @param userName
	 */
	public void declarationNoPan(String deductorPan, String deductorTan, String tenantId, String type,
			String userName) {
		int year = CommonUtil.getAssessmentYear(null);

		try {
			String rateType = "NO PAN AND ITR";
			List<DeducteeDeclaration> batchSaveDeducteeDeclarationList = new ArrayList<>();
			List<CollecteeDeclaration> batchSaveCollecteeDeclarationList = new ArrayList<>();
			List<ShareholderDeclaration> batchSaveShareholderDeclarationList = new ArrayList<>();

			// Deductee declaration Map
			Map<Integer, List<DeducteeDeclaration>> deducteeDeclarationMap = new HashMap<>();
			List<DeducteeDeclaration> deducteeDeclarationTotalList = deducteeDeclarationDAO.findAll(deductorPan,
					deductorTan, year);
			for (DeducteeDeclaration deductee : deducteeDeclarationTotalList) {
				Integer deducteeId = deductee.getDeducteeId();
				List<DeducteeDeclaration> deductees = new ArrayList<>();
				if (deducteeDeclarationMap.get(deducteeId) != null) {
					deductees = deducteeDeclarationMap.get(deducteeId);
				}
				deductees.add(deductee);
				deducteeDeclarationMap.put(deducteeId, deductees);
			}

			// collectee declaration Map
			Map<Integer, List<CollecteeDeclaration>> collecteeDeclarationMap = new HashMap<>();
			List<CollecteeDeclaration> collecteeDeclarationTotalList = collecteeDeclarationDAO.findAll(deductorPan,
					deductorTan, year);
			for (CollecteeDeclaration collectee : collecteeDeclarationTotalList) {
				Integer collecteeId = collectee.getCollecteeId();
				List<CollecteeDeclaration> collectees = new ArrayList<>();
				if (collecteeDeclarationMap.get(collecteeId) != null) {
					collectees = collecteeDeclarationMap.get(collecteeId);
				}
				collectees.add(collectee);
				collecteeDeclarationMap.put(collecteeId, collectees);
			}
			// shareholder declaration Map
			Map<Integer, List<ShareholderDeclaration>> shareholderDeclarationMap = new HashMap<>();
			List<ShareholderDeclaration> shareholderDeclarationTotalList = shareholderDeclarationDAO
					.findAll(deductorPan, deductorTan, year);
			for (ShareholderDeclaration shareholder : shareholderDeclarationTotalList) {
				Integer shareholderId = shareholder.getShareholderId();
				List<ShareholderDeclaration> shareholders = new ArrayList<>();
				if (shareholderDeclarationMap.get(shareholderId) != null) {
					shareholders = shareholderDeclarationMap.get(shareholderId);
				}
				shareholders.add(shareholder);
				shareholderDeclarationMap.put(shareholderId, shareholders);
			}

			if ("VENDOR".equalsIgnoreCase(type)) {
				// get deductee master details based on deductorPan.
				List<DeducteeMasterResidential> deducteeList = deducteeMasterResidentialDAO
						.getDeducteesByNoPan(deductorPan);
				if (!deducteeList.isEmpty()) {
					for (DeducteeMasterResidential deductee : deducteeList) {
						DeducteeDeclaration deducteeDeclaration = new DeducteeDeclaration();
						List<DeducteeDeclaration> deducteeDeclarationList = deducteeDeclarationMap
								.get(deductee.getDeducteeMasterId());
						if (deducteeDeclarationList == null) {
							deducteeDeclaration.setActive(true);
							deducteeDeclaration.setCreatedBy(userName);
							deducteeDeclaration.setCreatedDate(new Timestamp(new Date().getTime()));
							deducteeDeclaration.setModifiedBy(userName);
							deducteeDeclaration.setModifiedDate(new Timestamp(new Date().getTime()));
							deducteeDeclaration.setDeductorPan(deductorPan);
							deducteeDeclaration.setDeductorTan(deductorTan);
							deducteeDeclaration.setRateType(rateType);
							deducteeDeclaration.setTdsOrTcs("TDS");
							deducteeDeclaration.setYear(year);
							deducteeDeclaration.setDeducteeCode(deductee.getDeducteeCode());
							deducteeDeclaration.setDeducteeId(deductee.getDeducteeMasterId());
							deducteeDeclaration.setApplicableFrom(new Date());
							batchSaveDeducteeDeclarationList.add(deducteeDeclaration);
						}
					}
				}
			} else if ("CUSTOMER".equalsIgnoreCase(type)) {
				// get collectee master details based on collectorPan.
				List<CollecteeMaster> collecteeList = collecteeMasterDAO.getCollectorNoPan(deductorPan);
				if (!collecteeList.isEmpty()) {
					for (CollecteeMaster collectee : collecteeList) {
						CollecteeDeclaration collecteeDeclaration = new CollecteeDeclaration();
						List<CollecteeDeclaration> collecteeDeclarationList = collecteeDeclarationMap
								.get(collectee.getId());
						if (collecteeDeclarationList == null) {
							collecteeDeclaration.setActive(true);
							collecteeDeclaration.setCreatedBy(userName);
							collecteeDeclaration.setCreatedDate(new Timestamp(new Date().getTime()));
							collecteeDeclaration.setModifiedBy(userName);
							collecteeDeclaration.setModifiedDate(new Timestamp(new Date().getTime()));
							collecteeDeclaration.setDeductorPan(deductorPan);
							collecteeDeclaration.setDeductorTan(deductorTan);
							collecteeDeclaration.setRateType(rateType);
							collecteeDeclaration.setTdsOrTcs("TCS");
							collecteeDeclaration.setYear(year);
							collecteeDeclaration.setCollecteeCode(collectee.getCollecteeCode());
							collecteeDeclaration.setCollecteeId(collectee.getId());
							collecteeDeclaration.setApplicableFrom(new Date());
							batchSaveCollecteeDeclarationList.add(collecteeDeclaration);
						}

					}
				}
			} else if ("SHAREHOLDER".equalsIgnoreCase(type)) {
				// get Shareholder Master Residential details based on pan.
				List<ShareholderMasterResidential> shareholderList = shareholderMasterResidentialDAO
						.getAllShareholderNoPan(deductorPan);
				if (!shareholderList.isEmpty()) {
					for (ShareholderMasterResidential shareholder : shareholderList) {
						ShareholderDeclaration shareholderDeclaration = new ShareholderDeclaration();
						List<ShareholderDeclaration> shareholderDeclarationList = shareholderDeclarationMap
								.get(shareholder.getId());
						if (shareholderDeclarationList == null) {
							shareholderDeclaration.setActive(true);
							shareholderDeclaration.setCreatedBy(userName);
							shareholderDeclaration.setCreatedDate(new Timestamp(new Date().getTime()));
							shareholderDeclaration.setModifiedBy(userName);
							shareholderDeclaration.setModifiedDate(new Timestamp(new Date().getTime()));
							shareholderDeclaration.setDeductorPan(deductorPan);
							shareholderDeclaration.setDeductorTan(deductorTan);
							shareholderDeclaration.setRateType(rateType);
							shareholderDeclaration.setTdsOrTcs("SHAREHOLDER");
							shareholderDeclaration.setYear(year);
							shareholderDeclaration.setShareholderCode(shareholder.getFolioNo());
							shareholderDeclaration.setShareholderId(shareholder.getId());
							shareholderDeclaration.setApplicableFrom(new Date());
							batchSaveShareholderDeclarationList.add(shareholderDeclaration);
						}

					}
				}
			} else {
				logger.error("pan is not empty");
			}

			// batch save deductee declaration List
			if (!batchSaveDeducteeDeclarationList.isEmpty()) {
				deducteeDeclarationDAO.batchSaveDeducteeDeclaration(batchSaveDeducteeDeclarationList, tenantId);
			}
			// batch save collectee declaration List
			if (!batchSaveCollecteeDeclarationList.isEmpty()) {
				collecteeDeclarationDAO.batchSaveCollecteeDeclaration(batchSaveCollecteeDeclarationList, tenantId);
			}
			// batch save shareholder Declaration List
			if (!batchSaveShareholderDeclarationList.isEmpty()) {
				shareholderDeclarationDAO.batchSaveShareholderDeclaration(batchSaveShareholderDeclarationList,
						tenantId);
			}

		} catch (Exception e) {
			logger.error("bolb url not found in blob storage : {}", e.getMessage());
		}
	}

}
