package com.ey.in.tds.onboarding.service.deductor;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
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

import com.ey.in.tds.common.domain.transactions.jdbc.dao.BatchUploadDAO;
import com.ey.in.tds.common.domain.transactions.jdbc.dto.BatchUpload;
import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterDTO;
import com.ey.in.tds.common.dto.masters.deductor.DeductorMasterEditDTO;
import com.ey.in.tds.common.dto.masters.deductor.PersonResponsibleDetails;
import com.ey.in.tds.common.dto.masters.deductor.TanAddressDTO;
import com.ey.in.tds.common.dto.masters.deductor.TanAndPersonResponsibleDTO;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorTanAddress;
import com.ey.in.tds.common.onboarding.jdbc.dto.RoleDTO;
import com.ey.in.tds.common.service.Sha256SumService;
import com.ey.in.tds.common.util.BlobStorage;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.core.util.UploadTypes;
import com.ey.in.tds.jdbc.dao.DeductorMasterDAO;
import com.ey.in.tds.jdbc.dao.DeductorTanAddressDAO;
import com.ey.in.tds.jdbc.dao.RoleDAO;
import com.ey.in.tds.onboarding.dto.role.DeductorDTO;
import com.ey.in.tds.onboarding.dto.role.RoleDto;
import com.ey.in.tds.onboarding.service.util.excel.DeductorExcel;
import com.ey.in.tds.onboarding.service.util.excel.deductee.DeductorMasterExcel;
import com.ey.in.tds.onboarding.service.util.excel.deductee.DeductorMasterScopeExcel;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.microsoft.azure.storage.StorageException;

/**
 * Service Implementation for managing DeductorMaster.
 */
@Service
public class DeductorMasterService {

	private final Logger logger = LoggerFactory.getLogger(DeductorMasterService.class);

	@Autowired
	private DeductorMasterDAO deductorMasterDAO;

	@Autowired
	private DeductorTanAddressDAO deductorTanAddressDAO;

	@Autowired
	private RoleDAO roleDAO;

	@Autowired
	private Sha256SumService sha256SumService;

	@Autowired
	private BatchUploadDAO batchUploadDAO;

	@Autowired
	private BlobStorage blob;
	
	@Autowired
	private DeducorMasterBulkService deducorMasterBulkService;

