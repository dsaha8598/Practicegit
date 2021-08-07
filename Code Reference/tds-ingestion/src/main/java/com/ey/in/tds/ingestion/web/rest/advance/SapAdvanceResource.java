package com.ey.in.tds.ingestion.web.rest.advance;

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

import com.ey.in.tds.common.domain.transactions.jdbc.dto.AdvanceDTO;
import com.ey.in.tds.common.model.advance.AdvanceMismatchByBatchIdDTO;
import com.ey.in.tds.common.repository.common.PagedData;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.core.exceptions.TanNotFoundException;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.ingestion.dto.invoice.UpdateOnScreenDTO;
import com.ey.in.tds.ingestion.service.advance.SapAdvanceService;

@RestController
@RequestMapping("/api/ingestion")
public class SapAdvanceResource {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private SapAdvanceService sapAdvanceService;
	
	
	/**
	 * Update mismatch table based on Mismatch category for Excel
	 * 
	 * @param mismatchcategory
	 * @param advanceMismatchUpdateDTO
	 * @return
	 * @throws RecordNotFoundException
	 * @throws TanNotFoundException
	 */
	@PutMapping("sapadvance/{processedFrom}/{mismatchcategory}/action")
	public ResponseEntity<ApiStatus<UpdateOnScreenDTO>> uploadExcel(@RequestHeader(value = "TAN-NUMBER") String tan,
			@PathVariable String mismatchcategory, @RequestBody UpdateOnScreenDTO advanceMismatchUpdateDTO,
			@RequestHeader(value = "USER_NAME") String userName,
			@PathVariable String processedFrom) throws RecordNotFoundException, TanNotFoundException {
		// Handling null pointer exception
		if (tan == null && mismatchcategory == null) {
			throw new CustomException("Tan / Mismatch Category  Not Found", HttpStatus.BAD_REQUEST);
		}

		UpdateOnScreenDTO response = null;//TODO NEED TO CHANGE FOR SQL
				//sapAdvanceService.updateMismatchByAction(processedFrom,tan, advanceMismatchUpdateDTO, userName);

		logger.info("On Screen Update Object---: {} ", response);

		ApiStatus<UpdateOnScreenDTO> apiStatus = new ApiStatus<UpdateOnScreenDTO>(HttpStatus.OK, "SUCCESS",
				"LIST OF SAP MISMATCHES TO UPDATE", response);
		return new ResponseEntity<ApiStatus<UpdateOnScreenDTO>>(apiStatus, HttpStatus.OK);
	}

	
	/**
	 * 
	 * @param deductorMasterTan
	 * @param batchId
	 * @return
	 * @throws TanNotFoundException
	 */
	@GetMapping("/sapadvancemismatches/{processedFrom}/{batchId}")
	public ResponseEntity<ApiStatus<List<AdvanceMismatchByBatchIdDTO>>> advanceSAPMismatchSummaryByTanAndId(
			@RequestHeader(value = "TAN-NUMBER") String deductorMasterTan, @PathVariable UUID batchId,
			@PathVariable String processedFrom,
			@RequestBody Pagination pagination)
			throws TanNotFoundException {
		if (deductorMasterTan != null) {
			List<AdvanceMismatchByBatchIdDTO> advanceSAPMismatchSummaryTanAndIdList =null; //TODO NEED TO CHANGE FOR SQL
					//sapAdvanceService
					//.getSAPAdvanceMismatchSummaryByTanAndId(processedFrom ,deductorMasterTan, batchId,pagination);
			logger.info("SAP Advances in Mismatch Summary by BatchID : {}", advanceSAPMismatchSummaryTanAndIdList);
			logger.info("Batch Id : {}", batchId);

			ApiStatus<List<AdvanceMismatchByBatchIdDTO>> apiStatus = new ApiStatus<List<AdvanceMismatchByBatchIdDTO>>(
					HttpStatus.OK, "SUCCESS", "LIST OF SAP ADVANCE MISMATCH SUMMARY BASED ON BATCH ID",
					advanceSAPMismatchSummaryTanAndIdList);
			return new ResponseEntity<ApiStatus<List<AdvanceMismatchByBatchIdDTO>>>(apiStatus, HttpStatus.OK);

		} else {
			logger.error("Tan not Found");
			throw new CustomException("Tan Not Found", HttpStatus.BAD_REQUEST);
		}
	}


