package com.ey.in.tds.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ey.in.tds.common.domain.CessTypeMaster;
import com.ey.in.tds.common.domain.Country;
import com.ey.in.tds.common.domain.DeductorType;
import com.ey.in.tds.common.domain.ModeOfPayment;
import com.ey.in.tds.common.domain.ResidentialStatus;
import com.ey.in.tds.common.domain.Status;
import com.ey.in.tds.common.domain.SubNaturePaymentMaster;
import com.ey.in.tds.common.domain.Tan;
import com.ey.in.tds.common.domain.dividend.DividendDeductorType;
import com.ey.in.tds.common.domain.dividend.DividendInstrumentsMapping;
import com.ey.in.tds.common.domain.dividend.DividendRateTreaty;
import com.ey.in.tds.common.domain.dividend.ShareholderCategory;
import com.ey.in.tds.common.domain.dividend.ShareholderExemptedCategory;
import com.ey.in.tds.common.domain.dividend.ShareholderType;
import com.ey.in.tds.common.domain.tcs.TCSCessTypeMaster;
import com.ey.in.tds.common.dto.CustomNatureDTO;
import com.ey.in.tds.common.dto.CustomNatureOfPaymentMasterDTO;
import com.ey.in.tds.common.dto.NatureAndSectionDTO;
import com.ey.in.tds.common.dto.SectionNatureDTO;
import com.ey.in.tds.common.repository.NatureOfPaymentMasterRepository;
import com.ey.in.tds.common.tcs.repository.TCSNatureOfIncomeRepository;
import com.ey.in.tds.dto.CessTypesDTO;
import com.ey.in.tds.dto.ResidentialStatusDTO;
import com.ey.in.tds.dto.StatusDTO;
import com.ey.in.tds.dto.SubNaturePaymentMasterDTO;
import com.ey.in.tds.repository.CessTypeMasterRepository;
import com.ey.in.tds.repository.CountryRepository;
import com.ey.in.tds.repository.DeductorTypeRepository;
import com.ey.in.tds.repository.DividendRateRepository;
import com.ey.in.tds.repository.DividendStaticDataRepository;
import com.ey.in.tds.repository.ModeOfPaymentRepository;
import com.ey.in.tds.repository.ResidentialStatusRepository;
import com.ey.in.tds.repository.StatusRepository;
import com.ey.in.tds.repository.SubNaturePaymentMasterRepository;
import com.ey.in.tds.repository.TanRepository;
import com.ey.in.tds.tcs.repository.CollecteeTypeRepository;
import com.ey.in.tds.tcs.repository.TCSCessTypeMasterRepository;
import com.ey.in.tds.tcs.repository.TCSStdCodesRepository;

@Service
@Transactional
public class CommonAPIService {

	@Autowired
	private ModeOfPaymentRepository modeOfPaymentRepository;

	@Autowired
	private DeductorTypeRepository deductorTypeRespository;

	@Autowired
	private ResidentialStatusRepository residentialStatusRepository;

	@Autowired
	private NatureOfPaymentMasterRepository natureOfPaymentMasterRepository;

	@Autowired
	private SubNaturePaymentMasterRepository subNaturePaymentMasterRepository;

	@Autowired
	private CountryRepository countryRepository;

	@Autowired
	private TanRepository tanRepository;

	@Autowired
	private CessTypeMasterRepository cessTypeMasterRepository;

	@Autowired
	private StatusRepository statusRepository;
	
	@Autowired
	private TCSCessTypeMasterRepository tcsCessTypeMasterRepository;
	
	@Autowired
	private TCSNatureOfIncomeRepository tcsNatureOfIncomeRepository;
	
	@Autowired
	private CollecteeTypeRepository collecteeTypeRepository;
	
	@Autowired
	private TCSStdCodesRepository tcsStdCodesRepository;

	@Autowired
    private DividendStaticDataRepository dividendStaticDataRepository;


    @Autowired
    private DividendRateRepository dividendRateRepository;

