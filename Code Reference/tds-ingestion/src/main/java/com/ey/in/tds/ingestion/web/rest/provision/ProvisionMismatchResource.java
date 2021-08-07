package com.ey.in.tds.ingestion.web.rest.provision;

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
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.ProvisionDTO;
import com.ey.in.tds.common.dto.CommonDTO;
import com.ey.in.tds.common.dto.invoice.MismatchesFiltersDTO;
import com.ey.in.tds.common.model.provision.ProvisionMismatchByBatchIdDTO;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.common.service.BlobStorageService;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.core.exceptions.TanNotFoundException;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.ingestion.dto.invoice.UpdateOnScreenDTO;
import com.ey.in.tds.ingestion.jdbc.dao.ProvisionDAO;
import com.ey.in.tds.ingestion.service.batchupload.BatchUploadService;
import com.ey.in.tds.ingestion.service.provision.ProvisionMismatchService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.microsoft.azure.storage.StorageException;

import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvReader;

@RestController
@RequestMapping("/api/ingestion")
public class ProvisionMismatchResource {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ProvisionMismatchService provisionMisMatchService;

	@Autowired
	private BlobStorageService blobStorageService;

	@Autowired
	private BatchUploadService batchUploadService;

	@Autowired
	private ProvisionDAO provisionDAO;

