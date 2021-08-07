package com.ey.in.tds.returns.domain;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.ey.in.tds.core.util.FilingFileCountry;
import com.ey.in.tds.core.util.FilingFileRemittance;
import com.poiji.annotation.ExcelCell;

public class FilingDeducteeDetailBean extends AbstractFilingBean {

	private static final long serialVersionUID = -1703132467198848995L;

	private Map<String, Integer> lengthMap = new HashMap<String, Integer>() {
		private static final long serialVersionUID = 7119600734413398909L;
		{
			put("lineNo", 9); // 1
			put("recType", 2); // 2
			put("ddBatchNo", 9); // 3
			put("challanRecordNo", 9); // 4
			put("deducteeDetailRecNo", 9); // 5
			put("mode", 1); // 6
			put("deducteeSerialNo", 0); // 7
			put("deducteeCode", 1); // 8 // 1 Compnay, 2 Others
			put("lastDeducteePan", 10); // 9
			put("deducteePan", 10); // 10 // or deductee PAN
			put("lastDeducteeRefNo", 10); // 11
			put("deducteeRefNo", 10); // 12
			put("deducteeName", 75); // 13
			put("tdsIncomeTaxDD", 15); // 14
			put("tdsSurchargeDD", 15); // 15
			put("tdsCessDD", 15); // 16
			put("totalIncomeTaxDeductedAtSource", 15); // 17
			put("lastTotalIncomeTaxDeductedAtSource", 15); // 18
			put("totalTaxDeposited", 15); // 19
			put("lastTotalTaxDeposited", 15); // 20
			put("totalValueofPurchase", 0); // 21 // NA
			put("amountOfPayment", 15); // 22
			put("dateOnWhichAmountPaid", 8); // 23
			put("dateOnWhichTaxDeducted", 8); // 24
			put("dateOfDeposit", 0); // 25 // NA
			put("rateAtWhichTaxDeducted", 7); // 26
			put("grossingUpIndicator", 1); // 27
			put("bookCashEntry", 1); // 28 // One character
			put("dateOfFurnishingTaxDeductionCertificate", 0); // 29
			put("remark1", 1); // 30
			put("remark2", 1); // 31
			put("remark3", 14); // 32
			put("sectionCode", 3); // 33
			put("certNumAo", 10); // 34
			put("filler1", 0); // 35
			put("filler2", 0); // 36
			put("filler3", 0); // 37
			put("filler4", 0); // 38
			put("filler5", 0); // 39
			put("filler6", 0); // 40
			put("filler7", 0); // 41
			put("filler8", 0); // 42
			put("excess94NAmount", 15); // 43
			put("filler9", 0); // 44
			put("filler10", 0); // 45
			put("filler11", 0); // 46
			put("filler12", 0); // 47
			put("deducteeHash", 0); // 48
			
			// 27Q fields
			put("isDTAA", 1); // 35
			put("natureOfRemittance", 3); // 36
			put("uniqueAck15CA", 15); // 37
			put("countryOfDeductee", 3); // 38
			put("emailOfDeductee", 75); // 39
			put("contactNumberOfDeductee", 15); // 40
			put("addressOfDeducteeInCountry", 150); // 41
			put("tinOfDeductee", 25); // 42
		}
	};

	/*
	 * Present in deductee master table
	 */
	private String lineNo; // 1
	private String recType = "DD"; // 2
	private String ddBatchNo; // 3
	@ExcelCell(2)
	private String challanRecordNo; // 4
	private String deducteeDetailRecNo; // 5
	private String mode; // 6
	private String deducteeSerialNo = ""; // 7
	@ExcelCell(28)
	private String deducteeCode; // 8 // 1 Compnay, 2 Others
	@ExcelCell(14)
	private String lastDeducteePan; // 9
	@ExcelCell(15)
	private String deducteePan; // 10 // or deductee PAN
	private String lastDeducteeRefNo; // 11
	@ExcelCell(13)
	private String deducteeRefNo; // 12
	
	@ExcelCell(16)
	private String deducteeName; // 13
	
	@ExcelCell(19)
	private String tdsIncomeTaxDD; // 14
	