	/**
	 * Below method will return the list of section and nature along with id in
	 * Nature of payment
	 * 
	 * @return
	 */
	public List<CustomNatureDTO> findAllNatureOfPaymentMaster() {

		List<CustomNatureOfPaymentMasterDTO> nopList = natureOfPaymentMasterRepository.findAllNatureAndSection();
		List<CustomNatureDTO> natureOfPaymentList = new ArrayList<>();
		if (!nopList.isEmpty()) {
			for (CustomNatureOfPaymentMasterDTO nop : nopList) {
				CustomNatureDTO customNatureDTO = new CustomNatureDTO();
				customNatureDTO.setId(nop.getId());
				customNatureDTO.setNature(nop.getNature());
				customNatureDTO.setSection(nop.getSection());
				natureOfPaymentList.add(customNatureDTO);
			}
		}
		return natureOfPaymentList;

	}

	public List<SubNaturePaymentMasterDTO> findAllSubNatureOfPaymentMaster() {
		List<SubNaturePaymentMasterDTO> subNatureOfPaymentMasterDTOList = new ArrayList<>();
		SubNaturePaymentMasterDTO subNatureOfPaymentMasterDTO = null;
		List<SubNaturePaymentMaster> subNatureOfPaymentList = subNaturePaymentMasterRepository.findAll();
		for (SubNaturePaymentMaster subNaturePaymentMaster : subNatureOfPaymentList) {
			subNatureOfPaymentMasterDTO = new SubNaturePaymentMasterDTO();
			subNatureOfPaymentMasterDTO.setId(subNaturePaymentMaster.getId());
			subNatureOfPaymentMasterDTO.setNature(subNaturePaymentMaster.getNature());
			subNatureOfPaymentMasterDTO.setSection(subNaturePaymentMaster.getNaturePaymentMaster().getSection());
			subNatureOfPaymentMasterDTOList.add(subNatureOfPaymentMasterDTO);
		}

		return subNatureOfPaymentMasterDTOList;
	}

	public List<StatusDTO> getAllStatus() {
		List<StatusDTO> statusDTOList = new ArrayList<>();
		StatusDTO statusDTO = null;

		List<Status> statusList = statusRepository.findAll();

		for (Status status : statusList) {
			statusDTO = new StatusDTO();
			statusDTO.setId(status.getId());
			statusDTO.setPanCode(status.getPanCode());
			statusDTO.setStatus(status.getStatus());
			statusDTOList.add(statusDTO);
		}
		return statusDTOList;
	}

	public List<CessTypesDTO> getCessTypeMasters() {
		List<CessTypeMaster> cessTypeMasters = cessTypeMasterRepository.findAll();
		List<CessTypesDTO> cessTypeMastersList = new ArrayList<>();
		CessTypesDTO cessTypeMasterDTO = null;
		for (CessTypeMaster cessTypeMaster : cessTypeMasters) {
			cessTypeMasterDTO = new CessTypesDTO();
			cessTypeMasterDTO.setId(cessTypeMaster.getId());
			cessTypeMasterDTO.setCessType(cessTypeMaster.getCessType());
			cessTypeMastersList.add(cessTypeMasterDTO);
		}
		return cessTypeMastersList;
	}

	public List<Country> getCountries() {
		List<Country> countries = countryRepository.findAll();
		return countries;
	}

	public List<Tan> getTan(Long deductorId) {
		List<Tan> listTan = tanRepository.findTansById(deductorId);
		List<Tan> tanList = new ArrayList<>();

		Tan singleTan = null;
		for (Tan tan : listTan) {
			singleTan = new Tan();
			singleTan.setId(tan.getId());
			singleTan.setTan(tan.getTan());
			tanList.add(singleTan);
		}

		return tanList;
	}

	public List<ModeOfPayment> findAllModeOfPayments() {
		return modeOfPaymentRepository.findAll();
	}

	public List<DeductorType> findAllDeductorTypes() {
		return deductorTypeRespository.findAll();
	}

