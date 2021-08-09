package com.ey.in.tds.onboarding.tcs.service.chartofaccounts;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.ey.in.tds.common.domain.Classification;
import com.ey.in.tds.common.domain.tcs.TCSBatchUpload;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.TCSBatchUploadDAO;
import com.ey.in.tds.common.dto.chartofaccounts.ChartOfAccountsErrorDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorMaster;
import com.ey.in.tds.common.onboarding.tcs.jdbc.dto.ChartOfAccountsTCS;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.core.util.ChartOfAccountsType;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.feign.client.MastersClient;
import com.ey.in.tds.jdbc.dao.DeductorMasterDAO;
import com.ey.in.tds.onboarding.service.chartofaccounts.ChartOfAccountsExcel;
import com.ey.in.tds.onbording.tcs.jdbc.dao.ChartOfAccountsTCSDAO;
import com.microsoft.azure.storage.StorageException;

/**
 * contains logic for GL processing ,error report preparation 
 * @author Dipak Saha
 *
 */
@Service
public class ChartOfAccountServiceTCS {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private TCSBatchUploadDAO tCSBatchUploadDAO;
	
	@Autowired
	private ChartOfAccountsTCSDAO chartOfAccountsTCSDAO;
	
	@Autowired
	private BlobStorage blob;
	
	@Autowired
	private MastersClient mastersClient;
	
	@Autowired
	private DeductorMasterDAO  deductorMasterDAO;
	/**
	 * reading and processing the GL file and cretaing record in tcs batch upload
	 * @param multipartFile
	 * @param deductorPan
	 * @param tan
	 * @param tenantId
	 * @param userName
	 * @param assessmentYear
	 * @return
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws InvalidFormatException
	 */
	public TCSBatchUpload createChartOfAccounts(MultipartFile multipartFile, String deductorPan, String tan,
			String tenantId, String userName, Integer assessmentYear)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException, InvalidFormatException {

		TCSBatchUpload batchUpload = new TCSBatchUpload();

		try (XSSFWorkbook workbook = new XSSFWorkbook(multipartFile.getInputStream());) {

			XSSFSheet worksheet = workbook.getSheetAt(0);
			XSSFRow headerRow = worksheet.getRow(0);

			int headersCount = Excel.getNonEmptyCellsCount(headerRow);

			logger.info("Column header count : {}", headersCount);

			if (headersCount != ChartOfAccountsExcel.fieldMappings.size()) {
				batchUpload.setAssessmentYear(assessmentYear);
				batchUpload.setCollectorMasterTan(tan);
				batchUpload.setUploadType(UploadTypes.CHART_OF_ACCOUNTS.name());
				batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
				batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
				batchUpload.setSuccessCount(0L);
				batchUpload.setFailedCount(0L);
				batchUpload.setRowsCount(0L);
				batchUpload.setProcessed(0);
				batchUpload.setMismatchCount(0L);
				batchUpload.setStatus("Failed");
				batchUpload.setCreatedBy(userName);
				batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
				batchUpload.setCreatedBy(userName);
				batchUpload.setActive(true);
				String path=blob.uploadExcelToBlob(multipartFile);
				batchUpload.setFilePath(path);
				batchUpload.setFileName(multipartFile.getOriginalFilename());
				batchUpload=tCSBatchUploadDAO.save(batchUpload);
			}

			if (headersCount >= ChartOfAccountsExcel.fieldMappings.size()) {
				return processChartOfAccounts(multipartFile, deductorPan, tan, tenantId,  userName,
						assessmentYear);
			}
		} catch (Exception e) {
			logger.error("Exception occure while uploading Charts of Accounts {}"+e);
			throw new RuntimeException("Failed to process chart of accounts data ", e);
		}
		return batchUpload;
	}

