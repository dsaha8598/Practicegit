package com.ey.in.tds.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.MasterTdsHsnCode;

/**
 * 
 * @author vamsir
 *
 */
@Repository
public interface TdsHsnCodeRepository extends JpaRepository<MasterTdsHsnCode, Long> {

	@Override
	// @formatter:off
		@Query(value = "select * from tds.tds_hsn_code where active = 1 order by id DESC", nativeQuery = true)
	// @formatter:on
	List<MasterTdsHsnCode> findAll();

	Page<MasterTdsHsnCode> findAll(Pageable pageable);

	// @formatter:off
		@Query(value = "SELECT * FROM tds.tds_hsn_code WHERE hsn_code = ? AND active = 1", nativeQuery = true)
	// @formatter:on
	public List<MasterTdsHsnCode> findHSNRateDetails(Long hsn);

	// @formatter:off
		@Query(value = "SELECT * FROM tds.tds_hsn_code WHERE tds_section = ? AND active = 1", nativeQuery = true)
	// @formatter:on
	public List<MasterTdsHsnCode> getAllHsnByTdsSection(String tdsSection);

	// @formatter:off
		@Query(value = "SELECT distinct tds_section FROM tds.tds_hsn_code WHERE active = 1", nativeQuery = true)
	// @formatter:on
	public List<String> getAllTdsSection();

	// @formatter:off
		@Query(value = "SELECT * FROM tds.tds_hsn_code WHERE id = ? AND active = 1", nativeQuery = true)
	// @formatter:on
	public Optional<MasterTdsHsnCode> getById(Integer id);

}
