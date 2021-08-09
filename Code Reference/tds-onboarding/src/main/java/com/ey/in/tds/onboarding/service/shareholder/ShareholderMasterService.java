package com.ey.in.tds.onboarding.service.shareholder;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.aspose.cells.AutoFilter;
import com.aspose.cells.BackgroundType;
import com.aspose.cells.BorderType;
import com.aspose.cells.Cell;
import com.aspose.cells.CellBorderType;
import com.aspose.cells.CellsHelper;
import com.aspose.cells.Color;
import com.aspose.cells.ImportTableOptions;
import com.aspose.cells.Range;
import com.aspose.cells.Style;
import com.aspose.cells.TextAlignmentType;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.domain.transactions.jdbc.dao.BatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.dto.CommonDTO;
import com.ey.in.tds.common.dto.Pair;
import com.ey.in.tds.common.dto.onboarding.deductee.AddressDTO;
import com.ey.in.tds.common.dto.onboarding.shareholder.ShareholderMasterDTO;
import com.ey.in.tds.common.dto.onboarding.shareholder.ShareholderMasterNonResidentDTO;
import com.ey.in.tds.common.dto.onboarding.shareholder.ShareholderMasterNonResidential;
import com.ey.in.tds.common.dto.onboarding.shareholder.ShareholderMasterResidentDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.ShareholderMasterResidential;
import com.ey.in.tds.common.onboarding.jdbc.dto.ShareholderNonResidentialHistory;
import com.ey.in.tds.common.onboarding.jdbc.dto.ShareholderResidentialHistory;
import com.ey.in.tds.common.repository.common.PagedData;
import com.ey.in.tds.common.repository.common.Pagination;
import com.ey.in.tds.common.service.ActivityTrackerService;
import com.ey.in.tds.common.service.Sha256SumService;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.common.util.excel.Excel;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.util.ActivityTrackerStatus;
import com.ey.in.tds.core.util.ActivityType;
import com.ey.in.tds.core.util.CommonUtil;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.jdbc.dao.ShareholderMasterNonResidentialDAO;
import com.ey.in.tds.jdbc.dao.ShareholderMasterResidentialDAO;
import com.ey.in.tds.jdbc.dao.ShareholderNonResidentialHistoryDAO;
import com.ey.in.tds.jdbc.dao.ShareholderResidentialHistoryDAO;
import com.ey.in.tds.onboarding.service.util.excel.shareholder.NonResidentShareholderExcel;
import com.ey.in.tds.onboarding.service.util.excel.shareholder.ResidentShareholderExcel;
import com.ey.in.tds.onboarding.web.rest.util.ValidationUtil;
import com.microsoft.azure.storage.StorageException;

/**
 * 
 * @author dipak
 *
 */
