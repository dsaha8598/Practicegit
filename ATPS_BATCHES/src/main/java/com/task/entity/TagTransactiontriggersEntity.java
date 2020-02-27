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
}
