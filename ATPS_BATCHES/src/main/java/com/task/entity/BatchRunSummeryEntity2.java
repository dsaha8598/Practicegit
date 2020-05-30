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

//buyhbhjb
@Data
@Entity
@Table(name="BATCH_RUN_SUMMARY")
public class BatchRunSummeryEntity2{

	 @Id
	    @GenericGenerator(name = "sequence", strategy = "sequence", parameters = {
	            @org.hibernate.annotations.Parameter(name = "sequenceName", value = "sequence"),
	            @org.hibernate.annotations.Parameter(name = "allocationSize", value = "1"),
	    })
	    @GeneratedValue(generator = "sequence", strategy=GenerationType.SEQUENCE)
	
	private Integer summeryId;
	private String batchName;
	private String summeryDetails;
	@CreationTimestamp
	private Date createDt;
	@UpdateTimestamp
	private Date UpdatedDt;
	
	private String createdby;
	private String uopdatedBy;
	public Integer getSummeryId() {
		return summeryId;
	}
	public void setSummeryId(Integer summeryId) {
		this.summeryId = summeryId;
	}
	public String getBatchName() {
		return batchName;
	}
	public void setBatchName(String batchName) {
		this.batchName = batchName;
	}
	public String getSummeryDetails() {
		return summeryDetails;
	}
	public void setSummeryDetails(String summeryDetails) {
		this.summeryDetails = summeryDetails;
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
}
