package com.ey.in.tds.onboarding.service.util.excel.shareholder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ey.in.tds.common.dto.onboarding.shareholder.ShareholderMasterNonResidential;
import com.ey.in.tds.common.model.shareholder.ShareholderMasterNonResidentialErrorReportCsvDTO;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.common.util.excel.FieldMapping;

public class NonResidentShareholderExcel extends  Excel<ShareholderMasterNonResidential, ShareholderMasterNonResidentialErrorReportCsvDTO>{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());



    public static final String HEADER_SHAREHOLDER_FOLIONO = "FOLIO NUMBER";
    public static final String HEADER_SHAREHOLDER_UNIQUE_IDENTIFICATION_NUMBER = " UNIQUE SHAREHOLDER IDENTIFICATION NUMBER";
    public static final String HEADER_SHAREHOLDER_NAME = "SHAREHOLDER NAME";
    public static final String HEADER_SHAREHOLDER_CATEGORY = "CATEGORY OF SHAREHOLDER";
    public static final String HEADER_SHAREHOLDER_TYPE = "TYPE OF SHAREHOLDER";
    public static final String HEADER_KEY_SHAREHOLDER = "KEY SHAREHOLDER (AFFILIATED/ RELATED ENTITY)";
    public static final String HEADER_SHAREHOLDER_TIN = "TAX IDENTIFICATION NUMBER (NON-RESIDENT)";
    public static final String HEADER_SHAREHOLDER_PAN = "PAN";
    public static final String HEADER_SHAREHOLDER_PRINCIPAL_PLACE_OF_BUSINESS = "PRINCIPAL PLACE OF BUSINESS";
    public static final String HEADER_FLAT_DOOR_NO = "FLAT/ DOOR/ BUILDING NUMBER";
    public static final String HEADER_NAME_BUILDING_VILLAGE = "NAME OF PREMISES/ BUILDING/  VILLAGE";
    public static final String HEADER_ROAD_STREET = "ROAD/ STREET";
    public static final String HEADER_AREA_LOCALITY = "AREA/ LOCALITY";
    public static final String HEADER_TOWN_DISTRICT = "TOWN/ CITY/ DISTRICT";
    public static final String HEADER_STATE = "STATE";
    public static final String HEADER_PIN_CODE = "PIN/ ZIP CODE";
    public static final String HEADER_COUNTRY = "COUNTRY";
    public static final String HEADER_EMAIL_ID = "EMAIL ID";
    public static final String HEADER_CONTACT = "CONTACT NUMBER";

    public static final String HEADER_SHARE_TRANSFER_AGENT_NAME = "SHARE TRANSFER AGENT NAME";
    public static final String HEADER_DEMAT_ACCOUNT_NO = "DEMAT ACCOUNT NUMBER";
    public static final String HEADER_TOTAL_SHARES_HELD = "TOTAL NUMBER OF SHARES HELD";
    public static final String HEADER_PERCENTAGE_SHARES_HELD = "PERCENTAGE OF SHARES HELD";

    public static final String HEADER_SHARE_HELD_FROM_DATE = "DURATION OF SHARES HELD (FROM DATE)\n" +
            "DD/MM/YYYY";
    public static final String HEADER_SHARES_HELD_TO_DATE = "DURATION OF SHARES HELD (TO DATE)\n" +
            "DD/MM/YYYY";
    public static final String HEADER_FORM_15CACB_APPLICABLE = "FORM 15 CA/ CB APPLICABLE";
    public static final String HEADER_IS_TRC_AVAILABLE = "IS TAX RESIDENCY CERTIFICATE ('TRC') AVAILABLE";
    public static final String HEADER_TRC_APPLICABLE_FROM = "TRC AVAILABLE (FROM DATE)\n" +
            "DD/MM/YYYY";
    public static final String HEADER_TRC_APPLICABLE_TO = "TRC AVAILABLE (TO DATE)\n" +
            "DD/MM/YYYY";
    public static final String HEADER_IS_FORM_TEN_F_AVAILABLE = "IS FORM 10F AVAILABLE";
    public static final String HEADER_FORM_TEN_F_APPLICABLE_FROM = "FORM 10F AVAILABLE (FROM DATE)\n" +
            "DD/MM/YYYY";
    public static final String HEADER_FORM_TEN_F_APPLICABLE_TO = "FORM 10F AVAILABLE (TO DATE)\n" +
            "DD/MM/YYYY";
    public static final String HEADER_IS_PE_AVAILABLE_IN_INDIA = "IS THERE A PERMANENT ESTABLISHMENT '(PE') IN INDIA";
    public static final String HEADER_IS_NO_PE_DECLARATION_AVAILABLE = "IS NO PE DECLARATION AVAILABLE";
    public static final String HEADER_NO_PE_DECLARATION_APPLICABLE_FROM = "PERIOD OF NO PE DECLARATION (FROM DATE)\n" +
            "DD/MM/YYYY";
    public static final String HEADER_NO_PE_DECLARATION_APPLICABLE_TO = "PERIOD OF NO PE DECLARATION (TO DATE)\n" +
            "DD/MM/YYYY";
    public static final String HEADER_IS_POEM_OF_SHAREHOLDER_IN_INDIA = "IS THERE A PLACE OF EFFECTIVE MANAGEMENT ('POEM') OF SHAREHOLDER IN INDIA";
    public static final String HEADER_IS_NO_POEM_DECLARATION_AVAILABLE = "IS NO POEM DECLARATION AVAILABLE";
    public static final String HEADER_NO_POEM_DECLARATION_IN_INDIA_APPLICABLE_FROM = "PERIOD OF NO POEM IN INDIA (FROM DATE)\n" +
            "DD/MM/YYYY";
    public static final String HEADER_NO_POEM_DECLARATION_IN_INDIA_APPLICABLE_TO = "PERIOD OF NO POEM IN INDIA (TO DATE)\n" +
            "DD/MM/YYYY";
    public static final String HEADER_IS_MLI_SATISFACTION_DECLARATION_AVAILABLE = "IS MULTILATREAL INSTRUMENT- PRINCIPLE PURPOSE TEST / SIMPLIFIED LIMITATION OF BENEFITS SATISFACTION DECLARATION AVAILABLE";
    public static final String HEADER_IS_BENEFICIAL_OWNER_OF_INCOME = "BENEFICIAL OWNER OF INCOME";
    public static final String HEADER_IS_BENEFICIAL_OWNERSHIP_DECLARATION_AVAILABLE = "IS BENEFICIAL OWNERSHIP DECLARATION AVAILABLE";
    public static final String HEADER_IS_TRANSACTION_GAAR_COMPLIANT = "IS THE TRANSACTION GAAR COMPLIANT";
    public static final String HEADER_IS_COMMERCIAL_INDEMNITY_OR_TREATY_BENEFITS_WITHOUT_DOCUMENTS = "WHETHER COMMERCIAL INDEMNITY AVAILABLE";
    public static final String HEADER_IS_KUWAIT_SHAREHOLDER_TYPE = "IF COUNTRY OF SHAREHOLDER IS KUWAIT- WHETHER IT IS FOREIGN GOVERNMENT OR  POLITICAL SUB DIVISION OR A LOCAL AUTHORITY OR  THE CENTRAL BANK  OR OTHER GOVERNMENTAL AGENCIES OR GOVERNMENTAL FINANCIAL INSTITUTIONS";
    public static final String HEADER_IS_UK_VEHICLE_EXEMPT_TAX = "IF THE SHAREHOLDER IS AN INVESTMENT VEHICLE FROM UK- CHECK WHETHER DIVIDEND DERIVED FROM IMMOVABLE PROPERTY AND INCOME FROM SUCH IMMOVABLE PROPERTY IS EXEMPT FROM TAX";
    public static final String HEADER_ICELAND_DIVIDEND_TAXATION_RATE = "IF THE SHAREHOLDER IS AN ICELAND COMPANY- CHECK WHETHER THE SHAREHOLDING IN ICELAND COMPANY BY PERSONS OTHER THAN RESIDENT INDIVIDUALS EXCEEDS 25%. IF YES PROVIDE RATE OF DIVIDEND TAXATION";


    @Override
    public List<FieldMapping> getFieldMappings() {
        return fieldMappings;
    }

    public static List<FieldMapping> fieldMappings = Collections.unmodifiableList(Arrays.asList(
            new FieldMapping(HEADER_SHAREHOLDER_FOLIONO, "folioNo", "shareholder_master_folio_no"),
            new FieldMapping(HEADER_SHAREHOLDER_UNIQUE_IDENTIFICATION_NUMBER, "uniqueIdentificationNumber", "uniqueIdentificationNumber"),
            new FieldMapping(HEADER_SHAREHOLDER_NAME, "shareholderName", "shareholder_master_name",
                    Excel.VALIDATION_MANDATORY),
            new FieldMapping(HEADER_SHAREHOLDER_CATEGORY, "shareholderCategory", "shareholder_master_category", Excel.VALIDATION_MANDATORY),
            new FieldMapping(HEADER_SHAREHOLDER_TYPE, "shareholderType", "shareholder_master_type"),
            new FieldMapping(HEADER_KEY_SHAREHOLDER, "keyShareholder", "shareholder_master_key"),
            new FieldMapping(HEADER_SHAREHOLDER_TIN, "shareholderTin", "shareholder_master_tin"),
            new FieldMapping(HEADER_SHAREHOLDER_PAN, "shareholderPan", "shareholder_master_pan"),
            new FieldMapping(HEADER_SHAREHOLDER_PRINCIPAL_PLACE_OF_BUSINESS, "principalPlaceOfBusiness", "shareholder_master_principal_place_of_business"),
            new FieldMapping(HEADER_FLAT_DOOR_NO, "flatDoorBlockNo", "shareholder_master_flat_door_block_no"),
            new FieldMapping(HEADER_NAME_BUILDING_VILLAGE, "nameBuildingVillage",
                    "shareholder_master_name_building_village"),
            new FieldMapping(HEADER_ROAD_STREET, "roadStreetPostoffice", "shareholder_master_road_street_postoffice"),
            new FieldMapping(HEADER_AREA_LOCALITY, "areaLocality", "shareholder_master_area_locality"),
            new FieldMapping(HEADER_TOWN_DISTRICT, "townCityDistrict", "shareholder_master_town_city_district"),
            new FieldMapping(HEADER_STATE, "state", "shareholder_master_state",
                    (val) -> val != null ? ((String) val).toUpperCase() : null),
            new FieldMapping(HEADER_PIN_CODE, "pinCode", "shareholder_master_pin_code"),
            new FieldMapping(HEADER_COUNTRY, "country", "shareholder_master_country", Excel.VALIDATION_MANDATORY),
            new FieldMapping(HEADER_EMAIL_ID, "emailId", "shareholder_master_email_id"),
            new FieldMapping(HEADER_CONTACT, "contact", "shareholder_master_contact"),
            new FieldMapping(HEADER_SHARE_TRANSFER_AGENT_NAME, "shareTransferAgentName", "shareholder_master_share_transfer_agent_name"),
            new FieldMapping(HEADER_DEMAT_ACCOUNT_NO, "dematAccountNo", "shareholder_master_demat_account_no"),
            new FieldMapping(HEADER_TOTAL_SHARES_HELD, "totalSharesHeld", "shareholder_master_total_shares_held"),
            new FieldMapping(HEADER_PERCENTAGE_SHARES_HELD, "percentageSharesHeld", "shareholder_master_percetage_of_share_held"),
            new FieldMapping(HEADER_SHARE_HELD_FROM_DATE, "shareHeldFromDate", "shareholder_master_share_held_from_date"),
            new FieldMapping(HEADER_SHARES_HELD_TO_DATE, "shareHeldToDate", "shareholder_master_share_held_to_date"),
            new FieldMapping(HEADER_FORM_15CACB_APPLICABLE, "form15CACBApplicable", "shareholder_master_form15cacb_applicable"),
            new FieldMapping(HEADER_IS_TRC_AVAILABLE, "isTrcAvailable", "is_trc_available"),
            new FieldMapping(HEADER_TRC_APPLICABLE_FROM, "trcApplicableFrom", "trc_applicable_from"),
            new FieldMapping(HEADER_TRC_APPLICABLE_TO, "trcApplicableTo", "trc_applicable_to"),
            new FieldMapping(HEADER_IS_FORM_TEN_F_AVAILABLE, "isTenfAvailable", "is_tenf_available"),
            new FieldMapping(HEADER_FORM_TEN_F_APPLICABLE_FROM, "tenfApplicableFrom", "tenf_applicable_from"),
            new FieldMapping(HEADER_FORM_TEN_F_APPLICABLE_TO, "tenfApplicableTo", "tenf_applicable_to"),
            new FieldMapping(HEADER_IS_PE_AVAILABLE_IN_INDIA, "isPeAvailableInIndia", "is_pe_available_in_india"),
            new FieldMapping(HEADER_IS_NO_PE_DECLARATION_AVAILABLE, "isNoPeDeclarationAvailable", "is_no_pe_declaration_available"),
            new FieldMapping(HEADER_NO_PE_DECLARATION_APPLICABLE_FROM, "noPeDeclarationApplicableFrom", "no_pe_declaration_applicable_from"),
            new FieldMapping(HEADER_NO_PE_DECLARATION_APPLICABLE_TO, "noPeDeclarationApplicableTo", "no_pe_declaration_applicable_to"),
            new FieldMapping(HEADER_IS_POEM_OF_SHAREHOLDER_IN_INDIA, "isPoemOfShareholderInIndia", "is_poem_of_shareholder_in_india"),
            new FieldMapping(HEADER_IS_NO_POEM_DECLARATION_AVAILABLE, "isNoPoemDeclarationAvailable", "is_no_poem_declaration_available"),
            new FieldMapping(HEADER_NO_POEM_DECLARATION_IN_INDIA_APPLICABLE_FROM, "noPoemDeclarationInIndiaApplicableFrom", "no_poem_in_india_applicable_from"),
            new FieldMapping(HEADER_NO_POEM_DECLARATION_IN_INDIA_APPLICABLE_TO, "noPoemDeclarationInIndiaApplicableTo", "no_poem_in_india_applicable_to"),
            new FieldMapping(HEADER_IS_MLI_SATISFACTION_DECLARATION_AVAILABLE, "isMliSlobSatisfactionDeclarationAvailable", "is_mli_slob_satisfaction_declaration_available"),
            new FieldMapping(HEADER_IS_BENEFICIAL_OWNER_OF_INCOME, "isBeneficialOwnerOfIncome", "is_beneficial_owner_of_income"),
            new FieldMapping(HEADER_IS_BENEFICIAL_OWNERSHIP_DECLARATION_AVAILABLE, "isBeneficialOwnershipDeclarationAvailable", "is_beneficial_ownership_declaration_available"),
            new FieldMapping(HEADER_IS_TRANSACTION_GAAR_COMPLIANT, "isTransactionGAARCompliant", "is_transaction_gaar_compliant"),
            new FieldMapping(HEADER_IS_COMMERCIAL_INDEMNITY_OR_TREATY_BENEFITS_WITHOUT_DOCUMENTS, "isCommercialIndemnityOrTreatyBenefitsWithoutDocuments", "is_commercial_indemnity_or_treaty_benefits_without_documents"),
            new FieldMapping(HEADER_IS_KUWAIT_SHAREHOLDER_TYPE, "isKuwaitShareholderType", "isKuwaitShareholderType"),
            new FieldMapping(HEADER_IS_UK_VEHICLE_EXEMPT_TAX, "isUKVehicleExemptTax", "is_uk_vehicle_exempt_tax"),
            new FieldMapping(HEADER_ICELAND_DIVIDEND_TAXATION_RATE, "icelandDividendTaxationRate", "iceland_dividend_taxation_rate")
    ));

    public NonResidentShareholderExcel(XSSFWorkbook workbook) {
        super(workbook, fieldMappings, ShareholderMasterNonResidential.class,
                ShareholderMasterNonResidentialErrorReportCsvDTO.class);
    }

    public ShareholderType getType() {
        return ShareholderType.NON_RESIDENT;
    }

    @Override
    public ShareholderMasterNonResidential get(int index) {
        ShareholderMasterNonResidential shareholder = new ShareholderMasterNonResidential();
        for (FieldMapping fieldMapping : fieldMappings) {
            if(fieldMapping.getValueObjectField().equalsIgnoreCase("shareHeldFromDate") || fieldMapping.getValueObjectField().equalsIgnoreCase("shareHeldToDate")  || fieldMapping.getValueObjectField().equalsIgnoreCase("trcApplicableFrom")
            || fieldMapping.getValueObjectField().equalsIgnoreCase("trcApplicableTo") || fieldMapping.getValueObjectField().equalsIgnoreCase("tenfApplicableFrom") || fieldMapping.getValueObjectField().equalsIgnoreCase("tenfApplicableTo") ||
                    fieldMapping.getValueObjectField().equalsIgnoreCase("noPeDeclarationApplicableFrom")|| fieldMapping.getValueObjectField().equalsIgnoreCase("noPeDeclarationApplicableTo") || fieldMapping.getValueObjectField().equalsIgnoreCase("noPoemDeclarationInIndiaApplicableFrom") ||
                    fieldMapping.getValueObjectField().equalsIgnoreCase("noPoemDeclarationInIndiaApplicableTo"))
            {
                if (logger.isDebugEnabled()) {
                    logger.debug("Processing {}", fieldMapping.getExcelHeaderName());
                }
                populateValue(shareholder, index, fieldMapping);
            }
            else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Processing {}", fieldMapping.getExcelHeaderName());
                }
                populateValue(shareholder, index, fieldMapping);
            }
        }
        return shareholder;
    }

    public Optional<ShareholderMasterNonResidentialErrorReportCsvDTO> validate(int rowIndex) {
        StringJoiner errorMessages = new StringJoiner("\n");

        // validation check
        for (FieldMapping property : fieldMappings) {
            if (property.getValidator() != null) {
                String validationMessage = property.getValidator().apply(
                        getHeaders().get(getHeaderIndex(property.getExcelHeaderName().toLowerCase())),
                        getRawCellValue(rowIndex, property.getExcelHeaderName()));
                if (org.apache.commons.lang3.StringUtils.isNotEmpty(validationMessage)) {
                    errorMessages.add(validationMessage);
                }
            }
            if (property.getValidatorDouble() != null) {

                String validationMessage = getRawCellValue(rowIndex, property.getExcelHeaderName());
                String headerNamee = this.getHeaders()
                        .get(this.getHeaderIndex(property.getExcelHeaderName().toLowerCase()));
                if (StringUtils.isBlank(validationMessage))
                    errorMessages.add(headerNamee + " can not be empty");
            }
        }
        if (errorMessages.length() != 0) {
            ShareholderMasterNonResidentialErrorReportCsvDTO shareholderMasterNonResidentialErrorReportCsvDTO = getErrorDTO(
                    rowIndex);
            shareholderMasterNonResidentialErrorReportCsvDTO.setReason(errorMessages.toString());
            return Optional.of(shareholderMasterNonResidentialErrorReportCsvDTO);
        } else {
            return Optional.empty();
        }
    }

}
