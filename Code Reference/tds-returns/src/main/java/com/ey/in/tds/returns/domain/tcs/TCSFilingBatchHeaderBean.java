package com.ey.in.tds.returns.domain.tcs;

import java.util.HashMap;
import java.util.Map;

import com.ey.in.tds.returns.domain.AbstractFilingBean;

public class TCSFilingBatchHeaderBean extends AbstractFilingBean{

	private static final long serialVersionUID = -3036802574110902245L;
	private String lineNo;// 1
	private String recordType = "BH";// 2
	private String batchNo;// 3
	private String challanCount;// 4
	private String formNo = "27Q";// 5
	private String transactionType;// 6
	private String batchUpdationIndicator;// 7
	private String originalRrrNo;// 8
	private String previousRrrNo;// 9
	private String rrrNo;// 10
	private String rrrDate;// 11
	private String lastTanOfCollector;// 12
	private String tanOfCollector;// 13
	private String receiptNumber;// 14
	private String panOfCollector;// 15
	private String assessmentYr;// 16
	private String financialYr;// 17
	private String period;// 18
	private String employerName;// 19
	private String employerBranchDiv;// 20
	private String employerAddr1;// 21
	private String employerAddr2;// 22
	private String employerAddr3;// 23
	private String employerAddr4;// 24
	private String employerAddr5;// 25
	private String employerState;// 26
	private String employerPin;// 27
	private String employerEmail;// 28
	private String employerStd;// 29
	private String employerPhone;// 30
	private String employerAddrChange;// 31
	private String collectorType;// 32
	private String nameofPersonResponsilbleForTaxCollection;// 33
	private String designationofPersonResponsilbleForTaxCollection;// 34
	private String personResponsilbleAddr1;// 35
	private String personResponsilbleAddr2;// 36
	private String personResponsilbleAddr3;// 37
	private String personResponsilbleAddr4;// 38
	private String personResponsilbleAddr5;// 39
	private String personResponsilbleState;// 40
	private String personResponsilblePin;// 41
	private String personResponsilbleEmailId1;// 42
	private String mobileNumber;// 43
	private String personResponsilbleSTDCode;// 44
	private String personResponsilbleTelePhone;// 45
	private String personResponsilbleAddrChange;// 46
	private String grossTdsTotalAsPerChallan;// 47
	private String unMatchedChalanCnt;// 48
	private String countOfSalaryDetailRec;// 49
	private String grossTotalIncomeSd;// 50
	private String approvalTaken;// 51

	private String isRegularStatementForForm27EQFiledForEArlierPeriod; // 52
	private String lastCollectorType;// 53
	private String stateName;// 54
	private String paoCode;// 55
	private String ddoCode;// 56
	private String ministryName;// 57
	private String ministryNameOther;// 58
	private String pANOfResponsiblePerson;// 59
	private String paoRegistrationNo;// 60
	private String ddoRegistrationNo;// 61
	private String employerSTDAlt;// 62
	private String employerPhoneAlt;// 63
	private String employerEmailAlt;// 64
	private String personResponsilbleSTDCodeAlt;// 65
	private String personResponsilbleTelePhoneAlt;// 66
	private String personResponsilbleEmailIdAlt;// 67
	private String aIN;// 68
	private String gSTN;// 69
	private String batchHash;// 70
	
