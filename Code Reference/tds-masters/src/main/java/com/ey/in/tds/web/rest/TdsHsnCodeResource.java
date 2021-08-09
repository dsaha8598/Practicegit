package com.ey.in.tds.web.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

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
import com.ey.in.tds.common.domain.MasterBatchUpload;
import com.ey.in.tds.common.domain.MasterTdsHsnCode;
import com.ey.in.tds.common.dto.CommonDTO;
import com.ey.in.tds.common.dto.TdsHsnCodeDTO;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.TanNotFoundException;
import com.ey.in.tds.core.security.AllowedMimeTypes;
import com.ey.in.tds.core.service.BaseResource;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.service.TdsHsnService;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.micrometer.core.annotation.Timed;

/**
 * 
 * @author vamsir
 *
 */
@RestController
@RequestMapping("/api/masters/tds")
public class TdsHsnCodeResource extends BaseResource {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TdsHsnService tdsHsnService;

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
	public ResponseEntity<ApiStatus<MasterBatchUpload>> hsnCodeuploadExcel(@RequestParam("file") MultipartFile file,
			@RequestParam(value = "year", required = false) Integer assesssmentYear,
			@RequestParam(value = "assessmentMonth", required = false) Integer assessmentMonth,
			@RequestHeader(value = "X-USER-EMAIL") String userName) throws Exception {
		assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
		assessmentMonth = CommonUtil.getAssessmentMonthPlusOne(assessmentMonth);
		MasterBatchUpload masterBatchUpload = null;
		logger.info("Testing Something in Upload Excel");
		MultiTenantContext.setTenantId("master");
		String contentType = new Tika().detect(file.getInputStream());
		if (AllowedMimeTypes.XLSX.getMimeType().equals(contentType)
				|| AllowedMimeTypes.OOXML.getMimeType().equals(contentType)) {
			try {
				masterBatchUpload = tdsHsnService.saveFileData(file, assesssmentYear, assessmentMonth, userName);
			} catch (Exception e) {
				throw new CustomException("Unable to process the file", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			logger.warn("Unsupported file.");
			throw new CustomException("Invalid file, only Excel files are allowed", HttpStatus.BAD_REQUEST);
		}
		ApiStatus<MasterBatchUpload> apiStatus = new ApiStatus<MasterBatchUpload>(HttpStatus.OK, "SUCCESS",
				"UPLOADED HSN FILE SUCCESSFULLY ", masterBatchUpload);
		return new ResponseEntity<ApiStatus<MasterBatchUpload>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This api for get hsn code data.
	 * 
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/hsn")
	public ResponseEntity<ApiStatus<CommonDTO<MasterTdsHsnCode>>> getHsnSac(@RequestBody Pagination pagination)
			throws Exception {
		MultiTenantContext.setTenantId("master");
		CommonDTO<MasterTdsHsnCode> sacs = tdsHsnService.getAllHsnSac(pagination);
		ApiStatus<CommonDTO<MasterTdsHsnCode>> apiStatus = new ApiStatus<CommonDTO<MasterTdsHsnCode>>(HttpStatus.OK,
				"SUCCESS", "NO ALERT", sacs);
		return new ResponseEntity<ApiStatus<CommonDTO<MasterTdsHsnCode>>>(apiStatus, HttpStatus.OK);

	}

	/**
	 * 
	 * @param hsn
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@GetMapping("/hsn/search/{hsn}")
	@Timed
	public ResponseEntity<ApiStatus<List<MasterTdsHsnCode>>> fetchByHSN(@PathVariable Long hsn)
			throws JsonParseException, JsonMappingException, IOException {
		// We are setting for the Master
		MultiTenantContext.setTenantId("master");
		logger.info("Master tenant details {}", MultiTenantContext.getTenantId());
		List<MasterTdsHsnCode> hasRateDetails = tdsHsnService.findHSNRateDetails(hsn);
		ApiStatus<List<MasterTdsHsnCode>> apiStatus = new ApiStatus<List<MasterTdsHsnCode>>(HttpStatus.OK,
				"To get List of HSN Records based on HSN", "List of HSN Rates associated with HSN", hasRateDetails);
		return new ResponseEntity<ApiStatus<List<MasterTdsHsnCode>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This api for feign client
	 * 
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/master/hsn")
	public ResponseEntity<ApiStatus<List<MasterTdsHsnCode>>> getAllHsnDetalis() throws Exception {
		MultiTenantContext.setTenantId("master");
		List<MasterTdsHsnCode> sacs = tdsHsnService.getAllHsnDetalis();
		ApiStatus<List<MasterTdsHsnCode>> apiStatus = new ApiStatus<List<MasterTdsHsnCode>>(HttpStatus.OK, "SUCCESS",
				"NO ALERT", sacs);
		return new ResponseEntity<ApiStatus<List<MasterTdsHsnCode>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param tdsSection
	 * @return
	 */
	@GetMapping("/hsn/section/{tdsSection}")
	public ResponseEntity<ApiStatus<List<MasterTdsHsnCode>>> getAllHsnByTdsSeciton(@PathVariable String tdsSection) {
		// We are setting for the Master
		MultiTenantContext.setTenantId("master");
		logger.info("Master tenant details {}", MultiTenantContext.getTenantId());
		List<MasterTdsHsnCode> hasRateDetails = tdsHsnService.getAllHsnByTdsSeciton(tdsSection);
		ApiStatus<List<MasterTdsHsnCode>> apiStatus = new ApiStatus<List<MasterTdsHsnCode>>(HttpStatus.OK,
				"GET ALL HSN BASED ON TDS SECTION", "GET ALL HSN BASED ON TDS SECTION", hasRateDetails);
		return new ResponseEntity<ApiStatus<List<MasterTdsHsnCode>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/tdssection")
	public ResponseEntity<ApiStatus<List<String>>> getAllTdsSection() {
		MultiTenantContext.setTenantId("master");
		List<String> sacs = tdsHsnService.getAllTdsSection();
		ApiStatus<List<String>> apiStatus = new ApiStatus<List<String>>(HttpStatus.OK, "SUCCESS",
				"GET ALL TDS SECTIONS", sacs);
		return new ResponseEntity<ApiStatus<List<String>>>(apiStatus, HttpStatus.OK);
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
	public ResponseEntity<ApiStatus<MasterTdsHsnCode>> createHsnCode(@RequestBody TdsHsnCodeDTO tdsHsnCode,
			@RequestHeader(value = "USER_NAME") String userName) {
		MultiTenantContext.setTenantId("master");
		MasterTdsHsnCode hasRateDetails = tdsHsnService.createHsnCode(tdsHsnCode, userName);
		ApiStatus<MasterTdsHsnCode> apiStatus = new ApiStatus<MasterTdsHsnCode>(HttpStatus.CREATED, "SUCCESS",
				"Hsn code created successfuly", hasRateDetails);
		return new ResponseEntity<ApiStatus<MasterTdsHsnCode>>(apiStatus, HttpStatus.CREATED);
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
	public ResponseEntity<ApiStatus<Optional<MasterTdsHsnCode>>> createHsnCode(@PathVariable Integer id,
			@RequestHeader(value = "USER_NAME") String userName) {
		MultiTenantContext.setTenantId("master");
		Optional<MasterTdsHsnCode> hasRateDetails = tdsHsnService.fineById(id);
		ApiStatus<Optional<MasterTdsHsnCode>> apiStatus = new ApiStatus<Optional<MasterTdsHsnCode>>(HttpStatus.OK,
				"SUCCESS", "Get by hsn code based on id", hasRateDetails);
		return new ResponseEntity<ApiStatus<Optional<MasterTdsHsnCode>>>(apiStatus, HttpStatus.OK);
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
			@RequestHeader(value = "USER_NAME") String userName) {
		MultiTenantContext.setTenantId("master");
		TdsHsnCodeDTO hasRateDetails = tdsHsnService.updateHsn(tdsHsnCode, userName);
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
	public ResponseEntity<InputStreamResource> exportNOPKeywords() throws IOException, TanNotFoundException {
		MultiTenantContext.setTenantId("master");
		ByteArrayInputStream in = tdsHsnService.exportNOPAndSection();
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=Nature_of_payment_Report" + ".xlsx");
		logger.info("nop list download done ");
		return ResponseEntity.ok().headers(headers).body(new InputStreamResource(in));
	}

}
