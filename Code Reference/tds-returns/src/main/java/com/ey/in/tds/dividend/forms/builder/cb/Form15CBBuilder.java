package com.ey.in.tds.dividend.forms.builder.cb;

import com.ey.in.tds.dividend.common.*;
import com.ey.in.tds.dividend.fifteen.cb.*;
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
import java.util.Objects;

public class Form15CBBuilder implements RemitterBuilder, RemitteeBuilder, RemittanceRoyaltyDividendBuilder,
        RemittanceBusinessIncomeBuilder, RemittanceCapitalGainsBuilder, RemittanceMisc, RemittanceBuilder, ITActBuilder,
        DTAABuilder, TDSBuilder, AccountantBuilder, Generatable {

    private final static QName _Form15CB_QNAME = new QName("http://incometaxindiaefiling.gov.in/FORM15CAB", "FORM15CB");

    private ObjectFactory objectFactory;

    private FORM15CB form15CB;

    private File sourcefile;
    private String fileName;

    public Form15CBBuilder(int assessmentYear, String fileName) {
        this.objectFactory = new ObjectFactory();
        this.fileName = fileName;

        CreationInfo creationInfo = this.objectFactory.createCreationInfo();
        creationInfo.setSWCreatedBy("DIT-EFILING-JAVA");
        creationInfo.setXMLCreatedBy("DIT-EFILING-JAVA");
        creationInfo.setIntermediaryCity("Delhi");
        creationInfo.setSWVersionNo("1");
        creationInfo.setXMLCreationDate(Form15DataUtil.nowXmlDate());

        FormDetails formDetails = this.objectFactory.createFormDetails();
        formDetails.setAssessmentYear("" + assessmentYear);
        formDetails.setDescription("FORM15CB");
        formDetails.setFormName("FORM15CB");
        formDetails.setSchemaVer("Ver1.1");
        formDetails.setFormVer("1");

        this.form15CB = this.objectFactory.createFORM15CB();
        this.form15CB.setCreationInfo(creationInfo);
        this.form15CB.setFormDetails(formDetails);
    }

    public Form15CBBuilder(FORM15CB form15CB, String fileName) {
        this.form15CB = form15CB;
        this.fileName = fileName;
    }

    @Override
    public RemitteeBuilder remitter(RemitterDetails remitterDetails) {
        this.form15CB.setRemitterDetails(remitterDetails);
        return this;
    }

    @Override
    public RemitteeBuilder remitter(String iOrWe, String remitterHonorific, String remitterName, String pan,
                                    String beneficiaryHonorific) {
        RemitterDetails remitterDetails = this.objectFactory.createRemitterDetails();
        remitterDetails.setIorWe(iOrWe);
        remitterDetails.setRemitterHonorific(remitterHonorific);
        remitterDetails.setNameRemitter(remitterName);
        remitterDetails.setPAN(pan);
        if (beneficiaryHonorific != null && !beneficiaryHonorific.isEmpty()) {
            if (beneficiaryHonorific.equalsIgnoreCase("Mr")) {
                remitterDetails.setBeneficiaryHonorific("01");
            } else if (beneficiaryHonorific.equalsIgnoreCase("Ms")) {
                remitterDetails.setBeneficiaryHonorific("02");
            } else if (beneficiaryHonorific.equalsIgnoreCase("M/s")) {
                remitterDetails.setBeneficiaryHonorific("03");
            }
        }
        this.form15CB.setRemitterDetails(remitterDetails);
        return this;
    }

    @Override
    public RemittanceBuilder remittee(RemitteeDetls remitteeDetails) {
        this.form15CB.setRemitteeDetls(remitteeDetails);
        return this;
    }

    @Override
    public RemittanceBuilder remittee(String remitteeName, String flatDoorBuilding, String roadStreet,
                                      String premisesBuildingVillage, String townCityDistrict, String areaLocality, String zipCode, String state,
                                      String country) {
        RemitteeDetls remitteeDetails = this.objectFactory.createRemitteeDetls();
        remitteeDetails.setNameRemittee(remitteeName);
        Form15CAData form15CAData = new Form15CAData();
        AddressType remitteeAddress = this.objectFactory.createAddressType();
        remitteeAddress.setFlatDoorBuilding(flatDoorBuilding);
        remitteeAddress.setRoadStreet(roadStreet);
        remitteeAddress.setPremisesBuildingVillage(premisesBuildingVillage);
        remitteeAddress.setTownCityDistrict(townCityDistrict);
        remitteeAddress.setAreaLocality(areaLocality);
        remitteeAddress.setZipCode(zipCode);
        remitteeAddress.setState(state);
        remitteeAddress.setCountry(form15CAData.getCountryCodeValue(country).split("_")[1]);
        remitteeDetails.setRemitteeAddrs(remitteeAddress);
        this.form15CB.setRemitteeDetls(remitteeDetails);
        return this;
    }

    @Override
    public ITActBuilder remittance(RemittanceDetails remittanceDetails) {
        this.form15CB.setRemittanceDetails(remittanceDetails);
        return this;
    }

    @Override
    public ITActBuilder remittance(final String countryRemitanceMadeIn, final String currencyRemitanceMadeIn,
                                   final BigDecimal amountPayableInForeignCurrency, final BigDecimal amountPayableInINR,
                                   final String bankNameCode, final String bankBranchName, final String bankBranchBsrCode,
                                   final Date proposedDateOfRemmitance, final String natureOfRemittance, String releventPurposeCategoryRBI,
                                   String releventPurposeCodeRBI, String taxPayableGrossedUp) {
        RemittanceDetails remittanceDetails = this.objectFactory.createRemittanceDetails();
        Form15CAData form15CAData = new Form15CAData();
        Form15CBBankData form15CBBankData = new Form15CBBankData();
        remittanceDetails.setCountryRemMadeSecb(form15CAData.getCountryCodeValue(countryRemitanceMadeIn).split("_")[1]);
        if (remittanceDetails.getCountryRemMadeSecb() != null && remittanceDetails.getCountryRemMadeSecb().equals("9999"))
            remittanceDetails.setCountryRemMadeSecbDesc(countryRemitanceMadeIn);
        remittanceDetails.setCurrencySecbCode(form15CAData.getCurrencyCodeValue(currencyRemitanceMadeIn).split("_")[1]);
        if (remittanceDetails.getCurrencySecbCode() != null && remittanceDetails.getCurrencySecbCode().equals("99"))
            remittanceDetails.setCurrencySecbDesc(currencyRemitanceMadeIn);
        remittanceDetails.setNameBankCode(form15CBBankData.getBankCodeValue(bankNameCode).split("_")[1]);
        if (remittanceDetails.getNameBankCode() != null && remittanceDetails.getNameBankCode().equals("999"))
            remittanceDetails.setNameBankDesc(bankNameCode);
        remittanceDetails.setAmtPayForgnRem(amountPayableInForeignCurrency);
        remittanceDetails.setAmtPayIndRem(amountPayableInINR);
        remittanceDetails.setBranchName(bankBranchName);
        remittanceDetails.setBsrCode(bankBranchBsrCode);
        remittanceDetails.setPropDateRem(Form15DataUtil.toXmlDate(proposedDateOfRemmitance));
        remittanceDetails.setNatureRemCategory("16.16");
        remittanceDetails.setPropDateRem(Form15DataUtil.toXmlDate(proposedDateOfRemmitance));
        remittanceDetails.setRevPurCategory("RB-14.1");
        remittanceDetails.setRevPurCode("RB-14.1-" + releventPurposeCodeRBI);
        // Need To confirm
        remittanceDetails.setTaxPayGrossSecb(taxPayableGrossedUp);
        this.form15CB.setRemittanceDetails(remittanceDetails);
        return this;
    }

    @Override
    public DTAABuilder itAct(final ItActDetails itActDetails) {
        this.form15CB.setItActDetails(itActDetails);
        return this;
    }

    @Override
    public DTAABuilder notTaxableAsPerItActInIndia(String reason) {
        ItActDetails itActDetails = this.objectFactory.createItActDetails();
        itActDetails.setReasonNot(reason);
        this.form15CB.setItActDetails(itActDetails);
        return this;
    }

    @Override
    public DTAABuilder taxableAsPerItActInIndia(String section, BigDecimal amountChargeableToTax,
                                                BigDecimal taxLiability, String basisOfDetermination) {
        ItActDetails itActDetails = this.objectFactory.createItActDetails();
        itActDetails.setRemittanceCharIndia("Y"); //Hardcoded
        itActDetails.setSecRemCovered("115A");
        itActDetails.setAmtIncChrgIt(amountChargeableToTax);
        itActDetails.setTaxLiablIt(taxLiability);
        itActDetails.setBasisDeterTax(basisOfDetermination);
        this.form15CB.setItActDetails(itActDetails);
        return this;
    }

    @Override
    public TDSBuilder dtaa(DTAADetails dtaaDetails) {
        this.form15CB.setDTAADetails(dtaaDetails);
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
        if(taxResidencyCertificateAvailable.equals("Y")) {
        dtaaDetails.setTaxIncDtaa(TaxIncDtaa);
        dtaaDetails.setTaxLiablDtaa(TaxLiablDtaa);
        }
        dtaaDetails.setRemForRoyFlg(RemForRoyFlg);
        if(RemForRoyFlg.equals("Y")) {
        if (!StringUtils.isBlank(ArtDtaa)) {
            dtaaDetails.setArtDtaa(ArtDtaa);
        } else {
            dtaaDetails.setArtDtaa("ARTICLE 10");
        }
        dtaaDetails.setRateTdsADtaa(RateTdsADtaa);
        }
        dtaaDetails.setRemAcctBusIncFlg("N");
        dtaaDetails.setRemOnCapGainFlg("N");
        dtaaDetails.setOtherRemDtaa("N");
        dtaaDetails.setTaxIndDtaaFlg("N");
        dtaaDetails.setRelArtDetlDDtaa("NOT APPLICABLE");
        this.form15CB.setDTAADetails(dtaaDetails);
        return this;
    }

    @Override
    public RemittanceRoyaltyDividendBuilder ldcApplied(String releventDTAA, String natureOfPaymentDTAA,
                                                       BigDecimal taxableIncomeAsPerDTAAINR, BigDecimal taxAsPerDTAAINR) {
        DTAADetails dtaaDetails = this.objectFactory.createDTAADetails();
        dtaaDetails.setTaxResidCert("Y");
        dtaaDetails.setRelevantArtDtaa(releventDTAA);
        dtaaDetails.setNatureRemDtaa(natureOfPaymentDTAA);
        dtaaDetails.setTaxIncDtaa(taxAsPerDTAAINR);
        dtaaDetails.setTaxLiablDtaa(taxAsPerDTAAINR);
        this.form15CB.setDTAADetails(dtaaDetails);
        return this;
    }

    @Override
    public RemittanceRoyaltyDividendBuilder noLDC() {
        DTAADetails dtaaDetails = this.objectFactory.createDTAADetails();
        dtaaDetails.setTaxResidCert("N");
        this.form15CB.setDTAADetails(dtaaDetails);
        return this;
    }

    @Override
    public RemittanceBusinessIncomeBuilder notForRoyaltyOrDividend() {
        this.form15CB.getDTAADetails().setRemForRoyFlg("N");
        return this;
    }

    @Override
    public RemittanceBusinessIncomeBuilder royaltyOrDividend(String articleOfDTAA, BigDecimal rateAsPerDTAA) {
        this.form15CB.getDTAADetails().setRemForRoyFlg("Y");
        this.form15CB.getDTAADetails().setArtDtaa(articleOfDTAA);
        this.form15CB.getDTAADetails().setRateTdsADtaa(rateAsPerDTAA);
        return this;
    }

    @Override
    public RemittanceCapitalGainsBuilder businessIncome(BigDecimal taxableIncomeInIndia, String basisOfDetermination) {
        this.form15CB.getDTAADetails().setRemAcctBusIncFlg("Y");
        this.form15CB.getDTAADetails().setTaxIncDtaa(taxableIncomeInIndia);
        this.form15CB.getDTAADetails().setBasisTaxIncDtaa(basisOfDetermination);
        return this;
    }

    @Override
    public RemittanceCapitalGainsBuilder noBusinessIncome() {
        this.form15CB.getDTAADetails().setRemAcctBusIncFlg("N");
        return this;
    }

    @Override
    public RemittanceMisc capitalGains(BigDecimal longTermGains, BigDecimal shortTermGains,
                                       String basisOfDetermination) {
        this.form15CB.getDTAADetails().setRemOnCapGainFlg("Y");
        this.form15CB.getDTAADetails().setAmtLongTrm(longTermGains);
        this.form15CB.getDTAADetails().setAmtShortTrm(shortTermGains);
        return this;
    }

    @Override
    public RemittanceMisc noCapitalGains() {
        this.form15CB.getDTAADetails().setRemOnCapGainFlg("N");
        return this;
    }

    @Override
    public TDSBuilder miscIncomeTaxableInIndia(String natureOfRemittance, BigDecimal rateAsPerDTAA) {
        this.form15CB.getDTAADetails().setOtherRemDtaa("Y");
        this.form15CB.getDTAADetails().setOtherRemDtaa(natureOfRemittance);
        this.form15CB.getDTAADetails().setRateTdsDDtaa(natureOfRemittance);
        return this;
    }

    @Override
    public TDSBuilder miscIncomeNonTaxableInIndia(String natureOfRemittance, String articleOfDTAA) {
        this.form15CB.getDTAADetails().setOtherRemDtaa("Y");
        this.form15CB.getDTAADetails().setOtherRemDtaa(natureOfRemittance);
        this.form15CB.getDTAADetails().setRelArtDetlDDtaa(articleOfDTAA);
        return this;
    }

    @Override
    public TDSBuilder noMiscIncome() {
        this.form15CB.getDTAADetails().setOtherRemDtaa("N");
        return this;
    }

    @Override
    public AccountantBuilder tds(TDSDetails tdsDetails) {
        this.form15CB.setTDSDetails(tdsDetails);
        return this;
    }

    @Override
    public AccountantBuilder tds(TDSRateType rateType, String tdsSection, BigDecimal amountInForeignCurrency,
                                 BigDecimal amountInINR, BigDecimal tdsRate, BigDecimal remittanceIncludingTDSInForeignCurrency,
                                 Date deductionDate) {
        TDSDetails tdsDetails = this.objectFactory.createTDSDetails();
        tdsDetails.setAmtPayForgnTds(amountInForeignCurrency);
        tdsDetails.setAmtPayIndianTds(amountInINR);
        tdsDetails.setRateTdsSecB(tdsRate);
        tdsDetails.setActlAmtTdsForgn(remittanceIncludingTDSInForeignCurrency);
        // Need to confirm
        if (Objects.nonNull(rateType))
            tdsDetails.setRateTdsSecbFlg(rateType.numCode());
        tdsDetails.setDednDateTds(Form15DataUtil.toXmlDate(deductionDate));
        this.form15CB.setTDSDetails(tdsDetails);
        return this;
    }

    @Override
    public Generatable accountant(AcctntDetls accountantDetails) {
        this.form15CB.setAcctntDetls(accountantDetails);
        return this;
    }

    @Override
    public Generatable accountant(String name, String firmName, String membershipNumber, String registrationNumber,
                                  String flatDoorBuilding, String roadStreet, String premisesBuildingVillage, String townCityDistrict,
                                  String areaLocality, String pinCode, String state, String country) {
        AcctntDetls accountantDetails = this.objectFactory.createAcctntDetls();
        Form15CAData form15CAData = new Form15CAData();
        accountantDetails.setNameAcctnt(name);
        accountantDetails.setNameFirmAcctnt(firmName);
        accountantDetails.setMembershipNumber(membershipNumber);
        accountantDetails.setRegNoAcctnt(registrationNumber.length() < 8 ? String.format("%0" + (8 - registrationNumber.length()) + "d%s", 0, registrationNumber) : registrationNumber);

        AddressType accountantAddress = this.objectFactory.createAddressType();
        accountantAddress.setTownCityDistrict(townCityDistrict);
        accountantAddress.setFlatDoorBuilding(flatDoorBuilding);
        accountantAddress.setAreaLocality(areaLocality);
        accountantAddress.setPincode(pinCode);
        accountantAddress.setState(form15CAData.getStateCodeValue(state).split("_")[1]);
        accountantAddress.setCountry(form15CAData.getCountryCodeValue(country).split("_")[1]);
        accountantDetails.setAcctntAddrs(accountantAddress);
        this.form15CB.setAcctntDetls(accountantDetails);
        return this;
    }

    @Override
    public File generate() {
        try {
            this.sourcefile = new File(Form15XmlUtil.formsDirectory() + File.separator + this.fileName);
            JAXBContext context = JAXBContext.newInstance("com.ey.in.tds.dividend.fifteen.cb");
            JAXBElement<FORM15CB> element = new JAXBElement<FORM15CB>(_Form15CB_QNAME, FORM15CB.class, null,
                    this.form15CB);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("jaxb.formatted.output", true);
            marshaller.marshal(element, this.sourcefile);
        } catch (JAXBException e) {
            e.printStackTrace();
            throw new FormGenerationException(e, "Error while generating Form 15CB source xml");
        }
        return this.sourcefile;
    }
}
