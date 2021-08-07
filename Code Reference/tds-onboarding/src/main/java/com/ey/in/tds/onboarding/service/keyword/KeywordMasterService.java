package com.ey.in.tds.onboarding.service.keyword;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aspose.cells.AutoFilter;
import com.aspose.cells.BackgroundType;
import com.aspose.cells.CellsHelper;
import com.aspose.cells.Color;
import com.aspose.cells.ImportTableOptions;
import com.aspose.cells.Range;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Style;
import com.aspose.cells.TextAlignmentType;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.ey.in.tds.common.domain.NatureOfPaymentMaster;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.BatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.KeywordMasterDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.KeywordMaster;
import com.ey.in.tds.common.dto.kwywords.KeywordsErrorDto;
import com.ey.in.tds.common.model.keywordmaster.KeywordMasterDTO;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.common.util.CommonValidationsCassandra;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.feign.client.MastersClient;
import com.microsoft.azure.storage.StorageException;

@Service
public class KeywordMasterService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private KeywordMasterDAO keywordMasterDAO;

	@Autowired
	private BlobStorage blob;

	@Autowired
	private MastersClient mastersClient;

	@Autowired
	private BatchUploadDAO batchUploadDAO;

	public KeywordMaster createdKeywordMaster(KeywordMasterDTO keywordMasterDTO, String userName, String deductorPan) {
		if (logger.isDebugEnabled()) {
			logger.debug("request to create Keyword Master: {}", keywordMasterDTO);
		}

		List<KeywordMaster> keyWordMasterDb = keywordMasterDAO
				.findByNatureOfPaymentId(keywordMasterDTO.getNatureOfPaymentId().intValue(), deductorPan);
		for (KeywordMaster keyWord : keyWordMasterDb) {
			CommonValidationsCassandra.validateApplicableFields(keyWord.getApplicableTo(),
					keywordMasterDTO.getApplicableFrom());
		}

		KeywordMaster keywordMaster = new KeywordMaster();
		keywordMaster.setActive(true);
		keywordMaster.setCreatedBy(userName);
		keywordMaster.setCreatedDate(new Date());
		keywordMaster.setUpdatedBy(userName);
		keywordMaster.setUpdatedDate(new Date());
		keywordMaster.setApplicableFrom(keywordMasterDTO.getApplicableFrom());
		keywordMaster.setApplicableTo(keywordMasterDTO.getApplicableTo());
		keywordMaster.setNatureOfPaymentId(keywordMasterDTO.getNatureOfPaymentId());
		keywordMaster.setNatureOfPayment(keywordMasterDTO.getNatureOfPaymentName());
		keywordMaster.setKeywords(keywordMasterDTO.getKeyWords());
		keywordMaster.setPan(deductorPan);
		if ("NR".equalsIgnoreCase(keywordMasterDTO.getResOrNr())) {
			keywordMaster.setIsForNonResident(true);
		} else if ("RES".equalsIgnoreCase(keywordMasterDTO.getResOrNr())) {
			keywordMaster.setIsForNonResident(false);
		}
		keywordMaster = keywordMasterDAO.save(keywordMaster);
		return keywordMaster;
	}

	public KeywordMaster updateKeywordMaster(KeywordMasterDTO keywordMasterDTO, String userName, String deductorPan) {
		if (logger.isDebugEnabled()) {
			logger.debug("request to update Keyword Master: {}", keywordMasterDTO);
		}

		List<KeywordMaster> keywordMasterOptional = keywordMasterDAO.findById(keywordMasterDTO.getId(), deductorPan);
		KeywordMaster keywordMaster = null;
		if (keywordMasterOptional != null) {
			keywordMaster = keywordMasterOptional.get(0);
			keywordMaster.setId(keywordMasterDTO.getId());
			keywordMaster.setActive(keywordMasterDTO.getActive());
			keywordMaster.setUpdatedBy(userName);
			keywordMaster.setUpdatedDate(new Date());
			keywordMaster.setKeywords(keywordMasterDTO.getKeyWords());
			// Changed the Datatype to List.
			/*
			 * if (keywordMasterDTO.getKeyWords() != null) { String[] keywords =
			 * keywordMasterDTO.getKeyWords().split(","); keywordMaster.setKeywords(new
			 * HashSet<String>(Arrays.asList(keywords))); }
			 */
			keywordMaster.setNatureOfPaymentId(keywordMasterDTO.getNatureOfPaymentId());
			keywordMaster.setNatureOfPayment(keywordMasterDTO.getNatureOfPaymentName());
			keywordMaster.setApplicableFrom(keywordMasterDTO.getApplicableFrom());
			keywordMaster.setApplicableTo(keywordMasterDTO.getApplicableTo());
			keywordMaster = keywordMasterDAO.update(keywordMaster);
		} else {
			throw new CustomException("No Record Present for Keyword Master to update the Record",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return keywordMaster;
	}

	/**
	 * 
	 * @param deductorPan
	 * @param id
	 * @return
	 */
	public KeywordMasterDTO keywordMasterGetById(String deductorPan, Integer id) {
		KeywordMasterDTO keywordMasterDTO = new KeywordMasterDTO();

		List<KeywordMaster> keywordMasterOptional = keywordMasterDAO.findById(id, deductorPan);
		KeywordMaster keywordMaster = null;
		if (keywordMasterOptional != null) {
			keywordMaster = keywordMasterOptional.get(0);
			keywordMasterDTO.setActive(keywordMaster.getActive());
			keywordMasterDTO.setApplicableFrom(keywordMaster.getApplicableFrom());
			keywordMasterDTO.setApplicableTo(keywordMaster.getApplicableTo());
			keywordMasterDTO.setNatureOfPaymentId(keywordMaster.getNatureOfPaymentId());
			keywordMasterDTO.setNatureOfPaymentName(keywordMaster.getNatureOfPayment());
			keywordMasterDTO.setId(keywordMaster.getId());
			if (keywordMaster.getIsForNonResident().equals(true)) {
				keywordMasterDTO.setResOrNr("NR");
			} else if (keywordMaster.getIsForNonResident().equals(false)) {
				keywordMasterDTO.setResOrNr("RES");
			}
			if (keywordMaster.getKeywords() != null) {
				keywordMasterDTO.setKeyWords(String.join(",", keywordMaster.getKeywords()));
			}
		}
		return keywordMasterDTO;
	}

	/**
	 * 
	 * @return List<KeywordMasterDTO
	 */
	public List<KeywordMasterDTO> getAllKeywordMaster(String deductorPan) {
		List<KeywordMasterDTO> keywordMasterListDTO = new ArrayList<>();
		List<KeywordMaster> keywordMaster = keywordMasterDAO.findAllByDeductorPan(deductorPan);

		for (KeywordMaster keywordMasterSingle : keywordMaster) {
			KeywordMasterDTO keyWordDTO = new KeywordMasterDTO();
			keyWordDTO.setActive(keywordMasterSingle.getActive());
			keyWordDTO.setApplicableFrom(keywordMasterSingle.getApplicableFrom());
			keyWordDTO.setId(keywordMasterSingle.getId());
			if (keywordMasterSingle.getKeywords() != null) {
				keyWordDTO.setKeyWords(String.join(",", keywordMasterSingle.getKeywords()));
			}
			keyWordDTO.setNatureOfPaymentId(keywordMasterSingle.getNatureOfPaymentId());
			keyWordDTO.setNatureOfPaymentName(keywordMasterSingle.getNatureOfPayment());
			keyWordDTO.setApplicableTo(keywordMasterSingle.getApplicableTo());
			if (keywordMasterSingle.getIsForNonResident().equals(true)) {
				keyWordDTO.setResOrNr("NR");
			} else if (keywordMasterSingle.getIsForNonResident().equals(false)) {
				keyWordDTO.setResOrNr("RES");
			}
			keywordMasterListDTO.add(keyWordDTO);
		}
		return keywordMasterListDTO;
	}

	/**
	 * 
	 * @param file
	 * @param tenantId
	 * @param userEmail
	 * @param deductorPan
	 * @param batchId
	 * @return
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 * @throws InvalidFormatException
	 * @throws IntrusionException
	 * @throws ValidationException
	 * @throws ParseException
	 */
	public BatchUpload createKeywordMaster(MultipartFile file, String tenantId, String userEmail, String deductorPan,
			Integer batchId) throws IOException, InvalidKeyException, URISyntaxException, StorageException,
			InvalidFormatException, IntrusionException, ValidationException, ParseException {
		logger.info("Service method executing to process KeyWord file {}");
		BatchUpload batchupload = new BatchUpload();
		List<BatchUpload> listBatchupload = batchUploadDAO.getBatchUploadByTypeAndId(UploadTypes.KEYWORDS_EXCEL.name(),
				batchId);
		logger.info("Retrieved Batch upload record size {}" + listBatchupload.size());
		if (!listBatchupload.isEmpty()) {
			batchupload = listBatchupload.get(0);
		}
		String errorFilePath = null;
		KeywordMaster keywordMaster = new KeywordMaster();
		List<KeywordsErrorDto> listErrorDto = new ArrayList<>();
		KeywordMasterDTO keywordMasterDTO = new KeywordMasterDTO();
		Long successCount = 0L;
		int noOfRows = 0;
		int noOfColumns = 0;
		Map<String, String> residentialStatusMap = mastersClient.getResidentialStatusForSections().getBody().getData();
		try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
			// sheet1
			XSSFSheet sheet = workbook.getSheetAt(0);
			DataFormatter formatter = new DataFormatter();

			noOfRows = sheet.getPhysicalNumberOfRows();
			noOfColumns = sheet.getRow(0).getLastCellNum();
			// sheet2
			XSSFSheet sheet1 = workbook.getSheetAt(1);
			int noOfShee1Rows = sheet1.getPhysicalNumberOfRows();

			int sequenceNo = 2;
			if (noOfColumns >= 2) {
				Map<String, Integer> nopAndIds = new HashMap<>();
				for (int rownum = 1; rownum <= noOfShee1Rows; rownum++) {
					XSSFRow row = sheet1.getRow(rownum);
					if (row != null) {
						String nopId = row.getCell(0) != null ? row.getCell(0).toString() : "";
						String nature = row.getCell(1) != null ? row.getCell(1).toString() : "";
						nopAndIds.put(nature,
								Integer.valueOf(CommonUtil.d2.format(BigDecimal.valueOf(Double.valueOf(nopId)))));
					}
				}
				for (int rownum = 1; rownum <= noOfRows; rownum++) {
					XSSFRow row = sheet.getRow(rownum);
					if (row != null && row.getCell(0) != null && row.getCell(1) != null) {
						// nop
						Cell cell1 = row.getCell(0);
						// keywords
						Cell cell2 = row.getCell(1);
						// isResident
						Cell cell3 = row.getCell(2);

						String keywords = formatter.formatCellValue(cell2);
						String nopAndSection = cell1 != null ? cell1.toString() : "";
						String isResident = cell3 != null ? cell3.toString() : "";
						String reason = "";

						if (StringUtils.isNotBlank(keywords)) {
							keywordMasterDTO.setKeyWords(keywords);
						}
						if (StringUtils.isNotBlank(keywords) && StringUtils.isBlank(nopAndSection)) {
							reason = reason + " NATURE OF PAYMENT COLUMN MUST NOT BE EMPTY" + "\n";
						}
						if (StringUtils.isBlank(isResident)) {
							reason = reason + " IS RESIDENT COLUMN MUST NOT BE EMPTY(RES/NR (OR) N/Y)" + "\n";
						}
						if (StringUtils.isNotBlank(nopAndSection)) {
							int lastIndexOf = nopAndSection.lastIndexOf("-");
							String nop = nopAndSection.substring(0, lastIndexOf).trim();
							String section = nopAndSection.substring(lastIndexOf + 1, nopAndSection.length()).trim();
							Map<String, String> nopMap = new HashMap<>();
							nopMap.put("nop", nop);
							nopMap.put("section", section);
							if ("Y".equalsIgnoreCase(isResident) || "NR".equalsIgnoreCase(isResident)) {
								keywordMaster.setIsForNonResident(true);
							} else if ("N".equalsIgnoreCase(isResident) || "RES".equalsIgnoreCase(isResident)) {
								keywordMaster.setIsForNonResident(false);
							}
							/*
							 * if (residentialStatusMap != null && !residentialStatusMap.isEmpty()) { if
							 * (residentialStatusMap.get(section) != null &&
							 * "NR".equalsIgnoreCase(residentialStatusMap.get(section))) {
							 * keywordMaster.setIsForNonResident(true); } }
							 */
							// feign client for nop and section
							Optional<NatureOfPaymentMaster> response = mastersClient
									.getNOPBasedOnSectionAndNature(nopMap).getBody().getData();
							if (!response.isPresent()) {
								reason = reason + " NATURE OF PAYMENT AND SECTION IS NOT FOUND IN THE SYSTEM." + "\n";
							}
						}
						if (StringUtils.isNotBlank(reason)) {
							KeywordsErrorDto errorDto = new KeywordsErrorDto();
							errorDto.setNopAndSection(nopAndSection);
							errorDto.setKeyWords(keywords);
							errorDto.setReason(reason);
							listErrorDto.add(errorDto);
							errorDto.setSequenceNo(sequenceNo);
						}
						sequenceNo++;

						if (StringUtils.isAllBlank(reason) && StringUtils.isNotBlank(keywords)) {
							// checking nop is already present in DB or not
							List<KeywordMaster> keyWordMasterDb = keywordMasterDAO.findByNatureOfPayment(nopAndSection,
									deductorPan);
							if (!keyWordMasterDb.isEmpty()) {
								for (KeywordMaster keyword : keyWordMasterDb) {
									if (keyword.getKeywords() != null) {
										String localKeyword = keyword.getKeywords();
										if (!localKeyword.contains(keywords)) {
											keyword.setKeywords(localKeyword.concat("," + keywords));
										}
									} else {
										keyword.setKeywords(keywords);
									}
									keyword.setIsForNonResident(keywordMaster.getIsForNonResident());
									keywordMaster = keywordMasterDAO.update(keyword);
									logger.info("KeyWord got updated with id : {}", keywordMaster.getId());
								} // for
								++successCount;
							} // if
							else {
								keywordMaster.setPan(deductorPan);
								keywordMaster.setActive(true);
								keywordMaster.setCreatedBy(userEmail);
								keywordMaster.setCreatedDate(new Date());
								keywordMaster.setUpdatedBy(userEmail);
								keywordMaster.setUpdatedDate(new Date());
								keywordMaster.setApplicableFrom(new Date());
								keywordMaster.setNatureOfPaymentId(nopAndIds.get(nopAndSection));
								keywordMaster.setNatureOfPayment(nopAndSection);
								keywordMaster.setKeywords(keywords);
								try {
									keywordMaster = keywordMasterDAO.save(keywordMaster);
									successCount++;
								} catch (Exception e) {
									logger.error("error while saving record ::" + e);
								}
							}
						} // first if
					}
				} // first for
				if (!listErrorDto.isEmpty()) {
					String fileName = file.getOriginalFilename();
					// METHOD TO CREATE ERROR FILE
					try {
						File errorFile = generateErrorFile(listErrorDto, deductorPan, fileName);
						errorFilePath = blob.uploadExcelToBlobWithFile(errorFile, tenantId);
						batchupload.setErrorFilePath(errorFilePath);
					} catch (Exception e) {
						logger.error("error occured while saving error file  :" + e);
					}
				}
				batchupload.setFailedCount((long) listErrorDto.size());
				batchupload.setRowsCount((long) noOfRows - 1);
				batchupload.setSuccessCount(successCount);
				batchupload.setStatus("Processed");
				batchupload.setNewStatus("Processed");
				batchupload.setProcessEndTime(new Timestamp(new Date().getTime()));
				batchupload.setProcessedCount(successCount.intValue());
				batchupload = batchUploadDAO.update(batchupload);
				logger.info("KeyWord file processed and Updated Batch upload table{}");

			}
			return batchupload;
		} // try with resource
	}

	/**
	 * 
	 * @param listErrorDto
	 * @param deductorPan
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public File generateErrorFile(List<KeywordsErrorDto> listErrorDto, String deductorPan, String fileName)
			throws Exception {

		Workbook workbook = new Workbook();
		Worksheet sheet = workbook.getWorksheets().get(0);
		sheet.autoFitColumns();

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		// creating styles to apply cells
		Style style = workbook.createStyle();
		style.getFont().setBold(true);
		style.getFont().setSize(13);
		// style wit green color
		Style style1 = workbook.createStyle();
		style1.setForegroundColor(Color.fromArgb(169, 209, 142));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		// style with olive color
		Style style2 = workbook.createStyle();
		style2.setForegroundColor(Color.fromArgb(252, 199, 155));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		style2.setHorizontalAlignment(TextAlignmentType.CENTER);
		// STYLE FOR FADED BLUE
		Style style3 = workbook.createStyle();
		style3.setForegroundColor(Color.fromArgb(135, 206, 250));
		style3.setPattern(BackgroundType.SOLID);
		style3.getFont().setBold(true);
		style3.setHorizontalAlignment(TextAlignmentType.CENTER);
		// for black
		Style style4 = workbook.createStyle();
		style4.setForegroundColor(Color.fromArgb(0, 0, 0));
		style4.setPattern(BackgroundType.SOLID);
		style4.getFont().setBold(true);
		style4.getFont().setColor(Color.fromArgb(240, 248, 255));
		style4.setHorizontalAlignment(TextAlignmentType.CENTER);
		// for red
		Style style5 = workbook.createStyle();
		style5.setForegroundColor(Color.fromArgb(255, 0, 0));
		style5.setPattern(BackgroundType.SOLID);
		style5.getFont().setBold(true);
		style5.getFont().setColor(Color.fromArgb(240, 248, 255));
		style5.setHorizontalAlignment(TextAlignmentType.CENTER);

		sheet.setGridlinesVisible(false);
		sheet.freezePanes(0, 2, 0, 2);

		// getting the header msg and printing the value

		String msg = getErrorReportMsg(deductorPan);
		sheet.getCells().merge(0, 0, 4, 8);
		sheet.getCells().get("A1").setValue(msg);
		sheet.getCells().get("A1").setStyle(style);
		sheet.getCells().get("B5").setValue("Error/Information codes");
		sheet.getCells().get("B5").setStyle(style3);

		// setting the headersldcMasterXlsxReport
		sheet.getCells().get("A6").setValue("DEDUCTOR PAN");
		sheet.getCells().get("A6").setStyle(style1);

		sheet.getCells().get("B6").setValue("ERROR MESSAGE");
		sheet.getCells().get("B6").setStyle(style1);

		sheet.getCells().get("C6").setValue("SEQUENCE NO");
		sheet.getCells().get("C6").setStyle(style4);

		sheet.getCells().get("D6").setValue("NATURE OF PAYMENT");
		sheet.getCells().get("D6").setStyle(style2);

		sheet.getCells().get("E6").setValue("KEYWORD");
		sheet.getCells().get("E6").setStyle(style2);
		
		sheet.getCells().get("F6").setValue("IS RESIDENT");
		sheet.getCells().get("F6").setStyle(style2);

		int count = 7;
		for (KeywordsErrorDto errorDto : listErrorDto) {
			sheet.getCells().get("A" + count).setValue(deductorPan);
			sheet.getCells().get("B" + count).setValue(errorDto.getReason());
			sheet.getCells().get("C" + count).setValue(errorDto.getSequenceNo());
			sheet.getCells().get("D" + count).setValue(errorDto.getNopAndSection());
			sheet.getCells().get("E" + count).setValue(errorDto.getKeyWords());
			sheet.getCells().get("F" + count).setValue(errorDto.getIsResident());
			count++;
		} // for

		int maxdatacol = sheet.getCells().getMaxDataColumn();
		int maxdatarow = sheet.getCells().getMaxDataRow();
		// setting the gridlines for the columns
		String firstHeaderCellName = "A6";
		String lastHeaderCellName = "K6";
		String firstDataCellName = "A7";
		String lastDataCellName = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);
		CommonUtil.setBoardersForAsposeXlsx(sheet, firstHeaderCellName, lastHeaderCellName, firstDataCellName,
				lastDataCellName);

		sheet.autoFitColumns();
		sheet.autoFitRows();

		AutoFilter autoFilter = sheet.getAutoFilter();
		autoFilter.setRange("A6:F6");

		File file = new File("Keywords_Error" + UUID.randomUUID() + ".xlsx");
		try (FileOutputStream outputStream = new FileOutputStream(file)) {
			workbook.save(outputStream, SaveFormat.XLSX);
		}
		return file;

	}

	public String getErrorReportMsg(String deductorPan) {
		// get deductor names
		String deductorName = keywordMasterDAO.findByDeductorPan(deductorPan);
		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
		String date = simpleDateFormat.format(new Date());
		return "Keywords Error Report (Dated: " + date + ")\n Client Name: " + deductorName + "\n";
	}

	/**
	 * This method for delete keywords
	 * 
	 * @param id
	 * @param userName
	 * @param deductorPan
	 * @return
	 */
	public KeywordMaster deleteKeywords(Integer id, String userName, String deductorPan) {
		KeywordMaster keywordMaster = new KeywordMaster();
		List<KeywordMaster> keywordMasterOptional = keywordMasterDAO.findById(id, deductorPan);
		if (!keywordMasterOptional.isEmpty()) {
			keywordMaster = keywordMasterOptional.get(0);
			keywordMaster.setKeywords(StringUtils.EMPTY);
			keywordMaster.setUpdatedBy(userName);
			keywordMaster.setUpdatedDate(new Date());
			keywordMasterDAO.update(keywordMaster);
		}
		return keywordMaster;
	}

	/**
	 * This method for get all nature of payment based on deductor pan.
	 * 
	 * @param deductorPan
	 * @return
	 */
	public List<String> getnatureofpaymentkeywords(String deductorPan) {
		return keywordMasterDAO.getDeducteesByPan(deductorPan);
	}

	/**
	 * 
	 * @param tan
	 * @param tenantId
	 * @param pan
	 * @param natureOfPayments
	 * @return
	 */
	public MultipartFile exportExistingNOPKeywords(String tenantId, String pan, String natureOfPayments) {
		MultipartFile multipartFile = null;
		String[] keywordsHeaders = new String[] { "Nature Of Payment", "Section", "Keywords", "IsResident" };
		try {
			Workbook workbook = new Workbook();
			Worksheet worksheet = workbook.getWorksheets().get(0);
			worksheet.setName("Keywords");
			worksheet.autoFitColumns();
			worksheet.autoFitRows();
			worksheet.getCells().importArray(keywordsHeaders, 0, 0, false);
			List<KeywordMaster> keywordsList = new ArrayList<>();
			if (StringUtils.isBlank(natureOfPayments) || "ALL".equalsIgnoreCase(natureOfPayments)) {
				keywordsList = keywordMasterDAO.findAllByDeductorPan(pan);
			} else {
				String[] nops = natureOfPayments.split(",");
				for (int i = 0; i < nops.length; i++) {
					keywordsList.addAll(keywordMasterDAO.findByNatureOfPayment(nops[i], pan));
				}
			}
			logger.info("keywords List :{}", keywordsList);
			setKeywordsHeaders(keywordsList, worksheet);

			worksheet.autoFitColumns();
			Style style5 = workbook.createStyle();
			style5.setForegroundColor(Color.fromArgb(91, 155, 213));
			style5.setPattern(BackgroundType.SOLID);
			style5.getFont().setBold(true);
			style5.setHorizontalAlignment(TextAlignmentType.CENTER);
			Range headerColorRange1 = worksheet.getCells().createRange("A1:D1");
			headerColorRange1.setStyle(style5);

			// Creating AutoFilter by giving the cells range AutoFilter autoFilter =
			worksheet.getAutoFilter();
			// Creating AutoFilter by giving the cells range
			AutoFilter autoFilter = worksheet.getAutoFilter();
			autoFilter.setRange("A1:C1");
			File file = new File("Existing_Keywords_Report_" + pan + ".xlsx");
			OutputStream out = new FileOutputStream(file);
			workbook.save(out, SaveFormat.XLSX);

			InputStream inputstream = new FileInputStream(file);

			multipartFile = new MockMultipartFile("file", file.getName(), "application/vnd.ms-excel", inputstream);

		} catch (Exception exception) {
			logger.info("Exception occured while generating excell report for keywords.");
		}
		return multipartFile;
	}

	/**
	 * 
	 * @param keywordsList
	 * @param worksheet
	 * @throws Exception
	 */
	public void setKeywordsHeaders(List<KeywordMaster> keywordsList, Worksheet worksheet) throws Exception {
		int rowIndex = 1;
		for (KeywordMaster keywordMaster : keywordsList) {
			String nopAndSection = keywordMaster.getNatureOfPayment();
			logger.info("nop And Section :{}", nopAndSection);
			int lastIndexOf = nopAndSection.lastIndexOf("-");
			String nop = nopAndSection.substring(0, lastIndexOf);
			String section = nopAndSection.substring(lastIndexOf + 1, nopAndSection.length());
			Set<String> keywords = new HashSet<>();
			keywords.add(keywordMaster.getKeywords());
			String resOrNr = keywordMaster.getIsForNonResident().equals(true) ? "NR" : "RES";
			for (String keyword : keywords) {
				List<Object> rowData = new ArrayList<>();
				rowData.add(StringUtils.isBlank(nop) ? StringUtils.EMPTY // Nature of payment
						: nop.trim());
				rowData.add(StringUtils.isBlank(section) ? StringUtils.EMPTY // Section
						: section.trim());
				rowData.add(StringUtils.isBlank(keyword) ? StringUtils.EMPTY // keyword
						: keyword);
				rowData.add(StringUtils.isBlank(resOrNr) ? StringUtils.EMPTY // isResident
						: resOrNr);
				worksheet.getCells().importArrayList((ArrayList<Object>) rowData, rowIndex++, 0, false);
			}
		}
	}

}
