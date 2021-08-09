package com.ey.in.tds.returns.domain;

import java.util.List;

public class FilingFileBean extends AbstractFilingBean {

	private static final long serialVersionUID = -1741235213987896042L;

	private FilingHeaderBean headerBean;
	private FilingBatchHeaderBean batchHeaderBean;
	private List<FilingChallanDetailBean> challanDetailBeanList;
	private List<String> challanDetailsStringList;

	public FilingHeaderBean getHeaderBean() {
		return headerBean;
	}

	public void setHeaderBean(FilingHeaderBean headerBean) {
		this.headerBean = headerBean;
	}

	public FilingBatchHeaderBean getBatchHeaderBean() {
		return batchHeaderBean;
	}

	public void setBatchHeaderBean(FilingBatchHeaderBean batchHeaderBean) {
		this.batchHeaderBean = batchHeaderBean;
	}

	public List<FilingChallanDetailBean> getChallanDetailBeanList() {
		return challanDetailBeanList;
	}

	public void setChallanDetailBeanList(List<FilingChallanDetailBean> challanDetailBeanList) {
		this.challanDetailBeanList = challanDetailBeanList;
	}
	

	public List<String> getChallanDetailsStringList() {
		return challanDetailsStringList;
	}

	public void setChallanDetailsStringList(List<String> challanDetailsStringList) {
		this.challanDetailsStringList = challanDetailsStringList;
	}

	@Override
	public String toString() {
		return "FilingFileBean [headerBean=" + headerBean + ", batchHeaderBean=" + batchHeaderBean
				+ ", challanDetailBeanList=" + challanDetailBeanList + "]";
	}
	
}
