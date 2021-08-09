package com.ey.in.tds.returns.bot.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ey.in.tds.returns.bot.fs.ScreenShotTaker;
import com.ey.in.tds.returns.bot.pdf.FileDownloader;

public class Navigator {

    private Logger logger = LoggerFactory.getLogger(Navigator.class);

    private WebDriver driver;
    private String downloadDirAbsPath;
    private ScreenShotTaker screenShotTaker;

  public Navigator(WebDriver driver, String downloadDirAbsPath, ScreenShotTaker screenShotTaker) {
        this.driver = driver;
        this.downloadDirAbsPath = downloadDirAbsPath;
        this.screenShotTaker = screenShotTaker;
    }

    public void closePopupAfterLogin() {
        screenShotTaker.takeShot(driver, "1_before");

        String xPathContinueButton = "//*[@id=\"MyAccountHome\"]/table[2]/tbody/tr[2]/td/div/input";
        driver.findElement(By.xpath(xPathContinueButton)).click();

        screenShotTaker.takeShot(driver, "1_after");
    }

    public void clickOnWorkListAndYourInformation() {
        screenShotTaker.takeShot(driver, "2_before");

        String worklistXpath = "//*[@id=\"header\"]/div[2]/ul/li[7]/p/a/span";
        String forYourInfoXPath = "//*[@id=\"header\"]/div[2]/ul/li[7]/div/dl[2]/dt/a";
        driver.findElement(By.xpath(worklistXpath)).click();
        screenShotTaker.takeShot(driver, "2_between");
        driver.findElement(By.xpath(forYourInfoXPath)).click();
        screenShotTaker.takeShot(driver, "2_after");
    }


    public void checkTheDropDownWindow() {
        String checkXpath =  "//*[@id=\"ForcedLogin\"]/table[2]/tbody/tr/td[1]/input";
        if (driver.findElements(By.xpath(checkXpath)).size() > 0) {
            driver.findElement(By.xpath(checkXpath)).click();
        }
    }

    public void clickOnViewUploadFormDetails() {
        screenShotTaker.takeShot(driver, "3_before");

        String clickHereXPath = "//*[@id=\"dynamicContent\"]/div[2]/div[2]/h3/a";
        driver.findElement(By.xpath(clickHereXPath)).click();
        screenShotTaker.takeShot(driver, "3_after");
    }

    public void selectAssesmentYearAndForm15CBAndSearch(String assesmentYear) {
        selectAssesmentYearFromDropdown(assesmentYear);
        selectForm15CBFromDropdown();
        clickOnSearchAfterSelectingForm15CB();
    }
    

    /**
     * If assesment year is 2020-2021, then input should be 2020
     *
     * @param assesmentYear
     */
   private void selectAssesmentYearFromDropdown(String assesmentYear) {

        String assesmentYearDropDownXPath = "//*[@id=\"asstYear\"]";
        new Select(driver.findElement(By.xpath(assesmentYearDropDownXPath))).selectByValue(assesmentYear);
    }

    private void selectForm15CBFromDropdown() {

        String dropDownXPath = "//*[@id=\"SearchApprovedOrRejectedForms_searchCriteria_formName\"]";
        new Select(driver.findElement(By.xpath(dropDownXPath))).selectByValue("FORM15CB");
    }

    private void clickOnSearchAfterSelectingForm15CB() {
        String searchButtonXPath = "//*[@id=\"SearchApprovedOrRejectedForms_0\"]";
        driver.findElement(By.xpath(searchButtonXPath)).click();
        screenShotTaker.takeShot(driver, "after_searching");
    }

    public int calculateTotalPageNumbers() {

        String rowXPath = "//*[@id=\"dynamicContent\"]/div[2]/div/table[2]/tbody/tr/td";
        String text = driver.findElement(By.xpath(rowXPath)).getText();
        text = text.trim();
        return Integer.parseInt(text.substring(text.lastIndexOf(" ")).trim());
    }