	@ExcelCell(20)
	private String tdsSurchargeDD; // 15
	
	@ExcelCell(21)
	private String tdsCessDD; // 16
	
	@ExcelCell(22)
	private String totalIncomeTaxDeductedAtSource; // 17
	@ExcelCell(23)
	private String lastTotalIncomeTaxDeductedAtSource; // 18
	
	@ExcelCell(24)
	private String totalTaxDeposited; // 19
	@ExcelCell(25)
	private String lastTotalTaxDeposited; // 20
	
	private String totalValueofPurchase = ""; // 21 // NA
	
	// payment details
	@ExcelCell(18)
	private String amountOfPayment; // 22
	@ExcelCell(17)
	private String dateOnWhichAmountPaid; // 23
	@ExcelCell(26)
	private String dateOnWhichTaxDeducted; // 24
	
	private String dateOfDeposit = ""; // 25 // NA
	
	@ExcelCell(29)
	private String rateAtWhichTaxDeducted; // 26
	
	// invoicelineitem
	@ExcelCell(32)
	private String grossingUpIndicator; // 27
	
	@ExcelCell(30)
	private String bookCashEntry; // 28 // One character
	
	private String dateOfFurnishingTaxDeductionCertificate; // 29
	
	@ExcelCell(27)
	private String remark1; // 30
	
	private String remark2; // 31
	private String remark3; // 32
	
	@ExcelCell(7)
	private String sectionCode; // 33
	
	@ExcelCell(31)
	private String certNumAo; // 34
	
	private String filler1; // 35
	private String filler2; // 36
	private String filler3; // 37
	private String filler4; // 38
	private String filler5; // 39
	private String filler6; // 40
	private String filler7; // 41
	private String filler8; // 42
	
	@ExcelCell(33)
	private String excess94NAmount; // 43
	
	private String filler9; // 44
	private String filler10; // 45
	private String filler11; // 46
	private String filler12; // 47
	private String deducteeHash; // 48
	
	@ExcelCell(34)
	private String isDTAA;
	
	@ExcelCell(35)
	private String natureOfRemittance;
	
	@ExcelCell(36)
	private String uniqueAck15CA;
	
	@ExcelCell(37)
	private String countryOfDeductee;
	
	@ExcelCell(38)
	private String emailOfDeductee;
	
	@ExcelCell(39)
	private String contactNumberOfDeductee;
	
	@ExcelCell(40)
	private String addressOfDeducteeInCountry;
	
	@ExcelCell(41)
	private String tinOfDeductee;

	public FilingDeducteeDetailBean() {
		
	}
	// Non participating field
	private String formType;
	
	public FilingDeducteeDetailBean(String formType) {
		this.setFormType(formType);
	}

	public String getLastDeducteeRefNo() {
		return trim(getNullSafeValue(this.lastDeducteeRefNo), lengthMap.get("lastDeducteeRefNo"));
	}

	public void setLastDeducteeRefNo(String lastDeducteeRefNo) {
		this.lastDeducteeRefNo = lastDeducteeRefNo;
	}

	public String getFiller1() {
		return trim(getNullSafeValue(this.filler1), lengthMap.get("filler1"));
	}

	public void setFiller1(String filler1) {
		this.filler1 = filler1;
	}

	public String getFiller2() {
		return trim(getNullSafeValue(this.filler2), lengthMap.get("filler2"));
	}

	public void setFiller2(String filler2) {
		this.filler2 = filler2;
	}

	public String getFiller3() {
		return trim(getNullSafeValue(this.filler3), lengthMap.get("filler3"));
	}

	public void setFiller3(String filler3) {
		this.filler3 = filler3;
	}

	public String getFiller4() {
		return trim(getNullSafeValue(this.filler4), lengthMap.get("filler4"));
	}

	public void setFiller4(String filler4) {
		this.filler4 = filler4;
	}

	public String getFiller5() {
		return trim(getNullSafeValue(this.filler5), lengthMap.get("filler5"));
	}

	public void setFiller5(String filler5) {
		this.filler5 = filler5;
	}

	public String getFiller6() {
		return trim(getNullSafeValue(this.filler6), lengthMap.get("filler6"));
	}

