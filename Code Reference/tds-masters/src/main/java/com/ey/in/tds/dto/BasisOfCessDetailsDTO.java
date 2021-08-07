package com.ey.in.tds.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

public class BasisOfCessDetailsDTO implements Serializable {

	private static final long serialVersionUID = -6273075767634930406L;

	/**
	 * A DTO for the BasisOfCessDetails entity.
	 */

	private Long id;

	private Long deducteeStatusId;
	private String deducteeStatus;

	private Long deducteeResidentialStatusId;

	private String deducteeResidentialStatus;

	private Long invoiceSlabFrom;
	private Long invoiceSlabTo;

	@NotNull(message = "nature of payment id is mandatory")
	private Long natureOfPaymentMasterId;

	private String nature;

	private BigDecimal rate;

	private Long cessTypeId;

	private String cessTypeName;

	private Boolean active;

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getDeducteeStatusId() {
		return deducteeStatusId;
	}

	public void setDeducteeStatusId(Long deducteeStatusId) {
		this.deducteeStatusId = deducteeStatusId;
	}

	public String getDeducteeStatus() {
		return deducteeStatus;
	}

	public void setDeducteeStatus(String deducteeStatus) {
		this.deducteeStatus = deducteeStatus;
	}

	public Long getDeducteeResidentialStatusId() {
		return deducteeResidentialStatusId;
	}

	public void setDeducteeResidentialStatusId(Long deducteeResidentialStatusId) {
		this.deducteeResidentialStatusId = deducteeResidentialStatusId;
	}

	public String getDeducteeResidentialStatus() {
		return deducteeResidentialStatus;
	}

	public void setDeducteeResidentialStatus(String deducteeResidentialStatus) {
		this.deducteeResidentialStatus = deducteeResidentialStatus;
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

	public Long getNatureOfPaymentMasterId() {
		return natureOfPaymentMasterId;
	}

	public void setNatureOfPaymentMasterId(Long natureOfPaymentMasterId) {
		this.natureOfPaymentMasterId = natureOfPaymentMasterId;
	}

	public String getNature() {
		return nature;
	}

	public void setNature(String nature) {
		this.nature = nature;
	}

	public BigDecimal getRate() {
		return rate;
	}

	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}

	public Long getCessTypeId() {
		return cessTypeId;
	}

	public void setCessTypeId(Long cessTypeId) {
		this.cessTypeId = cessTypeId;
	}

	public String getCessTypeName() {
		return cessTypeName;
	}

	public void setCessTypeName(String cessTypeName) {
		this.cessTypeName = cessTypeName;
	}

	@Override
	public String toString() {
		return "BasisOfCessDetailsDTO [id=" + id + ", deducteeStatusId=" + deducteeStatusId + ", deducteeStatus="
				+ deducteeStatus + ", deducteeResidentialStatusId=" + deducteeResidentialStatusId
				+ ", deducteeResidentialStatus=" + deducteeResidentialStatus + ", invoiceSlabFrom=" + invoiceSlabFrom
				+ ", invoiceSlabTo=" + invoiceSlabTo + ", natureOfPaymentMasterId=" + natureOfPaymentMasterId
				+ ", nature=" + nature + ", rate=" + rate + ", cessTypeId=" + cessTypeId + ", cessTypeName="
				+ cessTypeName + ", active=" + active + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((active == null) ? 0 : active.hashCode());
		result = prime * result + ((cessTypeId == null) ? 0 : cessTypeId.hashCode());
		result = prime * result + ((cessTypeName == null) ? 0 : cessTypeName.hashCode());
		result = prime * result + ((deducteeResidentialStatus == null) ? 0 : deducteeResidentialStatus.hashCode());
		result = prime * result + ((deducteeResidentialStatusId == null) ? 0 : deducteeResidentialStatusId.hashCode());
		result = prime * result + ((deducteeStatus == null) ? 0 : deducteeStatus.hashCode());
		result = prime * result + ((deducteeStatusId == null) ? 0 : deducteeStatusId.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((invoiceSlabFrom == null) ? 0 : invoiceSlabFrom.hashCode());
		result = prime * result + ((invoiceSlabTo == null) ? 0 : invoiceSlabTo.hashCode());
		result = prime * result + ((nature == null) ? 0 : nature.hashCode());
		result = prime * result + ((natureOfPaymentMasterId == null) ? 0 : natureOfPaymentMasterId.hashCode());
		result = prime * result + ((rate == null) ? 0 : rate.hashCode());
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
		BasisOfCessDetailsDTO other = (BasisOfCessDetailsDTO) obj;
		if (active == null) {
			if (other.active != null)
				return false;
		} else if (!active.equals(other.active))
			return false;
		if (cessTypeId == null) {
			if (other.cessTypeId != null)
				return false;
		} else if (!cessTypeId.equals(other.cessTypeId))
			return false;
		if (cessTypeName == null) {
			if (other.cessTypeName != null)
				return false;
		} else if (!cessTypeName.equals(other.cessTypeName))
			return false;
		if (deducteeResidentialStatus == null) {
			if (other.deducteeResidentialStatus != null)
				return false;
		} else if (!deducteeResidentialStatus.equals(other.deducteeResidentialStatus))
			return false;
		if (deducteeResidentialStatusId == null) {
			if (other.deducteeResidentialStatusId != null)
				return false;
		} else if (!deducteeResidentialStatusId.equals(other.deducteeResidentialStatusId))
			return false;
		if (deducteeStatus == null) {
			if (other.deducteeStatus != null)
				return false;
		} else if (!deducteeStatus.equals(other.deducteeStatus))
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
		return true;
	}
}
