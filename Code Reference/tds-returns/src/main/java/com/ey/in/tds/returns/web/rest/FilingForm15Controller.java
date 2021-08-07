package com.ey.in.tds.returns.web.rest;

import java.io.IOException;
import java.util.List;

import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ey.in.tcs.common.domain.dividend.Form15FileFormat;
import com.ey.in.tcs.common.domain.dividend.Form15FileType;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.dto.dividend.Form15FilingDetailsDTO;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.security.AllowedMimeTypes;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.dividend.forms.builder.gh.Quarter;
import com.ey.in.tds.returns.dto.Form15CAPartCResponse;
import com.ey.in.tds.returns.services.Filing15GService;
import com.ey.in.tds.returns.services.Filing15HService;
import com.ey.in.tds.returns.services.Filing15caService;
import com.ey.in.tds.returns.services.Filing15cbService;
import com.ey.in.tds.returns.services.Form15CAXmlGenerationService;
import com.ey.in.tds.returns.services.Form15CBXmlGenerationService;
import com.ey.in.tds.returns.services.Form15XmlGenerationService;
import com.ey.in.tds.returns.services.ScrapperHelperService;

@RestController
@RequestMapping("/api/returns")
public class FilingForm15Controller {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private Filing15cbService filing15cbService;

	@Autowired
	private Filing15caService filing15caService;

	@Autowired
	private Filing15GService filing15GService;

	@Autowired
	private Filing15HService filing15HService;

	@Autowired
	private Form15XmlGenerationService form15XmlGenerationService;

	@Autowired
	private Form15CBXmlGenerationService form15CBXmlGenerationService;

	@Autowired
	Form15CAXmlGenerationService form15CAXmlGenerationService;

	@Autowired
	ScrapperHelperService scrapperHelperService;

