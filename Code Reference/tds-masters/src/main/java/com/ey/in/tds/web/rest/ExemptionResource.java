package com.ey.in.tds.web.rest;

import java.util.Calendar;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.MasterBatchUpload;
import com.ey.in.tds.common.dto.ExemptionDTO;
import com.ey.in.tds.core.exceptions.FileStorageException;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.service.ExemptionService;

/**
 * this class is responsible for the API to deal with the exemption list
 * 
 * @author dipak
 *
 */
@CrossOrigin("*")
@RestController
@RequestMapping("/api/masters/")
public class ExemptionResource {

	private final Logger logger = LoggerFactory.getLogger(ExemptionResource.class);

	@Autowired
	private ExemptionService exemptionService;

	/**
	 * responsible to retrieve all exemption list
	 * 
	 * @return
	 */
	@PostMapping("all/exemption")
	public ResponseEntity<ApiStatus<List<ExemptionDTO>>> getAllExemption() {
		MultiTenantContext.setTenantId("master");
		logger.info("Controller method executing to retrieve all exemption list {}");
		List<ExemptionDTO> list = exemptionService.getAllExemption();
		logger.info("Retrieved Exemption List Sucessfully {}");
		ApiStatus<List<ExemptionDTO>> apiStatus = new ApiStatus<List<ExemptionDTO>>(HttpStatus.OK, "SUCCESS",
				"NO ALERT", list);
		return new ResponseEntity<ApiStatus<List<ExemptionDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * to upload and process the file for divident rate act
	 * 
	 * @param file
	 * @param userName
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "exemptionList/upload/excel")
	public ResponseEntity<ApiStatus<MasterBatchUpload>> uploadDividendRateActsExcel(
			@RequestParam("file") MultipartFile file, @RequestHeader(value = "X-USER-EMAIL") String userName,
			@RequestParam("year") Integer assesssmentYear, @RequestParam("type") String uploadType) throws Exception {
		
		MultiTenantContext.setTenantId("master");
		Integer assessmentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
		
		if (file.isEmpty()) {
			throw new FileStorageException("Please select the file");
		} else if (!FilenameUtils.getExtension(file.getOriginalFilename()).equalsIgnoreCase("xlsx")) {
			throw new FileStorageException("Cannot upload this file (" + file.getOriginalFilename()
					+ ") as this type of file is not accepted");
		} else {
			MasterBatchUpload fileUpload = exemptionService.saveFileData(file, assesssmentYear, assessmentMonth,
					userName, uploadType);
			ApiStatus<MasterBatchUpload> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS",
					"UPLOADED DIVIDEND RATE ACTS FILE SUCCESSFULLY ", fileUpload);
			
			return new ResponseEntity<ApiStatus<MasterBatchUpload>>(apiStatus, HttpStatus.OK);
		}
	}
}
