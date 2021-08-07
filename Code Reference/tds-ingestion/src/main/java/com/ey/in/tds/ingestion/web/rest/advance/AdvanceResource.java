package com.ey.in.tds.ingestion.web.rest.advance;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.text.ParseException;
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

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.AdvanceDTO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.dto.CommonDTO;
import com.ey.in.tds.common.dto.invoice.MismatchesFiltersDTO;
import com.ey.in.tds.common.model.advance.AdvanceMismatchByBatchIdDTO;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.common.service.BlobStorageService;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.core.exceptions.TanNotFoundException;
import com.ey.in.tds.core.security.AllowedMimeTypes;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.ingestion.dto.invoice.UpdateOnScreenDTO;
import com.ey.in.tds.ingestion.jdbc.dao.AdvanceDAO;
import com.ey.in.tds.ingestion.service.advance.AdvanceService;
import com.ey.in.tds.ingestion.service.batchupload.BatchUploadService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.microsoft.azure.storage.StorageException;

import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvReader;

@RestController
@RequestMapping("/api/ingestion")
public class AdvanceResource {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	AdvanceService advanceService;

	@Autowired
	private AdvanceDAO advanceDAO;

	@Autowired
	private BlobStorageService blobStorageService;

	@Autowired
	private BatchUploadService batchUploadService;

