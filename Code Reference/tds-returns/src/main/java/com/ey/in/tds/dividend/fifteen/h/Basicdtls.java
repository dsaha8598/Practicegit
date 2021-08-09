//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.08.20 at 07:06:56 PM IST 
//


package com.ey.in.tds.dividend.fifteen.h;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="AssesseeName"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://incometaxindiaefiling.gov.in/FORM15H}nonEmptyString"&gt;
 *               &lt;minLength value="1"/&gt;
 *               &lt;maxLength value="125"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="AssesseePAN"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *               &lt;length value="10"/&gt;
 *               &lt;pattern value="[A-Z]{5}[0-9]{4}[A-Z]"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="AssesseeDOB"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}date"&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="PreviousYr"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *               &lt;enumeration value="2010"/&gt;
 *               &lt;enumeration value="2011"/&gt;
 *               &lt;enumeration value="2012"/&gt;
 *               &lt;enumeration value="2013"/&gt;
 *               &lt;enumeration value="2014"/&gt;
 *               &lt;enumeration value="2015"/&gt;
 *               &lt;enumeration value="2016"/&gt;
 *               &lt;enumeration value="2017"/&gt;
 *               &lt;enumeration value="2018"/&gt;
 *               &lt;enumeration value="2019"/&gt;
 *               &lt;enumeration value="2020"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="FlatDoorNo"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://incometaxindiaefiling.gov.in/FORM15H}nonEmptyString"&gt;
 *               &lt;maxLength value="50"/&gt;
 *               &lt;minLength value="1"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="PremisesName" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://incometaxindiaefiling.gov.in/FORM15H}nonEmptyString"&gt;
 *               &lt;minLength value="0"/&gt;
 *               &lt;maxLength value="50"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="RoadOrStreet" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://incometaxindiaefiling.gov.in/FORM15H}nonEmptyString"&gt;
 *               &lt;maxLength value="50"/&gt;
 *               &lt;minLength value="0"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="LocalityOrArea"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://incometaxindiaefiling.gov.in/FORM15H}nonEmptyString"&gt;
 *               &lt;maxLength value="50"/&gt;
 *               &lt;minLength value="1"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="CityOrTownOrDistrict"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://incometaxindiaefiling.gov.in/FORM15H}nonEmptyString"&gt;
 *               &lt;minLength value="1"/&gt;
 *               &lt;maxLength value="50"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="StateCode"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *               &lt;maxLength value="2"/&gt;
 *               &lt;minLength value="1"/&gt;
 *               &lt;enumeration value="01"/&gt;
 *               &lt;enumeration value="02"/&gt;
 *               &lt;enumeration value="03"/&gt;
 *               &lt;enumeration value="04"/&gt;
 *               &lt;enumeration value="05"/&gt;
 *               &lt;enumeration value="06"/&gt;
 *               &lt;enumeration value="07"/&gt;
 *               &lt;enumeration value="08"/&gt;
 *               &lt;enumeration value="09"/&gt;
 *               &lt;enumeration value="10"/&gt;
 *               &lt;enumeration value="11"/&gt;
 *               &lt;enumeration value="12"/&gt;
 *               &lt;enumeration value="13"/&gt;
 *               &lt;enumeration value="14"/&gt;
 *               &lt;enumeration value="15"/&gt;
 *               &lt;enumeration value="16"/&gt;
 *               &lt;enumeration value="17"/&gt;
 *               &lt;enumeration value="18"/&gt;
 *               &lt;enumeration value="19"/&gt;
 *               &lt;enumeration value="20"/&gt;
 *               &lt;enumeration value="21"/&gt;
 *               &lt;enumeration value="22"/&gt;
 *               &lt;enumeration value="23"/&gt;
 *               &lt;enumeration value="24"/&gt;
 *               &lt;enumeration value="25"/&gt;
 *               &lt;enumeration value="26"/&gt;
 *               &lt;enumeration value="27"/&gt;
 *               &lt;enumeration value="28"/&gt;
 *               &lt;enumeration value="29"/&gt;
 *               &lt;enumeration value="30"/&gt;
 *               &lt;enumeration value="31"/&gt;
 *               &lt;enumeration value="32"/&gt;
 *               &lt;enumeration value="33"/&gt;
 *               &lt;enumeration value="34"/&gt;
 *               &lt;enumeration value="35"/&gt;
 *               &lt;enumeration value="36"/&gt;
 *               &lt;enumeration value="37"/&gt;
 *               &lt;enumeration value="99"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="PinCode"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *               &lt;pattern value="[1-9]{1}[0-9]{5}"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="EmailAddress" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://incometaxindiaefiling.gov.in/FORM15H}nonEmptyString"&gt;
 *               &lt;maxLength value="125"/&gt;
 *               &lt;pattern value="|([\.a-zA-Z0-9_\-])+@([a-zA-Z0-9_\-])+(([a-zA-Z0-9_\-])*\.([a-zA-Z0-9_\-])+)+"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="STDcode" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}normalizedString"&gt;
 *               &lt;pattern value="[1-9]{1}[0-9]{0,4}|"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="PhoneNo" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}normalizedString"&gt;
 *               &lt;pattern value="[1-9]{1}[0-9]{0,9}|"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="MobileNo"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *               &lt;pattern value="[1-9]{1}[0-9]{9}"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="TaxAssessedFlag"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *               &lt;enumeration value="Y"/&gt;
 *               &lt;enumeration value="N"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="LatestAsstYr" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *               &lt;enumeration value=""/&gt;
 *               &lt;enumeration value="2009"/&gt;
 *               &lt;enumeration value="2010"/&gt;
 *               &lt;enumeration value="2011"/&gt;
 *               &lt;enumeration value="2012"/&gt;
 *               &lt;enumeration value="2013"/&gt;
 *               &lt;enumeration value="2014"/&gt;
 *               &lt;enumeration value="2015"/&gt;
 *               &lt;enumeration value="2016"/&gt;
 *               &lt;enumeration value="2017"/&gt;
 *               &lt;enumeration value="2018"/&gt;
 *               &lt;enumeration value="2019"/&gt;
 *               &lt;enumeration value="2020"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="EstimatedInc"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}unsignedLong"&gt;
 *               &lt;totalDigits value="14"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="EstimatedTotalIncPrvYr"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}long"&gt;
 *               &lt;totalDigits value="15"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;sequence minOccurs="0"&gt;
 *           &lt;element name="TotalNoOfForm15H"&gt;
 *             &lt;simpleType&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}unsignedLong"&gt;
 *                 &lt;totalDigits value="4"/&gt;
 *               &lt;/restriction&gt;
 *             &lt;/simpleType&gt;
 *           &lt;/element&gt;
 *           &lt;element name="AggregateAmtForm15H"&gt;
 *             &lt;simpleType&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}unsignedLong"&gt;
 *                 &lt;totalDigits value="14"/&gt;
 *               &lt;/restriction&gt;
 *             &lt;/simpleType&gt;
 *           &lt;/element&gt;
 *         &lt;/sequence&gt;
 *         &lt;element name="UniqueIdNumber"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://incometaxindiaefiling.gov.in/FORM15H}nonEmptyString"&gt;
 *               &lt;length value="26"/&gt;
 *               &lt;pattern value="[H][0-9]{9}[0-9]{6}[A-Z]{4}[0-9]{5}[A-Z]"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="DeclarationDate"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}date"&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="AmtOfIncPaid"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}unsignedLong"&gt;
 *               &lt;totalDigits value="14"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="DateIncPaid"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}date"&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="RecordType" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *               &lt;enumeration value=""/&gt;
 *               &lt;enumeration value="A"/&gt;
 *               &lt;enumeration value="D"/&gt;
 *               &lt;enumeration value="U"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "assesseeName",
    "assesseePAN",
    "assesseeDOB",
    "previousYr",
    "flatDoorNo",
    "premisesName",
    "roadOrStreet",
    "localityOrArea",
    "cityOrTownOrDistrict",
    "stateCode",
    "pinCode",
    "emailAddress",
    "stDcode",
    "phoneNo",
    "mobileNo",
    "taxAssessedFlag",
    "latestAsstYr",
    "estimatedInc",
    "estimatedTotalIncPrvYr",
    "totalNoOfForm15H",
    "aggregateAmtForm15H",
    "uniqueIdNumber",
    "declarationDate",
    "amtOfIncPaid",
    "dateIncPaid",
    "recordType"
})
@XmlRootElement(name = "Basicdtls")
public class Basicdtls {

