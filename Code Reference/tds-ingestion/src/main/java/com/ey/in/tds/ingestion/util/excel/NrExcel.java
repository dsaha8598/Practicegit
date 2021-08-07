package com.ey.in.tds.ingestion.util.excel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ey.in.tds.common.onboarding.jdbc.dto.NrTransactionsMeta;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.common.util.excel.FieldMapping;
import com.ey.in.tds.ingestion.dto.invoice.NrExcelErrorDTO;

public class NrExcel extends Excel<NrTransactionsMeta, NrExcelErrorDTO> {

	private static final String HEADER_DEDUCTOR_PAN = "Deductor PAN";
	private static final String HEADER_DEDUCTOR_TAN = "Deductor TAN";
	private static final String HEADER_DEDUCTEE_CODE = "Deductee Code";
	private static final String HEADER_DEDUCTEE_NAME = "Deductee Name";
	private static final String HEADER_DEDUCTEE_PAN = "DEDUCTEE PAN";
	private static final String HEADER_DEDUCTEE_STATUS = "Deductee Status";
	private static final String HEADER_FLAT_DOOR_BLOCK_NO = "Flat / Door / Block No";
	private static final String HEADER_NAME_BUILDING_VILLAGE = "Name of Premises / Building / Village";
	private static final String HEADER_ROAD_STREET = "Road / Street / Post Office";
	private static final String HEADER_AREA_LOCALITY = "Area / Locality";
	private static final String HEADER_TOWN_CITY_DISTRICT = "Town / City / District";
	private static final String HEADER_STATE = "State";
	private static final String HEADER_COUNTRY = "Country";
	private static final String HEADER_ZIP_CODE = "ZIP Code";
	private static final String HEADER_EMAIL_ID = "Email - ID";
	private static final String HEADER_CONTACT_NUMBER = "Contact Number";
	private static final String HEADER_TIN = "Taxpayer Identification Number";
	private static final String HEADER_COUNTRY_TO_REMITTANCE = "Country to which remittance is made";
	private static final String HEADER_IS_TRC_AVAILABLE = "Is Tax Residency Certificate ('TRC') available";
	private static final String HEADER_IS_TRC_FUTURE = "Are you likely to receive TRC in future?";
	private static final String HEADER_TRC_APPLICABLE_FROM = "TRC available (From date)";
	private static final String HEADER_TRC_APPLICABLE_TO = "TRC available (To date)";
	private static final String HEADER_IS_TENF_AVAILABLE = "Is Form 10F available";
	private static final String HEADER_IS_TENF_FUTURE = "Are you likely to receive Form 10F in future?";
	private static final String HEADER_TENF_APPLICABLE_FROM = "Form 10F available (From date)";
	private static final String HEADER_TENF_APPLICABLE_TO = "Form 10F available (To date)";
	private static final String HEADER_IS_PE_IN_INDIA = "Is there a Permanent Establishment '(PE') in India";
	private static final String HEADER_IS_NO_PE_DECLARATION_AVAILABLE = "Is No PE declaration available";
	private static final String HEADER_NO_PE_DOCUMENT_APPLICABLE_FROM = "Period of No PE declaration (From date)";
	private static final String HEADER_NO_PE_DOCUMENT_APPLICABLE_TO = "Period of No PE declaration (To date)";
	private static final String HEADER_IS_FIXED_BASE_AVAILBLE_INDIA = "Is fixed base available in India";
	private static final String HEADER_FIXED_BASE_AVAILBLE_INDIA_APPLICABLE_FROM = "Fixed Base in India (From Date)";
	private static final String HEADER_FIXED_BASE_AVAILBLE_INDIA_APPLICABLE_TO = "Fixed Base in India (To Date)";
	private static final String HEADER_STAY_PERIOD_FINANCIAL_YEAR = "Period of Stay likely in India in the financial year";
	private static final String HEADER_IS_NO_FIXED_BASE_DECLARATION_AVAILABLE = "Is 'No Fixed Base' declaration available";
	private static final String HEADER_IS_POEM_OF_DEDUCTEE = "Is there a Place Of Effective Management ('POEM') of deductee in India";
	private static final String HEADER_IS_NO_POEM_DECLARATION_AVAILABLE = "Is No POEM declaration available";
	private static final String HEADER_IS_NO_POEM_DECLARATION_IN_FUTURE = "Are you likely to receive 'No POEM declaration' in future?";
	private static final String HEADER_NO_POEM_APPLICABLE_FROM = "Period of No POEM in India (From date)";
	private static final String HEADER_NO_POEM_APPLICABLE_TO = "Period of No POEM in India (To date)";
	private static final String HEADER_BENEFICIAL_OWNER_OF_INCOME = "Beneficial Owner of income";
	private static final String HEADER_IS_BENEFICIAL_OWNERSHIP_DECLARATION = "Is beneficial ownership declaration available?";
	private static final String HEADER_NATURE_OF_PAYMENT_OR_REMITTANCE = "Nature of payment/remittance";
	private static final String HEADER_PO_NUMBER = "PO number";
	private static final String HEADER_PO_DATE = "PO date";
	private static final String HEADER_ERP_DOCUMENT_NUMBER = "ERP Document Number";
	private static final String HEADER_DATE_OF_POSTING = "Date of Posting";
	private static final String HEADER_VENDOR_DOCUMENT_NUMBER = "Vendor Document Number";
	private static final String HEADER_VENDOR_DOCUMENT_DATE = "Vendor Document date";
	private static final String HEADER_AMOUNT_IN_FOREIGN_CURRENCY = "Amount (in foreign currency)";
	private static final String HEADER_CURRENCY = "Currency";
	private static final String HEADER_AMOUNT_IN_INR = "Amount (in INR)";
	private static final String HEADER_AMOUNT_OF_INCOME_TAX = "Amount of Income on which tax is to be deducted (If different from amount paid / credited)";
	private static final String HEADER_IS_LDC_APPLIED = "Is Lower Deduction Certificate applied?";
	private static final String HEADER_CERTIFICATE_NUMBER = "Order/Certificate Number";
	private static final String HEADER_RATE_OF_LDC = "Rate of Lower Deduction Certificate";
	private static final String HEADER_RATE_AS_PER_DTAA = "Rate as per Income Tax Act, 1961 / Double Taxation Avoidance Agreement";
	private static final String HEADER_ARTICLE_OF_DTAA = "Article of DTAA";
	private static final String HEADER_DATE_OF_DEDUCTION_OF_TAX = "Date of deduction of tax at source";
	private static final String HEADER_DATE_OF_DEPOSIT_OF_TAX = "Date of Deposit of tax at source";
	private static final String HEADER_TDS_AMOUNT = "TDS Amount";
	private static final String HEADER_SURCHARGE = "Surcharge";
	private static final String HEADER_EDUCATION_CESS = "Education Cess";
	private static final String HEADER_INTEREST = "Interest";
	private static final String HEADER_FEE = "Fee";
	private static final String HEADER_TDS_AMOUNT_IN_FOREIGN_CURRENCY = "Actual amount of remittance after TDS (in foreign currency)";
	private static final String HEADER_PROPOSED_DATE_OF_REMITTANCE = "Proposed date of remittance";
	private static final String HEADER_WHETHER_INCOME_RECEIVED = "Whether income received is connected with PE";
	private static final String HEADER_WHETHER_TAX_PAYABLE_HAS_BEEN_GROSSED_UP = "In case the remittance is net of taxes, whether tax payable has been grossed up?";
	private static final String HEADER_MODE_OF_DEPOSIT = "Mode of deposit through book adjustment (Yes/No)";
	private static final String HEADER_BSR_CODE = "BSR Code / Form 24G Receipt No";
	private static final String HEADER_CHALLAN_SERIAL_NUMBER = "Challan Serial No. / DDO Serial No. of Form No. 24G";
	private static final String HEADER_MLI_PPT_CONDITION_SATISFIED = "Multilateral Instrument Principle Purpose Test ('MLI-PPT') Condition Satisfied";
	private static final String HEADER_MLI_SLOB_CONDITION_SATISFIED = "Multilateral Instrument Simplified Limitation on Benefit ('MLI -SLOB') Condition  Satisfied";
	private static final String HEADER_IS_MLI_PPT_SLOB = "Is MLI- PPT/ SLOB satisfaction declaration available?";
	private static final String HEADER_IS_TRANSACTION_GAAR_COMPLIANT = "Is the transaction GAAR compliant";
	private static final String HEADER_DOCUMENT_TYPE = "Document Type";
	private static final String HEADER_CHALLAN_PAID = "Challan Paid";
	private static final String HEADER_CHALLAN_GENERATED_DATE = "Challan Generated Date";
	private static final String HEADER_PROVISION_CAN_ADJUST = "Provision Can Adjust";
	private static final String HEADER_ADVANCE_CAN_ADJUST = "Advance Can Adjust";

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public List<FieldMapping> getFieldMappings() {
		return fieldMappings;
	}

