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
}
