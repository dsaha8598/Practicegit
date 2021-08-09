package com.ey.in.tds.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.FilingDeductorCollector;

@Repository
public interface FilingDeductorCollectorRepository extends JpaRepository<FilingDeductorCollector, Long> {

	@Query(value = "select * from tds.filing_deductor_collector where active = 1 order by id desc", nativeQuery = true)
	@Override
	public List<FilingDeductorCollector> findAll();

	@Query(value = "select * from tds.filing_deductor_collector where id= ? and active = 1", nativeQuery = true)
	@Override
	public Optional<FilingDeductorCollector> findById(Long id);
	
	@Query(value="select category_value from tds.filing_deductor_collector where category_description=?  and active = 1", nativeQuery = true)
	public String getCatagoryValueByDescriptions(String catagoryDescription);  //getCatagoryValueByDescription
	
	@Query(value="select category_description from tds.filing_deductor_collector where category_value=?  and active = 1", nativeQuery = true)
	public String getCategoryDescriptionByValue(String categoryValue); 


}
