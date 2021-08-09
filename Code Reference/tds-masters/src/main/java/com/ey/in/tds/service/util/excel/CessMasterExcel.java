package com.ey.in.tds.service.util.excel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ey.in.tds.dto.ArticleRateMasterDTO;
import com.ey.in.tds.dto.ArticleRateMasterErrorReportDTO;
import com.ey.in.tds.service.sac.FieldMapping;

/**
 * 
 * @author vamsir
 *
 */
public class CessMasterExcel extends MasterExcel<ArticleRateMasterDTO, ArticleRateMasterErrorReportDTO> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final String HEADER_NATURE_OF_REMITTANCE = "Nature of Remittance";
	private static final String HEADER_NAME_OF_COUNTRY = "Name Of Country";
	private static final String HEADER_ARTICLE_NAME = "Article Name";
	private static final String HEADER_ARTICLE_NUMBER = "Article Number";
	private static final String HEADER_NATURE_OF_PAYMENT = "Nature of Payment";
	private static final String HEADER_MAKE_AVAILABLE_CONDITION_SATISFIED = "Make Available condition satisfied";
	private static final String HEADER_MFN_CLAUSE_EXISTS = "Most Favoured Nation ('MFN') Clause Exists";
	private static final String HEADER_MFN_APPLICABLE_TO_SCOPE_OR_RATE = "MFN applicable to Scope/Rate";
	private static final String HEADER_MLI_PRINCIPLE_PURPOSE_TEST_CONDITION_SATISFIED = "MLI Principle Purpose Test Condition Satisfied";
	private static final String HEADER_MLI_SIMPLIFIED_LIMITATION_ON_BENEFIT_CONDITION_SATISFIED = "MLI Simplified Limitation on Benefit Condition Satisifed";
	private static final String HEADER_MFN_CLAUSE_IS_AVAILED = "Withholding tax rate if benefit of Most Favoured Nation ('MFN') clause is availed";
	private static final String HEADER_MFN_CLAUSE_IS_NOT_AVAILED = "Withholding tax rate if benefit of Most Favoured Nation ('MFN') clause is not availed";
	private static final String HEADER_TYPE_OF_PAYEE = "Type of payee";
	private static final String HEADER_REMARKS = "Remarks";
	private static final String HEADER_NON_EXEMPT = "Non-Exempt";
	private static final String HEADER_RATE = "Rate";
	private static final String HEADER_INCLUSION_EXCLUSION = "Is this an Inclusion or Exclusion condition? (Inclusion/Exclusion)";
	private static final String HEADER_CONDITION_APPLICABLE = "Condition applicable (Y/N)";
	private static final String HEADER_CONDITION = "Condition";
	private static final String HEADER_DETAILED_CONDITION = "Detailed  condition";
	private static final String HEADER_APPLICABLE_FROM = "Applicable from";
	private static final String HEADER_APPLICABLE_TO = "Applicable to";

	@Override
	public List<FieldMapping> getFieldMappings() {
		return fieldMappings;
	}

	public static final List<FieldMapping> fieldMappings = Collections.unmodifiableList(Arrays.asList(

			new FieldMapping(HEADER_NATURE_OF_REMITTANCE, "natureOfRemittance", "natureOfRemittance"),
			new FieldMapping(HEADER_NAME_OF_COUNTRY, "country", "country"),
			new FieldMapping(HEADER_ARTICLE_NAME, "articleName", "articleName"),
			new FieldMapping(HEADER_ARTICLE_NUMBER, "articleNumber", "articleNumber"),
			new FieldMapping(HEADER_NATURE_OF_PAYMENT, "natureOfPayment", "natureOfPayment"),
			new FieldMapping(HEADER_MAKE_AVAILABLE_CONDITION_SATISFIED, "makeAvailableConditionSatisfied",
					"makeAvailableConditionSatisfied"),
			new FieldMapping(HEADER_MFN_CLAUSE_EXISTS, "mfnClauseExists", "mfnClauseExists"),
			new FieldMapping(HEADER_MFN_APPLICABLE_TO_SCOPE_OR_RATE, "mfnApplicableToScopeOrRate",
					"mfnApplicableToScopeOrRate"),
			new FieldMapping(HEADER_MLI_PRINCIPLE_PURPOSE_TEST_CONDITION_SATISFIED, "mliPrinciplePurpose",
					"mliPrinciplePurpose"),
			new FieldMapping(HEADER_MLI_SIMPLIFIED_LIMITATION_ON_BENEFIT_CONDITION_SATISFIED, "mliSimplifiedLimitation",
					"mliSimplifiedLimitation"),
			new FieldMapping(HEADER_MFN_CLAUSE_IS_AVAILED, "mfnClauseIsAvailed", "mfnClauseIsAvailed"),
			new FieldMapping(HEADER_MFN_CLAUSE_IS_NOT_AVAILED, "mfnClauseIsNotAvailed", "mfnClauseIsNotAvailed"),
			new FieldMapping(HEADER_TYPE_OF_PAYEE, "typeOfPayee", "typeOfPayee"),
			new FieldMapping(HEADER_REMARKS, "remarks", "remarks"),
			new FieldMapping(HEADER_NON_EXEMPT, "nonExempt", "nonExempt"),
			new FieldMapping(HEADER_RATE, "articleRate", "articleRate"),
			new FieldMapping(HEADER_INCLUSION_EXCLUSION, "isInclusionOrExclusion", "isInclusionOrExclusion"),
			new FieldMapping(HEADER_CONDITION_APPLICABLE, "conditionApplicable", "conditionApplicable"),
			new FieldMapping(HEADER_CONDITION, "condition", "condition"),
			new FieldMapping(HEADER_DETAILED_CONDITION, "detailedCondition", "detailedCondition"),
			new FieldMapping(HEADER_APPLICABLE_FROM, "applicableFrom", "applicableFrom"),
			new FieldMapping(HEADER_APPLICABLE_TO, "applicableTo", "applicableTo")));

	public CessMasterExcel(XSSFWorkbook workbook) {
		super(workbook, fieldMappings, ArticleRateMasterDTO.class, ArticleRateMasterErrorReportDTO.class);
	}

	@Override
	public ArticleRateMasterDTO get(int index) {
		ArticleRateMasterDTO articleMaster = new ArticleRateMasterDTO();
		for (FieldMapping fieldMapping : fieldMappings) {
			populateValue(articleMaster, index, fieldMapping);
		}
		return articleMaster;
	}

	public Optional<ArticleRateMasterErrorReportDTO> validate(int rowIndex) {
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
			ArticleRateMasterErrorReportDTO articleRateMasterDTO = this.getErrorDTO(rowIndex);
			articleRateMasterDTO.setReason(errorMessages.toString());
			return Optional.of(articleRateMasterDTO);
		} else {
			return Optional.empty();
		}
	}

}