	/**
	 * Update mismatch table based on Mismatch category for Excel
	 * 
	 * @param mismatchcategory
	 * @param provisionMismatchUpdateDTO
	 * @return
	 * @throws RecordNotFoundException
	 * @throws TanNotFoundException
	 */
	@PutMapping("provision/{mismatchcategory}/action")
	public ResponseEntity<ApiStatus<UpdateOnScreenDTO>> uploadExcel(@RequestHeader(value = "TAN-NUMBER") String tan,
			@PathVariable String mismatchcategory, @RequestBody UpdateOnScreenDTO invoiceMismatchUpdateDTO)
			throws RecordNotFoundException, TanNotFoundException {
		// Handling null pointer exception
		if (tan == null && mismatchcategory == null) {
			throw new CustomException("Tan / Mismatch Category  Not Found", HttpStatus.BAD_REQUEST);
		}

		UpdateOnScreenDTO response = provisionMisMatchService.updateMismatchByAction(tan, invoiceMismatchUpdateDTO);

		ApiStatus<UpdateOnScreenDTO> apiStatus = new ApiStatus<UpdateOnScreenDTO>(HttpStatus.OK, "SUCCESS",
				"LIST OF MISMATCHES UPDATED", response);
		return new ResponseEntity<ApiStatus<UpdateOnScreenDTO>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * Get the List of records based on miss match summary by BatchId
	 * 
	 * @RequestHeader deductorTan
	 * @PathVariable batchUploadId
	 * @throws TanNotFoundException
	 * 
	 */
	@GetMapping("/provision/{batchId}/mismatchsummary")
	public ResponseEntity<ApiStatus<List<ProvisionMismatchByBatchIdDTO>>> getProvisionMisMatchSummaryById(
			@RequestHeader(value = "TAN-NUMBER") String deductorTan, @PathVariable Integer batchId)
			throws TanNotFoundException {
		if (deductorTan == null && batchId == null) {
			if (logger.isErrorEnabled()) {
				logger.error("tan and batchId not found");
			}
			throw new CustomException("TAN and BatchID Category Not present to proceed", HttpStatus.BAD_REQUEST);
		}
		List<ProvisionMismatchByBatchIdDTO> provision = provisionMisMatchService
				.getProvisionMisMatchSummaryById(deductorTan, batchId);
		if (logger.isInfoEnabled()) {
			logger.info("provision mismatch summary data done. ");
		}

		ApiStatus<List<ProvisionMismatchByBatchIdDTO>> apiStatus = new ApiStatus<List<ProvisionMismatchByBatchIdDTO>>(
				HttpStatus.OK, "SUCCESS", "LIST OF PROVISION MISMATCH SUMMARY BASED ON BATCH ID", provision);
		return new ResponseEntity<ApiStatus<List<ProvisionMismatchByBatchIdDTO>>>(apiStatus, HttpStatus.OK);

	}

	/**
	 * Get the List of records based on miss match by BatchId
	 * 
	 * @RequestHeader deductorTan
	 * @PathVariable batchUploadId
	 * @throws TanNotFoundException
	 */
	@PostMapping(value = "/provision/{batchId}/{mismatchCategory}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<ProvisionDTO>>> getProvisionMisMatchById(
			@RequestHeader(value = "TAN-NUMBER") String deductorTan, @PathVariable Integer batchId,
			@PathVariable String mismatchCategory, @RequestBody Pagination pagination) throws TanNotFoundException {
		if (deductorTan == null && batchId == null && mismatchCategory == null) {
			if (logger.isErrorEnabled()) {
				logger.error("tan and batchId and mismatchCategory not found");
			}
			throw new CustomException("TAN and BatchId and Mis Match not present To proceed", HttpStatus.BAD_REQUEST);
		}

		CommonDTO<ProvisionDTO> provision = provisionMisMatchService.getProvisionMisMatchById(deductorTan, batchId,
				mismatchCategory, pagination);
		if (logger.isInfoEnabled()) {
			logger.info("provision mismatch category data done. ");
		}
		ApiStatus<CommonDTO<ProvisionDTO>> apiStatus = new ApiStatus<CommonDTO<ProvisionDTO>>(HttpStatus.OK, "SUCCESS",
				"NO ALERT", provision);
		return new ResponseEntity<ApiStatus<CommonDTO<ProvisionDTO>>>(apiStatus, HttpStatus.OK);

	}

	/**
	 * Get the List of all records based on mismatchsummary.
	 * 
	 * @RequestHeader deductorTan
	 * @throws TanNotFoundException
	 */
	@GetMapping("/provision/all/mismatchsummary/{year}/{month}")
	public ResponseEntity<ApiStatus<List<ProvisionMismatchByBatchIdDTO>>> getAllProvisionMisMatchSummary(
			@RequestHeader(value = "TAN-NUMBER") String deductorTan, @PathVariable int year, @PathVariable int month)
			throws TanNotFoundException {
		if (deductorTan == null) {
			if (logger.isErrorEnabled()) {
				logger.error("Tan Not Found");
			}
			throw new CustomException("TAN Not Present To Proceed", HttpStatus.BAD_REQUEST);
		}
		List<ProvisionMismatchByBatchIdDTO> provision = provisionMisMatchService
				.getAllProvisionMisMatchSummary(deductorTan, year, month);
		if (logger.isInfoEnabled()) {
			logger.info("provision all mismatch summary data done. ");
		}
		ApiStatus<List<ProvisionMismatchByBatchIdDTO>> apiStatus = new ApiStatus<List<ProvisionMismatchByBatchIdDTO>>(
				HttpStatus.OK, "SUCCESS", "LIST OF PROVISION MISMATCH SUMMARY", provision);
		return new ResponseEntity<ApiStatus<List<ProvisionMismatchByBatchIdDTO>>>(apiStatus, HttpStatus.OK);

	}

	/**
	 * Get the List of records based on miss match
	 * 
	 * @RequestHeader deductorTan
	 * @throws TanNotFoundException
	 * @throws JsonProcessingException 
	 * @throws JsonMappingException 
	 */
	@PostMapping(value = "/provision/all/{mismatchCategory}/{year}/{month}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<ProvisionDTO>>> getAllProvisionMisMatch(
			@RequestHeader(value = "TAN-NUMBER") String deductorTan, @PathVariable String mismatchCategory,
			@PathVariable int year, @PathVariable int month, @RequestBody MismatchesFiltersDTO filters)
			throws TanNotFoundException, JsonMappingException, JsonProcessingException {
		if (deductorTan == null && mismatchCategory == null) {
			if (logger.isErrorEnabled()) {
				logger.error("Tan and MisMatchCategory Not Found");
			}
			throw new CustomException("Tan and mismatch Category Not present to proceed", HttpStatus.BAD_REQUEST);
		}
		CommonDTO<ProvisionDTO> provision = provisionMisMatchService.getAllProvisionMisMatch(deductorTan,
				mismatchCategory, year, month, filters);
		if (logger.isInfoEnabled()) {
			logger.info("provision all mismatch category data done.");
		}
		ApiStatus<CommonDTO<ProvisionDTO>> apiStatus = new ApiStatus<CommonDTO<ProvisionDTO>>(HttpStatus.OK, "SUCCESS",
				"NO ALERT", provision);
		return new ResponseEntity<ApiStatus<CommonDTO<ProvisionDTO>>>(apiStatus, HttpStatus.OK);

	}

	/**
	 * 
	 * @param tenantId
	 * @param tan
	 * @param pan
	 * @param year
	 * @param month
	 * @param isMismatch
	 * @param userName
	 * @return
	 * @throws IOException
	 * @throws TanNotFoundException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws ParseException
	 */
	@PostMapping(value = "/provision/async/export/{isMismatch}")
	public ResponseEntity<ApiStatus<String>> asyncExportProvisionMismatchReport(
			@RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "TAN-NUMBER", required = true) String tan,
			@RequestHeader(value = "DEDUCTOR-PAN", required = true) String pan,
			@RequestParam(value = "year", required = true) Integer year,
			@RequestParam(value = "month", required = true) Integer month,
			@PathVariable(value = "isMismatch", required = false) boolean isMismatch,
			@RequestHeader(value = "USER_NAME") String userName)
			throws IOException, TanNotFoundException, InvalidKeyException, URISyntaxException, StorageException, ParseException {

		logger.info("TAN: {}", tan);
		MultiTenantContext.setTenantId(tenantId);

		if (tan == null) {
			throw new CustomException("No Tan Found to continue the download", HttpStatus.BAD_REQUEST);
		}

		provisionMisMatchService.asyncExportProvisionMismatchReport(tan, tenantId, pan, year, month, userName, isMismatch);

		String message = "Provision mismatch report requested successfully and will be available shortly";
		logger.info("{}", message);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", message, message);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This method is used to get provision matrix values
	 * 
	 * @param deductorTan
	 * @return
	 * @throws CustomException
	 * @throws IOException
	 * @throws ParseException
	 */
	@GetMapping("/provision/matrix/{year}/{type}")
	public ResponseEntity<ApiStatus<Map<Integer, Object>>> getProvisionMatrix(
			@RequestHeader(value = "TAN-NUMBER") String deductorTan, @PathVariable("year") int year,
			@PathVariable("type") String type) throws CustomException, IOException, ParseException {
		if (deductorTan == null) {
			if (logger.isErrorEnabled()) {
				logger.error("DeductorTan Not Found");
			}
			throw new CustomException("DeductorTan Not present to proceed..", HttpStatus.BAD_REQUEST);
		}
		Map<Integer, Object> provision = provisionMisMatchService.getProvisionMatrix(deductorTan, year, type);

		if (logger.isInfoEnabled()) {
			logger.info("provision all matrix values is done ");
		}
		ApiStatus<Map<Integer, Object>> apiStatus = new ApiStatus<Map<Integer, Object>>(HttpStatus.OK, "SUCCESS",
				"LIST OF PROVISION MATRIX DATA", provision);
		return new ResponseEntity<ApiStatus<Map<Integer, Object>>>(apiStatus, HttpStatus.OK);
	}

	@PostMapping(value = "/provision/matrix/closingAmount/file/download")
	public ResponseEntity<ApiStatus<String>> provisionMatrixClosingAmountDownload(
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName, @RequestBody Map<String, String> provisionData)
			throws Exception {

		Integer year = Integer.valueOf(provisionData.get("year"));
		Integer month = Integer.valueOf(provisionData.get("month"));
		if (tan == null) {
			throw new CustomException("No Tan Found to continue the download", HttpStatus.BAD_REQUEST);
		}
		provisionMisMatchService.asyncProvisionMatrixClosingAmountDownload(tan, year, month, false, tenantId, userName,
				UploadTypes.PROVISION_CLOSING_REPORT.name());
		String message = "Provision closing report requested successfully and will be available shortly";
		logger.info("{}", message);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", message, message);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}

	@PostMapping(value = "/provision/matrix/openingAmount/file/download")
	public ResponseEntity<ApiStatus<String>> provisionMatrixOpeningAmountDownload(
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName, @RequestBody Map<String, String> provisionData)
			throws Exception {

		Integer year = Integer.valueOf(provisionData.get("year"));
		Integer batchUploadYear = year;
		Integer month = Integer.valueOf(provisionData.get("month"));
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

		provisionMisMatchService.asyncProvisionMatrixOpeningAmountDownload(tan, year, month, true, tenantId, userName,
				UploadTypes.PROVISION_OPENING_REPORT.name(), batchUploadYear);
		String message = "Provision opening report requested successfully and will be available shortly";
		logger.info("{}", message);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", message, message);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);

	}

	/**
	 * This method will download the file for the provisions with for the months
	 * 
	 * @param tan
	 * @param tenantId
	 * @param userName
	 * @param provisionData
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/provision/matrix/ftm/file/download")
	public ResponseEntity<ApiStatus<String>> provisionMatrixFtmDownload(@RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader(value = "USER_NAME") String userName,
			@RequestBody Map<String, String> provisionData) throws Exception {
		Integer year = Integer.valueOf(provisionData.get("year"));
		Integer month = Integer.valueOf(provisionData.get("month"));
		logger.info("TAN: {}", tan);

		if (tan == null) {
			throw new CustomException("No Tan Found to continue the download", HttpStatus.BAD_REQUEST);
		}
		provisionMisMatchService.asyncProvisionMatrixFtmDownload(tan, year, month, tenantId, userName);
		String message = "Provision ftm report requested successfully and will be available shortly";
		logger.info("{}", message);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", message, message);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This method downloads file for provision adjusted amount
	 * 
	 * @param tan
	 * @param tenantId
	 * @param userName
	 * @param provisionData
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/provision/matrix/adjusted/file/download")
	public ResponseEntity<ApiStatus<String>> provisionMatrixAdjustedFileDownload(
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName, @RequestBody Map<String, String> provisionData)
			throws Exception {

		Integer year = Integer.valueOf(provisionData.get("year"));
		Integer month = Integer.valueOf(provisionData.get("month"));
		logger.info("TAN: {}", tan);

		if (tan == null) {
			throw new CustomException("No Tan Found to continue the download", HttpStatus.BAD_REQUEST);
		}
		provisionMisMatchService.asyncProvisionMatrixAdjustedFileDownload(tan, year, month, tenantId, userName);
		String message = "Provision adjusted report requested successfully and will be available shortly";
		logger.info("{}", message);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", message, message);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}

	/**
	 * ADHOC
	 * 
	 * @param tan
	 * @param type
	 * @param tenantId
	 * @param pagination
	 * @return
	 */
	@PostMapping(value = "/provision/adhoc/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<ProvisionDTO>>> listOfBatchUploadFiles(
			@RequestHeader(value = "TAN-NUMBER") String tan, @PathVariable String type,
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestBody Pagination pagination) {

		CommonDTO<ProvisionDTO> listProvisionAdhocData = provisionMisMatchService.getListOfProvisionAdhoc(type, tan,
				pagination);
		logger.info("REST response for provision adhoc data : {}", listProvisionAdhocData);
		ApiStatus<CommonDTO<ProvisionDTO>> apiStatus = new ApiStatus<CommonDTO<ProvisionDTO>>(HttpStatus.CREATED,
				"SUCCESS", "NO ALERT", listProvisionAdhocData);
		return new ResponseEntity<ApiStatus<CommonDTO<ProvisionDTO>>>(apiStatus, HttpStatus.OK);
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
	@PostMapping(value = "provision/{restype}/{year}/{month}/{deducteeName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<ProvisionDTO>>> getResidentAndNonresident(
			@RequestHeader(value = "TAN-NUMBER") String tan, @PathVariable String restype,
			@RequestHeader("X-TENANT-ID") String tenantId, @PathVariable int year, @PathVariable int month,
			@PathVariable String deducteeName, @RequestBody Pagination pagination)
			throws RecordNotFoundException, TanNotFoundException {
		logger.info("X-TENANT-ID _______: {}", tenantId);

		CommonDTO<ProvisionDTO> response = null;
		if (tan != null && restype != null) {
			response = provisionMisMatchService.getResidentAndNonresident(restype, tan, year, month, pagination,
					deducteeName);
		} else {
			logger.error("Tan/ rest type not found to proceed");
			throw new CustomException("Tan/ rest type not found to proceed", HttpStatus.BAD_REQUEST);
		}

		logger.info("List of Resident and NonResident responses done");
		ApiStatus<CommonDTO<ProvisionDTO>> apiStatus = new ApiStatus<CommonDTO<ProvisionDTO>>(HttpStatus.OK, "SUCCESS",
				"NO ALERT", response);
		return new ResponseEntity<ApiStatus<CommonDTO<ProvisionDTO>>>(apiStatus, HttpStatus.OK);

	}

	@GetMapping(value = "provision/year/tan/BSRCode")
	public ResponseEntity<ApiStatus<List<ProvisionDTO>>> getProvisionLineItemsByReceiptSerialNoAndBSRCodeAndReceiptDate(
			@RequestParam("assessmentYear") Integer assessmentYear, @RequestParam("deductorTan") String deductorTan,
			@RequestParam("challanPaid") boolean challanPaid,
			@RequestParam("isForNonResidents") boolean isForNonResidents, @RequestParam("bsrCode") String bsrCode,
			@RequestParam("receiptSerailNo") String receiptSerailNo, @RequestParam("receiptDate") String receiptDate) {
		List<ProvisionDTO> list = provisionDAO.getProvisionLineItemsByReceiptSerialNoAndBSRCodeAndReceiptDate(
				assessmentYear, deductorTan, challanPaid, isForNonResidents, bsrCode, receiptSerailNo, receiptDate);
		ApiStatus<List<ProvisionDTO>> apiStatus = new ApiStatus<List<ProvisionDTO>>(HttpStatus.OK, "SUCCESS",
				"NO ALERT", list);
		return new ResponseEntity<ApiStatus<List<ProvisionDTO>>>(apiStatus, HttpStatus.OK);
	}

	@PostMapping("/provision/export/interestReport")
	public ResponseEntity<ApiStatus<String>> exportInterestComputationReport(
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader(value = "USER_NAME") String userName,
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestParam("year") int assessmentYear,
			@RequestParam(value = "month", required = true) Integer month,
			@RequestHeader(value = "DEDUCTOR-PAN") String deductorPan, @RequestBody MismatchesFiltersDTO filters)
			throws InvalidKeyException, FileNotFoundException, IOException, URISyntaxException, StorageException {
		MultiTenantContext.setTenantId(tenantId);
		provisionMisMatchService.asyncExportInterestComputationReport(tan, assessmentYear, userName, deductorPan, month,
				tenantId, filters.getDeducteeName(), filters.getResidentType());
		String message = "Advance Interest Computation report requested successfully and will be available shortly";
		logger.info("{}", message);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", message, message);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);

	}

	@PostMapping(value = "/provision/InterestRecords/{year}/{month}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<ProvisionDTO>>> listOfInvoiceInterestRecords(
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader(value = "TAN-NUMBER") String tan,
			@PathVariable int year, @PathVariable int month, @RequestBody MismatchesFiltersDTO filters)
			throws TanNotFoundException {
		MultiTenantContext.setTenantId(tenantId);

		CommonDTO<ProvisionDTO> listMismatchesAll = provisionMisMatchService.getProvisionInterestRecords(tan, year,
				month, filters);

		ApiStatus<CommonDTO<ProvisionDTO>> apiStatus = new ApiStatus<CommonDTO<ProvisionDTO>>(HttpStatus.OK, "SUCCESS",
				"NO ALERT", listMismatchesAll);
		return new ResponseEntity<ApiStatus<CommonDTO<ProvisionDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * to update the onscreen provision records
	 * 
	 * @param tenantId
	 * @param tan
	 * @param invoiceMismatchUpdateDTO
	 * @param token
	 * @param pan
	 * @return
	 * @throws URISyntaxException
	 */
	@PutMapping(value = "/provision/interest/action", consumes = MediaType.APPLICATION_JSON_VALUE)
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
		provisionMisMatchService.updateInterestrecords(tan, invoiceMismatchUpdateDTO, pan);

		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS",
				"LIST OF PROVISION INTEREST UPDATED", "SUCCESS");
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
	 * @throws ParseException 
	 * @throws StorageException 
	 * @throws URISyntaxException 
	 * @throws InvalidKeyException 
	 */
	@PostMapping(value = "/provision/download/stream", consumes = { "application/x-www-form-urlencoded" })
	ResponseEntity<StreamingResponseBody> getStream(HttpServletResponse response,
			@RequestParam("deductorTan") String deductorTan, @RequestParam("deductorPan") String deductorPan,
			@RequestParam("batchId") String batchId, @RequestHeader("X-TENANT-ID") String tenantId) throws IOException, ParseException, InvalidKeyException, URISyntaxException, StorageException {

		Calendar calendar = Calendar.getInstance();
		String fileName = "provision_mismach_report_" + deductorTan + "_" + calendar.get(Calendar.YEAR) + "_"
				+ calendar.get(Calendar.MONTH);

		response.addHeader("Content-disposition", "attachment;filename=" + fileName + ".zip");
		response.setContentType("application/octet-stream");

		logger.info("date :{}", Calendar.getInstance().toInstant());

		BatchUpload batchUpload = batchUploadService.getBatchUpload(Integer.valueOf(batchId));
		File filezip = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getOtherFileUrl());

		File csvFile = File.createTempFile("blob", "csv");
		provisionMisMatchService.decompressGzip(filezip.toPath(), csvFile.toPath());

		CsvReader csvReader = new CsvReader();

		csvReader.setContainsHeader(true);

		CsvContainer csv = csvReader.read(csvFile, StandardCharsets.UTF_8);
		ByteArrayInputStream byteArrayInputStream = provisionMisMatchService.generateMismatchExcel(csv.getRowCount(), csv,
				deductorTan, tenantId, deductorPan);

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

		// Cleaning up the Temp Files Created
		filezip.delete();
		csvFile.delete();
		response.setStatus(HttpServletResponse.SC_OK);

		return new ResponseEntity<StreamingResponseBody>(stream, HttpStatus.OK);
	}



}
