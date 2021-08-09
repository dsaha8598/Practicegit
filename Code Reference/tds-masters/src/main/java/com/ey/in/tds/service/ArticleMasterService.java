package com.ey.in.tds.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.aspose.cells.AutoFilter;
import com.aspose.cells.BackgroundType;
import com.aspose.cells.Cell;
import com.aspose.cells.CellsHelper;
import com.aspose.cells.Color;
import com.aspose.cells.ImportTableOptions;
import com.aspose.cells.Range;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Style;
import com.aspose.cells.TextAlignmentType;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.ey.in.tds.common.domain.ArticleMaster;
import com.ey.in.tds.common.domain.ArticleMasterConditions;
import com.ey.in.tds.common.domain.ArticleMasterDetailedConditions;
import com.ey.in.tds.common.domain.MasterBatchUpload;
import com.ey.in.tds.common.dto.ArticleDTO;
import com.ey.in.tds.common.dto.ArticleMasterRateDTO;
import com.ey.in.tds.common.service.Sha256SumService;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.dto.ArticleMasterConditionsDTO;
import com.ey.in.tds.dto.ArticleMasterDTO;
import com.ey.in.tds.dto.ArticleMasterDetailedConditionsDTO;
import com.ey.in.tds.dto.ArticleRateMasterDTO;
import com.ey.in.tds.dto.ArticleRateMasterErrorReportDTO;
import com.ey.in.tds.repository.ArticleMasterRepository;
import com.ey.in.tds.repository.MasterBatchUploadRepository;
import com.ey.in.tds.service.util.excel.ArticleMasterExcel;
import com.ey.in.tds.service.util.excel.MasterExcel;
import com.ey.in.tds.web.rest.util.CommonValidations;
import com.microsoft.azure.storage.StorageException;

/**
 * Service Implementation for managing ArticleMaster.
 */
@Service
public class ArticleMasterService {

	private final Logger logger = LoggerFactory.getLogger(ArticleMasterService.class);

	@Autowired
	private final ArticleMasterRepository articleMasterRepository;

	public ArticleMasterService(ArticleMasterRepository articleMasterRepository) {
		this.articleMasterRepository = articleMasterRepository;
	}

	@Autowired
	private Sha256SumService sha256SumService;

	@Autowired
	private MasterBatchUploadRepository masterBatchUploadRepository;

	@Autowired
	private MasterBatchUploadService masterBatchUploadService;