	private Map<String, Integer> lengthMap = new HashMap<String, Integer>() {
		private static final long serialVersionUID = 565827135117686547L;
		{
			put("lineNo", 9);// 1
			put("recordType", 2);// 2
			put("batchNo", 9);// 3
			put("challanCount", 9);// 4
			put("formNo", 4);// 5
			put("transactionType", 0);// 6
			put("batchUpdationIndicator", 0);// 7
			put("originalRrrNo", 0);// 8
			put("previousRrrNo", 15);// 9
			put("rrrNo", 0);// 10
			put("rrrDate", 0);// 11
			put("lastTanOfCollector", 0);// 12
			put("tanOfCollector", 10);// 13
			put("receiptNumber", 0);// 14
			put("panOfCollector", 10);// 15
			put("assessmentYr", 6);// 16
			put("financialYr", 6);// 17
			put("period", 2);// 18
			put("employerName", 75);// 19
			put("employerBranchDiv", 75);// 20
			put("employerAddr1", 25);// 21
			put("employerAddr2", 25);// 22
			put("employerAddr3", 25);// 23
			put("employerAddr4", 25);// 24
			put("employerAddr5", 25);// 25
			put("employerState", 2);// 26
			put("employerPin", 6);// 27
			put("employerEmail", 75);// 28
			put("employerStd", 5);// 29
			put("employerPhone", 10);// 30
			put("employerAddrChange", 1);// 31
			put("collectorType", 1);// 32
			put("nameofPersonResponsilbleForSal", 75);// 33
			put("designationofPersonResponsilbleForSal", 20);// 34
			put("personResponsilbleAddr1", 25);// 35
			put("personResponsilbleAddr2", 25);// 36
			put("personResponsilbleAddr3", 25);// 37
			put("personResponsilbleAddr4", 25);// 38
			put("personResponsilbleAddr5", 25);// 39
			put("personResponsilbleState", 2);// 40
			put("personResponsilblePin", 6);// 41
			put("personResponsilbleEmailId1", 75);// 42
			put("mobileNumber", 10);// 43
			put("personResponsilbleSTDCode", 5);// 44
			put("personResponsilbleTelePhone", 10);// 45
			put("personResponsilbleAddrChange", 1);// 46
			put("grossTdsTotalAsPerChallan", 15);// 47
			put("unMatchedChalanCnt", 9);// 48
			put("countOfSalaryDetailRec", 0);// 49
			put("grossTotalIncomeSd", 0);// 50
			put("approvalTaken", 1);// 51
			put("approvalNo", 1); // 52
			put("lastCollectorType", 1);// 53
			put("stateName", 2);// 54
			put("paoCode", 20);// 55
			put("ddoCode", 20);// 56
			put("ministryName", 3);// 57
			put("ministryNameOther", 150);// 58
			put("pANOfResponsiblePerson", 10);// 59
			put("paoRegistrationNo", 7);// 60
			put("ddoRegistrationNo", 10);// 61
			put("employerSTDAlt", 5);// 62
			put("employerPhoneAlt", 10);// 63
			put("employerEmailAlt", 75);// 64
			put("personResponsilbleSTDCodeAlt", 5);// 65
			put("personResponsilbleTelePhoneAlt", 10);// 66
			put("personResponsilbleEmailIdAlt", 75);// 67
			put("aIN", 7);// 68
			put("gSTN", 15);// 69
			put("batchHash", 0);// 70
		}
	};
	
	public String getLineNo() {
		return trim(getNullSafeValue(this.lineNo), lengthMap.get("lineNo"));
	}

	public void setLineNo(String lineNo) {
		this.lineNo = lineNo;
	}

	public String getRecordType() {
		return trim(getNullSafeValue(this.recordType), lengthMap.get("recordType"));
	}

	public void setRecordType(String recordType) {
		this.recordType = recordType;
	}

