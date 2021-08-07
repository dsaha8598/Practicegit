package com.ey.in.tds.ingestion.web.rest.invoice.tcs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.ey.in.tcs.common.model.payment.PaymentMismatchByBatchIdDTO;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.tcs.TCSBatchUpload;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.TcsPaymentDTO;
import com.ey.in.tds.common.dto.CommonDTO;
import com.ey.in.tds.common.dto.invoice.MismatchesFiltersDTO;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.common.service.BlobStorageService;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.core.exceptions.TanNotFoundException;
import com.ey.in.tds.core.security.AllowedMimeTypes;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.ingestion.dto.invoice.UpdateOnScreenDTO;
import com.ey.in.tds.ingestion.service.batchupload.TCSBatchUploadService;
import com.ey.in.tds.ingestion.tcs.dao.PaymentDAO;
import com.ey.in.tds.ingestion.tcs.service.PaymentService;
import com.microsoft.azure.storage.StorageException;

import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvReader;

@RestController
@RequestMapping("/api/ingestion/tcs")
public class PaymentResource {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private PaymentService paymentService;
	
	@Autowired
	private PaymentDAO paymentDAO;

	@Autowired
	private TCSBatchUploadService tcsBatchUploadService;

	@Autowired
	private BlobStorageService blobStorageService;
	/**
	 * Update mismatch table based on Mismatch category for Excel
	 * 
	 * @param mismatchcategory
	 * @param advanceMismatchUpdateDTO
	 * @return
	 * @throws RecordNotFoundException
	 * @throws TanNotFoundException
	 */
	@PutMapping("/payment/{mismatchcategory}/action")
	public ResponseEntity<ApiStatus<UpdateOnScreenDTO>> uploadExcel(@RequestHeader(value = "TAN-NUMBER") String tan,
			@PathVariable String mismatchcategory, @RequestBody UpdateOnScreenDTO paymentMismatchUpdateDTO,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader("X-TENANT-ID") String tenantId)
			throws RecordNotFoundException, TanNotFoundException {
		// Handling null pointer exception
		if (tan == null && mismatchcategory == null) {
			throw new CustomException("Tan / Mismatch Category  Not Found", HttpStatus.BAD_REQUEST);
		}

		UpdateOnScreenDTO response = paymentService.updateMismatchByAction(tan, paymentMismatchUpdateDTO, userName,tenantId);

		ApiStatus<UpdateOnScreenDTO> apiStatus = new ApiStatus<UpdateOnScreenDTO>(HttpStatus.OK, "SUCCESS",
				"LIST OF MISMATCHES UPDATED", response);
		return new ResponseEntity<ApiStatus<UpdateOnScreenDTO>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param deductorMasterTan
	 * @param batchId
	 * @return
	 * @throws TanNotFoundException
	 */
	@GetMapping("/payment/{batchId}/mismatchSummary")
	public ResponseEntity<ApiStatus<List<PaymentMismatchByBatchIdDTO>>> paymentMismatchSummaryByTanAndId(
			@RequestHeader(value = "TAN-NUMBER") String deductorMasterTan, @PathVariable Integer batchId,
			@RequestHeader("X-TENANT-ID") String tenantId)
			throws TanNotFoundException {
		if (deductorMasterTan != null) {
			List<PaymentMismatchByBatchIdDTO> paymentMismatchSummaryTanAndIdList = paymentService
					.getPaymentMismatchSummaryByTanAndId(deductorMasterTan, batchId);
			logger.info("Batch Id : {}", batchId);

			ApiStatus<List<PaymentMismatchByBatchIdDTO>> apiStatus = new ApiStatus<List<PaymentMismatchByBatchIdDTO>>(
					HttpStatus.OK, "SUCCESS", "LIST OF ADVANCE MISMATCH SUMMARY BASED ON BATCH ID",
					paymentMismatchSummaryTanAndIdList);
			return new ResponseEntity<ApiStatus<List<PaymentMismatchByBatchIdDTO>>>(apiStatus, HttpStatus.OK);

		} else {
			logger.error("Tan not Found");
			throw new CustomException("Tan Not Found", HttpStatus.BAD_REQUEST);
		}
	}
	/**
	 * 
	 * @param deductorMasterTan
	 * @param year
	 * @param month
	 * @return
	 * @throws TanNotFoundException
	 */
	@GetMapping(value = "/payment/all/mismatchSummary/{year}/{month}")
	public ResponseEntity<ApiStatus<List<PaymentMismatchByBatchIdDTO>>> paymentMismatchByTanAndActive(
			@RequestHeader(value = "TAN-NUMBER") String deductorMasterTan, @PathVariable int year,
			@PathVariable int month, @RequestHeader("X-TENANT-ID") String tenantId) throws TanNotFoundException {
		if (deductorMasterTan != null) {
			List<PaymentMismatchByBatchIdDTO> paymentMismatchTanAndActivelist = paymentService
					.getAllPaymentMismatchSummaryByTanAndActive(deductorMasterTan, year, month);
			logger.info("Payments in Mismatch Summary by BatchID done.");

			ApiStatus<List<PaymentMismatchByBatchIdDTO>> apiStatus = new ApiStatus<List<PaymentMismatchByBatchIdDTO>>(
					HttpStatus.OK, "SUCCESS", "LIST OF ADVANCE MISMATCH SUMMARY", paymentMismatchTanAndActivelist);
			return new ResponseEntity<ApiStatus<List<PaymentMismatchByBatchIdDTO>>>(apiStatus, HttpStatus.OK);

		} else {
			logger.error("Tan not Found");
			throw new CustomException("Tan Not Found", HttpStatus.BAD_REQUEST);
		}
	}
	/**
	 * 
	 * @param deductorMasterTan
	 * @param batchUploadId
	 * @param mismatchCategory
	 * @param pagination
	 * @return
	 * @throws TanNotFoundException
	 */
	@PostMapping(value = "/payment/{batchUploadId}/{mismatchCategory}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<TcsPaymentDTO>>> paymentAllMismatchSummaryByTanAndId(
			@RequestHeader(value = "TAN-NUMBER") String deductorMasterTan, @PathVariable Integer batchUploadId,
			@PathVariable String mismatchCategory, @RequestBody Pagination pagination,
			@RequestHeader("X-TENANT-ID") String tenantId) throws TanNotFoundException {
		if (deductorMasterTan != null) {
			CommonDTO<TcsPaymentDTO> tcsPaymentList = paymentService.getPaymentMismatchByTanBatchIdMismatchCategory(
					deductorMasterTan, batchUploadId, mismatchCategory, pagination);
			ApiStatus<CommonDTO<TcsPaymentDTO>> apiStatus = new ApiStatus<CommonDTO<TcsPaymentDTO>>(HttpStatus.OK,
					"SUCCESS", "NO ALERT", tcsPaymentList);
			return new ResponseEntity<ApiStatus<CommonDTO<TcsPaymentDTO>>>(apiStatus, HttpStatus.OK);
		} else {
			logger.error("Tan not Found");
			throw new CustomException("Tan Not Found", HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * 
	 * @param deductorMasterTan
	 * @param mismatchCategory
	 * @return
	 * @throws TanNotFoundException
	 */
	@PostMapping(value = "/payment/all/{mismatchCategory}/{year}/{month}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<TcsPaymentDTO>>> paymentAllMismatchByTanAndActive(
			@RequestHeader(value = "TAN-NUMBER") String deductorMasterTan, @PathVariable String mismatchCategory,
			@PathVariable int year, @PathVariable int month, @RequestBody MismatchesFiltersDTO filters,
			@RequestHeader("X-TENANT-ID") String tenantId)
			throws TanNotFoundException {
		logger.info("Entered to get all mismatch category");
		if (deductorMasterTan == null) {

			logger.error("Tan not Found");
			throw new CustomException("Tan Not Found", HttpStatus.BAD_REQUEST);
		}
		CommonDTO<TcsPaymentDTO> paymentAllMismatchTanAndActivelist = paymentService
				.getAllPaymentMismatchByTanAndMismatchCategory(deductorMasterTan, mismatchCategory, year, month,
						filters);
		logger.info("All Payments in Mismatch by BatchID done.");

		ApiStatus<CommonDTO<TcsPaymentDTO>> apiStatus = new ApiStatus<CommonDTO<TcsPaymentDTO>>(HttpStatus.OK, "SUCCESS",
				"NO ALERT", paymentAllMismatchTanAndActivelist);
		return new ResponseEntity<ApiStatus<CommonDTO<TcsPaymentDTO>>>(apiStatus, HttpStatus.OK);

	}	
	/**
	 * 
	 * @param file
	 * @param tenantId
	 * @param userName
	 * @return
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 */
	// For Import and Export Remediation
	@PostMapping("/paymentmismatches/remediation/import")
	public ResponseEntity<ApiStatus<TCSBatchUpload>> getStatusOfPaymentRemediationReport(
			@RequestParam("file") MultipartFile file, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {

		String contentType = new Tika().detect(file.getInputStream());
		if (AllowedMimeTypes.XLSX.getMimeType().equals(contentType)
				|| AllowedMimeTypes.OOXML.getMimeType().equals(contentType)) {
			TCSBatchUpload batchUpload = paymentService.importToPaymentBatchUpload(file, tenantId, userName);

			logger.info("Status of payment remediation report---{}", batchUpload);
			ApiStatus<TCSBatchUpload> apiStatus = new ApiStatus<TCSBatchUpload>(HttpStatus.OK, "SUCCESS",
					"UPLOADED ADVANCE FILE SUCCESSFULLY", batchUpload);
			return new ResponseEntity<ApiStatus<TCSBatchUpload>>(apiStatus, HttpStatus.OK);
		} else {
			logger.warn("Unsupported file upload attempt by {}", userName);
			throw new CustomException("Invalid file, only xlsx files are allowed", HttpStatus.BAD_REQUEST);
		}
	}
	/**
	 * 
	 * @param tan
	 * @param pan
	 * @param tenantId
	 * @param year
	 * @param month
	 * @param userName
	 * @return
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 */
	// DOWNLOAD CALL OF ADVANCE MISMATCH
	@PostMapping(value = "/paymentmismatchesreport/export", consumes = { "application/x-www-form-urlencoded" })
	public ResponseEntity<InputStreamResource> exportPaymentMismatchReport(
			@RequestParam(value = "tan", required = true) String tan,
			@RequestParam(value = "pan", required = true) String pan,
			@RequestParam(value = "tenantId", required = true) String tenantId,
			@RequestParam(value = "year", required = true) Integer year,
			@RequestParam(value = "month", required = true) Integer month,
			@RequestHeader(value = "USER_NAME") String userName)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {
        MultiTenantContext.setTenantId(tenantId);
		logger.info("TAN: {}", tan);

		if (tan == null) {
			throw new CustomException("No Tan Found to continue the download", HttpStatus.BAD_REQUEST);
		}

		ByteArrayInputStream in = paymentService.exportRemediationReport(tan, tenantId, pan, year, month, userName);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=Payment_Remediation_Report_" + tan + ".xlsx");

		logger.info("Remediation report export done.");
		return ResponseEntity.ok().headers(headers).body(new InputStreamResource(in));
	}
	/**
	 * 
	 * @param tenantId
	 * @param tan
	 * @param pan
	 * @param year
	 * @param month
	 * @param userName
	 * @return
	 * @throws IOException
	 * @throws TanNotFoundException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 */
	@PostMapping(value = "/paymentmismatchesreport/async/export")
	public ResponseEntity<ApiStatus<String>> asyncExportPaymentMismatchReport(
			@RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "TAN-NUMBER", required = true) String tan,
			@RequestHeader(value = "DEDUCTOR-PAN", required = true) String pan,
			@RequestParam(value = "year", required = true) Integer year,
			@RequestParam(value = "month", required = true) Integer month,
			@RequestHeader(value = "USER_NAME") String userName)
			throws IOException, TanNotFoundException, InvalidKeyException, URISyntaxException, StorageException {

		logger.info("TAN: {}", tan);

		if (tan == null) {
			throw new CustomException("No Tan Found to continue the download", HttpStatus.BAD_REQUEST);
		}

		paymentService.asyncExportRemediationReport(tan, tenantId, pan, year, month, userName);

		String message = "Payment mismatch report requested successfully and will be available shortly";
		logger.info("{}", message);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", message, message);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}

	/**
	 * calculating payment matrix values
	 * 
	 * @param deductorTan
	 * @return
	 * @throws CustomException
	 * @throws IOException
	 */
	@GetMapping("/payment/matrix/{year}")
	public ResponseEntity<ApiStatus<Map<Integer, Object>>> getPaymentMatrix(
			@RequestHeader(value = "TAN-NUMBER") String deductorTan, @PathVariable int year,
			@RequestHeader("X-TENANT-ID") String tenantId)
			throws CustomException, IOException {
		if (deductorTan == null) {
			if (logger.isErrorEnabled()) {
				logger.error("DeductorTan Not Found");
			}
			throw new CustomException("DeductorTan Not present to proceed..", HttpStatus.BAD_REQUEST);
		}
		Map<Integer, Object> payment = paymentService.getPaymentMatrix(deductorTan, year);

		if (logger.isInfoEnabled()) {
			logger.info("payment all matrix values.");
		}
		ApiStatus<Map<Integer, Object>> apiStatus = new ApiStatus<Map<Integer, Object>>(HttpStatus.OK, "SUCCESS",
				"LIST OF ADVANCE MATRIX DATA", payment);
		return new ResponseEntity<ApiStatus<Map<Integer, Object>>>(apiStatus, HttpStatus.OK);
	}
	/**
	 * 
	 * @param tan
	 * @param tenantId
	 * @param userName
	 * @param paymentData
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/payment/matrix/closingAmount/file/download")
	public ResponseEntity<ApiStatus<String>> paymentMatrixClosingAmountDownload(
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName, @RequestBody Map<String, String> paymentData)
			throws Exception {

		Integer year = Integer.valueOf(paymentData.get("year"));
		Integer month = Integer.valueOf(paymentData.get("month"));
		logger.info("TAN: {}", tan);

		if (tan == null) {
			throw new CustomException("No Tan Found to continue the download", HttpStatus.BAD_REQUEST);
		}
		paymentService.asyncPaymentMatrixClosingAmountDownload(tan, year, month, false, tenantId, userName,
				UploadTypes.PAYMENT_CLOSING_REPORT.name());
		String message = "Payment closing report requested successfully and will be available shortly";
		logger.info("{}", message);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", message, message);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}
	/**
	 * 
	 * @param tan
	 * @param tenantId
	 * @param userName
	 * @param paymentData
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/payment/matrix/openingAmount/file/download")
	public ResponseEntity<ApiStatus<String>> paymentMatrixOpeningAmountDownload(
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName, @RequestBody Map<String, String> paymentData)
			throws Exception {

		Integer year = Integer.valueOf(paymentData.get("year"));
		Integer batchUploadYear = year;
		Integer month = Integer.valueOf(paymentData.get("month"));
		logger.info("TAN: {}", tan);

		if (tan == null) {
			throw new CustomException("No Tan Found to continue the download", HttpStatus.BAD_REQUEST);
		}
		month = month - 1;
		if (month == 0) {
			month = 12;
		}
		if (month == 3) {
			year = year - 1;
		}

		paymentService.asyncPaymentMatrixOpeningAmountDownload(tan, year, month, true, tenantId, userName,
				UploadTypes.PAYMENT_OPENING_REPORT.name(), batchUploadYear);
		String message = "Payment opening report requested successfully and will be available shortly";
		logger.info("{}", message);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", message, message);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This method will download the file for the payments with for the months
	 * @param tan
	 * @param tenantId
	 * @param userName
	 * @param paymentData
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/payment/matrix/ftm/file/download")
	public ResponseEntity<ApiStatus<String>> paymentMatrixFtmDownload(@RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader(value = "USER_NAME") String userName,
			@RequestBody Map<String, String> paymentData) throws Exception {

		Integer year = Integer.valueOf(paymentData.get("year"));
		Integer month = Integer.valueOf(paymentData.get("month"));
		logger.info("TAN: {}", tan);

		if (tan == null) {
			throw new CustomException("No Tan Found to continue the download", HttpStatus.BAD_REQUEST);
		}
		paymentService.asyncPaymentMatrixFtmDownload(tan, year, month, tenantId, userName);
		String message = "Payment ftm report requested successfully and will be available shortly";
		logger.info("{}", message);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", message, message);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This method downloads file for payment adjusted amount
	 * @param tan
	 * @param tenantId
	 * @param userName
	 * @param paymentData
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/payment/matrix/adjusted/file/download")
	public ResponseEntity<ApiStatus<String>> paymentMatrixAdjustedFileDownload(
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName, @RequestBody Map<String, String> paymentData)
			throws Exception {

		Integer year = Integer.valueOf(paymentData.get("year"));
		Integer month = Integer.valueOf(paymentData.get("month"));
		logger.info("TAN: {}", tan);

		if (tan == null) {
			throw new CustomException("No Tan Found to continue the download", HttpStatus.BAD_REQUEST);
		}
		paymentService.asyncPaymentMatrixAdjustedFileDownload(tan, year, month, tenantId, userName);
		String message = "Payment adjusted report requested successfully and will be available shortly";
		logger.info("{}", message);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", message, message);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}
	/**
	 * 
	 * @param tan
	 * @param restype
	 * @param tenantId
	 * @param year
	 * @param month
	 * @param deducteeName
	 * @param pagination
	 * @return
	 * @throws RecordNotFoundException
	 * @throws TanNotFoundException
	 */
	@PostMapping(value = "/payment/{restype}/{year}/{month}/{deducteeName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<TcsPaymentDTO>>> getResidentAndNonresident(
			@RequestHeader(value = "TAN-NUMBER") String tan, @PathVariable String restype,
			@RequestHeader("X-TENANT-ID") String tenantId, @PathVariable int year, @PathVariable int month,
			@PathVariable String deducteeName, @RequestBody Pagination pagination)
			throws RecordNotFoundException, TanNotFoundException {
		logger.info("X-TENANT-ID _______: {}", tenantId);

		CommonDTO<TcsPaymentDTO> response = null;
		if (tan != null && restype != null) {
			response = paymentService.getResidentAndNonresident(restype, tan, year, month, pagination, deducteeName);
		} else {
			logger.error("Tan/ rest type not found to proceed");
			throw new CustomException("Tan/ rest type not found to proceed", HttpStatus.BAD_REQUEST);
		}
		
		logger.info("List of Resident and NonResident responses done");
		ApiStatus<CommonDTO<TcsPaymentDTO>> apiStatus = new ApiStatus<CommonDTO<TcsPaymentDTO>>(HttpStatus.OK, "SUCCESS",
				"NO ALERT", response);
		return new ResponseEntity<ApiStatus<CommonDTO<TcsPaymentDTO>>>(apiStatus, HttpStatus.OK);

	}
	
	/**
	 * 
	 * @param assessmentYear
	 * @param deductorTan
	 * @param challanPaid
	 * @param isForNonResidents
	 * @param bsrCode
	 * @param receiptSerailNo
	 * @param receiptDate
	 * @return
	 */
	@GetMapping(value = "/payment/serialNo/BSRCode/recieptDate", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<TcsPaymentDTO>>> getAdvacnceLineItemsByReceiptSerialNoAndBSRCodeAndReceiptDateTCS(
			@RequestParam("year")Integer assessmentYear,@RequestParam("deductorTan")String deductorTan,
			@RequestParam("challanPaid") boolean challanPaid,@RequestParam("isForNonResidents") boolean isForNonResidents,
		    @RequestParam("bsrCode")String bsrCode,@RequestParam("receiptSerailNo") String receiptSerailNo,
			@RequestParam("receiptDate")String receiptDate,@RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestParam("months") List<Integer> months,
			@RequestParam(value = "section", required = false) String section){
		MultiTenantContext.setTenantId(tenantId);
		List<TcsPaymentDTO> list=paymentDAO.getAdvacnceLineItemsByReceiptSerialNoAndBSRCodeAndReceiptDate(assessmentYear, deductorTan,
				challanPaid, isForNonResidents, bsrCode, receiptSerailNo, receiptDate,months,section);
		ApiStatus<List<TcsPaymentDTO>> apiStatus = new ApiStatus<List<TcsPaymentDTO>>(HttpStatus.OK, "SUCCESS",
				"NO ALERT", list);
		return new ResponseEntity<ApiStatus<List<TcsPaymentDTO>>>(apiStatus, HttpStatus.OK);
	}
	
	
	/**
	 * 
	 * @param assessmentYear
	 * @param challanMonth
	 * @param collectorTan
	 * @param pan
	 * @param tenantId
	 * @return
	 */
	@PostMapping(value = "/advance/adjustments")
	public ResponseEntity<ApiStatus<String>> advanceAdjustments(@RequestParam("assessmentYear") Integer assessmentYear,
			@RequestParam("challanMonth") Integer challanMonth,
			@RequestHeader(value = "TAN-NUMBER") String collectorTan, @RequestHeader(value = "DEDUCTOR-PAN") String pan,
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader(value = "USER_NAME") String userName) {
		TCSBatchUpload batchData = paymentService.advanceAdjustments(assessmentYear, collectorTan, tenantId, pan,
				challanMonth, userName);
		String message;
		if(batchData == null) {
			message = "Already Processed For This Month";
		}else {
			message = "Payment adjustments happened successfully ";
		}
		logger.info("{}", message);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", message, message);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param response
	 * @param deductorTan
	 * @param deductorPan
	 * @param batchId
	 * @param tenantId
	 * @return
	 * @throws IOException
	 */
	@PostMapping(value = "/payment/download/stream", consumes = { "application/x-www-form-urlencoded" })
	public ResponseEntity<StreamingResponseBody> getStream(HttpServletResponse response,
			@RequestParam("deductorTan") String deductorTan, @RequestParam("deductorPan") String deductorPan,
			@RequestParam("batchId") String batchId, @RequestHeader("X-TENANT-ID") String tenantId) throws IOException {
		Calendar calendar = Calendar.getInstance();
		logger.info("date: {}", Calendar.getInstance().toInstant());
		String fileName = "payment_mismach_report_" + deductorTan + "_" + calendar.get(Calendar.YEAR) + "_"
				+ calendar.get(Calendar.MONTH);
		response.addHeader("Content-disposition", "attachment;filename="+fileName+".zip");
		response.setContentType("application/octet-stream");
		
		TCSBatchUpload batchUpload = tcsBatchUploadService.getTCSBatchUpload(Integer.valueOf(batchId));
		File filezip = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getOtherFileUrl());
		File csvFile = File.createTempFile("blob", "csv");
		paymentService.decompressGzip(filezip.toPath(), csvFile.toPath());
		CsvReader csvReader = new CsvReader();
		csvReader.setContainsHeader(true);
		CsvContainer csv = csvReader.read(csvFile, StandardCharsets.UTF_8);
		ByteArrayInputStream byteArrayInputStream = paymentService.generatePaymentExcel(csv.getRowCount(), csv,
				deductorTan, tenantId, deductorPan);
		StreamingResponseBody stream = out -> {
			final ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream());
			try {
				final ZipEntry zipEntry = new ZipEntry(fileName+".xlsx");
				zipOut.putNextEntry(zipEntry);
				byte[] bytes = new byte[1024];
				int length;
				while ((length = byteArrayInputStream.read(bytes)) >= 0) {
					zipOut.write(bytes, 0, length);
				}
				zipOut.closeEntry();
				byteArrayInputStream.close();
				zipOut.close();
			} catch (final IOException e) {
				logger.error("Exception while reading and streaming data {} ", e);
			}
		};
		// Cleaning up the Temp Files Created
		filezip.delete();
		csvFile.delete();
		response.setStatus(HttpServletResponse.SC_OK);
		return new ResponseEntity<StreamingResponseBody>(stream, HttpStatus.OK);
	}

}
