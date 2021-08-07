package com.ey.in.tds.onboarding.service.util.excel.shareholder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ey.in.tds.common.model.shareholder.ShareholderMasterResidentialErrorReportCsvDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.ShareholderMasterResidential;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.common.util.excel.FieldMapping;

public class ResidentShareholderExcel extends Excel<ShareholderMasterResidential,  ShareholderMasterResidentialErrorReportCsvDTO> {
    private static final String HEADER_SHAREHOLDER_FOLIONO = "FOLIO NUMBER";
    private static final String HEADER_SHAREHOLDER_UNIQUE_IDENTIFICATION_NUMBER = "UNIQUE SHAREHOLDER IDENTIFICATION NUMBER";
    private static final String HEADER_SHAREHOLDER_NAME = "SHAREHOLDER NAME";
    private static final String HEADER_SHAREHOLDER_CATEGORY = "CATEGORY OF SHAREHOLDER";
    private static final String HEADER_SHAREHOLDER_TYPE = "TYPE OF SHAREHOLDER";
    private static final String HEADER_KEY_SHAREHOLDER = "KEY SHAREHOLDER (AFFILIATED/ RELATED ENTITY)";
    private static final String HEADER_SHAREHOLDER_PAN = "PAN";
    private static final String HEADER_FLAT_DOOR_NO = "FLAT/ DOOR/ BUILDING NUMBER";
    private static final String HEADER_NAME_BUILDING_VILLAGE = "NAME OF PREMISES/ BUILDING/  VILLAGE";
    private static final String HEADER_ROAD_STREET = "ROAD/ STREET";
    private static final String HEADER_AREA_LOCALITY = "AREA/ LOCALITY";
    private static final String HEADER_TOWN_DISTRICT = "TOWN/ CITY/ DISTRICT";
    private static final String HEADER_STATE = "STATE";
    private static final String HEADER_PIN_CODE = "PIN/ ZIP CODE";
    private static final String HEADER_COUNTRY = "COUNTRY";
    private static final String HEADER_EMAIL_ID = "EMAIL ID";
    private static final String HEADER_CONTACT = "CONTACT NUMBER";
    private static final String HEADER_SHARE_TRANSFER_AGENT_NAME = "SHARE TRANSFER AGENT NAME";
    private static final String HEADER_DEMAT_ACCOUNT_NO = "DEMAT ACCOUNT NUMBER";
    private static final String HEADER_TOTAL_SHARES_HELD = "TOTAL NUMBER OF SHARES HELD";
    private static final String HEADER_PERCENTAGE_SHARES_HELD = "PERCENTAGE OF SHARES HELD";
    private static final String HEADER_FORM_15GH_AVAILABLE = "IS FORM 15G/H AVAILABLE";
    private static final String HEADER_FORM_15GH_UNIQUE_IDENTIFICATION_NO = "FORM 15G/H UNIQUE IDENTIFICATION NUMBER";
    private static final String HEADER_AADHARNUMBER = "AADHAAR NUMBER";



    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public List<FieldMapping> getFieldMappings() {
        return fieldMappings;
    }

    public static List<FieldMapping> fieldMappings = Collections.unmodifiableList(Arrays.asList(
            new FieldMapping(HEADER_SHAREHOLDER_FOLIONO, "folioNo", "shareholder_master_folio_no",Excel.VALIDATION_MANDATORY),
            new FieldMapping(HEADER_SHAREHOLDER_UNIQUE_IDENTIFICATION_NUMBER, "uniqueIdentificationNumber", "uniqueIdentificationNumber"),
            new FieldMapping(HEADER_SHAREHOLDER_NAME, "shareholderName", "shareholder_master_name",
                    Excel.VALIDATION_MANDATORY),
            new FieldMapping(HEADER_SHAREHOLDER_CATEGORY, "shareholderCategory", "shareholder_master_category", Excel.VALIDATION_MANDATORY),
            new FieldMapping(HEADER_SHAREHOLDER_TYPE, "shareholderType", "shareholder_master_type"),
            new FieldMapping(HEADER_KEY_SHAREHOLDER, "keyShareholder", "shareholder_master_key_shareholder"),
            new FieldMapping(HEADER_SHAREHOLDER_PAN, "shareholderPan", "shareholder_master_pan"),
            new FieldMapping(HEADER_AADHARNUMBER,"aadharNumber","aadharNumber"),
            new FieldMapping(HEADER_FLAT_DOOR_NO, "flatDoorBlockNo", "shareholder_master_flat_door_block_no"),
            new FieldMapping(HEADER_NAME_BUILDING_VILLAGE, "nameBuildingVillage",
                    "shareholder_master_name_building_village"),
            new FieldMapping(HEADER_ROAD_STREET, "roadStreetPostoffice", "shareholder_master_road_street_postoffice"),
            new FieldMapping(HEADER_AREA_LOCALITY, "areaLocality", "shareholder_master_area_locality"),
            new FieldMapping(HEADER_TOWN_DISTRICT, "townCityDistrict", "shareholder_master_town_city_district"),
            new FieldMapping(HEADER_STATE, "state", "shareholder_master_state",
                    (val) -> val != null ? ((String) val).toUpperCase() : null),
            new FieldMapping(HEADER_PIN_CODE, "pinCode", "shareholder_master_pin_code"),
            new FieldMapping(HEADER_COUNTRY, "country", "shareholder_master_country",Excel.VALIDATION_MANDATORY),
            new FieldMapping(HEADER_EMAIL_ID, "emailId", "shareholder_master_email_id"),
            new FieldMapping(HEADER_CONTACT, "contact", "shareholder_master_contact"),
            new FieldMapping(HEADER_SHARE_TRANSFER_AGENT_NAME, "shareTransferAgentName", "shareholder_master_share_transfer_agent_name"),
            new FieldMapping(HEADER_DEMAT_ACCOUNT_NO, "dematAccountNo", "shareholder_master_demat_account_no"),
            new FieldMapping(HEADER_TOTAL_SHARES_HELD, "totalSharesHeld", "shareholder_master_total_of_shares_held"),
            new FieldMapping(HEADER_PERCENTAGE_SHARES_HELD, "percentageSharesHeld", "shareholder_master_percetage_of_share_held"),
            new FieldMapping(HEADER_FORM_15GH_AVAILABLE,"form15ghAvailable","shareholder_master_form15gh_available"),
            new FieldMapping(HEADER_FORM_15GH_UNIQUE_IDENTIFICATION_NO,"form15ghUniqueIdentificationNo","shareholder_master_form15gh_unique_identification_no")));

    public ResidentShareholderExcel(XSSFWorkbook workbook) {
        super(workbook, fieldMappings, ShareholderMasterResidential.class,
                ShareholderMasterResidentialErrorReportCsvDTO.class);
    }

    public ShareholderType getType() {
        return ShareholderType.RESIDENT;
    }

    @Override
    public ShareholderMasterResidential get(int index) {
        ShareholderMasterResidential shareholder = new ShareholderMasterResidential();
        for (FieldMapping fieldMapping : fieldMappings) {
            populateValue(shareholder, index, fieldMapping);
        }
        return shareholder;
    }

    public Optional<ShareholderMasterResidentialErrorReportCsvDTO> validate(int rowIndex) {
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
            ShareholderMasterResidentialErrorReportCsvDTO shareholderMasterResidentialErrorReportCsvDTO = this
                    .getErrorDTO(rowIndex);
            shareholderMasterResidentialErrorReportCsvDTO.setReason(errorMessages.toString());
            return Optional.of(shareholderMasterResidentialErrorReportCsvDTO);
        } else {
            return Optional.empty();
        }
    }
}
