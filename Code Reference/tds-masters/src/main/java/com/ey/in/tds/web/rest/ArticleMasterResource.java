package com.ey.in.tds.web.rest;

import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.apache.tika.Tika;
import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.ArticleMaster;
import com.ey.in.tds.common.domain.MasterBatchUpload;
import com.ey.in.tds.common.dto.ArticleDTO;
import com.ey.in.tds.common.dto.ArticleMasterRateDTO;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.security.AllowedMimeTypes;
import com.ey.in.tds.core.service.BaseResource;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.dto.ArticleMasterDTO;
import com.ey.in.tds.service.ArticleMasterService;
import com.ey.in.tds.web.rest.util.MasterESAPIValidation;

import io.micrometer.core.annotation.Timed;

/**
 * REST controller for managing ArticleMaster.
 */
@RestController
@CrossOrigin("*")
@RequestMapping("/api/masters")
public class ArticleMasterResource extends BaseResource {

	@Autowired
	private final ArticleMasterService articleMasterService;

	public ArticleMasterResource(ArticleMasterService articleMasterService) {
		this.articleMasterService = articleMasterService;
	}

	/**
	 * POST /article-masters : Create a new articleMaster.
	 *
	 * @param articleMaster the articleMaster to create
	 * @return the ResponseEntity with status 201 (Created) and with body the new
	 *         articleMaster
	 * @throws URISyntaxException  if the Location URI syntax is incorrect
	 * @throws ParseException
	 * @throws ValidationException
	 * @throws IntrusionException
	 */

