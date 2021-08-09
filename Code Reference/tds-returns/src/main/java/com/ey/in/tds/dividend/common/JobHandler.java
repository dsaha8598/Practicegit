package com.ey.in.tds.dividend.common;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class JobHandler {

    private FileUtil fileUtil;

    @Autowired
    JobHandler(FileUtil fileUtil) {
        this.fileUtil = fileUtil;
    }

    private ReentrantLock lock = new ReentrantLock();

    public void markProcessing(String jobId) {
        this.lock.lock();
        try {
            this.fileUtil.createFile(jobId + ".p");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            this.lock.unlock();
        }
    }

    public boolean isProcessing(String jobId) {
        return this.fileUtil.isFileExists(jobId + ".p");
    }

    public boolean markProcessed(String jobId) {
        this.lock.lock();
        boolean isProcessed = this.fileUtil.deleteFile(jobId + ".p");
        this.lock.unlock();
        return isProcessed;
    }

    public boolean isAnyProcessing() throws IOException {
        return this.fileUtil.ifAnyProcessing();
    }
}
