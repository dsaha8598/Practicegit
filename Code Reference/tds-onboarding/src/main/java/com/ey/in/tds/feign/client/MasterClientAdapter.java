package com.ey.in.tds.feign.client;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ey.in.tds.common.domain.Country;
import com.ey.in.tds.common.domain.dividend.DividendDeductorType;
import com.ey.in.tds.common.domain.dividend.DividendRateAct;
import com.ey.in.tds.common.domain.dividend.DividendRateTreaty;
import com.ey.in.tds.common.domain.dividend.ShareholderCategory;
import com.ey.in.tds.common.domain.dividend.ShareholderType;

@Component
public class MasterClientAdapter {

	private static MastersClient mastersClient;

	@Autowired
	public MasterClientAdapter(MastersClient mastersClient) {
		MasterClientAdapter.mastersClient = mastersClient;
	}

	private static List<Country> countries = null;

	public static List<Country> getCountries() {
		return countries == null ? countries = mastersClient.getCountries().getBody().getData() : countries;
	}

	private static Map<String, Country> countriesByName = null;

	public static Map<String, Country> countriesByName() {
		return countriesByName == null
				? countriesByName = getCountries().stream().collect(Collectors.toMap(Country::getName, x -> x))
				: countriesByName;
	}

	private static List<DividendDeductorType> dividendDeductorTypes = null;

	public static List<DividendDeductorType> getDividendDeductorTypes() {
		return dividendDeductorTypes == null
				? dividendDeductorTypes = mastersClient.getDividendDeductorTypes().getBody().getData()
				: dividendDeductorTypes;
	}

	private static Map<String, DividendDeductorType> dividendDeductorTypesByName = null;

	public static Map<String, DividendDeductorType> dividendDeductorTypesByName() {
		return dividendDeductorTypesByName == null
				? dividendDeductorTypesByName = getDividendDeductorTypes().stream()
						.collect(Collectors.toMap(DividendDeductorType::getName, x -> x))
				: dividendDeductorTypesByName;
	}

	private static List<ShareholderType> shareholderTypes = null;

	public static List<ShareholderType> getShareholderTypes() {
		return shareholderTypes == null ? shareholderTypes = mastersClient.getShareholderTypes().getBody().getData()
				: shareholderTypes;
	}

	private static Map<String, ShareholderType> shareholderTypesByName = null;

	public static Map<String, ShareholderType> shareholderTypesByName() {
		return shareholderTypesByName == null
				? shareholderTypesByName = getShareholderTypes().stream()
						.collect(Collectors.toMap(ShareholderType::getName, x -> x))
				: shareholderTypesByName;
	}

	private static BidiMap<String, String> shareholderTypeNameByPanCode = null;

	public static BidiMap<String, String> shareholderTypesNameByPanCode() {
		if (shareholderTypeNameByPanCode != null) {
			return shareholderTypeNameByPanCode;
		} else {
			Map<String, String> temp = getShareholderTypes().stream()
					.collect(Collectors.toMap(ShareholderType::getCode, ShareholderType::getName));
			shareholderTypeNameByPanCode = new DualHashBidiMap<>();
			temp.entrySet().forEach(e -> {
				shareholderTypeNameByPanCode.put(e.getKey(), e.getValue());
			});
			return shareholderTypeNameByPanCode;
		}
	}

	private static List<ShareholderCategory> shareholderCategories = null;

	public static List<ShareholderCategory> getShareholderCategories() {
		return shareholderCategories == null
				? shareholderCategories = mastersClient.getShareholderCategories().getBody().getData()
				: shareholderCategories;
	}

	private static Map<String, ShareholderCategory> shareholderCategoriesByName = null;

	public static Map<String, ShareholderCategory> shareholderCategoriesByName() {
		return shareholderCategoriesByName == null
				? shareholderCategoriesByName = getShareholderCategories().stream()
						.collect(Collectors.toMap(ShareholderCategory::getName, x -> x))
				: shareholderCategoriesByName;
	}

	private static List<DividendRateAct> dividendRateActs = null;

	public static List<DividendRateAct> getAllDividendRateActs() {
		return dividendRateActs == null ? dividendRateActs = mastersClient.getAllDividendRateActs().getBody().getData()
				: dividendRateActs;
//		return mastersClient.getAllDividendRateActs().getBody().getData();
	}

//	private static Map<Triple<String, String, String>, DividendRateAct> dividendRateActsMap = null;
//
//	public static Map<Triple<String, String, String>, DividendRateAct> dividendRateActsMap() {
//		return dividendRateActsMap == null
//				? dividendRateActsMap = getAllDividendRateActs().stream()
//						.collect(
//								Collectors.toMap(
//										x -> ImmutableTriple.of(x.getDividendDeductorType().getName(),
//												x.getShareholderCategory().getName(), x.getResidentialStatus()),
//										x -> x))
//				: dividendRateActsMap;
//	}

	public static Optional<DividendRateAct> getDividendRateAct(String dividendDeductorType, String shareholderCategory,
			String residentialStatus, Instant dividendDate) {
		dividendDate = dividendDate.truncatedTo(ChronoUnit.DAYS);
		for (DividendRateAct dividendRateAct : getAllDividendRateActs()) {
			if (dividendRateAct.getDividendDeductorType().getName().equalsIgnoreCase(dividendDeductorType)
					&& dividendRateAct.getShareholderCategory().getName().equalsIgnoreCase(shareholderCategory)
					&& dividendRateAct.getResidentialStatus().equalsIgnoreCase(residentialStatus)) {
				boolean matchingAct = dividendRateAct.getApplicableTo() != null
						? !dividendDate.isBefore(dividendRateAct.getApplicableFrom())
								&& !dividendDate.isAfter(dividendRateAct.getApplicableTo())
						: !dividendDate.isBefore(dividendRateAct.getApplicableFrom());
				if (dividendRateAct.getApplicableTo() != null) {
					if (matchingAct) {
						return Optional.of(dividendRateAct);
					}
				}
			}
		}
		return Optional.empty();
	}

	private static List<DividendRateTreaty> dividendRateTreaties = null;

	public static List<DividendRateTreaty> getAllDividendRateTreaties() {
		return dividendRateTreaties == null
				? dividendRateTreaties = mastersClient.getAllDividendRateTreaties().getBody().getData()
				: dividendRateTreaties;
//		return mastersClient.getAllDividendRateTreaties().getBody().getData();
	}

	private static Map<String, DividendRateTreaty> dividendRateTreatiesByCountryName = null;

	public static Map<String, DividendRateTreaty> dividendRateTreatiesByCountryName() {
		return dividendRateTreatiesByCountryName == null
				? dividendRateTreatiesByCountryName = getAllDividendRateTreaties().stream()
						.collect(Collectors.toMap(x -> x.getCountry().getName(), x -> x))
				: dividendRateTreatiesByCountryName;
	}
}
