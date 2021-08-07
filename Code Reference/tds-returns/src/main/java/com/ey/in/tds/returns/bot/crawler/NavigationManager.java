package com.ey.in.tds.returns.bot.crawler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ey.in.tds.returns.bot.domain.AckNumResponseMessage;
import com.ey.in.tds.returns.bot.domain.Form15CBDetails;
import com.ey.in.tds.returns.bot.fs.ScreenShotTaker;
import com.ey.in.tds.returns.bot.pdf.Form15CBMatcher;
import com.ey.in.tds.returns.bot.pdf.PdfExtractor;

@Component
public class NavigationManager {

    private Logger logger = LoggerFactory.getLogger(NavigationManager.class);
    private Form15CBMatcher form15CBMatcher;
    private PdfExtractor pdfRemitteeNameAddressExtractor;

    @Autowired
    public NavigationManager(Form15CBMatcher form15CBMatcher, PdfExtractor pdfRemitteeNameAddressExtractor) {
        this.form15CBMatcher = form15CBMatcher;
        this.pdfRemitteeNameAddressExtractor = pdfRemitteeNameAddressExtractor;
    }

  public AckNumResponseMessage extractAndMatchAckNum(WebDriver driver, String downloadDirAbsPath, List<Form15CBDetails> form15CBDetailsList,
                                                       String assesmentYear) {
        List<String> pdfPaths = downloadAndGetAllPDFAbsPath(driver, downloadDirAbsPath, assesmentYear);
        List<Form15CBDetails> form15CBDetailsFromPDfs = pdfPaths.stream()
                .map(pdfRemitteeNameAddressExtractor::extractFromFileAtPath)
                .collect(Collectors.toList());

        List<Form15CBDetails> formsWithAckNumFound = new ArrayList<>();
        List<Form15CBDetails> formsWithAckNumNotFound = new ArrayList<>();
        for (Form15CBDetails form15CBDetailsFromInput : form15CBDetailsList) {
            boolean found = false;
            form15CBDetailsFromInput.changeNullFieldsToEmpty();
            for (Form15CBDetails form15CBDetailsFromPDF : form15CBDetailsFromPDfs) {
                if (form15CBMatcher.isEquals(form15CBDetailsFromInput, form15CBDetailsFromPDF)) {
                    Form15CBDetails form15CBDetails = new Form15CBDetails(form15CBDetailsFromPDF.getAckNum(),
                            form15CBDetailsFromPDF.getAckDate(),
                            form15CBDetailsFromInput.getDeductorPan(),
                            form15CBDetailsFromInput.getInvoiceId(),
                            form15CBDetailsFromInput.getInvoicePostingDate());
                    formsWithAckNumFound.add(form15CBDetails);
                    found = true;
                }
            }
            if (!found) {
                formsWithAckNumNotFound.add(form15CBDetailsFromInput);
            }
        }
        return new AckNumResponseMessage(formsWithAckNumFound, formsWithAckNumNotFound);
    }

    private List<String> downloadAndGetAllPDFAbsPath(WebDriver driver, String downloadDirAbsPath,
                                                     String assesmentYear) {
        List<String> pathToAllDownloadedPDFs = new ArrayList<>();
        Navigator navigator = new Navigator(driver, downloadDirAbsPath, new ScreenShotTaker());
        navigator.checkTheDropDownWindow();
        navigator.closePopupAfterLogin();
        navigator.clickOnWorkListAndYourInformation();
        navigator.clickOnViewUploadFormDetails();
        navigator.selectAssesmentYearAndForm15CBAndSearch(assesmentYear);
        // search
        int totalPages = navigator.calculateTotalPageNumbers();
        logger.info("Total Pages for Form15CB and Assessment Year {} is {}", assesmentYear, totalPages);
        for (int i = 1; i <= totalPages; i++) {
            List<String> pathToPDFsOnPage = navigator.fetchAllFormDetailsForPage(i, assesmentYear);
            pathToAllDownloadedPDFs.addAll(pathToPDFsOnPage);
        }
        return pathToAllDownloadedPDFs;
    }
}
