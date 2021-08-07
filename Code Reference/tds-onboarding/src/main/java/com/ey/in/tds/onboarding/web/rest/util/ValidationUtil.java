package com.ey.in.tds.onboarding.web.rest.util;

import java.util.List;

import org.apache.commons.collections4.BidiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ey.in.tds.common.domain.Country;
import com.ey.in.tds.common.domain.dividend.ShareholderCategory;
import com.ey.in.tds.feign.client.MasterClientAdapter;

public final class ValidationUtil {

    private static final Logger log = LoggerFactory.getLogger(ValidationUtil.class);

    private ValidationUtil() {
    }

    public static boolean validateCountry(String country) {
        //log.info("Validating country name:{}", country);
        List<Country> countries = MasterClientAdapter.getCountries();
        return countries.stream().anyMatch(data -> data.getName().equalsIgnoreCase(country));
    }

    public static boolean validateShareholderCategory(String shareholderCategory) {
        //log.info("Validating shareholder category:{}", shareholderCategory);
        List<ShareholderCategory> shareholderCategories = MasterClientAdapter.getShareholderCategories();
        return shareholderCategories.stream().anyMatch(data -> data.getName().trim().equalsIgnoreCase(shareholderCategory.trim()));
    }

    public static String getShareholderTypeByPan(String shareHolderPan) {
        BidiMap<String, String> shareholderTypes = MasterClientAdapter.shareholderTypesNameByPanCode();
        String pan = "" + shareHolderPan.charAt(3);
        return shareholderTypes.get(pan);
    }

    public static boolean validateShareholderType(String shareholderType) {
        BidiMap<String, String> shareholderTypes = MasterClientAdapter.shareholderTypesNameByPanCode();
        String code = shareholderTypes.getKey(shareholderType);
        return code != null;
    }

    public static Boolean validateShareholderTypeAndPan(String shareholderType, String shareHolderPan) {
        BidiMap<String, String> shareholderTypes = MasterClientAdapter.shareholderTypesNameByPanCode();
        String pan = "" + shareHolderPan.charAt(3);
        return shareholderType.equals(shareholderTypes.get(pan));
    }

    public static boolean validateShareholderPan(String shareholderPan) {
        return shareholderPan.matches("[A-Z]{5}[0-9]{4}[A-Z]{1}");
    }

    public static boolean validateShareholderName(String shareholderName) {
        return shareholderName.matches("^[.\\p{Alnum}\\p{Space}&']{0,1024}$");
    }

    public static boolean validateShareholderTin(String shareholderTin) {
        return shareholderTin.matches("^[A-Z0-9]+$");
    }
}