	/**
	 * Save a deductorMaster.
	 * 
	 * @param userName
	 *
	 * @param deductorMaster the entity to save
	 * @return the persisted entity
	 * @throws JsonProcessingException
	 * @throws RecordNotFoundException
	 */
	@Transactional
	public DeductorMaster save(DeductorMasterDTO deductorMasterDTO, String userName) throws JsonProcessingException {
		logger.info("REST request to save DeductorMaster : {}", deductorMasterDTO);
		Map<String, Boolean> map = deductorMasterDTO.getScopeGroup();
		Map<String, Object> filter = new HashMap<>();
		for (TanAddressDTO tan : deductorMasterDTO.getTanList()) {
			if (filter.containsKey(tan.getTan())) {
				throw new CustomException("Cannot add Duplicate Tan ", HttpStatus.INTERNAL_SERVER_ERROR);
			} else {
				filter.put(tan.getTan(), tan);
			}
		}
		DeductorMaster deductorMaster = new DeductorMaster();
		deductorMaster.setCode(deductorMasterDTO.getDeductorCode());
		deductorMaster.setName(deductorMasterDTO.getDeductorName());
		deductorMaster.setApplicableFrom(deductorMasterDTO.getApplicableFrom());
		deductorMaster.setApplicableTo(deductorMasterDTO.getApplicableTo());
		deductorMaster.setEmail(deductorMasterDTO.getEmail());
		deductorMaster.setPhoneNumber(deductorMasterDTO.getPhoneNumber());
		deductorMaster.setCreatedBy(userName);
		deductorMaster.setModifiedBy(userName);
		deductorMaster.setCreatedDate(new Date());
		deductorMaster.setModifiedDate(new Date());
		deductorMaster.setActive(true);
		deductorMaster.setHaveMoreThanOneBranch(deductorMasterDTO.getDoesDeductorHasMorethanOneBranch());
		deductorMaster.setResidentialStatus(deductorMasterDTO.getResidentialStatus());
		deductorMaster.setStatus(deductorMasterDTO.getStatus());
		deductorMaster.setType(deductorMasterDTO.getDeductorTypeName());
		deductorMaster.setModeOfPayment(deductorMasterDTO.getModeOfPaymentType());
		deductorMaster.setDueDateOfTaxPayment(deductorMasterDTO.getDueDateOfTaxPayment());
		deductorMaster.setEmailAlternate(deductorMasterDTO.getEmailAlternate());
		deductorMaster.setPhoneNumberAlternate(deductorMasterDTO.getPhoneNumberAlternate());
		deductorMaster.setGstin(deductorMasterDTO.getGstin());
		deductorMaster.setDvndDeductorTypeName(deductorMasterDTO.getDvndDeductorTypeName());
		deductorMaster.setDeductorSalutation(deductorMasterDTO.getDeductorSalutation());
		if (map != null) {
			if (map.get("Tds") == true && map.get("Tcs") == false) {
				deductorMaster.setModuleType("1");
			} else if (map.get("Tds") == false && map.get("Tcs") == true) {
				deductorMaster.setModuleType("2");
			} else {
				deductorMaster.setModuleType("1,2");
			}
		}

		List<DeductorMaster> deductorMasterObj = deductorMasterDAO
				.findBasedOnDeductorCode(deductorMasterDTO.getDeductorCode());
		if (!deductorMasterObj.isEmpty() && deductorMasterObj != null) {
			throw new CustomException("Cannot add Duplicate Deductor Code ", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		if (!deductorMasterDTO.getPan().matches("[A-Z]{5}[0-9]{4}[A-Z]{1}")) {
			throw new CustomException("Cannot add Invalid PAN ", HttpStatus.INTERNAL_SERVER_ERROR);
		} else {
			deductorMaster.setPanField(deductorMasterDTO.getPan());
		}
		try {
			deductorMasterDAO.insert(deductorMaster);
			for (TanAddressDTO tanAddressDTO : deductorMasterDTO.getTanList()) {
				List<DeductorTanAddress> deductorTanAddressList = deductorTanAddressDAO
						.findPanNameByTan(tanAddressDTO.getTan());
				boolean tanExists = false;
				if (!deductorTanAddressList.isEmpty()) {
					tanExists = deductorTanAddressList.stream().anyMatch(o -> o.getTan().equals(tanAddressDTO.getTan()));
				}
				if (tanExists == true) {
					throw new CustomException("Cannot add Duplicate Tan ", HttpStatus.INTERNAL_SERVER_ERROR);
				}
				DeductorTanAddress deductorTanAddress = new DeductorTanAddress();
				deductorTanAddress = getDeductorTanAddressDetails(tanAddressDTO.getPersonResponsibleDetails());
				BeanUtils.copyProperties(tanAddressDTO, deductorTanAddress);
				deductorTanAddress.setPan(deductorMasterDTO.getPan());
				deductorTanAddress.setTan(tanAddressDTO.getTan());
				deductorTanAddress.setActive(true);
				deductorTanAddress.setCreatedDate(new Date());
				deductorTanAddress.setModifiedDate(new Date());
				deductorTanAddress.setModifiedBy(userName);
				deductorTanAddress.setCreatedBy(userName);
				deductorTanAddress.setAccountantSalutation(tanAddressDTO.getAccountantSalutation());
				deductorTanAddressDAO.save(deductorTanAddress, deductorMaster.getDeductorMasterId());
			}
		} catch (Exception e) {
			logger.error("Error occured at Deductor MasterService", e);
		}
		return deductorMaster;
	}

	@Transactional(readOnly = true)
	public List<DeductorMasterDTO> findAll() throws JsonParseException, JsonMappingException, IOException {
		List<DeductorMaster> deductorMaster = deductorMasterDAO.findAll();

		List<DeductorMasterDTO> deductorMasterListDTO = new ArrayList<>();

		for (DeductorMaster deductorMaterExisting : deductorMaster) {
			Map<String, Boolean> map = new HashMap<>();
			DeductorMasterDTO deductorMasterDTO = new DeductorMasterDTO();
			deductorMasterDTO.setDeductorCode(deductorMaterExisting.getCode());
			deductorMasterDTO.setDeductorName(deductorMaterExisting.getName());
			deductorMasterDTO.setApplicableFrom(deductorMaterExisting.getApplicableFrom());
			deductorMasterDTO.setApplicableTo(deductorMaterExisting.getApplicableTo());
			deductorMasterDTO.setDoesDeductorHasMorethanOneBranch(deductorMaterExisting.getHaveMoreThanOneBranch());
			deductorMasterDTO.setDueDateOfTaxPayment(deductorMaterExisting.getDueDateOfTaxPayment());
			deductorMasterDTO.setEmail(deductorMaterExisting.getEmail());
			deductorMasterDTO.setPhoneNumber(deductorMaterExisting.getPhoneNumber());
			deductorMasterDTO.setActive(deductorMaterExisting.getActive());
			deductorMasterDTO.setPan(deductorMaterExisting.getPanField());
			deductorMasterDTO.setResidentialStatus(deductorMaterExisting.getResidentialStatus());
			deductorMasterDTO.setStatus(deductorMasterDTO.getStatus());
			deductorMasterDTO.setDeductorTypeName(deductorMaterExisting.getType());
			deductorMasterDTO.setModeOfPaymentType(deductorMaterExisting.getModeOfPayment());
			deductorMasterDTO.setDueDateOfTaxPayment(deductorMaterExisting.getDueDateOfTaxPayment());
			deductorMasterDTO.setGstin(deductorMaterExisting.getGstin());
			deductorMasterDTO.setEmailAlternate(deductorMaterExisting.getEmailAlternate());
			deductorMasterDTO.setPhoneNumberAlternate(deductorMaterExisting.getPhoneNumberAlternate());
			deductorMasterDTO.setDeductorSalutation(deductorMaterExisting.getDeductorSalutation());
			if (deductorMaterExisting.getModuleType() != null) {
				if (deductorMaterExisting.getModuleType() != null) {
					if (deductorMaterExisting.getModuleType().equals("1")) {
						map.put("Tds", true);
						map.put("Tcs", false);
					} else if (deductorMaterExisting.getModuleType().equals("2")) {
						map.put("Tds", false);
						map.put("Tcs", true);
					} else {
						map.put("Tds", true);
						map.put("Tcs", true);
					}
				}
			}
			deductorMasterDTO.setScopeGroup(map);
			Set<TanAddressDTO> tanAddressList = new LinkedHashSet<>();
			List<DeductorTanAddress> deductorTanAddressList = deductorTanAddressDAO
					.findByDeductorPan(deductorMaterExisting.getPanField());
			for (DeductorTanAddress deductorTanAddress : deductorTanAddressList) {
				TanAddressDTO tanAddressDTO = new TanAddressDTO();
				PersonResponsibleDetails personResponsibleDetailsDTO = new PersonResponsibleDetails();
				personResponsibleDetailsDTO = getPersonResponsibleDetails(deductorTanAddress);
				BeanUtils.copyProperties(deductorTanAddress, tanAddressDTO);
				tanAddressDTO.setTan(deductorTanAddress.getTan());
				tanAddressDTO.setPersonResponsibleDetails(personResponsibleDetailsDTO);
				tanAddressList.add(tanAddressDTO);
			}
			deductorMasterDTO.setTanList(tanAddressList);
			deductorMasterListDTO.add(deductorMasterDTO);
		} // end of for each Deductor Master

		logger.info("REST response to get a list of DeductorMasters : {} ", deductorMasterListDTO);

		return deductorMasterListDTO;
	}

	@Transactional(readOnly = true)
	public DeductorMasterDTO findOne(String pan) {
		logger.info("REST request id to get a Deductor Master : {}", pan);
		DeductorMasterDTO deductorMasterDTO = new DeductorMasterDTO();
		List<DeductorMaster> deductorMasterObj = deductorMasterDAO.findBasedOnDeductorPan(pan);
		if (!deductorMasterObj.isEmpty() && deductorMasterObj != null) {
			deductorMasterDTO.setApplicableFrom(deductorMasterObj.get(0).getApplicableFrom());
			deductorMasterDTO.setApplicableTo(deductorMasterObj.get(0).getApplicableTo());
			deductorMasterDTO.setDeductorCode(deductorMasterObj.get(0).getCode());
			deductorMasterDTO.setDeductorName(deductorMasterObj.get(0).getName());
			deductorMasterDTO.setPhoneNumber(deductorMasterObj.get(0).getPhoneNumber());
			deductorMasterDTO.setEmail(deductorMasterObj.get(0).getEmail());
			deductorMasterDTO.setDoesDeductorHasMorethanOneBranch(deductorMasterObj.get(0).getHaveMoreThanOneBranch());
			deductorMasterDTO.setDueDateOfTaxPayment(deductorMasterObj.get(0).getDueDateOfTaxPayment());
			deductorMasterDTO.setPan(deductorMasterObj.get(0).getPanField());
			deductorMasterDTO.setResidentialStatus(deductorMasterObj.get(0).getResidentialStatus());
			deductorMasterDTO.setStatus(deductorMasterObj.get(0).getStatus());
			deductorMasterDTO.setDeductorTypeName(deductorMasterObj.get(0).getType());
			deductorMasterDTO.setModeOfPaymentType(deductorMasterObj.get(0).getModeOfPayment());
			deductorMasterDTO.setDueDateOfTaxPayment(deductorMasterObj.get(0).getDueDateOfTaxPayment());
			Set<TanAddressDTO> tanAddressList = new LinkedHashSet<>();
			List<DeductorTanAddress> deductorTanAddressList = deductorTanAddressDAO
					.findByDeductorPan(deductorMasterObj.get(0).getPanField());
			for (DeductorTanAddress deductorTanAddress : deductorTanAddressList) {
				TanAddressDTO tanAddressDTO = new TanAddressDTO();
				PersonResponsibleDetails personResponsibleDetailsDTO = new PersonResponsibleDetails();
				personResponsibleDetailsDTO = getPersonResponsibleDetails(deductorTanAddress);
				BeanUtils.copyProperties(deductorTanAddress, tanAddressDTO);
				tanAddressDTO.setTan(deductorTanAddress.getTan());
				tanAddressDTO.setPersonResponsibleDetails(personResponsibleDetailsDTO);
				tanAddressList.add(tanAddressDTO);
			}
			deductorMasterDTO.setTanList(tanAddressList);
			logger.info("DEDUCTOR RECORD : {}", deductorMasterDTO);
		}
		return deductorMasterDTO;
	}

	@Transactional(readOnly = true)
	public DeductorMasterEditDTO findOneByPan(String pan) {
		logger.info("REST request id to get a Deductor Master : {}", pan);
		Map<String, Boolean> map = new HashMap<>();
		DeductorMasterEditDTO deductorMasterDTO = new DeductorMasterEditDTO();
		List<DeductorMaster> deductorMasterObj = deductorMasterDAO.findBasedOnDeductorPan(pan);
		if (!deductorMasterObj.isEmpty() && deductorMasterObj != null) {
			deductorMasterDTO.setApplicableFrom(deductorMasterObj.get(0).getApplicableFrom());
			deductorMasterDTO.setApplicableTo(deductorMasterObj.get(0).getApplicableTo());
			deductorMasterDTO.setActive(deductorMasterObj.get(0).getActive());
			deductorMasterDTO.setDeductorCode(deductorMasterObj.get(0).getCode());
			deductorMasterDTO.setDeductorName(deductorMasterObj.get(0).getName());
			deductorMasterDTO.setPhoneNumber(deductorMasterObj.get(0).getPhoneNumber());
			deductorMasterDTO.setEmail(deductorMasterObj.get(0).getEmail());
			deductorMasterDTO.setDoesDeductorHasMorethanOneBranch(deductorMasterObj.get(0).getHaveMoreThanOneBranch());
			deductorMasterDTO.setDueDateOfTaxPayment(deductorMasterObj.get(0).getDueDateOfTaxPayment());
			deductorMasterDTO.setPan(deductorMasterObj.get(0).getPanField());
			deductorMasterDTO.setResidentialStatus(deductorMasterObj.get(0).getResidentialStatus());
			deductorMasterDTO.setStatus(deductorMasterObj.get(0).getStatus());
			deductorMasterDTO.setDeductorTypeName(deductorMasterObj.get(0).getType());
			deductorMasterDTO.setModeOfPaymentType(deductorMasterObj.get(0).getModeOfPayment());
			deductorMasterDTO.setDueDateOfTaxPayment(deductorMasterObj.get(0).getDueDateOfTaxPayment());
			deductorMasterDTO.setGstin(deductorMasterObj.get(0).getGstin());
			deductorMasterDTO.setDvndDeductorTypeName(deductorMasterObj.get(0).getDvndDeductorTypeName());
			deductorMasterDTO.setDeductorSalutation(deductorMasterObj.get(0).getDeductorSalutation());

			if (deductorMasterObj.get(0).getEmailAlternate() != null) {
				deductorMasterDTO.setEmailAlternate(deductorMasterObj.get(0).getEmailAlternate());
			}
			deductorMasterDTO.setPhoneNumberAlternate(deductorMasterObj.get(0).getPhoneNumberAlternate());
			if (deductorMasterObj.get(0).getModuleType() != null) {
				if (deductorMasterObj.get(0).getModuleType().equals("1")) {
					map.put("Tds", true);
					map.put("Tcs", false);
				} else if (deductorMasterObj.get(0).getModuleType().equals("2")) {
					map.put("Tds", false);
					map.put("Tcs", true);
				} else if (deductorMasterObj.get(0).getModuleType().equals("1,2")) {
					map.put("Tds", true);
					map.put("Tcs", true);
				}
			}
			deductorMasterDTO.setScopeGroup(map);
			Set<TanAndPersonResponsibleDTO> tanAddressList = new LinkedHashSet<>();
			List<DeductorTanAddress> deductorTanAddressList = deductorTanAddressDAO
					.findByDeductorPan(deductorMasterObj.get(0).getPanField());
			for (DeductorTanAddress deductorTanAddress : deductorTanAddressList) {
				TanAndPersonResponsibleDTO tanAddressDTO = new TanAndPersonResponsibleDTO();
				PersonResponsibleDetails personResponsibleDetailsDTO = new PersonResponsibleDetails();
				personResponsibleDetailsDTO = getPersonResponsibleDetails(deductorTanAddress);
				BeanUtils.copyProperties(deductorTanAddress, tanAddressDTO);
				tanAddressDTO.setTan(deductorTanAddress.getTan());
				tanAddressDTO.setStdCode(deductorTanAddress.getStdCode());
				tanAddressDTO.setPersonstdCode(deductorTanAddress.getPersonStdCode());
				tanAddressDTO.setAccountantSalutation(deductorTanAddress.getAccountantSalutation());
				BeanUtils.copyProperties(personResponsibleDetailsDTO, tanAddressDTO);
				tanAddressList.add(tanAddressDTO);
			}
			deductorMasterDTO.setTanList(tanAddressList);
			logger.info("DEDUCTOR RECORD : {}", deductorMasterDTO);
		}
		return deductorMasterDTO;
	}

	public DeductorMasterEditDTO updateDeductorMaster(@Valid DeductorMasterEditDTO deductorMasterDTO, String userName)
			throws JsonProcessingException {
		logger.info("REST request to update DeductorMaster : {}", deductorMasterDTO);

		DeductorMaster deductorMaster = new DeductorMaster();
		Map<String, Boolean> map = null;
		Map<String, TanAndPersonResponsibleDTO> filter = new HashMap<>();
		for (TanAndPersonResponsibleDTO tan : deductorMasterDTO.getTanList()) {
			if (filter.containsKey(tan.getTan())) {
				throw new CustomException("Cannot add Duplicate Tan ", HttpStatus.INTERNAL_SERVER_ERROR);
			} else {
				filter.put(tan.getTan(), tan);
			}
		}
		List<DeductorMaster> deductorMasterObj = deductorMasterDAO.findBasedOnDeductorPan(deductorMasterDTO.getPan());
		if (!deductorMasterObj.isEmpty()) {
			deductorMaster.setModifiedBy(userName);
			deductorMaster.setModifiedDate(new Date());
			deductorMaster.setResidentialStatus(deductorMasterDTO.getResidentialStatus());
			deductorMaster.setStatus(deductorMasterDTO.getStatus());
			deductorMaster.setPanField(deductorMasterDTO.getPan());
			deductorMaster.setDueDateOfTaxPayment(deductorMasterDTO.getDueDateOfTaxPayment());
			deductorMaster.setEmail(deductorMasterDTO.getEmail());
			deductorMaster.setPhoneNumber(deductorMasterDTO.getPhoneNumber());
			deductorMaster.setCode(deductorMasterDTO.getDeductorCode());
			deductorMaster.setName(deductorMasterDTO.getDeductorName());
			deductorMaster.setApplicableFrom(deductorMasterDTO.getApplicableFrom());
			deductorMaster.setHaveMoreThanOneBranch(deductorMasterDTO.getDoesDeductorHasMorethanOneBranch());
			deductorMaster.setApplicableTo(deductorMasterDTO.getApplicableTo());
			deductorMaster.setResidentialStatus(deductorMasterDTO.getResidentialStatus());
			deductorMaster.setStatus(deductorMasterDTO.getStatus());
			deductorMaster.setType(deductorMasterDTO.getDeductorTypeName());
			deductorMaster.setModeOfPayment(deductorMasterDTO.getModeOfPaymentType());
			deductorMaster.setDueDateOfTaxPayment(deductorMasterDTO.getDueDateOfTaxPayment());
			deductorMaster.setGstin(deductorMasterDTO.getGstin());
			deductorMaster.setEmailAlternate(deductorMasterDTO.getEmailAlternate());
			deductorMaster.setPhoneNumberAlternate(deductorMasterDTO.getPhoneNumberAlternate());
			map = deductorMasterDTO.getScopeGroup();
			if (map != null) {
				if (map.get("Tds") == true && map.get("Tcs") == false) {
					deductorMaster.setModuleType("1");
				} else if (map.get("Tds") == false && map.get("Tcs") == true) {
					deductorMaster.setModuleType("2");
				} else {
					deductorMaster.setModuleType("1,2");
				}
			}
			for (TanAndPersonResponsibleDTO tanAddressDTO : deductorMasterDTO.getTanList()) {
				DeductorTanAddress deductorTanAddress = new DeductorTanAddress();

				BeanUtils.copyProperties(tanAddressDTO, deductorTanAddress);

				deductorTanAddress.setPersonName(tanAddressDTO.getName());
				deductorTanAddress.setPersonPan(tanAddressDTO.getPan());
				deductorTanAddress.setPersonDesignation(tanAddressDTO.getDesignation());
				deductorTanAddress.setPersonFlatDoorBlockNo(tanAddressDTO.getPersonflatDoorBlockNo());
				deductorTanAddress.setPersonBuildingName(tanAddressDTO.getBuildingName());
				deductorTanAddress.setPersonStreetName(tanAddressDTO.getStreetName());
				deductorTanAddress.setPersonArea(tanAddressDTO.getArea());
				deductorTanAddress.setPersonCity(tanAddressDTO.getCity());
				deductorTanAddress.setPersonState(tanAddressDTO.getState());
				deductorTanAddress.setPersonPinCode(tanAddressDTO.getPersonpinCode());
				deductorTanAddress.setPersonTelephone(tanAddressDTO.getTelephone());
				deductorTanAddress.setPersonAlternateTelephone(tanAddressDTO.getAlternateTelephone());
				deductorTanAddress.setPersonMobileNumber(tanAddressDTO.getMobilenumber());
				deductorTanAddress.setPersonEmail(tanAddressDTO.getEmail());
				deductorTanAddress.setPersonAlternateEmail(tanAddressDTO.getAlternateEmail());
				deductorTanAddress.setPersonAddressChange(tanAddressDTO.getPersonAddressChange());

				deductorTanAddress.setPan(deductorMasterDTO.getPan());
				deductorTanAddress.setTan(tanAddressDTO.getTan());
				deductorTanAddress.setStdCode(tanAddressDTO.getStdCode());
				deductorTanAddress.setPersonStdCode(tanAddressDTO.getPersonstdCode());

				deductorTanAddress.setDvndOptedFor15CaCb(tanAddressDTO.getDvndOptedFor15CaCb());
				deductorTanAddress.setDvndAccountantName(tanAddressDTO.getDvndAccountantName());
				deductorTanAddress.setDvndAreaLocality(tanAddressDTO.getDvndAreaLocality());
				deductorTanAddress.setDvndBranchOfBank(tanAddressDTO.getDvndBranchOfBank());
				deductorTanAddress.setDvndBsrCodeOfBankBranch(tanAddressDTO.getDvndBsrCodeOfBankBranch());
				deductorTanAddress.setDvndCountry(tanAddressDTO.getDvndCountry());
				deductorTanAddress.setDvndFatherOrHusbandName(tanAddressDTO.getDvndFatherOrHusbandName());
				deductorTanAddress.setDvndFlatDoorBlockNo(tanAddressDTO.getDvndFlatDoorBlockNo());
				deductorTanAddress.setDvndMembershipNumber(tanAddressDTO.getDvndMembershipNumber());
				deductorTanAddress.setDvndNameOfBank(tanAddressDTO.getDvndNameOfBank());
				deductorTanAddress
						.setDvndNameOfPremisesBuildingVillage(tanAddressDTO.getDvndNameOfPremisesBuildingVillage());
				deductorTanAddress.setDvndNameOfProprietorship(tanAddressDTO.getDvndNameOfProprietorship());
				deductorTanAddress.setDvndPinCode(tanAddressDTO.getDvndPinCode());
				deductorTanAddress.setDvndPrincipalAreaOfBusiness(tanAddressDTO.getDvndPrincipalAreaOfBusiness());
				deductorTanAddress.setDvndRegistrationNumber(tanAddressDTO.getDvndRegistrationNumber());
				deductorTanAddress.setDvndRoadStreetPostOffice(tanAddressDTO.getDvndRoadStreetPostOffice());
				deductorTanAddress.setDvndState(tanAddressDTO.getDvndState());
				deductorTanAddress.setDvndTownCityDistrict(tanAddressDTO.getDvndTownCityDistrict());
				List<DeductorTanAddress> deductorTanAddressObj = deductorTanAddressDAO
						.findByPanAndTan(deductorTanAddress.getPan(), deductorTanAddress.getTan());
				if (!deductorTanAddressObj.isEmpty()) {
					deductorTanAddressDAO.update(deductorTanAddress);
				} else {
					deductorTanAddressDAO.save(deductorTanAddress, deductorMaster.getDeductorMasterId());
				}
			}
			deductorMaster = deductorMasterDAO.update(deductorMaster, userName);
		} else {
			throw new CustomException("Requested deductor master entry is not found in system",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return deductorMasterDTO;
	}

	/**
	 * retrieves user data,roles and list of tans
	 * 
	 * @return
	 */
	public Map<String, Object> getDeductorTanList() {
		List<DeductorMaster> deductorList = deductorMasterDAO.findAll();
		Map<String, Object> map = new HashMap<>();
		for (DeductorMaster deductorMaster : deductorList) {
			Set<String> tans = new HashSet<>();
			// to carry list of map, which contains module type as a key and list of roles
			// as value
			// List<Map<String,List<RoleDto>>> roleData = new ArrayList<>();
			// to store the module type as key and roles as a list
			// Map<String,List<RoleDto>> rolesMap=new HashMap<>();
			List<RoleDto> tdsRoleData = new ArrayList<>();
			List<RoleDto> tcsRoleData = new ArrayList<>();

			List<DeductorTanAddress> deductorTanAddressList = deductorTanAddressDAO
					.findByDeductorPan(deductorMaster.getPanField());
			for (DeductorTanAddress customTanDTO : deductorTanAddressList) {
				tans.add(customTanDTO.getTan());
			}
			List<RoleDTO> roles = roleDAO.fetchAllByDeductorPan(deductorMaster.getPanField());
			for (RoleDTO roleCassandra : roles) {
				RoleDto r = new RoleDto();
				r.setRoleId(roleCassandra.getRoleId());
				r.setRoleName(roleCassandra.getRoleName());
				if (roleCassandra.getModuleType() != null && roleCassandra.getModuleType() == 1) {
					tdsRoleData.add(r);
				}
				if (roleCassandra.getModuleType() != null && roleCassandra.getModuleType() == 2) {
					tcsRoleData.add(r);
				}
			} // for
				// rolesMap.put("TDS", tdsRoleData);
				// rolesMap.put("TCS", tcsRoleData);
			DeductorDTO deductorDTO = new DeductorDTO();
			deductorDTO.setTans(tans);
			deductorDTO.setRoles(tdsRoleData);
			deductorDTO.setRolesTcs(tcsRoleData);
			map.put(deductorMaster.getPanField(), deductorDTO);
		}
		return map;
	}

	public DeductorMasterDTO getDeductorByPan(String pan) {
		logger.info("REST request id to get a Deductor Master : {}", pan);
		DeductorMasterDTO deductorMasterDTO = new DeductorMasterDTO();
		List<DeductorMaster> deductorMasterObj = deductorMasterDAO.findBasedOnDeductorPan(pan);
		if (!deductorMasterObj.isEmpty() && deductorMasterObj != null) {
			deductorMasterDTO.setApplicableFrom(deductorMasterObj.get(0).getApplicableFrom());
			deductorMasterDTO.setApplicableTo(deductorMasterObj.get(0).getApplicableTo());
			deductorMasterDTO.setDeductorCode(deductorMasterObj.get(0).getCode());
			deductorMasterDTO.setDeductorName(deductorMasterObj.get(0).getName());
			deductorMasterDTO.setPhoneNumber(deductorMasterObj.get(0).getPhoneNumber());
			deductorMasterDTO.setEmail(deductorMasterObj.get(0).getEmail());
			deductorMasterDTO.setDoesDeductorHasMorethanOneBranch(deductorMasterObj.get(0).getHaveMoreThanOneBranch());
			deductorMasterDTO.setDueDateOfTaxPayment(deductorMasterObj.get(0).getDueDateOfTaxPayment());
			deductorMasterDTO.setPan(deductorMasterObj.get(0).getPanField());
			deductorMasterDTO.setResidentialStatus(deductorMasterObj.get(0).getResidentialStatus());
			deductorMasterDTO.setStatus(deductorMasterObj.get(0).getStatus());
			deductorMasterDTO.setDeductorTypeName(deductorMasterObj.get(0).getType());
			deductorMasterDTO.setModeOfPaymentType(deductorMasterObj.get(0).getModeOfPayment());
			deductorMasterDTO.setDueDateOfTaxPayment(deductorMasterObj.get(0).getDueDateOfTaxPayment());
			Set<TanAddressDTO> tanAddressList = new LinkedHashSet<>();
			List<DeductorTanAddress> deductorTanAddressList = deductorTanAddressDAO
					.findByDeductorPan(deductorMasterObj.get(0).getPanField());
			for (DeductorTanAddress deductorTanAddress : deductorTanAddressList) {
				TanAddressDTO tanAddressDTO = new TanAddressDTO();
				PersonResponsibleDetails personResponsibleDetailsDTO = new PersonResponsibleDetails();
				personResponsibleDetailsDTO = getPersonResponsibleDetails(deductorTanAddress);
				BeanUtils.copyProperties(deductorTanAddress, tanAddressDTO);
				tanAddressDTO.setTan(deductorTanAddress.getTan());
				tanAddressDTO.setPersonResponsibleDetails(personResponsibleDetailsDTO);
				tanAddressList.add(tanAddressDTO);
			}
			deductorMasterDTO.setTanList(tanAddressList);

			logger.info("DEDUCTOR RECORD : {}", deductorMasterDTO);
		}
		return deductorMasterDTO;
	}

	public DeductorTanAddress getDeductorTanAddressDetails(PersonResponsibleDetails personResponsibleDetails) {

		DeductorTanAddress deductorTanAddress = new DeductorTanAddress();
		deductorTanAddress.setPersonName(personResponsibleDetails.getName());
		deductorTanAddress.setPersonPan(personResponsibleDetails.getPan());
		deductorTanAddress.setPersonDesignation(personResponsibleDetails.getDesignation());
		deductorTanAddress.setPersonFlatDoorBlockNo(personResponsibleDetails.getPersonflatDoorBlockNo());
		deductorTanAddress.setPersonBuildingName(personResponsibleDetails.getBuildingName());
		deductorTanAddress.setPersonStreetName(personResponsibleDetails.getStreetName());
		deductorTanAddress.setPersonArea(personResponsibleDetails.getArea());
		deductorTanAddress.setPersonCity(personResponsibleDetails.getCity());
		deductorTanAddress.setPersonState(personResponsibleDetails.getState());
		deductorTanAddress.setPersonPinCode(personResponsibleDetails.getPersonpinCode());
		deductorTanAddress.setPersonTelephone(personResponsibleDetails.getTelephone());
		deductorTanAddress.setPersonAlternateTelephone(personResponsibleDetails.getAlternateTelephone());
		deductorTanAddress.setPersonMobileNumber(personResponsibleDetails.getMobilenumber());
		deductorTanAddress.setPersonEmail(personResponsibleDetails.getEmail());
		deductorTanAddress.setPersonAlternateEmail(personResponsibleDetails.getAlternateEmail());
		deductorTanAddress.setPersonAddressChange(personResponsibleDetails.getPersonAddressChange());
		deductorTanAddress.setStdCode(personResponsibleDetails.getStdCode());
		deductorTanAddress.setPersonStdCode(personResponsibleDetails.getPersonstdCode());

		deductorTanAddress.setDvndOptedFor15CaCb(personResponsibleDetails.getDvndOptedFor15CaCb());
		deductorTanAddress.setDvndAccountantName(personResponsibleDetails.getDvndAccountantName());
		deductorTanAddress.setDvndAreaLocality(personResponsibleDetails.getDvndAreaLocality());
		deductorTanAddress.setDvndBranchOfBank(personResponsibleDetails.getDvndBranchOfBank());
		deductorTanAddress.setDvndBsrCodeOfBankBranch(personResponsibleDetails.getDvndBsrCodeOfBankBranch());
		deductorTanAddress.setDvndCountry(personResponsibleDetails.getDvndCountry());
		deductorTanAddress.setDvndFatherOrHusbandName(personResponsibleDetails.getDvndFatherOrHusbandName());
		deductorTanAddress.setDvndFlatDoorBlockNo(personResponsibleDetails.getDvndFlatDoorBlockNo());
		deductorTanAddress.setDvndMembershipNumber(personResponsibleDetails.getDvndMembershipNumber());
		deductorTanAddress.setDvndNameOfBank(personResponsibleDetails.getDvndNameOfBank());
		deductorTanAddress
				.setDvndNameOfPremisesBuildingVillage(personResponsibleDetails.getDvndNameOfPremisesBuildingVillage());
		deductorTanAddress.setDvndNameOfProprietorship(personResponsibleDetails.getDvndNameOfProprietorship());
		deductorTanAddress.setDvndPinCode(personResponsibleDetails.getDvndPinCode());
		deductorTanAddress.setDvndPrincipalAreaOfBusiness(personResponsibleDetails.getDvndPrincipalAreaOfBusiness());
		deductorTanAddress.setDvndRegistrationNumber(personResponsibleDetails.getDvndRegistrationNumber());
		deductorTanAddress.setDvndRoadStreetPostOffice(personResponsibleDetails.getDvndRoadStreetPostOffice());
		deductorTanAddress.setDvndState(personResponsibleDetails.getDvndState());
		deductorTanAddress.setDvndTownCityDistrict(personResponsibleDetails.getDvndTownCityDistrict());
		return deductorTanAddress;

	}

	public PersonResponsibleDetails getPersonResponsibleDetails(DeductorTanAddress deductorTanAddress) {

		PersonResponsibleDetails personResponsibleDetails = new PersonResponsibleDetails();
		personResponsibleDetails.setName(deductorTanAddress.getPersonName());
		personResponsibleDetails.setPan(deductorTanAddress.getPersonPan());
		personResponsibleDetails.setDesignation(deductorTanAddress.getPersonDesignation());
		personResponsibleDetails.setPersonflatDoorBlockNo(deductorTanAddress.getPersonFlatDoorBlockNo());
		personResponsibleDetails.setBuildingName(deductorTanAddress.getPersonBuildingName());
		personResponsibleDetails.setStreetName(deductorTanAddress.getPersonStreetName());
		personResponsibleDetails.setArea(deductorTanAddress.getPersonArea());
		personResponsibleDetails.setCity(deductorTanAddress.getPersonCity());
		personResponsibleDetails.setState(deductorTanAddress.getPersonState());
		personResponsibleDetails.setPersonpinCode(deductorTanAddress.getPersonPinCode());
		personResponsibleDetails.setTelephone(deductorTanAddress.getPersonTelephone());
		personResponsibleDetails.setAlternateTelephone(deductorTanAddress.getPersonAlternateTelephone());
		personResponsibleDetails.setMobilenumber(deductorTanAddress.getPersonMobileNumber());
		personResponsibleDetails.setEmail(deductorTanAddress.getPersonEmail());
		personResponsibleDetails.setAlternateEmail(deductorTanAddress.getPersonAlternateEmail());
		personResponsibleDetails.setPersonAddressChange(deductorTanAddress.getPersonAddressChange());
		personResponsibleDetails.setStdCode(deductorTanAddress.getStdCode());
		personResponsibleDetails.setPersonstdCode(deductorTanAddress.getPersonStdCode());

		personResponsibleDetails.setDvndOptedFor15CaCb(deductorTanAddress.getDvndOptedFor15CaCb());
		personResponsibleDetails.setDvndAccountantName(deductorTanAddress.getDvndAccountantName());
		personResponsibleDetails.setDvndAreaLocality(deductorTanAddress.getDvndAreaLocality());
		personResponsibleDetails.setDvndBranchOfBank(deductorTanAddress.getDvndBranchOfBank());
		personResponsibleDetails.setDvndBsrCodeOfBankBranch(deductorTanAddress.getDvndBsrCodeOfBankBranch());
		personResponsibleDetails.setDvndCountry(deductorTanAddress.getDvndCountry());
		personResponsibleDetails.setDvndFatherOrHusbandName(deductorTanAddress.getDvndFatherOrHusbandName());
		personResponsibleDetails.setDvndFlatDoorBlockNo(deductorTanAddress.getDvndFlatDoorBlockNo());
		personResponsibleDetails.setDvndMembershipNumber(deductorTanAddress.getDvndMembershipNumber());
		personResponsibleDetails.setDvndNameOfBank(deductorTanAddress.getDvndNameOfBank());
		personResponsibleDetails
				.setDvndNameOfPremisesBuildingVillage(deductorTanAddress.getDvndNameOfPremisesBuildingVillage());
		personResponsibleDetails.setDvndNameOfProprietorship(deductorTanAddress.getDvndNameOfProprietorship());
		personResponsibleDetails.setDvndPinCode(deductorTanAddress.getDvndPinCode());
		personResponsibleDetails.setDvndPrincipalAreaOfBusiness(deductorTanAddress.getDvndPrincipalAreaOfBusiness());
		personResponsibleDetails.setDvndRegistrationNumber(deductorTanAddress.getDvndRegistrationNumber());
		personResponsibleDetails.setDvndRoadStreetPostOffice(deductorTanAddress.getDvndRoadStreetPostOffice());
		personResponsibleDetails.setDvndState(deductorTanAddress.getDvndState());
		personResponsibleDetails.setDvndTownCityDistrict(deductorTanAddress.getDvndTownCityDistrict());
		return personResponsibleDetails;

	}

	@Transactional
	public String getPanByReceiptTan(String receiptTan) {
		String pan = null;
		List<DeductorTanAddress> deductorTanAddressObj = deductorTanAddressDAO.findPanNameByTan(receiptTan);
		if (deductorTanAddressObj != null) {
			if (!StringUtils.isBlank(deductorTanAddressObj.get(0).getPan())) {
				String deductorPan = deductorTanAddressObj.get(0).getPan();
				List<DeductorMaster> deductorMaster = deductorMasterDAO.findBasedOnDeductorPan(deductorPan);
				if (!deductorMaster.isEmpty() && deductorMaster != null) {
					if (!StringUtils.isBlank(deductorMaster.get(0).getPanField())) {
						pan = deductorMaster.get(0).getPanField();
					}
				}
			}
		}
		return pan;
	}

	public Set<String> getDeductorsByPans(List<String> pans) {
		logger.info("REST request id to get a Deductor Master : {}", pans);
		Set<String> deductorPanNames = new HashSet<>();
		List<DeductorMaster> deductorRecordsList = new ArrayList<DeductorMaster>();
		for (String pan : pans) {
			List<DeductorMaster> listdeductorRecords = deductorMasterDAO.findAllDeductorsByPans(pan);
			if (!listdeductorRecords.isEmpty()) {
				deductorRecordsList.add(listdeductorRecords.get(0));
			}
		}
		if (!deductorRecordsList.isEmpty()) {
			for (DeductorMaster deductorMaster : deductorRecordsList) {
				deductorPanNames.add(deductorMaster.getPanField() + " - " + deductorMaster.getName());
			}
		}
		return deductorPanNames;
	}

	@Transactional(readOnly = true)
	public Map<String, Object> findByPan(String pan) {
		logger.info("REST request id to get a Deductor Master : {}", pan);
		Map<String, Object> map = new HashMap<>();
		DeductorMasterEditDTO deductorMasterDTO = new DeductorMasterEditDTO();
		List<DeductorMaster> deductorMasterObj = deductorMasterDAO.findBasedOnDeductorPan(pan);
		if (!deductorMasterObj.isEmpty()) {
			if (deductorMasterObj.get(0).getModuleType().equals("1")) {
				map.put("hasTds", true);
				map.put("hasTcs", false);
			} else if (deductorMasterObj.get(0).getModuleType().equals("2")) {
				map.put("hasTds", false);
				map.put("hasTcs", true);
			} else if (deductorMasterObj.get(0).getModuleType().equals("1,2")) {
				map.put("hasTds", true);
				map.put("hasTcs", true);
			}
			logger.info("DEDUCTOR RECORD : {}", deductorMasterDTO);
		}
		return map;
	}

	/**
	 * checks and returns the pan status
	 * 
	 * @param pan
	 * @return
	 */
	public String getPanStatus(String pan) {
		logger.info("Making the DB call to get the pan status{}");
		String status = "";
		List<DeductorMaster> list = deductorMasterDAO.findBasedOnDeductorPan(pan);
		if (!list.isEmpty()) {
			status = "Duplicate";
		} else {
			status = "Unique";
		}
		logger.info("Status of the pan is {}" + status);
		return status;
	}

	/**
	 * 
	 * @param tan
	 * @return
	 */
	public List<DeductorTanAddress> getDeductorBasedOnTan(String tan) {
		List<DeductorTanAddress> listTanAddress = new ArrayList<>();
		listTanAddress = deductorTanAddressDAO.findPanNameByTan(tan);
		if (!listTanAddress.isEmpty()) {
			return listTanAddress;
		}
		return listTanAddress;
	}

	/**
	 * 
	 * @param pan
	 * @param tenantId
	 * @return
	 */
	public List<DeductorTanAddress> getDeductorBasedOnPan(String pan, String tenantId) {
		List<DeductorTanAddress> listTanAddress = new ArrayList<>();
		listTanAddress = deductorTanAddressDAO.findByDeductorPan(pan);
		if (!listTanAddress.isEmpty()) {
			return listTanAddress;
		}
		return listTanAddress;
	}

	/**
	 * 
	 * @param file
	 * @param tan
	 * @param assesssmentYear
	 * @param assessmentMonth
	 * @param userName
	 * @param tenantId
	 * @param pan
	 * @return
	 * @throws StorageException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws InvalidKeyException
	 */
	@Transactional
	public BatchUpload saveFileData(MultipartFile multiPartFile, String deductorTan, Integer assesssmentYear,
			Integer assessmentMonth, String userName, String tenantId, String deductorPan)
			throws InvalidKeyException, IOException, URISyntaxException, StorageException {

		String sha256 = sha256SumService.getSHA256Hash(multiPartFile);

		if (isAlreadyProcessed(sha256)) {
			BatchUpload batchUpload = new BatchUpload();
			batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
			batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
			batchUpload.setCreatedBy(userName);
			batchUpload.setSuccessCount(0L);
			batchUpload.setFailedCount(0L);
			batchUpload.setRowsCount(0L);
			batchUpload.setProcessedCount(0);
			batchUpload.setMismatchCount(0L);
			batchUpload.setSha256sum(sha256);
			batchUpload.setStatus("Duplicate");
			batchUpload.setNewStatus("Duplicate");
			batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
			batchUpload = deductorBatchUpload(batchUpload, multiPartFile, deductorTan, assesssmentYear, assessmentMonth,
					userName, null, tenantId);
			return batchUpload;
		}

		try (XSSFWorkbook workbook = new XSSFWorkbook(multiPartFile.getInputStream());) {
			// sheet 1
			XSSFSheet deductorMasterSheet = workbook.getSheetAt(0);
			XSSFRow deductorMasterHeaderRow = deductorMasterSheet.getRow(2);
			int deductorMasterHeaderCount = DeductorExcel.getNonEmptyCellsCount(deductorMasterHeaderRow);
			// sheet 2
			XSSFSheet scopeSheet = workbook.getSheetAt(1);
			XSSFRow scopeHeaderRow = scopeSheet.getRow(2);
			int scopeHeaderCount = DeductorExcel.getNonEmptyCellsCount(scopeHeaderRow);
			logger.info("Header count of deductor master sheet=" + deductorMasterHeaderCount + " and scope sheet="
					+ scopeHeaderCount + "{}");

			logger.info("Column header count :{}", deductorMasterHeaderCount);
			BatchUpload batchUpload = new BatchUpload();
			if (deductorMasterHeaderCount != DeductorMasterExcel.fieldMappings.size()) {
				batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
				batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
				batchUpload.setSuccessCount(0L);
				batchUpload.setFailedCount(0L);
				batchUpload.setRowsCount(0L);
				batchUpload.setProcessedCount(0);
				batchUpload.setMismatchCount(0L);
				batchUpload.setSha256sum(sha256);
				batchUpload.setStatus("Failed");
				batchUpload.setCreatedBy(userName);
				batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
				return deductorBatchUpload(batchUpload, multiPartFile, deductorTan, assesssmentYear, assessmentMonth,
						userName, null, tenantId);
			}
			// sheet 2 header count
			logger.info("Column header count-2 :{}", scopeHeaderCount);
			if (scopeHeaderCount != DeductorMasterScopeExcel.fieldMappings.size()) {
				batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
				batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
				batchUpload.setSuccessCount(0L);
				batchUpload.setFailedCount(0L);
				batchUpload.setRowsCount(0L);
				batchUpload.setProcessedCount(0);
				batchUpload.setMismatchCount(0L);
				batchUpload.setSha256sum(sha256);
				batchUpload.setStatus("Failed");
				batchUpload.setCreatedBy(userName);
				batchUpload.setProcessEndTime(new Timestamp(new Date().getTime()));
				return deductorBatchUpload(batchUpload, multiPartFile, deductorTan, assesssmentYear, assessmentMonth,
						userName, null, tenantId);
			} else {
				batchUpload.setCreatedDate(new Timestamp(new Date().getTime()));
				batchUpload.setProcessStartTime(new Timestamp(new Date().getTime()));
				batchUpload.setStatus("Processing");
				batchUpload.setSuccessCount(0L);
				batchUpload.setFailedCount(0L);
				batchUpload.setRowsCount(0L);
				batchUpload.setProcessedCount(0);
				batchUpload.setMismatchCount(0L);
				batchUpload = deductorBatchUpload(batchUpload, multiPartFile, deductorTan, assesssmentYear,
						assessmentMonth, userName, null, tenantId);
			}
			if (deductorMasterHeaderCount == DeductorMasterExcel.fieldMappings.size()
					&& scopeHeaderCount == DeductorMasterScopeExcel.fieldMappings.size()) {
				deducorMasterBulkService.asyncProcessDeductorMaster(deductorMasterSheet, scopeSheet, multiPartFile, sha256, deductorTan,
						assesssmentYear, assessmentMonth, userName, tenantId, deductorPan, batchUpload);
				return batchUpload;
			} else {
				throw new CustomException("Data not found ", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			logger.info("failed to deductor master data:{}", e.getMessage());
			throw new RuntimeException("Failed to process deductor master data ");
		}
	}

	
	

	

	

	/**
	 * 
	 * @param sha256Sum
	 * @return
	 */
	private boolean isAlreadyProcessed(String sha256Sum) {
		List<BatchUpload> sha256Record = batchUploadDAO.getSha256Records(sha256Sum);
		return sha256Record != null && !sha256Record.isEmpty();
	}

	/**
	 * 
	 * @param batchUpload
	 * @param mFile
	 * @param tan
	 * @param assesssmentYear
	 * @param assessmentMonthPlusOne
	 * @param userName
	 * @param file
	 * @param tenant
	 * @return
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws URISyntaxException
	 * @throws StorageException
	 */
	public BatchUpload deductorBatchUpload(BatchUpload batchUpload, MultipartFile mFile, String tan,
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
		batchUpload.setUploadType(UploadTypes.DEDUCTOR_MASTER_EXCEL.name());
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

	public String getDeductorNameBasedOnPan(String pan) {

		return deductorMasterDAO.getDeductorName(pan);
	}

	public DeductorTanAddress getDeductorTanAddress(String deductorTan, String deductorPan) {
		DeductorTanAddress deductorTanAddress = null;
		List<DeductorTanAddress> deductorTanAddressOptional = deductorTanAddressDAO.findByPanAndTan(deductorPan,
				deductorTan);
		if (!deductorTanAddressOptional.isEmpty()) {
			deductorTanAddress = deductorTanAddressOptional.get(0);
		}
		return deductorTanAddress;
	}

}