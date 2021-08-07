package com.ey.in.tds.returns.services;

import java.text.ParseException;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.ey.in.tcs.common.domain.dividend.InvoiceShareholderNonResident;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.dividend.common.JobHandler;
import com.ey.in.tds.feign.client.IngestionClient;
import com.ey.in.tds.returns.bot.ScraperService;
import com.ey.in.tds.returns.bot.domain.AckNumRequestMessage;
import com.ey.in.tds.returns.bot.domain.AckNumResponseMessage;
import com.ey.in.tds.returns.bot.domain.Form15CBDetails;
import com.ey.in.tds.returns.dto.Credentials;

@Component
public class AckNumberFetcher {

    private Logger logger = LoggerFactory.getLogger(AckNumberFetcher.class);

    private ScraperService scraperService;

    private IngestionClient ingestionClient;

    private Form15CBDetails form15CBDetails;

    private boolean shouldfetchAckNum;

    private JobHandler jobHandler;

    @Autowired
    public AckNumberFetcher(ScraperService scraperService,
                            IngestionClient ingestionClient,
                            Form15CBDetails form15CBDetails,
                            @Value("${app.ack.num.enabled}") boolean shouldfetchAckNum,
                            JobHandler jobHandler) {
        this.scraperService = scraperService;
        this.ingestionClient = ingestionClient;
        this.form15CBDetails = form15CBDetails;
        this.shouldfetchAckNum = shouldfetchAckNum;
        this.jobHandler = jobHandler;
    }

    public String canFetchAckNum() {
        // This method returns the reason why we cannot fetch ack num, else it returns an empty string
        return scraperService.canFetchAckNum();
    }

    public AckNumResponseMessage fetchAckNumbers(Credentials credentials, String tenantId, Integer assessmentYear, String jobId) throws ParseException {
        List<Form15CBDetails> form15CBDetailsListObject = null;
        AckNumResponseMessage ackNumResponseMessage = null;
        try {
            logger.info("Got request to fetch acknowledgement numbers");
            validateIfScraperIsReady();
            ResponseEntity<ApiStatus<List<InvoiceShareholderNonResident>>> responseEntity = ingestionClient.getNonResidentShareholderForCBGenerated(tenantId, assessmentYear);
            List<InvoiceShareholderNonResident> invoiceShareholderNonResidentList = Objects.requireNonNull(responseEntity.getBody()).getData();
            List<Form15CBDetails> form15CBDetailsDtosForAssesmentYear = form15CBDetails.generateFromRecord(invoiceShareholderNonResidentList, tenantId);
            if (shouldfetchAckNum) {
                if (form15CBDetailsDtosForAssesmentYear.size() > 0) {
                    AckNumRequestMessage acknowledgementNumRequest = new AckNumRequestMessage(
                            credentials.getUserName(), credentials.getPassword(), credentials.getCaptcha(),
                            form15CBDetailsDtosForAssesmentYear, assessmentYear.toString());
                    ackNumResponseMessage = scraperService.loginAndFetchAckNumbers(acknowledgementNumRequest);
                }
            } else {
                /*form15CBDetailsListObject = new ArrayList<>();
                UUID shareHolderId = UUID.fromString("88e38230-e776-11ea-aad7-373651f6178c");
                Form15CBDetails form15CBDetailsObject = new Form15CBDetails("", "", "",
                        "", "", "", "", "", "", "123456789", "",
                        "", "2020-03-17", "ABACP4220A", UUID.fromString("363875ff-72a2-4fa3-9aa6-734b897b5fe1"), null);
                form15CBDetailsListObject.add(form15CBDetailsObject);
                ackNumResponseMessage = new AckNumResponseMessage(form15CBDetailsListObject, null);*/
            }
            if (ackNumResponseMessage != null && ackNumResponseMessage.getFormsWithAckNumFound().size() > 0) {
                logger.info("Updating acknowledgement number in table");
                ingestionClient.updateNonResidentShareholder(tenantId, ackNumResponseMessage.getFormsWithAckNumFound());
            }
        } catch (Exception e) {
            logger.error("Error in fetching/updating Acknowlegment Number", e);
            ackNumResponseMessage = null;
        } finally {
            this.scraperService.toggleStatus();
            this.jobHandler.markProcessed(jobId);
        }
        return ackNumResponseMessage;
    }

    private void validateIfScraperIsReady() {
        // Todo: Make sure that this response is propagated to the user
        if (scraperService.canFetchAckNum() != null) {
            throw new RuntimeException(scraperService.canFetchAckNum());
        }
    }
}
