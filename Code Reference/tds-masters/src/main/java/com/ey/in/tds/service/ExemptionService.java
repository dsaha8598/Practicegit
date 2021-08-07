package com.ey.in.tds.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
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
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.MasterBatchUpload;
import com.ey.in.tds.common.domain.dividend.DividendDeductorType;
import com.ey.in.tds.common.domain.dividend.DividendInstrumentsMapping;
import com.ey.in.tds.common.domain.dividend.ShareholderCategory;
import com.ey.in.tds.common.domain.dividend.ShareholderExemptedCategory;
import com.ey.in.tds.common.dto.ExemptionDTO;
import com.ey.in.tds.common.dto.ExemptionExcelDTO;
import com.ey.in.tds.common.service.Sha256SumService;
import com.ey.in.tds.common.util.MasterBlobStorage;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.dto.ExemptionErrorDTO;
import com.ey.in.tds.repository.DividendStaticDataRepository;
import com.ey.in.tds.repository.ExemptionRepository;
import com.ey.in.tds.repository.MasterBatchUploadRepository;
import com.ey.in.tds.service.util.excel.ExemptionExcel;
import com.microsoft.azure.storage.StorageException;

@Service
public class ExemptionService {

	private final Logger logger = LoggerFactory.getLogger(ExemptionService.class);
	@Autowired
	private ExemptionRepository exemptionRepository;

	@Autowired
	private Sha256SumService sha256SumService;

	@Autowired
	private MasterBatchUploadService masterBatchUploadService;

	@Autowired
	private MasterBatchUploadRepository masterBatchUploadRepository;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private DividendStaticDataRepository dividendStaticDataRepository;
	
	@Autowired
	private MasterBlobStorage blob;

	public List<ExemptionDTO> getAllExemption() {
		List<ExemptionDTO> list= exemptionRepository.getAllShareholderExemption();
       Collections.reverse(list);
       return list;
	}

