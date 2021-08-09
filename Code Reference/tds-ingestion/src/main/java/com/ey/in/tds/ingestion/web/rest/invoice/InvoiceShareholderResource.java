package com.ey.in.tds.ingestion.web.rest.invoice;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ValidationException;

import org.apache.tika.Tika;
import org.owasp.esapi.errors.IntrusionException;
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

import com.ey.in.tcs.common.domain.dividend.InvoiceShareholderNonResident;
import com.ey.in.tds.common.domain.dividend.InvoiceShareholderResident;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.dto.CommonDTO;
import com.ey.in.tds.common.dto.DeducteeDetailDTO;
import com.ey.in.tds.common.dto.DividendInvoiceDTO;
import com.ey.in.tds.common.dto.dividend.Form15CBDetails;
import com.ey.in.tds.common.dto.dividend.InvoiceShareholderNonResidentDTO;
import com.ey.in.tds.common.dto.dividend.InvoiceShareholderResidentDTO;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.core.exceptions.TanNotFoundException;
import com.ey.in.tds.core.security.AllowedMimeTypes;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.ingestion.service.invoiceshareholder.InvoiceShareholderService;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.microsoft.azure.storage.StorageException;

@RestController
@RequestMapping("/api/ingestion")
public class InvoiceShareholderResource {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private InvoiceShareholderService invoiceShareholderService;

	@PostMapping(value = "/invoices-shareholder/nonresident/{year}/{month}/{section}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<InvoiceShareholderNonResidentDTO>>> getNonResidentShareholder(
			@RequestHeader(value = "DEDUCTOR-PAN") String pan, @RequestHeader("X-TENANT-ID") String tenantId,
			@PathVariable int year, @PathVariable int month, @PathVariable String section,
			@RequestBody Pagination pagination) throws RecordNotFoundException, TanNotFoundException {
		logger.info("X-TENANT-ID _______: {}", tenantId);

		CommonDTO<InvoiceShareholderNonResidentDTO> response = null;
		if (pan != null) {
			response = invoiceShareholderService.getNonResidentShareholder(pan, year, month, pagination, section);
		} else {
			logger.error("Deductor Pan not found to proceed");
			throw new CustomException("Deductor Pan not found to proceed", HttpStatus.BAD_REQUEST);
		}

		logger.info("List of nonresident shareholder done");
		ApiStatus<CommonDTO<InvoiceShareholderNonResidentDTO>> apiStatus = new ApiStatus<CommonDTO<InvoiceShareholderNonResidentDTO>>(
				HttpStatus.OK, "SUCCESS", "NO ALERT", response);
		return new ResponseEntity<ApiStatus<CommonDTO<InvoiceShareholderNonResidentDTO>>>(apiStatus, HttpStatus.OK);

	}

	@GetMapping(value = "/invoices-shareholder/nonresident-cb/{assessmentYear}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<InvoiceShareholderNonResident>>> getNonResidentShareholderForCBGenerated(
			@RequestHeader("X-TENANT-ID") String tenantId, @PathVariable(value = "assessmentYear") int financialYear)
			throws RecordNotFoundException, ParseException {
		logger.info("X-TENANT-ID _______: {}", tenantId);

		List<InvoiceShareholderNonResident> response = null;
		response = invoiceShareholderService.getNonResidentShareholderfor15CB(financialYear);

		logger.info("List of nonresident shareholder done");
		ApiStatus<List<InvoiceShareholderNonResident>> apiStatus = new ApiStatus<List<InvoiceShareholderNonResident>>(
				HttpStatus.OK, "SUCCESS", "NO ALERT", response);
		return new ResponseEntity<ApiStatus<List<InvoiceShareholderNonResident>>>(apiStatus, HttpStatus.OK);
	}

	@PutMapping("/invoices-shareholder/nonresident-cb")
	public ResponseEntity<ApiStatus<List<InvoiceShareholderNonResident>>> updateNonResidentShareholder(
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestBody List<Form15CBDetails> form15CBDetails)
			throws RecordNotFoundException, TanNotFoundException, ParseException {
		logger.info("X-TENANT-ID _______: {}", tenantId);
		List<InvoiceShareholderNonResident> response = null;
		response = invoiceShareholderService.updateNonResidentShareholderfor15CB(form15CBDetails);
		ApiStatus<List<InvoiceShareholderNonResident>> apiStatus = new ApiStatus<List<InvoiceShareholderNonResident>>(
				HttpStatus.OK, "SUCCESS", "NO ALERT", response);
		return new ResponseEntity<ApiStatus<List<InvoiceShareholderNonResident>>>(apiStatus, HttpStatus.OK);

	}

