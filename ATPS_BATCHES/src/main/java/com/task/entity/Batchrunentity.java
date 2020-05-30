package com.task.entity;



import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.Data;

@Data
@Entity
@Table(name="BATCH_RUN_DTLS")
public class Batchrunentity {

	 @Id
	    @GenericGenerator(name = "sequence", strategy = "sequence", parameters = {
	            @org.hibernate.annotations.Parameter(name = "sequenceName", value = "sequence"),
	            @org.hibernate.annotations.Parameter(name = "allocationSize", value = "1"),
	    })
	    @GeneratedValue(generator = "sequence", strategy=GenerationType.SEQUENCE)
	
	private Integer runId;
	public Integer getRunId() {
		return runId;
	}
	public void setRunId(Integer runId) {
		this.runId = runId;
	}
	public String getBatchName() {
		return batchName;
	}
	public void setBatchName(String batchName) {
		this.batchName = batchName;
	}
	public Date getStartdate() {
		return Startdate;
	}
	public void setStartdate(Date startdate) {
		Startdate = startdate;
	}
	public String getRunStatus() {
		return runStatus;
	}
	public void setRunStatus(String runStatus) {
		this.runStatus = runStatus;
	}
	public Date getEndDt() {
		return endDt;
	}
	public void setEndDt(Date endDt) {
		this.endDt = endDt;
	}
	public Date getCreateDt() {
		return createDt;
	}
	public void setCreateDt(Date createDt) {
		this.createDt = createDt;
	}
	public Date getUpdatedDt() {
		return UpdatedDt;
	}
	public void setUpdatedDt(Date updatedDt) {
		UpdatedDt = updatedDt;
	}
	public String getCreatedby() {
		return createdby;
	}
	public void setCreatedby(String createdby) {
		this.createdby = createdby;
	}
	public String getUopdatedBy() {
		return uopdatedBy;
	}
	public void setUopdatedBy(String uopdatedBy) {
		this.uopdatedBy = uopdatedBy;
	}
	private String batchName;
	private Date Startdate;
	private String runStatus;
	private Date endDt;
	@CreationTimestamp
	private Date createDt;
	@UpdateTimestamp
	private Date UpdatedDt;
	//lssahsbasbasabsjabs
	private String createdby;
	private String uopdatedBy;
}
