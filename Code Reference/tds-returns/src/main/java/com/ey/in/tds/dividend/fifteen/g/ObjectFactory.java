//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.08.20 at 07:06:56 PM IST 
//


package com.ey.in.tds.dividend.fifteen.g;

import java.math.BigDecimal;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.ey.in.tds.dividend.fifteen.g package. 
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
    private final static QName _FormName_QNAME = new QName("http://incometaxindiaefiling.gov.in/common", "FormName");
    private final static QName _AssessmentYear_QNAME = new QName("http://incometaxindiaefiling.gov.in/common", "AssessmentYear");
    private final static QName _SchemaVer_QNAME = new QName("http://incometaxindiaefiling.gov.in/common", "SchemaVer");
    private final static QName _FormVer_QNAME = new QName("http://incometaxindiaefiling.gov.in/common", "FormVer");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.ey.in.tds.dividend.fifteen.g
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link FORM15G }
     * 
     */
    public FORM15G createFORM15G() {
        return new FORM15G();
    }

    /**
     * Create an instance of {@link Form15GDtls }
     * 
     */
    public Form15GDtls createForm15GDtls() {
        return new Form15GDtls();
    }

    /**
     * Create an instance of {@link Part1 }
     * 
     */
    public Part1 createPart1() {
        return new Part1();
    }

    /**
     * Create an instance of {@link FORM15G.FormCreationInfo }
     * 
     */
    public FORM15G.FormCreationInfo createFORM15GFormCreationInfo() {
        return new FORM15G.FormCreationInfo();
    }

    /**
     * Create an instance of {@link Form15GDtls.Part1Dtls }
     * 
     */
    public Form15GDtls.Part1Dtls createForm15GDtlsPart1Dtls() {
        return new Form15GDtls.Part1Dtls();
    }

    /**
     * Create an instance of {@link Basicdtls }
     * 
     */
    public Basicdtls createBasicdtls() {
        return new Basicdtls();
    }

    /**
     * Create an instance of {@link Part1 .IncomeDtls }
     * 
     */
    public Part1 .IncomeDtls createPart1IncomeDtls() {
        return new Part1 .IncomeDtls();
    }

    /**
     * Create an instance of {@link IncomeDtl }
     * 
     */
    public IncomeDtl createIncomeDtl() {
        return new IncomeDtl();
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
    @XmlElementDecl(namespace = "http://incometaxindiaefiling.gov.in/common", name = "FormName")
    public JAXBElement<String> createFormName(String value) {
        return new JAXBElement<String>(_FormName_QNAME, String.class, null, value);
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

}