	@PutMapping("/invoices-shareholder/nonresident-cb/{id}")
	public ResponseEntity<ApiStatus<List<InvoiceShareholderNonResident>>> updateNonResidentShareholder(
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@PathVariable Integer id) throws RecordNotFoundException, TanNotFoundException, ParseException {
		List<InvoiceShareholderNonResident> response = null;
		invoiceShareholderService.updateNonResidentShareholder15CBFlag(deductorPan, id);
		ApiStatus<List<InvoiceShareholderNonResident>> apiStatus = new ApiStatus<List<InvoiceShareholderNonResident>>(
				HttpStatus.OK, "SUCCESS", "NO ALERT", response);
		return new ResponseEntity<ApiStatus<List<InvoiceShareholderNonResident>>>(apiStatus, HttpStatus.OK);

	}

	@PostMapping(value = "/invoices-shareholder/resident/{year}/{month}/{section}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<InvoiceShareholderResidentDTO>>> getResidentShareholder(
			@RequestHeader(value = "DEDUCTOR-PAN") String pan, @RequestHeader("X-TENANT-ID") String tenantId,
			@PathVariable int year, @PathVariable int month, @PathVariable String section,
			@RequestBody Pagination pagination) throws RecordNotFoundException, TanNotFoundException {
		logger.info("X-TENANT-ID _______: {}", tenantId);

		CommonDTO<InvoiceShareholderResidentDTO> response = null;
		if (pan != null) {
			response = invoiceShareholderService.getResidentShareholder(pan, year, month, pagination, section);
		} else {
			logger.error("Deductor Pan not found to proceed");
			throw new CustomException("Deductor Pan not found to proceed", HttpStatus.BAD_REQUEST);
		}

		logger.info("List of resident shareholder done");
		ApiStatus<CommonDTO<InvoiceShareholderResidentDTO>> apiStatus = new ApiStatus<CommonDTO<InvoiceShareholderResidentDTO>>(
				HttpStatus.OK, "SUCCESS", "NO ALERT", response);
		return new ResponseEntity<ApiStatus<CommonDTO<InvoiceShareholderResidentDTO>>>(apiStatus, HttpStatus.OK);

	}