	public List<ResidentialStatusDTO> findAllResidentialStatuses() {
		List<ResidentialStatus> residentialStatusData = residentialStatusRepository.findAll();
		List<ResidentialStatusDTO> residentialStatusList = new ArrayList<>();
		for (ResidentialStatus residentialStatus : residentialStatusData) {
			ResidentialStatusDTO residentialStatusDTO = new ResidentialStatusDTO();
			residentialStatusDTO.setId(residentialStatus.getId());
			residentialStatusDTO.setStatus(residentialStatus.getStatus());
			residentialStatusList.add(residentialStatusDTO);
		}
		return residentialStatusList;
	}

	/**
	 * Below method returns deductor name based on tenant name
	 * 
	 * @param tenantName
	 * @return
	 */
	public String getDeductorName(String tenantName) {
		String deductorName = null;
		// deductorMasterRepository.findDeductorNameByTenantName(tenantName).trim();

		return deductorName;
	}

	/**
	 * Below method returns deductor name based on tan
	 * 
	 * @param tenantName
	 * @return
	 */
	//TODO NEED TO CHANGE FOR SQL
/*	public String getDeductorNameBasedonTan(String tan) {
		String deductorName = deductorMasterRepository.findDeductorNameByTan(tan);

		return deductorName;
	}  */

	public List<String> findAllNatureOfPaymentSections() {
		return natureOfPaymentMasterRepository.findAllNatureOfPaymentSections();
	}

	public List<CustomNatureOfPaymentMasterDTO> getNonResidentSections() {
		return natureOfPaymentMasterRepository.getNonResidentSections();
	}

	public List<CustomNatureOfPaymentMasterDTO> getResidentSections() {
		return natureOfPaymentMasterRepository.getResidentSections();
	}

	// To get Pan name based on pan code
	public String getPanNameByPanCode(String code) {
		String panName = null;
		Optional<Status> getPanByPanCode = statusRepository.findByPanCode(code);
		if (getPanByPanCode.isPresent()) {
			panName = getPanByPanCode.get().getStatus();
		}
		return panName;
	}
	
	/**
	 * 
	 * @return
	 */
	public List<String> getAllDeducteeStatus() {
		List<String> statusList = statusRepository.getAllDeducteeStatus();
		return statusList;
	}

	/**
	 * This method for get all country code.
	 */
	public List<String> getCountryCodes() {
		return countryRepository.getCountryCodes();
	}

	public List<SectionNatureDTO> getStatusbasedOnStatusAndResidentialStatus(String deducteeStatus,
			String deducteeResidentialStatus) {

		List<SectionNatureDTO> getSections = new ArrayList<SectionNatureDTO>();

		if (isNull(deducteeStatus) && !isNull(deducteeResidentialStatus)) {
			getSections = natureOfPaymentMasterRepository
					.getSectionsBasedOnResidentialStatus(deducteeResidentialStatus);
		} else if (!isNull(deducteeStatus) && isNull(deducteeResidentialStatus)) {
			getSections = natureOfPaymentMasterRepository.getSectionsBasedOnStatus(deducteeStatus);
		} else if (!isNull(deducteeStatus) && !isNull(deducteeResidentialStatus)) {
			getSections = natureOfPaymentMasterRepository
					.getSectionsBasedOnStatusIdAndDeducteeResidentialStatus(deducteeStatus, deducteeResidentialStatus);
		}
		return getSections;
	}

	public boolean isNull(String status) {
		if (status == null || status.equalsIgnoreCase("null")) {
			return true;
		} else {
			return false;
		}
	}

	public List<String> getNopBasedOnSection(String section, String residentalStatus, String deducteeStatus) {
		List<String> natures = new ArrayList<>();
		if (StringUtils.isNotBlank(section) && StringUtils.isNotBlank(residentalStatus)
				&& StringUtils.isNotBlank(deducteeStatus)) {
			natures = natureOfPaymentMasterRepository.getNOPBasedOnSectionAndResAndDeducteeStatus(section,
					residentalStatus, deducteeStatus);
			if (natures.isEmpty()) {
				natures = natureOfPaymentMasterRepository.getInactiveNopBasedOnSectionResDeducteeStatus(section,
						residentalStatus, deducteeStatus);
			}
		}
		return natures;
	}
	