	private TCSBatchUpload processChartOfAccounts(MultipartFile multipartFile, String deductorPan, String tan,
			String tenantId,  String userName, Integer assessmentYear)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException {
		int processedCount = 0;
		int errorCount = 0;
		Long duplicateCount = 0L;
		ArrayList<ChartOfAccountsErrorDTO> errorList = new ArrayList<>();
		TCSBatchUpload batchUpload = new TCSBatchUpload();
		File chartOfAccountsErrorFile = null;
		try {
			XSSFWorkbook workbook = new XSSFWorkbook(multipartFile.getInputStream());
			ChartOfAccountTCSExcel data = new ChartOfAccountTCSExcel(workbook);
			long dataRowsCount = data.getDataRowsCount();
			for (int rowIndex = 1; rowIndex <= dataRowsCount; rowIndex++) {
				boolean isAccountTypeValid = false;
				Optional<ChartOfAccountsErrorDTO> errorDTO = null;
				try {
					errorDTO = data.validate(rowIndex);
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
						boolean isValid = true;
						ChartOfAccountsTCS dto = data.get(rowIndex);
						if (dto.getAccountType() != null) {
							for (ChartOfAccountsType s : ChartOfAccountsType.values()) {
								if (dto.getAccountType().equalsIgnoreCase(s.toString())) {
									isAccountTypeValid = true;
								}
							}
							if (!isAccountTypeValid) {
								ChartOfAccountsErrorDTO accountTypeErrorDTO = null;
								accountTypeErrorDTO = data.getErrorDTO(rowIndex);
								accountTypeErrorDTO
										.setReason("Account type " + dto.getAccountType() + " not found in system.");
								errorList.add(accountTypeErrorDTO);
								++errorCount;
								isValid = false;
							}
						}
						if (dto.getClassification() != null) {
							if (ChartOfAccountsType.EXPENSES.toString().equalsIgnoreCase(dto.getAccountType())) {
								logger.info("Making feign call to get Classification");
								Optional<Classification> classification = mastersClient
										.findByClassificationCode(dto.getClassification().toUpperCase()).getBody()
										.getData();
								if (!classification.isPresent()) {
									ChartOfAccountsErrorDTO classificationErrorDTO = null;
									classificationErrorDTO = data.getErrorDTO(rowIndex);
									classificationErrorDTO.setReason(
											"Classification " + dto.getClassification() + " not found in system.");
									errorList.add(classificationErrorDTO);
									++errorCount;
									isValid = false;
								}
							}
						} else if (ChartOfAccountsType.EXPENSES.toString().equalsIgnoreCase(dto.getAccountType())
								&& dto.getClassification() == null) {
							ChartOfAccountsErrorDTO classificationErrorDTO = null;
							classificationErrorDTO = data.getErrorDTO(rowIndex);
							classificationErrorDTO.setReason("Classification is mandatory for expenses account type.");
							errorList.add(classificationErrorDTO);
							++errorCount;
							isValid = false;
						}
						if (isValid) {
							if (StringUtils.isBlank(dto.getAccountDescription())) {
								dto.setAccountDescription("");
							}
							if (StringUtils.isBlank(dto.getAccountType())) {
								dto.setAccountType("");
							}
							if (StringUtils.isBlank(dto.getClassification())) {
								dto.setClassification("");
							}
							dto.setPan(deductorPan);
							dto.setAccountDescription(dto.getAccountDescription().trim());
							dto.setAccountType(dto.getAccountType().trim());
							dto.setAccountCode(dto.getAccountCode().trim());
							dto.setActive(true);
							dto.setAssessmentYear(assessmentYear);
							dto.setBatchId(dto.getBatchId());
							dto.setClassification(dto.getClassification().trim());
							dto.setCreatedBy(userName);
							dto.setCreatedDate(new Date());
							// check duplication records in db
							List<ChartOfAccountsTCS> listChartOfAccounts = chartOfAccountsTCSDAO.getAccountDetails(
									deductorPan, dto.getAccountCode(), dto.getAccountType(),
									dto.getAccountDescription(), dto.getClassification());
							if (!listChartOfAccounts.isEmpty() && listChartOfAccounts != null) {
								++duplicateCount;
							} else {
								chartOfAccountsTCSDAO.save(dto);
								++processedCount;
							}
						}
					} catch (Exception e) {
						logger.error("Unable to process row number " + rowIndex + " due to " + e.getMessage(), e);
						ChartOfAccountsErrorDTO problematicDataError = data.getErrorDTO(rowIndex);
						if (StringUtils.isEmpty(problematicDataError.getReason())) {
							problematicDataError.setReason(
									"Unable to save data for row number " + rowIndex + " due to : " + e.getMessage());
						}
						errorList.add(problematicDataError);
						++errorCount;
					}
				}
			}

			batchUpload.setAssessmentYear(assessmentYear);
			batchUpload.setCollectorMasterTan(tan);
			batchUpload.setUploadType(UploadTypes.CHART_OF_ACCOUNTS.name());
			batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			batchUpload.setCreatedBy(userName);
			String path=blob.uploadExcelToBlob(multipartFile);
			batchUpload.setFilePath(path);
			batchUpload.setFileName(multipartFile.getOriginalFilename());
				batchUpload.setFailedCount(Long.valueOf(errorCount));
				batchUpload.setDuplicateCount(duplicateCount);
				batchUpload.setProcessed(processedCount);
				batchUpload.setSuccessCount(dataRowsCount);
				batchUpload.setRowsCount(dataRowsCount);
				batchUpload.setStatus("Processed");
				batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
				if (!errorList.isEmpty()) {
					chartOfAccountsErrorFile = prepareChartOfAccountsErrorFile(multipartFile.getOriginalFilename(), tan,
							deductorPan, errorList, new ArrayList<>(data.getHeaders()));
					String errorFp = blob.uploadExcelToBlobWithFile(chartOfAccountsErrorFile, tenantId);
					batchUpload.setErrorFilePath(errorFp);
				}
				tCSBatchUploadDAO.save(batchUpload);

		} catch (Exception e) {
			logger.error("Error occured during chart of accounts upload", e);
			batchUpload.setFileName(multipartFile.getOriginalFilename());
			batchUpload.setStatus("Failed");
			batchUpload.setCreatedBy(userName);
			batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			batchUpload.setRowsCount(0l);
			batchUpload.setProcessed(0);
			batchUpload.setFailedCount(0l);
			String path=blob.uploadExcelToBlob(multipartFile);
			batchUpload.setFilePath(path);
			tCSBatchUploadDAO.save(batchUpload);
		}
		return batchUpload;
	}

	/**
	 * prepars error report for tcharts of acounts upload
	 * @param originalFilename
	 * @param tan
	 * @param deductorPan
	 * @param errorList
	 * @param headers
	 * @return
	 * @throws Exception
	 */
	private File prepareChartOfAccountsErrorFile(String originalFilename, String tan, String deductorPan,
			ArrayList<ChartOfAccountsErrorDTO> errorList, ArrayList<String> headers) throws Exception {
		try {
			headers.addAll(0, Excel.STANDARD_ADDITIONAL_HEADERS);
			Workbook wkBook = charOfAccountsXlsxReport(errorList, tan, deductorPan, headers);
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

	private Workbook charOfAccountsXlsxReport(ArrayList<ChartOfAccountsErrorDTO> errorList, String tan,
			String deductorPan, ArrayList<String> headers) throws Exception {
		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);

		worksheet.getCells().importArrayList(headers, 5, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		setExtractDataForChartOfAccounts(errorList, worksheet, tan, headers);

		// Style for A6 to D6 headers
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

		// Style for E6 to AT6 headers
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

		List<DeductorMaster> getDeductorName =deductorMasterDAO.findByDeductorPan(deductorPan);

		Cell cellA1 = worksheet.getCells().get("A1");
		cellA1.setValue("TDS Payables Error Report (Dated: " + date + ")");
		Style a1Style = cellA1.getStyle();
		a1Style.getFont().setName("Cambria");
		a1Style.getFont().setSize(14);
		a1Style.getFont().setBold(true);
		cellA1.setStyle(a1Style);

		Cell cellA2 = worksheet.getCells().get("A2");
		if (!getDeductorName.isEmpty()) {
			cellA2.setValue("Client Name:" + getDeductorName.get(0).getName());
		} else {
			cellA2.setValue("Client Name:");
		}
		Style a2Style = cellA2.getStyle();
		a2Style.getFont().setName("Cambria");
		a2Style.getFont().setSize(14);
		a2Style.getFont().setBold(true);
		cellA2.setStyle(a2Style);

		// column B2 column
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
		return workbook;
	}

	/**
	 * binds data to the columns of GL error report
	 * @param errorDTOs
	 * @param worksheet
	 * @param tan
	 * @param headerNames
	 * @throws Exception
	 */
	private void setExtractDataForChartOfAccounts(ArrayList<ChartOfAccountsErrorDTO> errorDTOs, Worksheet worksheet,
			String tan, ArrayList<String> headerNames) throws Exception {
		if (!errorDTOs.isEmpty()) {
			int dataRowsStartIndex = 6;
			for (int i = 0; i < errorDTOs.size(); i++) {
				ChartOfAccountsErrorDTO errorDTO = errorDTOs.get(i);
				ArrayList<String> rowData = Excel.getValues(errorDTO, ChartOfAccountsExcel.fieldMappings, headerNames);
				rowData.set(0, StringUtils.isBlank(tan) ? StringUtils.EMPTY : tan);
				rowData.set(1, StringUtils.isBlank(errorDTO.getReason()) ? StringUtils.EMPTY : errorDTO.getReason());
				rowData.set(2, StringUtils.isBlank(errorDTO.getSerialNumber()) ? StringUtils.EMPTY
						: errorDTO.getSerialNumber());
				worksheet.getCells().importArrayList(rowData, i + dataRowsStartIndex, 0, false);
				worksheet.autoFitRow(i + dataRowsStartIndex);
				Excel.colorCodeErrorCells(worksheet, i + dataRowsStartIndex, headerNames, errorDTO.getReason());
			}
		}

	}
	public List<ChartOfAccountsTCS> getAllChartOfAccounts(String deductorPan) {
		return chartOfAccountsTCSDAO.findAllTcsChartsOfAccountsByDeductorPan(deductorPan);
	}
}
