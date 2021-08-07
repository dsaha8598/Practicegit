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

import com.ey.in.tds.common.model.deductee.DeducteeMasterNonResidentialErrorReportCsvDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeducteeMasterNonResidential;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.common.util.excel.FieldMapping;

/**
 * 
 * @author scriptbees.
 *
 */
public class NonResidentDeducteeExcel
		extends Excel<DeducteeMasterNonResidential, DeducteeMasterNonResidentialErrorReportCsvDTO> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final String HEADER_SOURCE_IDENTIFIER = "SOURCE IDENTIFIER";
	private static final String HEADER_SOURCE_FILE_NAME = "SOURCE FILE NAME";
	private static final String HEADER_COMPANY_CODE = "COMPANY CODE";
	private static final String HEADER_DEDUCTOR_PAN = "DEDUCTOR PAN";
	private static final String HEADER_DEDUCTOR_TAN = "DEDUCTOR TAN";
	private static final String HEADER_NAME_OF_THE_COMPANY = "NAME OF THE COMPANY";
	private static final String HEADER_DEDUCTEE_RESIDENTIAL_STATUS = "DEDUCTEE RESIDENTIAL STATUS";
	private static final String HEADER_DEDUCTEE_CODE = "DEDUCTEE CODE";
	private static final String HEADER_DEDUCTEE_NAME = "DEDUCTEE NAME";
	private static final String HEADER_DEDUCTEE_PAN = "DEDUCTEE PAN";
	private static final String HEADER_DEDUCTEE_TIN = "DEDUCTEE TIN";
	private static final String HEADER_DEDUCTEE_STATUS = "DEDUCTEE STATUS";
	private static final String HEADER_IS_TRC_AVAILABLE = "IS TRC AVAILABLE";
	private static final String HEADER_TRC_APPLICABLE_FROM = "TRC APPLICABLE FROM";
	private static final String HEADER_TRC_APPLICABLE_TO = "TRC APPLICABLE TO";
	private static final String HEADER_IS_TEN_F_AVAILABLE = "IS TEN F AVAILABLE";
	private static final String HEADER_IS_TEN_F_APPLICABLE_FROM = "IS TEN F APPLICABLE FROM";
	private static final String HEADER_IS_TEN_F_APPLICABLE_TO = "IS TEN F APPLICABLE TO";
	private static final String HEADER_NO_PE_DOCUMENT_AVAILABLE = "No PE DOCUMENT AVAILABLE";
	private static final String HEADER_NO_PE_DOCUMENT_APPLICABLE_FROM = "NO PE DOCUMENT APPLICABLE FROM";
	private static final String HEADER_NO_PE_DOCUMENT_APPLICABLE_TO = "NO PE DOCUMENT APPLICABLE TO";
	private static final String HEADER_IS_POEM_AVAILABLE = "IS POEM AVAILABLE";
	private static final String HEADER_POEM_APPLICABLE_FROM = "POEM APPLICABLE FROM";
	private static final String HEADER_POEM_APPLICABLE_TO = "POEM APPLICABLE TO";
	private static final String HEADER_IS_POEM_DECLARATION_IN_FUTURE = "ARE YOU LIKELY TO RECEIVE NO POEM DECLARATION IN FUTURE?";
	private static final String HEADER_POEM_FUTURE_DATE = "POEM FUTURE DATE";
	private static final String HEADER_EMAIL_ADDRESS = "EMAIL ADDRESS";
	private static final String HEADER_PHONE_NUMBER = "PHONE NUMBER";
	private static final String HEADER_FLAT_DOOR_NO = "FLAT DOOR NO";
	private static final String HEADER_NAME_BUILDING_VILLAGE = "NAME BUILDING VILLAGE";
	private static final String HEADER_ROAD_STREET = "ROAD STREET";
	private static final String HEADER_AREA_LOCALITY = "AREA LOCALITY";
	private static final String HEADER_TOWN_DISTRICT = "TOWN DISTRICT";
	private static final String HEADER_STATE = "STATE";
	private static final String HEADER_COUNTRY = "COUNTRY";
	private static final String HEADER_PIN_CODE = "PIN CODE";
	private static final String HEADER_SECTION = "SECTION";
	private static final String HEADER_RATE = "RATE";
	private static final String HEADER_APPLICABLE_FROM = "APPLICABLE FROM";
	private static final String HEADER_APPLICABLE_TO = "APPLICABLE TO";
	private static final String HEADER_USER_DEFINED_1 = "USER DEFINED 1";
	private static final String HEADER_USER_DEFINED_2 = "USER DEFINED 2";
	private static final String HEADER_USER_DEFINED_3 = "USER DEFINED 3";
	private static final String HEADER_DEDUCTEE_MASTER_RELATED_PARTY = "DEDUCTEE MASTER RELATED PARTY";
	private static final String HEADER_NR_COUNTRY_OF_RESIDENCE = "NR COUNTRY OF RESIDENCE";
	private static final String HEADER_IS_AMOUNT_CONNECTED_FIXED_BASE = "AMOUNT RECEIVED CONNECTED WITH FIXED BASE";
	private static final String HEADER_FIXED_BASE_AVAILBLE_INDIA_APPLICABLE_TO = "FIXEDBASE AVAILABLE INDIA APPLICABLE TO";
	private static final String HEADER_FIXEDBASE_AVAILBLE_INDIA_APPLICABLE_FROM = "FIXEDBASE AVAILABLE INDIA APPLICABLE FROM";
	private static final String HEADER_IS_FIXEDBASE_AVAILBLE_INDIA = "IS FIXEDBASE AVAILABLE INDIA";
	private static final String HEADER_WHETHER_PE_INDIA = "WHETHER PE INDIA";
	private static final String HEADER_WHETHER_PE_IN_INDIA_APPLICABLE_TO = "WHETHER PE IN INDIA APPLICABLE TO";
	private static final String HEADER_WHETHER_PE_IN_INDIA_APPLICABLE_FROM = "WHETHER PE IN INDIA APPLICABLE FROM";
	private static final String HEADER_IS_PE_AMOUNT_RECEIVED = "AMOUNT RECEIVED IS CONNECTED WITH PE";
	private static final String HEADER_IS_PE_INVOILVED_IN_PURCHASE_GOODS = "IS PE INVOLVED IN PURCHASE GOODS";
	private static final String HEADER_IS_BUSINESS_CARRIED_IN_INDIA = "IS BUSINESS CARRIED IN INDIA";
	private static final String HEADER_DEDUCTEE_MASTER_IS_GROSSING_UP = "DEDUCTEE MASTER IS GROSSING UP";
	private static final String HEADER_DEDUCTEE_MASTER_IS_DEDUCTEE_TRANSPARENT = "FISCALLY TRANSPARENT ENTITY";
	private static final String HEADER_DEDUCTEE_MASTER_IS_TRC_FUTURE = "DEDUCTEE MASTER IS TRC FUTURE";
	private static final String HEADER_DEDUCTEE_MASTER_TRC_FUTURE_DATE = "DEDUCTEE MASTER TRC FUTURE DATE";
	private static final String HEADER_DEDUCTEE_MASTER_TRC_AND_TENF_COUNTRY = "DEDUCTEE MASTER TRC AND TENF COUNTRY";
	private static final String HEADER_DEDUCTEE_MASTER_TRC_AND_TENF_TIN = "DEDUCTEE MASTER TRC AND TENF TIN";
	private static final String HEADER_DEDUCTEE_MASTER_TRC_AND_TENF_ADDRESS = "DEDUCTEE MASTER TRC AND TENF ADDRESS";
	private static final String HEADER_DEDUCTEE_MASTER_TRC_AND_TENF_PERIOD = "DEDUCTEE MASTER TRC AND TENF PERIOD";
	private static final String HEADER_DEDUCTEE_MASTER_TRC_AND_TENF_PAN = "DEDUCTEE MASTER TRC AND TENF PAN";
	private static final String HEADER_DEDUCTEE_MASTER_TRC_AND_TENF_STATUS = "DEDUCTEE MASTER TRC AND TENF STATUS";
	private static final String HEADER_DEDUCTEE_MASTER_IS_TENF_FUTURE = "DEDUCTEE MASTER IS TENF FUTURE";
	private static final String HEADER_DEDUCTEE_MASTER_TENF_FUTURE_DATE = "DEDUCTEE MASTER TENF FUTURE DATE";
	private static final String HEADER_PRINCIPLES_OF_BUSINESS_PLACE = "PRINCIPLES OF BUSINESS PLACE";
	private static final String HEADER_STAY_PERIOD_FINANCIAL_YEAR = "STAY PERIOD FINANCIAL YEAR";
	private static final String HEADER_NATURE_OF_PAYMENT = "NATURE OF PAYMENT";
	private static final String HEADER_COUNTRY_TO_REMITTANCE = "COUNTRY TO WHICH REMITTANCE IS MADE";
	private static final String HEADER_BENEFICIAL_OWNER_OF_INCOME = "BENEFICIAL OWNER OF INCOME";
	private static final String HEADER_IS_BENEFICIAL_OWNERSHIP_DECLARATION = "IS BENEFICIAL OWNERSHIP DECLARATION AVAILABLE";
	private static final String HEADER_MLI_PPT_CONDITION_SATISFIED = "MULTILATERAL INSTRUMENT PRINCIPLE PURPOSE TEST CONDITION SATISFIED";
	private static final String HEADER_MLI_SLOB_CONDITION_SATISFIED = "MULTILATERAL INSTRUMENT SIMPLIFIED LIMITATION ON BENEFIT";
	private static final String HEADER_IS_MLI_PPT_SLOB = "IS MLI-PPT SLOB SATISFACTION DECLARATION AVAILABLE";
	private static final String HEADER_NATURE_OF_REMITTANCE = "NATURE OF REMITTANCE";
	private static final String HEADER_ARTICLE_NUMBER_OF_DTAA = "ARTICLE NUMBER OF DTAA";
	private static final String HEADER_SECTION_OF_THE_INCOME_TAX_ACT = "SECTION OF THE INCOME TAX ACT 1961";
	private static final String HEADER_DO_YOU_HAVE_AGREEMENT_FOR_THE_TRANSACTION = "DO YOU HAVE AN AGREEMENT FOR THE TRANSACTION?";
	private static final String HEADER_ACCOUNTANT_NAME = "ACCOUNTANT NAME";
	private static final String HEADER_NAME_OF_PROPRIETORSHIP_FIRM = "NAME OF PROPRIETORSHIP FIRM";
	private static final String HEADER_CHARTERED_ACCOUNTANT_FLAT_DOOR_NO = "CHARTERED ACCOUNTANT FLAT DOOR NO";
	private static final String HEADER_CHARTERED_ACCOUNTANT_NAME_BUILDING_VILLAGE = "CHARTERED ACCOUNTANT NAME BUILDING VILLAGE";
	private static final String HEADER_CHARTERED_ACCOUNTANT_ROAD_STREET = "CHARTERED ACCOUNTANT ROAD STREET";
	private static final String HEADER_CHARTERED_ACCOUNTANT_AREA_LOCALITY = "CHARTERED ACCOUNTANT AREA LOCALITY";
	private static final String HEADER_CHARTERED_ACCOUNTANT_TOWN_DISTRICT = "CHARTERED ACCOUNTANT TOWN DISTRICT";
	private static final String HEADER_CHARTERED_ACCOUNTANT_STATE = "CHARTERED ACCOUNTANT STATE";
	private static final String HEADER_CHARTERED_ACCOUNTANT_COUNTRY = "CHARTERED ACCOUNTANT COUNTRY";
	private static final String HEADER_CHARTERED_ACCOUNTANT_PIN_CODE = "CHARTERED ACCOUNTANT PIN CODE";
	private static final String HEADER_MEMBERSHIP_NUMBER = "MEMBERSHIP NUMBER";
	private static final String HEADER_REGISTRATION_NUMBER = "REGISTRATION NUMBER";
	private static final String HEADER_DEDUCTEE_AADHAR_NUMBER = "DEDUCTEE AADHAR NUMBER";
	private static final String HEADER_THRESHOLD_LIMIT_APPLICABLE = "THRESHOLD LIMIT APPLICABLE (YES/NO)";
	private static final String HEADER_SECTION_CODE = "SECTION CODE";

	@Override
	public List<FieldMapping> getFieldMappings() {
		return fieldMappings;
	}

	public static List<FieldMapping> fieldMappings = Collections.unmodifiableList(
			Arrays.asList(new FieldMapping(HEADER_SOURCE_IDENTIFIER, "sourceIdentifier", "sourceIdentifier"),
					new FieldMapping(HEADER_SOURCE_FILE_NAME, "sourceFileName", "sourceFileName"),
					new FieldMapping(HEADER_NAME_OF_THE_COMPANY, "nameOfTheCompanyCode", "nameOfTheCompanyCode"),
					new FieldMapping(HEADER_COMPANY_CODE, "companyCode", "companyCode"),
					new FieldMapping(HEADER_DEDUCTOR_PAN, "deductorPan", "deductorPan", Excel.VALIDATION_MANDATORY),
					new FieldMapping(HEADER_DEDUCTOR_TAN, "deductorTan", "deductorTan"),
					new FieldMapping(HEADER_DEDUCTEE_RESIDENTIAL_STATUS, "deducteeResidentialStatus",
							"deducteeResidentialStatus",
							(header, value) -> StringUtils.isBlank(value)
									|| !(Arrays.asList("nr", "nonresident").contains(value.toLowerCase()))
											? "'" + header + "' should be either " + "'NR' or 'NONRESIDENT'"
											: null,
							(val) -> "NR"),
					new FieldMapping(HEADER_DEDUCTEE_CODE, "deducteeCode", "deducteeCode"),
					new FieldMapping(HEADER_DEDUCTEE_NAME, "deducteeName", "deducteeName", Excel.VALIDATION_MANDATORY),
					new FieldMapping(HEADER_DEDUCTEE_PAN, "deducteePAN", "deducteePAN"),
					new FieldMapping(HEADER_DEDUCTEE_TIN, "deducteeTin", "deducteeTin", Excel.VALIDATION_MANDATORY),
					new FieldMapping(HEADER_DEDUCTEE_STATUS, "deducteeStatus", "deducteeStatus"),
					new FieldMapping(HEADER_IS_TRC_AVAILABLE, "isTRCAvailable", "isTRCAvailable"),
					new FieldMapping(HEADER_TRC_APPLICABLE_FROM, "trcApplicableFrom", "trcApplicableFrom"),
					new FieldMapping(HEADER_TRC_APPLICABLE_TO, "trcApplicableTo", "trcApplicableTo"),
					new FieldMapping(HEADER_IS_TEN_F_AVAILABLE, "isTenFAvailable", "isTenFAvailable"),
					new FieldMapping(HEADER_IS_TEN_F_APPLICABLE_FROM, "tenFApplicableFrom", "tenFApplicableFrom"),
					new FieldMapping(HEADER_IS_TEN_F_APPLICABLE_TO, "tenFApplicableTo", "tenFApplicableTo"),
					new FieldMapping(HEADER_WHETHER_PE_INDIA, "whetherPEInIndia", "whetherPEInIndia"),
					new FieldMapping(HEADER_NO_PE_DOCUMENT_AVAILABLE, "noPEDocumentAvailable", "noPEDocumentAvailable"),
					new FieldMapping(HEADER_NO_PE_DOCUMENT_APPLICABLE_FROM, "noPEApplicableFrom", "noPEApplicableFrom"),
					new FieldMapping(HEADER_NO_PE_DOCUMENT_APPLICABLE_TO, "noPEApplicableTo", "noPEApplicableTo"),
					new FieldMapping(HEADER_IS_POEM_AVAILABLE, "isPOEMavailable", "isPOEMavailable"),
					new FieldMapping(HEADER_POEM_APPLICABLE_FROM, "poemApplicableFrom", "poemApplicableFrom"),
					new FieldMapping(HEADER_POEM_APPLICABLE_TO, "poemApplicableTo", "poemApplicableTo"),
					new FieldMapping(HEADER_EMAIL_ADDRESS, "emailAddress", "emailAddress"),
					new FieldMapping(HEADER_PHONE_NUMBER, "phoneNumber", "phoneNumber"),
					new FieldMapping(HEADER_FLAT_DOOR_NO, "flatDoorBlockNo", "flatDoorBlockNo"),
					new FieldMapping(HEADER_NAME_BUILDING_VILLAGE, "nameBuildingVillage", "nameBuildingVillage"),
					new FieldMapping(HEADER_ROAD_STREET, "roadStreetPostoffice", "roadStreetPostoffice"),
					new FieldMapping(HEADER_AREA_LOCALITY, "areaLocality", "areaLocality"),
					new FieldMapping(HEADER_TOWN_DISTRICT, "townCityDistrict", "townCityDistrict"),
					new FieldMapping(HEADER_STATE, "state", "state"),
					new FieldMapping(HEADER_COUNTRY, "country", "country"),
					new FieldMapping(HEADER_PIN_CODE, "pinCode", "pinCode"),
					new FieldMapping(HEADER_SECTION, "section", "section"),
					new FieldMapping(HEADER_RATE, "rate", "rate"),
					new FieldMapping(HEADER_SECTION_CODE, "sectionCode", "sectionCode"),
					new FieldMapping(HEADER_APPLICABLE_FROM, "applicableFrom", "applicableFrom",
							Excel.VALIDATION_MANDATORY),
					new FieldMapping(HEADER_APPLICABLE_TO, "applicableTo", "applicableTo"),
					new FieldMapping(HEADER_USER_DEFINED_1, "userDefinedField1", "userDefinedField1"),
					new FieldMapping(HEADER_USER_DEFINED_2, "userDefinedField2", "userDefinedField2"),
					new FieldMapping(HEADER_USER_DEFINED_3, "userDefinedField3", "userDefinedField3"),
					new FieldMapping(HEADER_DEDUCTEE_MASTER_RELATED_PARTY, "relatedParty", "relatedParty",
							(val) -> val != null ? ((String) val).trim().toUpperCase() : null),
					new FieldMapping(
							HEADER_NR_COUNTRY_OF_RESIDENCE, "nrCountryofResidence", "nrCountryofResidence",
							Excel.VALIDATION_MANDATORY),
					new FieldMapping(HEADER_IS_AMOUNT_CONNECTED_FIXED_BASE, "isAmountConnectedFixedBase",
							"isAmountConnectedFixedBase"),
					new FieldMapping(HEADER_FIXED_BASE_AVAILBLE_INDIA_APPLICABLE_TO,
							"fixedbaseAvailbleIndiaApplicableTo", "fixedbaseAvailbleIndiaApplicableTo"),
					new FieldMapping(HEADER_FIXEDBASE_AVAILBLE_INDIA_APPLICABLE_FROM,
							"fixedbaseAvailbleIndiaApplicableFrom", "fixedbaseAvailbleIndiaApplicableFrom"),
					new FieldMapping(HEADER_IS_FIXEDBASE_AVAILBLE_INDIA, "isFixedbaseAvailbleIndia",
							"isFixedbaseAvailbleIndia"),
					new FieldMapping(HEADER_WHETHER_PE_IN_INDIA_APPLICABLE_TO, "whetherPEInIndiaApplicableTo",
							"whetherPEInIndiaApplicableTo"),
					new FieldMapping(HEADER_WHETHER_PE_IN_INDIA_APPLICABLE_FROM, "whetherPEInIndiaApplicableFrom",
							"whetherPEInIndiaApplicableFrom"),
					new FieldMapping(HEADER_IS_PE_AMOUNT_RECEIVED, "isPEamountReceived", "isPEamountReceived"),
					new FieldMapping(HEADER_IS_PE_INVOILVED_IN_PURCHASE_GOODS, "isPEinvoilvedInPurchaseGoods",
							"isPEinvoilvedInPurchaseGoods"),
					new FieldMapping(HEADER_IS_BUSINESS_CARRIED_IN_INDIA, "isBusinessCarriedInIndia",
							"isBusinessCarriedInIndia"),
					new FieldMapping(HEADER_DEDUCTEE_MASTER_IS_GROSSING_UP, "isGrossingUp", "isGrossingUp"),
					new FieldMapping(HEADER_DEDUCTEE_MASTER_IS_DEDUCTEE_TRANSPARENT, "isDeducteeTransparent",
							"isDeducteeTransparent"),
					new FieldMapping(HEADER_DEDUCTEE_MASTER_TRC_AND_TENF_COUNTRY, "trcCountry", "trcCountry"),
					new FieldMapping(HEADER_DEDUCTEE_MASTER_TRC_AND_TENF_TIN, "trcTin", "trcTin"),
					new FieldMapping(HEADER_DEDUCTEE_MASTER_TRC_AND_TENF_ADDRESS, "trcAddress", "trcAddress"),
					new FieldMapping(HEADER_DEDUCTEE_MASTER_TRC_AND_TENF_PERIOD, "trcPeriod", "trcPeriod"),
					new FieldMapping(HEADER_DEDUCTEE_MASTER_TRC_AND_TENF_PAN, "trcPan", "trcPan"),
					new FieldMapping(HEADER_DEDUCTEE_MASTER_TRC_AND_TENF_STATUS, "trcStatus", "trcStatus"),
					new FieldMapping(HEADER_DEDUCTEE_MASTER_IS_TRC_FUTURE, "istrcFuture", "istrcFuture"),
					new FieldMapping(HEADER_DEDUCTEE_MASTER_TRC_FUTURE_DATE, "trcFutureDate", "trcFutureDate"),
					new FieldMapping(HEADER_DEDUCTEE_MASTER_IS_TENF_FUTURE, "istenfFuture", "istenfFuture"),
					new FieldMapping(HEADER_DEDUCTEE_MASTER_TENF_FUTURE_DATE, "tenfFutureDate", "tenfFutureDate"),
					new FieldMapping(HEADER_NATURE_OF_PAYMENT, "natureOfPayment", "natureOfPayment"),
					new FieldMapping(HEADER_PRINCIPLES_OF_BUSINESS_PLACE, "principlesOfBusinessPlace",
							"principlesOfBusinessPlace"),
					new FieldMapping(HEADER_STAY_PERIOD_FINANCIAL_YEAR, "stayPeriodFinancialYear",
							"stayPeriodFinancialYear"),
					new FieldMapping(HEADER_IS_POEM_DECLARATION_IN_FUTURE, "isPoemDeclaration", "isPoemDeclaration"),
					new FieldMapping(HEADER_POEM_FUTURE_DATE, "poemFutureDate", "poemFutureDate"),
					
					// new fields
					new FieldMapping(HEADER_COUNTRY_TO_REMITTANCE, "countryToRemittance", "countryToRemittance"),
					new FieldMapping(HEADER_BENEFICIAL_OWNER_OF_INCOME, "beneficialOwnerOfIncome",
							"beneficialOwnerOfIncome"),
					new FieldMapping(HEADER_IS_BENEFICIAL_OWNERSHIP_DECLARATION, "isBeneficialOwnershipOfDeclaration",
							"isBeneficialOwnershipOfDeclaration"),
					new FieldMapping(HEADER_MLI_PPT_CONDITION_SATISFIED, "mliPptConditionSatisifed",
							"mliPptConditionSatisifed"),
					new FieldMapping(HEADER_MLI_SLOB_CONDITION_SATISFIED, "mliSlobConditionSatisifed",
							"mliSlobConditionSatisifed"),
					new FieldMapping(HEADER_IS_MLI_PPT_SLOB, "isMliPptSlob", "isMliPptSlob"),
					new FieldMapping(HEADER_NATURE_OF_REMITTANCE, "natureOfRemittance", "natureOfRemittance"),
					new FieldMapping(HEADER_ARTICLE_NUMBER_OF_DTAA, "articleNumberDtaa", "articleNumberDtaa"),
					new FieldMapping(HEADER_SECTION_OF_THE_INCOME_TAX_ACT, "sectionOfIncometaxAct",
							"sectionOfIncometaxAct"),
					new FieldMapping(HEADER_DO_YOU_HAVE_AGREEMENT_FOR_THE_TRANSACTION, "aggreementForTransaction",
							"aggreementForTransaction"),
					new FieldMapping(HEADER_ACCOUNTANT_NAME, "accountantName", "accountantName"),
					new FieldMapping(HEADER_NAME_OF_PROPRIETORSHIP_FIRM, "nameOfTheProprietorshipFirm",
							"nameOfTheProprietorshipFirm"),
					new FieldMapping(HEADER_CHARTERED_ACCOUNTANT_FLAT_DOOR_NO, "caFlatDoorBlockNo",
							"caFlatDoorBlockNo"),
					new FieldMapping(HEADER_CHARTERED_ACCOUNTANT_NAME_BUILDING_VILLAGE, "caNameBuildingVillage",
							"caNameBuildingVillage"),
					new FieldMapping(HEADER_CHARTERED_ACCOUNTANT_ROAD_STREET, "caRoadStreetPostoffice",
							"caRoadStreetPostoffice"),
					new FieldMapping(HEADER_CHARTERED_ACCOUNTANT_AREA_LOCALITY, "caAreaLocality", "caAreaLocality"),
					new FieldMapping(HEADER_CHARTERED_ACCOUNTANT_TOWN_DISTRICT, "caTownCityDistrict",
							"caTownCityDistrict"),
					new FieldMapping(HEADER_CHARTERED_ACCOUNTANT_STATE, "caState", "caState"),
					new FieldMapping(HEADER_CHARTERED_ACCOUNTANT_COUNTRY, "caCountry", "caCountry"),
					new FieldMapping(HEADER_CHARTERED_ACCOUNTANT_PIN_CODE, "caPinCode", "caPinCode"),
					new FieldMapping(HEADER_MEMBERSHIP_NUMBER, "membershipNumber", "membershipNumber"),
					new FieldMapping(HEADER_REGISTRATION_NUMBER, "deducteeMasterRegistrationNumber",
							"deducteeMasterRegistrationNumber"),
					new FieldMapping(HEADER_DEDUCTEE_AADHAR_NUMBER, "deducteeAadharNumber", "deducteeAadharNumber"),
					new FieldMapping(HEADER_THRESHOLD_LIMIT_APPLICABLE, "isThresholdLimitApplicable",
							"isThresholdLimitApplicable")));

	public NonResidentDeducteeExcel(XSSFWorkbook workbook) {
		super(workbook, fieldMappings, DeducteeMasterNonResidential.class,
				DeducteeMasterNonResidentialErrorReportCsvDTO.class);
	}

	public DeducteeType getType() {
		return DeducteeType.NON_RESIDENT;
	}

	@Override
	public DeducteeMasterNonResidential get(int index) {
		DeducteeMasterNonResidential deductee = new DeducteeMasterNonResidential();
		for (FieldMapping fieldMapping : fieldMappings) {
			if (logger.isDebugEnabled()) {
				logger.debug("Processing {}", fieldMapping.getExcelHeaderName());
			}
			populateValue(deductee, index, fieldMapping);
		}
		return deductee;
	}

	public Optional<DeducteeMasterNonResidentialErrorReportCsvDTO> validate(int rowIndex) {
		StringJoiner errorMessages = new StringJoiner("\n");

		// validation check
		for (FieldMapping property : fieldMappings) {
			if (property.getValidator() != null) {
				String validationMessage = property.getValidator().apply(
						getHeaders().get(getHeaderIndex(property.getExcelHeaderName().toLowerCase())),
						getRawCellValue(rowIndex, property.getExcelHeaderName()));
				if (StringUtils.isNotEmpty(validationMessage)) {
					errorMessages.add(validationMessage);
				}
			}
		}

		if (errorMessages.length() != 0) {
			DeducteeMasterNonResidentialErrorReportCsvDTO deducteeMasterNonResidentialErrorReportCsvDTO = getErrorDTO(
					rowIndex);
			deducteeMasterNonResidentialErrorReportCsvDTO.setReason(errorMessages.toString());
			return Optional.of(deducteeMasterNonResidentialErrorReportCsvDTO);
		} else {
			return Optional.empty();
		}
	}
}