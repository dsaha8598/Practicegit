package com.ey.in.tds.onboarding.service.collector;

import java.util.Arrays;
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

import com.ey.in.tds.common.onboarding.jdbc.dto.CollectorOnBoardingInformationDTO;
import com.ey.in.tds.common.onboarding.response.dto.CollectorOnBoardingInformationResponseDTO;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.jdbc.dao.CollectorOnBoardingInfoDAO;
import com.ey.in.tds.onboarding.dto.collector.CollectorOnBoardingInfoDTO;
import com.ey.in.tds.onboarding.dto.collector.CollectorOnboardingConfigValuesDTO;
import com.ey.in.tds.onboarding.dto.deductor.ConfigValues;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CollectorOnBoardingService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private CollectorOnBoardingInfoDAO collectorOnBoardingInfoDAO;

	ObjectMapper objectMapper = new ObjectMapper();

	public CollectorOnBoardingInfoDTO createCollectorOnboardingInfo(
			CollectorOnBoardingInfoDTO collectorOnboardingInfoDTO, String tenantId) throws JsonProcessingException {
		CollectorOnBoardingInformationDTO collectorInfo = new CollectorOnBoardingInformationDTO();
		Boolean isCollectionReference = true;
		if(StringUtils.isBlank(collectorInfo.getCollectionReferenceId())) {
			isCollectionReference = false;
		}
		List<CollectorOnBoardingInformationDTO> collectorOnboardingInformation = collectorOnBoardingInfoDAO
				.findByCollectorPan(collectorOnboardingInfoDTO.getPan());
		if (!collectorOnboardingInformation.isEmpty() && collectorOnboardingInformation != null) {
			collectorOnboardingInformation.get(0).setActive(false);
			collectorOnBoardingInfoDAO.update(collectorOnboardingInformation.get(0));
		}
		Long collectorMasterId = collectorOnBoardingInfoDAO.getDeductorId(collectorOnboardingInfoDTO.getPan());
		CollectorOnboardingConfigValuesDTO collectorConfig = collectorOnboardingInfoDTO.getOnboardingConfigValues();
		collectorInfo.setPan(collectorOnboardingInfoDTO.getPan());
		collectorInfo.setChallanGeneration(collectorConfig.getChallanGeneration());
		String invoiceProcessScope = objectMapper.writeValueAsString(collectorConfig.getInvoiceProcessScope());
		String scopeProcess = objectMapper.writeValueAsString(collectorConfig.getScopeProcess());
		collectorInfo.setInvoiceProcessScope(invoiceProcessScope);
		collectorInfo.setScopeProcess(scopeProcess);
		collectorInfo.setCollectorMasterId(collectorMasterId.intValue());
		collectorInfo.setCreditNotes(collectorConfig.getCreditNotes());
		collectorInfo.setSectionDetermination(collectorConfig.getSectionDetermination());
		collectorInfo.setActive(true);
		collectorInfo.setApplicableFrom(collectorConfig.getApplicableFrom());
		collectorInfo.setApplicableTo(collectorConfig.getApplicableTo());
		collectorInfo.setLccTrackingNotification(collectorConfig.getLccTrackingNotification());
		collectorInfo.setTdsTransaction(collectorConfig.getTdsTransaction());
		collectorInfo.setTcsApplicability(collectorConfig.getTcsApplicability());
		collectorInfo.setBuyerThresholdComputation(collectorConfig.getBuyerThresholdComputation());
		collectorInfo.setGstImplication(collectorConfig.getGstImplication());
		collectorInfo.setDocumentOrPostingDate(collectorConfig.getDocumentOrPostingDate());
		if(isCollectionReference) {
			collectorInfo.setCollectionReferenceId(collectorConfig.getCollectionReferenceId());
		}else {
			collectorInfo.setCollectionReferenceId(null);
		}
		
		List<ConfigValues> configValues = collectorConfig.getPriority();
		String priority = objectMapper.writeValueAsString(configValues);
		collectorInfo.setPriority(priority);
		try {
			collectorInfo = collectorOnBoardingInfoDAO.save(collectorInfo);
			collectorConfig.setId(collectorInfo.getCollectorOnboardingInfoId());
			collectorOnboardingInfoDTO.setOnboardingConfigValues(collectorConfig);
		} catch (Exception e) {
			logger.error("Error occured while saving record in Collector Onboarding Info", e);
			throw new CustomException("Error occured while saving record in Collector Onboarding Info",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return collectorOnboardingInfoDTO;
	}

	@Transactional
	public Optional<CollectorOnBoardingInfoDTO> getCollectorOnboardingInfo(String collectorPan)
			throws JsonMappingException, JsonProcessingException {
		List<CollectorOnBoardingInformationDTO> collectorOnboardingInformationObject = collectorOnBoardingInfoDAO
				.findByCollectorPan(collectorPan);
		CollectorOnBoardingInfoDTO collectorOnBoardingInfoDTO = new CollectorOnBoardingInfoDTO();
		CollectorOnboardingConfigValuesDTO collectorConfigValuesDTO = new CollectorOnboardingConfigValuesDTO();
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		if (!collectorOnboardingInformationObject.isEmpty()) {
			collectorOnBoardingInfoDTO.setPan(collectorOnboardingInformationObject.get(0).getPan());
			collectorConfigValuesDTO.setApplicableFrom(collectorOnboardingInformationObject.get(0).getApplicableFrom());
			collectorConfigValuesDTO.setApplicableTo(collectorOnboardingInformationObject.get(0).getApplicableTo());
			collectorConfigValuesDTO.setBuyerThresholdComputation(
					collectorOnboardingInformationObject.get(0).getBuyerThresholdComputation());
			collectorConfigValuesDTO
					.setChallanGeneration(collectorOnboardingInformationObject.get(0).getChallanGeneration());
			collectorConfigValuesDTO.setCreditNotes(collectorOnboardingInformationObject.get(0).getCreditNotes());
			collectorConfigValuesDTO.setGstImplication(collectorOnboardingInformationObject.get(0).getGstImplication());
			collectorConfigValuesDTO.setLccTrackingNotification(
					collectorOnboardingInformationObject.get(0).getLccTrackingNotification());
			collectorConfigValuesDTO
					.setSectionDetermination(collectorOnboardingInformationObject.get(0).getSectionDetermination());
			collectorConfigValuesDTO
					.setTcsApplicability(collectorOnboardingInformationObject.get(0).getTcsApplicability());
			collectorConfigValuesDTO.setTdsTransaction(collectorOnboardingInformationObject.get(0).getTdsTransaction());

			String scopeProcess = collectorOnboardingInformationObject.get(0).getScopeProcess();
			List<String> scopeProcessList = mapper.readValue(scopeProcess, new TypeReference<List<String>>() {
			});
			collectorConfigValuesDTO.setScopeProcess(scopeProcessList);

			String invoiceProcessScope = collectorOnboardingInformationObject.get(0).getInvoiceProcessScope();
			List<String> invoiceProcessScopeList = mapper.readValue(invoiceProcessScope, new TypeReference<List<String>>() {
			});
			collectorConfigValuesDTO.setInvoiceProcessScope(invoiceProcessScopeList);
			collectorConfigValuesDTO.setId(collectorOnboardingInformationObject.get(0).getCollectorOnboardingInfoId());
			
			List<ConfigValues> configValueslist = null;
			if (StringUtils.isNotBlank(collectorOnboardingInformationObject.get(0).getPriority())) {
				configValueslist = mapper.readValue(collectorOnboardingInformationObject.get(0).getPriority(), new TypeReference<List<ConfigValues>>() {
				});
			}
			collectorConfigValuesDTO.setPriority(configValueslist);
			collectorOnBoardingInfoDTO.setOnboardingConfigValues(collectorConfigValuesDTO);
			collectorConfigValuesDTO.setCollectionReferenceId(collectorOnboardingInformationObject.get(0).getCollectionReferenceId());
			collectorConfigValuesDTO.setDocumentOrPostingDate(collectorOnboardingInformationObject.get(0).getDocumentOrPostingDate());
			return Optional.of(collectorOnBoardingInfoDTO);
		} else {
			return Optional.empty();
		}
	}

	/**
	 * 
	 * @param dto
	 * @return
	 */
	public Optional<CollectorOnBoardingInformationResponseDTO> copyToEntity(
			Optional<CollectorOnBoardingInformationDTO> dto) {
		CollectorOnBoardingInformationResponseDTO response = new CollectorOnBoardingInformationResponseDTO();
		CollectorOnBoardingInformationResponseDTO.Key key = new CollectorOnBoardingInformationResponseDTO.Key();
		key.setPan(dto.get().getPan());
		response.setKey(key);
		String invoiceProcssScope = dto.get().getInvoiceProcessScope();
		invoiceProcssScope = invoiceProcssScope.replace("[", "").replace("]", "");
		List<String> listInvoiceProcssScope = Stream.of(invoiceProcssScope.split(",")).collect(Collectors.toList());
		response.setInvoiceProcessScope(listInvoiceProcssScope);
		String scopeProcess = dto.get().getScopeProcess();
		scopeProcess = scopeProcess.replace("[", "").replace("]", "");
		List<String> listScopeProcess = Stream.of(scopeProcess.split(",")).collect(Collectors.toList());
		response.setScopeProcess(listScopeProcess);
		Map<String, String> mapPriority = null;
		if (StringUtils.isNotBlank(dto.get().getPriority())) {
			mapPriority = Arrays.stream(dto.get().getPriority().split(",")).map(s -> s.split(":"))
					.collect(Collectors.toMap(s -> s[0].trim(), s -> s[1].trim()));
		}
		response.setPriority(mapPriority);
		response.setBuyerThresholdComputation(dto.get().getBuyerThresholdComputation());
		response.setTdsTransaction(dto.get().getTdsTransaction());
		response.setTcsApplicability(dto.get().getTcsApplicability());
		response.setCreditNotes(dto.get().getCreditNotes());
		response.setLccTrackingNotification(dto.get().getLccTrackingNotification());
		response.setSectionDetermination(dto.get().getSectionDetermination());
		response.setActive(dto.get().getActive());
		response.setApplicableFrom(dto.get().getApplicableFrom());
		response.setApplicableTo(dto.get().getApplicableTo());
		response.setCollectionReferenceId(dto.get().getCollectionReferenceId());
		response.setDocumentOrPostingDate(dto.get().getDocumentOrPostingDate());

		return Optional.of(response);
	}

	/**
	 * 
	 * @param collectorTan
	 * @return
	 */
	public List<CollectorOnBoardingInformationDTO> getCollectorPan(String collectorPan) {
		return collectorOnBoardingInfoDAO.findByCollectorPan(collectorPan);
	}

}
