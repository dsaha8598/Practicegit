package com.ey.in.tds.onboarding.web.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ey.in.tds.onboarding.service.deductee.DeducteeMasterValidateService;

@RestController
@RequestMapping("api/onboarding")
public class DeducteeValidationResource {

	@Autowired
	private DeducteeMasterValidateService deducteeMasterValidateService;
	//TODO NEED TO CHANGE FOR SQL

/*	@GetMapping("all/deductees/upload")
	public ResponseEntity<ApiStatus<BatchUpload>> saveExcelFileForDeductee(@RequestHeader("TAN-NUMBER") String tan,
			@RequestParam(value = "assesssmentYear", required = false) Integer assesssmentYear,
			@RequestParam(value = "assessmentMonth", required = false) Integer assessmentMonth)
			throws InvalidKeyException, IOException, URISyntaxException, StorageException {
		assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
		assessmentMonth = CommonUtil.getAssessmentMonthPlusOne(assessmentMonth);
		BatchUpload batch = deducteeMasterValidateService.saveDeducteeInExel(tan, assesssmentYear, assessmentMonth);
		ApiStatus<BatchUpload> apiStatus = new ApiStatus<BatchUpload>(HttpStatus.OK, "SUCCESS",
				"Retrieved all deductee uploaded records successfully", batch);
		return new ResponseEntity<ApiStatus<BatchUpload>>(apiStatus, HttpStatus.OK);

	}  */
}