	public static List<FieldMapping> fieldMappings = Collections.unmodifiableList(Arrays.asList(

			new FieldMapping(HEADER_DEDUCTOR_PAN, "deductorPan", "deductorPan", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_DEDUCTOR_TAN, "deductorMasterTan", "deductorMasterTan", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_DEDUCTEE_CODE, "deducteeCode", "deducteeCode"),
			new FieldMapping(HEADER_DEDUCTEE_NAME, "deducteeName", "deducteeName", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_DEDUCTEE_PAN, "deducteePan", "deducteePan"),
			new FieldMapping(HEADER_DEDUCTEE_STATUS, "deducteeStatus", "deducteeStatus"),
			new FieldMapping(HEADER_FLAT_DOOR_BLOCK_NO, "flatDoorBlockNo", "flatDoorBlockNo"),
			new FieldMapping(HEADER_NAME_BUILDING_VILLAGE, "buildingVillage", "buildingVillage"),
			new FieldMapping(HEADER_ROAD_STREET, "roadStreet", "roadStreet"),
			new FieldMapping(HEADER_AREA_LOCALITY, "areaLocality", "areaLocality"),
			new FieldMapping(HEADER_TOWN_CITY_DISTRICT, "townCityDistrict", "townCityDistrict"),
			new FieldMapping(HEADER_STATE, "state", "state"),
			new FieldMapping(HEADER_COUNTRY, "country", "country"),
			new FieldMapping(HEADER_ZIP_CODE, "zipcode", "zipcode"),
			new FieldMapping(HEADER_EMAIL_ID, "email", "email"),
			new FieldMapping(HEADER_CONTACT_NUMBER, "contactNo", "contactNo"),
			new FieldMapping(HEADER_TIN, "tin", "tin"),
			new FieldMapping(HEADER_COUNTRY_TO_REMITTANCE, "countryToRemittance", "countryToRemittance"),
			new FieldMapping(HEADER_IS_TRC_AVAILABLE, "isTrcAvailable", "isTrcAvailable"),
			new FieldMapping(HEADER_IS_TRC_FUTURE, "isTrcFuture", "isTrcFuture"),
			new FieldMapping(HEADER_TRC_APPLICABLE_FROM, "isTrcApplicableFrom", "isTrcApplicableFrom"),
			new FieldMapping(HEADER_TRC_APPLICABLE_TO, "isTrcApplicableTo", "isTrcApplicableTo"),
			new FieldMapping(HEADER_IS_TENF_AVAILABLE, "isTenfAvailable", "isTenfAvailable"),
			new FieldMapping(HEADER_IS_TENF_FUTURE, "isTenfFuture", "isTenfFuture"),
			new FieldMapping(HEADER_TENF_APPLICABLE_FROM, "isTenfApplicableFrom", "isTenfApplicableFrom"),
			new FieldMapping(HEADER_TENF_APPLICABLE_TO, "isTenfApplicableTo", "isTenfApplicableTo"),
			new FieldMapping(HEADER_IS_PE_IN_INDIA, "isPeIndia", "isPeIndia"),
			new FieldMapping(HEADER_IS_NO_PE_DECLARATION_AVAILABLE, "isNoPeDocumentAvailable", "isNoPeDocumentAvailable"),
			new FieldMapping(HEADER_NO_PE_DOCUMENT_APPLICABLE_FROM, "noPeDocumentApplicableFrom",
					"noPeDocumentApplicableFrom"),
			new FieldMapping(HEADER_NO_PE_DOCUMENT_APPLICABLE_TO, "noPeDocumentApplicableTo",
					"noPeDocumentApplicableTo"),
			new FieldMapping(HEADER_IS_FIXED_BASE_AVAILBLE_INDIA, "isFixedbaseAvailbleIndia",
					"isFixedbaseAvailbleIndia"),
			new FieldMapping(HEADER_FIXED_BASE_AVAILBLE_INDIA_APPLICABLE_FROM, "isFixedbaseApplicableFrom",
					"isFixedbaseApplicableFrom"),
			new FieldMapping(HEADER_FIXED_BASE_AVAILBLE_INDIA_APPLICABLE_TO, "isFixedbaseApplicableTo",
					"isFixedbaseApplicableTo"),
			new FieldMapping(HEADER_STAY_PERIOD_FINANCIAL_YEAR, "stayPeriodFinancialYear", "stayPeriodFinancialYear"),
			new FieldMapping(HEADER_IS_NO_FIXED_BASE_DECLARATION_AVAILABLE, "isNoFixedBaseDeclaration",
					"isNoFixedBaseDeclaration"),
			new FieldMapping(HEADER_IS_POEM_OF_DEDUCTEE, "isPoemOfDeductee", "isPoemOfDeductee"),
			new FieldMapping(HEADER_IS_NO_POEM_DECLARATION_AVAILABLE, "isNoPoemAvailable", "isNoPoemAvailable"),
			new FieldMapping(HEADER_IS_NO_POEM_DECLARATION_IN_FUTURE, "isNoPoemDeclarationInFuture",
					"isNoPoemDeclarationInFuture"),
			new FieldMapping(HEADER_NO_POEM_APPLICABLE_FROM, "isNoPoemApplicableFrom", "isNoPoemApplicableFrom"),
			new FieldMapping(HEADER_NO_POEM_APPLICABLE_TO, "isNoPoemApplicableTo", "isNoPoemApplicableTo"),
			new FieldMapping(HEADER_BENEFICIAL_OWNER_OF_INCOME, "beneficialOwnerOfIncome", "beneficialOwnerOfIncome"),
			new FieldMapping(HEADER_IS_BENEFICIAL_OWNERSHIP_DECLARATION, "isBeneficialOwnershipOfDeclaration",
					"isBeneficialOwnershipOfDeclaration"),
			new FieldMapping(HEADER_NATURE_OF_PAYMENT_OR_REMITTANCE, "natureOfPaymentOrRemittance",
					"natureOfPaymentOrRemittance", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_PO_NUMBER, "poNumber", "poNumber"),
			new FieldMapping(HEADER_PO_DATE, "poDate", "poDate"),
			new FieldMapping(HEADER_ERP_DOCUMENT_NUMBER, "erpDocumentNo", "erpDocumentNo"),
			new FieldMapping(HEADER_DATE_OF_POSTING, "documentPostingDate", "documentPostingDate",
					Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_VENDOR_DOCUMENT_NUMBER, "vendorDocumentNo", "vendorDocumentNo"),
			new FieldMapping(HEADER_VENDOR_DOCUMENT_DATE, "vendorDocumentDate", "vendorDocumentDate"),
			new FieldMapping(HEADER_AMOUNT_IN_FOREIGN_CURRENCY, "amountInForeignCurrency", "amountInForeignCurrency"),
			new FieldMapping(HEADER_CURRENCY, "currency", "currency"),
			new FieldMapping(HEADER_AMOUNT_IN_INR, "amountInInr", "amountInInr"),
			new FieldMapping(HEADER_AMOUNT_OF_INCOME_TAX, "amountOfIncometax", "amountOfIncometax"),
			new FieldMapping(HEADER_IS_LDC_APPLIED, "isLdcApplied", "isLdcApplied"),
			new FieldMapping(HEADER_CERTIFICATE_NUMBER, "certificateNo", "certificateNo"),
			new FieldMapping(HEADER_RATE_OF_LDC, "rateOfLdc", "rateOfLdc"),
			new FieldMapping(HEADER_RATE_AS_PER_DTAA, "rateAsPerIncometax", "rateAsPerIncometax"),
			new FieldMapping(HEADER_ARTICLE_OF_DTAA, "articleOfDtaa", "articleOfDtaa"),
			new FieldMapping(HEADER_DATE_OF_DEDUCTION_OF_TAX, "dateOfDeductionOfTax", "dateOfDeductionOfTax"),
			new FieldMapping(HEADER_DATE_OF_DEPOSIT_OF_TAX, "dateOfDepositOfTax", "dateOfDepositOfTax"),
			new FieldMapping(HEADER_TDS_AMOUNT, "tdsAmount", "tdsAmount"),
			new FieldMapping(HEADER_SURCHARGE, "surcharge", "surcharge"),
			new FieldMapping(HEADER_EDUCATION_CESS, "eductaionCess", "eductaionCess"),
			new FieldMapping(HEADER_INTEREST, "interest", "interest"), new FieldMapping(HEADER_FEE, "eductaionFee", "eductaionFee"),
			new FieldMapping(HEADER_TDS_AMOUNT_IN_FOREIGN_CURRENCY, "tdsAmountInForeignCurrency",
					"tdsAmountInForeignCurrency"),
			new FieldMapping(HEADER_PROPOSED_DATE_OF_REMITTANCE, "dateOfRemittance", "dateOfRemittance"),
			new FieldMapping(HEADER_WHETHER_INCOME_RECEIVED, "whetherIncomeReceived", "whetherIncomeReceived"),
			new FieldMapping(HEADER_WHETHER_TAX_PAYABLE_HAS_BEEN_GROSSED_UP, "isGrossedUp", "isGrossedUp"),
			new FieldMapping(HEADER_MODE_OF_DEPOSIT, "madeOfDeposit", "madeOfDeposit"),
			new FieldMapping(HEADER_BSR_CODE, "bsrCode", "bsrCode"),
			new FieldMapping(HEADER_CHALLAN_SERIAL_NUMBER, "challanSerialNumber", "challanSerialNumber"),
			new FieldMapping(HEADER_MLI_PPT_CONDITION_SATISFIED, "mliPptConditionSatisifed",
					"mliPptConditionSatisifed"),
			new FieldMapping(HEADER_MLI_SLOB_CONDITION_SATISFIED, "mliSlobConditionSatisifed",
					"mliSlobConditionSatisifed"),
			new FieldMapping(HEADER_IS_MLI_PPT_SLOB, "isMliOrPptSlob", "isMliOrPptSlob"),
			new FieldMapping(HEADER_IS_TRANSACTION_GAAR_COMPLIANT, "isGAARComplaint", "isGAARComplaint"),
			new FieldMapping(HEADER_DOCUMENT_TYPE, "documentType", "documentType", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_CHALLAN_PAID, "challanPaid", "challanPaid"),
			new FieldMapping(HEADER_CHALLAN_GENERATED_DATE, "challanGeneratedDate", "challanGeneratedDate"),
			new FieldMapping(HEADER_PROVISION_CAN_ADJUST, "advanceCanAdjust", "advanceCanAdjust"),
			new FieldMapping(HEADER_ADVANCE_CAN_ADJUST, "provisionCanAdjust", "provisionCanAdjust")));

	public NrExcel(XSSFWorkbook workbook) {

		super(workbook, fieldMappings, NrTransactionsMeta.class, NrExcelErrorDTO.class);

	}

	@Override
	public NrTransactionsMeta get(int index) {
		NrTransactionsMeta invoiceExcelDTO = new NrTransactionsMeta();
		for (FieldMapping fieldMapping : fieldMappings) {
			populateValue(invoiceExcelDTO, index, fieldMapping);
		}
		return invoiceExcelDTO;
	}

	public Optional<NrExcelErrorDTO> validate(int rowIndex) {
		StringJoiner errorMessages = new StringJoiner("\n");

		// validation check
		for (FieldMapping fieldMapping : fieldMappings) {
			logger.debug("Processing {}", fieldMapping.getExcelHeaderName());
			logger.debug("column {}", fieldMapping);
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
			NrExcelErrorDTO invoiceErrorReportCsvDTO = this.getErrorDTO(rowIndex);
			invoiceErrorReportCsvDTO.setReason(errorMessages.toString());
			return Optional.of(invoiceErrorReportCsvDTO);
		} else {
			return Optional.empty();
		}
	}

}