    @XmlElement(name = "AssesseeName", required = true)
    protected String assesseeName;
    @XmlElement(name = "AssesseePAN", required = true)
    protected String assesseePAN;
    @XmlElement(name = "AssesseeDOB", required = true)
    protected XMLGregorianCalendar assesseeDOB;
    @XmlElement(name = "PreviousYr", required = true)
    protected String previousYr;
    @XmlElement(name = "FlatDoorNo", required = true)
    protected String flatDoorNo;
    @XmlElement(name = "PremisesName")
    protected String premisesName;
    @XmlElement(name = "RoadOrStreet")
    protected String roadOrStreet;
    @XmlElement(name = "LocalityOrArea", required = true)
    protected String localityOrArea;
    @XmlElement(name = "CityOrTownOrDistrict", required = true)
    protected String cityOrTownOrDistrict;
    @XmlElement(name = "StateCode", required = true)
    protected String stateCode;
    @XmlElement(name = "PinCode", required = true)
    protected String pinCode;
    @XmlElement(name = "EmailAddress")
    protected String emailAddress;
    @XmlElement(name = "STDcode")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String stDcode;
    @XmlElement(name = "PhoneNo")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String phoneNo;
    @XmlElement(name = "MobileNo", required = true)
    protected String mobileNo;
    @XmlElement(name = "TaxAssessedFlag", required = true)
    protected String taxAssessedFlag;
    @XmlElement(name = "LatestAsstYr")
    protected String latestAsstYr;
    @XmlElement(name = "EstimatedInc", required = true)
    protected BigInteger estimatedInc;
    @XmlElement(name = "EstimatedTotalIncPrvYr")
    protected long estimatedTotalIncPrvYr;
    @XmlElement(name = "TotalNoOfForm15H")
    protected BigInteger totalNoOfForm15H;
    @XmlElement(name = "AggregateAmtForm15H")
    protected BigInteger aggregateAmtForm15H;
    @XmlElement(name = "UniqueIdNumber", required = true)
    protected String uniqueIdNumber;
    @XmlElement(name = "DeclarationDate", required = true)
    protected XMLGregorianCalendar declarationDate;
    @XmlElement(name = "AmtOfIncPaid", required = true)
    protected BigInteger amtOfIncPaid;
    @XmlElement(name = "DateIncPaid", required = true)
    protected XMLGregorianCalendar dateIncPaid;
    @XmlElement(name = "RecordType")
    protected String recordType;

