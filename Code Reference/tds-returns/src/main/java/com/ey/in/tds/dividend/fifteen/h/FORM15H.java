//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.08.20 at 07:06:56 PM IST 
//


package com.ey.in.tds.dividend.fifteen.h;

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
 *         &lt;element name="FormCreationInfo"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element ref="{http://incometaxindiaefiling.gov.in/common}CreationInfo"/&gt;
 *                   &lt;element ref="{http://incometaxindiaefiling.gov.in/common}Form_Details"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element ref="{http://incometaxindiaefiling.gov.in/FORM15H}Form15HDtls"/&gt;
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
    "formCreationInfo",
    "form15HDtls"
})
@XmlRootElement(name = "FORM15H")
public class FORM15H {

    @XmlElement(name = "FormCreationInfo", required = true)
    protected FormCreationInfo formCreationInfo;
    @XmlElement(name = "Form15HDtls", required = true)
    protected Form15HDtls form15HDtls;

    /**
     * Gets the value of the formCreationInfo property.
     * 
     * @return
     *     possible object is
     *     {@link FormCreationInfo }
     *     
     */
    public FormCreationInfo getFormCreationInfo() {
        return formCreationInfo;
    }

    /**
     * Sets the value of the formCreationInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link FormCreationInfo }
     *     
     */
    public void setFormCreationInfo(FormCreationInfo value) {
        this.formCreationInfo = value;
    }

    /**
     * Gets the value of the form15HDtls property.
     * 
     * @return
     *     possible object is
     *     {@link Form15HDtls }
     *     
     */
    public Form15HDtls getForm15HDtls() {
        return form15HDtls;
    }

    /**
     * Sets the value of the form15HDtls property.
     * 
     * @param value
     *     allowed object is
     *     {@link Form15HDtls }
     *     
     */
    public void setForm15HDtls(Form15HDtls value) {
        this.form15HDtls = value;
    }


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
        "formDetails"
    })
    public static class FormCreationInfo {

        @XmlElement(name = "CreationInfo", namespace = "http://incometaxindiaefiling.gov.in/common", required = true)
        protected CreationInfo creationInfo;
        @XmlElement(name = "Form_Details", namespace = "http://incometaxindiaefiling.gov.in/common", required = true)
        protected FormDetails formDetails;

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

    }

}
