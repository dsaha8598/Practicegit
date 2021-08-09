package com.ey.in.tds;

import java.io.FileNotFoundException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

import com.ey.in.tds.core.swagger.SwaggerConfig;
import com.ey.in.tds.core.util.CommonUtil;

import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableAspectJAutoProxy
@EnableSwagger2
@EnableAsync
@SpringBootApplication
public class AppMastersApplication {

	@Value("${spring.application.name}")
	private String applicationName;
	
	public static void main(String[] args) throws FileNotFoundException {
		SpringApplication.run(AppMastersApplication.class, args);
		CommonUtil.setAsposeLicense();
	}

	@Bean
	public Docket api() {
		return new SwaggerConfig().getSwaggerDocker(applicationName);
	}
}
