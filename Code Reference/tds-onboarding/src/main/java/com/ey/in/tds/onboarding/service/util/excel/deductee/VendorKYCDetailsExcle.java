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

import com.ey.in.tds.common.model.deductee.KYCDetailsErrorFilDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.KYCDetails;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.common.util.excel.FieldMapping;

/**
 * 
 * @author vamsir
 *
 */
public class VendorKYCDetailsExcle extends Excel<KYCDetails, KYCDetailsErrorFilDTO> {

	private static final String HEADER_VENDOR_NAME = "Vendor Name";
	private static final String HEADER_VENDOR_PAN = "Vendor PAN";
	private static final String HEADER_VENDOR_CODE = "Vendor Code";
	private static final String HEADER_EMAIL_ID = "Email-ID";
	private static final String HEADER_MOBILE_NUMBER = "Mobile Number";
	private static final String HEADER_EMAIL_TRIGGERED = "Email Triggered?";

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public List<FieldMapping> getFieldMappings() {
		return fieldMappings;
	}

	public static List<FieldMapping> fieldMappings = Collections.unmodifiableList(Arrays.asList(
			new FieldMapping(HEADER_VENDOR_NAME, "customerName", "customerName", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_VENDOR_PAN, "customerPan", "customerPan"),
			new FieldMapping(HEADER_VENDOR_CODE, "customerCode", "customerCode", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_EMAIL_ID, "emailId", "emailId", Excel.VALIDATION_MANDATORY),
			new FieldMapping(HEADER_MOBILE_NUMBER, "phoneNumber", "phoneNumber"), new FieldMapping(
					HEADER_EMAIL_TRIGGERED, "isEmailTriggered", "isEmailTriggered", Excel.VALIDATION_MANDATORY)));

	public VendorKYCDetailsExcle(XSSFWorkbook workbook) {
		super(workbook, fieldMappings, KYCDetails.class, KYCDetailsErrorFilDTO.class);
	}

	public DeducteeType getType() {
		return DeducteeType.RESIDENT;
	}

	@Override
	public KYCDetails get(int index) {
		KYCDetails kycDetails = new KYCDetails();
		for (FieldMapping fieldMapping : fieldMappings) {
			populateValue(kycDetails, index, fieldMapping);
		}
		return kycDetails;
	}

	public Optional<KYCDetailsErrorFilDTO> validate(int rowIndex) {
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
			KYCDetailsErrorFilDTO kycDetailsErrorFileDTO = this.getErrorDTO(rowIndex);
			kycDetailsErrorFileDTO.setReason(errorMessages.toString());
			return Optional.of(kycDetailsErrorFileDTO);
		} else {
			return Optional.empty();
		}
	}

}
