package com.ey.in.tds.web.rest;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.MasterBatchUpload;
import com.ey.in.tds.common.domain.dividend.DividendRateAct;
import com.ey.in.tds.common.domain.dividend.DividendRateTreaty;
import com.ey.in.tds.common.dto.DividendRateActDTO;
import com.ey.in.tds.common.dto.DividendRateTreatyDTO;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.FileStorageException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.service.DividendRateService;

import io.swagger.annotations.ApiParam;

/**
 * 
 * @author dipak
 *
 */
@CrossOrigin("*")
@RestController
@RequestMapping("/api/masters")
public class DividendRateResource {

	@Autowired
	private DividendRateService dividendRateService;


	@PostMapping(value = "/dividendRateActs", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<Long>> createDividendRateAct(@RequestHeader("X-USER-EMAIL") String userName,
			@Valid @RequestBody DividendRateActDTO dividendRateActDTO) {

		MultiTenantContext.setTenantId("master");
		if (dividendRateActDTO.getApplicableTo() != null
				&& (dividendRateActDTO.getApplicableFrom().equals(dividendRateActDTO.getApplicableTo())
						|| dividendRateActDTO.getApplicableFrom().isAfter(dividendRateActDTO.getApplicableTo()))) {
			throw new CustomException("From Date should not be equals or greater than To Date", HttpStatus.BAD_REQUEST);
		}

		DividendRateAct result = dividendRateService.createDividendRateAct(dividendRateActDTO, userName);
		ApiStatus<Long> apiStatus = new ApiStatus<>(HttpStatus.CREATED, "To create a Rate Master Act Record",
				"Rate Master Act Record Created ", result.getId());
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}
	
	@GetMapping(value = "/dividendRateActs/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<DividendRateAct>> getDividendRateActById(
			@ApiParam(value = "Dividend Rate Act id", example = "132", required = true) @PathVariable(name = "id", required = true) @NotNull final Long id) {
		MultiTenantContext.setTenantId("master");
		return this.dividendRateService.getDividendRateActById(id).map(dividendRateAct -> {
			ApiStatus<DividendRateAct> apiStatus = new ApiStatus<>(HttpStatus.OK,
					"To get a Dividend Rate Act Record by Id", "A Dividend Rate Act record", dividendRateAct);
			return new ResponseEntity<>(apiStatus, HttpStatus.OK);
		}).orElse(new ResponseEntity<>(new ApiStatus<>(HttpStatus.NOT_FOUND, "To get a Dividend Rate Act Record by Id",
				"Dividend Rate Act not found for Id: " + id, null), HttpStatus.NOT_FOUND));
	}

	@GetMapping(value = "/dividendRateActs", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<DividendRateAct>>> getAllDividendRateActs() {
		MultiTenantContext.setTenantId("master");
		ApiStatus<List<DividendRateAct>> apiStatus = new ApiStatus<>(HttpStatus.OK,
				"To get List of Dividend Rate Act Records", "List of Rate Master Act Records",
				dividendRateService.getAllDividendRateActs());
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}
	
	@PutMapping(value = "/dividendRateActs/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<DividendRateAct>> updateDividendRateActEndDate(
			@RequestHeader("X-USER-EMAIL") String userName,
			@PathVariable(name = "id", required = true) @NotNull final Long id,
			@RequestParam(value = "applicableTo", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") @NotNull final LocalDate applicableTo)
			throws RecordNotFoundException {
		MultiTenantContext.setTenantId("master");
		DividendRateAct dividendRateAct = dividendRateService.updateDividendRateActEndDate(id,
				applicableTo.atStartOfDay(ZoneId.systemDefault()).toInstant(), userName);
		ApiStatus<DividendRateAct> apiStatus = new ApiStatus<>(HttpStatus.OK, "To update a Dividend Rate Act Record",
				"Dividend Rate Act Record updated", dividendRateAct);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}

	@PostMapping(value = "/dividendRateTreaties", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<Long>> createDividendRateTreaty(@RequestHeader("X-USER-EMAIL") String userName,
			@RequestBody DividendRateTreatyDTO dividendRateTreatyDTO) {
		MultiTenantContext.setTenantId("master");
		if (dividendRateTreatyDTO.getApplicableTo() != null && (dividendRateTreatyDTO.getApplicableFrom()
				.equals(dividendRateTreatyDTO.getApplicableTo())
				|| dividendRateTreatyDTO.getApplicableFrom().isAfter(dividendRateTreatyDTO.getApplicableTo()))) {
			throw new CustomException("From Date should not be equals or greater than To Date", HttpStatus.BAD_REQUEST);
		}

		DividendRateTreaty result = dividendRateService.createDividendRateTreaty(dividendRateTreatyDTO, userName);
		ApiStatus<Long> apiStatus = new ApiStatus<Long>(HttpStatus.CREATED, "To create a Rate Master Act Record",
				"Rate Master Act Record Created ", result.getId());
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}

	@GetMapping(value = "/dividendRateTreaties/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<DividendRateTreaty>> getDividendRateTreatyById(
			@ApiParam(value = "Dividend Rate Treaty id", example = "132", required = true) @PathVariable(name = "id", required = true) @NotNull final Long id) {
		return this.dividendRateService.getDividendRateTreatyById(id).map(dividendRateTreaty -> {
			MultiTenantContext.setTenantId("master");
			ApiStatus<DividendRateTreaty> apiStatus = new ApiStatus<>(HttpStatus.OK,
					"To get a Dividend Rate Treaty Record by Id", "A Dividend Rate Treaty record", dividendRateTreaty);
			return new ResponseEntity<>(apiStatus, HttpStatus.OK);
		}).orElse(
				new ResponseEntity<>(new ApiStatus<>(HttpStatus.NOT_FOUND, "To get a Dividend Rate Treaty Record by Id",
						"Dividend Rate Treaty not found for Id: " + id, null), HttpStatus.NOT_FOUND));
	}

	@GetMapping(value = "/dividendRateTreaties", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<DividendRateTreaty>>> getAllDividendRateTreaties() {
		MultiTenantContext.setTenantId("master");
		ApiStatus<List<DividendRateTreaty>> apiStatus = new ApiStatus<>(HttpStatus.OK,
				"To get List of Dividend Rate Treaty Records", "List of Dividend Rate Treaty Records",
				dividendRateService.getAllDividendRateTreaties());
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}

	@PutMapping(value = "/dividendRateTreaties/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<DividendRateTreaty>> updateDividendRateTreatyEndDate(
			@RequestHeader("X-USER-EMAIL") String userName,
			@PathVariable(name = "id", required = true) @NotNull final Long id,
			@RequestParam(value = "applicableTo", required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") @NotNull final LocalDate applicableTo)
			throws RecordNotFoundException {
		MultiTenantContext.setTenantId("master");
		DividendRateTreaty dividendRateTreaty = dividendRateService.updateDividendRateTreatyEndDate(id,
				applicableTo.atStartOfDay(ZoneId.systemDefault()).toInstant(), userName);
		ApiStatus<DividendRateTreaty> apiStatus = new ApiStatus<>(HttpStatus.OK, "To update a Dividend Rate Act Record",
				"Dividend Rate Act Record updated", dividendRateTreaty);
		return new ResponseEntity<>(apiStatus, HttpStatus.OK);
	}

	/**
	 * to upload and process the file for divident rate act
	 * @param file
	 * @param userName
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "dividendRateActs/upload/excel")
	public ResponseEntity<ApiStatus<MasterBatchUpload>> uploadDividendRateActsExcel(
			@RequestParam("file") MultipartFile file, @RequestHeader(value = "X-USER-EMAIL") String userName)
			throws Exception {
		MultiTenantContext.setTenantId("master");
		Integer assesssmentYear = Calendar.getInstance().get(Calendar.YEAR)+1;
		Integer assessmentMonth = Calendar.getInstance().get(Calendar.MONTH)+1;
		if (file.isEmpty()) {
			throw new FileStorageException("Please select the file");
		} else if (!FilenameUtils.getExtension(file.getOriginalFilename()).equalsIgnoreCase("xlsx")) {
			throw new FileStorageException("Cannot upload this file (" + file.getOriginalFilename()
					+ ") as this type of file is not accepted");
		} else {
			MasterBatchUpload fileUpload = dividendRateService.saveFileData(file, assesssmentYear, assessmentMonth, userName);
			ApiStatus<MasterBatchUpload> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS",
					"UPLOADED DIVIDEND RATE ACTS FILE SUCCESSFULLY ", fileUpload);
			return new ResponseEntity<ApiStatus<MasterBatchUpload>>(apiStatus, HttpStatus.OK);
		}
	}

	@PostMapping(value = "dividendRateTreaties/upload/excel")
	public ResponseEntity<ApiStatus<MasterBatchUpload>> uploadDividendRateTreatiesExcel(
			@RequestParam("file") MultipartFile file, @RequestHeader(value = "X-USER-EMAIL") String userName)
			throws Exception {
		MultiTenantContext.setTenantId("master");
		Integer assesssmentYear = Calendar.getInstance().get(Calendar.YEAR)+1;
		Integer assessmentMonth = Calendar.getInstance().get(Calendar.MONTH)+1;
		if (file.isEmpty()) {
			throw new FileStorageException("Please select the file");
		} else if (!FilenameUtils.getExtension(file.getOriginalFilename()).equalsIgnoreCase("xlsx")) {
			throw new FileStorageException("Cannot upload this file (" + file.getOriginalFilename()
					+ ") as this type of file is not accepted");
		} else {
			MasterBatchUpload fileUpload =dividendRateService.saveDividendRateTreatyFileData(file, assesssmentYear, assessmentMonth, userName);
			ApiStatus<MasterBatchUpload> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS",
					"UPLOADED DIVIDEND RATE TREATIES FILE SUCCESSFULLY ", fileUpload);
			return new ResponseEntity<ApiStatus<MasterBatchUpload>>(apiStatus, HttpStatus.OK);
		}
	}

	/*@PostMapping(value = "/downloadFile/{id}")
	public ResponseEntity<ByteArrayResource> downloadFile(
			@PathVariable(name = "id", required = true) @NotNull final Long id,
			@RequestParam(value = "downloadType", required = true) FileUpload.DownlloadType downloadType) {
		return this.dividendRateFileProcessor.getFileById(id).map(dumpedFile -> ResponseEntity.ok()
				.contentType(MediaType.parseMediaType("application/ms-excel"))
				.header(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=\""
								+ (downloadType == DownlloadType.SOURCE_FILE ? dumpedFile.getSourceFileName()
										: dumpedFile.getErrorFileName())
								+ "\"")
				.body(new ByteArrayResource(downloadType == DownlloadType.SOURCE_FILE ? dumpedFile.getSourceFileData()
						: dumpedFile.getErrorFileData())))
				.orElse(ResponseEntity.notFound().build());
	}*/

}
