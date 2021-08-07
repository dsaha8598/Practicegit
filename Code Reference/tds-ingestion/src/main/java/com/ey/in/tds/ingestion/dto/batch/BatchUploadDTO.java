package com.ey.in.tds.ingestion.dto.batch;

import java.io.Serializable;
import java.util.Date;

public class BatchUploadDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private Date dateOfUpload;
	private long duplicateRecords;
	private String errorFileDownloadUrl;
	private String sucessFileDownloadUrl;
	private String otherFileDownloadUrl;
	private long errorRecords;
	private long errors;
	private String fileName;
	private String fileStatus;
	private Integer id;
	private long mismatchCount;
	private long processedRecords;
	private Integer referenceId;
	private long successCount;
	private String tan;
	private long totalRecords;
	private String uploadBy;
	private String uploadedFileDownloadUrl;
	private int year;
	private int month;
	private Date processEndTime;
	private String fileType;
	private Integer runId;
	private String uploadType;
	private String new_status;
	private String notdsFileUrl;
	private Long notdsCount;

	public String getNew_status() {
		return new_status;
	}

	public void setNew_status(String new_status) {
		this.new_status = new_status;
	}

	public Date getDateOfUpload() {
		return dateOfUpload;
	}

	public void setDateOfUpload(Date dateOfUpload) {
		this.dateOfUpload = dateOfUpload;
	}

	public long getDuplicateRecords() {
		return duplicateRecords;
	}

	public void setDuplicateRecords(long duplicateRecords) {
		this.duplicateRecords = duplicateRecords;
	}

	public String getErrorFileDownloadUrl() {
		return errorFileDownloadUrl;
	}

	public void setErrorFileDownloadUrl(String errorFileDownloadUrl) {
		this.errorFileDownloadUrl = errorFileDownloadUrl;
	}

	public String getSucessFileDownloadUrl() {
		return sucessFileDownloadUrl;
	}

	public void setSucessFileDownloadUrl(String sucessFileDownloadUrl) {
		this.sucessFileDownloadUrl = sucessFileDownloadUrl;
	}

	public String getOtherFileDownloadUrl() {
		return otherFileDownloadUrl;
	}

	public void setOtherFileDownloadUrl(String otherFileDownloadUrl) {
		this.otherFileDownloadUrl = otherFileDownloadUrl;
	}

	public long getErrorRecords() {
		return errorRecords;
	}

	public void setErrorRecords(long errorRecords) {
		this.errorRecords = errorRecords;
	}

	public long getErrors() {
		return errors;
	}

	public void setErrors(long errors) {
		this.errors = errors;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileStatus() {
		return fileStatus;
	}

	public void setFileStatus(String fileStatus) {
		this.fileStatus = fileStatus;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public long getMismatchCount() {
		return mismatchCount;
	}

	public void setMismatchCount(long mismatchCount) {
		this.mismatchCount = mismatchCount;
	}

	public long getProcessedRecords() {
		return processedRecords;
	}

	public void setProcessedRecords(long processedRecords) {
		this.processedRecords = processedRecords;
	}

	public Integer getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(Integer referenceId) {
		this.referenceId = referenceId;
	}

	public long getSuccessCount() {
		return successCount;
	}

	public void setSuccessCount(long successCount) {
		this.successCount = successCount;
	}

	public String getTan() {
		return tan;
	}

	public void setTan(String tan) {
		this.tan = tan;
	}

	public long getTotalRecords() {
		return totalRecords;
	}

	public void setTotalRecords(long totalRecords) {
		this.totalRecords = totalRecords;
	}

	public String getUploadBy() {
		return uploadBy;
	}

	public void setUploadBy(String uploadBy) {
		this.uploadBy = uploadBy;
	}

	public String getUploadedFileDownloadUrl() {
		return uploadedFileDownloadUrl;
	}

	public void setUploadedFileDownloadUrl(String uploadedFileDownloadUrl) {
		this.uploadedFileDownloadUrl = uploadedFileDownloadUrl;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public Date getProcessEndTime() {
		return processEndTime;
	}

	public void setProcessEndTime(Date processEndTime) {
		this.processEndTime = processEndTime;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public Integer getRunId() {
		return runId;
	}

	public void setRunId(Integer runId) {
		this.runId = runId;
	}

	public String getUploadType() {
		return uploadType;
	}

	public void setUploadType(String uploadType) {
		this.uploadType = uploadType;
	}

	public String getNotdsFileUrl() {
		return notdsFileUrl;
	}

	public void setNotdsFileUrl(String notdsFileUrl) {
		this.notdsFileUrl = notdsFileUrl;
	}

	public Long getNotdsCount() {
		return notdsCount;
	}

	public void setNotdsCount(Long notdsCount) {
		this.notdsCount = notdsCount;
	}

	@Override
	public String toString() {
		return "BatchUploadDTO [dateOfUpload=" + dateOfUpload + ", duplicateRecords=" + duplicateRecords
				+ ", errorFileDownloadUrl=" + errorFileDownloadUrl + ", sucessFileDownloadUrl=" + sucessFileDownloadUrl
				+ ", otherFileDownloadUrl=" + otherFileDownloadUrl + ", errorRecords=" + errorRecords + ", errors="
				+ errors + ", fileName=" + fileName + ", fileStatus=" + fileStatus + ", id=" + id + ", mismatchCount="
				+ mismatchCount + ", processedRecords=" + processedRecords + ", referenceId=" + referenceId
				+ ", successCount=" + successCount + ", tan=" + tan + ", totalRecords=" + totalRecords + ", uploadBy="
				+ uploadBy + ", uploadedFileDownloadUrl=" + uploadedFileDownloadUrl + ", year=" + year + ", month="
				+ month + ", processEndTime=" + processEndTime + ", fileType=" + fileType + ", runId=" + runId
				+ ", uploadType=" + uploadType + ", new_status=" + new_status + ", notdsFileUrl=" + notdsFileUrl
				+ ", notdsCount=" + notdsCount + "]";
	}


}
