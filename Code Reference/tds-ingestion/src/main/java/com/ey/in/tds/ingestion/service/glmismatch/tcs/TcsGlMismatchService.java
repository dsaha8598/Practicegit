package com.ey.in.tds.ingestion.service.glmismatch.tcs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterDTO;
import com.ey.in.tds.common.service.Sha256SumService;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.feign.client.OnboardingClient;
import com.ey.in.tds.ingestion.dto.invoice.InvoiceMismatchUpdateDTO;
import com.microsoft.azure.storage.StorageException;

@Service
public class TcsGlMismatchService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private BlobStorage blob;

	@Autowired
	private Sha256SumService sha256SumService;


	@Autowired
	private OnboardingClient onboardingClient;

	/**
	 * Get get all the mismatches list
	 * 
	 * @return list of InvoiceMismatchAllDTO's
	 */
	//TODO NEED TO CHANGE FOR SQL
/*	public PagedData<InvoiceMismatchByBatchIdDTO> getInvoiceMismatchAllForGl(int year, int month,
			Pagination pagination) {
		List<InvoiceMismatchByBatchIdDTO> listGlMisMatchAllDTO = new ArrayList<>();

		PagedData<GeneralLedger> listGlMisMatch = generalLedgerRepository
				.getGeneralLedgerByAssessmentYearAssessmentMonth(year, month, pagination);

		listGlMisMatchAllDTO.add(groupMismatchesByType("SM-RM-AMM", listGlMisMatch.getData(), null));
		listGlMisMatchAllDTO.add(groupMismatchesByType("SM-RMM-AM", listGlMisMatch.getData(), null));
		listGlMisMatchAllDTO.add(groupMismatchesByType("SMM-RMM-AMM", listGlMisMatch.getData(), null));
		listGlMisMatchAllDTO.add(groupMismatchesByType("SM-RMM-AMM", listGlMisMatch.getData(), null));
		listGlMisMatchAllDTO.add(groupMismatchesByType("SMM-RM-AMM", listGlMisMatch.getData(), null));
		listGlMisMatchAllDTO.add(groupMismatchesByType("SMM-RM-AM", listGlMisMatch.getData(), null));
		listGlMisMatchAllDTO.add(groupMismatchesByType("NAD", listGlMisMatch.getData(), null));
		if (logger.isDebugEnabled()) {
			logger.debug("Invoice mismatch for gl " + listGlMisMatchAllDTO);
		}
		return new PagedData<>(listGlMisMatchAllDTO, listGlMisMatch.getPageSize(), listGlMisMatch.getPageStates());
	}  */

