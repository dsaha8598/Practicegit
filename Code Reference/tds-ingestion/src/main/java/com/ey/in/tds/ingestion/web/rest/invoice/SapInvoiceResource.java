package com.ey.in.tds.ingestion.web.rest.invoice;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ey.in.tds.common.dto.invoice.InvoiceMismatchByBatchIdDTO;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.core.exceptions.TanNotFoundException;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.ingestion.dto.invoice.UpdateOnScreenDTO;
import com.ey.in.tds.ingestion.service.sap.SapInvoiceService;

@RestController
@RequestMapping("/api/ingestion")
public class SapInvoiceResource {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private SapInvoiceService sapInvoiceService;

	/**
	 * Get the List oF records for SAP based on mismatch and Batch Id
	 * 
	 * @param batchId
	 * @PatVariable processedFrom
	 * @return
	 * @throws TanNotFoundException
	 */
	@GetMapping("/sapmismatches/{processedFrom}/{batchId}")
	public ResponseEntity<ApiStatus<List<InvoiceMismatchByBatchIdDTO>>> listOfMisMatchesBasedonBatchUploadIdForPdfInvoice(
			@PathVariable UUID batchId, @RequestHeader(value = "TAN-NUMBER") String tan,
			@PathVariable String processedFrom,
			@RequestBody Pagination pagination) throws TanNotFoundException {

		if (batchId != null && tan != null || processedFrom != null) {
			List<InvoiceMismatchByBatchIdDTO> listMismatchesByBatchId =null;//TODO NEED TO CHANGE FOR SQL
					//sapInvoiceService
				//	.getSAPInvoiceMismatchByBatchUploadIDForExcel(batchId, tan, processedFrom,pagination);

			logger.info("List of SAP mismatches based on Batch Upload Id---: {} ", listMismatchesByBatchId);
			ApiStatus<List<InvoiceMismatchByBatchIdDTO>> apiStatus = new ApiStatus<List<InvoiceMismatchByBatchIdDTO>>(
					HttpStatus.OK, "SUCCESS", "LIST OF SAP MISMATCHES FOR PARTICULAR BATCH", listMismatchesByBatchId);
			return new ResponseEntity<ApiStatus<List<InvoiceMismatchByBatchIdDTO>>>(apiStatus, HttpStatus.OK);

		} else {
			logger.error("No batch Id/tan, Cannot process");
			throw new CustomException("No batch Id/tan, Cannot process", HttpStatus.BAD_REQUEST);
		}
	}// end of method

