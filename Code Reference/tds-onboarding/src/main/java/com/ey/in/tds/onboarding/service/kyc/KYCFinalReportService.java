package com.ey.in.tds.onboarding.service.kyc;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFDataValidationConstraint;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.aspose.cells.Cells;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Worksheet;
import com.aspose.cells.XlsSaveOptions;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.BatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.KYCDetails;
import com.ey.in.tds.common.service.BlobStorageService;
import com.ey.in.tds.common.service.Sha256SumService;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.jdbc.dao.DeductorMasterDAO;
import com.ey.in.tds.jdbc.dao.KYCDetailsDAO;
import com.microsoft.azure.storage.StorageException;

import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;

/**
 * @author Prince Gupta
 */

@Service
public class KYCFinalReportService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private KYCDetailsDAO kycDetailsDAO;

    private Sha256SumService sha256SumService;

    private BatchUploadDAO batchUploadDAO;

    private BlobStorage blobStorage;

    @Autowired
    private BlobStorageService blobStorageService;

    private KYCDetailsService kycDetailsService;

    private ResourceLoader resourceLoader;

    private DeductorMasterDAO deductorMasterDAO;

    private static String[] clientFinalResponseTCS = new String[]{"TCS  - Normal rate", "TCS  - Higher rate - No ITR",
            "TCS  - Higher rate - No PAN", "TCS  - Higher rate - No PAN & ITR"};
    private static String[] clientFinalResponseTDS = new String[]{"TDS  - Normal rate", "TDS  - Higher rate - No ITR",
            "TDS  - Higher rate - No PAN", "TDS  - Higher rate - No PAN & ITR"};
    private static String[] actions = new String[]{"Accept", "Reject"};

    @Autowired
    KYCFinalReportService(final KYCDetailsDAO kycDetailsDAO, final Sha256SumService sha256SumService,
                          final BatchUploadDAO batchUploadDAO, final BlobStorage blobStorage,
                          final KYCDetailsService kycDetailsService, final ResourceLoader resourceLoader,
                          final DeductorMasterDAO deductorMasterDAO) {
        this.kycDetailsDAO = kycDetailsDAO;
        this.sha256SumService = sha256SumService;
        this.batchUploadDAO = batchUploadDAO;
        this.blobStorage = blobStorage;
        this.kycDetailsService = kycDetailsService;
        this.resourceLoader = resourceLoader;
        this.deductorMasterDAO = deductorMasterDAO;
    }

    /**
     * This method will identify and update KYC Details * for PAN & ITR Joint
     * Condition for Higher Tax or Normal Tax Applicability.
     *
     * @param deductorPan
     * @param tan
     * @param type
     */

    public void triggerFinalReport(String deductorPan, String tenantId, String tan, String userName, Integer year,
                                   String type) throws Exception {
        String tcs_tds_flag;
        boolean isTCS;
        if ("CUSTOMER".equalsIgnoreCase(type)) {
            tcs_tds_flag = "TCS";
            isTCS = true;
        } else if ("VENDOR".equalsIgnoreCase(type)) {
            tcs_tds_flag = "TDS";
            isTCS = false;
        } else {
            tcs_tds_flag = "TCS";
            isTCS = true;
        }

        List<KYCDetails> kycList;
        if (isTCS) {
            kycList = kycDetailsDAO.getKycTCSOrTDSList(deductorPan, tan, year, tcs_tds_flag, type);
        } else {
            kycList = kycDetailsDAO.getKycListTypeWise(deductorPan, tan, year, type);
        }
        for (KYCDetails kycDetails : kycList) {
            Boolean isITRConditionSatisfied = isITRConditionSatisfied(kycDetails);
            if (isITRConditionSatisfied != kycDetails.getIsPanVerifyStatus()) {
                kycDetails.setIsHigherTcsRateApplicable(true);
                kycDetails.setHigherTcsRateApplicableConclusion(
                        kycDetails.getIsPanVerifyStatus() ? (isTCS ? CONCLUSION.HIGHER_RATE_NO_ITR_TCS.getDisplayName() : CONCLUSION.HIGHER_RATE_NO_ITR_TDS.getDisplayName())
                                : (isTCS ? CONCLUSION.HIGHER_RATE_NO_PAN_TCS.getDisplayName() : CONCLUSION.HIGHER_RATE_NO_PAN_TDS.getDisplayName()));
            } else if (isITRConditionSatisfied && kycDetails.getIsPanVerifyStatus()) {
                kycDetails.setIsHigherTcsRateApplicable(false);
                kycDetails.setHigherTcsRateApplicableConclusion(isTCS ? CONCLUSION.NORMAL_RATE_TCS.getDisplayName() : CONCLUSION.NORMAL_RATE_TDS.getDisplayName());
            } else {
                kycDetails.setIsHigherTcsRateApplicable(true);
                kycDetails.setHigherTcsRateApplicableConclusion(isTCS ? CONCLUSION.HIGHER_RATE_NO_PAN_ITR_TCS.getDisplayName() : CONCLUSION.HIGHER_RATE_NO_PAN_ITR_TDS.getDisplayName());
            }
            kycDetails.setType(type.toUpperCase());
            kycDetailsDAO.update(kycDetails);
        }
        asyncKycFinalReportReport(deductorPan, tenantId, tan, userName, year, type);
    }

    /**
     * Method will process KYC Final Report according to Customer final response.
     *
     * @param deductorTan
     * @param tenantId
     * @param deductorPan
     * @param batchId
     * @param userName
     * @param type
     * @return
     * @throws IOException
     * @throws InvalidKeyException
     * @throws URISyntaxException
     * @throws StorageException
     * @throws ParseException
     */
    @Async
    public BatchUpload saveFinalReportResponseData(String deductorTan, String tenantId, String deductorPan,
                                                   Integer batchId, String userName, String type) throws ParseException {
        MultiTenantContext.setTenantId(tenantId);
        BatchUpload batchUpload = new BatchUpload();
        List<BatchUpload> batchList = batchUploadDAO.getBatchListBasedOnTanAndGroupId(deductorTan, batchId);
        if (!batchList.isEmpty()) {
            batchUpload = batchList.get(0);
            batchUpload.setFailedCount(0L);
            batchUpload.setProcessedCount(0);
            batchUpload.setRowsCount(0L);
            batchUpload.setUploadType(type.toUpperCase());
            batchUpload.setStatus("Processing");
            batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
            batchUploadDAO.update(batchUpload);
            batchUpload = asyncKycFinalReportReport(batchUpload, tenantId, batchList.get(0).getFilePath(), type);
        }
        return batchUpload;
    }

    /**
     * @param batchUpload
     * @param tenantId
     * @param path
     * @return
     */
    private BatchUpload asyncKycFinalReportReport(BatchUpload batchUpload, String tenantId, String path, String type) {
        String otherFileurl = null;
        Biff8EncryptionKey.setCurrentUserPassword("password");
        com.aspose.cells.Workbook workbook;
        com.aspose.cells.Workbook workbook_temp;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            logger.info("kyc file path for final report : {}", path);
            File file = blobStorageService.getFileFromBlobUrl(tenantId, path);
            workbook = new com.aspose.cells.Workbook(file.getAbsolutePath());
            workbook_temp = new com.aspose.cells.Workbook(file.getAbsolutePath());

            Worksheet worksheet = workbook.getWorksheets().get(0);
            worksheet.getCells().deleteRows(0, 6);
            ByteArrayOutputStream baout = new ByteArrayOutputStream();
            workbook.save(baout, SaveFormat.CSV);
            File xlsxInvoiceFile = new File("TestCsvFile");
            FileUtils.writeByteArrayToFile(xlsxInvoiceFile, baout.toByteArray());
            CsvReader csvReader = new CsvReader();
            csvReader.setContainsHeader(true);
            CsvContainer csv = csvReader.read(xlsxInvoiceFile, StandardCharsets.UTF_8);

            List<KYCDetails> kycDetailsList = new ArrayList<>();
            String errorFilepath = null;
            int processedCount = 0;
            int errorCount = 0;
            int rowCount = 7;
            Cells cells = workbook_temp.getWorksheets().get(0).getCells();
            for (CsvRow row : csv.getRows()) {
                String clientFinalResponse = row.getField("Client Final Response");
                String outputIndicative = row.getField("Output Indicative");
                // action token
                String userAction = row.getField("Action Taken");
                // id
                Integer kycId = StringUtils.isNotBlank(row.getField("Kyc Id"))
                        ? Integer.parseInt(row.getField("Kyc Id"))
                        : null;
                if (StringUtils.isNotBlank(userAction)) {
                    KYCDetails kyc = new KYCDetails();
                    if (kycId != null) {
                        kyc.setId(kycId);
                        kyc.setFinalRateUserAction(userAction);
                        if ("Accept".equalsIgnoreCase(userAction)) {
                            kyc.setHigherTcsRateApplicableFinalConclusion(outputIndicative);
                            cells.get(rowCount, 20).setValue(outputIndicative);
                        } else if ("Reject".equalsIgnoreCase(userAction)) {
                            kyc.setHigherTcsRateApplicableFinalConclusion(clientFinalResponse);
                            cells.get(rowCount, 20).setValue(clientFinalResponse);
                        }
                    }
                    kycDetailsList.add(kyc);
                    processedCount++;
                }
                rowCount++;
            }
            // batch update kyc details
            if (!kycDetailsList.isEmpty()) {
                kycDetailsDAO.batchUpdateFinalResponse(kycDetailsList);
            }
            workbook_temp.save(out, new XlsSaveOptions(SaveFormat.XLSX));
            if (batchUpload.getBatchUploadID() != null) {
                String tcs_tds_flag = "VENDOR".equalsIgnoreCase(type) ? "TDS" : "TCS";
                String fileName = tcs_tds_flag + " Rate Remediation Report" + "_" + CommonUtil.TDS_FILE_DATE_FORMATTER.format(new Date()) + ".xlsx";
                File fileInner = getConvertedExcelFile(out.toByteArray(), fileName);
                path = blobStorage.uploadExcelToBlobWithFile(fileInner, tenantId);
                batchUpload.setFilePath(path);
                batchUpload.setProcessedCount(processedCount);
                batchUpload.setFailedCount(Long.valueOf(errorCount));
                batchUpload.setErrorFilePath(errorFilepath);
                batchUpload.setStatus("Processed");
                batchUpload.setOtherFileUrl(otherFileurl);
                batchUpload.setRowsCount((long) processedCount + errorCount);
                batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
                batchUploadDAO.update(batchUpload);
            }
        } catch (Exception e1) {
            logger.error("Exception occurred while updating kyc final report response {}", e1.getMessage());
        }
        return batchUpload;
    }

