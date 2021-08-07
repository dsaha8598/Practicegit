package com.ey.in.tds.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.FilingSectionCode;

@Repository
public interface FilingSectionCodeRepository extends JpaRepository<FilingSectionCode, Long> {

	@Query(value = "select * from tds.filing_section_code where active = 1 order by id desc", nativeQuery = true)
	@Override
	public List<FilingSectionCode> findAll();

	@Query(value = "select * from tds.filing_section_code where id= ? and active = 1", nativeQuery = true)
	@Override
	public Optional<FilingSectionCode> findById(Long id);
	
}