	@Transactional
	public MasterBatchUpload saveFileData(MultipartFile file, Integer assesssmentYear, Integer assessmentMonth,
			String userName, String uploadType)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException {

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
			int headersCount = ExemptionExcel.getNonEmptyCellsCount(headerRow);
			logger.info("Column header count :{}", headersCount);
			MasterBatchUpload masterBatchUpload = new MasterBatchUpload();
			if (headersCount != ExemptionExcel.fieldMappings.size()) {
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
			if (headersCount == ExemptionExcel.fieldMappings.size()) {
				final Long batchUploadId = masterBatchUpload.getId();
				CompletableFuture.runAsync(() -> this.processExemptionList(workbook, assesssmentYear, assessmentMonth,
						userName, batchUploadId, uploadType));
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

	private void processExemptionList(XSSFWorkbook workbook, Integer assesssmentYear, Integer assessmentMonth,
			String userName, Long batchuploadID, String uploadType) {
		MultiTenantContext.setTenantId("master");
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRES_NEW);
		transactionTemplate.executeWithoutResult((status) -> {
			try {
				ExemptionExcel excelData = new ExemptionExcel(workbook);
				long dataRowsCount = excelData.getDataRowsCount();
				/*
				 * masterBatchUpload.setSha256sum(sha256);
				 * masterBatchUpload.setMismatchCount(0L);
				 * 
				 * masterBatchUpload.setRowsCount(dataRowsCount);
				 */
				Long errorCount = 0L;
				Long duplicateCount = 0L;
				String error = null;
				Integer successRecordsCount = 0;
				ArrayList<ExemptionErrorDTO> errorList = new ArrayList<>();
				Map<String, Long> shareHolderCatagoryMap = new HashMap<>();
				Map<String, Long> deductorTypeMap = new HashMap<>();

				List<ShareholderCategory> listCatagory = dividendStaticDataRepository.findAllShareholderCategory();
				List<DividendDeductorType> listDeductor = dividendStaticDataRepository.findAllDividendDeductorTypes();

				listCatagory.stream().forEach(n -> shareHolderCatagoryMap.put(n.getName().trim(), n.getId()));
				listDeductor.stream().forEach(n -> deductorTypeMap.put(n.getName().trim(), n.getId()));

				for (int rowIndex = 1; rowIndex <= dataRowsCount; rowIndex++) {
					Optional<ExemptionErrorDTO> errorDTO = null;
					String errorReason = "";
					boolean isValid = true;

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

							ExemptionExcelDTO exemption = excelData.get(rowIndex);
							Long catagoryId = null;
							Long deductorTypeId = null;
							/*
							 * dividendStaticDataRepository.getAllDividendInstrumentsMapping(Long
							 * dividendDeductorTypeId, Long shareholderCategoryId, String
							 * residentialStatus);
							 */

							if (!deductorTypeMap.containsKey(exemption.getDeductorType().trim())) {
								errorReason = errorReason + "Invalid Deductor Type";
							} else {

								deductorTypeId = deductorTypeMap.get(exemption.getDeductorType().trim());
								if (!shareHolderCatagoryMap.containsKey(exemption.getShareHolderCatagory().trim())) {
									// if not present then creating a shareholder catagory
									ShareholderCategory catagory = new ShareholderCategory();
									catagory.setName(exemption.getShareHolderCatagory().trim());
									catagory.setExempted(true);
									catagory.setActive(true);
									catagory.setOrderId(new Random().nextInt(1000));
									catagoryId = dividendStaticDataRepository.saveShareHolderCatagory(catagory);
								} else {
									catagoryId = shareHolderCatagoryMap.get(exemption.getShareHolderCatagory().trim());
								}

								List<DividendInstrumentsMapping> list = dividendStaticDataRepository
										.getAllDividendInstrumentsMapping(deductorTypeId, catagoryId,
												exemption.getResidentialStatus().equals("Resident")?"RES":"NR");

								if (list.isEmpty()) {
									saveExemptionRecord(deductorTypeId, catagoryId, exemption);
								} else {
									errorReason = errorReason + "Duplicate Record";
								}
							}

							if (StringUtils.isNotBlank(errorReason)) {
								ExemptionErrorDTO dto = excelData.getErrorDTO(rowIndex);
								dto.setReason(errorReason);
								errorList.add(dto);
								errorCount++;
							} else {
								successRecordsCount++;
							}

						} catch (Exception e) {
							logger.error("Unable to process row number " + rowIndex + " due to " + e.getMessage(), e);
							ExemptionErrorDTO problematicDataError = excelData.getErrorDTO(rowIndex);
							if (StringUtils.isBlank(problematicDataError.getReason())) {
								problematicDataError.setReason(
										"Unable to process row number " + rowIndex + " due to : " + e.getMessage());
							}
							errorList.add(problematicDataError);
							++errorCount;
						}
					} // else block processing records with all mandatory fields

				} // for loop

				String errorFilePath=null;
				if(!errorList.isEmpty()) {
					File file=prepareNonResidentDeducteesErrorFile(uploadType, errorList, excelData.getHeaders());
					errorFilePath=blob.uploadExcelToBlobWithFile(file);
					
				}
				MasterBatchUpload masterBatchUpload = masterBatchUploadRepository.getOne(batchuploadID);
				masterBatchUpload.setDuplicateCount(duplicateCount);
				masterBatchUpload.setFailedCount(errorCount);
				masterBatchUpload.setStatus("Processed");
				masterBatchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
				masterBatchUpload.setProcessed(successRecordsCount);
				masterBatchUpload.setErrorFilePath("");
				masterBatchUpload.setRowsCount(dataRowsCount);
				masterBatchUpload.setErrorFilePath(errorFilePath);
				masterBatchUploadRepository.save(masterBatchUpload);

			} catch (Exception e) {
				logger.info("Exception occured while processing file " + e + "{}");
			}
		});
	}

	@Transactional
	public void saveExemptionRecord(Long deductorTypeId, Long catagoryId, ExemptionExcelDTO exemption) {
		DividendDeductorType deductorType = dividendStaticDataRepository.findDividendDeductorTypeById(deductorTypeId)
				.get();
		ShareholderCategory catagory = dividendStaticDataRepository.findShareholderCategoryById(catagoryId).get();

		ShareholderExemptedCategory sme = new ShareholderExemptedCategory();
		sme.setDividendDeductorType(deductorType);
		sme.setExempted(true);
		sme.setShareholderCategory(catagory);
		dividendStaticDataRepository.saveShareholderExemptedCategory(sme);

		DividendInstrumentsMapping dim = new DividendInstrumentsMapping();
		dim.setActive(true);
		dim.setDividendDeductorType(deductorType);
		dim.setResidentialStatus(exemption.getResidentialStatus().equals("Resident")?"RES":"NR");
		dim.setSection(exemption.getSection());
		dim.setShareholderCategory(catagory);
		dividendStaticDataRepository.saveDividendInstrumentsMapping(dim);

	}
	
