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

import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.common.util.excel.FieldMapping;
import com.ey.in.tds.onboarding.dto.collector.CollecteeSectionThresholdLedgerCsvDTO;
import com.ey.in.tds.onboarding.dto.collector.CollecteeSectionThresholdLedgerDTO;

public class CollecteeSectionThreshholdLedgerExcel extends Excel<CollecteeSectionThresholdLedgerDTO, CollecteeSectionThresholdLedgerCsvDTO> {

	private static final String HEADER_SERIAL_NUMBER = "S.No";
	private static final String HEADER_COLLECTEE_CODE = "Collectee Code";
	private static final String HEADER_COLLECTEE_NAME = "Collectee Name";
	private static final String HEADER_OPENING_BALANCE_INVOICE = "Opening Balance - Invoice";
	private static final String HEADER_OPENING_BALANCE_ADVANCE = "Opening Balance - Advance";
	private static final String HEADER_LEDGER_BALANCE = "Ledger Balance \n" + "(as on report date)";
	private static final String HEADER_TOTAL_TCS_PAID = "Total TCS paid";
	private static final String HEADER_SECTION = "Section";

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public List<FieldMapping> getFieldMappings() {
		return fieldMappings;
	}

	public static List<FieldMapping> fieldMappings = Collections.unmodifiableList(
			Arrays.asList(new FieldMapping(HEADER_SERIAL_NUMBER, "serialNumber", "serialNumber"),
					new FieldMapping(HEADER_COLLECTEE_CODE, "collecteeCode", "collecteeCode"),
					new FieldMapping(HEADER_COLLECTEE_NAME, "collectorPan", "collectorPan"),
					new FieldMapping(HEADER_OPENING_BALANCE_INVOICE, "amountUtilized", "amountUtilized"),
					new FieldMapping(HEADER_OPENING_BALANCE_ADVANCE, "advancePending", "advancePending"),
					new FieldMapping(HEADER_LEDGER_BALANCE, "amountUtilized", "amountUtilized"),
					new FieldMapping(HEADER_TOTAL_TCS_PAID, "amountUtilized", "amountUtilized"),
					new FieldMapping(HEADER_SECTION, "collecteeSection", "collecteeSection")));

	public CollecteeSectionThreshholdLedgerExcel(XSSFWorkbook workbook) {
		super(workbook, fieldMappings, CollecteeSectionThresholdLedgerDTO.class, CollecteeSectionThresholdLedgerCsvDTO.class);
	}

	@Override
	public CollecteeSectionThresholdLedgerDTO get(int index) {
		CollecteeSectionThresholdLedgerDTO collectee = new CollecteeSectionThresholdLedgerDTO();
		for (FieldMapping fieldMapping : fieldMappings) {
			populateValue(collectee, index, fieldMapping);
		}
		return collectee;
	}

	public Optional<CollecteeSectionThresholdLedgerCsvDTO> validate(int rowIndex) {
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
			CollecteeSectionThresholdLedgerCsvDTO collecteeMasterErrorReportCsvDTO = this.getErrorDTO(rowIndex);
			collecteeMasterErrorReportCsvDTO.setReason(errorMessages.toString());
			return Optional.of(collecteeMasterErrorReportCsvDTO);
		} else {
			return Optional.empty();
		}
	}

}