	public List<String> getNopBasedOnSectionForEdit(String section, String residentalStatus, String deducteeStatus) {
		List<String> natures = new ArrayList<>();
		if (StringUtils.isNotBlank(section) && StringUtils.isNotBlank(residentalStatus)
				&& StringUtils.isNotBlank(deducteeStatus)) {
			natures = natureOfPaymentMasterRepository.getInactiveNopBasedOnSectionResDeducteeStatus(section,
					residentalStatus, deducteeStatus);
		}
		return natures;
	}
	/**
	 * 
	 * @return
	 */
	public List<CessTypesDTO> getTcsCessTypeMasters() {
		List<TCSCessTypeMaster> cessTypeMasters = tcsCessTypeMasterRepository.findAll();
		List<CessTypesDTO> cessTypeMastersList = new ArrayList<>();
		CessTypesDTO cessTypeMasterDTO = null;
		for (TCSCessTypeMaster cessTypeMaster : cessTypeMasters) {
			cessTypeMasterDTO = new CessTypesDTO();
			cessTypeMasterDTO.setId(cessTypeMaster.getId());
			cessTypeMasterDTO.setCessType(cessTypeMaster.getCessType());
			cessTypeMastersList.add(cessTypeMasterDTO);
		}
		return cessTypeMastersList;
	}
	/**
	 * 
	 * @param collecteeStatus
	 * @param collecteeIndicator
	 * @return
	 */
	public List<SectionNatureDTO> getSectionAndNOI(String collecteeStatus, Boolean collecteeIndicator) {
		List<SectionNatureDTO> getSections = new ArrayList<SectionNatureDTO>();
		String residentStatus = null;
		if (collecteeIndicator) {
			residentStatus = "NR";
		} else {
			residentStatus = "RES";
		}
		if (StringUtils.isBlank(collecteeStatus)) {
			getSections = tcsNatureOfIncomeRepository.getSectionsBasedOnCollecteeIndicator(residentStatus);
		} else {
			getSections = tcsNatureOfIncomeRepository.getSectionsBasedOnCollecteeIndicatorAndStatus(collecteeStatus,
					residentStatus);
		}
		return getSections;
	}
	/**
	 * 
	 * @param section
	 * @param collecteeIndicator
	 * @param collecteeStatus
	 * @return
	 */
	public List<String> getTcsNoiBasedOnSection(String section, Boolean collecteeIndicator, String collecteeStatus) {
		List<String> natures = new ArrayList<>();
		String residentStatus = null;
		if (collecteeIndicator) {
			residentStatus = "NR";
		} else {
			residentStatus = "RES";
		}
		if (StringUtils.isNotBlank(section) && StringUtils.isNotBlank(collecteeStatus)) {
			natures = tcsNatureOfIncomeRepository.getNOPBasedOnSectionAndResAndDeducteeStatus(section, residentStatus,
					collecteeStatus);
		}
		return natures;
	}
	
	/**
	 * 
	 * @return
	 */
	public List<CustomNatureOfPaymentMasterDTO> findAllTcsNatureOfIncome() {
		return tcsNatureOfIncomeRepository.findAllNatureAndSection();
	}

	/**
	 * 
	 * @param collecteeStatus
	 * @return
	 */
	public String getCollecteeType(String collecteeStatus) {
		String status = null;
		if(StringUtils.isNotBlank(collecteeStatus)) {
			status = collecteeTypeRepository.getCollecteeType(collecteeStatus);
		}
		return status;
	}
	/**
	 * This method get states based on country.
	 * @param country
	 * @return
	 */
	public List<String> findAllStates(String country) {
		List<String> states = new ArrayList<>();
		if ("INDIA".equalsIgnoreCase(country)) {
			return states = tcsStdCodesRepository.findAllStates(country);
		} else {
			return states;
		}
	}
	
