package com.ey.in.tds.ingestion.dto.batch;

public class ReceiptBatchUploadDTO extends BatchUploadDTO {

	
	private static final long serialVersionUID = 1L;

	private int receiptMonth;
	
	private Integer receiptId;	

	public Integer getReceiptId() {
		return receiptId;
	}

	public void setReceiptId(Integer receiptId) {
		this.receiptId = receiptId;
	}

	public int getReceiptMonth() {
		return receiptMonth;
	}

	public void setReceiptMonth(int receiptMonth) {
		this.receiptMonth = receiptMonth;
	}

}
