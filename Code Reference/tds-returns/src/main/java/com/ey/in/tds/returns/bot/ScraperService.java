package com.ey.in.tds.returns.bot;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ey.in.tds.returns.bot.crawler.NavigationManager;
import com.ey.in.tds.returns.bot.domain.AckNumRequestMessage;
import com.ey.in.tds.returns.bot.domain.AckNumResponseMessage;
import com.ey.in.tds.returns.bot.fs.ScreenShotTaker;
import com.ey.in.tds.returns.bot.fs.TempFolderCreator;

@Service
public class ScraperService {

    private Logger logger = LoggerFactory.getLogger(ScraperService.class);

    private WebDriver driver;
    private String downloadDirAbsPath;
    private NavigationManager ackNumFetcher;
    private ScreenShotTaker screenShotTaker;
    private boolean isWorkerBusy;
    private String chromeDriverPath;

    @Autowired
    public ScraperService(NavigationManager ackNumFetcher, ScreenShotTaker screenShotTaker,
                          @Value("${app.chrome.driver.path}") String chromeDriverPath) throws IOException {
        this.downloadDirAbsPath = new TempFolderCreator().createNewTempFolderAndReturnPath();
        this.ackNumFetcher = ackNumFetcher;
        this.screenShotTaker = screenShotTaker;
        this.isWorkerBusy = false;
        this.chromeDriverPath = chromeDriverPath;
    }

    /**
     * Returns null if we can proceed with ack num fetch job
     *
     * @return
     */
    public String canFetchAckNum() {
        if (isWorkerBusy) {
            return "Worker is busy";
        }
        if (driver == null) {
            return "Driver not initialized";
        }
        return null;
    }

    private void initDriver() {
        ChromeOptions options = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("downloadAndReturnAbsPath.prompt_for_download", false);
        prefs.put("profile.default_content_settings.popups", 0);
        prefs.put("downloadAndReturnAbsPath.default_directory", downloadDirAbsPath);
        prefs.put("download.default_directory", downloadDirAbsPath);
        options.setExperimentalOption("prefs", prefs);
        System.setProperty("webdriver.chrome.driver", chromeDriverPath);
        // options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200", "--ignore-certificate-errors", "--silent");
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--headless", "--disable-gpu", "--window-size=1920,1200", "--ignore-certificate-errors", "--silent");

        driver = new ChromeDriver(options);
        logger.info("Initializing Chrome Session with download path at: {}", downloadDirAbsPath);
        isWorkerBusy = false;
    }

    public String startSessionAndGenerateCaptcha() throws Exception {
        tryQuittingIfBrowserIsOpen();
        initDriver();
        driver.get("https://portal.incometaxindiaefiling.gov.in/e-Filing/UserLogin/LoginHome.html");
        closePopup();
        clickOnLogin();
        screenShotTaker.takeShot(driver, "login_page");
        return extractCaptchaImageJs();
    }

    public void toggleStatus() {
        isWorkerBusy = false;
    }

    public byte[] startSessionAndGenerateCaptchaByteArray() throws Exception {
        tryQuittingIfBrowserIsOpen();
        initDriver();
        driver.get("https://portal.incometaxindiaefiling.gov.in/e-Filing/UserLogin/LoginHome.html");
        closePopup();
        clickOnLogin();
        screenShotTaker.takeShot(driver, "login_page");
        return extractCaptchaImageJsByteArray();
    }

    private void tryQuittingIfBrowserIsOpen() {
        try {
            driver.quit();
        } catch (Exception ignored) {
        }
    }

    private void closePopup() {
        try {
            Thread.sleep(1000);
            String crossButtonPath = "/html/body/div[1]/div[1]/button";
            driver.findElement(By.xpath(crossButtonPath)).click();
        } catch (Exception ex) {

        }
    }

    private void clickOnLogin() throws InterruptedException {
        Thread.sleep(1000);
        String loginButtonXPath = "//*[@id=\"staticContentsUrl\"]/section[1]/div/app-register-options/ul/app-register[2]/li/h1/input";
        driver.findElement(By.xpath(loginButtonXPath)).click();
    }

