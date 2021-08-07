
package com.ey.in.tds.ingestion.web.rest.invoice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
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

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.InvoiceLineItem;
import com.ey.in.tds.common.dto.CRDTO;
import com.ey.in.tds.common.dto.CommonDTO;
import com.ey.in.tds.common.dto.InvoiceDTO;
import com.ey.in.tds.common.dto.InvoiceLineItemCRDTO;
import com.ey.in.tds.common.dto.csvfile.CsvFileDTO;
import com.ey.in.tds.common.dto.invoice.InvoiceMismatchByBatchIdDTO;
import com.ey.in.tds.common.dto.invoice.MismatchesFiltersDTO;
import com.ey.in.tds.common.ingestion.response.dto.InvoiceLineItemResponseDTO;
import com.ey.in.tds.common.model.invoicelineitem.InvoiceLineItemDTO;
import com.ey.in.tds.common.model.invoicelineitem.InvoicePDFDTO;
import com.ey.in.tds.common.model.invoicelineitem.InvoicePdfList;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.common.service.BlobStorageService;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.core.exceptions.TanNotFoundException;
import com.ey.in.tds.core.security.AllowedMimeTypes;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.ingestion.dto.invoice.UpdateOnScreenDTO;
import com.ey.in.tds.ingestion.jdbc.dao.InvoiceLineItemDAO;
import com.ey.in.tds.ingestion.service.batchupload.BatchUploadService;
import com.ey.in.tds.ingestion.service.invoicelineitem.InvoiceLineItemService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.StorageException;

@RestController
@RequestMapping("/api/ingestion")
public class InvoiceResource {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private InvoiceLineItemService invoiceLineItemService;

	@Autowired
	private BatchUploadService batchUploadService;

	@Autowired
	private BlobStorageService blobStorageService;

	@Autowired
	private InvoiceLineItemDAO invoiceLineItemDAO;

	/*
	 * This api will return invoice line table data based on section to challans
	 */

	@GetMapping("/invoicedata/{section}")
	public Object getInvoiceLineItemTableData(@PathVariable String section) {
		// change to
		// TODO : Fix this
		logger.error("FixMe!");
		throw new RuntimeException("FixMe!");

	}

