package com.ey.in.tds.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.ArticleMasterConditions;
import com.ey.in.tds.common.util.GlobalConstants;

/**
 * Spring Data repository for the ArticleMasterConditions entity.
 */
@Repository
public interface ArticleMasterConditionsRepository extends JpaRepository<ArticleMasterConditions, Long> {
	@Override
	// @formatter:off
	@Query(value = "select * from tds.article_master_conditions " 
			+ "where active=1 " 
			+ GlobalConstants.IN_EFFECT_QUERY
			+ "order by id DESC", nativeQuery = true)
	// @formatter:on
	public List<ArticleMasterConditions> findAll();

	@Override
	// @formatter:off
	@Query(value = "select * from tds.article_master_conditions " 
			+ "where active = 1 and id= ?"
			+ GlobalConstants.IN_EFFECT_QUERY, nativeQuery = true)
	// @formatter:on
	public Optional<ArticleMasterConditions> findById(Long id);
}
