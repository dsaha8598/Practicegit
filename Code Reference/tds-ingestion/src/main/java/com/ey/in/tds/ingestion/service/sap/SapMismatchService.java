package com.ey.in.tds.ingestion.service.sap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ey.in.tds.common.service.Sha256SumService;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.ingestion.dto.invoice.InvoiceMismatchUpdateDTO;
import com.microsoft.azure.storage.StorageException;

@Service
public class SapMismatchService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private BlobStorage blob;

	@Autowired
	private Sha256SumService sha256SumService;


	/**
	 * 
	 * @param invoiceMismatchUpdateDTO
	 */
	public void updateMismatchByActionForSap(InvoiceMismatchUpdateDTO invoiceMismatchUpdateDTO) {

	}

	/**
	 * 
	 * @param file
	 * @return BatchUpload Object
	 * @throws IOException
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws InvalidKeyException
	 */
	//TODO NEED TO CHANGE FOR SQL
/*	public BatchUpload importToBatchUploadForSap(MultipartFile file, String tenantId)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {

		int year = Calendar.getInstance().get(Calendar.YEAR);
		int month = Calendar.getInstance().get(Calendar.YEAR) + 1;
		logger.info(file.getOriginalFilename());
		String sha256 = sha256SumService.getSHA256Hash(file);
		BatchUpload batchUpload = new BatchUpload();

		List<BatchUpload> batch = batchUploadRepository
				.getSha256RecordsBasedonYearMonth(year, month, UploadTypes.SAP_REM.name(), sha256, Pagination.DEFAULT)
				.getData();

		String path = blob.uploadExcelToBlob(file, tenantId);
		if (!batch.isEmpty()) {
			logger.info("Duplicate PDF Record inserting (" + file.getOriginalFilename() + ")");
			BatchUpload.Key batchUploadKey = new BatchUpload.Key(year, "missingDeductorTan", UploadTypes.SAP_REM.name(),
					UUID.randomUUID());
			batchUpload.setNewStatus("Duplicate");
			batchUpload.setKey(batchUploadKey);
			batchUpload.setReferenceId(batch.get(0).getKey().getId());
		} else {
			BatchUpload.Key batchUploadKey = new BatchUpload.Key(year, "missingDeductorTan", UploadTypes.SAP_REM.name(),
					UUID.randomUUID());
			batchUpload.setKey(batchUploadKey);
			batchUpload.setNewStatus("Uploaded");
			logger.info("Unique record creating (" + file.getOriginalFilename() + ")");
		}
		batchUpload.setFileName(file.getOriginalFilename());
		batchUpload.setFilePath("https://javastorageblob.blob.core.windows.net" + path);
		batchUpload.setCreatedDate(new Date());
		batchUpload.setCreatedBy("remediation user admin");
		batchUpload.setActive(true);
		batchUpload.setSuccessCount(0L);
		batchUpload.setFailedCount(0L);
		batchUpload.setRowsCount(0L);
		batchUpload = batchUploadRepository.save(batchUpload);

		return null;
	}   */

	public ByteArrayInputStream exportRemediationReport(UUID batchId, String mismatchcategory) throws IOException {
		return null;
	}

	//TODO NEED TO CHANGE FOR SQL
