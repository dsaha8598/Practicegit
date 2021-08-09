package com.ey.in.tds.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ey.in.tds.common.domain.State;
import com.ey.in.tds.dto.CountryDTO;
import com.ey.in.tds.dto.StateDTO;
import com.ey.in.tds.repository.StateRepository;

@Service
public class StateService {

	@Autowired
	private StateRepository stateRepository;

	public List<StateDTO> getStates() {
		List<State> getAllStates = stateRepository.findAll();
		List<StateDTO> getAllStateDTOs = new ArrayList<StateDTO>();

		for (State state : getAllStates) {

			StateDTO stateDTO = new StateDTO();
			CountryDTO countryDTO = new CountryDTO();
			stateDTO.setId(state.getId());
			stateDTO.setName(state.getName().toUpperCase());

			countryDTO.setId(state.getCountry().getId());
			countryDTO.setName(state.getCountry().getName().toUpperCase());

			stateDTO.setCountry(countryDTO);
			getAllStateDTOs.add(stateDTO);
		}
		return getAllStateDTOs;
	}

}