    public List<String> fetchAllFormDetailsForPage(int pageNumber, String assesmentYear) {
        return peekFormDetailsOnPageAndReturnPDFPaths(pageNumber, assesmentYear);
    }

    private void goToPageNumber(int pageNumber) {

        String jsFunCallWithParam = "paginationSumbmit(" + pageNumber + ")";
        //Creating the JavascriptExecutor interface object by Type casting
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(jsFunCallWithParam);
        screenShotTaker.takeShot(driver, "4_page_number");
        logger.info("Current URL is {} and page number {}", driver.getCurrentUrl(), pageNumber);
    }

    private List<WebElement> getRowsInTableForCurrentPage() {

        String rowsXPath = "//*[@id=\"dynamicContent\"]/div[2]/div/table[1]/tbody/tr";
        List<WebElement> rows = driver.findElements(By.xpath(rowsXPath));
        List<WebElement> actualRows = new ArrayList<WebElement>();
        for (WebElement row : rows) {
            try {
                String formDetailsLinkText = ((RemoteWebElement) row)
                        .findElementByTagName("a").getText();
                if (formDetailsLinkText.trim().equalsIgnoreCase("View Form")) {
                    actualRows.add(row);
                }
            } catch (NoSuchElementException ignored) {
            }
        }
        return actualRows;
    }

    /**
     * Steps:
     * 1. Click on View Form
     * 2. Scrape Content From Details Page and Extract PDF
     * 3. Click on back button
     * 4. Click on Worklist -> For Your Information
     * 5. Click here link
     * 6. Select form 15CB from dropdown
     * 7. Navigate to selected page
     * 8. Click on Search button
     * 9. Repeat process for another entry in page
     *
     * @throws IOException
     */
 private List<String> peekFormDetailsOnPageAndReturnPDFPaths(int pageNum, String assesmentYear) {
        List<String> pathToPDFsOnPage = new ArrayList<>();
        int currentEntryCounterForPage = 0;
        for (; ; currentEntryCounterForPage++) {
            goToPageNumber(pageNum);
            List<WebElement> actualRows = getRowsInTableForCurrentPage();
            if (currentEntryCounterForPage >= actualRows.size()) {
                break;
            }
            List<WebElement> formDetailsViewHereElement = actualRows.stream()
                    .map(actualRow -> ((RemoteWebElement) actualRow).findElementByTagName("a"))
                    .collect(Collectors.toList());
            formDetailsViewHereElement.get(currentEntryCounterForPage).click();
            String pdfPath = downloadPDFAndGetAbsPath();
            pathToPDFsOnPage.add(pdfPath);
            clickOnWorkListAndYourInformation();
            clickOnViewUploadFormDetails();
            selectAssesmentYearAndForm15CBAndSearch(assesmentYear);
        }
        return pathToPDFsOnPage.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private String downloadPDFAndGetAbsPath() {
        screenShotTaker.takeShot(driver, "before_fetching_form_details");
        String ackNumberXPath = "//*[@id=\"UpdateFormApproval\"]/table/tbody/tr[8]/td/div[2]/input";
        String downloadPDFXPath = "//*[@id=\"UpdateFormApproval\"]/table/tbody/tr[10]/td/div/a";
        String backButtonXPath = "//*[@id=\"UpdateFormApproval\"]/table/tbody/tr[11]/td/input";


        String ackNumber = driver.findElement(By.xpath(ackNumberXPath)).getAttribute("value");
        logger.info("Fetching data for Ack Num {}", ackNumber);
        // Download PDF and extract file system path
        driver.findElement(By.xpath(downloadPDFXPath)).click();
        String downloadedFilePath = null;
        try {
            downloadedFilePath = new FileDownloader().downloadAndReturnAbsPath(downloadDirAbsPath);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        driver.findElement(By.xpath(backButtonXPath)).click();
        return downloadedFilePath;
    }


}
