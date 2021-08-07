package com.ey.in.tds.tcs.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ey.in.tds.common.domain.tcs.CollecteeType;
import com.ey.in.tds.tcs.repository.CollecteeTypeRepository;
/**
 * 
 * @author Scriptbees.
 *
 */
@Service
public class TCSCollecteeTypeService {

	@Autowired
	private CollecteeTypeRepository collecteeTypeRepo;
	/**
	 * This method for get all collectee type
	 * @return
	 */
	public List<CollecteeType> getCollecteeTypes() {
		List<CollecteeType> listOfCollectee = collecteeTypeRepo.getCollecteeTypeStatus();

		return listOfCollectee;
	}

}
