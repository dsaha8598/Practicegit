package com.ey.in.tds.returns.web.rest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.lf5.util.StreamUtils;
import org.apache.poi.util.IOUtils;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

import com.aspose.cells.Cell;
import com.aspose.cells.Cells;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.FilingDeductorCollector;
import com.ey.in.tds.common.domain.FilingMinistryCode;
import com.ey.in.tds.common.domain.FilingMinorHeadCode;
import com.ey.in.tds.common.domain.FilingSectionCode;
import com.ey.in.tds.common.domain.FilingStateCode;
import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterDTO;
import com.ey.in.tds.common.dto.onboarding.deductee.DeducteeMasterDTO;
import com.ey.in.tds.common.dto.returns.ChallanReceiptDTO;
import com.ey.in.tds.common.dto.returns.FilingFilesDTO;
import com.ey.in.tds.common.jdbc.returns.dao.FilingFilesDAO;
import com.ey.in.tds.common.resturns.response.dto.FilingFilesResponseDTO;
import com.ey.in.tds.common.resturns.response.dto.FilingStatusResponseDTO;
import com.ey.in.tds.common.returns.jdbc.dto.FilingFiles;
import com.ey.in.tds.common.returns.jdbc.dto.FilingStatus;
import com.ey.in.tds.common.service.BlobStorageService;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.InvalidFileTypeException;
import com.ey.in.tds.core.security.AllowedMimeTypes;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.ReturnType;
import com.ey.in.tds.returns.services.FilingFileGenerationService;
import com.ey.in.tds.returns.services.RPUFileReadingService;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.microsoft.azure.storage.StorageException;

/**
 * @author Sumit Agrawal
 *
 */
@RestController
@RequestMapping("/api/returns")
public class FilingFileGenerationController {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private FilingFileGenerationService filingFileGenerationService;

	@Autowired
	private RPUFileReadingService rpuFileReadingService;

	@Autowired
	private BlobStorageService blobStorageService;

	@Autowired
	private FilingFilesDAO filingFilesDAO;

	/**
	 * This api is for processing Excel and creating text file
	 * 
	 * @param multipartFile
	 * @param formType
	 * @param quarter
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/upload/rpusampledata", consumes = { "multipart/form-data" })
	public ResponseEntity<ApiStatus<String>> generateTDS27QFileFromExcel(
			@RequestHeader(value = "TAN-NUMBER", required = true) String tanNumber,
			@RequestHeader(value = "DEDUCTOR-PAN", required = true) String deductorPan,
			@RequestParam(value = "file", required = true) MultipartFile file,
			@RequestParam(value = "formType", required = true) String formType,
			@RequestParam(value = "quarter", required = true) String quarter,
			@RequestParam(value = "assessmentYear", required = true) Integer assessmentYear,
			@RequestParam(value = "isCorrection", required = false) boolean isCorrection,
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader(value = "USER_NAME") String userName)
			throws Exception {

		logger.info("Entered in generateTDS26QFileFromExcel()");
		String extention = FilenameUtils.getExtension(file.getOriginalFilename());
		if (!(extention.equalsIgnoreCase("xls") || extention.equalsIgnoreCase("xlsx"))) {
			throw new InvalidFileTypeException("Please select xls or xlsx");
		}
		String contentType = new Tika().detect(file.getInputStream());
		if (AllowedMimeTypes.XLSX.getMimeType().equals(contentType)
				|| AllowedMimeTypes.OOXML.getMimeType().equals(contentType)) {
			File xlsxFile = convert(file.getInputStream(), file.getOriginalFilename());

			if (isCorrection) {
				formType = formType + "-Correction";
			}

			rpuFileReadingService.generateFile(xlsxFile, file, formType, quarter, deductorPan, tanNumber,
					assessmentYear, tenantId, userName, formType, isCorrection);

			ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS",
					formType + " generation requested and will be available shortly", "");

			return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
		} else {
			logger.warn("Unsupported file upload attempt by {}", userName);
			throw new CustomException("Invalid file contents, only xlsx files are allowed", HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * This api returns the Filing status
	 * 
	 * @param assessmentYear
	 * @param deductorId
	 * @return
	 */
	@GetMapping(value = "/filing/{assessmentYear}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<FilingFilesDTO>>> getFilingFilesByYearAndDeductorId(
			@PathVariable("assessmentYear") Integer assessmentYear, @RequestHeader("TAN-NUMBER") String deductorTan) {

		List<FilingFilesDTO> filingStatus = filingFileGenerationService.findFilingByYear(assessmentYear, deductorTan);

		ApiStatus<List<FilingFilesDTO>> apiStatus = new ApiStatus<List<FilingFilesDTO>>(HttpStatus.OK, "SUCCESS",
				"Filing Files data", filingStatus);

		return new ResponseEntity<ApiStatus<List<FilingFilesDTO>>>(apiStatus, HttpStatus.OK);
	}

