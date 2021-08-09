package com.ey.in.tds.onboarding.service.shareholder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aspose.cells.AutoFilter;
import com.aspose.cells.BackgroundType;
import com.aspose.cells.Cell;
import com.aspose.cells.CellsHelper;
import com.aspose.cells.Color;
import com.aspose.cells.ImportTableOptions;
import com.aspose.cells.Range;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Style;
import com.aspose.cells.TextAlignmentType;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.BatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.dto.onboarding.shareholder.ShareholderMasterNonResidential;
import com.ey.in.tds.common.model.shareholder.ShareholderMasterNonResidentialErrorReportCsvDTO;
import com.ey.in.tds.common.model.shareholder.ShareholderMasterResidentialErrorReportCsvDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.ShareholderMasterResidential;
import com.ey.in.tds.common.onboarding.jdbc.dto.ShareholderNonResidentialHistory;
import com.ey.in.tds.common.onboarding.jdbc.dto.ShareholderResidentialHistory;
import com.ey.in.tds.common.service.BlobStorageService;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.jdbc.dao.DeductorMasterDAO;
import com.ey.in.tds.jdbc.dao.ShareholderMasterNonResidentialDAO;
import com.ey.in.tds.jdbc.dao.ShareholderMasterResidentialDAO;
import com.ey.in.tds.jdbc.dao.ShareholderNonResidentialHistoryDAO;
import com.ey.in.tds.jdbc.dao.ShareholderResidentialHistoryDAO;
import com.ey.in.tds.onboarding.service.util.excel.shareholder.NonResidentShareholderExcel;
import com.ey.in.tds.onboarding.service.util.excel.shareholder.ResidentShareholderExcel;
import com.ey.in.tds.onboarding.web.rest.util.ValidationUtil;
import com.microsoft.azure.storage.StorageException;

import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;

