package com.ey.in.tds.service.util.excel;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aspose.cells.BackgroundType;
import com.aspose.cells.Cell;
import com.aspose.cells.Color;
import com.aspose.cells.Style;
import com.aspose.cells.Worksheet;
import com.ey.in.tds.service.sac.FieldMapping;


public abstract class AbstractExcel<T> {

	public static final Collection<? extends String> STANDARD_ADDITIONAL_HEADERS = Arrays.asList("Deductor TAN",
			"ERROR MESSAGE", "SEQUENCE NUMBER");
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final SimpleDateFormat defaultDateFormatter = new SimpleDateFormat("dd/MM/yyyy");

	private XSSFSheet sheet;
	private List<String> headers = new LinkedList<>();
	private List<String> _headersLowerCase = new LinkedList<>();
	private long dataRowsCount = 0;
	private Class<T> entityClass;

	private Map<String, Field> entityDbColumnToFieldMap = new HashMap<>();


	public static final BiFunction<String, String, String> VALIDATION_MANDATORY = (header,
			value) -> StringUtils.isEmpty(value) ? "'" + header + "' is mandatory" : null;

	public static final BiFunction<Double, Double, String> VALIDATION_MANDATORY_DOUBLE = (header,
			value) -> value == null ? "'" + header + "' is mandatory" : null;

	// For Date
	public static final BiFunction<Date, Date, String> VALIDATION_MANDATORY_DATE = (header,
			value) -> value == null ? "'" + header + "' is mandatory" : null;

	public AbstractExcel(XSSFWorkbook workbook, List<FieldMapping> fieldMappings, Class<T> entityClass) {
		this.sheet = workbook.getSheetAt(0);
		this.entityClass = entityClass;


		prepareDbColumnToFieldMaps();

		prepareHeaders(fieldMappings);

		prepareDataRowsCount();
	}

	private void prepareDbColumnToFieldMaps() {
		for (Field field : entityClass.getDeclaredFields()) {
			String fieldName = field.getName();
			if (fieldName != null) {
				field.setAccessible(true);
				entityDbColumnToFieldMap.put(fieldName, field);
			}
		}

	}

	private void prepareDataRowsCount() {
		long dataRowsCount = 0;
		for (int rowIndex = 1; rowIndex < sheet.getPhysicalNumberOfRows(); rowIndex++) {
			if (isRowCompletelyEmpty(rowIndex)) {
				break;
			} else {
				dataRowsCount++;
			}
		}
		this.dataRowsCount = dataRowsCount;
	}

	private void prepareHeaders(List<FieldMapping> fieldMappings) {

		XSSFRow headerRow = sheet.getRow(0);

		int actualHeadersCount = getNonEmptyCellsCount(headerRow);
		int expectedHeadersCount = fieldMappings.size();

		if (actualHeadersCount != expectedHeadersCount) {
			throw new RuntimeException(
					"Expecting " + expectedHeadersCount + " headers but the uploaded file has " + actualHeadersCount);
		}

		for (int index = 0; index < expectedHeadersCount; index++) {
			String header = headerRow.getCell(index).getStringCellValue();
			this.headers.add(header != null ? header.trim() : null);
			// Maintain lowercase headers for internal use especially for `indexOf`
			this._headersLowerCase.add(header != null ? header.trim().toLowerCase() : null);
		}

		fieldMappings.stream().forEach(fieldMapping -> {
			if (!this._headersLowerCase.contains(fieldMapping.getExcelHeaderName().toLowerCase())) {
				throw new RuntimeException(String.format("Expected header : %s is not found in excel headers %s ",
						fieldMapping.getExcelHeaderName(), Arrays.asList(this.headers).toString()));
			}
		});
	}

	public long getDataRowsCount() {
		return this.dataRowsCount;
	}

	public XSSFSheet getSheet() {
		return sheet;
	}

	public List<String> getHeaders() {
		return Collections.unmodifiableList(headers);
	}

	public int getHeaderIndex(String header) {
		return header != null ? this._headersLowerCase.indexOf(header.toLowerCase()) : -1;
	}

