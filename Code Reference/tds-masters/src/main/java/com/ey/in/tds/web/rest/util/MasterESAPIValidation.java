package com.ey.in.tds.web.rest.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.validation.Valid;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Validator;
import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;
import org.springframework.http.HttpStatus;

import com.ey.in.tcs.common.TCSMasterDTO;
import com.ey.in.tds.common.domain.FineRateMaster;
import com.ey.in.tds.common.domain.NatureOfPaymentMaster;
import com.ey.in.tds.common.domain.TdsMonthTracker;
import com.ey.in.tds.common.domain.tcs.TCSFineRateMaster;
import com.ey.in.tds.common.domain.tcs.TCSMonthTracker;
import com.ey.in.tds.common.domain.tcs.TCSNatureOfIncome;
import com.ey.in.tds.common.dto.CustomThresholdGroupLimit;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.dto.ArticleMasterDTO;
import com.ey.in.tds.dto.CessMasterDTO;
import com.ey.in.tds.dto.CessTypeMasterDTO;
import com.ey.in.tds.dto.SurchargeMasterDTO;
import com.ey.in.tds.dto.TdsMasterDTO;
import com.ey.in.tds.tcs.dto.TCSCessMasterDTO;
import com.ey.in.tds.tcs.dto.TCSSurchargeMasterDTO;

public class MasterESAPIValidation {