@Service
public class ShareholderBulkService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private BatchUploadDAO batchUploadDAO;

	@Autowired
	private ShareholderMasterNonResidentialDAO shareholderMasterNonResidentialDAO;

	@Autowired
	private ShareholderMasterResidentialDAO shareholderMasterResidentialDAO;

	@Autowired
	private ShareholderResidentialHistoryDAO shareholderResidentialHistoryDAO;

	@Autowired
	private ShareholderNonResidentialHistoryDAO shareholderNonResidentialHistoryDAO;

	@Autowired
	private BlobStorage blob;

	@Autowired
	private DeductorMasterDAO deductorMasterDAO;

	@Autowired
	private BlobStorageService blobStorageService;

	private ShareholderMasterNonResidential generateSuccessDTO(CsvRow row, Integer assesssmentYear, String fileName,
			String deductorPan, String userName,Integer batchUploadId) throws ParseException {
		ShareholderMasterNonResidential nr = new ShareholderMasterNonResidential();
		nr.setBatchUploadId(batchUploadId);
		nr.setDeductorPan(deductorPan);
		nr.setShareholderResidentialStatus("NR");
		nr.setCreatedBy(userName);
		nr.setCreatedDate(new Date());
		nr.setShareholderName(row.getField("SHAREHOLDER NAME").trim());
		nr.setFolioNo(row.getField(NonResidentShareholderExcel.HEADER_SHAREHOLDER_FOLIONO).trim());
		nr.setCountry(row.getField(NonResidentShareholderExcel.HEADER_COUNTRY));
		nr.setShareholderCategory(row.getField(NonResidentShareholderExcel.HEADER_SHAREHOLDER_CATEGORY));
		nr.setShareholderPan(StringUtils.isEmpty(row.getField(NonResidentShareholderExcel.HEADER_SHAREHOLDER_PAN))?null:row.getField(NonResidentShareholderExcel.HEADER_SHAREHOLDER_PAN).trim());
		nr.setShareholderType(row.getField(NonResidentShareholderExcel.HEADER_SHAREHOLDER_TYPE));

		if (StringUtils.isNotBlank(row.getField(NonResidentShareholderExcel.HEADER_SHARE_HELD_FROM_DATE))) {
			nr.setShareHeldFromDate(
					dateConverter(row.getField(NonResidentShareholderExcel.HEADER_SHARE_HELD_FROM_DATE)));
		}
		if (StringUtils.isNotBlank(row.getField(NonResidentShareholderExcel.HEADER_SHARES_HELD_TO_DATE))) {
			nr.setShareHeldToDate(dateConverter(row.getField(NonResidentShareholderExcel.HEADER_SHARES_HELD_TO_DATE)));
		}
		if (StringUtils.isNotBlank(row.getField(NonResidentShareholderExcel.HEADER_IS_TRC_AVAILABLE))) {
			nr.setIsTrcAvailable(booleanConverter(row.getField(NonResidentShareholderExcel.HEADER_IS_TRC_AVAILABLE)));
		}
		if (StringUtils.isNotBlank(row.getField(NonResidentShareholderExcel.HEADER_TRC_APPLICABLE_FROM))) {
			nr.setTrcApplicableFrom(
					dateConverter(row.getField(NonResidentShareholderExcel.HEADER_TRC_APPLICABLE_FROM)));
		}
		if (StringUtils.isNotBlank(row.getField(NonResidentShareholderExcel.HEADER_TRC_APPLICABLE_TO))) {
			nr.setTrcApplicableTo(dateConverter(row.getField(NonResidentShareholderExcel.HEADER_TRC_APPLICABLE_TO)));
		}

		nr.setIsTenfAvailable(
				booleanConverter(row.getField(NonResidentShareholderExcel.HEADER_IS_FORM_TEN_F_AVAILABLE)));
		if (StringUtils.isNotBlank(row.getField(NonResidentShareholderExcel.HEADER_FORM_TEN_F_APPLICABLE_FROM))) {
			nr.setTenfApplicableFrom(
					dateConverter(row.getField(NonResidentShareholderExcel.HEADER_FORM_TEN_F_APPLICABLE_FROM)));
		}
		if (StringUtils.isNotBlank(row.getField(NonResidentShareholderExcel.HEADER_FORM_TEN_F_APPLICABLE_TO))) {
			nr.setTenfApplicableTo(
					dateConverter(row.getField(NonResidentShareholderExcel.HEADER_FORM_TEN_F_APPLICABLE_TO)));
		}

		nr.setIsPeAvailableInIndia(
				booleanConverter(row.getField(NonResidentShareholderExcel.HEADER_IS_PE_AVAILABLE_IN_INDIA)));

		nr.setIsNoPeDeclarationAvailable(
				booleanConverter(row.getField(NonResidentShareholderExcel.HEADER_IS_NO_PE_DECLARATION_AVAILABLE)));
		if (StringUtils
				.isNotBlank(row.getField(NonResidentShareholderExcel.HEADER_NO_PE_DECLARATION_APPLICABLE_FROM))) {
			nr.setNoPeDeclarationApplicableFrom(
					dateConverter(row.getField(NonResidentShareholderExcel.HEADER_NO_PE_DECLARATION_APPLICABLE_FROM)));
		}
		if (StringUtils.isNotBlank(row.getField(NonResidentShareholderExcel.HEADER_NO_PE_DECLARATION_APPLICABLE_TO))) {
			nr.setNoPeDeclarationApplicableTo(
					dateConverter(row.getField(NonResidentShareholderExcel.HEADER_NO_PE_DECLARATION_APPLICABLE_TO)));
		}

		nr.setIsNoPoemDeclarationAvailable(
				booleanConverter(row.getField(NonResidentShareholderExcel.HEADER_IS_NO_POEM_DECLARATION_AVAILABLE)));
		if (StringUtils.isNotBlank(
				row.getField(NonResidentShareholderExcel.HEADER_NO_POEM_DECLARATION_IN_INDIA_APPLICABLE_FROM))) {
			nr.setNoPoemDeclarationInIndiaApplicableFrom(dateConverter(
					row.getField(NonResidentShareholderExcel.HEADER_NO_POEM_DECLARATION_IN_INDIA_APPLICABLE_FROM)));
		}
		if (StringUtils.isNotBlank(
				row.getField(NonResidentShareholderExcel.HEADER_NO_POEM_DECLARATION_IN_INDIA_APPLICABLE_TO))) {
			nr.setNoPoemDeclarationInIndiaApplicableTo(dateConverter(
					row.getField(NonResidentShareholderExcel.HEADER_NO_POEM_DECLARATION_IN_INDIA_APPLICABLE_TO)));
		}

		nr.setIsMliSlobSatisfactionDeclarationAvailable(booleanConverter(
				row.getField(NonResidentShareholderExcel.HEADER_IS_MLI_SATISFACTION_DECLARATION_AVAILABLE)));
		nr.setIsBeneficialOwnerOfIncome(
				booleanConverter(row.getField(NonResidentShareholderExcel.HEADER_IS_BENEFICIAL_OWNER_OF_INCOME)));
		nr.setIsBeneficialOwnershipDeclarationAvailable(booleanConverter(
				row.getField(NonResidentShareholderExcel.HEADER_IS_BENEFICIAL_OWNERSHIP_DECLARATION_AVAILABLE)));
		nr.setIsTransactionGAARCompliant(
				booleanConverter(row.getField(NonResidentShareholderExcel.HEADER_IS_TRANSACTION_GAAR_COMPLIANT)));
		nr.setIsKuwaitShareholderType(
				booleanConverter(row.getField(NonResidentShareholderExcel.HEADER_IS_KUWAIT_SHAREHOLDER_TYPE)));
		nr.setIsUKVehicleExemptTax(
				booleanConverter(row.getField(NonResidentShareholderExcel.HEADER_IS_UK_VEHICLE_EXEMPT_TAX)));

		if (StringUtils.isBlank(row.getField(NonResidentShareholderExcel.HEADER_ICELAND_DIVIDEND_TAXATION_RATE))) {
			nr.setIcelandDividendTaxationRate(BigDecimal.ZERO);
			nr.setIcelandRateLessThanTwenty(null);
		} else {
			nr.setIcelandDividendTaxationRate(
					decimalConverter(row.getField(NonResidentShareholderExcel.HEADER_ICELAND_DIVIDEND_TAXATION_RATE)));
			nr.setIcelandRateLessThanTwenty(nr.getIcelandDividendTaxationRate().compareTo(new BigDecimal(20)) == -1);
		}
		nr.setTotalSharesHeld(decimalConverter(row.getField(NonResidentShareholderExcel.HEADER_TOTAL_SHARES_HELD)));
		nr.setPercentageSharesHeld(
				decimalConverter(row.getField(NonResidentShareholderExcel.HEADER_PERCENTAGE_SHARES_HELD)));
		nr.setSourceIdentifier("EXCEL FILE");
		nr.setKeyShareholder(booleanConverter(row.getField(NonResidentShareholderExcel.HEADER_KEY_SHAREHOLDER)));
		nr.setStringAssesmentYearDividendDetails("{" + assesssmentYear + ":0.0}");

		nr.setUniqueIdentificationNumber(
				row.getField(NonResidentShareholderExcel.HEADER_SHAREHOLDER_UNIQUE_IDENTIFICATION_NUMBER));
		nr.setShareholderTin(row.getField(NonResidentShareholderExcel.HEADER_SHAREHOLDER_TIN));
		nr.setPrincipalPlaceOfBusiness(
				row.getField(NonResidentShareholderExcel.HEADER_SHAREHOLDER_PRINCIPAL_PLACE_OF_BUSINESS));
		nr.setFlatDoorBlockNo(row.getField(NonResidentShareholderExcel.HEADER_FLAT_DOOR_NO));
		nr.setNameBuildingVillage(row.getField(NonResidentShareholderExcel.HEADER_NAME_BUILDING_VILLAGE));
		nr.setRoadStreetPostoffice(row.getField(NonResidentShareholderExcel.HEADER_ROAD_STREET));
		nr.setAreaLocality(row.getField(NonResidentShareholderExcel.HEADER_AREA_LOCALITY));
		nr.setTownCityDistrict(row.getField(NonResidentShareholderExcel.HEADER_TOWN_DISTRICT));
		nr.setState(row.getField(NonResidentShareholderExcel.HEADER_STATE));
		nr.setPinCode(row.getField(NonResidentShareholderExcel.HEADER_PIN_CODE));
		nr.setCountry(row.getField(NonResidentShareholderExcel.HEADER_COUNTRY));
		nr.setEmailId(row.getField(NonResidentShareholderExcel.HEADER_EMAIL_ID));
		nr.setContact(row.getField(NonResidentShareholderExcel.HEADER_CONTACT));
		nr.setShareTransferAgentName(row.getField(NonResidentShareholderExcel.HEADER_SHARE_TRANSFER_AGENT_NAME));
		nr.setDematAccountNo(row.getField(NonResidentShareholderExcel.HEADER_DEMAT_ACCOUNT_NO));
		nr.setForm15CACBApplicable(row.getField(NonResidentShareholderExcel.HEADER_FORM_15CACB_APPLICABLE));
		nr.setIsPoemOfShareholderInIndia(
				booleanConverter(row.getField(NonResidentShareholderExcel.HEADER_IS_POEM_OF_SHAREHOLDER_IN_INDIA)));
		nr.setIsCommercialIndemnityOrTreatyBenefitsWithoutDocuments(
				booleanConverter(row.getField(NonResidentShareholderExcel.HEADER_IS_POEM_OF_SHAREHOLDER_IN_INDIA)));

		return nr;
	}

	private ShareholderMasterNonResidentialErrorReportCsvDTO generateErrorDTO(CsvRow row, String deductorPan) {
		ShareholderMasterNonResidentialErrorReportCsvDTO errorDto = new ShareholderMasterNonResidentialErrorReportCsvDTO();
		errorDto.setShareholderName(row.getField("SHAREHOLDER NAME").trim());
		errorDto.setFolioNo(row.getField(NonResidentShareholderExcel.HEADER_SHAREHOLDER_FOLIONO));
		errorDto.setCountry(row.getField(NonResidentShareholderExcel.HEADER_COUNTRY));
		errorDto.setShareholderCategory(row.getField(NonResidentShareholderExcel.HEADER_SHAREHOLDER_CATEGORY));
		errorDto.setShareholderPan(row.getField(NonResidentShareholderExcel.HEADER_SHAREHOLDER_PAN));
		errorDto.setShareholderType(row.getField(NonResidentShareholderExcel.HEADER_SHAREHOLDER_TYPE));
		errorDto.setForm15CACBApplicable(row.getField(NonResidentShareholderExcel.HEADER_FORM_15CACB_APPLICABLE));
		errorDto.setShareHeldFromDate(row.getField(NonResidentShareholderExcel.HEADER_SHARE_HELD_FROM_DATE));
		errorDto.setShareHeldToDate(row.getField(NonResidentShareholderExcel.HEADER_SHARES_HELD_TO_DATE));
		errorDto.setIsTrcAvailable(row.getField(NonResidentShareholderExcel.HEADER_IS_TRC_AVAILABLE));
		errorDto.setTrcApplicableFrom(row.getField(NonResidentShareholderExcel.HEADER_TRC_APPLICABLE_FROM));
		errorDto.setTrcApplicableTo(row.getField(NonResidentShareholderExcel.HEADER_TRC_APPLICABLE_TO));

		errorDto.setIsTenfAvailable(row.getField(NonResidentShareholderExcel.HEADER_IS_FORM_TEN_F_AVAILABLE));
		errorDto.setTenfApplicableFrom(row.getField(NonResidentShareholderExcel.HEADER_FORM_TEN_F_APPLICABLE_FROM));
		errorDto.setTenfApplicableTo(row.getField(NonResidentShareholderExcel.HEADER_FORM_TEN_F_APPLICABLE_TO));

		errorDto.setIsPeAvailableInIndia(row.getField(NonResidentShareholderExcel.HEADER_IS_PE_AVAILABLE_IN_INDIA));

		errorDto.setIsNoPeDeclarationAvailable(
				row.getField(NonResidentShareholderExcel.HEADER_IS_NO_PE_DECLARATION_AVAILABLE));
		errorDto.setNoPeDeclarationApplicableFrom(
				row.getField(NonResidentShareholderExcel.HEADER_NO_PE_DECLARATION_APPLICABLE_FROM));
		errorDto.setNoPeDeclarationApplicableTo(
				row.getField(NonResidentShareholderExcel.HEADER_NO_PE_DECLARATION_APPLICABLE_TO));

		errorDto.setIsNoPoemDeclarationAvailable(
				row.getField(NonResidentShareholderExcel.HEADER_IS_NO_POEM_DECLARATION_AVAILABLE));
		errorDto.setNoPoemDeclarationInIndiaApplicableFrom(
				row.getField(NonResidentShareholderExcel.HEADER_NO_POEM_DECLARATION_IN_INDIA_APPLICABLE_FROM));
		errorDto.setNoPoemDeclarationInIndiaApplicableTo(
				row.getField(NonResidentShareholderExcel.HEADER_NO_POEM_DECLARATION_IN_INDIA_APPLICABLE_TO));

		errorDto.setIsMliSlobSatisfactionDeclarationAvailable(
				row.getField(NonResidentShareholderExcel.HEADER_IS_MLI_SATISFACTION_DECLARATION_AVAILABLE));
		errorDto.setIsBeneficialOwnerOfIncome(
				row.getField(NonResidentShareholderExcel.HEADER_IS_BENEFICIAL_OWNER_OF_INCOME));
		errorDto.setIsBeneficialOwnershipDeclarationAvailable(
				row.getField(NonResidentShareholderExcel.HEADER_IS_BENEFICIAL_OWNERSHIP_DECLARATION_AVAILABLE));
		errorDto.setIsTransactionGAARCompliant(
				row.getField(NonResidentShareholderExcel.HEADER_IS_TRANSACTION_GAAR_COMPLIANT));
		errorDto.setIsKuwaitShareholderType(
				row.getField(NonResidentShareholderExcel.HEADER_IS_KUWAIT_SHAREHOLDER_TYPE));
		errorDto.setIsUKVehicleExemptTax(row.getField(NonResidentShareholderExcel.HEADER_IS_UK_VEHICLE_EXEMPT_TAX));

		errorDto.setIcelandDividendTaxationRate(
				row.getField(NonResidentShareholderExcel.HEADER_ICELAND_DIVIDEND_TAXATION_RATE));
		errorDto.setTotalSharesHeld(row.getField(NonResidentShareholderExcel.HEADER_TOTAL_SHARES_HELD));
		errorDto.setPercentageSharesHeld(row.getField(NonResidentShareholderExcel.HEADER_PERCENTAGE_SHARES_HELD));
		errorDto.setSourceIdentifier("EXCEL FILE");
		errorDto.setKeyShareholder(row.getField(NonResidentShareholderExcel.HEADER_KEY_SHAREHOLDER));

		errorDto.setUniqueIdentificationNumber(
				row.getField(NonResidentShareholderExcel.HEADER_SHAREHOLDER_UNIQUE_IDENTIFICATION_NUMBER));
		errorDto.setShareholderTin(row.getField(NonResidentShareholderExcel.HEADER_SHAREHOLDER_TIN));
		errorDto.setPrincipalPlaceOfBusiness(
				row.getField(NonResidentShareholderExcel.HEADER_SHAREHOLDER_PRINCIPAL_PLACE_OF_BUSINESS));
		errorDto.setFlatDoorBlockNo(row.getField(NonResidentShareholderExcel.HEADER_FLAT_DOOR_NO));
		errorDto.setNameBuildingVillage(row.getField(NonResidentShareholderExcel.HEADER_NAME_BUILDING_VILLAGE));
		errorDto.setRoadStreetPostoffice(row.getField(NonResidentShareholderExcel.HEADER_ROAD_STREET));
		errorDto.setAreaLocality(row.getField(NonResidentShareholderExcel.HEADER_AREA_LOCALITY));
		errorDto.setTownCityDistrict(row.getField(NonResidentShareholderExcel.HEADER_TOWN_DISTRICT));
		errorDto.setState(row.getField(NonResidentShareholderExcel.HEADER_STATE));
		errorDto.setPinCode(row.getField(NonResidentShareholderExcel.HEADER_PIN_CODE));
		errorDto.setCountry(row.getField(NonResidentShareholderExcel.HEADER_COUNTRY));
		errorDto.setEmailId(row.getField(NonResidentShareholderExcel.HEADER_EMAIL_ID));
		errorDto.setContact(row.getField(NonResidentShareholderExcel.HEADER_CONTACT));
		errorDto.setShareTransferAgentName(row.getField(NonResidentShareholderExcel.HEADER_SHARE_TRANSFER_AGENT_NAME));
		errorDto.setDematAccountNo(row.getField(NonResidentShareholderExcel.HEADER_DEMAT_ACCOUNT_NO));
		errorDto.setForm15CACBApplicable(row.getField(NonResidentShareholderExcel.HEADER_DEMAT_ACCOUNT_NO));
		errorDto.setIsPoemOfShareholderInIndia(
				row.getField(NonResidentShareholderExcel.HEADER_IS_POEM_OF_SHAREHOLDER_IN_INDIA));
		errorDto.setIsCommercialIndemnityOrTreatyBenefitsWithoutDocuments(
				row.getField(NonResidentShareholderExcel.HEADER_IS_POEM_OF_SHAREHOLDER_IN_INDIA));

		return errorDto;
	}

	private Date dateConverter(String date) throws ParseException {
		logger.info("Date to be formatted is "+date+"{}");
		if (date.contains("-")) {
			return new SimpleDateFormat("dd-MM-yyyy").parse(date);
		} else if (date.contains("/")) {
			return  new SimpleDateFormat("dd/MM/yyyy").parse(date);
		} else {
			return  DateUtil.getJavaDate(Double.parseDouble(date));
		}
		//return new SimpleDateFormat("dd-MM-yyyy").parse(date.trim());
	}

	private boolean booleanConverter(String value) {
		return StringUtils.isBlank(value) ? false : (value.equals("Yes") ? true : false);
	}

	private BigDecimal decimalConverter(String value) {
		return StringUtils.isBlank(value) ? BigDecimal.ZERO : new BigDecimal(value);
	}
	

	private BatchUpload shareholderBatchUpload(BatchUpload batchUpload, MultipartFile mFile, String tan,
			Integer assesssmentYear, Integer assessmentMonthPlusOne, String userName, File file, String tenant)
			throws IOException, StorageException, InvalidKeyException, URISyntaxException {
		logger.info("batch", batchUpload);
		String errorFp = null;
		String path = null;
		if (file != null) {
			errorFp = blob.uploadExcelToBlobWithFile(file, tenant);
			batchUpload.setErrorFilePath(errorFp);
		}
		if (mFile != null) {
			path = blob.uploadExcelToBlob(mFile);
			batchUpload.setFileName(mFile.getOriginalFilename());
			batchUpload.setFilePath(path);
		}
		int month = assessmentMonthPlusOne;
		batchUpload.setAssessmentMonth(month);
		batchUpload.setAssessmentYear(assesssmentYear);
		batchUpload.setDeductorMasterTan(tan);
		batchUpload.setUploadType(UploadTypes.SHAREHOLDER_EXCEL.name());
		batchUpload.setCreatedBy(userName);
		batchUpload.setActive(true);
		if (batchUpload.getBatchUploadID() != null) {
			batchUpload.setBatchUploadID(batchUpload.getBatchUploadID());
			batchUpload = batchUploadDAO.update(batchUpload);
			logger.info("Excel got processed and Batch upload tale updated successfully  {}");
		} else {
			batchUpload = batchUploadDAO.save(batchUpload);
			logger.info("Record inserted into Batch upload tabble {}");
		}
		return batchUpload;

	}

	private File prepareNonResidentShareholderErrorFile(String originalFilename, String deductorTan, String deductorPan,
			ArrayList<ShareholderMasterNonResidentialErrorReportCsvDTO> errorList, ArrayList<String> headers)
			throws Exception {
		headers.addAll(0, Excel.STANDARD_ADDITIONAL_HEADERS);
		Workbook wkBook = shareholderNonResidentXlsxReport(errorList, deductorTan, deductorPan, headers);
		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		wkBook.save(baout, SaveFormat.XLSX);
		File shareholderErrorFile = new File(
				FilenameUtils.getBaseName(originalFilename) + "_" + System.currentTimeMillis() + "_Error_Report.xlsx");
		FileUtils.writeByteArrayToFile(shareholderErrorFile, baout.toByteArray());
		baout.close();
		return shareholderErrorFile;
	}

	private Workbook shareholderNonResidentXlsxReport(
			ArrayList<ShareholderMasterNonResidentialErrorReportCsvDTO> errorList, String deductorTan,
			String deductorPan, ArrayList<String> headerNames) throws Exception {
		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);

		worksheet.getCells().importArrayList(headerNames, 5, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		setExtractDataForShareholderNonResident(errorList, worksheet, deductorTan, headerNames);

// Style for A6 to D6 headers
		Style style1 = workbook.createStyle();
		style1.setForegroundColor(Color.fromArgb(169, 209, 142));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange1 = worksheet.getCells().createRange(5, 0, 1, 2);
		headerColorRange1.setStyle(style1);

		Cell cellD6 = worksheet.getCells().get("C6");
		Style styleD6 = cellD6.getStyle();
		styleD6.setForegroundColor(Color.fromArgb(0, 0, 0));
		styleD6.setPattern(BackgroundType.SOLID);
		styleD6.getFont().setBold(true);
		styleD6.getFont().setColor(Color.getWhite());
		styleD6.setHorizontalAlignment(TextAlignmentType.CENTER);
		cellD6.setStyle(styleD6);

// Style for E6 to AT6 headers
		Style style2 = workbook.createStyle();
		style2.setForegroundColor(Color.fromArgb(252, 199, 155));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		style2.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange2 = worksheet.getCells().createRange(5, 3, 1, headerNames.size() - 3);
		headerColorRange2.setStyle(style2);

		worksheet.autoFitColumns();
		worksheet.autoFitRows();
		worksheet.setGridlinesVisible(false);
		worksheet.freezePanes(0, 2, 0, 2);

		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

		String date = simpleDateFormat.format(new Date());

		List<DeductorMaster> response = deductorMasterDAO.findByDeductorPan(deductorPan);

		Cell cellA1 = worksheet.getCells().get("A1");
		cellA1.setValue("TDS Payables Error Report (Dated: " + date + ")");
		Style a1Style = cellA1.getStyle();
		a1Style.getFont().setName("Cambria");
		a1Style.getFont().setSize(14);
		a1Style.getFont().setBold(true);
		cellA1.setStyle(a1Style);

		Cell cellA2 = worksheet.getCells().get("A2");
		if (!response.isEmpty()) {
			cellA2.setValue("Client Name:" + response.get(0).getName());
		} else {
			cellA2.setValue("Client Name:" + StringUtils.EMPTY);
		}

		Style a2Style = cellA2.getStyle();
		a2Style.getFont().setName("Cambria");
		a2Style.getFont().setSize(14);
		a2Style.getFont().setBold(true);
		cellA2.setStyle(a2Style);

// column B5 column
		Cell cellB5 = worksheet.getCells().get("B5");
		cellB5.setValue("Error/Information codes");
		Style b5Style = cellB5.getStyle();
		b5Style.setForegroundColor(Color.fromArgb(180, 199, 231));
		b5Style.setPattern(BackgroundType.SOLID);
		b5Style.getFont().setBold(true);
		cellB5.setStyle(b5Style);

		int maxdatacol = worksheet.getCells().getMaxDataColumn();
		int maxdatarow = worksheet.getCells().getMaxDataRow();

		String firstHeaderCellName = "A6";
		String lastHeaderCellName = "AS6";
		String firstDataCellName = "A7";
		String lastDataCellName = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);

		CommonUtil.setBoardersForAsposeXlsx(worksheet, firstHeaderCellName, lastHeaderCellName, firstDataCellName,
				lastDataCellName);
// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A6:AS6");
		return workbook;
	}

	private void setExtractDataForShareholderNonResident(
			ArrayList<ShareholderMasterNonResidentialErrorReportCsvDTO> errorDTOs, Worksheet worksheet,
			String deductorTan, ArrayList<String> headerNames) throws Exception {

		if (!errorDTOs.isEmpty()) {
			int dataRowsStartIndex = 6;
			for (int i = 0; i < errorDTOs.size(); i++) {
				ShareholderMasterNonResidentialErrorReportCsvDTO errorDTO = errorDTOs.get(i);
				ArrayList<String> rowData = Excel.getValues(errorDTO, NonResidentShareholderExcel.fieldMappings,
						headerNames);
				rowData.set(0, StringUtils.isBlank(deductorTan) ? StringUtils.EMPTY : deductorTan);
				rowData.set(1, StringUtils.isBlank(errorDTO.getReason()) ? StringUtils.EMPTY : errorDTO.getReason());
				worksheet.getCells().importArrayList(rowData, i + dataRowsStartIndex, 0, false);
				worksheet.autoFitRow(i + dataRowsStartIndex);
				Excel.colorCodeErrorCells(worksheet, i + dataRowsStartIndex, headerNames, errorDTO.getReason());
			}
		}
	}

	private File prepareResidentShareholderErrorFile(String originalFilename, String deductorTan, String deductorPan,
			ArrayList<ShareholderMasterResidentialErrorReportCsvDTO> errorList, ArrayList<String> headers)
			throws Exception {
		headers.addAll(0, Excel.STANDARD_ADDITIONAL_HEADERS);
		Workbook wkBook = shareholderResidentXlsxReport(errorList, deductorTan, deductorPan, headers);
		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		wkBook.save(baout, SaveFormat.XLSX);
		File shareholderErrorFile = new File(
				FilenameUtils.getBaseName(originalFilename) + "_" + System.currentTimeMillis() + "_Error_Report.xlsx");
		FileUtils.writeByteArrayToFile(shareholderErrorFile, baout.toByteArray());
		baout.close();
		return shareholderErrorFile;
	}

	// Shareholder Residential
	private Workbook shareholderResidentXlsxReport(ArrayList<ShareholderMasterResidentialErrorReportCsvDTO> errorList,
			String deductorTan, String deductorPan, ArrayList<String> headerNames) throws Exception {
		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);

		worksheet.getCells().importArrayList(headerNames, 5, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		setExtractDataForShareholderResident(errorList, worksheet, deductorTan, headerNames);

		// Style for A6 to D6 headers
		Style style1 = workbook.createStyle();
		style1.setForegroundColor(Color.fromArgb(169, 209, 142));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange1 = worksheet.getCells().createRange(5, 0, 1, 2);
		headerColorRange1.setStyle(style1);

		Cell cellD6 = worksheet.getCells().get("C6");
		Style styleD6 = cellD6.getStyle();
		styleD6.setForegroundColor(Color.fromArgb(0, 0, 0));
		styleD6.setPattern(BackgroundType.SOLID);
		styleD6.getFont().setBold(true);
		styleD6.getFont().setColor(Color.getWhite());
		styleD6.setHorizontalAlignment(TextAlignmentType.CENTER);
		cellD6.setStyle(styleD6);

		// Style for E6 to AT6 headers
		Style style2 = workbook.createStyle();
		style2.setForegroundColor(Color.fromArgb(252, 199, 155));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		style2.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange2 = worksheet.getCells().createRange(5, 3, 1, headerNames.size() - 3);
		headerColorRange2.setStyle(style2);

		worksheet.autoFitColumns();
		worksheet.autoFitRows();
		worksheet.setGridlinesVisible(false);
		worksheet.freezePanes(0, 2, 0, 2);

		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

		String date = simpleDateFormat.format(new Date());

		List<DeductorMaster> response = deductorMasterDAO.findByDeductorPan(deductorPan);

		Cell cellA1 = worksheet.getCells().get("A1");
		cellA1.setValue("TDS Payables Error Report (Dated: " + date + ")");
		Style a1Style = cellA1.getStyle();
		a1Style.getFont().setName("Cambria");
		a1Style.getFont().setSize(14);
		a1Style.getFont().setBold(true);
		cellA1.setStyle(a1Style);

		Cell cellA2 = worksheet.getCells().get("A2");
		if (response.isEmpty()) {
			cellA2.setValue("Client Name:" + response.get(0).getName());
		} else {
			cellA2.setValue("Client Name:" + StringUtils.EMPTY);
		}

		Style a2Style = cellA2.getStyle();
		a2Style.getFont().setName("Cambria");
		a2Style.getFont().setSize(14);
		a2Style.getFont().setBold(true);
		cellA2.setStyle(a2Style);

		// column B5 column
		Cell cellB5 = worksheet.getCells().get("B5");
		cellB5.setValue("Error/Information codes");
		Style b5Style = cellB5.getStyle();
		b5Style.setForegroundColor(Color.fromArgb(180, 199, 231));
		b5Style.setPattern(BackgroundType.SOLID);
		b5Style.getFont().setBold(true);
		cellB5.setStyle(b5Style);

		int maxdatacol = worksheet.getCells().getMaxDataColumn();
		int maxdatarow = worksheet.getCells().getMaxDataRow();

		String firstHeaderCellName = "A6";
		String lastHeaderCellName = "AS6";
		String firstDataCellName = "A7";
		String lastDataCellName = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);

		CommonUtil.setBoardersForAsposeXlsx(worksheet, firstHeaderCellName, lastHeaderCellName, firstDataCellName,
				lastDataCellName);
		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A6:Y6");
		return workbook;
	}

	private void setExtractDataForShareholderResident(
			ArrayList<ShareholderMasterResidentialErrorReportCsvDTO> errorDTOs, Worksheet worksheet, String deductorTan,
			ArrayList<String> headerNames) throws Exception {

		if (!errorDTOs.isEmpty()) {
			int dataRowsStartIndex = 6;
			for (int i = 0; i < errorDTOs.size(); i++) {
				ShareholderMasterResidentialErrorReportCsvDTO errorDTO = errorDTOs.get(i);
				ArrayList<String> rowData = Excel.getValues(errorDTO, ResidentShareholderExcel.fieldMappings,
						headerNames);
				rowData.set(0, StringUtils.isBlank(deductorTan) ? StringUtils.EMPTY : deductorTan);
				rowData.set(1, StringUtils.isBlank(errorDTO.getReason()) ? StringUtils.EMPTY : errorDTO.getReason());
				worksheet.getCells().importArrayList(rowData, i + dataRowsStartIndex, 0, false);
				worksheet.autoFitRow(i + dataRowsStartIndex);
				Excel.colorCodeErrorCells(worksheet, i + dataRowsStartIndex, headerNames, errorDTO.getReason());
			}
		}
	}

	public MultipartFile generateShareholderPanXlsxReport(
			List<ShareholderMasterNonResidential> shareholderNonResidencePans,
			List<ShareholderMasterResidential> shareholderResidencePans) throws Exception {

		Map<String, ShareholderMasterResidential> shareholderResidenceMap = new HashMap<>();
		for (ShareholderMasterResidential sharehoderResidencePan : shareholderResidencePans) {
			shareholderResidenceMap.put(sharehoderResidencePan.getShareholderPan(), sharehoderResidencePan);
		}

		Map<String, ShareholderMasterNonResidential> shareholderNonResidenceMap = new HashMap<>();
		for (ShareholderMasterNonResidential shareholderNonResidence : shareholderNonResidencePans) {
			shareholderNonResidenceMap.put(shareholderNonResidence.getShareholderPan(), shareholderNonResidence);
		}

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);

		String[] headerNames = new String[] { "PAN", "Name" };
		worksheet.getCells().importArray(headerNames, 0, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		if (!shareholderNonResidencePans.isEmpty()) {
			int rowIndex = 1;
			for (Map.Entry<String, ShareholderMasterNonResidential> entry : shareholderNonResidenceMap.entrySet()) {
				ShareholderMasterNonResidential shareholderNonResidencePan = entry.getValue();
				ArrayList<String> rowData = new ArrayList<>();
				rowData.add(StringUtils.isBlank(shareholderNonResidencePan.getShareholderPan()) ? StringUtils.EMPTY
						: shareholderNonResidencePan.getShareholderPan());
				rowData.add(StringUtils.isBlank(shareholderNonResidencePan.getShareholderName()) ? StringUtils.EMPTY
						: shareholderNonResidencePan.getShareholderName());
				worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);
			}
		} else if (!shareholderResidencePans.isEmpty()) {
			int rowIndex = 1;
			for (Map.Entry<String, ShareholderMasterResidential> entry : shareholderResidenceMap.entrySet()) {
				ShareholderMasterResidential shareholderResidencePan = entry.getValue();
				ArrayList<String> rowData = new ArrayList<>();
				rowData.add(StringUtils.isBlank(shareholderResidencePan.getShareholderPan()) ? StringUtils.EMPTY
						: shareholderResidencePan.getShareholderPan());
				rowData.add(StringUtils.isBlank(shareholderResidencePan.getShareholderName()) ? StringUtils.EMPTY
						: shareholderResidencePan.getShareholderName());
				worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);
			}

		}

		File file = new File("pan_upload_template_" + System.currentTimeMillis() + ".xlsx");
		OutputStream out = new FileOutputStream(file);
		workbook.save(out, SaveFormat.XLSX);
		InputStream inputstream = new FileInputStream(file);

		MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "application/vnd.ms-excel",
				IOUtils.toByteArray(inputstream));
		inputstream.close();
		out.close();

		return multipartFile;
	}

	@Async
	public BatchUpload processResidentShareholdersWithCSV(MultipartFile uploadedFile, String sha256, String deductorTan,
			Integer assesssmentYear, Integer assessmentMonthPlusOne, String userName, String tenantId,
			String deductorPan, BatchUpload batchUpload) throws Exception {

		MultiTenantContext.setTenantId(tenantId);

		File shareholderErrorFile = null;
		File file = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getFilePath());
		Workbook workbook = new Workbook(file.getAbsolutePath());
		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		workbook.save(baout, SaveFormat.CSV);
		File xlsxInvoiceFile = new File("TestCsvFile");
		FileUtils.writeByteArrayToFile(xlsxInvoiceFile, baout.toByteArray());
		CsvReader csvReader = new CsvReader();
		csvReader.setContainsHeader(true);
		CsvContainer csv = csvReader.read(xlsxInvoiceFile, StandardCharsets.UTF_8);

		ArrayList<ShareholderMasterResidentialErrorReportCsvDTO> errorList = new ArrayList<>();
		ArrayList<ShareholderMasterResidential> successList = new ArrayList<>();

		AtomicInteger errorCount = new AtomicInteger();
		Long duplicateFolioNumberCount = 0l;
		Map<String, CsvRow> csvMap = new HashMap<>();
		List<CsvRow> listCsvRows = null;
		List<CsvRow> listCsvRowsUnique = new ArrayList<>();
		List<String> folioNoList = shareholderMasterResidentialDAO.getListOfFolioNo(deductorPan);
		listCsvRows = csv.getRows();

		listCsvRows.stream().forEach(n -> {//
			if (csvMap.containsKey(n.getField("FOLIO NUMBER"))) {
				errorCount.addAndGet(1);
				ShareholderMasterResidentialErrorReportCsvDTO errors = generateErrorDTO(n);
				errors.setReason("Folio number " + n.getField("FOLIO NUMBER") + " is duplicate.");
				errorList.add(errors);

			} else {
				csvMap.put(n.getField("FOLIO NUMBER"), n);
				listCsvRowsUnique.add(n);
			}
		});

		listCsvRowsUnique.stream().forEach(row -> {
			logger.info("Reading row no "+row.getOriginalLineNumber()+"{}");
			try {
				if (checkNull(row)) {

					String errorReason = "";

					String folioNo = row.getField("FOLIO NUMBER");
					if (StringUtils.isNotBlank(folioNo)) {
						if (isFolioNumberDuplicate(folioNo, folioNoList).equals(true)) {
							errorReason = errorReason + "\n" + "Folio number " + folioNo + " is duplicate.";
						}
					} else {
						errorReason = errorReason + "\n" + "Folio number is Mandatory.";
					}

					String shareHolderName = row.getField("SHAREHOLDER NAME");
					if (StringUtils.isBlank(shareHolderName)) {
						errorReason = errorReason + "\n" + "SHAREHOLDER NAME is Mandatory.";
					}

					String shareHolderCatagory = row.getField("CATEGORY OF SHAREHOLDER");
					if (StringUtils.isNotBlank(shareHolderCatagory)) {
						if (!ValidationUtil.validateShareholderCategory(shareHolderCatagory)) {
							errorReason = errorReason + "\n" + "Shareholder Category " + shareHolderCatagory
									+ " is not valid.";
						}
					} else {
						errorReason = errorReason + "\n" + "Shareholder Category  is Mandatory.";
					}

					String shareHolderType = row.getField("TYPE OF SHAREHOLDER");
					String pan = row.getField("PAN");

					boolean isValidPan=true;
					if (StringUtils.isNotBlank(pan)) {
						if (pan.matches("[A-Z]{5}[0-9]{4}[A-Z]{1}") && ValidationUtil.validateShareholderPan(pan)) {
						} else {
							errorReason = errorReason + "\n" + "Pan " + pan + " is not valid.";
							isValidPan=false;
						}
					}

					if (StringUtils.isBlank(shareHolderType) && StringUtils.isBlank(pan)) {
						// Generate error report
						errorReason = errorReason + "\n" + "Either Shareholder type or PAN value should exist";

					} else if (StringUtils.isNotBlank(pan) && isValidPan && StringUtils.isBlank(shareHolderType)) {
						String shareholderType = ValidationUtil.getShareholderTypeByPan(pan);
						if (shareholderType == null) {
							// Generate error report
							errorReason = errorReason + "\n" + "Invalid Shareholder Type for given PAN " + pan;
						}
					} else if (StringUtils.isBlank(pan) && StringUtils.isNotBlank(shareHolderType)) {
						boolean flag = ValidationUtil.validateShareholderType(shareHolderType);
						if (!flag) {
							// Generate error report
							errorReason = errorReason + "\n" + "Invalid Shareholder Type " + shareHolderType;
						}
					} else {
						boolean flag =  isValidPan?ValidationUtil.validateShareholderTypeAndPan(shareHolderType, pan):false;
						if (!flag) {

							errorReason = errorReason + "\n" + "Invalid Shareholder Type " + shareHolderType
									+ " for given PAN " + pan;
						}
					}

					if (StringUtils.isNotBlank(row.getField("AADHAR NUMBER"))
							&& !row.getField("AADHAR NUMBER").matches("^[2-9]{1}[0-9]{3}[0-9]{4}[0-9]{4}$")) {
						errorReason = errorReason + "\n" + "Invalid Aadhar Number";
					}

					String pincode = row.getField("PIN/ ZIP CODE");
					if (StringUtils.isNotBlank(pincode) && pincode.matches(".*[a-zA-Z]+.*")) {

						errorReason = errorReason + "Pin Code Can not Contain Alphabetical Chracters";
					}

					String country = row.getField("COUNTRY");
					if (StringUtils.isNotBlank(country)) {
						if (!ValidationUtil.validateCountry(country.trim())) {
							errorReason = errorReason + "\n" + "Shareholder Country " + country + " is not valid.";
						}
					} else {
						errorReason = errorReason + "\n" + "Shareholder Country  is Mandatory.";
					}

					String totalShare = row.getField("TOTAL NUMBER OF SHARES HELD");
					if (StringUtils.isNotBlank(totalShare)) {
						try {
							Double.parseDouble(totalShare);
						} catch (Exception e) {
							errorReason = errorReason + "\n"
									+ "TOTAL NUMBER OF SHARES HELD Column Can Contain Only Integer";
						}
					}
					String percentageOfShare = row.getField("PERCENTAGE OF SHARES HELD ");
					if (StringUtils.isNotBlank(percentageOfShare)) {
						try {
							Double.parseDouble(percentageOfShare);
						} catch (Exception e) {
							errorReason = errorReason + "\n"
									+ "PERCENTAGE OF SHARES HELD  Column Can Contain Only Decimal Values";
						}
					}

					if (StringUtils.isBlank(errorReason)) {
						ShareholderMasterResidential resident = generateSuccessDTO(row, assesssmentYear,
								uploadedFile.getOriginalFilename(), deductorPan,batchUpload.getBatchUploadID(),userName);
						successList.add(resident);
						resident = null;
					} else {
						errorCount.addAndGet(1);
						ShareholderMasterResidentialErrorReportCsvDTO errors = generateErrorDTO(row);
						errors.setReason(errorReason);
						errorList.add(errors);
						errors = null;
					}
				}
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				logger.info("Exception occured while reading share holder resident excel {}"+e);
			} // if
		});// for loop iterating rows

		// save the successrecords
		try {
			logger.info("Sucess records to be saved " + successList + "{}");
			if (!successList.isEmpty()) {
				shareholderMasterResidentialDAO.batchSaveShareHolderResident(successList);
			}
		} catch (Exception e) {
			logger.info("Exception occured while saving list of share holder records {}");
		}

		batchUpload.setSuccessCount((long) successList.size());
		batchUpload.setFailedCount((long) errorCount.get());
		batchUpload.setProcessedCount(successList.size());
		batchUpload.setDuplicateCount((long) duplicateFolioNumberCount);
		batchUpload.setStatus("Processed");
		batchUpload.setProcessEndTime(new Timestamp(System.currentTimeMillis()));
		batchUpload.setModifiedBy(userName);
		batchUpload.setModifiedDate(new Timestamp(System.currentTimeMillis()));
		batchUpload.setRowsCount((long) successList.size() + errorCount.get());

		if (!errorList.isEmpty()) {
			List<String> errorHeader = Arrays.asList("FOLIO NUMBER", "UNIQUE SHAREHOLDER IDENTIFICATION NUMBER",
					"SHAREHOLDER NAME", "CATEGORY OF SHAREHOLDER", "TYPE OF SHAREHOLDER",
					"KEY SHAREHOLDER (AFFILIATED/ RELATED ENTITY)", "PAN", "AADHAR NUMBER",
					"FLAT/ DOOR/ BUILDING NUMBER", "NAME OF PREMISES/ BUILDING/  VILLAGE", "ROAD/ STREET",
					"AREA/ LOCALITY", "TOWN/ CITY/ DISTRICT", "STATE", "PIN/ ZIP CODE", "COUNTRY", "EMAIL ID",
					"CONTACT NUMBER", "SHARE TRANSFER AGENT NAME", "DEMAT ACCOUNT NUMBER",
					"TOTAL NUMBER OF SHARES HELD", "PERCENTAGE OF SHARES HELD", "IS FORM 15G/H AVAILABLE",
					"FORM 15G/H UNIQUE IDENTIFICATION NUMBER");
			shareholderErrorFile = prepareResidentShareholderErrorFile(uploadedFile.getOriginalFilename(), deductorTan,
					deductorPan, errorList, new ArrayList<>(errorHeader));
		}

		// Generating Shareholder pan file and uploading to pan validation

		MultipartFile panfile = null;
		panfile = generateShareholderPanXlsxReport(Collections.emptyList(), successList);
		String panUrl = blob.uploadExcelToBlob(panfile, tenantId);
		batchUpload.setOtherFileUrl(panUrl);

		// passing null in place of uploaded file, as the file is already being uploaded
		// and saved
		return shareholderBatchUpload(batchUpload, null, deductorTan, assesssmentYear, assessmentMonthPlusOne, userName,
				shareholderErrorFile, tenantId);

	}

	private ShareholderMasterResidentialErrorReportCsvDTO generateErrorDTO(CsvRow row) {
		ShareholderMasterResidentialErrorReportCsvDTO resident = new ShareholderMasterResidentialErrorReportCsvDTO();
		resident.setFolioNo(row.getField("FOLIO NUMBER"));
		resident.setUniqueIdentificationNumber(row.getField("UNIQUE SHAREHOLDER IDENTIFICATION NUMBER"));
		resident.setUniqueIdentificationNumber(row.getField("UNIQUE SHAREHOLDER IDENTIFICATION NUMBER"));
		resident.setShareholderName(row.getField("SHAREHOLDER NAME"));
		resident.setShareholderCategory(row.getField("CATEGORY OF SHAREHOLDER"));
		resident.setShareholderPan(row.getField("PAN"));
		resident.setShareholderType(row.getField("TYPE OF SHAREHOLDER"));
		resident.setKeyShareholder(row.getField("KEY SHAREHOLDER (AFFILIATED/ RELATED ENTITY)"));
		resident.setAadharNumber(row.getField("AADHAR NUMBER"));
		resident.setFlatDoorBlockNo(row.getField("FLAT/ DOOR/ BUILDING NUMBER"));
		resident.setRoadStreetPostoffice(row.getField("ROAD/ STREET"));
		resident.setAreaLocality(row.getField("AREA/ LOCALITY"));
		resident.setTownCityDistrict(row.getField("TOWN/ CITY/ DISTRICT"));
		resident.setState(row.getField("STATE"));
		resident.setPinCode(row.getField("PIN/ ZIP CODE"));
		resident.setCountry(row.getField("COUNTRY"));
		resident.setEmailId(row.getField("EMAIL ID"));
		resident.setContact(row.getField("CONTACT NUMBER"));
		resident.setShareTransferAgentName(row.getField("SHARE TRANSFER AGENT NAME"));
		resident.setDematAccountNo(row.getField("DEMAT ACCOUNT NUMBER"));
		resident.setTotalSharesHeld(row.getField("TOTAL NUMBER OF SHARES HELD"));
		resident.setPercentageSharesHeld(row.getField("PERCENTAGE OF SHARES HELD "));
		resident.setForm15ghAvailable(row.getField("IS FORM 15G/H AVAILABLE"));
		resident.setForm15ghUniqueIdentificationNo(row.getField("FORM 15G/H UNIQUE IDENTIFICATION NUMBER"));
		resident.setNameBuildingVillage(row.getField("NAME OF PREMISES/ BUILDING/  VILLAGE"));
		return resident;
	}

	private ShareholderMasterResidential generateSuccessDTO(CsvRow row, Integer assesssmentYear, String fileName,
			String deductorPan,int batchUploadId,String userName) {
		ShareholderMasterResidential resident = new ShareholderMasterResidential();
		
		resident.setCreatedBy(userName);
		resident.setCreatedDate(new Timestamp(System.currentTimeMillis()));
		resident.setBatchUploadId(batchUploadId);
		resident.setFolioNo(row.getField("FOLIO NUMBER"));
		resident.setUniqueIdentificationNumber(row.getField("UNIQUE SHAREHOLDER IDENTIFICATION NUMBER"));
		resident.setShareholderName(row.getField("SHAREHOLDER NAME"));
		resident.setShareholderCategory(row.getField("CATEGORY OF SHAREHOLDER"));
		resident.setShareholderPan(StringUtils.isBlank(row.getField("PAN"))?null:row.getField("PAN").trim());
		resident.setShareholderType(row.getField("TYPE OF SHAREHOLDER"));
		String isKey = row.getField("KEY SHAREHOLDER (AFFILIATED/ RELATED ENTITY)");
		resident.setKeyShareholder(StringUtils.isNotBlank(isKey) && isKey.equals("Yes") ? true : false);
		resident.setAadharNumber(row.getField("AADHAR NUMBER"));
		resident.setFlatDoorBlockNo(row.getField("FLAT/ DOOR/ BUILDING NUMBER"));
		resident.setRoadStreetPostoffice(row.getField("ROAD/ STREET"));
		resident.setAreaLocality(row.getField("AREA/ LOCALITY"));
		resident.setTownCityDistrict(row.getField("TOWN/ CITY/ DISTRICT"));
		resident.setState(row.getField("STATE"));
		resident.setPinCode(row.getField("PIN/ ZIP CODE"));
		resident.setCountry(row.getField("COUNTRY"));
		resident.setEmailId(row.getField("EMAIL ID"));
		resident.setContact(row.getField("CONTACT NUMBER"));
		resident.setShareTransferAgentName(row.getField("SHARE TRANSFER AGENT NAME"));
		resident.setDematAccountNo(row.getField("DEMAT ACCOUNT NUMBER"));
		if (StringUtils.isNotBlank(row.getField("TOTAL NUMBER OF SHARES HELD"))) {
			resident.setTotalSharesHeld(new BigDecimal(row.getField("TOTAL NUMBER OF SHARES HELD")));
		} else {
			resident.setTotalSharesHeld(BigDecimal.ZERO);
		}
		if (StringUtils.isNotBlank(row.getField("PERCENTAGE OF SHARES HELD "))) {
			resident.setPercentageSharesHeld(new BigDecimal(row.getField("PERCENTAGE OF SHARES HELD ")));
		} else {
			resident.setPercentageSharesHeld(BigDecimal.ZERO);
		}
		String isForm15GHAvailable = row.getField("IS FORM 15G/H AVAILABLE");
		resident.setForm15ghAvailable(
				StringUtils.isNotBlank(isForm15GHAvailable) && isForm15GHAvailable.equals("Yes") ? true : false);
		resident.setForm15ghUniqueIdentificationNo(row.getField("FORM 15G/H UNIQUE IDENTIFICATION NUMBER"));
		resident.setNameBuildingVillage(row.getField("NAME OF PREMISES/ BUILDING/  VILLAGE"));
		resident.setSourceIdentifier("EXCEL FILE");
		resident.setSourceFileName(fileName);
		resident.setStringAssesmentYearDividendDetails("{" + assesssmentYear + ":0.0}");
		resident.setDeductorPan(deductorPan);
		resident.setShareholderResidentialStatus("RES");
		return resident;
	}

	private Boolean isFolioNumberDuplicate(String folioNo, List<String> list) {
		return list.stream().anyMatch(n -> n.trim().equals(folioNo));
	}

	public boolean checkNull(CsvRow row) throws IllegalAccessException {
		boolean isnull = true;
		isnull = row.getFields().stream().anyMatch(n -> StringUtils.isNotBlank(n));
		return isnull;
	}

	@Async
	public BatchUpload processNonResidentShareholdersWithCSV(XSSFWorkbook wb, MultipartFile uploadedFile, String sha256,
			String deductorTan, Integer assesssmentYear, Integer assessmentMonthPlusOne, String userName,
			String tenantId, String deductorPan, BatchUpload batchUpload) throws Exception {

		MultiTenantContext.setTenantId(tenantId);

		File shareholderErrorFile = null;
		File file = blobStorageService.getFileFromBlobUrl(tenantId, batchUpload.getFilePath());
		NonResidentShareholderExcel excel = new NonResidentShareholderExcel(wb);
		Workbook workbook = new Workbook(file.getAbsolutePath());
		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		workbook.save(baout, SaveFormat.CSV);
		File xlsxInvoiceFile = new File("TestCsvFile");
		FileUtils.writeByteArrayToFile(xlsxInvoiceFile, baout.toByteArray());
		CsvReader csvReader = new CsvReader();
		csvReader.setContainsHeader(true);
		CsvContainer csv = csvReader.read(xlsxInvoiceFile, StandardCharsets.UTF_8);

		ArrayList<ShareholderMasterNonResidentialErrorReportCsvDTO> errorList = new ArrayList<>();
		ArrayList<ShareholderMasterNonResidential> successList = new ArrayList<>();

		AtomicInteger errorCount = new AtomicInteger();
		Long duplicateFolioNumberCount = 0l;
		Map<String, CsvRow> csvMap = new HashMap<>();
		List<CsvRow> listCsvRows = null;
		List<CsvRow> listCsvRowsUnique = new ArrayList<>();
		List<String> folioNoList = shareholderMasterNonResidentialDAO.getListOfFolioNo(deductorPan);
		listCsvRows = csv.getRows();

		listCsvRows.stream().forEach(n -> {//
			try {
				if (checkNull(n)) {
					if (csvMap.containsKey(n.getField("FOLIO NUMBER"))) {
						errorCount.addAndGet(1);
						ShareholderMasterNonResidentialErrorReportCsvDTO errors = generateErrorDTO(n, deductorPan);
						errors.setReason("Folio number " + n.getField("FOLIO NUMBER") + " is duplicate.");
						errorList.add(errors);

					} else {
						csvMap.put(n.getField("FOLIO NUMBER"), n);
						listCsvRowsUnique.add(n);
					}
				}
			} catch (IllegalAccessException e) {
				logger.info("Exception occured while filtering unique CSV records");
			}
		});

		listCsvRowsUnique.stream().forEach(row -> {
			logger.info("Reading row no "+row.getOriginalLineNumber()+"{}");

			try {
				if (checkNull(row)) {
					String reason = "";

					String folioNo = row.getField(NonResidentShareholderExcel.HEADER_SHAREHOLDER_FOLIONO);
					if (StringUtils.isNotBlank(folioNo)) {
						if (isFolioNumberDuplicate(folioNo.trim(), folioNoList).equals(true)) {
							reason = reason + "Folio number " + folioNo + " is duplicate \n ";
						}
					} else {
						reason = reason + "Folio number  is Mandatory \n ";
					}

					String country = row.getField(NonResidentShareholderExcel.HEADER_COUNTRY);
					if (StringUtils.isNotBlank(country)) {
						if (!ValidationUtil.validateCountry(country.trim())) {
							reason = reason + "Shareholder Country " + country + " is not valid,\n";
						}
					} else {
						reason = reason + "Shareholder Country  is  Mandatory,\n";
					}

					String catagory = row.getField(NonResidentShareholderExcel.HEADER_SHAREHOLDER_CATEGORY);
					if (StringUtils.isNotBlank(catagory)) {
						if (!ValidationUtil.validateShareholderCategory(catagory.trim())) {
							reason = reason + "Shareholder Category " + catagory + " is not valid \n";
						}
					} else {
						reason = reason + "Shareholder Category  is  Mandatory,\n";
					}

					String pan = row.getField(NonResidentShareholderExcel.HEADER_SHAREHOLDER_PAN);
					boolean isValidPan=true;
					if (StringUtils.isNotBlank(pan)) {
						if (pan.matches("[A-Z]{5}[0-9]{4}[A-Z]{1}")) {
							if (!ValidationUtil.validateShareholderPan(pan)) {
								reason = reason + "Pan " + pan + " is not valid \n";
								isValidPan=false;
							}
						} else {
							reason = reason + "PAN should be of pattern ABCDE1234F \n";
							isValidPan=false;
						}
					}

					String type = row.getField(NonResidentShareholderExcel.HEADER_SHAREHOLDER_TYPE);
					if (StringUtils.isBlank(type) && StringUtils.isBlank(pan)) {
						logger.info("Either Shareholder type or PAN value should exist");
						reason = reason + "Either Shareholder type or PAN value should exist \n";

					} else if (StringUtils.isNotBlank(pan) && StringUtils.isBlank(type) && isValidPan) {
						String shareholderType = ValidationUtil.getShareholderTypeByPan(pan);
						if (shareholderType == null) {
							reason = reason + "Invalid Shareholder Type for given PAN " + pan + "\n";
						}
					} else if (StringUtils.isBlank(pan) && StringUtils.isNotBlank(type)) {
						boolean flag = ValidationUtil.validateShareholderType(type);
						if (!flag) {
							reason = "Invalid Shareholder Type " + type + "\n";
						}
					} else {
						boolean flag = isValidPan?ValidationUtil.validateShareholderTypeAndPan(type, pan):false;
						if (!flag) {
							reason = reason + "Invalid Shareholder Type " + type + " for given PAN " + pan + "\n";
						}
					}

					String stringShareHeldFrom = row.getField(NonResidentShareholderExcel.HEADER_SHARE_HELD_FROM_DATE);
					String stringShareHeldTo = row.getField(NonResidentShareholderExcel.HEADER_SHARES_HELD_TO_DATE);
					Date shareHeldFrom = null;
					Date shareHeldTo = null;

					if (StringUtils.isNotBlank(stringShareHeldFrom)) {
						try {
							shareHeldFrom = dateConverter(stringShareHeldFrom);
						} catch (Exception e) {
							reason = reason
									+ "Please Provide Date in Specified Format For DURATION OF SHARES HELD (FROM DATE) DD/MM/YYYY";
						}
					}

					if (StringUtils.isNotBlank(stringShareHeldTo)) {
						try {
							shareHeldTo = dateConverter(stringShareHeldTo);
						} catch (Exception e) {
							reason = reason
									+ "Please Provide Date in Specified Format For DURATION OF SHARES HELD (TO DATE)\n"
									+ "DD/MM/YYYY";
						}
					}

					if (shareHeldFrom != null) {
						if (shareHeldTo != null
								&& (shareHeldFrom.equals(shareHeldTo) || shareHeldFrom.after(shareHeldTo))) {
							reason = reason
									+ "Share Held From Date should not be equals or greater than Applicable To Date \n";
						}
					}

					String stringTrcFrom = row.getField(NonResidentShareholderExcel.HEADER_TRC_APPLICABLE_FROM);
					String stringTrcTo = row.getField(NonResidentShareholderExcel.HEADER_TRC_APPLICABLE_TO);
					Boolean isTrc = booleanConverter(row.getField(NonResidentShareholderExcel.HEADER_IS_TRC_AVAILABLE));
					Date trcFrom = null;
					Date trcTo = null;
					// Is TRC Available is true
					if (isTrc.equals(true)) {

						try {
							trcFrom = dateConverter(stringTrcFrom);
						} catch (Exception e) {
							reason = reason + "Invalid Date Format In TRC Applicable From Date . \n";
						}
						try {
							trcTo = dateConverter(stringTrcTo);
						} catch (Exception e) {
							reason = reason + "Invalid Date Format In Trc Applicable To Date . \n";
						}
						if (trcFrom == null) {
							reason = reason + "TRC Applicable From Date is mandatory. \n";
						} else {
							if (trcFrom != null) {
								if (trcTo != null && (trcFrom.equals(trcTo) || trcFrom.after(trcTo))) {
									reason = reason
											+ "Trc Applicable From Date should not be equals or greater than Trc Applicable To Date \n";
								}
							}
						}
					}

					String stringTenFFrom = row.getField(NonResidentShareholderExcel.HEADER_FORM_TEN_F_APPLICABLE_FROM);
					String stringTenFTo = row.getField(NonResidentShareholderExcel.HEADER_FORM_TEN_F_APPLICABLE_TO);
					Boolean isTenF = booleanConverter(
							row.getField(NonResidentShareholderExcel.HEADER_IS_FORM_TEN_F_AVAILABLE));
					Date tenFFrom = null;
					Date tenFTo = null;
					// Is TenF Available is true
					if (isTenF.equals(true)) {
						try {
							tenFFrom = dateConverter(stringTenFFrom);
						} catch (Exception e) {
							reason = reason + "Invalid Date Format In TENF Applicable From Date . \n";
						}
						try {
							tenFTo = dateConverter(stringTenFTo);
						} catch (Exception e) {
							reason = reason + "Invalid Date Format In TENF Applicable To Date . \n";
						}
						if (tenFFrom == null) {
							reason = reason + "TENF Applicable From Date is mandatory. \n";
						} else {
							if (tenFFrom != null) {
								if (tenFTo != null && (tenFFrom.equals(tenFTo) || tenFFrom.after(tenFTo))) {
									reason = reason
											+ "TENF Applicable From Date should not be equals or greater than TENF Applicable To Date \n";
								}
							}
						}
					}

					String stringIsNoPeDeclarationFrom = row
							.getField(NonResidentShareholderExcel.HEADER_NO_PE_DECLARATION_APPLICABLE_FROM);
					String stringIsNoPeDeclarationTo = row
							.getField(NonResidentShareholderExcel.HEADER_NO_PE_DECLARATION_APPLICABLE_TO);
					Boolean isNoPeDeclaration = booleanConverter(
							row.getField(NonResidentShareholderExcel.HEADER_IS_NO_PE_DECLARATION_AVAILABLE));
					Date noPeDeclarationFrom = null;
					Date noPeDeclarationTo = null;
					// No PE Document Available is true
					if (isNoPeDeclaration.equals(true)) {
						try {
							noPeDeclarationFrom = dateConverter(stringIsNoPeDeclarationFrom);
						} catch (Exception e) {
							reason = reason + "Invalid Date Format In NO PE Applicable From Date . \n";
						}
						try {
							noPeDeclarationTo = dateConverter(stringIsNoPeDeclarationTo);
						} catch (Exception e) {
							reason = reason + "Invalid Date Format In NO PE Applicable To Date . \n";
						}
						if (noPeDeclarationFrom == null) {
							reason = reason + "NO PE Applicable From Date is mandatory. \n";
						} else {
							if (noPeDeclarationFrom != null) {
								if (noPeDeclarationTo != null && (noPeDeclarationFrom.equals(noPeDeclarationTo)
										|| noPeDeclarationFrom.after(noPeDeclarationTo))) {
									reason = reason
											+ "NO PE Document Applicable From Date should not be equals or greater than NO PE Document Applicable To Date \n";
								}
							}
						}
					}

					String stringIsNoPoemDeclarationFrom = row
							.getField(NonResidentShareholderExcel.HEADER_NO_PE_DECLARATION_APPLICABLE_FROM);
					String stringNoPoemDeclarationTo = row
							.getField(NonResidentShareholderExcel.HEADER_NO_PE_DECLARATION_APPLICABLE_TO);
					Boolean isIsNoPeDeclaration = booleanConverter(
							row.getField(NonResidentShareholderExcel.HEADER_IS_NO_PE_DECLARATION_AVAILABLE));
					Date noPoemDeclarationFrom = null;
					Date noPoemDeclarationTo = null;
					// Is POEM Available is true
					if (isIsNoPeDeclaration.equals(true)) {
						try {
							noPoemDeclarationFrom = dateConverter(stringIsNoPoemDeclarationFrom);
						} catch (Exception e) {
							reason = reason + "Invalid Date Format In POEM Applicable From Date . \n";
						}
						try {
							noPoemDeclarationTo = dateConverter(stringNoPoemDeclarationTo);
						} catch (Exception e) {
							reason = reason + "Invalid Date Format In POEM Applicable To Date . \n";
						}
						if (noPoemDeclarationFrom == null) {
							// error report
							reason = reason + "POEM Applicable From Date is mandatory \n";
						} else {
							if (noPoemDeclarationFrom != null) {
								if (noPoemDeclarationTo != null && (noPoemDeclarationFrom.equals(noPoemDeclarationTo)
										|| noPoemDeclarationFrom.after(noPoemDeclarationTo))) {
									// error report
									reason = reason
											+ "POEM Applicable From Date should not be equals or greater than POEM Applicable To Date \n";
								}
							}
						}
					}
					if (StringUtils.isEmpty(reason)) {
						ShareholderMasterNonResidential nr;
						try {
							nr = generateSuccessDTO(row, assesssmentYear, uploadedFile.getOriginalFilename(),
									deductorPan, userName,batchUpload.getBatchUploadID());
							successList.add(nr);
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							logger.info("Exception occured wile preparing NR share holder DTO to save {}" + e);
							e.printStackTrace();
						}

					} else {
						ShareholderMasterNonResidentialErrorReportCsvDTO error = generateErrorDTO(row, deductorPan);
						error.setReason(reason);
						errorCount.addAndGet(1);
						errorList.add(error);
					}
				} // if block to check null records
			} catch (Exception e) {
				logger.info("Exception occured :" + e);
			}

		});
		if (!successList.isEmpty()) {
			logger.info("Saving success NR shareholder Data {}");
			shareholderMasterNonResidentialDAO.batchSaveShareHolderNonResident(successList);
		}
		batchUpload.setSuccessCount((long) successList.size());
		batchUpload.setFailedCount((long) errorCount.get());
		batchUpload.setProcessedCount(successList.size());
		batchUpload.setDuplicateCount(0l);
		batchUpload.setStatus("Processed");
		batchUpload.setProcessEndTime(new Timestamp(System.currentTimeMillis()));
		batchUpload.setCreatedBy(userName);
		batchUpload.setRowsCount((long) successList.size() + errorCount.get());
		if (!errorList.isEmpty()) {
			shareholderErrorFile = prepareNonResidentShareholderErrorFile(uploadedFile.getOriginalFilename(),
					deductorTan, deductorPan, errorList, new ArrayList<>(excel.getHeaders()));
		}
		// Generating shareholder pan file and uploading to pan validation
		MultipartFile panFile = null;
		panFile = generateShareholderPanXlsxReport(successList, Collections.emptyList());
		String panUrl = blob.uploadExcelToBlob(panFile, tenantId);
		batchUpload.setOtherFileUrl(panUrl);
		return shareholderBatchUpload(batchUpload, null, deductorTan, assesssmentYear, assessmentMonthPlusOne, userName,
				shareholderErrorFile, tenantId);
	}

}
