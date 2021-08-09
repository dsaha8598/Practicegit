//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.08.20 at 07:06:56 PM IST 
//


package com.ey.in.tds.dividend.fifteen.ca;

import java.math.BigDecimal;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.ey.in.tds.dividend.fifteen.ca package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _SWVersionNo_QNAME = new QName("http://incometaxindiaefiling.gov.in/common", "SWVersionNo");
    private final static QName _SWCreatedBy_QNAME = new QName("http://incometaxindiaefiling.gov.in/common", "SWCreatedBy");
    private final static QName _XMLCreatedBy_QNAME = new QName("http://incometaxindiaefiling.gov.in/common", "XMLCreatedBy");
    private final static QName _XMLCreationDate_QNAME = new QName("http://incometaxindiaefiling.gov.in/common", "XMLCreationDate");
    private final static QName _IntermediaryCity_QNAME = new QName("http://incometaxindiaefiling.gov.in/common", "IntermediaryCity");
    private final static QName _FormName_QNAME = new QName("http://incometaxindiaefiling.gov.in/common", "FormName");
    private final static QName _Description_QNAME = new QName("http://incometaxindiaefiling.gov.in/common", "Description");
    private final static QName _AssessmentYear_QNAME = new QName("http://incometaxindiaefiling.gov.in/common", "AssessmentYear");
    private final static QName _SchemaVer_QNAME = new QName("http://incometaxindiaefiling.gov.in/common", "SchemaVer");
    private final static QName _FormVer_QNAME = new QName("http://incometaxindiaefiling.gov.in/common", "FormVer");
    private final static QName _Declaration_QNAME = new QName("http://incometaxindiaefiling.gov.in/FORM15CAB", "Declaration");
    private final static QName _State_QNAME = new QName("http://incometaxindiaefiling.gov.in/common", "State");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.ey.in.tds.dividend.fifteen.ca
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link FORM15CA }
     * 
     */
    public FORM15CA createFORM15CA() {
        return new FORM15CA();
    }

    /**
     * Create an instance of {@link CreationInfo }
     * 
     */
    public CreationInfo createCreationInfo() {
        return new CreationInfo();
    }

    /**
     * Create an instance of {@link FormDetails }
     * 
     */
    public FormDetails createFormDetails() {
        return new FormDetails();
    }

    /**
     * Create an instance of {@link RemitterDetls }
     * 
     */
    public RemitterDetls createRemitterDetls() {
        return new RemitterDetls();
    }

    /**
     * Create an instance of {@link AddressType }
     * 
     */
    public AddressType createAddressType() {
        return new AddressType();
    }

    /**
     * Create an instance of {@link RemitteeDetls }
     * 
     */
    public RemitteeDetls createRemitteeDetls() {
        return new RemitteeDetls();
    }

    /**
     * Create an instance of {@link RemittanceDetls }
     * 
     */
    public RemittanceDetls createRemittanceDetls() {
        return new RemittanceDetls();
    }

    /**
     * Create an instance of {@link AcctntDetls }
     * 
     */
    public AcctntDetls createAcctntDetls() {
        return new AcctntDetls();
    }

    /**
     * Create an instance of {@link AcntntAddressType }
     * 
     */
    public AcntntAddressType createAcntntAddressType() {
        return new AcntntAddressType();
    }

    /**
     * Create an instance of {@link AoOrderDetls }
     * 
     */
    public AoOrderDetls createAoOrderDetls() {
        return new AoOrderDetls();
    }

    /**
     * Create an instance of {@link ItActDetails }
     * 
     */
    public ItActDetails createItActDetails() {
        return new ItActDetails();
    }

    /**
     * Create an instance of {@link DTAADetails }
     * 
     */
    public DTAADetails createDTAADetails() {
        return new DTAADetails();
    }

    /**
     * Create an instance of {@link TDSDetails }
     * 
     */
    public TDSDetails createTDSDetails() {
        return new TDSDetails();
    }

    /**
     * Create an instance of {@link Declaration }
     * 
     */
    public Declaration createDeclaration() {
        return new Declaration();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://incometaxindiaefiling.gov.in/common", name = "SWVersionNo")
    public JAXBElement<String> createSWVersionNo(String value) {
        return new JAXBElement<String>(_SWVersionNo_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://incometaxindiaefiling.gov.in/common", name = "SWCreatedBy")
    public JAXBElement<String> createSWCreatedBy(String value) {
        return new JAXBElement<String>(_SWCreatedBy_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://incometaxindiaefiling.gov.in/common", name = "XMLCreatedBy")
    public JAXBElement<String> createXMLCreatedBy(String value) {
        return new JAXBElement<String>(_XMLCreatedBy_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://incometaxindiaefiling.gov.in/common", name = "XMLCreationDate")
    public JAXBElement<XMLGregorianCalendar> createXMLCreationDate(XMLGregorianCalendar value) {
        return new JAXBElement<XMLGregorianCalendar>(_XMLCreationDate_QNAME, XMLGregorianCalendar.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://incometaxindiaefiling.gov.in/common", name = "IntermediaryCity")
    public JAXBElement<String> createIntermediaryCity(String value) {
        return new JAXBElement<String>(_IntermediaryCity_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://incometaxindiaefiling.gov.in/common", name = "FormName")
    public JAXBElement<String> createFormName(String value) {
        return new JAXBElement<String>(_FormName_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://incometaxindiaefiling.gov.in/common", name = "Description")
    public JAXBElement<String> createDescription(String value) {
        return new JAXBElement<String>(_Description_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://incometaxindiaefiling.gov.in/common", name = "AssessmentYear")
    public JAXBElement<String> createAssessmentYear(String value) {
        return new JAXBElement<String>(_AssessmentYear_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://incometaxindiaefiling.gov.in/common", name = "SchemaVer")
    public JAXBElement<String> createSchemaVer(String value) {
        return new JAXBElement<String>(_SchemaVer_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigDecimal }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://incometaxindiaefiling.gov.in/common", name = "FormVer")
    public JAXBElement<BigDecimal> createFormVer(BigDecimal value) {
        return new JAXBElement<BigDecimal>(_FormVer_QNAME, BigDecimal.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Declaration }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://incometaxindiaefiling.gov.in/FORM15CAB", name = "Declaration")
    public JAXBElement<Declaration> createDeclaration(Declaration value) {
        return new JAXBElement<Declaration>(_Declaration_QNAME, Declaration.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://incometaxindiaefiling.gov.in/common", name = "State")
    public JAXBElement<String> createState(String value) {
        return new JAXBElement<String>(_State_QNAME, String.class, null, value);
    }

}