	public File prepareNonResidentDeducteesErrorFile(String originalFileName, 
			ArrayList<ExemptionErrorDTO> errorList, List<String> headers)
			throws Exception {
		ArrayList<String> modifiedHEaders=new ArrayList<>();
		headers.stream().forEach(n->modifiedHEaders.add(n));
		modifiedHEaders.addAll(0, Excel.STANDARD_ADDITIONAL_HEADERS);
		Workbook wkBook = deducteeNonResidentXlsxReport(errorList,  modifiedHEaders);
		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		wkBook.save(baout, SaveFormat.XLSX);
		File deducteeErrorsFile = new File(
				FilenameUtils.getBaseName(originalFileName) + "_" + UUID.randomUUID() + "_Error_Report.xlsx");
		FileUtils.writeByteArrayToFile(deducteeErrorsFile, baout.toByteArray());
		baout.close();
		return deducteeErrorsFile;
	}
	
	public Workbook deducteeNonResidentXlsxReport(
			ArrayList<ExemptionErrorDTO> deducteeNonResidentialErrorReportsCsvList,
			ArrayList<String> headerNames) throws Exception {

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);

		headerNames.remove(0);
		worksheet.getCells().importArrayList(headerNames, 5, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		setExtractDataForDeducteeNonResident(deducteeNonResidentialErrorReportsCsvList, worksheet, 
				headerNames);

		// Style for A6 to D6 headers
		Style style1 = workbook.createStyle();
		style1.setForegroundColor(Color.fromArgb(169, 209, 142));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange1 = worksheet.getCells().createRange(5, 0, 1, 2);
		headerColorRange1.setStyle(style1);

		Cell cellD6 = worksheet.getCells().get("A6");
		Style styleD6 = cellD6.getStyle();
		styleD6.setForegroundColor(Color.fromArgb(0, 0, 0));
		styleD6.setPattern(BackgroundType.SOLID);
		styleD6.getFont().setBold(true);
		styleD6.getFont().setColor(Color.getWhite());
		styleD6.setHorizontalAlignment(TextAlignmentType.CENTER);
		cellD6.setStyle(styleD6);

		// Style for E6 to AT6 headers
		Style style2 = workbook.createStyle();
		style2.setForegroundColor(Color.fromArgb(252, 199, 155));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		style2.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange2 = worksheet.getCells().createRange(5, 2, 1, headerNames.size() - 2);
		headerColorRange2.setStyle(style2);

		worksheet.autoFitColumns();
		worksheet.autoFitRows();
		worksheet.setGridlinesVisible(false);
		worksheet.freezePanes(0, 2, 0, 2);

		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

		String date = simpleDateFormat.format(new Date());


		Cell cellA1 = worksheet.getCells().get("A1");
		cellA1.setValue("Exemption List Error Report (Dated: " + date + ")");
		Style a1Style = cellA1.getStyle();
		a1Style.getFont().setName("Cambria");
		a1Style.getFont().setSize(14);
		a1Style.getFont().setBold(true);
		cellA1.setStyle(a1Style);

		Cell cellA2 = worksheet.getCells().get("A2");
			cellA2.setValue("Super Admin : EY India");

		Style a2Style = cellA2.getStyle();
		a2Style.getFont().setName("Cambria");
		a2Style.getFont().setSize(14);
		a2Style.getFont().setBold(true);
		cellA2.setStyle(a2Style);

		// column B5 column
		Cell cellB5 = worksheet.getCells().get("B5");
		cellB5.setValue("Error/Information codes");
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
	
	private void setExtractDataForDeducteeNonResident(
			ArrayList<ExemptionErrorDTO> errorDTOs, Worksheet worksheet,
			List<String> headerNames) throws Exception {

		if (!errorDTOs.isEmpty()) {
			int dataRowsStartIndex = 6;
			for (int i = 0; i < errorDTOs.size(); i++) {
				ExemptionErrorDTO errorDTO = errorDTOs.get(i);
				ArrayList<String> rowData = Excel.getValues(errorDTO, ExemptionExcel.fieldMappings,
						headerNames);
				rowData.set(0, (1+i)+"");
				rowData.set(1, StringUtils.isBlank(errorDTO.getReason()) ? StringUtils.EMPTY : errorDTO.getReason());
				worksheet.getCells().importArrayList(rowData, i + dataRowsStartIndex, 0, false);
				worksheet.autoFitRow(i + dataRowsStartIndex);
				worksheet.autoFitColumn(i);
				Excel.colorCodeErrorCells(worksheet, i + dataRowsStartIndex, headerNames, errorDTO.getReason());
			}
		}
	}


	
	
}