	/**
	 * Get the List oF records for invoice based on mismatch and Batch Id
	 * 
	 * @param batchId
	 * @return
	 * @throws TanNotFoundException
	 */
	@GetMapping("/tdsmismatches/{batchId}")
	public ResponseEntity<ApiStatus<List<InvoiceMismatchByBatchIdDTO>>> listOfMisMatchesBasedonBatchUploadIdType(
			@PathVariable Integer batchId, @RequestHeader(value = "TAN-NUMBER") String tan)
			throws TanNotFoundException {

		if (batchId != null && tan != null) {
			List<InvoiceMismatchByBatchIdDTO> listMismatchesByBatchId = invoiceLineItemService
					.getInvoiceMismatchsByBatchUploadIDType(batchId, tan);

			logger.info("List of mismatches based on Batch Upload pdf done");
			ApiStatus<List<InvoiceMismatchByBatchIdDTO>> apiStatus = new ApiStatus<List<InvoiceMismatchByBatchIdDTO>>(
					HttpStatus.OK, "SUCCESS", "LIST OF MISMATCHES FOR PARTICULAR BATCH", listMismatchesByBatchId);
			return new ResponseEntity<ApiStatus<List<InvoiceMismatchByBatchIdDTO>>>(apiStatus, HttpStatus.OK);

		} else {
			logger.error("No batch Id/tan, Cannot process");
			throw new CustomException("No batch Id/tan, Cannot process", HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * List of mismatches for excel (for Excel)
	 * 
	 * @return
	 * @throws TanNotFoundException
	 */
	@GetMapping("tdsmismatches/all/{year}/{month}")
	public ResponseEntity<ApiStatus<List<InvoiceMismatchByBatchIdDTO>>> listOfMisMatchesForExcelBasedonBatchUploadId(
			@RequestHeader(value = "TAN-NUMBER") String tan, @PathVariable int year, @PathVariable int month)
			throws TanNotFoundException {
		if (tan != null) {

			List<InvoiceMismatchByBatchIdDTO> listMismatchesAll = invoiceLineItemService.getInvoiceMismatchAll(tan,
					year, month);

			ApiStatus<List<InvoiceMismatchByBatchIdDTO>> apiStatus = new ApiStatus<List<InvoiceMismatchByBatchIdDTO>>(
					HttpStatus.OK, "SUCCESS", "LIST OF MISMATCHES", listMismatchesAll);
			return new ResponseEntity<ApiStatus<List<InvoiceMismatchByBatchIdDTO>>>(apiStatus, HttpStatus.OK);
		} else {
			logger.error("Tan / Processed From Not Found");
			throw new CustomException("Tan / Processed From Not Found", HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Based on MismatchCategory and BatchId get the record for INVOICE_EXCEL
	 * 
	 * @param batchId
	 * @param mismatchcategory
	 * @return
	 * @throws TanNotFoundException
	 */
	@PostMapping(value = "tdsmismatches/{batchId}/{mismatchcategory}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<InvoiceLineItem>>> listOfMisMatchesBasedonBatchUploadIdCategory(
			@RequestHeader(value = "TAN-NUMBER") String tan, @PathVariable Integer batchId,
			@PathVariable String mismatchcategory, @RequestBody Pagination pagination) throws TanNotFoundException {
		if (tan != null && batchId != null && mismatchcategory != null) {
			CommonDTO<InvoiceLineItem> listMismatchesByBatchIdMiscmatchCategory = invoiceLineItemService
					.getInvoiceMismatchByBatchUploadIDMismatchCategory(batchId, mismatchcategory, Arrays.asList(tan),
							pagination);

			ApiStatus<CommonDTO<InvoiceLineItem>> apiStatus = new ApiStatus<CommonDTO<InvoiceLineItem>>(HttpStatus.OK,
					"SUCCESS", "NO ALERT", listMismatchesByBatchIdMiscmatchCategory);
			return new ResponseEntity<ApiStatus<CommonDTO<InvoiceLineItem>>>(apiStatus, HttpStatus.OK);
		} else {
			logger.error("Tan Not Found");
			throw new CustomException("Tan Not Found", HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Update mismatch table based on Mismatch category for Excel
	 * 
	 * @param mismatchcategory
	 * @param invoiceMismatchUpdateDTO
	 * @return
	 * @throws URISyntaxException
	 */
	@PutMapping(value = "tdsmismatches/{mismatchcategory}/action", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<UpdateOnScreenDTO>> updateMismatchByAction(
			@RequestHeader(value = "TAN-NUMBER") String tan, @PathVariable("mismatchcategory") String mismatchcategory,
			@RequestBody UpdateOnScreenDTO invoiceMismatchUpdateDTO, @RequestHeader("Authorization") String token,
			@RequestHeader(value = "DEDUCTOR-PAN") String pan) throws URISyntaxException {
		// Handling null pointer exception
		if (tan == null && mismatchcategory == null) {
			throw new CustomException("Tan / Mismatch Category  Not Found", HttpStatus.BAD_REQUEST);
		}
		UpdateOnScreenDTO response = invoiceLineItemService.updateMismatchByAction(tan, invoiceMismatchUpdateDTO, token,
				pan);

		ApiStatus<UpdateOnScreenDTO> apiStatus = new ApiStatus<UpdateOnScreenDTO>(HttpStatus.OK, "SUCCESS",
				"LIST OF MISMATCHES UPDATED", response);
		return new ResponseEntity<ApiStatus<UpdateOnScreenDTO>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param tan
	 * @param restype
	 * @param tenantId
	 * @param year
	 * @param month
	 * @param filetype
	 * @param deducteeName
	 * @param pagination
	 * @return
	 * @throws RecordNotFoundException
	 * @throws TanNotFoundException
	 */
	@PostMapping(value = "invoices-lineitem/{restype}/{filetype}/{year}/{month}/{deducteeName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<InvoiceLineItem>>> getResidentAndNonresident(
			@RequestHeader(value = "TAN-NUMBER") String tan, @PathVariable String restype,
			@RequestHeader("X-TENANT-ID") String tenantId, @PathVariable int year, @PathVariable int month,
			@PathVariable String filetype, @PathVariable String deducteeName, @RequestBody Pagination pagination)
			throws RecordNotFoundException, TanNotFoundException {
		logger.info("X-TENANT-ID _______: {}", tenantId);

		CommonDTO<InvoiceLineItem> response = null;
		if (tan != null && restype != null && filetype != null) {
			response = invoiceLineItemService.getResidentAndNonresident(restype, filetype, tan, year, month, pagination,
					deducteeName);
		} else {
			logger.error("Tan/ rest type / file type not found to proceed");
			throw new CustomException("Tan/ rest type / file type not found to proceed", HttpStatus.BAD_REQUEST);
		}

		logger.info("List of Resident and NonResident responses done");
		ApiStatus<CommonDTO<InvoiceLineItem>> apiStatus = new ApiStatus<CommonDTO<InvoiceLineItem>>(HttpStatus.OK,
				"SUCCESS", "NO ALERT", response);
		return new ResponseEntity<ApiStatus<CommonDTO<InvoiceLineItem>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * To get the mismatch records based on mismatch Category
	 * 
	 * @param mismatchcategory
	 * @return
	 * @throws TanNotFoundException
	 * @throws JsonProcessingException 
	 * @throws JsonMappingException 
	 */
	@PostMapping(value = "tdsmismatches/all/{mismatchcategory}/{year}/{month}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<InvoiceLineItem>>> listOfMisMatchesBasedOnMismatchCategory(
			@RequestHeader(value = "TAN-NUMBER") String tan, @PathVariable String mismatchcategory,
			@PathVariable int year, @PathVariable int month, @RequestBody MismatchesFiltersDTO filters)
			throws TanNotFoundException, JsonMappingException, JsonProcessingException {
		if (tan == null && mismatchcategory == null) {
			throw new CustomException("No Tan/Mismatch Category Found to proceed", HttpStatus.BAD_REQUEST);
		}

		CommonDTO<InvoiceLineItem> listMismatchesAll = invoiceLineItemService
				.getInvoiceMismatchBasedOnMismatchCategory(mismatchcategory, tan, year, month, filters);

		ApiStatus<CommonDTO<InvoiceLineItem>> apiStatus = new ApiStatus<CommonDTO<InvoiceLineItem>>(HttpStatus.OK,
				"SUCCESS", "NO ALERT", listMismatchesAll);
		return new ResponseEntity<ApiStatus<CommonDTO<InvoiceLineItem>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This api returns invoice line item data based on UUID and Tan-number
	 * 
	 * @param tan
	 * @param id
	 * @return
	 */
	@PostMapping("/lineitem")
	public ResponseEntity<ApiStatus<Map<String, Object>>> getLineItemDataBasedOnId(
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestBody Map<String, String> lineItemData) {
		Integer year = Integer.valueOf(lineItemData.get("year"));
		Integer month = Integer.valueOf(lineItemData.get("month"));
		Integer id = Integer.parseInt(lineItemData.get("id"));
		String type = lineItemData.get("type");
		Long documentPostingDate = Long.valueOf(lineItemData.get("documentPostingDate"));
		Map<String, Object> invoiceMetaData = invoiceLineItemService.getLineItemData(tan, year, month, id, type,
				documentPostingDate);
		ApiStatus<Map<String, Object>> apiStatus = new ApiStatus<Map<String, Object>>(HttpStatus.OK, "SUCCESS",
				"NO ALERT", invoiceMetaData);
		return new ResponseEntity<ApiStatus<Map<String, Object>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This api returns invoice line item data based on Nature of remittance
	 * 
	 * @param tan
	 * @param natureOfRemittance
	 * @return
	 */
	@GetMapping("/invoiceMetaByNatureOfRemittance")
	public ResponseEntity<ApiStatus<Map<String, Object>>> getInvoiceMetaByNatureOfRemittance(
			@RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestParam("natureOfRemittance") String natureOfRemittance,
			@RequestParam("invoiceMetaNrId") Integer invoiceMetaNrId) {
		if (tan == null) {
			logger.error("Tan Not Found");
			throw new CustomException("Tan Not Found", HttpStatus.BAD_REQUEST);
		}
		Map<String, Object> invoiceMetaData = invoiceLineItemService
				.getInvoiceMetaByNatureOfRemittance(natureOfRemittance, invoiceMetaNrId);
		logger.info(String.format("Response is: %s", "responseId"));
		ApiStatus<Map<String, Object>> apiStatus = new ApiStatus<Map<String, Object>>(HttpStatus.OK, "SUCCESS",
				"Invoice Line Item Data Based On Nature of remittance", invoiceMetaData);
		return new ResponseEntity<ApiStatus<Map<String, Object>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This api returns update invoice line item data based on UUID and Tan-number
	 * 
	 * @param tan
	 * @param id
	 * @param invoiceLineItemDTO
	 * @return
	 */
	@PutMapping("/invoicelineitemupdate")
	public ResponseEntity<ApiStatus<InvoiceLineItemResponseDTO>> updateDataBasedOnId(
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestBody InvoiceLineItemDTO invoiceLineItemDTO,
			@RequestHeader(value = "USER_NAME") String userName) {
		if (tan == null && invoiceLineItemDTO == null) {
			throw new CustomException("Tan and InvoiceLineItem is not present", HttpStatus.BAD_REQUEST);
		}
		InvoiceLineItem response = invoiceLineItemService.updateInvoiceLineItemById(tan, invoiceLineItemDTO, userName);
		InvoiceLineItemResponseDTO invoiceLineItem = new InvoiceLineItemResponseDTO();
		BeanUtils.copyProperties(response, invoiceLineItem);
		ApiStatus<InvoiceLineItemResponseDTO> apiStatus = new ApiStatus<InvoiceLineItemResponseDTO>(HttpStatus.OK,
				"SUCCESS", "Invoice Line Item Data is Update Based On Id", invoiceLineItem);
		return new ResponseEntity<ApiStatus<InvoiceLineItemResponseDTO>>(apiStatus, HttpStatus.OK);
	}


	/**
	 * Author : Rahul Varma For Invoice PDF Processing of reading CSV file
	 * 
	 * @param tan
	 * @param tenantId
	 * @return
	 * @throws IOException
	 */
	@GetMapping("/getcsvdata/pdffile")
	public ResponseEntity<ApiStatus<List<CsvFileDTO>>> getDataFromCsv(@RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestParam(value = "batchId") Integer batchId)
			throws IOException {
		List<CsvFileDTO> listOfCsvFileData = invoiceLineItemService.getCsvDataBasedOnFile(tenantId, tan, batchId);
		ApiStatus<List<CsvFileDTO>> apiStatus = new ApiStatus<List<CsvFileDTO>>(HttpStatus.OK, "SUCCESS",
				"Getting Csv data based on csv file", listOfCsvFileData);
		return new ResponseEntity<ApiStatus<List<CsvFileDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for getting Invoice By year, tan and challan month
	 * 
	 * @param tan
	 * @param id
	 * @param year
	 * @param month
	 * @return
	 */
	@GetMapping("/invoice/{assessmentYear}/{challanMonth}/{challanPaid}")
	public ResponseEntity<ApiStatus<List<InvoiceLineItemDTO>>> getInvoiceNonLineItem(
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader(value = "TAN-NUMBER") String tan,
			@PathVariable("assessmentYear") int assessmentYear, @PathVariable("challanMonth") int challanMonth,
			@PathVariable("challanPaid") boolean challanPaid) {
		logger.info("Tenant Id : " + tenantId);
		if (tan == null && assessmentYear > 0 && challanMonth > 0) {
			throw new CustomException("Required parameters are not present", HttpStatus.BAD_REQUEST);
		}
		List<InvoiceLineItemDTO> invoices = invoiceLineItemService
				.getInvoiceLineItemByAssessmentYearChallanMonthDeductorTan(assessmentYear, challanMonth,
						Arrays.asList(tan), challanPaid, false, Pagination.UNPAGED);
		logger.info(String.format("Response size is: %s", invoices.size()));
		logger.info("LIst of response : {}", invoices);
		ApiStatus<List<InvoiceLineItemDTO>> apiStatus = new ApiStatus<List<InvoiceLineItemDTO>>(HttpStatus.OK,
				"SUCCESS", "Getting invoice line items by assessment year, tan and challan month", invoices);
		return new ResponseEntity<ApiStatus<List<InvoiceLineItemDTO>>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping("/invoice/nonResident/{assessmentYear}/{challanMonth}/{challanPaid}")
	public ResponseEntity<ApiStatus<List<InvoiceLineItemDTO>>> getInvoiceNonResidentLineItem(
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader(value = "TAN-NUMBER") String tan,
			@PathVariable("assessmentYear") int assessmentYear, @PathVariable("challanMonth") int challanMonth,
			@PathVariable("challanPaid") boolean challanPaid) {
		logger.info("Tenant Id : " + tenantId);
		if (tan == null && assessmentYear > 0 && challanMonth > 0) {
			throw new CustomException("Required parameters are not present", HttpStatus.BAD_REQUEST);
		}
		List<InvoiceLineItemDTO> invoices = invoiceLineItemService
				.getInvoiceLineItemByAssessmentYearChallanMonthDeductorTan(assessmentYear, challanMonth,
						Arrays.asList(tan), challanPaid, true, Pagination.UNPAGED);
		logger.info(String.format("Response size is: %s", invoices.size()));
		ApiStatus<List<InvoiceLineItemDTO>> apiStatus = new ApiStatus<List<InvoiceLineItemDTO>>(HttpStatus.OK,
				"SUCCESS", "Getting invoice line items by assessment year, tan and challan month", invoices);
		return new ResponseEntity<ApiStatus<List<InvoiceLineItemDTO>>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping("/convert/rate/{amount}/{currencyType}")
	public ResponseEntity<ApiStatus<BigDecimal>> calculateAmonutByRate(@RequestHeader(value = "TAN-NUMBER") String tan,
			@PathVariable("amount") BigDecimal amount, @PathVariable("currencyType") String currencyType) {
		if (tan == null && amount.doubleValue() > 0 && currencyType == null) {
			throw new CustomException("Required parameters are not present", HttpStatus.BAD_REQUEST);
		}
		BigDecimal finalCaluclatedAmount = invoiceLineItemService.getCurrencyRate(amount, currencyType);
		logger.info(String.format("Final Caluclated Amount is: %s", finalCaluclatedAmount));
		ApiStatus<BigDecimal> apiStatus = new ApiStatus<BigDecimal>(HttpStatus.OK, "SUCCESS",
				"Final Caluclated Amount is", finalCaluclatedAmount.setScale(2, RoundingMode.UP));
		return new ResponseEntity<ApiStatus<BigDecimal>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping("/csv/to/list/{batchId}/{assesmentYear}")
	public ResponseEntity<ApiStatus<InvoicePdfList>> readCsvForInvoicePdfProcess(
			@PathVariable("batchId") String batchId, @PathVariable("assesmentYear") Integer assesmentYear,
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader(value = "TAN-NUMBER") String tan)
			throws Exception {

		BatchUpload batchUpload = batchUploadService.get(assesmentYear, tan, UploadTypes.INVOICE_PDF.name(),
				Integer.valueOf(batchId));

		File file = blobStorageService.getFileFromBlobFileName(tenantId, batchUpload.getSuccessFileUrl(),
				batchUpload.getSourceFilePath());

		InvoicePdfList invoices = invoiceLineItemService.readCsvForInvoicePdfProcess(file, batchUpload);

		ApiStatus<InvoicePdfList> apiStatus = new ApiStatus<InvoicePdfList>(HttpStatus.OK, "SUCCESS",
				"List of invoice data", invoices);
		return new ResponseEntity<ApiStatus<InvoicePdfList>>(apiStatus, HttpStatus.OK);
	}

	@PostMapping("/invoice/pdf/save/{batchId}/{batchUploadGroupId}")
	public ResponseEntity<ApiStatus<List<InvoicePDFDTO>>> invoicePdfSave(@RequestBody String invoicePdfSaveGroupDTOs,
			@PathVariable Integer batchId, @RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestHeader(value = "USER_NAME") String userName,
			@RequestHeader(value = "DEDUCTOR-PAN", required = true) String deductorPan,
			@RequestHeader("Authorization") String token, @PathVariable Integer batchUploadGroupId) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		List<InvoicePDFDTO> participantJsonList = objectMapper.readValue(invoicePdfSaveGroupDTOs,
				new TypeReference<List<InvoicePDFDTO>>() {
				});

		List<InvoicePDFDTO> invoices = invoiceLineItemService.invoicePdfSave(participantJsonList, userName, tan,
				batchId, batchUploadGroupId, deductorPan, token);

		ApiStatus<List<InvoicePDFDTO>> apiStatus = new ApiStatus<List<InvoicePDFDTO>>(HttpStatus.OK, "SUCCESS",
				"List of invoice data", invoices);
		return new ResponseEntity<ApiStatus<List<InvoicePDFDTO>>>(apiStatus, HttpStatus.OK);
	}

	/*
	 * @PostMapping("/cr") public ResponseEntity<ApiStatus<List<InvoiceLineItem>>>
	 * invoiceCrDr(@RequestHeader(value = "TAN-NUMBER") String tan,
	 * 
	 * @RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader(value =
	 * "USER_NAME") String userName,
	 * 
	 * @RequestBody List<InvoiceCRDTO> invoiceCRDTOs) throws Exception {
	 * 
	 * List<InvoiceLineItem> invoices = invoiceLineItemService.invoiceCrDr(tan,
	 * tenantId, userName, invoiceCRDTOs);
	 * 
	 * ApiStatus<List<InvoiceLineItem>> apiStatus = new
	 * ApiStatus<List<InvoiceLineItem>>(HttpStatus.OK, "SUCCESS",
	 * "List of invoice data", invoices); return new
	 * ResponseEntity<ApiStatus<List<InvoiceLineItem>>>(apiStatus, HttpStatus.OK); }
	 */

	@PostMapping(value = "/document/{type}/{year}/{month}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<InvoiceLineItemResponseDTO>>> getInvoiceByType(
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader(value = "X-TENANT-ID") String tenantId,
			@PathVariable String type, @RequestBody Pagination pagination, @PathVariable int year,
			@PathVariable int month) {

		CommonDTO<InvoiceLineItemResponseDTO> invoices = invoiceLineItemService.getInvoiceByType(tan, type, pagination,
				year, month);

		ApiStatus<CommonDTO<InvoiceLineItemResponseDTO>> apiStatus = new ApiStatus<CommonDTO<InvoiceLineItemResponseDTO>>(
				HttpStatus.OK, "SUCCESS", "NO ALERT", invoices);
		return new ResponseEntity<ApiStatus<CommonDTO<InvoiceLineItemResponseDTO>>>(apiStatus, HttpStatus.OK);
	}
	
	/**
	 * 
	 * @param tan
	 * @param tenantId
	 * @param pagination
	 * @param year
	 * @param month
	 * @return
	 */
	@PostMapping(value = "/cr/{year}/{month}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<InvoiceLineItemResponseDTO>>> getCRInvoices(
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestBody Pagination pagination, @PathVariable int year,
			@PathVariable int month) {

		CommonDTO<InvoiceLineItemResponseDTO> invoices = invoiceLineItemService.getCRInvoices(tan, "CR", pagination,
				year, month);

		ApiStatus<CommonDTO<InvoiceLineItemResponseDTO>> apiStatus = new ApiStatus<CommonDTO<InvoiceLineItemResponseDTO>>(
				HttpStatus.OK, "SUCCESS", "NO ALERT", invoices);
		return new ResponseEntity<ApiStatus<CommonDTO<InvoiceLineItemResponseDTO>>>(apiStatus, HttpStatus.OK);
	}
	
	/**
	 * 
	 * @param tan
	 * @param tenantId
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/pdf/transaction/status")
	public ResponseEntity<ApiStatus<String>> getPdfTransactionStatus(
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestParam(value = "assessmentYear", required = true) Integer assessmentYear,
			@RequestParam(value = "assessmentMonth", required = true) Integer assessmentMonth) throws Exception {
		String startDate = CommonUtil.getQueryDate(CommonUtil.getMonthStartDate(assessmentYear, assessmentMonth - 1));
		String endDate = CommonUtil.getQueryDate(CommonUtil.getMonthEndDate(assessmentYear, assessmentMonth - 1));
		String result = invoiceLineItemService.getPdfActivityTrackerResult(assessmentYear,
				UploadTypes.INVOICE_PDF.name(), tan, startDate, endDate);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", "List of invoice line item", result);
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param tan
	 * @param tenantId
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/tds/calculation/status")
	public ResponseEntity<ApiStatus<String>> getTdsCalculationStatus(
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestParam(value = "assessmentYear", required = true) Integer assessmentYear,
			@RequestParam(value = "assessmentMonth", required = true) Integer assessmentMonth) throws Exception {
		String startDate = CommonUtil.getQueryDate(CommonUtil.getMonthStartDate(assessmentYear, assessmentMonth - 1));
		String endDate = CommonUtil.getQueryDate(CommonUtil.getMonthEndDate(assessmentYear, assessmentMonth - 1));
		String result = invoiceLineItemService.getTdsCalculationForInvoice(assessmentYear, tan, startDate, endDate);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", "List of invoice line item", result);
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param tan
	 * @param year
	 * @param tenantId
	 * @return
	 * @throws Exception
	 */
	/*
	 * @GetMapping("/allinvoice") public
	 * ResponseEntity<ApiStatus<List<InvoiceLineItem>>> getAllInvoiceLineItemData(
	 * 
	 * @RequestHeader(value = "TAN-NUMBER") String tan,
	 * 
	 * @RequestParam(value = "assessmentYear") Integer assessmentYear,
	 * 
	 * @RequestHeader(value = "X-TENANT-ID") String tenantId) throws Exception {
	 * List<InvoiceLineItem> invoices = invoiceLineItemService
	 * .getAllInvoiceLineItemData(CommonUtil.getAssessmentYear(assessmentYear),
	 * tan); ApiStatus<List<InvoiceLineItem>> apiStatus = new
	 * ApiStatus<List<InvoiceLineItem>>(HttpStatus.OK, "SUCCESS",
	 * "List of invoice line item", invoices); return new
	 * ResponseEntity<ApiStatus<List<InvoiceLineItem>>>(apiStatus, HttpStatus.OK); }
	 */

	@PostMapping(value = "/invoice/originaldocument", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<InvoiceLineItem>>> getInvoicesByCrData(@RequestBody CRDTO crData) {
		CommonDTO<InvoiceLineItem> invoices = invoiceLineItemService.getInvoicesByCrData(crData);
		ApiStatus<CommonDTO<InvoiceLineItem>> apiStatus = new ApiStatus<CommonDTO<InvoiceLineItem>>(HttpStatus.OK,
				"SUCCESS", "NO ALERT", invoices);
		return new ResponseEntity<ApiStatus<CommonDTO<InvoiceLineItem>>>(apiStatus, HttpStatus.OK);
	}

	@PostMapping(value = "/invoice/originaldocument/adjust", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<InvoiceLineItemCRDTO>> adjustments(@RequestBody CRDTO crData,
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader(value = "DEDUCTOR-PAN") String pan,
			@RequestHeader("Authorization") String token) {

		InvoiceLineItemCRDTO invoices = invoiceLineItemService.adjustments(crData, token, pan, tenantId);

		ApiStatus<InvoiceLineItemCRDTO> apiStatus = new ApiStatus<InvoiceLineItemCRDTO>(HttpStatus.OK, "SUCCESS",
				"CR Adjustments done successfully.", invoices);
		return new ResponseEntity<ApiStatus<InvoiceLineItemCRDTO>>(apiStatus, HttpStatus.OK);
	}

	@PostMapping(value = "invoice/originaldocument/reject", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<Integer>> updateInvoices(@RequestBody InvoiceDTO invoiceData) {
		Integer count = invoiceLineItemService.updateInvoices(invoiceData);
		ApiStatus<Integer> apiStatus = new ApiStatus<Integer>(HttpStatus.OK, "SUCCESS", "NO ALERT", count);
		return new ResponseEntity<ApiStatus<Integer>>(apiStatus, HttpStatus.OK);
	}

	@PostMapping(value = "/invoice/currentmonthdata/{year}/{month}/{deducteeName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<InvoiceLineItem>>> getInvoicesByCurrentMonth(
			@PathVariable(value = "year") int assessmentYear, @PathVariable(value = "month") int month,
			@PathVariable(value = "deducteeName") String deducteeName, @RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestBody Pagination pagination) {
		CommonDTO<InvoiceLineItem> invoices = invoiceLineItemService.getInvoicesByCurrentMonth(assessmentYear, month,
				deducteeName, tan, pagination, "");
		ApiStatus<CommonDTO<InvoiceLineItem>> apiStatus = new ApiStatus<CommonDTO<InvoiceLineItem>>(HttpStatus.OK,
				"SUCCESS", "NO ALERT", invoices);
		return new ResponseEntity<ApiStatus<CommonDTO<InvoiceLineItem>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param month
	 * @param deducteeName
	 * @param tan
	 * @param section
	 * @param pagination
	 * @return
	 */
	@PostMapping(value = "/invoice/currentmonthdata/{year}/{month}/{deducteeName}/{section}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<InvoiceLineItem>>> getInvoicesByCurrentMonth(
			@PathVariable(value = "year") int assessmentYear, @PathVariable(value = "month") int month,
			@PathVariable(value = "deducteeName") String deducteeName, @RequestHeader(value = "TAN-NUMBER") String tan,
			@PathVariable(value = "section") String section, @RequestBody Pagination pagination) {
		CommonDTO<InvoiceLineItem> invoices = invoiceLineItemService.getInvoicesByCurrentMonth(assessmentYear, month,
				deducteeName, tan, pagination, section);
		ApiStatus<CommonDTO<InvoiceLineItem>> apiStatus = new ApiStatus<CommonDTO<InvoiceLineItem>>(HttpStatus.OK,
				"SUCCESS", "NO ALERT", invoices);
		return new ResponseEntity<ApiStatus<CommonDTO<InvoiceLineItem>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param invoicesAndCrData
	 * @param tenantId
	 * @param pan
	 * @param token
	 * @return
	 */
	@PostMapping(value = "/invoice/currentmonthadjustments", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<InvoiceLineItemCRDTO>> adjustmentsForCurrentMonth(
			@RequestBody InvoiceLineItemCRDTO invoicesAndCrData, @RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestHeader(value = "DEDUCTOR-PAN") String pan, @RequestHeader("Authorization") String token) {

		InvoiceLineItemCRDTO invoices = invoiceLineItemService.adjustmentsForCurrentMonth(invoicesAndCrData, token, pan,
				tenantId);

		ApiStatus<InvoiceLineItemCRDTO> apiStatus = new ApiStatus<InvoiceLineItemCRDTO>(HttpStatus.OK, "SUCCESS",
				"CR Adjustments done sucessfully.", invoices);
		return new ResponseEntity<ApiStatus<InvoiceLineItemCRDTO>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * fiegn call to get invoice based on year and deductor master tan
	 * 
	 * @param assessmentYear
	 * @param deductorMasterTan
	 * @return
	 */
	@GetMapping(value = "/findBy/year/deductorTan")
	public ResponseEntity<ApiStatus<List<InvoiceLineItem>>> findByYearAndDeductorTan(
			@RequestParam("year") Integer assessmentYear, @RequestParam("tan") String deductorMasterTan,
			@RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestParam("month") Integer assessmentMonth) {
		logger.info("feign call executing to get the invoice line item data   {}");
		List<InvoiceLineItem> listInvoice = invoiceLineItemDAO.findAll(deductorMasterTan, assessmentYear, assessmentMonth);
		ApiStatus<List<InvoiceLineItem>> apiStatus = new ApiStatus<List<InvoiceLineItem>>(HttpStatus.OK, "SUCCESS",
				"Invoice retrieved sucessfully.", listInvoice);
		logger.info("feign call execution succeded to get the invoice line item data   {}");
		return new ResponseEntity<ApiStatus<List<InvoiceLineItem>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param deductorTan
	 * @param deducteeType
	 * @param type
	 * @param year
	 * @param month
	 * @param isMismatch
	 * @return
	 */
	@GetMapping(value = "deductees/{deducteetype}/{type}/{year}/{month}/{ismismatch}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<Set<String>>> getDeductees(@RequestHeader("TAN-NUMBER") String deductorTan,
			@PathVariable(value = "deducteetype", required = true) String deducteeType,
			@PathVariable(value = "type", required = true) String type,
			@PathVariable(value = "year", required = true) int year,
			@PathVariable(value = "month", required = true) int month,
			@PathVariable(value = "ismismatch", required = true) boolean isMismatch) {
		ApiStatus<Set<String>> apiStatus = new ApiStatus<Set<String>>(HttpStatus.OK, "SUCCESS",
				"LIST OF " + deducteeType.toUpperCase() + " DEDUCTEES",
				invoiceLineItemService.getDeductees(deductorTan, deducteeType, type, year, month, isMismatch));
		return new ResponseEntity<ApiStatus<Set<String>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * feign call to retrieve invoice for tds-returns
	 * 
	 * @param year
	 * @param deductorTan
	 * @param challanPaid
	 * @param isForNonResidents
	 * @param bsrCode
	 * @param receiptSerailNo
	 * @param receiptDate
	 * @return
	 */
	@GetMapping(value = "invoice/yearTanBsrCode")
	public ResponseEntity<ApiStatus<List<InvoiceLineItem>>> invoiceByTanYearBSRCodeSerialNoAndDate(
			@RequestParam("assessmentYear") Integer year, @RequestParam("TAN") String deductorTan,
			@RequestParam("ChallanPaid") Boolean challanPaid,
			@RequestParam("ResidentIndicator") boolean isForNonResidents, @RequestParam("BSRCode") String bsrCode,
			@RequestParam("RecieptSerialNo") String receiptSerailNo, @RequestParam("RecieptDate") String receiptDate,
			@RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestParam("isForm16") Boolean isForm16) {
		List<InvoiceLineItem> listInvoice = invoiceLineItemDAO
				.getInvoiceLineItemsByReceiptSerialNoAndBSRCodeAndReceiptDate(year, deductorTan, challanPaid,
						isForNonResidents, bsrCode, receiptSerailNo, receiptDate, isForm16);
		// List<InvoiceLineItemResponseDTO> listResponse =
		// invoiceLineItemService.copyToResponse(listInvoice);
		ApiStatus<List<InvoiceLineItem>> apiStatus = new ApiStatus<List<InvoiceLineItem>>(HttpStatus.OK, "SUCCESS",
				"Invoice retrieved sucessfully.", listInvoice);
		return new ResponseEntity<ApiStatus<List<InvoiceLineItem>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * fiegn call to get invoice for tds-returns
	 * 
	 * @param assessmentYear
	 * @param challanMonths
	 * @param deductorTan
	 * @param challanPaid
	 * @param isForNonResidents
	 * @return
	 */
	@GetMapping(value = "invpoice/year/Tan/challanMonths")
	public ResponseEntity<ApiStatus<List<InvoiceLineItemResponseDTO>>> getInvoiceLineItemsByAssessmentYearChallanMonthDeductorTan(
			@RequestParam("assessmentYear") Integer assessmentYear,
			@RequestParam("challanMonths") Integer[] challanMonths,
			@RequestParam("deductorTan") List<String> deductorTan, @RequestParam("challanPaid") boolean challanPaid,
			@RequestParam("isForNonResidents") boolean isForNonResidents) {

		List<Integer> listChallanMonth = Arrays.asList(challanMonths);
		List<InvoiceLineItem> list = invoiceLineItemDAO.getInvoiceLineItemsByAssessmentYearChallanMonthDeductorTan(
				assessmentYear, listChallanMonth, deductorTan, challanPaid, isForNonResidents, Pagination.UNPAGED);
		List<InvoiceLineItemResponseDTO> listResponse = invoiceLineItemService.copyToResponse(list);
		ApiStatus<List<InvoiceLineItemResponseDTO>> apiStatus = new ApiStatus<List<InvoiceLineItemResponseDTO>>(
				HttpStatus.OK, "SUCCESS", "Invoice retrieved sucessfully.", listResponse);
		return new ResponseEntity<ApiStatus<List<InvoiceLineItemResponseDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for NR Transaction excel upload.
	 * 
	 * @param file
	 * @param userName
	 * @param tenantId
	 * @param assessmentYear
	 * @param tan
	 * @param pan
	 * @param token
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/invoice/nr/import")
	public ResponseEntity<ApiStatus<BatchUpload>> getStatusOfRemediationReport(
			@RequestParam("files") MultipartFile file, @RequestHeader(value = "USER_NAME") String userName,
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestParam("year") int assessmentYear,
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader(value = "DEDUCTOR-PAN") String pan,
			@RequestHeader(value = "Authorization", required = false) String token, @RequestParam("type") String type)
			throws Exception {
		if (file == null) {
			if (logger.isErrorEnabled()) {
				logger.error("File Not Found");
			}
			throw new CustomException("file is not imported", HttpStatus.BAD_REQUEST);
		}
		String contentType = new Tika().detect(file.getInputStream(), file.getOriginalFilename());
		if (AllowedMimeTypes.XLSX.getMimeType().equals(contentType)
				|| AllowedMimeTypes.OOXML.getMimeType().equals(contentType)) {
			int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
			BatchUpload batchUpload = invoiceLineItemService.saveInvoiceData(file, tan, assessmentYear, month, userName,
					tenantId, pan, token, type);
			ApiStatus<BatchUpload> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS",
					"Invoice file uploaded successfully", batchUpload);
			return new ResponseEntity<ApiStatus<BatchUpload>>(apiStatus, HttpStatus.OK);
		} else {
			logger.warn("Unsupported file upload attempt by {}", userName);
			throw new CustomException("Invalid file contents, only xlsx files are allowed", HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * to genearte the interest computation report
	 * 
	 * @param tan
	 * @param userName
	 * @param tenantId
	 * @param assessmentYear
	 * @param month
	 * @param deductorPan
	 * @return
	 * @throws InvalidKeyException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws StorageException
	 */
	@PostMapping("/invoice/export/interestReport")
	public ResponseEntity<ApiStatus<String>> exportInterestComputationReport(
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestHeader(value = "USER_NAME") String userName,
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestParam("month") int month,
			@RequestParam(value = "year", required = true) Integer assessmentYear,
			@RequestHeader(value = "DEDUCTOR-PAN") String deductorPan)
			throws InvalidKeyException, FileNotFoundException, IOException, URISyntaxException, StorageException {
		MultiTenantContext.setTenantId(tenantId);
		invoiceLineItemService.asyncExportInterestComputationReport(tan, assessmentYear, userName, deductorPan, month,
				tenantId);
		String message = "Invoice Interest Computation report requested successfully and will be available shortly";
		logger.info("{}", message);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", message, message);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);

	}

	@PostMapping(value = "invoice/InterestRecords/{year}/{month}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<InvoiceLineItem>>> listOfInvoiceInterestRecords(
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader(value = "TAN-NUMBER") String tan,
			@PathVariable int year, @PathVariable int month, @RequestBody MismatchesFiltersDTO filters)
			throws TanNotFoundException {
		MultiTenantContext.setTenantId(tenantId);

		CommonDTO<InvoiceLineItem> listMismatchesAll = invoiceLineItemService.getInvoiceInterestRecords(tan, year,
				month, filters);

		ApiStatus<CommonDTO<InvoiceLineItem>> apiStatus = new ApiStatus<CommonDTO<InvoiceLineItem>>(HttpStatus.OK,
				"SUCCESS", "NO ALERT", listMismatchesAll);
		return new ResponseEntity<ApiStatus<CommonDTO<InvoiceLineItem>>>(apiStatus, HttpStatus.OK);
	}

	@PutMapping(value = "/invoice/interest/action", consumes = MediaType.APPLICATION_JSON_VALUE)
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
		invoiceLineItemService.updateInterestrecords(tan, invoiceMismatchUpdateDTO, pan);

		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS", "LIST OF MISMATCHES UPDATED",
				"SUCCESS");
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}
	
	@PostMapping(value = "/nr/stagging", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<BatchUpload>> nrStaggingData(
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestHeader(value = "DEDUCTOR-PAN") String pan, @RequestBody Map<String, Object> invoiceObj)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		Integer year = (Integer) invoiceObj.get("year");
		Integer month = (Integer) invoiceObj.get("month");
		String type = (String) invoiceObj.get("type");
		year = CommonUtil.getAssessmentYear(year);
		month = CommonUtil.getAssessmentMonth(month);
		BatchUpload batchUpload = invoiceLineItemService.generateNrStaggingFile(tan, pan, year, month, tenantId, type);

		ApiStatus<BatchUpload> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS",
				"Generating invoice stagging file.", batchUpload);
		return new ResponseEntity<ApiStatus<BatchUpload>>(apiStatus, HttpStatus.OK);
	}
}