	@GetMapping(value = "/sapadvance/all/{processedFrom}/{year}/{month}")
	public ResponseEntity<ApiStatus<List<AdvanceMismatchByBatchIdDTO>>> advanceSAPMismatchByTanAndActive(
			@RequestHeader(value = "TAN-NUMBER") String deductorMasterTan, @PathVariable int year,
			@PathVariable int month,
			@PathVariable String processedFrom,
			@RequestBody Pagination pagination) throws TanNotFoundException {
		
		if (deductorMasterTan != null) {
			//TODO NEED TO CHANGE FOR SQL
			List<AdvanceMismatchByBatchIdDTO> advanceMismatchTanAndActivelist = null;
					//sapAdvanceService
					//.getAllSAPAdvanceMismatchSummaryByTanAndActive(processedFrom, deductorMasterTan, year, month,
					//		pagination);
			logger.info("SAP Advances in Mismatch Summary by BatchID : {}", advanceMismatchTanAndActivelist);

			ApiStatus<List<AdvanceMismatchByBatchIdDTO>> apiStatus = new ApiStatus<List<AdvanceMismatchByBatchIdDTO>>(
					HttpStatus.OK, "SUCCESS", "LIST OF SAP ADVANCE MISMATCH SUMMARY", advanceMismatchTanAndActivelist);
			return new ResponseEntity<ApiStatus<List<AdvanceMismatchByBatchIdDTO>>>(apiStatus, HttpStatus.OK);

		} else {
			logger.error("Tan not Found");
			throw new CustomException("Tan Not Found", HttpStatus.BAD_REQUEST);
		}
	}


	@GetMapping(value = "/sapadvancemismatches/{processedFrom}/{batchUploadId}/{mismatchCategory}")
	public ResponseEntity<ApiStatus<PagedData<AdvanceDTO>>> advanceSAPAllMismatchSummaryByTanAndId(
			@RequestHeader(value = "TAN-NUMBER") String deductorMasterTan, @PathVariable UUID batchUploadId,
			@PathVariable String mismatchCategory,@PathVariable String processedFrom,
			@RequestBody Pagination pagination) throws TanNotFoundException {
		 
		if (deductorMasterTan != null) {  //TODO NEED TO CHANGE FOR SQL
			/*/*		PagedData<AdvanceD> advanceAllMismatchSummaryTanAndIdList = sapAdvanceService
					.getSAPAdvanceMismatchByTanBatchIdMismatchCategory(processedFrom,deductorMasterTan, batchUploadId,
							mismatchCategory,pagination);
			logger.info("All Advances SAP in Mismatch Summary by BatchID : {}", advanceAllMismatchSummaryTanAndIdList);

			ApiStatus<PagedData<Advance>> apiStatus = new ApiStatus<PagedData<Advance>>(HttpStatus.OK, "SUCCESS",
					"LIST OF ADVANCE SAP MISMATCH SUMMARY BASED ON BATCH ID AND CATEGORY",
					advanceAllMismatchSummaryTanAndIdList);
			return new ResponseEntity<ApiStatus<PagedData<Advance>>>(apiStatus, HttpStatus.OK); */
		return null;

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
	 */
	@GetMapping(value = "/sapadvance/all/{processedFrom}/{mismatchCategory}/{year}/{month}")
	public ResponseEntity<ApiStatus<PagedData<AdvanceDTO>>> advanceSAPAllMismatchByTanAndActive(
			@RequestHeader(value = "TAN-NUMBER") String deductorMasterTan, @PathVariable String mismatchCategory,
			@PathVariable int year, @PathVariable int month,
			@PathVariable String processedFrom,
			@RequestBody Pagination pagination) throws TanNotFoundException {
		
		logger.info("Entered to get all mismatch category");
		if (deductorMasterTan == null) {

			logger.error("Tan not Found");
			throw new CustomException("Tan Not Found", HttpStatus.BAD_REQUEST);
		}
	/*	PagedData<Advance> advanceSAPAllMismatchTanAndActivelist = sapAdvanceService
				.getAllSAPAdvanceMismatchByTanAndMismatchCategory(processedFrom,deductorMasterTan, mismatchCategory, year, month,
						pagination);
		logger.info("All Advances SAP in Mismatch by BatchID : {}", advanceSAPAllMismatchTanAndActivelist);

		ApiStatus<PagedData<Advance>> apiStatus = new ApiStatus<PagedData<Advance>>(HttpStatus.OK, "SUCCESS",
				"LIST OF ADVANCE SAP MISMATCH SUMMARY BASED ON CATEGORY", advanceSAPAllMismatchTanAndActivelist);
		return new ResponseEntity<ApiStatus<PagedData<Advance>>>(apiStatus, HttpStatus.OK);  */
       return null;
	}

}
