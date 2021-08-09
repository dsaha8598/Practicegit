package com.ey.in.tds.dividend.forms.builder.g;

import com.ey.in.tds.dividend.common.Form15DataUtil;
import com.ey.in.tds.dividend.common.Form15XmlUtil;
import com.ey.in.tds.dividend.common.FormGenerationException;
import com.ey.in.tds.dividend.fifteen.g.*;
import com.ey.in.tds.dividend.fifteen.g.FORM15G.FormCreationInfo;
import com.ey.in.tds.dividend.fifteen.g.Form15GDtls.Part1Dtls;
import com.ey.in.tds.dividend.fifteen.g.Part1.IncomeDtls;
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

public class Form15GBuilder implements FilingDetailsBuilder<Form15GBasicDetailsBuilder>, Form15GBasicDetailsBuilder,
        AddressBuilder<AddMoreBuilder<Form15GBasicDetailsBuilder>>, TaxDetailsBuilder<AddMoreBuilder<Form15GBasicDetailsBuilder>>,
        IncomeDetailsBuilder<AddMoreBuilder<Form15GBasicDetailsBuilder>>, AddMoreBuilder<Form15GBasicDetailsBuilder> {

    private final static QName _FORM15G_QNAME = new QName("http://incometaxindiaefiling.gov.in/FORM15G", "FORM15G");

    private ObjectFactory objectFactory;

    private FORM15G form15G;
    private Part1 part1;
    private Basicdtls basicdetails;

    private File sourcefile;
    private String fileName;

    private CorrectionType correctionType;
    private int financialYear;

    private LongAdder serNo = new LongAdder();

    public Form15GBuilder(int assessmentYear, String fileName) {
        this.fileName = fileName;
        this.objectFactory = new ObjectFactory();

        CreationInfo creationInfo = this.objectFactory.createCreationInfo();
        creationInfo.setSWCreatedBy("ITD_JAVA_UTILITY");
        creationInfo.setSWVersionNo("1.0");
        creationInfo.setXMLCreatedBy("ITD_JAVA_UTILITY");
        creationInfo.setXMLCreationDate(Form15DataUtil.nowXmlDate());

        FormDetails formDetails = this.objectFactory.createFormDetails();
        formDetails.setAssessmentYear("" + assessmentYear);
        formDetails.setFormName("FORM15G");
        formDetails.setSchemaVer("V1.0");
        formDetails.setFormVer(BigDecimal.valueOf(1.0));

        FormCreationInfo formCreationInfo = this.objectFactory.createFORM15GFormCreationInfo();
        formCreationInfo.setCreationInfo(creationInfo);
        formCreationInfo.setFormDetails(formDetails);

        this.form15G = this.objectFactory.createFORM15G();
        this.form15G.setFormCreationInfo(formCreationInfo);
    }

    public Form15GBuilder(FORM15G form15G, String fileName) {
        this.form15G = form15G;
        this.fileName = fileName;
        this.serNo = new LongAdder();
    }

    @Override
    public Form15GBasicDetailsBuilder original(String tan, Quarter quarter, int financialYear,
                                               String acknowlegementNo) {
        Form15GDtls form15GDetails = this.objectFactory.createForm15GDtls();
        form15GDetails.setTAN(tan);
        form15GDetails.setQuarter(quarter.name());
        form15GDetails.setFilingType("O");
        form15GDetails.setFinancialYr("" + financialYear);
        form15GDetails.setAcknowledgeNumber(acknowlegementNo);
//		Form15GDtls form15GDtls = this.objectFactory.createForm15GDtls();
//		Part1 part1 = this.objectFactory.createPart1();
        form15GDetails.setPart1Dtls(new Part1Dtls());
        this.form15G.setForm15GDtls(form15GDetails);
        this.financialYear = financialYear;
        return this;
    }

    @Override
    public Form15GBasicDetailsBuilder correction(String tan, Quarter quarter, int financialYear,
                                                 String acknowlegementNo, CorrectionType correctionType) {
        Form15GDtls form15GDetails = this.objectFactory.createForm15GDtls();
        form15GDetails.setTAN(tan);
        form15GDetails.setQuarter(quarter.name());
        form15GDetails.setFilingType("C");
        form15GDetails.setFinancialYr("" + financialYear);
        form15GDetails.setAcknowledgeNumber(acknowlegementNo);
        this.financialYear = financialYear;
//        this.form15G.getForm15GDtls().setPart1Dtls(new Part1Dtls());
        form15GDetails.setPart1Dtls(new Form15GDtls.Part1Dtls());
        this.form15G.setForm15GDtls(form15GDetails);
        this.correctionType = correctionType;
        //this.correctionType = CorrectionType.ADDITION;  //Hardcoded
        return this;
    }

    @Override
    public AddressBuilder<AddMoreBuilder<Form15GBasicDetailsBuilder>> basicDetails(String assesseeName, String assesseePAN,
                                                                                   ShareholderType shareholderType, int previousYear, String email, String stdCode, String telephone,
                                                                                   String mobile) {
        this.part1 = this.objectFactory.createPart1();
        this.basicdetails = this.objectFactory.createBasicdtls();
        int nextYear = (this.financialYear + 1) % 100;
        String uniqueIdentificationNo = "G" + RandomStringUtils.randomNumeric(9) + (this.financialYear) + (nextYear)
                + this.form15G.getForm15GDtls().getTAN();
        this.basicdetails.setUniqueIdNumber(uniqueIdentificationNo);
        this.basicdetails.setAssesseeName(assesseeName);
        this.basicdetails.setAssesseePAN(assesseePAN);
        this.basicdetails.setStatus(shareholderType.numCode());
        this.basicdetails.setPreviousYr("" + previousYear);
        this.basicdetails.setResidentialStatus("RES");
        if(!StringUtils.isBlank(email))
        this.basicdetails.setEmailAddress(email);
        this.basicdetails.setSTDcode(stdCode);
        //this.basicdetails.setPhoneNo(telephone);
        if(!StringUtils.isBlank(mobile))
        this.basicdetails.setMobileNo(mobile);
        if (this.correctionType != null) {
            this.basicdetails.setRecordType(this.correctionType.numCode());
        }
        return this;
    }

    @Override
    public TaxDetailsBuilder<AddMoreBuilder<Form15GBasicDetailsBuilder>> address(String flatDoorBuilding, String roadStreet,
                                                                                 String premisesBuildingVillage, String townCityDistrict, String areaLocality, String pinCode,
                                                                                 String state) {
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
    public IncomeDetailsBuilder<AddMoreBuilder<Form15GBasicDetailsBuilder>> taxDetails(boolean taxableUnderItAct,
                                                                                       Integer latestAssesmentYear, BigDecimal estimatedIncome, BigDecimal estimatedTotalIncomePY, BigDecimal totalFormsFiled,
                                                                                       BigDecimal aggregateAmountFormFiledFor, Date declarationDate, BigDecimal incomePaid, Date incomePaidDate) {
        this.basicdetails.setTaxAssessedFlag(Form15DataUtil.yOrN(taxableUnderItAct));
        this.basicdetails.setEstimatedInc(estimatedIncome.toBigInteger());
        this.basicdetails.setEstimatedTotalIncPrvYr(estimatedTotalIncomePY.longValue());
        this.basicdetails.setTotalNoOfForm15G(totalFormsFiled == null ? null : totalFormsFiled.toBigInteger());
        this.basicdetails.setAggregateAmtForm15G(aggregateAmountFormFiledFor == null ? null : aggregateAmountFormFiledFor.toBigInteger());
        this.basicdetails.setDeclarationDate(Form15DataUtil.toXmlDate(declarationDate));
        this.basicdetails.setAmtOfIncPaid(incomePaid.toBigInteger());
        this.basicdetails.setDateIncPaid(Form15DataUtil.toXmlDate(incomePaidDate));
        this.part1.setBasicdtls(this.basicdetails);
        if (this.basicdetails.getTaxAssessedFlag().equalsIgnoreCase("Y"))
            this.basicdetails.setLatestAsstYr("" + latestAssesmentYear);
        return this;
    }

    @Override
    public AddMoreBuilder<Form15GBasicDetailsBuilder> incomeDetails(String uniqueIdentifiactionNo, String investmentAccountIdNo,
                                                                    String natureOfIncome, String tdsSection, BigDecimal incomeAmount) {
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
        this.form15G.getForm15GDtls().getPart1Dtls().getPart1().add(this.part1);
        return this;
    }

    @Override
    public Form15GBasicDetailsBuilder addMore() {
        return this;
    }

    @Override
    public File generate() {
        try {
            this.sourcefile = new File(Form15XmlUtil.formsDirectory() + File.separator + "Source_" + new Date().getTime() + this.fileName);
            JAXBContext context = JAXBContext.newInstance("com.ey.in.tds.dividend.fifteen.g");
            JAXBElement<FORM15G> element = new JAXBElement<FORM15G>(_FORM15G_QNAME, FORM15G.class, null, this.form15G);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("jaxb.formatted.output", true);
            marshaller.marshal(element, this.sourcefile);
        } catch (JAXBException e) {
            e.printStackTrace();
            throw new FormGenerationException(e, "Error while generating Form 15G source xml");
        }
        return this.sourcefile;
    }
}
