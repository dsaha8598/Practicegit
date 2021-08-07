package com.ey.in.tds.onboarding.tcs.web.rest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
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
import com.ey.in.tds.common.domain.tcs.TCSBatchUpload;
import com.ey.in.tds.common.onboarding.response.dto.ChartsOfAccountsResponseDTO;
import com.ey.in.tds.common.onboarding.tcs.jdbc.dto.ChartOfAccountsTCS;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.security.AllowedMimeTypes;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.onboarding.tcs.service.chartofaccounts.ChartOfAccountServiceTCS;
import com.microsoft.azure.storage.StorageException;


/**
 * contains logic for GL
 * @author Dipak Saha
 *
 */
@RestController
@RequestMapping("/api/onboarding")
@CrossOrigin("*")
public class ChartOfAccountsResourcseTCS {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private ChartOfAccountServiceTCS chartOfAccountServiceTCS;
	/**
	 * reading and saving GL excel file
	 * @param file
	 * @param tan
	 * @param assessmentYear
	 * @param type
	 * @param tenantId
	 * @param deductorPan
	 * @param userName
	 * @param batchId
	 * @return
	 * @throws InvalidKeyException
	 * @throws InvalidFormatException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws StorageException
	 */
	
	@PostMapping("/tcs/chartofaccounts/upload/excel")
	public ResponseEntity<ApiStatus<TCSBatchUpload>> tcsCreatedChartOfAccounts(@RequestParam("files") MultipartFile file,
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestParam("year") int assessmentYear,
			@RequestParam("type") String type, @RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @RequestHeader(value = "USER_NAME") String userName)
			throws InvalidKeyException, InvalidFormatException, IOException, URISyntaxException, StorageException {
		MultiTenantContext.setTenantId(tenantId);
		logger.info("API execution started to process the GL excel file {}" );
		String contentType = new Tika().detect(file.getInputStream());
		if (AllowedMimeTypes.XLSX.getMimeType().equals(contentType)
				|| AllowedMimeTypes.OOXML.getMimeType().equals(contentType)) {
			TCSBatchUpload chartOfAccounts = chartOfAccountServiceTCS.createChartOfAccounts(file, deductorPan, tan, tenantId,
					 userName, assessmentYear);
           logger.info("File processed sucessFully {}"+chartOfAccounts);
			ApiStatus<TCSBatchUpload> apiStatus = new ApiStatus<TCSBatchUpload>(HttpStatus.OK, "SUCCESS",
					"Chart of accounts record created successfully", chartOfAccounts);
			return new ResponseEntity<ApiStatus<TCSBatchUpload>>(apiStatus, HttpStatus.OK);
		} else {
			logger.warn("Unsupported file upload attempt by {}", userName);
			throw new CustomException("Invalid file contents, only xlsx files are allowed", HttpStatus.BAD_REQUEST);
		}
	}
	/**
	 * retrieving all gl records
	 * @param deductorPan
	 * @return
	 */
	@GetMapping("tcs/chartofaccounts")
	public ResponseEntity<ApiStatus<List<ChartsOfAccountsResponseDTO>>> getChartOfAccounts(
			@RequestHeader("DEDUCTOR-PAN") String deductorPan,@RequestHeader(value = "X-TENANT-ID") String tenantId) {
		MultiTenantContext.setTenantId(tenantId);
		List<ChartOfAccountsTCS> listChartOfAccounts = chartOfAccountServiceTCS
				.getAllChartOfAccounts(deductorPan);
		List<ChartsOfAccountsResponseDTO> listResponse=new ArrayList<>();
        for(ChartOfAccountsTCS dto:listChartOfAccounts)	{
        	ChartsOfAccountsResponseDTO response=new ChartsOfAccountsResponseDTO();
        	response.setAccountCode(dto.getAccountCode());
        	response.setPan(dto.getPan());
        	BeanUtils.copyProperties(dto, response);
        	listResponse.add(response);
        }

		ApiStatus<List<ChartsOfAccountsResponseDTO>> apiStatus = new ApiStatus<List<ChartsOfAccountsResponseDTO>>(HttpStatus.OK, "SUCCESS",
				"Get all chart of accounts", listResponse);
		return new ResponseEntity<ApiStatus<List<ChartsOfAccountsResponseDTO>>>(apiStatus, HttpStatus.OK);
	}
}
