package com.ey.in.tds.returns.web.rest.tcs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.IOUtils;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.tcs.TCSBatchUpload;
import com.ey.in.tds.common.dto.returns.tcs.TCSFilingFilesDTO;
import com.ey.in.tds.common.jdbc.returns.dao.TCSFilingFilesDAO;
import com.ey.in.tds.common.returns.jdbc.dto.TCSFilingFiles;
import com.ey.in.tds.common.returns.jdbc.dto.TCSFilingStatus;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.InvalidFileTypeException;
import com.ey.in.tds.core.security.AllowedMimeTypes;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.returns.services.TCSRpuFileReadingService;
import com.ey.in.tds.returns.services.tcs.TCSFilingFileGenerationService;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.microsoft.azure.storage.StorageException;

@RestController
@RequestMapping("/api/returns/tcs")
public class TCSFilingFileGenerationController {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	TCSFilingFileGenerationService tcsFilingFileGenerationService;

	@Autowired
	TCSFilingFilesDAO tCSFilingFilesDAO;

	@Autowired
	TCSRpuFileReadingService tCSRpuFileReadingService;

	/**
	 * returns filing file data based on collector tan and year
	 * 
	 * @param assessmentYear
	 * @param deductorTan
	 * @return
	 */
	@GetMapping(value = "/filing/{assessmentYear}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<TCSFilingFilesDTO>>> getTCSFilingFilesByYearAndCollectorTan(
			@PathVariable("assessmentYear") Integer assessmentYear, @RequestHeader("TAN-NUMBER") String deductorTan) {
		List<TCSFilingFilesDTO> filingStatus = tcsFilingFileGenerationService.findTCSFilingByYear(assessmentYear,
				deductorTan);

		ApiStatus<List<TCSFilingFilesDTO>> apiStatus = new ApiStatus<List<TCSFilingFilesDTO>>(HttpStatus.OK, "SUCCESS",
				"TCS Filing Files data", filingStatus);

		return new ResponseEntity<ApiStatus<List<TCSFilingFilesDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This api returns the tcs filing files details
	 * 
	 * @param assessmentYear
	 * @param deductorId
	 * @param quarter
	 * @return
	 */
	@PostMapping(value = "/filingfiles", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<TCSFilingFilesDTO>>> getTCSFilingFiles(
			@RequestBody Map<String, String> requestParams, @RequestHeader("TAN-NUMBER") String deductorTan) {
		String assessmentYear = requestParams.get("year");
		String deductorPan = requestParams.get("deductorPan");
		String quarter = requestParams.get("quarter");
		Integer year = Integer.parseInt(assessmentYear);
		List<TCSFilingFilesDTO> filingStatus = tcsFilingFileGenerationService.tcsFindFilingByYearDeductorIdQuarter(year,
				deductorPan, quarter, deductorTan, null);

		ApiStatus<List<TCSFilingFilesDTO>> apiStatus = new ApiStatus<List<TCSFilingFilesDTO>>(HttpStatus.OK, "SUCCESS",
				"NO ALERT", filingStatus);

		return new ResponseEntity<ApiStatus<List<TCSFilingFilesDTO>>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping(value = "/filing/status")
	public ResponseEntity<ApiStatus<TCSFilingStatus>> findTCSFilingFilesByYearAndQuarterAndFileType(
			@RequestHeader("X-TENANT-ID") String tenantId,
			@RequestParam(value = "assessmentYear", required = true) Integer assessmentYear,
			@RequestHeader("TAN-NUMBER") String tanNumber, @RequestParam("quarter") String quarter,
			@RequestHeader(value = "USER_NAME") String userName) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		TCSFilingStatus dto = tcsFilingFileGenerationService.findTCSFilingStatusByYearAndQuarterAndFileType(tenantId,
				assessmentYear, quarter, tanNumber);
		ApiStatus<TCSFilingStatus> apiStatus = new ApiStatus<TCSFilingStatus>(HttpStatus.OK, "SUCCESS",
				"TCS Filing staus details", dto);

		return new ResponseEntity<ApiStatus<TCSFilingStatus>>(apiStatus, HttpStatus.OK);
	}

	@PostMapping(value = "/create/filingRecord")
	public ResponseEntity<ApiStatus<String>> createReturnFilingReport(@RequestHeader("TAN-NUMBER") String tanNumber,
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @RequestParam("assessmentYear") int assessmentYear,
			@RequestParam(value = "formType", required = true) String formType, @RequestParam("quarter") String quarter,
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader(value = "USER_NAME") String userName,
			@RequestParam(value = "AGGRIGATED", required = false) Boolean isAggriated) throws InvalidKeyException,
			URISyntaxException, StorageException, JsonParseException, JsonMappingException, IOException {
		MultiTenantContext.setTenantId(tenantId);

		tcsFilingFileGenerationService.asyncCreateReturnFilingReportTCS(formType, tanNumber, deductorPan,
				assessmentYear, quarter, tenantId, userName, isAggriated);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS",
				"27EQ generation requested successfully", "27EQ generation requested successfully");
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * feign call to return data for FVU
	 * 
	 * @param deductorTan
	 * @param year
	 * @param formType
	 * @param quarter
	 * @param tenantId
	 * @param fileType
	 * @return
	 * @throws Exception
	 */
	@GetMapping(value = "getFilingFiles/By/year/Quarter/DeductorTanAndFormTypeFileType")
	public ResponseEntity<ApiStatus<List<TCSFilingFiles>>> findByYearQuarterDeductorTanAndFormTypeFileType(
			@RequestParam("assessmentYear") int year, @RequestParam("quarter") String quarter,
			@RequestHeader("TAN-NUMBER") String deductorTan,
			@RequestParam(value = "formType", required = true) String formType,
			@RequestHeader(value = "fileType") String fileType, @RequestHeader("X-TENANT-ID") String tenantId)
			throws Exception {

		MultiTenantContext.setTenantId(tenantId);
		logger.info("Feign call execution started to get Filing File {}");
		List<TCSFilingFiles> list = tCSFilingFilesDAO.findByYearQuarterDeductorTanAndFormTypeFileType(year, quarter,
				deductorTan, formType, fileType);
		ApiStatus<List<TCSFilingFiles>> apiStatus = new ApiStatus<List<TCSFilingFiles>>(HttpStatus.OK, "SUCCESS",
				"SUCCESS", list);
		logger.info("Feign call execution SuccessFull with data {}", list.toString());

		return new ResponseEntity<ApiStatus<List<TCSFilingFiles>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * feign call to save data in filing files from FVU
	 * 
	 * @param tenantId
	 * @param tCSFilingFiles
	 * @return
	 */
	@PostMapping(value = "filigFiles/feign/save")
	ResponseEntity<ApiStatus<TCSFilingFiles>> feignSaveFilingFiles(@RequestHeader("X-TENANT-ID") String tenantId,
			@RequestBody TCSFilingFiles tCSFilingFiles) {
		MultiTenantContext.setTenantId(tenantId);
		logger.info("Feign call execution started to save data in filing files {}");
		tCSFilingFiles = tCSFilingFilesDAO.save(tCSFilingFiles);
		ApiStatus<TCSFilingFiles> apiStatus = new ApiStatus<TCSFilingFiles>(HttpStatus.OK, "SUCCESS", "SUCCESS",
				tCSFilingFiles);
		logger.info("Feign call execution succeded to save data in filing files {}");

		return new ResponseEntity<ApiStatus<TCSFilingFiles>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API for feign client update tcs filing files
	 * 
	 * @param filingFilesObj
	 * @param tenantId
	 * @return
	 */
	@PutMapping(value = "/update/filingfiles", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<TCSFilingFiles>> tdsUpdateFilingFiles(@RequestBody TCSFilingFiles filingFilesObj,
			@RequestHeader("X-TENANT-ID") String tenantId) {
		TCSFilingFiles filingFiles = tcsFilingFileGenerationService.updateFilingFiles(filingFilesObj);
		ApiStatus<TCSFilingFiles> apiStatus = new ApiStatus<TCSFilingFiles>(HttpStatus.OK, "SUCCESS", "NO ALERT",
				filingFiles);
		return new ResponseEntity<ApiStatus<TCSFilingFiles>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This api is for processing Excel and creating text file
	 * 
	 * @param multipartFile
	 * @param formType
	 * @param quarter
	 * @return
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws InvalidFileTypeException
	 * @throws IOException
	 * @throws InvalidFormatException
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
			throws InvalidKeyException, URISyntaxException, StorageException, InvalidFileTypeException, IOException,
			InvalidFormatException {

		MultiTenantContext.setTenantId(tenantId);
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

			tCSRpuFileReadingService.generateFile(xlsxFile, file, formType, quarter, deductorPan, tanNumber,
					assessmentYear, tenantId, userName, formType, isCorrection);

			ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS",
					formType + " generation requested and will be available shortly", "");

			return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
		} else {
			logger.warn("Unsupported file upload attempt by {}", userName);
			throw new CustomException("Invalid file contents, only xlsx files are allowed", HttpStatus.BAD_REQUEST);
		}
	}

	// Below function will convert multipart to file
	public File convert(InputStream inputStream, String originalFilename) throws IOException {
		File tempFile = File.createTempFile(originalFilename, ".xlsx");
		tempFile.deleteOnExit();
		FileOutputStream out = new FileOutputStream(tempFile);
		IOUtils.copy(inputStream, out);
		return tempFile;
	}

	@PostMapping(value = "/filing/generateExcel")
	public ResponseEntity<ApiStatus<TCSBatchUpload>> generateExcellFromText(
			@RequestHeader(value = "X-TENANT-ID", required = true) String tenantId,
			@RequestHeader(value = "TAN-NUMBER", required = true) String tanNumber,
			@RequestHeader(value = "USER_NAME") String userName,
			@RequestParam(value = "url", required = true) String fileUrl) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		TCSBatchUpload url = tCSRpuFileReadingService.generateExcellFromText(fileUrl, tenantId,tanNumber,userName);
		ApiStatus<TCSBatchUpload> apiStatus = new ApiStatus<TCSBatchUpload>(HttpStatus.OK, "SUCCESS",
				"Excel file generated Successfully", url);
		return new ResponseEntity<ApiStatus<TCSBatchUpload>>(apiStatus, HttpStatus.OK);
	}

}
