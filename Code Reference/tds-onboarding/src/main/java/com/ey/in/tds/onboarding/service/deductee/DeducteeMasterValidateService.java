package com.ey.in.tds.onboarding.service.deductee;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DeducteeMasterValidateService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	// TODO : Fix this by moving away from deductee_master
	/*
	 * @Autowired private DeducteeMasterRepository deducteeMasterRepository;
	 */

//TODO NEED TO CHANGE FOR SQL

/*	public BatchUpload saveDeducteeInExel(String tan, Integer assessmentYear, Integer assessmentMonthPlusOne)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		logger.info("Processing deductee upload for tan : " + tan);
		BatchUpload batchUpload = null;
		try (XSSFWorkbook wb = new XSSFWorkbook()) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			XSSFSheet sheet = wb.createSheet("Deductee Details");

			XSSFRow row1 = sheet.createRow(0);

			// Create a cell
			row1.createCell(0).setCellValue("Deductee Name");
			row1.createCell(1).setCellValue("Deductee PAN");
			row1.createCell(2).setCellValue("Hash Code");
			row1.createCell(3).setCellValue("Status");

			XSSFCellStyle style4 = wb.createCellStyle();
			XSSFFont font4 = wb.createFont();
			style4.setFont(font4);
			style4.setLocked(false);

			// int rowindex = 1;
			// TODO : Fix this by moving away from deductee_master
			/*
			 * List<DeducteeMaster> deducteeList = deducteeMasterRepository.findAll();
			 * 
			 * for (DeducteeMaster listData : deducteeList) {
			 * 
			 * XSSFRow row2 = sheet.createRow(rowindex++);
			 * row2.createCell(0).setCellValue(listData.getName());
			 * row2.createCell(1).setCellValue(listData.getPan());
			 * row2.createCell(2).setCellValue(listData.getKey().getDeducteeMasterId().
			 * toString()); row2.createCell(3).setCellValue(""); }
			 */
	/*		wb.write(out);  

			// byteArrayOutputStream to file

//		File file = new File();
//		   FileInputStream input = new FileInputStream(file);
//		   MultipartFile multipartFile = new MockMultipartFile("file",
//		           file.getName(), "text/plain", IOUtils.toByteArray(input));

			convertFileToMultiPart(out);

			String type = "Validate";

//		String path = blobStorage.uploadExcelToBlob(multipartFile);
			String sha256 = "DFSDFSDFSD34ASF234234";

			List<BatchUpload> listBatch = batchUploadRepository.getSha256Records(sha256, Pagination.UNPAGED).getData();

			if (!listBatch.isEmpty()) {

//			BatchUpload.Key batchUploadKey = new BatchUpload.Key(assessmentYear, assessmentMonth, type, "Duplicate",
//					sha256, UUID.randomUUID());

				BatchUpload.Key batchUploadKey = new BatchUpload.Key(assessmentYear, tan, type, UUID.randomUUID());

				batchUpload = new BatchUpload();
				batchUpload.setReferenceId(listBatch.get(0).getKey().getId());
				batchUpload.setKey(batchUploadKey);
				batchUpload.setStatus("Duplicate");
			} else {
				BatchUpload.Key batchUploadKey = new BatchUpload.Key(assessmentYear, tan, type, UUID.randomUUID());
				batchUpload = new BatchUpload();
				batchUpload.setStatus("Uploaded");
				batchUpload.setKey(batchUploadKey);
//			logger.info("Unique record creating (" + file.getOriginalFilename() + ")");
			}

//		batchUpload.setFileName(file.getName());
//		batchUpload.setFilePath(path);
			batchUpload.setSha256sum(sha256);
			batchUpload.setCreatedDate(new Date());
			batchUpload.setCreatedBy("invoice  pdf user");
			batchUpload.setActive(true);
			batchUpload.setSuccessCount(0L);
			batchUpload.setFailedCount(0L);
			batchUpload.setRowsCount(0L);
			batchUpload.setProcessedCount(0);
			batchUpload.setDuplicateCount(0L);
			batchUpload.setMismatchCount(0L);
//		batchUpload = batchUploadRepository.save(batchUpload);
			wb.close();
		}
		return batchUpload;

	}  */

	private static MultipartFile convertFileToMultiPart(ByteArrayOutputStream out) throws IOException {
		FileOutputStream files = new FileOutputStream("abc.xlsx");
		out.writeTo(files);

		return null;
	}

	// TODO : Fix this by moving away from deductee_master
	/*
	 * public DeducteeMaster updateDeducteeDetails(String deductorTan, UUID batchId,
	 * String sha256,Integer assessmentYear) throws IOException { DeducteeMaster
	 * deductee = null; String status = "Uploaded"; String uploadType =
	 * "Deductee_pan_validation"; BatchUpload.Key batchUploadKey = new
	 * BatchUpload.Key(assessmentYear, deductorTan,uploadType, batchId);
	 * 
	 * Optional<BatchUpload> batchUpload =
	 * batchUploadRepository.findById(batchUploadKey); if (batchUpload.isPresent())
	 * { String path = batchUpload.get().getFilePath();
	 * 
	 * File file = new File(path);
	 * 
	 * MultipartFile multipartFile = new MockMultipartFile(file.getName(), new
	 * FileInputStream(file));
	 * 
	 * XSSFWorkbook workbook = new XSSFWorkbook(multipartFile.getInputStream());
	 * XSSFSheet worksheet = workbook.getSheetAt(0); int in = 1; while (in <=
	 * worksheet.getLastRowNum()) { XSSFRow row = worksheet.getRow(in++);
	 * logger.info(row.getCell(0).getStringCellValue());
	 * logger.info(row.getCell(1).getStringCellValue());
	 * logger.info(row.getCell(2).getStringCellValue());
	 * logger.info(row.getCell(3).getStringCellValue());
	 * logger.info(row.getCell(4).getStringCellValue());
	 * 
	 * if (row.getCell(2).getStringCellValue() != null) { UUID deducteeId =
	 * UUID.fromString(row.getCell(2).getStringCellValue()); //changes made as in
	 * hearders we are getting tan in header and deductor_master_tan is primary key
	 * //DeducteeMaster.Key deducteeMasterKey = new DeducteeMaster.Key(deducteeId);
	 * DeducteeMaster.Key deducteeMasterKey = new
	 * DeducteeMaster.Key(deductorTan,deducteeId); Optional<DeducteeMaster>
	 * deducteeMaster = deducteeMasterRepository.findById(deducteeMasterKey); if
	 * (deducteeMaster.isPresent()) { if
	 * (row.getCell(3).getStringCellValue().equalsIgnoreCase("Match") ||
	 * row.getCell(3).getStringCellValue().equalsIgnoreCase("Partial Match")) {
	 * deducteeMaster.get().setPanStatus("Verified");
	 * deducteeMaster.get().setPanVerifiedDate(new Date()); } else {
	 * deducteeMaster.get().setPanStatus("Not Verified");
	 * deducteeMaster.get().setPanVerifiedDate(new Date()); } deductee =
	 * deducteeMasterRepository.save(deducteeMaster.get()); } } } workbook.close();
	 * } return deductee; }
	 */
}