	/**
	 * Save a articleMaster.
	 *
	 * @param articleMasterDTO to save Article master entity
	 * @param userName
	 * @return String
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	public ArticleMaster saveArticleMaster(ArticleMasterDTO articleMasterDTO, String userName)
			throws IllegalAccessException, InvocationTargetException {

		if (logger.isDebugEnabled()) {
			logger.debug("REST request to save ArticleMaster : {}", articleMasterDTO);
		}
		Optional<ArticleMaster> articleMasterDb = articleMasterRepository.getArticleByCountryNameNumber(
				articleMasterDTO.getCountry(), articleMasterDTO.getArticleName(), articleMasterDTO.getArticleNumber());
		if (articleMasterDb.isPresent()) {
			CommonValidations.validateApplicableFields(articleMasterDb.get().getApplicableTo(),
					articleMasterDTO.getApplicableFrom());
		}
		ArticleMaster articleMaster = new ArticleMaster();
		ArticleMasterConditions articleMasterConditions = null;
		Set<ArticleMasterConditions> setArticleMasterConditions = new HashSet<>();
		Set<ArticleMasterDetailedConditions> setArticleMasterDetailedConditions = new HashSet<>();

		ArticleMasterDetailedConditions articleMasterDetailedConditions = null;

		Set<ArticleMasterConditionsDTO> articleMasterConditionsDTOs = articleMasterDTO.getArticleMasterConditions();
		Set<ArticleMasterDetailedConditionsDTO> articleMasterDetailedConditionsDTOs = articleMasterDTO
				.getArticleMasterConditions().iterator().next().getArticleMasterDetailedConditions();
		articleMaster.setApplicableFrom(articleMasterDTO.getApplicableFrom());
		articleMaster.setApplicableTo(articleMasterDTO.getApplicableTo());
		articleMaster.setArticleName(articleMasterDTO.getArticleName());
		articleMaster.setArticleNumber(articleMasterDTO.getArticleNumber());
		articleMaster.setArticleRate(articleMasterDTO.getArticleRate());
		articleMaster.setCountry(articleMasterDTO.getCountry());
		articleMaster.setIsInclusionOrExclusion(articleMasterDTO.getIsInclusionOrExclusion());
		articleMaster.setActive(true);
		articleMaster.setCreatedBy(userName);
		articleMaster.setCreatedDate(Instant.now());
		articleMaster.setModifiedBy(userName);
		articleMaster.setModifiedDate(Instant.now());
		articleMaster.setNatureOfRemittance(articleMasterDTO.getNatureOfRemittance());
		articleMaster.setMakeAvailableConditionSatisfied(articleMasterDTO.getMakeAvailableConditionSatisfied());
		articleMaster.setMfnClauseExists(articleMasterDTO.getMfnClauseExists());
		articleMaster.setMliPrinciplePurpose(articleMasterDTO.getMliPrinciplePurpose());
		articleMaster.setMliSimplifiedLimitation(articleMasterDTO.getMliSimplifiedLimitation());
		articleMaster.setMfnClauseIsAvailed(articleMasterDTO.getMfnClauseIsAvailed());
		articleMaster.setMfnClauseIsNotAvailed(articleMasterDTO.getMfnClauseIsNotAvailed());
		articleMaster.setExempt(articleMasterDTO.getExempt());
		for (ArticleMasterConditionsDTO articleConditions : articleMasterConditionsDTOs) {
			articleMasterConditions = new ArticleMasterConditions();
			articleMasterConditions.setCondition(articleConditions.getCondition());
			articleMasterConditions
					.setIsDetailedConditionApplicable(articleConditions.getIsDetailedConditionApplicable());
			articleMasterConditions.setIsInclusionOrExclusion(articleMasterDTO.getIsInclusionOrExclusion());
			articleMasterConditions.setArticleMaster(articleMaster);
			if (articleConditions.getIsDetailedConditionApplicable()) {
				for (ArticleMasterDetailedConditionsDTO articleDetailedConditions : articleMasterDetailedConditionsDTOs) {
					articleMasterDetailedConditions = new ArticleMasterDetailedConditions();

					articleMasterDetailedConditions.setCondition(articleDetailedConditions.getCondition());
					articleMasterDetailedConditions.setArticleMasterCondition(articleMasterConditions);
					setArticleMasterDetailedConditions.add(articleMasterDetailedConditions);
				}
				articleMasterConditions.setArticleMasterDetailedConditions(setArticleMasterDetailedConditions);
			}
			setArticleMasterConditions.add(articleMasterConditions);
		}
		articleMaster.setArticleMasterConditions(setArticleMasterConditions);
		try {
			articleMaster = articleMasterRepository.save(articleMaster);
		} catch (Exception exception) {
			logger.error("Error while saving the ArticleMaster data :" + exception);
		}

		return articleMaster;
	}

	/**
	 * Get one articleMaster by id.
	 *
	 * @param id the id of the entity
	 * @return the entity
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	@Transactional(readOnly = true)
	public ArticleMasterDTO findOne(Long id) throws IllegalAccessException, InvocationTargetException {
		if (logger.isDebugEnabled()) {
			logger.debug("REST request to get ArticleMaster : {}", id);
		}
		ArticleMasterDTO articleMasterDTO = new ArticleMasterDTO();
		Set<ArticleMasterConditionsDTO> articleMasterConditionsDTOList = new HashSet<>();
		ArticleMasterConditionsDTO articleMasterConditionsDTO = null;
		Optional<ArticleMaster> articleMaterOptional = articleMasterRepository.findById(id);

		if (articleMaterOptional.isPresent()) {
			ArticleMaster articleMaster = articleMaterOptional.get();
			articleMasterDTO.setId(articleMaster.getId());
			articleMasterDTO.setNatureOfRemittance(articleMaster.getNatureOfRemittance());
			articleMasterDTO.setArticleName(articleMaster.getArticleName());
			articleMasterDTO.setArticleNumber(articleMaster.getArticleNumber());
			articleMasterDTO.setArticleRate(articleMaster.getArticleRate());
			articleMasterDTO.setCountry(articleMaster.getCountry());
			articleMasterDTO.setApplicableFrom(articleMaster.getApplicableFrom());
			articleMasterDTO.setApplicableTo(articleMaster.getApplicableTo());
			articleMasterDTO.setIsInclusionOrExclusion(articleMaster.getIsInclusionOrExclusion());
			articleMasterDTO.setActive(articleMaster.isActive());
			articleMasterDTO.setNatureOfRemittance(articleMaster.getNatureOfRemittance());
			articleMasterDTO.setMakeAvailableConditionSatisfied(articleMaster.getMakeAvailableConditionSatisfied());
			articleMasterDTO.setMfnClauseExists(articleMaster.getMfnClauseExists());
			articleMasterDTO.setMliPrinciplePurpose(articleMaster.getMliPrinciplePurpose());
			articleMasterDTO.setMliSimplifiedLimitation(articleMaster.getMliSimplifiedLimitation());
			articleMasterDTO.setMfnClauseIsAvailed(articleMaster.getMfnClauseIsAvailed());
			articleMasterDTO.setMfnClauseIsNotAvailed(articleMaster.getMfnClauseIsNotAvailed());
			articleMasterDTO.setExempt(articleMaster.getExempt());

			Set<ArticleMasterConditions> articleMasterConditionsList = articleMaster.getArticleMasterConditions();
			for (ArticleMasterConditions articleMasterConditions : articleMasterConditionsList) {
				articleMasterConditionsDTO = new ArticleMasterConditionsDTO();
				articleMasterConditionsDTO.setId(articleMasterConditions.getId());
				articleMasterConditionsDTO.setCondition(articleMasterConditions.getCondition());
				articleMasterConditionsDTO
						.setIsDetailedConditionApplicable(articleMasterConditions.getIsDetailedConditionApplicable());
				Set<ArticleMasterDetailedConditionsDTO> articleMasterDetailedConditionsDTOList = new HashSet<>();
				if (articleMasterConditions.getIsDetailedConditionApplicable()) {
					ArticleMasterDetailedConditionsDTO articleMasterDetailedConditionsDTO = null;
					Set<ArticleMasterDetailedConditions> articleMasterDetailedConditionsList = articleMasterConditions
							.getArticleMasterDetailedConditions();

					for (ArticleMasterDetailedConditions articleMasterDetailedConditions : articleMasterDetailedConditionsList) {
						articleMasterDetailedConditionsDTO = new ArticleMasterDetailedConditionsDTO();
						articleMasterDetailedConditionsDTO.setId(articleMasterDetailedConditions.getId());
						articleMasterDetailedConditionsDTO.setCondition(articleMasterDetailedConditions.getCondition());
						articleMasterDetailedConditionsDTOList.add(articleMasterDetailedConditionsDTO);
					}

					articleMasterConditionsDTO
							.setArticleMasterDetailedConditions(articleMasterDetailedConditionsDTOList);
				}

				articleMasterConditionsDTOList.add(articleMasterConditionsDTO);
			}
			articleMasterDTO.setArticleMasterConditions(articleMasterConditionsDTOList);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("REST request to get ArticleMaster Record : {}", articleMasterDTO);
		}
		return articleMasterDTO;
	}

	/**
	 * Get all the articleMasters.
	 *
	 * @param pageable the pagination information
	 * @return the list of entities
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	@Transactional(readOnly = true)
	public List<ArticleMasterDTO> findAll() throws IllegalAccessException, InvocationTargetException {
		List<ArticleMasterDTO> articleMasterDTOList = new ArrayList<>();
		List<ArticleMaster> artileMasterList = articleMasterRepository.findAll();
		artileMasterList.stream().sorted(Comparator.comparing(ArticleMaster::getArticleName).reversed())
				.collect(Collectors.toList());

		ArticleMasterDTO articleMasterDTO = null;
		for (ArticleMaster articleMaster : artileMasterList) {
			articleMasterDTO = new ArticleMasterDTO();
			articleMasterDTO.setId(articleMaster.getId());
			articleMasterDTO.setArticleName(articleMaster.getArticleName());
			articleMasterDTO.setArticleNumber(articleMaster.getArticleNumber());
			articleMasterDTO.setArticleRate(articleMaster.getArticleRate());
			articleMasterDTO.setCountry(articleMaster.getCountry());
			articleMasterDTO.setApplicableFrom(articleMaster.getApplicableFrom());
			articleMasterDTO.setApplicableTo(articleMaster.getApplicableTo());
			articleMasterDTO.setIsInclusionOrExclusion(articleMaster.getIsInclusionOrExclusion());
			articleMasterDTO.setActive(articleMaster.isActive());
			articleMasterDTO.setNatureOfRemittance(articleMaster.getNatureOfRemittance());
			articleMasterDTO.setMakeAvailableConditionSatisfied(articleMaster.getMakeAvailableConditionSatisfied());
			articleMasterDTO.setMfnClauseExists(articleMaster.getMfnClauseExists());
			articleMasterDTO.setMliPrinciplePurpose(articleMaster.getMliPrinciplePurpose());
			articleMasterDTO.setMliSimplifiedLimitation(articleMaster.getMliSimplifiedLimitation());
			articleMasterDTO.setMfnClauseIsAvailed(articleMaster.getMfnClauseIsAvailed());
			articleMasterDTO.setMfnClauseIsNotAvailed(articleMaster.getMfnClauseIsNotAvailed());
			articleMasterDTO.setExempt(articleMaster.getExempt());

			Set<ArticleMasterConditionsDTO> articleMasterConditionsDTOList = new HashSet<>();
			ArticleMasterConditionsDTO articleMasterConditionsDTO = null;
			Set<ArticleMasterConditions> articleMasterConditionsList = articleMaster.getArticleMasterConditions();

			for (ArticleMasterConditions articleMasterConditions : articleMasterConditionsList) {
				articleMasterConditionsDTO = new ArticleMasterConditionsDTO();
				articleMasterConditionsDTO.setId(articleMasterConditions.getId());
				articleMasterConditionsDTO.setCondition(articleMasterConditions.getCondition());
				articleMasterConditionsDTO
						.setIsDetailedConditionApplicable(articleMasterConditions.getIsDetailedConditionApplicable());

				Set<ArticleMasterDetailedConditions> articleMasterDetailedConditionsList;
				Set<ArticleMasterDetailedConditionsDTO> articleMasterDetailedConditionsDTOList = new HashSet<>();
				if (articleMasterConditions.getIsDetailedConditionApplicable()) {
					ArticleMasterDetailedConditionsDTO articleMasterDetailedConditionsDTO = null;
					articleMasterDetailedConditionsList = articleMasterConditions.getArticleMasterDetailedConditions();

					for (ArticleMasterDetailedConditions articleMasterDetailedConditions : articleMasterDetailedConditionsList) {
						articleMasterDetailedConditionsDTO = new ArticleMasterDetailedConditionsDTO();
						articleMasterDetailedConditionsDTO.setId(articleMasterDetailedConditions.getId());
						articleMasterDetailedConditionsDTO.setCondition(articleMasterDetailedConditions.getCondition());
						articleMasterDetailedConditionsDTOList.add(articleMasterDetailedConditionsDTO);
					}

					articleMasterConditionsDTO
							.setArticleMasterDetailedConditions(articleMasterDetailedConditionsDTOList);
				}

				articleMasterConditionsDTOList.add(articleMasterConditionsDTO);
			}
			articleMasterDTO.setArticleMasterConditions(articleMasterConditionsDTOList);
			articleMasterDTOList.add(articleMasterDTO);
		}
		return articleMasterDTOList;
	}

	/**
	 * Update a articleMaster.
	 *
	 * @param articleMasterDTO to update Article master entity
	 * @param userName
	 * @return String
	 */

