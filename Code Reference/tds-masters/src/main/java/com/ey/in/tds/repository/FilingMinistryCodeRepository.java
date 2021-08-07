package com.ey.in.tds.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.FilingMinistryCode;

@Repository
public interface FilingMinistryCodeRepository extends JpaRepository<FilingMinistryCode, Long> {

	@Query(value = "select * from tds.filing_ministry_code where active = 1 order by id desc", nativeQuery = true)
	@Override
	public List<FilingMinistryCode> findAll();

	@Query(value = "select * from tds.filing_ministry_code where id= ? and active = 1", nativeQuery = true)
	@Override
	public Optional<FilingMinistryCode> findById(Long id);
}
