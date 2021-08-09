package com.ey.in.tds.onboarding.service.deductor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ey.in.tds.common.domain.masters.DividendOnboardingInfoMetaData.ClientSpecificRule;
import com.ey.in.tds.common.domain.masters.DividendOnboardingInfoMetaData.RuleApplicability;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorOnboardingInformationDTO;
import com.ey.in.tds.common.onboarding.response.dto.DeductorOnboardingInformationResponseDTO;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.jdbc.dao.DeductorMasterDAO;
import com.ey.in.tds.jdbc.dao.DeductorOnboardingInfoDAO;
import com.ey.in.tds.onboarding.dto.deductor.ConfigValues;
import com.ey.in.tds.onboarding.dto.deductor.DeductorOnboardingInfoDTO;
import com.ey.in.tds.onboarding.dto.deductor.OnboardingConfigValuesDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DeductorOnBoardingService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private DeductorOnboardingInfoDAO deductorOnboardingInfoDAO;
	
	@Autowired
	private DeductorMasterDAO deductorMasterDAO;

	ObjectMapper objectMapper = new ObjectMapper();

	@Transactional
	public DeductorOnboardingInfoDTO createDeductorOnboardingInfo(DeductorOnboardingInfoDTO deductorOnboardingInfoDTO,
			String tenantId) throws JsonProcessingException {
		DeductorOnboardingInformationDTO deductorInfo = new DeductorOnboardingInformationDTO();
		List<DeductorOnboardingInformationDTO> deductorOnboardingInformation = deductorOnboardingInfoDAO
				.findByDeductorPan(deductorOnboardingInfoDTO.getPan());
		if (!deductorOnboardingInformation.isEmpty() && deductorOnboardingInformation != null) {
			deductorOnboardingInformation.get(0).setActive(false);
			deductorOnboardingInfoDAO.update(deductorOnboardingInformation.get(0));
		}
		OnboardingConfigValuesDTO deductorConfig = deductorOnboardingInfoDTO.getOnboardingConfigValues();
		deductorInfo.setPan(deductorOnboardingInfoDTO.getPan());
		deductorInfo.setCrpt(deductorConfig.getCnp());
		String ipp = objectMapper.writeValueAsString(deductorConfig.getIpp());
		String ppa = objectMapper.writeValueAsString(deductorConfig.getPpa());
		String tif = objectMapper.writeValueAsString(deductorConfig.getTif());
		String accountNumber = objectMapper.writeValueAsString(deductorConfig.getAccountNumber());
		deductorInfo.setAccountNumber(accountNumber);
		deductorInfo.setIpp(ipp);
		deductorInfo.setPpa(ppa);
		deductorInfo.setTif(tif);
		deductorInfo.setCp(deductorConfig.getCp());
		deductorInfo.setProvisionProcessing(deductorConfig.getProvisionProcessing());
		deductorInfo.setProvisionTracking(deductorConfig.getProvisionTracking());
		deductorInfo.setActive(true);
		deductorInfo.setCreatedDate(new Date());
		deductorInfo.setModifiedDate(new Date());
		deductorInfo.setCreatedBy(tenantId);
		deductorInfo.setModifiedBy(tenantId);
		deductorInfo.setApplicableFrom(deductorConfig.getApplicableFrom());
		deductorInfo.setApplicableTo(deductorConfig.getApplicableTo());
		deductorInfo.setRoundoff(deductorConfig.getRoundoff());
		deductorInfo.setPertransactionlimit(deductorConfig.getPertransactionlimit());
		deductorInfo.setSelectedSectionsForTransactionLimit(deductorConfig.getSelectedSectionsForTransactionLimit());
		deductorInfo.setInterestCalculationType(deductorConfig.getInterestCalculationType());
		deductorInfo.setDvndEnabled(deductorConfig.getDvndEnabled());
		deductorInfo.setDvndPrepForm15CaCb(deductorConfig.getDvndPrepForm15CaCb());
		deductorInfo.setDvndDdtPaidBeforeEOY(deductorConfig.getDvndDdtPaidBeforeEOY());
		deductorInfo.setDvndFileForm15gh(deductorConfig.getDvndFileForm15gh());
		deductorInfo.setAdvanceProcessing(deductorConfig.getAdvanceProcessing());
		Map<String, String> map = new HashMap<>();
		for (ConfigValues configValuesDTO : deductorConfig.getPriority()) {
			map.put(configValuesDTO.getConfigCode(), configValuesDTO.getConfigValue());
		}

		// String priority = objectMapper.writeValueAsString(map);
		String priority = map.keySet().stream().map(key -> '"' + key + '"' + ":" + '"' + map.get(key) + '"')
				.collect(Collectors.joining(", ", "{", "}"));
		deductorInfo.setPriority(priority);

		// converting into custom format for spark process
		Map<ClientSpecificRule, RuleApplicability> dvdndClientSpecRule = deductorConfig.getDvndClientSpecificRules();
		if (dvdndClientSpecRule != null) {
			String clientSpecificRule = (String) dvdndClientSpecRule.keySet().stream()
					.map(key -> '"' + key.toString() + '"' + ":" + '"' + dvdndClientSpecRule.get(key) + '"')
					.collect(Collectors.joining(", ", "{", "}"));
			deductorInfo.setStringClientSpecificRules(clientSpecificRule);
		}

		try {
			deductorOnboardingInfoDAO.save(deductorInfo);
		} catch (Exception e) {
			logger.error("Error occured while saving record in Deductor Onboarding Info", e);
			throw new CustomException("Error occured while saving record in Deductor Onboarding Info",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return deductorOnboardingInfoDTO;
	}

	@Transactional
	public Optional<DeductorOnboardingInformationDTO> getDeductorOnboardingInfo(String deductorPan) {
		List<DeductorOnboardingInformationDTO> deductorOnboardingInformationObject = deductorOnboardingInfoDAO
				.findByDeductorPan(deductorPan);
		if (!deductorOnboardingInformationObject.isEmpty()) {
			return Optional.of(deductorOnboardingInformationObject.get(0));
		} else {
			return Optional.empty();
		}
	}

	/**
	 * 
	 * @param deductorInformation
	 * @param tenantId
	 * @return
	 * @throws JsonProcessingException
	 */
	public Map<String, String> getDeductorCustomJobs(Map<String, String> deductorInformation, String deductorPan)
			throws JsonProcessingException {
		List<DeductorOnboardingInformationDTO> deductorInfoObj = deductorOnboardingInfoDAO
				.findByDeductorPan(deductorPan);
		if (!deductorInfoObj.isEmpty() && deductorInfoObj != null) {
			DeductorOnboardingInformationDTO deductorOnboardingInformation = deductorInfoObj.get(0);
			String customJobs = objectMapper.writeValueAsString(deductorInformation);
			deductorOnboardingInformation.setCustomJobs(customJobs);
			deductorOnboardingInfoDAO.updateCustomJobs(deductorOnboardingInformation);
		}
		return deductorInformation;
	}

	/**
	 * 
	 * @param dto
	 * @return
	 * @throws JsonProcessingException 
	 * @throws JsonMappingException 
	 */
	public Optional<DeductorOnboardingInformationResponseDTO> copyToEntity(
			Optional<DeductorOnboardingInformationDTO> dto) throws JsonMappingException, JsonProcessingException {
		DeductorOnboardingInformationResponseDTO response = new DeductorOnboardingInformationResponseDTO();
		response.setPan(dto.get().getPan());
		String ipp = dto.get().getIpp();
		ipp = ipp.replace("[", "").replace("]", "");
		List<Integer> listipp = Stream.of(ipp.split(",")).map(Integer::parseInt).collect(Collectors.toList());
		response.setIpp(listipp);
		String ppa = dto.get().getPpa();
		if (StringUtils.isNotBlank(ppa)) {
			ppa = ppa.replace("[", "").replace("]", "");
			List<Integer> listPpa = Stream.of(ppa.split(",")).map(Integer::parseInt).collect(Collectors.toList());
			response.setPpa(listPpa);
		}
		response.setCnp(dto.get().getCrpt());
		String tifs = dto.get().getTif();
		tifs = tifs.replace("[", "").replace("]", "");
		List<Integer> listTif = Stream.of(tifs.split(",")).map(Integer::parseInt).collect(Collectors.toList());
		response.setTif(listTif);
		response.setCp(dto.get().getCp());
		if (dto.get().getAccountNumber() != null) {
			List<String> listAccount = new ArrayList<String>(Arrays.asList(dto.get().getAccountNumber().split(",")));
			response.setAccountNumber(listAccount);
		}
		response.setProvisionTracking(dto.get().getProvisionTracking());
		response.setProvisionProcessing(dto.get().getProvisionProcessing());
		response.setAdvanceProcessing(dto.get().getAdvanceProcessing());
		Map<String, String> mapPriority = null;
		if (StringUtils.isNotBlank(dto.get().getPriority())) {
			mapPriority = Arrays.stream(dto.get().getPriority().replace("\"","").replace("{", "").replace("}", "").split(","))
					.map(s -> s.split(":")).collect(Collectors.toMap(s -> s[0].trim(), s -> s[1].trim()));

		}
		response.setPriority(mapPriority);
		response.setActive(dto.get().getActive());
		Map<String, String> mapCustomJobs = null;
		if (StringUtils.isNotBlank(dto.get().getCustomJobs())) {
			mapCustomJobs = Arrays.stream(dto.get().getCustomJobs().split(",")).map(s -> s.split(":"))
					.collect(Collectors.toMap(s -> s[0].trim(), s -> s[1].trim()));
		}
		response.setCustomJobs(mapCustomJobs);
		response.setApplicableFrom(dto.get().getApplicableFrom());
		response.setApplicableTo(dto.get().getApplicableTo());
		response.setRoundoff(dto.get().getRoundoff());
		response.setPertransactionlimit(dto.get().getPertransactionlimit());
		response.setSelectedSectionsForTransactionLimit(dto.get().getSelectedSectionsForTransactionLimit());
		response.setInterestCalculationType(dto.get().getInterestCalculationType());
		response.setDvndDdtPaidBeforeEOY(dto.get().getDvndDdtPaidBeforeEOY());
		response.setDvndEnabled(dto.get().getDvndEnabled());
		response.setDvndFileForm15gh(dto.get().getDvndFileForm15gh());
		response.setDvndPrepForm15CaCb(dto.get().getDvndPrepForm15CaCb());
		
		if(dto.get().getDvndEnabled()) {
			response.setDvndDeductorTypeName(deductorMasterDAO.findBasedOnDeductorPan(dto.get().getPan()).get(0).getDvndDeductorTypeName());
		}
		
		
		//logic to convert string client specific rules into map
		if(StringUtils.isNotBlank(dto.get().getStringClientSpecificRules())) {
			HashMap<ClientSpecificRule, RuleApplicability> clientSpecificRule=new HashMap<>();
			if(dto.get().getStringClientSpecificRules().contains("=")){
				 String[] array=dto.get().getStringClientSpecificRules().replace("{", "").replace("}","").split(",");
				   for(int index=0;index<array.length;index++) {
					   String specificrule=array[index].split("=")[0].trim();
					   String[] ruleArray=array[index].split("=")[1].replace("[", "").replace("]", "").split("-");
					   boolean keyStrategicShareholders=ruleArray[0].split(":")[1].trim().equalsIgnoreCase("true")?true:false;
					   boolean allShareholders=ruleArray[1].split(":")[1].trim().equalsIgnoreCase("true")?true:false;
					   RuleApplicability ruleApplicability=new RuleApplicability(keyStrategicShareholders,allShareholders);	
					   clientSpecificRule.put(ClientSpecificRule.valueOf(specificrule),ruleApplicability);
			}
			}else {
			clientSpecificRule = new ObjectMapper().readValue(dto.get().getStringClientSpecificRules(), new TypeReference<HashMap<ClientSpecificRule, RuleApplicability>>() {});
					   
		   }
			response.setDvndClientSpecificRules(clientSpecificRule);
		}

		return Optional.of(response);
	}

	/**
	 * 
	 * @param deductorTan
	 * @return
	 */
	public List<DeductorOnboardingInformationDTO> getDeductorPan(String deductorPan) {
		return deductorOnboardingInfoDAO.findByDeductorPan(deductorPan);
	}

}
