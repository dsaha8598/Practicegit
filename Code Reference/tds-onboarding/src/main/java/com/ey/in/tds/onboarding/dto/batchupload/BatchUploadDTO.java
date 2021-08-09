package com.ey.in.tds.onboarding.dto.batchupload;

import java.io.Serializable;
import java.util.Date;

public class BatchUploadDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long id;

	private String fileStatus;

	private String fileName;

	private String filePath;

	private Date dateOfUpload;
	
	private String uploadBy;
	
	private Integer totalRecords;
	
	private Integer duplicateRecords;
	
	private Integer errors;
	
	private Integer mismatchCount;
	
	private Integer successCount;


	
	public Integer getMismatchCount() {
		return mismatchCount;
	}

	public void setMismatchCount(Integer mismatchCount) {
		this.mismatchCount = mismatchCount;
	}

	public Integer getSuccessCount() {
		return successCount;
	}

	public void setSuccessCount(Integer successCount) {
		this.successCount = successCount;
	}

	public void setTotalRecords(Integer totalRecords) {
		this.totalRecords = totalRecords;
	}

	public void setDuplicateRecords(Integer duplicateRecords) {
		this.duplicateRecords = duplicateRecords;
	}

	public void setErrors(Integer errors) {
		this.errors = errors;
	}

	private String errorFileDownloadUrl;
	
	private String uploadedFileDownloadUrl;
		

	Date getDateOfUpload() {
		return dateOfUpload;
	}

	void setDateOfUpload(Date dateOfUpload) {
		this.dateOfUpload = dateOfUpload;
	}

	String getUploadBy() {
		return uploadBy;
	}

	void setUploadBy(String uploadBy) {
		this.uploadBy = uploadBy;
	}

	int getTotalRecords() {
		return totalRecords;
	}

	void setTotalRecords(int totalRecords) {
		this.totalRecords = totalRecords;
	}

	int getDuplicateRecords() {
		return duplicateRecords;
	}

	void setDuplicateRecords(int duplicateRecords) {
		this.duplicateRecords = duplicateRecords;
	}

	int getErrors() {
		return errors;
	}

	void setErrors(int errors) {
		this.errors = errors;
	}

	String getErrorFileDownloadUrl() {
		return errorFileDownloadUrl;
	}

	void setErrorFileDownloadUrl(String errorFileDownloadUrl) {
		this.errorFileDownloadUrl = errorFileDownloadUrl;
	}

	String getUploadedFileDownloadUrl() {
		return uploadedFileDownloadUrl;
	}

	void setUploadedFileDownloadUrl(String uploadedFileDownloadUrl) {
		this.uploadedFileDownloadUrl = uploadedFileDownloadUrl;
	}

	Long getId() {
		return id;
	}

	void setId(Long id) {
		this.id = id;
	}

	String getFileStatus() {
		return fileStatus;
	}

	void setFileStatus(String fileStatus) {
		this.fileStatus = fileStatus;
	}

	String getFileName() {
		return fileName;
	}

	void setFileName(String fileName) {
		this.fileName = fileName;
	}

	String getFilePath() {
		return filePath;
	}

	void setFilePath(String filePath) {
		this.filePath = filePath;
	}


	
}
