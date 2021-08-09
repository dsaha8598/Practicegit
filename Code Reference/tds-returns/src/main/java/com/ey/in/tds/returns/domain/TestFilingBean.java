package com.ey.in.tds.returns.domain;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TestFilingBean {

	public static void main(String[] args) throws IOException {
		FilingFileBean tDS27QFileBean = new FilingFileBean();

		// getting data for challan
		List<FilingChallanDetailBean> filingChallanDetails = new ArrayList<FilingChallanDetailBean>();

		FilingChallanDetailBean filingChallanDetailBean = new FilingChallanDetailBean(1, "194C");
		filingChallanDetailBean.setLineNo("3");
		filingChallanDetailBean.setRecType("CD");
		filingChallanDetailBean.setChBatchNo("1");
		filingChallanDetailBean.setChallanDetailRecordNo("1");
		filingChallanDetailBean.setCountOfDeducteeDetail("1");
		filingChallanDetailBean.setNillChallanIndicator("N");
		filingChallanDetailBean.setChallanUpdationIndicator("");

		filingChallanDetailBean.setFiller3("");
		filingChallanDetailBean.setFiller4("");
		filingChallanDetailBean.setFiller5("");
		filingChallanDetailBean.setLastBankChallanNo("");
		filingChallanDetailBean.setBankChallanNo("04808");
		filingChallanDetailBean.setLastTransferVoucherNo("");
		filingChallanDetailBean.setDdoSerialNumberForm24G("");
		filingChallanDetailBean.setLastBankBranchCode("");
		filingChallanDetailBean.setBankBranchCode("0510308");
		filingChallanDetailBean.setLastDateOfBankChallanNo("");
		filingChallanDetailBean.setDateOfBankChallanNo("07082019");
		filingChallanDetailBean.setFiller6("");
		filingChallanDetailBean.setFiller7("");
		filingChallanDetailBean.setSection("");
		filingChallanDetailBean.setOltasIncomeTax("450.00");
		filingChallanDetailBean.setOltasSurcharge("0.00");
		filingChallanDetailBean.setOltasCess("0.00");
		filingChallanDetailBean.setOltasInterest("0.00");
		filingChallanDetailBean.setOltasOthers("0.00");
		filingChallanDetailBean.setTotalOfDepositAmountAsPerChallan("450.00");
		filingChallanDetailBean.setLastTotalOfDepositAmountAsPerChallan("");
		filingChallanDetailBean.setTotalTaxDepositedAsPerDeducteeAnex("450.00");
		filingChallanDetailBean.setTdsIncomeTaxC("450.00");
		filingChallanDetailBean.setTdsSurchargeC("0.00");
		filingChallanDetailBean.setTdsCessC("0.00");
		filingChallanDetailBean.setSumTotalIncTaxDedAtSource("450.00");
		filingChallanDetailBean.setTdsInterest("0.00");
		filingChallanDetailBean.setTdsOthers("0.00");
		filingChallanDetailBean.setChequeDDNo("");
		filingChallanDetailBean.setBookCash("N");
		filingChallanDetailBean.setRemark("");
		filingChallanDetailBean.setLateFee("0.00");
		filingChallanDetailBean.setMinorHeadCodeChallan("200");
		filingChallanDetailBean.setChallanHash("");
		filingChallanDetails.add(filingChallanDetailBean);

		// getting data for deductee set to bean
		List<FilingDeducteeDetailBean> deducteeDetails = new ArrayList<>();

		FilingDeducteeDetailBean filingDeducteeDetailBean = new FilingDeducteeDetailBean("26Q");

		filingDeducteeDetailBean.setLineNo("4");
		filingDeducteeDetailBean.setRecType("DD");
		filingDeducteeDetailBean.setDdBatchNo("1");
		filingDeducteeDetailBean.setChallanRecordNo("1");
		filingDeducteeDetailBean.setDeducteeDetailRecNo("1");
		filingDeducteeDetailBean.setMode("O");
		filingDeducteeDetailBean.setDeducteeSerialNo("");
		filingDeducteeDetailBean.setDeducteeCode("1");
		filingDeducteeDetailBean.setLastDeducteePan("");
		filingDeducteeDetailBean.setDeducteePan("AADFU5896K");
		filingDeducteeDetailBean.setLastDeducteeRefNo("");
		filingDeducteeDetailBean.setDeducteeRefNo("");
		filingDeducteeDetailBean.setDeducteeName("UNISPACE BUSINESS CENTER");
		filingDeducteeDetailBean.setTdsIncomeTaxDD("450.00");
		filingDeducteeDetailBean.setTdsSurchargeDD("0.00");
		filingDeducteeDetailBean.setTdsCessDD("0.00");
		filingDeducteeDetailBean.setTotalIncomeTaxDeductedAtSource("450.00");
		filingDeducteeDetailBean.setLastTotalIncomeTaxDeductedAtSource("");
		filingDeducteeDetailBean.setTotalTaxDeposited("450.00");
		filingDeducteeDetailBean.setLastTotalTaxDeposited("");
		filingDeducteeDetailBean.setTotalValueofPurchase("");
		filingDeducteeDetailBean.setAmountOfPayment("450.00");
		filingDeducteeDetailBean.setDateOnWhichAmountPaid("31072019");
		filingDeducteeDetailBean.setDateOnWhichTaxDeducted("31072019");
		filingDeducteeDetailBean.setDateOfDeposit("");
		filingDeducteeDetailBean.setRateAtWhichTaxDeducted("2.0000");
		filingDeducteeDetailBean.setGrossingUpIndicator("");
		filingDeducteeDetailBean.setBookCashEntry("");
		filingDeducteeDetailBean.setDateOfFurnishingTaxDeductionCertificate("");
		filingDeducteeDetailBean.setRemark1("");
		filingDeducteeDetailBean.setRemark2("");
		filingDeducteeDetailBean.setRemark3("");
		filingDeducteeDetailBean.setSectionCode("94C");
		filingDeducteeDetailBean.setCertNumAo("");
		filingDeducteeDetailBean.setFiller1("");
		filingDeducteeDetailBean.setFiller2("");
		filingDeducteeDetailBean.setFiller3("");
		filingDeducteeDetailBean.setFiller4("");
		filingDeducteeDetailBean.setFiller5("");
		filingDeducteeDetailBean.setFiller6("");
		filingDeducteeDetailBean.setFiller7("");
		filingDeducteeDetailBean.setFiller8("");
		filingDeducteeDetailBean.setDeducteeHash("");
		deducteeDetails.add(filingDeducteeDetailBean);

		// getting the deductor details
		FilingBatchHeaderBean filingHeaderBean = new FilingBatchHeaderBean();

		// setting the child beans
		tDS27QFileBean.setBatchHeaderBean(filingHeaderBean);
		tDS27QFileBean.setChallanDetailBeanList(filingChallanDetails);

		// generate the text
		String textUrl = generateTextFile(tDS27QFileBean, "Q2");
		System.out.println(textUrl);
	}

	public static String generateTextFile(FilingFileBean filingFileBean, String formType) throws IOException {
		// File file = new File(RandomStringUtils.random(8, true, true) + ".txt");

		File file = new File("kHrbcD4A" + ".txt");
		System.out.println(file.getAbsolutePath());
		String uri = null;
		// Create the file
		file.createNewFile();
		try (FileWriter writer = new FileWriter(file)) {

			// Write bean data to text file
			// Writing Header data
			writer.write(generateHeader(filingFileBean));
			writer.write("\n");
			// Writing Batch header data
			writer.write(filingFileBean.getBatchHeaderBean().toString());
			writer.write("\n");

			// Transfer Voucher Detail Record Writing Challan
			List<FilingChallanDetailBean> filingChallanDetails = filingFileBean.getChallanDetailBeanList();
			for (FilingChallanDetailBean filingChallanDetail : filingChallanDetails) {
				writer.write(filingChallanDetail.toString());
				writer.write("\n");
				// Writing Deductee details
				List<FilingDeducteeDetailBean> deducteeDetailList = filingChallanDetail.getDeducteeDetailBeanList();
				for (FilingDeducteeDetailBean deducteeDetail : deducteeDetailList) {
					writer.write(deducteeDetail.toString());
					writer.write("\n");
				}
			}
			writer.close();
			// uri = blobStorage.uploadExcelToBlobWithFile(file, tenantId);
		} catch (Exception e1) {
		}

		return uri;
	}

	public static String generateHeader(FilingFileBean tDS27QFileBean) {
		FilingHeaderBean filingHeaderBean = new FilingHeaderBean();

		filingHeaderBean.setLineNo(1);
		filingHeaderBean.setRecordType("FH");
		filingHeaderBean.setFileType("NS1");
		// should this by dynamic?
		filingHeaderBean.setUploaderType("R");
		SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy");
		String dateString = formatter.format(new Date());
		filingHeaderBean.setFileDate(dateString);

		// should be unique across all files? (all files meaning?)
		filingHeaderBean.setFileSeq(1);
		filingHeaderBean.setUploaderType("D");

		filingHeaderBean.setTanOfDeductor(tDS27QFileBean.getBatchHeaderBean().getTanOfDeductor());
		filingHeaderBean.setNoOfBatches(1);

		filingHeaderBean.setRpuName("E&Y TDS Application");

		filingHeaderBean.setRecordHash("");
		filingHeaderBean.setFvuVersion("");
		filingHeaderBean.setFileHash("");
		filingHeaderBean.setSamVersion("");
		filingHeaderBean.setSamHash("");
		filingHeaderBean.setScmVersion("");
		filingHeaderBean.setScmHash("");
		filingHeaderBean.setFileHash("");
		filingHeaderBean.setConsHash("");
		return filingHeaderBean.toString();
	}
}
