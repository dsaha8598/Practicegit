package com.ey.in.tds.authorization.config;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ey.in.tds.common.admin.config.JwtTokenValidator;
import com.ey.in.tds.common.config.MultiTenantAspect;
import com.ey.in.tds.common.config.MultiTenantContext;
import com.ey.in.tds.core.util.TDSUtil;

@Aspect
@Component
public class AuthorizationMultiTenantAspect extends MultiTenantAspect {

	public static final String AUTHORIZATION_HEADER = "Authorization";
	
	@Autowired
	private JwtTokenValidator jwtTokenValidator;

	@Value("${tds.noauth.antMatchersList}")
	private String insecureURLList;

	@Override
	@Before("execution(* com.ey.in.tds.authorization.web.rest..*.*(..)) "
			+ "AND @annotation(org.springframework.web.bind.annotation.RestController)")
	public void checkAndSetTenantContext() {
		HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getRequest();
		String urlPath = httpRequest.getRequestURI();
		if (logger.isDebugEnabled())
			logger.debug("urlPath " + urlPath);
		boolean allowURLS = TDSUtil.allowURLS(insecureURLList, urlPath);
		if (!allowURLS) {
			String idToken = httpRequest.getHeader(AUTHORIZATION_HEADER).replaceFirst("Bearer ", StringUtils.EMPTY);
			String uniqueUserName;
			try {
				uniqueUserName = jwtTokenValidator.getUsername(idToken);
				String tenantId = uniqueUserName.substring(uniqueUserName.lastIndexOf("@") + 1,
						uniqueUserName.length());
				if ((tenantId != null) && !tenantProperties.tenantIds().contains(tenantId)) {
					throw new RuntimeException("Could not find configuration for tenant : " + tenantId);
				}

				MultiTenantContext.setTenantId(tenantId);
			} catch (Exception e) {
				logger.error("exception while parsing token");
			}
		}
	}
}
