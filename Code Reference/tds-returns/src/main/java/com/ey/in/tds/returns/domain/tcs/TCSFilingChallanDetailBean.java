package com.ey.in.tds.returns.domain.tcs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ey.in.tds.returns.domain.AbstractFilingBean;
import com.poiji.annotation.ExcelCell;

public class TCSFilingChallanDetailBean extends AbstractFilingBean{


	private static final long serialVersionUID = -5213605113555989485L;

	private int challanMonth;
	private String challanSection;
	private List<TCSFilingDeducteeDetailBean> deducteeDetailBeanList = new ArrayList<TCSFilingDeducteeDetailBean>();
	private Map<String, Integer> lengthMap = new HashMap<String, Integer>() {
		private static final long serialVersionUID = -2364998406902121001L;
		{
			put("lineNo", 9); // 1
			put("recType", 2); // 2
			put("chBatchNo", 9); // 3
			put("challanDetailRecordNo", 9); // 4
			put("countOfCollecteeDetail", 9); // 5
			put("nillChallanIndicator", 1); // 6
			put("challanUpdationIndicator", 0); // 7
			put("filler3", 0); // 8
			put("filler4", 0); // 9
			put("filler5", 0); // 10
			put("lastBankChallanNo", 0); // 11
			put("bankChallanNo", 5); // 12
			put("lastTransferVoucherNo", 0); // 13
			put("ddoSerialNumberForm24G", 9); // 14
			put("lastBankBranchCode", 0); // 15
			put("bankBranchCode", 7); // 16
			put("lastDateOfBankChallanNo", 0); // 17
			put("dateOfBankChallanNo", 8); // 18
			put("filler6", 0); // 19
			put("filler7", 0); // 20
			put("section", 0); // 21
			put("oltasTCSIncomeTax", 15); // 22
			put("oltasTCSSurcharge", 15); // 23
			put("oltasTCSCess", 15); // 24
			put("oltasTCSInterest", 15); // 25
			put("oltasTCSOthers", 15); // 26
			put("totalOfDepositAmountAsPerChallan", 15); // 27
			put("lastTotalOfDepositAmountAsPerChallan", 0); // 28
			put("totalTaxDepositedAsPerCollecteeAnex", 15); // 29
			put("tcsIncomeTaxC", 15); // 30
			put("tcsSurchargeC", 15); // 31
			put("tcsCessC", 15); // 32
			put("sumTotalIncTaxDeductedAtSource", 15); // 33
			put("tdsInterest", 15); // 34
			put("tdsOthers", 15); // 35
			put("chequeDDNo", 15); // 36
			put("bookCash", 1); // 37
			put("remark", 14); // 38
			put("lateFee", 15); // 39
			put("minorHeadCodeChallan", 3); // 40
			put("challanHash", 0); // 41
		}
	};

	@ExcelCell(1)
	private String lineNo; // 1
	private String recType = "CD"; // 2
	private String chBatchNo; // 3
	private String challanDetailRecordNo; // 4
	private String countOfCollecteeDetail; // 5
	private String nillChallanIndicator; // 6
	private String challanUpdationIndicator; // 7
	private String filler3; // 8
	private String filler4; // 9
	private String filler5; // 1
	
	@ExcelCell(17)
	private String lastBankChallanNo; // 11
	@ExcelCell(18)
	private String bankChallanNo; // 12
	private String lastTransferVoucherNo; // 13
	private String ddoSerialNumberForm24G; // 14
	@ExcelCell(13)
	private String lastBankBranchCode; // 15
	@ExcelCell(14)
	private String bankBranchCode; // 16
	@ExcelCell(15)
	private String lastDateOfBankChallanNo; // 
	@ExcelCell(16)
	private String dateOfBankChallanNo;
	private String filler6; // 19
	private String filler7; // 20
	@ExcelCell(3)
	private String section;
	private String oltasTCSIncomeTax; // 22
	private String oltasTCSSurcharge; // 23
	private String oltasTCSCess; // 24
	private String oltasTCSInterest; // 25
	private String oltasTCSOthers; // 26
	@ExcelCell(11)
	private String totalOfDepositAmountAsPerChallan; // 27

	@ExcelCell(10)
	private String lastTotalOfDepositAmountAsPerChallan; // 28
	private String totalTaxDepositedAsPerCollecteeAnex; // 29
	
	@ExcelCell(4)
	private String tcsIncomeTaxC; // 30
	@ExcelCell(5)
	private String tcsSurchargeC; // 31
	@ExcelCell(6)
	private String tcsCessC; // 32
	// calculate total for three challan
	private String sumTotalIncTaxDeductedAtSource; // 33
	@ExcelCell(7)
	private String tdsInterest; // 34
	@ExcelCell(8)
	private String tdsOthers; // 35
	@ExcelCell(12)
	private String chequeDDNo; // 36
	private String bookCash; // 37