    private byte[] extractCaptchaImage() throws IOException, InterruptedException {
        Thread.sleep(1000);
        int xPosOffset = 780;
        int yPosOffset = 240;
        int xSizeOffset = 170;
        int ySizeOffset = 50;
        String captchaImageId = "captchaImg";
        WebElement ele = driver.findElement(By.id(captchaImageId));
        logger.info("Captcha Image URL in browser is {}", ele.getAttribute("src"));
        // Get entire page screenshot
        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        BufferedImage fullImg = ImageIO.read(screenshot);
        // Get the location of element on the page
        Point point = ele.getLocation();
        point = point.moveBy(xPosOffset, yPosOffset);
        // Get width and height of the element
        int eleWidth = ele.getSize().getWidth() + xSizeOffset;
        int eleHeight = ele.getSize().getHeight() + ySizeOffset;
        // Crop the entire page screenshot to get only element screenshot
        BufferedImage eleScreenshot = fullImg.getSubimage(point.getX(), point.getY(),
                eleWidth, eleHeight);
        ImageIO.write(eleScreenshot, "png", screenshot);
        return FileUtils.readFileToByteArray(screenshot);
    }

    private String extractCaptchaImageJs() throws IOException, InterruptedException {
        Thread.sleep(1000);
        String jsCode = "  var ele = document.getElementById(\"captchaImg\")\n" +
                "    var cnv = document.createElement('canvas');\n" +
                "    cnv.width = ele.width; cnv.height = ele.height;\n" +
                "    cnv.getContext('2d').drawImage(ele, 0, 0);\n" +
                "    return cnv.toDataURL().substring(22);";
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String image = (String) js.executeScript(jsCode);
        return image;
    }

    private byte[] extractCaptchaImageJsByteArray() throws IOException, InterruptedException {
        Thread.sleep(1000);
        String jsCode = "  var ele = document.getElementById(\"captchaImg\")\n" +
                "    var cnv = document.createElement('canvas');\n" +
                "    cnv.width = ele.width; cnv.height = ele.height;\n" +
                "    cnv.getContext('2d').drawImage(ele, 0, 0);\n" +
                "    return cnv.toDataURL().substring(22);";
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String image = (String) js.executeScript(jsCode);
        return Base64.getDecoder().decode(image);
    }

    public AckNumResponseMessage loginAndFetchAckNumbers(AckNumRequestMessage loginRequest) {
        if (isWorkerBusy) {
            throw new RuntimeException("Worker is busy");
        }
        isWorkerBusy = true;
        loginWithCredsAndCaptcha(loginRequest.getUserName(), loginRequest.getPassword(), loginRequest.getCaptcha());
        screenShotTaker.takeShot(driver, "after_login");
        AckNumResponseMessage ackNumSearchResponseDto = null;
        try {
            ackNumSearchResponseDto = ackNumFetcher.extractAndMatchAckNum(driver, downloadDirAbsPath,
                    loginRequest.getForm15CBDetails(), loginRequest.getAssessmentYear());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("Error in Scrapper service while crawling");
        } finally {
            logOut();
            isWorkerBusy = false;
        }
        return ackNumSearchResponseDto;
    }

    private void loginWithCredsAndCaptcha(String userName, String password, String captcha) {
        screenShotTaker.takeShot(driver, "before_login");
        logger.info("Logging in");
        String xPathUserName = "//*[@id=\"Login_userName\"]";
        String xPathPassword = "//*[@id=\"Login_password\"]";
        String xPathCaptchaInput = "//*[@id=\"Login_captchaCode\"]";
        String loginButtonXPath = "//*[@id=\"button1\"]";

        // Search for username / password and captcha input and fill the inputs
        driver.findElement(By.xpath(xPathUserName)).sendKeys(userName);
        driver.findElement(By.xpath(xPathPassword)).sendKeys(password);
        driver.findElement(By.xpath(xPathCaptchaInput)).sendKeys(captcha);

        // Click on Login Button
        driver.findElement(By.xpath(loginButtonXPath)).click();
    }

    private void logOut() {
        screenShotTaker.takeShot(driver, "before_logout");
        isWorkerBusy = false;
        try {
            String xPathLogoutButton = "//*[@id=\"header\"]/div[1]/div[2]/div/input";
            driver.findElement(By.xpath(xPathLogoutButton)).click();
        } catch (Exception ignored) {
        } finally {
            driver.quit();
            logger.info("Closed Chrome Session");
        }
    }
}
