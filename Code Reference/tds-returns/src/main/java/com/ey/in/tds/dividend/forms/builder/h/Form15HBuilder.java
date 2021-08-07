package com.ey.in.tds.dividend.forms.builder.h;

import com.ey.in.tds.dividend.common.Form15DataUtil;
import com.ey.in.tds.dividend.common.Form15XmlUtil;
import com.ey.in.tds.dividend.common.FormGenerationException;
import com.ey.in.tds.dividend.fifteen.h.*;
import com.ey.in.tds.dividend.fifteen.h.FORM15H.FormCreationInfo;
import com.ey.in.tds.dividend.fifteen.h.Part1.IncomeDtls;
import com.ey.in.tds.dividend.forms.builder.AddressBuilder;
import com.ey.in.tds.dividend.forms.builder.gh.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.atomic.LongAdder;

public class Form15HBuilder implements FilingDetailsBuilder<Form15HBasicDetailsBuilder>, Form15HBasicDetailsBuilder,
        AddressBuilder<AddMoreBuilder<Form15HBasicDetailsBuilder>>,
        TaxDetailsBuilder<AddMoreBuilder<Form15HBasicDetailsBuilder>>,
        IncomeDetailsBuilder<AddMoreBuilder<Form15HBasicDetailsBuilder>>, AddMoreBuilder<Form15HBasicDetailsBuilder> {

    private final static QName _FORM15H_QNAME = new QName("http://incometaxindiaefiling.gov.in/FORM15H", "FORM15H");

    private ObjectFactory objectFactory;

    private FORM15H form15H;
    private Part1 part1;
    private Basicdtls basicdetails;

    private File sourcefile;
    private String fileName;

    private CorrectionType correctionType;
    private int financialYear;

    private LongAdder serNo = new LongAdder();

    public Form15HBuilder(int assessmentYear, String fileName) {
        this.fileName = fileName;
        this.objectFactory = new ObjectFactory();

        CreationInfo creationInfo = this.objectFactory.createCreationInfo();
        creationInfo.setSWCreatedBy("ITD_JAVA_UTILITY");
        creationInfo.setSWVersionNo("1.0");
        creationInfo.setXMLCreatedBy("ITD_JAVA_UTILITY");
        creationInfo.setXMLCreationDate(Form15DataUtil.nowXmlDate());

        FormDetails formDetails = this.objectFactory.createFormDetails();
        formDetails.setAssessmentYear("" + assessmentYear);
        formDetails.setFormName("FORM15H");
        formDetails.setSchemaVer("V1.0");
        formDetails.setFormVer(BigDecimal.valueOf(1.0));

        FormCreationInfo formCreationInfo = this.objectFactory.createFORM15HFormCreationInfo();
        formCreationInfo.setCreationInfo(creationInfo);
        formCreationInfo.setFormDetails(formDetails);

        this.form15H = this.objectFactory.createFORM15H();
        this.form15H.setFormCreationInfo(formCreationInfo);
    }

    public Form15HBuilder(FORM15H form15G, String fileName) {
        this.form15H = form15G;
        this.fileName = fileName;
        this.serNo = new LongAdder();
    }

    @Override
    public Form15HBasicDetailsBuilder original(String tan, Quarter quarter, int financialYear,
                                               String acknowlegementNo) {
        Form15HDtls form15HDetails = this.objectFactory.createForm15HDtls();
        form15HDetails.setTAN(tan);
        form15HDetails.setQuarter(quarter.name());
        form15HDetails.setFilingType("O");
        form15HDetails.setFinancialYr("" + financialYear);
        form15HDetails.setAcknowledgeNumber(acknowlegementNo);
        //this.form15H.getForm15HDtls().setPart1Dtls(new Part1Dtls());
        form15HDetails.setPart1Dtls(new Form15HDtls.Part1Dtls());
        this.form15H.setForm15HDtls(form15HDetails);
        this.financialYear = financialYear;
        return this;
    }

    @Override
    public Form15HBasicDetailsBuilder correction(String tan, Quarter quarter, int financialYear,
                                                 String acknowlegementNo, CorrectionType correctionType) {
        Form15HDtls form15HDetails = this.objectFactory.createForm15HDtls();
        form15HDetails.setTAN(tan);
        form15HDetails.setQuarter(quarter.name());
        form15HDetails.setFilingType("C");
        form15HDetails.setFinancialYr("" + financialYear);
        form15HDetails.setAcknowledgeNumber(acknowlegementNo);
//        this.form15H.getForm15HDtls().setPart1Dtls(new Part1Dtls());
        form15HDetails.setPart1Dtls(new Form15HDtls.Part1Dtls());
        this.form15H.setForm15HDtls(form15HDetails);
        this.financialYear = financialYear;
        this.correctionType = correctionType;
        this.correctionType = CorrectionType.ADDITION;  //hardcoded
        return this;
    }

    @Override
    public AddressBuilder<AddMoreBuilder<Form15HBasicDetailsBuilder>> basicDetails(String assesseeName,
                                                                                   String assesseePAN, Date assesseeDOB, int previousYear, String email, String stdCode, String telephone,
                                                                                   String mobile) {
        this.part1 = this.objectFactory.createPart1();
        this.basicdetails = this.objectFactory.createBasicdtls();
        int nextYear = (this.financialYear + 1) % 100;
        String uniqueIdentificationNo = "H" + RandomStringUtils.randomNumeric(9) + (this.financialYear-1) + (nextYear-1)
                + this.form15H.getForm15HDtls().getTAN();
        this.basicdetails.setUniqueIdNumber(uniqueIdentificationNo);
        this.basicdetails.setAssesseeName(assesseeName);
        this.basicdetails.setAssesseePAN(assesseePAN);
        this.basicdetails.setAssesseeDOB(Form15DataUtil.toXmlDate(assesseeDOB));
        this.basicdetails.setPreviousYr("" + previousYear);
        if(!StringUtils.isBlank(email))
        this.basicdetails.setEmailAddress(email);
        if(!StringUtils.isBlank(stdCode))
        this.basicdetails.setSTDcode(stdCode);
        // this.basicdetails.setPhoneNo(telephone);
        if(!StringUtils.isBlank(mobile))
        this.basicdetails.setMobileNo(mobile);
        if (this.correctionType != null) {
            this.basicdetails.setRecordType(this.correctionType.numCode());
        }
        return this;
    }

    @Override
    public TaxDetailsBuilder<AddMoreBuilder<Form15HBasicDetailsBuilder>> address(String flatDoorBuilding,
                                                                                 String roadStreet, String premisesBuildingVillage, String townCityDistrict, String areaLocality,
                                                                                 String pinCode, String state) {
        this.basicdetails.setFlatDoorNo(flatDoorBuilding);
        this.basicdetails.setRoadOrStreet(roadStreet);
        this.basicdetails.setPremisesName(premisesBuildingVillage);
        this.basicdetails.setCityOrTownOrDistrict(townCityDistrict);
        this.basicdetails.setLocalityOrArea(areaLocality);
        this.basicdetails.setPinCode(pinCode);
        this.basicdetails.setStateCode(state);
        return this;
    }

    @Override
    public IncomeDetailsBuilder<AddMoreBuilder<Form15HBasicDetailsBuilder>> taxDetails(boolean taxableUnderItAct,
                                                                                       Integer latestAssesmentYear, BigDecimal estimatedIncome, BigDecimal estimatedTotalIncomePY, BigDecimal totalFormsFiled,
                                                                                       BigDecimal aggregateAmountFormFiledFor, Date declarationDate, BigDecimal incomePaid, Date incomePaidDate) {
        this.basicdetails.setTaxAssessedFlag(Form15DataUtil.yOrN(taxableUnderItAct));
        this.basicdetails.setEstimatedInc(estimatedIncome.toBigInteger());
        this.basicdetails.setEstimatedTotalIncPrvYr(estimatedTotalIncomePY.longValue());
        this.basicdetails.setTotalNoOfForm15H(totalFormsFiled == null ? null : totalFormsFiled.toBigInteger());
        this.basicdetails.setAggregateAmtForm15H(aggregateAmountFormFiledFor == null ? null : aggregateAmountFormFiledFor.toBigInteger());
        this.basicdetails.setDeclarationDate(Form15DataUtil.toXmlDate(declarationDate));
        this.basicdetails.setAmtOfIncPaid(incomePaid.toBigInteger());
        this.basicdetails.setDateIncPaid(Form15DataUtil.toXmlDate(incomePaidDate));
        this.part1.setBasicdtls(this.basicdetails);
        if (this.basicdetails.getTaxAssessedFlag().equalsIgnoreCase("Y"))
            this.basicdetails.setLatestAsstYr("" + latestAssesmentYear);
        return this;
    }

    @Override
    public AddMoreBuilder<Form15HBasicDetailsBuilder> incomeDetails(String uniqueIdentifiactionNo,
                                                                    String investmentAccountIdNo, String natureOfIncome, String tdsSection, BigDecimal incomeAmount) {
        IncomeDtls incomeDetails = this.objectFactory.createPart1IncomeDtls();
        IncomeDtl incomeDetail = this.objectFactory.createIncomeDtl();
        incomeDetail.setIdenficationNum(uniqueIdentifiactionNo);
        incomeDetail.setNatureOfInc(natureOfIncome);
        incomeDetail.setDeductSection(tdsSection);
        incomeDetail.setAmtOfInc(incomeAmount.toBigInteger());
        this.serNo.increment();
        incomeDetail.setSrlNo(this.serNo.longValue());
        incomeDetails.getIncomeDtl().add(incomeDetail);
        this.part1.setIncomeDtls(incomeDetails);
        this.form15H.getForm15HDtls().getPart1Dtls().getPart1().add(this.part1);
        return this;
    }

    @Override
    public Form15HBasicDetailsBuilder addMore() {
        return this;
    }

    @Override
    public File generate() {
        try {
            this.sourcefile = new File(Form15XmlUtil.formsDirectory() + File.separator + "Source_" + new Date().getTime() + this.fileName);
            JAXBContext context = JAXBContext.newInstance("com.ey.in.tds.dividend.fifteen.h");
            JAXBElement<FORM15H> element = new JAXBElement<FORM15H>(_FORM15H_QNAME, FORM15H.class, null, this.form15H);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("jaxb.formatted.output", true);
            marshaller.marshal(element, this.sourcefile);
        } catch (JAXBException e) {
            e.printStackTrace();
            throw new FormGenerationException(e, "Error while generating Form 15H source xml");
        }
        return this.sourcefile;
    }
}
