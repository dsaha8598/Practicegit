///*
// *
// * 
// * import java.util.Arrays;
// * 
// * import org.aspectj.lang.ProceedingJoinPoint; import
// * org.aspectj.lang.annotation.Around; import
// * org.aspectj.lang.annotation.Aspect; import
// * org.aspectj.lang.annotation.Pointcut; import
// * org.aspectj.lang.reflect.MethodSignature; import org.slf4j.Logger; import
// * org.slf4j.LoggerFactory;
// * 
// * 
// * @Aspect public class MethodLogger {
// * 
// * private static final Logger logger =
// * LoggerFactory.getLogger(MethodLogger.class);
// * 
// * @Pointcut("execution(com.ey.in.tds.ingestion.web.rest.advance.*(..))") public
// * void myPointCut() {
// * 
// * }
// * 
// * @Around("myPointCut()") public Object around(ProceedingJoinPoint point)
// * throws Throwable {
// * 
// * long start = System.currentTimeMillis(); Object result = point.proceed();
// * Logger.info("#%s(%s): %s in %[msec]s",
// * MethodSignature.class.cast(point.getSignature()).getMethod().getName(),
// * point.getArgs(), result, System.currentTimeMillis() - start); return result;
// * 
// * 
// * String className =
// * MethodSignature.class.cast(point.getSignature()).getClass().getName(); String
// * methodName =
// * MethodSignature.class.cast(point.getSignature()).getMethod().getName();
// * logger.info( "Entering method: {}() of class: {} with parameters: {}",
// * methodName, className, Arrays.toString(point.getArgs()));
// * 
// * try { return point.proceed(); } catch(Throwable e){ throw e; } finally {
// * logger.info( "Exiting method: {}() of class: {} with parameters: {}",
// * methodName, className, Arrays.toString(point.getArgs())); } } }
// */
//
// package com.ey.in.tds.ingestion.web.rest.util;
// 
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Pointcut;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//@Component
//@Aspect
//public class MethodLogger {
//
//    Logger logger = LoggerFactory.getLogger(MethodLogger.class);
//
//    
//    //@Pointcut("execution(* com.ey.in.tds.ingestion.web.rest.provision.ProvisionMismatchResource.*(..))")
//    
//	/*
//	 * @Pointcut("execution(* com.ey.in.tds.ingestion.web.rest.advance.AdvanceResource.*(..))"
//	 * ) public void myPointCut() {
//	 * 
//	 * }
//	 */
//     
//    @Around("execution(* com.ey.in.tds.ingestion.*(..))")
//    public Object applicationLogger(ProceedingJoinPoint pjp) throws Throwable {
//
//        ObjectMapper mapper = new ObjectMapper();
//        String methodName = pjp.getSignature().getName();
//        String className = pjp.getTarget().getClass().toString();
//        Object[] array = pjp.getArgs();
//        logger.info("method invoked " + className + " : " + methodName + "()" + "argument: "
//                + mapper.writeValueAsString(array));
//
//        Object object = pjp.proceed();
//        logger.info(className + " : " + methodName + "()" + "Response: " + mapper.writeValueAsString(object));
//
//        return object;
//    }
//
//}
//
//
//
//
//
