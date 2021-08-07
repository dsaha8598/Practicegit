package com.ey.in.tds.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ey.in.tds.common.domain.Activity;
import com.ey.in.tds.common.repository.ActivityRepository;

@Service
public class ActivityService {

	private final Logger logger = LoggerFactory.getLogger(ActivityService.class);

	@Autowired
	private ActivityRepository activityRepository;

	/**
	 * This method for get all activity records.
	 * 
	 * @return
	 */
	public List<Activity> getAllActivity() {
		List<Activity> listOfActivity = activityRepository.findAll();
		if (logger.isDebugEnabled()) {
			logger.debug("list of activity", listOfActivity);
		}
		return listOfActivity;
	}
}
