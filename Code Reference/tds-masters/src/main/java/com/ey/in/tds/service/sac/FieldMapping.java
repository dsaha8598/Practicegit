package com.ey.in.tds.service.sac;

import java.util.function.BiFunction;
import java.util.function.Function;

public class FieldMapping {

	private String excelHeaderName;
	private String valueObjectField;
	private String dbColumnName;
	private Function<Object, ?> processor;
	private BiFunction<String, String, String> validator;
	private BiFunction<Double, Double, String> validatorDouble;

	public FieldMapping(String excelHeaderName, String valueObjectField, String dbColumnName,
			BiFunction<String, String, String> validator, Function<Object, ?> processor) {
		this.excelHeaderName = excelHeaderName;
		this.valueObjectField = valueObjectField;
		this.dbColumnName = dbColumnName;
		this.processor = processor;
		this.validator = validator;
	}

	public FieldMapping(String excelHeaderName, String valueObjectField, String dbColumnName,
			BiFunction<String, String, String> validator) {
		this.excelHeaderName = excelHeaderName;
		this.valueObjectField = valueObjectField;
		this.dbColumnName = dbColumnName;
		this.validator = validator;
	}

	public FieldMapping(String excelHeaderName, String valueObjectField,
			BiFunction<Double, Double, String> validatorDouble, String dbColumnName) {
		this.excelHeaderName = excelHeaderName;
		this.valueObjectField = valueObjectField;
		this.dbColumnName = dbColumnName;
		this.validatorDouble = validatorDouble;
	}

	public FieldMapping(String excelHeaderName, String valueObjectField, String dbColumnName,
			Function<Object, ?> processor) {
		this.excelHeaderName = excelHeaderName;
		this.valueObjectField = valueObjectField;
		this.dbColumnName = dbColumnName;
		this.processor = processor;
	}

	public FieldMapping(String excelHeaderName, String valueObjectField, String dbColumnName) {
		this.excelHeaderName = excelHeaderName;
		this.valueObjectField = valueObjectField;
		this.dbColumnName = dbColumnName;
	}

	public FieldMapping(String excelHeaderName, String valueObjectField,
			BiFunction<String, String, String> validationMandatory) {
		this.excelHeaderName = excelHeaderName;
		this.valueObjectField = valueObjectField;
	}

	public FieldMapping(String excelHeaderName, String valueObjectField) {
		this.excelHeaderName = excelHeaderName;
		this.valueObjectField = valueObjectField;
	}

	public String getExcelHeaderName() {
		return excelHeaderName;
	}

	public String getDbColumnName() {
		return this.dbColumnName;
	}

	public Function<Object, ?> getDataProcessor() {
		return processor;
	}

	public BiFunction<String, String, String> getValidator() {
		return this.validator;
	}

	public String getValueObjectField() {
		return this.valueObjectField;
	}

	public BiFunction<Double, Double, String> getValidatorDouble() {
		return validatorDouble;
	}

	public void setValidatorDouble(BiFunction<Double, Double, String> validatorDouble) {
		this.validatorDouble = validatorDouble;
	}

	@Override
	public String toString() {
		return "HEADERS{" + "excelHeaderName='" + excelHeaderName + '\'' + ", valueObjectField=" + valueObjectField
				+ ", dbColumnName='" + dbColumnName + '\'' + ", processor=" + processor + ", validator=" + validator
				+ '}';
	}

}