	@PostMapping(value = "/filingstatus/{assessmentYear}/{quarter}/{filingFormType}/{pnrOrTokenNumber}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<FilingStatusResponseDTO>> saveFilingStatus(
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader("TAN-NUMBER") String tanNumber,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@PathVariable("assessmentYear") Integer assessmentYear, @PathVariable("quarter") String quarter,
			@PathVariable("filingFormType") String filingFormType,
			@PathVariable("pnrOrTokenNumber") String pnrOrTokenNumber) {

		if (assessmentYear == null) {
			throw new IllegalArgumentException("Assessment Year cannot be null");
		}
		if (quarter == null) {
			throw new IllegalArgumentException("Quarter cannot be null");
		}
		if (pnrOrTokenNumber == null || StringUtils.isEmpty(pnrOrTokenNumber)) {
			throw new IllegalArgumentException("PNR number cannot be null");
		}
		if (pnrOrTokenNumber.length() != 15) {
			throw new IllegalArgumentException("PNR number should contain 15 characters");
		}
		String filingType = ReturnType.REGULAR.name();

		FilingStatus filingStatus = filingFileGenerationService.findByYearAndQuarter(assessmentYear, quarter, tanNumber,
				filingFormType);
		if (filingStatus != null) {
			filingStatus.setPnrOrTokenNumber(pnrOrTokenNumber);
			filingStatus.setStatus("GENERATED");
			filingStatus = filingFileGenerationService.updateFilingStatus(filingStatus);
		} else {
			filingStatus = filingFileGenerationService.saveFilingStatus(assessmentYear, quarter, deductorPan, tanNumber,
					filingFormType, tenantId, userName, pnrOrTokenNumber, filingType);
		}
		FilingStatusResponseDTO response = filingFileGenerationService.copyToResponseDTO(filingStatus);
		ApiStatus<FilingStatusResponseDTO> apiStatus = new ApiStatus<FilingStatusResponseDTO>(HttpStatus.OK, "SUCCESS",
				"updated filing status successfully", response);
		return new ResponseEntity<ApiStatus<FilingStatusResponseDTO>>(apiStatus, HttpStatus.OK);
	}

