package com.ey.in.tds.ingestion.dto.batchgroup;

import java.util.Date;

public class BatchUploadGroupDTO {
	
	private Integer id;
	
	private String  groupName;
	
	private Date createdDate;
	
	private Integer totalRecords;
	
	private Integer processedRecords;
	
	private Integer errorRecords;
	
	private Integer underValidationRecords;
	
	private Date postingDateOfTheDocument;

	public Date getPostingDateOfTheDocument() {
		return postingDateOfTheDocument;
	}

	public void setPostingDateOfTheDocument(Date postingDateOfTheDocument) {
		this.postingDateOfTheDocument = postingDateOfTheDocument;
	}

	public Integer getUnderValidationRecords() {
		return underValidationRecords;
	}

	public void setUnderValidationRecords(Integer underValidationRecords) {
		this.underValidationRecords = underValidationRecords;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}


	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Integer getTotalRecords() {
		return totalRecords;
	}

	public void setTotalRecords(Integer totalRecords) {
		this.totalRecords = totalRecords;
	}

	public Integer getProcessedRecords() {
		return processedRecords;
	}

	public void setProcessedRecords(Integer processedRecords) {
		this.processedRecords = processedRecords;
	}

	public Integer getErrorRecords() {
		return errorRecords;
	}

	public void setErrorRecords(Integer errorRecords) {
		this.errorRecords = errorRecords;
	}
	
}
