package com.ey.in.tds.onboarding.service.kyc;

import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.common.config.RedisUtil;
import com.ey.in.tds.common.model.emailnotification.Email;
import com.ey.in.tds.common.onboarding.jdbc.dto.DeductorMaster;
import com.ey.in.tds.common.onboarding.jdbc.dto.KycRedisDTO;
import com.ey.in.tds.common.service.EmailService;
import com.ey.in.tds.core.util.RedisKeys;
import com.ey.in.tds.jdbc.dao.DeductorMasterDAO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class KYCOTPService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisUtil<Map<String, Object>> redisUtilUserTenantInfo;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private DeductorMasterDAO deductorMasterDAO;

    @Value("${tds.from-email}")
    private String mailsentfrom;

    private final int OTP_TILL_VALID_MINUTES = 5;
    private final String SUBJECT = "";

    public void sendMailOTP(String key) {
        logger.info("** Got Request for OTP Send for key : {}", key);
        Context ctx = new Context();
        Random generator = new Random();
        int otp = 100000 + generator.nextInt(900000);
        ctx.setVariable("otp", otp);
        String customerJSONString = redisUtilUserTenantInfo.getMapAsAll(RedisKeys.KYC.name()).get(key);
        try {
            KycRedisDTO redisDTO = new ObjectMapper().readValue(customerJSONString, KycRedisDTO.class);
            MultiTenantContext.setTenantId(redisDTO.getTenantId());
            List<DeductorMaster> response = deductorMasterDAO.findByDeductorPan(redisDTO.getDeductorPan());
            ctx.setVariable("name", redisDTO.getCustomerName());
            ctx.setVariable("clientName", response.get(0).getName());

            String body = templateEngine.process("kycemailotptemplate", ctx);
            try {
                Email email = new Email();	
                email.setFrom("EY on behalf of " + response.get(0).getName() + " <" + mailsentfrom + ">");
                email.setTo(redisDTO.getEmailId());
                email.setSubject("Verification Code, assistance required for " + response.get(0).getName());
                emailService.sendHtmlTemplateNotification(email, body);
            } catch (MessagingException e) {
                logger.error("Exception occurred while sending OTP email for key {}  : {}", key, e.getMessage());
            }
            redisUtilUserTenantInfo.putMap(RedisKeys.KYC.name(), key + "_OTP", otp + "");
            redisUtilUserTenantInfo.setExpire(key + "_OTP", OTP_TILL_VALID_MINUTES, TimeUnit.MINUTES);
            logger.info("** OTP Sent for key : {} valid for {} minutes", key, OTP_TILL_VALID_MINUTES);
        } catch (JsonProcessingException e) {
            logger.error("Unable to convert JSON String to KycRedisDTO : {}", e.getMessage());
        }

    }

    public Map<String, String> verifyMailOTP(String key, String otp) {
        logger.info("** Got Request for OTP Verification for key : {} with OTP : {}", key, otp);
        String customerOTPString = redisUtilUserTenantInfo.getMapAsAll(RedisKeys.KYC.name()).get(key + "_OTP");
        String customerString = redisUtilUserTenantInfo.getMapAsAll(RedisKeys.KYC.name()).get(key);
        Map<String, String> responseMap = new HashMap<>();
        String isOTPVerified = customerOTPString != null ? customerOTPString.equals(otp) + "" : "false";
        if(isOTPVerified.equals("true")){
            logger.info("** As OTP for key : {} with OTP : {} verified, so expiring same on redis.", key, otp);
            redisUtilUserTenantInfo.setExpire(key + "_OTP", 0, TimeUnit.MILLISECONDS);
        }
        responseMap.put("isOTPVerified", isOTPVerified);
        responseMap.put("data", customerString);
        logger.info("** OTP Verification for key : {} with OTP : {} is {}", key, otp, isOTPVerified);
        return responseMap;
    }
}