	protected void populateValue(Object entityObject, int excelRowIndex, FieldMapping fieldMapping) {
		if (entityObject == null) {
			throw new RuntimeException("Unable to populate value as the passed-in entity is null");
		}

		if (fieldMapping != null) {
			if (fieldMapping.getValueObjectField() == null) {
				logger.info("Data is not populated from column : " + fieldMapping.getExcelHeaderName());
				return;
			}

			try {
				Field field;
				if (entityDbColumnToFieldMap.containsKey(fieldMapping.getValueObjectField())) {
					field = entityDbColumnToFieldMap.get(fieldMapping.getValueObjectField());
					logger.info("* Field Type *" + field.getType());
					logger.info("* Get Cell Value * " + getCellValue(excelRowIndex, fieldMapping, field.getType()));

					field.set(entityObject, getCellValue(excelRowIndex, fieldMapping, field.getType()));

				} else {
					logger.error("Unable to find field for db column name : " + fieldMapping.getDbColumnName());
					throw new RuntimeException(
							"Unable to find field for db column name : " + fieldMapping.getDbColumnName());
				}
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Unable to populate value for field " + fieldMapping.getExcelHeaderName(),
						e);
			}

		}
	}

	private Object getCellValue(int rowIndex, FieldMapping fieldMapping, Class<?> fieldType) {
		XSSFCell cell = getSheet().getRow(rowIndex).getCell(getHeaderIndex(fieldMapping.getExcelHeaderName()));

		if (fieldType == null) {
			throw new RuntimeException("Field type can not be null");
		}

		if (cell == null) {
			return null;
		}

		try {
			if ("String".equalsIgnoreCase(fieldType.getSimpleName())) {
				if (cell.getCellType().equals(CellType.NUMERIC)) {
					cell.setCellType(CellType.STRING);
				}
				String value = cell.getStringCellValue();
				value = (value != null) ? value.trim() : null; // Always trim the whitespace for string values
				return (fieldMapping.getDataProcessor() != null) ? fieldMapping.getDataProcessor().apply(value) : value;

			} else if ("Date".equalsIgnoreCase(fieldType.getSimpleName())) {

				Date date = null;
				if (cell.getCellType().equals(CellType.NUMERIC)) {
					date = DateUtil.getJavaDate((double) cell.getNumericCellValue());
				} else if (cell.getCellType().equals(CellType.STRING)) {
					date = defaultDateFormatter.parse(cell.getStringCellValue());
				} else {
					date = cell.getDateCellValue();
				}
				return (fieldMapping.getDataProcessor() != null) ? fieldMapping.getDataProcessor().apply(date) : date;

			} else if ("int".equalsIgnoreCase(fieldType.getSimpleName())
					|| ("Integer".equalsIgnoreCase(fieldType.getSimpleName()))) {

				if (cell.getCellType().equals(CellType.STRING)) {
					cell.setCellType(CellType.NUMERIC);
				}
				int intValue = (int) cell.getNumericCellValue();
				return (fieldMapping.getDataProcessor() != null) ? fieldMapping.getDataProcessor().apply(intValue)
						: intValue;

			} else if ("long".equalsIgnoreCase(fieldType.getSimpleName())
					|| ("Long".equalsIgnoreCase(fieldType.getSimpleName()))) {
				long longValue = (long) cell.getNumericCellValue();
				return (fieldMapping.getDataProcessor() != null) ? fieldMapping.getDataProcessor().apply(longValue)
						: longValue;

			} else if ("double".equalsIgnoreCase(fieldType.getSimpleName())
					|| ("Double".equalsIgnoreCase(fieldType.getSimpleName()))) {

				if (cell.getCellType().equals(CellType.STRING)) {
					double doubleValue = Double.parseDouble(cell.getStringCellValue());
					return (fieldMapping.getDataProcessor() != null)
							? fieldMapping.getDataProcessor().apply(doubleValue)
							: doubleValue;

				} else {
					double doubleValue = cell.getNumericCellValue();
					return (fieldMapping.getDataProcessor() != null)
							? fieldMapping.getDataProcessor().apply(doubleValue)
							: doubleValue;
				}

			} else if ("BigDecimal".equalsIgnoreCase(fieldType.getSimpleName())) {
				if (cell.getCellType().equals(CellType.STRING)) {
					double doubleValue = Double.parseDouble(cell.getStringCellValue());
					return (fieldMapping.getDataProcessor() != null)
							? fieldMapping.getDataProcessor().apply(BigDecimal.valueOf(doubleValue))
							: BigDecimal.valueOf(doubleValue);

				} else {
					double doubleValue = cell.getNumericCellValue();
					return (fieldMapping.getDataProcessor() != null)
							? fieldMapping.getDataProcessor().apply(BigDecimal.valueOf(doubleValue))
							: BigDecimal.valueOf(doubleValue);
				}

			} else if ("float".equalsIgnoreCase(fieldType.getSimpleName())
					|| ("Float".equalsIgnoreCase(fieldType.getSimpleName()))) {
				double floatValue = (float) cell.getNumericCellValue();
				return (fieldMapping.getDataProcessor() != null) ? fieldMapping.getDataProcessor().apply(floatValue)
						: floatValue;

			} else if ("boolean".equalsIgnoreCase(fieldType.getSimpleName())
					|| ("Boolean".equalsIgnoreCase(fieldType.getSimpleName()))) {
				String value = cell.getStringCellValue();
				value = (value != null) ? value.trim() : null;

				return (value != null) && ("y".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value)
						|| "1".equalsIgnoreCase(value) || Boolean.parseBoolean(value));

			} else if ("UUID".equalsIgnoreCase(fieldType.getSimpleName())) {
				String value = cell.getStringCellValue();

				value = (value != null) ? value.trim() : null; // Always trim the whitespace for string values
				UUID id = UUID.fromString(value);
				return (fieldMapping.getDataProcessor() != null) ? fieldMapping.getDataProcessor().apply(id) : id;
			}

			else {
				throw new RuntimeException("Mapping not defined for property : " + fieldMapping + " and field : "
						+ fieldType.getSimpleName());
			}
		} catch (Exception e) {
			throw new RuntimeException("Encountered an error while reading value from uploaded file for row index :"
					+ rowIndex + " field : " + fieldMapping.getExcelHeaderName() + ". The value type expected is "
					+ fieldType.getSimpleName());
		}
	}

	public abstract T get(int index);

	public String getRawCellValue(int rowIndex, String columnHeader) {
		XSSFCell cell = getSheet().getRow(rowIndex).getCell(getHeaderIndex(columnHeader.toLowerCase()));
		if (cell == null) {
			return null;
		}
		String rawValue;
		try {
			if (cell.getCellType().equals(CellType.NUMERIC)) {
				if (DateUtil.isCellDateFormatted(cell)) {
					rawValue = cell.getDateCellValue().toString();
				} else {
					cell.setCellType(CellType.STRING);
					rawValue = cell.getStringCellValue();
				}
			} else {
				rawValue = cell.getStringCellValue();
			}
			return rawValue != null ? rawValue.trim() : "";
		} catch (Exception e) {
			throw new RuntimeException("Encountered an error while getting raw string value for row : " + rowIndex
					+ " and column : " + columnHeader, e);
		}
	}

	public static int getNonEmptyCellsCount(XSSFRow row) {
		int nonEmptyCellCount = 0;
		for (int i = 0; i < row.getPhysicalNumberOfCells(); i++) {
			XSSFCell cell = row.getCell(i);
			if (cell != null && StringUtils.isNotEmpty(cell.getStringCellValue())) {
				nonEmptyCellCount++;
			} else {
				break;
			}
		}
		return nonEmptyCellCount;
	}

	public boolean isRowCompletelyEmpty(int rowIndex) {
		XSSFRow row = sheet.getRow(rowIndex);
		if (row == null) {
			return true;
		}
		for (int i = 0; i < this.headers.size(); i++) {
			if (row.getCell(i) != null && StringUtils.isNotEmpty(getRawCellValue(rowIndex, this.headers.get(i)))) {
				return false;
			}
		}
		return true;
	}

	public static void colorCodeErrorCells(Worksheet worksheet, int rowIndex, List<String> headerNames,
			String errorDescription) {

		String[] errorMessages = errorDescription.split("\n");

		for (String errorMessage : errorMessages) {
			int headerStart = errorMessage.indexOf("'") + 1;
			int headerEnd = errorMessage.indexOf("'", headerStart);
			if (headerStart >= 0 && headerEnd > 0) {
				String header = errorMessage.substring(headerStart, headerEnd);
				Cell cell = worksheet.getCells().get(rowIndex, headerNames.indexOf(header));
				Style style = cell.getStyle();
				style.setForegroundColor(Color.fromArgb(255, 0, 0));
				style.setPattern(BackgroundType.SOLID);
				cell.setStyle(style);
			}
		}
	}

	public abstract List<FieldMapping> getFieldMappings();

	public static ArrayList<String> getValues(Object valueObject, List<FieldMapping> fieldMappings,
			List<String> headerNames) {
		String[] values = new String[headerNames.size()];
		Arrays.fill(values, "");

		List<String> smallCaseHeaderNames = headerNames.stream().map(String::toLowerCase).collect(Collectors.toList());

		for (FieldMapping fieldMapping : fieldMappings) {

			Field field = null;
			try {
				if (fieldMapping.getValueObjectField() != null) {
					field = valueObject.getClass().getDeclaredField(fieldMapping.getValueObjectField());
					field.setAccessible(true);

					Object actValue = field.get(valueObject);

					if (actValue != null) {
						if (actValue instanceof String) {
							values[smallCaseHeaderNames
									.indexOf(fieldMapping.getExcelHeaderName().toLowerCase())] = (String) actValue;
						} else {
							throw new RuntimeException(
									"Unsupported value found in class : " + valueObject.getClass().getSimpleName()
											+ " for field : " + fieldMapping.getValueObjectField()
											+ ". Value is of type " + actValue.getClass().getSimpleName());
						}
					}
				} else {
					throw new RuntimeException(
							"Missing value from header : '" + fieldMapping.getExcelHeaderName() + "'");
				}
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Unable to set value for field : '" + field.getName() + "' from header : '"
						+ fieldMapping.getExcelHeaderName() + "'", e);
			} catch (NoSuchFieldException e) {
				throw new RuntimeException(
						"Unable to get field for header : '" + fieldMapping.getExcelHeaderName() + "'", e);
			}

		}

		return new ArrayList<>(Arrays.asList(values));
	}
}
