package com.ey.in.tds.returns.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.poiji.annotation.ExcelCell;

/***
 * 
 * @author Admin Bean for TDS Statement for Non Salary category (Challan /
 *         Transfer Voucher Detail Record)
 */
public class FilingChallanDetailBean extends AbstractFilingBean {

	private static final long serialVersionUID = 2671080476063138403L;

	private int challanMonth;
	private String challanSection;
	private List<FilingDeducteeDetailBean> deducteeDetailBeanList = new ArrayList<FilingDeducteeDetailBean>();
	private Map<String, Integer> lengthMap = new HashMap<String, Integer>() {
		private static final long serialVersionUID = -2364998406902121001L;
		{
			put("lineNo", 9); // 1
			put("recType", 2); // 2
			put("chBatchNo", 9); // 3
			put("challanDetailRecordNo", 9); // 4
			put("countOfDeducteeDetail", 9); // 5
			put("nillChallanIndicator", 1); // 6
			put("challanUpdationIndicator", 1); // 7
			put("filler3", 0); // 8
			put("filler4", 0); // 9
			put("filler5", 0); // 10
			put("lastBankChallanNo", 5); // 11
			put("bankChallanNo", 5); // 12
			put("lastTransferVoucherNo", 0); // 13
			put("ddoSerialNumberForm24G", 9); // 14
			put("lastBankBranchCode", 7); // 15
			put("bankBranchCode", 7); // 16
			put("lastDateOfBankChallanNo", 8); // 17
			put("dateOfBankChallanNo", 8); // 18
			put("filler6", 0); // 19
			put("filler7", 0); // 20
			put("section", 0); // 21
			put("oltasIncomeTax", 15); // 22
			put("oltasSurcharge", 15); // 23
			put("oltasCess", 15); // 24
			put("oltasInterest", 15); // 25
			put("oltasOthers", 15); // 26
			put("totalOfDepositAmountAsPerChallan", 15); // 27
			put("lastTotalOfDepositAmountAsPerChallan", 15); // 28
			put("totalTaxDepositedAsPerDeducteeAnex", 15); // 29
			put("tdsIncomeTaxC", 15); // 30
			put("tdsSurchargeC", 15); // 31
			put("tdsCessC", 15); // 32
			put("sumTotalIncTaxDedAtSource", 15); // 33
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

	public FilingChallanDetailBean() {

	}

	public FilingChallanDetailBean(int assessmentMonth, String challanSection) {
		this.challanMonth = assessmentMonth;
		this.challanSection = challanSection;
	}

	/*
	 * Fields from challan table
	 */
	@ExcelCell(1)
	private String lineNo; // 1
	private String recType = "CD"; // 2
	private String chBatchNo; // 3
	private String challanDetailRecordNo; // 4
	private String countOfDeducteeDetail; // 5
	private String nillChallanIndicator; // 6
	private String challanUpdationIndicator; // 7
	private String filler3; // 8
	private String filler4; // 9
	private String filler5; // 10

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
	private String lastDateOfBankChallanNo; // 17
	@ExcelCell(16)
	private String dateOfBankChallanNo; // 18
	private String filler6; // 19
	private String filler7; // 20
	@ExcelCell(3)
	private String section; // 21

	private String oltasIncomeTax; // 22
	private String oltasSurcharge; // 23
	private String oltasCess; // 24
	private String oltasInterest; // 25
	private String oltasOthers; // 26

	@ExcelCell(11)
	private String totalOfDepositAmountAsPerChallan; // 27

	@ExcelCell(10)
	private String lastTotalOfDepositAmountAsPerChallan; // 28
	private String totalTaxDepositedAsPerDeducteeAnex; // 29

	@ExcelCell(4)
	private String tdsIncomeTaxC; // 30
	@ExcelCell(5)
	private String tdsSurchargeC; // 31
	@ExcelCell(6)
	private String tdsCessC; // 32

	// calculate total for three challan
	private String sumTotalIncTaxDedAtSource; // 33

	@ExcelCell(7)
	private String tdsInterest; // 34
	@ExcelCell(8)
	private String tdsOthers; // 35

	// payment fields
	@ExcelCell(12)
	private String chequeDDNo; // 36
	private String bookCash; // 37

	private String remark; // 38
	@ExcelCell(8)
	private String lateFee; // 39
	private String minorHeadCodeChallan; // 40
	private String challanHash; // 41

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

	public String getCountOfDeducteeDetail() {
		return trim(getNullSafeValue(this.countOfDeducteeDetail), lengthMap.get("countOfDeducteeDetail"));
	}

	public void setCountOfDeducteeDetail(String countOfDeducteeDetail) {
		this.countOfDeducteeDetail = countOfDeducteeDetail;
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
		return trim(cleanseData(getNullSafeValue(this.lastDateOfBankChallanNo)), lengthMap.get("lastDateOfBankChallanNo"));
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

	public String getOltasIncomeTax() {
		return trim(getNullSafeValue(this.oltasIncomeTax), lengthMap.get("oltasIncomeTax"));
	}

	public void setOltasIncomeTax(String oltasIncomeTax) {
		this.oltasIncomeTax = oltasIncomeTax;
	}

	public String getOltasSurcharge() {
		return trim(getNullSafeValue(this.oltasSurcharge), lengthMap.get("oltasSurcharge"));
	}

	public void setOltasSurcharge(String oltasSurcharge) {
		this.oltasSurcharge = oltasSurcharge;
	}

	public String getOltasCess() {
		return trim(getNullSafeValue(this.oltasCess), lengthMap.get("oltasCess"));
	}

	public void setOltasCess(String oltasCess) {
		this.oltasCess = oltasCess;
	}

	public String getOltasInterest() {
		return trim(getNullSafeValue(this.oltasInterest), lengthMap.get("oltasInterest"));
	}

	public void setOltasInterest(String oltasInterest) {
		this.oltasInterest = oltasInterest;
	}

	public String getOltasOthers() {
		return trim(getNullSafeValue(this.oltasOthers), lengthMap.get("oltasOthers"));
	}

	public void setOltasOthers(String oltasOthers) {
		this.oltasOthers = oltasOthers;
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

	public String getTotalTaxDepositedAsPerDeducteeAnex() {
		return trim(getNullSafeValue(this.totalTaxDepositedAsPerDeducteeAnex),
				lengthMap.get("totalTaxDepositedAsPerDeducteeAnex"));
	}

	public void setTotalTaxDepositedAsPerDeducteeAnex(String totalTaxDepositedAsPerDeducteeAnex) {
		this.totalTaxDepositedAsPerDeducteeAnex = totalTaxDepositedAsPerDeducteeAnex;
	}

	public String getTdsIncomeTaxC() {
		return trim(getNullSafeValue(this.tdsIncomeTaxC), lengthMap.get("tdsIncomeTaxC"));
	}

	public void setTdsIncomeTaxC(String tdsIncomeTaxC) {
		this.tdsIncomeTaxC = tdsIncomeTaxC;
	}

	public String getTdsSurchargeC() {
		return trim(getNullSafeValue(this.tdsSurchargeC), lengthMap.get("tdsSurchargeC"));
	}

	public void setTdsSurchargeC(String tdsSurchargeC) {
		this.tdsSurchargeC = tdsSurchargeC;
	}

	public String getTdsCessC() {
		return trim(getNullSafeValue(this.tdsCessC), lengthMap.get("tdsCessC"));
	}

	public void setTdsCessC(String tdsCessC) {
		this.tdsCessC = tdsCessC;
	}

	public String getSumTotalIncTaxDedAtSource() {
		return trim(getNullSafeValue(this.sumTotalIncTaxDedAtSource), lengthMap.get("sumTotalIncTaxDedAtSource"));
	}

	public void setSumTotalIncTaxDedAtSource(String sumTotalIncTaxDedAtSource) {
		this.sumTotalIncTaxDedAtSource = sumTotalIncTaxDedAtSource;
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

	@Override
	public String toString() {
		return getLineNo() + "^" // 1
				+ getRecType() + "^" // 2
				+ getChBatchNo() + "^" // 3
				+ getChallanDetailRecordNo() + "^" // 4
				+ getCountOfDeducteeDetail() + "^" // 5
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
				+ getOltasIncomeTax() + "^" // 22
				+ getOltasSurcharge() + "^" // 23
				+ getOltasCess() + "^" // 24
				+ getOltasInterest() + "^" // 25
				+ getOltasOthers() + "^" // 26
				+ getTotalOfDepositAmountAsPerChallan() + "^" // 27
				+ getLastTotalOfDepositAmountAsPerChallan() + "^" // 28
				+ getTotalTaxDepositedAsPerDeducteeAnex() + "^" // 29
				+ getTdsIncomeTaxC() + "^" // 30
				+ getTdsSurchargeC() + "^" // 31
				+ getTdsCessC() + "^" // 32
				+ getSumTotalIncTaxDedAtSource() + "^" // 33
				+ getTdsInterest() + "^" // 34
				+ getTdsOthers() + "^" // 35
				+ getChequeDDNo() + "^" // 36
				+ getBookCash() + "^" // 37
				+ getRemark() + "^" // 38
				+ getLateFee() + "^" // 39
				+ getMinorHeadCodeChallan() + "^" // 40
				+ getChallanHash();// 41
	}

	public List<FilingDeducteeDetailBean> getDeducteeDetailBeanList() {
		return deducteeDetailBeanList;
	}

	public void setDeducteeDetailBeanList(List<FilingDeducteeDetailBean> deducteeDetailBeanList) {
		this.deducteeDetailBeanList = deducteeDetailBeanList;
	}

	public int getChallanMonth() {
		return challanMonth;
	}

	public void setChallanMonth(int challanMonth) {
		this.challanMonth = challanMonth;
	}

	public String getChallanSection() {
		return challanSection;
	}

	public void setChallanSection(String challanSection) {
		this.challanSection = challanSection;
	}

	@Override
	public int hashCode() {
		return Objects.hash(bankChallanNo, challanMonth, challanSection);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FilingChallanDetailBean other = (FilingChallanDetailBean) obj;
		return Objects.equals(bankChallanNo, other.bankChallanNo) && challanMonth == other.challanMonth
				&& Objects.equals(challanSection, other.challanSection);
	}
	public static String getStringValue() {
		return "lineNo" + "^" // 1
		+ "recType" + "^" // 2
		+ "chBatchNo" + "^" // 3
		+ "challanDetailRecordNo" + "^" // 4
		+ "countOfDeducteeDetail" + "^" // 5
		+ "nillChallanIndicator" + "^" // 6
		+ "challanUpdationIndicator" + "^" // 7
		+ "filler" + "^" // 8
		+ "filler4" + "^" // 9
		+ "filler5" + "^" // 10
		+ "lastBankChallanNo" + "^" // 11
		+ "bankChallanNo" + "^" // 12
		+ "lastTransferVoucherNo" + "^" // 13
		+ "ddoSerialNumberForm24G" + "^" // 14
		+ "lastBankBranchCode" + "^" // 15
		+ "bankBranchCode" + "^" // 16
		+  "lastDateOfBankChallanNo"   + "^" // 17
		+  "dateOfBankChallanNo"   + "^" // 18
		+  "filler6"   + "^" // 19
		+  "filler7"   + "^" // 20
		+  "section"   + "^" // 21
		+  "oltasIncomeTax"   + "^" // 22
		+  "oltasSurcharge"   + "^" // 23
		+  "oltasCess"   + "^" // 24
		+  "oltasInterest"   + "^" // 25
		+  "oltasOthers"   + "^" // 26
		+  "totalOfDepositAmountAsPerChallan"   + "^" // 27
		+  "lastTotalOfDepositAmountAsPerChallan"   + "^" // 28
		+  "totalTaxDepositedAsPerDeducteeAnex"   + "^" // 29
		+  "tdsIncomeTaxC"   + "^" // 30
		+  "tdsSurchargeC"   + "^" // 31
		+  "tdsCessC"   + "^" // 32
		+  "sumTotalIncTaxDedAtSource"   + "^" // 33
		+  "tdsInterest"   + "^" // 34
		+  "tdsOthers"   + "^" // 35
		+  "chequeDDNo"   + "^" // 36
		+  "bookCash"   + "^" // 37
		+  "remark"   + "^" // 38
		+  "lateFee"   + "^" // 39
		+  "minorHeadCodeChallan"   + "^" // 40
		+  "challanHash"  ;// 41
	}

}