	@PostMapping(value = "/article", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<ApiStatus<ArticleMaster>> createArticleMaster(
			@Valid @RequestBody ArticleMasterDTO articleMasterDTO, @RequestHeader("X-USER-EMAIL") String userName)
			throws URISyntaxException, IntrusionException, ValidationException, ParseException, IllegalAccessException,
			InvocationTargetException {
		MultiTenantContext.setTenantId("master");
		if (articleMasterDTO.getId() != null) {
			throw new CustomException("Record cannot have an Id already", HttpStatus.BAD_REQUEST);
		}
		if (articleMasterDTO.getApplicableTo() != null
				&& (articleMasterDTO.getApplicableFrom().equals(articleMasterDTO.getApplicableTo())
						|| articleMasterDTO.getApplicableFrom().isAfter(articleMasterDTO.getApplicableTo()))) {
			throw new CustomException("From date cannot be equal to or greater than To Date", HttpStatus.BAD_REQUEST);
		}
		// ESAPI Validating user input
		MasterESAPIValidation.articleMasterMasterInputValidation(articleMasterDTO);
		ArticleMaster result = articleMasterService.saveArticleMaster(articleMasterDTO, userName);

		ApiStatus<ArticleMaster> apiStatus = new ApiStatus<ArticleMaster>(HttpStatus.CREATED,
				"To create a Article Master", "Article Master Created", result);
		return new ResponseEntity<ApiStatus<ArticleMaster>>(apiStatus, HttpStatus.OK);

	}

	/**
	 * GET /article-masters/:id : get the "id" articleMaster.
	 *
	 * @param id the id of the articleMaster to retrieve
	 * @return the ResponseEntity with status 200 (OK) and with body the
	 *         articleMaster, or with status 404 (Not Found)
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */

	@GetMapping(value = "/article/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<ApiStatus<ArticleMasterDTO>> getArticleMaster(@PathVariable Long id)
			throws IllegalAccessException, InvocationTargetException {
		MultiTenantContext.setTenantId("master");
		if (id == null) {
			throw new CustomException("Id cannot be Null", HttpStatus.BAD_REQUEST);
		}
		ArticleMasterDTO articleMaster = articleMasterService.findOne(id);
		ApiStatus<ArticleMasterDTO> apiStatus = new ApiStatus<ArticleMasterDTO>(HttpStatus.OK,
				"To get a Single Article Master", "Article Master Single Record", articleMaster);
		return new ResponseEntity<ApiStatus<ArticleMasterDTO>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * GET /article-masters : get all the articleMasters.
	 *
	 * @param pageable the pagination information
	 * @return the ResponseEntity with status 200 (OK) and the list of
	 *         articleMasters in body
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	@GetMapping(value = "/article", produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<ApiStatus<List<ArticleMasterDTO>>> getAllArticleMasters()
			throws IllegalAccessException, InvocationTargetException {
		MultiTenantContext.setTenantId("master");
		List<ArticleMasterDTO> articleMasterList = articleMasterService.findAll();
		info("REST request to get a page of ArticleMasters : {}", articleMasterList);

		ApiStatus<List<ArticleMasterDTO>> apiStatus = new ApiStatus<List<ArticleMasterDTO>>(HttpStatus.CREATED,
				"To get a Single Article Master", "Article Master Single Record", articleMasterList);

		return new ResponseEntity<ApiStatus<List<ArticleMasterDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * PUT /article-masters : Updates an existing articleMaster.
	 *
	 * @param articleMaster the articleMaster to update
	 * @return the ResponseEntity with status 200 (OK) and with body the updated
	 *         articleMaster, or with status 400 (Bad Request) if the articleMaster
	 *         is not valid, or with status 500 (Internal Server Error) if the
	 *         articleMaster couldn't be updated
	 * @throws URISyntaxException  if the Location URI syntax is incorrect
	 * @throws ParseException
	 * @throws ValidationException
	 * @throws IntrusionException
	 */

	@PutMapping(value = "/article", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<ApiStatus<ArticleMaster>> updateArticleMaster(
			@Valid @RequestBody ArticleMasterDTO articleMasterDTO, @RequestHeader("X-USER-EMAIL") String userName)
			throws URISyntaxException, IntrusionException, ValidationException, ParseException {
		MultiTenantContext.setTenantId("master");
		if (articleMasterDTO.getId() == null) {
			throw new CustomException("Id cannot be Null", HttpStatus.BAD_REQUEST);
		}
		if (articleMasterDTO.getApplicableTo() != null
				&& (articleMasterDTO.getApplicableFrom().equals(articleMasterDTO.getApplicableTo())
						|| articleMasterDTO.getApplicableFrom().isAfter(articleMasterDTO.getApplicableTo()))) {
			throw new CustomException("Applicable To cannot be greater than Applicable From", HttpStatus.BAD_REQUEST);
		}
		// ESAPI Validating user input
		MasterESAPIValidation.articleMasterMasterInputValidation(articleMasterDTO);
		ArticleMaster result = articleMasterService.updateArticleMaster(articleMasterDTO, userName);
		ApiStatus<ArticleMaster> apiStatus = new ApiStatus<ArticleMaster>(HttpStatus.OK,
				"To update a Single Article Master", "Article Master Updated", result);
		return new ResponseEntity<ApiStatus<ArticleMaster>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * This API is to fetch all article master records based on the country
	 * 
	 * @param country
	 * @return
	 */
	@GetMapping(value = "/articlesbycountry/{country}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ApiStatus<List<ArticleDTO>>> getArticlesByCountry(
			@PathVariable(value = "country") String country) {
		MultiTenantContext.setTenantId("master");
		List<ArticleDTO> articleMaster = articleMasterService.getArticlesByCountry(country);
		ApiStatus<List<ArticleDTO>> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", "NO ALERT", articleMaster);
		return new ResponseEntity<ApiStatus<List<ArticleDTO>>>(apiStatus, HttpStatus.OK);
	}

	/**
	 * 
	 * @param articleDTO
	 * @return
	 */
	@PostMapping("/articlerate")
	public ResponseEntity<ApiStatus<ArticleDTO>> getArticleRate(@RequestBody ArticleMasterRateDTO articleDTO) {
		MultiTenantContext.setTenantId("master");
		ArticleDTO articleMaster = articleMasterService.getArticleRate(articleDTO);
		ApiStatus<ArticleDTO> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", "NO ALERT", articleMaster);
		return new ResponseEntity<ApiStatus<ArticleDTO>>(apiStatus, HttpStatus.OK);

	}
	
	//Feign client
	@GetMapping("/articlemaster")
	public ResponseEntity<ApiStatus<List<Map<String, Object>>>> getArticleMasterData() {
		MultiTenantContext.setTenantId("master");
		List<Map<String, Object>> articleMaster = articleMasterService.getArticleMasterData();
		ApiStatus<List<Map<String, Object>>> apiStatus = new ApiStatus<>(HttpStatus.OK, "SUCCESS", "NO ALERT", articleMaster);
		return new ResponseEntity<ApiStatus<List<Map<String, Object>>>>(apiStatus, HttpStatus.OK);

	}

	/**
	 * This API for article master excel upload.
	 * 
	 * @param file
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @param userName
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/articlemaster/upload/excel")
	public ResponseEntity<ApiStatus<MasterBatchUpload>> articleMasteruploadExcel(
			@RequestParam("file") MultipartFile file,
			@RequestParam(value = "year", required = false) Integer assesssmentYear,
			@RequestParam(value = "assessmentMonth", required = false) Integer assessmentMonth,
			@RequestHeader(value = "X-USER-EMAIL") String userName) throws Exception {
		assesssmentYear = CommonUtil.getAssessmentYear(assesssmentYear);
		assessmentMonth = CommonUtil.getAssessmentMonthPlusOne(assessmentMonth);
		MasterBatchUpload masterBatchUpload = null;
		logger.info("Testing Something in Upload Excel");
		MultiTenantContext.setTenantId("master");
		String contentType = new Tika().detect(file.getInputStream());
		if (AllowedMimeTypes.XLSX.getMimeType().equals(contentType)
				|| AllowedMimeTypes.OOXML.getMimeType().equals(contentType)) {
			try {
				masterBatchUpload = articleMasterService.saveFileData(file, assesssmentYear, assessmentMonth, userName);
			} catch (Exception e) {
				throw new CustomException("Unable to process the file", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			logger.warn("Unsupported file.");
			throw new CustomException("Invalid file, only Excel files are allowed", HttpStatus.BAD_REQUEST);
		}
		ApiStatus<MasterBatchUpload> apiStatus = new ApiStatus<MasterBatchUpload>(HttpStatus.OK, "SUCCESS",
				"UPLOADED ARTICLE MASTER FILE SUCCESSFULLY ", masterBatchUpload);
		return new ResponseEntity<ApiStatus<MasterBatchUpload>>(apiStatus, HttpStatus.OK);
	}

}