	// --------------------- Dividend static data ---------------------------

    public List<DividendDeductorType> getAllDividendDeductorTypes() {
        return this.dividendStaticDataRepository.findAllDividendDeductorTypes();
    }

    public Optional<DividendDeductorType> getDividendDeductorTypeById(long id) {
        return this.dividendStaticDataRepository.findDividendDeductorTypeById(id);
    }

    public List<ShareholderType> getAllShareholderTypes() {
        return this.dividendStaticDataRepository.findAllShareholderTypes();
    }

    public Optional<ShareholderType> getShareholderTypeById(long id) {
        return this.dividendStaticDataRepository.findShareholderTypeById(id);
    }

    public List<ShareholderCategory> getAllShareholderCategories(final Optional<Boolean> exempted) {
        return this.dividendStaticDataRepository.findAllShareholderCategories(exempted);
    }

    public Optional<ShareholderCategory> getAllShareholderCategoryById(long id) {
        return this.dividendStaticDataRepository.findShareholderCategoryById(id);
    }

    public List<DividendInstrumentsMapping> getAllDividendInstrumentsMapping(Long dividendDeductorTypeId,
                                                                             Long shareholderCategoryId, String residentialStatus) {
        return this.dividendStaticDataRepository.getAllDividendInstrumentsMapping(dividendDeductorTypeId,
                shareholderCategoryId, residentialStatus);
    }


    public List<ShareholderExemptedCategory> getAllShareholderExemptedCategories() {
        return this.dividendStaticDataRepository.getAllShareholderExemptedCategories();
    }

    public Boolean getShareholderExemptedCategory(String dividendDeductorType, String shareholderCategory) {
        Boolean result = false;
        List<ShareholderExemptedCategory> allShareholderExemptedCategories = dividendStaticDataRepository.getAllShareholderExemptedCategories();
        List<DividendDeductorType> allDividendDeductorTypes = dividendStaticDataRepository.findAllDividendDeductorTypes();
        Optional<Boolean> aBoolean = allShareholderExemptedCategories.stream().filter(x -> x.getShareholderCategory().getName().equalsIgnoreCase(shareholderCategory))
                .filter(x -> x.getDividendDeductorType().getName().equalsIgnoreCase(dividendDeductorType))
                .map(x -> x.isExempted()).findAny();
        if (aBoolean.isPresent()) {
            result = aBoolean.get();
        }
        return result;
    }

    public DividendRateTreaty getDividendRateByCountryId(Country country) {
        List<DividendRateTreaty> list = dividendRateRepository.findDividendRateTreatyByCountryId(country);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    
	public List<String> getAllSection() {
		return natureOfPaymentMasterRepository.getAllSection();
	}
	
	public List<String> getAllNopBasedOnSection(String section) {
		return natureOfPaymentMasterRepository.getNopBasedOnSection(section);
	}
	
	/**
	 * 
	 * @param deducteeStatus
	 * @param deducteeResidentialStatus
	 * @return
	 */
	public List<NatureAndSectionDTO> getNatuesAndSections(String deducteeStatus,
			String deducteeResidentialStatus) {
		List<NatureAndSectionDTO> natueList = new ArrayList<>();
		List<SectionNatureDTO> getSections = new ArrayList<>();
		getSections = natureOfPaymentMasterRepository
				.getSectionsBasedOnStatusIdAndDeducteeResidentialStatus(deducteeStatus, deducteeResidentialStatus);
		for (SectionNatureDTO sections : getSections) {
			NatureAndSectionDTO section = new NatureAndSectionDTO();
			section.setNature(sections.getNature());
			section.setSection(sections.getSection());
			natueList.add(section);
		}
		return natueList;
	}

	public Map<String, String> getDeducteeStatus() {
		List<Status> statusList = statusRepository.findAll();
		Map<String, String> statusMap = new HashMap<>();

		for (Status status : statusList) {
			statusMap.put(status.getPanCode(), status.getStatus());
		}
		return statusMap;
	}

	
}
