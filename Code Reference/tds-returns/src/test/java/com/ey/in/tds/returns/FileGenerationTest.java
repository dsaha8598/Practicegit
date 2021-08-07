/**
 * 
 */
package com.ey.in.tds.returns;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.returns.domain.FilingBatchHeaderBean;
import com.ey.in.tds.returns.domain.FilingChallanDetailBean;
import com.ey.in.tds.returns.domain.FilingDeducteeDetailBean;
import com.ey.in.tds.returns.domain.FilingFileBean;
import com.ey.in.tds.returns.domain.FilingHeaderBean;
import com.ey.in.tds.returns.services.RawFileGenerationService;
import com.microsoft.azure.storage.StorageException;

/**
 * @author venkatd
 *
 */
public class FileGenerationTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RawFileGenerationService service = new RawFileGenerationService();

		FilingFileBean fileBean = new FilingFileBean();
		FilingBatchHeaderBean filingBatchHeader = new FilingBatchHeaderBean();
		filingBatchHeader.setLineNo(StringUtils.EMPTY + 2); // 1
		filingBatchHeader.setRecordType("BH"); // 2
		filingBatchHeader.setBatchNo("1"); // 3
		filingBatchHeader.setChallanCount(StringUtils.EMPTY);// 4 Calculated after processing
		filingBatchHeader.setFormNo("26Q"); // 5
		filingBatchHeader.setTransactionType(StringUtils.EMPTY); // 6
		filingBatchHeader.setBatchUpdationIndicator(StringUtils.EMPTY); // 7
		filingBatchHeader.setOriginalRrrNo(StringUtils.EMPTY); // 8
		filingBatchHeader.setPreviousRrrNo(StringUtils.EMPTY); // 9
		filingBatchHeader.setRrrNo(StringUtils.EMPTY); // 10
		filingBatchHeader.setRrrDate(StringUtils.EMPTY); // 11
		filingBatchHeader.setLastTanOfDeductor(StringUtils.EMPTY); // 12
		filingBatchHeader.setTanOfDeductor("HYDS35527C"); // 13

		// NA 14
		filingBatchHeader.setPanOfDeductor("AATCS0112R"); // 15
		filingBatchHeader.setAssessmentYr("202122"); // 17
		filingBatchHeader.setFinancialYr("202021"); // 13
		filingBatchHeader.setPeriod("Q3");// 18
		filingBatchHeader.setEmployerName("ScriptBees IT Private Limited");// 19
		filingBatchHeader.setEmployerBranchDiv("NA"); // 20
		filingBatchHeader.setEmployerAddr1("203 Sreenivasa Colony West"); // 21

		filingBatchHeader.setEmployerAddr2("Ameerpet"); // 22
		filingBatchHeader.setEmployerAddr3("Hyderabad"); // 23
		filingBatchHeader.setEmployerAddr4("500038"); // 24
		filingBatchHeader.setEmployerAddr5("India"); // 25

		filingBatchHeader.setEmployerState(StringUtils.EMPTY); // 26

		filingBatchHeader.setEmployerPin("500038"); // 27
		filingBatchHeader.setEmployerEmail("venkat@scriptbees.com"); // 28

		filingBatchHeader.setEmployerStd(StringUtils.EMPTY); // 29
		filingBatchHeader.setEmployerPhone(StringUtils.EMPTY); // 30

		filingBatchHeader.setEmployerSTDAlt(StringUtils.EMPTY); // 29

		filingBatchHeader.setEmployerPhoneAlt(StringUtils.EMPTY); // 30

		filingBatchHeader.setEmployerAddrChange("N"); // 31

		filingBatchHeader.setDeductorType("");

		filingBatchHeader.setNameofPersonResponsilbleForSal("Venkat Dulipalli"); // 33
		filingBatchHeader.setDesignationofPersonResponsilbleForSal("CEO"); // 34
		filingBatchHeader.setPersonResponsilbleAddr1("A 506 SVC Treewalk");

		filingBatchHeader.setPersonResponsilbleAddr2("Kondapur"); // 36
		filingBatchHeader.setPersonResponsilbleAddr3(""); // 37
		filingBatchHeader.setPersonResponsilbleAddr4(""); // 38
		filingBatchHeader.setPersonResponsilbleAddr5(""); // 39

		filingBatchHeader.setPersonResponsilbleState(CommonUtil.d2.format(Double.valueOf("0"))); // 40
		filingBatchHeader.setPersonResponsilblePin("500081"); // 41
		filingBatchHeader.setPersonResponsilbleEmailId1(""); // 42
		filingBatchHeader.setMobileNumber("9963856876"); // 43

		filingBatchHeader.setPersonResponsilbleSTDCode(StringUtils.EMPTY); // 44
		filingBatchHeader.setPersonResponsilbleTelePhone(StringUtils.EMPTY); // 45
		filingBatchHeader.setPersonResponsilbleSTDCodeAlt(StringUtils.EMPTY); // 44

		filingBatchHeader.setPersonResponsilbleAddrChange("N"); // 46
		// Total/sum *(Challan: Column L)
		filingBatchHeader.setGrossTdsTotalAsPerChallan(StringUtils.EMPTY); // 47
		filingBatchHeader.setUnMatchedChalanCnt(StringUtils.EMPTY); // 48
		filingBatchHeader.setCountOfSalaryDetailRec(StringUtils.EMPTY); // 49
		filingBatchHeader.setGrossTotalIncomeSd(StringUtils.EMPTY); // 50
		filingBatchHeader.setApprovalTaken("N"); // 51

		// Whether regular statement for Form 26Q filed for earlier period
		if (StringUtils.isBlank(filingBatchHeader.getPreviousRrrNo())) {
			filingBatchHeader.setApprovalNo("N"); // 52
		} else {
			filingBatchHeader.setApprovalNo("Y"); // 52
		}
		filingBatchHeader.setLastDeductorType(StringUtils.EMPTY); // 53

		filingBatchHeader.setStateName(CommonUtil.d2.format(Double.valueOf("0"))); // 54
		filingBatchHeader.setPaoCode(StringUtils.EMPTY); // 55 TODO - Future
		filingBatchHeader.setDdoCode(StringUtils.EMPTY); // 56 TODO - Future
		filingBatchHeader.setMinistryName(StringUtils.EMPTY); // 57
		filingBatchHeader.setMinistryNameOther(StringUtils.EMPTY); // 58
		filingBatchHeader.setpANOfResponsiblePerson(StringUtils.EMPTY); // 59
		filingBatchHeader.setPaoRegistrationNo(StringUtils.EMPTY); // 60
		filingBatchHeader.setDdoRegistrationNo(StringUtils.EMPTY); // 61
		filingBatchHeader.setEmployerEmailAlt(StringUtils.EMPTY); // 64
		filingBatchHeader.setPersonResponsilbleEmailIdAlt(StringUtils.EMPTY); // 67
		filingBatchHeader.setaIN(StringUtils.EMPTY); // 68
		filingBatchHeader.setgSTN(StringUtils.EMPTY); // 69
		filingBatchHeader.setBatchHash(StringUtils.EMPTY); // 70

		FilingChallanDetailBean challanDetailBean = new FilingChallanDetailBean();

		challanDetailBean.setBankBranchCode("0000809");
		challanDetailBean.setBankChallanNo("00078");
		challanDetailBean.setBookCash("N");
		challanDetailBean.setChallanDetailRecordNo("1");
		challanDetailBean.setChallanHash("");
		challanDetailBean.setChallanMonth(11);
		challanDetailBean.setChallanSection("194C");
		challanDetailBean.setChallanUpdationIndicator("");
		challanDetailBean.setChBatchNo("1");
		challanDetailBean.setChequeDDNo("");
		challanDetailBean.setCountOfDeducteeDetail("9000000");
		challanDetailBean.setDateOfBankChallanNo("01042020");
		challanDetailBean.setDdoSerialNumberForm24G("");

		//
		challanDetailBean.setFiller3("");
		challanDetailBean.setFiller4("");
		challanDetailBean.setFiller5("");
		challanDetailBean.setFiller6("");
		challanDetailBean.setFiller7("");
		challanDetailBean.setLastBankBranchCode("");
		challanDetailBean.setLastBankChallanNo("");
		challanDetailBean.setLastDateOfBankChallanNo("");
		challanDetailBean.setLastTotalOfDepositAmountAsPerChallan("");
		challanDetailBean.setLastTransferVoucherNo("");
		challanDetailBean.setLateFee("0.00");
		challanDetailBean.setLineNo("3");
		challanDetailBean.setMinorHeadCodeChallan("200");
		challanDetailBean.setNillChallanIndicator("N");
		
		challanDetailBean.setOltasCess("0.00");
		challanDetailBean.setOltasIncomeTax("9000000.00");
		challanDetailBean.setOltasInterest("0.00");
		challanDetailBean.setOltasOthers("0.00");
		challanDetailBean.setOltasSurcharge("0.00");
		
		
		challanDetailBean.setRecType("CD");
		challanDetailBean.setRemark("");
		challanDetailBean.setSection("194C");
		challanDetailBean.setSumTotalIncTaxDedAtSource("9000000.00");
		challanDetailBean.setTdsCessC("0.00");
		challanDetailBean.setTdsIncomeTaxC("9000000.00");
		challanDetailBean.setTdsInterest("0.00");
		challanDetailBean.setTdsOthers("0.00");
		challanDetailBean.setTdsSurchargeC("0.00");
		challanDetailBean.setTotalOfDepositAmountAsPerChallan("9000000.00");
		challanDetailBean.setTotalTaxDepositedAsPerDeducteeAnex("9000000.00");
		

		List<FilingDeducteeDetailBean> deducteeDetailBeanList = new ArrayList<>();


		for (int i = 0; i < 2; i++) {
			FilingDeducteeDetailBean deducteeDetailBean = new FilingDeducteeDetailBean();
			deducteeDetailBean.setAddressOfDeducteeInCountry("");
			deducteeDetailBean.setAmountOfPayment("10.00");
			deducteeDetailBean.setBookCashEntry("");
			deducteeDetailBean.setCertNumAo("");
			deducteeDetailBean.setChallanRecordNo("");
			deducteeDetailBean.setContactNumberOfDeductee("");
			deducteeDetailBean.setCountryOfDeductee("");
			deducteeDetailBean.setDateOfDeposit("09122020");
			deducteeDetailBean.setDateOfFurnishingTaxDeductionCertificate("");
			deducteeDetailBean.setDateOnWhichAmountPaid("09122020");
			deducteeDetailBean.setDateOnWhichTaxDeducted("09122020");
			deducteeDetailBean.setDdBatchNo("1");
			deducteeDetailBean.setDeducteeCode("1234567890");
			deducteeDetailBean.setDeducteeHash("");
			deducteeDetailBean.setDeducteeName("Praveen Morsa");
			deducteeDetailBean.setDeducteePan("AAAPA1234A");
			deducteeDetailBean.setDeducteeRefNo("1234567890");
			
			deducteeDetailBean.setEmailOfDeductee("");
			deducteeDetailBean.setExcess94NAmount("");
			deducteeDetailBean.setFiller1("");
			deducteeDetailBean.setFiller10("");
			deducteeDetailBean.setFiller11("");
			deducteeDetailBean.setFiller12("");

			deducteeDetailBean.setFiller2("");
			deducteeDetailBean.setFiller3("");
			deducteeDetailBean.setFiller4("");
			deducteeDetailBean.setFiller5("");
			deducteeDetailBean.setFiller6("");
			deducteeDetailBean.setFiller7("");
			deducteeDetailBean.setFiller8("");
			deducteeDetailBean.setFiller9("");

			deducteeDetailBean.setFormType("26Q");
			deducteeDetailBean.setGrossingUpIndicator("");
			deducteeDetailBean.setIsDTAA("");
			deducteeDetailBean.setLastDeducteePan("");
			deducteeDetailBean.setLastDeducteeRefNo("");
			deducteeDetailBean.setLastTotalIncomeTaxDeductedAtSource("");
			deducteeDetailBean.setLastTotalTaxDeposited("");

			deducteeDetailBean.setMode("O");
			deducteeDetailBean.setNatureOfRemittance("");
			deducteeDetailBean.setRateAtWhichTaxDeducted("10.0000");
			deducteeDetailBean.setRecType("DD");
			deducteeDetailBean.setRemark1("");
			deducteeDetailBean.setRemark2("");
			deducteeDetailBean.setRemark3("");
			deducteeDetailBean.setSectionCode("94C");
			deducteeDetailBean.setTdsSurchargeDD("0.00");
			deducteeDetailBean.setTdsIncomeTaxDD("1.00");
			deducteeDetailBean.setTdsSurchargeDD("0.00");
			deducteeDetailBean.setTinOfDeductee("");
			deducteeDetailBean.setTotalIncomeTaxDeductedAtSource("1.00");
			deducteeDetailBean.setTotalTaxDeposited("1.00");
			deducteeDetailBean.setTotalValueofPurchase("1.00");
			deducteeDetailBean.setTdsCessDD("0.00");
			deducteeDetailBean.setUniqueAck15CA("");

			deducteeDetailBean.setLineNo(""+(i+1));
			deducteeDetailBean.setChallanRecordNo(""+1);
			deducteeDetailBean.setDeducteeSerialNo(""+(i+1));
			deducteeDetailBean.setDeducteeDetailRecNo(""+(i+1));
			deducteeDetailBeanList.add(deducteeDetailBean);
		}
		challanDetailBean.setDeducteeDetailBeanList(deducteeDetailBeanList);
		
		List<FilingChallanDetailBean> filingChallanDetailbeanList=new ArrayList<>();
		filingChallanDetailbeanList.add(challanDetailBean);
		fileBean.setChallanDetailBeanList(filingChallanDetailbeanList);



		FilingHeaderBean filingHeaderBean = new FilingHeaderBean();

		filingHeaderBean.setLineNo(1);
		filingHeaderBean.setRecordType("FH");
		filingHeaderBean.setFileType("NS1");
		filingHeaderBean.setUploadType("R");

		SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy");
		String dateString = formatter.format(new Date());
		filingHeaderBean.setFileDate(dateString);

		// should be unique across all files? (all files meaning?)
		filingHeaderBean.setFileSeq(1);
		filingHeaderBean.setUploaderType("D");

		filingHeaderBean.setTanOfDeductor("HYDS35527C");
		filingHeaderBean.setNoOfBatches(1);

		filingHeaderBean.setRpuName("NSDL RPU 3.4");

		filingHeaderBean.setRecordHash(StringUtils.EMPTY);
		filingHeaderBean.setFvuVersion(StringUtils.EMPTY);
		filingHeaderBean.setFileHash(StringUtils.EMPTY);
		filingHeaderBean.setSamVersion(StringUtils.EMPTY);
		filingHeaderBean.setSamHash(StringUtils.EMPTY);
		filingHeaderBean.setScmVersion(StringUtils.EMPTY);
		filingHeaderBean.setScmHash(StringUtils.EMPTY);
		filingHeaderBean.setFileHash(StringUtils.EMPTY);
		filingHeaderBean.setConsHash(StringUtils.EMPTY);
		fileBean.setHeaderBean(filingHeaderBean);

		fileBean.setBatchHeaderBean(filingBatchHeader);

		try {
			service.generateTextFile(fileBean, "", "26Q", false, 2021, "Q3");
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (StorageException e) {
			e.printStackTrace();
		}

	}

}
