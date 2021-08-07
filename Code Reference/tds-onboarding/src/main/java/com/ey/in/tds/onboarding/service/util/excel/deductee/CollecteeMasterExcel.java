package com.ey.in.tds.onboarding.service.util.excel.deductee;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ey.in.tds.common.model.deductee.CollecteeMasterErrorReportCsvDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.CollecteeMaster;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.common.util.excel.FieldMapping;

/**
 * 
 * @author vamsir
 *
 */
public class CollecteeMasterExcel extends Excel<CollecteeMaster, CollecteeMasterErrorReportCsvDTO> {

	private static final String HEADER_SOURCE_IDENTIFIER = "Source Identifier";
	private static final String HEADER_SOURCE_FILE_NAME = "Source File Name";
	private static final String HEADER_NAME_OF_THE_COLLECTOR = "Name of the collector";
	private static final String HEADER_COLLECTOR_CODE = "Collector Code";
	private static final String HEADER_COLLECTOR_PAN = "Collector PAN";
	private static final String HEADER_COLLECTOR_TAN = "Collector TAN";
	private static final String HEADER_NON_RESIDENT_COLLECTEE_INDICATOR = "Non-Resident Collectee Indicator";
	private static final String HEADER_COLLECTEE_CODE = "Collectee Code";
	private static final String HEADER_NAME_OF_THE_COLLECTEE = "Name of the collectee";
	private static final String HEADER_COLLECTEE_PAN = "Collectee PAN";
	private static final String HEADER_COLLECTEE_AADHAR_NUMBER = "Collectee Aadhar number";
	private static final String HEADER_GSTIN_NUMBER = "GSTIN Number";
	private static final String HEADER_COLLECTEE_TYPE = "Collectee Type";
	private static final String HEADER_DISTRIBUTION_CHANNEL = "Distribution channel";
	private static final String HEADER_EMAIL_ADDRESS = "E-Mail Address";
	private static final String HEADER_PHONE_NUMBER = "Phone Number";
	private static final String HEADER_FLAT_DOOR_NO = "Flat/Door/Block No";
	private static final String HEADER_NAME_BUILDING_VILLAGE = "Name of the Premises/Building/Village";
	private static final String HEADER_ROAD_STREET = "Road/ Street / Post Office";
	private static final String HEADER_AREA_LOCALITY = "Area/Locality";
	private static final String HEADER_TOWN_DISTRICT = "Town/ City/ District";
	private static final String HEADER_STATE = "State";
	private static final String HEADER_COUNTRY = "Country";
	private static final String HEADER_PIN_CODE = "Pin Code";
	private static final String HEADER_TCS_SECTION = "TCS Section";
	private static final String HEADER_TCS_RATE = "TCS Rate";
	private static final String HEADER_TAX_CODE = "Tax Code";
	private static final String HEADER_TDS_INDICATOR = "TDS Indicator";
	private static final String HEADER_NATURE_OF_INCOME = "Nature of collection/ income";
	private static final String HEADER_APPLICABLE_FROM = "Applicable From";
	private static final String HEADER_APPLICABLE_TO = "Applicable To";
	private static final String HEADER_NO_COLLECTION_DECLARATION_AS_PER_FORM_27C = "No collection declaration as per form 27C";
	private static final String HEADER_NUMBER_AO_FOR_LOWER_COLLECTION_OF_TAX = "Number of the certificate u/s 206C issued by the AO for lower collection of tax";
	private static final String HEADER_BALANCES_FOR_SECTION_206C = "Balances as on 30 September 2020 for section 206C(1H)";
	private static final String HEADER_ADVANCE_BALANCES_FOR_SECTION_206C = "Advance Balances as on 30 September 2020 for section 206C(1H)";
	private static final String HEADER_COLLECTIONS_BALANCES_FOR_SECTION_206C = "Collections Balances as on 30 September 2020 for section 206C(1H)";
	private static final String HEADER_ADVANCES_AS_OF_MARCH = "Advances As Of March";
	private static final String HEADER_CURRENT_BALANCE_YEAR = "Current Balance Year";
	private static final String HEADER_CURRENT_BALANCE_MONTH = "Current Balance Month";
	private static final String HEADER_PREVIOUS_BALANCE_YEAR = "Previous Balance Year";
	private static final String HEADER_PREVIOUS_BALANCE_MONTH = "Previous Balance Month";
	private static final String HEADER_USER_DEFINED_FIELD_1 = "User Defined Field 1";
	private static final String HEADER_USER_DEFINED_FIELD_2 = "User Defined Field 2";
	private static final String HEADER_USER_DEFINED_FIELD_3 = "User Defined Field 3";
	private static final String HEADER_USER_DEFINED_FIELD_4 = "User Defined Field 4";
	private static final String HEADER_USER_DEFINED_FIELD_5 = "User Defined Field 5";

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public List<FieldMapping> getFieldMappings() {
		return fieldMappings;
	}