/*	public InvoiceMismatchByBatchIdDTO groupMismatchesByType(String type, List<GeneralLedger> listGlMismatches,
			UUID id) {
		InvoiceMismatchByBatchIdDTO invoiceMismatchByBatchIdDTO = new InvoiceMismatchByBatchIdDTO();

		invoiceMismatchByBatchIdDTO.setId(id);
		invoiceMismatchByBatchIdDTO.setMismatchcategory(type);

		Long i = 0L;
		BigDecimal lowDeductionAmount = new BigDecimal(0).setScale(2, RoundingMode.UP);

		BigDecimal excessDeductionAmount = new BigDecimal(0).setScale(2, RoundingMode.UP);

		BigDecimal tdsSystemAmount = new BigDecimal(0).setScale(2, RoundingMode.UP);

		BigDecimal tdsClientAmount = new BigDecimal(0).setScale(2, RoundingMode.UP);

		BigDecimal invoiceValue = new BigDecimal(0).setScale(2, RoundingMode.UP);

		if (listGlMismatches.size() > 0) {

			// TODO: Need to write the logic for mismatch calculations
			/*
			 * for (GeneralLedger generalLEdger : listGlMismatches) { } // end for loop
			 */
		/*	invoiceMismatchByBatchIdDTO.setTotalRecords(BigDecimal.valueOf(i++));
			invoiceMismatchByBatchIdDTO.setInvoiceValue(invoiceValue);
			invoiceMismatchByBatchIdDTO.setTdsSystemAmount(tdsSystemAmount);
			invoiceMismatchByBatchIdDTO.setTdsClientAmount(tdsClientAmount);
			invoiceMismatchByBatchIdDTO.setExcessDeduction(excessDeductionAmount.abs());
			invoiceMismatchByBatchIdDTO.setShortDeduction(lowDeductionAmount.abs());

			return invoiceMismatchByBatchIdDTO;
		} else {
			return invoiceMismatchByBatchIdDTO;
		}
	}  */

	/**
	 * 
	 * @param invoiceMismatchUpdateDTO
	 */
	public void updateMismatchByActionForGl(InvoiceMismatchUpdateDTO invoiceMismatchUpdateDTO) {
		// TODO Auto-generated method stub
	}

	public ByteArrayInputStream exportRemediationReport(String deductorTan, String tenantId, String deductorPan)
			throws IOException {

		return null;
	}

	//TODO NEED TO CHANGE FOR SQL
	/*public List<InvoiceMismatchUpdateDTO> updateRemediationReportForGl(MultipartFile file, String tenantId,
			String userName) throws IOException, InvalidKeyException, URISyntaxException, StorageException {

		int year = Calendar.getInstance().get(Calendar.YEAR);
		int month = Calendar.getInstance().get(Calendar.YEAR) + 1;
		logger.info("Original file   " + file.getOriginalFilename());
		String sha256 = sha256SumService.getSHA256Hash(file);
		BatchUpload batchUpload = new BatchUpload();

		List<BatchUpload> batch = batchUploadRepository
				.getSha256RecordsBasedonYearMonth(year, month, UploadTypes.GL_REM.name(), sha256, Pagination.UNPAGED)
				.getData();
		String path = blob.uploadExcelToBlob(file, tenantId);
		if (!batch.isEmpty()) {
			logger.info("Duplicate PDF Record inserting " + file.getOriginalFilename());
			BatchUpload.Key batchUploadKey = new BatchUpload.Key(year, "missingDeductorTan", UploadTypes.GL_REM.name(),
					UUID.randomUUID());
			batchUpload.setKey(batchUploadKey);
			batchUpload.setReferenceId(batch.get(0).getKey().getId());
		} else {
			BatchUpload.Key batchUploadKey = new BatchUpload.Key(year, "missingDeductorTan", UploadTypes.GL_REM.name(),
					UUID.randomUUID());
			batchUpload.setKey(batchUploadKey);
			logger.info(" Unique record creating " + file.getOriginalFilename());
		}
		batchUpload.setFileName(file.getOriginalFilename());
		batchUpload.setFilePath("https://javastorageblob.blob.core.windows.net" + path);
		batchUpload.setModifiedBy(userName);
		batchUpload.setModifiedDate(new Date());
		batchUpload.setActive(true);
		batchUpload.setSuccessCount(0L);
		batchUpload.setFailedCount(0L);
		batchUpload.setRowsCount(0L);
		batchUpload = batchUploadRepository.save(batchUpload);

		Biff8EncryptionKey.setCurrentUserPassword("password");
		List<InvoiceMismatchUpdateDTO> invoceMisMatchUpdateDtoList = new ArrayList<>();
		try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
			XSSFSheet worksheet = workbook.getSheetAt(0);
			DataFormatter formatter = new DataFormatter();
			InvoiceMismatchUpdateDTO invoiceMismatch = null;
			int in = 6;
			while (in <= worksheet.getLastRowNum()) {
				XSSFRow row = worksheet.getRow(in++);
				invoiceMismatch = new InvoiceMismatchUpdateDTO();
				if (row.getCell(in) != null) {
					String userAction = formatter.formatCellValue(row.getCell(16));
					invoiceMismatch.setSequenceNumber(formatter.formatCellValue(row.getCell(0)));
					invoiceMismatch.setSectionsTdsSection(formatter.formatCellValue(row.getCell(1)));
					// invoiceMismatch.setSectionsTdsRate(formatter.formatCellValue(row.getCell(2)));
					invoiceMismatch.setToolTdsSection(formatter.formatCellValue(row.getCell(3)));
					// invoiceMismatch.setToolTdsRate(formatter.formatCellValue(row.getCell(4)));
					invoiceMismatch.setMismatchSection(formatter.formatCellValue(row.getCell(5)));
					invoiceMismatch.setMismatchRate(formatter.formatCellValue(row.getCell(6)));
					invoiceMismatch.setMismatchDeductionType(formatter.formatCellValue(row.getCell(7)));
					invoiceMismatch.setMismatchInterpretation(formatter.formatCellValue(row.getCell(8)));
					invoiceMismatch.setDeductorTan(formatter.formatCellValue(row.getCell(9)));
					invoiceMismatch.setDeducteePan(formatter.formatCellValue(row.getCell(10)));
					invoiceMismatch.setNameOfTheDeductee(formatter.formatCellValue(row.getCell(11)));
					invoiceMismatch.setServiceDescriptionInvoice(formatter.formatCellValue(row.getCell(12)));
					invoiceMismatch.setServiceDescriptionPo(formatter.formatCellValue(row.getCell(13)));
					invoiceMismatch.setServiceDescriptionGlText(formatter.formatCellValue(row.getCell(14)));
					invoiceMismatch.setHashCode(formatter.formatCellValue(row.getCell(15)));
					invoiceMismatch.setActionTaken(formatter.formatCellValue(row.getCell(16)));
					invoiceMismatch.setActionReason(formatter.formatCellValue(row.getCell(17)));
					if (userAction.equals("Accept")) {
						invoiceMismatch.setFinalTdsSection(formatter.formatCellValue(row.getCell(1)));
						// invoiceMismatch.setFinalTdsRate(formatter.formatCellValue(row.getCell(2)));
					} else if (userAction.equals("Modify")) {
						// invoiceMismatch.setFinalTdsRate(formatter.formatCellValue(row.getCell(18)));
						invoiceMismatch.setFinalTdsSection(formatter.formatCellValue(row.getCell(19)));
					} else if (userAction.equals("Reject")) {
						invoiceMismatch.setFinalTdsSection(formatter.formatCellValue(row.getCell(5)));
						// invoiceMismatch.setFinalTdsRate(formatter.formatCellValue(row.getCell(6)));
					}

					invoceMisMatchUpdateDtoList.add(invoiceMismatch);
				}
			}
		}
		return invoceMisMatchUpdateDtoList;
	}  */