//    private Boolean isITRConditionSatisfied(final KYCDetails kycDetailsObj) {
//        Boolean itrConditionSatisfied;
//        if ("NA".equalsIgnoreCase(kycDetailsObj.getItrFinancialYear1())) {
//            itrConditionSatisfied = isITRConditionSatisfied(kycDetailsObj.getItrFinancialYear3(),
//                    kycDetailsObj.getAggregateTcsOrTdsGreaterThan50kForYear3(), kycDetailsObj.getItrFinancialYear2(),
//                    kycDetailsObj.getAggregateTcsOrTdsGreaterThan50kForYear2());
//        } else {
//            itrConditionSatisfied = isITRConditionSatisfied(kycDetailsObj.getItrFinancialYear1(),
//                    kycDetailsObj.getAggregateTcsOrTdsGreaterThan50kForYear1(), kycDetailsObj.getItrFinancialYear2(),
//                    kycDetailsObj.getAggregateTcsOrTdsGreaterThan50kForYear2());
//        }
//        return itrConditionSatisfied;
//    }

    private Boolean isITRConditionSatisfied(final KYCDetails kycDetailsObj) {
        if ("NA".equalsIgnoreCase(kycDetailsObj.getItrFinancialYear1())) {
            if ("No".equalsIgnoreCase(kycDetailsObj.getItrFinancialYear3())
                    && "No".equalsIgnoreCase(kycDetailsObj.getItrFinancialYear2())
                    && "Yes".equalsIgnoreCase(kycDetailsObj.getAggregateTcsOrTdsGreaterThan50kForYear3())
                    && "Yes".equalsIgnoreCase(kycDetailsObj.getAggregateTcsOrTdsGreaterThan50kForYear2())
            ) {
                return false;
            } else {
                return true;
            }
        } else {
            if ("No".equalsIgnoreCase(kycDetailsObj.getItrFinancialYear2())
                    && "No".equalsIgnoreCase(kycDetailsObj.getItrFinancialYear1())
                    && "Yes".equalsIgnoreCase(kycDetailsObj.getAggregateTcsOrTdsGreaterThan50kForYear2())
                    && "Yes".equalsIgnoreCase(kycDetailsObj.getAggregateTcsOrTdsGreaterThan50kForYear1())
            ) {
                return false;
            } else {
                return true;
            }
        }
    }

    private Boolean isITRConditionSatisfied(final String year1ITR, final String tcsTdsGt50KYear1, final String year2ITR,
                                            final String tcsTdsGt50KYear2) {
        return !(
                (("No".equalsIgnoreCase(year2ITR) || "NA".equalsIgnoreCase(year2ITR)) && "Yes".equalsIgnoreCase(tcsTdsGt50KYear2)) &&
                        (("No".equalsIgnoreCase(year1ITR) || "NA".equalsIgnoreCase(year1ITR)) && "Yes".equalsIgnoreCase(tcsTdsGt50KYear1))
        );
    }

    private enum CONCLUSION {
        HIGHER_RATE_NO_ITR_TCS("TCS  - Higher rate - No ITR"), HIGHER_RATE_NO_PAN_TCS("TCS  - Higher rate - No PAN"),
        HIGHER_RATE_NO_PAN_ITR_TCS("TCS  - Higher rate - No PAN & ITR"), NORMAL_RATE_TCS("TCS  - Normal rate"),
        HIGHER_RATE_NO_ITR_TDS("TDS  - Higher rate - No ITR"), HIGHER_RATE_NO_PAN_TDS("TDS  - Higher rate - No PAN"),
        HIGHER_RATE_NO_PAN_ITR_TDS("TDS  - Higher rate - No PAN & ITR"), NORMAL_RATE_TDS("TDS  - Normal rate");

        private String displayName;

        CONCLUSION(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
    }

    /**
     * @param deductorPan
     * @param tenantId
     * @param deductorTan
     * @param userName
     * @throws Exception
     */
    @Async
    private void asyncKycFinalReportReport(String deductorPan, String tenantId, String deductorTan, String userName,
                                           Integer year, String type) throws Exception {
        MultiTenantContext.setTenantId(tenantId);
        kycFinalReport(deductorPan, tenantId, deductorTan, userName, year, type);
    }

    /**
     * @param tenantId
     * @param deductorPan
     * @return
     */
    private String getReportHeader(String tenantId, String deductorPan) {
        MultiTenantContext.setTenantId(tenantId);
        List<DeductorMaster> getDeductor = deductorMasterDAO.findBasedOnDeductorPan(deductorPan);
        String pattern = "dd-MM-yyyy hh:mm aaa";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
        String date = simpleDateFormat.format(new Date());
        return "Final Report (Dated: " + date + ")\n Client Name: " + getDeductor.get(0).getName() + "\n";
    }

    private String getPANConsideredForTV(KYCDetails kyc) {
        if (StringUtils.isNotBlank(kyc.getKycPan()) && StringUtils.isNotBlank(kyc.getCustomerPan())) {
            return "KYC";
        } else if (StringUtils.isNotBlank(kyc.getKycPan()) && StringUtils.isBlank(kyc.getCustomerPan())) {
            return "KYC";
        } else if (StringUtils.isBlank(kyc.getKycPan()) && StringUtils.isNotBlank(kyc.getCustomerPan())) {
            return "Input";
        }
        return "NA";
    }

    /**
     * @param deductorPan
     * @param tenantId
     * @param deductorTan
     * @param userName
     * @throws Exception
     */
    private void kycFinalReport(String deductorPan, String tenantId, String deductorTan, String userName, Integer year,
                                String type) throws Exception {
        MultiTenantContext.setTenantId(tenantId);

        int month = CommonUtil.getAssessmentMonthPlusOne(null);
        String tcs_tds_flag;
        String uploadType = StringUtils.EMPTY;
        if ("CUSTOMER".equalsIgnoreCase(type)) {
            uploadType = UploadTypes.CUSTOMER_KYC_FINAL_REPORT.name();
            tcs_tds_flag = "TCS";
        } else if ("VENDOR".equalsIgnoreCase(type)) {
            uploadType = UploadTypes.VENDOR_KYC_FINAL_REPORT.name();
            tcs_tds_flag = "TDS";
        } else {
            uploadType = UploadTypes.SHAREHOLDER_KYC_FINAL_REPORT.name();
            tcs_tds_flag = "TCS";
        }
        String fileName = uploadType;
        BatchUpload batchUpload = kycDetailsService.saveBatchUploadReport(deductorTan, tenantId, year, null, 0L,
                uploadType, "Processing", month, userName, null, fileName);
        List<KYCDetails> kycList;
        if ("TCS".equalsIgnoreCase(tcs_tds_flag)) {
            // get kyc list
            kycList = kycDetailsDAO.getKycTCSOrTDSList(deductorPan, deductorTan, year, tcs_tds_flag, type);
        } else {
            kycList = kycDetailsDAO.getKycListTypeWise(deductorPan, deductorTan, year, type);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Resource resource = null;
        if ("CUSTOMER".equalsIgnoreCase(type)) {
            resource = resourceLoader.getResource("classpath:templates/" + "customer_final_report.xlsx");
        } else if ("VENDOR".equalsIgnoreCase(type)) {
            resource = resourceLoader.getResource("classpath:templates/" + "vendor_final_report.xlsx");
        } else {
            resource = resourceLoader.getResource("classpath:templates/" + "shareholder_final_report.xlsx");
        }
        InputStream input = resource.getInputStream();
        try (XSSFWorkbook wb = new XSSFWorkbook(input)) {
            XSSFSheet sheet = wb.getSheetAt(0);
            sheet.protectSheet("password");
            String msg = getReportHeader(tenantId, deductorPan);

            org.apache.poi.ss.usermodel.Font fonts = wb.createFont();
            fonts.setBold(true);

            XSSFCellStyle style = wb.createCellStyle();
            style.setFont(fonts);
            style.setWrapText(true);
            style.setBorderLeft(BorderStyle.MEDIUM);
            style.setBorderTop(BorderStyle.MEDIUM);
            style.setBorderBottom(BorderStyle.MEDIUM);
            style.setBorderRight(BorderStyle.MEDIUM);
            style.setAlignment(HorizontalAlignment.LEFT);
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            style.setLocked(true);

            Font fonts2 = wb.createFont();
            fonts2.setBold(false);

            XSSFCellStyle style1 = wb.createCellStyle();
            style1.setFont(fonts);
            style1.setWrapText(true);
            style1.setFont(fonts2);
            style1.setLocked(true);

            XSSFCellStyle unlockedCellStyle = wb.createCellStyle();
            unlockedCellStyle.setFont(fonts);
            unlockedCellStyle.setWrapText(true);
            unlockedCellStyle.setFont(fonts2);
            unlockedCellStyle.setLocked(false);

            XSSFRow row0 = sheet.createRow(0);
            row0.createCell(0).setCellValue(msg);
            row0.getCell(0).setCellStyle(style);

            /**
             * Extra headers for merged columns
             */

            XSSFCellStyle style3 = wb.createCellStyle();
            style3.setFont(fonts);
            style3.setWrapText(true);
            style3.setLocked(true);
            style3.setAlignment(HorizontalAlignment.CENTER);
            style3.setVerticalAlignment(VerticalAlignment.CENTER);
            XSSFColor xssfColor = new XSSFColor(Color.LIGHT_GRAY);
            style3.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style3.setFillForegroundColor(xssfColor);

            XSSFRow row5 = sheet.getRow(5);
            row5.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));
            row5.createCell(11).setCellValue(getFinancialYear(2));
            row5.getCell(11).setCellStyle(style3);

            row5.createCell(13).setCellValue(getFinancialYear(1));
            row5.getCell(13).setCellStyle(style3);

            row5.createCell(15).setCellValue(getFinancialYear(0));
            row5.getCell(15).setCellStyle(style3);

            /**
             * END
             */

            int rowindex = 7;
            for (KYCDetails kyc : kycList) {
                XSSFRow row1 = sheet.createRow(rowindex++);
                row1.setHeightInPoints((2 * sheet.getDefaultRowHeightInPoints()));
                createSXSSFCell(style1, row1, 0, kyc.getCustomerName());
                createSXSSFCell(style1, row1, 1, kyc.getCustomerPan());
                createSXSSFCell(style1, row1, 2, kyc.getCustomerCode());
                createSXSSFCell(style1, row1, 3, "VENDOR".equalsIgnoreCase(type) ? "NA" : kyc.getTurnoverExceed10cr());
                createSXSSFCell(style1, row1, 4, "VENDOR".equalsIgnoreCase(type) ? "TDS" : kyc.getTdsTcsClientFinalResponse());
                createSXSSFCell(style1, row1, 5, StringUtils.isNotBlank(kyc.getKycPan()) ? kyc.getKycPan() : StringUtils.EMPTY);
                createSXSSFCell(style1, row1, 6, StringUtils.isNotBlank(kyc.getCustomerPan()) ? "YES" : "NO");
                createSXSSFCell(style1, row1, 7, getPANConsideredForTV(kyc));
                createSXSSFCell(style1, row1, 8, kyc.getIsPanVerifyStatus() ? "Valid" : "Invalid");
                createSXSSFCell(style1, row1, 9, kyc.getMatchScore());
                createSXSSFCell(style1, row1, 10,
                        kyc.getIsPanVerifyStatus()
                                ? "YES"
                                : "NO");
                createSXSSFCell(style1, row1, 11, kyc.getItrFinancialYear3());
                createSXSSFCell(style1, row1, 12, kyc.getAggregateTcsOrTdsGreaterThan50kForYear3());
                createSXSSFCell(style1, row1, 13, kyc.getItrFinancialYear2());
                createSXSSFCell(style1, row1, 14, kyc.getAggregateTcsOrTdsGreaterThan50kForYear2());
                createSXSSFCell(style1, row1, 15, kyc.getItrFinancialYear1());
                createSXSSFCell(style1, row1, 16, kyc.getAggregateTcsOrTdsGreaterThan50kForYear1());
                createSXSSFCell(style1, row1, 17, isITRConditionSatisfied(kyc) ? "YES" : "NO");
                createSXSSFCell(style1, row1, 18, kyc.getHigherTcsRateApplicableConclusion());
                createSXSSFCell(unlockedCellStyle, row1, 19,kyc.getFinalRateUserAction());
                CellRangeAddressList addressList1 = new CellRangeAddressList(rowindex - 1, rowindex - 1, 19, 19);
                attachValidation(sheet, addressList1, actions);
                createSXSSFCell(unlockedCellStyle, row1, 20, kyc.getFinalRateUserAction() == null ? StringUtils.EMPTY : 
                	kyc.getHigherTcsRateApplicableConclusion());
                CellRangeAddressList addressList = new CellRangeAddressList(rowindex - 1, rowindex - 1, 20, 20);
                attachValidation(sheet, addressList, "VENDOR".equalsIgnoreCase(type) ? clientFinalResponseTDS : clientFinalResponseTCS);
                createSXSSFCell(style1, row1, 21, kyc.getId().toString());
            }
            sheet.setColumnHidden(21, true);
            wb.write(out);
            kycDetailsService.saveBatchUploadReport(deductorTan, tenantId, year, out, kycList != null ? ((long) kycList.size()) : 0L,
                    uploadType, "Processed", month, userName, batchUpload.getBatchUploadID(),
                    null);
        } catch (Exception e) {
            logger.info("Exception occurred while preparing kyc final report {} ", e.getMessage());
        }
    }

    /**
     * @param style
     * @param row
     * @param cellNumber
     * @param value
     */
    private static void createSXSSFCell(XSSFCellStyle style, XSSFRow row, int cellNumber, String value) {
        Cell cell = row.createCell(cellNumber);
        cell.setCellValue(StringUtils.isBlank(value) == true ? StringUtils.EMPTY : value);
        cell.setCellStyle(style);
    }

    /**
     * @param sheet
     * @param cellRangeAddressList
     * @param list
     */
    private void attachValidation(XSSFSheet sheet, CellRangeAddressList cellRangeAddressList, String[] list) {
        XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheet);
        XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper
                .createExplicitListConstraint(list);
        XSSFDataValidation dataValidation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint,
                cellRangeAddressList);
        dataValidation.setShowErrorBox(true);
        dataValidation.setSuppressDropDownArrow(true);
        dataValidation.setShowPromptBox(true);
        sheet.addValidationData(dataValidation);
    }

    private String getFinancialYear(int index) {
        int month;
        int year;
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());

        // calendar will start from 0-11 [0=jan, 11=dec]
        month = cal.get(Calendar.MONTH);
        int advance = (month < 3) ? 0 : 1;
        year = cal.get(Calendar.YEAR) + advance;
        return ((year - 2 - index) + " - " + (year - 1 - index));
    }

    protected File getConvertedExcelFile(byte[] byteArray, String fileName) throws IOException {
        File someFile = new File(fileName);
        try (FileOutputStream fos = new FileOutputStream(someFile)) {
            fos.write(byteArray);
            fos.flush();
            fos.close();
            return someFile;
        }
    }
}