	public static List<FieldMapping> fieldMappings = Collections.unmodifiableList(Arrays.asList(
			new FieldMapping(HEADER_SOURCE_IDENTIFIER, "sourceIdentifier", "sourceIdentifier"),
			new FieldMapping(HEADER_SOURCE_FILE_NAME, "sourceFileName", "sourceFileName"),
			new FieldMapping(HEADER_NAME_OF_THE_COLLECTOR, "nameOfTheCollector", "nameOfTheCollector"),
			new FieldMapping(HEADER_COLLECTOR_CODE, "collectorCode", "collectorCode", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_COLLECTOR_PAN, "collectorPan", "collectorPan", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_COLLECTOR_TAN, "collectorTan", "collectorTan"),
			new FieldMapping(HEADER_NON_RESIDENT_COLLECTEE_INDICATOR, "nonResidentCollecteeIndicator",
					"nonResidentCollecteeIndicator", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_COLLECTEE_CODE, "collecteeCode", "collecteeCode", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_NAME_OF_THE_COLLECTEE, "nameOfTheCollectee", "nameOfTheCollectee",
					Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_COLLECTEE_PAN, "collecteePan", "collecteePan"),
			new FieldMapping(HEADER_COLLECTEE_AADHAR_NUMBER, "collecteeAadharNumber", "collecteeAadharNumber"),
			new FieldMapping(HEADER_GSTIN_NUMBER, "gstinNumber", "gstinNumber"),
			new FieldMapping(HEADER_COLLECTEE_TYPE, "collecteeType", "collecteeType", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_DISTRIBUTION_CHANNEL, "distributionChannel", "distributionChannel"),
			new FieldMapping(HEADER_EMAIL_ADDRESS, "emailAddress", "emailAddress"),
			new FieldMapping(HEADER_PHONE_NUMBER, "phoneNumber", "phoneNumber"),
			new FieldMapping(HEADER_FLAT_DOOR_NO, "flatDoorBlockNo", "flatDoorBlockNo"),
			new FieldMapping(HEADER_NAME_BUILDING_VILLAGE, "nameOfTheBuildingVillage", "nameOfTheBuildingVillage"),
			new FieldMapping(HEADER_ROAD_STREET, "roadStreetPostoffice", "roadStreetPostoffice"),
			new FieldMapping(HEADER_AREA_LOCALITY, "areaLocality", "areaLocality"),
			new FieldMapping(HEADER_TOWN_DISTRICT, "townCityDistrict", "townCityDistrict"),
			new FieldMapping(HEADER_STATE, "state", "state",
					(val) -> val != null ? ((String) val).toUpperCase() : null),
			new FieldMapping(HEADER_COUNTRY, "country", "country"),
			new FieldMapping(HEADER_PIN_CODE, "pinCode", "pinCode"),
			new FieldMapping(HEADER_TCS_SECTION, "tcsSection", "tcsSection"),
			new FieldMapping(HEADER_TCS_RATE, "tcsRate", "tcsRate"),
			new FieldMapping(HEADER_TAX_CODE, "tdsCode", "tdsCode"),
			new FieldMapping(HEADER_TDS_INDICATOR, "tdsIndicator", "tdsIndicator"),
			new FieldMapping(HEADER_NATURE_OF_INCOME, "natureOfIncome", "natureOfIncome"),
			new FieldMapping(HEADER_APPLICABLE_FROM, "applicableFrom", "applicableFrom", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_APPLICABLE_TO, "applicableTo", "applicableTo"),
			new FieldMapping(HEADER_NO_COLLECTION_DECLARATION_AS_PER_FORM_27C, "noCollectionDeclarationAsPerForm27c",
					"noCollectionDeclarationAsPerForm27c"),
			// TODO need to change
			new FieldMapping(HEADER_NUMBER_AO_FOR_LOWER_COLLECTION_OF_TAX, "balancesForSection206c",
					"balancesForSection206c"),
			new FieldMapping(HEADER_BALANCES_FOR_SECTION_206C, "balancesForSection206c", "balancesForSection206c"),
			new FieldMapping(HEADER_ADVANCE_BALANCES_FOR_SECTION_206C, "advanceBalancesForSection206c",
					"advanceBalancesForSection206c"),
			new FieldMapping(HEADER_COLLECTIONS_BALANCES_FOR_SECTION_206C, "collectionsBalancesForSection206c",
					"collectionsBalancesForSection206c"),
			new FieldMapping(HEADER_ADVANCES_AS_OF_MARCH, "advancesAsOfMarch", "advancesAsOfMarch"),
			new FieldMapping(HEADER_CURRENT_BALANCE_YEAR, "currentBalanceYear", "currentBalanceYear"),
			new FieldMapping(HEADER_CURRENT_BALANCE_MONTH, "currentBalanceMonth", "currentBalanceMonth"),
			new FieldMapping(HEADER_PREVIOUS_BALANCE_YEAR, "previousBalanceYear", "previousBalanceYear"),
			new FieldMapping(HEADER_PREVIOUS_BALANCE_MONTH, "previousBalanceMonth", "previousBalanceMonth"),
			new FieldMapping(HEADER_USER_DEFINED_FIELD_1, "userDefinedField1", "userDefinedField1"),
			new FieldMapping(HEADER_USER_DEFINED_FIELD_2, "userDefinedField2", "userDefinedField2"),
			new FieldMapping(HEADER_USER_DEFINED_FIELD_3, "userDefinedField3", "userDefinedField3"),
			new FieldMapping(HEADER_USER_DEFINED_FIELD_4, "userDefinedField4", "userDefinedField4"),
			new FieldMapping(HEADER_USER_DEFINED_FIELD_5, "userDefinedField5", "userDefinedField5")));