	/**
	 * to get the resident invoice share holder type using id
	 * 
	 * @param deductorPan
	 * @param id
	 * @param tenantId
	 * @return
	 */
	@GetMapping(value = "/invoice-shareholder/resident/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<InvoiceShareholderResidentDTO>> getResidentialShareholder(
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @PathVariable Integer id,
			@RequestHeader(value = "X-TENANT-ID") String tenantId) {
		InvoiceShareholderResidentDTO result = invoiceShareholderService.getResidentialShareholder(deductorPan, id);
		logger.info("REST response to get a Shareholder Resident Record : {}", result);
		ApiStatus<InvoiceShareholderResidentDTO> apiStatus = new ApiStatus<InvoiceShareholderResidentDTO>(HttpStatus.OK,
				"SUCCESS", "TO GET A SHAREHOLDER RESIDENT RECORD SUCCESSFULLY ", result);
		return new ResponseEntity<ApiStatus<InvoiceShareholderResidentDTO>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping(value = "/invoice-shareholder/nonresident/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<InvoiceShareholderNonResidentDTO>> getNonResidentialShareholder(
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @PathVariable Integer id,
			@RequestHeader(value = "X-TENANT-ID") String tenantId) {
		InvoiceShareholderNonResidentDTO result = invoiceShareholderService.getNonResidentialShareholder(deductorPan,
				id);
		logger.info("REST response to get a Shareholder NonResident Record : {}", result);
		ApiStatus<InvoiceShareholderNonResidentDTO> apiStatus = new ApiStatus<InvoiceShareholderNonResidentDTO>(
				HttpStatus.OK, "SUCCESS", "TO GET A SHAREHOLDER NONRESIDENT RECORD SUCCESSFULLY ", result);
		return new ResponseEntity<ApiStatus<InvoiceShareholderNonResidentDTO>>(apiStatus, HttpStatus.OK);
	}

	@PutMapping(value = "/invoice-shareholder/approveModify/resident", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<InvoiceShareholderResident>> approveOrModifyResidentInvoice(
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestBody DividendInvoiceDTO invoiceDTO) {
		InvoiceShareholderResident result = invoiceShareholderService.approveOrModifyResidentInvoice(invoiceDTO);
		logger.info("REST response to approve/modify a Shareholder Resident Record : {}", result);
		ApiStatus<InvoiceShareholderResident> apiStatus = new ApiStatus<InvoiceShareholderResident>(HttpStatus.OK,
				"SUCCESS", "TO approve/modify a SHAREHOLDER RESIDENT RECORD SUCCESSFULLY ", result);
		return new ResponseEntity<ApiStatus<InvoiceShareholderResident>>(apiStatus, HttpStatus.OK);
	}

	@PutMapping(value = "/invoice-shareholder/approveModify/nonresident", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<InvoiceShareholderNonResident>> approveOrModifyNonResidentInvoice(
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestBody DividendInvoiceDTO invoiceDTO) {
		InvoiceShareholderNonResident result = invoiceShareholderService.approveOrModifyNonResidentInvoice(invoiceDTO);
		logger.info("REST response to approve/modify a Shareholder Non Resident Record : {}", result);
		ApiStatus<InvoiceShareholderNonResident> apiStatus = new ApiStatus<InvoiceShareholderNonResident>(HttpStatus.OK,
				"SUCCESS", "TO approve/modify a SHAREHOLDER Non RESIDENT RECORD SUCCESSFULLY ", result);
		return new ResponseEntity<ApiStatus<InvoiceShareholderNonResident>>(apiStatus, HttpStatus.OK);
	}

	@PostMapping("/shareholder-invoice/batch/excel")
	public ResponseEntity<ApiStatus<BatchUpload>> uploadExcel(@RequestParam("year") int assessmentYear,
			@RequestParam("files") MultipartFile[] files, @RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @RequestParam("type") String type,
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader(value = "USER_NAME") String userName)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException, InvalidFormatException,
			ParseException, IntrusionException, ValidationException,
			org.apache.poi.openxml4j.exceptions.InvalidFormatException, org.owasp.esapi.errors.ValidationException {
		boolean validFiles = true;
		if (files != null && files.length > 0) {
			for (MultipartFile file : files) {
				String contentType = new Tika().detect(file.getInputStream(), file.getOriginalFilename());
				if (!AllowedMimeTypes.XLSX.getMimeType().equals(contentType)
						&& !AllowedMimeTypes.OOXML.getMimeType().equals(contentType)
						&& !AllowedMimeTypes.CSV.getMimeType().equals(contentType)) {
					validFiles = false;
				}
			}
			if (!validFiles) {
				logger.warn("Unsupported file upload attempt by {}", userName);
				throw new CustomException("Invalid file contents, only xlsx and csv files are allowed",
						HttpStatus.BAD_REQUEST);
			}
			logger.info("Upload Params: {}, {}, {}", assessmentYear, files.length, deductorPan);

			BatchUpload batchData = invoiceShareholderService.processApproveDividendProcessing(assessmentYear, files,
					tenantId, userName, deductorPan, tan, type,null);
			ApiStatus<BatchUpload> apiStatus = new ApiStatus<BatchUpload>(HttpStatus.CREATED, "SUCCESS", "NO ALERT",
					batchData);
			return new ResponseEntity<ApiStatus<BatchUpload>>(apiStatus, HttpStatus.OK);

		} else {
			throw new IllegalArgumentException("Atleast one file must be uploaded.");
		}
	}

	@PostMapping("/invoice-shareholder/batch/approve")
	public ResponseEntity<ApiStatus<BatchUpload>> batchApprove(@RequestParam("files") MultipartFile[] files,
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestParam("type") String type, @RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestHeader(value = "USER_NAME") String userName,@RequestParam("residentType")String residentType,
			@RequestParam("assessmentYear") int assessmentYear)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException, InvalidFormatException,
			ParseException, IntrusionException, ValidationException,
			org.apache.poi.openxml4j.exceptions.InvalidFormatException, org.owasp.esapi.errors.ValidationException {
		boolean validFiles = true;
		if (files != null && files.length > 0) {
			for (MultipartFile file : files) {
				String contentType = new Tika().detect(file.getInputStream(), file.getOriginalFilename());
				if (!AllowedMimeTypes.XLSX.getMimeType().equals(contentType)
						&& !AllowedMimeTypes.OOXML.getMimeType().equals(contentType)) {
					validFiles = false;
				}
			}
			if (!validFiles) {
				logger.warn("Unsupported file upload attempt by {}", userName);
				throw new CustomException("Invalid file contents, only xlsx files are allowed",
						HttpStatus.BAD_REQUEST);
			}
			logger.info("Upload Params: {}, {}, {}", files.length, deductorPan);

			// type would be changed for this
			BatchUpload batchData = invoiceShareholderService.processApproveDividendProcessing(assessmentYear, files,
					tenantId, userName, deductorPan, tan, type,residentType);
			ApiStatus<BatchUpload> apiStatus = new ApiStatus<BatchUpload>(HttpStatus.CREATED, "SUCCESS", "NO ALERT",
					batchData);
			return new ResponseEntity<ApiStatus<BatchUpload>>(apiStatus, HttpStatus.OK);

		} else {
			throw new IllegalArgumentException("Atleast one file must be uploaded.");
		}
	}

	// invoice resident shareholder
	@PostMapping("/resident/create")
	public ResponseEntity<ApiStatus<InvoiceShareholderResident>> createResidentShareholder(
			@RequestHeader("X-TENANT-ID") String tenantId,
			@RequestBody InvoiceShareholderResident invoiceShareholderResident)
			throws RecordNotFoundException, TanNotFoundException {
		logger.info("X-TENANT-ID _______: {}", tenantId);
		InvoiceShareholderResident response = invoiceShareholderService
				.createShareholderResident(invoiceShareholderResident);
		ApiStatus<InvoiceShareholderResident> apiStatus = new ApiStatus<InvoiceShareholderResident>(HttpStatus.OK,
				"SUCCESS", "NO ALERT", response);
		return new ResponseEntity<ApiStatus<InvoiceShareholderResident>>(apiStatus, HttpStatus.OK);

	}

	// invoice non resident
	@PostMapping("/nonresident/create")
	public ResponseEntity<ApiStatus<InvoiceShareholderNonResident>> createNonResidentShareholder(
			@RequestHeader("X-TENANT-ID") String tenantId,
			@RequestBody InvoiceShareholderNonResident invoiceShareholderNonResident)
			throws RecordNotFoundException, TanNotFoundException {
		logger.info("X-TENANT-ID _______: {}", tenantId);
		InvoiceShareholderNonResident response = invoiceShareholderService
				.createShareholderNonResident(invoiceShareholderNonResident);
		ApiStatus<InvoiceShareholderNonResident> apiStatus = new ApiStatus<InvoiceShareholderNonResident>(HttpStatus.OK,
				"SUCCESS", "NO ALERT", response);
		return new ResponseEntity<ApiStatus<InvoiceShareholderNonResident>>(apiStatus, HttpStatus.OK);

	}

	// List of invoice shareholder non resident for 15 cb
	@PostMapping(value = "/invoices-shareholder/nonresident", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<InvoiceShareholderNonResident>>> getNonResidentShareholderFor15CB(
			@RequestHeader(value = "DEDUCTOR-PAN") String pan, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestParam("dateOfPosting") String dateOfPosting, @RequestParam("year") Integer year)
			throws RecordNotFoundException, TanNotFoundException, ParseException {
		logger.info("X-TENANT-ID _______: {}", tenantId);

		List<InvoiceShareholderNonResident> response = null;
		if (pan != null) {
			response = invoiceShareholderService.getNonResidentShareholderfor15CB(pan, dateOfPosting, year);
		} else {
			logger.error("Deductor Pan not found to proceed");
			throw new CustomException("Deductor Pan not found to proceed", HttpStatus.BAD_REQUEST);
		}

		logger.info("List of nonresident shareholder done");
		ApiStatus<List<InvoiceShareholderNonResident>> apiStatus = new ApiStatus<List<InvoiceShareholderNonResident>>(
				HttpStatus.OK, "SUCCESS", "NO ALERT", response);
		return new ResponseEntity<ApiStatus<List<InvoiceShareholderNonResident>>>(apiStatus, HttpStatus.OK);

	}

	@GetMapping("/distinct/dateOfPosting")
	public ResponseEntity<ApiStatus<Set<String>>> getDateOfPosting(

			@RequestHeader(value = "DEDUCTOR-PAN") String deductorPan, @RequestParam("assesmentYear") Integer year)
			throws RecordNotFoundException, TanNotFoundException {
		ApiStatus<Set<String>> apiStatus = new ApiStatus<Set<String>>(HttpStatus.OK, "SUCCESS",
				"LIST OF Distint Date of Posting", invoiceShareholderService.getDateOfPostingList(deductorPan, year));
		return new ResponseEntity<ApiStatus<Set<String>>>(apiStatus, HttpStatus.OK);
	}

	// List of invoice shareholder resident for 15 GH
	@PostMapping(value = "/invoices-shareholder/resident", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<InvoiceShareholderResident>>> getResidentShareholderFor15GH(
			@RequestHeader(value = "DEDUCTOR-TAN") String tan, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestParam("quarter") String quarter, @RequestParam("assessmentYear") int assessmentYear)
			throws RecordNotFoundException, TanNotFoundException, ParseException {
		logger.info("X-TENANT-ID _______: {}", tenantId);

		List<InvoiceShareholderResident> response = null;
		if (tan != null) {
			response = invoiceShareholderService.getResidentShareholderfor15GH(tan, quarter, assessmentYear);
		} else {
			logger.error("Deductor Pan not found to proceed");
			throw new CustomException("Deductor Pan not found to proceed", HttpStatus.BAD_REQUEST);
		}

		logger.info("List of resident shareholder done");
		ApiStatus<List<InvoiceShareholderResident>> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", "NO ALERT",
				response);
		return new ResponseEntity<ApiStatus<List<InvoiceShareholderResident>>>(apiStatus, HttpStatus.OK);

	}

	@PostMapping(value = "/invoices/shareholder/resident")
	public ResponseEntity<ApiStatus<List<DeducteeDetailDTO>>> getResidentShareholders(
			@RequestHeader(value = "DEDUCTOR-TAN") String tan, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestBody Map<String, String> receiptObj) {

		String receiptSerailNo = receiptObj.get("receiptSerailNo");
		String bsrCode = receiptObj.get("bsrCode");
		String receiptDate = receiptObj.get("receiptDate");
		Integer assessmentYear = Integer.valueOf(receiptObj.get("assessmentYear"));
		List<DeducteeDetailDTO> response = invoiceShareholderService.getResidentShareholders(receiptSerailNo, bsrCode,
				receiptDate, assessmentYear, tan);

		ApiStatus<List<DeducteeDetailDTO>> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", "NO ALERT", response);
		return new ResponseEntity<ApiStatus<List<DeducteeDetailDTO>>>(apiStatus, HttpStatus.OK);

	}

	@PostMapping(value = "/invoices/shareholder/nonresident")
	public ResponseEntity<ApiStatus<List<DeducteeDetailDTO>>> getNonResidentShareholders(
			@RequestHeader(value = "DEDUCTOR-TAN") String tan, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestBody Map<String, String> receiptObj) {

		String receiptSerailNo = receiptObj.get("receiptSerailNo");
		String bsrCode = receiptObj.get("bsrCode");
		String receiptDate = receiptObj.get("receiptDate");
		Integer assessmentYear = Integer.valueOf(receiptObj.get("assessmentYear"));
		List<DeducteeDetailDTO> response = invoiceShareholderService.getNonResidentShareholders(receiptSerailNo,
				bsrCode, receiptDate, assessmentYear, tan);

		ApiStatus<List<DeducteeDetailDTO>> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", "NO ALERT", response);
		return new ResponseEntity<ApiStatus<List<DeducteeDetailDTO>>>(apiStatus, HttpStatus.OK);

	}

	@GetMapping("/invoice-shareholder/downloadSummary/{deductorPan}/{residentType}/{year}")
	public ResponseEntity<ApiStatus<String>> dividendDownloadSummary(@PathVariable String residentType,
			@PathVariable String deductorPan, @PathVariable Integer year, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestParam(value = "tan") String tan,@RequestParam(value = "userName") String userName
			)
			throws Exception {

		if (deductorPan == null) {
			throw new CustomException("No Pan Found to continue the download", HttpStatus.BAD_REQUEST);
		}
		invoiceShareholderService.dividendDownloadSummary(deductorPan, residentType,
				year,tan, userName);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", "NO ALERT", "Dividend Summery Report will Be Available Shortly");
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping("/invoice-shareholder/liabilityReport/{deductorPan}/{year}")
	public ResponseEntity<ApiStatus<String>> dividendLiabilityReport(@PathVariable String deductorPan,
			@PathVariable Integer year, @RequestHeader("X-TENANT-ID") String tenantId,@RequestParam(value = "tan")  String tan,
			@RequestParam(value = "level")  String level) throws Exception {

		if (deductorPan == null) {
			throw new CustomException("No Pan Found to continue the download", HttpStatus.BAD_REQUEST);
		}
		invoiceShareholderService.dividendLiabilityReport(deductorPan, year,tan,level);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", "NO ALERT", "Dividend Liability Report will Be Available Shortly");
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}
	
	 /**
	 * 
	 * @param deductorPan
	 * @param type
	 * @return
	 */
	@GetMapping(value = "/payout/count/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<Map<String, Integer>>> getActiveAndInactiveDeducteeCount(
			@RequestHeader(value = "DEDUCTOR-PAN") String deductorPan,
			@PathVariable(value = "type", required = true) String type) {
		ApiStatus<Map<String, Integer>> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", " ",
				invoiceShareholderService.getActiveAndInactiveShareHolderCounts(deductorPan, type));
		return new ResponseEntity<ApiStatus<Map<String, Integer>>>(apiStatus, HttpStatus.OK);
	}

}