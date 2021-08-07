package com.ey.in.tds.web.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ey.in.tds.common.domain.Activity;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.service.ActivityService;

@RestController
@RequestMapping("/api/masters")
public class ActivityResource {

	@Autowired
	private  ActivityService activityService;
	
	/**
	 * This API for get all activity records.
	 * @return
	 */
	@GetMapping(value = "/activity")
	public ResponseEntity<ApiStatus<List<Activity>>> getAllActivity() {
		List<Activity> result = activityService.getAllActivity();
		ApiStatus<List<Activity>> apiStatus = new ApiStatus<List<Activity>>(HttpStatus.OK,
				"SUCCESS", " GET ALL ACTIVITY RECORDS", result);
		return new ResponseEntity<ApiStatus<List<Activity>>>(apiStatus, HttpStatus.OK);
	}
	
}
