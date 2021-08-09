package com.ey.in.tds.web.rest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aspose.cells.Cell;
import com.aspose.cells.Cells;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.MasterBatchUpload;
import com.ey.in.tds.common.service.MasterBlobStorageService;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.service.BaseResource;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.service.MasterBatchUploadService;

/**
 * 
 * @author vamsir
 *
 */
@CrossOrigin("*")
@RestController
@RequestMapping("/api/masters")
public class MasterBatchUploadResource extends BaseResource {

	@Autowired
	private MasterBatchUploadService masterBatchUploadService;

	@Autowired
	private MasterBlobStorageService masterBlobStorageService;

	/**
	 * This API for get batch upload records based on type and year.
	 * 
	 * @param tan
	 * @param type
	 * @param tenantId
	 * @param year
	 * @return
	 */
	@GetMapping("/master/batch/{type}/{year}")
	public ResponseEntity<ApiStatus<List<MasterBatchUpload>>> listOfBatchUploadFiles(@PathVariable String type,
			@PathVariable int year) {
		MultiTenantContext.setTenantId("master");
		List<MasterBatchUpload> listBatchUploads = masterBatchUploadService.getListOfBatchUploadFiles(type, year);
		logger.info("REST response for master batch upload data : {}", listBatchUploads);
		ApiStatus<List<MasterBatchUpload>> apiStatus = new ApiStatus<List<MasterBatchUpload>>(HttpStatus.OK, "SUCCESS",
				"LIST OF MASTER BATCH UPLOAD RECORDS", listBatchUploads);
		return new ResponseEntity<ApiStatus<List<MasterBatchUpload>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param deductorTan
	 * @param deductorPan
	 * @param typeOfDownload
	 * @param uploadType
	 * @param assesmentYear
	 * @param batchId
	 * @param tenantId
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/master/batch/download", consumes = { "application/x-www-form-urlencoded" })
	public ResponseEntity<Resource> downloadFile(@RequestParam("typeOfDownload") String typeOfDownload,
			@RequestParam("uploadType") String uploadType, @RequestParam("assesmentYear") Integer assesmentYear,
			@RequestParam("batchId") int batchId) throws Exception {
		MultiTenantContext.setTenantId("master");
		HttpHeaders header = new HttpHeaders();
		try {
			File file = null;
			Integer intgerBatchId = Integer.valueOf(batchId);
			logger.info("typeOfDownload :{} uploadType :{}", typeOfDownload, uploadType);
			MasterBatchUpload masterBatch = masterBatchUploadService.getFileDetails(assesmentYear, uploadType,
					intgerBatchId);
			if (masterBatch == null) {
				throw new CustomException("No Record found for Batch Upload", HttpStatus.BAD_REQUEST);
			}
			if (typeOfDownload.equalsIgnoreCase("UPLOADED")) {
				file = masterBlobStorageService.getFileFromBlobUrl(masterBatch.getFilePath());
			} else if (typeOfDownload.equalsIgnoreCase("SUCCESS")) {
				file = masterBlobStorageService.getFileFromBlobUrl(masterBatch.getSuccessFileUrl());
			} else if (typeOfDownload.equalsIgnoreCase("OTHER")) {
				file = masterBlobStorageService.getFileFromBlobUrl(masterBatch.getOtherFileUrl());
			} else if (typeOfDownload.equalsIgnoreCase("ERROR")) {
				if (StringUtils.isBlank(masterBatch.getErrorFilePath())) {
					throw new CustomException("No errors exists");
				}
				file = masterBlobStorageService.getFileFromBlobUrl(masterBatch.getErrorFilePath());
			}
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
			cell.setValue(e.getMessage());
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

}
