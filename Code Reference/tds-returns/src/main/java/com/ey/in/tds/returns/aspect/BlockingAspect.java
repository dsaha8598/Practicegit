package com.ey.in.tds.returns.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class BlockingAspect {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Around("execution(static * java.awt..*.show(..))")
	public Object intercept(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		if (System.getProperty("java.awt.headless").equals("true")) {
			return proceedingJoinPoint.proceed();
		} else {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

	@Before("execution(static * java.awt..*.show(..)) ")
	public void checkAndSetTenantContext() {
		logger.info("... Weaving java.awt..*.show ...");
	}

	@Around("execution(* com.ey.in.tds.returns.web.rest..*.*(..))")
	public Object intercept1(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		if (System.getProperty("java.awt.headless").equals("true")) {
			return proceedingJoinPoint.proceed();
		} else {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

	@Before("execution(* com.ey.in.tds.returns.web.rest..*.*(..)) ")
	public void checkAndSetTenantContext1() {
		logger.info("... Weaving com.ey.in.tds.reports.web.rest..*.* ...");
	}

	@Pointcut("call(static * javax.swing..*.*(..))")
	public void anyStaticOperation() {
		logger.info("... Weaving javax.swing..*.* ...");
	}

	@Around("execution(static * javax.swing..*.*(..))")
	public Object aroundStaticMethods(ProceedingJoinPoint jp) throws Throwable {
		return jp.proceed();
	}
}
