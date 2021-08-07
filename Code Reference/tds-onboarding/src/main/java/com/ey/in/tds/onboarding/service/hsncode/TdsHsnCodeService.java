package com.ey.in.tds.onboarding.service.hsncode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
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
import com.ey.in.tds.common.domain.NatureOfPaymentMaster;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.BatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.dto.CommonDTO;
import com.ey.in.tds.common.dto.NatureOfPaymentMasterDTO;
import com.ey.in.tds.common.dto.TdsHsnCodeDTO;
import com.ey.in.tds.common.dto.TdsHsnCodeErrorReportDTO;
import com.ey.in.tds.common.dto.TdsHsnCodeExcelDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.TdsHsnCode;
import com.ey.in.tds.common.repository.common.PagedData;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.common.service.Sha256SumService;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.feign.client.MastersClient;
import com.ey.in.tds.jdbc.dao.DeductorMasterDAO;
import com.ey.in.tds.jdbc.dao.TdsHsnCodeDAO;
import com.ey.in.tds.onboarding.service.util.excel.deductee.TdsHsnCodeExcel;
import com.microsoft.azure.storage.StorageException;

@Service
public class TdsHsnCodeService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TdsHsnCodeDAO tdsHsnCodeDAO;

	@Autowired
	private Sha256SumService sha256SumService;

	@Autowired
	private BatchUploadDAO batchUploadDAO;

	@Autowired
	private BlobStorage blob;

	@Autowired
	private MastersClient mastersClient;

	@Autowired
	private DeductorMasterDAO deductorMasterDAO;

	@Autowired
	private MastersClient masterClient;

	/**
	 * 
	 * @param file
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @param userName
	 * @param tan
	 * @param deductorPan
	 * @param tenantId
	 * @return
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 */
	@Transactional
	public BatchUpload saveFileData(MultipartFile file, Integer assesssmentYear, Integer assessmentMonth,
			String userName, String tan, String deductorPan, String tenantId)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {

		String sha256 = sha256SumService.getSHA256Hash(file);
		if (isAlreadyProcessed(sha256)) {
			BatchUpload batchUpload = new BatchUpload();
			batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			batchUpload.setCreatedBy(userName);
			batchUpload.setDeductorMasterTan(tan);
			batchUpload.setSuccessCount(0L);
			batchUpload.setFailedCount(0L);
			batchUpload.setRowsCount(0L);
			batchUpload.setProcessedCount(0);
			batchUpload.setMismatchCount(0L);
			batchUpload.setSha256sum(sha256);
			batchUpload.setStatus("Duplicate");
			batchUpload.setNewStatus("Duplicate");
			batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			batchUpload = hsnCodeBatchUpload(batchUpload, file, tan, assesssmentYear, assessmentMonth, userName, null,
					tenantId);
			return batchUpload;
		}
		try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());) {
			XSSFSheet worksheet = workbook.getSheetAt(0);
			XSSFRow headerRow = worksheet.getRow(0);
			int headersCount = TdsHsnCodeExcel.getNonEmptyCellsCount(headerRow);
			logger.info("Column header count :{}", headersCount);
			BatchUpload batchUpload = new BatchUpload();
			if (headersCount != TdsHsnCodeExcel.fieldMappings.size()) {
				batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
				batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
				batchUpload.setSuccessCount(0L);
				batchUpload.setFailedCount(0L);
				batchUpload.setRowsCount(0L);
				batchUpload.setProcessedCount(0);
				batchUpload.setMismatchCount(0L);
				batchUpload.setSha256sum(sha256);
				batchUpload.setStatus("Failed");
				batchUpload.setCreatedBy(userName);
				batchUpload.setDeductorMasterTan(tan);
				batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
				return hsnCodeBatchUpload(batchUpload, file, tan, assesssmentYear, assessmentMonth, userName, null,
						tenantId);
			} else {
				batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
				batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
				batchUpload.setStatus("Processing");
				batchUpload.setSuccessCount(0L);
				batchUpload.setFailedCount(0L);
				batchUpload.setRowsCount(0L);
				batchUpload.setProcessedCount(0);
				batchUpload.setMismatchCount(0L);
				batchUpload.setDeductorMasterTan(tan);
				batchUpload = hsnCodeBatchUpload(batchUpload, file, tan, assesssmentYear, assessmentMonth, userName,
						null, tenantId);
			}
			if (headersCount == TdsHsnCodeExcel.fieldMappings.size()) {
				return saveTdsHsnCode(workbook, file, sha256, assesssmentYear, assessmentMonth, userName, batchUpload,
						tenantId, tan, deductorPan);
			} else {
				throw new CustomException("Data not found ", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to process article master data ", e);
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
	public BatchUpload hsnCodeBatchUpload(BatchUpload batchUpload, MultipartFile mFile, String tan,
			Integer assesssmentYear, Integer month, String userName, File file, String tenant)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {
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
		batchUpload.setAssessmentMonth(month);
		batchUpload.setAssessmentYear(assesssmentYear);
		batchUpload.setDeductorMasterTan(tan);
		batchUpload.setUploadType(UploadTypes.TDS_HSN_CODE_EXCEL.name());
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
	 * 
	 * @param workbook
	 * @param file
	 * @param sha256
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @param userName
	 * @param batchUpload
	 * @param deductorPan
	 * @param uploadType
	 * @return
	 * @throws IOException
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws InvalidKeyException
	 */
	@Async
	private BatchUpload saveTdsHsnCode(XSSFWorkbook workbook, MultipartFile file, String sha256,
			Integer assesssmentYear, Integer assessmentMonth, String userName, BatchUpload batchUpload, String tenantId,
			String tan, String deductorPan)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException {
		File tdsHsnCodeErrorFile = null;
		ArrayList<TdsHsnCodeErrorReportDTO> errorList = new ArrayList<>();
		try {
			TdsHsnCodeExcel excelData = new TdsHsnCodeExcel(workbook);
			batchUpload.setSha256sum(sha256);
			batchUpload.setMismatchCount(0L);
			long dataRowsCount = excelData.getDataRowsCount();
			batchUpload.setRowsCount(dataRowsCount);
			Long errorCount = 0L;
			int processedRecordsCount = 0;
			Long duplicateCount = 0L;
			for (int rowIndex = 1; rowIndex <= dataRowsCount; rowIndex++) {
				Optional<TdsHsnCodeErrorReportDTO> errorDTO = null;
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
						TdsHsnCodeExcelDTO tdsHsnDto = excelData.get(rowIndex);
						TdsHsnCode tdsHsnCode = new TdsHsnCode();
						TdsHsnCodeErrorReportDTO hsnCodeError = excelData.getErrorDTO(rowIndex);
						if (StringUtils.isBlank(hsnCodeError.getReason())) {
							hsnCodeError.setReason("");
						}
						if (String.valueOf(tdsHsnDto.getHsnCode()).matches("[0-9]{1,8}")) {
							tdsHsnCode.setHsnCode(tdsHsnDto.getHsnCode());
						} else {
							hsnCodeError.setReason(
									hsnCodeError.getReason() + "HSN/SAC Code enter max size is 8 digit number" + "\n");
							errorCount++;
							errorList.add(hsnCodeError);
							isValid = false;
						}
						if (StringUtils.isNotBlank(tdsHsnDto.getTdsSection())
								&& StringUtils.isBlank(tdsHsnDto.getNatureOfPayment())) {
							hsnCodeError.setReason("Nature of payment is mandatory.");
							errorCount++;
							errorList.add(hsnCodeError);
							isValid = false;
						}
						if (StringUtils.isNotBlank(tdsHsnDto.getNatureOfPayment())) {
							Map<String, String> nopMap = new HashMap<>();
							nopMap.put("nop", tdsHsnDto.getNatureOfPayment().trim());
							// feign client for nature of payment id based on section and nature
							Optional<NatureOfPaymentMaster> natureOfPaymentOptional = mastersClient
									.getNOPBasedOnNature(nopMap).getBody().getData();
							if (natureOfPaymentOptional.isPresent()) {
								tdsHsnCode.setNatureOfPayment(tdsHsnDto.getNatureOfPayment().trim());
							} else {
								hsnCodeError.setReason(hsnCodeError.getReason()
										+ "Nature of payment master not found in system " + "\n");
								errorCount++;
								errorList.add(hsnCodeError);
								isValid = false;
							}
						}
						if (isValid) {
							tdsHsnCode.setDescription(tdsHsnDto.getDesc().trim());
							tdsHsnCode.setTdsSection(
									tdsHsnDto.getTdsSection() == null ? null : tdsHsnDto.getTdsSection().trim());
							tdsHsnCode.setActive(true);
							tdsHsnCode.setCreatedBy(userName);
							tdsHsnCode.setCreatedDate(new Timestamp(new Date().getTime()));
							tdsHsnCode.setModifiedBy(userName);
							tdsHsnCode.setModifiedDate(new Timestamp(new Date().getTime()));
							tdsHsnCode.setDeductorPan(deductorPan);
							tdsHsnCode.setDeductorMasterTan(tan);
							// save in tds_hcs_code
							try {
								tdsHsnCodeDAO.save(tdsHsnCode);
								++processedRecordsCount;
							} catch (Exception e) {
								logger.error("Duplicate hsn code not allowed");
								duplicateCount++;
							}
						}
					} catch (Exception e) {
						logger.error("Unable to process row number " + rowIndex + " due to " + e.getMessage(), e);
						TdsHsnCodeErrorReportDTO problematicDataError = excelData.getErrorDTO(rowIndex);
						if (StringUtils.isBlank(problematicDataError.getReason())) {
							problematicDataError.setReason(
									"Unable to process row number " + rowIndex + " due to : " + e.getMessage());
						}
						errorList.add(problematicDataError);
						++errorCount;
					}
				}
			}
			batchUpload.setSuccessCount(Long.valueOf(processedRecordsCount));
			batchUpload.setFailedCount(errorCount);
			batchUpload.setProcessedCount(processedRecordsCount);
			batchUpload.setDuplicateCount(duplicateCount);
			batchUpload.setStatus("Processed");
			batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			batchUpload.setCreatedBy(userName);
			if (!errorList.isEmpty()) {
				tdsHsnCodeErrorFile = prepareHsnCodeErrorFile(file.getOriginalFilename(), errorList,
						new ArrayList<>(excelData.getHeaders()), tan, deductorPan);
			}
		} catch (Exception e) {
			batchUpload.setStatus("Process failed");
			logger.error("Exception occurred :", e);

		}
		return hsnCodeBatchUpload(batchUpload, file, tan, assesssmentYear, assessmentMonth, userName,
				tdsHsnCodeErrorFile, tenantId);
	}

	/**
	 * 
	 * @param sha256Sum
	 * @return
	 */
	private boolean isAlreadyProcessed(String sha256Sum) {
		List<BatchUpload> sha256Record = batchUploadDAO.getSha256Records(sha256Sum);
		return sha256Record != null && !sha256Record.isEmpty();
	}

	/**
	 * 
	 * @param originalFilename
	 * @param errorList
	 * @param headers
	 * @param tan
	 * @return
	 * @throws Exception
	 */
	private File prepareHsnCodeErrorFile(String originalFilename, List<TdsHsnCodeErrorReportDTO> errorList,
			ArrayList<String> headers, String tan, String deductorPan) throws Exception {
		try {
			headers.addAll(0, Excel.STANDARD_ADDITIONAL_HEADERS);
			Workbook wkBook = hsnCodeXlsxReport(errorList, headers, tan, deductorPan);
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
	 * @param tan
	 * @return
	 * @throws Exception
	 */
	private Workbook hsnCodeXlsxReport(List<TdsHsnCodeErrorReportDTO> errorList, ArrayList<String> headers, String tan,
			String deductorPan) throws Exception {

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);

		worksheet.getCells().importArrayList(headers, 5, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		setExtractDataForHsnCode(errorList, worksheet, headers, tan);

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
		Range headerColorRange2 = worksheet.getCells().createRange("D6:G6");
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
		String lastHeaderCellName = "G6";
		String firstDataCellName = "A7";
		String lastDataCellName = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);

		CommonUtil.setBoardersForAsposeXlsx(worksheet, firstHeaderCellName, lastHeaderCellName, firstDataCellName,
				lastDataCellName);
		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A6:G6");
		worksheet.autoFitColumn(1);
		worksheet.autoFitRows();
		return workbook;
	}

	/**
	 * 
	 * @param errorList
	 * @param worksheet
	 * @param headers
	 * @param deductorTan
	 * @throws Exception
	 */
	private void setExtractDataForHsnCode(List<TdsHsnCodeErrorReportDTO> errorList, Worksheet worksheet,
			ArrayList<String> headers, String deductorTan) throws Exception {
		if (!errorList.isEmpty()) {
			int dataRowsStartIndex = 6;
			int serialNumber = 1;
			for (int i = 0; i < errorList.size(); i++) {
				TdsHsnCodeErrorReportDTO errorDTO = errorList.get(i);
				ArrayList<String> rowData = Excel.getValues(errorDTO, TdsHsnCodeExcel.fieldMappings, headers);
				rowData.set(0, StringUtils.isBlank(deductorTan) ? StringUtils.EMPTY : deductorTan);
				rowData.set(1, StringUtils.isBlank(errorDTO.getReason()) ? StringUtils.EMPTY : errorDTO.getReason());
				rowData.set(2, String.valueOf(serialNumber));
				worksheet.getCells().importArrayList(rowData, i + dataRowsStartIndex, 0, false);
				worksheet.autoFitRow(i + dataRowsStartIndex);
				worksheet.autoFitColumn(i);
				Excel.colorCodeErrorCells(worksheet, i + dataRowsStartIndex, headers, errorDTO.getReason());
				serialNumber++;
			}
		}
	}

	/**
	 * 
	 * @param deductorPan
	 * @param tan
	 * @return
	 */
	public CommonDTO<TdsHsnCodeDTO> getAllHsnSac(Pagination pagination, String deductorPan, String tan) {
		List<TdsHsnCodeDTO> sacs = new ArrayList<>();
		List<TdsHsnCode> hsnList = tdsHsnCodeDAO.getAllHsnCode(pagination, deductorPan, tan);
		for (TdsHsnCode hsnCode : hsnList) {
			TdsHsnCodeDTO hsnSacDTO = new TdsHsnCodeDTO();
			hsnSacDTO.setHsnCode(hsnCode.getHsnCode());
			hsnSacDTO.setDescription(hsnCode.getDescription());
			hsnSacDTO.setNatureOfPayment(hsnCode.getNatureOfPayment());
			hsnSacDTO.setTdsSection(hsnCode.getTdsSection());
			hsnSacDTO.setId(hsnCode.getId());
			sacs.add(hsnSacDTO);
		}
		BigInteger hsnCodeCount = tdsHsnCodeDAO.getAllHsnCodeCount(deductorPan, tan);
		PagedData<TdsHsnCodeDTO> pagedData = new PagedData<>(sacs, sacs.size(), pagination.getPageNumber(),
				hsnCodeCount.intValue() > (pagination.getPageSize() * pagination.getPageNumber()) ? false : true);
		CommonDTO<TdsHsnCodeDTO> hsnCodeList = new CommonDTO<>();
		hsnCodeList.setResultsSet(pagedData);
		hsnCodeList.setCount(hsnCodeCount);
		logger.info("Retrieved data : {}", sacs);
		return hsnCodeList;
	}

	/**
	 * 
	 * @param hsn
	 * @param tan
	 * @param deductorPan
	 * @return
	 */
	public List<TdsHsnCodeDTO> findHSNRateDetails(Long hsn, String deductorPan, String tan) {
		List<TdsHsnCodeDTO> tdsHsnCodeAndNopList = new ArrayList<>();
		List<TdsHsnCode> hsnList = tdsHsnCodeDAO.getHsnList(hsn, deductorPan, tan);
		if (!hsnList.isEmpty()) {
			for (TdsHsnCode hsnCode : hsnList) {
				TdsHsnCodeDTO tdsHsnCodeAndNop = new TdsHsnCodeDTO();
				tdsHsnCodeAndNop.setId(hsnCode.getId());
				tdsHsnCodeAndNop.setHsnCode(hsnCode.getHsnCode());
				tdsHsnCodeAndNop.setDescription(hsnCode.getDescription());
				tdsHsnCodeAndNop.setNatureOfPayment(hsnCode.getNatureOfPayment());
				tdsHsnCodeAndNop.setTdsSection(hsnCode.getTdsSection());
				tdsHsnCodeAndNopList.add(tdsHsnCodeAndNop);
			}
		}
		return tdsHsnCodeAndNopList;
	}

	/**
	 * 
	 * @param tdsHsnCode
	 * @param deductorPan
	 * @param tan
	 * @return
	 */
	public TdsHsnCode createHsnCode(TdsHsnCodeDTO tdsHsnCode, String deductorPan, String tan, String userName) {
		TdsHsnCode hsn = new TdsHsnCode();
		hsn.setHsnCode(tdsHsnCode.getHsnCode());
		hsn.setNatureOfPayment(tdsHsnCode.getNatureOfPayment());
		hsn.setDescription(tdsHsnCode.getDescription());
		hsn.setTdsSection(tdsHsnCode.getTdsSection());
		hsn.setActive(true);
		hsn.setCreatedBy(userName);
		hsn.setCreatedDate(new Timestamp(new Date().getTime()));
		hsn.setModifiedBy(userName);
		hsn.setModifiedDate(new Timestamp(new Date().getTime()));
		hsn.setDeductorPan(deductorPan);
		hsn.setDeductorMasterTan(tan);
		try {
			tdsHsnCodeDAO.save(hsn);
		} catch (Exception e) {
			logger.error("Duplicate hsn code not allowed");
			throw new CustomException("Duplicate hsn code not allowed", HttpStatus.BAD_REQUEST);
		}
		return hsn;
	}

	/**
	 * 
	 * @param id
	 * @param deductorPan
	 * @param tan
	 * @return
	 */
	public TdsHsnCode fineById(Integer id, String deductorPan, String tan) {
		List<TdsHsnCode> hsnList = tdsHsnCodeDAO.getById(id, deductorPan, tan);
		if (!hsnList.isEmpty()) {
			return hsnList.get(0);
		} else {
			return new TdsHsnCode();
		}
	}

	/**
	 * 
	 * @param tdsHsnCode
	 * @param deductorPan
	 * @param tan
	 * @param userName
	 * @return
	 */
	public TdsHsnCodeDTO updateHsn(TdsHsnCodeDTO tdsHsnCode, String deductorPan, String tan, String userName) {
		List<TdsHsnCode> hsnList = tdsHsnCodeDAO.getById(tdsHsnCode.getId(), deductorPan, tan);
		if (!hsnList.isEmpty()) {
			hsnList.get(0).setDescription(tdsHsnCode.getDescription());
			hsnList.get(0).setHsnCode(tdsHsnCode.getHsnCode());
			hsnList.get(0).setNatureOfPayment(tdsHsnCode.getNatureOfPayment());
			hsnList.get(0).setTdsSection(tdsHsnCode.getTdsSection());
			hsnList.get(0).setModifiedBy(userName);
			hsnList.get(0).setModifiedDate(new Date());
			try {
				// update hsn code
				tdsHsnCodeDAO.update(hsnList.get(0));
			} catch (Exception e) {
				logger.error("Duplicate hsn code not allowed");
				throw new CustomException("Duplicate hsn code not allowed", HttpStatus.BAD_REQUEST);
			}
		}
		return tdsHsnCode;
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public ByteArrayInputStream exportNOPAndSection() throws IOException {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DefaultIndexedColorMap defaultIndexedColorMap = new DefaultIndexedColorMap();
		try (XSSFWorkbook wb = new XSSFWorkbook()) {
			XSSFSheet sheet = wb.createSheet("Nature and sections");
			sheet.lockAutoFilter(false);
			sheet.lockSort(false);
			// protection enable
			sheet.protectSheet("password");
			// creating the header row and applying style to the header
			XSSFRow row = sheet.createRow(0);
			sheet.setDefaultColumnWidth(25);
			sheet.setAutoFilter(CellRangeAddress.valueOf("A1:B1"));
			XSSFCellStyle style0 = wb.createCellStyle();

			style0.setBorderTop(BorderStyle.MEDIUM);
			style0.setBorderBottom(BorderStyle.MEDIUM);
			style0.setBorderRight(BorderStyle.MEDIUM);
			style0.setAlignment(HorizontalAlignment.CENTER);
			style0.setVerticalAlignment(VerticalAlignment.CENTER);

			Font font = wb.createFont();
			font.setBold(true);
			font.setFontName("Arial");
			style0.setFont(font);

			// Black colour
			style0.setFillForegroundColor(new XSSFColor(new java.awt.Color(46, 134, 193), defaultIndexedColorMap));
			style0.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			// creating cells for the header
			row.createCell(0).setCellValue("NatureOfPayment");
			row.getCell(0).setCellStyle(style0);
			row.createCell(1).setCellValue("Section");
			row.getCell(1).setCellStyle(style0);

			// feign client for get all nop
			List<NatureOfPaymentMasterDTO> response = masterClient.findAll().getBody().getData();
			logger.info("nop response size is :{}", response.size());

			int rowNumber = 1;
			if (!response.isEmpty()) {
				for (NatureOfPaymentMasterDTO nop : response) {
					XSSFRow row2 = sheet.createRow(rowNumber);
					row2.createCell(0).setCellValue(nop.getNature());
					row2.createCell(1).setCellValue(nop.getSection());
					rowNumber++;
				}
			}

			wb.write(out);
		}
		return new ByteArrayInputStream(out.toByteArray());
	}

	/**
	 * 
	 * @param deductorPan
	 * @param tan
	 * @return
	 */
	public List<String> getAllSections(String deductorPan, String tan) {
		return tdsHsnCodeDAO.getAllSections(deductorPan, tan);
	}

	/**
	 * 
	 * @param tdsSection
	 * @param deductorPan
	 * @param tan
	 * @return
	 */
	public List<TdsHsnCodeDTO> fetchHsnByTdsSecion(String tdsSection, String deductorPan, String tan) {
		List<TdsHsnCodeDTO> tdsHsnCodeAndNopList = new ArrayList<>();
		List<TdsHsnCode> hsnList = tdsHsnCodeDAO.fetchHsnByTdsSecion(tdsSection, deductorPan, tan);
		if (!hsnList.isEmpty()) {
			for (TdsHsnCode hsnCode : hsnList) {
				TdsHsnCodeDTO tdsHsnCodeAndNop = new TdsHsnCodeDTO();
				tdsHsnCodeAndNop.setId(hsnCode.getId());
				tdsHsnCodeAndNop.setHsnCode(hsnCode.getHsnCode());
				tdsHsnCodeAndNop.setDescription(hsnCode.getDescription());
				tdsHsnCodeAndNop.setNatureOfPayment(hsnCode.getNatureOfPayment());
				tdsHsnCodeAndNop.setTdsSection(hsnCode.getTdsSection());
				tdsHsnCodeAndNopList.add(tdsHsnCodeAndNop);
			}
		}
		return tdsHsnCodeAndNopList;
	}

}
