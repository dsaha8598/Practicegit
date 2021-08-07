package com.ey.in.tds.onboarding.web.rest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import com.ey.in.tds.common.dto.chartofaccounts.ChartOfAccountsDTO;
import com.ey.in.tds.common.ingestion.response.dto.BatchUploadResponseDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.ChartOfAccounts;
import com.ey.in.tds.common.onboarding.response.dto.ChartsOfAccountsResponseDTO;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.security.AllowedMimeTypes;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.onboarding.service.chartofaccounts.ChartOfAccountsService;
import com.microsoft.azure.storage.StorageException;

@RestController
@RequestMapping("/api/onboarding")
public class ChartOfAccountsResource {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ChartOfAccountsService chartOfAccountsService;

	/**
	 * 
	 * @param chartOfAccountsDTO
	 * @param deductorPan
	 * @param userName
	 * @return
	 */
	@PostMapping(value = "/chartofaccounts", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<ChartsOfAccountsResponseDTO>> createdChartOfAccounts(
			@RequestBody ChartOfAccountsDTO chartOfAccountsDTO, @RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestHeader(value = "USER_NAME") String userName) {

		ChartOfAccounts chartOfAccounts = chartOfAccountsService.createdChartOfAccounts(chartOfAccountsDTO, deductorPan,
				userName);
		ChartsOfAccountsResponseDTO response = chartOfAccountsService.copyToResponse(chartOfAccounts);

		ApiStatus<ChartsOfAccountsResponseDTO> apiStatus = new ApiStatus<ChartsOfAccountsResponseDTO>(HttpStatus.OK,
				"SUCCESS", "Chart of accounts record created successfully", response);
		return new ResponseEntity<ApiStatus<ChartsOfAccountsResponseDTO>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
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
	@PostMapping("/chartofaccounts/upload/excel")
	public ResponseEntity<ApiStatus<BatchUploadResponseDTO>> createdChartOfAccounts(
			@PathVariable("file") MultipartFile file, @RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestParam("year") int assessmentYear, @RequestParam("type") String type,
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestHeader(value = "USER_NAME") String userName, @RequestParam("batchId") Integer batchId)
			throws InvalidKeyException, InvalidFormatException, IOException, URISyntaxException, StorageException {
		String contentType = new Tika().detect(file.getInputStream());
		if (AllowedMimeTypes.XLSX.getMimeType().equals(contentType)
				|| AllowedMimeTypes.OOXML.getMimeType().equals(contentType)) {
			BatchUploadResponseDTO chartOfAccounts = chartOfAccountsService.createChartOfAccounts(file, deductorPan, tan, tenantId,
					batchId, userName, assessmentYear);

			ApiStatus<BatchUploadResponseDTO> apiStatus = new ApiStatus<BatchUploadResponseDTO>(HttpStatus.OK, "SUCCESS",
					"Chart of accounts record created successfully", chartOfAccounts);
			return new ResponseEntity<ApiStatus<BatchUploadResponseDTO>>(apiStatus, HttpStatus.OK);
		} else {
			logger.warn("Unsupported file upload attempt by {}", userName);
			throw new CustomException("Invalid file contents, only xlsx files are allowed", HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * 
	 * @param deductorPan
	 * @return
	 */
	@GetMapping("/chartofaccounts")
	public ResponseEntity<ApiStatus<List<ChartsOfAccountsResponseDTO>>> getChartOfAccounts(
			@RequestHeader("DEDUCTOR-PAN") String deductorPan) {

		List<ChartOfAccounts> listChartOfAccounts = chartOfAccountsService.getAllChartOfAccounts(deductorPan);
		List<ChartsOfAccountsResponseDTO> listResponse = chartOfAccountsService.copyToListResponse(listChartOfAccounts);

		ApiStatus<List<ChartsOfAccountsResponseDTO>> apiStatus = new ApiStatus<List<ChartsOfAccountsResponseDTO>>(
				HttpStatus.OK, "SUCCESS", "Get all chart of accounts", listResponse);
		return new ResponseEntity<ApiStatus<List<ChartsOfAccountsResponseDTO>>>(apiStatus, HttpStatus.OK);
	}
}
