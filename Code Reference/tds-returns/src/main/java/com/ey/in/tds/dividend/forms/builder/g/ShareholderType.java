package com.ey.in.tds.dividend.forms.builder.g;

public enum ShareholderType {

//	Company
//	Person (individual)
//	Hindu Undivided Family (HUF)
//	Firm/ LLP
//	Association of Persons (AOP)
//	Trust
//	Body of Individuals (BOI)
//	Local Authority	
//	Artificial Juridical Person
//	Government Agency

	INDIVIDUAL("1", "Person (individual)"), HUF("2", "Hindu Undivided Family (HUF)"),
	AOP_BOI("3", "Association of Persons (AOP)"), AOP_TRUST("4", "Trust"), LOCAL_AUTHORITY("5", "Local Authority"),
	ARTIFICIAL_JURIDICAL_PERSON("6", "Artificial Juridical Person"),
	CO_OPERATIVE_SOCIETY("7", "Body of Individuals (BOI)"), CO_OPERATIVE_BANK("8", "Government Agency"),
	PRIVATE_DISCRETIONARY_TRUST("9", "Trust");

	private final String numCode;

	private final String shareholderType;

	private ShareholderType(final String numCode, final String shareholderType) {
		this.numCode = numCode;
		this.shareholderType = shareholderType;
	}

	public static ShareholderType byShareholderType(String shareholderType) {
		for (ShareholderType status : ShareholderType.values()) {
			if (status.shareholderType.equalsIgnoreCase(shareholderType))
				return status;
		}
		throw new IllegalArgumentException("Invalid Shareholder Type: " + shareholderType);
	}

	public String numCode() {
		return this.numCode;
	}

	public String shareholderType() {
		return this.shareholderType;
	}
}
