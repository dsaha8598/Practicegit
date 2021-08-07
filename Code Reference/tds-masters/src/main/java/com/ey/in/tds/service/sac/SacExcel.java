package com.ey.in.tds.service.sac;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ey.in.tds.common.dto.sac.ServicesAccountingCode;
import com.ey.in.tds.common.model.sac.SacErrorReportCsvDTO;
import com.ey.in.tds.service.util.excel.MasterExcel;

/**
 * 
 * @author
 *
 */
public class SacExcel extends MasterExcel<ServicesAccountingCode, SacErrorReportCsvDTO> {

	private static final String HEADER_HEADING_AND_GROUP = "Heading & Group";
	private static final String HEADER_SERVICE_CODE = "Service Code (Tariff)";
	private static final String HEADER_SERVICE_DESCRIPTION = "Service Description";
	private static final String HEADER_TDS_SECTION = "TDS Section (Only for Resident servicer providers)";
	private static final String HEADER_NATURE_OF_PAYMENT = "Nature of Payment";
	private static final String HEADER_FOR_HUF_INDIVIDUAL_SERVICE_PROVIDERS = "For HUF and Individal service providers";
	private static final String HEADER_FOR_OTHER_THAN_HUF_INDIVIDUAL_SERVICE_PROVIDERS = "For Other than HUF and Individuals service providers";
	private static final String HEADER_DIRECT_TAX_TEAM_COMMENT = "Direct Tax Team comment";
	private static final String HEADER_KEYWORDS_FROM_THIS_WORKSHEET = "Keywords from this worksheet";
	private static final String HEADER_ADDITIONAL_KEYWORDS_FROM_THIS_WORKSHEET = "Additional keywords from the Keywords worksheet";

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	public static List<FieldMapping> fieldMappings = Collections.unmodifiableList(Arrays.asList(

			new FieldMapping(HEADER_HEADING_AND_GROUP, "headingAndGroup"),
			new FieldMapping(HEADER_SERVICE_CODE, "serviceCode"), new FieldMapping(HEADER_TDS_SECTION, "tdsSection"),
			new FieldMapping(HEADER_NATURE_OF_PAYMENT, "natureOfPayment"),
			new FieldMapping(HEADER_FOR_HUF_INDIVIDUAL_SERVICE_PROVIDERS, "hufIndividal"),
			new FieldMapping(HEADER_FOR_OTHER_THAN_HUF_INDIVIDUAL_SERVICE_PROVIDERS, "otherThanHuf"),
			new FieldMapping(HEADER_DIRECT_TAX_TEAM_COMMENT, "directTaxTeam"),
			new FieldMapping(HEADER_KEYWORDS_FROM_THIS_WORKSHEET, "keywordsfromWorksheet"),
			new FieldMapping(HEADER_ADDITIONAL_KEYWORDS_FROM_THIS_WORKSHEET, "additionalKeywordsfromWorksheet"),
			new FieldMapping(HEADER_SERVICE_DESCRIPTION, "serviceDescription")));

	public SacExcel(XSSFWorkbook workbook) {

		super(workbook, fieldMappings, ServicesAccountingCode.class, SacErrorReportCsvDTO.class);

	}

	@Override
	public ServicesAccountingCode get(int index) {
		ServicesAccountingCode servicesAccountingCode = new ServicesAccountingCode();
		for (FieldMapping fieldMapping : fieldMappings) {
			populateValue(servicesAccountingCode, index, fieldMapping);
		}
		return servicesAccountingCode;
	}

	public Optional<SacErrorReportCsvDTO> validate(int rowIndex) {
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
			SacErrorReportCsvDTO sacErrorReportCsvDTO = this.getErrorDTO(rowIndex);
			sacErrorReportCsvDTO.setReason(errorMessages.toString());
			return Optional.of(sacErrorReportCsvDTO);
		} else {
			return Optional.empty();
		}
	}

	@Override
	public List<FieldMapping> getFieldMappings() {

		return fieldMappings;
	}

}
