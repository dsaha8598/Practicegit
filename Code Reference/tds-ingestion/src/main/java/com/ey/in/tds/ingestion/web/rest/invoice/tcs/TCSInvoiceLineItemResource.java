package com.ey.in.tds.ingestion.web.rest.invoice.tcs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
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

import com.ey.in.tcs.common.domain.TCSLedger;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.tcs.TCSBatchUpload;
import com.ey.in.tds.common.domain.tcs.TCSInvoiceLineItem;
import com.ey.in.tds.common.dto.CommonDTO;
import com.ey.in.tds.common.dto.InvoiceLineItemCRDTO;
import com.ey.in.tds.common.dto.csvfile.CsvFileDTO;
import com.ey.in.tds.common.dto.invoice.InvoiceMismatchByBatchIdDTO;
import com.ey.in.tds.common.dto.invoice.MismatchesFiltersDTO;
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
import com.ey.in.tds.ingestion.service.batchupload.TCSBatchUploadService;
import com.ey.in.tds.ingestion.tcs.dao.TCSInvoiceLineItemDAO;
import com.ey.in.tds.ingestion.tcs.service.TCSInvoiceLineItemService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.StorageException;

import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvReader;

/**
 * 
 * @author scriptbees
 *
 */
@RestController
@RequestMapping("/api/ingestion/tcs")
public class TCSInvoiceLineItemResource {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TCSInvoiceLineItemService tcsInvoiceLineItemService;

	@Autowired
	private TCSBatchUploadService tcsBatchUploadService;

	@Autowired
	private BlobStorageService blobStorageService;

	@Autowired
	private TCSInvoiceLineItemDAO tcsInvoiceLineItemDAO;

	/**
	 * This api will insert record in invoice line item table
	 */
	@PostMapping("/create")
	public TCSInvoiceLineItem createRecord(@RequestBody TCSInvoiceLineItem tcsInvoiceLineItem,
			@RequestHeader(value = "X-TENANT-ID") String tenantId) {
		logger.info("Creating invoice line item---: {}", tcsInvoiceLineItem);
		return tcsInvoiceLineItemService.create(tcsInvoiceLineItem);
	}

