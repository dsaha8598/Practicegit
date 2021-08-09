//
//package com.ey.in.tds.ingestion.aop.advice;
//
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//@Component
//@Aspect
//public class LoggingAdvice {
//
//	Logger logger = LoggerFactory.getLogger(LoggingAdvice.class);
//
//	@Value("${logging.level.root}")
//	private String level;
//
//	@Around("execution(* com.ey.in.tds.ingestion..*(..))")
//	public Object applicationLogger(ProceedingJoinPoint pjp) throws Throwable {
//
//		ObjectMapper mapper = new ObjectMapper();
//		String methodName = pjp.getSignature().getName();
//		String className = pjp.getTarget().getClass().toString();
//		Object[] array = pjp.getArgs();
//		if (level.equals("INFO")) {
//			if (logger.isInfoEnabled()) {
//				logger.info("method invoked: " + className + " : " + methodName + "()" + " Arguments : "
//						+ mapper.writeValueAsString(array));
//				Object object = pjp.proceed();
//				logger.info(className + " : " + methodName + "()" + " Response: " + mapper.writeValueAsString(object));
//				return object;
//			}
//		} else if (level.equals("WARN")) {
//			if (logger.isWarnEnabled()) {
//				logger.warn("method invoked: " + className + " : " + methodName + "()" + " Arguments : "
//						+ mapper.writeValueAsString(array));
//				Object object = pjp.proceed();
//				logger.warn(className + " : " + methodName + "()" + " Response: " + mapper.writeValueAsString(object));
//				return object;
//			}
//		} else {
//			if (logger.isErrorEnabled()) {
//				logger.error("method invoked: " + className + " : " + methodName + "()" + " Arguments : "
//						+ mapper.writeValueAsString(array));
//				Object object = pjp.proceed();
//				logger.error(className + " : " + methodName + "()" + " Response: " + mapper.writeValueAsString(object));
//				return object;
//			}
//		}
//		return array;
//	}
//}
