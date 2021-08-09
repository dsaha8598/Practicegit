package com.ey.in.tds.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.MasterBatchUpload;

/**
 * Spring Data repository for the master batch upload entity.
 * 
 * @author vamsir
 */
@Repository
public interface MasterBatchUploadRepository extends JpaRepository<MasterBatchUpload, Long> {

	// @formatter:off
	@Query(value = "SELECT * FROM tds.batch_upload WHERE sha256sum  = ? AND active = 1", nativeQuery = true)
	// @formatter:on
	Optional<MasterBatchUpload> getSha256Records(String sha256Sum);

	// @formatter:off
	@Query(value = "SELECT * FROM tds.batch_upload WHERE upload_type = ? AND assessment_year = ? AND active = 1 ORDER BY id DESC", nativeQuery = true)
	// @formatter:on
	List<MasterBatchUpload> getBatchUplodFiles(String type, int year);

	// @formatter:off
	@Query(value = "SELECT * FROM tds.batch_upload WHERE assessment_year = ? AND upload_type = ? AND id = ? AND active = 1 ", nativeQuery = true)
	// @formatter:on
	Optional<MasterBatchUpload> findById(Integer assesmentYear, String uploadType, Integer batchId);

}
