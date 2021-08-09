package com.ey.in.tds.returns.domain;

public class FilingDeducteeErrorBean {

	private String lineNo; // 1
	private String recType; // 2
	private String ddBatchNo; // 3
	private String challanRecordNo; // 4
	private String deducteeDetailRecNo; // 5
	private String mode; // 6
	private String deducteeSerialNo; // 7
	private String deducteeCode; // 8 // 1 Compnay, 2 Others
	private String lastDeducteePan; // 9
	private String deducteePan; // 10 // or deductee PAN
	private String lastDeducteeRefNo; // 11
	private String deducteeRefNo; // 12
	private String deducteeName; // 13
	private String tdsIncomeTaxDD; // 14
	private String tdsSurchargeDD; // 15
	private String tdsCessDD; // 16
	private String totalIncomeTaxDeductedAtSource; // 17
	private String lastTotalIncomeTaxDeductedAtSource; // 18
	private String totalTaxDeposited; // 19
	private String lastTotalTaxDeposited; // 20
	private String totalValueofPurchase = ""; // 21 // NA
	private String amountOfPayment; // 22
	private String dateOnWhichAmountPaid; // 23
	private String dateOnWhichTaxDeducted; // 24
	private String dateOfDeposit = ""; // 25 // NA
	private String rateAtWhichTaxDeducted; // 26
	private String grossingUpIndicator; // 27
	private String bookCashEntry; // 28 // One character
	private String dateOfFurnishingTaxDeductionCertificate; // 29
	private String remark1; // 30
	private String remark2; // 31
	private String remark3; // 32
	private String sectionCode; // 33
	private String certNumAo; // 34
	private String filler1; // 35
	private String filler2; // 36
	private String filler3; // 37
	private String filler4; // 38
	private String filler5; // 39
	private String filler6; // 40
	private String filler7; // 41
	private String filler8; // 42
	private String excess94NAmount; // 43
	private String filler9; // 44
	private String filler10; // 45
	private String filler11; // 46
	private String filler12; // 47
	private String deducteeHash; // 48
	private String isDTAA;
	private String natureOfRemittance;
	private String uniqueAck15CA;
	private String countryOfDeductee;
	private String emailOfDeductee;
	private String contactNumberOfDeductee;
	private String addressOfDeducteeInCountry;
	private String tinOfDeductee;
	private String serialNumber;
	private String reason;
	private String deductorMasterTan;

	public String getLineNo() {
		return lineNo;
	}

	public void setLineNo(String lineNo) {
		this.lineNo = lineNo;
	}

	public String getRecType() {
		return recType;
	}

	public void setRecType(String recType) {
		this.recType = recType;
	}

	public String getDdBatchNo() {
		return ddBatchNo;
	}

	public void setDdBatchNo(String ddBatchNo) {
		this.ddBatchNo = ddBatchNo;
	}

	public String getChallanRecordNo() {
		return challanRecordNo;
	}

	public void setChallanRecordNo(String challanRecordNo) {
		this.challanRecordNo = challanRecordNo;
	}

	public String getDeducteeDetailRecNo() {
		return deducteeDetailRecNo;
	}

