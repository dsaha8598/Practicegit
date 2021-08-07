package com.ey.in.tds.tcs.service;

import java.time.Instant;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ey.in.tds.common.domain.tcs.TCSMonthTracker;
import com.ey.in.tds.common.dto.tdsmonthlytracker.MonthTrackerDTO;
import com.ey.in.tds.common.tcs.repository.TCSMonthTrackerRepository;
import com.fasterxml.jackson.core.JsonProcessingException;

@Service
public class TCSMonthTrackerService {

	@Autowired
	private TCSMonthTrackerRepository tcsMonthTrackerRepository;

	public TCSMonthTracker save(@Valid TCSMonthTracker tdsMonthTracker, String userName) {
		tdsMonthTracker.setActive(true);
		tdsMonthTracker.setCreatedBy(userName);
		tdsMonthTracker.setCreatedDate(Instant.now());
		return tcsMonthTrackerRepository.save(tdsMonthTracker);

	}

	public List<MonthTrackerDTO> findAll() {
		List<TCSMonthTracker> tdsMonthTrackers = tcsMonthTrackerRepository.getAllTCSMonthTracker();
		List<MonthTrackerDTO> tdsMonthTrackerList = new ArrayList<>();
		for (TCSMonthTracker tdsMonthTrackerObj : tdsMonthTrackers) {
			MonthTrackerDTO tdsMonthTrackerDTO = new MonthTrackerDTO();
			tdsMonthTrackerDTO.setId(tdsMonthTrackerObj.getId());
			tdsMonthTrackerDTO.setDueDateForChallanPayment(tdsMonthTrackerObj.getDueDateForChallanPayment());
			tdsMonthTrackerDTO.setDueDateForFiling(tdsMonthTrackerObj.getDueDateForFiling());
			tdsMonthTrackerDTO.setMonth(tdsMonthTrackerObj.getMonth());
			tdsMonthTrackerDTO.setMonthClosureForProcessing(tdsMonthTrackerObj.getMonthClosureForProcessing());
			tdsMonthTrackerDTO.setYear(tdsMonthTrackerObj.getYear());
			tdsMonthTrackerDTO.setApplicableFrom(tdsMonthTrackerObj.getApplicableFrom());
			tdsMonthTrackerDTO.setApplicableTo(tdsMonthTrackerObj.getApplicableTo());
			tdsMonthTrackerDTO.setMonthName(Month.of(tdsMonthTrackerObj.getMonth()).name());
			tdsMonthTrackerList.add(tdsMonthTrackerDTO);
		}
		return tdsMonthTrackerList;
	}

	public MonthTrackerDTO findById(Long id) {
		Optional<TCSMonthTracker> tdsMonthTrackerObj = tcsMonthTrackerRepository.findById(id);
		MonthTrackerDTO tdsMonthTrackerDTO = new MonthTrackerDTO();
		if (tdsMonthTrackerObj.isPresent()) {
			tdsMonthTrackerDTO.setId(tdsMonthTrackerObj.get().getId());
			tdsMonthTrackerDTO.setDueDateForChallanPayment(tdsMonthTrackerObj.get().getDueDateForChallanPayment());
			tdsMonthTrackerDTO.setDueDateForFiling(tdsMonthTrackerObj.get().getDueDateForFiling());
			tdsMonthTrackerDTO.setMonth(tdsMonthTrackerObj.get().getMonth());
			tdsMonthTrackerDTO.setMonthClosureForProcessing(tdsMonthTrackerObj.get().getMonthClosureForProcessing());
			tdsMonthTrackerDTO.setYear(tdsMonthTrackerObj.get().getYear());
			tdsMonthTrackerDTO.setApplicableFrom(tdsMonthTrackerObj.get().getApplicableFrom());
			tdsMonthTrackerDTO.setApplicableTo(tdsMonthTrackerObj.get().getApplicableTo());
			tdsMonthTrackerDTO.setMonthName(Month.of(tdsMonthTrackerObj.get().getMonth()).name());
		}
		return tdsMonthTrackerDTO;
	}

	public TCSMonthTracker update(@Valid TCSMonthTracker tdsMonthTracker, String userName) throws JsonProcessingException {
		TCSMonthTracker monthTracker = new TCSMonthTracker();
		Optional<TCSMonthTracker> monthTrackerObj = tcsMonthTrackerRepository.findById(tdsMonthTracker.getId());
		if (monthTrackerObj.isPresent()) {
			monthTracker = monthTrackerObj.get();
			monthTracker.setDueDateForChallanPayment(tdsMonthTracker.getDueDateForChallanPayment());
			monthTracker.setDueDateForFiling(tdsMonthTracker.getDueDateForFiling());
			monthTracker.setModifiedBy(userName);
			monthTracker.setModifiedDate(Instant.now());
			monthTracker.setMonth(tdsMonthTracker.getMonth());
			monthTracker.setMonthClosureForProcessing(tdsMonthTracker.getMonthClosureForProcessing());
			monthTracker.setYear(tdsMonthTracker.getYear());
			monthTracker.setApplicableFrom(tdsMonthTracker.getApplicableFrom());
			monthTracker.setApplicableTo(tdsMonthTracker.getApplicableTo());
			monthTracker = tcsMonthTrackerRepository.save(monthTracker);
		}

		return monthTracker;
	}

	public TCSMonthTracker findByAssessmentYearMonth(Integer year, Integer month) {
		TCSMonthTracker tdsMonthTracker = new TCSMonthTracker();
		Optional<TCSMonthTracker> tdsMonthTrackerObj = tcsMonthTrackerRepository.findByAssessmentYearMonth(year, month);
		if (tdsMonthTrackerObj.isPresent()) {
			return tdsMonthTrackerObj.get();
		}
		return tdsMonthTracker;
	}

}