	public String getBatchNo() {
		return trim(getNullSafeValue(this.batchNo), lengthMap.get("batchNo"));
	}

	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}

	public String getChallanCount() {
		return trim(getNullSafeValue(this.challanCount), lengthMap.get("challanCount"));
	}

	public void setChallanCount(String challanCount) {
		this.challanCount = challanCount;
	}

	public String getFormNo() {
		return trim(getNullSafeValue(this.formNo), lengthMap.get("formNo"));
	}

	public void setFormNo(String formNo) {
		this.formNo = formNo;
	}

	public String getTransactionType() {
		return trim(getNullSafeValue(this.transactionType), lengthMap.get("transactionType"));
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public String getBatchUpdationIndicator() {
		return trim(getNullSafeValue(this.batchUpdationIndicator), lengthMap.get("batchUpdationIndicator"));
	}

	public void setBatchUpdationIndicator(String batchUpdationIndicator) {
		this.batchUpdationIndicator = batchUpdationIndicator;
	}

	public String getOriginalRrrNo() {
		return trim(getNullSafeValue(this.originalRrrNo), lengthMap.get("originalRrrNo"));
	}

	public void setOriginalRrrNo(String originalRrrNo) {
		this.originalRrrNo = originalRrrNo;
	}

	public String getPreviousRrrNo() {
		return trim(getNullSafeValue(this.previousRrrNo), lengthMap.get("previousRrrNo"));
	}

	public void setPreviousRrrNo(String previousRrrNo) {
		this.previousRrrNo = previousRrrNo;
	}

	public String getRrrNo() {
		return trim(getNullSafeValue(this.rrrNo), lengthMap.get("rrrNo"));
	}

	public void setRrrNo(String rrrNo) {
		this.rrrNo = rrrNo;
	}

	public String getRrrDate() {
		return trim(getNullSafeValue(this.rrrDate), lengthMap.get("rrrDate"));
	}

	public void setRrrDate(String rrrDate) {
		this.rrrDate = rrrDate;
	}

	public String getLastTanOfCollector() {
		return trim(getNullSafeValue(this.lastTanOfCollector), lengthMap.get("lastTanOfCollector"));
	}

	public void setLastTanOfCollector(String lastTanOfCollector) {
		this.lastTanOfCollector = lastTanOfCollector;
	}

	public String getTanOfCollector() {
		return trim(getNullSafeValue(this.tanOfCollector), lengthMap.get("tanOfCollector"));
	}

	public void setTanOfCollector(String tanOfCollector) {
		this.tanOfCollector = tanOfCollector;
	}

	public String getReceiptNumber() {
		return trim(getNullSafeValue(this.receiptNumber), lengthMap.get("receiptNumber"));
	}

	public void setReceiptNumber(String receiptNumber) {
		this.receiptNumber = receiptNumber;
	}

	public String getPanOfDeductor() {
		return trim(getNullSafeValue(this.panOfCollector), lengthMap.get("panOfCollector"));
	}

	public void setPanOfDeductor(String panOfCollector) {
		this.panOfCollector = panOfCollector;
	}

	public String getAssessmentYr() {
		return trim(cleanseData(getNullSafeValue(this.assessmentYr)), lengthMap.get("assessmentYr"));
	}

	public void setAssessmentYr(String assessmentYr) {
		this.assessmentYr = assessmentYr;
	}

	public String getFinancialYr() {
		return trim(cleanseData(getNullSafeValue(this.financialYr)), lengthMap.get("financialYr"));
	}

	public void setFinancialYr(String financialYr) {
		this.financialYr = financialYr;
	}

	public String getPeriod() {
		return trim(getNullSafeValue(this.period), lengthMap.get("period"));
	}

	public void setPeriod(String period) {
		this.period = period;
	}

	public String getEmployerName() {
		return trim(getNullSafeValue(this.employerName), lengthMap.get("employerName"));
	}

	public void setEmployerName(String employerName) {
		this.employerName = employerName;
	}

	public String getEmployerBranchDiv() {
		return trim(cleanseData(cleanseData(getNullSafeValue(this.employerBranchDiv))), lengthMap.get("employerBranchDiv"));
	}

	public void setEmployerBranchDiv(String employerBranchDiv) {
		this.employerBranchDiv = employerBranchDiv;
	}

	public String getEmployerAddr1() {
		return trim(cleanseData(getNullSafeValue(this.employerAddr1)), lengthMap.get("employerAddr1"));
	}

	public void setEmployerAddr1(String employerAddr1) {
		this.employerAddr1 = employerAddr1;
	}

	public String getEmployerAddr2() {
		return trim(cleanseData(getNullSafeValue(this.employerAddr2)), lengthMap.get("employerAddr2"));
	}

	public void setEmployerAddr2(String employerAddr2) {
		this.employerAddr2 = employerAddr2;
	}

	public String getEmployerAddr3() {
		return trim(cleanseData(getNullSafeValue(this.employerAddr3)), lengthMap.get("employerAddr3"));
	}

	public void setEmployerAddr3(String employerAddr3) {
		this.employerAddr3 = employerAddr3;
	}

	public String getEmployerAddr4() {
		return trim(cleanseData(getNullSafeValue(this.employerAddr4)), lengthMap.get("employerAddr4"));
	}

	public void setEmployerAddr4(String employerAddr4) {
		this.employerAddr4 = employerAddr4;
	}

	public String getEmployerAddr5() {
		return trim(cleanseData(getNullSafeValue(this.employerAddr5)), lengthMap.get("employerAddr5"));
	}

	public void setEmployerAddr5(String employerAddr5) {
		this.employerAddr5 = employerAddr5;
	}

	public String getEmployerState() {
		return trim(getNullSafeValue(this.employerState), lengthMap.get("employerState"));
	}

	public void setEmployerState(String employerState) {
		this.employerState = employerState;
	}

	public String getEmployerPin() {
		return trim(getNullSafeValue(this.employerPin), lengthMap.get("employerPin"));
	}

	public void setEmployerPin(String employerPin) {
		this.employerPin = employerPin;
	}

	public String getEmployerEmail() {
		return trim(getNullSafeValue(this.employerEmail), lengthMap.get("employerEmail"));
	}

	public void setEmployerEmail(String employerEmail) {
		this.employerEmail = employerEmail;
	}

	public String getEmployerStd() {
		return trim(getNullSafeValue(this.employerStd), lengthMap.get("employerStd"));
	}

	public void setEmployerStd(String employerStd) {
		this.employerStd = employerStd;
	}

	public String getEmployerPhone() {
		return trim(getNullSafeValue(this.employerPhone), lengthMap.get("employerPhone"));
	}

	public void setEmployerPhone(String employerPhone) {
		this.employerPhone = employerPhone;
	}

	public String getEmployerAddrChange() {
		return trim(getNullSafeValue(this.employerAddrChange), lengthMap.get("employerAddrChange"));
	}

	public void setEmployerAddrChange(String employerAddrChange) {
		this.employerAddrChange = employerAddrChange;
	}

	public String getCollectorType() {
		return trim(getNullSafeValue(this.collectorType), lengthMap.get("collectorType"));
	}

	public void setCollectorType(String collectorType) {
		this.collectorType = collectorType;
	}

	public String getNameofPersonResponsilbleForTaxCollection() {
		return trim(getNullSafeValue(this.nameofPersonResponsilbleForTaxCollection),
				lengthMap.get("nameofPersonResponsilbleForSal"));
	}

	public void setNameofPersonResponsilbleForTaxCollection(String nameofPersonResponsilbleForTaxCollection) {
		this.nameofPersonResponsilbleForTaxCollection = nameofPersonResponsilbleForTaxCollection;
	}

	public String getDesignationofPersonResponsilbleForTaxCollection() {
		return trim(getNullSafeValue(this.designationofPersonResponsilbleForTaxCollection),
				lengthMap.get("designationofPersonResponsilbleForSal"));
	}

	public void setDesignationofPersonResponsilbleForTaxCollection(String designationofPersonResponsilbleForTaxCollection) {
		this.designationofPersonResponsilbleForTaxCollection = designationofPersonResponsilbleForTaxCollection;
	}

	public String getPersonResponsilbleAddr1() {
		return trim(getNullSafeValue(this.personResponsilbleAddr1), lengthMap.get("personResponsilbleAddr1"));
	}

	public void setPersonResponsilbleAddr1(String personResponsilbleAddr1) {
		this.personResponsilbleAddr1 = personResponsilbleAddr1;
	}

	public String getPersonResponsilbleAddr2() {
		return trim(getNullSafeValue(this.personResponsilbleAddr2), lengthMap.get("personResponsilbleAddr2"));
	}

	public void setPersonResponsilbleAddr2(String personResponsilbleAddr2) {
		this.personResponsilbleAddr2 = personResponsilbleAddr2;
	}

	public String getPersonResponsilbleAddr3() {
		return trim(getNullSafeValue(this.personResponsilbleAddr3), lengthMap.get("personResponsilbleAddr3"));
	}

	public void setPersonResponsilbleAddr3(String personResponsilbleAddr3) {
		this.personResponsilbleAddr3 = personResponsilbleAddr3;
	}

	public String getPersonResponsilbleAddr4() {
		return trim(getNullSafeValue(this.personResponsilbleAddr4), lengthMap.get("personResponsilbleAddr4"));
	}

	public void setPersonResponsilbleAddr4(String personResponsilbleAddr4) {
		this.personResponsilbleAddr4 = personResponsilbleAddr4;
	}

	public String getPersonResponsilbleAddr5() {
		return trim(getNullSafeValue(this.personResponsilbleAddr5), lengthMap.get("personResponsilbleAddr5"));
	}

	public void setPersonResponsilbleAddr5(String personResponsilbleAddr5) {
		this.personResponsilbleAddr5 = personResponsilbleAddr5;
	}

	public String getPersonResponsilbleState() {
		return trim(getNullSafeValue(this.personResponsilbleState), lengthMap.get("personResponsilbleState"));
	}

	public void setPersonResponsilbleState(String personResponsilbleState) {
		this.personResponsilbleState = personResponsilbleState;
	}

	public String getPersonResponsilblePin() {
		return trim(getNullSafeValue(this.personResponsilblePin), lengthMap.get("personResponsilblePin"));
	}

	public void setPersonResponsilblePin(String personResponsilblePin) {
		this.personResponsilblePin = personResponsilblePin;
	}

	public String getPersonResponsilbleEmailId1() {
		return trim(getNullSafeValue(this.personResponsilbleEmailId1), lengthMap.get("personResponsilbleEmailId1"));
	}

	public void setPersonResponsilbleEmailId1(String personResponsilbleEmailId1) {
		this.personResponsilbleEmailId1 = personResponsilbleEmailId1;
	}

	public String getMobileNumber() {
		return trim(getNullSafeValue(this.mobileNumber), lengthMap.get("mobileNumber"));
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getPersonResponsilbleSTDCode() {
		return trim(getNullSafeValue(this.personResponsilbleSTDCode), lengthMap.get("personResponsilbleSTDCode"));
	}

	public void setPersonResponsilbleSTDCode(String personResponsilbleSTDCode) {
		this.personResponsilbleSTDCode = personResponsilbleSTDCode;
	}

	public String getPersonResponsilbleTelePhone() {
		return trim(getNullSafeValue(this.personResponsilbleTelePhone), lengthMap.get("personResponsilbleTelePhone"));
	}

	public void setPersonResponsilbleTelePhone(String personResponsilbleTelePhone) {
		this.personResponsilbleTelePhone = personResponsilbleTelePhone;
	}

	public String getPersonResponsilbleAddrChange() {
		return trim(getNullSafeValue(this.personResponsilbleAddrChange), lengthMap.get("personResponsilbleAddrChange"));
	}

	public void setPersonResponsilbleAddrChange(String personResponsilbleAddrChange) {
		this.personResponsilbleAddrChange = personResponsilbleAddrChange;
	}

	public String getGrossTdsTotalAsPerChallan() {
		return trim(getNullSafeValue(this.grossTdsTotalAsPerChallan), lengthMap.get("grossTdsTotalAsPerChallan"));
	}

	public void setGrossTdsTotalAsPerChallan(String grossTdsTotalAsPerChallan) {
		this.grossTdsTotalAsPerChallan = grossTdsTotalAsPerChallan;
	}

	public String getUnMatchedChalanCnt() {
		return trim(getNullSafeValue(this.unMatchedChalanCnt), lengthMap.get("unMatchedChalanCnt"));
	}

	public void setUnMatchedChalanCnt(String unMatchedChalanCnt) {
		this.unMatchedChalanCnt = unMatchedChalanCnt;
	}

	public String getCountOfSalaryDetailRec() {
		return trim(getNullSafeValue(this.countOfSalaryDetailRec), lengthMap.get("countOfSalaryDetailRec"));
	}

	public void setCountOfSalaryDetailRec(String countOfSalaryDetailRec) {
		this.countOfSalaryDetailRec = countOfSalaryDetailRec;
	}

	public String getGrossTotalIncomeSd() {
		return trim(getNullSafeValue(this.grossTotalIncomeSd), lengthMap.get("grossTotalIncomeSd"));
	}

	public void setGrossTotalIncomeSd(String grossTotalIncomeSd) {
		this.grossTotalIncomeSd = grossTotalIncomeSd;
	}

	public String getApprovalTaken() {
		return trim(getNullSafeValue(this.approvalTaken), lengthMap.get("approvalTaken"));
	}

	public void setApprovalTaken(String approvalTaken) {
		this.approvalTaken = approvalTaken;
	}


	public String getPanOfCollector() {
		return panOfCollector;
	}

	public void setPanOfCollector(String panOfCollector) {
		this.panOfCollector = panOfCollector;
	}

	public String getIsRegularStatementForForm27EQFiledForEArlierPeriod() {
		return isRegularStatementForForm27EQFiledForEArlierPeriod;
	}

	public void setIsRegularStatementForForm27EQFiledForEArlierPeriod(
			String isRegularStatementForForm27EQFiledForEArlierPeriod) {
		this.isRegularStatementForForm27EQFiledForEArlierPeriod = isRegularStatementForForm27EQFiledForEArlierPeriod;
	}

	public String getLastCollectorType() {
		return trim(getNullSafeValue(this.lastCollectorType), lengthMap.get("lastCollectorType"));
	}

	public void setLastCollectorType(String lastCollectorType) {
		this.lastCollectorType = lastCollectorType;
	}

	public String getStateName() {
		return trim(getNullSafeValue(this.stateName), lengthMap.get("stateName"));
	}

	public void setStateName(String stateName) {
		this.stateName = stateName;
	}

	public String getPaoCode() {
		return trim(getNullSafeValue(this.paoCode), lengthMap.get("paoCode"));
	}

	public void setPaoCode(String paoCode) {
		this.paoCode = paoCode;
	}

	public String getDdoCode() {
		return trim(getNullSafeValue(this.ddoCode), lengthMap.get("ddoCode"));
	}

	public void setDdoCode(String ddoCode) {
		this.ddoCode = ddoCode;
	}

	public String getMinistryName() {
		return trim(getNullSafeValue(this.ministryName), lengthMap.get("ministryName"));
	}

	public void setMinistryName(String ministryName) {
		this.ministryName = ministryName;
	}

	public String getMinistryNameOther() {
		return trim(getNullSafeValue(this.ministryNameOther), lengthMap.get("ministryNameOther"));
	}

	public void setMinistryNameOther(String ministryNameOther) {
		this.ministryNameOther = ministryNameOther;
	}

	public String getpANOfResponsiblePerson() {
		return trim(getNullSafeValue(this.pANOfResponsiblePerson), lengthMap.get("pANOfResponsiblePerson"));
	}

	public void setpANOfResponsiblePerson(String pANOfResponsiblePerson) {
		this.pANOfResponsiblePerson = pANOfResponsiblePerson;
	}

	public String getPaoRegistrationNo() {
		return trim(getNullSafeValue(this.paoRegistrationNo), lengthMap.get("paoRegistrationNo"));
	}

	public void setPaoRegistrationNo(String paoRegistrationNo) {
		this.paoRegistrationNo = paoRegistrationNo;
	}

	public String getDdoRegistrationNo() {
		return trim(getNullSafeValue(this.ddoRegistrationNo), lengthMap.get("ddoRegistrationNo"));
	}

	public void setDdoRegistrationNo(String ddoRegistrationNo) {
		this.ddoRegistrationNo = ddoRegistrationNo;
	}

	public String getBatchHash() {
		return trim(getNullSafeValue(this.batchHash), lengthMap.get("batchHash"));
	}

	public void setBatchHash(String batchHash) {
		this.batchHash = batchHash;
	}

	public String getEmployerSTDAlt() {
		return trim(getNullSafeValue(this.employerSTDAlt), lengthMap.get("employerSTDAlt"));
	}

	public void setEmployerSTDAlt(String employerSTDAlt) {
		this.employerSTDAlt = employerSTDAlt;
	}

	public String getEmployerPhoneAlt() {
		return trim(getNullSafeValue(this.employerPhoneAlt), lengthMap.get("employerPhoneAlt"));
	}

	public void setEmployerPhoneAlt(String employerPhoneAlt) {
		this.employerPhoneAlt = employerPhoneAlt;
	}

	public String getEmployerEmailAlt() {
		return trim(getNullSafeValue(this.employerEmailAlt), lengthMap.get("employerEmailAlt"));
	}

	public void setEmployerEmailAlt(String employerEmailAlt) {
		this.employerEmailAlt = employerEmailAlt;
	}

	public String getPersonResponsilbleSTDCodeAlt() {
		return trim(getNullSafeValue(this.personResponsilbleSTDCodeAlt), lengthMap.get("personResponsilbleSTDCodeAlt"));
	}

	public void setPersonResponsilbleSTDCodeAlt(String personResponsilbleSTDCodeAlt) {
		this.personResponsilbleSTDCodeAlt = personResponsilbleSTDCodeAlt;
	}

	public String getPersonResponsilbleTelePhoneAlt() {
		return trim(getNullSafeValue(this.personResponsilbleTelePhoneAlt),
				lengthMap.get("personResponsilbleTelePhoneAlt"));
	}

	public void setPersonResponsilbleTelePhoneAlt(String personResponsilbleTelePhoneAlt) {
		this.personResponsilbleTelePhoneAlt = personResponsilbleTelePhoneAlt;
	}

	public String getPersonResponsilbleEmailIdAlt() {
		return trim(getNullSafeValue(this.personResponsilbleEmailIdAlt), lengthMap.get("personResponsilbleEmailIdAlt"));
	}

	public void setPersonResponsilbleEmailIdAlt(String personResponsilbleEmailIdAlt) {
		this.personResponsilbleEmailIdAlt = personResponsilbleEmailIdAlt;
	}

	public String getaIN() {
		return trim(getNullSafeValue(this.aIN), lengthMap.get("aIN"));
	}

	public void setaIN(String aIN) {
		this.aIN = aIN;
	}

	public String getgSTN() {
		return trim(getNullSafeValue(this.gSTN), lengthMap.get("gSTN"));
	}

	public void setgSTN(String gSTN) {
		this.gSTN = gSTN;
	}

	@Override
	public String toString() {
		String value=getLineNo() + "^" // 1
				+ getRecordType() + "^" // 2
				+ getBatchNo() + "^" // 3
				+ getChallanCount() + "^" // 4
				+ getFormNo() + "^" // 5
				+ getTransactionType() + "^" // 6
				+ getBatchUpdationIndicator() + "^" // 7
				+ getOriginalRrrNo() + "^" // 8
				+ getPreviousRrrNo() + "^" // 9
				+ getRrrNo() + "^" // 10
				+ getRrrDate() + "^" // 11
				+ getLastTanOfCollector() + "^" // 12
				+ getTanOfCollector() + "^" // 13
				+ getReceiptNumber() + "^" // 14
				+ getPanOfDeductor() + "^" // 15
				+ getAssessmentYr() + "^" // 16
				+ getFinancialYr() + "^" // 17
				+ getPeriod() + "^" // 18
				+ getEmployerName() + "^" // 19
				+ getEmployerBranchDiv() + "^" // 20
				+ getEmployerAddr1() + "^" // 21
				+ getEmployerAddr2() + "^" // 22
				+ getEmployerAddr3() + "^" // 23
				+ getEmployerAddr4() + "^" // 24
				+ getEmployerAddr5() + "^" // 25
				+ getEmployerState() + "^" // 26
				+ getEmployerPin() + "^" // 27
				+ getEmployerEmail() + "^" // 28
				+ getEmployerStd() + "^" // 29
				+ getEmployerPhone() + "^" // 30
				+ getEmployerAddrChange() + "^" // 31
				+ getCollectorType() + "^" // 32
				+ getNameofPersonResponsilbleForTaxCollection() + "^" // 33
				+ getDesignationofPersonResponsilbleForTaxCollection() + "^" // 34
				+ getPersonResponsilbleAddr1() + "^" // 35
				+ getPersonResponsilbleAddr2() + "^" // 36
				+ getPersonResponsilbleAddr3() + "^" // 37
				+ getPersonResponsilbleAddr4() + "^" // 38
				+ getPersonResponsilbleAddr5() + "^" // 39
				+ getPersonResponsilbleState() + "^" // 40
				+ getPersonResponsilblePin() + "^" // 41
				+ getPersonResponsilbleEmailId1() + "^" // 42
				+ getMobileNumber() + "^" // 43
				+ getPersonResponsilbleSTDCode() + "^" // 44
				+ getPersonResponsilbleTelePhone() + "^" // 45
				+ getPersonResponsilbleAddrChange() + "^" // 46
				+ getGrossTdsTotalAsPerChallan() + "^" // 47
				+ getUnMatchedChalanCnt() + "^" // 48
				+ getCountOfSalaryDetailRec() + "^" // 49
				+ getGrossTotalIncomeSd() + "^" // 50
				+ getApprovalTaken() + "^" // 51
				+ getIsRegularStatementForForm27EQFiledForEArlierPeriod() + "^" // 52
				+ getLastCollectorType() + "^" // 53
				+ getStateName() + "^" // 54
				+ getPaoCode() + "^" // 55
				+ getDdoCode() + "^" // 56
				+ getMinistryName() + "^" // 57
				+ getMinistryNameOther() + "^" // 58
				+ getpANOfResponsiblePerson() + "^" // 59
				+ getPaoRegistrationNo() + "^" // 60
				+ getDdoRegistrationNo() + "^" // 61
				+ getEmployerSTDAlt() + "^" // 62
				+ getEmployerPhoneAlt() + "^" // 63
				+ getEmployerEmailAlt() + "^" // 64
				+ getPersonResponsilbleSTDCodeAlt() + "^" // 65
				+ getPersonResponsilbleTelePhoneAlt() + "^" // 66
				+ getPersonResponsilbleEmailIdAlt() + "^" // 67
				+ getaIN() + "^" // 68
				+ getgSTN() + "^" // 69
				+ getBatchHash(); // 70;
		return value;
	}

	
	
	
}
