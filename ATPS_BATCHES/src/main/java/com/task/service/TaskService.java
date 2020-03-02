package com.task.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.task.entity.BatchRunSummeryEntity2;
import com.task.entity.Batchrunentity;
import com.task.entity.TagTransactiontriggersEntity;
import com.task.entity.UserMasterEntity;
import com.task.mail.UserMailSender;
import com.task.repository.BatchRunRepo;
import com.task.repository.BatchRunSummeryRepo;
import com.task.repository.TagMasterRepo;
import com.task.repository.TagTransactionTriggerRepo;
import com.task.repository.UserMasterRepo;

@Service
public class TaskService {

	/**
	 * injecting BatchRunDetailsEntity class object
	 */
	@Autowired
	BatchRunRepo batchRepo;

	/**
	 * injecting TagTransactionTriggerRepo object
	 */
	@Autowired
	TagTransactionTriggerRepo triggerRepo;

	/**
	 * injecting TagMasterRepo object
	 */
	@Autowired
	TagMasterRepo tagRepo;//added a kjh comment kjhiu ggg

	/**
	 * injecting UserMasterRepo object
	 */
	@Autowired
	UserMasterRepo userRepo;

	/***
	 * 
	 */
	@Autowired
	BatchRunSummeryRepo batchSummeryRepo;

	/**
	 * injecting MailSender class object
	 */
	@Autowired
	UserMailSender sendMail;

	/**
	 * declaring variables
	 */
	Batchrunentity brentity = null, savedEntity = null;
	Integer runSequenceId = 0;
	UserMasterEntity userEntity = null;
	private static String batchName = "LOW_BALANCE";
	BatchRunSummeryEntity2 summeryEntity = null;
	int successCount = 0, failureCount = 0, recordCount = 0;

	/**
	 * this method is used to set the data into batch details
	 * 
	 * @return runSequenceId
	 */
	public Integer preProcess() {
		Batchrunentity brentity = new Batchrunentity();
		brentity.setBatchName(batchName);
		brentity.setRunStatus("ST");
		brentity.setCreatedby(batchName);
		brentity.setStartdate(new Date());
		savedEntity = batchRepo.save(brentity);
		runSequenceId = savedEntity.getRunId();
		System.out.println(runSequenceId);
		return runSequenceId;
		System.out.println("added a statement");
	}

	/**
	 * start method retrieves record from TagTransactiontriggers table process eac h
	 * recor calls process()
	 */
	@SuppressWarnings("rawtypes")
	public void start() {
		String status = "fail";
		String reason = "low balance";
		// to assign date you have o change the date in your system
		Date date = new Date();
		System.out.println(date);
		String reminderMessageSwitch = "p";
		// retrieveing and storing in list collection
		List<TagTransactiontriggersEntity> getTagTriggerTransactionRecords = triggerRepo
				.getTagTriggerTransactionRecords(status, reason, date, reminderMessageSwitch);
		// counting no of reccords retrieved
		recordCount = getTagTriggerTransactionRecords.size();
		// creating thread pool
		ExecutorService service = Executors.newFixedThreadPool(10); // creating ExecutorService object by using
						////											// Executors factory class which gives thread pool
																	// of fixed length
		CompletionService<Future> pool = new ExecutorCompletionService<Future>(service); // creating
																							// ExecutorCompletionService
																							// object by passing service
																							// as argument and
																							// asssigning the object to
																							// the reference variable of
																							// CompletionService
																							// Interface
		// itereating the collection object
		for (TagTransactiontriggersEntity trigentity : getTagTriggerTransactionRecords) {
			pool.submit(new Callable<Future>() { // calling the submit method to execute the task and creating a inner
													// class of callable
				public Future call() { // overriding the call() and placing the tasks to be executed whose return type
										// is Future object which contains the retrirvrd data
					Integer tagId = trigentity.getTagId();
					Integer userId = tagRepo.getUserId(tagId);
					System.out.println(tagId);
					// getting the userMasterEntity class object
					Optional<UserMasterEntity> userOptional = userRepo.findById(userId);
					if (userOptional.isPresent()) {
						userEntity = userOptional.get();
						// calling process() with current object reference
						process(userEntity, tagId);
					}
					return null;
				}
			});

		} // for

	}// start()

	/**
	 * calls sendUserMail() by supplying required inputs
	 * 
	 * @param userEntity
	 * @param tagId
	 */
	public void process(UserMasterEntity userEntity, Integer tagId) {
		String name = userEntity.getFirtsName();
		String email = userEntity.getEmailId();// to send the mail
		// calling this method to send the mails to the users
		String msg = sendMail.sendUserMail(name, email);
		if (msg.equals("success")) {
			this.updateMessageSwitch(tagId);
			successCount++;
		} else {
			System.out.println("**************************failed******************************************");
			failureCount++;
			return;
		}
	}// process

	public void updateMessageSwitch(Integer tagId) {
		String messageSwitch = "c";
		// updating the message switch
		triggerRepo.updateMessageSwitch(tagId, messageSwitch);
		System.out.println(
				"***********************************************************record updated*********************************************");
	}

	public void postprocess() {
		summeryEntity = new BatchRunSummeryEntity2();
		summeryEntity.setBatchName(batchName);
		String msg = "total " + recordCount + "  processed and " + successCount + "records updated" + failureCount
				+ "failed";
		summeryEntity.setSummeryDetails(msg);
		// saving details to BatchRunSummeryDetails table
		batchSummeryRepo.save(summeryEntity);

		Batchrunentity brentity = new Batchrunentity();
		brentity.setRunId(runSequenceId);
		brentity.setEndDt(new Date());
		brentity.setRunStatus("completed");
		batchRepo.save(brentity);
	}

}
