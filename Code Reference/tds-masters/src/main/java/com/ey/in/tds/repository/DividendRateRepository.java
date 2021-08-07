package com.ey.in.tds.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.ey.in.tds.common.domain.Country;
import com.ey.in.tds.common.domain.dividend.CountrySpecificRules;
import com.ey.in.tds.common.domain.dividend.DividendRateAct;
import com.ey.in.tds.common.domain.dividend.DividendRateTreaty;
import com.ey.in.tds.common.domain.dividend.Range;

public interface DividendRateRepository {

    public DividendRateAct saveDividendRateAct(final DividendRateAct dividendRateAct);

    public Optional<DividendRateAct> findDividendRateActById(final Long id);

    public List<DividendRateAct> findAllDividendRateActs();

    public boolean isDuplicateDividendRateAct(long dividendDeductorTypeId, long shareholderCategoryId,
                                              String residentialStatus, String section, Instant applicableFrom, Instant applicableTo);

    public DividendRateTreaty saveDividendRateTreaty(final DividendRateTreaty dividendRateTreaty);

    public Optional<DividendRateTreaty> findDividendRateTreatyById(final Long id);

    public List<DividendRateTreaty> findAllDividendRateTreaties();

    public boolean isDuplicateDividendRateTreaty(Country country, String taxTreatyClause, Boolean mfnClauseExists,
                                                 Boolean mliArticle8Applicable, Boolean mliPptConditionSatisfied, Boolean mliSlobConditionSatisfied,
                                                 Range foreignCompShareholdingInIndComp, CountrySpecificRules countrySpecificRules, Instant applicableFrom,
                                                 Instant applicableTo,BigDecimal mfnNotAvailedCompanyTaxRate,BigDecimal mfnNotAvailedNonCompanyTaxRate);

    List<DividendRateTreaty> findDividendRateTreatyByCountryId(Country country);
}
