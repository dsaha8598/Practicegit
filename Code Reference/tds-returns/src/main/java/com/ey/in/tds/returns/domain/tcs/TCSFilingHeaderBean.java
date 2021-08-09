package com.ey.in.tds.returns.domain.tcs;

import java.util.HashMap;
import java.util.Map;

import com.ey.in.tds.returns.domain.AbstractFilingBean;

public class TCSFilingHeaderBean extends AbstractFilingBean{

	
	private static final long serialVersionUID = -4001548965107592522L;
	private int lineNo = 1;// 1
	private String recordType = "FH";// 2
	private String fileType = "NS1";// 3
	private String uploadType = "R";// 4
	private String fileDate;// 5
	private int fileSeq;// 6
	private String uploaderType = "D";// 7
	private String tanOfCollector;// 8
	private int noOfBatches;// 9
	/**
	 * Name of Return Preparation Utility
	 */
	private String rpuName;// 10
	private String recordHash;// 11
	private String fvuVersion;// 12
	private String fileHash;// 13
	private String samVersion;// 14
	private String samHash;// 15
	private String scmVersion;// 16
	private String scmHash;// 17
	/**
	 * Consolidated file hash
	 */
	private String consHash;// 18
	private Map<String, Integer> lengthMap = new HashMap<String, Integer>() {
		private static final long serialVersionUID = -2720054360432417369L;
		{
			put("lineNo", 9);// 1
			put("recordType", 2);// 2
			put("fileType", 4);// 3
			put("uploadType", 2);// 4
			put("fileDate", 8);// 5
			put("fileSeq", 11);// 6
			put("uploaderType", 1);// 7
			put("tanOfCollector", 10);// 8
			put("noOfBatches", 9);// 9
			put("rpuName", 75);// 10
			put("recordHash", 0);// 11
			put("fvuVersion", 0);// 12
			put("fileHash", 0);// 13
			put("samVersion", 0);// 14
			put("samHash", 0);// 15
			put("scmVersion", 0);// 16
			put("scmHash", 0);// 17
			put("consHash", 0);// 18
		}
	};
	
	public int getLineNo() {
		return this.lineNo;
	}

	public void setLineNo(int lineNo) {
		this.lineNo = lineNo;
	}

	public String getRecordType() {
		return trim(getNullSafeValue(this.recordType), lengthMap.get("recordType"));
	}

	public void setRecordType(String recordType) {
		this.recordType = recordType;
	}

	public String getFileType() {
		return trim(getNullSafeValue(this.fileType), lengthMap.get("fileType"));
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getUploadType() {
		return trim(getNullSafeValue(this.uploadType), lengthMap.get("uploadType"));
	}

	public void setUploadType(String uploadType) {
		this.uploadType = uploadType;
	}

	public String getFileDate() {
		return trim(getNullSafeValue(this.fileDate), lengthMap.get("fileDate"));
	}

	public void setFileDate(String fileDate) {
		this.fileDate = fileDate;
	}

	public int getFileSeq() {
		return this.fileSeq;
	}

	public void setFileSeq(int fileSeq) {
		this.fileSeq = fileSeq;
	}

	public String getUploaderType() {
		return trim(getNullSafeValue(this.uploaderType), lengthMap.get("uploaderType"));
	}

	public void setUploaderType(String uploaderType) {
		this.uploaderType = uploaderType;
	}

	public String getTanOfColletor() {
		return trim(getNullSafeValue(this.tanOfCollector), lengthMap.get("tanOfCollector"));
	}

	public void setTanOfCollector(String tanOfCollector) {
		this.tanOfCollector = tanOfCollector;
	}

	public int getNoOfBatches() {
		return this.noOfBatches;
	}

	public void setNoOfBatches(int noOfBatches) {
		this.noOfBatches = noOfBatches;
	}

	public String getRpuName() {
		return trim(getNullSafeValue(this.rpuName), lengthMap.get("rpuName"));
	}

	public void setRpuName(String rpuName) {
		this.rpuName = rpuName;
	}

	public String getRecordHash() {
		return trim(getNullSafeValue(this.recordHash), lengthMap.get("recordHash"));
	}

	public void setRecordHash(String recordHash) {
		this.recordHash = recordHash;
	}

	public String getFvuVersion() {
		return trim(getNullSafeValue(this.fvuVersion), lengthMap.get("fvuVersion"));
	}

	public void setFvuVersion(String fvuVersion) {
		this.fvuVersion = fvuVersion;
	}

	public String getFileHash() {
		return trim(getNullSafeValue(this.fileHash), lengthMap.get("fileHash"));
	}

	public void setFileHash(String fileHash) {
		this.fileHash = fileHash;
	}

	public String getSamVersion() {
		return trim(getNullSafeValue(this.samVersion), lengthMap.get("samVersion"));
	}

	public void setSamVersion(String samVersion) {
		this.samVersion = samVersion;
	}

	public String getSamHash() {
		return trim(getNullSafeValue(this.samHash), lengthMap.get("samHash"));
	}

	public void setSamHash(String samHash) {
		this.samHash = samHash;
	}

	public String getScmVersion() {
		return trim(getNullSafeValue(this.scmVersion), lengthMap.get("scmVersion"));
	}

	public void setScmVersion(String scmVersion) {
		this.scmVersion = scmVersion;
	}

	public String getScmHash() {
		return trim(getNullSafeValue(this.scmHash), lengthMap.get("scmHash"));
	}

	public void setScmHash(String scmHash) {
		this.scmHash = scmHash;
	}

	public String getConsHash() {
		return trim(getNullSafeValue(this.consHash), lengthMap.get("consHash"));
	}

	public void setConsHash(String consHash) {
		this.consHash = consHash;
	}

	@Override
	public String toString() {
		String returnString = null;
		
			returnString = String.valueOf(lineNo) + "^" // 1
					+ getRecordType() + "^" // 2
					+ getFileType() + "^" // 3
					+ getUploadType() + "^" // 4
					+ getFileDate() + "^" // 5
					+ getFileSeq() + "^" // 6
					+ getUploaderType() + "^" // 7
					+ getTanOfColletor() + "^" // 8
					+ getNoOfBatches() + "^" // 9
					+ getRpuName() + "^" // 10
					+ getRecordHash() + "^" // 11
					+ getFvuVersion() + "^" // 12
					+ getFileHash() + "^" // 13
					+ getSamVersion() + "^" // 14
					+ getSamHash() + "^" // 15
					+ getScmVersion() + "^" // 16
					+ getScmHash() + "^" // 17
					+ getConsHash();// 18
		return returnString;
	}
}
