package com.ey.in.tds.returns.services;

import java.text.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ey.in.tds.dividend.common.JobHandler;
import com.ey.in.tds.returns.bot.ScraperService;
import com.ey.in.tds.returns.bot.domain.AckNumResponseMessage;
import com.ey.in.tds.returns.dto.Credentials;

@Service
public class ScrapperHelperService {

    private AckNumberFetcher ackNumberFetcher;
    private JobHandler jobHandler;
    private ScraperService scraperService;
    private Logger logger = LoggerFactory.getLogger(ScrapperHelperService.class);

    @Autowired
    public ScrapperHelperService(AckNumberFetcher ackNumberFetcher,
                                 JobHandler jobHandler,
                                 ScraperService scraperService) {
        this.ackNumberFetcher = ackNumberFetcher;
        this.jobHandler = jobHandler;
        this.scraperService = scraperService;
    }


    public AckNumResponseMessage generateAcknowledgementForm15CAPartC(Credentials credentials, String jobId, String tenantId, Integer assessmentYear) throws ParseException {
        this.jobHandler.markProcessing(jobId);
        logger.info("Job Id {} processing started", jobId);
        // Fetch and filter records for Form 15 CA Part C
        AckNumResponseMessage ackNumResponseMessage = null;
        try {
            ackNumResponseMessage = fetchRecordsWithAckNum(credentials, tenantId, assessmentYear, jobId);
        } catch (Exception e) {
            logger.error("Error during job id {}", jobId, e);
        } finally {
            this.jobHandler.markProcessed(jobId);
            logger.info("Job Id {} processed complete", jobId);
        }
        return ackNumResponseMessage;
    }

    private AckNumResponseMessage fetchRecordsWithAckNum(Credentials credentials, String tenantId, Integer assessmentYear, String jobId) throws ParseException {
        AckNumResponseMessage recordWithAckNums = null;
        return recordWithAckNums = ackNumberFetcher.fetchAckNumbers(credentials, tenantId, assessmentYear, jobId);
    }

    public String canGenerateForm15CAPartC() {
        return ackNumberFetcher.canFetchAckNum();
    }

    public boolean isScraperBusy() {
        return ackNumberFetcher.canFetchAckNum() != null
                && ackNumberFetcher.canFetchAckNum().equalsIgnoreCase("Worker is busy");
    }

   /* public void deleteFolderForJobId(String jobId) {
        fileUtil.deleteDirectory(jobId);
    }*/

    public String startSessionAndGenerateCaptcha() throws Exception {
        return scraperService.startSessionAndGenerateCaptcha();
    }

}
