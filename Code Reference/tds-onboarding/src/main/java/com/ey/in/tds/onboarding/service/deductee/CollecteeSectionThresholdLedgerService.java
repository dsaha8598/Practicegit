package com.ey.in.tds.onboarding.service.deductee;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.aspose.cells.ImportTableOptions;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.tcs.TCSBatchUpload;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.TCSBatchUploadDAO;
import com.ey.in.tds.common.service.Sha256SumService;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.jdbc.dao.CollecteeSectionThresholdLedgerDAO;
import com.ey.in.tds.onboarding.dto.collector.CollecteeSectionThresholdLedgerDTO;
import com.ey.in.tds.onboarding.service.util.excel.deductee.CollecteeSectionThreshholdLedgerExcel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.StorageException;

/**
 * 
 * @author Scriptbees
 *
 */

@Service
@Transactional
public class CollecteeSectionThresholdLedgerService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private Sha256SumService sha256SumService;

	@Autowired
	private CollecteeSectionThresholdLedgerDAO collecteeSectionThresholdLedgerDAO;

	@Autowired
	private TCSBatchUploadDAO tcsBatchUploadDAO;

	@Autowired
	private BlobStorage blob;

	/**
	 * 
	 * @param file
	 * @param tenantId
	 * @return
	 * @throws Exception
	 */
	@Transactional
	public TCSBatchUpload saveFileData(MultipartFile multiPartFile, String tenantId, String typeOfFile,
			Integer assessmentYear) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		String sha256 = sha256SumService.getSHA256Hash(multiPartFile);
		if (isAlreadyProcessed(sha256)) {
			TCSBatchUpload tcsBatchUpload = new TCSBatchUpload();
			tcsBatchUpload.setCreatedDate(new Date());
			tcsBatchUpload.setProcessStartTime(new Date());
			tcsBatchUpload.setSuccessCount(0L);
			tcsBatchUpload.setFailedCount(0L);
			tcsBatchUpload.setRowsCount(0L);
			tcsBatchUpload.setProcessed(0);
			tcsBatchUpload.setMismatchCount(0L);
			tcsBatchUpload.setSha256sum(sha256);
			tcsBatchUpload.setStatus("Duplicate");
			tcsBatchUpload.setNewStatus("Duplicate");
			tcsBatchUpload.setProcessEndTime(new Date());
			tcsBatchUpload = collecteeBatchUpload(tcsBatchUpload, multiPartFile, assessmentYear, null, tenantId);
			return tcsBatchUpload;
		}
		List<CollecteeSectionThresholdLedgerDTO> collecteeList = new ArrayList<>();
		try (XSSFWorkbook workbook = new XSSFWorkbook(multiPartFile.getInputStream());) {
			XSSFSheet worksheet = workbook.getSheetAt(0);
			XSSFRow headerRow = worksheet.getRow(0);
			int headersCount = Excel.getNonEmptyCellsCount(headerRow);
			logger.info("Column header count {}:", headersCount);
			TCSBatchUpload tcsBatchUpload = new TCSBatchUpload();
			if (headersCount != CollecteeSectionThreshholdLedgerExcel.fieldMappings.size()) {
				tcsBatchUpload.setCreatedDate(new Date());
				tcsBatchUpload.setProcessStartTime(new Date());
				tcsBatchUpload.setSuccessCount(0L);
				tcsBatchUpload.setFailedCount(0L);
				tcsBatchUpload.setRowsCount(0L);
				tcsBatchUpload.setProcessed(0);
				tcsBatchUpload.setMismatchCount(0L);
				tcsBatchUpload.setSha256sum(sha256);
				tcsBatchUpload.setStatus("Failed");
				tcsBatchUpload.setProcessEndTime(new Date());
				return collecteeBatchUpload(tcsBatchUpload, multiPartFile, assessmentYear, null, tenantId);
			} else {
				tcsBatchUpload.setCreatedDate(new Date());
				tcsBatchUpload.setProcessStartTime(new Date());
				tcsBatchUpload.setStatus("Processing");
				tcsBatchUpload.setSuccessCount(0L);
				tcsBatchUpload.setFailedCount(0L);
				tcsBatchUpload.setRowsCount(0L);
				tcsBatchUpload.setProcessed(0);
				tcsBatchUpload.setMismatchCount(0L);
				tcsBatchUpload = collecteeBatchUpload(tcsBatchUpload, multiPartFile, assessmentYear, null, tenantId);
			}
			if (headersCount == CollecteeSectionThreshholdLedgerExcel.fieldMappings.size()) {
				return processCollecteeSectionThresholdLedger(workbook, multiPartFile, sha256, tcsBatchUpload,
						tenantId);
			} else {
				throw new CustomException("Data not found ", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to process collectee data ", e);
		}
	}

	public TCSBatchUpload processCollecteeSectionThresholdLedger(XSSFWorkbook workbook, MultipartFile uploadedFile,
			String sha256, TCSBatchUpload tcsBatchUpload, String tenantId) throws Exception {

		MultiTenantContext.setTenantId("master");

		DecimalFormat df = new DecimalFormat("################.#############");
		List<CollecteeSectionThresholdLedgerDTO> sacList = new ArrayList<>();
		tcsBatchUpload.setSha256sum(sha256);
		tcsBatchUpload.setMismatchCount(0L);
		Long duplicateCount = 0L;
		Long errorCount = 0L;

		try {

			XSSFSheet sheet = workbook.getSheetAt(0);
			Iterator<Row> itr = sheet.iterator();
			long totalRows = sheet.getPhysicalNumberOfRows();
			tcsBatchUpload.setRowsCount(totalRows);
			Map<String, Integer> map = new HashMap<>();
			// get column names
			while (itr.hasNext()) {
				Row row = itr.next();
				short minColIx = row.getFirstCellNum(); // get the first column index for a row
				short maxColIx = row.getLastCellNum(); // get the last column index for a row
				for (short colIx = minColIx; colIx < maxColIx; colIx++) { // loop from first to last index
					Cell cell = row.getCell(colIx); // get the cell
					map.put(cell.getStringCellValue(), cell.getColumnIndex()); // add the cell contents (name of column)
																				// and cell index to the map
				}
				break;
			}

			// get the column index for the column with header name
			int collecteeCode = map.get("Collectee Code");
			int collecteeName = map.get("Collectee Name");
			int openingBalanceInvoice = map.get("Opening Balance - Invoice");
			int openingBalanceAdvance = map.get("Opening Balance - Advance");
			int ledgerBalance = map.get("Ledger Balance \n" + "(as on report date)");
			int totalTcsPaid = map.get("Total TCS paid");
			int section = map.get("Section");

			for (int x = 1; x <= totalRows; x++) {
				CollecteeSectionThresholdLedgerDTO rr = new CollecteeSectionThresholdLedgerDTO(); // Data structure to
																									// hold the data
																									// from the xls
																									// file.
				XSSFRow dataRow = sheet.getRow(x); // get row 1 to row n (rows containing data)
				if (dataRow == null) {
					continue;
				}
				// Get the cells for each of the indexes
				Cell cc = dataRow.getCell(collecteeCode);
				Cell cn = dataRow.getCell(collecteeName);
				Cell obi = dataRow.getCell(openingBalanceInvoice);
				Cell oba = dataRow.getCell(openingBalanceAdvance);
				Cell lb = dataRow.getCell(ledgerBalance);
				Cell ttp = dataRow.getCell(totalTcsPaid);
				Cell s = dataRow.getCell(section);
				try {
					// Get the values out of those cells and set them
					rr.setCollecteeCode(cc.getStringCellValue());
					rr.setAmountUtilized(BigDecimal.valueOf(obi.getNumericCellValue()));
					rr.setCollecteeSection(s.getStringCellValue());
					rr.setActive(1);
					sacList.add(rr);
				} catch (Exception e) {
					logger.error(e.getLocalizedMessage());
				}

			}
			ObjectMapper mapper = new ObjectMapper();
			String jsonString = mapper.writeValueAsString(sacList);
			logger.info("Excel upload : {}", jsonString);

			int duplicateRecordsCount = processCollecteeSectionThresholdRecords(sacList);
			// increment duplicate count if same deductee in database exists with same
			// sections and pan then
			// marking as duplicate.
			duplicateCount += duplicateRecordsCount;
			int processedRecordsCount = sacList.size();
			tcsBatchUpload.setSuccessCount(totalRows);
			tcsBatchUpload.setFailedCount(errorCount);
			tcsBatchUpload.setProcessed(processedRecordsCount);
			tcsBatchUpload.setDuplicateCount(0L);
			tcsBatchUpload.setStatus("Processed");
			tcsBatchUpload.setCreatedDate(new Date());
			tcsBatchUpload.setProcessEndTime(new Date());

			// Generating deductee pan file and uploading to pan validation
			MultipartFile file = generateCollecteePanXlsxReport(sacList);
			String panUrl = blob.uploadExcelToBlob(file, tenantId);
			tcsBatchUpload.setOtherFileUrl(panUrl);

		} catch (Exception e) {
			logger.error("Exception occurred :", e);
		}

		return tcsBatchUpload;
	}

	private boolean isAlreadyProcessed(String sha256Sum) {
		List<TCSBatchUpload> sha256Record = tcsBatchUploadDAO.getSha256Records(sha256Sum);
		return sha256Record != null && !sha256Record.isEmpty();
	}

	public int processCollecteeSectionThresholdRecords(List<CollecteeSectionThresholdLedgerDTO> collecteeList)
			throws Exception {
		for (CollecteeSectionThresholdLedgerDTO collectee : collecteeList) {
			collecteeSectionThresholdLedgerDAO.save(collectee);
		}
		return 0;
	}

	public TCSBatchUpload collecteeBatchUpload(TCSBatchUpload tcsBatchUpload, MultipartFile mFile,
			Integer assesssmentYear, File file, String tenant)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		logger.info("batch", tcsBatchUpload);
		String errorFp = null;
		String path = null;
		if (file != null) {
			errorFp = blob.uploadExcelToBlobWithFile(file, tenant);
			tcsBatchUpload.setErrorFilePath(errorFp);
		}
		if (mFile != null) {
			path = blob.uploadExcelToBlob(mFile);
			tcsBatchUpload.setFileName(mFile.getOriginalFilename());
			tcsBatchUpload.setFilePath(path);
		}
		tcsBatchUpload.setAssessmentYear(assesssmentYear);
		tcsBatchUpload.setUploadType(UploadTypes.COLLECTEE_LEDGER.name());
		tcsBatchUpload.setActive(true);
		if (tcsBatchUpload.getId() != null) {
			tcsBatchUpload.setId(tcsBatchUpload.getId());
			return tcsBatchUpload = tcsBatchUploadDAO.update(tcsBatchUpload);
		} else {
			return tcsBatchUpload = tcsBatchUploadDAO.save(tcsBatchUpload);
		}
	}

	/**
	 * 
	 * @param collecteePans
	 * @return
	 * @throws Exception
	 */
	public MultipartFile generateCollecteePanXlsxReport(List<CollecteeSectionThresholdLedgerDTO> collecteePans)
			throws Exception {

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);

		String[] headerNames = new String[] { "Collectee Code", "Section" };
		worksheet.getCells().importArray(headerNames, 0, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);
		if (!collecteePans.isEmpty()) {
			int rowIndex = 1;
			for (CollecteeSectionThresholdLedgerDTO collecteePan : collecteePans) {
				ArrayList<String> rowData = new ArrayList<String>();
				rowData.add(StringUtils.isBlank(collecteePan.getCollecteeCode()) ? StringUtils.EMPTY
						: collecteePan.getCollecteeCode());
				rowData.add(StringUtils.isBlank(collecteePan.getCollecteeSection()) ? StringUtils.EMPTY
						: collecteePan.getCollecteeSection());
				worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);
			}

		}
		File file = new File("collectee_ledger_upload_template_" + UUID.randomUUID() + ".xlsx");
		OutputStream out = new FileOutputStream(file);
		workbook.save(out, SaveFormat.XLSX);
		InputStream inputstream = new FileInputStream(file);
		MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "application/vnd.ms-excel",
				IOUtils.toByteArray(inputstream));
		inputstream.close();
		out.close();

		return multipartFile;
	}
}