	public void setFiller6(String filler6) {
		this.filler6 = filler6;
	}

	public String getFiller7() {
		return trim(getNullSafeValue(this.filler7), lengthMap.get("filler7"));
	}

	public void setFiller7(String filler7) {
		this.filler7 = filler7;
	}

	public String getFiller8() {
		return trim(getNullSafeValue(this.filler8), lengthMap.get("filler8"));
	}

	public void setFiller8(String filler8) {
		this.filler8 = filler8;
	}

	public String getLineNo() {
		return trim(getNullSafeValue(this.lineNo), lengthMap.get("lineNo"));
	}

	public void setLineNo(String lineNo) {
		this.lineNo = lineNo;
	}

	public String getRecType() {
		return trim(getNullSafeValue(this.recType), lengthMap.get("recType"));
	}

	public void setRecType(String recType) {
		this.recType = recType;
	}

	public String getDdBatchNo() {
		return trim(getNullSafeValue(this.ddBatchNo), lengthMap.get("ddBatchNo"));
	}

	public void setDdBatchNo(String ddBatchNo) {
		this.ddBatchNo = ddBatchNo;
	}

	public String getChallanRecordNo() {
		return trim(getNullSafeValue(this.challanRecordNo), lengthMap.get("challanRecordNo"));
	}

	public void setChallanRecordNo(String challanRecordNo) {
		this.challanRecordNo = challanRecordNo;
	}

	public String getDeducteeDetailRecNo() {
		return trim(getNullSafeValue(this.deducteeDetailRecNo), lengthMap.get("deducteeDetailRecNo"));
	}

	public void setDeducteeDetailRecNo(String deducteeDetailRecNo) {
		this.deducteeDetailRecNo = deducteeDetailRecNo;
	}

