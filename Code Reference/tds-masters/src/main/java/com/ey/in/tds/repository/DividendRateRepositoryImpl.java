package com.ey.in.tds.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.ey.in.tds.common.domain.Country;
import com.ey.in.tds.common.domain.dividend.CountrySpecificRules;
import com.ey.in.tds.common.domain.dividend.DividendRateAct;
import com.ey.in.tds.common.domain.dividend.DividendRateTreaty;
import com.ey.in.tds.common.domain.dividend.Range;

@Repository
public class DividendRateRepositoryImpl extends AbstractJPA implements DividendRateRepository {

    @Override
    public DividendRateAct saveDividendRateAct(DividendRateAct dividendRateAct) {
        entityManager().persist(dividendRateAct);
        return dividendRateAct;
    }

    @Override
    public Optional<DividendRateAct> findDividendRateActById(Long id) {
        CriteriaBuilder criteriaBuilder = criteriaBuilder();
        CriteriaQuery<DividendRateAct> query = criteriaQuery(DividendRateAct.class);
        Root<DividendRateAct> root = query.from(DividendRateAct.class);
        query.where(criteriaBuilder.equal(root.get("id"), id));
        query.select(root);
        return getSingleResultSafely(query);
    }

    @Override
    public List<DividendRateAct> findAllDividendRateActs() {
        CriteriaBuilder criteriaBuilder = criteriaBuilder();
        CriteriaQuery<DividendRateAct> query = criteriaQuery(DividendRateAct.class);
        Root<DividendRateAct> root = query.from(DividendRateAct.class);
        query.where(criteriaBuilder.equal(root.get("active"), true));
        query.select(root);
        query.orderBy(criteriaBuilder.asc(root.get("dividendDeductorType").get("name")),
                criteriaBuilder.asc(root.get("shareholderCategory").get("name")),
                criteriaBuilder.asc(root.get("residentialStatus")));
        return getUnmodifiableResultList(query);
    }

    @Override
    public boolean isDuplicateDividendRateAct(long dividendDeductorTypeId, long shareholderCategoryId,
                                              String residentialStatus, String section, Instant applicableFrom, Instant applicableTo) {
        CriteriaBuilder criteriaBuilder = entityManager().getCriteriaBuilder();
        CriteriaQuery<Long> query = criteriaQuery(Long.class);
        Root<DividendRateAct> root = query.from(DividendRateAct.class);

        Predicate basicConditions = criteriaBuilder.and(
                criteriaBuilder.equal(root.get("dividendDeductorType").get("id").as(Long.class),
                        dividendDeductorTypeId),
                criteriaBuilder.equal(root.get("shareholderCategory").get("id").as(Long.class), shareholderCategoryId),
                criteriaBuilder.equal(root.get("residentialStatus"), residentialStatus),
                criteriaBuilder.equal(root.get("section"), section), criteriaBuilder.equal(root.get("active"), true));

        query.where(basicConditions,
                isDateRangeConflictingPredicate(criteriaBuilder, root, applicableFrom, applicableTo));

        query.select(criteriaBuilder.countDistinct(root));
        return typedQuery(query).getSingleResult().longValue() != 0;
    }

    @Override
    public DividendRateTreaty saveDividendRateTreaty(DividendRateTreaty dividendRateTreaty) {
        entityManager().persist(dividendRateTreaty);
        return dividendRateTreaty;
    }

    @Override
    public Optional<DividendRateTreaty> findDividendRateTreatyById(Long id) {
        CriteriaBuilder criteriaBuilder = criteriaBuilder();
        CriteriaQuery<DividendRateTreaty> query = criteriaQuery(DividendRateTreaty.class);
        Root<DividendRateTreaty> root = query.from(DividendRateTreaty.class);
        query.where(criteriaBuilder.equal(root.get("id"), id));
        query.select(root);
        return getSingleResultSafely(query);
    }

    @Override
    public List<DividendRateTreaty> findAllDividendRateTreaties() {
        CriteriaBuilder criteriaBuilder = criteriaBuilder();
        CriteriaQuery<DividendRateTreaty> query = criteriaQuery(DividendRateTreaty.class);
        Root<DividendRateTreaty> root = query.from(DividendRateTreaty.class);
        query.where(criteriaBuilder.equal(root.get("active"), true));
        query.select(root);
        query.orderBy(criteriaBuilder.asc(root.get("country").get("name")));
        return getUnmodifiableResultList(query);
    }

