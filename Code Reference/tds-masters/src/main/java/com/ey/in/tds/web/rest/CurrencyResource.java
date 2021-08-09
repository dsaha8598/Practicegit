package com.ey.in.tds.web.rest;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.apache.commons.io.FilenameUtils;
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

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.Currency;
import com.ey.in.tds.common.domain.MasterBatchUpload;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.FileStorageException;
import com.ey.in.tds.core.exceptions.InvalidFileTypeException;
import com.ey.in.tds.core.security.AllowedMimeTypes;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.service.CurrencyService;
import com.microsoft.azure.storage.StorageException;

@RestController
@RequestMapping("/api/masters")
public class CurrencyResource {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private CurrencyService currencyService;

	/**
	 * This API for create currency record.
	 * 
	 * @param currency
	 * @param userName
	 * @return
	 */
	@PostMapping(value = "/currency", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<Currency>> createCurrency(@Valid @RequestBody Currency currency,
			@RequestHeader(value = "USER_NAME") String userName) {
		MultiTenantContext.setTenantId("master");
		Currency result = currencyService.saveCurrency(currency, userName);
		ApiStatus<Currency> apiStatus = new ApiStatus<Currency>(HttpStatus.CREATED, "SUCCESS", "CURRENCY IS CREATED",
				result);
		return new ResponseEntity<ApiStatus<Currency>>(apiStatus, HttpStatus.OK);

	}

	/**
	 * This API for get all currency records.
	 * 
	 * @return
	 */
	@GetMapping(value = "/currency")
	public ResponseEntity<ApiStatus<List<Currency>>> getAllCurrency() {
		MultiTenantContext.setTenantId("master");
		List<Currency> result = currencyService.getAllCurrency();
		ApiStatus<List<Currency>> apiStatus = new ApiStatus<List<Currency>>(HttpStatus.OK, "SUCCESS",
				" GET ALL CURRENCY RECORDS", result);
		return new ResponseEntity<ApiStatus<List<Currency>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for get by id currency record.
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/currency/{id}")
	public ResponseEntity<ApiStatus<Currency>> getByCurrencyId(@PathVariable Long id) {
		MultiTenantContext.setTenantId("master");
		Currency result = currencyService.getByCurrencyId(id);
		ApiStatus<Currency> apiStatus = new ApiStatus<Currency>(HttpStatus.OK, "SUCCESS",
				" GET CURRENCY RECORDS BASED ON ID", result);
		return new ResponseEntity<ApiStatus<Currency>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param date
	 * @return
	 * @throws ParseException
	 */
	@GetMapping(value = "/currency/by/{date}")
	public ResponseEntity<ApiStatus<List<Currency>>> getCurrencyDate(@PathVariable String date) throws ParseException {
		MultiTenantContext.setTenantId("master");
		List<Currency> result = currencyService
				.getCurrencyDate(new SimpleDateFormat(CommonUtil.TDS_DATE_FORMAT).parse(date));

		ApiStatus<List<Currency>> apiStatus = new ApiStatus<List<Currency>>(HttpStatus.OK, "SUCCESS",
				"CURRENCY RECORDS BY DATE", result);
		return new ResponseEntity<ApiStatus<List<Currency>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param file
	 * @param userName
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws InvalidFileTypeException
	 */
	@PostMapping(value = "/read/currency/import", consumes = { "multipart/form-data" })
	public ResponseEntity<ApiStatus<MasterBatchUpload>> uploadReceiptPdf(@RequestParam("file") MultipartFile file,
			@RequestHeader(value = "USER_NAME") String userName) throws IOException, ParseException,
			InvalidKeyException, URISyntaxException, StorageException, InvalidFileTypeException {
		MultiTenantContext.setTenantId("master");
		int assesssmentYear = CommonUtil.getAssessmentYear(null);
		int assessmentMonth = CommonUtil.getAssessmentMonthPlusOne(null);
		MasterBatchUpload response = null;
		String extention = FilenameUtils.getExtension(file.getOriginalFilename());
		if (file.isEmpty()) {
			throw new FileStorageException("Please select the file");
		} else if (!(extention.equalsIgnoreCase("pdf"))) {
			throw new InvalidFileTypeException("Please select pdf file");
		} else {
			String contentType = new Tika().detect(file.getInputStream());
			if (AllowedMimeTypes.PDF.getMimeType().equals(contentType)) {
				response = currencyService.saveFileData(file, assesssmentYear, assessmentMonth, userName);
				ApiStatus<MasterBatchUpload> apiStatus = new ApiStatus<MasterBatchUpload>(HttpStatus.OK, "SUCCESS",
						"Receipt data saved successfully", response);
				return new ResponseEntity<ApiStatus<MasterBatchUpload>>(apiStatus, HttpStatus.OK);
			} else {
				logger.warn("Unsupported file upload attempt by {}", userName);
				throw new CustomException("Invalid file contents, only xlsx files are allowed", HttpStatus.BAD_REQUEST);
			}

		}
	}
	
	/**
	 * 
	 * @param foreignCurrency
	 * @param currencyType
	 * @return
	 */
	@GetMapping(value = "/currency/calculation")
	public ResponseEntity<ApiStatus<BigDecimal>> getCurrencyAmountCalculation(
			@RequestParam("foreignCurrency") int foreignCurrency, @RequestParam("currencyType") String currencyType) {
		MultiTenantContext.setTenantId("master");
		BigDecimal amount = currencyService.getCurrencyAmountCalculation(foreignCurrency, currencyType);
		ApiStatus<BigDecimal> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", "NO ALERT", amount);
		return new ResponseEntity<ApiStatus<BigDecimal>>(apiStatus, HttpStatus.OK);
	}
	
	//Feign client
	@GetMapping(value = "/get/all/currencydata")
	public ResponseEntity<ApiStatus<List<Map<String, Object>>>> getAllCurrencyData() {
		MultiTenantContext.setTenantId("master");
		List<Map<String, Object>> result = currencyService.getAllCurrencyData();
		ApiStatus<List<Map<String, Object>>> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS",
				" GET ALL CURRENCY RECORDS", result);
		return new ResponseEntity<ApiStatus<List<Map<String, Object>>>>(apiStatus, HttpStatus.OK);
	}

}