/*	public String updateRemediationReportForSap(MultipartFile file, String tenantId, String deductorTan)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException, ParseException {

		int year = Calendar.getInstance().get(Calendar.YEAR);
		int month = Calendar.getInstance().get(Calendar.YEAR) + 1;
		logger.info("Original file   " + file.getOriginalFilename());
		String sha256 = sha256SumService.getSHA256Hash(file);
		BatchUpload batchUpload = new BatchUpload();

		List<BatchUpload> batch = batchUploadRepository
				.getSha256RecordsBasedonYearMonth(year, month, UploadTypes.INV_REM.name(), sha256, Pagination.DEFAULT)
				.getData();

		String path = blob.uploadExcelToBlob(file, tenantId);
		if (!batch.isEmpty()) {
			logger.info("Duplicate PDF Record inserting " + file.getOriginalFilename());
			BatchUpload.Key batchUploadKey = new BatchUpload.Key(year, "missingDeductorTan", UploadTypes.INV_REM.name(),
					UUID.randomUUID());
			batchUpload.setKey(batchUploadKey);
			batchUpload.setReferenceId(batch.get(0).getKey().getId());
		} else {
			BatchUpload.Key batchUploadKey = new BatchUpload.Key(year, "missingDeductorTan", UploadTypes.INV_REM.name(),
					UUID.randomUUID());
			batchUpload.setKey(batchUploadKey);
			logger.info(" Unique record creating " + file.getOriginalFilename());
		}
		batchUpload.setFilePath(path);
		batchUpload.setCreatedDate(new Date());
		batchUpload.setCreatedBy("remediation user");
		batchUpload.setActive(true);
		batchUpload.setSuccessCount(0L);
		batchUpload.setFailedCount(0L);
		batchUpload.setRowsCount(0L);
		batchUpload.setProcessedCount(0);
		batchUpload.setDuplicateCount(0L);
		batchUpload.setMismatchCount(0L);

		batchUpload = batchUploadRepository.save(batchUpload);
		logger.info("Created succcessfully");

		Biff8EncryptionKey.setCurrentUserPassword("password");
		try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
			XSSFSheet worksheet = workbook.getSheetAt(0);
			worksheet.setColumnHidden(15, false);
			worksheet.setColumnHidden(16, false);
			worksheet.setColumnHidden(17, false);
			worksheet.setColumnHidden(18, false);

			DataFormatter formatter = new DataFormatter();
			int in = 6;
			while (in <= worksheet.getLastRowNum()) {
				XSSFRow row = worksheet.getRow(in++);
				String userAction = formatter.formatCellValue(row.getCell(19));
				if (row.getCell(in) != null) {
					UUID batchUploadId = UUID.fromString(formatter.formatCellValue(row.getCell(15)));
					UUID invoiceId = UUID.fromString(formatter.formatCellValue(row.getCell(16)));
					UUID invoiceLineItem = UUID.fromString(formatter.formatCellValue(row.getCell(17)));
					String category = formatter.formatCellValue(row.getCell(18));
					// TODO these fields must be available
					Integer assessmentYear = Integer.valueOf(formatter.formatCellValue(row.getCell(25)));
					Date documentPostingDate = new SimpleDateFormat("dd/MM/yyyy")
							.parse(formatter.formatCellValue(row.getCell(26)));

					InvoiceMismatch.Key invoiceKey = new InvoiceMismatch.Key(Calendar.getInstance().get(Calendar.YEAR),
							Calendar.getInstance().get(Calendar.MONTH) + 1, batchUploadId, category, invoiceId);
					Optional<InvoiceMismatch> invoiceMismatchOptional = invoiceMismatchRepository.findById(invoiceKey);
					InvoiceMismatch invoiceMismatchData = null;

					if (invoiceMismatchOptional.isPresent()) {
						invoiceMismatchData = invoiceMismatchOptional.get();
						if (invoiceMismatchData.getActive()) {
							invoiceMismatchData.setActive(false);
							invoiceMismatchRepository.save(invoiceMismatchData);
						}
						InvoiceLineItem.Key lineItemKey = new InvoiceLineItem.Key(assessmentYear, deductorTan,
								documentPostingDate, invoiceLineItem);
						Optional<InvoiceLineItem> invoiceLineItemOptional = invoiceLineItemRepository
								.findById(lineItemKey);
						InvoiceLineItem invoiceLineItemData = null;
						if (invoiceLineItemOptional.isPresent()) {
							invoiceLineItemData = invoiceLineItemOptional.get();
							if (userAction.equals("Accept")) {
								invoiceLineItemData.setFinalTdsSection(formatter.formatCellValue(row.getCell(1)));
								invoiceLineItemData
										.setFinalTdsRate(new BigDecimal(formatter.formatCellValue(row.getCell(2))));
							} else if (userAction.equals("Modify")) {
								invoiceLineItemData.setFinalReason(formatter.formatCellValue(row.getCell(20)));
								invoiceLineItemData
										.setFinalTdsRate(new BigDecimal(formatter.formatCellValue(row.getCell(21))));
								invoiceLineItemData.setFinalTdsSection(formatter.formatCellValue(row.getCell(22)));
							} else if (userAction.equals("Reject")) {
								invoiceLineItemData.setFinalTdsSection(formatter.formatCellValue(row.getCell(5)));
								invoiceLineItemData
										.setFinalTdsRate(new BigDecimal(formatter.formatCellValue(row.getCell(6))));
							}
							invoiceLineItemRepository.save(invoiceLineItemData);
						}
					}

				}
			}
		}
		return "Record Successfully Updated ";
	}  */
}