    @Override
    public boolean isDuplicateDividendRateTreaty(Country country, String taxTreatyClause, Boolean mfnClauseExists,
                                                 Boolean mliArticle8Applicable, Boolean mliPptConditionSatisfied, Boolean mliSlobConditionSatisfied,
                                                 Range foreignCompShareholdingInIndComp, CountrySpecificRules countrySpecificRules, Instant applicableFrom,
                                                 Instant applicableTo,BigDecimal mfnNotAvailedCompanyTaxRate,BigDecimal mfnNotAvailedNonCompanyTaxRate) {
        CriteriaBuilder criteriaBuilder = entityManager().getCriteriaBuilder();
        CriteriaQuery<DividendRateTreaty> query = criteriaQuery(DividendRateTreaty.class);
        Root<DividendRateTreaty> root = query.from(DividendRateTreaty.class);

        Predicate basicConditions = criteriaBuilder.and(criteriaBuilder.equal(root.get("country"), country),
                criteriaBuilder.equal(root.get("taxTreatyClause"), taxTreatyClause),
                criteriaBuilder.equal(root.get("mfnClauseExists"), mfnClauseExists),
                criteriaBuilder.equal(root.get("mliArticle8Applicable"), mliArticle8Applicable),
                criteriaBuilder.equal(root.get("mliPptConditionSatisfied"), mliPptConditionSatisfied),
                criteriaBuilder.equal(root.get("mliSlobConditionSatisfied"), mliSlobConditionSatisfied),
                criteriaBuilder.equal(root.get("foreignCompShareholdingInIndComp"), foreignCompShareholdingInIndComp),
                criteriaBuilder.equal(root.get("mfnNotAvailedCompanyTaxRate"), mfnNotAvailedCompanyTaxRate),
                criteriaBuilder.equal(root.get("mfnNotAvailedNonCompanyTaxRate"), mfnNotAvailedNonCompanyTaxRate),
                criteriaBuilder.equal(root.get("active"), true));

        query.where(basicConditions,
                isDateRangeConflictingPredicate(criteriaBuilder, root, applicableFrom, applicableTo));

        query.select(root);
        List<DividendRateTreaty> existingRecords = getUnmodifiableResultList(query);
        if (existingRecords.isEmpty()) {
            return false;
        } else {
            // Check if country specific rules matches
            for (DividendRateTreaty dividendRateTreaty : existingRecords) {
                if (CountrySpecificRules.SPECIFIC_RULES_COUNTRIES.contains(dividendRateTreaty.getCountry().getId())
                    /*
                     * && dividendRateTreaty.getCountrySpecificRules() != null &&
                     * countrySpecificRules != null
                     */) {
                    if (dividendRateTreaty.getCountry().getId().longValue() == 162
                            || dividendRateTreaty.getCountry().getId().longValue() == 31) {
                        // 162-PORTUGAL, 31-ZAMBIA
                        Range reqPeriodOfShareholding = countrySpecificRules != null && countrySpecificRules.getRules()
                                .get(CountrySpecificRules.Type.PERIOD_OF_SHAREHOLDING) != null
                                ? (Range) countrySpecificRules.getRules()
                                .get(CountrySpecificRules.Type.PERIOD_OF_SHAREHOLDING)
                                : null;
                        Range periodOfShareholding = dividendRateTreaty.getCountrySpecificRules() != null
                                && dividendRateTreaty.getCountrySpecificRules().getRules()
                                .get(CountrySpecificRules.Type.PERIOD_OF_SHAREHOLDING) != null
                                ? (Range) dividendRateTreaty.getCountrySpecificRules().getRules()
                                .get(CountrySpecificRules.Type.PERIOD_OF_SHAREHOLDING)
                                : null;
                        if ((periodOfShareholding != null && reqPeriodOfShareholding != null
                                && periodOfShareholding.equals(reqPeriodOfShareholding))
                                || (periodOfShareholding == null && reqPeriodOfShareholding == null)) {
                            return true;
                        }
                    } else if (dividendRateTreaty.getCountry().getId().longValue() == 8) { // 8-ICELAND
                        Range reqShareholding = countrySpecificRules != null && countrySpecificRules.getRules().get(
                                CountrySpecificRules.Type.SHAREHOLDING_IN_FOREIGN_COMPANY_BY_PERSONS_OTHER_THAN_INDIVIDUAL_RESIDENTS) != null
                                ? (Range) countrySpecificRules.getRules().get(
                                CountrySpecificRules.Type.SHAREHOLDING_IN_FOREIGN_COMPANY_BY_PERSONS_OTHER_THAN_INDIVIDUAL_RESIDENTS)
                                : null;
                        Range shareholding = dividendRateTreaty.getCountrySpecificRules() != null && dividendRateTreaty
                                .getCountrySpecificRules().getRules()
                                .get(CountrySpecificRules.Type.SHAREHOLDING_IN_FOREIGN_COMPANY_BY_PERSONS_OTHER_THAN_INDIVIDUAL_RESIDENTS) != null
                                ? (Range) dividendRateTreaty.getCountrySpecificRules().getRules().get(
                                CountrySpecificRules.Type.SHAREHOLDING_IN_FOREIGN_COMPANY_BY_PERSONS_OTHER_THAN_INDIVIDUAL_RESIDENTS)
                                : null;
                        Boolean reqFlag = countrySpecificRules != null && countrySpecificRules.getRules().get(
                                CountrySpecificRules.Type.IS_DIVIDEND_TAXABLE_AT_A_RATE_LOWER_THAN_CORPORATE_TAX_RATE) != null
                                ? (Boolean) countrySpecificRules.getRules().get(
                                CountrySpecificRules.Type.IS_DIVIDEND_TAXABLE_AT_A_RATE_LOWER_THAN_CORPORATE_TAX_RATE)
                                : null;
                        Boolean flag = dividendRateTreaty.getCountrySpecificRules() != null && dividendRateTreaty
                                .getCountrySpecificRules().getRules()
                                .get(CountrySpecificRules.Type.IS_DIVIDEND_TAXABLE_AT_A_RATE_LOWER_THAN_CORPORATE_TAX_RATE) != null
                                ? (Boolean) dividendRateTreaty.getCountrySpecificRules().getRules().get(
                                CountrySpecificRules.Type.IS_DIVIDEND_TAXABLE_AT_A_RATE_LOWER_THAN_CORPORATE_TAX_RATE)
                                : null;
                        if ((shareholding != null && reqShareholding != null && reqFlag != null && flag != null
                                && flag.booleanValue() == reqFlag.booleanValue()
                                && shareholding.equals(reqShareholding))
                                || (shareholding != null && reqShareholding != null
                                && shareholding.equals(reqShareholding))
                                || (reqFlag != null && flag != null && flag.booleanValue() == reqFlag.booleanValue())
                                || (shareholding == null && reqShareholding == null && reqFlag == null
                                && flag == null)) {
                            return true;
                        }
                    } else if (dividendRateTreaty.getCountry().getId().longValue() == 153) { // 153-KUWAIT
                        Boolean reqFlag = countrySpecificRules != null && countrySpecificRules.getRules().get(
                                CountrySpecificRules.Type.BENEFICIARY_IS_FOREIGN_GOVT_OR_POLITICAL_SUB_DIVISION_OR_A_LOCAL_AUTHORITY_OR_CENTRAL_BANK_OR_OTHER_GOVT_BODY) != null
                                ? (Boolean) countrySpecificRules.getRules().get(
                                CountrySpecificRules.Type.BENEFICIARY_IS_FOREIGN_GOVT_OR_POLITICAL_SUB_DIVISION_OR_A_LOCAL_AUTHORITY_OR_CENTRAL_BANK_OR_OTHER_GOVT_BODY)
                                : null;
                        Boolean flag = dividendRateTreaty.getCountrySpecificRules() != null && dividendRateTreaty
                                .getCountrySpecificRules().getRules()
                                .get(CountrySpecificRules.Type.BENEFICIARY_IS_FOREIGN_GOVT_OR_POLITICAL_SUB_DIVISION_OR_A_LOCAL_AUTHORITY_OR_CENTRAL_BANK_OR_OTHER_GOVT_BODY) != null
                                ? (Boolean) dividendRateTreaty.getCountrySpecificRules().getRules().get(
                                CountrySpecificRules.Type.BENEFICIARY_IS_FOREIGN_GOVT_OR_POLITICAL_SUB_DIVISION_OR_A_LOCAL_AUTHORITY_OR_CENTRAL_BANK_OR_OTHER_GOVT_BODY)
                                : null;
                        if ((reqFlag != null && flag != null && flag.booleanValue() == reqFlag.booleanValue()
                                || (reqFlag != null && flag != null))) {
                            return true;
                        }
                    } else if (dividendRateTreaty.getCountry().getId().longValue() == 153) { // 153-KUWAIT
                        Boolean reqFlag = countrySpecificRules != null && countrySpecificRules.getRules().get(
                                CountrySpecificRules.Type.DIVIDEND_DERIVED_FROM_IMMOVABLE_PROPERTY_BY_AN_INVESTMENT_VEHICLE_WHOSE_INCOME_FROM_SUCH_IMMOVABLE_PROPERTY_IS_EXEMPT_FROM_TAX) != null
                                ? (Boolean) countrySpecificRules.getRules().get(
                                CountrySpecificRules.Type.DIVIDEND_DERIVED_FROM_IMMOVABLE_PROPERTY_BY_AN_INVESTMENT_VEHICLE_WHOSE_INCOME_FROM_SUCH_IMMOVABLE_PROPERTY_IS_EXEMPT_FROM_TAX)
                                : null;
                        Boolean flag = dividendRateTreaty.getCountrySpecificRules() != null && dividendRateTreaty
                                .getCountrySpecificRules().getRules()
                                .get(CountrySpecificRules.Type.DIVIDEND_DERIVED_FROM_IMMOVABLE_PROPERTY_BY_AN_INVESTMENT_VEHICLE_WHOSE_INCOME_FROM_SUCH_IMMOVABLE_PROPERTY_IS_EXEMPT_FROM_TAX) != null
                                ? (Boolean) dividendRateTreaty.getCountrySpecificRules().getRules().get(
                                CountrySpecificRules.Type.DIVIDEND_DERIVED_FROM_IMMOVABLE_PROPERTY_BY_AN_INVESTMENT_VEHICLE_WHOSE_INCOME_FROM_SUCH_IMMOVABLE_PROPERTY_IS_EXEMPT_FROM_TAX)
                                : null;
                        if ((reqFlag != null && flag != null && flag.booleanValue() == reqFlag.booleanValue()
                                || (reqFlag != null && flag != null))) {
                            return true;
                        }
                    }
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public List<DividendRateTreaty> findDividendRateTreatyByCountryId(Country country) {
        CriteriaBuilder criteriaBuilder = criteriaBuilder();
        CriteriaQuery<DividendRateTreaty> query = criteriaQuery(DividendRateTreaty.class);
        Root<DividendRateTreaty> root = query.from(DividendRateTreaty.class);
        query.where(criteriaBuilder.equal(root.get("country"), country));
        query.select(root);
        return getUnmodifiableResultList(query);
    }

    private <T> Predicate isDateRangeConflictingPredicate(CriteriaBuilder criteriaBuilder, Root<T> root,
                                                          Instant applicableFrom, Instant applicableTo) {
        applicableFrom = applicableFrom.truncatedTo(ChronoUnit.DAYS);
        applicableTo = applicableTo != null ? applicableTo.truncatedTo(ChronoUnit.DAYS) : null;
        Predicate applicableToNotNull = criteriaBuilder.isNotNull(root.get("applicableTo"));
        Predicate p1 = criteriaBuilder.or(
                criteriaBuilder.between(root.get("applicableFrom"), root.get("applicableTo"),
                        criteriaBuilder.literal(applicableFrom)),
                applicableTo != null ? criteriaBuilder.between(root.get("applicableFrom"), root.get("applicableTo"),
                        criteriaBuilder.literal(applicableTo)) : criteriaBuilder.disjunction());

        Predicate applicableToIsNull = criteriaBuilder.isNull(root.get("applicableTo"));
        Predicate p2 = criteriaBuilder.or(
                criteriaBuilder.not(
                        criteriaBuilder.lessThan(criteriaBuilder.literal(applicableFrom), root.get("applicableFrom"))),
                applicableTo != null ? criteriaBuilder.not(
                        criteriaBuilder.lessThan(criteriaBuilder.literal(applicableTo), root.get("applicableFrom")))
                        : criteriaBuilder.disjunction());
        return criteriaBuilder.or(criteriaBuilder.and(applicableToNotNull, p1),
                criteriaBuilder.and(applicableToIsNull, p2));
    }

	
}
