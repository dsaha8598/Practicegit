package com.ey.in.tds.service;

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.ey.in.tds.common.domain.NatureOfPaymentMaster;
import com.ey.in.tds.common.domain.ThresholdGroupAndNopMapping;
import com.ey.in.tds.common.domain.ThresholdLimitGroupMaster;
import com.ey.in.tds.common.dto.CustomThresholdGroupLimit;
import com.ey.in.tds.common.dto.CustomThresholdGroupLimitInterface;
import com.ey.in.tds.common.dto.NatureDTO;
import com.ey.in.tds.common.repository.NatureOfPaymentMasterRepository;
import com.ey.in.tds.common.repository.ThresholdGroupAndNopMappingRepository;
import com.ey.in.tds.common.repository.ThresholdLimitGroupMasterRepository;
import com.ey.in.tds.core.exceptions.CustomException;

/**
 * 
 * @author vamsir
 *
 */
@Service
public class ThresholdLimitGroupMasterService {

	@Autowired
	private ThresholdLimitGroupMasterRepository thresholdLimitGroupMasterRepository;

	@Autowired
	private NatureOfPaymentMasterRepository natureOfPaymentMasterRepository;

	@Autowired
	private ThresholdGroupAndNopMappingRepository thresholdGroupAndNopMappingRepository;

	/**
	 * This method for save threshold group
	 * 
	 * @param thresholdGroupDto
	 * @param userName
	 * @return
	 */
	public CustomThresholdGroupLimit save(@Valid CustomThresholdGroupLimit thresholdGroupDto, String userName) {
		
		List<Map<String, Object>> mapDate =  thresholdGroupAndNopMappingRepository.getThresholdGroupData();
		Set<String> groupNameList = new HashSet<>();
		Set<BigInteger> nopIdList = new HashSet<>();
		if (!mapDate.isEmpty()) {
			for (Map<String, Object> map : mapDate) {
				groupNameList.add((String) map.get("groupName"));
				nopIdList.add((BigInteger) map.get("nopId"));
			}
		}
		
		ThresholdLimitGroupMaster thresholdLimit = new ThresholdLimitGroupMaster();
		thresholdLimit.setActive(true);
		thresholdLimit.setCreatedBy(userName);
		thresholdLimit.setCreatedDate(Instant.now());
		if(!groupNameList.isEmpty() && !groupNameList.contains(thresholdGroupDto.getGroupName())) {
			thresholdLimit.setGroupName(thresholdGroupDto.getGroupName());
		} else {
			throw new CustomException("Duplicate group name not allowed ", HttpStatus.BAD_REQUEST);
		}
		thresholdLimit.setThresholdAmount(thresholdGroupDto.getThresholdAmount());
		thresholdLimit.setApplicableFrom(thresholdGroupDto.getApplicableFrom());
		thresholdLimit.setApplicableTo(thresholdGroupDto.getApplicableTo());
		thresholdLimit.setTaxApplicabilityAfterThreshold(thresholdGroupDto.getTaxApplicability());
		List<ThresholdGroupAndNopMapping> groupNopList = new ArrayList<>();
		for (NatureDTO nature : thresholdGroupDto.getNature()) {
			Optional<NatureOfPaymentMaster> naturePaymentMasterOptional = natureOfPaymentMasterRepository
					.findById(nature.getNatureId());
			if (naturePaymentMasterOptional.isPresent()) {
				ThresholdGroupAndNopMapping groupNop = new ThresholdGroupAndNopMapping();
				NatureOfPaymentMaster nop = new NatureOfPaymentMaster();
				groupNop.setActive(true);
				groupNop.setCreatedBy(userName);
				groupNop.setCreatedDate(Instant.now());
				if(!nopIdList.isEmpty() && !nopIdList.contains(BigInteger.valueOf(nature.getNatureId()))) {
					nop.setId(nature.getNatureId());
				} else {
					throw new CustomException("Duplicate nature of payment not allowed ", HttpStatus.BAD_REQUEST);
				}
				groupNop.setThresholdLimitGroupMaster(thresholdLimit);
				groupNop.setNatureOfPaymentMaster(nop);
				groupNopList.add(groupNop);
			}
		}
		thresholdLimit.setThresholdGroupAndNopMapping(groupNopList);
		thresholdLimitGroupMasterRepository.save(thresholdLimit);

		return thresholdGroupDto;

	}

