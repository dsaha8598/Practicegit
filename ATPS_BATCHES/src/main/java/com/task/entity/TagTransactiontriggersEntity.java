package com.task.entity;

import java.sql.Date;

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
@Table(name="TAG_TX_TRIGGERS")
public class TagTransactiontriggersEntity {

	 @Id
	    @GenericGenerator(name = "sequence", strategy = "sequence", parameters = {
	            @org.hibernate.annotations.Parameter(name = "sequenceName", value = "sequence"),
	            @org.hibernate.annotations.Parameter(name = "allocationSize", value = "1"),
	    })
	    @GeneratedValue(generator = "sequence", strategy=GenerationType.SEQUENCE)
	
	private Integer triggerID;
		private Integer tagId;
		private String vehicleRegNum;
		private Integer tollPlazaiD;
		private Double tollAmmount;
		private String transactionstatus;
		private String tranxFailReason;
		private String reminderMessage;
		@CreationTimestamp
		private Date createDt;
		@UpdateTimestamp
		private Date UpdatedDt;
		
		private String createdby;
		private String uopdatedBy;
	public Integer getTriggerID() {
		return triggerID;
	}
	public void setTriggerID(Integer triggerID) {
		this.triggerID = triggerID;
	}
	public Integer getTagId() {
		return tagId;
	}
	public void setTagId(Integer tagId) {
		this.tagId = tagId;
	}
	public String getVehicleRegNum() {
		return vehicleRegNum;
	}
	public void setVehicleRegNum(String vehicleRegNum) {
		this.vehicleRegNum = vehicleRegNum;
	}
	public Integer getTollPlazaiD() {
		return tollPlazaiD;
	}
	public void setTollPlazaiD(Integer tollPlazaiD) {
		this.tollPlazaiD = tollPlazaiD;
	}
	public Double getTollAmmount() {
		return tollAmmount;
	}
	public void setTollAmmount(Double tollAmmount) {
		this.tollAmmount = tollAmmount;
	}
	public String getTransactionstatus() {
		return transactionstatus;
	}
	public void setTransactionstatus(String transactionstatus) {
		this.transactionstatus = transactionstatus;
	}
	public String getTranxFailReason() {
		return tranxFailReason;
	}
	public void setTranxFailReason(String tranxFailReason) {
		this.tranxFailReason = tranxFailReason;
	}
	public String getReminderMessage() {
		return reminderMessage;
	}
	public void setReminderMessage(String reminderMessage) {
		this.reminderMessage = reminderMessage;
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
