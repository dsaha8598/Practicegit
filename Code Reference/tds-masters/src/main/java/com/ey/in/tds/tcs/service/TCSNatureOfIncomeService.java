package com.ey.in.tds.tcs.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ey.in.tds.common.domain.BasisOfCessDetails;
import com.ey.in.tds.common.domain.BasisOfSurchargeDetails;
import com.ey.in.tds.common.domain.tcs.TCSNatureOfIncome;
import com.ey.in.tds.common.tcs.repository.TCSNatureOfIncomeRepository;
import com.ey.in.tds.core.exceptions.CustomException;
import com.ey.in.tds.core.exceptions.RecordNotFoundException;
import com.ey.in.tds.dto.NatureOfPaymentMasterDTO;
import com.ey.in.tds.dto.NopCessRateSurageRateDTO;
import com.ey.in.tds.dto.SubNaturePaymentMasterDTO;
import com.ey.in.tds.repository.BasisOfCessDetailsRepository;
import com.ey.in.tds.repository.BasisOfSurchargeDetailsRepository;
import com.ey.in.tds.web.rest.util.CommonValidations;
import com.fasterxml.jackson.core.JsonProcessingException;
/**
 * 
 * @author scriptbees
 *
 */
@Service
public class TCSNatureOfIncomeService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TCSNatureOfIncomeRepository tcsNatureOfIncomeRepository;

	/**
	 * This method is used to create new Nature of Payment record
	 * 
	 * @param userName
	 * 
	 * @param natureOfPaymentMastersfromui
	 * @return
	 * @throws JsonProcessingException
	 */

	public TCSNatureOfIncome save(TCSNatureOfIncome tcsNatureOfIncome, String userName) throws JsonProcessingException {
		logger.info("REST request to save NatureOfPaymentMaster : {} ", tcsNatureOfIncome);

		/**
		 * Below logic written for checking the unique subnature
		 */
		/*
		 * if (tcsNatureOfIncome.getIsSubNaturePaymentApplies()) {
		 * Set<SubNaturePaymentMaster> unique =
		 * tcsNatureOfIncome.getSubNaturePaymentMasters().stream()
		 * .collect(collectingAndThen( toCollection(() -> new
		 * TreeSet<>(comparing(SubNaturePaymentMaster::getNature))), HashSet::new)); if
		 * (unique.size() < tcsNatureOfIncome.getSubNaturePaymentMasters().size()) {
		 * throw new
		 * BadRequestException("Subnature of payment name should not be same"); } }
		 */
		Optional<TCSNatureOfIncome> nopDb = tcsNatureOfIncomeRepository
				.findBySectionAndNOP(tcsNatureOfIncome.getSection(), tcsNatureOfIncome.getNature());
		if (nopDb.isPresent()) {
			CommonValidations.validateApplicableFields(nopDb.get().getApplicableTo(),
					tcsNatureOfIncome.getApplicableFrom());
		}
		TCSNatureOfIncome response = null;
		TCSNatureOfIncome natureOfIncome = new TCSNatureOfIncome();
		natureOfIncome.setApplicableFrom(tcsNatureOfIncome.getApplicableFrom());
		natureOfIncome.setApplicableTo(tcsNatureOfIncome.getApplicableTo());
		natureOfIncome.setDisplayValue(tcsNatureOfIncome.getDisplayValue());
		natureOfIncome.setNature(tcsNatureOfIncome.getNature());
		natureOfIncome.setSection(tcsNatureOfIncome.getSection());
		natureOfIncome.setIsSubNaturePaymentApplies(tcsNatureOfIncome.getIsSubNaturePaymentApplies());
		natureOfIncome.setCreatedBy(userName);
		natureOfIncome.setCreatedDate(Instant.now());
		natureOfIncome.setModifiedBy(userName);
		natureOfIncome.setModifiedDate(Instant.now());
		natureOfIncome.setActive(true);

		// Set<SubNaturePaymentMaster> set = new HashSet<>();
		/*
		 * SubNaturePaymentMaster subSet = null; if
		 * (tcsNatureOfIncome.getIsSubNaturePaymentApplies()) {
		 * Set<SubNaturePaymentMaster> subSets =
		 * tcsNatureOfIncome.getSubNaturePaymentMasters(); for (SubNaturePaymentMaster
		 * setOfSubNature : subSets) { Optional<SubNaturePaymentMaster>
		 * subNaturePaymentMasterOptional = subNaturePaymentMasterRepository
		 * .findByNature(setOfSubNature.getNature()); if
		 * (!subNaturePaymentMasterOptional.isPresent()) { subSet = new
		 * SubNaturePaymentMaster(); subSet.setNature(setOfSubNature.getNature());
		 * subSet.setNaturePaymentMaster(natureOfIncome); subSet.setActive(true);
		 * subSet.setCreatedDate(Instant.now()); subSet.setCreatedBy(userName);
		 * set.add(subSet); } else { throw new
		 * CustomException(setOfSubNature.getNature() + " is already present  ",
		 * HttpStatus.INTERNAL_SERVER_ERROR); } }
		 * natureOfIncome.setSubNaturePaymentMasters(set); }
		 */
		response = tcsNatureOfIncomeRepository.save(natureOfIncome);
		return response;
	}

	/**
	 * This method is used to get all Nature of Payment record
	 * 
	 * @return
	 * @throws RecordNotFoundException
	 */
	@Transactional(readOnly = true)
	public List<NatureOfPaymentMasterDTO> findAll() {
		List<NatureOfPaymentMasterDTO> arrayList = new ArrayList<>();
		List<SubNaturePaymentMasterDTO> getSetData = null;
		NatureOfPaymentMasterDTO natureList = null;
		// SubNaturePaymentMasterDTO setSubnature = null;
		// Set<SubNaturePaymentMaster> set = null;
		List<TCSNatureOfIncome> list = tcsNatureOfIncomeRepository.findAll();
		if (!list.isEmpty()) {
			for (TCSNatureOfIncome nature : list) {
				natureList = new NatureOfPaymentMasterDTO();
				getSetData = new ArrayList<>();
				natureList.setApplicableFrom(nature.getApplicableFrom());
				natureList.setApplicableTo(nature.getApplicableTo());
				natureList.setDisplayValue(nature.getDisplayValue());
				natureList.setId(nature.getId());
				natureList.setIsSubNaturePaymentApplies(nature.getIsSubNaturePaymentApplies());
				natureList.setNature(nature.getNature());
				natureList.setSection(nature.getSection());
				/*
				 * set = nature.getSubNaturePaymentMasters(); for (SubNaturePaymentMaster sets :
				 * set) { setSubnature = new SubNaturePaymentMasterDTO();
				 * setSubnature.setId(sets.getId()); setSubnature.setNature(sets.getNature());
				 * setSubnature.setNaturePaymentMaster(nature); getSetData.add(setSubnature); }
				 */
				natureList.setSubNaturePaymentMasters(getSetData);
				arrayList.add(natureList);
			}
		}
		return arrayList;
	}

	/**
	 * This method is used to get Nature of Payment record
	 * 
	 * @param id
	 * @return
	 * @throws RecordNotFoundException
	 */
	@Transactional(readOnly = true)
	public NatureOfPaymentMasterDTO findOne(Long id) {
		logger.info("REST request id to get a NatureOfPaymentMaster : {} ", id);
		NatureOfPaymentMasterDTO natureOfPaymentMasterDTO = new NatureOfPaymentMasterDTO();
		List<SubNaturePaymentMasterDTO> getSetData = null;
		// SubNaturePaymentMasterDTO setSubnature = null;
		// Set<SubNaturePaymentMaster> set = null;
		Optional<TCSNatureOfIncome> natureOfpayment = tcsNatureOfIncomeRepository.findById(id);
		TCSNatureOfIncome nature = null;
		if (natureOfpayment.isPresent()) {
			getSetData = new ArrayList<>();
			nature = natureOfpayment.get();
			natureOfPaymentMasterDTO.setApplicableFrom(nature.getApplicableFrom());
			natureOfPaymentMasterDTO.setApplicableTo(nature.getApplicableTo());
			natureOfPaymentMasterDTO.setDisplayValue(nature.getDisplayValue());
			natureOfPaymentMasterDTO.setId(nature.getId());
			natureOfPaymentMasterDTO.setIsSubNaturePaymentApplies(nature.getIsSubNaturePaymentApplies());
			natureOfPaymentMasterDTO.setNature(nature.getNature());
			natureOfPaymentMasterDTO.setSection(nature.getSection());
			/*
			 * set = nature.getSubNaturePaymentMasters(); for (SubNaturePaymentMaster sets :
			 * set) { setSubnature = new SubNaturePaymentMasterDTO();
			 * setSubnature.setId(sets.getId()); setSubnature.setNature(sets.getNature());
			 * setSubnature.setNaturePaymentMaster(nature); getSetData.add(setSubnature); }
			 */
			natureOfPaymentMasterDTO.setSubNaturePaymentMasters(getSetData);
		}
		return natureOfPaymentMasterDTO;
	}

	/**
	 * This method is used to update Nature of Payment record
	 * 
	 * @param tcsNatureOfIncome
	 * @param userName
	 * @return
	 * @throws JsonProcessingException
	 * @throws RecordNotFoundException
	 */
	public TCSNatureOfIncome updateNatureOfPaymentMaster(@Valid TCSNatureOfIncome tcsNatureOfIncome, String userName)
			throws JsonProcessingException {
		logger.info("REST request to update NatureOfPaymentMaster : {} ", tcsNatureOfIncome);
		TCSNatureOfIncome response = null;
		Optional<TCSNatureOfIncome> natureOfPaymentMasters = tcsNatureOfIncomeRepository
				.findById(tcsNatureOfIncome.getId());
		// Set<SubNaturePaymentMaster> set = new HashSet<>();
		// SubNaturePaymentMaster subSetUpdate = null;
		if (natureOfPaymentMasters.isPresent()) {
			TCSNatureOfIncome natureOfPayment = natureOfPaymentMasters.get();
			natureOfPayment.setId(tcsNatureOfIncome.getId());
			natureOfPayment.setApplicableFrom(tcsNatureOfIncome.getApplicableFrom());
			natureOfPayment.setApplicableTo(tcsNatureOfIncome.getApplicableTo());
			natureOfPayment.setDisplayValue(tcsNatureOfIncome.getDisplayValue());
			natureOfPayment.setIsSubNaturePaymentApplies(tcsNatureOfIncome.getIsSubNaturePaymentApplies());
			natureOfPayment.setSection(tcsNatureOfIncome.getSection());
			natureOfPayment.setNature(tcsNatureOfIncome.getNature());
			natureOfPayment.setModifiedBy(userName);
			natureOfPayment.setModifiedDate(Instant.now());
			/*
			 * if (natureOfPayment.getSubNaturePaymentMasters() != null ||
			 * !natureOfPayment.getSubNaturePaymentMasters().isEmpty()) { for
			 * (SubNaturePaymentMaster subSet :
			 * natureOfPayment.getSubNaturePaymentMasters()) { subSetUpdate = new
			 * SubNaturePaymentMaster(); subSetUpdate.setId(subSet.getId());
			 * subSetUpdate.setNature(subSet.getNature());
			 * subSetUpdate.setNaturePaymentMaster(tcsNatureOfIncome);
			 * set.add(subSetUpdate); } }
			 */
			// natureOfPayment.setSubNaturePaymentMasters(set);
			response = tcsNatureOfIncomeRepository.save(natureOfPayment);
		} else {
			throw new CustomException("No record found  with id " + tcsNatureOfIncome.getId(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	@Transactional(readOnly = true)
	public NatureOfPaymentMasterDTO findBySection(String section) {
		logger.info("REST request id to get a NatureOfPaymentMaster : {} ", section);
		NatureOfPaymentMasterDTO natureOfPaymentMasterDTO = new NatureOfPaymentMasterDTO();
		List<SubNaturePaymentMasterDTO> getSetData = null;
		// SubNaturePaymentMasterDTO setSubnature = null;
		// Set<SubNaturePaymentMaster> set = null;
		Optional<TCSNatureOfIncome> natureOfpayment = tcsNatureOfIncomeRepository.findBySectionFor2061H(section);
		TCSNatureOfIncome nature = null;
		if (natureOfpayment.isPresent()) {
			getSetData = new ArrayList<>();
			nature = natureOfpayment.get();
			natureOfPaymentMasterDTO.setApplicableFrom(nature.getApplicableFrom());
			natureOfPaymentMasterDTO.setApplicableTo(nature.getApplicableTo());
			natureOfPaymentMasterDTO.setDisplayValue(nature.getDisplayValue());
			natureOfPaymentMasterDTO.setId(nature.getId());
			natureOfPaymentMasterDTO.setIsSubNaturePaymentApplies(nature.getIsSubNaturePaymentApplies());
			natureOfPaymentMasterDTO.setNature(nature.getNature());
			natureOfPaymentMasterDTO.setSection(nature.getSection());
			/*
			 * set = nature.getSubNaturePaymentMasters(); for (SubNaturePaymentMaster sets :
			 * set) { setSubnature = new SubNaturePaymentMasterDTO();
			 * setSubnature.setId(sets.getId()); setSubnature.setNature(sets.getNature());
			 * setSubnature.setNaturePaymentMaster(nature); getSetData.add(setSubnature); }
			 */
			natureOfPaymentMasterDTO.setSubNaturePaymentMasters(getSetData);
		}
		return natureOfPaymentMasterDTO;
	}

}
