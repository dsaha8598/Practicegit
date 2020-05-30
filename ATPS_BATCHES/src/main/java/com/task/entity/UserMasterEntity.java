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
@Table(name="USER_MASTER")
public class UserMasterEntity {

	 @Id
	    @GenericGenerator(name = "sequence", strategy = "sequence", parameters = {
	            @org.hibernate.annotations.Parameter(name = "sequenceName", value = "sequence"),
	            @org.hibernate.annotations.Parameter(name = "allocationSize", value = "1"),
	    })
	    @GeneratedValue(generator = "sequence", strategy=GenerationType.SEQUENCE)
	
	private Integer userId;	
	private String firtsName;
	private String lastName;
	private String emailId;
	private Long phno;
	private String gender;
	@CreationTimestamp
	private Date createDt;
	@UpdateTimestamp
	private Date UpdatedDt;
	
	private String createdby;
	private String uopdatedBy;
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public String getFirtsName() {
		return firtsName;
	}
	public void setFirtsName(String firtsName) {
		this.firtsName = firtsName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getEmailId() {
		return emailId;
	}
	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}
	public Long getPhno() {
		return phno;
	}
	public void setPhno(Long phno) {
		this.phno = phno;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
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
