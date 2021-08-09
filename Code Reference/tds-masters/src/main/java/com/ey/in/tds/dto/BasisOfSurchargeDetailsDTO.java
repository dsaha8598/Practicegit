package com.ey.in.tds.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class BasisOfSurchargeDetailsDTO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 478608019231118488L;

	private Long id;

	private Long natureOfPaymentMasterId;

	private String nature;

	private Long deducteeStatusId;

	private String status;

	private Long deducteeResidentialStatusId;

	private String residentStatus;

	private Long invoiceSlabFrom;
	private Long invoiceSlabTo;
	private BigDecimal rate;
	private Long shareholderCatagoryId;
    private Long shareholderTypeId;
	

	public String getNature() {
		return nature;
	}

	public void setNature(String nature) {
		this.nature = nature;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getResidentStatus() {
		return residentStatus;
	}

	public void setResidentStatus(String residentStatus) {
		this.residentStatus = residentStatus;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getNatureOfPaymentMasterId() {
		return natureOfPaymentMasterId;
	}

	public void setNatureOfPaymentMasterId(Long natureOfPaymentMasterId) {
		this.natureOfPaymentMasterId = natureOfPaymentMasterId;
	}

	public Long getDeducteeStatusId() {
		return deducteeStatusId;
	}

	public void setDeducteeStatusId(Long deducteeStatusId) {
		this.deducteeStatusId = deducteeStatusId;
	}

	public Long getDeducteeResidentialStatusId() {
		return deducteeResidentialStatusId;
	}

	public void setDeducteeResidentialStatusId(Long deducteeResidentialStatusId) {
		this.deducteeResidentialStatusId = deducteeResidentialStatusId;
	}

	public Long getInvoiceSlabFrom() {
		return invoiceSlabFrom;
	}

	public void setInvoiceSlabFrom(Long invoiceSlabFrom) {
		this.invoiceSlabFrom = invoiceSlabFrom;
	}

	public Long getInvoiceSlabTo() {
		return invoiceSlabTo;
	}

	public void setInvoiceSlabTo(Long invoiceSlabTo) {
		this.invoiceSlabTo = invoiceSlabTo;
	}

	public BigDecimal getRate() {
		return rate;
	}

	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}


	public Long getShareholderCatagoryId() {
		return shareholderCatagoryId;
	}

	public void setShareholderCatagoryId(Long shareholderCatagoryId) {
		this.shareholderCatagoryId = shareholderCatagoryId;
	}

	public Long getShareholderTypeId() {
		return shareholderTypeId;
	}

	public void setShareholderTypeId(Long shareholderTypeId) {
		this.shareholderTypeId = shareholderTypeId;
	}

	@Override
	public String toString() {
		return "BasisOfSurchargeDetailsDTO [id=" + id + ", natureOfPaymentMasterId=" + natureOfPaymentMasterId
				+ ", nature=" + nature + ", deducteeStatusId=" + deducteeStatusId + ", status=" + status
				+ ", deducteeResidentialStatusId=" + deducteeResidentialStatusId + ", residentStatus=" + residentStatus
				+ ", invoiceSlabFrom=" + invoiceSlabFrom + ", invoiceSlabTo=" + invoiceSlabTo + ", rate=" + rate + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((deducteeResidentialStatusId == null) ? 0 : deducteeResidentialStatusId.hashCode());
		result = prime * result + ((deducteeStatusId == null) ? 0 : deducteeStatusId.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((invoiceSlabFrom == null) ? 0 : invoiceSlabFrom.hashCode());
		result = prime * result + ((invoiceSlabTo == null) ? 0 : invoiceSlabTo.hashCode());
		result = prime * result + ((nature == null) ? 0 : nature.hashCode());
		result = prime * result + ((natureOfPaymentMasterId == null) ? 0 : natureOfPaymentMasterId.hashCode());
		result = prime * result + ((rate == null) ? 0 : rate.hashCode());
		result = prime * result + ((residentStatus == null) ? 0 : residentStatus.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BasisOfSurchargeDetailsDTO other = (BasisOfSurchargeDetailsDTO) obj;
		if (deducteeResidentialStatusId == null) {
			if (other.deducteeResidentialStatusId != null)
				return false;
		} else if (!deducteeResidentialStatusId.equals(other.deducteeResidentialStatusId))
			return false;
		if (deducteeStatusId == null) {
			if (other.deducteeStatusId != null)
				return false;
		} else if (!deducteeStatusId.equals(other.deducteeStatusId))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (invoiceSlabFrom == null) {
			if (other.invoiceSlabFrom != null)
				return false;
		} else if (!invoiceSlabFrom.equals(other.invoiceSlabFrom))
			return false;
		if (invoiceSlabTo == null) {
			if (other.invoiceSlabTo != null)
				return false;
		} else if (!invoiceSlabTo.equals(other.invoiceSlabTo))
			return false;
		if (nature == null) {
			if (other.nature != null)
				return false;
		} else if (!nature.equals(other.nature))
			return false;
		if (natureOfPaymentMasterId == null) {
			if (other.natureOfPaymentMasterId != null)
				return false;
		} else if (!natureOfPaymentMasterId.equals(other.natureOfPaymentMasterId))
			return false;
		if (rate == null) {
			if (other.rate != null)
				return false;
		} else if (!rate.equals(other.rate))
			return false;
		if (residentStatus == null) {
			if (other.residentStatus != null)
				return false;
		} else if (!residentStatus.equals(other.residentStatus))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		return true;
	}

}