    /**
     * Gets the value of the assesseeName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAssesseeName() {
        return assesseeName;
    }

    /**
     * Sets the value of the assesseeName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAssesseeName(String value) {
        this.assesseeName = value;
    }

    /**
     * Gets the value of the assesseePAN property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAssesseePAN() {
        return assesseePAN;
    }

    /**
     * Sets the value of the assesseePAN property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAssesseePAN(String value) {
        this.assesseePAN = value;
    }

    /**
     * Gets the value of the assesseeDOB property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getAssesseeDOB() {
        return assesseeDOB;
    }

    /**
     * Sets the value of the assesseeDOB property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setAssesseeDOB(XMLGregorianCalendar value) {
        this.assesseeDOB = value;
    }

    /**
     * Gets the value of the previousYr property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPreviousYr() {
        return previousYr;
    }

    /**
     * Sets the value of the previousYr property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPreviousYr(String value) {
        this.previousYr = value;
    }

    /**
     * Gets the value of the flatDoorNo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFlatDoorNo() {
        return flatDoorNo;
    }

    /**
     * Sets the value of the flatDoorNo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFlatDoorNo(String value) {
        this.flatDoorNo = value;
    }

    /**
     * Gets the value of the premisesName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPremisesName() {
        return premisesName;
    }

    /**
     * Sets the value of the premisesName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPremisesName(String value) {
        this.premisesName = value;
    }

    /**
     * Gets the value of the roadOrStreet property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRoadOrStreet() {
        return roadOrStreet;
    }

    /**
     * Sets the value of the roadOrStreet property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRoadOrStreet(String value) {
        this.roadOrStreet = value;
    }

    /**
     * Gets the value of the localityOrArea property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocalityOrArea() {
        return localityOrArea;
    }

    /**
     * Sets the value of the localityOrArea property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocalityOrArea(String value) {
        this.localityOrArea = value;
    }

    /**
     * Gets the value of the cityOrTownOrDistrict property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCityOrTownOrDistrict() {
        return cityOrTownOrDistrict;
    }

    /**
     * Sets the value of the cityOrTownOrDistrict property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCityOrTownOrDistrict(String value) {
        this.cityOrTownOrDistrict = value;
    }

    /**
     * Gets the value of the stateCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStateCode() {
        return stateCode;
    }

    /**
     * Sets the value of the stateCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStateCode(String value) {
        this.stateCode = value;
    }

    /**
     * Gets the value of the pinCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPinCode() {
        return pinCode;
    }

    /**
     * Sets the value of the pinCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPinCode(String value) {
        this.pinCode = value;
    }

    /**
     * Gets the value of the emailAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * Sets the value of the emailAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmailAddress(String value) {
        this.emailAddress = value;
    }

    /**
     * Gets the value of the stDcode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSTDcode() {
        return stDcode;
    }

    /**
     * Sets the value of the stDcode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSTDcode(String value) {
        this.stDcode = value;
    }

    /**
     * Gets the value of the phoneNo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPhoneNo() {
        return phoneNo;
    }

    /**
     * Sets the value of the phoneNo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPhoneNo(String value) {
        this.phoneNo = value;
    }

    /**
     * Gets the value of the mobileNo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMobileNo() {
        return mobileNo;
    }

    /**
     * Sets the value of the mobileNo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMobileNo(String value) {
        this.mobileNo = value;
    }

    /**
     * Gets the value of the taxAssessedFlag property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaxAssessedFlag() {
        return taxAssessedFlag;
    }

    /**
     * Sets the value of the taxAssessedFlag property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaxAssessedFlag(String value) {
        this.taxAssessedFlag = value;
    }

    /**
     * Gets the value of the latestAsstYr property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLatestAsstYr() {
        return latestAsstYr;
    }

    /**
     * Sets the value of the latestAsstYr property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLatestAsstYr(String value) {
        this.latestAsstYr = value;
    }

    /**
     * Gets the value of the estimatedInc property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getEstimatedInc() {
        return estimatedInc;
    }

    /**
     * Sets the value of the estimatedInc property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setEstimatedInc(BigInteger value) {
        this.estimatedInc = value;
    }

    /**
     * Gets the value of the estimatedTotalIncPrvYr property.
     * 
     */
    public long getEstimatedTotalIncPrvYr() {
        return estimatedTotalIncPrvYr;
    }

