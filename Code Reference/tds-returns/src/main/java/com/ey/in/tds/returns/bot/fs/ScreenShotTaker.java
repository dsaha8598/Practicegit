package com.ey.in.tds.returns.bot.fs;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ey.in.tds.returns.bot.ScraperService;

@Component
public class ScreenShotTaker {

    private Logger logger = LoggerFactory.getLogger(ScraperService.class);

    //@Value("${app.screenshot.enabled}")
    private boolean isScreenShotEnabled;

   public void takeShot(WebDriver driver, String fileName) {
        if(!isScreenShotEnabled) {
            trySleep(1000);
            return;
        }
        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(screenshot, new File(fileName + ".png"));
        } catch (IOException e) {
            logger.error("Failed to take screenshot of {}",fileName);
        }
    }

    private void trySleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }
}