	/**
	 * 
	 * @param natureOfPaymentMaster
	 * @throws IntrusionException
	 * @throws ValidationException
	 * @throws ParseException
	 */
	public static void natureOfPaymentMasterInputValidation(NatureOfPaymentMaster natureOfPaymentMaster)
			throws IntrusionException, ValidationException, ParseException {
		try {
			Validator validator = ESAPI.validator();
			DateFormat formatter = new SimpleDateFormat(CommonUtil.TDS_DATE_FORMAT);
			validator.getValidInput("Section input", natureOfPaymentMaster.getSection(), "NatureOfPaymentSection", 9,
					false);
			validator.getValidInput("Nature of payment input", natureOfPaymentMaster.getNature(),
					"NatureOfPayment", 2048, false);
			validator.getValidInput("Dispaly Name input", natureOfPaymentMaster.getDisplayValue(),
					"NatureOfPaymentSection", 5, false);
			Date date = Date.from(natureOfPaymentMaster.getApplicableFrom());
			String fromDate = formatter.format(date);
			validator.getValidDate("Applicable From input", fromDate, formatter, false);
			String toDate = null;
			if (natureOfPaymentMaster.getApplicableTo() != null) {
				date = Date.from(natureOfPaymentMaster.getApplicableTo());
				toDate = formatter.format(date);
			}
			validator.getValidDate("Applicable To input", toDate, formatter, true);
		} catch (Exception e) {
			throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * 
	 * @param surchargeMasterDTO
	 * @throws IntrusionException
	 * @throws ValidationException
	 * @throws ParseException
	 */
	public static void surchargeMasterInputValidation(SurchargeMasterDTO surchargeMasterDTO)
			throws IntrusionException, ValidationException, ParseException {
		try {
			Validator validator = ESAPI.validator();
			DateFormat formatter = new SimpleDateFormat(CommonUtil.TDS_DATE_FORMAT);
			String rate = String.valueOf(surchargeMasterDTO.getSurchargeRate());
			validator.isValidDouble("Surcharge rate input", rate, 0, 100, false);
			Date date = Date.from(surchargeMasterDTO.getApplicableFrom());
			String fromDate = formatter.format(date);
			validator.getValidDate("Applicable From input", fromDate, formatter, false);
			String toDate = null;
			if (surchargeMasterDTO.getApplicableTo() != null) {
				date = Date.from(surchargeMasterDTO.getApplicableTo());
				toDate = formatter.format(date);
			}
			validator.getValidDate("Applicable To input", toDate, formatter, true);
		} catch (Exception e) {
			throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	
	
	/**
	 * 
	 * @param TCSsurchargeMasterDTO
	 * @throws IntrusionException
	 * @throws ValidationException
	 * @throws ParseException
	 */
	public static void surchargeMasterInputValidation(TCSSurchargeMasterDTO surchargeMasterDTO)
			throws IntrusionException, ValidationException, ParseException {
		try {
			Validator validator = ESAPI.validator();
			DateFormat formatter = new SimpleDateFormat(CommonUtil.TDS_DATE_FORMAT);
			//String rate = String.valueOf(surchargeMasterDTO.getSurchargeRate());
			//validator.isValidDouble("Surcharge rate input", rate, 0, 100, false);
			Date date = Date.from(surchargeMasterDTO.getApplicableFrom());
			String fromDate = formatter.format(date);
			validator.getValidDate("Applicable From input", fromDate, formatter, false);
			String toDate = null;
			if (surchargeMasterDTO.getApplicableTo() != null) {
				date = Date.from(surchargeMasterDTO.getApplicableTo());
				toDate = formatter.format(date);
			}
			validator.getValidDate("Applicable To input", toDate, formatter, true);
		} catch (Exception e) {
			throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * 
	 * @param cessTypeMasterDTO
	 * @throws IntrusionException
	 * @throws ValidationException
	 * @throws ParseException
	 */
	public static void cessTypeMasterInputValidation(CessTypeMasterDTO cessTypeMasterDTO)
			throws IntrusionException, ValidationException, ParseException {
		try {
			Validator validator = ESAPI.validator();
			DateFormat formatter = new SimpleDateFormat(CommonUtil.TDS_DATE_FORMAT);
			validator.getValidInput("Cess type input", cessTypeMasterDTO.getCessType(), "SafeString", 50, false);
			Date date = Date.from(cessTypeMasterDTO.getApplicableFrom());
			String fromDate = formatter.format(date);
			validator.getValidDate("Applicable From input", fromDate, formatter, false);
			String toDate = null;
			if (cessTypeMasterDTO.getApplicableTo() != null) {
				date = Date.from(cessTypeMasterDTO.getApplicableTo());
				toDate = formatter.format(date);
			}
			validator.getValidDate("Applicable To input", toDate, formatter, true);
		} catch (Exception e) {
			throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * 
	 * @param cessMasterDTO
	 * @throws IntrusionException
	 * @throws ValidationException
	 * @throws ParseException
	 */
	public static void cessMasterInputValidation(CessMasterDTO cessMasterDTO)
			throws IntrusionException, ValidationException, ParseException {
		try {
			Validator validator = ESAPI.validator();
			DateFormat formatter = new SimpleDateFormat(CommonUtil.TDS_DATE_FORMAT);
			validator.getValidInput("Cess type input", cessMasterDTO.getCessTypeName(), "SafeString", 50, true);
			String rate = String.valueOf(cessMasterDTO.getRate());
			validator.getValidInput("Surcharge rate input", rate, "SafeString", 6, false);
			Date date = Date.from(cessMasterDTO.getApplicableFrom());
			String fromDate = formatter.format(date);
			validator.getValidDate("Applicable From input", fromDate, formatter, false);
			String toDate = null;
			if (cessMasterDTO.getApplicableTo() != null) {
				date = Date.from(cessMasterDTO.getApplicableTo());
				toDate = formatter.format(date);
			}
			validator.getValidDate("Applicable To input", toDate, formatter, true);
		} catch (Exception e) {
			throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	
	/**
	 * 
	 * @param cessMasterDTO
	 * @throws IntrusionException
	 * @throws ValidationException
	 * @throws ParseException
	 */
	public static void cessMasterInputValidation(TCSCessMasterDTO cessMasterDTO)
			throws IntrusionException, ValidationException, ParseException {
		try {
			Validator validator = ESAPI.validator();
			DateFormat formatter = new SimpleDateFormat(CommonUtil.TDS_DATE_FORMAT);
			validator.getValidInput("Cess type input", cessMasterDTO.getCessTypeName(), "SafeString", 50, true);
			String rate = String.valueOf(cessMasterDTO.getRate());
			validator.getValidInput("Surcharge rate input", rate, "SafeString", 6, false);
			Date date = Date.from(cessMasterDTO.getApplicableFrom());
			String fromDate = formatter.format(date);
			validator.getValidDate("Applicable From input", fromDate, formatter, false);
			String toDate = null;
			if (cessMasterDTO.getApplicableTo() != null) {
				date = Date.from(cessMasterDTO.getApplicableTo());
				toDate = formatter.format(date);
			}
			validator.getValidDate("Applicable To input", toDate, formatter, true);
		} catch (Exception e) {
			throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	

	/**
	 * 
	 * @param tdsMonthTracker
	 * @throws IntrusionException
	 * @throws ValidationException
	 * @throws ParseException
	 */
	public static void tdsMonthTrackerInputValidation(@Valid TdsMonthTracker tdsMonthTracker)
			throws IntrusionException, ValidationException, ParseException {
		try {
			Validator validator = ESAPI.validator();
			DateFormat formatter = new SimpleDateFormat(CommonUtil.TDS_DATE_FORMAT);
			String month = String.valueOf(tdsMonthTracker.getMonth());
			validator.isValidNumber("Month input", month, 0, 3, false);
			String year = String.valueOf(tdsMonthTracker.getYear());
			validator.isValidNumber("Year type input", year, 0, 5, false);
			String dueDateForFiling = formatter.format(tdsMonthTracker.getDueDateForFiling());
			validator.getValidDate("Due date for filing input", dueDateForFiling, formatter, false);
			String dueDateForChallanPayment = formatter.format(tdsMonthTracker.getDueDateForChallanPayment());
			validator.getValidDate("Due date for Challan payment input", dueDateForChallanPayment, formatter, false);
			String monthClosureForProcessing = formatter.format(tdsMonthTracker.getMonthClosureForProcessing());
			validator.getValidDate("Month closure for processing input", monthClosureForProcessing, formatter, false);
			Date date = Date.from(tdsMonthTracker.getApplicableFrom());
			String fromDate = formatter.format(date);
			validator.getValidDate("Applicable From input", fromDate, formatter, false);
			String toDate = null;
			if (tdsMonthTracker.getApplicableTo() != null) {
				date = Date.from(tdsMonthTracker.getApplicableTo());
				toDate = formatter.format(date);
			}
			validator.getValidDate("Applicable To input", toDate, formatter, true);
		} catch (Exception e) {
			throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	

	/**
	 * 
	 * @param tcsMonthTracker
	 * @throws IntrusionException
	 * @throws ValidationException
	 * @throws ParseException
	 */
	public static void tcsMonthTrackerInputValidation(@Valid TCSMonthTracker tcsMonthTracker)
			throws IntrusionException, ValidationException, ParseException {
		try {
			Validator validator = ESAPI.validator();
			DateFormat formatter = new SimpleDateFormat(CommonUtil.TDS_DATE_FORMAT);
			String month = String.valueOf(tcsMonthTracker.getMonth());
			validator.isValidNumber("Month input", month, 0, 3, false);
			String year = String.valueOf(tcsMonthTracker.getYear());
			validator.isValidNumber("Year type input", year, 0, 5, false);
			String dueDateForFiling = formatter.format(tcsMonthTracker.getDueDateForFiling());
			validator.getValidDate("Due date for filing input", dueDateForFiling, formatter, false);
			String dueDateForChallanPayment = formatter.format(tcsMonthTracker.getDueDateForChallanPayment());
			validator.getValidDate("Due date for Challan payment input", dueDateForChallanPayment, formatter, false);
			String monthClosureForProcessing = formatter.format(tcsMonthTracker.getMonthClosureForProcessing());
			validator.getValidDate("Month closure for processing input", monthClosureForProcessing, formatter, false);
			Date date = Date.from(tcsMonthTracker.getApplicableFrom());
			String fromDate = formatter.format(date);
			validator.getValidDate("Applicable From input", fromDate, formatter, false);
			String toDate = null;
			if (tcsMonthTracker.getApplicableTo() != null) {
				date = Date.from(tcsMonthTracker.getApplicableTo());
				toDate = formatter.format(date);
			}
			validator.getValidDate("Applicable To input", toDate, formatter, true);
		} catch (Exception e) {
			throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * 
	 * @param tdsMasterDTO
	 * @throws IntrusionException
	 * @throws ValidationException
	 * @throws ParseException
	 */
	public static void tdsRateInputValidation(TdsMasterDTO tdsMasterDTO)
			throws IntrusionException, ValidationException, ParseException {
		try {
			Validator validator = ESAPI.validator();
			DateFormat formatter = new SimpleDateFormat(CommonUtil.TDS_DATE_FORMAT);
			validator.getValidInput("Nature of payment", tdsMasterDTO.getNatureOfPaymentMaster(), "NatureOfPayment", 2048, true);
			String rate = String.valueOf(tdsMasterDTO.getRate());
			validator.isValidDouble("Rate", rate, 0, 100, false);
			validator.getValidInput("SAC code", tdsMasterDTO.getSaccode(), "SafeString", 15, true);
			validator.getValidInput("Deductee resident status", tdsMasterDTO.getResidentialStatusName(), "SafeString",
					10, true);
			//String annualTransactionLimit = String.valueOf(tdsMasterDTO.getAnnualTransactionLimit());
			//validator.isValidNumber("Annual transaction limit", annualTransactionLimit, 0, 15, false);
			String perTransactionLimit = String.valueOf(tdsMasterDTO.getPerTransactionLimit());
			validator.isValidNumber("Per transaction limit", perTransactionLimit, 0, 15, false);
			Date date = Date.from(tdsMasterDTO.getApplicableFrom());
			String fromDate = formatter.format(date);
			validator.getValidDate("Applicable From input", fromDate, formatter, false);
			String toDate = null;
			if (tdsMasterDTO.getApplicableTo() != null) {
				date = Date.from(tdsMasterDTO.getApplicableTo());
				toDate = formatter.format(date);
			}
			validator.getValidDate("Applicable To input", toDate, formatter, true);
		} catch (Exception e) {
			throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	
	/**
	 * 
	 * @param tcsMasterDTO
	 * @throws IntrusionException
	 * @throws ValidationException
	 * @throws ParseException
	 */
	public static void tcsRateInputValidation(TCSMasterDTO tcsMasterDTO)
			throws IntrusionException, ValidationException, ParseException {
		try {
			Validator validator = ESAPI.validator();
			DateFormat formatter = new SimpleDateFormat(CommonUtil.TDS_DATE_FORMAT);
			//validator.getValidInput("Nature of Income", tcsMasterDTO.getNatureOfPaymentMaster(), "NatureOfIncome", 120, true);
			String rate = String.valueOf(tcsMasterDTO.getRate());
			validator.isValidDouble("Rate", rate, 0, 100, false);
//			validator.getValidInput("SAC code", tcsMasterDTO.getSaccode(), "SafeString", 6, true);
//			validator.getValidInput("Deductee resident status", tcsMasterDTO.getResidentialStatusName(), "SafeString",
//					10, true);
//			String annualTransactionLimit = String.valueOf(tcsMasterDTO.getAnnualTransactionLimit());
//			validator.isValidNumber("Annual transaction limit", annualTransactionLimit, 0, 15, false);
			String perTransactionLimit = String.valueOf(tcsMasterDTO.getPerTransactionLimit());
			validator.isValidNumber("Per transaction limit", perTransactionLimit, 0, 15, false);
			Date date = Date.from(tcsMasterDTO.getApplicableFrom());
			String fromDate = formatter.format(date);
			validator.getValidDate("Applicable From input", fromDate, formatter, false);
			String toDate = null;
			if (tcsMasterDTO.getApplicableTo() != null) {
				date = Date.from(tcsMasterDTO.getApplicableTo());
				toDate = formatter.format(date);
			}
			validator.getValidDate("Applicable To input", toDate, formatter, true);
		} catch (Exception e) {
			throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}


	/**
	 * 
	 * @param fineRateMaster
	 * @throws IntrusionException
	 * @throws ValidationException
	 * @throws ParseException
	 */
	public static void fineRateMasterInputValidation(FineRateMaster fineRateMaster)
			throws IntrusionException, ValidationException, ParseException {
		try {
			Validator validator = ESAPI.validator();
			DateFormat formatter = new SimpleDateFormat(CommonUtil.TDS_DATE_FORMAT);
			validator.getValidInput("Interest type input", fineRateMaster.getInterestType(), "SafeString", 20, false);
			String rate = String.valueOf(fineRateMaster.getRate());
			validator.isValidDouble("Rate", rate, 0, 100, false);
			String finePerDay = String.valueOf(fineRateMaster.getFinePerDay());
			validator.isValidDouble("Fine per day input", finePerDay, 0, 100, true);
			validator.getValidInput("Type of interest calculation input", fineRateMaster.getTypeOfIntrestCalculation(),
					"SafeString", 20, false);
			Date date = Date.from(fineRateMaster.getApplicableFrom());
			String fromDate = formatter.format(date);
			validator.getValidDate("Applicable From input", fromDate, formatter, false);
			String toDate = null;
			if (fineRateMaster.getApplicableTo() != null) {
				date = Date.from(fineRateMaster.getApplicableTo());
				toDate = formatter.format(date);
			}
			validator.getValidDate("Applicable To input", toDate, formatter, true);
		} catch (Exception e) {
			throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * 
	 * @param articleMasterDTO
	 * @throws IntrusionException
	 * @throws ValidationException
	 * @throws ParseException
	 */
	public static void articleMasterMasterInputValidation(ArticleMasterDTO articleMasterDTO)
			throws IntrusionException, ValidationException, ParseException {
		try {
			Validator validator = ESAPI.validator();
			DateFormat formatter = new SimpleDateFormat(CommonUtil.TDS_DATE_FORMAT);
			validator.getValidInput("Country", articleMasterDTO.getCountry(), "Country", 64, false);
			validator.getValidInput("Article Number", articleMasterDTO.getArticleNumber(), "SafeString", 30, false);
			validator.getValidInput("Article Name", articleMasterDTO.getArticleName(), "SafeString", 30, false);
			String rate = String.valueOf(articleMasterDTO.getArticleRate());
			validator.isValidDouble("Rate", rate, 0, 100, false);
			Date date = Date.from(articleMasterDTO.getApplicableFrom());
			String fromDate = formatter.format(date);
			validator.getValidDate("Applicable From input", fromDate, formatter, false);
			String toDate = null;
			if (articleMasterDTO.getApplicableTo() != null) {
				date = Date.from(articleMasterDTO.getApplicableTo());
				toDate = formatter.format(date);
			}
			validator.getValidDate("Applicable To input", toDate, formatter, true);
		} catch (Exception e) {
			throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	/**
	 * 
	 * @param TCSFindRateMaster
	 * @throws IntrusionException
	 * @throws ValidationException
	 * @throws ParseException
	 */
	public static void tcsFineRateMasterInputValidation(TCSFineRateMaster tcsFineRateMaster)
			throws IntrusionException, ValidationException, ParseException {
		try {
			Validator validator = ESAPI.validator();
			DateFormat formatter = new SimpleDateFormat(CommonUtil.TDS_DATE_FORMAT);
			validator.getValidInput("Interest type input", tcsFineRateMaster.getInterestType(), "SafeString", 20,
					false);
			String rate = String.valueOf(tcsFineRateMaster.getRate());
			validator.isValidDouble("Rate", rate, 0, 100, false);
			String finePerDay = String.valueOf(tcsFineRateMaster.getFinePerDay());
			validator.isValidDouble("Fine per day input", finePerDay, 0, 100, true);
			validator.getValidInput("Type of interest calculation input",
					tcsFineRateMaster.getTypeOfIntrestCalculation(), "SafeString", 20, false);
			Date date = Date.from(tcsFineRateMaster.getApplicableFrom());
			String fromDate = formatter.format(date);
			validator.getValidDate("Applicable From input", fromDate, formatter, false);
			String toDate = null;
			if (tcsFineRateMaster.getApplicableTo() != null) {
				date = Date.from(tcsFineRateMaster.getApplicableTo());
				toDate = formatter.format(date);
			}
			validator.getValidDate("Applicable To input", toDate, formatter, true);
		} catch (Exception e) {
			throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	/**
	 * 
	 * @param tcsNatureOfIncome
	 * @throws IntrusionException
	 * @throws ValidationException
	 * @throws ParseException
	 */
	public static void natureOfIncomeInputValidation(@Valid TCSNatureOfIncome tcsNatureOfIncome)
			throws IntrusionException, ValidationException, ParseException{
		try {
			Validator validator = ESAPI.validator();
			DateFormat formatter = new SimpleDateFormat(CommonUtil.TDS_DATE_FORMAT);
			validator.getValidInput("Section input", tcsNatureOfIncome.getSection(), "NatureOfPaymentSection", 10,
					false);
			validator.getValidInput("Nature of income input", tcsNatureOfIncome.getNature(),
					"NatureOfPayment", 2048, false);
			validator.getValidInput("Dispaly Name input", tcsNatureOfIncome.getDisplayValue(),
					"NatureOfPaymentSection", 10, false);
			Date date = Date.from(tcsNatureOfIncome.getApplicableFrom());
			String fromDate = formatter.format(date);
			validator.getValidDate("Applicable From input", fromDate, formatter, false);
			String toDate = null;
			if (tcsNatureOfIncome.getApplicableTo() != null) {
				date = Date.from(tcsNatureOfIncome.getApplicableTo());
				toDate = formatter.format(date);
			}
			validator.getValidDate("Applicable To input", toDate, formatter, true);
		} catch (Exception e) {
			throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * 
	 * @param thresholdGroup
	 */
	public static void tdsThresholdGroup(@Valid CustomThresholdGroupLimit thresholdGroup) {
		try {
			Validator validator = ESAPI.validator();
			DateFormat formatter = new SimpleDateFormat(CommonUtil.TDS_DATE_FORMAT);
			Date date = Date.from(thresholdGroup.getApplicableFrom());
			String fromDate = formatter.format(date);
			validator.getValidDate("Applicable From input", fromDate, formatter, false);
			String toDate = null;
			if (thresholdGroup.getApplicableTo() != null) {
				date = Date.from(thresholdGroup.getApplicableTo());
				toDate = formatter.format(date);
			}
			validator.getValidDate("Applicable To input", toDate, formatter, true);
		} catch (Exception e) {
			throw new CustomException(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
}
