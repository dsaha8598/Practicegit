//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.08.20 at 07:06:56 PM IST 
//


package com.ey.in.tds.dividend.fifteen.h;

import java.util.ArrayList;
import java.util.List;
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
 *         &lt;element ref="{http://incometaxindiaefiling.gov.in/FORM15H}Basicdtls"/&gt;
 *         &lt;element name="IncomeDtls"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element ref="{http://incometaxindiaefiling.gov.in/FORM15H}IncomeDtl" maxOccurs="unbounded"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
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
    "basicdtls",
    "incomeDtls"
})
@XmlRootElement(name = "Part1")
public class Part1 {

    @XmlElement(name = "Basicdtls", required = true)
    protected Basicdtls basicdtls;
    @XmlElement(name = "IncomeDtls", required = true)
    protected IncomeDtls incomeDtls;

    /**
     * Gets the value of the basicdtls property.
     * 
     * @return
     *     possible object is
     *     {@link Basicdtls }
     *     
     */
    public Basicdtls getBasicdtls() {
        return basicdtls;
    }

    /**
     * Sets the value of the basicdtls property.
     * 
     * @param value
     *     allowed object is
     *     {@link Basicdtls }
     *     
     */
    public void setBasicdtls(Basicdtls value) {
        this.basicdtls = value;
    }

    /**
     * Gets the value of the incomeDtls property.
     * 
     * @return
     *     possible object is
     *     {@link Part1 .IncomeDtls }
     *     
     */
    public IncomeDtls getIncomeDtls() {
        return incomeDtls;
    }

    /**
     * Sets the value of the incomeDtls property.
     * 
     * @param value
     *     allowed object is
     *     {@link Part1 .IncomeDtls }
     *     
     */
    public void setIncomeDtls(IncomeDtls value) {
        this.incomeDtls = value;
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
     *         &lt;element ref="{http://incometaxindiaefiling.gov.in/FORM15H}IncomeDtl" maxOccurs="unbounded"/&gt;
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
        "incomeDtl"
    })
    public static class IncomeDtls {

        @XmlElement(name = "IncomeDtl", required = true)
        protected List<IncomeDtl> incomeDtl;

        /**
         * Gets the value of the incomeDtl property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the incomeDtl property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getIncomeDtl().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link IncomeDtl }
         * 
         * 
         */
        public List<IncomeDtl> getIncomeDtl() {
            if (incomeDtl == null) {
                incomeDtl = new ArrayList<IncomeDtl>();
            }
            return this.incomeDtl;
        }

    }

}
