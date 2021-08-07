//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.08.20 at 07:06:56 PM IST 
//


package com.ey.in.tds.dividend.fifteen.cb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element ref="{http://incometaxindiaefiling.gov.in/common}CreationInfo"/&gt;
 *         &lt;element ref="{http://incometaxindiaefiling.gov.in/common}Form_Details"/&gt;
 *         &lt;element ref="{http://incometaxindiaefiling.gov.in/FORM15CAB}RemitterDetails"/&gt;
 *         &lt;element ref="{http://incometaxindiaefiling.gov.in/FORM15CAB}RemitteeDetls"/&gt;
 *         &lt;element ref="{http://incometaxindiaefiling.gov.in/FORM15CAB}RemittanceDetails"/&gt;
 *         &lt;element ref="{http://incometaxindiaefiling.gov.in/FORM15CAB}ItActDetails"/&gt;
 *         &lt;element ref="{http://incometaxindiaefiling.gov.in/FORM15CAB}DTAADetails"/&gt;
 *         &lt;element ref="{http://incometaxindiaefiling.gov.in/FORM15CAB}TDSDetails"/&gt;
 *         &lt;element ref="{http://incometaxindiaefiling.gov.in/FORM15CAB}AcctntDetls"/&gt;
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
    "creationInfo",
    "formDetails",
    "remitterDetails",
    "remitteeDetls",
    "remittanceDetails",
    "itActDetails",
    "dtaaDetails",
    "tdsDetails",
    "acctntDetls"
})
@XmlRootElement(name = "FORM15CB")
public class FORM15CB {

    @XmlElement(name = "CreationInfo", namespace = "http://incometaxindiaefiling.gov.in/common", required = true)
    protected CreationInfo creationInfo;
    @XmlElement(name = "Form_Details", namespace = "http://incometaxindiaefiling.gov.in/common", required = true)
    protected FormDetails formDetails;
    @XmlElement(name = "RemitterDetails", required = true)
    protected RemitterDetails remitterDetails;
    @XmlElement(name = "RemitteeDetls", required = true)
    protected RemitteeDetls remitteeDetls;
    @XmlElement(name = "RemittanceDetails", required = true)
    protected RemittanceDetails remittanceDetails;
    @XmlElement(name = "ItActDetails", required = true)
    protected ItActDetails itActDetails;
    @XmlElement(name = "DTAADetails", required = true)
    protected DTAADetails dtaaDetails;
    @XmlElement(name = "TDSDetails", required = true)
    protected TDSDetails tdsDetails;
    @XmlElement(name = "AcctntDetls", required = true)
    protected AcctntDetls acctntDetls;

    /**
     * Gets the value of the creationInfo property.
     * 
     * @return
     *     possible object is
     *     {@link CreationInfo }
     *     
     */
    public CreationInfo getCreationInfo() {
        return creationInfo;
    }

    /**
     * Sets the value of the creationInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link CreationInfo }
     *     
     */
    public void setCreationInfo(CreationInfo value) {
        this.creationInfo = value;
    }

    /**
     * Gets the value of the formDetails property.
     * 
     * @return
     *     possible object is
     *     {@link FormDetails }
     *     
     */
    public FormDetails getFormDetails() {
        return formDetails;
    }

    /**
     * Sets the value of the formDetails property.
     * 
     * @param value
     *     allowed object is
     *     {@link FormDetails }
     *     
     */
    public void setFormDetails(FormDetails value) {
        this.formDetails = value;
    }

    /**
     * Gets the value of the remitterDetails property.
     * 
     * @return
     *     possible object is
     *     {@link RemitterDetails }
     *     
     */
    public RemitterDetails getRemitterDetails() {
        return remitterDetails;
    }

    /**
     * Sets the value of the remitterDetails property.
     * 
     * @param value
     *     allowed object is
     *     {@link RemitterDetails }
     *     
     */
    public void setRemitterDetails(RemitterDetails value) {
        this.remitterDetails = value;
    }

    /**
     * Gets the value of the remitteeDetls property.
     * 
     * @return
     *     possible object is
     *     {@link RemitteeDetls }
     *     
     */
    public RemitteeDetls getRemitteeDetls() {
        return remitteeDetls;
    }

    /**
     * Sets the value of the remitteeDetls property.
     * 
     * @param value
     *     allowed object is
     *     {@link RemitteeDetls }
     *     
     */
    public void setRemitteeDetls(RemitteeDetls value) {
        this.remitteeDetls = value;
    }

    /**
     * Gets the value of the remittanceDetails property.
     * 
     * @return
     *     possible object is
     *     {@link RemittanceDetails }
     *     
     */
    public RemittanceDetails getRemittanceDetails() {
        return remittanceDetails;
    }

    /**
     * Sets the value of the remittanceDetails property.
     * 
     * @param value
     *     allowed object is
     *     {@link RemittanceDetails }
     *     
     */
    public void setRemittanceDetails(RemittanceDetails value) {
        this.remittanceDetails = value;
    }

    /**
     * Gets the value of the itActDetails property.
     * 
     * @return
     *     possible object is
     *     {@link ItActDetails }
     *     
     */
    public ItActDetails getItActDetails() {
        return itActDetails;
    }

    /**
     * Sets the value of the itActDetails property.
     * 
     * @param value
     *     allowed object is
     *     {@link ItActDetails }
     *     
     */
    public void setItActDetails(ItActDetails value) {
        this.itActDetails = value;
    }

    /**
     * Gets the value of the dtaaDetails property.
     * 
     * @return
     *     possible object is
     *     {@link DTAADetails }
     *     
     */
    public DTAADetails getDTAADetails() {
        return dtaaDetails;
    }

    /**
     * Sets the value of the dtaaDetails property.
     * 
     * @param value
     *     allowed object is
     *     {@link DTAADetails }
     *     
     */
    public void setDTAADetails(DTAADetails value) {
        this.dtaaDetails = value;
    }

    /**
     * Gets the value of the tdsDetails property.
     * 
     * @return
     *     possible object is
     *     {@link TDSDetails }
     *     
     */
    public TDSDetails getTDSDetails() {
        return tdsDetails;
    }

    /**
     * Sets the value of the tdsDetails property.
     * 
     * @param value
     *     allowed object is
     *     {@link TDSDetails }
     *     
     */
    public void setTDSDetails(TDSDetails value) {
        this.tdsDetails = value;
    }

    /**
     * Gets the value of the acctntDetls property.
     * 
     * @return
     *     possible object is
     *     {@link AcctntDetls }
     *     
     */
    public AcctntDetls getAcctntDetls() {
        return acctntDetls;
    }

    /**
     * Sets the value of the acctntDetls property.
     * 
     * @param value
     *     allowed object is
     *     {@link AcctntDetls }
     *     
     */
    public void setAcctntDetls(AcctntDetls value) {
        this.acctntDetls = value;
    }

}
