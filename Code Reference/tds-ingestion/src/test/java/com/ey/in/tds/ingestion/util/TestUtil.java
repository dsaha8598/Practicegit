package com.ey.in.tds.ingestion.util;

import java.util.Date;
import java.util.Random;

import org.springframework.http.HttpHeaders;

import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.InvoiceLineItem;

public class TestUtil {

	public static final String TAN_NUMBER = "XSWED1234C";

	public static HttpHeaders getMandatoryHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("TAN-NUMBER", TAN_NUMBER);
		return headers;
	}

	public static String getE2EURL(String uri) {
		return "http://localhost:8004/api/ingestion" + (uri.startsWith("/") ? uri : "/" + uri);
	}

	public static BatchUpload getRandomBatchUpload() {
		/*
		 * BatchUpload batchUploadKey = new BatchUpload(new Random().nextInt(2000),
		 * TAN_NUMBER, "INVOICE" + new Random().nextInt(2000), UUID.randomUUID());
		 */

		BatchUpload batchUpload = new BatchUpload();
		//batchUpload.setKey(batchUploadKey);
		return batchUpload;
	}

/*	public static Provision getRandomProvision() {
		Provision.Key provisionKey = new Provision.Key(2019, TAN_NUMBER, new Date(), UUID.randomUUID());

		Provision provision = new Provision();
		provision.setKey(provisionKey);
		return provision;
	} */

	//TODO NEED TO CHANGE FOR SQL
/*	public static FieldSpecification getRandomFieldSpecification() {

		FieldSpecification.Key key = new FieldSpecification.Key("Field Spec", UUID.randomUUID());
		FieldSpecification fieldSpecification = new FieldSpecification();
		fieldSpecification.setKey(key);

		return fieldSpecification;
	}

	public static EmailNotification getRandomEmailNotification() {

		EmailNotification.Key key = new EmailNotification.Key(new Random().nextInt(2000), new Random().nextInt(12),
				UUID.randomUUID());
		EmailNotification emailNotification = new EmailNotification();
		emailNotification.setKey(key);

		return emailNotification;
	}   */

/*	public static TdsInterestCalculation getRandomTdsInterestCalculation() {

		TdsInterestCalculation.Key key = new TdsInterestCalculation.Key(new Random().nextInt(2000),
				new Random().nextInt(12), UUID.randomUUID());
		TdsInterestCalculation tdsInterestCalculation = new TdsInterestCalculation();
		tdsInterestCalculation.setKey(key);

		return tdsInterestCalculation;
	}  */
	//TODO NEED TO CHANGE FOR SQL
	/*
	 * public static UploadRecordProcess getRandomUploadRecordProcess() {
	 * 
	 * UploadRecordProcess.Key key = new UploadRecordProcess.Key(new
	 * Random().nextInt(2000), UUID.randomUUID(), "Status Success",
	 * UUID.randomUUID()); UploadRecordProcess uploadRecordProcess = new
	 * UploadRecordProcess(); uploadRecordProcess.setKey(key);
	 * 
	 * return uploadRecordProcess; }
	 */

	//TODO NEED TO CHANGE FOR SQL
/*	public static LdcUtilization getRandomLdcUtilization() {

		LdcUtilization.Key key = new LdcUtilization.Key(new Random().nextInt(2000), new Random().nextInt(12), "TAN",
				UUID.randomUUID());
		LdcUtilization ldcUtilization = new LdcUtilization();
		ldcUtilization.setKey(key);

		return ldcUtilization;
	}  */

/*	public static InvoiceMismatch getRandomInvoiceMismatch() {

		InvoiceMismatch.Key key = new InvoiceMismatch.Key(new Random().nextInt(2000), new Random().nextInt(12),
				UUID.randomUUID(), UUID.randomUUID());
		InvoiceMismatch invoiceMismatch = new InvoiceMismatch();
		invoiceMismatch.setKey(key);

		return invoiceMismatch;
	}   */

/*	public static TdsExcess getRandomTdsExcess() {

		TdsExcess.Key key = new TdsExcess.Key(new Random().nextInt(2000), new Random().nextInt(12), "TAN", "Section",
				UUID.randomUUID());
		TdsExcess tdsExcess = new TdsExcess();
		tdsExcess.setKey(key);

		return tdsExcess;
	}   */

/*	public static TdsMismatch getRandomTdsMismatch() {

		TdsMismatch.Key key = new TdsMismatch.Key(new Random().nextInt(2000), new Random().nextInt(12),
				UUID.randomUUID(), UUID.randomUUID());
		TdsMismatch tdsMismatch = new TdsMismatch();
		tdsMismatch.setKey(key);

		return tdsMismatch;
	}*/

	public static InvoiceLineItem getRandomInvoiceLineItem() {
		String tan = "XSWED1234C";
		InvoiceLineItem invoiceLineItem = new InvoiceLineItem();
		invoiceLineItem.setAssessmentYear(new Random().nextInt(2000));
		invoiceLineItem.setDeductorMasterTan(tan);
		invoiceLineItem.setDocumentPostingDate(new Date());

		return invoiceLineItem;
	}

/*	public static GeneralLedger getRandomGeneralLedger() {
		GeneralLedger.Key generalLedgerKey = new GeneralLedger.Key(9, TAN_NUMBER, UUID.randomUUID());

		GeneralLedger generalLedger = new GeneralLedger();
		generalLedger.setKey(generalLedgerKey);
		return generalLedger;
	}  */
}