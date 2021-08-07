package com.ey.in.tds.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.ArticleMaster;
import com.ey.in.tds.common.util.GlobalConstants;

/**
 * Spring Data repository for the ArticleMaster entity.
 */
@Repository
public interface ArticleMasterRepository extends JpaRepository<ArticleMaster, Long> {

	// @formatter:off
	@Query(value = "select top 1 * from tds.article_master "
			+ "where active=1 and country = ? and name = ? and number = ? " + GlobalConstants.IN_EFFECT_QUERY
			+ "order by applicable_to desc", nativeQuery = true)
	// @formatter:on
	Optional<ArticleMaster> getArticleByCountryNameNumber(String country, String articleName, String articleNumber);

	@Override
	// @formatter:off
	@Query(value = "select * from tds.article_master " + "where active=1 " + GlobalConstants.IN_EFFECT_QUERY
			+ "order by id DESC", nativeQuery = true)
	// @formatter:on
	public List<ArticleMaster> findAll();

	@Override
	// @formatter:off
	@Query(value = "select * from tds.article_master " + "where active = 1 and id= ?"
			+ GlobalConstants.IN_EFFECT_QUERY, nativeQuery = true)
	// @formatter:on
	public Optional<ArticleMaster> findById(Long id);

	// @formatter:off
	@Query(value = "select * from tds.article_master where active=1 and country = ? " + GlobalConstants.IN_EFFECT_QUERY
			+ "order by id DESC", nativeQuery = true)
	// @formatter:on
	List<ArticleMaster> getArticlesByCountry(String country);

	// @formatter:off
	@Query(value = "select * from tds.article_master "
			+ "where active=1 and country = ? and number = ? and name = ? and nature_of_remittance = ? "
			+ GlobalConstants.IN_EFFECT_QUERY, nativeQuery = true)
	// @formatter:on
	Optional<ArticleMaster> getArticleByCountryNameNumberNOR(String country, String articleNumber, String articleName,
			String natureOfRemittance);

	// @formatter:off
	@Query(value = "select * from tds.article_master "
			+ "where active=1 and country = ? and number = ? and nature_of_remittance = ? and non_exempt = ? "
			+ GlobalConstants.IN_EFFECT_QUERY + "order by id DESC", nativeQuery = true)
	// @formatter:on
	Optional<ArticleMaster> getArticleRateByExempt(String country, String articleNumber, String natureOfRemittance,
			Boolean exempt);

	// @formatter:off
	@Query(value = "select * from tds.article_master "
			+ "where active=1 and country = ? and number = ? and nature_of_remittance = ? "
			+ GlobalConstants.IN_EFFECT_QUERY + "order by id DESC", nativeQuery = true)
	// @formatter:on
	Optional<ArticleMaster> getArticleRate(String country, String articleNumber, String natureOfRemittance);

	// @formatter:off
	@Query(value = "select * from tds.article_master " + "where active=1 and country = ? and nature_of_remittance = ? "
			+ GlobalConstants.IN_EFFECT_QUERY + "order by id DESC", nativeQuery = true)
	// @formatter:on
	List<ArticleMaster> getArticleRateByNatureOfRemittance(String country, String natureOfRemittance);
	
	@Query(value = "select number,name,rate,country,nature_of_remittance as natureOfRemittance, make_available_condition_satisfied as lob"
			+ " from tds.article_master where active=1 " + GlobalConstants.IN_EFFECT_QUERY
			+ "order by id DESC", nativeQuery = true)
	// @formatter:on
	List<Map<String, Object>> getArticleMasterData();
}