	public String getMode() {
		return trim(getNullSafeValue(this.mode), lengthMap.get("mode"));
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getDeducteeSerialNo() {
		return trim(getNullSafeValue(this.deducteeSerialNo), lengthMap.get("deducteeSerialNo"));
	}

	public void setDeducteeSerialNo(String employeeSerialNo) {
		this.deducteeSerialNo = employeeSerialNo;
	}

	public String getDeducteeCode() {
		return trim(getNullSafeValue(this.deducteeCode), lengthMap.get("deducteeCode"));
	}

	public void setDeducteeCode(String deducteeCode) {
		this.deducteeCode = deducteeCode;
	}

	public String getLastDeducteePan() {
		return trim(getNullSafeValue(this.lastDeducteePan), lengthMap.get("lastDeducteePan"));
	}

	public void setLastDeducteePan(String lastDeducteePan) {
		this.lastDeducteePan = lastDeducteePan;
	}

	public String getDeducteePan() {
		return trim(getNullSafeValue(this.deducteePan), lengthMap.get("deducteePan"));
	}

	public void setDeducteePan(String employeePan) {
		this.deducteePan = employeePan;
	}

	public String getDeducteeName() {
		return trim(getNullSafeValue(this.deducteeName), lengthMap.get("deducteeName"));
	}

	public void setDeducteeName(String employeeName) {
		this.deducteeName = employeeName;
	}

	public String getTdsIncomeTaxDD() {
		return trim(getNullSafeValue(this.tdsIncomeTaxDD), lengthMap.get("tdsIncomeTaxDD"));
	}

	public void setTdsIncomeTaxDD(String tdsIncomeTaxDD) {
		this.tdsIncomeTaxDD = tdsIncomeTaxDD;
	}

	public String getTdsSurchargeDD() {
		return trim(getNullSafeValue(this.tdsSurchargeDD), lengthMap.get("tdsSurchargeDD"));
	}

	public void setTdsSurchargeDD(String tdsSurchargeDD) {
		this.tdsSurchargeDD = tdsSurchargeDD;
	}

	public String getTdsCessDD() {
		return trim(getNullSafeValue(this.tdsCessDD), lengthMap.get("tdsCessDD"));
	}

	public void setTdsCessDD(String tdsCessDD) {
		this.tdsCessDD = tdsCessDD;
	}

	public String getTotalIncomeTaxDeductedAtSource() {
		return trim(getNullSafeValue(this.totalIncomeTaxDeductedAtSource),
				lengthMap.get("totalIncomeTaxDeductedAtSource"));
	}

	public void setTotalIncomeTaxDeductedAtSource(String totalIncomeTaxDeductedAtSource) {
		this.totalIncomeTaxDeductedAtSource = totalIncomeTaxDeductedAtSource;
	}

	public String getLastTotalIncomeTaxDeductedAtSource() {
		return trim(getNullSafeValue(this.lastTotalIncomeTaxDeductedAtSource),
				lengthMap.get("lastTotalIncomeTaxDeductedAtSource"));
	}

	public void setLastTotalIncomeTaxDeductedAtSource(String lastTotalIncomeTaxDeductedAtSource) {
		this.lastTotalIncomeTaxDeductedAtSource = lastTotalIncomeTaxDeductedAtSource;
	}

	public String getTotalTaxDeposited() {
		return trim(getNullSafeValue(this.totalTaxDeposited), lengthMap.get("totalTaxDeposited"));
	}

	public void setTotalTaxDeposited(String totalTaxDeposited) {
		this.totalTaxDeposited = totalTaxDeposited;
	}

	public String getLastTotalTaxDeposited() {
		return trim(getNullSafeValue(this.lastTotalTaxDeposited), lengthMap.get("lastTotalTaxDeposited"));
	}

	public void setLastTotalTaxDeposited(String lastTotalTaxDeposited) {
		this.lastTotalTaxDeposited = lastTotalTaxDeposited;
	}

	public String getTotalValueofPurchase() {
		return trim(getNullSafeValue(this.totalValueofPurchase), lengthMap.get("totalValueofPurchase"));
	}

	public void setTotalValueofPurchase(String totalValueofPurchase) {
		this.totalValueofPurchase = totalValueofPurchase;
	}

	public String getAmountOfPayment() {
		return trim(getNullSafeValue(this.amountOfPayment), lengthMap.get("amountOfPayment"));
	}

	public void setAmountOfPayment(String amountOfPayment) {
		this.amountOfPayment = amountOfPayment;
	}

	public String getDateOnWhichAmountPaid() {
		return trim(cleanseData(getNullSafeValue(this.dateOnWhichAmountPaid)), lengthMap.get("dateOnWhichAmountPaid"));
	}

	public void setDateOnWhichAmountPaid(String dateOnWhichAmountPaid) {
		this.dateOnWhichAmountPaid = dateOnWhichAmountPaid;
	}

	public String getDateOnWhichTaxDeducted() {
		return trim(cleanseData(getNullSafeValue(this.dateOnWhichTaxDeducted)), lengthMap.get("dateOnWhichTaxDeducted"));
	}

	public void setDateOnWhichTaxDeducted(String dateOnWhichTaxDeducted) {
		this.dateOnWhichTaxDeducted = dateOnWhichTaxDeducted;
	}

	public String getDateOfDeposit() {
		return trim(getNullSafeValue(this.dateOfDeposit), lengthMap.get("dateOfDeposit"));
	}

	public void setDateOfDeposit(String dateOfDeposit) {
		this.dateOfDeposit = dateOfDeposit;
	}

	public String getRateAtWhichTaxDeducted() {
		return trim(getNullSafeValue(this.rateAtWhichTaxDeducted), lengthMap.get("rateAtWhichTaxDeducted"));
	}

	public void setRateAtWhichTaxDeducted(String rateAtWhichTaxDeducted) {
		this.rateAtWhichTaxDeducted = rateAtWhichTaxDeducted;
	}

	public String getGrossingUpIndicator() {
		return trim(getNullSafeValue(this.grossingUpIndicator), lengthMap.get("grossingUpIndicator"));
	}

	public void setGrossingUpIndicator(String grossingUpIndicator) {
		this.grossingUpIndicator = grossingUpIndicator;
	}

	public String getBookCashEntry() {
		return trim(getNullSafeValue(this.bookCashEntry), lengthMap.get("bookCashEntry"));
	}

	public void setBookCashEntry(String bookCashEntry) {
		this.bookCashEntry = bookCashEntry;
	}

	public String getDateOfFurnishingTaxDeductionCertificate() {
		return trim(getNullSafeValue(this.dateOfFurnishingTaxDeductionCertificate),
				lengthMap.get("dateOfFurnishingTaxDeductionCertificate"));
	}

	public void setDateOfFurnishingTaxDeductionCertificate(String dateOfFurnishingTaxDeductionCertificate) {
		this.dateOfFurnishingTaxDeductionCertificate = dateOfFurnishingTaxDeductionCertificate;
	}

	public String getRemark1() {
		return trim(getNullSafeValue(this.remark1), lengthMap.get("remark1"));
	}

	public void setRemark1(String remark1) {
		this.remark1 = remark1;
	}

	public String getRemark2() {
		return trim(getNullSafeValue(this.remark2), lengthMap.get("remark2"));
	}

	public void setRemark2(String remark2) {
		this.remark2 = remark2;
	}

	public String getRemark3() {
		return trim(getNullSafeValue(this.remark3), lengthMap.get("remark3"));
	}

	public void setRemark3(String remark3) {
		this.remark3 = remark3;
	}

	public String getSectionCode() {
		return getNullSafeValue(this.sectionCode);
	}

	public void setSectionCode(String sectionCode) {
		this.sectionCode = sectionCode;
	}

	public String getCertNumAo() {
		return trim(getNullSafeValue(this.certNumAo), lengthMap.get("certNumAo"));
	}

	public void setCertNumAo(String certNumAo) {
		this.certNumAo = certNumAo;
	}

	public String getDeducteeHash() {
		return trim(getNullSafeValue(this.deducteeHash), lengthMap.get("deducteeHash"));
	}

	public void setDeducteeHash(String deducteeHash) {
		this.deducteeHash = deducteeHash;
	}

	public String getDeducteeRefNo() {
		return trim(getNullSafeValue(this.deducteeRefNo), lengthMap.get("deducteeRefNo"));
	}

	public void setDeducteeRefNo(String deducteeRefNo) {
		this.deducteeRefNo = deducteeRefNo;
	}

	public String getExcess94NAmount() {
		return trim(getNullSafeValue(this.excess94NAmount), lengthMap.get("excess94NAmount"));
	}

	public void setExcess94NAmount(String excess94nAmount) {
		excess94NAmount = excess94nAmount;
	}

	public String getFiller9() {
		return trim(getNullSafeValue(this.filler9), lengthMap.get("filler9"));
	}

	public void setFiller9(String filler9) {
		this.filler9 = filler9;
	}

	public String getFiller10() {
		return trim(getNullSafeValue(this.filler10), lengthMap.get("filler10"));
	}

	public void setFiller10(String filler10) {
		this.filler10 = filler10;
	}

	public String getFiller11() {
		return trim(getNullSafeValue(this.filler11), lengthMap.get("filler11"));
	}

	public void setFiller11(String filler11) {
		this.filler11 = filler11;
	}

	public String getFiller12() {
		return trim(getNullSafeValue(this.filler12), lengthMap.get("filler12"));
	}

	public void setFiller12(String filler12) {
		this.filler12 = filler12;
	}
	
	public String getIsDTAA() {
		return trim(getNullSafeValue(this.isDTAA), lengthMap.get("isDTAA"));
	}

	public void setIsDTAA(String isDTAA) {
		this.isDTAA = isDTAA;
	}

	public String getNatureOfRemittance() {
		String natureOfRemittanceCode = StringUtils.EMPTY;
		if (StringUtils.isNotBlank(natureOfRemittance)) {
			natureOfRemittanceCode = FilingFileRemittance.getfilingFileRemittance(natureOfRemittance);
		}
		return trim(getNullSafeValue(natureOfRemittanceCode), lengthMap.get("natureOfRemittance"));
	}

	public void setNatureOfRemittance(String natureOfRemittance) {
		this.natureOfRemittance = natureOfRemittance;
	}

	public String getUniqueAck15CA() {
		return trim(getNullSafeValue(this.uniqueAck15CA), lengthMap.get("uniqueAck15CA"));
	}

	public void setUniqueAck15CA(String uniqueAck15CA) {
		this.uniqueAck15CA = uniqueAck15CA;
	}

	public String getCountryOfDeductee() {
		String countryCode = StringUtils.EMPTY;
		if (StringUtils.isNotBlank(countryOfDeductee)) {
			countryCode = FilingFileCountry.getfilingFileCountry(this.countryOfDeductee);
		}
		return trim(getNullSafeValue(countryCode), lengthMap.get("countryOfDeductee"));
	}

	public void setCountryOfDeductee(String countryOfDeductee) {
		this.countryOfDeductee = countryOfDeductee;
	}

	public String getEmailOfDeductee() {
		return trim(getNullSafeValue(this.emailOfDeductee), lengthMap.get("emailOfDeductee"));
	}

	public void setEmailOfDeductee(String emailOfDeductee) {
		this.emailOfDeductee = emailOfDeductee;
	}

	public String getContactNumberOfDeductee() {
		return trim(getNullSafeValue(this.contactNumberOfDeductee), lengthMap.get("contactNumberOfDeductee"));
	}

	public void setContactNumberOfDeductee(String contactNumberOfDeductee) {
		this.contactNumberOfDeductee = contactNumberOfDeductee;
	}

	public String getAddressOfDeducteeInCountry() {
		return trim(getNullSafeValue(this.addressOfDeducteeInCountry), lengthMap.get("addressOfDeducteeInCountry"));
	}

	public void setAddressOfDeducteeInCountry(String addressOfDeducteeInCountry) {
		this.addressOfDeducteeInCountry = addressOfDeducteeInCountry;
	}

	public String getTinOfDeductee() {
		return trim(getNullSafeValue(this.tinOfDeductee), lengthMap.get("tinOfDeductee"));
	}

	public void setTinOfDeductee(String tinOfDeductee) {
		this.tinOfDeductee = tinOfDeductee;
	}

	@Override
	public String toString() {
		String value =  getLineNo() + "^" // 1
				+ getRecType() + "^" // 2
				+ getDdBatchNo() + "^" // 3
				+ getChallanRecordNo() + "^" // 4
				+ getDeducteeDetailRecNo() + "^" // 5
				+ getMode() + "^" // 6
				+ getDeducteeSerialNo() + "^" // 7
				+ getDeducteeCode() + "^" // 8
				+ getLastDeducteePan() + "^" // 9
				+ getDeducteePan() + "^" // 10
				+ getLastDeducteeRefNo() + "^" // 11
				+ getDeducteeRefNo() + "^" // 12
				+ getDeducteeName() + "^" // 13
				+ getTdsIncomeTaxDD() + "^" // 14
				+ getTdsSurchargeDD() + "^" // 15
				+ getTdsCessDD() + "^" // 16
				+ getTotalIncomeTaxDeductedAtSource() + "^" // 17
				+ getLastTotalIncomeTaxDeductedAtSource() + "^" // 18
				+ getTotalTaxDeposited() + "^" // 19
				+ getLastTotalTaxDeposited() + "^" // 20
				+ getTotalValueofPurchase() + "^" // 21
				+ getAmountOfPayment() + "^" // 22
				+ getDateOnWhichAmountPaid() + "^" // 23
				+ getDateOnWhichTaxDeducted() + "^" // 24
				+ getDateOfDeposit() + "^" // 25
				+ getRateAtWhichTaxDeducted() + "^" // 26
				+ getGrossingUpIndicator() + "^" // 27
				+ getBookCashEntry() + "^" // 28
				+ getDateOfFurnishingTaxDeductionCertificate() + "^" // 29
				+ getRemark1() + "^" // 30
				+ getRemark2() + "^" // 31
				+ getRemark3() + "^" // 32
				+ getSectionCode() + "^" // 33
				+ getCertNumAo() + "^"; // 34
				
				if("26Q".equals(getFormType())) {
					value = value + getFiller1() + "^" // 35
							+ getFiller2() + "^" // 36
							+ getFiller3() + "^" // 37
							+ getFiller4() + "^" // 38
							+ getFiller5() + "^" // 39
							+ getFiller6() + "^" // 40
							+ getFiller7() + "^" // 41
							+ getFiller8() + "^"; // 42
				} else if("27Q".equals(getFormType())) {
					value = value + getIsDTAA() + "^" // 35
							+ getNatureOfRemittance() + "^" // 36
							+ getUniqueAck15CA() + "^" // 37
							+ getCountryOfDeductee() + "^" // 38
							+ getEmailOfDeductee() + "^" // 39
							+ getContactNumberOfDeductee() + "^" // 40
							+ getAddressOfDeducteeInCountry() + "^" // 41
							+ getTinOfDeductee() + "^"; // 42
				}
				
				value = value + getExcess94NAmount() + "^" // 43 - for 94N it should be
				+ getFiller9() + "^" // 44
				+ getFiller10() + "^" // 45
				+ getFiller11() + "^" // 46
				+ getFiller12() + "^" // 47
				+ getDeducteeHash(); // 48
		return value;
	}

	public String getFormType() {
		return formType;
	}

	public void setFormType(String formType) {
		this.formType = formType;
	}
	
	public static String getObjectInString(String formType) {
		String value =  "lineNo" + "^" // 1
				+ "recType" + "^" // 2
				+ "ddBatchNo" + "^" // 3
				+ "challanRecordNo" + "^" // 4
				+ "deducteeDetailRecNo" + "^" // 5
				+ "mode" + "^" // 6
				+ "deducteeSerialNo" + "^" // 7
				+ "deducteeCode" + "^" // 8
				+ "lastDeducteePan" + "^" // 9
				+ "deducteePan" + "^" // 10
				+ "lastDeducteeRefNo" + "^" // 11
				+ "deducteeRefNo" + "^" // 12
				+"deducteeName" + "^" // 13
				+ "tdsIncomeTaxDD" + "^" // 14
				+ "tdsSurchargeDD" + "^" // 15
				+"tdsCessDD" + "^" // 16
				+ "totalIncomeTaxDeductedAtSource" + "^" // 17
				+ "lastTotalIncomeTaxDeductedAtSource" + "^" // 18
				+ "totalTaxDeposited" + "^" // 19
				+ "lastTotalTaxDeposited" + "^" // 20
				+ "totalValueofPurchase" + "^" // 21
				+ "amountOfPayment" + "^" // 22
				+ "dateOnWhichAmountPaid" + "^" // 23
				+ "dateOnWhichTaxDeducted" + "^" // 24
				+ "dateOfDeposit" + "^" // 25
				+ "rateAtWhichTaxDeducted" + "^" // 26
				+ "grossingUpIndicator" + "^" // 27
				+ "bookCashEntry" + "^" // 28
				+ "dateOfFurnishingTaxDeductionCertificate"+ "^" // 29
				+ "remark1" + "^" // 30
				+ "remark2" + "^" // 31
				+ "remark3" + "^" // 32
				+ "sectionCode" + "^" // 33
				+ "certNumAo" + "^"; // 34
				
				if("26Q".equals(formType)) {
					value = value + "filler1" + "^" // 35
							+ "filler2" + "^" // 36
							+ "filler3" + "^" // 37
							+ "filler4" + "^" // 38
							+ "filler5" + "^" // 39
							+ "filler6" + "^" // 40
							+ "filler7" + "^" // 41
							+ "filler8" + "^"; // 42
				} else if("27Q".equals(formType)) {
					value = value + "isDTAA" + "^" // 35
							+ "natureOfRemittance" + "^" // 36
							+ "uniqueAck15CA" + "^" // 37
							+ "countryOfDeductee" + "^" // 38
							+ "emailOfDeductee" + "^" // 39
							+ "contactNumberOfDeductee" + "^" // 40
							+ "addressOfDeducteeInCountry" + "^" // 41
							+ "tinOfDeductee" + "^"; // 42
				}
				
				value = value + "excess94NAmount" + "^" // 43 - for 94N it should be
				+ "filler9" + "^" // 44
				+ "filler10" + "^" // 45
				+ "filler11" + "^" // 46
				+ "filler12" + "^" // 47
				+ "deducteeHash"; // 48
		return value;
	}
}