    /**
     * Sets the value of the estimatedTotalIncPrvYr property.
     * 
     */
    public void setEstimatedTotalIncPrvYr(long value) {
        this.estimatedTotalIncPrvYr = value;
    }

    /**
     * Gets the value of the totalNoOfForm15H property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTotalNoOfForm15H() {
        return totalNoOfForm15H;
    }

    /**
     * Sets the value of the totalNoOfForm15H property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTotalNoOfForm15H(BigInteger value) {
        this.totalNoOfForm15H = value;
    }

    /**
     * Gets the value of the aggregateAmtForm15H property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getAggregateAmtForm15H() {
        return aggregateAmtForm15H;
    }

    /**
     * Sets the value of the aggregateAmtForm15H property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setAggregateAmtForm15H(BigInteger value) {
        this.aggregateAmtForm15H = value;
    }

    /**
     * Gets the value of the uniqueIdNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUniqueIdNumber() {
        return uniqueIdNumber;
    }

    /**
     * Sets the value of the uniqueIdNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUniqueIdNumber(String value) {
        this.uniqueIdNumber = value;
    }

    /**
     * Gets the value of the declarationDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDeclarationDate() {
        return declarationDate;
    }

    /**
     * Sets the value of the declarationDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDeclarationDate(XMLGregorianCalendar value) {
        this.declarationDate = value;
    }

    /**
     * Gets the value of the amtOfIncPaid property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getAmtOfIncPaid() {
        return amtOfIncPaid;
    }

    /**
     * Sets the value of the amtOfIncPaid property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setAmtOfIncPaid(BigInteger value) {
        this.amtOfIncPaid = value;
    }

    /**
     * Gets the value of the dateIncPaid property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateIncPaid() {
        return dateIncPaid;
    }

    /**
     * Sets the value of the dateIncPaid property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateIncPaid(XMLGregorianCalendar value) {
        this.dateIncPaid = value;
    }

    /**
     * Gets the value of the recordType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRecordType() {
        return recordType;
    }

    /**
     * Sets the value of the recordType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRecordType(String value) {
        this.recordType = value;
    }

}