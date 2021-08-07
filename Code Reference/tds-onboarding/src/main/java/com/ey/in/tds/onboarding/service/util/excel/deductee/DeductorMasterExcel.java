package com.ey.in.tds.onboarding.service.util.excel.deductee;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterExcelDTO;
import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterExcelErrorDTO;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.common.util.excel.FieldMapping;
import com.ey.in.tds.onboarding.service.util.excel.DeductorExcel;

/**
 * 
 * @author Scriptbees.
 *
 */
public class DeductorMasterExcel extends DeductorExcel<DeductorMasterExcelDTO, DeductorMasterExcelErrorDTO> {

	private static final String HEADER_TDS_MODULE = "TDS Module";
	private static final String HEADER_TCS_MODULE = "TCS Module";
	private static final String HEADER_DEDUCTOR_CODE = "Deductor Code";
	private static final String HEADER_DEDUCTOR_SALUTATION = "Deductor Salutation";
	private static final String HEADER_DEDUCTOR_NAME = "Deductor Name";
	private static final String HEADER_DEDUCTOR_PAN = "Deductor Pan";
	private static final String HEADER_DEDUCTOR_RESIDENTIAL_STATUS = "Deductor Residential Status";
	private static final String HEADER_DEDUCTOR_TYPE = "Deductor Type";
	private static final String HEADER_DEDUCTOR_STATUS = "Deductor Status";
	private static final String HEADER_MODE_OF_PAYMENT = "Mode Of Payment";
	private static final String HEADER_DUE_DATE_OF_TAX_PAYMENT = "Due Date Of Tax Payment";
	private static final String HEADER_EMAIL = "Email";
	private static final String HEADER_EMAIL_ALTERNATE = "Email (Alternate)";
	private static final String HEADER_MOBILE_NUMBER = "Mobile Number";
	private static final String HEADER_MOBILE_NUMBER_ALTERNATE = "Mobile Number (Alternate)";
	private static final String HEADER_GOODS_AND_SERVICES_TAX_NUMBER = "Goods And Services Tax Number (GSTIN)";
	private static final String HEADER_APPLICABLE_FROM = "Applicable From";
	private static final String HEADER_APPLICABLE_TO = "Applicable To";
	private static final String HEADER_DEDUCTOR_HAVE_MORE_THAN_ONE_BRANCH = "Deductor Have More Than One Branch";
	private static final String HEADER_TAN = "Tan";
	private static final String HEADER_COUNTRY = "Country";
	private static final String HEADER_STATES = "States";
	private static final String HEADER_CITY = "City";
	private static final String HEADER_STD_CODE = "STD Code";
	private static final String HEADER_PIN_CODE = "PIN Code";
	private static final String HEADER_AREA_LOCALITY = "Area/Locality";
	private static final String HEADER_ROAD_STREET_POST_OFFICE = "Road/Street/Post Office";
	private static final String HEADER_NAME_OF_BUILDING = "Name Of Building";
	private static final String HEADER_FLAT_DOOR_BLOCK_NO = "Flat/Door/Block No";
	private static final String HEADER_PERSON_NAME = "Person Name";
	private static final String HEADER_PERSON_PAN = "Person Pan";
	private static final String HEADER_PERSON_DESIGNATION = "Person Designation";
	private static final String HEADER_PERSON_STATE = "Person State";
	private static final String HEADER_PERSON_CITY = "Person City";
	private static final String HEADER_PERSON_STD_CODE = "Person STD Code";
	private static final String HEADER_PERSON_PIN_CODE = "Person PIN Code";
	private static final String HEADER_PERSON_AREA_LOCALITY = "Person Area/Locality";
	private static final String HEADER_PERSON_STREET_ROAD_NAME = "Person Street/Road Name";
	private static final String HEADER_PERSON_NAME_OF_BUILDING = "Person Name Of Building";
	private static final String HEADER_PERSON_FLATE_DOOR_BLOCK_NO = "Flat/Door/Block NO";
	private static final String HEADER_PERSON_EMAIL = "Person Email";
	private static final String HEADER_PERSON_EMAIL_ALTERNATE = "Person Email (Alternate)";
	private static final String HEADER_TELEPHONE = "Telephone";
	private static final String HEADER_TELEPHONE_ALTERNATE = "Telephone (Alternate)";
	private static final String HEADER_PERSON_MOBILE_NUMBER = "Person Mobile Number";
	private static final String HEADER_ADDRESS_CHANGE = "Address Change";
	