	@PostMapping(value = "/create/15cb")
	public ResponseEntity<ApiStatus<String>> create15cbReport(@RequestHeader("TAN-NUMBER") String tanNumber,
			@RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestParam(value = "fileFormat", required = true) Form15FileFormat fileFormat,
			@RequestParam("assessmentYear") Integer assessmentYear, @RequestParam("dateOfPosting") String dateOfPosting,
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader(value = "USER_NAME") String userName)
			throws Exception {

		String filing = null;
		if (fileFormat.isExcel()) {
			filing = this.filing15cbService.generate15CBExcelFilingReport(tanNumber, deductorPan, dateOfPosting,
					tenantId, userName, assessmentYear);
		} else {
			filing = this.form15CBXmlGenerationService.create15CBXmlFilingReport(tanNumber, deductorPan, dateOfPosting,
					tenantId, userName, assessmentYear);
		}
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS", filing, filing);
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	@PostMapping(value = "/create/15ca")
	public ResponseEntity<ApiStatus<String>> create15caReport(@RequestHeader("TAN-NUMBER") String tanNumber,
			@RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestParam(value = "fileFormat", required = true) Form15FileFormat fileFormat,
			@RequestParam("dateOfPosting") String postingDate, @RequestParam("assessmentYear") Integer assessmentYear,
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader(value = "USER_NAME") String userName)
			throws Exception {

		String filing = null;
		if (fileFormat.isExcel()) {
			filing = filing15caService.generate15CAExcelFilingReport(tanNumber, deductorPan, postingDate, tenantId,
					userName, assessmentYear);
		} else {
			filing = form15CAXmlGenerationService.generate15CAXmlFilingReport(tanNumber, deductorPan, postingDate,
					tenantId, userName, assessmentYear);
		}
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS", filing, filing);
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	@PostMapping(value = "/create/15g")
	public ResponseEntity<ApiStatus<String>> create15gReport(@RequestHeader("TAN-NUMBER") String tanNumber,
			@RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestParam(value = "fileFormat", required = true) Form15FileFormat fileFormat,
			@RequestParam("assessmentYear") Integer assessmentYear, @RequestParam("quarter") Quarter quarter,
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader(value = "USER_NAME") String userName)
			throws Exception {

		String filing = null;
		if (fileFormat.isExcel()) {
			filing = filing15GService.generate15GExcelFilingReport(tanNumber, deductorPan, quarter, tenantId, userName,
					assessmentYear);
		} else {
			filing = form15XmlGenerationService.create15GXmlFilingReport(tanNumber, deductorPan, quarter, tenantId,
					userName, assessmentYear);
		}
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS", filing, filing);
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	@PostMapping(value = "/create/15h")
	public ResponseEntity<ApiStatus<String>> create15hReport(@RequestHeader("TAN-NUMBER") String tanNumber,
			@RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestParam(value = "fileFormat", required = true) Form15FileFormat fileFormat,
			@RequestParam("assessmentYear") Integer assessmentYear, @RequestParam("quarter") Quarter quarter,
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader(value = "USER_NAME") String userName)
			throws Exception {

		String filing = null;
		if (fileFormat.isExcel()) {
			filing = filing15HService.generate15HExcelFilingReport(tanNumber, deductorPan, quarter, tenantId, userName,
					assessmentYear);
		} else {
			filing = form15XmlGenerationService.create15HXmlFilingReport(tanNumber, deductorPan, quarter, tenantId,
					userName, assessmentYear);
		}
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS", filing, filing);
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping(value = "/filing/form15/byPostingDate/{assessmentYear}")
	public ResponseEntity<ApiStatus<List<Form15FilingDetailsDTO>>> getFilingStatuses(
			@PathVariable("assessmentYear") Integer assessmentYear, @RequestHeader("TAN-NUMBER") String deductorTan,
			@RequestParam("dateOfPosting") String dateOfPosting,
			@RequestParam(value = "fileType", required = true) Form15FileType fileType,
			@RequestParam(value = "fileFormat", required = true) Form15FileFormat fileFormat) {

		logger.info("Tenenat return  Id ", MultiTenantContext.getTenantId());
		// logger.info("Keyspace return id ", KeyspaceContext.getKeyspaceId());

		List<Form15FilingDetailsDTO> filingStatus = filing15cbService.findFilingFiles(deductorTan, assessmentYear,
				dateOfPosting, fileType, fileFormat);

		ApiStatus<List<Form15FilingDetailsDTO>> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS",
				"Filing Files data", filingStatus);

		return new ResponseEntity<ApiStatus<List<Form15FilingDetailsDTO>>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping(value = "/filing/form15/byQuarter/{assessmentYear}")
	public ResponseEntity<ApiStatus<List<Form15FilingDetailsDTO>>> getFilingStatuses(
			@PathVariable("assessmentYear") Integer assessmentYear, @RequestHeader("TAN-NUMBER") String deductorTan,
			@RequestParam("quarter") Quarter quarter,
			@RequestParam(value = "fileType", required = true) Form15FileType fileType,
			@RequestParam(value = "fileFormat", required = true) Form15FileFormat fileFormat) {

		logger.info("Tenenat return Id ", MultiTenantContext.getTenantId());
		// logger.info("Keyspace return id ", KeyspaceContext.getKeyspaceId());

		List<Form15FilingDetailsDTO> filingStatus = filing15cbService.findFilingFiles(deductorTan, assessmentYear,
				quarter != null ? quarter.startingDate(assessmentYear) : null, fileType, fileFormat);

		ApiStatus<List<Form15FilingDetailsDTO>> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS",
				"Filing Files data", filingStatus);

		return new ResponseEntity<ApiStatus<List<Form15FilingDetailsDTO>>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping(path = "/start")
	public ResponseEntity<Form15CAPartCResponse> isScraperBusy() throws Exception {
		Form15CAPartCResponse response = new Form15CAPartCResponse(null);
		//TODO The code is commented as the ITR website is undermaintainance, comments should be removed once the website is live
		/*if (scrapperHelperService.isScraperBusy()) {
			response.setMessage("Acknowledgment Number Fetching From ITR Portal is under process");
			response.setStatus(false);

		} else {
			response.setStatus(true);
			response.setMessage("Web Scraper is free and ready to take job");
			response.setCaptchaImage(scrapperHelperService.startSessionAndGenerateCaptcha());
		}*/
		response.setStatus(true);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	/*
	 * @PostMapping(path = "/generateForm15AC") public
	 * ResponseEntity<ApiStatus<Form15CAPartCResponse>> generateForm15ACReports(
	 * 
	 * @ModelAttribute Credentials credentials, @RequestHeader("X-TENANT-ID") String
	 * tenantId,
	 * 
	 * @RequestParam("assessmentYear") Integer assessmentYear) throws IOException {
	 * 
	 * AckNumResponseMessage ackNumResponseMessage = null;
	 * ApiStatus<Form15CAPartCResponse> apiStatus = null; if
	 * (this.jobHandler.isAnyProcessing()) { Form15CAPartCResponse
	 * form15CAPartCResponse = new Form15CAPartCResponse(null);
	 * form15CAPartCResponse.
	 * setMessage("Another process is running. Please try after sometime.");
	 * form15CAPartCResponse.setStatus(false); apiStatus = new
	 * ApiStatus<>(HttpStatus.OK, "SUCCESS",
	 * "Another process is running. Please try after sometime.",
	 * form15CAPartCResponse); return new
	 * ResponseEntity<ApiStatus<Form15CAPartCResponse>>(apiStatus, HttpStatus.OK); }
	 * if (scrapperHelperService.canGenerateForm15CAPartC() != null) {
	 * Form15CAPartCResponse form15CAPartCResponse = new
	 * Form15CAPartCResponse(null);
	 * form15CAPartCResponse.setMessage(scrapperHelperService.
	 * canGenerateForm15CAPartC()); form15CAPartCResponse.setStatus(false);
	 * apiStatus = new ApiStatus<>(HttpStatus.SERVICE_UNAVAILABLE,
	 * "SERVICE_UNAVAILABLE", "SERVICE_UNAVAILABLE", form15CAPartCResponse); return
	 * new ResponseEntity<ApiStatus<Form15CAPartCResponse>>(apiStatus,
	 * HttpStatus.SERVICE_UNAVAILABLE); }
	 * 
	 * String jobId = UUID.randomUUID().toString();
	 * Executors.newSingleThreadExecutor().execute(() -> { try {
	 * scrapperHelperService.generateAcknowledgementForm15CAPartC(credentials,
	 * jobId, tenantId, assessmentYear); } catch (Exception ex) {
	 * logger.error("Failed to complete job {}", jobId, ex); } }); return new
	 * ResponseEntity<ApiStatus<Form15CAPartCResponse>>(apiStatus, HttpStatus.OK); }
	 */

	/**
	 * this method is responsile to generate form 15 CA from excel(15CA) being
	 * uploaded by client it will process the file and will take the data from excel
	 * sheet and will update the system data so that newly generated output file
	 * will come with modified data
	 * 
	 * @param tenantId
	 * @param file
	 * @return
	 * @throws IOException
	 */
	@PostMapping("generarte/form15CA/fromExcel")
	public ResponseEntity<ApiStatus<String>> generateForm15CAFromExcel(@RequestHeader("TAN-NUMBER") String tanNumber,
			@RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestParam(value = "fileFormat", required = true) Form15FileFormat fileFormat,
			@RequestParam("assessmentYear") Integer assessmentYear, @RequestParam("dateOfPosting") String dateOfPosting,
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader(value = "USER_NAME") String userName,
			@RequestParam("file") MultipartFile file) throws IOException {
		MultiTenantContext.setTenantId(tenantId);
		String contentType = new Tika().detect(file.getInputStream(), file.getOriginalFilename());
		logger.info("Controller method processing the file with content type " + contentType + "{}");
		if (AllowedMimeTypes.XLSX.getMimeType().equals(contentType)
				|| AllowedMimeTypes.OOXML.getMimeType().equals(contentType)) {
			filing15caService.generateForm15CAFromExcel(file, tanNumber, deductorPan, dateOfPosting, tenantId, userName,
					assessmentYear, fileFormat);
		} else {
			throw new CustomException("Only Excel Files are Allowed");
		}
		return null;

	}

	@PostMapping("generarte/form15CB/fromExcel")
	public ResponseEntity<ApiStatus<String>> generateForm15CBFromExcel(@RequestHeader("TAN-NUMBER") String tanNumber,
			@RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestParam(value = "fileFormat", required = true) Form15FileFormat fileFormat,
			@RequestParam("dateOfPosting") String postingDate, @RequestParam("assessmentYear") Integer assessmentYear,
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader(value = "USER_NAME") String userName,
			@RequestParam("file") MultipartFile file) throws IOException {
		MultiTenantContext.setTenantId(tenantId);
		String contentType = new Tika().detect(file.getInputStream(), file.getOriginalFilename());
		logger.info("Controller method processing the file with content type " + contentType + "{}");
		if (AllowedMimeTypes.XLSX.getMimeType().equals(contentType)
				|| AllowedMimeTypes.OOXML.getMimeType().equals(contentType)) {
			filing15cbService.generateForm15CBFromExcel(file, tanNumber, deductorPan, postingDate, tenantId, userName,
					assessmentYear, fileFormat);
		} else {
			throw new CustomException("Only Excel Files are Allowed");
		}
		return null;

	}
}
