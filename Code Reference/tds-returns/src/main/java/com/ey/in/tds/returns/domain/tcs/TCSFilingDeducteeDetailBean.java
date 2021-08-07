package com.ey.in.tds.returns.domain.tcs;

import com.ey.in.tds.returns.domain.AbstractFilingBean;
import com.microsoft.sqlserver.jdbc.StringUtils;
import com.poiji.annotation.ExcelCell;

public class TCSFilingDeducteeDetailBean extends AbstractFilingBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8793476001121238143L;
	private String lineNo; // 1
	private String recType = "DD"; // 2
	private String ddBatchNo; // 3
	@ExcelCell(1)
	private String challanRecordNo; // 4
	private String collecteeDetailRecNo; // 5
	private String mode = "O"; // 6
	private String deducteeSerialNo = ""; // 7
	@ExcelCell(27)
	private String collecteeCode = "1"; // 8 // 1 Compnay, 2 Others
	private String lastCollecteePan = ""; // 9
	@ExcelCell(14)
	private String collecteePan; // 10 // or deductee PAN
	private String lastCollecteeRefNo; // 11
	private String collecteeRefNo; // 12

	private String collecteeName; // 13

	@ExcelCell(18)
	private String tcsIncomeTaxDD; // 14

	@ExcelCell(19)
	private String tcsSurchargeDD; // 15

	@ExcelCell(20)
	private String tcsCessDD; // 16

	@ExcelCell(21)
	private String totalIncomeTaxCollected; // 17

	private String lastTotalIncomeTaxCollectedAtSource; // 18

	@ExcelCell(23)
	private String totalTaxDeposited; // 19

	private String lastTotalTaxDeposited; // 20

	private String totalValueofTransaction = ""; // 21 // NA

	// payment details
	@ExcelCell(17)
	private String amountReceived; // 22
	@ExcelCell(16)
	private String dateOnWhichAmountReceived; // 23
	@ExcelCell(25)
	private String dateOnWhichTaxCollected; // 24
	private String dateOfDeposit = ""; // 25 // NA

	@ExcelCell(28)
	private String rateAtWhichTaxCollected; // 26
	@ExcelCell(31)
	private String grossingUpIndicator; // 27

	@ExcelCell(29)
	private String bookCashEntry; // 28 // One character

	private String dateOfFurnishingTaxDeductionCertificate; // 29

	private String isCollecteeNonResident; // 30

	private String isCollecteeHavingPermanentResidentInIndia;

	private String collectionCode;
	private String noOfCertificateIssued;

	private String isPaymentByCollecteeLiable;
	private String challanNumber;
	private String filter1;
	private String filter2;
	private String filter3;
	private String filter4;
	private String filter5;
	private String filter6;
	private String filter7;
	private String filter8;
	private String filter9;
	private String filter10;
	private String filter11;
	private String reason;

	// Non participating field
	private String formType;

	public String getFormType() {
		return formType;
	}

	public void setFormType(String formType) {
		this.formType = formType;
	}

	public TCSFilingDeducteeDetailBean() {

	}

	public TCSFilingDeducteeDetailBean(String formType) {
		this.setFormType(formType);
	}

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

	public String getCollecteeDetailRecNo() {
		return collecteeDetailRecNo;
	}

	public void setCollecteeDetailRecNo(String collecteeDetailRecNo) {
		this.collecteeDetailRecNo = collecteeDetailRecNo;
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

	public String getCollecteeCode() {
		return collecteeCode;
	}

	public void setCollecteeCode(String collecteeCode) {
		this.collecteeCode = collecteeCode;
	}

	public String getLastCollecteePan() {
		return lastCollecteePan;
	}

	public void setLastCollecteePan(String lastCollecteePan) {
		this.lastCollecteePan = lastCollecteePan;
	}

	public String getCollecteePan() {
		return collecteePan;
	}

	public void setCollecteePan(String collecteePan) {
		this.collecteePan = collecteePan;
	}

	public String getLastCollecteeRefNo() {
		return lastCollecteeRefNo;
	}

	public void setLastCollecteeRefNo(String lastCollecteeRefNo) {
		this.lastCollecteeRefNo = lastCollecteeRefNo;
	}

	public String getCollecteeRefNo() {
		return collecteeRefNo;
	}

	public void setCollecteeRefNo(String collecteeRefNo) {
		this.collecteeRefNo = collecteeRefNo;
	}

	public String getCollecteeName() {
		return collecteeName == null ? StringUtils.EMPTY : collecteeName;
	}

	public void setCollecteeName(String collecteeName) {
		this.collecteeName = collecteeName;
	}

	public String getTcsIncomeTaxDD() {
		return tcsIncomeTaxDD;
	}

	public void setTcsIncomeTaxDD(String tcsIncomeTaxDD) {
		this.tcsIncomeTaxDD = tcsIncomeTaxDD;
	}

	public String getTcsSurchargeDD() {
		return tcsSurchargeDD;
	}

	public void setTcsSurchargeDD(String tcsSurchargeDD) {
		this.tcsSurchargeDD = tcsSurchargeDD;
	}

	public String getTcsCessDD() {
		return tcsCessDD;
	}

	public void setTcsCessDD(String tcsCessDD) {
		this.tcsCessDD = tcsCessDD;
	}

	public String getTotalIncomeTaxCollected() {
		return totalIncomeTaxCollected;
	}

	public void setTotalIncomeTaxCollected(String totalIncomeTaxCollected) {
		this.totalIncomeTaxCollected = totalIncomeTaxCollected;
	}

	public String getLastTotalIncomeTaxCollectedAtSource() {
		return lastTotalIncomeTaxCollectedAtSource;
	}

	public void setLastTotalIncomeTaxCollectedAtSource(String lastTotalIncomeTaxCollectedAtSource) {
		this.lastTotalIncomeTaxCollectedAtSource = lastTotalIncomeTaxCollectedAtSource;
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

	public String getTotalValueofTransaction() {
		return totalValueofTransaction;
	}

	public void setTotalValueofTransaction(String totalValueofTransaction) {
		this.totalValueofTransaction = totalValueofTransaction;
	}

	public String getAmountReceived() {
		return amountReceived;
	}

	public void setAmountReceived(String amountReceived) {
		this.amountReceived = amountReceived;
	}

	public String getDateOnWhichAmountReceived() {
		return dateOnWhichAmountReceived;
	}

	public void setDateOnWhichAmountReceived(String dateOnWhichAmountReceived) {
		this.dateOnWhichAmountReceived = dateOnWhichAmountReceived;
	}

	public void setDateOnWhichTaxCollected(String dateOnWhichTaxCollected) {
		this.dateOnWhichTaxCollected = dateOnWhichTaxCollected;
	}

	public String getDateOfDeposit() {
		return dateOfDeposit;
	}

	public void setDateOfDeposit(String dateOfDeposit) {
		this.dateOfDeposit = dateOfDeposit;
	}

	public String getRateAtWhichTaxCollected() {
		return rateAtWhichTaxCollected;
	}

	public void setRateAtWhichTaxCollected(String rateAtWhichTaxCollected) {
		this.rateAtWhichTaxCollected = rateAtWhichTaxCollected;
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

	public String getIsCollecteeNonResident() {
		return isCollecteeNonResident;
	}

	public void setIsCollecteeNonResident(String isCollecteeNonResident) {
		this.isCollecteeNonResident = isCollecteeNonResident;
	}

	public String getIsCollecteeHavingPermanentResidentInIndia() {
		return isCollecteeHavingPermanentResidentInIndia;
	}

	public void setIsCollecteeHavingPermanentResidentInIndia(String isCollecteeHavingPermanentResidentInIndia) {
		this.isCollecteeHavingPermanentResidentInIndia = isCollecteeHavingPermanentResidentInIndia;
	}

	public String getCollectionCode() {
		return collectionCode;
	}

	public void setCollectionCode(String collectionCode) {
		this.collectionCode = collectionCode;
	}

	public String getNoOfCertificateIssued() {
		return noOfCertificateIssued;
	}

	public void setNoOfCertificateIssued(String noOfCertificateIssued) {
		this.noOfCertificateIssued = noOfCertificateIssued;
	}

	public String getIsPaymentByCollecteeLiable() {
		return isPaymentByCollecteeLiable;
	}

	public void setIsPaymentByCollecteeLiable(String isPaymentByCollecteeLiable) {
		this.isPaymentByCollecteeLiable = isPaymentByCollecteeLiable;
	}

	public String getChallanNumber() {
		return challanNumber;
	}

	public void setChallanNumber(String challanNumber) {
		this.challanNumber = challanNumber;
	}

	public String getFilter1() {
		return filter1;
	}

	public void setFilter1(String filter1) {
		this.filter1 = filter1;
	}

	public String getFilter2() {
		return filter2;
	}

	public void setFilter2(String filter2) {
		this.filter2 = filter2;
	}

	public String getFilter3() {
		return filter3;
	}

	public void setFilter3(String filter3) {
		this.filter3 = filter3;
	}

	public String getFilter4() {
		return filter4;
	}

	public void setFilter4(String filter4) {
		this.filter4 = filter4;
	}

	public String getFilter5() {
		return filter5;
	}

	public void setFilter5(String filter5) {
		this.filter5 = filter5;
	}

	public String getFilter6() {
		return filter6;
	}

	public void setFilter6(String filter6) {
		this.filter6 = filter6;
	}

	public String getFilter7() {
		return filter7;
	}

	public void setFilter7(String filter7) {
		this.filter7 = filter7;
	}

	public String getFilter8() {
		return filter8;
	}

	public void setFilter8(String filter8) {
		this.filter8 = filter8;
	}

	public String getFilter9() {
		return filter9;
	}

	public void setFilter9(String filter9) {
		this.filter9 = filter9;
	}

	public String getFilter10() {
		return filter10;
	}

	public void setFilter10(String filter10) {
		this.filter10 = filter10;
	}

	public String getFilter11() {
		return filter11;
	}

	public void setFilter11(String filter11) {
		this.filter11 = filter11;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getDateOnWhichTaxCollected() {
		return dateOnWhichTaxCollected;
	}

	@Override
	public String toString() {
		String value = getLineNo() + "^" // 1
				+ getRecType() + "^" // 2
				+ getDdBatchNo() + "^" // 3
				+ getChallanRecordNo() + "^" // 4
				+ getCollecteeDetailRecNo() + "^" // 5
				+ getMode() + "^" // 6
				+ getDeducteeSerialNo() + "^" // 7
				+ getCollecteeCode() + "^" // 8
				+ getLastCollecteePan() + "^" // 9
				+ getCollecteePan() + "^" // 10
				+ getLastCollecteeRefNo() + "^" // 11
				+ getCollecteeRefNo() + "^" // 12
				+ getCollecteeName() + "^" // 13
				+ getTcsIncomeTaxDD() + "^" // 14
				+ getTcsSurchargeDD() + "^" // 15
				+ getTcsCessDD() + "^" // 16
				+ getTotalIncomeTaxCollected() + "^" // 17
				+ getLastTotalIncomeTaxCollectedAtSource() + "^" // 18
				+ getTotalTaxDeposited() + "^" // 19
				+ getLastTotalTaxDeposited() + "^" // 20
				+ getTotalValueofTransaction() + "^" // 21
				+ getAmountReceived() + "^" // 22
				+ getDateOnWhichAmountReceived() + "^" // 23
				+ getDateOnWhichTaxCollected() + "^" // 24
				+ getDateOfDeposit() + "^" // 25
				+ getRateAtWhichTaxCollected() + "^" // 26
				+ getGrossingUpIndicator() + "^" // 27
				+ getBookCashEntry() + "^" // 28
				+ getDateOfFurnishingTaxDeductionCertificate() + "^" // 29
				+ getReason() + "^"// 30
				+ getIsCollecteeNonResident() + "^" // 31
				+ getIsCollecteeHavingPermanentResidentInIndia() + "^" // 32
				+ getCollectionCode() + "^" // 33
				+ getNoOfCertificateIssued() + "^" // 34
				+ getIsPaymentByCollecteeLiable() + "^"// 35
				+ getChallanNumber() + "^" // 36
				+ getFilter1() + "^" // 37
				+ getFilter2() + "^" // 38
				+ getFilter3() + "^" // 39
				+ getFilter4() + "^" // 40
				+ getFilter5() + "^" // 41
				+ getFilter6() + "^" // 42
				+ getFilter7() + "^" // 43
				+ getFilter8() + "^" // 44
				+ getFilter9() + "^" // 45
				+ getFilter10() + "^" // 46
				+ getFilter11() + "^" ;// 47
		return value;
	}

}