	@PutMapping(value = "/filingstatus/{assessmentYear}/{quarter}/{uuid}/{pnrOrTokenNumber}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<FilingStatusResponseDTO>> updateFilingStatus(
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader("TAN-NUMBER") String tanNumber,
			@PathVariable("assessmentYear") Integer assessmentYear, @PathVariable("quarter") String quarter,
			@PathVariable("uuid") Integer id, @PathVariable("pnrOrTokenNumber") String pnrOrTokenNumber) {

		if (assessmentYear == null) {
			throw new IllegalArgumentException("Assessment Year cannot be null");
		}
		if (quarter == null) {
			throw new IllegalArgumentException("Quarter cannot be null");
		}
		if (id == null) {
			throw new IllegalArgumentException("Id cannot be null");
		}
		// FilingStatus.Key filingStatusKey = new FilingStatus.Key(assessmentYear,
		// quarter, uuid);
		FilingStatus filingStatus = filingFileGenerationService.findById(id);
		filingStatus.setPnrOrTokenNumber(pnrOrTokenNumber);
		filingStatus = filingFileGenerationService.updateFilingStatus(filingStatus);
		FilingStatusResponseDTO response = filingFileGenerationService.copyToResponseDTO(filingStatus);
		ApiStatus<FilingStatusResponseDTO> apiStatus = new ApiStatus<FilingStatusResponseDTO>(HttpStatus.OK, "SUCCESS",
				"updated filing status successfully", response);
		return new ResponseEntity<ApiStatus<FilingStatusResponseDTO>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This api returns the filing files details
	 * 
	 * @param assessmentYear
	 * @param deductorId
	 * @param quarter
	 * @return
	 */
	@PostMapping(value = "/filingfiles", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<FilingFilesDTO>>> getFilingFiles(
			@RequestBody Map<String, String> requestParams, @RequestHeader("TAN-NUMBER") String deductorTan) {
		String assessmentYear = requestParams.get("year");
		String deductorPan = requestParams.get("deductorPan");
		String quarter = requestParams.get("quarter");
		Integer year = Integer.parseInt(assessmentYear);

		List<FilingFilesDTO> filingStatus = filingFileGenerationService.findFilingByYearDeductorIdQuarter(year,
				deductorPan, quarter, deductorTan, null);

		ApiStatus<List<FilingFilesDTO>> apiStatus = new ApiStatus<List<FilingFilesDTO>>(HttpStatus.OK, "SUCCESS",
				"NO ALERT", filingStatus);

		return new ResponseEntity<ApiStatus<List<FilingFilesDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * Create filing based on quarter
	 * 
	 * @param assessmentYear
	 * @return
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws InvalidKeyException
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	@PostMapping(value = "/create/filingRecord")
	public ResponseEntity<ApiStatus<String>> createReturnFilingReport(@RequestHeader("TAN-NUMBER") String tanNumber,
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @RequestParam("assessmentYear") int assessmentYear,
			@RequestParam(value = "formType", required = true) String formType, @RequestParam("quarter") String quarter,
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader(value = "USER_NAME") String userName,
			@RequestParam(value = "AGGRIGATED", required = false) Boolean isAggriated) throws InvalidKeyException,
			URISyntaxException, StorageException, JsonParseException, JsonMappingException, IOException {
		MultiTenantContext.setTenantId(tenantId);

		filingFileGenerationService.asyncCreateReturnFilingReport(formType, tanNumber, deductorPan, assessmentYear,
				quarter, tenantId, userName, isAggriated);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS",
				formType + " generation requested successfully", formType + " generation requested successfully");
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This api returns deductor details
	 * 
	 * @param token
	 * @param deductorId
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */

	@GetMapping(value = "/getdeductor", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<DeductorMasterDTO>> getDeductorData(
			@RequestHeader(value = "Authorization", required = true) String token,
			@RequestHeader(value = "DEDUCTOR-ID", required = true) Long deductorId,
			@RequestHeader("X-TENANT-ID") String tenantId) throws IOException {

		DeductorMasterDTO result = filingFileGenerationService.getDeductorData(deductorId, tenantId);

		ApiStatus<DeductorMasterDTO> apiStatus = new ApiStatus<DeductorMasterDTO>(HttpStatus.OK, "SUCCESS", "NO ALERT",
				result);
		return new ResponseEntity<ApiStatus<DeductorMasterDTO>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This api returns the challans data based on quarter
	 * 
	 * @param token
	 * @param assessmentYear
	 * @param quarter
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@GetMapping(value = "/getchallan/{assessmentYear}/{quarter}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<ChallanReceiptDTO>>> getChallanData(
			@RequestHeader("TAN-NUMBER") String tanNumber, @PathVariable int assessmentYear,
			@PathVariable String quarter, @RequestHeader("X-TENANT-ID") String tenantId) {

		List<ChallanReceiptDTO> result = filingFileGenerationService.getChallanData(assessmentYear, quarter, tenantId,
				tanNumber);

		ApiStatus<List<ChallanReceiptDTO>> apiStatus = new ApiStatus<List<ChallanReceiptDTO>>(HttpStatus.OK, "SUCCESS",
				"NO ALERT", result);
		return new ResponseEntity<ApiStatus<List<ChallanReceiptDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This api returns the deductee data based on tannumber
	 * 
	 * @param token
	 * @param tanNumber
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@GetMapping(value = "/getdeductee", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<DeducteeMasterDTO>>> getDeducteeData(
			@RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "TAN-NUMBER", required = true) String tanNumber) {

		List<DeducteeMasterDTO> result = filingFileGenerationService.getResidentDeducteeData(tanNumber, tenantId);

		ApiStatus<List<DeducteeMasterDTO>> apiStatus = new ApiStatus<List<DeducteeMasterDTO>>(HttpStatus.OK,
				"To get data for deductee master", "Deductee Data", result);
		return new ResponseEntity<ApiStatus<List<DeducteeMasterDTO>>>(apiStatus, HttpStatus.OK);
	}

	// Feign client for Filing testing api's

	@GetMapping(value = "/filing/deductorcollector")
	public ResponseEntity<ApiStatus<List<FilingDeductorCollector>>> getAlldeductorCollectorForReturns(
			@RequestHeader(value = "X-TENANT-ID", required = false) String tenantId) {
		List<FilingDeductorCollector> result = filingFileGenerationService.getAlldeductorCollector(tenantId);
		ApiStatus<List<FilingDeductorCollector>> apiStatus = new ApiStatus<List<FilingDeductorCollector>>(HttpStatus.OK,
				"To get data for Filing Deductor Collector", "Filing Deductor Collector Data", result);
		return new ResponseEntity<ApiStatus<List<FilingDeductorCollector>>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping(value = "/filing/ministrycode")
	public ResponseEntity<ApiStatus<List<FilingMinistryCode>>> getAllMinistryCode(

			@RequestHeader(value = "X-TENANT-ID", required = false) String tenantId) {
		List<FilingMinistryCode> result = filingFileGenerationService.getAllMinistryCode(tenantId);
		ApiStatus<List<FilingMinistryCode>> apiStatus = new ApiStatus<List<FilingMinistryCode>>(HttpStatus.OK,
				"To get data for Filing Ministry Code", "Filing Ministry Code Data", result);
		return new ResponseEntity<ApiStatus<List<FilingMinistryCode>>>(apiStatus, HttpStatus.OK);

	}

	@GetMapping(value = "/filing/minorheadcode")
	public ResponseEntity<ApiStatus<List<FilingMinorHeadCode>>> getAllMinorHeadCode(
			@RequestHeader(value = "X-TENANT-ID", required = false) String tenantId) {
		List<FilingMinorHeadCode> result = filingFileGenerationService.getAllMinorHeadCode(tenantId);
		ApiStatus<List<FilingMinorHeadCode>> apiStatus = new ApiStatus<List<FilingMinorHeadCode>>(HttpStatus.OK,
				"To get data for Filing Minor Head Code", "Filing Minor Head Code Data", result);
		return new ResponseEntity<ApiStatus<List<FilingMinorHeadCode>>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping(value = "/filing/sectioncode")
	public ResponseEntity<ApiStatus<List<FilingSectionCode>>> getAllSectionCode(
			@RequestHeader(value = "X-TENANT-ID", required = false) String tenantId) {
		List<FilingSectionCode> result = filingFileGenerationService.getAllSectionCode(tenantId);

		ApiStatus<List<FilingSectionCode>> apiStatus = new ApiStatus<List<FilingSectionCode>>(HttpStatus.OK,
				"To get data for Filing Section Code", "Filing Section Code Data", result);
		return new ResponseEntity<ApiStatus<List<FilingSectionCode>>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping(value = "/filing/statecode")
	public ResponseEntity<ApiStatus<List<FilingStateCode>>> getAllStateCode(
			@RequestHeader(value = "X-TENANT-ID", required = false) String tenantId) {
		List<FilingStateCode> result = filingFileGenerationService.getAllStateCode(tenantId);
		ApiStatus<List<FilingStateCode>> apiStatus = new ApiStatus<List<FilingStateCode>>(HttpStatus.OK,
				"To get data for Filing State Code", "Filing State Code Data", result);
		return new ResponseEntity<ApiStatus<List<FilingStateCode>>>(apiStatus, HttpStatus.OK);
	}

	@PostMapping(value = "/filing/download", consumes = { "application/x-www-form-urlencoded" })
	public ResponseEntity<Resource> downloadFile(@RequestParam(value = "tenantId", required = true) String tenantId,
			@RequestParam(value = "fileUrl", required = true) String fileUrl) throws Exception {

		HttpHeaders header = new HttpHeaders();
		try {
			logger.info("Blob url : {}", fileUrl);
			File file = blobStorageService.getFileFromBlobUrl(tenantId, fileUrl);
			Resource resource = new FileSystemResource(file);

			header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"");
			header.add("Cache-Control", "no-cache, no-store, must-revalidate");
			header.add("Pragma", "no-cache");
			header.add("Expires", "0");

			return ResponseEntity.ok().headers(header).contentLength(file.length())
					.contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);
		} catch (Exception e) {
			logger.error("Exception occured while fetching the file from blob", e);
			// Downloading exception in file
			Workbook workbook = new Workbook();
			Worksheet worksheet = workbook.getWorksheets().get(0);
			Cells cells = worksheet.getCells();
			worksheet.setName("Error Details");
			Cell cell = cells.get("A1");
			cell.setValue("Error Message");
			// Adding a double value to the cell
			cell = cells.get("A2");
			cell.setValue(e);
			// Write the output to the file
			ByteArrayOutputStream baout = new ByteArrayOutputStream();
			workbook.save(baout, SaveFormat.XLSX);
			File file = File.createTempFile("Error_File", ".xlsx");
			FileUtils.writeByteArrayToFile(file, baout.toByteArray());

			Resource resource = new FileSystemResource(file);

			header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"");
			header.add("Cache-Control", "no-cache, no-store, must-revalidate");
			header.add("Pragma", "no-cache");
			header.add("Expires", "0");

			return ResponseEntity.ok().headers(header).contentLength(file.length())
					.contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);
		}
	}

	@PostMapping(value = "/filing/download/zip", consumes = { "application/x-www-form-urlencoded" })
	public ResponseEntity<Resource> downloadFillingFiles(@RequestHeader("X-TENANT-ID") String tenantId,
			@RequestParam(value = "assessmentYear", required = true) Integer assessmentYear,
			@RequestHeader("TAN-NUMBER") String tanNumber,
			@RequestParam(value = "requestNumber", required = true) Integer requestNumber) throws Exception {

		HttpHeaders header = new HttpHeaders();
		try {

			List<FilingFiles> filingFiles = filingFilesDAO.findFilingByRequestNumber(assessmentYear, tanNumber,
					requestNumber);
			// generating zip file
			String zipFileName = "FilingFiles_" + UUID.randomUUID().toString().concat(".zip");
			FileOutputStream fos = new FileOutputStream(zipFileName);
			ZipOutputStream zos = new ZipOutputStream(fos);

			for (FilingFiles filingFile : filingFiles) {

				File file = blobStorageService.getFileFromBlobUrl(tenantId, filingFile.getFileUrl());

				logger.info("Blob url : {}", filingFile.getFileUrl());

				zos.putNextEntry(new ZipEntry(file.getName()));

				byte[] bytes = Files.readAllBytes(file.toPath());
				zos.write(bytes, 0, bytes.length);
				zos.closeEntry();
			}
			zos.close();

			FileSystemResource resource = new FileSystemResource(zipFileName);
			StreamUtils.copy(resource.getInputStream(), zos);

			header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipFileName + "\"");
			header.add("Cache-Control", "no-cache, no-store, must-revalidate");
			header.add("Pragma", "no-cache");
			header.add("Expires", "0");

			return ResponseEntity.ok().headers(header).contentType(MediaType.parseMediaType("application/octet-stream"))
					.body(resource);
		} catch (Exception e) {
			logger.error("Exception occured while fetching the file from blob", e);
			// Downloading exception in file
			Workbook workbook = new Workbook();
			Worksheet worksheet = workbook.getWorksheets().get(0);
			Cells cells = worksheet.getCells();
			worksheet.setName("Error Details");
			Cell cell = cells.get("A1");
			cell.setValue("Error Message");
			// Adding a double value to the cell
			cell = cells.get("A2");
			cell.setValue(e);
			// Write the output to the file
			ByteArrayOutputStream baout = new ByteArrayOutputStream();
			workbook.save(baout, SaveFormat.XLSX);
			File file = File.createTempFile("Error_File", ".xlsx");
			FileUtils.writeByteArrayToFile(file, baout.toByteArray());

			Resource resource = new FileSystemResource(file);

			header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"");
			header.add("Cache-Control", "no-cache, no-store, must-revalidate");
			header.add("Pragma", "no-cache");
			header.add("Expires", "0");

			return ResponseEntity.ok().headers(header).contentLength(file.length())
					.contentType(MediaType.parseMediaType("application/octet-stream")).body(resource);
		}
	}

	@GetMapping(value = "/filing/26Q/from/conso")
	public ResponseEntity<ApiStatus<FilingFilesResponseDTO>> get26QFileFromConsoFile(
			@RequestHeader("X-TENANT-ID") String tenantId,
			@RequestParam(value = "assessmentYear", required = true) Integer assessmentYear,
			@RequestHeader("TAN-NUMBER") String tanNumber, @RequestParam("quarter") String quarter,
			@RequestHeader(value = "USER_NAME") String userName) throws Exception {

		FilingFiles dto = filingFileGenerationService.get26QFileFromConsoFile(tenantId, tanNumber, assessmentYear,
				quarter, userName);
		FilingFilesResponseDTO filingFilesResponse = filingFileGenerationService.copyToFilingFileResponseDTO(dto);
		ApiStatus<FilingFilesResponseDTO> apiStatus = new ApiStatus<FilingFilesResponseDTO>(HttpStatus.OK, "SUCCESS",
				"26Q file generated with conso file", filingFilesResponse);

		return new ResponseEntity<ApiStatus<FilingFilesResponseDTO>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping(value = "/filing/status")
	public ResponseEntity<ApiStatus<FilingStatusResponseDTO>> findByYearAndQuarterAndFileType(
			@RequestHeader("X-TENANT-ID") String tenantId,
			@RequestParam(value = "assessmentYear", required = true) Integer assessmentYear,
			@RequestHeader("TAN-NUMBER") String tanNumber, @RequestParam("quarter") String quarter,
			@RequestParam(value = "formType", required = false) String formType,
			@RequestHeader(value = "USER_NAME") String userName) throws Exception {

		FilingStatus dto = filingFileGenerationService.findByYearAndQuarterAndFileType(tenantId, assessmentYear,
				quarter, tanNumber, formType);
		FilingStatusResponseDTO filingFilesResponse = filingFileGenerationService.copyToResponseDTO(dto);
		ApiStatus<FilingStatusResponseDTO> apiStatus = new ApiStatus<FilingStatusResponseDTO>(HttpStatus.OK, "SUCCESS",
				"Filing staus details", filingFilesResponse);

		return new ResponseEntity<ApiStatus<FilingStatusResponseDTO>>(apiStatus, HttpStatus.OK);
	}

	// Below function will convert multipart to file
	public File convert(InputStream inputStream, String originalFilename) throws IOException {
		File tempFile = File.createTempFile(FilenameUtils.getBaseName(originalFilename), ".xlsx");
		tempFile.deleteOnExit();
		FileOutputStream out = new FileOutputStream(tempFile);
		IOUtils.copy(inputStream, out);
		return tempFile;
	}

	/**
	 * 
	 * @param tenantId
	 * @param tanNumber
	 * @param userName
	 * @param filingObj
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/filing/generateExcel")
	public ResponseEntity<ApiStatus<String>> generateExcellFromText(
			@RequestHeader(value = "X-TENANT-ID", required = true) String tenantId,
			@RequestHeader("TAN-NUMBER") String tanNumber, @RequestHeader("USER_NAME") String userName,
			@RequestBody Map<String, String> filingObj) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		String fileUrl = filingObj.get("url");
		String formType = filingObj.get("formType");
		rpuFileReadingService.generateExcellFromText(fileUrl, tenantId, false, formType, tanNumber, userName);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS", "Generating Excel file.",
				"Generating Excel file.");
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping(value = "/filing/form16a/receipt/details")
	public ResponseEntity<ApiStatus<Map<String, Object>>> getReceiptInvoiceDeails(
			@RequestHeader("TAN-NUMBER") String tanNumber, @RequestParam("assessmentYear") int assessmentYear,
			@RequestParam(value = "formType", required = true) String formType, @RequestParam("quarter") String quarter,
			@RequestHeader("X-TENANT-ID") String tenantId) throws ParseException {

		Map<String, Object> result = filingFileGenerationService.getReceiptInvoiceDeails(tenantId, assessmentYear,
				quarter, tanNumber, formType);

		ApiStatus<Map<String, Object>> apiStatus = new ApiStatus<Map<String, Object>>(HttpStatus.OK,
				"To get data for Filing form 16a", "Filing Section Code Data", result);
		return new ResponseEntity<ApiStatus<Map<String, Object>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for feign client for get filing files
	 * 
	 * @param filingfilesId
	 * @param tenantId
	 * @return
	 */
	@GetMapping(value = "/filingfiles/id")
	public ResponseEntity<ApiStatus<FilingFiles>> getFilingFilesById(@RequestParam("id") Integer filingfilesId,
			@RequestHeader("X-TENANT-ID") String tenantId) {

		FilingFiles filingStatus = filingFileGenerationService.findByFilingFilesId(filingfilesId);

		ApiStatus<FilingFiles> apiStatus = new ApiStatus<FilingFiles>(HttpStatus.OK, "SUCCESS", "NO ALERT",
				filingStatus);

		return new ResponseEntity<ApiStatus<FilingFiles>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for feign client update filing files
	 * 
	 * @param filingFilesObj
	 * @param tenantId
	 * @return
	 */
	@PutMapping(value = "/update/filingfiles", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<FilingFiles>> updateFilingFiles(@RequestBody FilingFiles filingFilesObj,
			@RequestHeader("X-TENANT-ID") String tenantId) {

		FilingFiles filingFiles = filingFileGenerationService.updateFilingFiles(filingFilesObj);

		ApiStatus<FilingFiles> apiStatus = new ApiStatus<FilingFiles>(HttpStatus.OK, "SUCCESS", "NO ALERT",
				filingFiles);

		return new ResponseEntity<ApiStatus<FilingFiles>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * feign call to save data in filing files from FVU
	 * 
	 * @param tenantId
	 * @param filingFiles
	 * @return
	 */
	@PostMapping(value = "/filigFiles")
	ResponseEntity<ApiStatus<FilingFiles>> saveFilingFiles(@RequestHeader("X-TENANT-ID") String tenantId,
			@RequestBody FilingFiles filingFiles) {
		MultiTenantContext.setTenantId(tenantId);
		logger.info("Feign call execution started to save data in filing files {}");
		FilingFiles result = filingFileGenerationService.save(filingFiles);
		ApiStatus<FilingFiles> apiStatus = new ApiStatus<FilingFiles>(HttpStatus.OK, "SUCCESS", "SUCCESS", result);
		logger.info("Feign call execution succeded to save data in filing files {}");
		return new ResponseEntity<ApiStatus<FilingFiles>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for feign client
	 * 
	 * @param year
	 * @param quarter
	 * @param deductorTan
	 * @param formType
	 * @param fileType
	 * @param tenantId
	 * @return
	 * @throws Exception
	 */
	@GetMapping(value = "/filingFiles/by/year/quarter/tan/formType/filetype")
	public ResponseEntity<ApiStatus<List<FilingFiles>>> tdsFindByYearQuarterDeductorTanAndFormTypeFileType(
			@RequestParam("assessmentYear") int year, @RequestParam("quarter") String quarter,
			@RequestHeader("TAN-NUMBER") String deductorTan,
			@RequestParam(value = "formType", required = true) String formType,
			@RequestHeader(value = "fileType") String fileType, @RequestHeader("X-TENANT-ID") String tenantId)
			throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		logger.info("Feign call execution started to get Filing File {}");
		List<FilingFiles> list = filingFileGenerationService.findByYearQuarterDeductorTanAndFormTypeFileType(year,
				quarter, deductorTan, formType, fileType);
		ApiStatus<List<FilingFiles>> apiStatus = new ApiStatus<List<FilingFiles>>(HttpStatus.OK, "SUCCESS", "SUCCESS",
				list);
		logger.info("Feign call execution SuccessFull with data {}", list.toString());
		return new ResponseEntity<ApiStatus<List<FilingFiles>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param filingObj
	 * @param tanNumber
	 * @param deductorPan
	 * @param tenantId
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/filing/emailForm16Certificates")
	public ResponseEntity<ApiStatus<String>> emailForm16Certificates(@RequestBody Map<String, String> filingObj,
			@RequestHeader("TAN-NUMBER") String tanNumber,
			@RequestHeader(value = "DEDUCTOR-PAN", required = true) String deductorPan,
			@RequestHeader(value = "X-TENANT-ID", required = true) String tenantId) throws Exception {

		String assessmentYear = filingObj.get("assesmentYear");
		String quarter = filingObj.get("quarter");
		Integer year = Integer.parseInt(assessmentYear);
		Integer filingId = Integer.parseInt(filingObj.get("filingId"));
		String ccEmail = filingObj.get("email");
		String contactName = filingObj.get("name");

		filingFileGenerationService.emailForm16Certificates(year, quarter, filingId, tanNumber, tenantId, deductorPan,
				ccEmail, contactName);

		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS", "Emails triggered successfully",
				"");
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API is used for upload signed pdfs.
	 * 
	 * @param userName
	 * @param file
	 * @param assesmentYear
	 * @param quarter
	 * @param filingId
	 * @param tenantId
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/filing/uploadsignedpdfs", consumes = { "multipart/form-data" })
	public ResponseEntity<ApiStatus<FilingFiles>> uploadSignedPdfs(@RequestHeader("USER_NAME") String userName,
			@RequestParam(value = "file", required = true) MultipartFile file,
			@RequestParam(value = "year", required = true) Integer assesmentYear,
			@RequestParam(value = "quarter", required = true) String quarter,
			@RequestParam(value = "id", required = true) String filingId,
			@RequestHeader(value = "X-TENANT-ID", required = true) String tenantId) throws Exception {

		FilingFiles filingFiles = filingFileGenerationService.uploadSignedPdfs(assesmentYear, quarter,
				Integer.parseInt(filingId), userName, tenantId, file);

		ApiStatus<FilingFiles> apiStatus = new ApiStatus<FilingFiles>(HttpStatus.OK, "SUCCESS",
				"Uploaded file successfully", filingFiles);
		return new ResponseEntity<ApiStatus<FilingFiles>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param filingObj
	 * @param tanNumber
	 * @param deductorPan
	 * @param tenantId
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/form16emailreport")
	public ResponseEntity<ApiStatus<String>> downloadForm16EmailReport(@RequestBody Map<String, String> filingObj,
			@RequestHeader("TAN-NUMBER") String tanNumber,
			@RequestHeader(value = "DEDUCTOR-PAN", required = true) String deductorPan,
			@RequestHeader(value = "X-TENANT-ID", required = true) String tenantId,
			@RequestHeader(value = "USER_NAME") String userName) throws Exception {
		String quarter = filingObj.get("quarter");
		Integer year = Integer.parseInt(filingObj.get("year"));
		year = CommonUtil.getAssessmentYear(year);
		Integer month = CommonUtil.getAssessmentMonth(null);
		String formType = filingObj.get("formType");
		filingFileGenerationService.asyncDownloadForm16EmailReport(year, quarter, tanNumber, tenantId, deductorPan,
				month, userName, formType);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS", "FORM16A report generated successfully",
				"");
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param userName
	 * @param tenantId
	 * @param tan
	 * @param form16AObj
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/filing/requestform16Amanually")
	public ResponseEntity<ApiStatus<FilingFiles>> requestForm16aManually(@RequestHeader("USER_NAME") String userName,
			@RequestHeader(value = "X-TENANT-ID", required = true) String tenantId,
			@RequestHeader("TAN-NUMBER") String tan, @RequestBody Map<String, String> form16AObj) throws Exception {
		String quarter = form16AObj.get("quarter");
		String formType = form16AObj.get("formType");
		Integer year = Integer.parseInt(form16AObj.get("year"));
		year = CommonUtil.getAssessmentYear(year);
		Long requestNumber = new Long(form16AObj.get("requestNumber"));
		FilingFiles filingFiles = filingFileGenerationService.requestForm16aManually(year, quarter, userName, tenantId,
				formType, requestNumber, tan);

		ApiStatus<FilingFiles> apiStatus = new ApiStatus<FilingFiles>(HttpStatus.OK, "SUCCESS",
				"Requested form16A successfully", filingFiles);
		return new ResponseEntity<ApiStatus<FilingFiles>>(apiStatus, HttpStatus.OK);
	}
	
	@PostMapping(value = "/filing/getform16Adetails")
	public ResponseEntity<ApiStatus<Map<String, Object>>> getForm16AEmailDetails(
			@RequestHeader("USER_NAME") String userName,
			@RequestHeader(value = "X-TENANT-ID", required = true) String tenantId,
			@RequestHeader("TAN-NUMBER") String tan) throws Exception {
		Map<String, Object> filingFiles = filingFileGenerationService.getForm16AEmailDetails(tan);

		ApiStatus<Map<String, Object>> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", "NO ALERT", filingFiles);
		return new ResponseEntity<ApiStatus<Map<String, Object>>>(apiStatus, HttpStatus.OK);
	}

}
