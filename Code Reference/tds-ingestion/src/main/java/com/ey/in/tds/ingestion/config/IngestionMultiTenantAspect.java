package com.ey.in.tds.ingestion.config;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import com.ey.in.tds.common.config.MultiTenantAspect;

@Aspect
@Component
public class IngestionMultiTenantAspect extends MultiTenantAspect {

	@Override
	@Before("execution(* com.ey.in.tds.ingestion.web.rest..*.*(..)) "
			+ "AND @annotation(org.springframework.web.bind.annotation.RestController)")
	public void checkAndSetTenantContext() {
		super.checkAndSetTenantContext();
	}
}