	public ArticleMaster updateArticleMaster(@Valid ArticleMasterDTO articleMasterDTO, String userName) {
		if (logger.isDebugEnabled()) {
			logger.debug("REST request to update ArticleMaster : {}", articleMasterDTO);
		}
		Optional<ArticleMaster> articleMasterData = articleMasterRepository.findById(articleMasterDTO.getId());
		ArticleMaster articleMaster = null;
		Set<ArticleMasterConditionsDTO> articleMasterConditionsDTOs = null;
		Set<ArticleMasterDetailedConditionsDTO> articleMasterDetailedConditionsDTOs = null;
		if (articleMasterData.isPresent()) {

			articleMaster = articleMasterData.get();
			ArticleMasterConditions articleMasterConditions = null;
			Set<ArticleMasterConditions> setArticleMasterConditions = new HashSet<>();
			Set<ArticleMasterDetailedConditions> setArticleMasterDetailedConditions = new HashSet<>();

			ArticleMasterDetailedConditions articleMasterDetailedConditions = null;

			articleMaster.setApplicableFrom(articleMasterDTO.getApplicableFrom());
			articleMaster.setApplicableTo(articleMasterDTO.getApplicableTo());
			articleMaster.setArticleName(articleMasterDTO.getArticleName());
			articleMaster.setArticleNumber(articleMasterDTO.getArticleNumber());
			articleMaster.setArticleRate(articleMasterDTO.getArticleRate());
			articleMaster.setCountry(articleMasterDTO.getCountry());
			articleMaster.setIsInclusionOrExclusion(articleMasterDTO.getIsInclusionOrExclusion());
			articleMaster.setModifiedBy(userName);
			articleMaster.setModifiedDate(Instant.now());
			articleMasterConditionsDTOs = articleMasterDTO.getArticleMasterConditions();

			for (ArticleMasterConditionsDTO articleConditions : articleMasterConditionsDTOs) {
				articleMasterConditions = new ArticleMasterConditions();

				articleMasterConditions.setId(articleConditions.getId());
				articleMasterConditions.setCondition(articleConditions.getCondition());
				articleMasterConditions
						.setIsDetailedConditionApplicable(articleConditions.getIsDetailedConditionApplicable());
				articleMasterConditions.setIsInclusionOrExclusion(articleMasterDTO.getIsInclusionOrExclusion());
				articleMasterConditions.setArticleMaster(articleMaster);

				setArticleMasterConditions.add(articleMasterConditions);
				articleMasterDetailedConditionsDTOs = articleConditions.getArticleMasterDetailedConditions();
				if (articleConditions.getIsDetailedConditionApplicable()) {
					for (ArticleMasterDetailedConditionsDTO articleDetailedConditions : articleMasterDetailedConditionsDTOs) {
						articleMasterDetailedConditions = new ArticleMasterDetailedConditions();

						articleMasterDetailedConditions.setId(articleDetailedConditions.getId());
						articleMasterDetailedConditions.setCondition(articleDetailedConditions.getCondition());
						articleMasterDetailedConditions.setArticleMasterCondition(articleMasterConditions);
						setArticleMasterDetailedConditions.add(articleMasterDetailedConditions);
					}
					articleMasterConditions.setArticleMasterDetailedConditions(setArticleMasterDetailedConditions);
				}
				setArticleMasterConditions.add(articleMasterConditions);
			}
			articleMaster.setArticleMasterConditions(setArticleMasterConditions);
		}

		try {
			articleMaster = articleMasterRepository.save(articleMaster);
		} catch (Exception e) {
			logger.error("Error occured while updating article record");
		}
		return articleMaster;
	}