	public void setDeducteeDetailRecNo(String deducteeDetailRecNo) {
		this.deducteeDetailRecNo = deducteeDetailRecNo;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getDeducteeSerialNo() {
		return deducteeSerialNo;
	}

	public void setDeducteeSerialNo(String deducteeSerialNo) {
		this.deducteeSerialNo = deducteeSerialNo;
	}

	public String getDeducteeCode() {
		return deducteeCode;
	}

	public void setDeducteeCode(String deducteeCode) {
		this.deducteeCode = deducteeCode;
	}

	public String getLastDeducteePan() {
		return lastDeducteePan;
	}

	public void setLastDeducteePan(String lastDeducteePan) {
		this.lastDeducteePan = lastDeducteePan;
	}

	public String getDeducteePan() {
		return deducteePan;
	}

	public void setDeducteePan(String deducteePan) {
		this.deducteePan = deducteePan;
	}

	public String getLastDeducteeRefNo() {
		return lastDeducteeRefNo;
	}

	public void setLastDeducteeRefNo(String lastDeducteeRefNo) {
		this.lastDeducteeRefNo = lastDeducteeRefNo;
	}

	public String getDeducteeRefNo() {
		return deducteeRefNo;
	}

	public void setDeducteeRefNo(String deducteeRefNo) {
		this.deducteeRefNo = deducteeRefNo;
	}

	public String getDeducteeName() {
		return deducteeName;
	}

	public void setDeducteeName(String deducteeName) {
		this.deducteeName = deducteeName;
	}

	public String getTdsIncomeTaxDD() {
		return tdsIncomeTaxDD;
	}

	public void setTdsIncomeTaxDD(String tdsIncomeTaxDD) {
		this.tdsIncomeTaxDD = tdsIncomeTaxDD;
	}

	public String getTdsSurchargeDD() {
		return tdsSurchargeDD;
	}

	public void setTdsSurchargeDD(String tdsSurchargeDD) {
		this.tdsSurchargeDD = tdsSurchargeDD;
	}

	public String getTdsCessDD() {
		return tdsCessDD;
	}

	public void setTdsCessDD(String tdsCessDD) {
		this.tdsCessDD = tdsCessDD;
	}

	public String getTotalIncomeTaxDeductedAtSource() {
		return totalIncomeTaxDeductedAtSource;
	}

	public void setTotalIncomeTaxDeductedAtSource(String totalIncomeTaxDeductedAtSource) {
		this.totalIncomeTaxDeductedAtSource = totalIncomeTaxDeductedAtSource;
	}

	public String getLastTotalIncomeTaxDeductedAtSource() {
		return lastTotalIncomeTaxDeductedAtSource;
	}

	public void setLastTotalIncomeTaxDeductedAtSource(String lastTotalIncomeTaxDeductedAtSource) {
		this.lastTotalIncomeTaxDeductedAtSource = lastTotalIncomeTaxDeductedAtSource;
	}

	public String getTotalTaxDeposited() {
		return totalTaxDeposited;
	}

	public void setTotalTaxDeposited(String totalTaxDeposited) {
		this.totalTaxDeposited = totalTaxDeposited;
	}

	public String getLastTotalTaxDeposited() {
		return lastTotalTaxDeposited;
	}

	public void setLastTotalTaxDeposited(String lastTotalTaxDeposited) {
		this.lastTotalTaxDeposited = lastTotalTaxDeposited;
	}

	public String getTotalValueofPurchase() {
		return totalValueofPurchase;
	}

	public void setTotalValueofPurchase(String totalValueofPurchase) {
		this.totalValueofPurchase = totalValueofPurchase;
	}

	public String getAmountOfPayment() {
		return amountOfPayment;
	}

	public void setAmountOfPayment(String amountOfPayment) {
		this.amountOfPayment = amountOfPayment;
	}

	public String getDateOnWhichAmountPaid() {
		return dateOnWhichAmountPaid;
	}

	public void setDateOnWhichAmountPaid(String dateOnWhichAmountPaid) {
		this.dateOnWhichAmountPaid = dateOnWhichAmountPaid;
	}

	public String getDateOnWhichTaxDeducted() {
		return dateOnWhichTaxDeducted;
	}

	public void setDateOnWhichTaxDeducted(String dateOnWhichTaxDeducted) {
		this.dateOnWhichTaxDeducted = dateOnWhichTaxDeducted;
	}

	public String getDateOfDeposit() {
		return dateOfDeposit;
	}

	public void setDateOfDeposit(String dateOfDeposit) {
		this.dateOfDeposit = dateOfDeposit;
	}

	public String getRateAtWhichTaxDeducted() {
		return rateAtWhichTaxDeducted;
	}

	public void setRateAtWhichTaxDeducted(String rateAtWhichTaxDeducted) {
		this.rateAtWhichTaxDeducted = rateAtWhichTaxDeducted;
	}

	public String getGrossingUpIndicator() {
		return grossingUpIndicator;
	}

	public void setGrossingUpIndicator(String grossingUpIndicator) {
		this.grossingUpIndicator = grossingUpIndicator;
	}

	public String getBookCashEntry() {
		return bookCashEntry;
	}

	public void setBookCashEntry(String bookCashEntry) {
		this.bookCashEntry = bookCashEntry;
	}

	public String getDateOfFurnishingTaxDeductionCertificate() {
		return dateOfFurnishingTaxDeductionCertificate;
	}

	public void setDateOfFurnishingTaxDeductionCertificate(String dateOfFurnishingTaxDeductionCertificate) {
		this.dateOfFurnishingTaxDeductionCertificate = dateOfFurnishingTaxDeductionCertificate;
	}

	public String getRemark1() {
		return remark1;
	}

	public void setRemark1(String remark1) {
		this.remark1 = remark1;
	}

	public String getRemark2() {
		return remark2;
	}

	public void setRemark2(String remark2) {
		this.remark2 = remark2;
	}

	public String getRemark3() {
		return remark3;
	}

	public void setRemark3(String remark3) {
		this.remark3 = remark3;
	}

	public String getSectionCode() {
		return sectionCode;
	}

	public void setSectionCode(String sectionCode) {
		this.sectionCode = sectionCode;
	}

	public String getCertNumAo() {
		return certNumAo;
	}

	public void setCertNumAo(String certNumAo) {
		this.certNumAo = certNumAo;
	}

	public String getFiller1() {
		return filler1;
	}

	public void setFiller1(String filler1) {
		this.filler1 = filler1;
	}

	public String getFiller2() {
		return filler2;
	}

	public void setFiller2(String filler2) {
		this.filler2 = filler2;
	}

	public String getFiller3() {
		return filler3;
	}

	public void setFiller3(String filler3) {
		this.filler3 = filler3;
	}

	public String getFiller4() {
		return filler4;
	}

	public void setFiller4(String filler4) {
		this.filler4 = filler4;
	}

	public String getFiller5() {
		return filler5;
	}

	public void setFiller5(String filler5) {
		this.filler5 = filler5;
	}

	public String getFiller6() {
		return filler6;
	}

	public void setFiller6(String filler6) {
		this.filler6 = filler6;
	}

	public String getFiller7() {
		return filler7;
	}

	public void setFiller7(String filler7) {
		this.filler7 = filler7;
	}

	public String getFiller8() {
		return filler8;
	}

	public void setFiller8(String filler8) {
		this.filler8 = filler8;
	}

	public String getExcess94NAmount() {
		return excess94NAmount;
	}

	public void setExcess94NAmount(String excess94nAmount) {
		excess94NAmount = excess94nAmount;
	}

	public String getFiller9() {
		return filler9;
	}

	public void setFiller9(String filler9) {
		this.filler9 = filler9;
	}

	public String getFiller10() {
		return filler10;
	}

	public void setFiller10(String filler10) {
		this.filler10 = filler10;
	}

	public String getFiller11() {
		return filler11;
	}

	public void setFiller11(String filler11) {
		this.filler11 = filler11;
	}

	public String getFiller12() {
		return filler12;
	}

	public void setFiller12(String filler12) {
		this.filler12 = filler12;
	}

	public String getDeducteeHash() {
		return deducteeHash;
	}

	public void setDeducteeHash(String deducteeHash) {
		this.deducteeHash = deducteeHash;
	}

	public String getIsDTAA() {
		return isDTAA;
	}

	public void setIsDTAA(String isDTAA) {
		this.isDTAA = isDTAA;
	}

	public String getNatureOfRemittance() {
		return natureOfRemittance;
	}

	public void setNatureOfRemittance(String natureOfRemittance) {
		this.natureOfRemittance = natureOfRemittance;
	}

	public String getUniqueAck15CA() {
		return uniqueAck15CA;
	}

	public void setUniqueAck15CA(String uniqueAck15CA) {
		this.uniqueAck15CA = uniqueAck15CA;
	}

	public String getCountryOfDeductee() {
		return countryOfDeductee;
	}

	public void setCountryOfDeductee(String countryOfDeductee) {
		this.countryOfDeductee = countryOfDeductee;
	}

	public String getEmailOfDeductee() {
		return emailOfDeductee;
	}

	public void setEmailOfDeductee(String emailOfDeductee) {
		this.emailOfDeductee = emailOfDeductee;
	}

	public String getContactNumberOfDeductee() {
		return contactNumberOfDeductee;
	}

	public void setContactNumberOfDeductee(String contactNumberOfDeductee) {
		this.contactNumberOfDeductee = contactNumberOfDeductee;
	}

	public String getAddressOfDeducteeInCountry() {
		return addressOfDeducteeInCountry;
	}

	public void setAddressOfDeducteeInCountry(String addressOfDeducteeInCountry) {
		this.addressOfDeducteeInCountry = addressOfDeducteeInCountry;
	}

	public String getTinOfDeductee() {
		return tinOfDeductee;
	}

	public void setTinOfDeductee(String tinOfDeductee) {
		this.tinOfDeductee = tinOfDeductee;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getDeductorMasterTan() {
		return deductorMasterTan;
	}

	public void setDeductorMasterTan(String deductorMasterTan) {
		this.deductorMasterTan = deductorMasterTan;
	}

}
