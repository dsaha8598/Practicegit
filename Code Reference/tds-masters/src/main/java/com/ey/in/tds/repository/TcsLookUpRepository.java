package com.ey.in.tds.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ey.in.tds.common.domain.TcsLookUp;

public interface TcsLookUpRepository extends JpaRepository<TcsLookUp, Long> {

	@Query(value = "SELECT * FROM tcs.tcs_onboarding_lookup WHERE active=1;", nativeQuery = true)
	public List<TcsLookUp> findAllActive();

}
