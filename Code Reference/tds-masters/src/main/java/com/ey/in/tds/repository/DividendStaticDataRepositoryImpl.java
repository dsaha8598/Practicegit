package com.ey.in.tds.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.dividend.DividendDeductorType;
import com.ey.in.tds.common.domain.dividend.DividendInstrumentsMapping;
import com.ey.in.tds.common.domain.dividend.ShareholderCategory;
import com.ey.in.tds.common.domain.dividend.ShareholderExemptedCategory;
import com.ey.in.tds.common.domain.dividend.ShareholderType;

@Repository
public class DividendStaticDataRepositoryImpl extends AbstractJPA implements DividendStaticDataRepository {

    @Override
    public List<DividendDeductorType> findAllDividendDeductorTypes() {
        CriteriaBuilder criteriaBuilder = criteriaBuilder();
        CriteriaQuery<DividendDeductorType> query = criteriaQuery(DividendDeductorType.class);
        Root<DividendDeductorType> root = query.from(DividendDeductorType.class);
        query.where(criteriaBuilder.equal(root.get("active"), true));
        query.select(root);
        query.orderBy(criteriaBuilder.asc(root.get("orderId")));
        return getUnmodifiableResultList(query);
    }

    @Override
    public Optional<DividendDeductorType> findDividendDeductorTypeById(long id) {
        CriteriaBuilder criteriaBuilder = criteriaBuilder();
        CriteriaQuery<DividendDeductorType> query = criteriaQuery(DividendDeductorType.class);
        Root<DividendDeductorType> root = query.from(DividendDeductorType.class);
        query.where(criteriaBuilder.equal(root.get("id"), id));
        query.select(root);
        return getSingleResultSafely(query);
    }

    @Override
    public List<ShareholderType> findAllShareholderTypes() {
        CriteriaBuilder criteriaBuilder = criteriaBuilder();
        CriteriaQuery<ShareholderType> query = criteriaQuery(ShareholderType.class);
        Root<ShareholderType> root = query.from(ShareholderType.class);
        query.where(criteriaBuilder.equal(root.get("active"), true));
        query.select(root);
        query.orderBy(criteriaBuilder.asc(root.get("orderId")));
        return getUnmodifiableResultList(query);
    }

    @Override
    public Optional<ShareholderType> findShareholderTypeById(long id) {
        CriteriaBuilder criteriaBuilder = criteriaBuilder();
        CriteriaQuery<ShareholderType> query = criteriaQuery(ShareholderType.class);
        Root<ShareholderType> root = query.from(ShareholderType.class);
        query.where(criteriaBuilder.equal(root.get("id"), id));
        query.select(root);
        return getSingleResultSafely(query);
    }

    @Override
    public List<ShareholderCategory> findAllShareholderCategories(final Optional<Boolean> exempted) {
        CriteriaBuilder criteriaBuilder = criteriaBuilder();
        CriteriaQuery<ShareholderCategory> query = criteriaQuery(ShareholderCategory.class);
        Root<ShareholderCategory> root = query.from(ShareholderCategory.class);
        if (exempted.isPresent()) {
            query.where(criteriaBuilder.equal(root.get("exempted"), exempted.get()),
                    criteriaBuilder.equal(root.get("active"), true));
        } else {
            query.where(criteriaBuilder.equal(root.get("active"), true));
        }
        query.select(root);
        query.orderBy(criteriaBuilder.asc(root.get("orderId")));
        return getUnmodifiableResultList(query);
    }

    public Optional<ShareholderCategory> findShareholderCategoryById(long id) {
        CriteriaBuilder criteriaBuilder = criteriaBuilder();
        CriteriaQuery<ShareholderCategory> query = criteriaQuery(ShareholderCategory.class);
        Root<ShareholderCategory> root = query.from(ShareholderCategory.class);
        query.where(criteriaBuilder.equal(root.get("id"), id));
        query.select(root);
        return getSingleResultSafely(query);
    }
    
    public List<ShareholderCategory> findAllShareholderCategory() {
        CriteriaBuilder criteriaBuilder = criteriaBuilder();
        CriteriaQuery<ShareholderCategory> query = criteriaQuery(ShareholderCategory.class);
        Root<ShareholderCategory> root = query.from(ShareholderCategory.class);
        query.where(criteriaBuilder.equal(root.get("active"), 1));
        query.select(root);
        return  getUnmodifiableResultList(query);
    }

    @Override
    public List<DividendInstrumentsMapping> getAllDividendInstrumentsMapping(Long dividendDeductorTypeId,
                                                                             Long shareholderCategoryId, String residentialStatus) {
        CriteriaBuilder criteriaBuilder = criteriaBuilder();
        CriteriaQuery<DividendInstrumentsMapping> query = criteriaQuery(DividendInstrumentsMapping.class);
        Root<DividendInstrumentsMapping> root = query.from(DividendInstrumentsMapping.class);
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(criteriaBuilder.equal(root.get("active"), true));
        if (dividendDeductorTypeId != null) {
            predicates.add(criteriaBuilder.or(criteriaBuilder.isNull(root.get("dividendDeductorType").get("id")),
                    criteriaBuilder.equal(root.get("dividendDeductorType").get("id"), dividendDeductorTypeId)));
        }
        if (shareholderCategoryId != null) {
            predicates.add(criteriaBuilder.equal(root.get("shareholderCategory").get("id"), shareholderCategoryId));
        }
        if (residentialStatus != null && !residentialStatus.isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("residentialStatus"), residentialStatus));
        }
        query.select(root);
        query.where(predicates.toArray(new Predicate[predicates.size()]));
        return getUnmodifiableResultList(query);
    }

    @Override
    public List<ShareholderExemptedCategory> getAllShareholderExemptedCategories() {
        CriteriaBuilder criteriaBuilder = criteriaBuilder();
        CriteriaQuery<ShareholderExemptedCategory> query = criteriaQuery(ShareholderExemptedCategory.class);
        Root<ShareholderExemptedCategory> root = query.from(ShareholderExemptedCategory.class);
        query.select(root);
        return getUnmodifiableResultList(query);
    }
    
    @Override
    public Long saveShareHolderCatagory(ShareholderCategory catagory) {
    	 super.entityManager().persist(catagory);
    	 return catagory.getId();
    	
    }
    @Override
    public Long saveDividendInstrumentsMapping(DividendInstrumentsMapping dividendInstrumentsMapping) {
    	 super.entityManager().persist(dividendInstrumentsMapping);
    	 return dividendInstrumentsMapping.getId();    }
    
    @Override
    public Long saveShareholderExemptedCategory(ShareholderExemptedCategory shareholderExemptedCategory) {
    	 super.entityManager().persist(shareholderExemptedCategory);
    	 return shareholderExemptedCategory.getId();
    }
    
}
