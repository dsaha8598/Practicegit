package com.ey.in.tds.ingestion.web.rest.batchupload;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import com.ey.in.tds.common.config.TenantProperties;
import com.ey.in.tds.common.domain.tcs.TCSBatchUpload;
import com.ey.in.tds.common.domain.transactions.ReconciliationTriggerRequest;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.dto.BatchProcessDTO;
import com.ey.in.tds.common.dto.sftp.SftpPathDTO;
import com.ey.in.tds.common.ingestion.response.dto.SparkState;
import com.ey.in.tds.common.repository.common.PagedData;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.common.service.BlobStorageService;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.TanNotFoundException;
import com.ey.in.tds.core.security.AllowedMimeTypes;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.TenantConfiguration;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.ingestion.dto.batch.BatchUploadDTO;
import com.ey.in.tds.ingestion.dto.batch.BatchUploadGroupCustomDTO;
import com.ey.in.tds.ingestion.dto.batch.ReceiptBatchUploadDTO;
import com.ey.in.tds.ingestion.dto.batchgroup.BatchUploadGroupDTO;
import com.ey.in.tds.ingestion.service.batchupload.TCSBatchUploadService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.microsoft.azure.storage.StorageException;

/**
 * 
 * @author Scriptbees
 *
 */
@RestController
@RequestMapping("/api/ingestion/tcs")
public class TCSBatchUploadController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TCSBatchUploadService tcsBatchUploadService;

	@Autowired
	BlobStorageService blobStorageService;

	@Autowired
	TenantProperties tenantProperties;

	/**
	 * getting batch upload by file type and year
	 * 
	 * @param tan
	 * @param type
	 * @param tenantId
	 * @param year
	 * @return
	 */
	@GetMapping("/batch/{type}/{year}")
	public ResponseEntity<ApiStatus<List<BatchUploadDTO>>> listOfBatchUploadFiles(
			@RequestHeader(value = "TAN-NUMBER") String tan, @PathVariable String type,
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @PathVariable String year) {
		List<BatchUploadDTO> listBatchUploads = tcsBatchUploadService.getListOfBatchUploadFiles(type,
				Arrays.asList(tan), Integer.parseInt(year));
		logger.info("REST response for batch upload data : {}", listBatchUploads);
		ApiStatus<List<BatchUploadDTO>> apiStatus = new ApiStatus<List<BatchUploadDTO>>(HttpStatus.OK, "SUCCESS",
				"LIST OF BATCH RECORDS", listBatchUploads);
		return new ResponseEntity<ApiStatus<List<BatchUploadDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * returns the batch uploadd based on upload type and tan
	 * 
	 * @param assessmentYear
	 * @param type
	 * @param pagination
	 * @param tan
	 * @return
	 */
	@PostMapping(value = "/paginationTest", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<PagedData<TCSBatchUpload>>> paginationTest(
			@RequestParam("assessmentYear") int assessmentYear, @RequestParam("type") String type,
			@RequestBody(required = false) Pagination pagination, @RequestHeader(value = "TAN-NUMBER") String tan) {
		System.out.println(assessmentYear + " == " + tan);
		List<TCSBatchUpload> listBatchUploads = tcsBatchUploadService.getBatchUploads(type, Arrays.asList(tan),
				assessmentYear);
		logger.info("REST response for batch upload data : {}", listBatchUploads);
		ApiStatus<PagedData<TCSBatchUpload>> apiStatus = new ApiStatus<>(HttpStatus.CREATED, "SUCCESS", "NO ALERT",
				new PagedData<TCSBatchUpload>(listBatchUploads, pagination.getPageSize(), pagination.getPageNumber(),
						false));
		return new ResponseEntity<ApiStatus<PagedData<TCSBatchUpload>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param file
	 * @param assessmentYear
	 * @param assessmentMonth
	 * @return
	 * @throws Exception 
	 */
	@PostMapping("/batch/excel")
	public ResponseEntity<ApiStatus<TCSBatchUpload>> uploadExcel(@RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestParam("year") int assessmentYear, @RequestParam(value = "type") String type,
			@RequestParam("files") MultipartFile[] files, @RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestParam(value = "keywordId", required = false) Integer keywordId, HttpServletRequest request,
			@RequestHeader(value = "USER_NAME") String userName,
			@RequestParam(value = "month", required = false) Integer month,
			@RequestParam(value = "startDate", required = false) Date startDate,
			@RequestParam(value = "endDate", required = false) Date endDate)
			throws Exception {
		// We have the data in redis for the User and this will be used in Spark
		// Notebooks.
		boolean validFiles = true;
		if (files != null && files.length > 0) {
			for (MultipartFile file : files) {
				String contentType = new Tika().detect(file.getInputStream(), file.getOriginalFilename());
				if (!AllowedMimeTypes.XLSX.getMimeType().equals(contentType)
						&& !AllowedMimeTypes.OOXML.getMimeType().equals(contentType)
						&& !AllowedMimeTypes.CSV.getMimeType().equals(contentType)) {
					validFiles = false;
				}
			}
			if (!validFiles) {
				logger.warn("Unsupported file upload attempt by {}", userName);
				throw new CustomException("Invalid file contents, only xlsx and csv files are allowed",
						HttpStatus.BAD_REQUEST);
			}
			logger.info("Upload Params: {}, {}, {}, {}, {}", tan, assessmentYear, type, files.length, deductorPan);
			String userEmail = request.getHeader("USER_NAME");
			String token = request.getHeader("Authorization");
			TCSBatchUpload batchData = tcsBatchUploadService.saveUploadExcel(assessmentYear, files, tan, type, tenantId,
					userEmail, deductorPan, keywordId, token, month, startDate, endDate);
			ApiStatus<TCSBatchUpload> apiStatus = new ApiStatus<TCSBatchUpload>(HttpStatus.CREATED, "SUCCESS",
					"NO ALERT", batchData);
			return new ResponseEntity<ApiStatus<TCSBatchUpload>>(apiStatus, HttpStatus.OK);
		} else {
			throw new IllegalArgumentException("Atleast one file must be uploaded.");
		}
	}

	/**
	 * 
	 * @param tan
	 * @param assessmentYear
	 * @param type
	 * @param file
	 * @param tenantId
	 * @param userEmail
	 * @param deductorPan
	 * @param userName
	 * @return
	 * @throws Exception 
	 */
	@PostMapping("/batch/excel/panvalidation")
	public ResponseEntity<ApiStatus<TCSBatchUpload>> uploadExcel(@RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestParam("year") int assessmentYear, @RequestParam("type") String type,
			@PathVariable("file") MultipartFile file, @RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userEmail, @RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestHeader(value = "USER_NAME") String userName)
			throws Exception {
		String contentType = new Tika().detect(file.getInputStream());
		if (AllowedMimeTypes.XLSX.getMimeType().equals(contentType)
				|| AllowedMimeTypes.OOXML.getMimeType().equals(contentType)) {
			// We have the data in redis for the User and this will be used in Spark
			// Notebooks.
			// As of now only single file upload allowed for pan validation
			MultipartFile[] files = new MultipartFile[1];
			files[0] = file;
			TCSBatchUpload batchData = tcsBatchUploadService.saveUploadExcel(assessmentYear, files, tan, type, tenantId,
					userEmail, deductorPan, null, null, null, null, null);
			ApiStatus<TCSBatchUpload> apiStatus = new ApiStatus<TCSBatchUpload>(HttpStatus.CREATED, "SUCCESS",
					"FILE UPLOADED SUCCESSFULLY", batchData);
			return new ResponseEntity<ApiStatus<TCSBatchUpload>>(apiStatus, HttpStatus.OK);
		} else {
			logger.warn("Unsupported file upload attempt by {}", userName);
			throw new CustomException("Invalid file contents, only xlsx files are allowed", HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * 
	 * @param file
	 * @param assessmentYear
	 * @param assessmentMonth
	 * @return
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws IOException
	 * @throws ParseException
	 */
	@PostMapping("/batch/pdf")
	public ResponseEntity<ApiStatus<TCSBatchUpload>> uploadPDF(@RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestParam("files") MultipartFile[] files, @RequestParam("year") int assessmentYear,
			@RequestParam("type") String type, @RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName, @RequestParam("groupName") String groupName,
			@RequestParam("residentType") String residentType, @RequestParam("postingDate") String postingDate,
			@RequestHeader("DEDUCTOR-PAN") String deductorPan)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException, ParseException {
		boolean validFiles = true;
		if (files != null && files.length > 0) {
			for (MultipartFile file : files) {
				String contentType = new Tika().detect(file.getInputStream());
				if (!AllowedMimeTypes.PDF.getMimeType().equals(contentType)) {
					validFiles = false;
				}
			}
			if (!validFiles) {
				throw new CustomException("Invalid file contents, only pdf files are allowed", HttpStatus.BAD_REQUEST);
			}
			TCSBatchUpload batchData = tcsBatchUploadService.saveUploadPDF(assessmentYear, files, tan, type, tenantId,
					userName, groupName, postingDate, residentType, deductorPan);
			ApiStatus<TCSBatchUpload> apiStatus = new ApiStatus<TCSBatchUpload>(HttpStatus.CREATED, "SUCCESS",
					"FILE UPLOADED SUCCESSFULLY", batchData);
			return new ResponseEntity<ApiStatus<TCSBatchUpload>>(apiStatus, HttpStatus.OK);
		} else {
			logger.warn("Unsupported file upload attempt by {}", userName);
			throw new IllegalArgumentException("Atleast one file must be uploaded.");
		}
	}

	/**
	 * 
	 * @param batchUpload
	 * @param deductorTan
	 * @return
	 */
	@PostMapping("/batchupload")
	public TCSBatchUpload create(@RequestBody TCSBatchUpload batchUpload,
			@RequestHeader(value = "TAN-NUMBER") String deductorTan, @RequestHeader("X-TENANT-ID") String tenantId) {
		batchUpload.setCollectorMasterTan(deductorTan);
		logger.info("Creating the Batch Upload {}", batchUpload);
		batchUpload = tcsBatchUploadService.create(batchUpload);
		return batchUpload;
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param month
	 * @param type
	 * @param status
	 * @param sha256sum
	 * @param id
	 * @param deductorTan
	 * @return
	 */
	@GetMapping("/batchupload")
	public TCSBatchUpload get(@RequestParam("assessmentYear") int assessmentYear, @RequestParam("month") int month,
			@RequestParam("type") String type, @RequestParam("status") String status,
			@RequestParam("sha256sum") String sha256sum, @RequestParam("id") Integer id,
			@RequestHeader(value = "TAN-NUMBER") String deductorTan, @RequestHeader("X-TENANT-ID") String tenantId) {
		// test
		TCSBatchUpload batchUpload = tcsBatchUploadService.get(assessmentYear, deductorTan, type, id);
		return batchUpload;
	}

	/**
	 * 
	 * @param batchUpload
	 * @param deductorTan
	 * @return
	 */
	@PutMapping("/batchupload")
	public TCSBatchUpload update(@RequestBody TCSBatchUpload batchUpload,
			@RequestHeader(value = "TAN-NUMBER") String deductorTan, @RequestHeader("X-TENANT-ID") String tenantId) {
		batchUpload.setCollectorMasterTan(deductorTan);
		logger.info("Updating the Batch Upload : {}", batchUpload);
		batchUpload = tcsBatchUploadService.update(batchUpload);
		return batchUpload;
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param month
	 * @param type
	 * @param status
	 * @param sha256sum
	 * @param id
	 * @param deductorTan
	 */
	@DeleteMapping("/batchupload")
	public void delete(@RequestParam("assessmentYear") int assessmentYear, @RequestParam("month") int month,
			@RequestParam("type") String type, @RequestParam("status") String status,
			@RequestParam("sha256sum") String sha256sum, @RequestParam("id") Integer id,
			@RequestHeader(value = "TAN-NUMBER") String deductorTan, @RequestHeader("X-TENANT-ID") String tenantId) {
		logger.info("Deleting the Batch Upload id : {}", id);
		tcsBatchUploadService.deleteById(assessmentYear, deductorTan, type, id);
	}

	/**
	 * 
	 * @param sftpPathDTO
	 * @param tenantName
	 * @return
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws IOException
	 * @throws ParseException
	 */
	@PostMapping(value = "/batch/sftp/to/blob", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<TCSBatchUpload>> uploadSftpToBlob(@RequestBody SftpPathDTO sftpPathDTO,
			@RequestHeader(value = "X-TENANT-ID", required = false) String tenantName)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException, ParseException {
		// We have the data in redis for the User and this will be used in Spark
		// Based on userEmail need to get TAN and PAN for batch
		logger.info("SAP file data : {}", sftpPathDTO);
		logger.info("Tenant ID : {}", tenantName);
		TCSBatchUpload tcsBatchData = null;
		if (sftpPathDTO.getSrcPath().contains("Input")) {
			if (StringUtils.isEmpty(tenantName) && !sftpPathDTO.getUserEmail().isEmpty()) {
				tenantName = sftpPathDTO.getUserEmail().substring(sftpPathDTO.getUserEmail().lastIndexOf("@") + 1,
						sftpPathDTO.getUserEmail().length());
			}
			TenantConfiguration.BlobStorage blobStorage = tenantProperties.getConfiguration(tenantName)
					.getBlobStorage();
			String filePath = sftpPathDTO.getSrcPath().split("Input\\\\")[1].replace("\\", "/");
			logger.info("SAP file path : {}", filePath);
			String blobURL = blobStorage.getProtocol() + "://" + blobStorage.getAccountName()
					+ ".blob.core.windows.net/" + blobStorage.getContainer() + "/" + filePath;
			String[] strs = sftpPathDTO.getSrcPath().split("\\\\");
			String panSplit = strs[strs.length - 1];
			String pan = panSplit.split("_")[0];
			String tan = panSplit.split("_")[1];
			logger.info("PAN : {}", pan);
			logger.info("TAN : {}", tan);
			// convert url to file
			File file = blobStorageService.getFileFromBlobFileName(tenantName, blobURL, filePath);
			InputStream inputstream = new FileInputStream(file);
			// converting file to multipart file
			MultipartFile multipartFile = new MockMultipartFile("file", file.getName(),
					FilenameUtils.getExtension(file.getName()), IOUtils.toByteArray(inputstream));
			String fileName = sftpPathDTO.getSrcPath();
			// ADVANCE_EXTRACT, DEDUCTEE_NONRESIDENT, DEDUCTEE_RESIDENT, GL_EXTRACT,
			// PROVISION_EXTRACT, TDS_INVOICE_EXTRACT
			// Get the Type Based on the SRC Path.
			// BLOB URL (Construct the Blob URL from the Path.)
			String type = "";
			if (fileName.contains("PAYMENT")) {
				type = "PAYMENT_SAP";
			} else if (fileName.contains("INVOICE")) {
				type = "INVOICE_SAP";
			} else if (fileName.contains("GL")) {
				type = "GL_SAP";
			} else if (fileName.contains("COLLECTEE")) {
				type = "COLLECTEE_SAP";
			}
			logger.info("type : {}", type);
			tcsBatchData = tcsBatchUploadService.uploadBlobUrlToBatch(CommonUtil.getAssessmentYear(null), multipartFile,
					tan, type, tenantName, sftpPathDTO.getUserEmail(), blobURL, pan, filePath);
			logger.info("SAP_FTP Batch Upload {}", tcsBatchData);
			ApiStatus<TCSBatchUpload> apiStatus = new ApiStatus<TCSBatchUpload>(HttpStatus.CREATED, "SUCCESS",
					"FILE UPLOADED SUCCESSFULLY", tcsBatchData);
			return new ResponseEntity<ApiStatus<TCSBatchUpload>>(apiStatus, HttpStatus.OK);
		} else {
			tcsBatchData = uploadInvoicePdfBlobUrlToBatch(sftpPathDTO).getBody().getData();
			logger.info("SAP_FTP Batch Upload {}", tcsBatchData);
			ApiStatus<TCSBatchUpload> apiStatus = new ApiStatus<TCSBatchUpload>(HttpStatus.CREATED, "SUCCESS",
					"FILE UPLOADED SUCCESSFULLY", tcsBatchData);
			return new ResponseEntity<ApiStatus<TCSBatchUpload>>(apiStatus, HttpStatus.OK);
		}

	}

	/**
	 * PDF Process
	 * 
	 * @param tan
	 * @param year
	 * @return
	 */

	@GetMapping("/batch/group/{year}")
	public ResponseEntity<ApiStatus<List<BatchUploadGroupDTO>>> getGroupRecordsByYear(
			@RequestHeader(value = "TAN-NUMBER") String tan, @PathVariable Integer year) {
		List<BatchUploadGroupDTO> listOfGroup = tcsBatchUploadService.getGroupsBasedOnTan(tan, year);
		logger.info("REST response for batch upload group data : {}", listOfGroup);
		ApiStatus<List<BatchUploadGroupDTO>> apiStatus = new ApiStatus<List<BatchUploadGroupDTO>>(HttpStatus.CREATED,
				"SUCCESS", "LIST OF Group Records", listOfGroup);
		return new ResponseEntity<ApiStatus<List<BatchUploadGroupDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param tan
	 * @param groupId
	 * @return
	 */
	@GetMapping("/batch/group/all/{groupId}")
	public ResponseEntity<ApiStatus<List<BatchUploadGroupCustomDTO>>> getBatchUploadRecordsBasedOnTanAndBatchGroupId(
			@RequestHeader(value = "TAN-NUMBER") String tan, @PathVariable Integer groupId,
			@RequestHeader(value = "X-TENANT-ID", required = false) String tenantName) {
		List<BatchUploadGroupCustomDTO> batchDataBasedOnGroupId = tcsBatchUploadService.getBatchDataByGroupId(tan,
				groupId);
		ApiStatus<List<BatchUploadGroupCustomDTO>> apiStatus = new ApiStatus<List<BatchUploadGroupCustomDTO>>(
				HttpStatus.CREATED, "SUCCESS", "LIST OF BATCH RECORDS BASED ON BATCH GROUP ID",
				batchDataBasedOnGroupId);
		return new ResponseEntity<ApiStatus<List<BatchUploadGroupCustomDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param file
	 * @param tenantId
	 * @return
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws IOException
	 */
	// Test blob api for uploading file to blob
	@PostMapping(value = "/upload/blob", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<String>> uploadFileTOBlob(@RequestParam("file") MultipartFile file,
			@RequestHeader(value = "X-TENANT-ID") String tenantId)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException {
		String blobUrl = tcsBatchUploadService.uploadFileTOBlob(file, tenantId);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.CREATED, "SUCCESS", "FILE UPLOADED SUCCESSFULLY",
				blobUrl);
		logger.info("Creating the Batch Upload {}", blobUrl);
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param sftpPathDTO
	 * @return
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws IOException
	 */
	@PostMapping(value = "/invoice/pdf/blob/to/batch", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<TCSBatchUpload>> uploadInvoicePdfBlobUrlToBatch(
			@RequestBody SftpPathDTO sftpPathDTO)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException {
		logger.info("Blob URL :  {}", sftpPathDTO.getSrcPath());
		String batchId = null;
		String tenantId = null;
		// convert url to file
		String[] strs = sftpPathDTO.getSrcPath().replace("\\", "/").split("/");
		String fileName = strs[strs.length - 1];
		String[] names = fileName.split("_");
		if (names.length > 1) {
			batchId = names[0];
			tenantId = names[1];
			if (StringUtils.isNotBlank(tenantId)) {
				MultiTenantContext.setTenantId(tenantId);
			} else {
				throw new CustomException("There is no Tenant ID in file Name");
			}
		}
		TCSBatchUpload batchData = tcsBatchUploadService.uploadInvoicePdfBlobUrlToBatch(UploadTypes.INVOICE_PDF.name(),
				tenantId, Integer.valueOf(batchId), fileName);
		ApiStatus<TCSBatchUpload> apiStatus = new ApiStatus<TCSBatchUpload>(HttpStatus.CREATED, "SUCCESS",
				"FILE UPLOADED SUCCESSFULLY", batchData);
		logger.info("Creating the Batch Upload {}", batchData);
		return new ResponseEntity<ApiStatus<TCSBatchUpload>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param blobUrl
	 * @param tenantId
	 * @param type
	 * @param userEmail
	 * @param tan
	 * @param deductorPan
	 * @return
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws IOException
	 * @throws ParseException
	 */
	@GetMapping("/batch/blob")
	public ResponseEntity<ApiStatus<TCSBatchUpload>> uploadBlobUrlToBatch(@RequestParam("blobUrl") String blobUrl,
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestParam("type") String type,
			@RequestParam("userEmail") String userEmail, @RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestHeader("DEDUCTOR-PAN") String deductorPan)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException, ParseException {
		logger.info("Blob URL :  {}", blobUrl);
		// convert url to file
		File file = blobStorageService.getFileFromBlobUrl(tenantId, blobUrl);
		InputStream inputstream = new FileInputStream(file);
		// converting file to multipart file
		MultipartFile multipartFile = new MockMultipartFile("file", file.getName(),
				FilenameUtils.getExtension(file.getName()), IOUtils.toByteArray(inputstream));
		TCSBatchUpload batchData = tcsBatchUploadService.uploadBlobUrlToBatch(CommonUtil.getAssessmentYear(null),
				multipartFile, tan, type, tenantId, userEmail, blobUrl, deductorPan,
				multipartFile.getOriginalFilename());
		ApiStatus<TCSBatchUpload> apiStatus = new ApiStatus<TCSBatchUpload>(HttpStatus.CREATED, "SUCCESS",
				"FILE UPLOADED SUCCESSFULLY", batchData);
		logger.info("Creating the Batch Upload {}", batchData);
		return new ResponseEntity<ApiStatus<TCSBatchUpload>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param tenantId
	 * @param tan
	 * @param year
	 * @param type
	 * @return
	 */
	@GetMapping("/receipt/{type}/{year}")
	public ResponseEntity<ApiStatus<List<ReceiptBatchUploadDTO>>> getReceiptDataBasedOnBatchUploadId(
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader("TAN-NUMBER") String tan,
			@PathVariable int year, @PathVariable String type) {
		List<ReceiptBatchUploadDTO> response = tcsBatchUploadService.getReceiptDataBasedOnBatchId(type, year,
				Arrays.asList(tan), tenantId);
		ApiStatus<List<ReceiptBatchUploadDTO>> apiStatus = new ApiStatus<List<ReceiptBatchUploadDTO>>(
				HttpStatus.CREATED, "SUCCESS", "LIST OF RECEIPT AND BATCH UPLOAD DATA", response);
		return new ResponseEntity<ApiStatus<List<ReceiptBatchUploadDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param tan
	 * @param tenantId
	 * @param userEmail
	 * @param batchProcessDTO
	 * @return
	 * @throws JsonProcessingException
	 */
	@PostMapping("/batchprocess")
	public ResponseEntity<ApiStatus<TCSBatchUpload>> batchProcess(@RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader(value = "USER_NAME") String userEmail,
			@RequestBody BatchProcessDTO batchProcessDTO) throws JsonProcessingException {
		TCSBatchUpload batchData = tcsBatchUploadService.batchProcess(userEmail, tan, tenantId, batchProcessDTO);
		ApiStatus<TCSBatchUpload> apiStatus = new ApiStatus<TCSBatchUpload>(HttpStatus.CREATED, "SUCCESS",
				"PROCESS STARTED SUCCESSFULLY", batchData);
		return new ResponseEntity<ApiStatus<TCSBatchUpload>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * Method to upload reconciliation file for article 34A
	 * 
	 * @param tan              -- Tan of Deductee
	 * @param assessmentYear   -- Assessment Year
	 * @param assessmentMonths -- Assessment Months
	 * @param fileType         -- This will be the one of Reconciliation File Type
	 *                         {@link ReconciliationTriggerRequest.TYPE}.
	 * @param file             -- Actual File to upload
	 * @param tenantId         -- It will be used to map file on Azure Blob
	 * @param request          -- Object of HttpServletRequest @see
	 *                         {@link HttpServletRequest}
	 * @return --Response Entity<ApiStatus<BatchUpload> @see {@link BatchUpload}
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	@PostMapping("/batch/excel/reconciliation")
	public ResponseEntity<ApiStatus<TCSBatchUpload>> uploadReconciliationExcel(
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestParam("year") int assessmentYear,
			@RequestParam("month") int assessmentMonth, @RequestParam("fileType") String fileType,
			@RequestParam("file") MultipartFile file, @RequestHeader(value = "X-TENANT-ID") String tenantId,
			HttpServletRequest request)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException, InvalidFormatException {
		String userEmail = request.getHeader("USER_NAME");
		String token = request.getHeader("Authorization");
		TCSBatchUpload batchData = tcsBatchUploadService.saveUploadExcelForReconciliation(assessmentYear,
				assessmentMonth, file, tan, tenantId, userEmail, token, fileType);
		ApiStatus<TCSBatchUpload> apiStatus = new ApiStatus<TCSBatchUpload>(HttpStatus.CREATED, "SUCCESS",
				"FILE UPLOADED SUCCESSFULLY", batchData);
		return new ResponseEntity<ApiStatus<TCSBatchUpload>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * Method to trigger reconciliation for provided tan and assessment year.
	 * 
	 * @param tan            -- Tan for reconciliation
	 * @param assessmentYear -- Assessment year
	 * @param request        -- Object of HttpServletRequest @see
	 *                       {@link HttpServletRequest}
	 * @return --Response Entity<ApiStatus<String>
	 */

	@PostMapping("/batch/excel/triggerReconciliation")
	public ResponseEntity<ApiStatus<String>> triggerReconciliation(@RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestParam("year") int assessmentYear, HttpServletRequest request,
			@RequestHeader(value = "X-TENANT-ID") String tenantId) {
		String userEmail = request.getHeader("USER_NAME");
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.ACCEPTED, "SUCCESS",
				"REQUEST ACCEPTED SUCCESSFULLY",
				tcsBatchUploadService.processReconciliation(assessmentYear, tan, userEmail, tenantId));
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param tan
	 * @param pan
	 * @param tenantId
	 * @param year
	 * @param month
	 * @return
	 * @throws IOException
	 * @throws TanNotFoundException
	 */
	@PostMapping(value = "/keywordsreport/export", consumes = { "application/x-www-form-urlencoded" })
	public ResponseEntity<InputStreamResource> exportRemediationReport(
			@RequestParam(value = "tan", required = true) String tan,
			@RequestParam(value = "pan", required = true) String pan,
			@RequestParam(value = "tenantId", required = true) String tenantId,
			@RequestParam(value = "year", required = true) Integer year,
			@RequestParam(value = "month", required = true) Integer month) throws IOException, TanNotFoundException {
		logger.info("TAN: {}", tan);
		MultiTenantContext.setTenantId(tenantId);
		if (tan == null) {
			throw new CustomException("No Tan Found to continue the download", HttpStatus.BAD_REQUEST);
		}
		ByteArrayInputStream in = tcsBatchUploadService.exportNOIKeywords(tan, tenantId, pan, year, month);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=Keywords_Report_" + tan + ".xlsx");
		logger.info("NOI keywords report export done ");
		return ResponseEntity.ok().headers(headers).body(new InputStreamResource(in));
	}

	/**
	 * 
	 * @param tenantId
	 * @param tan
	 * @param userEmail
	 * @return
	 * @throws IOException
	 * @throws TanNotFoundException
	 */
	@GetMapping(value = "/spark/notebook/powerbi")
	public ResponseEntity<ApiStatus<String>> triggerPowerBiSparkNoteBook(
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestHeader(value = "USER_NAME") String userEmail) throws IOException, TanNotFoundException {
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.ACCEPTED, "SUCCESS",
				"Spark request sent successfully",
				tcsBatchUploadService.triggerPowerBiSparkNoteBook(tenantId, tan, userEmail));
		logger.info("triggerPowerBiSparkNoteBook Done");
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping(value = "/spark/run/status")
	public ResponseEntity<ApiStatus<SparkState>> getSparkJobRunStatus(@RequestParam("runId") Integer runId)
			throws IOException, TanNotFoundException {
		ApiStatus<SparkState> apiStatus = new ApiStatus<SparkState>(HttpStatus.ACCEPTED, "SUCCESS", "NO ALERT",
				tcsBatchUploadService.getSparkJobRunStatus(runId));
		logger.info("Spark job run status Done");
		return new ResponseEntity<ApiStatus<SparkState>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * This API for get tcs batch upload details based on list of types
	 * 
	 * @param tan
	 * @param types
	 * @param tenantId
	 * @param year
	 * @return
	 */
	@GetMapping("/batch/types/{types}/{year}")
	public ResponseEntity<ApiStatus<List<BatchUploadDTO>>> listOfBatchUploadFiles(
			@RequestHeader(value = "TAN-NUMBER") String tan, @PathVariable(value = "types") List<String> types,
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @PathVariable Integer year) {
		MultiTenantContext.setTenantId(tenantId);
		List<BatchUploadDTO> listBatchUploads = tcsBatchUploadService.getListOfBatchUploadFileTypes(types, tan, year,
				tenantId);
		logger.info("REST response for batch upload data : {}", listBatchUploads);
		ApiStatus<List<BatchUploadDTO>> apiStatus = new ApiStatus<List<BatchUploadDTO>>(HttpStatus.OK, "SUCCESS",
				"LIST OF BATCH RECORDS", listBatchUploads);
		return new ResponseEntity<ApiStatus<List<BatchUploadDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param assessmentYear
	 * @param challanMonth
	 * @param type
	 * @param collectorTan
	 * @param pan
	 * @param tenantId
	 * @param userName
	 * @return
	 * @throws JsonProcessingException
	 */
	@PostMapping(value = "/batch/liability/caluclation")
	public ResponseEntity<ApiStatus<List<BatchUploadDTO>>> reverseLiabilityCaluclation(
			@RequestParam("year") Integer assessmentYear, @RequestParam("month") Integer challanMonth,
			@RequestParam("type") String type, @RequestHeader(value = "TAN-NUMBER") String collectorTan,
			@RequestHeader(value = "DEDUCTOR-PAN") String pan, @RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName) throws JsonProcessingException {

		List<TCSBatchUpload> batchData = tcsBatchUploadService.liabilityCaluclation(assessmentYear, collectorTan,
				tenantId, pan, challanMonth, userName, type);
		List<BatchUploadDTO> batchUploadDTO = tcsBatchUploadService.setBatchDto(batchData);
		String message = StringUtils.EMPTY;
		if(type.equalsIgnoreCase("refresh")) {
			message  = "NO ALERT";
		}else if(type.equalsIgnoreCase("REVERSE_LIABILITY")){ 
			message = "Liability caluclation started successfully ";
		}else {
			message = type + " caluclation happened successfully ";
		}
		logger.info("{}", message);
		ApiStatus<List<BatchUploadDTO>> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", message, batchUploadDTO);
		return new ResponseEntity<ApiStatus<List<BatchUploadDTO>>>(apiStatus, HttpStatus.OK);
	}

}
