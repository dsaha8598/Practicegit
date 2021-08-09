package com.ey.in.tds.dividend.forms.builder.ca;

public enum PartType {

	A("PA"), B("PB"), C("PC");

	private final String typeName;

	private PartType(final String typeName) {
		this.typeName = typeName;
	}

	public String typeName() {
		return this.typeName;
	}

	public static PartType byTypeName(String typeName) {
		for (PartType partType : PartType.values()) {
			if (partType.typeName.equalsIgnoreCase(typeName)) {
				return partType;
			}
		}
		throw new IllegalArgumentException("Invalid typeName: " + typeName);
	}
}