	/**
	 * 
	 * @param country
	 * @return
	 */
	public List<ArticleDTO> getArticlesByCountry(String country) {
		return articleMasterRepository.getArticlesByCountry(country).parallelStream().map(this::convertArticleDTO)
				.collect(Collectors.toList());
	}

	/**
	 * 
	 * @param articleMaster
	 * @return
	 */
	public ArticleDTO convertArticleDTO(ArticleMaster articleMaster) {
		ArticleDTO articleDTO = new ArticleDTO();
		articleDTO.setArticleName(articleMaster.getArticleName());
		articleDTO.setArticleNumber(articleMaster.getArticleNumber());
		return articleDTO;
	}

	/**
	 * 
	 * @param articleMasterDTO
	 * @return
	 */
	public ArticleDTO getArticleRate(ArticleMasterRateDTO articleMasterDTO) {
		ArticleDTO articleDTO = new ArticleDTO();
		String[] treaty = articleMasterDTO.getNatureOfRemittance().split("-");
		String treatyType = treaty[treaty.length - 1];
		ArticleMaster articleMaster = null;
		// TODO nature of remittance need to re-write dynamically.
		if (treatyType.toUpperCase().contains("ROYAL")) {
			treatyType = "ROYALTY";
		} else if (treatyType.toUpperCase().contains("INTEREST")) {
			treatyType = "INTEREST";
		} else {
			treatyType = "FTS";
		}

		if (StringUtils.isNotBlank(articleMasterDTO.getArticleNumber())) {
			Optional<ArticleMaster> articleMasterResponse = articleMasterRepository.getArticleRate(
					articleMasterDTO.getCountry(), articleMasterDTO.getArticleNumber(), treatyType.trim());
			if (articleMasterResponse.isPresent()) {
				articleMaster = articleMasterResponse.get();
			}
		} else {
			List<ArticleMaster> articleMasterList = articleMasterRepository
					.getArticleRateByNatureOfRemittance(articleMasterDTO.getCountry(), treatyType.trim());
			if (!articleMasterList.isEmpty()) {
				articleMaster = articleMasterList.get(0);
			}
		}

		if (articleMaster != null) {
			if (articleMasterDTO.getMakeAvailableCondition() != null
					&& articleMasterDTO.getMakeAvailableCondition().equals(true) && articleMasterDTO.getIsLOB() != null
					&& articleMasterDTO.getIsLOB().equals(true)) {
				if (articleMasterDTO.getIsMFN() != null) {
					articleMaster.setMfnClauseExists(articleMasterDTO.getIsMFN());
				}
				if (articleMaster.getMfnClauseExists().equals(true)) {
					articleDTO.setRate(articleMaster.getMfnClauseIsAvailed());
				} else {
					articleDTO.setRate(articleMaster.getMfnClauseIsNotAvailed());
				}

			} else {
				articleDTO.setRate(articleMaster.getArticleRate());
			}
			articleDTO.setArticleName(articleMaster.getArticleName());
			articleDTO.setArticleNumber(articleMaster.getArticleNumber());
		}

		return articleDTO;
	}