	/**
	 * List of mismatches for excel (for Excel and SAP)
	 * 
	 * @return
	 * @throws TanNotFoundException
	 */
	@GetMapping("sapmismatches/{processedFrom}/all/{year}/{month}")
	public ResponseEntity<ApiStatus<List<InvoiceMismatchByBatchIdDTO>>> listOfMisMatchesForExcelBasedonBatchUploadId(
			@RequestHeader(value = "TAN-NUMBER") String tan, @PathVariable int year, @PathVariable int month,
			@PathVariable String processedFrom,
			@RequestBody Pagination pagination) throws TanNotFoundException {
		if (tan != null || processedFrom != null) {
			List<InvoiceMismatchByBatchIdDTO> listSAPMismatchesAll = null;
			//TODO NEED TO CHANGE FOR SQL
					//sapInvoiceService.getSAPInvoiceMismatchAll(tan,
					//year, month, processedFrom,pagination);
			logger.info("List of SAP mismatches for excel based on year and month----: {} ", listSAPMismatchesAll);

			ApiStatus<List<InvoiceMismatchByBatchIdDTO>> apiStatus = new ApiStatus<List<InvoiceMismatchByBatchIdDTO>>(
					HttpStatus.OK, "SUCCESS", "LIST OF SAP MISMATCHES", listSAPMismatchesAll);
			return new ResponseEntity<ApiStatus<List<InvoiceMismatchByBatchIdDTO>>>(apiStatus, HttpStatus.OK);
		} else {
			logger.error("Tan / Processed From Not Found");
			throw new CustomException("Tan / Processed From Not Found", HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Based on MismatchCategory and BatchId get the record for SAP
	 * 
	 * @param batchId
	 * @param mismatchcategory
	 * @return
	 * @throws TanNotFoundException
	 */
/*	@PostMapping(value = "sapmismatches/{processedFrom}/{batchId}/{mismatchcategory}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<PagedData<InvoiceLineItem>>> listOfSAPMisMatchesBasedonBatchUploadIdCategory(
			@RequestHeader(value = "TAN-NUMBER") String tan, @PathVariable UUID batchId,
			@PathVariable String mismatchcategory, @PathVariable String processedFrom,
			@RequestBody Pagination pagination) throws TanNotFoundException {
		if (tan != null && batchId != null && mismatchcategory != null) {
			PagedData<InvoiceLineItem> listSAPMismatchesByBatchIdMiscmatchCategory = sapInvoiceService
					.getSAPInvoiceMismatchByBatchUploadIDMismatchCategory(batchId, mismatchcategory, tan,
							processedFrom,pagination);

			logger.info("List of mismatches based on Batch upload Id category : {} ",
					listSAPMismatchesByBatchIdMiscmatchCategory);
			ApiStatus<PagedData<InvoiceLineItem>> apiStatus = new ApiStatus<PagedData<InvoiceLineItem>>(HttpStatus.OK, "SUCCESS",
					"NO ALERT", listSAPMismatchesByBatchIdMiscmatchCategory);
			return new ResponseEntity<ApiStatus<PagedData<InvoiceLineItem>>>(apiStatus, HttpStatus.OK);
		} else {
			logger.error("Tan Not Found");
			throw new CustomException("Tan Not Found", HttpStatus.BAD_REQUEST);
		}
	}  */

	/**
	 * To get the mismatch records based on mismatch Category for SAP
	 * 
	 * @param mismatchcategory
	 * @return
	 * @throws TanNotFoundException
	 */
/*	@GetMapping("sapmismatches/all/{processedFrom}/{mismatchcategory}/{year}/{month}")
	public ResponseEntity<ApiStatus<PagedData<InvoiceLineItem>>> listOfSAPMisMatchesBasedOnMismatchCategory(
			@RequestHeader(value = "TAN-NUMBER") String tan, @PathVariable String mismatchcategory,
			@PathVariable int year, @PathVariable int month, @PathVariable String processedFrom,
			@RequestBody Pagination pagination)
			throws TanNotFoundException {
		if (tan == null && mismatchcategory == null) {
			throw new CustomException("No Tan/Mismatch Category Found to proceed", HttpStatus.BAD_REQUEST);
		}

		PagedData<InvoiceLineItem> listSAPMismatchesAllBasesOnMIstachCategory = sapInvoiceService
				.getSAPInvoiceMismatchBasedOnMismatchCategory(mismatchcategory, tan, year, month, processedFrom,
						pagination);

		logger.info("List of SAP mismatches based mismatch category---: {}",
				listSAPMismatchesAllBasesOnMIstachCategory);
		ApiStatus<PagedData<InvoiceLineItem>> apiStatus = new ApiStatus<PagedData<InvoiceLineItem>>(HttpStatus.OK, "SUCCESS",
				"LIST OF SAP MISMATCHES BASED ON MISMATCH CATEGORY", listSAPMismatchesAllBasesOnMIstachCategory);
		return new ResponseEntity<ApiStatus<PagedData<InvoiceLineItem>>>(apiStatus, HttpStatus.OK);
	} */

	/**
	 * On Screen Update mismatch table based on Mismatch category for SAP Excel
	 * 
	 * @param mismatchcategory
	 * @param invoiceMismatchUpdateDTO
	 * @return
	 * @throws RecordNotFoundException
	 * @throws TanNotFoundException
	 */
	@PutMapping("sapmismatches/{processedFrom}/{mismatchcategory}/action")
	public ResponseEntity<ApiStatus<UpdateOnScreenDTO>> uploadExcel(@RequestHeader(value = "TAN-NUMBER") String tan,
			@PathVariable String mismatchcategory, @RequestBody UpdateOnScreenDTO invoiceMismatchUpdateDTO,
			@PathVariable String processedFrom) throws RecordNotFoundException, TanNotFoundException {
		// Handling null pointer exception
		if (tan == null && mismatchcategory == null) {
			throw new CustomException("Tan / Mismatch Category  Not Found", HttpStatus.BAD_REQUEST);
		}

		UpdateOnScreenDTO response = null;
		//TODO NEED TO CHANGE FOR SQL
				//sapInvoiceService.updateSAPMismatchByAction(tan, invoiceMismatchUpdateDTO,
				//processedFrom);

		logger.info("On Screen Update Object---: {} ", response);

		ApiStatus<UpdateOnScreenDTO> apiStatus = new ApiStatus<UpdateOnScreenDTO>(HttpStatus.OK, "SUCCESS",
				"LIST OF MISMATCHES UPDATED FOR SAP", response);
		return new ResponseEntity<ApiStatus<UpdateOnScreenDTO>>(apiStatus, HttpStatus.OK);
	}

}