	public CollecteeMasterExcel(XSSFWorkbook workbook) {
		super(workbook, fieldMappings, CollecteeMaster.class, CollecteeMasterErrorReportCsvDTO.class);
	}

	@Override
	public CollecteeMaster get(int index) {
		CollecteeMaster collectee = new CollecteeMaster();
		for (FieldMapping fieldMapping : fieldMappings) {
			populateValue(collectee, index, fieldMapping);
		}
		return collectee;
	}

	public Optional<CollecteeMasterErrorReportCsvDTO> validate(int rowIndex) {
		StringJoiner errorMessages = new StringJoiner("\n");

		// validation check
		for (FieldMapping fieldMapping : fieldMappings) {
			logger.debug("Processing {}", fieldMapping.getExcelHeaderName());
			if (fieldMapping.getValidator() != null) {
				String validationMessage = fieldMapping.getValidator().apply(
						this.getHeaders().get(this.getHeaderIndex(fieldMapping.getExcelHeaderName().toLowerCase())),
						getRawCellValue(rowIndex, fieldMapping.getExcelHeaderName()));
				if (StringUtils.isNotEmpty(validationMessage)) {
					errorMessages.add(validationMessage);
				}
			}
			// ======================================================
			if (fieldMapping.getValidatorDouble() != null) {

				String validationMessage = getRawCellValue(rowIndex, fieldMapping.getExcelHeaderName());
				String headerNamee = this.getHeaders()
						.get(this.getHeaderIndex(fieldMapping.getExcelHeaderName().toLowerCase()));
				if (StringUtils.isBlank(validationMessage))
					errorMessages.add(headerNamee + " can not be empty");
			}
			// =======================================================
		}

		if (errorMessages.length() != 0) {
			CollecteeMasterErrorReportCsvDTO collecteeMasterErrorReportCsvDTO = this.getErrorDTO(rowIndex);
			collecteeMasterErrorReportCsvDTO.setReason(errorMessages.toString());
			return Optional.of(collecteeMasterErrorReportCsvDTO);
		} else {
			return Optional.empty();
		}
	}
}