	//dividend headers
	private static final String HEADER_DIVIDEND_DEDUCTOR_TYPE = "Dividend Deductor type";
	private static final String HEADER_DIVIDEND_OPT_IN_15CA_CB = "Dividend- Opt-In for form 15CA/CB?";
	private static final String HEADER_PRINCIPLE_AREA_OF_BUSINESS = "Principal area of business";
	private static final String HEADER_NAME_OF_BANK = "Name of Bank";
	private static final String HEADER_BRANCH_OF_BANK = "Branch of the bank";
	private static final String HEADER_FATHER_OR_HUSBAND_NAME = "Father's/ Husband Name";
	private static final String HEADER_BSR_CODE_OF_BRANCH = "BSR code of the bank branch (7 digit)";
	private static final String HEADER_NAME_OF_PREMISES_BUILDING_VILLAGE = "Name of Premises / Building / Village";
	private static final String HEADER_NAME_OF_PROPRIETORSHIP = "Name of proprietorship/firm";
	private static final String HEADER_DIVIDEND_FLAT_DOOR_BLOCK_NO = "Flat / Door / Block No";
	private static final String HEADER_DIVIDEND_AREA_LOCALITY = "Area / Locality";
	private static final String HEADER_TOWN_CITY_DISTRICT = "Town / City / District";
	private static final String HEADER_DIVIDEND_PINCODE = "Pin-code";
	private static final String HEADER_DIVIDEND_COUNTRY = "COUNTRY";
	private static final String HEADER_DIVIDEND_STATE = "STATE";
	private static final String HEADER_MEMEBERSHIP_NUMBER = "Membership Number";
	private static final String HEADER_DIVIDEND_ROAD_STREET_POST_OFFICE = "Road / Street / Post Office";
	private static final String HEADER_REGISTRATION_NUMBER = "Registration Number";
	private static final String HEADER_ACCOUNTANT_NAME="Accountant Name";
	private static final String HEADER_ACCOUNTANT_SALUTATION="Accountant Salutation";

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public List<FieldMapping> getFieldMappings() {
		return fieldMappings;
	}
	public static List<FieldMapping> fieldMappings = Collections.unmodifiableList(Arrays.asList(
			new FieldMapping(HEADER_TDS_MODULE, "tdsModule", "tdsModule"),
			new FieldMapping(HEADER_TCS_MODULE, "tcsModule", "tcsModule"),
			new FieldMapping(HEADER_DEDUCTOR_CODE, "deductorCode", "deductorCode", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_DEDUCTOR_SALUTATION, "deductorSalutation", "deductorSalutation"),
			new FieldMapping(HEADER_DEDUCTOR_NAME, "deductorName", "deductorName", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_DEDUCTOR_PAN, "deductorPan", "deductorPan", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_DEDUCTOR_RESIDENTIAL_STATUS, "deductorResidentialStatus",
					"deductorResidentialStatus", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_DEDUCTOR_TYPE, "deductorType", "deductorType", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_DEDUCTOR_STATUS, "deductorStatus", "deductorStatus", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_MODE_OF_PAYMENT, "modeOfPayment", "modeOfPayment"),
			new FieldMapping(HEADER_DUE_DATE_OF_TAX_PAYMENT, "dueDateOfTaxPayment", "dueDateOfTaxPayment"),
			new FieldMapping(HEADER_EMAIL, "email", "email", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_EMAIL_ALTERNATE, "emailAlternate", "emailAlternate"),
			new FieldMapping(HEADER_MOBILE_NUMBER, "mobileNumber", "mobileNumber", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_MOBILE_NUMBER_ALTERNATE, "mobileNumberAlternate", "mobileNumberAlternate"),
			new FieldMapping(HEADER_GOODS_AND_SERVICES_TAX_NUMBER, "goodsAndServicesTaxNumber",
					"goodsAndServicesTaxNumber"),
			new FieldMapping(HEADER_APPLICABLE_FROM, "applicableFrom", "applicableFrom", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_APPLICABLE_TO, "applicableTo", "applicableTo"),
			new FieldMapping(HEADER_DEDUCTOR_HAVE_MORE_THAN_ONE_BRANCH, "deductorHaveMoreThanOneBranch",
					"deductorHaveMoreThanOneBranch"),
			new FieldMapping(HEADER_TAN, "tan", "tan", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_COUNTRY, "country", "country", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_STATES, "states", "states", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_CITY, "city", "city", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_STD_CODE, "stdCode", "stdCode", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_PIN_CODE, "pinCode", "pinCode", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_AREA_LOCALITY, "areaOrLocality", "areaOrLocality", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_ROAD_STREET_POST_OFFICE, "roadOrStreetOrPostOffice", "roadOrStreetOrPostOffice",
					Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_NAME_OF_BUILDING, "nameOfBuilding", "nameOfBuilding", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_FLAT_DOOR_BLOCK_NO, "flatOrDoorOrBlockNo", "flatOrDoorOrBlockNo",
					Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_PERSON_NAME, "personName", "personName", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_PERSON_PAN, "personPan", "personPan", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_PERSON_DESIGNATION, "personDesignation", "personDesignation",
					Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_PERSON_STATE, "personState", "personState", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_PERSON_CITY, "personCity", "personCity", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_PERSON_STD_CODE, "personStdCode", "personStdCode", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_PERSON_PIN_CODE, "personPinCode", "personPinCode", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_PERSON_AREA_LOCALITY, "personAreaOrLocality", "personAreaOrLocality",
					Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_PERSON_STREET_ROAD_NAME, "personStreetOrRoadName", "personStreetOrRoadName",
					Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_PERSON_NAME_OF_BUILDING, "personNameOfBuilding", "personNameOfBuilding",
					Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_PERSON_FLATE_DOOR_BLOCK_NO, "personFlateOrDoorOrBlockNo",
					"personFlateOrDoorOrBlockNo", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_PERSON_EMAIL, "personEmail", "personEmail", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_PERSON_EMAIL_ALTERNATE, "personEmailAlternate", "personEmailAlternate"),
			new FieldMapping(HEADER_TELEPHONE, "telephone", "telephone", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_TELEPHONE_ALTERNATE, "telephoneAlternate", "telephoneAlternate"),
			new FieldMapping(HEADER_PERSON_MOBILE_NUMBER, "personMobileNumber", "personMobileNumber",
					Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_ADDRESS_CHANGE, "addressChange", "addressChange"),
			
			//for dividend
			new FieldMapping(HEADER_DIVIDEND_OPT_IN_15CA_CB, "dvndOptedFor15CaCb", "dvndOptedFor15CaCb"),
			new FieldMapping(HEADER_PRINCIPLE_AREA_OF_BUSINESS, "dvndPrincipalAreaOfBusiness", "dvndPrincipalAreaOfBusiness"),
			new FieldMapping(HEADER_DIVIDEND_DEDUCTOR_TYPE, "dvndDeductorTypeName", "dvndDeductorTypeName"),
			new FieldMapping(HEADER_NAME_OF_BANK, "dvndNameOfBank", "dvndNameOfBank"),
			new FieldMapping(HEADER_BRANCH_OF_BANK, "dvndBranchOfBank", "dvndBranchOfBank"),
			new FieldMapping(HEADER_BSR_CODE_OF_BRANCH, "dvndBsrCodeOfBankBranch", "dvndBsrCodeOfBankBranch"),
			new FieldMapping(HEADER_FATHER_OR_HUSBAND_NAME, "dvndFatherOrHusbandName", "dvndFatherOrHusbandName"),
			new FieldMapping(HEADER_NAME_OF_PREMISES_BUILDING_VILLAGE, "dvndNameOfPremisesBuildingVillage", "dvndNameOfPremisesBuildingVillage"),
			new FieldMapping(HEADER_NAME_OF_PROPRIETORSHIP, "dvndNameOfProprietorship", "dvndNameOfProprietorship"),
			new FieldMapping(HEADER_DIVIDEND_AREA_LOCALITY, "dvndAreaLocality", "dvndAreaLocality"),
			new FieldMapping(HEADER_TOWN_CITY_DISTRICT, "dvndTownCityDistrict", "dvndTownCityDistrict"),
			new FieldMapping(HEADER_DIVIDEND_PINCODE, "dvndPinCode", "dvndPinCode"),
			new FieldMapping(HEADER_DIVIDEND_COUNTRY, "dvndCountry", "dvndCountry"),
			new FieldMapping(HEADER_DIVIDEND_STATE, "dvndState", "dvndState"),
			new FieldMapping(HEADER_MEMEBERSHIP_NUMBER, "dvndMembershipNumber", "dvndMembershipNumber"),
			new FieldMapping(HEADER_DIVIDEND_ROAD_STREET_POST_OFFICE, "dvndRoadStreetPostOffice", "dvndRoadStreetPostOffice"),
			new FieldMapping(HEADER_REGISTRATION_NUMBER, "dvndRegistrationNumber", "dvndRegistrationNumber"),
			new FieldMapping(HEADER_DIVIDEND_FLAT_DOOR_BLOCK_NO, "dvndFlatDoorBlockNo", "dvndFlatDoorBlockNo"),
			new FieldMapping(HEADER_ACCOUNTANT_SALUTATION, "accountantSalutation", "accountantSalutation"),
			new FieldMapping(HEADER_ACCOUNTANT_NAME, "dvndAccountantName", "dvndAccountantName")));

	public DeductorMasterExcel(XSSFSheet workSheet) {
		super(workSheet, fieldMappings, DeductorMasterExcelDTO.class, DeductorMasterExcelErrorDTO.class);
	}

	@Override
	public DeductorMasterExcelDTO get(int index) {
		DeductorMasterExcelDTO deductor = new DeductorMasterExcelDTO();
		for (FieldMapping fieldMapping : fieldMappings) {
			populateValue(deductor, index, fieldMapping);
		}
		return deductor;
	}

	public Optional<DeductorMasterExcelErrorDTO> validate(int rowIndex) {
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
			DeductorMasterExcelErrorDTO deductorMasterErrorDTO = this.getErrorDTO(rowIndex);
			deductorMasterErrorDTO.setReason(errorMessages.toString());
			return Optional.of(deductorMasterErrorDTO);
		} else {
			return Optional.empty();
		}
	}

}
