package com.ey.in.tds.returns.bot.pdf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class FileDownloader {

    private Logger logger = LoggerFactory.getLogger(FileDownloader.class);

    public String downloadAndReturnAbsPath(String downloadDirAbsPath) throws IOException, InterruptedException {
        Thread.sleep(1500);
        return getLatestFilefromDir(downloadDirAbsPath).getAbsolutePath();
    }

    private File getLatestFilefromDir(String dirPath) {
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            logger.error("No latest file found in directory {}", dirPath);
            return null;
        }

        File lastModifiedFile = files[0];
        for (int i = 1; i < files.length; i++) {
            if (lastModifiedFile.lastModified() < files[i].lastModified()) {
                lastModifiedFile = files[i];
            }
        }
        return lastModifiedFile;
    }
}