@Service
public class ShareholderMasterService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ShareholderMasterResidentialDAO shareholderMasterResidentialDAO;

	@Autowired
	private BlobStorage blob;

	@Autowired
	private ShareholderMasterNonResidentialDAO shareholderMasterNonResidentialDAO;

	@Autowired
	private ShareholderResidentialHistoryDAO shareholderResidentialHistoryDAO;

	@Autowired
	private ShareholderNonResidentialHistoryDAO shareholderNonResidentialHistoryDAO;

	@Autowired
	private BatchUploadDAO batchUploadDAO;

	@Autowired
	private Sha256SumService sha256SumService;

	@Autowired
	private ShareholderBulkService shareholderBulkService;

	@Autowired
	private ActivityTrackerService activityTrackerService;

	/**
	 * to create residen
	 * 
	 * @param shareholderMasterResidentDTO
	 * @param deductorPan
	 * @param username
	 * @param form15GHFile
	 * @return
	 * @throws IOException
	 * @throws StorageException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 */
	public ShareholderMasterResidential createResidentShareHolder(
			ShareholderMasterResidentDTO shareholderMasterResidentDTO, String deductorPan, String username,
			MultipartFile form15GHFile) throws IOException, StorageException, InvalidKeyException, URISyntaxException {
		logger.info("REST request to save a shareholder residential Record : {}", shareholderMasterResidentDTO);

		String tenantId = MultiTenantContext.getTenantId();
		Boolean isFolioNumberDuplicate = null;
		Integer assesmentYear=Calendar.getInstance().get(Calendar.YEAR)+1;

		isFolioNumberDuplicate = isFolioNumberDuplicateResident(shareholderMasterResidentDTO, deductorPan, tenantId);
		if (isFolioNumberDuplicate.equals(true)) {
			throw new CustomException("Folio number already exists", HttpStatus.BAD_REQUEST);
		}

		validateShareholderTypeAndPan(shareholderMasterResidentDTO);

		ShareholderMasterResidential shareholderMasterResidential = new ShareholderMasterResidential();

		shareholderMasterResidential.setDeductorPan(deductorPan);
		shareholderMasterResidential.setUniqueIdentificationNumber(shareholderMasterResidentDTO.getUniqueIdentificationNumber());
		shareholderMasterResidential.setAadharNumber(shareholderMasterResidentDTO.getAadharNumber());
		shareholderMasterResidential.setFolioNo(shareholderMasterResidentDTO.getFolioNo());
		shareholderMasterResidential.setShareholderType(shareholderMasterResidentDTO.getShareholderType());
		shareholderMasterResidential.setShareholderName(shareholderMasterResidentDTO.getShareholderName());
		shareholderMasterResidential.setShareholderCategory(shareholderMasterResidentDTO.getShareholderCategory());
		shareholderMasterResidential.setKeyShareholder(shareholderMasterResidentDTO.getKeyShareholder());
		shareholderMasterResidential.setShareholderPan(shareholderMasterResidentDTO.getShareholderPan());
		shareholderMasterResidential
				.setShareholderResidentialStatus(shareholderMasterResidentDTO.getShareholderResidentialStatus());

		shareholderMasterResidential.setFlatDoorBlockNo(shareholderMasterResidentDTO.getAddress().getFlatDoorBlockNo());
		shareholderMasterResidential
				.setNameBuildingVillage(shareholderMasterResidentDTO.getAddress().getNameBuildingVillage());
		shareholderMasterResidential
				.setRoadStreetPostoffice(shareholderMasterResidentDTO.getAddress().getRoadStreetPostoffice());
		shareholderMasterResidential.setAreaLocality(shareholderMasterResidentDTO.getAddress().getAreaLocality());
		shareholderMasterResidential
				.setTownCityDistrict(shareholderMasterResidentDTO.getAddress().getTownCityDistrict());
		shareholderMasterResidential.setState(shareholderMasterResidentDTO.getAddress().getStateName());
		shareholderMasterResidential.setCountry(shareholderMasterResidentDTO.getAddress().getCountry());
		shareholderMasterResidential.setPinCode(shareholderMasterResidentDTO.getAddress().getPinCode());
		shareholderMasterResidential.setEmailId(shareholderMasterResidentDTO.getEmailId());
		shareholderMasterResidential.setContact(shareholderMasterResidentDTO.getContact());
		shareholderMasterResidential
				.setShareTransferAgentName(shareholderMasterResidentDTO.getShareTransferAgentName());
		shareholderMasterResidential.setDematAccountNo(shareholderMasterResidentDTO.getDematAccountNo());
		shareholderMasterResidential.setTotalSharesHeld(shareholderMasterResidentDTO.getTotalSharesHeld());
		shareholderMasterResidential.setPercentageSharesHeld(shareholderMasterResidentDTO.getPercentageSharesHeld());
		shareholderMasterResidential.setForm15ghAvailable(shareholderMasterResidentDTO.getForm15ghAvailable());
		if (shareholderMasterResidentDTO.getForm15ghAvailable().equals(true) && (form15GHFile != null)) {
			String form15ghUrl = blob.uploadExcelToBlob(form15GHFile);
			shareholderMasterResidential.setForm15ghFileAddress(form15ghUrl);
		}
		shareholderMasterResidential
				.setForm15ghUniqueIdentificationNo(shareholderMasterResidentDTO.getForm15ghUniqueIdentificationNo());
		shareholderMasterResidential.setCreatedBy(username);
		shareholderMasterResidential.setCreatedDate(new Date());
		shareholderMasterResidential.setModifiedBy(username);
		shareholderMasterResidential.setModifiedDate(new Date());
		shareholderMasterResidential.setVersion(1);
		shareholderMasterResidential.setActive(true);
		shareholderMasterResidential.setStringAssesmentYearDividendDetails("{"+assesmentYear+":0.0}");
		shareholderMasterResidentialDAO.save(shareholderMasterResidential);
		shareholderResidentialHistoryDAO.save(ShareholderResidentialHistory.from(shareholderMasterResidential));
		return shareholderMasterResidential;
	}

	public void updateActivityStatusResident(String deductorTan, String deductorPan, String userName) {
		Integer assessmentYear = CommonUtil.getAssessmentYear(null);
		Integer assessmentMonthPlusOne = CommonUtil.getAssessmentMonthPlusOne(null);
		String activityStatus = getShareholderResidentPanStatus(deductorPan, assessmentYear, assessmentMonthPlusOne);
		activityTrackerService.updateActivity(deductorTan, assessmentYear, assessmentMonthPlusOne, userName,
				ActivityType.PAN_VERIFICATION.getActivityType(), activityStatus);
	}

	public String getShareholderResidentPanStatus(String deductorPan, Integer year, Integer month) {
		String startDate = CommonUtil.getQueryDate(CommonUtil.getMonthStartDate(year, month - 1));
		String endDate = CommonUtil.getQueryDate(CommonUtil.getMonthEndDate(year, month - 1));
		return getShareholderResidentPanStatus(deductorPan, startDate, endDate);
	}

	public String getShareholderResidentPanStatus(String deductorPan, String startDate, String endDate) {
		long countValidPan = 0;
		// shareholderMasterResidentialDAO
		// .countShareholderResidentialPanStatusValid(deductorPan, startDate, endDate);
		logger.info("Total shareholder residential valid pan status: {}", countValidPan);

		long countInValidPan = 0;
		// shareholderMasterResidentialDAO
		// .countShareholderResidentialPanStatusInValid(deductorPan, startDate,
		// endDate);
		logger.info("Total shareholder residential invalid pan status: {}", countInValidPan);

		long countEmptyPan = 0;
		// shareholderMasterResidentialDAO
		// .countShareholderResidentialPanStatusEmpty(deductorPan, startDate, endDate);
		logger.info("Total shareholder residential Empty pan status: {}", countEmptyPan);

		if (countValidPan == 0 && countInValidPan == 0 && countEmptyPan == 0) {
			return ActivityTrackerStatus.NORECORDS.name();
		} else if (countInValidPan > 0 || countEmptyPan > 0) {
			return ActivityTrackerStatus.PENDING.name();
		} else if (countValidPan > 0 && countInValidPan == 0 && countEmptyPan == 0) {
			return ActivityTrackerStatus.VALIDATED.name();
		} else {
			return StringUtils.EMPTY;
		}
	}

	/**
	 * checking for the uniqueness of the folio number based on the residential pan
	 * of the share holder
	 * 
	 * @param folioNo
	 * @param deductorPan
	 * @param tenantId
	 * @param residentialStatus
	 * @return
	 */
	private Boolean isFolioNumberDuplicateResident(ShareholderMasterResidentDTO dto, String deductorPan,
			String tenantId) {
		logger.info("Checking for duplicate folio number {}");
		List<ShareholderMasterResidential> list = shareholderMasterResidentialDAO
				.getResidentShareholderByFolioNumberPanTenantId(deductorPan, tenantId, dto.getFolioNo().trim());
		if (!list.isEmpty()) {
			String dbPan = list.get(0).getShareholderPan();
			String dtoPan = dto.getShareholderPan();
			boolean duplicate=false;
			if (StringUtils.isNotBlank(dtoPan) && StringUtils.isNotBlank(dbPan) && dtoPan.equals(dbPan)) {
				duplicate= true;
			} 
			if(StringUtils.isBlank(dtoPan) && StringUtils.isBlank(dbPan)){
				duplicate= true;
			}
			return duplicate;
		} else {
			return false;
		}
	}

	/**
	 * checking for the uniqueness of the folio number based on the pan of the share
	 * holder
	 * 
	 * @param folioNo
	 * @param deductorPan
	 * @param tenantId
	 * @param residentialStatus
	 * @return
	 */
	private Boolean isFolioNumberDuplicateNonResident(ShareholderMasterNonResidentDTO shareholderMasterNonResidentDTO,
			String deductorPan, String tenantId) {

		List<ShareholderMasterNonResidential> list = shareholderMasterNonResidentialDAO
				.getNonResidentShareholderByFolioNumberPanTenantId(deductorPan, tenantId,
						shareholderMasterNonResidentDTO.getFolioNo());
		if (!list.isEmpty()) {
			String dbPan = list.get(0).getShareholderPan();
			String dtoPan = shareholderMasterNonResidentDTO.getShareholderPan();
			boolean duplicate=false;
			if (StringUtils.isNotBlank(dtoPan) && StringUtils.isNotBlank(dbPan) && dtoPan.equals(dbPan)) {
				duplicate= true;
			} 
			if(StringUtils.isBlank(dtoPan) && StringUtils.isBlank(dbPan)){
				duplicate= true;
			}
			return duplicate;
		} else {
			return false;
		}
	}

	/**
	 * validating share holder type and pan
	 * 
	 * @param shareholderMasterResidentDTO
	 */
	private void validateShareholderTypeAndPan(ShareholderMasterResidentDTO shareholderMasterResidentDTO) {
		if (StringUtils.isBlank(shareholderMasterResidentDTO.getShareholderType())
				&& StringUtils.isBlank(shareholderMasterResidentDTO.getShareholderPan())) {
			throw new CustomException("Either Shareholder type or PAN value should exist", HttpStatus.BAD_REQUEST);
		} else if (StringUtils.isNotBlank(shareholderMasterResidentDTO.getShareholderPan())
				&& StringUtils.isBlank(shareholderMasterResidentDTO.getShareholderType())) {
			String shareholderType = ValidationUtil
					.getShareholderTypeByPan(shareholderMasterResidentDTO.getShareholderPan());
			if (shareholderType == null) {
				throw new CustomException("Invalid Shareholder Type for given PAN", HttpStatus.BAD_REQUEST);
			}
			shareholderMasterResidentDTO.setShareholderType(shareholderType);
		} else if (StringUtils.isBlank(shareholderMasterResidentDTO.getShareholderPan())
				&& StringUtils.isNotBlank(shareholderMasterResidentDTO.getShareholderType())) {
			boolean flag = ValidationUtil.validateShareholderType(shareholderMasterResidentDTO.getShareholderType());
			if (!flag) {
				throw new CustomException("Invalid Shareholder Type", HttpStatus.BAD_REQUEST);
			}
		} else {
			boolean flag = ValidationUtil.validateShareholderTypeAndPan(
					shareholderMasterResidentDTO.getShareholderType(),
					shareholderMasterResidentDTO.getShareholderPan());
			if (!flag) {
				throw new CustomException("Invalid Shareholder Type for given PAN", HttpStatus.BAD_REQUEST);
			}
		}
	}

	public ShareholderMasterDTO getResidentialShareholder(String deductorPan, Integer id) {
		ShareholderMasterDTO shareholderMasterDTO = new ShareholderMasterDTO();
		AddressDTO address = new AddressDTO();
		String tenantId = MultiTenantContext.getTenantId();
		ShareholderMasterResidential shareholderMasterResidential = null;

		shareholderMasterResidential = shareholderMasterResidentialDAO.findById(id, deductorPan);
		logger.info("Retrieved share holder record " + shareholderMasterResidential);
		if (shareholderMasterResidential != null) {
			shareholderMasterDTO.setId(shareholderMasterResidential.getId());
			shareholderMasterDTO.setUniqueIdentificationNumber(shareholderMasterResidential.getUniqueIdentificationNumber());
			shareholderMasterDTO.setFolioNo(shareholderMasterResidential.getFolioNo());
			shareholderMasterDTO.setShareholderName(shareholderMasterResidential.getShareholderName());
			shareholderMasterDTO.setShareholderCategory(shareholderMasterResidential.getShareholderCategory());
			shareholderMasterDTO.setShareholderType(shareholderMasterResidential.getShareholderType());
			shareholderMasterDTO.setKeyShareholder(shareholderMasterResidential.getKeyShareholder());
			shareholderMasterDTO.setShareholderPan(shareholderMasterResidential.getShareholderPan());
			shareholderMasterDTO.setAadharNumber(shareholderMasterResidential.getAadharNumber());
			shareholderMasterDTO.setPanStatus(shareholderMasterResidential.getPanStatus());
			shareholderMasterDTO.setPanVerifiedDate(shareholderMasterResidential.getPanVerifiedDate());
			shareholderMasterDTO
					.setShareholderResidentialStatus(shareholderMasterResidential.getShareholderResidentialStatus());
			address.setFlatDoorBlockNo(shareholderMasterResidential.getFlatDoorBlockNo());
			address.setNameBuildingVillage(shareholderMasterResidential.getNameBuildingVillage());
			address.setRoadStreetPostoffice(shareholderMasterResidential.getRoadStreetPostoffice());
			address.setAreaLocality(shareholderMasterResidential.getAreaLocality());
			address.setTownCityDistrict(shareholderMasterResidential.getTownCityDistrict());
			if (StringUtils.isNotBlank(shareholderMasterResidential.getCountry())) {
				address.setCountry(shareholderMasterResidential.getCountry().toUpperCase());
			} else {
				address.setCountry(shareholderMasterResidential.getCountry());
			}
			if (StringUtils.isNotBlank(shareholderMasterResidential.getState())) {
				address.setStateName(shareholderMasterResidential.getState().toUpperCase());
			} else {
				address.setStateName(shareholderMasterResidential.getState());
			}

			address.setPinCode(shareholderMasterResidential.getPinCode());
			shareholderMasterDTO.setAddress(address);

			shareholderMasterDTO.setEmailId(shareholderMasterResidential.getEmailId());
			shareholderMasterDTO.setContact(shareholderMasterResidential.getContact());
			shareholderMasterDTO.setShareTransferAgentName(shareholderMasterResidential.getShareTransferAgentName());
			shareholderMasterDTO.setDematAccountNo(shareholderMasterResidential.getDematAccountNo());
			shareholderMasterDTO.setTotalSharesHeld(shareholderMasterResidential.getTotalSharesHeld());
			shareholderMasterDTO.setPercentageSharesHeld(shareholderMasterResidential.getPercentageSharesHeld());
			shareholderMasterDTO.setForm15ghAvailable(shareholderMasterResidential.getForm15ghAvailable());
			String form15ghUrl = shareholderMasterResidential.getForm15ghFileAddress();
			if (StringUtils.isNotBlank(form15ghUrl)) {
				String form15ghFileName = form15ghUrl.substring(form15ghUrl.lastIndexOf('/') + 1);
				shareholderMasterDTO.setForm15ghFileName(form15ghFileName);
				shareholderMasterDTO.setForm15ghFileAddress(shareholderMasterResidential.getForm15ghFileAddress());
			}
			shareholderMasterDTO.setForm15ghUniqueIdentificationNo(
					shareholderMasterResidential.getForm15ghUniqueIdentificationNo());
			shareholderMasterDTO.setPanStatus(shareholderMasterResidential.getPanStatus());
			shareholderMasterDTO.setPanVerifiedDate(shareholderMasterResidential.getPanVerifiedDate());
			shareholderMasterDTO.setCreatedDate(shareholderMasterResidential.getCreatedDate());
			shareholderMasterDTO
					.setAssessmentYearDividendDetails(shareholderMasterResidential.getAssessmentYearDividendDetails());
		}
		return shareholderMasterDTO;
	}

	/**
	 * to get te list of resident share holder
	 * 
	 * @param deductorPan
	 * @param pagination
	 * @param shareholderName
	 * @return
	 */
	public CommonDTO<ShareholderMasterDTO> getListOfResidentialShareholder(String deductorPan, Pagination pagination,
			String shareholderName) {
		List<ShareholderMasterDTO> shareholderMasterDTOList = new ArrayList<>();
		CommonDTO<ShareholderMasterDTO> shareholderMasterData = new CommonDTO<>();
		ShareholderMasterDTO shareholderMasterDTO = null;

		logger.info("Shareholder name ---- : {}", shareholderName);
		List<ShareholderMasterResidential> residentList = null;

		if ("noshareholderfilter".equalsIgnoreCase(shareholderName)) {
			residentList = shareholderMasterResidentialDAO.findAllByPanTenantIdAndName(deductorPan, shareholderName,
					pagination);
		} else {
			residentList = shareholderMasterResidentialDAO.findAllByPanTenantIdAndName(deductorPan, shareholderName,
					pagination);
		}

		logger.info("LIST OF RESIDENTIAL Shareholder : {}", residentList);
		BigInteger count = shareholderMasterResidentialDAO.getCountByPanTenantId(deductorPan, shareholderName);
		logger.info("Retrieved Count is " + count + "{}");
		

		for (ShareholderMasterResidential shareholderMasterResidential : residentList) {

			shareholderMasterDTO = new ShareholderMasterDTO();
			AddressDTO address = new AddressDTO();

			BeanUtils.copyProperties(shareholderMasterResidential, shareholderMasterDTO);

			shareholderMasterDTO
					.setShareholderResidentialStatus(shareholderMasterResidential.getShareholderResidentialStatus());
			shareholderMasterDTO.setId(shareholderMasterResidential.getId());
			shareholderMasterDTO.setKeyShareholder(shareholderMasterResidential.getKeyShareholder());
			shareholderMasterDTO.setPanStatus(shareholderMasterResidential.getPanStatus());
			shareholderMasterDTO.setAadharNumber(shareholderMasterResidential.getAadharNumber());
			shareholderMasterDTO.setUniqueIdentificationNumber(shareholderMasterResidential.getUniqueIdentificationNumber());

			address.setAreaLocality(shareholderMasterResidential.getAreaLocality());
			address.setCountry(shareholderMasterResidential.getCountry());
			address.setFlatDoorBlockNo(shareholderMasterResidential.getFlatDoorBlockNo());
			address.setNameBuildingVillage(shareholderMasterResidential.getNameBuildingVillage());
			address.setPinCode(shareholderMasterResidential.getPinCode());
			address.setRoadStreetPostoffice(shareholderMasterResidential.getRoadStreetPostoffice());
			address.setStateName(shareholderMasterResidential.getState());
			address.setTownCityDistrict(shareholderMasterResidential.getTownCityDistrict());

			shareholderMasterDTO.setAddress(address);
			shareholderMasterDTO.setShareholderPan(shareholderMasterResidential.getShareholderPan());
			shareholderMasterDTO.setPanStatus(shareholderMasterResidential.getPanStatus());
			shareholderMasterDTO.setPanVerifiedDate(shareholderMasterResidential.getPanVerifiedDate());
			shareholderMasterDTO
					.setAssessmentYearDividendDetails(shareholderMasterResidential.getAssessmentYearDividendDetails());

			String form15ghUrl = shareholderMasterResidential.getForm15ghFileAddress();
			if (StringUtils.isNotBlank(form15ghUrl)) {
				String form15ghFileName = form15ghUrl.substring(form15ghUrl.lastIndexOf('/') + 1);
				shareholderMasterDTO.setForm15ghFileName(form15ghFileName);
				shareholderMasterDTO.setForm15ghFileAddress(shareholderMasterResidential.getForm15ghFileAddress());
			}

			shareholderMasterDTO.setForm15ghAvailable(shareholderMasterResidential.getForm15ghAvailable());
			shareholderMasterDTO.setForm15ghUniqueIdentificationNo(
					shareholderMasterResidential.getForm15ghUniqueIdentificationNo());
			shareholderMasterDTOList.add(shareholderMasterDTO);

		}
		PagedData<ShareholderMasterDTO> pagedData = new PagedData<>(shareholderMasterDTOList,
				shareholderMasterDTOList.size(), pagination.getPageNumber(),
				count.intValue() > (pagination.getPageSize() * pagination.getPageNumber()) ? false : true);

		shareholderMasterData.setCount(count);
		shareholderMasterData.setResultsSet(pagedData);

		return shareholderMasterData;
	}

	public ShareholderMasterResidential updateResidentShareholder(
			ShareholderMasterResidentDTO shareholderMasterResidentDTO, String deductorPan, String username,
			MultipartFile form15GHFile) throws IOException, StorageException, InvalidKeyException, URISyntaxException {

		logger.info("REST request to update a Shareholder Resident record : {}", shareholderMasterResidentDTO);

		Integer id = shareholderMasterResidentDTO.getId();

		ShareholderMasterResidential shareholderMasterResidential = null;
		shareholderMasterResidential = shareholderMasterResidentialDAO.findById(id, deductorPan);

		if (shareholderMasterResidential != null) {

			shareholderMasterResidential.setKeyShareholder(shareholderMasterResidentDTO.getKeyShareholder());
			shareholderMasterResidential.setEmailId(shareholderMasterResidentDTO.getEmailId());
			shareholderMasterResidential.setContact(shareholderMasterResidentDTO.getContact());
			shareholderMasterResidential
					.setShareTransferAgentName(shareholderMasterResidentDTO.getShareTransferAgentName());
			shareholderMasterResidential.setDematAccountNo(shareholderMasterResidentDTO.getDematAccountNo());
			shareholderMasterResidential.setTotalSharesHeld(shareholderMasterResidentDTO.getTotalSharesHeld());
			shareholderMasterResidential
					.setPercentageSharesHeld(shareholderMasterResidentDTO.getPercentageSharesHeld());
			shareholderMasterResidential
					.setFlatDoorBlockNo(shareholderMasterResidentDTO.getAddress().getFlatDoorBlockNo());
			shareholderMasterResidential
					.setNameBuildingVillage(shareholderMasterResidentDTO.getAddress().getNameBuildingVillage());
			shareholderMasterResidential
					.setRoadStreetPostoffice(shareholderMasterResidentDTO.getAddress().getRoadStreetPostoffice());
			shareholderMasterResidential.setAreaLocality(shareholderMasterResidentDTO.getAddress().getAreaLocality());
			shareholderMasterResidential
					.setTownCityDistrict(shareholderMasterResidentDTO.getAddress().getTownCityDistrict());
			shareholderMasterResidential.setState(shareholderMasterResidentDTO.getAddress().getStateName());
			shareholderMasterResidential.setCountry(shareholderMasterResidentDTO.getAddress().getCountry());
			shareholderMasterResidential.setPinCode(shareholderMasterResidentDTO.getAddress().getPinCode());
			shareholderMasterResidential.setModifiedBy(username);
			shareholderMasterResidential.setModifiedDate(new Date());
			shareholderMasterResidential.setShareholderPan(shareholderMasterResidentDTO.getShareholderPan());
			shareholderMasterResidential.setForm15ghAvailable(shareholderMasterResidentDTO.getForm15ghAvailable());

			if (shareholderMasterResidentDTO.getForm15ghAvailable().equals(true) && (form15GHFile != null)) {
				String form15ghUrl = blob.uploadExcelToBlob(form15GHFile);
				shareholderMasterResidential.setForm15ghFileAddress(form15ghUrl);
			}
			shareholderMasterResidential.setForm15ghUniqueIdentificationNo(
					shareholderMasterResidentDTO.getForm15ghUniqueIdentificationNo());
			shareholderMasterResidential.setVersion(shareholderMasterResidential.getVersion() + 1);
			shareholderMasterResidentialDAO.update(shareholderMasterResidential);
			ShareholderResidentialHistory updatedResidentHistory = ShareholderResidentialHistory
					.from(shareholderMasterResidential);
			shareholderResidentialHistoryDAO.update(updatedResidentHistory);
		}
		return shareholderMasterResidential;
	}

	public ShareholderMasterNonResidential createNonResident(
			ShareholderMasterNonResidentDTO shareholderMasterNonResidentDTO, MultipartFile trcFile,
			MultipartFile tenFFile, MultipartFile noPEFile, MultipartFile noPoemFile, MultipartFile mliFile,
			MultipartFile beneficialOwnershipFile, String deductorPan, String userName)
			throws IOException, StorageException, InvalidKeyException, URISyntaxException {
		String tenantId = MultiTenantContext.getTenantId();
		Integer year=Calendar.getInstance().get(Calendar.YEAR)+1;

		Boolean isFolioNumberDuplicate = isFolioNumberDuplicateNonResident(shareholderMasterNonResidentDTO, deductorPan,
				tenantId);
		if (isFolioNumberDuplicate.equals(true)) {
			throw new CustomException("Folio number already exists", HttpStatus.BAD_REQUEST);
		}

		validateShareholderTypeAndPan(shareholderMasterNonResidentDTO);

		ShareholderMasterNonResidential shareholderMasterNonResidential = new ShareholderMasterNonResidential();

		shareholderMasterNonResidential.setUniqueIdentificationNumber(shareholderMasterNonResidentDTO.getUniqueIdentificationNumber());
		shareholderMasterNonResidential.setDeductorPan(deductorPan);
		shareholderMasterNonResidential.setFolioNo(shareholderMasterNonResidentDTO.getFolioNo());
		shareholderMasterNonResidential.setShareholderName(shareholderMasterNonResidentDTO.getShareholderName());
		shareholderMasterNonResidential.setShareholderType(shareholderMasterNonResidentDTO.getShareholderType());
		shareholderMasterNonResidential
				.setShareholderCategory(shareholderMasterNonResidentDTO.getShareholderCategory());
		shareholderMasterNonResidential.setShareholderPan(shareholderMasterNonResidentDTO.getShareholderPan());
		shareholderMasterNonResidential.setShareholderTin(shareholderMasterNonResidentDTO.getShareholderTin());
		shareholderMasterNonResidential
				.setShareholderResidentialStatus(shareholderMasterNonResidentDTO.getShareholderResidentialStatus());
		shareholderMasterNonResidential
				.setPrincipalPlaceOfBusiness(shareholderMasterNonResidentDTO.getPrincipalPlaceOfBusiness());
		shareholderMasterNonResidential.setKeyShareholder(shareholderMasterNonResidentDTO.getKeyShareholder());
		shareholderMasterNonResidential
				.setFlatDoorBlockNo(shareholderMasterNonResidentDTO.getAddress().getFlatDoorBlockNo());
		shareholderMasterNonResidential
				.setNameBuildingVillage(shareholderMasterNonResidentDTO.getAddress().getNameBuildingVillage());
		shareholderMasterNonResidential
				.setRoadStreetPostoffice(shareholderMasterNonResidentDTO.getAddress().getRoadStreetPostoffice());
		shareholderMasterNonResidential.setAreaLocality(shareholderMasterNonResidentDTO.getAddress().getAreaLocality());
		shareholderMasterNonResidential
				.setTownCityDistrict(shareholderMasterNonResidentDTO.getAddress().getTownCityDistrict());
		shareholderMasterNonResidential.setState(shareholderMasterNonResidentDTO.getAddress().getStateName());
		shareholderMasterNonResidential.setCountry(shareholderMasterNonResidentDTO.getAddress().getCountry());
		shareholderMasterNonResidential.setPinCode(shareholderMasterNonResidentDTO.getAddress().getPinCode());
		shareholderMasterNonResidential.setEmailId(shareholderMasterNonResidentDTO.getEmailId());
		shareholderMasterNonResidential.setContact(shareholderMasterNonResidentDTO.getContact());
		shareholderMasterNonResidential
				.setShareTransferAgentName(shareholderMasterNonResidentDTO.getShareTransferAgentName());
		shareholderMasterNonResidential.setDematAccountNo(shareholderMasterNonResidentDTO.getDematAccountNo());

		if (shareholderMasterNonResidentDTO.getTotalSharesHeld() != null) {
			shareholderMasterNonResidential.setTotalSharesHeld(shareholderMasterNonResidentDTO.getTotalSharesHeld());
		} else {
			shareholderMasterNonResidential.setTotalSharesHeld(BigDecimal.ZERO);
		}
		if (shareholderMasterNonResidentDTO.getPercentageSharesHeld() != null) {
			shareholderMasterNonResidential
					.setPercentageSharesHeld(shareholderMasterNonResidentDTO.getPercentageSharesHeld());
		} else {
			shareholderMasterNonResidential.setPercentageSharesHeld(BigDecimal.ZERO);
		}

		shareholderMasterNonResidential.setShareHeldFromDate(shareholderMasterNonResidentDTO.getShareHeldFromDate());
		shareholderMasterNonResidential.setShareHeldToDate(shareholderMasterNonResidentDTO.getShareHeldToDate());
		shareholderMasterNonResidential.setCreatedDate(new Date());
		shareholderMasterNonResidential.setCreatedBy(userName);
		shareholderMasterNonResidential.setModifiedBy(userName);
		shareholderMasterNonResidential.setModifiedDate(new Date());

		shareholderMasterNonResidential.setIsTrcAvailable(shareholderMasterNonResidentDTO.getTrcAvailable());
		if (shareholderMasterNonResidentDTO.getTrcAvailable().equals(true)) {
			if (shareholderMasterNonResidentDTO.getTrcApplicableFrom() != null) {
				if (shareholderMasterNonResidentDTO.getTrcApplicableTo() != null && (shareholderMasterNonResidentDTO
						.getTrcApplicableFrom().equals(shareholderMasterNonResidentDTO.getTrcApplicableTo())
						|| shareholderMasterNonResidentDTO.getTrcApplicableFrom()
								.after(shareholderMasterNonResidentDTO.getTrcApplicableTo()))) {
					throw new CustomException(
							"Trc Applicable From Date should not be equals or greater than Trc Applicable To Date",
							HttpStatus.BAD_REQUEST);
				} else {
					shareholderMasterNonResidential
							.setTrcApplicableFrom(shareholderMasterNonResidentDTO.getTrcApplicableFrom());
					shareholderMasterNonResidential
							.setTrcApplicableTo(shareholderMasterNonResidentDTO.getTrcApplicableTo());
				}
			} else {
				throw new CustomException("Trc Applicable From date is Mandatory", HttpStatus.BAD_REQUEST);
			}
		} else {
			shareholderMasterNonResidential
					.setTrcApplicableFrom(shareholderMasterNonResidentDTO.getTrcApplicableFrom());
			shareholderMasterNonResidential.setTrcApplicableTo(shareholderMasterNonResidentDTO.getTrcApplicableTo());
		}
		if (shareholderMasterNonResidentDTO.getTrcAvailable().equals(true) && (trcFile != null)) {
			String trcUri = blob.uploadExcelToBlob(trcFile);
			shareholderMasterNonResidential.setTrcFileAddress(trcUri);
		}

		shareholderMasterNonResidential.setIsTenfAvailable(shareholderMasterNonResidentDTO.getTenfAvailable());
		if (shareholderMasterNonResidentDTO.getTenfAvailable().equals(true)) {
			if (shareholderMasterNonResidentDTO.getTenfApplicableFrom() != null) {
				if (shareholderMasterNonResidentDTO.getTenfApplicableTo() != null && (shareholderMasterNonResidentDTO
						.getTenfApplicableFrom().equals(shareholderMasterNonResidentDTO.getTenfApplicableTo())
						|| shareholderMasterNonResidentDTO.getTenfApplicableFrom()
								.after(shareholderMasterNonResidentDTO.getTenfApplicableTo()))) {
					throw new CustomException(
							"Form 10f Applicable From Date should not be equals or greater than 10f Applicable To Date",
							HttpStatus.BAD_REQUEST);
				} else {
					shareholderMasterNonResidential
							.setTenfApplicableFrom(shareholderMasterNonResidentDTO.getTenfApplicableFrom());
					shareholderMasterNonResidential
							.setTenfApplicableTo(shareholderMasterNonResidentDTO.getTenfApplicableTo());
				}
			} else {
				throw new CustomException("Form 10f Applicable From date is Mandatory", HttpStatus.BAD_REQUEST);
			}
		} else {
			shareholderMasterNonResidential
					.setTenfApplicableFrom(shareholderMasterNonResidentDTO.getTenfApplicableFrom());
			shareholderMasterNonResidential.setTenfApplicableTo(shareholderMasterNonResidentDTO.getTenfApplicableTo());
		}
		if (shareholderMasterNonResidentDTO.getTenfAvailable().equals(true) && (tenFFile != null)) {
			String tenfUri = blob.uploadExcelToBlob(tenFFile);
			shareholderMasterNonResidential.setTenfFileAddress(tenfUri);
		}

		shareholderMasterNonResidential
				.setIsPeAvailableInIndia(shareholderMasterNonResidentDTO.getPeAvailableInIndia());
		shareholderMasterNonResidential
				.setIsNoPeDeclarationAvailable(shareholderMasterNonResidentDTO.getNoPeDeclarationAvailable());
		if (shareholderMasterNonResidentDTO.getNoPeDeclarationAvailable().equals(true)) {
			if (shareholderMasterNonResidentDTO.getNoPeApplicableFrom() != null) {
				if (shareholderMasterNonResidentDTO.getNoPeApplicableTo() != null && (shareholderMasterNonResidentDTO
						.getNoPeApplicableFrom().equals(shareholderMasterNonResidentDTO.getNoPeApplicableTo())
						|| shareholderMasterNonResidentDTO.getNoPeApplicableFrom()
								.after(shareholderMasterNonResidentDTO.getNoPeApplicableTo()))) {
					throw new CustomException(
							"No PE Declaration Applicable From Date should not be equals or greater than No PE Declaration Applicable To Date",
							HttpStatus.BAD_REQUEST);
				} else {
					shareholderMasterNonResidential
							.setNoPeDeclarationApplicableFrom(shareholderMasterNonResidentDTO.getNoPeApplicableFrom());
					shareholderMasterNonResidential
							.setNoPeDeclarationApplicableTo(shareholderMasterNonResidentDTO.getNoPeApplicableTo());
				}
			} else {
				throw new CustomException("No PE Declaration Applicable From date is Mandatory",
						HttpStatus.BAD_REQUEST);
			}
		} else {
			shareholderMasterNonResidential
					.setNoPeDeclarationApplicableFrom(shareholderMasterNonResidentDTO.getNoPeApplicableFrom());
			shareholderMasterNonResidential
					.setNoPeDeclarationApplicableTo(shareholderMasterNonResidentDTO.getNoPeApplicableTo());
		}
		if (shareholderMasterNonResidentDTO.getNoPeDeclarationAvailable().equals(true) && (noPEFile != null)) {
			String noPEUri = blob.uploadExcelToBlob(noPEFile);
			shareholderMasterNonResidential.setNoPeFileAddress(noPEUri);
		}

		shareholderMasterNonResidential
				.setIsPoemOfShareholderInIndia(shareholderMasterNonResidentDTO.getPoemOfShareholderInIndia());
		shareholderMasterNonResidential
				.setIsNoPoemDeclarationAvailable(shareholderMasterNonResidentDTO.getNoPoemDeclarationAvailable());
		if (shareholderMasterNonResidentDTO.getNoPoemDeclarationAvailable().equals(true)) {
			if (shareholderMasterNonResidentDTO.getNoPoemInIndiaApplicableFrom() != null) {
				if (shareholderMasterNonResidentDTO.getNoPoemInIndiaApplicableTo() != null
						&& (shareholderMasterNonResidentDTO.getNoPoemInIndiaApplicableFrom()
								.equals(shareholderMasterNonResidentDTO.getNoPoemInIndiaApplicableTo())
								|| shareholderMasterNonResidentDTO.getNoPoemInIndiaApplicableFrom()
										.after(shareholderMasterNonResidentDTO.getNoPoemInIndiaApplicableTo()))) {
					throw new CustomException(
							"No Poem Declaration Applicable From Date should not be equals or greater than No Poem Declaration Applicable To Date",
							HttpStatus.BAD_REQUEST);
				} else {
					shareholderMasterNonResidential.setNoPoemDeclarationInIndiaApplicableFrom(
							shareholderMasterNonResidentDTO.getNoPoemInIndiaApplicableFrom());
					shareholderMasterNonResidential.setNoPoemDeclarationInIndiaApplicableTo(
							shareholderMasterNonResidentDTO.getNoPoemInIndiaApplicableTo());
				}
			} else {
				throw new CustomException("No Poem Declaration Applicable From date is Mandatory",
						HttpStatus.BAD_REQUEST);
			}
		} else {
			shareholderMasterNonResidential.setNoPoemDeclarationInIndiaApplicableFrom(
					shareholderMasterNonResidentDTO.getNoPoemInIndiaApplicableFrom());
			shareholderMasterNonResidential.setNoPoemDeclarationInIndiaApplicableTo(
					shareholderMasterNonResidentDTO.getNoPoemInIndiaApplicableTo());
		}
		if (shareholderMasterNonResidentDTO.getNoPoemDeclarationAvailable().equals(true) && (noPoemFile != null)) {
			String noPoemUri = blob.uploadExcelToBlob(noPoemFile);
			shareholderMasterNonResidential.setNoPoemFileAddress(noPoemUri);
		}

		shareholderMasterNonResidential.setIsMliSlobSatisfactionDeclarationAvailable(
				shareholderMasterNonResidentDTO.getMliSlobSatisfactionDeclarationAvailable());
		if (shareholderMasterNonResidentDTO.getMliSlobSatisfactionDeclarationAvailable().equals(true)
				&& (mliFile != null)) {
			String mliUri = blob.uploadExcelToBlob(mliFile);
			shareholderMasterNonResidential.setMliFileAddress(mliUri);
		}
		shareholderMasterNonResidential
				.setIsBeneficialOwnerOfIncome(shareholderMasterNonResidentDTO.getBeneficialOwnerOfIncome());
		shareholderMasterNonResidential.setIsBeneficialOwnershipDeclarationAvailable(
				shareholderMasterNonResidentDTO.getBeneficialOwnershipDeclarationAvailable());
		if (shareholderMasterNonResidentDTO.getBeneficialOwnershipDeclarationAvailable().equals(true)
				&& (beneficialOwnershipFile != null)) {
			String beneficialOwnershipUri = blob.uploadExcelToBlob(beneficialOwnershipFile);
			shareholderMasterNonResidential.setBeneficialOwnershipFileAddress(beneficialOwnershipUri);
		}
		shareholderMasterNonResidential
				.setIsTransactionGAARCompliant(shareholderMasterNonResidentDTO.getTransactionGAARCompliant());
		shareholderMasterNonResidential.setIsCommercialIndemnityOrTreatyBenefitsWithoutDocuments(
				shareholderMasterNonResidentDTO.getCommercialIndemnityOrTreatyBenefitsWithoutDocuments());

		if (shareholderMasterNonResidentDTO.getAddress().getCountry().equalsIgnoreCase("Kuwait")) {
			shareholderMasterNonResidential
					.setIsKuwaitShareholderType(shareholderMasterNonResidentDTO.getIsKuwaitShareholderType());
		} else {
			shareholderMasterNonResidential.setIsKuwaitShareholderType(null);
		}
		if (shareholderMasterNonResidentDTO.getAddress().getCountry().equalsIgnoreCase("UK")) {
			shareholderMasterNonResidential
					.setIsUKVehicleExemptTax(shareholderMasterNonResidentDTO.getIsUKVehicleExemptTax());
		} else {
			shareholderMasterNonResidential.setIsUKVehicleExemptTax(null);
		}
		if (shareholderMasterNonResidentDTO.getAddress().getCountry().equalsIgnoreCase("Iceland")) {
			shareholderMasterNonResidential
					.setIcelandDividendTaxationRate(shareholderMasterNonResidentDTO.getIcelandDividendTaxationRate());
			shareholderMasterNonResidential.setIcelandRateLessThanTwenty(shareholderMasterNonResidentDTO
					.getIcelandDividendTaxationRate().compareTo(new BigDecimal(20)) == -1);
		} else {
			shareholderMasterNonResidential.setIcelandDividendTaxationRate(null);
			shareholderMasterNonResidential.setIcelandRateLessThanTwenty(null);
		}
		shareholderMasterNonResidential
				.setForm15CACBApplicable(shareholderMasterNonResidentDTO.getForm15CACBApplicable());
		shareholderMasterNonResidential.setVersion(1);
		shareholderMasterNonResidential.setStringAssesmentYearDividendDetails("{"+year+":0.0}");
		shareholderMasterNonResidentialDAO.save(shareholderMasterNonResidential);
		shareholderNonResidentialHistoryDAO
				.save(ShareholderNonResidentialHistory.from(shareholderMasterNonResidential));
		return shareholderMasterNonResidential;
	}

	public void updateActivityStatusNonResident(String deductorTan, String deductorPan, String userName) {
		Integer assessmentYear = CommonUtil.getAssessmentYear(null);
		Integer assessmentMonthPlusOne = CommonUtil.getAssessmentMonthPlusOne(null);
		String activityStatus = getShareholderNonResidentPanStatus(deductorPan, assessmentYear, assessmentMonthPlusOne);
		activityTrackerService.updateActivity(deductorTan, assessmentYear, assessmentMonthPlusOne, userName,
				ActivityType.PAN_VERIFICATION.getActivityType(), activityStatus);
	}

	public String getShareholderNonResidentPanStatus(String deductorPan, Integer year, Integer month) {
		String startDate = CommonUtil.getQueryDate(CommonUtil.getMonthStartDate(year, month - 1));
		String endDate = CommonUtil.getQueryDate(CommonUtil.getMonthEndDate(year, month - 1));
		return getShareholderNonResidentPanStatus(deductorPan, startDate, endDate);
	}

	public String getShareholderNonResidentPanStatus(String deductorPan, String startDate, String endDate) {
		long countValidPan = 0;
		// shareholderMasterNonResidentialDAO
		// .countShareholderNonResidentialPanStatusValid(deductorPan, startDate,
		// endDate);
		logger.info("Total shareholder non residential valid pan status: {}", countValidPan);

		long countInValidPan = 0;
		// shareholderMasterNonResidentialDAO
		// .countShareholderResidentialPanStatusInValid(deductorPan, startDate,
		// endDate);
		logger.info("Total shareholder non residential invalid pan status: {}", countInValidPan);

		long countEmptyPan = 0;
		// shareholderMasterNonResidentialDAO
		// .countShareholderResidentialPanStatusEmpty(deductorPan, startDate, endDate);
		logger.info("Total shareholder non residential Empty pan status: {}", countEmptyPan);

		if (countValidPan == 0 && countInValidPan == 0 && countEmptyPan == 0) {
			return ActivityTrackerStatus.NORECORDS.name();
		} else if (countInValidPan > 0 || countEmptyPan > 0) {
			return ActivityTrackerStatus.PENDING.name();
		} else if (countValidPan > 0 && countInValidPan == 0 && countEmptyPan == 0) {
			return ActivityTrackerStatus.VALIDATED.name();
		} else {
			return StringUtils.EMPTY;
		}
	}

	@Transactional
	public BatchUpload saveFileData(MultipartFile multiPartFile, String deductorTan, Integer assesssmentYear,
			Integer assessmentMonthPlusOne, String userName, String tenantId, String deductorPan) throws Exception {
		String sha256 = sha256SumService.getSHA256Hash(multiPartFile);
		if (isAlreadyProcessed(sha256)) {
			BatchUpload batchUpload = new BatchUpload();
			batchUpload.setCreatedDate(new Date());
			batchUpload.setProcessStartTime(new Date());
			batchUpload.setCreatedBy(userName);
			batchUpload.setSuccessCount(0L);
			batchUpload.setFailedCount(0L);
			batchUpload.setRowsCount(0L);
			batchUpload.setProcessedCount(0);
			batchUpload.setMismatchCount(0L);
			batchUpload.setSha256sum(sha256);
			batchUpload.setStatus("Duplicate");
			batchUpload.setNewStatus("Duplicate");
			batchUpload.setProcessEndTime(new Date());
			batchUpload = shareholderBatchUpload(batchUpload, multiPartFile, deductorTan, assesssmentYear,
					assessmentMonthPlusOne, userName, null, tenantId);
			return batchUpload;
		}
		try (XSSFWorkbook workbook = new XSSFWorkbook(multiPartFile.getInputStream());) {
			XSSFSheet worksheet = workbook.getSheetAt(0);
			XSSFRow headerRow = worksheet.getRow(0);
			int headersCount = Excel.getNonEmptyCellsCount(headerRow);
			logger.info("Column header count {}:", headersCount);
			BatchUpload batchUpload = new BatchUpload();
			if (headersCount != ResidentShareholderExcel.fieldMappings.size()
					&& headersCount != NonResidentShareholderExcel.fieldMappings.size()) {
				batchUpload.setCreatedDate(new Timestamp(System.currentTimeMillis()));
				batchUpload.setProcessStartTime(new Timestamp(System.currentTimeMillis()));
				batchUpload.setSuccessCount(0L);
				batchUpload.setFailedCount(0L);
				batchUpload.setRowsCount(0L);
				batchUpload.setProcessedCount(0);
				batchUpload.setMismatchCount(0L);
				batchUpload.setSha256sum(sha256);
				batchUpload.setStatus("Failed");
				batchUpload.setCreatedBy(userName);
				batchUpload.setProcessEndTime(new Timestamp(System.currentTimeMillis()));
				return shareholderBatchUpload(batchUpload, multiPartFile, deductorTan, assesssmentYear,
						assessmentMonthPlusOne, userName, null, tenantId);
			} else {
				batchUpload.setCreatedDate(new Timestamp(System.currentTimeMillis()));
				batchUpload.setProcessStartTime(new Timestamp(System.currentTimeMillis()));
				batchUpload.setStatus("Processing");
				batchUpload.setSuccessCount(0L);
				batchUpload.setFailedCount(0L);
				batchUpload.setRowsCount(0L);
				batchUpload.setProcessedCount(0);
				batchUpload.setMismatchCount(0L);
				batchUpload = shareholderBatchUpload(batchUpload, multiPartFile, deductorTan, assesssmentYear,
						assessmentMonthPlusOne, userName, null, tenantId);
			}
			if (headersCount > ResidentShareholderExcel.fieldMappings.size()) {

				 shareholderBulkService.processNonResidentShareholdersWithCSV(workbook, multiPartFile, sha256,
						deductorTan, assesssmentYear, assessmentMonthPlusOne, userName, tenantId, deductorPan,
						batchUpload);
				 return batchUpload;
			} else {

				 shareholderBulkService.processResidentShareholdersWithCSV(multiPartFile, sha256, deductorTan,
						assesssmentYear, assessmentMonthPlusOne, userName, tenantId, deductorPan, batchUpload);
                 return batchUpload;
			}

		} catch (Exception e) {
			throw new RuntimeException("Failed to process Shareholder data ", e);
		}
	}

	private boolean isAlreadyProcessed(String sha256Sum) {

		List<BatchUpload> sha256Record = batchUploadDAO.getSha256Records(sha256Sum);

		return sha256Record != null && !sha256Record.isEmpty();
	}

	public BatchUpload shareholderBatchUpload(BatchUpload batchUpload, MultipartFile mFile, String tan,
			Integer assesssmentYear, Integer assessmentMonthPlusOne, String userName, File file, String tenant)
			throws IOException, InvalidKeyException, URISyntaxException, StorageException {
		logger.info("batch", batchUpload);
		String errorFp = null;
		String path = null;
		if (file != null) {
			errorFp = blob.uploadExcelToBlobWithFile(file, tenant);
			batchUpload.setErrorFilePath(errorFp);
		}
		if (mFile != null) {
			path = blob.uploadExcelToBlob(mFile);
			batchUpload.setFileName(mFile.getOriginalFilename());
			batchUpload.setFilePath(path);
		}
		int month = assessmentMonthPlusOne;
		batchUpload.setAssessmentMonth(month);
		batchUpload.setAssessmentYear(assesssmentYear);
		batchUpload.setDeductorMasterTan(tan);
		batchUpload.setUploadType(UploadTypes.SHAREHOLDER_EXCEL.name());
		batchUpload.setCreatedBy(userName);
		batchUpload.setActive(true);
		if (batchUpload.getBatchUploadID() != null) {
			batchUpload.setBatchUploadID(batchUpload.getBatchUploadID());
			batchUpload = batchUploadDAO.update(batchUpload);
		} else {
			batchUpload = batchUploadDAO.save(batchUpload);
		}
		return batchUpload;
	}

	public Set<String> getShareholderNames(String deductorPan, String shareholderType) {
		Set<String> shareholderNames = null;
		String tenantId = MultiTenantContext.getTenantId();
		if ("resident".equalsIgnoreCase(shareholderType)) {
			shareholderNames = null;/*
									 * shareholderMasterResidentialDAO.getShareholderByPanTenantId(deductorPan,
									 * tenantId)
									 * .parallelStream().map(ShareholderMasterResidential::getShareholderName).
									 * distinct() .collect(Collectors.toSet());
									 */
		} else {
			shareholderNames = null;/*
									 * shareholderMasterNonResidentialDAO .getShareholderByPanTenantId(deductorPan,
									 * tenantId).parallelStream()
									 * .map(ShareholderMasterNonResidential::getShareholderName).distinct().collect(
									 * Collectors.toSet());
									 */
		}
		return shareholderNames;
	}

	public List<Pair<String, String>> getShareholderByResidentialStatus(String deductorPan,String residentialStatus) {
		Map<String, String> shareholdersByPan = new HashMap<>();
		if(residentialStatus.equalsIgnoreCase("RES")) {
			List<ShareholderMasterResidential> residentShareholders = shareholderMasterResidentialDAO
					.getAllResidentShareholderByPanTenantId(deductorPan);
			shareholdersByPan = residentShareholders.stream()
					.filter(shareholderMasterResidential -> shareholderMasterResidential.getShareholderPan() != null)
					.collect(Collectors.toMap(ShareholderMasterResidential::getShareholderPan,
							ShareholderMasterResidential::getShareholderName, (oldValue, newValue) -> {
								return oldValue;
							}));
		}else {

			List<ShareholderMasterNonResidential> nonResidentShareholders = shareholderMasterNonResidentialDAO
					.getAllNonResidentShareholderByPanTenantId(deductorPan);
			shareholdersByPan = nonResidentShareholders.stream()
					.filter(shareholderMasterResidential -> shareholderMasterResidential.getShareholderPan() != null)
					.collect(Collectors.toMap(ShareholderMasterNonResidential::getShareholderPan,
							ShareholderMasterNonResidential::getShareholderName, (oldValue, newValue) -> {
								return oldValue;
							}));
		}

		return shareholdersByPan.entrySet().stream().map(m -> Pair.of(m.getKey(), m.getValue()))
				.collect(Collectors.toList());
	}

	public ShareholderMasterNonResidential getShareholderNonResidentByFolioNumber(String deductorPan, String tenantId,
			String foliono) {
		List<ShareholderMasterNonResidential> shareholders = shareholderMasterNonResidentialDAO
				.getNonResidentShareholderByFolioNumberPanTenantId(deductorPan, tenantId, foliono);
		if (shareholders.size() == 0) {
			throw new CustomException("shareholder not found for given folio number");
		} else {
			return shareholders.get(0);
		}
	}

	private void validateShareholderTypeAndPan(ShareholderMasterNonResidentDTO shareholderMasterNonResidentDTO) {
		if (StringUtils.isBlank(shareholderMasterNonResidentDTO.getShareholderType())
				&& StringUtils.isBlank(shareholderMasterNonResidentDTO.getShareholderPan())) {
			throw new CustomException("Either Shareholder type or PAN value should exist", HttpStatus.BAD_REQUEST);
		} else if (StringUtils.isNotBlank(shareholderMasterNonResidentDTO.getShareholderPan())
				&& StringUtils.isBlank(shareholderMasterNonResidentDTO.getShareholderType())) {
			String shareholderType = ValidationUtil
					.getShareholderTypeByPan(shareholderMasterNonResidentDTO.getShareholderPan());
			if (shareholderType == null) {
				throw new CustomException("Invalid Shareholder Type for given PAN", HttpStatus.BAD_REQUEST);
			}
			shareholderMasterNonResidentDTO.setShareholderType(shareholderType);
		} else if (StringUtils.isBlank(shareholderMasterNonResidentDTO.getShareholderPan())
				&& StringUtils.isNotBlank(shareholderMasterNonResidentDTO.getShareholderType())) {
			boolean flag = ValidationUtil.validateShareholderType(shareholderMasterNonResidentDTO.getShareholderType());
			if (!flag) {
				throw new CustomException("Invalid Shareholder Type", HttpStatus.BAD_REQUEST);
			}
		} else {
			boolean flag = ValidationUtil.validateShareholderTypeAndPan(
					shareholderMasterNonResidentDTO.getShareholderType(),
					shareholderMasterNonResidentDTO.getShareholderPan());
			if (!flag) {
				throw new CustomException("Invalid Shareholder Type for given PAN", HttpStatus.BAD_REQUEST);
			}
		}
	}

	/**
	 * to retrieve non resident share holder data and return to UI
	 * 
	 * @param deductorPan
	 * @param pagination
	 * @param shareholderName
	 * @param tenantId
	 * @return
	 */
	public CommonDTO<ShareholderMasterDTO> getListOfNonResidentialShareholders(String deductorPan,
			Pagination pagination, String shareholderName, String tenantId) {

		List<ShareholderMasterDTO> shareholderMasterDTOS = new ArrayList<>();
		CommonDTO<ShareholderMasterDTO> shareHolderMasterData = new CommonDTO<ShareholderMasterDTO>();
		BigInteger count = null;

		ShareholderMasterDTO shareholderMasterDTO = null;
		logger.info("shareholder name ---- : {}", shareholderName);
		List<ShareholderMasterNonResidential> nonResidentList = null;

		if ("noshareholderfilter".equalsIgnoreCase(shareholderName)) {
			nonResidentList = shareholderMasterNonResidentialDAO.findAllByPanTenantIdAndName(deductorPan,
					shareholderName, pagination);
		} else {
			nonResidentList = shareholderMasterNonResidentialDAO.findAllByPanTenantIdAndName(deductorPan,
					shareholderName, pagination);
		}
		for (ShareholderMasterNonResidential shareholderMasterNonResidential : nonResidentList) {

			shareholderMasterDTO = new ShareholderMasterDTO();
			AddressDTO address = new AddressDTO();

			BeanUtils.copyProperties(shareholderMasterNonResidential, shareholderMasterDTO);
			shareholderMasterDTO.setId(shareholderMasterNonResidential.getId());

			address.setAreaLocality(shareholderMasterNonResidential.getAreaLocality());
			address.setFlatDoorBlockNo(shareholderMasterNonResidential.getFlatDoorBlockNo());
			address.setNameBuildingVillage(shareholderMasterNonResidential.getNameBuildingVillage());
			address.setPinCode(shareholderMasterNonResidential.getPinCode());
			address.setRoadStreetPostoffice(shareholderMasterNonResidential.getRoadStreetPostoffice());
			if (StringUtils.isNotBlank(shareholderMasterNonResidential.getCountry())) {
				address.setCountry(shareholderMasterNonResidential.getCountry().toUpperCase());
			} else {
				address.setCountry(shareholderMasterNonResidential.getCountry());
			}
			if (StringUtils.isNotBlank(shareholderMasterNonResidential.getState())) {
				address.setStateName(shareholderMasterNonResidential.getState().toUpperCase());
			} else {
				address.setStateName(shareholderMasterNonResidential.getState());
			}
			address.setTownCityDistrict(shareholderMasterNonResidential.getTownCityDistrict());
			shareholderMasterDTO.setAddress(address);
			shareholderMasterDTO.setKeyShareholder(shareholderMasterNonResidential.getKeyShareholder());
			shareholderMasterDTO
					.setNoPeApplicableFrom(shareholderMasterNonResidential.getNoPeDeclarationApplicableFrom());
			shareholderMasterDTO.setNoPoemInIndiaApplicableTo(
					shareholderMasterNonResidential.getNoPoemDeclarationInIndiaApplicableTo());
			shareholderMasterDTO.setNoPoemInIndiaApplicableFrom(
					shareholderMasterNonResidential.getNoPoemDeclarationInIndiaApplicableFrom());
			shareholderMasterDTO.setNoPeApplicableTo(shareholderMasterNonResidential.getNoPeDeclarationApplicableTo());
			shareholderMasterDTO.setPanStatus(shareholderMasterNonResidential.getPanStatus());
			shareholderMasterDTO.setPanVerifiedDate(shareholderMasterNonResidential.getPanVerifiedDate());

			String trcUrl = shareholderMasterNonResidential.getTrcFileAddress();
			if (StringUtils.isNotBlank(trcUrl)) {
				String trcFileName = trcUrl.substring(trcUrl.lastIndexOf('/') + 1);
				shareholderMasterDTO.setTrcFileName(trcFileName);
				shareholderMasterDTO.setTrcFileAddress(shareholderMasterNonResidential.getTrcFileAddress());
			}

			String tenfUrl = shareholderMasterNonResidential.getTenfFileAddress();
			if (StringUtils.isNotBlank(tenfUrl)) {
				String tenfFileName = tenfUrl.substring(tenfUrl.lastIndexOf('/') + 1);
				shareholderMasterDTO.setTenfFileName(tenfFileName);
				shareholderMasterDTO.setTenfFileAddress(shareholderMasterNonResidential.getTenfFileAddress());
			}

			String noPEUrl = shareholderMasterNonResidential.getNoPeFileAddress();
			if (StringUtils.isNotBlank(noPEUrl)) {
				String noPeFileName = noPEUrl.substring(noPEUrl.lastIndexOf('/') + 1);
				shareholderMasterDTO.setNoPeFileName(noPeFileName);
				shareholderMasterDTO.setNoPeFileAddress(shareholderMasterNonResidential.getNoPeFileAddress());
			}

			String noPoemUrl = shareholderMasterNonResidential.getNoPoemFileAddress();
			if (StringUtils.isNotBlank(noPoemUrl)) {
				String noPoemFileName = noPoemUrl.substring(noPoemUrl.lastIndexOf('/') + 1);
				shareholderMasterDTO.setNoPoemFileName(noPoemFileName);
				shareholderMasterDTO.setNoPoemFileAddress(shareholderMasterNonResidential.getNoPoemFileAddress());
			}

			String mliUrl = shareholderMasterNonResidential.getMliFileAddress();
			if (StringUtils.isNotBlank(mliUrl)) {
				String mliFileName = mliUrl.substring(mliUrl.lastIndexOf('/') + 1);
				shareholderMasterDTO.setMliFileName(mliFileName);
				shareholderMasterDTO.setMliFileAddress(shareholderMasterNonResidential.getMliFileAddress());
			}

			String beneficialUrl = shareholderMasterNonResidential.getBeneficialOwnershipFileAddress();
			if (StringUtils.isNotBlank(beneficialUrl)) {
				String beneficialFileName = beneficialUrl.substring(beneficialUrl.lastIndexOf('/') + 1);
				shareholderMasterDTO.setBeneficialOwnershipFileName(beneficialFileName);
				shareholderMasterDTO.setBeneficialOwnershipFileAddress(
						shareholderMasterNonResidential.getBeneficialOwnershipFileAddress());
			}
			shareholderMasterDTO.setAssessmentYearDividendDetails(
					shareholderMasterNonResidential.getAssessmentYearDividendDetails());
			shareholderMasterDTO.setForm15CACBApplicable(shareholderMasterNonResidential.getForm15CACBApplicable());
			shareholderMasterDTOS.add(shareholderMasterDTO);
		}

		count = shareholderMasterNonResidentialDAO.getCountByPanTenantId(deductorPan, shareholderName);

		PagedData<ShareholderMasterDTO> pagedData = new PagedData<>(shareholderMasterDTOS, shareholderMasterDTOS.size(),
				pagination.getPageNumber(),
				count.intValue() > (pagination.getPageSize() * pagination.getPageNumber()) ? false : true);

		shareHolderMasterData.setCount(count);
		shareHolderMasterData.setResultsSet(pagedData);
		return shareHolderMasterData;
	}

	public ShareholderMasterNonResidential updateNonResident(
			ShareholderMasterNonResidentDTO shareholderMasterNonResidentDTO, String deductorPan, MultipartFile trcFile,
			MultipartFile tenFFile, MultipartFile noPEFile, MultipartFile noPoemFile, MultipartFile mliFile,
			MultipartFile beneficialOwnershipFile, String userName)
			throws IOException, StorageException, InvalidKeyException, URISyntaxException {

		logger.info("REST request to update NON Resident Shareholder Master Record : {}",
				shareholderMasterNonResidentDTO);

		ShareholderMasterNonResidential shareholderMasterNonResidential = null;
		shareholderMasterNonResidential = shareholderMasterNonResidentialDAO
				.findById(shareholderMasterNonResidentDTO.getId()).get(0);
		logger.info("Record retrieved successfully {}");

		BeanUtils.copyProperties(shareholderMasterNonResidentDTO, shareholderMasterNonResidential);

		shareholderMasterNonResidential
				.setFlatDoorBlockNo(shareholderMasterNonResidentDTO.getAddress().getFlatDoorBlockNo());
		shareholderMasterNonResidential
				.setNameBuildingVillage(shareholderMasterNonResidentDTO.getAddress().getNameBuildingVillage());
		shareholderMasterNonResidential
				.setRoadStreetPostoffice(shareholderMasterNonResidentDTO.getAddress().getRoadStreetPostoffice());
		shareholderMasterNonResidential.setAreaLocality(shareholderMasterNonResidentDTO.getAddress().getAreaLocality());
		shareholderMasterNonResidential
				.setTownCityDistrict(shareholderMasterNonResidentDTO.getAddress().getTownCityDistrict());
		shareholderMasterNonResidential.setState(shareholderMasterNonResidentDTO.getAddress().getStateName());
		shareholderMasterNonResidential.setCountry(shareholderMasterNonResidentDTO.getAddress().getCountry());
		shareholderMasterNonResidential.setPinCode(shareholderMasterNonResidentDTO.getAddress().getPinCode());
		shareholderMasterNonResidential
				.setNoPeDeclarationApplicableFrom(shareholderMasterNonResidentDTO.getNoPeApplicableFrom());
		shareholderMasterNonResidential
				.setNoPeDeclarationApplicableTo(shareholderMasterNonResidentDTO.getNoPeApplicableTo());
		shareholderMasterNonResidential.setNoPoemDeclarationInIndiaApplicableFrom(
				shareholderMasterNonResidentDTO.getNoPoemInIndiaApplicableFrom());
		shareholderMasterNonResidential.setNoPoemDeclarationInIndiaApplicableTo(
				shareholderMasterNonResidentDTO.getNoPoemInIndiaApplicableTo());
		shareholderMasterNonResidential.setModifiedDate(new Date());
		shareholderMasterNonResidential.setModifiedBy(userName);
		shareholderMasterNonResidential.setShareholderPan(shareholderMasterNonResidentDTO.getShareholderPan());

		shareholderMasterNonResidential.setIsTrcAvailable(shareholderMasterNonResidentDTO.getTrcAvailable());
		if (shareholderMasterNonResidentDTO.getTrcAvailable().equals(true) && (trcFile != null)) {
			String trcUri = blob.uploadExcelToBlob(trcFile);
			shareholderMasterNonResidential.setTrcFileAddress(trcUri);
		}

		shareholderMasterNonResidential.setIsTenfAvailable(shareholderMasterNonResidentDTO.getTenfAvailable());
		if (shareholderMasterNonResidentDTO.getTenfAvailable().equals(true) && (tenFFile != null)) {
			String tenfUri = blob.uploadExcelToBlob(tenFFile);
			shareholderMasterNonResidential.setTenfFileAddress(tenfUri);
		}
		shareholderMasterNonResidential
				.setIsPeAvailableInIndia(shareholderMasterNonResidentDTO.getPeAvailableInIndia());
		shareholderMasterNonResidential
				.setIsNoPeDeclarationAvailable(shareholderMasterNonResidentDTO.getNoPeDeclarationAvailable());
		if (shareholderMasterNonResidentDTO.getNoPeDeclarationAvailable() != null
				&& shareholderMasterNonResidentDTO.getNoPeDeclarationAvailable().equals(true) && (noPEFile != null)) {
			String noPEUri = blob.uploadExcelToBlob(noPEFile);
			shareholderMasterNonResidential.setNoPeFileAddress(noPEUri);
		}

		shareholderMasterNonResidential
				.setIsPoemOfShareholderInIndia(shareholderMasterNonResidentDTO.getPoemOfShareholderInIndia());
		shareholderMasterNonResidential
				.setIsNoPoemDeclarationAvailable(shareholderMasterNonResidentDTO.getNoPoemDeclarationAvailable());
		if (shareholderMasterNonResidentDTO.getNoPoemDeclarationAvailable() != null
				&& shareholderMasterNonResidentDTO.getNoPoemDeclarationAvailable().equals(true)
				&& (noPoemFile != null)) {
			String noPoemUri = blob.uploadExcelToBlob(noPoemFile);
			shareholderMasterNonResidential.setNoPoemFileAddress(noPoemUri);
		}

		shareholderMasterNonResidential.setIsMliSlobSatisfactionDeclarationAvailable(
				shareholderMasterNonResidentDTO.getMliSlobSatisfactionDeclarationAvailable());
		if (shareholderMasterNonResidentDTO.getMliSlobSatisfactionDeclarationAvailable().equals(true)
				&& (mliFile != null)) {
			String mliUri = blob.uploadExcelToBlob(mliFile);
			shareholderMasterNonResidential.setMliFileAddress(mliUri);
		}

		shareholderMasterNonResidential
				.setIsBeneficialOwnerOfIncome(shareholderMasterNonResidentDTO.getBeneficialOwnerOfIncome());
		shareholderMasterNonResidential.setIsBeneficialOwnershipDeclarationAvailable(
				shareholderMasterNonResidentDTO.getBeneficialOwnershipDeclarationAvailable());
		if (shareholderMasterNonResidentDTO.getBeneficialOwnershipDeclarationAvailable() != null
				&& shareholderMasterNonResidentDTO.getBeneficialOwnershipDeclarationAvailable().equals(true)
				&& (beneficialOwnershipFile != null)) {
			String beneficialOwnershipUri = blob.uploadExcelToBlob(beneficialOwnershipFile);
			shareholderMasterNonResidential.setBeneficialOwnershipFileAddress(beneficialOwnershipUri);
		}
		shareholderMasterNonResidential
				.setForm15CACBApplicable(shareholderMasterNonResidentDTO.getForm15CACBApplicable());
		shareholderMasterNonResidential.setVersion(shareholderMasterNonResidential.getVersion() + 1);
		shareholderMasterNonResidentialDAO.update(shareholderMasterNonResidential);
		ShareholderNonResidentialHistory updatedNonResident = ShareholderNonResidentialHistory
				.from(shareholderMasterNonResidential);
		shareholderNonResidentialHistoryDAO.update(updatedNonResident);

		return shareholderMasterNonResidential;
	}

	/**
	 * to get non resident share holder using id
	 * 
	 * @param id
	 * @return
	 */
	public ShareholderMasterDTO getNonResidential(Integer id) {
		ShareholderMasterDTO shareholderMasterNonResidentDTO = new ShareholderMasterDTO();
		AddressDTO address = new AddressDTO();
		ShareholderMasterNonResidential shareholderMasterNonResidential = shareholderMasterNonResidentialDAO
				.findById(id).get(0);
		logger.info(
				"Retrieved non resident share holder record success fully " + shareholderMasterNonResidential + "{}");
		BeanUtils.copyProperties(shareholderMasterNonResidential, shareholderMasterNonResidentDTO);
		shareholderMasterNonResidentDTO.setId(shareholderMasterNonResidential.getId());

		address.setAreaLocality(shareholderMasterNonResidential.getAreaLocality());
		address.setFlatDoorBlockNo(shareholderMasterNonResidential.getFlatDoorBlockNo());
		address.setNameBuildingVillage(shareholderMasterNonResidential.getNameBuildingVillage());
		address.setPinCode(shareholderMasterNonResidential.getPinCode());
		address.setRoadStreetPostoffice(shareholderMasterNonResidential.getRoadStreetPostoffice());
		if (StringUtils.isNotBlank(shareholderMasterNonResidential.getCountry())) {
			address.setCountry(shareholderMasterNonResidential.getCountry().toUpperCase());
		} else {
			address.setCountry(shareholderMasterNonResidential.getCountry());
		}
		if (StringUtils.isNotBlank(shareholderMasterNonResidential.getState())) {
			address.setStateName(shareholderMasterNonResidential.getState().toUpperCase());
		} else {
			address.setStateName(shareholderMasterNonResidential.getState());
		}
		address.setTownCityDistrict(shareholderMasterNonResidential.getTownCityDistrict());
		shareholderMasterNonResidentDTO
				.setPoemOfShareholderInIndia(shareholderMasterNonResidential.getIsPoemOfShareholderInIndia());
		shareholderMasterNonResidentDTO
				.setPeAvailableInIndia(shareholderMasterNonResidential.getIsPeAvailableInIndia());
		shareholderMasterNonResidentDTO.setTrcAvailable(shareholderMasterNonResidential.getIsTrcAvailable());
		shareholderMasterNonResidentDTO.setTenfAvailable(shareholderMasterNonResidential.getIsTenfAvailable());
		shareholderMasterNonResidentDTO
				.setNoPeDeclarationAvailable(shareholderMasterNonResidential.getIsNoPeDeclarationAvailable());
		shareholderMasterNonResidentDTO
				.setNoPoemDeclarationAvailable(shareholderMasterNonResidential.getIsNoPoemDeclarationAvailable());
		shareholderMasterNonResidentDTO.setMliSlobSatisfactionDeclarationAvailable(
				shareholderMasterNonResidential.getIsMliSlobSatisfactionDeclarationAvailable());
		shareholderMasterNonResidentDTO
				.setBeneficialOwnerOfIncome(shareholderMasterNonResidential.getIsBeneficialOwnerOfIncome());
		shareholderMasterNonResidentDTO.setBeneficialOwnershipDeclarationAvailable(
				shareholderMasterNonResidential.getIsBeneficialOwnershipDeclarationAvailable());
		shareholderMasterNonResidentDTO
				.setTransactionGAARCompliant(shareholderMasterNonResidential.getIsTransactionGAARCompliant());
		shareholderMasterNonResidentDTO.setCommercialIndemnityOrTreatyBenefitsWithoutDocuments(
				shareholderMasterNonResidential.getIsCommercialIndemnityOrTreatyBenefitsWithoutDocuments());
		shareholderMasterNonResidentDTO.setAddress(address);
		shareholderMasterNonResidentDTO.setKeyShareholder(shareholderMasterNonResidential.getKeyShareholder());
		shareholderMasterNonResidentDTO
				.setNoPeApplicableFrom(shareholderMasterNonResidential.getNoPeDeclarationApplicableFrom());
		shareholderMasterNonResidentDTO.setNoPoemInIndiaApplicableTo(
				shareholderMasterNonResidential.getNoPoemDeclarationInIndiaApplicableTo());
		shareholderMasterNonResidentDTO.setNoPoemInIndiaApplicableFrom(
				shareholderMasterNonResidential.getNoPoemDeclarationInIndiaApplicableFrom());
		shareholderMasterNonResidentDTO
				.setNoPeApplicableTo(shareholderMasterNonResidential.getNoPeDeclarationApplicableTo());
		shareholderMasterNonResidentDTO.setPanStatus(shareholderMasterNonResidential.getPanStatus());
		shareholderMasterNonResidentDTO.setPanVerifiedDate(shareholderMasterNonResidential.getPanVerifiedDate());

		String trcUrl = shareholderMasterNonResidential.getTrcFileAddress();
		if (StringUtils.isNotBlank(trcUrl)) {
			String trcFileName = trcUrl.substring(trcUrl.lastIndexOf('/') + 1);
			shareholderMasterNonResidentDTO.setTrcFileName(trcFileName);
			shareholderMasterNonResidentDTO.setTrcFileAddress(shareholderMasterNonResidential.getTrcFileAddress());
		}

		String tenfUrl = shareholderMasterNonResidential.getTenfFileAddress();
		if (StringUtils.isNotBlank(tenfUrl)) {
			String tenfFileName = tenfUrl.substring(tenfUrl.lastIndexOf('/') + 1);
			shareholderMasterNonResidentDTO.setTenfFileName(tenfFileName);
			shareholderMasterNonResidentDTO.setTenfFileAddress(shareholderMasterNonResidential.getTenfFileAddress());
		}

		String noPEUrl = shareholderMasterNonResidential.getNoPeFileAddress();
		if (StringUtils.isNotBlank(noPEUrl)) {
			String noPeFileName = noPEUrl.substring(noPEUrl.lastIndexOf('/') + 1);
			shareholderMasterNonResidentDTO.setNoPeFileName(noPeFileName);
			shareholderMasterNonResidentDTO.setNoPeFileAddress(shareholderMasterNonResidential.getNoPeFileAddress());
		}

		String noPoemUrl = shareholderMasterNonResidential.getNoPoemFileAddress();
		if (StringUtils.isNotBlank(noPoemUrl)) {
			String noPoemFileName = noPoemUrl.substring(noPoemUrl.lastIndexOf('/') + 1);
			shareholderMasterNonResidentDTO.setNoPoemFileName(noPoemFileName);
			shareholderMasterNonResidentDTO
					.setNoPoemFileAddress(shareholderMasterNonResidential.getNoPoemFileAddress());
		}

		String mliUrl = shareholderMasterNonResidential.getMliFileAddress();
		if (StringUtils.isNotBlank(mliUrl)) {
			String mliFileName = mliUrl.substring(mliUrl.lastIndexOf('/') + 1);
			shareholderMasterNonResidentDTO.setMliFileName(mliFileName);
			shareholderMasterNonResidentDTO.setMliFileAddress(shareholderMasterNonResidential.getMliFileAddress());
		}

		String beneficialUrl = shareholderMasterNonResidential.getBeneficialOwnershipFileAddress();
		if (StringUtils.isNotBlank(beneficialUrl)) {
			String beneficialFileName = beneficialUrl.substring(beneficialUrl.lastIndexOf('/') + 1);
			shareholderMasterNonResidentDTO.setBeneficialOwnershipFileName(beneficialFileName);
			shareholderMasterNonResidentDTO.setBeneficialOwnershipFileAddress(
					shareholderMasterNonResidential.getBeneficialOwnershipFileAddress());
		}
		shareholderMasterNonResidentDTO
				.setAssessmentYearDividendDetails(shareholderMasterNonResidential.getAssessmentYearDividendDetails());
		shareholderMasterNonResidentDTO
				.setForm15CACBApplicable(shareholderMasterNonResidential.getForm15CACBApplicable());

		return shareholderMasterNonResidentDTO;
	}

	public List<ShareholderMasterResidential> getResidentShareholdersByPan(String deductorPan, String shareHolderPan) {
		return shareholderMasterResidentialDAO.getResidentShareholdersByPan(deductorPan, shareHolderPan);
	}

	public List<ShareholderMasterNonResidential> getNonResidentShareholdersByPan(String deductorPan,
			String shareHolderPan) {
		return shareholderMasterNonResidentialDAO.getNonResidentShareholdersByPan(deductorPan, shareHolderPan);
	}

	public List<ShareholderMasterNonResidential> getNonResidentialById(Integer id) {
		return shareholderMasterNonResidentialDAO.findById(id);
	}
	
	 public Workbook shareholderPANValidationReport(String deductorPan) throws Exception {

	        List<ShareholderMasterResidential> shareholderMasterResidentials = shareholderMasterResidentialDAO
	                .getAllResidentShareholderByPanTenantId(deductorPan);

	        List<ShareholderMasterNonResidential> shareholderMasterNonResidentials = shareholderMasterNonResidentialDAO
	                .getAllNonResidentShareholderByPanTenantId(deductorPan);

	        Workbook workbook = new Workbook();
	        Worksheet worksheet = workbook.getWorksheets().get(0);
	        worksheet.setName("Shareholder Pan Validation");

	        String[] mainHeaderNames = new String[]{"Vendor Details from Shareholder Master", "",
	                "Vendor Details from TRACES/NSDL Portal", "", "Vendor Name Comparison", "", "Interpretation"};
	        worksheet.getCells().importArray(mainHeaderNames, 0, 0, false);

	        String[] subHeaderNames = new String[]{"PAN", "Vendor Name", "Residential status", "Name from Traces", "Match score",
	                "Match category", "Remarks"};
	        worksheet.getCells().importArray(subHeaderNames, 1, 0, false);

	        ImportTableOptions tableOptions = new ImportTableOptions();
	        tableOptions.setConvertGridStyle(true);

	        int rowIndex = 2;
	        if (!shareholderMasterResidentials.isEmpty()) {
	            for (ShareholderMasterResidential shareholderMasterResidential : shareholderMasterResidentials) {
	                ArrayList<String> rowData = new ArrayList<String>();
	                rowData.add(StringUtils.isBlank(shareholderMasterResidential.getShareholderPan()) ? StringUtils.EMPTY
	                        : shareholderMasterResidential.getShareholderPan());
	                rowData.add(StringUtils.isBlank(shareholderMasterResidential.getShareholderName()) ? StringUtils.EMPTY
	                        : shareholderMasterResidential.getShareholderName());
	                rowData.add(StringUtils.isBlank(shareholderMasterResidential.getShareholderResidentialStatus()) ? StringUtils.EMPTY
	                        : shareholderMasterResidential.getShareholderResidentialStatus());
	                rowData.add(StringUtils.isBlank(shareholderMasterResidential.getNameAsPerTraces()) ? StringUtils.EMPTY
	                        : shareholderMasterResidential.getNameAsPerTraces());
	                rowData.add(StringUtils.isBlank(shareholderMasterResidential.getMatchScore()) ? StringUtils.EMPTY
	                        : shareholderMasterResidential.getMatchScore());
	                rowData.add(StringUtils.isBlank(shareholderMasterResidential.getPanStatus()) ? StringUtils.EMPTY
	                        : shareholderMasterResidential.getPanStatus());
	                rowData.add(
	                        StringUtils.isBlank(shareholderMasterResidential.getRemarksAsPerTraces()) ? StringUtils.EMPTY
	                                : shareholderMasterResidential.getRemarksAsPerTraces());
	                worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);
	            }
	        }
	        if (!shareholderMasterNonResidentials.isEmpty()) {
	            for (ShareholderMasterNonResidential shareholderMasterNonResidential : shareholderMasterNonResidentials) {
	                ArrayList<String> rowData = new ArrayList<String>();
	                rowData.add(StringUtils.isBlank(shareholderMasterNonResidential.getShareholderPan()) ? StringUtils.EMPTY
	                        : shareholderMasterNonResidential.getShareholderPan());
	                rowData.add(StringUtils.isBlank(shareholderMasterNonResidential.getShareholderName()) ? StringUtils.EMPTY
	                        : shareholderMasterNonResidential.getShareholderName());
	                rowData.add(StringUtils.isBlank(shareholderMasterNonResidential.getShareholderResidentialStatus()) ? StringUtils.EMPTY
	                        : shareholderMasterNonResidential.getShareholderResidentialStatus());
	                rowData.add(StringUtils.isBlank(shareholderMasterNonResidential.getNameAsPerTraces()) ? StringUtils.EMPTY
	                        : shareholderMasterNonResidential.getNameAsPerTraces());
	                rowData.add(StringUtils.isBlank(shareholderMasterNonResidential.getMatchScore()) ? StringUtils.EMPTY
	                        : shareholderMasterNonResidential.getMatchScore());
	                rowData.add(StringUtils.isBlank(shareholderMasterNonResidential.getPanStatus()) ? StringUtils.EMPTY
	                        : shareholderMasterNonResidential.getPanStatus());
	                rowData.add(
	                        StringUtils.isBlank(shareholderMasterNonResidential.getRemarksAsPerTraces()) ? StringUtils.EMPTY
	                                : shareholderMasterNonResidential.getRemarksAsPerTraces());
	                worksheet.getCells().importArrayList(rowData, rowIndex++, 0, false);
	            }
	        }

	        Cell a1 = worksheet.getCells().get("A2");
	        Style style1 = a1.getStyle();
	        style1.setForegroundColor(Color.fromArgb(217, 217, 217));
	        style1.setPattern(BackgroundType.SOLID);
	        style1.getFont().setBold(true);
	        style1.setHorizontalAlignment(TextAlignmentType.CENTER);
	        a1.setStyle(style1);

	        Cell a2 = worksheet.getCells().get("B2");
	        Style style2 = a2.getStyle();
	        style2.setForegroundColor(Color.fromArgb(217, 217, 217));
	        style2.setPattern(BackgroundType.SOLID);
	        style2.getFont().setBold(true);
	        style2.setHorizontalAlignment(TextAlignmentType.CENTER);
	        a2.setStyle(style2);

	        Cell a3 = worksheet.getCells().get("C2");
	        Style style3 = a3.getStyle();
	        style3.setForegroundColor(Color.fromArgb(217, 217, 217));
	        style3.setPattern(BackgroundType.SOLID);
	        style3.getFont().setBold(true);
	        style3.setHorizontalAlignment(TextAlignmentType.CENTER);
	        a3.setStyle(style3);

	        Cell a4 = worksheet.getCells().get("D2");
	        Style style4 = a4.getStyle();
	        style4.setForegroundColor(Color.fromArgb(217, 217, 217));
	        style4.setPattern(BackgroundType.SOLID);
	        style4.getFont().setBold(true);
	        style4.setHorizontalAlignment(TextAlignmentType.CENTER);
	        a4.setStyle(style4);

	        Cell a5 = worksheet.getCells().get("E2");
	        Style style5 = a5.getStyle();
	        style5.setForegroundColor(Color.fromArgb(217, 217, 217));
	        style5.setPattern(BackgroundType.SOLID);
	        style5.getFont().setBold(true);
	        style5.setHorizontalAlignment(TextAlignmentType.CENTER);
	        a5.setStyle(style5);

	        Cell a6 = worksheet.getCells().get("F2");
	        Style style6 = a6.getStyle();
	        style6.setForegroundColor(Color.fromArgb(217, 217, 217));
	        style6.setPattern(BackgroundType.SOLID);
	        style6.getFont().setBold(true);
	        style6.setHorizontalAlignment(TextAlignmentType.CENTER);
	        a6.setStyle(style6);

	        Cell a7 = worksheet.getCells().get("G2");
	        Style style7 = a7.getStyle();
	        style7.setForegroundColor(Color.fromArgb(217, 217, 217));
	        style7.setPattern(BackgroundType.SOLID);
	        style7.getFont().setBold(true);
	        style7.setHorizontalAlignment(TextAlignmentType.CENTER);
	        a7.setStyle(style7);

	        // css for main heaeders
	        Style mergeRange1Style = workbook.createStyle();
	        mergeRange1Style.setForegroundColor(Color.fromArgb(157, 195, 230));
	        mergeRange1Style.setPattern(BackgroundType.SOLID);
	        mergeRange1Style.getFont().setBold(true);
	        mergeRange1Style.setHorizontalAlignment(TextAlignmentType.CENTER);
	        mergeRange1Style.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, Color.getBlack());
	        mergeRange1Style.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, Color.getBlack());
	        mergeRange1Style.setBorder(BorderType.LEFT_BORDER, CellBorderType.THIN, Color.getBlack());
	        mergeRange1Style.setBorder(BorderType.RIGHT_BORDER, CellBorderType.THIN, Color.getBlack());

	        // Merge cells
	        Range mergeRange1 = worksheet.getCells().createRange("A1:B1");
	        mergeRange1.merge();
	        mergeRange1.setStyle(mergeRange1Style);

	        Range mergeRange2 = worksheet.getCells().createRange("C1:D1");
	        mergeRange2.merge();
	        mergeRange2.setStyle(mergeRange1Style);

	        Range mergeRange3 = worksheet.getCells().createRange("E1:F1");
	        mergeRange3.merge();
	        mergeRange3.setStyle(mergeRange1Style);

	        worksheet.getCells().get("G1").setStyle(mergeRange1Style);

	        worksheet.autoFitColumns();
	        worksheet.autoFitRows();
	        worksheet.setGridlinesVisible(false);

	        int maxdatacol = worksheet.getCells().getMaxDataColumn();
	        int maxdatarow = worksheet.getCells().getMaxDataRow();

	        String cellname = CellsHelper.cellIndexToName(maxdatarow, maxdatacol);

	        Range range;
	        if (!cellname.equalsIgnoreCase("G2")) {
	            range = worksheet.getCells().createRange("A3:" + cellname);
	            Style style = workbook.createStyle();
	            style.setBorder(BorderType.TOP_BORDER, CellBorderType.THIN, Color.getBlack());
	            style.setBorder(BorderType.BOTTOM_BORDER, CellBorderType.THIN, Color.getBlack());
	            style.setBorder(BorderType.LEFT_BORDER, CellBorderType.THIN, Color.getBlack());
	            style.setBorder(BorderType.RIGHT_BORDER, CellBorderType.THIN, Color.getBlack());

	            Iterator<?> cellArray = range.iterator();
	            while (cellArray.hasNext()) {
	                Cell temp = (Cell) cellArray.next();
	                // Saving the modified style to the cell.
	                temp.setStyle(style);
	            }
	        } else {
	            range = worksheet.getCells().createRange("A1:" + cellname);
	            range.setOutlineBorders(CellBorderType.THIN, Color.getBlack());
	        }

	        // Creating AutoFilter by giving the cells range
	        AutoFilter autoFilter = worksheet.getAutoFilter();
	        autoFilter.setRange("A2:G2");

	        return workbook;
	    }
	 
	 public ShareholderMasterNonResidential getNonResidentShareHolderById(Integer id) {
		 return shareholderMasterNonResidentialDAO
					.findById(id).get(0);
	 }
	 
	

}