	/**
	 * Update mismatch table based on Mismatch category for Excel
	 * 
	 * @param mismatchcategory
	 * @param advanceMismatchUpdateDTO
	 * @return
	 * @throws RecordNotFoundException
	 * @throws TanNotFoundException
	 */
	@PutMapping("advance/{mismatchcategory}/action")
	public ResponseEntity<ApiStatus<UpdateOnScreenDTO>> uploadExcel(@RequestHeader(value = "TAN-NUMBER") String tan,
			@PathVariable String mismatchcategory, @RequestBody UpdateOnScreenDTO advanceMismatchUpdateDTO,
			@RequestHeader(value = "USER_NAME") String userName) throws RecordNotFoundException, TanNotFoundException {
		// Handling null pointer exception
		if (tan == null && mismatchcategory == null) {
			throw new CustomException("Tan / Mismatch Category  Not Found", HttpStatus.BAD_REQUEST);
		}

		UpdateOnScreenDTO response = advanceService.updateMismatchByAction(tan, advanceMismatchUpdateDTO, userName);

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
	@GetMapping("/advance/{batchId}/mismatchSummary")
	public ResponseEntity<ApiStatus<List<AdvanceMismatchByBatchIdDTO>>> advanceMismatchSummaryByTanAndId(
			@RequestHeader(value = "TAN-NUMBER") String deductorMasterTan, @PathVariable Integer batchId)
			throws TanNotFoundException {
		if (deductorMasterTan != null) {
			List<AdvanceMismatchByBatchIdDTO> advanceMismatchSummaryTanAndIdList = advanceService
					.getAdvanceMismatchSummaryByTanAndId(deductorMasterTan, batchId);
			logger.info("Batch Id : {}", batchId);

			ApiStatus<List<AdvanceMismatchByBatchIdDTO>> apiStatus = new ApiStatus<List<AdvanceMismatchByBatchIdDTO>>(
					HttpStatus.OK, "SUCCESS", "LIST OF ADVANCE MISMATCH SUMMARY BASED ON BATCH ID",
					advanceMismatchSummaryTanAndIdList);
			return new ResponseEntity<ApiStatus<List<AdvanceMismatchByBatchIdDTO>>>(apiStatus, HttpStatus.OK);

		} else {
			logger.error("Tan not Found");
			throw new CustomException("Tan Not Found", HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping(value = "/advance/all/mismatchSummary/{year}/{month}")
	public ResponseEntity<ApiStatus<List<AdvanceMismatchByBatchIdDTO>>> advanceMismatchByTanAndActive(
			@RequestHeader(value = "TAN-NUMBER") String deductorMasterTan, @PathVariable int year,
			@PathVariable int month) throws TanNotFoundException {
		if (deductorMasterTan != null) {
			List<AdvanceMismatchByBatchIdDTO> advanceMismatchTanAndActivelist = advanceService
					.getAllAdvanceMismatchSummaryByTanAndActive(deductorMasterTan, year, month);
			logger.info("Advances in Mismatch Summary by BatchID done.");

			ApiStatus<List<AdvanceMismatchByBatchIdDTO>> apiStatus = new ApiStatus<List<AdvanceMismatchByBatchIdDTO>>(
					HttpStatus.OK, "SUCCESS", "LIST OF ADVANCE MISMATCH SUMMARY", advanceMismatchTanAndActivelist);
			return new ResponseEntity<ApiStatus<List<AdvanceMismatchByBatchIdDTO>>>(apiStatus, HttpStatus.OK);

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
	@PostMapping(value = "/advance/{batchUploadId}/{mismatchCategory}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<AdvanceDTO>>> advanceAllMismatchSummaryByTanAndId(
			@RequestHeader(value = "TAN-NUMBER") String deductorMasterTan, @PathVariable Integer batchUploadId,
			@PathVariable String mismatchCategory, @RequestBody Pagination pagination) throws TanNotFoundException {
		if (deductorMasterTan != null) {
			CommonDTO<AdvanceDTO> advanceAllMismatchSummaryTanAndIdList = advanceService
					.getAdvanceMismatchByTanBatchIdMismatchCategory(deductorMasterTan, batchUploadId, mismatchCategory,
							pagination);
			logger.info("All Advances in Mismatch Summary by BatchID done.");

			ApiStatus<CommonDTO<AdvanceDTO>> apiStatus = new ApiStatus<CommonDTO<AdvanceDTO>>(HttpStatus.OK, "SUCCESS",
					"NO ALERT", advanceAllMismatchSummaryTanAndIdList);
			return new ResponseEntity<ApiStatus<CommonDTO<AdvanceDTO>>>(apiStatus, HttpStatus.OK);

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
	 * @throws JsonProcessingException 
	 * @throws JsonMappingException 
	 */
	@PostMapping(value = "/advance/all/{mismatchCategory}/{year}/{month}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<AdvanceDTO>>> advanceAllMismatchByTanAndActive(
			@RequestHeader(value = "TAN-NUMBER") String deductorMasterTan, @PathVariable String mismatchCategory,
			@PathVariable int year, @PathVariable int month, @RequestBody MismatchesFiltersDTO filters)
			throws TanNotFoundException, JsonMappingException, JsonProcessingException {
		logger.info("Entered to get all mismatch category");
		if (deductorMasterTan == null) {

			logger.error("Tan not Found");
			throw new CustomException("Tan Not Found", HttpStatus.BAD_REQUEST);
		}
		CommonDTO<AdvanceDTO> advanceAllMismatchTanAndActivelist = advanceService
				.getAllAdvanceMismatchByTanAndMismatchCategory(deductorMasterTan, mismatchCategory, year, month,
						filters);
		logger.info("All Advances in Mismatch by BatchID done.");

		ApiStatus<CommonDTO<AdvanceDTO>> apiStatus = new ApiStatus<CommonDTO<AdvanceDTO>>(HttpStatus.OK, "SUCCESS",
				"NO ALERT", advanceAllMismatchTanAndActivelist);
		return new ResponseEntity<ApiStatus<CommonDTO<AdvanceDTO>>>(apiStatus, HttpStatus.OK);

	}

	// For Import and Export Remediation
	@PostMapping("advancemismatches/remediation/import")
	public ResponseEntity<ApiStatus<BatchUpload>> getStatusOfAdvanceRemediationReport(
			@RequestParam("file") MultipartFile file, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {

		String contentType = new Tika().detect(file.getInputStream());
		if (AllowedMimeTypes.XLSX.getMimeType().equals(contentType)
				|| AllowedMimeTypes.OOXML.getMimeType().equals(contentType)) {
			BatchUpload batchUpload = advanceService.importToAdvanceBatchUpload(file, tenantId, userName);

			logger.info("Status of advance remediation report---{}", batchUpload);
			ApiStatus<BatchUpload> apiStatus = new ApiStatus<BatchUpload>(HttpStatus.OK, "SUCCESS",
					"UPLOADED ADVANCE FILE SUCCESSFULLY", batchUpload);
			return new ResponseEntity<ApiStatus<BatchUpload>>(apiStatus, HttpStatus.OK);
		} else {
			logger.warn("Unsupported file upload attempt by {}", userName);
			throw new CustomException("Invalid file, only xlsx files are allowed", HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * 
	 * @param tenantId
	 * @param tan
	 * @param pan
	 * @param year
	 * @param month
	 * @param userName
	 * @param isMismatch
	 * @return
	 * @throws IOException
	 * @throws TanNotFoundException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws ParseException
	 */
	@PostMapping(value = "/advancemismatchesreport/async/export/{isMismatch}")
	public ResponseEntity<ApiStatus<String>> asyncExportAdvanceMismatchReport(
			@RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "TAN-NUMBER", required = true) String tan,
			@RequestHeader(value = "DEDUCTOR-PAN", required = true) String pan,
			@RequestParam(value = "year", required = true) Integer year,
			@RequestParam(value = "month", required = true) Integer month,
			@RequestHeader(value = "USER_NAME") String userName,
			@PathVariable(value = "isMismatch", required = false) boolean isMismatch) throws IOException,
			TanNotFoundException, InvalidKeyException, URISyntaxException, StorageException, ParseException {

		logger.info("TAN: {}", tan);

		if (tan == null) {
			throw new CustomException("No Tan Found to continue the download", HttpStatus.BAD_REQUEST);
		}

		advanceService.asyncExportRemediationReport(tan, tenantId, pan, year, month, userName, isMismatch);

		String message = "Advance mismatch report requested successfully and will be available shortly";
		logger.info("{}", message);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", message, message);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}

	/**
	 * calculating advance matrix values
	 * 
	 * @param deductorTan
	 * @return
	 * @throws CustomException
	 * @throws IOException
	 */
	@GetMapping("/advance/matrix/{year}/{type}")
	public ResponseEntity<ApiStatus<Map<Integer, Object>>> getAdvanceMatrix(
			@RequestHeader(value = "TAN-NUMBER") String deductorTan, @PathVariable("year") int year,
			@PathVariable("type") String type) throws CustomException, IOException {
		if (deductorTan == null) {
			if (logger.isErrorEnabled()) {
				logger.error("DeductorTan Not Found");
			}
			throw new CustomException("DeductorTan Not present to proceed..", HttpStatus.BAD_REQUEST);
		}
		Map<Integer, Object> advance = advanceService.getAdvanceMatrix(deductorTan, year, type);

		if (logger.isInfoEnabled()) {
			logger.info("advance all matrix values.");
		}
		ApiStatus<Map<Integer, Object>> apiStatus = new ApiStatus<Map<Integer, Object>>(HttpStatus.OK, "SUCCESS",
				"LIST OF ADVANCE MATRIX DATA", advance);
		return new ResponseEntity<ApiStatus<Map<Integer, Object>>>(apiStatus, HttpStatus.OK);
	}

	@PostMapping(value = "/advance/matrix/closingAmount/file/download")
	public ResponseEntity<ApiStatus<String>> advanceMatrixClosingAmountDownload(
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName, @RequestBody Map<String, String> advanceData)
			throws Exception {

		Integer year = Integer.valueOf(advanceData.get("year"));
		Integer month = Integer.valueOf(advanceData.get("month"));
		logger.info("TAN: {}", tan);

		if (tan == null) {
			throw new CustomException("No Tan Found to continue the download", HttpStatus.BAD_REQUEST);
		}
		advanceService.asyncAdvanceMatrixClosingAmountDownload(tan, year, month, false, tenantId, userName,
				UploadTypes.ADVANCE_CLOSING_REPORT.name());
		String message = "Advance closing report requested successfully and will be available shortly";
		logger.info("{}", message);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", message, message);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}

	@PostMapping(value = "/advance/matrix/openingAmount/file/download")
	public ResponseEntity<ApiStatus<String>> advanceMatrixOpeningAmountDownload(
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName, @RequestBody Map<String, String> advanceData)
			throws Exception {

		Integer year = Integer.valueOf(advanceData.get("year"));
		Integer batchUploadYear = year;
		Integer month = Integer.valueOf(advanceData.get("month"));
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

		advanceService.asyncAdvanceMatrixOpeningAmountDownload(tan, year, month, true, tenantId, userName,
				UploadTypes.ADVANCE_OPENING_REPORT.name(), batchUploadYear);
		String message = "Advance opening report requested successfully and will be available shortly";
		logger.info("{}", message);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", message, message);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This method will download the file for the advances with for the months
	 * 
	 * @param tan
	 * @param tenantId
	 * @param userName
	 * @param advanceData
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/advance/matrix/ftm/file/download")
	public ResponseEntity<ApiStatus<String>> advanceMatrixFtmDownload(@RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader(value = "USER_NAME") String userName,
			@RequestBody Map<String, String> advanceData) throws Exception {

		Integer year = Integer.valueOf(advanceData.get("year"));
		Integer month = Integer.valueOf(advanceData.get("month"));
		logger.info("TAN: {}", tan);

		if (tan == null) {
			throw new CustomException("No Tan Found to continue the download", HttpStatus.BAD_REQUEST);
		}
		advanceService.asyncAdvanceMatrixFtmDownload(tan, year, month, tenantId, userName);
		String message = "Advance ftm report requested successfully and will be available shortly";
		logger.info("{}", message);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", message, message);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This method downloads file for advance adjusted amount
	 * 
	 * @param tan
	 * @param tenantId
	 * @param userName
	 * @param advanceData
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/advance/matrix/adjusted/file/download")
	public ResponseEntity<ApiStatus<String>> advanceMatrixAdjustedFileDownload(
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName, @RequestBody Map<String, String> advanceData)
			throws Exception {

		MultiTenantContext.setTenantId(tenantId);
		Integer year = Integer.valueOf(advanceData.get("year"));
		Integer month = Integer.valueOf(advanceData.get("month"));
		logger.info("TAN: {}", tan);

		if (tan == null) {
			throw new CustomException("No Tan Found to continue the download", HttpStatus.BAD_REQUEST);
		}
		advanceService.asyncAdvanceMatrixAdjustedFileDownload(tan, year, month, tenantId, userName);
		String message = "Advance adjusted report requested successfully and will be available shortly";
		logger.info("{}", message);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", message, message);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}

	@PostMapping(value = "advance/{restype}/{year}/{month}/{deducteeName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<AdvanceDTO>>> getResidentAndNonresident(
			@RequestHeader(value = "TAN-NUMBER") String tan, @PathVariable String restype,
			@RequestHeader("X-TENANT-ID") String tenantId, @PathVariable int year, @PathVariable int month,
			@PathVariable String deducteeName, @RequestBody Pagination pagination)
			throws RecordNotFoundException, TanNotFoundException {
		logger.info("X-TENANT-ID _______: {}", tenantId);
		MultiTenantContext.setTenantId(tenantId);
		CommonDTO<AdvanceDTO> response = null;
		if (tan != null && restype != null) {
			response = advanceService.getResidentAndNonresident(restype, tan, year, month, pagination, deducteeName);
		} else {
			logger.error("Tan/ rest type not found to proceed");
			throw new CustomException("Tan/ rest type not found to proceed", HttpStatus.BAD_REQUEST);
		}

		logger.info("List of Resident and NonResident responses done");
		ApiStatus<CommonDTO<AdvanceDTO>> apiStatus = new ApiStatus<CommonDTO<AdvanceDTO>>(HttpStatus.OK, "SUCCESS",
				"NO ALERT", response);
		return new ResponseEntity<ApiStatus<CommonDTO<AdvanceDTO>>>(apiStatus, HttpStatus.OK);

	}

	@GetMapping(value = "advance/serialNo/BSRCode/recieptDate", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<AdvanceDTO>>> getAdvacnceLineItemsByReceiptSerialNoAndBSRCodeAndReceiptDate(
			@RequestParam("year") Integer assessmentYear, @RequestParam("deductorTan") String deductorTan,
			@RequestParam("challanPaid") boolean challanPaid,
			@RequestParam("isForNonResidents") boolean isForNonResidents, @RequestParam("bsrCode") String bsrCode,
			@RequestParam("receiptSerailNo") String receiptSerailNo, @RequestParam("receiptDate") String receiptDate) {
		List<AdvanceDTO> list = advanceDAO.getAdvacnceLineItemsByReceiptSerialNoAndBSRCodeAndReceiptDate(assessmentYear,
				deductorTan, challanPaid, isForNonResidents, bsrCode, receiptSerailNo, receiptDate);
		ApiStatus<List<AdvanceDTO>> apiStatus = new ApiStatus<List<AdvanceDTO>>(HttpStatus.OK, "SUCCESS", "NO ALERT",
				list);
		return new ResponseEntity<ApiStatus<List<AdvanceDTO>>>(apiStatus, HttpStatus.OK);
	}

	@PostMapping("/advance/export/interestReport")
	public ResponseEntity<ApiStatus<String>> exportInterestComputationReport(
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader(value = "USER_NAME") String userName,
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestParam("year") int assessmentYear,
			@RequestParam(value = "month", required = true) Integer month,
			@RequestHeader(value = "DEDUCTOR-PAN") String deductorPan, @RequestBody MismatchesFiltersDTO filters)
			throws InvalidKeyException, FileNotFoundException, IOException, URISyntaxException, StorageException {
		MultiTenantContext.setTenantId(tenantId);
		advanceService.asyncExportInterestComputationReport(tan, assessmentYear, userName, deductorPan, month,
				tenantId);
		String message = "Advance Interest Computation report requested successfully and will be available shortly";
		logger.info("{}", message);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", message, message);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);

	}

	@PostMapping(value = "/advance/InterestRecords/{year}/{month}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<AdvanceDTO>>> listOfInvoiceInterestRecords(
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader(value = "TAN-NUMBER") String tan,
			@PathVariable int year, @PathVariable int month, @RequestBody MismatchesFiltersDTO filters)
			throws TanNotFoundException {
		MultiTenantContext.setTenantId(tenantId);

		CommonDTO<AdvanceDTO> listMismatchesAll = advanceService.getAdvanceInterestRecords(tan, year, month, filters);

		ApiStatus<CommonDTO<AdvanceDTO>> apiStatus = new ApiStatus<CommonDTO<AdvanceDTO>>(HttpStatus.OK, "SUCCESS",
				"NO ALERT", listMismatchesAll);
		return new ResponseEntity<ApiStatus<CommonDTO<AdvanceDTO>>>(apiStatus, HttpStatus.OK);
	}

	@PutMapping(value = "/advance/interest/action", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<String>> updateInterestByAction(
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestBody UpdateOnScreenDTO invoiceMismatchUpdateDTO, @RequestHeader("Authorization") String token,
			@RequestHeader(value = "DEDUCTOR-PAN") String pan) throws URISyntaxException {
		MultiTenantContext.setTenantId(tenantId);
		logger.info("Controller method executing to update onscreen interest record {}");
		// Handling null pointer exception
		if (tan == null) {
			throw new CustomException("Tan   Not Found", HttpStatus.BAD_REQUEST);
		}
		advanceService.updateInterestrecords(tan, invoiceMismatchUpdateDTO, pan);

		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS",
				"LIST OF ADVANCE INTEREST UPDATED", "SUCCESS");
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
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
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws InvalidKeyException
	 * @throws ParseException
	 */
	@PostMapping(value = "/advance/download/stream", consumes = { "application/x-www-form-urlencoded" })
	ResponseEntity<StreamingResponseBody> getStream(HttpServletResponse response,
			@RequestParam("deductorTan") String deductorTan, @RequestParam("deductorPan") String deductorPan,
			@RequestParam("batchId") String batchId, @RequestHeader("X-TENANT-ID") String tenantId)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException, ParseException {

		Calendar calendar = Calendar.getInstance();
		String fileName = "advance_mismach_report_" + deductorTan + "_" + calendar.get(Calendar.YEAR) + "_"
				+ calendar.get(Calendar.MONTH);

		response.addHeader("Content-disposition", "attachment;filename=" + fileName + ".zip");
		response.setContentType("application/octet-stream");

		logger.info("date :{}", Calendar.getInstance().toInstant());

		BatchUpload batchUpload = batchUploadService.getBatchUpload(Integer.valueOf(batchId));
		File filezip = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getOtherFileUrl());

		File csvFile = File.createTempFile("blob", "csv");
		advanceService.decompressGzip(filezip.toPath(), csvFile.toPath());
		CsvReader csvReader = new CsvReader();
		csvReader.setContainsHeader(true);
		CsvContainer csv = csvReader.read(csvFile, StandardCharsets.UTF_8);
		ByteArrayInputStream byteArrayInputStream = advanceService.generateMismatchExcel(csv.getRowCount(), csv, deductorTan,
				tenantId, deductorPan);
		StreamingResponseBody stream = out -> {
			final ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream());
			try {
				final ZipEntry zipEntry = new ZipEntry(fileName + ".xlsx");
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
		// Cleaning up the Temp Files CreatedABCCD1234E
		filezip.delete();
		csvFile.delete();
		response.setStatus(HttpServletResponse.SC_OK);
		return new ResponseEntity<StreamingResponseBody>(stream, HttpStatus.OK);
	}

	/**
	 * 
	 * @param tenantId
	 * @param userName
	 * @param deductorTan
	 * @param deductorPan
	 * @return
	 * @throws InvalidKeyException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws StorageException
	 */
	@PostMapping("/download/nopoadvances/report")
	public ResponseEntity<ApiStatus<BatchUpload>> downloadPoExcle(@RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName,
			@RequestHeader(value = "TAN-NUMBER") String deductorTan,
			@RequestHeader(value = "DEDUCTOR-PAN") String deductorPan, @RequestParam(value = "type") String type)
			throws InvalidKeyException, IOException, URISyntaxException, StorageException {
		MultiTenantContext.setTenantId(tenantId);
		BatchUpload batchUpload = advanceService.downloadPoExcle(tenantId, userName, deductorPan, deductorTan, type);
		ApiStatus<BatchUpload> apiStatus = new ApiStatus<BatchUpload>(HttpStatus.OK, "SUCCESS",
				"UPLOADED ADVANCE FILE SUCCESSFULLY", batchUpload);
		return new ResponseEntity<ApiStatus<BatchUpload>>(apiStatus, HttpStatus.OK);
	}

}
