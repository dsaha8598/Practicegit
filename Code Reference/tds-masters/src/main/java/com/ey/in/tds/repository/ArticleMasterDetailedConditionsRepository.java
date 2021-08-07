package com.ey.in.tds.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.ArticleMasterDetailedConditions;
import com.ey.in.tds.common.util.GlobalConstants;

/**
 * Spring Data repository for the ArticleMasterDetailedConditions entity.
 */
@Repository
public interface ArticleMasterDetailedConditionsRepository
		extends JpaRepository<ArticleMasterDetailedConditions, Long> {
	@Override
	// @formatter:off
	@Query(value = "select * from tds.article_master_detailed_conditions " 
			+ "where active=1 " 
			+ GlobalConstants.IN_EFFECT_QUERY
			+ "order by id DESC", nativeQuery = true)
	// @formatter:on
	public List<ArticleMasterDetailedConditions> findAll();

	@Override
	// @formatter:off
	@Query(value = "select * from tds.article_master_detailed_conditions " 
			+ "where active = 1 and id= ?"
			+ GlobalConstants.IN_EFFECT_QUERY, nativeQuery = true)
	// @formatter:on
	public Optional<ArticleMasterDetailedConditions> findById(Long id);
}
