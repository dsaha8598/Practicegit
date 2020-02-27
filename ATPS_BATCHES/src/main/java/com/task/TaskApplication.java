package com.task;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.task.service.TaskService;

@SpringBootApplication
public class TaskApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx=SpringApplication.run(TaskApplication.class, args);
		TaskService service=ctx.getBean(TaskService.class);
	    service.preProcess();
		service.start();
	    service.postprocess();
		
	}

}
