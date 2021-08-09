package com.ey.in.tds.returns.domain.tcs;

import java.util.List;

import com.ey.in.tds.returns.domain.AbstractFilingBean;

public class TCSFilingFileBean extends AbstractFilingBean{

	
	private static final long serialVersionUID = -6798514215424857901L;

	private TCSFilingHeaderBean headerBean;
	private TCSFilingBatchHeaderBean batchHeaderBean;
	private List<TCSFilingChallanDetailBean> challanDetailBeanList;
	public TCSFilingHeaderBean getHeaderBean() {
		return headerBean;
	}
	public void setHeaderBean(TCSFilingHeaderBean headerBean) {
		this.headerBean = headerBean;
	}
	public TCSFilingBatchHeaderBean getBatchHeaderBean() {
		return batchHeaderBean;
	}
	public void setBatchHeaderBean(TCSFilingBatchHeaderBean batchHeaderBean) {
		this.batchHeaderBean = batchHeaderBean;
	}
	public List<TCSFilingChallanDetailBean> getChallanDetailBeanList() {
		return challanDetailBeanList;
	}
	public void setChallanDetailBeanList(List<TCSFilingChallanDetailBean> challanDetailBeanList) {
		this.challanDetailBeanList = challanDetailBeanList;
	}
	@Override
	public String toString() {
		return "TCSFilingFileBean [headerBean=" + headerBean + ", batchHeaderBean=" + batchHeaderBean
				+ ", challanDetailBeanList=" + challanDetailBeanList + "]";
	}
	
	
}
