package com.ey.in.tds.ingestion.dto.invoice;

public class InvoiceCrDtO {

	private boolean found_section;
	private boolean multiple_sections;
	private boolean amount_less;
	private boolean reject_result;

	public boolean isFound_section() {
		return found_section;
	}

	public void setFound_section(boolean found_section) {
		this.found_section = found_section;
	}

	public boolean isMultiple_sections() {
		return multiple_sections;
	}

	public void setMultiple_sections(boolean multiple_sections) {
		this.multiple_sections = multiple_sections;
	}

	public boolean isAmount_less() {
		return amount_less;
	}

	public void setAmount_less(boolean amount_less) {
		this.amount_less = amount_less;
	}

	public boolean isReject_result() {
		return reject_result;
	}

	public void setReject_result(boolean reject_result) {
		this.reject_result = reject_result;
	}

}