	/**
	 * This method for get all threshold group
	 * 
	 * @return
	 */
	public List<CustomThresholdGroupLimitInterface> getAllThresholdGroup() {
		List<CustomThresholdGroupLimitInterface> thresholdGroupList = thresholdLimitGroupMasterRepository
				.getThresholdLimitGroup();
		return thresholdGroupList;
	}

	public List<CustomThresholdGroupLimitInterface> getThresholdLimitGroupByNopId(Long nopId) {
		List<CustomThresholdGroupLimitInterface> thresholdGroupList = thresholdLimitGroupMasterRepository
				.getThresholdLimitGroupByNopId(nopId);
		return thresholdGroupList;
	}

	/**
	 * This method for get threshold group based on id
	 * 
	 * @param id
	 * @return
	 */
	public CustomThresholdGroupLimit getById(Long id) {
		Optional<ThresholdLimitGroupMaster> thresholdGroupOptional = thresholdLimitGroupMasterRepository.findById(id);
		CustomThresholdGroupLimit customDto = new CustomThresholdGroupLimit();
		if (thresholdGroupOptional.isPresent()) {
			customDto.setGroupName(thresholdGroupOptional.get().getGroupName());
			customDto.setApplicableFrom(thresholdGroupOptional.get().getApplicableFrom());
			customDto.setApplicableTo(thresholdGroupOptional.get().getApplicableTo());
			customDto.setId(thresholdGroupOptional.get().getId());
			customDto.setThresholdAmount(thresholdGroupOptional.get().getThresholdAmount());
			customDto.setTaxApplicability(thresholdGroupOptional.get().getTaxApplicabilityAfterThreshold());
			List<ThresholdGroupAndNopMapping> thresholdMappingList = thresholdGroupAndNopMappingRepository
					.findByGroupId(id);
			List<NatureDTO> natureList = new ArrayList<>();
			if (!thresholdMappingList.isEmpty()) {
				for (ThresholdGroupAndNopMapping threshold : thresholdMappingList) {
					NatureDTO natureDTO = new NatureDTO();
					natureDTO.setNatureId(threshold.getNatureOfPaymentMaster().getId());
					natureDTO.setNature(threshold.getNatureOfPaymentMaster().getNature());
					natureList.add(natureDTO);
				}
			}
			customDto.setNature(natureList);
		} else {
			throw new CustomException("Threshold limit group master id not found in db ", HttpStatus.BAD_REQUEST);
		}
		return customDto;

	}

	/**
	 * This method for update threshold group
	 * 
	 * @param thresholdGroup
	 * @param userName
	 * @return
	 */
	public CustomThresholdGroupLimit update(@Valid CustomThresholdGroupLimit thresholdGroup, String userName) {
		Optional<ThresholdLimitGroupMaster> thresholdGroupOptional = thresholdLimitGroupMasterRepository
				.findById(thresholdGroup.getId());
		ThresholdLimitGroupMaster thresholdDb = null;
		if (thresholdGroupOptional.isPresent()) {
			thresholdDb = thresholdGroupOptional.get();
			thresholdDb.setApplicableFrom(thresholdGroup.getApplicableFrom());
			thresholdDb.setApplicableTo(thresholdGroup.getApplicableTo());
			thresholdDb.setModifiedBy(userName);
			thresholdDb.setModifiedDate(Instant.now());
			thresholdLimitGroupMasterRepository.save(thresholdDb);
		}
		return thresholdGroup;
	}

	/**
	 * 
	 * @return
	 */
	public List<ThresholdLimitGroupMaster> findByGroupIds() {
		List<ThresholdLimitGroupMaster> thresholdMappingList = thresholdLimitGroupMasterRepository.findAll();
		return thresholdMappingList;
	}

	/**
	 * 
	 * @param natureid
	 * @return
	 */
	public Optional<ThresholdGroupAndNopMapping> getThresholdGroupId(Long natureid) {
		Optional<ThresholdGroupAndNopMapping> thresholdMappingList = thresholdGroupAndNopMappingRepository
				.fineNatureOfPaymentId(natureid);
		if (thresholdMappingList.isPresent()) {
			return thresholdMappingList;
		} else {
			return Optional.empty();
		}
	}

	public List<Map<String, Object>> getThresholdGroupData() {
		return thresholdGroupAndNopMappingRepository.getThresholdGroupData();
	}

	public List<ThresholdGroupAndNopMapping> getThresholdNopGroupData(Long nopId) {
		return thresholdGroupAndNopMappingRepository.findGroupAndNopMappingByNopId(nopId);
	}
	

}
