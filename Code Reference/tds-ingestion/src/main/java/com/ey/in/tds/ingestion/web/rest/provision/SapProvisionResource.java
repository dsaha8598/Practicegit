package com.ey.in.tds.ingestion.web.rest.provision;

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

import com.ey.in.tds.common.model.provision.ProvisionMismatchByBatchIdDTO;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.core.exceptions.TanNotFoundException;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.ingestion.dto.invoice.UpdateOnScreenDTO;
import com.ey.in.tds.ingestion.service.provision.SapProvisionService;

@RestController
@RequestMapping("/api/ingestion")
public class SapProvisionResource {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private SapProvisionService sapProvisionService;

	/**
	 * Get the List oF records for SAP based on mismatch and Batch Id
	 * 
	 * @param batchId
	 * @PatVariable processedFrom
	 * @return
	 * @throws TanNotFoundException
	 */
	@GetMapping("/sapprovisionmismatches/{processedFrom}/{batchId}")
	public ResponseEntity<ApiStatus<List<ProvisionMismatchByBatchIdDTO>>> listOfSAPMisMatchesBasedonBatchUploadIdForProvision(
			@PathVariable UUID batchId, @RequestHeader(value = "TAN-NUMBER") String tan,
			@PathVariable String processedFrom,
			@RequestBody Pagination pagination) throws TanNotFoundException {

		if (batchId != null && tan != null || processedFrom != null) {
			List<ProvisionMismatchByBatchIdDTO> listProvisionSapMismatchesByBatchId =null;	
					//sapProvisionService
					//.getSAPProvisionMismatchByBatchUploadIDForExcel(batchId, tan, processedFrom,pagination);

			logger.info("List of SAP mismatches based on Batch Upload Id for Provision---: {} ", listProvisionSapMismatchesByBatchId);
			ApiStatus<List<ProvisionMismatchByBatchIdDTO>> apiStatus = new ApiStatus<List<ProvisionMismatchByBatchIdDTO>>(
					HttpStatus.OK, "SUCCESS", "LIST OF SAP PROVISION MISMATCHES FOR PARTICULAR BATCH", listProvisionSapMismatchesByBatchId);
			return new ResponseEntity<ApiStatus<List<ProvisionMismatchByBatchIdDTO>>>(apiStatus, HttpStatus.OK);

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
	@GetMapping("sapprovisionmismatches/{processedFrom}/all/{year}/{month}")
	public ResponseEntity<ApiStatus<List<ProvisionMismatchByBatchIdDTO>>> listOfSAPProvisionMisMatchesForExcelBasedonBatchUploadId(
			@RequestHeader(value = "TAN-NUMBER") String tan, @PathVariable int year, @PathVariable int month,
			@PathVariable String processedFrom,
			@RequestBody Pagination pagination) throws TanNotFoundException {
		if (tan != null || processedFrom != null) {
			List<ProvisionMismatchByBatchIdDTO> listSAPProvisionMismatchesAll =null;
                   //TODO NEED TO CHANGE FOR SQL
					//sapProvisionService.getSAPProvisionInvoiceMismatchAll(tan,
					//year, month, processedFrom,pagination);
			logger.info("List of SAP mismatches for excel based on year and month for Provision----: {} ", listSAPProvisionMismatchesAll);

			ApiStatus<List<ProvisionMismatchByBatchIdDTO>> apiStatus = new ApiStatus<List<ProvisionMismatchByBatchIdDTO>>(
					HttpStatus.OK, "SUCCESS", "LIST OF SAP PROVISION MISMATCHES", listSAPProvisionMismatchesAll);
			return new ResponseEntity<ApiStatus<List<ProvisionMismatchByBatchIdDTO>>>(apiStatus, HttpStatus.OK);
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
/*	@PostMapping(value = "sapprovisionmismatches/{processedFrom}/{batchId}/{mismatchcategory}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<PagedData<Provision>>> listOfSAPMisMatchesBasedonBatchUploadIdCategory(
			@RequestHeader(value = "TAN-NUMBER") String tan, @PathVariable UUID batchId,
			@PathVariable String mismatchcategory, @PathVariable String processedFrom,
			@RequestBody Pagination pagination) throws TanNotFoundException {
		if (tan != null && batchId != null && mismatchcategory != null) {
			PagedData<Provision> listSAPProvisionMismatchesByBatchIdMiscmatchCategory = sapProvisionService
					.getSAPProvisionMismatchByBatchUploadIDMismatchCategory(batchId, mismatchcategory, tan,
							processedFrom,pagination);

			logger.info("List of SAP mismatches based on Batch upload Id category for Provision : {} ",
					listSAPProvisionMismatchesByBatchIdMiscmatchCategory);
			ApiStatus<PagedData<Provision>> apiStatus = new ApiStatus<PagedData<Provision>>(HttpStatus.OK, "SUCCESS",
					"NO ALERT", listSAPProvisionMismatchesByBatchIdMiscmatchCategory);
			return new ResponseEntity<ApiStatus<PagedData<Provision>>>(apiStatus, HttpStatus.OK);
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
/*	@GetMapping("sapprovisionmismatches/all/{processedFrom}/{mismatchcategory}/{year}/{month}")
	public ResponseEntity<ApiStatus<PagedData<Provision>>> listOfSAPProvisionMisMatchesBasedOnMismatchCategory(
			@RequestHeader(value = "TAN-NUMBER") String tan, @PathVariable String mismatchcategory,
			@PathVariable int year, @PathVariable int month, @PathVariable String processedFrom,
			@RequestBody Pagination pagination)
			throws TanNotFoundException {
		if (tan == null && mismatchcategory == null) {
			throw new CustomException("No Tan/Mismatch Category Found to proceed", HttpStatus.BAD_REQUEST);
		}

		PagedData<Provision> listSAPProvisionMismatchesAllBasesOnMIstachCategory = sapProvisionService
				.getSAPProvisionnMismatchBasedOnMismatchCategory(mismatchcategory, tan, year, month, processedFrom,
						pagination);

		logger.info("List of SAP mismatches based mismatch category---: {}",
				listSAPProvisionMismatchesAllBasesOnMIstachCategory);
		ApiStatus<PagedData<Provision>> apiStatus = new ApiStatus<PagedData<Provision>>(HttpStatus.OK, "SUCCESS",
				"LIST OF SAP MISMATCHES BASED ON MISMATCH CATEGORY", listSAPProvisionMismatchesAllBasesOnMIstachCategory);
		return new ResponseEntity<ApiStatus<PagedData<Provision>>>(apiStatus, HttpStatus.OK);
	}  */

	/**
	 * On Screen Update mismatch table based on Mismatch category for SAP Excel
	 * 
	 * @param mismatchcategory
	 * @param invoiceMismatchUpdateDTO
	 * @return
	 * @throws RecordNotFoundException
	 * @throws TanNotFoundException
	 */
	@PutMapping("sapprovisionmismatches/{processedFrom}/{mismatchcategory}/action")
	public ResponseEntity<ApiStatus<UpdateOnScreenDTO>> uploadSAPPRovisionExcel(@RequestHeader(value = "TAN-NUMBER") String tan,
			@PathVariable String mismatchcategory, @RequestBody UpdateOnScreenDTO invoiceMismatchUpdateDTO,
			@PathVariable String processedFrom) throws RecordNotFoundException, TanNotFoundException {
		// Handling null pointer exception
		if (tan == null && mismatchcategory == null) {
			throw new CustomException("Tan / Mismatch Category  Not Found", HttpStatus.BAD_REQUEST);
		}

		UpdateOnScreenDTO response = null;
		//TODO NEED TO CHANAGE FOR SQL
				//sapProvisionService.updateSAPProvisionMismatchByAction(tan, invoiceMismatchUpdateDTO,
				//processedFrom);

		logger.info("On Screen Update Object---: {} ", response);

		ApiStatus<UpdateOnScreenDTO> apiStatus = new ApiStatus<UpdateOnScreenDTO>(HttpStatus.OK, "SUCCESS",
				"LIST OF MISMATCHES UPDATED FOR SAP PROVISION", response);
		return new ResponseEntity<ApiStatus<UpdateOnScreenDTO>>(apiStatus, HttpStatus.OK);
	}


}
