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
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.tika.Tika;
import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
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
import com.ey.in.tds.common.domain.transactions.ReconciliationTriggerRequest;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.dto.BatchProcessDTO;
import com.ey.in.tds.common.dto.sftp.SftpPathDTO;
import com.ey.in.tds.common.ingestion.response.dto.BatchUploadResponseDTO;
import com.ey.in.tds.common.ingestion.response.dto.SparkState;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.common.service.BlobStorageService;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.TanNotFoundException;
import com.ey.in.tds.core.security.AllowedMimeTypes;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.TenantConfiguration;
import com.ey.in.tds.ingestion.dto.batch.BatchUploadDTO;
import com.ey.in.tds.ingestion.dto.batch.BatchUploadGroupCustomDTO;
import com.ey.in.tds.ingestion.dto.batch.ReceiptBatchUploadDTO;
import com.ey.in.tds.ingestion.dto.batchgroup.BatchUploadGroupDTO;
import com.ey.in.tds.ingestion.service.batchupload.BatchUploadService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.microsoft.azure.storage.StorageException;

@RestController
@RequestMapping("/api/ingestion")
public class BatchUploadController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private BatchUploadService batchUploadService;

	@Autowired
	BlobStorageService blobStorageService;

	@Autowired
	TenantProperties tenantProperties;

	/**
	 * getting batch upload by file type and year
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
		MultiTenantContext.setTenantId(tenantId);
		List<BatchUploadDTO> listBatchUploads = batchUploadService
				.getListOfBatchUploadFiles(type, Arrays.asList(tan), Integer.parseInt(year), Pagination.UNPAGED);
				
		logger.info("REST response for batch upload data : {}", listBatchUploads);
		ApiStatus<List<BatchUploadDTO>> apiStatus = new ApiStatus<List<BatchUploadDTO>>(HttpStatus.CREATED, "SUCCESS",
				"LIST OF BATCH RECORDS", listBatchUploads);
		return new ResponseEntity<ApiStatus<List<BatchUploadDTO>>>(apiStatus, HttpStatus.OK);
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
	 * @throws InvalidFormatException
	 * @throws ParseException
	 * @throws ValidationException
	 * @throws IntrusionException
	 */
	@PostMapping("/batch/excel")
	public ResponseEntity<ApiStatus<BatchUpload>> uploadExcel(@RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestParam("year") int assessmentYear, @RequestParam(value = "type") String type,
			@RequestParam("files") MultipartFile[] files, @RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestParam(value = "keywordId", required = false) Integer keywordId, HttpServletRequest request,
			@RequestHeader(value = "USER_NAME") String userName)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException, InvalidFormatException,
			ParseException, IntrusionException, ValidationException {
		// We have the data in redis for the User and this will be used in Spark
		// Notebooks.
		boolean validFiles = true;
		if (files != null && files.length > 0) {
			for (MultipartFile file : files) {
				String contentType = new Tika().detect(file.getInputStream(),file.getOriginalFilename());
				if (!AllowedMimeTypes.XLSX.getMimeType().equals(contentType)
						&& !AllowedMimeTypes.OOXML.getMimeType().equals(contentType)
						&& !AllowedMimeTypes.CSV.getMimeType().equals(contentType)) {
					validFiles = false;
				}
			}
			if (!validFiles) {
				logger.warn("Unsupported file upload attempt by {}", userName);
				throw new CustomException("Invalid file contents, only xlsx and csv files are allowed", HttpStatus.BAD_REQUEST);
			}

			logger.info("Upload Params: {}, {}, {}, {}, {}", tan, assessmentYear, type, files.length, deductorPan);

			String userEmail = request.getHeader("USER_NAME");
			String token = request.getHeader("Authorization");
			BatchUpload batchData = batchUploadService.saveUploadExcel(assessmentYear, files, tan, type, tenantId,
					userEmail, deductorPan, keywordId, token);
			ApiStatus<BatchUpload> apiStatus = new ApiStatus<BatchUpload>(HttpStatus.CREATED, "SUCCESS", "NO ALERT",
					batchData);
			return new ResponseEntity<ApiStatus<BatchUpload>>(apiStatus, HttpStatus.OK);

		} else {
			throw new IllegalArgumentException("Atleast one file must be uploaded.");
		}
	}

	@PostMapping("/batch/excel/panvalidation")
	public ResponseEntity<ApiStatus<BatchUploadResponseDTO>> uploadExcel(@RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestParam("year") int assessmentYear, @RequestParam("type") String type,
			@PathVariable("file") MultipartFile file, @RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userEmail, @RequestHeader("DEDUCTOR-PAN") String deductorPan,
			@RequestHeader(value = "USER_NAME") String userName)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException, InvalidFormatException,
			ParseException, IntrusionException, ValidationException {
			String contentType = new Tika().detect(file.getInputStream());
			if (AllowedMimeTypes.XLSX.getMimeType().equals(contentType)
				|| AllowedMimeTypes.OOXML.getMimeType().equals(contentType)) {
			// We have the data in redis for the User and this will be used in Spark
			// Notebooks.
			// As of now only single file upload allowed for pan validation
			MultipartFile[] files = new MultipartFile[1];
			files[0] = file;

			BatchUpload batchData = batchUploadService.saveUploadExcel(assessmentYear, files, tan, type, tenantId,
					userEmail, deductorPan, null, null);
			BatchUploadResponseDTO response=batchUploadService.copyToResponseDTO(batchData);
			ApiStatus<BatchUploadResponseDTO> apiStatus = new ApiStatus<BatchUploadResponseDTO>(HttpStatus.CREATED, "SUCCESS",
					"FILE UPLOADED SUCCESSFULLY", response);
			return new ResponseEntity<ApiStatus<BatchUploadResponseDTO>>(apiStatus, HttpStatus.OK);
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
	public ResponseEntity<ApiStatus<BatchUploadResponseDTO>> uploadPDF(@RequestHeader(value = "TAN-NUMBER") String tan,
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

			BatchUpload batchData = batchUploadService.saveUploadPDF(assessmentYear, files, tan, type, tenantId,
					userName, groupName, postingDate, Pagination.UNPAGED, residentType, deductorPan);
			BatchUploadResponseDTO response=new BatchUploadResponseDTO();
			BeanUtils.copyProperties(batchData, response);
			ApiStatus<BatchUploadResponseDTO> apiStatus = new ApiStatus<BatchUploadResponseDTO>(HttpStatus.CREATED, "SUCCESS",
					"FILE UPLOADED SUCCESSFULLY", response);
			return new ResponseEntity<ApiStatus<BatchUploadResponseDTO>>(apiStatus, HttpStatus.OK);
		} else {
			logger.warn("Unsupported file upload attempt by {}", userName);
			throw new IllegalArgumentException("Atleast one file must be uploaded.");
		}
	}

	// Author code ended

	@PostMapping("/batchupload")
	public BatchUploadResponseDTO create(@RequestBody BatchUpload batchUpload,
			@RequestHeader(value = "TAN-NUMBER") String deductorTan) {
		batchUpload.setDeductorMasterTan(deductorTan);
		logger.info("Creating the Batch Upload {}", batchUpload);
		 batchUpload=batchUploadService.create(batchUpload);
		 return batchUploadService.copyToResponseDTO(batchUpload);
	}

	@GetMapping("/batchupload")
	public BatchUploadResponseDTO get(@RequestParam("assessmentYear") int assessmentYear, @RequestParam("month") int month,
			@RequestParam("type") String type, @RequestParam("status") String status,
			@RequestParam("sha256sum") String sha256sum, @RequestParam("id") Integer id,
			@RequestHeader(value = "TAN-NUMBER") String deductorTan) {
		// test
		BatchUpload batchUpload= batchUploadService.get(assessmentYear, deductorTan, type, id);
		return batchUploadService.copyToResponseDTO(batchUpload);
	}

	@PutMapping("/batchupload")
	public BatchUploadResponseDTO update(@RequestBody BatchUpload batchUpload,
			@RequestHeader(value = "TAN-NUMBER") String deductorTan) {
		batchUpload.setDeductorMasterTan(deductorTan);
		logger.info("Updating the Batch Upload : {}", batchUpload);
		batchUpload=batchUploadService.update(batchUpload);
		return batchUploadService.copyToResponseDTO(batchUpload);
	}

	@DeleteMapping("/batchupload")
	public void delete(@RequestParam("assessmentYear") int assessmentYear, @RequestParam("month") int month,
			@RequestParam("type") String type, @RequestParam("status") String status,
			@RequestParam("sha256sum") String sha256sum, @RequestParam("id") Integer id,
			@RequestHeader(value = "TAN-NUMBER") String deductorTan) {
		logger.info("Deleting the Batch Upload id : {}", id);
		batchUploadService.deleteById(assessmentYear, deductorTan, type, id);
	}

	@PostMapping(value = "/batch/sftp/to/blob", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<BatchUploadResponseDTO>> uploadSftpToBlob(@RequestBody SftpPathDTO sftpPathDTO,
			@RequestHeader(value = "X-TENANT-ID", required = false) String tenantName)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException, ParseException {
		// We have the data in redis for the User and this will be used in Spark

		// Based on userEmail need to get TAN and PAN for batch

		logger.info("SAP file data : {}", sftpPathDTO);
		logger.info("SAP tenant id : {}", tenantName);
		BatchUpload batchData = null;
		BatchUploadResponseDTO response=null;
		if (StringUtils.isEmpty(tenantName) && !sftpPathDTO.getUserEmail().isEmpty()) {
			tenantName = sftpPathDTO.getUserEmail().substring(sftpPathDTO.getUserEmail().lastIndexOf("@") + 1,
					sftpPathDTO.getUserEmail().length());
		}
		MultiTenantContext.setTenantId(tenantName);
		if (sftpPathDTO.getSrcPath().contains("Input") || sftpPathDTO.getSrcPath().contains("morphes")) {
			TenantConfiguration.BlobStorage blobStorage = tenantProperties.getConfiguration(tenantName).getBlobStorage();
			String filePath = StringUtils.EMPTY;
			if (sftpPathDTO.getSrcPath().contains("morphes")) {
				filePath = sftpPathDTO.getSrcPath().split("morphes\\\\")[1].replace("\\", "/");
			} else {
				filePath = sftpPathDTO.getSrcPath().split("Input\\\\")[1].replace("\\", "/");
			}
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
			
			String type = getUploadType(fileName, sftpPathDTO.getSrcPath());

			logger.info("type : {}", type);
			batchData = batchUploadService.uploadBlobUrlToBatch(CommonUtil.getAssessmentYear(null), multipartFile, tan,
					type, tenantName, sftpPathDTO.getUserEmail(), blobURL, pan, filePath);
			
			response = batchUploadService.copyToResponseDTO(batchData);
			ApiStatus<BatchUploadResponseDTO> apiStatus = new ApiStatus<BatchUploadResponseDTO>(HttpStatus.CREATED, "SUCCESS",
					"FILE UPLOADED SUCCESSFULLY", response);
			return new ResponseEntity<ApiStatus<BatchUploadResponseDTO>>(apiStatus, HttpStatus.OK);
		} else {
			batchData = batchUploadService.uploadInvoicePdfBlobUrlToBatch(sftpPathDTO, tenantName);
		}
		logger.info("SAP_FTP Batch Upload {}", batchData);
		
		ApiStatus<BatchUploadResponseDTO> apiStatus = new ApiStatus<BatchUploadResponseDTO>(HttpStatus.CREATED, "SUCCESS",
				"FILE UPLOADED SUCCESSFULLY", response);
		return new ResponseEntity<ApiStatus<BatchUploadResponseDTO>>(apiStatus, HttpStatus.OK);	
	}
	
	public String getUploadType(String fileName, String filePath) {
		String type = "";
		if (filePath.contains("Input")) {
			if (fileName.contains("ADVANCE")) {
				type = "ADVANCE_SAP";
			} else if (fileName.contains("INVOICE")) {
				type = "INVOICE_SAP";
			} else if (fileName.contains("GL")) {
				type = "GL_SAP";
			} else if (fileName.contains("PROVISION")) {
				type = "PROVISION_SAP";
			} else if (fileName.contains("DEDUCTEE_RESIDENT") || fileName.contains("NONRESIDENT")) {
				type = "DEDUCTEE_SAP";
			}
		} else {
			if (fileName.contains("ADVANCE")) {
				type = "ADVANCES";
			} else if (fileName.contains("INVOICE")) {
				type = "INVOICE_EXCEL";
			} else if (fileName.contains("GL")) {
				type = "GL";
			} else if (fileName.contains("PROVISION")) {
				type = "PROVISIONS";
			} else if (fileName.contains("DEDUCTEE_RESIDENT") || fileName.contains("NONRESIDENT")) {
				type = "DEDUCTEE_EXCEL";
			}
		}
		return type;
	}

	// PDF Process
	@GetMapping("batch/group/{year}")
	public ResponseEntity<ApiStatus<List<BatchUploadGroupDTO>>> getGroupRecordsByYear(
			@RequestHeader(value = "TAN-NUMBER") String tan, @PathVariable Integer year) {
		List<BatchUploadGroupDTO> listOfGroup = batchUploadService.getGroupsBasedOnTan(tan, year);
		logger.info("REST response for batch upload group data : {}", listOfGroup);
		ApiStatus<List<BatchUploadGroupDTO>> apiStatus = new ApiStatus<List<BatchUploadGroupDTO>>(HttpStatus.CREATED,
				"SUCCESS", "LIST OF Group Records", listOfGroup);
		return new ResponseEntity<ApiStatus<List<BatchUploadGroupDTO>>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping("batch/group/all/{groupId}")
	public ResponseEntity<ApiStatus<List<BatchUploadGroupCustomDTO>>> getBatchUploadRecordsBasedOnTanAndBatchGroupId(
			@RequestHeader(value = "TAN-NUMBER") String tan, @PathVariable Integer groupId) {
		List<BatchUploadGroupCustomDTO> batchDataBasedOnGroupId = batchUploadService.getBatchDataByGroupId(tan, groupId,
				Pagination.DEFAULT);
		ApiStatus<List<BatchUploadGroupCustomDTO>> apiStatus = new ApiStatus<List<BatchUploadGroupCustomDTO>>(
				HttpStatus.CREATED, "SUCCESS", "LIST OF BATCH RECORDS BASED ON BATCH GROUP ID",
				batchDataBasedOnGroupId);
		return new ResponseEntity<ApiStatus<List<BatchUploadGroupCustomDTO>>>(apiStatus, HttpStatus.OK);
	}

	// Test blob api for uploading file to blob
	@PostMapping(value = "/upload/blob", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<String>> uploadFileTOBlob(@RequestParam("file") MultipartFile file,
			@RequestHeader(value = "X-TENANT-ID") String tenantId)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException {

		String blobUrl = batchUploadService.uploadFileTOBlob(file, tenantId);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.CREATED, "SUCCESS", "FILE UPLOADED SUCCESSFULLY",
				blobUrl);
		logger.info("Creating the Batch Upload {}", blobUrl);
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}
	
	@GetMapping("/batch/blob")
	public ResponseEntity<ApiStatus<BatchUploadResponseDTO>> uploadBlobUrlToBatch(@RequestParam("blobUrl") String blobUrl,
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

		BatchUpload batchData = batchUploadService.uploadBlobUrlToBatch(CommonUtil.getAssessmentYear(null),
				multipartFile, tan, type, tenantId, userEmail, blobUrl, deductorPan,
				multipartFile.getOriginalFilename());
		BatchUploadResponseDTO response=batchUploadService.copyToResponseDTO(batchData);
		ApiStatus<BatchUploadResponseDTO> apiStatus = new ApiStatus<BatchUploadResponseDTO>(HttpStatus.CREATED, "SUCCESS",
				"FILE UPLOADED SUCCESSFULLY", response);
		logger.info("Creating the Batch Upload {}", batchData);
		return new ResponseEntity<ApiStatus<BatchUploadResponseDTO>>(apiStatus, HttpStatus.OK);
	}

	@GetMapping("/receipt/{type}/{year}")
	public ResponseEntity<ApiStatus<List<ReceiptBatchUploadDTO>>> getReceiptDataBasedOnBatchUploadId(
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader("TAN-NUMBER") String tan,
			@PathVariable int year, @PathVariable String type) {

		List<ReceiptBatchUploadDTO> response = batchUploadService.getReceiptDataBasedOnBatchId(type, year,
				Arrays.asList(tan), tenantId);
		ApiStatus<List<ReceiptBatchUploadDTO>> apiStatus = new ApiStatus<List<ReceiptBatchUploadDTO>>(
				HttpStatus.CREATED, "SUCCESS", "LIST OF RECEIPT AND BATCH UPLOAD DATA", response);
		return new ResponseEntity<ApiStatus<List<ReceiptBatchUploadDTO>>>(apiStatus, HttpStatus.OK);
	}

	@PostMapping("/batchprocess")
	public ResponseEntity<ApiStatus<BatchUploadResponseDTO>> batchProcess(@RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader(value = "USER_NAME") String userEmail,
			@RequestBody BatchProcessDTO batchProcessDTO) throws JsonProcessingException, ParseException {
		BatchUpload batchData = batchUploadService.batchProcess(userEmail, tan, tenantId, batchProcessDTO);
		BatchUploadResponseDTO response=batchUploadService.copyToResponseDTO(batchData);
		ApiStatus<BatchUploadResponseDTO> apiStatus = new ApiStatus<BatchUploadResponseDTO>(HttpStatus.CREATED, "SUCCESS",
				"FILE UPLOADED SUCCESSFULLY", response);
		return new ResponseEntity<ApiStatus<BatchUploadResponseDTO>>(apiStatus, HttpStatus.OK);
	}
	
	/**
	 * 
	 * @param tan
	 * @param tenantId
	 * @param userEmail
	 * @param batchProcessDTO
	 * @return
	 * @throws JsonProcessingException
	 * @throws ParseException 
	 */
	@PostMapping("/batchprocess/interest")
	public ResponseEntity<ApiStatus<BatchUpload>> interestBatchProcess(@RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestHeader("X-TENANT-ID") String tenantId, @RequestHeader(value = "USER_NAME") String userEmail,
			@RequestBody BatchProcessDTO batchProcessDTO) throws JsonProcessingException, ParseException {
		BatchUpload batchData = batchUploadService.batchProcess(userEmail, tan, tenantId, batchProcessDTO);
		ApiStatus<BatchUpload> apiStatus = new ApiStatus<BatchUpload>(HttpStatus.CREATED, "SUCCESS",
				"FILE UPLOADED SUCCESSFULLY", batchData);
		return new ResponseEntity<ApiStatus<BatchUpload>>(apiStatus, HttpStatus.OK);
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
	public ResponseEntity<ApiStatus<BatchUploadResponseDTO>> uploadReconciliationExcel(
			@RequestHeader(value = "TAN-NUMBER") String tan, @RequestParam("year") int assessmentYear,
			@RequestParam("month") int assessmentMonth, @RequestParam("fileType") String fileType,
			@RequestParam("file") MultipartFile file, @RequestHeader(value = "X-TENANT-ID") String tenantId,
			HttpServletRequest request)
			throws InvalidKeyException, URISyntaxException, StorageException, IOException, InvalidFormatException {
		String userEmail = request.getHeader("USER_NAME");
		String token = request.getHeader("Authorization");
		BatchUpload batchData = batchUploadService.saveUploadExcelForReconciliation(assessmentYear, assessmentMonth,
				file, tan, tenantId, userEmail, token, fileType);
		BatchUploadResponseDTO response=batchUploadService.copyToResponseDTO(batchData);
		ApiStatus<BatchUploadResponseDTO> apiStatus = new ApiStatus<BatchUploadResponseDTO>(HttpStatus.CREATED, "SUCCESS",
				"FILE UPLOADED SUCCESSFULLY", response);
		return new ResponseEntity<ApiStatus<BatchUploadResponseDTO>>(apiStatus, HttpStatus.OK);
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
				batchUploadService.processReconciliation(assessmentYear, tan, userEmail, tenantId));
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
	public ResponseEntity<InputStreamResource> exportNOPKeywords(
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

		ByteArrayInputStream in = batchUploadService.exportNOPKeywords(tan, tenantId, pan, year, month);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "attachment; filename=Keywords_Report_" + tan + ".xlsx");

		logger.info("NOP keywords report export done ");
		return ResponseEntity.ok().headers(headers).body(new InputStreamResource(in));
	}

	@GetMapping(value = "/spark/notebook/powerbi")
	public ResponseEntity<ApiStatus<String>> triggerPowerBiSparkNoteBook(
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestHeader(value = "USER_NAME") String userEmail) throws IOException, TanNotFoundException {

		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.ACCEPTED, "SUCCESS",
				"Spark request sent successfully",
				batchUploadService.triggerPowerBiSparkNoteBook(tenantId, tan, userEmail));

		logger.info("triggerPowerBiSparkNoteBook Done");
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}
	/**
	 * to get the job status of processing transactions
	 * @param runId
	 * @return
	 * @throws IOException
	 * @throws TanNotFoundException
	 */

	@GetMapping(value = "tds/spark/run/status")
	public ResponseEntity<ApiStatus<SparkState>> getSparkJobRunStatus(@RequestParam("runId") Integer runId)
			throws IOException, TanNotFoundException {
		ApiStatus<SparkState> apiStatus = new ApiStatus<SparkState>(HttpStatus.ACCEPTED, "SUCCESS",
				"NO ALERT", batchUploadService.getSparkJobRunStatus(runId));
		logger.info("Spark job run status Done");
		return new ResponseEntity<ApiStatus<SparkState>>(apiStatus, HttpStatus.OK);
	}
	
	
	/** * to process the interest file  and update the table
	 * 
	 * @param file
	 * @param userName
	 * @param tenantId
	 * @param assessmentYear
	 * @param tan
	 * @param pan
	 * @param token
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/batch/interesetCalculationReport/import")
	public ResponseEntity<ApiStatus<String>> processInterestCalculationReport(@RequestParam("files") MultipartFile file,
			@RequestHeader(value = "USER_NAME") String userName, @RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestParam("type")String uploadType ,
			@RequestParam("year") int assessmentYear,
			@RequestHeader(value = "TAN-NUMBER") String tan,
			@RequestHeader(value = "DEDUCTOR-PAN") String pan,
			@RequestHeader(value = "Authorization", required = false) String token) throws Exception {
		if (file == null) {
			if (logger.isErrorEnabled()) {
				logger.error("File Not Found");
			}
			throw new CustomException("file is not imported", HttpStatus.BAD_REQUEST);
		}
		Integer month=Calendar.getInstance().get(Calendar.MONTH)+1;
		String contentType = new Tika().detect(file.getInputStream(), file.getOriginalFilename());
		if (AllowedMimeTypes.XLSX.getMimeType().equals(contentType)
				|| AllowedMimeTypes.OOXML.getMimeType().equals(contentType)) {
			batchUploadService.asyncProcessInterestComputationFile(tenantId, userName, tan, pan, assessmentYear,month,
					 file,uploadType);
			;
			ApiStatus<String> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS",
					"Invoice Interest Computation file uploaded successfully", "SUCCESS");
			return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
		} else {
			logger.warn("Unsupported file upload attempt by {}", userName);
			throw new CustomException("Invalid file contents, only xlsx files are allowed", HttpStatus.BAD_REQUEST);
		}
	}
	
	/**
	 * 
	 * @param assessmentYear
	 * @param challanMonth
	 * @param type
	 * @param deductorTan
	 * @param pan
	 * @param tenantId
	 * @param userName
	 * @return
	 * @throws JsonProcessingException
	 * @throws ParseException 
	 */
	@PostMapping(value = "/batch/liability/caluclation")
	public ResponseEntity<ApiStatus<List<BatchUploadDTO>>> reverseLiabilityCaluclation(
			@RequestParam("year") Integer assessmentYear, @RequestParam("month") Integer challanMonth,
			@RequestParam("type") String type, @RequestHeader(value = "TAN-NUMBER") String deductorTan,
			@RequestHeader(value = "DEDUCTOR-PAN") String pan, @RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName) throws JsonProcessingException, ParseException {

		List<BatchUpload> batchData = batchUploadService.liabilityCaluclation(assessmentYear, deductorTan,
				tenantId, pan, challanMonth, userName, type);
		List<BatchUploadDTO> batchUploadDTO = batchUploadService.setBatchDto(batchData);
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
	
	@PostMapping(value = "/batch/nrliability/caluclation")
	public ResponseEntity<ApiStatus<List<BatchUploadDTO>>> nrLiabilityCaluclation(
			@RequestParam("year") Integer assessmentYear, @RequestParam("month") Integer challanMonth,
			@RequestParam("type") String type, @RequestHeader(value = "TAN-NUMBER") String deductorTan,
			@RequestHeader(value = "DEDUCTOR-PAN") String pan, @RequestHeader(value = "X-TENANT-ID") String tenantId,
			@RequestHeader(value = "USER_NAME") String userName) throws JsonProcessingException, ParseException {

		List<BatchUpload> batchData = batchUploadService.nrLiabilityCaluclation(assessmentYear, deductorTan,
				tenantId, pan, challanMonth, userName, type);
		List<BatchUploadDTO> batchUploadDTO = batchUploadService.setBatchDto(batchData);
		String message = StringUtils.EMPTY;
		if (type.equalsIgnoreCase("refresh")) {
			message = "NO ALERT";
		} else if (type.equalsIgnoreCase("NR_REVERSE_LIABILITY")) {
			message = "NR Liability caluclation started successfully ";
		} else {
			message = type + " NR caluclation happened successfully ";
		}
		logger.info("{}", message);
		ApiStatus<List<BatchUploadDTO>> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", message, batchUploadDTO);
		return new ResponseEntity<ApiStatus<List<BatchUploadDTO>>>(apiStatus, HttpStatus.OK);
	}
	
	/**
	 * 
	 * @param tenantId
	 * @param tan
	 * @param userName
	 * @param deductorPan
	 * @param token
	 * @param requestParam
	 * @return
	 * @throws Exception 
	 */
	@PostMapping("/batchprocess/retrigger")
	public ResponseEntity<ApiStatus<String>> getBatchUploadDetails(@RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader("TAN-NUMBER") String tan, @RequestHeader(value = "USER_NAME") String userName,
			@RequestHeader(value = "DEDUCTOR-PAN") String deductorPan, @RequestHeader("Authorization") String token,
			@RequestBody Map<String, String> requestParam) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		int batchId = Integer.parseInt(requestParam.get("id"));
		int assessmentYear = Integer.parseInt(requestParam.get("assessmentYear"));
		String fileType = requestParam.get("fileType");
		String response = batchUploadService.getBatchUploadDetails(fileType, batchId, tan, assessmentYear, tenantId,
				userName, deductorPan, token);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS", "NO ALERT", response);
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}
	
	
	/**
	 * 
	 * @param tenantId
	 * @param tan
	 * @param assessmentYear
	 * @param assessmentMonth
	 * @param deductorPanfileType
	 * @param userName
	 * @return
	 */
	@PostMapping("/batchprocess/revert")
	public ResponseEntity<ApiStatus<String>> revertAllMismatchs(@RequestHeader("X-TENANT-ID") String tenantId,
			@RequestHeader("TAN-NUMBER") String tan, @RequestBody Map<String, String> requestParam,
			@RequestHeader(value = "DEDUCTOR-PAN") String deductorPan,
			@RequestHeader(value = "USER_NAME") String userName) {
		MultiTenantContext.setTenantId(tenantId);
		int assessmentMonth = Integer.parseInt(requestParam.get("assessmentMonth"));
		int assessmentYear = Integer.parseInt(requestParam.get("assessmentYear"));
		String fileType = requestParam.get("fileType");
		String response = batchUploadService.revertAllMismatchs(assessmentMonth, tan, assessmentYear, deductorPan,
				fileType);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS", "NO ALERT", response);
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param tan
	 * @param deductorPan
	 * @param year
	 * @param tenantId
	 * @param userName
	 * @param type
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/batch/notds/reports")
	public ResponseEntity<ApiStatus<String>> getReportsByType(@RequestHeader(value = "TAN-NUMBER") String deductorTan,
			@RequestHeader("DEDUCTOR-PAN") String deductorPan, @RequestParam(value = "batchId") int batchId,
			@RequestHeader(value = "X-TENANT-ID") String tenantId, @RequestHeader(value = "USER_NAME") String userName,
			@RequestParam(value = "type") String type, @RequestParam(value = "year") int year) throws Exception {
		MultiTenantContext.setTenantId(tenantId);
		String message = batchUploadService.getNotdsReport(deductorTan, batchId, deductorPan, tenantId, type, userName, year);
		ApiStatus<String> apiStatus = new ApiStatus<String>(HttpStatus.OK, "SUCCESS",
				"NOTDS Report generated successfully", message);
		return new ResponseEntity<ApiStatus<String>>(apiStatus, HttpStatus.OK);
	}
	
}
