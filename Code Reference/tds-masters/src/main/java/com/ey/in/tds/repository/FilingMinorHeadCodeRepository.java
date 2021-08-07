package com.ey.in.tds.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ey.in.tds.common.domain.FilingMinorHeadCode;

public interface FilingMinorHeadCodeRepository extends JpaRepository<FilingMinorHeadCode, Long> {

	@Query(value = "select * from tds.filing_minor_head_code where active = 1 order by id desc", nativeQuery = true)
	@Override
	public List<FilingMinorHeadCode> findAll();

	@Query(value = "select * from tds.filing_minor_head_code where id= ? and active = 1", nativeQuery = true)
	@Override
	public Optional<FilingMinorHeadCode> findById(Long id);
}
