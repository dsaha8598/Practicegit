package com.ey.in.tds.dividend.forms.builder.ca;

import com.ey.in.tds.dividend.common.Form15CAData;
import com.ey.in.tds.dividend.common.Form15DataUtil;
import com.ey.in.tds.dividend.common.Form15XmlUtil;
import com.ey.in.tds.dividend.common.FormGenerationException;
import com.ey.in.tds.dividend.fifteen.ca.*;
import com.ey.in.tds.dividend.forms.builder.Generatable;
import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import java.io.File;
import java.math.BigDecimal;
import java.util.Date;

public class Form15CABuilder implements Form15CAPartBuilder, RemitterPartABuilder, RemitterPartBBuilder,
        RemitterPartCBuilder, RemitteePartABuilder, RemitteePartBBuilder, RemitteePartCBuilder, RemittancePartABuilder,
        RemittancePartBBuilder, RemittancePartCBuilder, AOOrderBuilder, ITActBuilder, DTAABuilder, RemittanceRoyaltyDividendBuilder,
        RemittanceBusinessIncomeBuilder, RemittanceCapitalGainsBuilder, RemittanceMisc, TDSBuilder, AccountantBuilder,
        DeclarationBuilder, Generatable {

    //	private final static QName _Declaration_QNAME = new QName("http://incometaxindiaefiling.gov.in/FORM15CAB",
//			"Declaration");
    private final static QName _FORM15CA_QNAME = new QName("http://incometaxindiaefiling.gov.in/FORM15CAB", "FORM15CA");

    private ObjectFactory objectFactory;

    private FORM15CA form15CA;

    private File sourcefile;
    private String fileName;

    public Form15CABuilder(int assessmentYear, String fileName) {
        this.fileName = fileName;
        this.objectFactory = new ObjectFactory();

        CreationInfo creationInfo = this.objectFactory.createCreationInfo();
        creationInfo.setSWCreatedBy("ITD_JAVA_UTILITY");
        creationInfo.setXMLCreatedBy("ITD_JAVA_UTILITY");
        creationInfo.setIntermediaryCity("Delhi");
        creationInfo.setSWVersionNo("1.0");
        creationInfo.setXMLCreationDate(Form15DataUtil.nowXmlDate());

        FormDetails formDetails = this.objectFactory.createFormDetails();
        formDetails.setAssessmentYear("2017");
        formDetails.setDescription("FORM15CA");
        formDetails.setFormName("FORM15CA");
        formDetails.setSchemaVer("Ver1.0");
        formDetails.setFormVer(BigDecimal.valueOf(1.1));

        this.form15CA = this.objectFactory.createFORM15CA();
        this.form15CA.setCreationInfo(creationInfo);
        this.form15CA.setFormDetails(formDetails);
    }

    public Form15CABuilder(FORM15CA form15CA, String fileName) {
        this.form15CA = form15CA;
        this.fileName = fileName;
    }

    @Override
    public RemitterPartABuilder partA() {
        this.form15CA.setPartType(PartType.A.typeName());
        return this;
    }

    @Override
    public RemitterPartBBuilder partB() {
        this.form15CA.setPartType(PartType.B.typeName());
        return this;
    }

    @Override
    public RemitterPartCBuilder partC() {
        this.form15CA.setPartType(PartType.C.typeName());
        return this;
    }

    @Override
    public RemitteePartABuilder remitterPA(String name, String pan, String tan, String email, String phone,
                                           String status, String domestic, final String flatDoorBuilding, String roadStreet,
                                           String premisesBuildingVillage, final String townCityDistrict, final String areaLocality,
                                           final String zipCode, final String state, final String country) {
        RemitterDetls remitterDetails = this.objectFactory.createRemitterDetls();
        Form15CAData form15CAData = new Form15CAData();
        remitterDetails.setNameRemitter(name);
        remitterDetails.setPAN(pan);
        remitterDetails.setTan(tan);
        remitterDetails.setEmailRemitter(email);
        remitterDetails.setPhoneRemitter(phone);
        remitterDetails.setStatus(form15CAData.getRemitterStatus(status).split("_")[1]);
        remitterDetails.setDomesticFlg(form15CAData.getDomesticFlag(domestic).split("_")[1]);

        AddressType address = this.objectFactory.createAddressType();
        address.setFlatDoorBuilding(flatDoorBuilding);
        address.setRoadStreet(roadStreet);
        address.setPremisesBuildingVillage(premisesBuildingVillage);
        address.setTownCityDistrict(townCityDistrict);
        address.setAreaLocality(areaLocality);
        address.setZipCode(zipCode);
        address.setPincode(zipCode);
        address.setState(form15CAData.getStateCodeValue(state).split("_")[1]);
        address.setCountry(form15CAData.getCountryCodeValue(country).split("_")[1]);
        remitterDetails.setRemitterAddrs(address);

        this.form15CA.setRemitterDetls(remitterDetails);
        return this;
    }

    @Override
    public RemitteePartBBuilder remitterPB(String name, String pan, String tan, String email, String phone,
                                           String status, String domestic, String flatDoorBuilding, String roadStreet, String premisesBuildingVillage,
                                           String townCityDistrict, String areaLocality, String zipCode, String state, String country) {
        RemitterDetls remitterDetails = this.objectFactory.createRemitterDetls();
        Form15CAData form15CAData = new Form15CAData();
        remitterDetails.setNameRemitter(name);
        remitterDetails.setPAN(pan);
        remitterDetails.setTan(tan);
        remitterDetails.setEmailRemitter(email);
        remitterDetails.setPhoneRemitter(phone);
        remitterDetails.setStatus(form15CAData.getRemitterStatus(status).split("_")[1]);
        remitterDetails.setDomesticFlg(form15CAData.getDomesticFlag(domestic).split("_")[1]);

        AddressType address = this.objectFactory.createAddressType();
        address.setFlatDoorBuilding(flatDoorBuilding);
        address.setRoadStreet(roadStreet);
        address.setPremisesBuildingVillage(premisesBuildingVillage);
        address.setTownCityDistrict(townCityDistrict);
        address.setAreaLocality(areaLocality);
        address.setPincode(zipCode);
        address.setZipCode(zipCode);
        address.setState(form15CAData.getStateCodeValue(state).split("_")[1]);
        address.setCountry(form15CAData.getCountryCodeValue(country).split("_")[1]);
        remitterDetails.setRemitterAddrs(address);

        this.form15CA.setRemitterDetls(remitterDetails);
        return this;
    }

    @Override
    public RemitteePartCBuilder remitterPC(String name, String pan, String tan, String email, String phone,
                                           String status, String domestic, String priciplePlaceOfBiz, String ariaCode, String aoType, String rangeCode,
                                           String aoNumber, String flatDoorBuilding, String roadStreet, String premisesBuildingVillage,
                                           String townCityDistrict, String areaLocality, String zipCode, String state, String country) {
        RemitterDetls remitterDetails = this.objectFactory.createRemitterDetls();
        Form15CAData form15CAData = new Form15CAData();
        remitterDetails.setNameRemitter(name);
        remitterDetails.setPAN(pan);
        remitterDetails.setTan(tan);
        remitterDetails.setEmailRemitter(email);
        remitterDetails.setPhoneRemitter(phone);
        remitterDetails.setStatus(form15CAData.getRemitterStatus(status).split("_")[1]);
        remitterDetails.setDomesticFlg(form15CAData.getDomesticFlag(domestic).split("_")[1]);
        remitterDetails.setPrincPlcBusRemter(priciplePlaceOfBiz);

        AddressType address = this.objectFactory.createAddressType();
        address.setFlatDoorBuilding(flatDoorBuilding);
        address.setRoadStreet(roadStreet);
        address.setPremisesBuildingVillage(premisesBuildingVillage);
        address.setTownCityDistrict(townCityDistrict);
        address.setAreaLocality(areaLocality);
        address.setPincode(zipCode);
        address.setState(form15CAData.getStateCodeValue(state).split("_")[1]);
        address.setCountry(form15CAData.getCountryCodeValue(country).split("_")[1]);
        remitterDetails.setRemitterAddrs(address);

        this.form15CA.setRemitterDetls(remitterDetails);
        return this;
    }

    @Override
    public RemittancePartABuilder remiteePA(String name, String pan, String email, String phone,
                                            String remittanceMadeInCountry, final String flatDoorBuilding, String roadStreet,
                                            String premisesBuildingVillage, final String townCityDistrict, final String areaLocality,
                                            final String zipCode, final String state, final String country) {
        RemitteeDetls remitteeDetails = this.objectFactory.createRemitteeDetls();
        Form15CAData form15CAData = new Form15CAData();
        remitteeDetails.setNameRemittee(name);
        if (!StringUtils.isBlank(pan))
            remitteeDetails.setPanRemittee(pan);
        if (!StringUtils.isBlank(email))
            remitteeDetails.setEmailRemittee(email);
        if (!StringUtils.isBlank(phone))
            remitteeDetails.setPhoneRemittee(phone);
        remitteeDetails.setCountryRemMade(form15CAData.getCountryCodeValue(remittanceMadeInCountry).split("_")[1]);
        if (remitteeDetails.getCountryRemMade().equalsIgnoreCase("9999")) {
            remitteeDetails.setCountryRemMadeDesc(remittanceMadeInCountry);
        }

        AddressType address = this.objectFactory.createAddressType();
        address.setFlatDoorBuilding(flatDoorBuilding);
        address.setRoadStreet(roadStreet);
        address.setPremisesBuildingVillage(premisesBuildingVillage);
        address.setTownCityDistrict(townCityDistrict);
        address.setAreaLocality(areaLocality);
        address.setZipCode(zipCode);
        address.setState(form15CAData.getStateCodeValue(state).split("_")[1]);
        address.setCountry(form15CAData.getCountryCodeValue(country).split("_")[1]);
        remitteeDetails.setRemitteeAddrs(address);

        this.form15CA.setRemitteeDetls(remitteeDetails);
        return this;
    }

    @Override
    public RemittancePartBBuilder remiteePB(String name, String pan, String email, String phone,
                                            String remittanceMadeInCountry, String flatDoorBuilding, String roadStreet, String premisesBuildingVillage,
                                            String townCityDistrict, String areaLocality, String zipCode, String state, String country) {
        RemitteeDetls remitteeDetails = this.objectFactory.createRemitteeDetls();
        Form15CAData form15CAData = new Form15CAData();
        remitteeDetails.setNameRemittee(name);
        if (!StringUtils.isBlank(pan))
            remitteeDetails.setPanRemittee(pan);
        if (!StringUtils.isBlank(email))
            remitteeDetails.setEmailRemittee(email);
        if (!StringUtils.isBlank(phone))
            remitteeDetails.setPhoneRemittee(phone);
        remitteeDetails.setCountryRemMade(form15CAData.getCountryCodeValue(remittanceMadeInCountry).split("_")[1]);

        AddressType address = this.objectFactory.createAddressType();
        address.setFlatDoorBuilding(flatDoorBuilding);
        address.setRoadStreet(roadStreet);
        address.setPremisesBuildingVillage(premisesBuildingVillage);
        address.setTownCityDistrict(townCityDistrict);
        address.setAreaLocality(areaLocality);
        address.setZipCode(zipCode);
        address.setState(form15CAData.getStateCodeValue(state).split("_")[1]);
        address.setCountry(form15CAData.getCountryCodeValue(country).split("_")[1]);
        remitteeDetails.setRemitteeAddrs(address);

        this.form15CA.setRemitteeDetls(remitteeDetails);
        return this;
    }

    @Override
    public RemittancePartCBuilder remiteePC(String name, String pan, String email, String phone, String status,
                                            String principlePlaceOfBiz, String flatDoorBuilding, String roadStreet, String premisesBuildingVillage,
                                            String townCityDistrict, String areaLocality, String zipCode, String state, String country) {
        RemitteeDetls remitteeDetails = this.objectFactory.createRemitteeDetls();
        Form15CAData form15CAData = new Form15CAData();
        remitteeDetails.setNameRemittee(name);
        if (!StringUtils.isBlank(pan))
            remitteeDetails.setPanRemittee(pan);
        if (!StringUtils.isBlank(email))
            remitteeDetails.setEmailRemittee(email);
        if (!StringUtils.isBlank(phone))
            remitteeDetails.setPhoneRemittee(phone);
        remitteeDetails.setStatusRemittee(form15CAData.getRemitterStatus(status).split("_")[1]);
        remitteeDetails.setPrincPlcBusRemtee(principlePlaceOfBiz);

        AddressType address = this.objectFactory.createAddressType();
        address.setFlatDoorBuilding(flatDoorBuilding);
        address.setRoadStreet(roadStreet);
        address.setPremisesBuildingVillage(premisesBuildingVillage);
        address.setTownCityDistrict(townCityDistrict);
        address.setAreaLocality(areaLocality);
        address.setZipCode(zipCode);
        address.setState("99");
        address.setCountry(form15CAData.getCountryCodeValue(country).split("_")[1]);
        remitteeDetails.setRemitteeAddrs(address);
        this.form15CA.setRemitteeDetls(remitteeDetails);
        return this;
    }

    @Override
    public DeclarationBuilder remittance(BigDecimal amountWithoutTDS, BigDecimal accumulatedAmountInFY,
                                         String bankNameCode, String bankBranch, Date proposedDateOfRemittance, String natureOfRemittance,
                                         BigDecimal amountOfTDS, BigDecimal rateOfTDS, Date dateOfDeduction, String releventPurposeCategoryRBI,
                                         String releventPurposeCodeRBI) {
        RemittanceDetls remittanceDetails = this.objectFactory.createRemittanceDetls();
        Form15CAData form15CAData = new Form15CAData();
        remittanceDetails.setAmtPayBefTds(amountWithoutTDS);
        remittanceDetails.setAggAmtRem(accumulatedAmountInFY);
        remittanceDetails.setNameBankCode(form15CAData.getBankCode15CBValue(bankNameCode).split("_")[1]);
        if (remittanceDetails.getNameBankCode() != null && remittanceDetails.getNameBankCode().equals("999")) {
            remittanceDetails.setNameBankDesc(bankNameCode);
            remittanceDetails.setNameBankCode("999");
        }
        remittanceDetails.setBranchName(bankBranch);
        remittanceDetails.setPropDateRem(Form15DataUtil.toXmlDate(proposedDateOfRemittance));
        remittanceDetails.setNatureRemCategory("16.16");
        remittanceDetails.setAmtTaxDeductn(amountOfTDS);
        remittanceDetails.setRateofTDS(rateOfTDS);
        remittanceDetails.setDateOfDeductn(Form15DataUtil.toXmlDate(dateOfDeduction));
        remittanceDetails.setPropDateRem(Form15DataUtil.toXmlDate(proposedDateOfRemittance));
        remittanceDetails.setRevPurCategory("RB-14.1");
        remittanceDetails.setRevPurCode("RB-14.1-" + releventPurposeCodeRBI);
        this.form15CA.setRemittanceDetls(remittanceDetails);
        return this;
    }

    @Override
    public AOOrderBuilder remittance(String countryRemittanceMade, String currencyRemittanceMade,
                                     BigDecimal amountPayableInForegnCurrency, BigDecimal amountPayableInINR, String bankNameCode,
                                     String bankBranch, String bankBranchBSRCode, Date proposedDateOfRemittance, String natureOfRemittance,
                                     BigDecimal amountOfTDS, BigDecimal rateOfTDS, Date dateOfDeduction, String releventPurposeCategoryRBI,
                                     String releventPurposeCodeRBI) {
        RemittanceDetls remittanceDetails = this.objectFactory.createRemittanceDetls();
        Form15CAData form15CAData = new Form15CAData();
        remittanceDetails.setCountryRemMadeSecb(form15CAData.getCountryRemCodeValue(countryRemittanceMade).split("_")[1]);
        if (remittanceDetails.getCountryRemMadeSecb() != null && remittanceDetails.getCountryRemMadeSecb().equals("9999")) {
            remittanceDetails.setCountryRemMadeSecbDesc(countryRemittanceMade);
            remittanceDetails.setCountryRemMadeSecb("9999");
        }
        remittanceDetails.setCurrencySecbCode(form15CAData.getCurrencyCodeValue(currencyRemittanceMade).split("_")[1]);
        if (remittanceDetails.getCurrencySecbCode() != null && remittanceDetails.getCurrencySecbCode().equals("99")) {
            remittanceDetails.setCurrencySecbDesc(currencyRemittanceMade);
            remittanceDetails.setCurrencySecbCode("99");
        }
        remittanceDetails.setNameBankCode(form15CAData.getBankCode15CBValue(bankNameCode).split("_")[1]);
        if (remittanceDetails.getNameBankCode() != null && remittanceDetails.getNameBankCode().equals("999")) {
            remittanceDetails.setNameBankDesc(bankNameCode);
            remittanceDetails.setNameBankCode("999");
        }
        remittanceDetails.setBranchName(bankBranch);
        remittanceDetails.setBsrCode(bankBranchBSRCode);
        remittanceDetails.setAmtPayForgnRem(amountPayableInForegnCurrency);
        remittanceDetails.setAmtPayIndRem(amountPayableInINR);
        remittanceDetails.setPropDateRem(Form15DataUtil.toXmlDate(proposedDateOfRemittance));
        remittanceDetails.setNatureRemCategory("16.16");
        remittanceDetails.setAmtTaxDeductn(amountOfTDS);
        remittanceDetails.setRateofTDS(rateOfTDS);
        remittanceDetails.setDateOfDeductn(Form15DataUtil.toXmlDate(dateOfDeduction));
        remittanceDetails.setPropDateRem(Form15DataUtil.toXmlDate(proposedDateOfRemittance));
        remittanceDetails.setRevPurCategory("RB-14.1");
        remittanceDetails.setRevPurCode("RB-14.1-" + releventPurposeCodeRBI);
        this.form15CA.setRemittanceDetls(remittanceDetails);
        return this;
    }

    @Override
    public ITActBuilder remittance(String countryRemittanceMade, String currencyRemittanceMade,
                                   BigDecimal amountPayableInForegnCurrency, BigDecimal amountPayableInINR, String bankNameCode,
                                   String bankBranch, String bankBranchBSRCode, Date proposedDateOfRemittance, String natureOfRemittance,
                                   String releventPurposeCategoryRBI, String releventPurposeCodeRBI, String taxPayableGrossedUp) {
        RemittanceDetls remittanceDetails = this.objectFactory.createRemittanceDetls();
        Form15CAData form15CAData = new Form15CAData();
        remittanceDetails.setCountryRemMadeSecb(form15CAData.getCountryCodeValue(countryRemittanceMade).split("_")[1]);
        if (remittanceDetails.getCountryRemMadeSecb() != null && remittanceDetails.getCountryRemMadeSecb().equals("9999")) {
            remittanceDetails.setCountryRemMadeSecbDesc(countryRemittanceMade);
            remittanceDetails.setCountryRemMadeSecb("9999");
        }
        remittanceDetails.setCurrencySecbCode(form15CAData.getCurrencyCodeValue(currencyRemittanceMade).split("_")[1]);
        if (remittanceDetails.getCurrencySecbCode() != null && remittanceDetails.getCurrencySecbCode().equals("99")) {
            remittanceDetails.setCurrencySecbDesc(currencyRemittanceMade);
            remittanceDetails.setCurrencySecbCode("99");
        }
        remittanceDetails.setNameBankCode(form15CAData.getBankCode15CBValue(bankNameCode).split("_")[1]);
        if (remittanceDetails.getNameBankCode() != null && remittanceDetails.getNameBankCode().equals("999")) {
            remittanceDetails.setNameBankDesc(bankNameCode);
            remittanceDetails.setNameBankCode("999");
        }
        remittanceDetails.setAmtPayForgnRem(amountPayableInForegnCurrency);
        remittanceDetails.setAmtPayIndRem(amountPayableInINR);
        remittanceDetails.setBranchName(bankBranch);
        remittanceDetails.setBsrCode(bankBranchBSRCode);
        remittanceDetails.setNatureRemCategory("16.16");
        remittanceDetails.setPropDateRem(Form15DataUtil.toXmlDate(proposedDateOfRemittance));
        remittanceDetails.setRevPurCategory("RB-14.1");
        remittanceDetails.setRevPurCode("RB-14.1-" + releventPurposeCodeRBI);
        // Need To confirm
        remittanceDetails.setTaxPayGrossSecb(taxPayableGrossedUp);
        this.form15CA.setRemittanceDetls(remittanceDetails);
        return this;
    }

    @Override
    public DeclarationBuilder aoCertificate(String section, String officerName, String officerDesignation,
                                            Date dateOfCertificate, String certificateNumber, String orderAoFlg) {
        AoOrderDetls aoOrderDetails = this.objectFactory.createAoOrderDetls();
        Form15CAData form15CAData = new Form15CAData();
        if (orderAoFlg != null && orderAoFlg.equalsIgnoreCase("Y")) {
            if(!StringUtils.isBlank(section))
            aoOrderDetails.setCertSection(form15CAData.getCertSection15CBValue(section).split("_")[1]);
            aoOrderDetails.setNameAo(officerName);
            aoOrderDetails.setDesgAo(officerDesignation);
            aoOrderDetails.setOrderDateAo(Form15DataUtil.toXmlDate(dateOfCertificate));
            aoOrderDetails.setOrderNumAo(certificateNumber);
        }
        aoOrderDetails.setOrderAoFlg(orderAoFlg);
        this.form15CA.setAoOrderDetls(aoOrderDetails);
        return this;
    }

    @Override
    public DTAABuilder actDetail(String section, BigDecimal amountChargeableToTax, BigDecimal taxLiability,
                                 String basisOfDetermination) {
        ItActDetails itActDetails = this.objectFactory.createItActDetails();
        itActDetails.setSecRemCovered("115A");
        itActDetails.setAmtIncChrgIt(amountChargeableToTax);
        itActDetails.setTaxLiablIt(taxLiability);
        itActDetails.setBasisDeterTax(basisOfDetermination);
        this.form15CA.setItActDetails(itActDetails);
        return this;
    }

    @Override
    public RemittanceRoyaltyDividendBuilder ldcApplied(String releventDTAA, String natureOfPaymentDTAA, BigDecimal taxableIncomeAsPerDTAAINR,
                                                       BigDecimal taxAsPerDTAAINR) {
        DTAADetails dtaaDetails = this.objectFactory.createDTAADetails();
        dtaaDetails.setTaxResidCert("Y");
        dtaaDetails.setRelevantArtDtaa(releventDTAA);
        dtaaDetails.setNatureRemDtaa(natureOfPaymentDTAA);
        dtaaDetails.setTaxIncDtaa(taxAsPerDTAAINR);
        dtaaDetails.setTaxLiablDtaa(taxAsPerDTAAINR);
        this.form15CA.setDTAADetails(dtaaDetails);
        return this;
    }


    @Override
    public TDSBuilder dtaa(String taxResidencyCertificateAvailable,
                           String relevantDtaa,
                           String RelevantArtDtaa, BigDecimal TaxIncDtaa,
                           BigDecimal TaxLiablDtaa, String RemForRoyFlg, String ArtDtaa,
                           BigDecimal RateTdsADtaa, String RemAcctBusIncFlg,
                           String RemOnCapGainFlg, String OtherRemDtaa, String TaxIndDtaaFlg) {
        DTAADetails dtaaDetails = this.objectFactory.createDTAADetails();
        dtaaDetails.setTaxResidCert(taxResidencyCertificateAvailable);
        dtaaDetails.setRelevantDtaa(relevantDtaa);
        dtaaDetails.setRelevantArtDtaa(RelevantArtDtaa);
        dtaaDetails.setRemForRoyFlg(RemForRoyFlg);
        if(RemForRoyFlg.equals("Y")) {
        if (!StringUtils.isBlank(ArtDtaa)) {
            dtaaDetails.setArtDtaa(ArtDtaa);
        } else {
            dtaaDetails.setArtDtaa("ARTICLE 10");
        }
        dtaaDetails.setTaxIncDtaa(TaxIncDtaa);
        dtaaDetails.setTaxLiablDtaa(TaxLiablDtaa);
        dtaaDetails.setRateTdsADtaa(RateTdsADtaa);
        }
        dtaaDetails.setRemAcctBusIncFlg("N");
        dtaaDetails.setRemOnCapGainFlg("N");
        dtaaDetails.setOtherRemDtaa("N");
        dtaaDetails.setTaxIndDtaaFlg("N");
        dtaaDetails.setRelArtDetlDDtaa("NOT APPLICABLE");
        this.form15CA.setDTAADetails(dtaaDetails);
        return this;
    }

    @Override
    public RemittanceRoyaltyDividendBuilder noLDC() {
        DTAADetails dtaaDetails = this.objectFactory.createDTAADetails();
        dtaaDetails.setTaxResidCert("N");
        this.form15CA.setDTAADetails(dtaaDetails);
        return this;
    }

    @Override
    public RemittanceBusinessIncomeBuilder notForRoyaltyOrDividend() {
        this.form15CA.getDTAADetails().setRemForRoyFlg("N");
        return this;
    }

    @Override
    public RemittanceBusinessIncomeBuilder royaltyOrDividend(String articleOfDTAA, BigDecimal rateAsPerDTAA) {
        this.form15CA.getDTAADetails().setRemForRoyFlg("Y");
        this.form15CA.getDTAADetails().setArtDtaa(articleOfDTAA);
        this.form15CA.getDTAADetails().setRateTdsADtaa(rateAsPerDTAA);
        return this;
    }

    @Override
    public RemittanceCapitalGainsBuilder businessIncome(BigDecimal taxableIncomeInIndia, String basisOfDetermination) {
        this.form15CA.getDTAADetails().setRemAcctBusIncFlg("Y");
        this.form15CA.getDTAADetails().setAmtToTaxInd(taxableIncomeInIndia);
        this.form15CA.getDTAADetails().setBasisTaxIncDtaa(basisOfDetermination);
        return this;
    }

    @Override
    public RemittanceCapitalGainsBuilder noBusinessIncome() {
        this.form15CA.getDTAADetails().setRemAcctBusIncFlg("N");
        return this;
    }

    @Override
    public RemittanceMisc capitalGains(BigDecimal longTermGains, BigDecimal shortTermGains,
                                       String basisOfDetermination) {
        this.form15CA.getDTAADetails().setRemOnCapGainFlg("Y");
        this.form15CA.getDTAADetails().setAmtLongTrm(longTermGains);
        this.form15CA.getDTAADetails().setAmtShortTrm(shortTermGains);
        return this;
    }

    @Override
    public RemittanceMisc noCapitalGains() {
        this.form15CA.getDTAADetails().setRemOnCapGainFlg("N");
        return this;
    }

    @Override
    public TDSBuilder miscIncomeTaxableInIndia(String natureOfRemittance, BigDecimal rateAsPerDTAA) {
        this.form15CA.getDTAADetails().setOtherRemDtaa("Y");
        this.form15CA.getDTAADetails().setOtherRemDtaa(natureOfRemittance);
        this.form15CA.getDTAADetails().setRateTdsDDtaa(natureOfRemittance);
        return this;
    }

    @Override
    public TDSBuilder miscIncomeNonTaxableInIndia(String natureOfRemittance, String articleOfDTAA) {
        this.form15CA.getDTAADetails().setOtherRemDtaa("Y");
        this.form15CA.getDTAADetails().setOtherRemDtaa(natureOfRemittance);
        this.form15CA.getDTAADetails().setRelArtDetlDDtaa(articleOfDTAA);
        return this;
    }

    @Override
    public TDSBuilder noMiscIncome() {
        this.form15CA.getDTAADetails().setOtherRemDtaa("N");
        return this;
    }

    @Override
    public AccountantBuilder tdsDetails(String tdsRateSection, BigDecimal taxAmountINR,
                                        BigDecimal taxAmountInForeignCurrency, BigDecimal tdsRate,
                                        BigDecimal remittanceAmountIncludingTDSInForeignCurrency) {
        TDSDetails tdsDetails = this.objectFactory.createTDSDetails();
        tdsDetails.setAmtPayForgnTds(taxAmountInForeignCurrency);
        tdsDetails.setAmtPayIndianTds(taxAmountINR);
        tdsDetails.setActlAmtTdsForgn(remittanceAmountIncludingTDSInForeignCurrency);
        tdsDetails.setRateTdsSecB(tdsRate);
        // Need to confirm
        // tdsDetails.setDednDateTds();
        // tdsDetails.setRateTdsSecbFlg(tdsRateSection);
        this.form15CA.setTDSDetails(tdsDetails);
        return this;
    }

    @Override
    public AOOrderBuilder accountant(String name, String firmName, String membershipNumber, String certificateNo,
                                     Date certificateDate, String flatDoorBuilding, String roadStreet, String premisesBuildingVillage,
                                     String townCityDistrict, String areaLocality, String pinCode, String state, String country) {
        AcctntDetls accountantDetails = this.objectFactory.createAcctntDetls();
        Form15CAData form15CAData = new Form15CAData();
        accountantDetails.setNameAcctnt(name);
        accountantDetails.setNameFirmAcctnt(firmName);
        accountantDetails.setMembershipNumber(membershipNumber);
        accountantDetails.setCertNumber(certificateNo);
        accountantDetails.setCertDate(Form15DataUtil.toXmlDate(certificateDate));

        AcntntAddressType address = this.objectFactory.createAcntntAddressType();
        address.setFlatDoorBuilding(flatDoorBuilding);
        address.setRoadStreet(roadStreet);
        address.setPremisesBuildingVillage(premisesBuildingVillage);
        address.setTownCityDistrict(townCityDistrict);
        address.setAreaLocality(areaLocality);
        address.setPincode(pinCode);
        address.setState(form15CAData.getStateCodeValue(state).split("_")[1]);
        address.setCountry(form15CAData.getCountryCodeValue(country).split("_")[1]);
        accountantDetails.setAcctntAddrs(address);
        this.form15CA.setAcctntDetls(accountantDetails);
        return this;
    }

    @Override
    public Generatable declaration(String salutation, String name, String gaurdian, String designation,
                                   Date verificationDate, String verificationPlace) {
        Declaration declaration = this.objectFactory.createDeclaration();
        declaration.setIWe(Form15DataUtil.iOrWeForXmlGeneration(salutation));
        declaration.setStyledName(name);
        declaration.setLandlordName(gaurdian);
        declaration.setVerDesignation(designation);
        declaration.setVerificationDate(Form15DataUtil.toXmlDate(verificationDate));
        declaration.setVerificationPlace(verificationPlace);
        this.form15CA.setDeclaration(declaration);
        return this;
    }

    @Override
    public File generate() {
        try {
            this.sourcefile = new File(Form15XmlUtil.formsDirectory() + File.separator + this.fileName);
            JAXBContext context = JAXBContext.newInstance("com.ey.in.tds.dividend.fifteen.ca");
            JAXBElement<FORM15CA> element = new JAXBElement<FORM15CA>(_FORM15CA_QNAME, FORM15CA.class, null,
                    this.form15CA);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("jaxb.formatted.output", true);
            marshaller.marshal(element, this.sourcefile);
        } catch (JAXBException e) {
            e.printStackTrace();
            throw new FormGenerationException(e, "Error while generating Form 15CA Part "
                    + PartType.byTypeName(this.form15CA.getPartType()).name() + " source xml");
        }
        return this.sourcefile;
    }
}
