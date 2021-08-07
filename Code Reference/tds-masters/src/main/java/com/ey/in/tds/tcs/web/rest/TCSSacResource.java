package com.ey.in.tds.tcs.web.rest;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.transactions.jdbc.tcs.dto.TCSHsnSacDTO;
import com.ey.in.tds.common.dto.CommonDTO;
import com.ey.in.tds.common.dto.HSNRateAndNOIDetails;
import com.ey.in.tds.common.dto.sac.TcsHsnSacExelData;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.common.tcs.repository.TCSNatureOfIncomeRepository;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.security.AllowedMimeTypes;
import com.ey.in.tds.core.service.BaseResource;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.tcs.service.TCSSacService;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.micrometer.core.annotation.Timed;
/**
 * 
 * @author scriptbees.
 *
 */
@RestController
@RequestMapping("/api/masters/tcs")
public class TCSSacResource extends BaseResource {
	
	public static final String CLASS_NAME = "SacResource";

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TCSSacService newSacService;
	
	@Autowired
	private TCSNatureOfIncomeRepository tcsNatureOfIncomeRepository;

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
	@PostMapping("/sac/upload/excel")
	public ResponseEntity<ApiStatus<Boolean>> createdServicesAccountingCode(@RequestParam("file") MultipartFile file,
			@RequestHeader(value = "TAN-NUMBER") String deductorTan,
			@RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @RequestHeader(value = "USER_NAME") String userName)
			throws  IOException{
		MultiTenantContext.setTenantId("master");
		info("Entering: " + CLASS_NAME + " Method: upload SAC Excel with user name : {} ", userName);
		boolean uploadedData = false;
		String contentType = new Tika().detect(file.getInputStream());
		if (AllowedMimeTypes.XLSX.getMimeType().equals(contentType)
				|| AllowedMimeTypes.OOXML.getMimeType().equals(contentType)
				|| AllowedMimeTypes.APPLICATION.getMimeType().equals(contentType)) {
			MultiTenantContext.setTenantId("master");
			logger.info("Testing Something in SAC Upload Excel");
			logger.info("TAN-NUMBER: {}", deductorTan);
			try {
				uploadedData = newSacService.saveFileData(file, deductorTan, userName, tenantId,
						deductorPan);
			} catch (InterruptedException e) {
				logger.error("Unable to process the file", e);
				Thread.currentThread().interrupt();
			} catch (ExecutionException e) {
				throw new CustomException("Unable to process the file", HttpStatus.INTERNAL_SERVER_ERROR);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			logger.warn("Unsupported file upload attempt by {}", userName);
			throw new CustomException("Invalid file, only Excel files are allowed", HttpStatus.BAD_REQUEST);
		}
		info("Exiting from: " + CLASS_NAME + " Method: upload SAC Excel user name : {} ", userName);
		ApiStatus<Boolean> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS",
				"UPLOADED SAC FILE SUCCESSFULLY ", uploadedData);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);

	}
	/**
	 * This api for get redis data.
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/sac")
	public ResponseEntity<ApiStatus<List<TcsHsnSacExelData>>> getSac() throws Exception {
		MultiTenantContext.setTenantId("master");
		List<TcsHsnSacExelData> sacs = newSacService.getAllSacs();
		ApiStatus<List<TcsHsnSacExelData>> apiStatus = new ApiStatus<List<TcsHsnSacExelData>>(HttpStatus.OK,
				"SUCCESS", "Get all SAC", sacs);
		return new ResponseEntity<ApiStatus<List<TcsHsnSacExelData>>>(apiStatus, HttpStatus.OK);

	}
	
	/**
	 * This api for get hsn code data. 
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/hsn")
	public ResponseEntity<ApiStatus<CommonDTO<TCSHsnSacDTO>>> getHsnSac(@RequestBody Pagination pagination) throws Exception {
		MultiTenantContext.setTenantId("master");
		CommonDTO<TCSHsnSacDTO> sacs = newSacService.getAllHsnSac(pagination);
		
		ApiStatus<CommonDTO<TCSHsnSacDTO>> apiStatus = new ApiStatus<CommonDTO<TCSHsnSacDTO>>(HttpStatus.OK,
				"SUCCESS", "NO ALERT", sacs);
		return new ResponseEntity<ApiStatus<CommonDTO<TCSHsnSacDTO>>>(apiStatus, HttpStatus.OK);

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
	public ResponseEntity<ApiStatus<List<HSNRateAndNOIDetails>>> fetchByHSN(@PathVariable Long hsn)
			throws JsonParseException, JsonMappingException, IOException {
		// We are setting for the Master
		MultiTenantContext.setTenantId("master");
		logger.info("Master tenant details {}", MultiTenantContext.getTenantId());
		List<HSNRateAndNOIDetails> hasRateDetails = tcsNatureOfIncomeRepository.findHSNRateDetails(hsn);
		ApiStatus<List<HSNRateAndNOIDetails>> apiStatus = new ApiStatus<List<HSNRateAndNOIDetails>>(HttpStatus.OK,
				"To get List of HSN Records based on HSN", "List of HSN Rates associated with HSN", hasRateDetails);
		return new ResponseEntity<ApiStatus<List<HSNRateAndNOIDetails>>>(apiStatus, HttpStatus.OK);
	}

}