	private String remark; // 38
	@ExcelCell(8)
	private String lateFee; // 39
	private String minorHeadCodeChallan; // 40
	private String challanHash; // 41
	
	
	public List<TCSFilingDeducteeDetailBean> getDeducteeDetailBeanList() {
		return deducteeDetailBeanList;
	}

	public void setDeducteeDetailBeanList(List<TCSFilingDeducteeDetailBean> deducteeDetailBeanList) {
		this.deducteeDetailBeanList = deducteeDetailBeanList;
	}

	public String getLineNo() {
		return trim(getNullSafeValue(this.lineNo), lengthMap.get("lineNo"));
	}

	public void setLineNo(String lineNo) {
		this.lineNo = lineNo;
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

	public String getDdoSerialNumberForm24G() {
		return trim(getNullSafeValue(this.ddoSerialNumberForm24G), lengthMap.get("ddoSerialNumberForm24G"));
	}

	public void setDdoSerialNumberForm24G(String ddoSerialNumberForm24G) {
		this.ddoSerialNumberForm24G = ddoSerialNumberForm24G;
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

	public String getRecType() {
		return trim(getNullSafeValue(this.recType), lengthMap.get("recType"));
	}

	public void setRecType(String recType) {
		this.recType = recType;
	}

	public String getChBatchNo() {
		return trim(getNullSafeValue(this.chBatchNo), lengthMap.get("chBatchNo"));
	}

	public void setChBatchNo(String cHBatchNo) {
		this.chBatchNo = cHBatchNo;
	}

	public String getChallanDetailRecordNo() {
		return trim(getNullSafeValue(this.challanDetailRecordNo), lengthMap.get("challanDetailRecordNo"));
	}

	public void setChallanDetailRecordNo(String challanDetailRecordNo) {
		this.challanDetailRecordNo = challanDetailRecordNo;
	}

	public String getCountOfCollecteeDetail() {
		return trim(getNullSafeValue(this.countOfCollecteeDetail), lengthMap.get("countOfCollecteeDetail"));
	}

	public void setCountOfCollecteeDetail(String countOfCollecteeDetail) {
		this.countOfCollecteeDetail = countOfCollecteeDetail;
	}

	public String getNillChallanIndicator() {
		return trim(getNullSafeValue(this.nillChallanIndicator), lengthMap.get("nillChallanIndicator"));
	}

	public void setNillChallanIndicator(String nillChallanIndicator) {
		this.nillChallanIndicator = nillChallanIndicator;
	}

	public String getChallanUpdationIndicator() {
		return trim(getNullSafeValue(this.challanUpdationIndicator), lengthMap.get("challanUpdationIndicator"));
	}

	public void setChallanUpdationIndicator(String challanUpdationIndicator) {
		this.challanUpdationIndicator = challanUpdationIndicator;
	}

	public String getLastBankChallanNo() {
		return trim(getNullSafeValue(this.lastBankChallanNo), lengthMap.get("lastBankChallanNo"));
	}

	public void setLastBankChallanNo(String lastBankChallanNo) {
		this.lastBankChallanNo = lastBankChallanNo;
	}

	public String getBankChallanNo() {
		return trim(getNullSafeValue(this.bankChallanNo), lengthMap.get("bankChallanNo"));
	}

	public void setBankChallanNo(String bankChallanNo) {
		this.bankChallanNo = bankChallanNo;
	}

	public String getLastTransferVoucherNo() {
		return trim(getNullSafeValue(this.lastTransferVoucherNo), lengthMap.get("lastTransferVoucherNo"));
	}

	public void setLastTransferVoucherNo(String lastTransferVoucherNo) {
		this.lastTransferVoucherNo = lastTransferVoucherNo;
	}

	public String getLastBankBranchCode() {
		return trim(getNullSafeValue(this.lastBankBranchCode), lengthMap.get("lastBankBranchCode"));
	}

	public void setLastBankBranchCode(String lastBankBranchCode) {
		this.lastBankBranchCode = lastBankBranchCode;
	}

	public String getBankBranchCode() {
		return trim(getNullSafeValue(this.bankBranchCode), lengthMap.get("bankBranchCode"));
	}

	public void setBankBranchCode(String bankBranchCode) {
		this.bankBranchCode = bankBranchCode;
	}

	public String getLastDateOfBankChallanNo() {
		return trim(getNullSafeValue(this.lastDateOfBankChallanNo), lengthMap.get("lastDateOfBankChallanNo"));
	}

	public void setLastDateOfBankChallanNo(String lastDateOfBankChallanNo) {
		this.lastDateOfBankChallanNo = lastDateOfBankChallanNo;
	}

	public String getDateOfBankChallanNo() {
		return trim(cleanseData(getNullSafeValue(this.dateOfBankChallanNo)), lengthMap.get("dateOfBankChallanNo"));
	}

	public void setDateOfBankChallanNo(String dateOfBankChallanNo) {
		this.dateOfBankChallanNo = dateOfBankChallanNo;
	}

	public String getSection() {
		return trim(getNullSafeValue(this.section), lengthMap.get("section"));
	}

	public void setSection(String section) {
		this.section = section;
	}

	public String getOltasTCSIncomeTax() {
		return trim(getNullSafeValue(this.oltasTCSIncomeTax), lengthMap.get("oltasTCSIncomeTax"));
	}

	public void setOltasTCSIncomeTax(String oltasTCSIncomeTax) {
		this.oltasTCSIncomeTax = oltasTCSIncomeTax;
	}

	public String getOltasTCSSurcharge() {
		return trim(getNullSafeValue(this.oltasTCSSurcharge), lengthMap.get("oltasTCSSurcharge"));
	}

	public void setOltasTCSSurcharge(String oltasTCSSurcharge) {
		this.oltasTCSSurcharge = oltasTCSSurcharge;
	}

	public String getOltasTCSCess() {
		return trim(getNullSafeValue(this.oltasTCSCess), lengthMap.get("oltasTCSCess"));
	}

	public void setOltasTCSCess(String oltasTCSCess) {
		this.oltasTCSCess = oltasTCSCess;
	}

	public String getOltasTCSInterest() {
		return trim(getNullSafeValue(this.oltasTCSInterest), lengthMap.get("oltasTCSInterest"));
	}

	public void setOltasTCSInterest(String oltasTCSInterest) {
		this.oltasTCSInterest = oltasTCSInterest;
	}

	public String getOltasTCSOthers() {
		return trim(getNullSafeValue(this.oltasTCSOthers), lengthMap.get("oltasTCSOthers"));
	}

	public void setOltasTCSOthers(String oltasTCSOthers) {
		this.oltasTCSOthers = oltasTCSOthers;
	}

	public String getTotalOfDepositAmountAsPerChallan() {
		return trim(getNullSafeValue(this.totalOfDepositAmountAsPerChallan),
				lengthMap.get("totalOfDepositAmountAsPerChallan"));
	}

	public void setTotalOfDepositAmountAsPerChallan(String totalOfDepositAmountAsPerChallan) {
		this.totalOfDepositAmountAsPerChallan = totalOfDepositAmountAsPerChallan;
	}

	public String getLastTotalOfDepositAmountAsPerChallan() {
		return trim(getNullSafeValue(this.lastTotalOfDepositAmountAsPerChallan),
				lengthMap.get("lastTotalOfDepositAmountAsPerChallan"));
	}

	public void setLastTotalOfDepositAmountAsPerChallan(String lastTotalOfDepositAmountAsPerChallan) {
		this.lastTotalOfDepositAmountAsPerChallan = lastTotalOfDepositAmountAsPerChallan;
	}

	public String getTotalTaxDepositedAsPerCollecteeAnex() {
		return trim(getNullSafeValue(this.totalTaxDepositedAsPerCollecteeAnex),
				lengthMap.get("totalTaxDepositedAsPerCollecteeAnex"));
	}

	public void setTotalTaxDepositedAsPerCollecteeAnex(String totalTaxDepositedAsPerCollecteeAnex) {
		this.totalTaxDepositedAsPerCollecteeAnex = totalTaxDepositedAsPerCollecteeAnex;
	}

	public String getTcsIncomeTaxC() {
		return trim(getNullSafeValue(this.tcsIncomeTaxC), lengthMap.get("tcsIncomeTaxC"));
	}

	public void setTcsIncomeTaxC(String tcsIncomeTaxC) {
		this.tcsIncomeTaxC = tcsIncomeTaxC;
	}

	public String getTcsSurchargeC() {
		return trim(getNullSafeValue(this.tcsSurchargeC), lengthMap.get("tcsSurchargeC"));
	}

	public void setTcsSurchargeC(String tcsSurchargeC) {
		this.tcsSurchargeC = tcsSurchargeC;
	}

	public String getTcsCessC() {
		return trim(getNullSafeValue(this.tcsCessC), lengthMap.get("tcsCessC"));
	}

	public void setTcsCessC(String tcsCessC) {
		this.tcsCessC = tcsCessC;
	}

	public String getSumTotalIncTaxDeductedAtSource() {
		return trim(getNullSafeValue(this.sumTotalIncTaxDeductedAtSource), lengthMap.get("sumTotalIncTaxDeductedAtSource"));
	}

	public void setSumTotalIncTaxDeductedAtSource(String sumTotalIncTaxDeductedAtSource) {
		this.sumTotalIncTaxDeductedAtSource = sumTotalIncTaxDeductedAtSource;
	}

	public String getTdsInterest() {
		return trim(getNullSafeValue(this.tdsInterest), lengthMap.get("tdsInterest"));
	}

	public void setTdsInterest(String tdsInterest) {
		this.tdsInterest = tdsInterest;
	}

	public String getTdsOthers() {
		return trim(getNullSafeValue(this.tdsOthers), lengthMap.get("tdsOthers"));
	}

	public void setTdsOthers(String tdsOthers) {
		this.tdsOthers = tdsOthers;
	}

	public String getChequeDDNo() {
		return trim(getNullSafeValue(this.chequeDDNo), lengthMap.get("chequeDDNo"));
	}

	public void setChequeDDNo(String chequeDDNo) {
		this.chequeDDNo = chequeDDNo;
	}

	public String getBookCash() {
		return trim(getNullSafeValue(this.bookCash), lengthMap.get("bookCash"));
	}

	public void setBookCash(String bookCash) {
		this.bookCash = bookCash;
	}

	public String getRemark() {
		return trim(getNullSafeValue(this.remark), lengthMap.get("remark"));
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getChallanHash() {
		return trim(getNullSafeValue(this.challanHash), lengthMap.get("challanHash"));
	}

	public void setChallanHash(String challanHash) {
		this.challanHash = challanHash;
	}

	public String getLateFee() {
		return trim(getNullSafeValue(this.lateFee), lengthMap.get("lateFee"));
	}

	public void setLateFee(String lateFee) {
		this.lateFee = lateFee;
	}

	public String getMinorHeadCodeChallan() {
		return trim(getNullSafeValue(this.minorHeadCodeChallan), lengthMap.get("minorHeadCodeChallan"));
	}

	public void setMinorHeadCodeChallan(String minorHeadCodeChallan) {
		this.minorHeadCodeChallan = minorHeadCodeChallan;
	}
	public String getChallanSection() {
		return challanSection;
	}

	public void setChallanSection(String challanSection) {
		this.challanSection = challanSection;
	}
	public int getChallanMonth() {
		return challanMonth;
	}

	public void setChallanMonth(int challanMonth) {
		this.challanMonth = challanMonth;
	}
	
	//constructors
	public TCSFilingChallanDetailBean() {

	}

	public TCSFilingChallanDetailBean(int assessmentMonth, String challanSection) {
		this.challanMonth = assessmentMonth;
		this.challanSection = challanSection;
	}
	
	@Override
	public String toString() {
		return getLineNo() + "^" // 1
				+ getRecType() + "^" // 2
				+ getChBatchNo() + "^" // 3
				+ getChallanDetailRecordNo() + "^" // 4
				+ getCountOfCollecteeDetail() + "^" // 5
				+ getNillChallanIndicator() + "^" // 6
				+ getChallanUpdationIndicator() + "^" // 7
				+ getFiller3() + "^" // 8
				+ getFiller4() + "^" // 9
				+ getFiller5() + "^" // 10
				+ getLastBankChallanNo() + "^" // 11
				+ getBankChallanNo() + "^" // 12
				+ getLastTransferVoucherNo() + "^" // 13
				+ getDdoSerialNumberForm24G() + "^" // 14
				+ getLastBankBranchCode() + "^" // 15
				+ getBankBranchCode() + "^" // 16
				+ getLastDateOfBankChallanNo() + "^" // 17
				+ getDateOfBankChallanNo() + "^" // 18
				+ getFiller6() + "^" // 19
				+ getFiller7() + "^" // 20
				+ getSection() + "^" // 21
				+ getOltasTCSIncomeTax() + "^" // 22
				+ getOltasTCSSurcharge() + "^" // 23
				+ getOltasTCSCess() + "^" // 24
				+ getOltasTCSInterest() + "^" // 25
				+ getOltasTCSOthers() + "^" // 26
				+ getTotalOfDepositAmountAsPerChallan() + "^" // 27
				+ getLastTotalOfDepositAmountAsPerChallan() + "^" // 28
				+ getTotalTaxDepositedAsPerCollecteeAnex() + "^" // 29
				+ getTcsIncomeTaxC() + "^" // 30
				+ getTcsSurchargeC() + "^" // 31
				+ getTcsCessC() + "^" // 32
				+ getSumTotalIncTaxDeductedAtSource() + "^" // 33
				+ getTdsInterest() + "^" // 34
				+ getTdsOthers() + "^" // 35
				+ getChequeDDNo() + "^" // 36
				+ getBookCash() + "^" // 37
				+ getRemark() + "^" // 38
				+ getLateFee() + "^" // 39
				+ getMinorHeadCodeChallan() + "^" // 40
				+ getChallanHash();// 41
	}

}