	/**
	 * Get the List oF records for invoice based on mismatch and Batch Id
	 * 
	 * @param tcsBatchId
	 * @return
	 * @throws TanNotFoundException
	 */
	@GetMapping("/tdsmismatches/{tcsBatchId}")
	public ResponseEntity<ApiStatus<List<InvoiceMismatchByBatchIdDTO>>> listOfMisMatchesBasedonBatchUploadIdType(
			@PathVariable Integer tcsBatchId, @RequestHeader(value = "TAN-NUMBER") String collectorTan,
			@RequestHeader(value = "X-TENANT-ID") String tenantId) throws TanNotFoundException {
		if (tcsBatchId != null && collectorTan != null) {
			List<InvoiceMismatchByBatchIdDTO> listMismatchesByBatchId = tcsInvoiceLineItemService
					.getInvoiceMismatchsByBatchUploadIDType(tcsBatchId, collectorTan);
			logger.info("List of tcs mismatches based on Tcs Batch Upload pdf done");
			ApiStatus<List<InvoiceMismatchByBatchIdDTO>> apiStatus = new ApiStatus<List<InvoiceMismatchByBatchIdDTO>>(
					HttpStatus.OK, "SUCCESS", "LIST OF TCS MISMATCHES FOR PARTICULAR BATCH", listMismatchesByBatchId);
			return new ResponseEntity<ApiStatus<List<InvoiceMismatchByBatchIdDTO>>>(apiStatus, HttpStatus.OK);
		} else {
			logger.error("No batch Id/tan, Cannot process");
			throw new CustomException("No batch Id/tan, Cannot process", HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Get the List oF records for invoice based on mismatch and Batch Id
	 * 
	 * @param tcsBatchId
	 * @return
	 * @throws TanNotFoundException
	 */
	@GetMapping("/tcsmismatches/{tcsBatchId}")
	public ResponseEntity<ApiStatus<List<InvoiceMismatchByBatchIdDTO>>> listOfMisMatchesBasedonBatchUploadIdTypeForTCS(
			@PathVariable Integer tcsBatchId, @RequestHeader(value = "TAN-NUMBER") String collectorTan,
			@RequestHeader(value = "X-TENANT-ID") String tenantId) throws TanNotFoundException {
		if (tcsBatchId != null && collectorTan != null) {
			List<InvoiceMismatchByBatchIdDTO> listMismatchesByBatchId = tcsInvoiceLineItemService
					.getInvoiceMismatchsByBatchUploadIDType(tcsBatchId, collectorTan);
			logger.info("List of tcs mismatches based on Tcs Batch Upload pdf done");
			ApiStatus<List<InvoiceMismatchByBatchIdDTO>> apiStatus = new ApiStatus<List<InvoiceMismatchByBatchIdDTO>>(
					HttpStatus.OK, "SUCCESS", "LIST OF TCS MISMATCHES FOR PARTICULAR BATCH", listMismatchesByBatchId);
			return new ResponseEntity<ApiStatus<List<InvoiceMismatchByBatchIdDTO>>>(apiStatus, HttpStatus.OK);
		} else {
			logger.error("No batch Id/tan, Cannot process");
			throw new CustomException("No batch Id/tan, Cannot process", HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * List of tcs mismatches for excel (for Excel)
	 * 
	 * @return
	 * @throws TanNotFoundException
	 */
	@GetMapping("/tcsmismatches/all/{year}/{month}")
	public ResponseEntity<ApiStatus<List<InvoiceMismatchByBatchIdDTO>>> listOfMisMatchesForExcelBasedonBatchUploadId(
			@RequestHeader(value = "TAN-NUMBER") String collectorTan, @PathVariable int year, @PathVariable int month,
			@RequestHeader(value = "X-TENANT-ID") String tenantId) throws TanNotFoundException {
		if (collectorTan != null) {
			List<InvoiceMismatchByBatchIdDTO> listMismatchesAll = tcsInvoiceLineItemService
					.getInvoiceMismatchAll(collectorTan, year, month);
			ApiStatus<List<InvoiceMismatchByBatchIdDTO>> apiStatus = new ApiStatus<List<InvoiceMismatchByBatchIdDTO>>(
					HttpStatus.OK, "SUCCESS", "LIST OF TCS MISMATCHES", listMismatchesAll);
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
	@PostMapping(value = "/tdsmismatches/{batchId}/{mismatchcategory}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<TCSInvoiceLineItem>>> listOfMisMatchesBasedonBatchUploadIdCategory(
			@RequestHeader(value = "TAN-NUMBER") String collectorTan, @PathVariable Integer batchId,
			@PathVariable String mismatchcategory, @RequestBody Pagination pagination,
			@RequestHeader(value = "X-TENANT-ID") String tenantId) throws TanNotFoundException {
		MultiTenantContext.setTenantId(tenantId);
		if (collectorTan != null && batchId != null && mismatchcategory != null) {
			CommonDTO<TCSInvoiceLineItem> listMismatchesByBatchIdMiscmatchCategory = tcsInvoiceLineItemService
					.getInvoiceMismatchByBatchUploadIDMismatchCategory(batchId, mismatchcategory, collectorTan,
							pagination);
			ApiStatus<CommonDTO<TCSInvoiceLineItem>> apiStatus = new ApiStatus<CommonDTO<TCSInvoiceLineItem>>(
					HttpStatus.OK, "SUCCESS", "NO ALERT", listMismatchesByBatchIdMiscmatchCategory);
			return new ResponseEntity<ApiStatus<CommonDTO<TCSInvoiceLineItem>>>(apiStatus, HttpStatus.OK);
		} else {
			logger.error("Tan Not Found");
			throw new CustomException("Tan Not Found", HttpStatus.BAD_REQUEST);
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
	@PostMapping(value = "/tcsmismatches/{batchId}/{mismatchcategory}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<TCSInvoiceLineItem>>> listOfMisMatchesBasedonBatchUploadIdCategoryForTCS(
			@RequestHeader(value = "TAN-NUMBER") String collectorTan, @PathVariable Integer batchId,
			@PathVariable String mismatchcategory, @RequestBody Pagination pagination,
			@RequestHeader(value = "X-TENANT-ID") String tenantId) throws TanNotFoundException {
		// MultiTenantContext.setTenantId(tenantId);
		if (collectorTan != null && batchId != null && mismatchcategory != null) {
			CommonDTO<TCSInvoiceLineItem> listMismatchesByBatchIdMiscmatchCategory = tcsInvoiceLineItemService
					.getInvoiceMismatchByBatchUploadIDMismatchCategory(batchId, mismatchcategory, collectorTan,
							pagination);
			ApiStatus<CommonDTO<TCSInvoiceLineItem>> apiStatus = new ApiStatus<CommonDTO<TCSInvoiceLineItem>>(
					HttpStatus.OK, "SUCCESS", "NO ALERT", listMismatchesByBatchIdMiscmatchCategory);
			return new ResponseEntity<ApiStatus<CommonDTO<TCSInvoiceLineItem>>>(apiStatus, HttpStatus.OK);
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
	@PutMapping(value = "/tcsmismatches/{mismatchcategory}/action", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<UpdateOnScreenDTO>> updateMismatchByAction(
			@RequestHeader(value = "TAN-NUMBER") String collectorTan,
			@PathVariable("mismatchcategory") String mismatchcategory,
			@RequestBody UpdateOnScreenDTO invoiceMismatchUpdateDTO, @RequestHeader("Authorization") String token,
			@RequestHeader(value = "DEDUCTOR-PAN") String collectorPan,
			@RequestHeader(value = "X-TENANT-ID") String tenantId) throws URISyntaxException {
		// Handling null pointer exception
		if (collectorTan == null && mismatchcategory == null) {
			throw new CustomException("Tan / Mismatch Category  Not Found", HttpStatus.BAD_REQUEST);
		}
		UpdateOnScreenDTO response = tcsInvoiceLineItemService.updateMismatchByAction(collectorTan,
				invoiceMismatchUpdateDTO, token, collectorPan, tenantId);
		ApiStatus<UpdateOnScreenDTO> apiStatus = new ApiStatus<UpdateOnScreenDTO>(HttpStatus.OK, "SUCCESS",
				"LIST OF TCS MISMATCHES UPDATED", response);
		return new ResponseEntity<ApiStatus<UpdateOnScreenDTO>>(apiStatus, HttpStatus.OK);
	}

	@PostMapping(value = "/invoices-lineitem/{restype}/{filetype}/{year}/{month}/{collecteeName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<TCSInvoiceLineItem>>> getResidentAndNonresident(
			@RequestHeader(value = "TAN-NUMBER") String collectorTan, @PathVariable String restype,
			@RequestHeader("X-TENANT-ID") String tenantId, @PathVariable int year, @PathVariable int month,
			@PathVariable String filetype, @PathVariable String collecteeName, @RequestBody Pagination pagination)
			throws RecordNotFoundException, TanNotFoundException {
		logger.info("X-TENANT-ID _______: {}", tenantId);
		CommonDTO<TCSInvoiceLineItem> response = null;
		if (collectorTan != null && restype != null && filetype != null) {
			response = tcsInvoiceLineItemService.getResidentAndNonresident(restype, filetype, collectorTan, year, month,
					pagination, collecteeName);
		} else {
			logger.error("Tan/ rest type / file type not found to proceed");
			throw new CustomException("Tan/ rest type / file type not found to proceed", HttpStatus.BAD_REQUEST);
		}
		logger.info("List of collectee responses done");
		ApiStatus<CommonDTO<TCSInvoiceLineItem>> apiStatus = new ApiStatus<CommonDTO<TCSInvoiceLineItem>>(HttpStatus.OK,
				"SUCCESS", "NO ALERT", response);
		return new ResponseEntity<ApiStatus<CommonDTO<TCSInvoiceLineItem>>>(apiStatus, HttpStatus.OK);

	}

	/**
	 * To get the mismatch records based on mismatch Category
	 * 
	 * @param mismatchcategory
	 * @return
	 * @throws TanNotFoundException
	 */
	@PostMapping(value = "/tcsmismatches/all/{mismatchcategory}/{year}/{month}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<TCSInvoiceLineItem>>> listOfMisMatchesBasedOnMismatchCategory(
			@RequestHeader(value = "TAN-NUMBER") String collectorTan, @PathVariable String mismatchcategory,
			@PathVariable int year, @PathVariable int month, @RequestBody MismatchesFiltersDTO filters,
			@RequestHeader(value = "X-TENANT-ID") String tenantId) throws TanNotFoundException {
		if (collectorTan == null && mismatchcategory == null) {
			throw new CustomException("No Tan/Mismatch Category Found to proceed", HttpStatus.BAD_REQUEST);
		}
		CommonDTO<TCSInvoiceLineItem> listMismatchesAll = tcsInvoiceLineItemService
				.getInvoiceMismatchBasedOnMismatchCategory(mismatchcategory, collectorTan, year, month, filters);
		ApiStatus<CommonDTO<TCSInvoiceLineItem>> apiStatus = new ApiStatus<CommonDTO<TCSInvoiceLineItem>>(HttpStatus.OK,
				"SUCCESS", "NO ALERT", listMismatchesAll);
		return new ResponseEntity<ApiStatus<CommonDTO<TCSInvoiceLineItem>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This api returns invoice line item data based on UUID and Tan-number
	 * 
	 * @param collectorTan
	 * @param id
	 * @return
	 */
	@PostMapping("/lineitem")
	public ResponseEntity<ApiStatus<Map<String, Object>>> getLineItemDataBasedOnId(
			@RequestHeader(value = "TAN-NUMBER") String collectorTan, @RequestBody Map<String, String> lineItemData,
			@RequestHeader(value = "X-TENANT-ID") String tenantId) {
		Integer year = Integer.valueOf(lineItemData.get("year"));
		Integer month = Integer.valueOf(lineItemData.get("month"));
		Integer id = Integer.parseInt(lineItemData.get("id"));
		String type = lineItemData.get("type");
		Long documentPostingDate = Long.valueOf(lineItemData.get("documentPostingDate"));
		Map<String, Object> invoiceMetaData = tcsInvoiceLineItemService.getLineItemData(collectorTan, year, month, id,
				type, documentPostingDate);
		ApiStatus<Map<String, Object>> apiStatus = new ApiStatus<Map<String, Object>>(HttpStatus.OK, "SUCCESS",
				"NO ALERT", invoiceMetaData);
		return new ResponseEntity<ApiStatus<Map<String, Object>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This api returns invoice line item data based on Nature of remittance
	 * 
	 * @param collectorTan
	 * @param natureOfRemittance
	 * @return
	 */

	/*
	 * @GetMapping("/invoiceMetaByNatureOfRemittance") public
	 * ResponseEntity<ApiStatus<Map<String, Object>>>
	 * getInvoiceMetaByNatureOfRemittance(
	 * 
	 * @RequestHeader(value = "TAN-NUMBER") String collectorTan,
	 * 
	 * @RequestParam("natureOfRemittance") String natureOfRemittance,
	 * 
	 * @RequestParam("invoiceMetaNrId") Integer invoiceMetaNrId,
	 * 
	 * @RequestHeader(value = "X-TENANT-ID") String tenantId) { if (collectorTan ==
	 * null) { logger.error("Tan Not Found"); throw new
	 * CustomException("Tan Not Found", HttpStatus.BAD_REQUEST); } Map<String,
	 * Object> invoiceMetaData = tcsInvoiceLineItemService
	 * .getInvoiceMetaByNatureOfRemittance(natureOfRemittance, invoiceMetaNrId);
	 * logger.info(String.format("Response is: %s", "responseId"));
	 * ApiStatus<Map<String, Object>> apiStatus = new ApiStatus<Map<String,
	 * Object>>(HttpStatus.OK, "SUCCESS",
	 * "TCS Invoice Line Item Data Based On Nature of remittance", invoiceMetaData);
	 * return new ResponseEntity<ApiStatus<Map<String, Object>>>(apiStatus,
	 * HttpStatus.OK); }
	 */

	/**
	 * This api returns update invoice line item data based on UUID and Tan-number
	 * 
	 * @param collectorTan
	 * @param id
	 * @param invoiceLineItemDTO
	 * @return
	 */
	@PutMapping("/invoicelineitemupdate")
	public ResponseEntity<ApiStatus<TCSInvoiceLineItem>> updateDataBasedOnId(
			@RequestHeader(value = "TAN-NUMBER") String collectorTan,
			@RequestBody TCSInvoiceLineItem invoiceLineItemDTO, @RequestHeader(value = "USER_NAME") String userName,
			@RequestHeader(value = "X-TENANT-ID") String tenantId) {
		if (collectorTan == null && invoiceLineItemDTO == null) {
			throw new CustomException("Tan and InvoiceLineItem is not present", HttpStatus.BAD_REQUEST);
		}
		TCSInvoiceLineItem response = tcsInvoiceLineItemService.updateInvoiceLineItemById(collectorTan,
				invoiceLineItemDTO, userName);
		TCSInvoiceLineItem invoiceLineItem = new TCSInvoiceLineItem();
		BeanUtils.copyProperties(response, invoiceLineItem);
		ApiStatus<TCSInvoiceLineItem> apiStatus = new ApiStatus<TCSInvoiceLineItem>(HttpStatus.OK, "SUCCESS",
				"TCS Invoice Line Item Data is Update Based On Id", invoiceLineItem);
		return new ResponseEntity<ApiStatus<TCSInvoiceLineItem>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for Create Invoice Meta Nr
	 * 
	 * @param collectorTan
	 * @param invoiceMetaNrDTO
	 * @param lineItemId
	 * @param year
	 * @param month
	 * @return
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws InvalidKeyException
	 */

	/*
	 * @PostMapping("/invoicemeta") public
	 * ResponseEntity<ApiStatus<InvoiceMetaNrResponseDTO>> createInvoiceMetaNr(
	 * 
	 * @RequestHeader(value = "TAN-NUMBER") String collectorTan,
	 * 
	 * @RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader(value =
	 * "USER_NAME") String userName,
	 * 
	 * @RequestParam("invoiceMetaNrDTO") String invoiceMetaData,
	 * 
	 * @RequestParam(value = "files", required = false) MultipartFile[] files,
	 * 
	 * @RequestHeader(value = "DEDUCTOR-PAN") String
	 * collectorPan, @RequestHeader("Authorization") String token) throws
	 * JsonParseException, JsonMappingException, IOException, InvalidKeyException,
	 * URISyntaxException, StorageException { InvoiceMetaNrDTO invoiceMetaNrDTO =
	 * new ObjectMapper().readValue(invoiceMetaData, InvoiceMetaNrDTO.class);
	 * InvoiceMetaNr invoice =
	 * tcsInvoiceLineItemService.createInvoiceMetaNr(collectorTan, invoiceMetaNrDTO,
	 * files, tenantId, userName, collectorPan, token); InvoiceMetaNrResponseDTO
	 * response = new InvoiceMetaNrResponseDTO(); BeanUtils.copyProperties(invoice,
	 * response); ApiStatus<InvoiceMetaNrResponseDTO> apiStatus = new
	 * ApiStatus<InvoiceMetaNrResponseDTO>(HttpStatus.OK, "SUCCESS",
	 * "Record Validated Successfully.", response); return new
	 * ResponseEntity<ApiStatus<InvoiceMetaNrResponseDTO>>(apiStatus,
	 * HttpStatus.OK);
	 * 
	 * }
	 */

	/**
	 * This API for getting Invoice Meta Nr By ID
	 * 
	 * @param collectorTan
	 * @param id
	 * @param year
	 * @param month
	 * @return
	 */

	/*
	 * @GetMapping("/invoicemeta/{id}") public
	 * ResponseEntity<ApiStatus<InvoiceMetaNrDTO>> getInvoiceNonLineItem(
	 * 
	 * @RequestHeader(value = "TAN-NUMBER") String collectorTan, @PathVariable
	 * Integer id,
	 * 
	 * @RequestParam(name = "year") int year, @RequestParam("month") int month,
	 * 
	 * @RequestHeader(value = "X-TENANT-ID") String tenantId) { if (collectorTan ==
	 * null && id == null) { throw new CustomException("Tan/Id is not present",
	 * HttpStatus.BAD_REQUEST); } InvoiceMetaNrDTO invoice =
	 * tcsInvoiceLineItemService.getInvoiceMetaNr(collectorTan, id, year, month);
	 * ApiStatus<InvoiceMetaNrDTO> apiStatus = new
	 * ApiStatus<InvoiceMetaNrDTO>(HttpStatus.OK, "SUCCESS",
	 * "Getting Invoice Meta Nr Based on ID", invoice); return new
	 * ResponseEntity<ApiStatus<InvoiceMetaNrDTO>>(apiStatus, HttpStatus.OK); }
	 */

	/**
	 * Author : Rahul Varma For Invoice PDF Processing of reading CSV file
	 * 
	 * @param collectorTan
	 * @param tenantId
	 * @return
	 * @throws IOException
	 */
	@GetMapping("/getcsvdata/pdffile")
	public ResponseEntity<ApiStatus<List<CsvFileDTO>>> getDataFromCsv(
			@RequestHeader(value = "TAN-NUMBER") String collectorTan,
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestParam(value = "batchId") Integer batchId)
			throws IOException {
		List<CsvFileDTO> listOfCsvFileData = tcsInvoiceLineItemService.getCsvDataBasedOnFile(tenantId, collectorTan,
				batchId);
		ApiStatus<List<CsvFileDTO>> apiStatus = new ApiStatus<List<CsvFileDTO>>(HttpStatus.OK, "SUCCESS",
				"Getting Csv data based on csv file", listOfCsvFileData);
		return new ResponseEntity<ApiStatus<List<CsvFileDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for getting Invoice By year, tan and challan month
	 * 
	 * @param collectorTan
	 * @param id
	 * @param year
	 * @param month
	 * @return
	 */
	@GetMapping("/invoice/{assessmentYear}/{challanMonth}/{challanPaid}")
	public ResponseEntity<ApiStatus<List<TCSInvoiceLineItem>>> getInvoiceNonLineItem(
			@RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestHeader(value = "TAN-NUMBER") String collectorTan,
			@PathVariable("assessmentYear") int assessmentYear, @PathVariable("challanMonth") int challanMonth,
			@PathVariable("challanPaid") boolean challanPaid) {
		logger.info("Tenant Id : " + tenantId);
		if (collectorTan == null && assessmentYear > 0 && challanMonth > 0) {
			throw new CustomException("Required parameters are not present", HttpStatus.BAD_REQUEST);
		}
		List<TCSInvoiceLineItem> invoices = tcsInvoiceLineItemService
				.getInvoiceLineItemByAssessmentYearChallanMonthDeductorTan(assessmentYear, challanMonth,
						Arrays.asList(collectorTan), challanPaid, false);
		logger.info(String.format("Response size is: %s", invoices.size()));
		logger.info("LIst of response : {}", invoices);
		ApiStatus<List<TCSInvoiceLineItem>> apiStatus = new ApiStatus<List<TCSInvoiceLineItem>>(HttpStatus.OK,
				"SUCCESS", "Getting tcs invoice line items by assessment year, tan and challan month", invoices);
		return new ResponseEntity<ApiStatus<List<TCSInvoiceLineItem>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param tenantId
	 * @param collectorTan
	 * @param assessmentYear
	 * @param challanMonth
	 * @param challanPaid
	 * @return
	 */
	@GetMapping("/invoice/nonResident/{assessmentYear}/{challanMonth}/{challanPaid}")
	public ResponseEntity<ApiStatus<List<TCSInvoiceLineItem>>> getInvoiceNonResidentLineItem(
			@RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestHeader(value = "TAN-NUMBER") String collectorTan,
			@PathVariable("assessmentYear") int assessmentYear, @PathVariable("challanMonth") int challanMonth,
			@PathVariable("challanPaid") boolean challanPaid) {
		logger.info("Tenant Id : " + tenantId);
		if (collectorTan == null && assessmentYear > 0 && challanMonth > 0) {
			throw new CustomException("Required parameters are not present", HttpStatus.BAD_REQUEST);
		}
		List<TCSInvoiceLineItem> invoices = tcsInvoiceLineItemService
				.getInvoiceLineItemByAssessmentYearChallanMonthDeductorTan(assessmentYear, challanMonth,
						Arrays.asList(collectorTan), challanPaid, true);
		logger.info(String.format("Response size is: %s", invoices.size()));
		ApiStatus<List<TCSInvoiceLineItem>> apiStatus = new ApiStatus<List<TCSInvoiceLineItem>>(HttpStatus.OK,
				"SUCCESS", "Getting invoice line items by assessment year, tan and challan month", invoices);
		return new ResponseEntity<ApiStatus<List<TCSInvoiceLineItem>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param collectorTan
	 * @param amount
	 * @param currencyType
	 * @param tenantId
	 * @return
	 */
	@GetMapping("/convert/rate/{amount}/{currencyType}")
	public ResponseEntity<ApiStatus<BigDecimal>> calculateAmonutByRate(
			@RequestHeader(value = "TAN-NUMBER") String collectorTan, @PathVariable("amount") BigDecimal amount,
			@PathVariable("currencyType") String currencyType, @RequestHeader(value = "X-TENANT-ID") String tenantId) {
		if (collectorTan == null && amount.doubleValue() > 0 && currencyType == null) {
			throw new CustomException("Required parameters are not present", HttpStatus.BAD_REQUEST);
		}
		BigDecimal finalCaluclatedAmount = tcsInvoiceLineItemService.getCurrencyRate(amount, currencyType);
		logger.info(String.format("Final Caluclated Amount is: %s", finalCaluclatedAmount));
		ApiStatus<BigDecimal> apiStatus = new ApiStatus<BigDecimal>(HttpStatus.OK, "SUCCESS",
				"Final Caluclated Amount is", finalCaluclatedAmount.setScale(2, RoundingMode.UP));
		return new ResponseEntity<ApiStatus<BigDecimal>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param batchId
	 * @param assesmentYear
	 * @param tenantId
	 * @param collectorTan
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/csv/to/list/{batchId}/{assesmentYear}")
	public ResponseEntity<ApiStatus<InvoicePdfList>> readCsvForInvoicePdfProcess(
			@PathVariable("batchId") String batchId, @PathVariable("assesmentYear") Integer assesmentYear,
			@RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestHeader(value = "TAN-NUMBER") String collectorTan) throws Exception {
		TCSBatchUpload batchUpload = tcsBatchUploadService.get(assesmentYear, collectorTan,
				UploadTypes.INVOICE_PDF.name(), Integer.valueOf(batchId));
		File file = blobStorageService.getFileFromBlobFileName(tenantId, batchUpload.getSuccessFileUrl(),
				batchUpload.getSourceFilePath());
		InvoicePdfList invoices = tcsInvoiceLineItemService.readCsvForInvoicePdfProcess(file, batchUpload);
		ApiStatus<InvoicePdfList> apiStatus = new ApiStatus<InvoicePdfList>(HttpStatus.OK, "SUCCESS",
				"List of invoice data", invoices);
		return new ResponseEntity<ApiStatus<InvoicePdfList>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param invoicePdfSaveGroupDTOs
	 * @param batchId
	 * @param collectorTan
	 * @param userName
	 * @param collectorPan
	 * @param token
	 * @param batchUploadGroupId
	 * @param tenantId
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/invoice/pdf/save/{batchId}/{batchUploadGroupId}")
	public ResponseEntity<ApiStatus<List<InvoicePDFDTO>>> invoicePdfSave(@RequestBody String invoicePdfSaveGroupDTOs,
			@PathVariable Integer batchId, @RequestHeader(value = "TAN-NUMBER") String collectorTan,
			@RequestHeader(value = "USER_NAME") String userName,
			@RequestHeader(value = "DEDUCTOR-PAN", required = true) String collectorPan,
			@RequestHeader("Authorization") String token, @PathVariable Integer batchUploadGroupId,
			@RequestHeader(value = "X-TENANT-ID") String tenantId) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		List<InvoicePDFDTO> participantJsonList = objectMapper.readValue(invoicePdfSaveGroupDTOs,
				new TypeReference<List<InvoicePDFDTO>>() {
				});
		List<InvoicePDFDTO> invoices = null;
		// List<InvoicePDFDTO> invoices =
		// tcsInvoiceLineItemService.invoicePdfSave(participantJsonList, userName,
		// collectorTan, batchId, batchUploadGroupId, collectorPan, token);
		ApiStatus<List<InvoicePDFDTO>> apiStatus = new ApiStatus<List<InvoicePDFDTO>>(HttpStatus.OK, "SUCCESS",
				"List of invoice data", invoices);
		return new ResponseEntity<ApiStatus<List<InvoicePDFDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param tan
	 * @param tenantId
	 * @param userName
	 * @param invoiceCRDTOs
	 * @return
	 * @throws Exception
	 */
	/*
	 * @PostMapping("/cr") public ResponseEntity<ApiStatus<List<InvoiceLineItem>>>
	 * invoiceCrDr(@RequestHeader(value = "TAN-NUMBER") String tan,
	 * 
	 * @RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader(value =
	 * "USER_NAME") String userName,
	 * 
	 * @RequestBody List<InvoiceCRDTO> invoiceCRDTOs) throws Exception {
	 * List<InvoiceLineItem> invoices = tcsInvoiceLineItemService.invoiceCrDr(tan,
	 * tenantId, userName, invoiceCRDTOs); ApiStatus<List<InvoiceLineItem>>
	 * apiStatus = new ApiStatus<List<InvoiceLineItem>>(HttpStatus.OK, "SUCCESS",
	 * "List of invoice data", invoices); return new
	 * ResponseEntity<ApiStatus<List<InvoiceLineItem>>>(apiStatus, HttpStatus.OK); }
	 */

	/**
	 * 
	 * @param collectorTan
	 * @param tenantId
	 * @param type
	 * @param pagination
	 * @param year
	 * @param month
	 * @return
	 */
	@PostMapping(value = "/document/{type}/{year}/{month}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<TCSInvoiceLineItem>>> getInvoiceByType(
			@RequestHeader(value = "TAN-NUMBER") String collectorTan,
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @PathVariable String type,
			@RequestBody Pagination pagination, @PathVariable int year, @PathVariable int month) {
		CommonDTO<TCSInvoiceLineItem> invoices = tcsInvoiceLineItemService.getInvoiceByType(collectorTan, type,
				pagination, year, month);
		ApiStatus<CommonDTO<TCSInvoiceLineItem>> apiStatus = new ApiStatus<CommonDTO<TCSInvoiceLineItem>>(HttpStatus.OK,
				"SUCCESS", "NO ALERT", invoices);
		return new ResponseEntity<ApiStatus<CommonDTO<TCSInvoiceLineItem>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param collectorTan
	 * @param tenantId
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/pdf/transaction/status")
	public ResponseEntity<ApiStatus<String>> getTcsPdfTransactionStatus(
			@RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestHeader(value = "TAN-NUMBER") String collectorTan,
			@RequestParam(value = "assessmentYear", required = true) Integer assessmentYear,
			@RequestParam(value = "assessmentMonth", required = true) Integer assessmentMonth) throws Exception {
		String startDate = CommonUtil.getQueryDate(CommonUtil.getMonthStartDate(assessmentYear, assessmentMonth - 1));
		String endDate = CommonUtil.getQueryDate(CommonUtil.getMonthEndDate(assessmentYear, assessmentMonth - 1));
		String result = tcsInvoiceLineItemService.getPdfActivityTrackerResult(assessmentYear,
				UploadTypes.INVOICE_PDF.name(), collectorTan, startDate, endDate);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", "List of invoice line item", result);
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param collectorTan
	 * @param tenantId
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/calculation/status")
	public ResponseEntity<ApiStatus<String>> getTcsCalculationStatus(
			@RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestHeader(value = "TAN-NUMBER") String collectorTan,
			@RequestParam(value = "assessmentYear", required = true) Integer assessmentYear,
			@RequestParam(value = "assessmentMonth", required = true) Integer assessmentMonth) throws Exception {
		String startDate = CommonUtil.getQueryDate(CommonUtil.getMonthStartDate(assessmentYear, assessmentMonth - 1));
		String endDate = CommonUtil.getQueryDate(CommonUtil.getMonthEndDate(assessmentYear, assessmentMonth - 1));
		String result = tcsInvoiceLineItemService.getTdsCalculationForInvoice(assessmentYear, collectorTan, startDate,
				endDate);
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
	@GetMapping("/allinvoice")
	public ResponseEntity<ApiStatus<List<TCSInvoiceLineItem>>> getAllInvoiceLineItemData(
			@RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestParam(value = "assessmentYear") Integer assessmentYear,
			@RequestHeader(value = "X-TENANT-ID") String tenantId) throws Exception {
		List<TCSInvoiceLineItem> invoices = tcsInvoiceLineItemService
				.getAllInvoiceLineItemData(CommonUtil.getAssessmentYear(assessmentYear), tan);
		ApiStatus<List<TCSInvoiceLineItem>> apiStatus = new ApiStatus<List<TCSInvoiceLineItem>>(HttpStatus.OK,
				"SUCCESS", "List of invoice line item", invoices);
		return new ResponseEntity<ApiStatus<List<TCSInvoiceLineItem>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param crData
	 * @return
	 */
	/*
	 * @PostMapping(value = "/invoice/originaldocument", produces =
	 * MediaType.APPLICATION_JSON_VALUE) public
	 * ResponseEntity<ApiStatus<CommonDTO<TCSInvoiceLineItem>>>
	 * getInvoicesByCrData(@RequestBody CRDTO crData) {
	 * CommonDTO<TCSInvoiceLineItem> invoices =
	 * tcsInvoiceLineItemService.getInvoicesByCrData(crData);
	 * ApiStatus<CommonDTO<TCSInvoiceLineItem>> apiStatus = new
	 * ApiStatus<CommonDTO<TCSInvoiceLineItem>>(HttpStatus.OK, "SUCCESS",
	 * "NO ALERT", invoices); return new
	 * ResponseEntity<ApiStatus<CommonDTO<TCSInvoiceLineItem>>>(apiStatus,
	 * HttpStatus.OK); }
	 */

	/**
	 * 
	 * @param crData
	 * @param tenantId
	 * @param collectorPan
	 * @param token
	 * @return
	 */
	/*
	 * @PostMapping(value = "/invoice/originaldocument/adjust", produces =
	 * MediaType.APPLICATION_JSON_VALUE) public
	 * ResponseEntity<ApiStatus<InvoiceLineItemCRDTO>> adjustments(@RequestBody
	 * CRDTO crData,
	 * 
	 * @RequestHeader(value = "X-TENANT-ID") String tenantId,
	 * 
	 * @RequestHeader(value = "DEDUCTOR-PAN") String
	 * collectorPan, @RequestHeader("Authorization") String token) {
	 * InvoiceLineItemCRDTO invoices = tcsInvoiceLineItemService.adjustments(crData,
	 * token, collectorPan, tenantId); ApiStatus<InvoiceLineItemCRDTO> apiStatus =
	 * new ApiStatus<InvoiceLineItemCRDTO>(HttpStatus.OK, "SUCCESS",
	 * "CR Adjustments done sucessfully.", invoices); return new
	 * ResponseEntity<ApiStatus<InvoiceLineItemCRDTO>>(apiStatus, HttpStatus.OK); }
	 */

	/**
	 * 
	 * @param invoiceData
	 * @return
	 */
	/*
	 * @PostMapping(value = "invoice/originaldocument/reject", produces =
	 * MediaType.APPLICATION_JSON_VALUE) public
	 * ResponseEntity<ApiStatus<InvoiceLineItem>> updateInvoices(@RequestBody
	 * InvoiceDTO invoiceData) { InvoiceLineItem invoice =
	 * invoiceLineItemService.updateInvoices(invoiceData);
	 * ApiStatus<InvoiceLineItem> apiStatus = new
	 * ApiStatus<InvoiceLineItem>(HttpStatus.OK, "SUCCESS", "NO ALERT", invoice);
	 * return new ResponseEntity<ApiStatus<InvoiceLineItem>>(apiStatus,
	 * HttpStatus.OK); }
	 */

	/**
	 * 
	 * @param assessmentYear
	 * @param month
	 * @param collecteeName
	 * @param collectorTan
	 * @param pagination
	 * @param tenantId
	 * @return
	 */
	@PostMapping(value = "/invoice/currentmonthdata/{year}/{month}/{collecteeName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<CommonDTO<TCSInvoiceLineItem>>> getInvoicesByCurrentMonth(
			@PathVariable(value = "year") int assessmentYear, @PathVariable(value = "month") int month,
			@PathVariable(value = "collecteeName") String collecteeName,
			@RequestHeader(value = "TAN-NUMBER") String collectorTan, @RequestBody Pagination pagination,
			@RequestHeader(value = "X-TENANT-ID") String tenantId) {
		CommonDTO<TCSInvoiceLineItem> invoices = tcsInvoiceLineItemService.getInvoicesByCurrentMonth(assessmentYear,
				month, collecteeName, collectorTan, pagination);
		ApiStatus<CommonDTO<TCSInvoiceLineItem>> apiStatus = new ApiStatus<CommonDTO<TCSInvoiceLineItem>>(HttpStatus.OK,
				"SUCCESS", "NO ALERT", invoices);
		return new ResponseEntity<ApiStatus<CommonDTO<TCSInvoiceLineItem>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param invoicesAndCrData
	 * @param tenantId
	 * @param collectorPan
	 * @param token
	 * @return
	 */
	@PostMapping(value = "/invoice/currentmonthadjustments", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<InvoiceLineItemCRDTO>> adjustmentsForCurrentMonth(
			@RequestBody InvoiceLineItemCRDTO invoicesAndCrData, @RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestHeader(value = "DEDUCTOR-PAN") String collectorPan, @RequestHeader("Authorization") String token) {
		InvoiceLineItemCRDTO invoices = tcsInvoiceLineItemService.adjustmentsForCurrentMonth(invoicesAndCrData, token,
				collectorPan, tenantId);
		ApiStatus<InvoiceLineItemCRDTO> apiStatus = new ApiStatus<InvoiceLineItemCRDTO>(HttpStatus.OK, "SUCCESS",
				"CR Adjustments done sucessfully.", invoices);
		return new ResponseEntity<ApiStatus<InvoiceLineItemCRDTO>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * fiegn call to get invoice based on year and deductor master tan
	 * 
	 * @param assessmentYear
	 * @param collectorTan
	 * @return
	 */
	@GetMapping(value = "/findBy/year/deductorTan")
	public ResponseEntity<ApiStatus<List<TCSInvoiceLineItem>>> findByYearAndDeductorTan(
			@RequestParam("year") Integer assessmentYear, @RequestParam("tan") String collectorTan,
			@RequestHeader(value = "X-TENANT-ID") String tenantId) {
		logger.info("feign call executing to get the tcs invoice line item data   {}");
		List<TCSInvoiceLineItem> listInvoice = tcsInvoiceLineItemDAO.findAll(collectorTan, assessmentYear);
		ApiStatus<List<TCSInvoiceLineItem>> apiStatus = new ApiStatus<List<TCSInvoiceLineItem>>(HttpStatus.OK,
				"SUCCESS", "Invoice retrieved sucessfully.", listInvoice);
		logger.info("feign call execution succeded to get the tcs invoice line item data   {}");
		return new ResponseEntity<ApiStatus<List<TCSInvoiceLineItem>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param collectorTan
	 * @param deducteeType
	 * @param type
	 * @param year
	 * @param month
	 * @param isMismatch
	 * @param tenantId
	 * @return
	 */
	@GetMapping(value = "/collectees/{type}/{year}/{month}/{ismismatch}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<Set<String>>> getDeductees(@RequestHeader("TAN-NUMBER") String collectorTan,
			@PathVariable(value = "type", required = true) String type,
			@PathVariable(value = "year", required = true) int year,
			@PathVariable(value = "month", required = true) int month,
			@PathVariable(value = "ismismatch", required = true) boolean isMismatch,
			@RequestHeader(value = "X-TENANT-ID") String tenantId) {
		ApiStatus<Set<String>> apiStatus = new ApiStatus<Set<String>>(HttpStatus.OK, "SUCCESS",
				"LIST OF " + type.toUpperCase() + " COLLECTEES",
				tcsInvoiceLineItemService.getCollectees(collectorTan, type, year, month, isMismatch));
		return new ResponseEntity<ApiStatus<Set<String>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * fiegn call to get invoice for tds-returns
	 * 
	 * @param assessmentYear
	 * @param challanMonths
	 * @param collectorTan
	 * @param challanPaid
	 * @param isForNonResidents
	 * @return
	 */
	@GetMapping(value = "/invoice/year/Tan/challanMonths")
	public ResponseEntity<ApiStatus<List<TCSInvoiceLineItem>>> getInvoiceLineItemsByAssessmentYearChallanMonthDeductorTan(
			@RequestParam("assessmentYear") Integer assessmentYear,
			@RequestParam("challanMonths") Integer[] challanMonths,
			@RequestParam("collectorTan") List<String> collectorTan, @RequestParam("challanPaid") boolean challanPaid,
			@RequestParam("isForNonResidents") boolean isForNonResidents,
			@RequestHeader(value = "X-TENANT-ID") String tenantId) {
		List<Integer> listChallanMonth = Arrays.asList(challanMonths);
		List<TCSInvoiceLineItem> list = tcsInvoiceLineItemDAO
				.getInvoiceLineItemsByAssessmentYearChallanMonthDeductorTan(assessmentYear, listChallanMonth,
						collectorTan, challanPaid, isForNonResidents);
		ApiStatus<List<TCSInvoiceLineItem>> apiStatus = new ApiStatus<List<TCSInvoiceLineItem>>(HttpStatus.OK,
				"SUCCESS", "tcs Invoice line item retrieved sucessfully.", list);
		return new ResponseEntity<ApiStatus<List<TCSInvoiceLineItem>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * Update mismatch table based on Mismatch category for Pdf
	 * 
	 * @param mismatchcategory
	 * @param invoiceMismatchUpdateDTO
	 * @return
	 * @throws RecordNotFoundException
	 * @throws TanNotFoundException
	 */
	@PutMapping("/tcsmismatches/invoice/pdf/{mismatchcategory}/action")
	public ResponseEntity<ApiStatus<TCSInvoiceLineItem>> uploadPDF(@PathVariable String mismatchcategory,
			@RequestBody TCSInvoiceLineItem invoiceMismatchUpdateDTO,
			@RequestHeader(value = "TAN-NUMBER") String collectorTan, @RequestHeader("X-TENANT-ID") String tenantId)
			throws RecordNotFoundException, TanNotFoundException {
		if (collectorTan == null) {
			logger.error("Tan Not Found");
			throw new TanNotFoundException("Tan Not Found");
		}
		TCSInvoiceLineItem response = tcsInvoiceLineItemService.updateMismatchByActionForPdf(collectorTan,
				invoiceMismatchUpdateDTO);
		ApiStatus<TCSInvoiceLineItem> apiStatus = new ApiStatus<TCSInvoiceLineItem>(HttpStatus.OK, "SUCCESS",
				"LIST OF TCS MISMATCHES FOR PDF TO UPDATE", response);
		return new ResponseEntity<ApiStatus<TCSInvoiceLineItem>>(apiStatus, HttpStatus.OK);

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
	@PostMapping("/tcsmismatches/remediation/import")
	public ResponseEntity<ApiStatus<TCSBatchUpload>> getStatusOfRemediationReport(
			@RequestParam("file") MultipartFile file, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		String contentType = new Tika().detect(file.getInputStream());
		if (AllowedMimeTypes.XLSX.getMimeType().equals(contentType)
				|| AllowedMimeTypes.OOXML.getMimeType().equals(contentType)) {
			TCSBatchUpload batchUpload = tcsInvoiceLineItemService.importToBatchUpload(file, tenantId, userName);
			// TODO: To get the imported file and store it in another table for
			// batch_upload_remediation table
			logger.info("Remediation report status: {} ", batchUpload.getFileName());
			ApiStatus<TCSBatchUpload> apiStatus = new ApiStatus<TCSBatchUpload>(HttpStatus.OK, "SUCCESS",
					"UPLOADED REMEDIATION FILE SUCCESSFULLY", batchUpload);
			return new ResponseEntity<ApiStatus<TCSBatchUpload>>(apiStatus, HttpStatus.OK);
		} else {
			logger.warn("Unsupported file upload attempt by {}", userName);
			throw new CustomException("Invalid file contents, only xlsx files are allowed", HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * 
	 * @param collectorTan
	 * @param collectorPan
	 * @param tenantId
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
	@PostMapping(value = "/tcsmismatchesreport/export", consumes = { "application/x-www-form-urlencoded" })
	public ResponseEntity<InputStreamResource> exportRemediationReport(
			@RequestParam(value = "tan", required = true) String collectorTan,
			@RequestParam(value = "pan", required = true) String collectorPan,
			@RequestParam(value = "tenantId", required = true) String tenantId,
			@RequestParam(value = "year", required = true) Integer year,
			@RequestParam(value = "month", required = true) Integer month,
			@RequestHeader(value = "USER_NAME") String userName)
			throws IOException, TanNotFoundException, InvalidKeyException, URISyntaxException, StorageException {
		logger.info("TAN: {}", collectorTan);
		if (collectorTan == null) {
			throw new CustomException("No Tan Found to continue the download", HttpStatus.BAD_REQUEST);
		}
		ByteArrayInputStream in = tcsInvoiceLineItemService.exportRemediationReport(collectorTan, tenantId,
				collectorPan, year, month, userName);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=Invoice_Remediation_Report_" + collectorTan + ".xlsx");
		logger.info("Remediation report export done ");
		return ResponseEntity.ok().headers(headers).body(new InputStreamResource(in));
	}

	/**
	 * 
	 * @param tenantId
	 * @param collectorTan
	 * @param collectorPan
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
	@PostMapping(value = "/tdsmismatchesreport/async/export")
	public ResponseEntity<ApiStatus<String>> asyncExportRemediationReport(@RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "TAN-NUMBER", required = true) String collectorTan,
			@RequestHeader(value = "DEDUCTOR-PAN", required = true) String collectorPan,
			@RequestParam(value = "year", required = true) Integer year,
			@RequestParam(value = "month", required = true) Integer month,
			@RequestHeader(value = "USER_NAME") String userName)
			throws IOException, TanNotFoundException, InvalidKeyException, URISyntaxException, StorageException {
		logger.info("TAN: {}", collectorTan);
		if (collectorTan == null) {
			throw new CustomException("No Tan Found to continue the download", HttpStatus.BAD_REQUEST);
		}
		tcsInvoiceLineItemService.asyncExportRemediationReport(collectorTan, tenantId, collectorPan, year, month,
				userName);
		String message = "Invoice mismatch report requested successfully and will be available shortly";
		logger.info("{}", message);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", message, message);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}

	@PostMapping(value = "/download/stream", consumes = { "application/x-www-form-urlencoded" })
	ResponseEntity<StreamingResponseBody> getStream(HttpServletResponse response,
			@RequestParam("deductorTan") String deductorTan,
			@RequestParam("deductorPan") String deductorPan,
			@RequestParam("batchId") String batchId, @RequestHeader("X-TENANT-ID") String tenantId) throws IOException {

		Calendar calendar=Calendar.getInstance();
		String fileName="invoice_mismach_report_"+deductorTan+"_"+calendar.get(Calendar.YEAR)+"_"+calendar.get(Calendar.MONTH);

		response.addHeader("Content-disposition", "attachment;filename="+fileName+".zip");
		response.setContentType("application/octet-stream");

		logger.info("date :{}", Calendar.getInstance().toInstant());

		TCSBatchUpload batchUpload = tcsBatchUploadService.getTCSBatchUpload(Integer.valueOf(batchId));
		File filezip = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getOtherFileUrl());

		File csvFile = File.createTempFile("blob", "csv");
		tcsInvoiceLineItemService.decompressGzip(filezip.toPath(), csvFile.toPath());

		CsvReader csvReader = new CsvReader();

		csvReader.setContainsHeader(true);

		CsvContainer csv = csvReader.read(csvFile, StandardCharsets.UTF_8);
		ByteArrayInputStream byteArrayInputStream = tcsInvoiceLineItemService.generateExcel(csv.getRowCount(), csv,
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

	/**
	 * to generate the report having exempted invoices
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
	@PostMapping(value = "/exemption/async/export")
	public ResponseEntity<ApiStatus<String>> asyncTCExportExemptionReport(@RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "TAN-NUMBER", required = true) String tan,
			@RequestHeader(value = "DEDUCTOR-PAN", required = true) String pan,
			@RequestParam(value = "year", required = true) Integer year,
			@RequestParam(value = "month", required = true) Integer month,
			@RequestHeader(value = "X-USER-EMAIL") String userName)
			throws IOException, TanNotFoundException, InvalidKeyException, URISyntaxException, StorageException {
		logger.info("TAN: {}", tan);

		if (tan == null) {
			throw new CustomException("No Tan Found to continue the download", HttpStatus.BAD_REQUEST);
		}

		tcsInvoiceLineItemService.asyncTCExportExemptionReport(tan, tenantId, pan, year, month, userName);

		String message = "Invoice Exempted report requested successfully and will be available shortly";
		logger.info("{}", message);
		ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", message, message);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}
	
	@GetMapping(value = "invoice/yearTanBsrCode")
	public ResponseEntity<ApiStatus<List<TCSLedger>>> invoiceByTanYearBSRCodeSerialNoAndDateTCS(
			@RequestParam("assessmentYear") Integer year, @RequestParam("TAN") String deductorTan,
			@RequestParam("ChallanPaid") Boolean challanPaid,
			@RequestParam("ResidentIndicator") boolean isForNonResidents, @RequestParam("BSRCode") String bsrCode,
			@RequestParam("RecieptSerialNo") String receiptSerailNo, @RequestParam("RecieptDate") String receiptDate,
			@RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestParam("months") List<Integer> months,
			@RequestParam(value = "section", required = false) String section) {
		MultiTenantContext.setTenantId(tenantId);
		List<TCSLedger> listInvoice = tcsInvoiceLineItemDAO
				.getInvoiceLineItemsByReceiptSerialNoAndBSRCodeAndReceiptDate(year, deductorTan, challanPaid,
						isForNonResidents, bsrCode, receiptSerailNo, receiptDate,months,section);
		ApiStatus<List<TCSLedger>> apiStatus = new ApiStatus<List<TCSLedger>>(
				HttpStatus.OK, "SUCCESS", "Invoice retrieved sucessfully.", listInvoice);
		return new ResponseEntity<ApiStatus<List<TCSLedger>>>(apiStatus, HttpStatus.OK);
	}

}
