package com.ey.in.tds.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.FilingStateCode;

@Repository
public interface FilingStateCodeRepository extends JpaRepository<FilingStateCode, Long> {

	@Query(value = "select * from tds.filing_state_code where active = 1 order by id desc", nativeQuery = true)
	@Override
	public List<FilingStateCode> findAll();

	@Query(value = "select * from tds.filing_state_code where id= ? and active = 1", nativeQuery = true)
	@Override
	public Optional<FilingStateCode> findById(Long id);

}
