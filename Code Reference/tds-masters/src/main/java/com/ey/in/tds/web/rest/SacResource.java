package com.ey.in.tds.web.rest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.text.ParseException;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.MasterBatchUpload;
import com.ey.in.tds.common.dto.sac.ServicesAccountingCode;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.InvalidFileTypeException;
import com.ey.in.tds.core.security.AllowedMimeTypes;
import com.ey.in.tds.core.service.BaseResource;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.service.SacService;
import com.microsoft.azure.storage.StorageException;

@RestController
@RequestMapping("/api/masters")
public class SacResource extends BaseResource {
	public static final String CLASS_NAME = "SacResource";

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private SacService newSacService;

	/**
	 * 
	 * @param file
	 * @param deductorTan
	 * @param assessmentYear
	 * @param type
	 * @param tenantId
	 * @param deductorPan
	 * @param userName
	 * @return
	 * @throws InvalidKeyException
	 * @throws InvalidFormatException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws ParseException
	 * @throws InvalidFileTypeException
	 */
	@PostMapping("/sac/upload/excel")
	public ResponseEntity<ApiStatus<MasterBatchUpload>> createdServicesAccountingCode(
			@RequestParam("file") MultipartFile file, @RequestHeader(value = "TAN-NUMBER") String deductorTan,
			@RequestParam("year") int assessmentYear, @RequestParam("type") String type,
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestHeader(value = "USER_NAME") String userName) throws InvalidKeyException, InvalidFormatException,
			IOException, URISyntaxException, StorageException, ParseException, InvalidFileTypeException {
		MultiTenantContext.setTenantId("master");
		info("Entering: " + CLASS_NAME + " Method: upload SAC Excel with user name : {} ", userName);
		MasterBatchUpload masterBatchUpload = null;
		int assessmentMonth = CommonUtil.getAssessmentMonthPlusOne(null);
		String contentType = new Tika().detect(file.getInputStream());

		if (AllowedMimeTypes.XLSX.getMimeType().equals(contentType)
				|| AllowedMimeTypes.OOXML.getMimeType().equals(contentType)) {
			logger.info("Testing Something in SAC Upload Excel");
			logger.info("TAN-NUMBER: {}", deductorTan);
			try {
				masterBatchUpload = newSacService.saveFileData(file, assessmentYear, assessmentMonth, userName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			logger.warn("Unsupported file upload attempt by {}", userName);
			throw new CustomException("Invalid file, only Excel files are allowed", HttpStatus.BAD_REQUEST);
		}
		info("Exiting from: " + CLASS_NAME + " Method: upload SAC Excel user name : {} ", userName);
		ApiStatus<MasterBatchUpload> apiStatus = new ApiStatus<MasterBatchUpload>(HttpStatus.OK, "SUCCESS",
				"UPLOADED COLLECTEE FILE SUCCESSFULLY ", masterBatchUpload);
		return new ResponseEntity<ApiStatus<MasterBatchUpload>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/sac")
	public ResponseEntity<ApiStatus<List<ServicesAccountingCode>>> getSac() throws Exception {
		MultiTenantContext.setTenantId("master");
		List<ServicesAccountingCode> Sacs = newSacService.getAllSacs();
		ApiStatus<List<ServicesAccountingCode>> apiStatus = new ApiStatus<List<ServicesAccountingCode>>(HttpStatus.OK,
				"SUCCESS", "Get all SAC", Sacs);
		return new ResponseEntity<ApiStatus<List<ServicesAccountingCode>>>(apiStatus, HttpStatus.OK);
	}

}
