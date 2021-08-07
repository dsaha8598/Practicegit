package com.ey.in.tds.onboarding.web.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

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

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.dto.CommonDTO;
import com.ey.in.tds.common.dto.TdsHsnCodeDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.TdsHsnCode;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.TanNotFoundException;
import com.ey.in.tds.core.security.AllowedMimeTypes;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.onboarding.service.hsncode.TdsHsnCodeService;

@RestController
@RequestMapping("/api/onboarding")
public class TdsHsnCodeResource {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TdsHsnCodeService tdsHsnService;

	/**
	 * 
	 * @param file
	 * @param deductorTan
	 * @param tenantId
	 * @param deductorPan
	 * @param userName
	 * @return
	 * @throws IOException
	 */
	@PostMapping("/hsn/upload/excel")
	public ResponseEntity<ApiStatus<BatchUpload>> hsnCodeuploadExcel(@RequestParam("file") MultipartFile file,
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader(value = "DEDUCTOR-PAN") String deductorPan,
			@RequestParam(value = "year", required = false) Integer assesssmentYear,
			@RequestParam(value = "assessmentMonth", required = false) Integer assessmentMonth,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader("X-TENANT-ID") String tenantId)
			throws Exception {
		assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
		assessmentMonth = CommonUtil.getAssessmentMonthPlusOne(assessmentMonth);
		BatchUpload masterBatchUpload = null;
		logger.info("Testing Something in Upload Excel");
		MultiTenantContext.setTenantId(tenantId);
		String contentType = new Tika().detect(file.getInputStream());
		if (AllowedMimeTypes.XLSX.getMimeType().equals(contentType)
				|| AllowedMimeTypes.OOXML.getMimeType().equals(contentType)) {
			try {
				masterBatchUpload = tdsHsnService.saveFileData(file, assesssmentYear, assessmentMonth, userName, tan,
						deductorPan, tenantId);
			} catch (Exception e) {
				throw new CustomException("Unable to process the file", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			logger.warn("Unsupported file.");
			throw new CustomException("Invalid file, only Excel files are allowed", HttpStatus.BAD_REQUEST);
		}
		ApiStatus<BatchUpload> apiStatus = new ApiStatus<BatchUpload>(HttpStatus.OK, "SUCCESS",
				"UPLOADED HSN FILE SUCCESSFULLY ", masterBatchUpload);
		return new ResponseEntity<ApiStatus<BatchUpload>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This api for get hsn code data.
	 * 
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/hsn")
	public ResponseEntity<ApiStatus<CommonDTO<TdsHsnCodeDTO>>> getHsnSac(@RequestBody Pagination pagination,
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader(value = "DEDUCTOR-PAN") String deductorPan,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader("X-TENANT-ID") String tenantId)
			throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		CommonDTO<TdsHsnCodeDTO> sacs = tdsHsnService.getAllHsnSac(pagination, deductorPan, tan);
		ApiStatus<CommonDTO<TdsHsnCodeDTO>> apiStatus = new ApiStatus<CommonDTO<TdsHsnCodeDTO>>(HttpStatus.OK,
				"SUCCESS", "NO ALERT", sacs);
		return new ResponseEntity<ApiStatus<CommonDTO<TdsHsnCodeDTO>>>(apiStatus, HttpStatus.OK);

	}

	/**
	 * 
	 * @param hsn
	 * @param tan
	 * @param deductorPan
	 * @param userName
	 * @param tenantId
	 * @return
	 */
	@GetMapping("/hsn/search/{hsn}")
	public ResponseEntity<ApiStatus<List<TdsHsnCodeDTO>>> fetchByHSN(@PathVariable Long hsn,
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader(value = "DEDUCTOR-PAN") String deductorPan,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader("X-TENANT-ID") String tenantId) {
		MultiTenantContext.setTenantId(tenantId);
		List<TdsHsnCodeDTO> hasRateDetails = tdsHsnService.findHSNRateDetails(hsn, deductorPan, tan);
		ApiStatus<List<TdsHsnCodeDTO>> apiStatus = new ApiStatus<List<TdsHsnCodeDTO>>(HttpStatus.OK,
				"GET HSN LIST BASED ON HSN CODE", "GET HSN LIST BASED ON HSN CODE", hasRateDetails);
		return new ResponseEntity<ApiStatus<List<TdsHsnCodeDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param tdsHsnCode
	 * @param tan
	 * @param deductorPan
	 * @param userName
	 * @param tenantId
	 * @return
	 * @throws IOException
	 */
	@PostMapping(value = "/hsncode", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<TdsHsnCode>> createHsnCode(@RequestBody TdsHsnCodeDTO tdsHsnCode,
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader(value = "DEDUCTOR-PAN") String deductorPan,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader("X-TENANT-ID") String tenantId) {
		MultiTenantContext.setTenantId(tenantId);
		TdsHsnCode hasRateDetails = tdsHsnService.createHsnCode(tdsHsnCode, deductorPan, tan, userName);
		ApiStatus<TdsHsnCode> apiStatus = new ApiStatus<TdsHsnCode>(HttpStatus.CREATED, "SUCCESS",
				"Hsn code created successfuly", hasRateDetails);
		return new ResponseEntity<ApiStatus<TdsHsnCode>>(apiStatus, HttpStatus.CREATED);
	}

	/**
	 * 
	 * @param tdsHsnCode
	 * @param tan
	 * @param deductorPan
	 * @param userName
	 * @param tenantId
	 * @return
	 */
	@GetMapping("/hsn/{id}")
	public ResponseEntity<ApiStatus<TdsHsnCode>> createHsnCode(@PathVariable Integer id,
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader(value = "DEDUCTOR-PAN") String deductorPan,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader("X-TENANT-ID") String tenantId) {
		MultiTenantContext.setTenantId(tenantId);
		TdsHsnCode hasRateDetails = tdsHsnService.fineById(id, deductorPan, tan);
		ApiStatus<TdsHsnCode> apiStatus = new ApiStatus<TdsHsnCode>(HttpStatus.OK, "SUCCESS",
				"Get by hsn code based on id", hasRateDetails);
		return new ResponseEntity<ApiStatus<TdsHsnCode>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param tdsHsnCode
	 * @param tan
	 * @param deductorPan
	 * @param userName
	 * @param tenantId
	 * @return
	 */
	@PutMapping("/hsn")
	public ResponseEntity<ApiStatus<TdsHsnCodeDTO>> updateHsn(@RequestBody TdsHsnCodeDTO tdsHsnCode,
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader(value = "DEDUCTOR-PAN") String deductorPan,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader("X-TENANT-ID") String tenantId) {
		MultiTenantContext.setTenantId(tenantId);
		TdsHsnCodeDTO hasRateDetails = tdsHsnService.updateHsn(tdsHsnCode, deductorPan, tan, userName);
		ApiStatus<TdsHsnCodeDTO> apiStatus = new ApiStatus<TdsHsnCodeDTO>(HttpStatus.OK, "SUCCESS",
				"Hsn code update successfuly", hasRateDetails);
		return new ResponseEntity<ApiStatus<TdsHsnCodeDTO>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param tan
	 * @param pan
	 * @param tenantId
	 * @return
	 * @throws IOException
	 * @throws TanNotFoundException
	 */
	@PostMapping(value = "/hsn/nops/export", consumes = { "application/x-www-form-urlencoded" })
	public ResponseEntity<InputStreamResource> exportNOPKeywords(
			@RequestParam(value = "tan", required = true) String tan,
			@RequestParam(value = "pan", required = true) String pan,
			@RequestParam(value = "tenantId", required = true) String tenantId)
			throws IOException, TanNotFoundException {
		logger.info("TAN: {}", tan);
		MultiTenantContext.setTenantId(tenantId);
		ByteArrayInputStream in = tdsHsnService.exportNOPAndSection();
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=Nature_of_payment_Report_" + tan + ".xlsx");
		logger.info("nop list download done ");
		return ResponseEntity.ok().headers(headers).body(new InputStreamResource(in));
	}

	/**
	 * 
	 * @param tan
	 * @param deductorPan
	 * @param userName
	 * @param tenantId
	 * @return
	 */
	@GetMapping("/hsn/sections")
	public ResponseEntity<ApiStatus<List<String>>> getAllSections(@RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestHeader(value = "DEDUCTOR-PAN") String deductorPan,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader("X-TENANT-ID") String tenantId) {
		MultiTenantContext.setTenantId(tenantId);
		List<String> hasRateDetails = tdsHsnService.getAllSections(deductorPan, tan);
		ApiStatus<List<String>> apiStatus = new ApiStatus<List<String>>(HttpStatus.OK, "GET ALL TDS SECTION",
				"GET ALL TDS SECTION", hasRateDetails);
		return new ResponseEntity<ApiStatus<List<String>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param tdsSection
	 * @param tan
	 * @param deductorPan
	 * @param userName
	 * @param tenantId
	 * @return
	 */
	@GetMapping("/hsn/section/{tdsSection}")
	public ResponseEntity<ApiStatus<List<TdsHsnCodeDTO>>> fetchHsnByTdsSecion(@PathVariable String tdsSection,
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader(value = "DEDUCTOR-PAN") String deductorPan,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader("X-TENANT-ID") String tenantId) {
		MultiTenantContext.setTenantId(tenantId);
		List<TdsHsnCodeDTO> hasRateDetails = tdsHsnService.fetchHsnByTdsSecion(tdsSection, deductorPan, tan);
		ApiStatus<List<TdsHsnCodeDTO>> apiStatus = new ApiStatus<List<TdsHsnCodeDTO>>(HttpStatus.OK,
				"GET HSN LIST BASED ON TDS SECTION", "GET HSN LIST BASED ON TDS SECTION", hasRateDetails);
		return new ResponseEntity<ApiStatus<List<TdsHsnCodeDTO>>>(apiStatus, HttpStatus.OK);
	}
}
