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

import com.ey.in.tds.common.model.deductee.DeducteeMasterResidentialErrorReportCsvDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeducteeMasterResidential;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.common.util.excel.FieldMapping;

/**
 * 
 * @author scriptbees.
 *
 */
public class ResidentDeducteeExcel
		extends Excel<DeducteeMasterResidential, DeducteeMasterResidentialErrorReportCsvDTO> {

	private static final String HEADER_SOURCE_IDENTIFIER = "SourceIdentifier";
	private static final String HEADER_SOURCE_FILE_NAME = "SourceFileName";
	private static final String HEADER_NAME_OF_THE_COMPANY = "DeductorName";
	private static final String HEADER_COMPANY_CODE = "DeductorCode";
	private static final String HEADER_DEDUCTOR_PAN = "DeductorPAN";
	private static final String HEADER_DEDUCTOR_TAN = "DeductorTAN";
	private static final String HEADER_DEDUCTEE_RESIDENTIAL_STATUS = "DeducteeResidentialStatus";
	private static final String HEADER_DEDUCTEE_CODE = "DeducteeCode";
	private static final String HEADER_DEDUCTEE_NAME = "DeducteeName";
	private static final String HEADER_DEDUCTEE_PAN = "DeducteePAN";
	private static final String HEADER_DEDUCTEE_STATUS = "DeducteeStatus";
	private static final String HEADER_EMAIL_ADDRESS = "DeducteeEmail";
	private static final String HEADER_PHONE_NUMBER = "DeducteePhone";
	private static final String HEADER_FLAT_DOOR_NO = "DeducteeFloorNumber";
	private static final String HEADER_NAME_BUILDING_VILLAGE = "DeducteeBuildingName";
	private static final String HEADER_ROAD_STREET = "DeducteeStreet";
	private static final String HEADER_AREA_LOCALITY = "DeducteeArea";
	private static final String HEADER_TOWN_DISTRICT = "DeducteeTown";
	private static final String HEADER_STATE = "DeducteeState";
	private static final String HEADER_COUNTRY = "DeducteeCountry";
	private static final String HEADER_PIN_CODE = "DeducteePincode";
	private static final String HEADER_SECTION = "TDSSection";
	private static final String HEADER_NATURE_OF_PAYMENT = "NatureOfPayment";
	private static final String HEADER_RATE = "TDSRate";
	private static final String HEADER_APPLICABLE_FROM = "ApplicableFrom";
	private static final String HEADER_APPLICABLE_TO = "ApplicableTo";
	private static final String HEADER_USER_DEFINED_1 = "UserDefinedField1";
	private static final String HEADER_USER_DEFINED_2 = "UserDefinedField2";
	private static final String HEADER_USER_DEFINED_3 = "UserDefinedField3";
	private static final String HEADER_DEDUCTEE_AADHAR_NUMBER = "DeducteeAadharNumber";
	private static final String HEADER_SECTION_CODE = "TDSTaxCodeERP";
	private static final String HEADER_THRESHOLD_LIMIT_APPLICABLE = "TDSThresholdApplicabilityFlag";
	private static final String HEADER_TDS_EXCEMPTION_FLAG = "TDSExemptionFlag";
	private static final String HEADER_TDS_EXCEMPTION_REASON = "TDSExemptionReason";
	private static final String HEADER_DEDUCTEE_MASTER_BALANCES_OF_194Q = "DeducteeMasterBalancesOf194Q";
	private static final String HEADER_ADVANCE_BALANCES_OF_194Q = "AdvanceBalancesOf194Q";
	private static final String HEADER_PROVISION_BALANCES_OF_194Q = "ProvisionBalancesOf194Q";
	private static final String HEADER_CURRENT_BALANCE_MONTH = "CurrentBalanceMonth";
	private static final String HEADER_CURRENT_BALANCE_YEAR = "CurrentBalanceYear";
	private static final String HEADER_PROVISIONS_AS_OF_MARCH = "ProvisionsAsOfMarch";
	private static final String HEADER_ADVANCES_AS_OF_MARCH = "AdvancesAsOfMarch";
	private static final String HEADER_PREVIOUS_BALANCE_MONTH = "PreviousBalanceMonth";
	private static final String HEADER_PREVIOUS_BALANCE_YEAR = "PreviousBalanceYear";
	private static final String HEADER_TDSAPPLICABILITY_UNDER_SECTION = "TdsApplicabilityUnderSection";

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public List<FieldMapping> getFieldMappings() {
		return fieldMappings;
	}

	public static List<FieldMapping> fieldMappings = Collections.unmodifiableList(
			Arrays.asList(new FieldMapping(HEADER_SOURCE_IDENTIFIER, "sourceIdentifier", "sourceIdentifier"),
					new FieldMapping(HEADER_SOURCE_FILE_NAME, "sourceFileName", "sourceFileName"),
					new FieldMapping(HEADER_NAME_OF_THE_COMPANY, "nameOfTheCompanyCode", "nameOfTheCompanyCode"),
					new FieldMapping(HEADER_DEDUCTOR_PAN, "deductorPan", "deductorPan", Excel.VALIDATION_MANDATORY),
					new FieldMapping(HEADER_DEDUCTEE_RESIDENTIAL_STATUS, "deducteeResidentialStatus",
							"deducteeResidentialStatus",
							(header, value) -> StringUtils.isBlank(value)
									|| !(Arrays.asList("res", "resident").contains(value.toLowerCase()))
											? "'" + header + "' should be either " + "'RES' or 'RESIDENT'"
											: null,
							(val) -> "RES"),
					new FieldMapping(HEADER_DEDUCTEE_CODE, "deducteeCode", "deducteeCode"),
					new FieldMapping(HEADER_DEDUCTEE_NAME, "deducteeName", "deducteeName", Excel.VALIDATION_MANDATORY),
					new FieldMapping(HEADER_DEDUCTEE_PAN, "deducteePAN", "deducteePAN"),
					new FieldMapping(HEADER_DEDUCTEE_STATUS, "deducteeStatus", "deducteeStatus"),
					new FieldMapping(HEADER_EMAIL_ADDRESS, "emailAddress", "emailAddress"),
					new FieldMapping(HEADER_PHONE_NUMBER, "phoneNumber", "phoneNumber"),
					new FieldMapping(HEADER_FLAT_DOOR_NO, "flatDoorBlockNo", "flatDoorBlockNo"),
					new FieldMapping(HEADER_NAME_BUILDING_VILLAGE, "nameBuildingVillage", "nameBuildingVillage"),
					new FieldMapping(HEADER_ROAD_STREET, "roadStreetPostoffice", "roadStreetPostoffice"),
					new FieldMapping(HEADER_AREA_LOCALITY, "areaLocality", "areaLocality"),
					new FieldMapping(HEADER_TOWN_DISTRICT, "townCityDistrict", "townCityDistrict"),
					new FieldMapping(HEADER_STATE, "state", "state",
							(val) -> val != null ? ((String) val).toUpperCase() : null),
					new FieldMapping(HEADER_COUNTRY, "country", "country"),
					new FieldMapping(HEADER_PIN_CODE, "pinCode", "pinCode"),
					new FieldMapping(HEADER_SECTION, "section", "section"),
					new FieldMapping(HEADER_NATURE_OF_PAYMENT, "natureOfPayment", "natureOfPayment"),
					new FieldMapping(HEADER_RATE, "rate", "rate"),
					new FieldMapping(HEADER_APPLICABLE_FROM, "applicableFrom", "applicableFrom",
							Excel.VALIDATION_MANDATORY),
					new FieldMapping(HEADER_APPLICABLE_TO, "applicableTo", "applicableTo"),
					new FieldMapping(HEADER_USER_DEFINED_1, "userDefinedField1", "userDefinedField1"),
					new FieldMapping(HEADER_USER_DEFINED_2, "userDefinedField2", "userDefinedField2"),
					new FieldMapping(HEADER_USER_DEFINED_3, "userDefinedField3", "userDefinedField3"),
					new FieldMapping(HEADER_DEDUCTOR_TAN, "deductorTan", "deductorTan"),
					new FieldMapping(HEADER_COMPANY_CODE, "companyCode", "companyCode"),
					new FieldMapping(HEADER_DEDUCTEE_AADHAR_NUMBER, "deducteeAadharNumber", "deducteeAadharNumber"),
					new FieldMapping(HEADER_SECTION_CODE, "sectionCode", "sectionCode"),
					new FieldMapping(HEADER_THRESHOLD_LIMIT_APPLICABLE, "isThresholdLimitApplicable",
							"isThresholdLimitApplicable"),
					new FieldMapping(HEADER_TDS_EXCEMPTION_FLAG, "tdsExcemptionFlag", "tdsExcemptionFlag"),
					new FieldMapping(HEADER_TDS_EXCEMPTION_REASON, "tdsExcemptionReason", "tdsExcemptionReason"),
					new FieldMapping(HEADER_DEDUCTEE_MASTER_BALANCES_OF_194Q, "deducteeMasterBalancesOf194q",
							"deducteeMasterBalancesOf194q"),
					new FieldMapping(HEADER_ADVANCE_BALANCES_OF_194Q, "advanceBalancesOf194q", "advanceBalancesOf194q"),
					new FieldMapping(HEADER_PROVISION_BALANCES_OF_194Q, "provisionBalancesOf194q",
							"provisionBalancesOf194q"),
					new FieldMapping(HEADER_CURRENT_BALANCE_MONTH, "currentBalanceMonth", "currentBalanceMonth",
							Excel.VALIDATION_MANDATORY),
					new FieldMapping(HEADER_CURRENT_BALANCE_YEAR, "currentBalanceYear", "currentBalanceYear",
							Excel.VALIDATION_MANDATORY),
					new FieldMapping(HEADER_PROVISIONS_AS_OF_MARCH, "provisionsAsOfMarch", "provisionsAsOfMarch"),
					new FieldMapping(HEADER_ADVANCES_AS_OF_MARCH, "advancesAsOfMarch", "advancesAsOfMarch"),
					new FieldMapping(HEADER_PREVIOUS_BALANCE_MONTH, "previousBalanceMonth", "previousBalanceMonth",
							Excel.VALIDATION_MANDATORY),
					new FieldMapping(HEADER_PREVIOUS_BALANCE_YEAR, "previousBalanceYear", "previousBalanceYear",
							Excel.VALIDATION_MANDATORY),
					new FieldMapping(HEADER_TDSAPPLICABILITY_UNDER_SECTION, "tdsApplicabilityUnderSection",
							"tdsApplicabilityUnderSection")));

	public ResidentDeducteeExcel(XSSFWorkbook workbook) {
		super(workbook, fieldMappings, DeducteeMasterResidential.class,
				DeducteeMasterResidentialErrorReportCsvDTO.class);
	}

	public DeducteeType getType() {
		return DeducteeType.RESIDENT;
	}

	@Override
	public DeducteeMasterResidential get(int index) {
		DeducteeMasterResidential deductee = new DeducteeMasterResidential();
		for (FieldMapping fieldMapping : fieldMappings) {
			populateValue(deductee, index, fieldMapping);
		}
		return deductee;
	}

	public Optional<DeducteeMasterResidentialErrorReportCsvDTO> validate(int rowIndex) {
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
			DeducteeMasterResidentialErrorReportCsvDTO deducteeMasterResidentialErrorReportCsvDTO = this
					.getErrorDTO(rowIndex);
			deducteeMasterResidentialErrorReportCsvDTO.setReason(errorMessages.toString());
			return Optional.of(deducteeMasterResidentialErrorReportCsvDTO);
		} else {
			return Optional.empty();
		}
	}
}
