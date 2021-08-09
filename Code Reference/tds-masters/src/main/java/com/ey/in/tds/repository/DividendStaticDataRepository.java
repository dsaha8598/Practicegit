package com.ey.in.tds.repository;

import java.util.List;
import java.util.Optional;

import com.ey.in.tds.common.domain.dividend.DividendDeductorType;
import com.ey.in.tds.common.domain.dividend.DividendInstrumentsMapping;
import com.ey.in.tds.common.domain.dividend.ShareholderCategory;
import com.ey.in.tds.common.domain.dividend.ShareholderExemptedCategory;
import com.ey.in.tds.common.domain.dividend.ShareholderType;
import com.ey.in.tds.common.dto.ExemptionDTO;

public interface DividendStaticDataRepository extends JPA{

    public List<DividendDeductorType> findAllDividendDeductorTypes();

    public Optional<DividendDeductorType> findDividendDeductorTypeById(long id);

    public List<ShareholderType> findAllShareholderTypes();

    public Optional<ShareholderType> findShareholderTypeById(long id);

    public List<ShareholderCategory> findAllShareholderCategories(final Optional<Boolean> exempted);

    public Optional<ShareholderCategory> findShareholderCategoryById(long id);

    public List<DividendInstrumentsMapping> getAllDividendInstrumentsMapping(Long dividendDeductorTypeId,
                                                                             Long shareholderCategoryId, String residentialStatus);

    public List<ShareholderExemptedCategory> getAllShareholderExemptedCategories();
    
    public List<ShareholderCategory> findAllShareholderCategory();
    
    public Long saveShareHolderCatagory(ShareholderCategory catagory);
    
    public Long saveDividendInstrumentsMapping(DividendInstrumentsMapping dividendInstrumentsMapping);
    
    public Long saveShareholderExemptedCategory(ShareholderExemptedCategory shareholderExemptedCategory);
   
}