/*	public GLCategoryDTO convertGLModelToDTO(GeneralLedger generalLedger) {
		GLCategoryDTO gLCategoryDTO = null;
		try {
			if (generalLedger != null) {
				gLCategoryDTO = new GLCategoryDTO();
				gLCategoryDTO.setId(generalLedger.getKey().getId());
				gLCategoryDTO.setSourceIdentifier(generalLedger.getSourceIdentifier());
				gLCategoryDTO.setSourceFileName(generalLedger.getSourceFileName());
				gLCategoryDTO.setCompanyCode(generalLedger.getCompanyCode());
				gLCategoryDTO.setNameOfTheCompanyCode(generalLedger.getNameOfTheCompanyCode());
				gLCategoryDTO.setDeductorPAN(generalLedger.getDeductorPan());
				gLCategoryDTO.setDeductorTAN(generalLedger.getDeductorTan());
				gLCategoryDTO.setDeductorGSTIN(generalLedger.getDeductorGstin());
				gLCategoryDTO.setDeductorCode(generalLedger.getDeductorCode());
				gLCategoryDTO.setNameOfTheDeductee(generalLedger.getNameOfTheDeductee());
				gLCategoryDTO.setNonResidentDeducteeIndicator(generalLedger.getNonResidentDeducteeIndicator());
				gLCategoryDTO.setDeducteePAN(generalLedger.getDeducteePan());
				gLCategoryDTO.setDeducteeTIN(generalLedger.getDeducteeTin());
				gLCategoryDTO.setDeducteeGSTIN(generalLedger.getDeducteeGstin());
				gLCategoryDTO.setDocumentNumber(generalLedger.getDocumentNumber());
				gLCategoryDTO.setDocumentType(generalLedger.getDocumentType());
				gLCategoryDTO.setDocumentDate(generalLedger.getDocumentDate());
				gLCategoryDTO.setPostingDateOfDocument(generalLedger.getPostingDate());
				gLCategoryDTO.setLineItemNumber(generalLedger.getLineItemNumber());
				gLCategoryDTO.setAccountCode(generalLedger.getAccountCode());
				gLCategoryDTO.setAccountDescription(generalLedger.getAccountDescription());
				gLCategoryDTO.setTdsSection(generalLedger.getFinalTdsSection());
				gLCategoryDTO.setInvoiceAmountInLocalCurrency(generalLedger.getInvoiceAmountInLocalCurrency());
				gLCategoryDTO.setMiroMumber(generalLedger.getMiroDocumentNumber());
				gLCategoryDTO.setMigoNumber(generalLedger.getMigoDocumentNumber());
				gLCategoryDTO.setReferenceNumber(generalLedger.getReferenceNumber());
				gLCategoryDTO.setUserDefinedField1(generalLedger.getUserDefinedField_1());
				gLCategoryDTO.setUserDefinedField2(generalLedger.getUserDefinedField_2());
				gLCategoryDTO.setUserDefinedField3(generalLedger.getUserDefinedField_3());
				gLCategoryDTO.setClientAmount(generalLedger.getClientAmount());
				gLCategoryDTO.setClientSection(generalLedger.getClientTdsSection());
				gLCategoryDTO.setToolDerivedAmount(generalLedger.getToolDerivedAmount());
				gLCategoryDTO.setToolDerivedSection(generalLedger.getToolDerivedTdsSection());
			}
		} catch (Exception e) {
			logger.error("Exception occurred while convertGLModelToDTO : ", e);
		}
		return gLCategoryDTO;
	}  */

	@SuppressWarnings("unused")
	private ByteArrayOutputStream generateErrorReport(MultipartFile file, String tan, String tenantId,
			String deductorPan) throws IOException {

		return null;

	}

	@SuppressWarnings("unused")
	private String sendFileToBlobStorage(ByteArrayOutputStream out, String tenantId)
			throws IOException, URISyntaxException, InvalidKeyException, StorageException {
		File file = getConvertedExcelFile(out);
		logger.info("Original file   " + file.getName());
		String paths = blob.uploadExcelToBlobWithFile(file, tenantId);
		return paths;
	}

	private File getConvertedExcelFile(ByteArrayOutputStream out) throws FileNotFoundException, IOException {
		byte[] bytes = out.toByteArray();
		File someFile = new File("RemediationError_Report.xlsx");
		try (FileOutputStream fos = new FileOutputStream(someFile)) {
			fos.write(bytes);
			fos.flush();
			fos.close();
			return someFile;
		}
	}

	public String getErrorReportMsg(String deductorMasterTan, String tenantId, String deductorPan) {
		// feign client to get Deductor Name
		ResponseEntity<ApiStatus<DeductorMasterDTO>> getDeductor = onboardingClient.getDeductorByPan(deductorPan,
				tenantId);
		DeductorMasterDTO deductorData = getDeductor.getBody().getData();

		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

		String date = simpleDateFormat.format(new Date());
		return "General Ledger Remediation Report (Dated: " + date + ")\n Client Name: " + deductorData.getDeductorName()
				+ "\n";
	}

}