	/**
	 * 
	 * @param file
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @param userName
	 * @return
	 * @throws IOException
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws InvalidKeyException
	 */
	@Transactional
	public MasterBatchUpload saveFileData(MultipartFile file, Integer assesssmentYear, Integer assessmentMonth,
			String userName) throws IOException, InvalidKeyException, URISyntaxException, StorageException {

		String uploadType = UploadTypes.ARTICLE_MASTER_EXCEL.name();
		
		String sha256 = sha256SumService.getSHA256Hash(file);
		if (isAlreadyProcessed(sha256)) {
			MasterBatchUpload masterBatchUpload = new MasterBatchUpload();
			masterBatchUpload.setCreatedDate(Instant.now());
			masterBatchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			masterBatchUpload.setCreatedBy(userName);
			masterBatchUpload.setSuccessCount(0L);
			masterBatchUpload.setFailedCount(0L);
			masterBatchUpload.setRowsCount(0L);
			masterBatchUpload.setProcessed(0);
			masterBatchUpload.setMismatchCount(0L);
			masterBatchUpload.setSha256sum(sha256);
			masterBatchUpload.setStatus("Duplicate");
			masterBatchUpload.setNewStatus("Duplicate");
			masterBatchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			masterBatchUpload = masterBatchUploadService.masterBatchUpload(masterBatchUpload, file, assesssmentYear,
					assessmentMonth, userName, null, uploadType);
			return masterBatchUpload;
		}
		try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());) {
			XSSFSheet worksheet = workbook.getSheetAt(0);
			XSSFRow headerRow = worksheet.getRow(0);
			int headersCount = ArticleMasterExcel.getNonEmptyCellsCount(headerRow);
			logger.info("Column header count :{}", headersCount);
			MasterBatchUpload masterBatchUpload = new MasterBatchUpload();
			if (headersCount != ArticleMasterExcel.fieldMappings.size()) {
				masterBatchUpload.setCreatedDate(Instant.now());
				masterBatchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
				masterBatchUpload.setSuccessCount(0L);
				masterBatchUpload.setFailedCount(0L);
				masterBatchUpload.setRowsCount(0L);
				masterBatchUpload.setProcessed(0);
				masterBatchUpload.setMismatchCount(0L);
				masterBatchUpload.setSha256sum(sha256);
				masterBatchUpload.setStatus("Failed");
				masterBatchUpload.setCreatedBy(userName);
				masterBatchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
				return masterBatchUploadService.masterBatchUpload(masterBatchUpload, file, assesssmentYear,
						assessmentMonth, userName, null, uploadType);
			} else {
				masterBatchUpload.setCreatedDate(Instant.now());
				masterBatchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
				masterBatchUpload.setStatus("Processing");
				masterBatchUpload.setSuccessCount(0L);
				masterBatchUpload.setFailedCount(0L);
				masterBatchUpload.setRowsCount(0L);
				masterBatchUpload.setProcessed(0);
				masterBatchUpload.setMismatchCount(0L);
				masterBatchUpload = masterBatchUploadService.masterBatchUpload(masterBatchUpload, file, assesssmentYear,
						assessmentMonth, userName, null, uploadType);
			}
			if (headersCount == ArticleMasterExcel.fieldMappings.size()) {
				return saveArticleMasterData(workbook, file, sha256, assesssmentYear, assessmentMonth, userName,
						masterBatchUpload, uploadType);
			} else {
				throw new CustomException("Data not found ", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to process article master data ", e);
		}
	}

	/**
	 * 
	 * @param workbook
	 * @param file
	 * @param sha256
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @param userName
	 * @param masterBatchUpload
	 * @return
	 * @throws IOException
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws InvalidKeyException
	 */
	@Async
	private MasterBatchUpload saveArticleMasterData(XSSFWorkbook workbook, MultipartFile file, String sha256,
			Integer assesssmentYear, Integer assessmentMonth, String userName, MasterBatchUpload masterBatchUpload,
			String uploadType) throws InvalidKeyException, URISyntaxException, StorageException, IOException {

		File articleErrorFile = null;
		ArrayList<ArticleRateMasterErrorReportDTO> errorList = new ArrayList<>();
		try {
			ArticleMasterExcel excelData = new ArticleMasterExcel(workbook);
			masterBatchUpload.setSha256sum(sha256);
			masterBatchUpload.setMismatchCount(0L);
			long dataRowsCount = excelData.getDataRowsCount();
			masterBatchUpload.setRowsCount(dataRowsCount);
			Long errorCount = 0L;
			int processedRecordsCount = 0;
			for (int rowIndex = 1; rowIndex <= dataRowsCount; rowIndex++) {
				Optional<ArticleRateMasterErrorReportDTO> errorDTO = null;
				try {
					errorDTO = excelData.validate(rowIndex);
				} catch (Exception e) {
					logger.error("Unable validate row number " + rowIndex + " due to "
							+ ExceptionUtils.getRootCauseMessage(e), e);
					++errorCount;
					continue;
				}
				if (errorDTO.isPresent()) {
					++errorCount;
					errorList.add(errorDTO.get());
				} else {
					try {
						ArticleRateMasterDTO articleMasterDTO = excelData.get(rowIndex);

						Optional<ArticleMaster> articleMasterDb = articleMasterRepository
								.getArticleByCountryNameNumberNOR(articleMasterDTO.getCountry(),
										articleMasterDTO.getArticleNumber(), articleMasterDTO.getArticleName(),
										articleMasterDTO.getNatureOfRemittance());
						ArticleMasterConditions articleMasterConditions = new ArticleMasterConditions();
						Set<ArticleMasterConditions> articleMasterConditionsList = new HashSet<>();
						Set<ArticleMasterDetailedConditions> articleMasterDetailedConditionsList = new HashSet<>();
						ArticleMaster articleMaster = new ArticleMaster();
						if (articleMasterDb.isPresent()) {
							articleMaster = articleMasterDb.get();
						} else {
							articleMaster.setCreatedBy("EY Admin");
							articleMaster.setCreatedDate(Instant.now());
						}
						articleMaster.setApplicableFrom(articleMasterDTO.getApplicableFrom().toInstant());
						if (articleMasterDTO.getApplicableTo() != null) {
							articleMaster.setApplicableTo(articleMasterDTO.getApplicableTo().toInstant());
						}
						articleMaster.setArticleName(articleMasterDTO.getArticleName());
						articleMaster.setArticleNumber(articleMasterDTO.getArticleNumber());
						articleMaster.setArticleRate(articleMasterDTO.getArticleRate());
						articleMaster.setCountry(articleMasterDTO.getCountry().toUpperCase());
						articleMaster.setActive(true);
						articleMaster.setModifiedBy("EY Admin");
						articleMaster.setModifiedDate(Instant.now());
						articleMaster.setNatureOfRemittance(articleMasterDTO.getNatureOfRemittance().toLowerCase());
						articleMaster.setMakeAvailableConditionSatisfied(
								articleMasterDTO.getMakeAvailableConditionSatisfied());
						articleMaster.setArticleRate(articleMasterDTO.getArticleRate() == null ? BigDecimal.ZERO
								: articleMasterDTO.getArticleRate());
						articleMaster.setMfnClauseExists(isNull(articleMasterDTO.getMfnClauseExists()));
						articleMaster.setMliPrinciplePurpose(articleMasterDTO.getMliPrinciplePurpose());
						articleMaster.setMliSimplifiedLimitation(isNull(articleMasterDTO.getMliSimplifiedLimitation()));
						articleMaster.setMfnClauseIsAvailed(
								articleMasterDTO.getMfnClauseIsAvailed() == null ? BigDecimal.ZERO
										: articleMasterDTO.getMfnClauseIsAvailed());
						articleMaster.setMfnClauseIsNotAvailed(
								articleMasterDTO.getMfnClauseIsNotAvailed() == null ? BigDecimal.ZERO
										: articleMasterDTO.getMfnClauseIsNotAvailed());
						articleMaster.setExempt(isNull(articleMasterDTO.getNonExempt()));
						articleMaster.setMfnApplicableToScopeOrRate(articleMasterDTO.getMfnApplicableToScopeOrRate());
						if (StringUtils.isNotBlank(articleMasterDTO.getIsInclusionOrExclusion())) {
							articleMaster.setIsInclusionOrExclusion(
									"INCLUSION".equalsIgnoreCase(articleMasterDTO.getIsInclusionOrExclusion()));
							if (StringUtils.isNotBlank(articleMasterDTO.getCondition())) {
								articleMasterConditions.setCondition(articleMasterDTO.getCondition());
							} else {
								articleMasterConditions.setCondition("INCLUSION");
							}
							articleMasterConditions.setIsInclusionOrExclusion(
									"INCLUSION".equalsIgnoreCase(articleMasterDTO.getIsInclusionOrExclusion()));
						} else {
							articleMaster.setIsInclusionOrExclusion(true);
							articleMasterConditions.setIsInclusionOrExclusion(true);
							articleMasterConditions.setCondition("INCLUSION");
						}
						articleMasterConditions
								.setIsDetailedConditionApplicable(isNull(articleMasterDTO.getConditionApplicable()));
						articleMasterDTO.setConditionApplicable(isNull(articleMasterDTO.getConditionApplicable()));
						if (articleMasterDTO.getConditionApplicable()) {
							if (articleMaster.getArticleMasterConditions() != null) {
								for (ArticleMasterConditions dbArticleMasterConditions : articleMaster
										.getArticleMasterConditions()) {
									if (dbArticleMasterConditions.getId() != null) {
										Set<ArticleMasterDetailedConditions> dbArticleMasterDetailedConditionsList = dbArticleMasterConditions
												.getArticleMasterDetailedConditions();
										for (ArticleMasterDetailedConditions dbArticleMasterDetailedCondition : dbArticleMasterDetailedConditionsList) {
											dbArticleMasterDetailedCondition
													.setArticleMasterCondition(articleMasterConditions);
											articleMasterDetailedConditionsList.add(dbArticleMasterDetailedCondition);
										}
										articleMasterConditions.setId(dbArticleMasterConditions.getId());
									}
								}
							}
							ArticleMasterDetailedConditions articleMasterDetailedConditions = new ArticleMasterDetailedConditions();
							articleMasterDetailedConditions.setCondition(articleMasterDTO.getDetailedCondition());
							articleMasterDetailedConditions.setArticleMasterCondition(articleMasterConditions);
							articleMasterDetailedConditionsList.add(articleMasterDetailedConditions);
						}
						articleMasterConditions.setArticleMasterDetailedConditions(articleMasterDetailedConditionsList);
						articleMasterConditions.setArticleMaster(articleMaster);
						articleMasterConditionsList.add(articleMasterConditions);
						articleMaster.setArticleMasterConditions(articleMasterConditionsList);
						try {
							articleMasterRepository.save(articleMaster);
							++processedRecordsCount;
						} catch (Exception e) {
							logger.error("Exception occurred while inserting data:", e);
						}

					} catch (Exception e) {
						logger.error("Unable to process row number " + rowIndex + " due to " + e.getMessage(), e);
						ArticleRateMasterErrorReportDTO problematicDataError = excelData.getErrorDTO(rowIndex);
						if (StringUtils.isBlank(problematicDataError.getReason())) {
							problematicDataError.setReason(
									"Unable to process row number " + rowIndex + " due to : " + e.getMessage());
						}
						errorList.add(problematicDataError);
						++errorCount;
					}
				}
			} 
			
			masterBatchUpload.setSuccessCount(Long.valueOf(processedRecordsCount));
			masterBatchUpload.setFailedCount(errorCount);
			masterBatchUpload.setProcessed(processedRecordsCount);
			masterBatchUpload.setDuplicateCount(0L);
			masterBatchUpload.setStatus("Processed");
			masterBatchUpload.setCreatedDate(Instant.now());
			masterBatchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			masterBatchUpload.setCreatedBy(userName);
			
			if (!errorList.isEmpty()) {
				articleErrorFile = prepareArticleMasterErrorFile(file.getOriginalFilename(), errorList,
						new ArrayList<>(excelData.getHeaders()));
			}
			 
		} catch (Exception e) {
			masterBatchUpload.setStatus("Process failed");
			logger.error("Exception occurred :", e);

		}
		return masterBatchUploadService.masterBatchUpload(masterBatchUpload, file, assesssmentYear, assessmentMonth,
				userName, articleErrorFile, uploadType);
	}

	public boolean isNull(Boolean isValue) {
		return isValue != null ? isValue : false;
	}
	
	/**
	 * 
	 * @param sha256Sum
	 * @return
	 */
	private boolean isAlreadyProcessed(String sha256Sum) {
		Optional<MasterBatchUpload> sha256Record = masterBatchUploadRepository.getSha256Records(sha256Sum);
		return sha256Record.isPresent();
	}

	/**
	 * 
	 * @param originalFileName
	 * @param collectorTan
	 * @param collectorPan
	 * @param errorList
	 * @param headers
	 * @return
	 * @throws Exception
	 */
	public File prepareArticleMasterErrorFile(String originalFileName, ArrayList<ArticleRateMasterErrorReportDTO> errorList,
			ArrayList<String> headers) throws Exception {
		try {
			headers.addAll(0, MasterExcel.STANDARD_ADDITIONAL_HEADERS);
			Workbook wkBook = articleMasterXlsxReport(errorList, headers);
			ByteArrayOutputStream baout = new ByteArrayOutputStream();
			wkBook.save(baout, SaveFormat.XLSX);
			File errorFile = new File(
					FilenameUtils.getBaseName(originalFileName) + "_" + UUID.randomUUID() + "_Error_Report.xlsx");
			FileUtils.writeByteArrayToFile(errorFile, baout.toByteArray());
			baout.close();
			return errorFile;
		} catch (Exception e) {
			logger.error("Encountered an error while preparing error file", e);
			throw e;
		}
	}
	/**
	 * 
	 * @param errorDTOs
	 * @param headerNames
	 * @return
	 * @throws Exception
	 */
	public Workbook articleMasterXlsxReport(List<ArticleRateMasterErrorReportDTO> errorDTOs, ArrayList<String> headerNames)
			throws Exception {

		Workbook workbook = new Workbook();
		Worksheet worksheet = workbook.getWorksheets().get(0);

		worksheet.getCells().importArrayList(headerNames, 5, 0, false);

		ImportTableOptions tableOptions = new ImportTableOptions();
		tableOptions.setConvertGridStyle(true);

		setExtractDataForArticleMaster(errorDTOs, worksheet, headerNames);

		// Style for A6 to B6 headers
		Style style1 = workbook.createStyle();
		style1.setForegroundColor(Color.fromArgb(169, 209, 142));
		style1.setPattern(BackgroundType.SOLID);
		style1.getFont().setBold(true);
		style1.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange1 = worksheet.getCells().createRange("A6:B6");
		headerColorRange1.setStyle(style1);

		Cell cellD6 = worksheet.getCells().get("C6");
		Style styleD6 = cellD6.getStyle();
		styleD6.setForegroundColor(Color.fromArgb(0, 0, 0));
		styleD6.setPattern(BackgroundType.SOLID);
		styleD6.getFont().setBold(true);
		styleD6.getFont().setColor(Color.getWhite());
		styleD6.setHorizontalAlignment(TextAlignmentType.CENTER);
		cellD6.setStyle(styleD6);

		// Style for D6 to AF6 headers
		Style style2 = workbook.createStyle();
		style2.setForegroundColor(Color.fromArgb(252, 199, 155));
		style2.setPattern(BackgroundType.SOLID);
		style2.getFont().setBold(true);
		style2.setHorizontalAlignment(TextAlignmentType.CENTER);
		Range headerColorRange2 = worksheet.getCells().createRange("C6:AO6");
		headerColorRange2.setStyle(style2);

		worksheet.autoFitColumns();
		worksheet.autoFitRows();
		worksheet.setGridlinesVisible(false);
		worksheet.freezePanes(0, 2, 0, 2);

		String pattern = "dd-MM-yyyy hh:mm aaa";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

		String date = simpleDateFormat.format(new Date());

		Cell cellA1 = worksheet.getCells().get("A1");
		cellA1.setValue("TDS Payables Error Report (Dated: " + date + ")");
		Style a1Style = cellA1.getStyle();
		a1Style.getFont().setName("Cambria");
		a1Style.getFont().setSize(14);
		a1Style.getFont().setBold(true);
		cellA1.setStyle(a1Style);

		Cell cellA2 = worksheet.getCells().get("A2");
		cellA2.setValue("Client Name:" + "Super Admin");
		Style a2Style = cellA2.getStyle();
		a2Style.getFont().setName("Cambria");
		a2Style.getFont().setSize(14);
		a2Style.getFont().setBold(true);
		cellA2.setStyle(a2Style);

		// column B5 column
		Cell cellB5 = worksheet.getCells().get("A5");
		cellB5.setValue("Error/Information");
		Style b5Style = cellB5.getStyle();
		b5Style.setForegroundColor(Color.fromArgb(180, 199, 231));
		b5Style.setPattern(BackgroundType.SOLID);
		b5Style.getFont().setBold(true);
		cellB5.setStyle(b5Style);

		int maxdatacol = worksheet.getCells().getMaxDataColumn();
		int maxdatarow = worksheet.getCells().getMaxDataRow();

		String firstHeaderCellName = "A6";
		String lastHeaderCellName = "AO6";
		String firstDataCellName = "A7";
		String lastDataCellName = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);

		CommonUtil.setBoardersForAsposeXlsx(worksheet, firstHeaderCellName, lastHeaderCellName, firstDataCellName,
				lastDataCellName);
		// Creating AutoFilter by giving the cells range
		AutoFilter autoFilter = worksheet.getAutoFilter();
		autoFilter.setRange("A6:AO6");
		worksheet.autoFitColumn(1);
		worksheet.autoFitRows();
		return workbook;
	}
	/**
	 * 
	 * @param errorDTOs
	 * @param worksheet
	 * @param headerNames
	 * @throws Exception
	 */
	public void setExtractDataForArticleMaster(List<ArticleRateMasterErrorReportDTO> errorDTOs, Worksheet worksheet,
			List<String> headerNames) throws Exception {
		if (!errorDTOs.isEmpty()) {
			int dataRowsStartIndex = 6;
			for (int i = 0; i < errorDTOs.size(); i++) {
				ArticleRateMasterErrorReportDTO errorDTO = errorDTOs.get(i);
				ArrayList<String> rowData = MasterExcel.getValues(errorDTO, ArticleMasterExcel.fieldMappings,
						headerNames);
				rowData.set(0, StringUtils.isBlank(errorDTO.getReason()) ? StringUtils.EMPTY : errorDTO.getReason());
				rowData.set(1, StringUtils.isBlank(errorDTO.getSerialNumber()) ? StringUtils.EMPTY
						: errorDTO.getSerialNumber());
				worksheet.getCells().importArrayList(rowData, i + dataRowsStartIndex, 0, false);
				worksheet.autoFitRow(i + dataRowsStartIndex);
				worksheet.autoFitColumn(i);
				MasterExcel.colorCodeErrorCells(worksheet, i + dataRowsStartIndex, headerNames, errorDTO.getReason());
			}
		}
	}

	public List<Map<String, Object>> getArticleMasterData() {
		return articleMasterRepository.getArticleMasterData();
	}

}