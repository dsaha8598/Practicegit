//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.08.20 at 07:06:56 PM IST 
//


package com.ey.in.tds.dividend.fifteen.ca;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
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
 *         &lt;element name="AmtPayBefTds" type="{http://incometaxindiaefiling.gov.in/FORM15CAB}positiveDecimalAmt" minOccurs="0"/&gt;
 *         &lt;element name="AggAmtRem" type="{http://incometaxindiaefiling.gov.in/FORM15CAB}positiveDecimalAmt" minOccurs="0"/&gt;
 *         &lt;element name="NameBankCode" type="{http://incometaxindiaefiling.gov.in/FORM15CAB}nameType"/&gt;
 *         &lt;element name="NameBankDesc" type="{http://incometaxindiaefiling.gov.in/FORM15CAB}nameType" minOccurs="0"/&gt;
 *         &lt;element name="BranchName" type="{http://incometaxindiaefiling.gov.in/FORM15CAB}nameType" minOccurs="0"/&gt;
 *         &lt;element name="PropDateRem"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}date"&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="NatureRemCategory"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *               &lt;maxLength value="5"/&gt;
 *               &lt;minLength value="4"/&gt;
 *               &lt;enumeration value="16.1"/&gt;
 *               &lt;enumeration value="16.2"/&gt;
 *               &lt;enumeration value="16.3"/&gt;
 *               &lt;enumeration value="16.4"/&gt;
 *               &lt;enumeration value="16.5"/&gt;
 *               &lt;enumeration value="16.6"/&gt;
 *               &lt;enumeration value="16.7"/&gt;
 *               &lt;enumeration value="16.8"/&gt;
 *               &lt;enumeration value="16.9"/&gt;
 *               &lt;enumeration value="16.10"/&gt;
 *               &lt;enumeration value="16.11"/&gt;
 *               &lt;enumeration value="16.12"/&gt;
 *               &lt;enumeration value="16.13"/&gt;
 *               &lt;enumeration value="16.14"/&gt;
 *               &lt;enumeration value="16.15"/&gt;
 *               &lt;enumeration value="16.16"/&gt;
 *               &lt;enumeration value="16.17"/&gt;
 *               &lt;enumeration value="16.18"/&gt;
 *               &lt;enumeration value="16.19"/&gt;
 *               &lt;enumeration value="16.20"/&gt;
 *               &lt;enumeration value="16.21"/&gt;
 *               &lt;enumeration value="16.22"/&gt;
 *               &lt;enumeration value="16.23"/&gt;
 *               &lt;enumeration value="16.24"/&gt;
 *               &lt;enumeration value="16.25"/&gt;
 *               &lt;enumeration value="16.26"/&gt;
 *               &lt;enumeration value="16.27"/&gt;
 *               &lt;enumeration value="16.28"/&gt;
 *               &lt;enumeration value="16.29"/&gt;
 *               &lt;enumeration value="16.30"/&gt;
 *               &lt;enumeration value="16.31"/&gt;
 *               &lt;enumeration value="16.32"/&gt;
 *               &lt;enumeration value="16.33"/&gt;
 *               &lt;enumeration value="16.34"/&gt;
 *               &lt;enumeration value="16.35"/&gt;
 *               &lt;enumeration value="16.36"/&gt;
 *               &lt;enumeration value="16.37"/&gt;
 *               &lt;enumeration value="16.38"/&gt;
 *               &lt;enumeration value="16.39"/&gt;
 *               &lt;enumeration value="16.40"/&gt;
 *               &lt;enumeration value="16.41"/&gt;
 *               &lt;enumeration value="16.42"/&gt;
 *               &lt;enumeration value="16.43"/&gt;
 *               &lt;enumeration value="16.44"/&gt;
 *               &lt;enumeration value="16.45"/&gt;
 *               &lt;enumeration value="16.46"/&gt;
 *               &lt;enumeration value="16.47"/&gt;
 *               &lt;enumeration value="16.48"/&gt;
 *               &lt;enumeration value="16.49"/&gt;
 *               &lt;enumeration value="16.50"/&gt;
 *               &lt;enumeration value="16.51"/&gt;
 *               &lt;enumeration value="16.52"/&gt;
 *               &lt;enumeration value="16.53"/&gt;
 *               &lt;enumeration value="16.54"/&gt;
 *               &lt;enumeration value="16.55"/&gt;
 *               &lt;enumeration value="16.56"/&gt;
 *               &lt;enumeration value="16.57"/&gt;
 *               &lt;enumeration value="16.58"/&gt;
 *               &lt;enumeration value="16.59"/&gt;
 *               &lt;enumeration value="16.60"/&gt;
 *               &lt;enumeration value="16.61"/&gt;
 *               &lt;enumeration value="16.62"/&gt;
 *               &lt;enumeration value="16.63"/&gt;
 *               &lt;enumeration value="16.64"/&gt;
 *               &lt;enumeration value="16.99"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="NatureRemCode" type="{http://incometaxindiaefiling.gov.in/FORM15CAB}nameType" minOccurs="0"/&gt;
 *         &lt;element name="AmtTaxDeductn" type="{http://incometaxindiaefiling.gov.in/FORM15CAB}positiveDecimalAmt" minOccurs="0"/&gt;
 *         &lt;element name="DateOfDeductn" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}date"&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="BsrCode" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *               &lt;pattern value="[0-9]{3}[A-Z0-9]{4}"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="CountryRemMadeSecb" type="{http://incometaxindiaefiling.gov.in/common}countryType" minOccurs="0"/&gt;
 *         &lt;element name="CountryRemMadeSecbDesc" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://incometaxindiaefiling.gov.in/FORM15CAB}nonEmptyString"&gt;
 *               &lt;maxLength value="125"/&gt;
 *               &lt;minLength value="0"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="CurrencySecbCode" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://incometaxindiaefiling.gov.in/FORM15CAB}nonEmptyStringOnlyCaps"&gt;
 *               &lt;maxLength value="75"/&gt;
 *               &lt;minLength value="1"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="CurrencySecbDesc" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://incometaxindiaefiling.gov.in/FORM15CAB}nonEmptyStringOnlyCaps"&gt;
 *               &lt;maxLength value="125"/&gt;
 *               &lt;minLength value="1"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="AmtPayForgnRem" type="{http://incometaxindiaefiling.gov.in/FORM15CAB}positiveDecimalAmt" minOccurs="0"/&gt;
 *         &lt;element name="AmtPayIndRem" type="{http://incometaxindiaefiling.gov.in/FORM15CAB}positiveDecimalAmt" minOccurs="0"/&gt;
 *         &lt;element name="TaxPayGrossSecb" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *               &lt;pattern value="Y|N"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="RevPurCategory"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *               &lt;maxLength value="7"/&gt;
 *               &lt;minLength value="6"/&gt;
 *               &lt;enumeration value="RB-0.1"/&gt;
 *               &lt;enumeration value="RB-0.2"/&gt;
 *               &lt;enumeration value="RB-0.3"/&gt;
 *               &lt;enumeration value="RB-0.4"/&gt;
 *               &lt;enumeration value="RB-0.5"/&gt;
 *               &lt;enumeration value="RB-0.6"/&gt;
 *               &lt;enumeration value="RB-0.7"/&gt;
 *               &lt;enumeration value="RB-0.8"/&gt;
 *               &lt;enumeration value="RB-1.1"/&gt;
 *               &lt;enumeration value="RB-2.1"/&gt;
 *               &lt;enumeration value="RB-3.1"/&gt;
 *               &lt;enumeration value="RB-5.1"/&gt;
 *               &lt;enumeration value="RB-6.1"/&gt;
 *               &lt;enumeration value="RB-7.1"/&gt;
 *               &lt;enumeration value="RB-8.1"/&gt;
 *               &lt;enumeration value="RB-9.1"/&gt;
 *               &lt;enumeration value="RB-10.1"/&gt;
 *               &lt;enumeration value="RB-11.1"/&gt;
 *               &lt;enumeration value="RB-12.1"/&gt;
 *               &lt;enumeration value="RB-13.1"/&gt;
 *               &lt;enumeration value="RB-14.1"/&gt;
 *               &lt;enumeration value="RB-15.1"/&gt;
 *               &lt;enumeration value="RB-16.1"/&gt;
 *               &lt;enumeration value="RB-17.1"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="RevPurCode"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *               &lt;maxLength value="13"/&gt;
 *               &lt;minLength value="12"/&gt;
 *               &lt;enumeration value="RB-0.1-S0017"/&gt;
 *               &lt;enumeration value="RB-0.1-S0019"/&gt;
 *               &lt;enumeration value="RB-0.1-S0099"/&gt;
 *               &lt;enumeration value="RB-0.1-S0026"/&gt;
 *               &lt;enumeration value="RB-0.1-S0027"/&gt;
 *               &lt;enumeration value="RB-0.2-S0003"/&gt;
 *               &lt;enumeration value="RB-0.2-S0004"/&gt;
 *               &lt;enumeration value="RB-0.2-S0005"/&gt;
 *               &lt;enumeration value="RB-0.2-S0006"/&gt;
 *               &lt;enumeration value="RB-0.2-S0007"/&gt;
 *               &lt;enumeration value="RB-0.2-S0008"/&gt;
 *               &lt;enumeration value="RB-0.3-S0001"/&gt;
 *               &lt;enumeration value="RB-0.3-S0002"/&gt;
 *               &lt;enumeration value="RB-0.3-S0009"/&gt;
 *               &lt;enumeration value="RB-0.3-S0010"/&gt;
 *               &lt;enumeration value="RB-0.4-S0011"/&gt;
 *               &lt;enumeration value="RB-0.4-S0012"/&gt;
 *               &lt;enumeration value="RB-0.5-S0013"/&gt;
 *               &lt;enumeration value="RB-0.6-S0014"/&gt;
 *               &lt;enumeration value="RB-0.6-S0015"/&gt;
 *               &lt;enumeration value="RB-0.6-S0016"/&gt;
 *               &lt;enumeration value="RB-0.7-S0020"/&gt;
 *               &lt;enumeration value="RB-0.7-S0021"/&gt;
 *               &lt;enumeration value="RB-0.7-S0022"/&gt;
 *               &lt;enumeration value="RB-0.7-S0023"/&gt;
 *               &lt;enumeration value="RB-0.8-S0024"/&gt;
 *               &lt;enumeration value="RB-0.8-S0025"/&gt;
 *               &lt;enumeration value="RB-1.1-S0101"/&gt;
 *               &lt;enumeration value="RB-1.1-S0102"/&gt;
 *               &lt;enumeration value="RB-1.1-S0103"/&gt;
 *               &lt;enumeration value="RB-1.1-S0104"/&gt;
 *               &lt;enumeration value="RB-1.1-S0108"/&gt;
 *               &lt;enumeration value="RB-1.1-S0109"/&gt;
 *               &lt;enumeration value="RB-2.1-S0201"/&gt;
 *               &lt;enumeration value="RB-2.1-S0202"/&gt;
 *               &lt;enumeration value="RB-2.1-S0203"/&gt;
 *               &lt;enumeration value="RB-2.1-S0204"/&gt;
 *               &lt;enumeration value="RB-2.1-S0205"/&gt;
 *               &lt;enumeration value="RB-2.1-S0206"/&gt;
 *               &lt;enumeration value="RB-2.1-S0207"/&gt;
 *               &lt;enumeration value="RB-2.1-S0208"/&gt;
 *               &lt;enumeration value="RB-2.1-S0209"/&gt;
 *               &lt;enumeration value="RB-2.1-S0210"/&gt;
 *               &lt;enumeration value="RB-2.1-S0211"/&gt;
 *               &lt;enumeration value="RB-2.1-S0212"/&gt;
 *               &lt;enumeration value="RB-2.1-S0214"/&gt;
 *               &lt;enumeration value="RB-2.1-S0215"/&gt;
 *               &lt;enumeration value="RB-2.1-S0216"/&gt;
 *               &lt;enumeration value="RB-2.1-S0217"/&gt;
 *               &lt;enumeration value="RB-2.1-S0218"/&gt;
 *               &lt;enumeration value="RB-2.1-S0219"/&gt;
 *               &lt;enumeration value="RB-2.1-S0220"/&gt;
 *               &lt;enumeration value="RB-2.1-S0221"/&gt;
 *               &lt;enumeration value="RB-2.1-S0222"/&gt;
 *               &lt;enumeration value="RB-2.1-S0223"/&gt;
 *               &lt;enumeration value="RB-2.1-S0224"/&gt;
 *               &lt;enumeration value="RB-3.1-S0301"/&gt;
 *               &lt;enumeration value="RB-3.1-S0303"/&gt;
 *               &lt;enumeration value="RB-3.1-S0304"/&gt;
 *               &lt;enumeration value="RB-3.1-S0305"/&gt;
 *               &lt;enumeration value="RB-3.1-S0306"/&gt;
 *               &lt;enumeration value="RB-5.1-S0501"/&gt;
 *               &lt;enumeration value="RB-5.1-S0502"/&gt;
 *               &lt;enumeration value="RB-6.1-S0601"/&gt;
 *               &lt;enumeration value="RB-6.1-S0602"/&gt;
 *               &lt;enumeration value="RB-6.1-S0603"/&gt;
 *               &lt;enumeration value="RB-6.1-S0605"/&gt;
 *               &lt;enumeration value="RB-6.1-S0607"/&gt;
 *               &lt;enumeration value="RB-6.1-S0608"/&gt;
 *               &lt;enumeration value="RB-6.1-S0609"/&gt;
 *               &lt;enumeration value="RB-6.1-S0610"/&gt;
 *               &lt;enumeration value="RB-6.1-S0611"/&gt;
 *               &lt;enumeration value="RB-6.1-S0612"/&gt;
 *               &lt;enumeration value="RB-7.1-S0701"/&gt;
 *               &lt;enumeration value="RB-7.1-S0702"/&gt;
 *               &lt;enumeration value="RB-7.1-S0703"/&gt;
 *               &lt;enumeration value="RB-8.1-S0801"/&gt;
 *               &lt;enumeration value="RB-8.1-S0802"/&gt;
 *               &lt;enumeration value="RB-8.1-S0803"/&gt;
 *               &lt;enumeration value="RB-8.1-S0804"/&gt;
 *               &lt;enumeration value="RB-8.1-S0805"/&gt;
 *               &lt;enumeration value="RB-8.1-S0806"/&gt;
 *               &lt;enumeration value="RB-8.1-S0807"/&gt;
 *               &lt;enumeration value="RB-8.1-S0808"/&gt;
 *               &lt;enumeration value="RB-8.1-S0809"/&gt;
 *               &lt;enumeration value="RB-9.1-S0901"/&gt;
 *               &lt;enumeration value="RB-9.1-S0902"/&gt;
 *               &lt;enumeration value="RB-10.1-S1002"/&gt;
 *               &lt;enumeration value="RB-10.1-S1003"/&gt;
 *               &lt;enumeration value="RB-10.1-S1004"/&gt;
 *               &lt;enumeration value="RB-10.1-S1005"/&gt;
 *               &lt;enumeration value="RB-10.1-S1006"/&gt;
 *               &lt;enumeration value="RB-10.1-S1007"/&gt;
 *               &lt;enumeration value="RB-10.1-S1008"/&gt;
 *               &lt;enumeration value="RB-10.1-S1009"/&gt;
 *               &lt;enumeration value="RB-10.1-S1010"/&gt;
 *               &lt;enumeration value="RB-10.1-S1011"/&gt;
 *               &lt;enumeration value="RB-10.1-S1013"/&gt;
 *               &lt;enumeration value="RB-10.1-S1014"/&gt;
 *               &lt;enumeration value="RB-10.1-S1015"/&gt;
 *               &lt;enumeration value="RB-10.1-S1016"/&gt;
 *               &lt;enumeration value="RB-10.1-S1017"/&gt;
 *               &lt;enumeration value="RB-10.1-S1018"/&gt;
 *               &lt;enumeration value="RB-10.1-S1020"/&gt;
 *               &lt;enumeration value="RB-10.1-S1021"/&gt;
 *               &lt;enumeration value="RB-10.1-S1022"/&gt;
 *               &lt;enumeration value="RB-10.1-S1023"/&gt;
 *               &lt;enumeration value="RB-10.1-S1099"/&gt;
 *               &lt;enumeration value="RB-11.1-S1104"/&gt;
 *               &lt;enumeration value="RB-11.1-S1101"/&gt;
 *               &lt;enumeration value="RB-11.1-S1103"/&gt;
 *               &lt;enumeration value="RB-11.1-S1105"/&gt;
 *               &lt;enumeration value="RB-11.1-S1106"/&gt;
 *               &lt;enumeration value="RB-11.1-S1107"/&gt;
 *               &lt;enumeration value="RB-11.1-S1108"/&gt;
 *               &lt;enumeration value="RB-11.1-S1109"/&gt;
 *               &lt;enumeration value="RB-12.1-S1201"/&gt;
 *               &lt;enumeration value="RB-12.1-S1202"/&gt;
 *               &lt;enumeration value="RB-13.1-S1301"/&gt;
 *               &lt;enumeration value="RB-13.1-S1302"/&gt;
 *               &lt;enumeration value="RB-13.1-S1303"/&gt;
 *               &lt;enumeration value="RB-13.1-S1304"/&gt;
 *               &lt;enumeration value="RB-13.1-S1305"/&gt;
 *               &lt;enumeration value="RB-13.1-S1306"/&gt;
 *               &lt;enumeration value="RB-13.1-S1307"/&gt;
 *               &lt;enumeration value="RB-14.1-S1401"/&gt;
 *               &lt;enumeration value="RB-14.1-S1402"/&gt;
 *               &lt;enumeration value="RB-14.1-S1403"/&gt;
 *               &lt;enumeration value="RB-14.1-S1405"/&gt;
 *               &lt;enumeration value="RB-14.1-S1408"/&gt;
 *               &lt;enumeration value="RB-14.1-S1409"/&gt;
 *               &lt;enumeration value="RB-14.1-S1410"/&gt;
 *               &lt;enumeration value="RB-14.1-S1411"/&gt;
 *               &lt;enumeration value="RB-14.1-S1412"/&gt;
 *               &lt;enumeration value="RB-15.1-S1501"/&gt;
 *               &lt;enumeration value="RB-15.1-S1502"/&gt;
 *               &lt;enumeration value="RB-15.1-S1503"/&gt;
 *               &lt;enumeration value="RB-15.1-S1504"/&gt;
 *               &lt;enumeration value="RB-15.1-S1505"/&gt;
 *               &lt;enumeration value="RB-16.1-S1601"/&gt;
 *               &lt;enumeration value="RB-16.1-S1602"/&gt;
 *               &lt;enumeration value="RB-17.1-S1701"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="RateOfDed" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}decimal"&gt;
 *               &lt;minInclusive value="0"/&gt;
 *               &lt;maxInclusive value="100"/&gt;
 *               &lt;fractionDigits value="2"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="RateofTDS" minOccurs="0"&gt;
 *           &lt;simpleType&gt;
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}decimal"&gt;
 *               &lt;minInclusive value="0"/&gt;
 *               &lt;maxInclusive value="100"/&gt;
 *               &lt;fractionDigits value="2"/&gt;
 *             &lt;/restriction&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="CountryRemMadeRecipient" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/&gt;
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
    "amtPayBefTds",
    "aggAmtRem",
    "nameBankCode",
    "nameBankDesc",
    "branchName",
    "propDateRem",
    "natureRemCategory",
    "natureRemCode",
    "amtTaxDeductn",
    "dateOfDeductn",
    "bsrCode",
    "countryRemMadeSecb",
    "countryRemMadeSecbDesc",
    "currencySecbCode",
    "currencySecbDesc",
    "amtPayForgnRem",
    "amtPayIndRem",
    "taxPayGrossSecb",
    "revPurCategory",
    "revPurCode",
    "rateOfDed",
    "rateofTDS",
    "countryRemMadeRecipient"
})
@XmlRootElement(name = "RemittanceDetls")
public class RemittanceDetls {

    @XmlElement(name = "AmtPayBefTds")
    protected BigDecimal amtPayBefTds;
    @XmlElement(name = "AggAmtRem")
    protected BigDecimal aggAmtRem;
    @XmlElement(name = "NameBankCode", required = true)
    protected String nameBankCode;
    @XmlElement(name = "NameBankDesc")
    protected String nameBankDesc;
    @XmlElement(name = "BranchName")
    protected String branchName;
    @XmlElement(name = "PropDateRem", required = true)
    protected XMLGregorianCalendar propDateRem;
    @XmlElement(name = "NatureRemCategory", required = true)
    protected String natureRemCategory;
    @XmlElement(name = "NatureRemCode")
    protected String natureRemCode;
    @XmlElement(name = "AmtTaxDeductn")
    protected BigDecimal amtTaxDeductn;
    @XmlElement(name = "DateOfDeductn")
    protected XMLGregorianCalendar dateOfDeductn;
    @XmlElement(name = "BsrCode")
    protected String bsrCode;
    @XmlElement(name = "CountryRemMadeSecb")
    protected String countryRemMadeSecb;
    @XmlElement(name = "CountryRemMadeSecbDesc")
    protected String countryRemMadeSecbDesc;
    @XmlElement(name = "CurrencySecbCode")
    protected String currencySecbCode;
    @XmlElement(name = "CurrencySecbDesc")
    protected String currencySecbDesc;
    @XmlElement(name = "AmtPayForgnRem")
    protected BigDecimal amtPayForgnRem;
    @XmlElement(name = "AmtPayIndRem")
    protected BigDecimal amtPayIndRem;
    @XmlElement(name = "TaxPayGrossSecb")
    protected String taxPayGrossSecb;
    @XmlElement(name = "RevPurCategory", required = true)
    protected String revPurCategory;
    @XmlElement(name = "RevPurCode", required = true)
    protected String revPurCode;
    @XmlElement(name = "RateOfDed")
    protected BigDecimal rateOfDed;
    @XmlElement(name = "RateofTDS")
    protected BigDecimal rateofTDS;
    @XmlElement(name = "CountryRemMadeRecipient")
    protected Object countryRemMadeRecipient;

    /**
     * Gets the value of the amtPayBefTds property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getAmtPayBefTds() {
        return amtPayBefTds;
    }

    /**
     * Sets the value of the amtPayBefTds property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setAmtPayBefTds(BigDecimal value) {
        this.amtPayBefTds = value;
    }

    /**
     * Gets the value of the aggAmtRem property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getAggAmtRem() {
        return aggAmtRem;
    }

    /**
     * Sets the value of the aggAmtRem property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setAggAmtRem(BigDecimal value) {
        this.aggAmtRem = value;
    }

    /**
     * Gets the value of the nameBankCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNameBankCode() {
        return nameBankCode;
    }

    /**
     * Sets the value of the nameBankCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNameBankCode(String value) {
        this.nameBankCode = value;
    }

    /**
     * Gets the value of the nameBankDesc property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNameBankDesc() {
        return nameBankDesc;
    }

    /**
     * Sets the value of the nameBankDesc property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNameBankDesc(String value) {
        this.nameBankDesc = value;
    }

    /**
     * Gets the value of the branchName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBranchName() {
        return branchName;
    }

    /**
     * Sets the value of the branchName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBranchName(String value) {
        this.branchName = value;
    }

    /**
     * Gets the value of the propDateRem property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getPropDateRem() {
        return propDateRem;
    }

    /**
     * Sets the value of the propDateRem property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setPropDateRem(XMLGregorianCalendar value) {
        this.propDateRem = value;
    }

    /**
     * Gets the value of the natureRemCategory property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNatureRemCategory() {
        return natureRemCategory;
    }

    /**
     * Sets the value of the natureRemCategory property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNatureRemCategory(String value) {
        this.natureRemCategory = value;
    }

    /**
     * Gets the value of the natureRemCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNatureRemCode() {
        return natureRemCode;
    }

    /**
     * Sets the value of the natureRemCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNatureRemCode(String value) {
        this.natureRemCode = value;
    }

    /**
     * Gets the value of the amtTaxDeductn property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getAmtTaxDeductn() {
        return amtTaxDeductn;
    }

    /**
     * Sets the value of the amtTaxDeductn property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setAmtTaxDeductn(BigDecimal value) {
        this.amtTaxDeductn = value;
    }

    /**
     * Gets the value of the dateOfDeductn property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateOfDeductn() {
        return dateOfDeductn;
    }

    /**
     * Sets the value of the dateOfDeductn property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateOfDeductn(XMLGregorianCalendar value) {
        this.dateOfDeductn = value;
    }

    /**
     * Gets the value of the bsrCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBsrCode() {
        return bsrCode;
    }

    /**
     * Sets the value of the bsrCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBsrCode(String value) {
        this.bsrCode = value;
    }

    /**
     * Gets the value of the countryRemMadeSecb property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCountryRemMadeSecb() {
        return countryRemMadeSecb;
    }

    /**
     * Sets the value of the countryRemMadeSecb property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCountryRemMadeSecb(String value) {
        this.countryRemMadeSecb = value;
    }

    /**
     * Gets the value of the countryRemMadeSecbDesc property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCountryRemMadeSecbDesc() {
        return countryRemMadeSecbDesc;
    }

    /**
     * Sets the value of the countryRemMadeSecbDesc property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCountryRemMadeSecbDesc(String value) {
        this.countryRemMadeSecbDesc = value;
    }

    /**
     * Gets the value of the currencySecbCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCurrencySecbCode() {
        return currencySecbCode;
    }

    /**
     * Sets the value of the currencySecbCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCurrencySecbCode(String value) {
        this.currencySecbCode = value;
    }

    /**
     * Gets the value of the currencySecbDesc property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCurrencySecbDesc() {
        return currencySecbDesc;
    }

    /**
     * Sets the value of the currencySecbDesc property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCurrencySecbDesc(String value) {
        this.currencySecbDesc = value;
    }

    /**
     * Gets the value of the amtPayForgnRem property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getAmtPayForgnRem() {
        return amtPayForgnRem;
    }

    /**
     * Sets the value of the amtPayForgnRem property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setAmtPayForgnRem(BigDecimal value) {
        this.amtPayForgnRem = value;
    }

    /**
     * Gets the value of the amtPayIndRem property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getAmtPayIndRem() {
        return amtPayIndRem;
    }

    /**
     * Sets the value of the amtPayIndRem property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setAmtPayIndRem(BigDecimal value) {
        this.amtPayIndRem = value;
    }

    /**
     * Gets the value of the taxPayGrossSecb property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaxPayGrossSecb() {
        return taxPayGrossSecb;
    }

    /**
     * Sets the value of the taxPayGrossSecb property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaxPayGrossSecb(String value) {
        this.taxPayGrossSecb = value;
    }

    /**
     * Gets the value of the revPurCategory property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRevPurCategory() {
        return revPurCategory;
    }

    /**
     * Sets the value of the revPurCategory property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRevPurCategory(String value) {
        this.revPurCategory = value;
    }

    /**
     * Gets the value of the revPurCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRevPurCode() {
        return revPurCode;
    }

    /**
     * Sets the value of the revPurCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRevPurCode(String value) {
        this.revPurCode = value;
    }

    /**
     * Gets the value of the rateOfDed property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getRateOfDed() {
        return rateOfDed;
    }

    /**
     * Sets the value of the rateOfDed property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setRateOfDed(BigDecimal value) {
        this.rateOfDed = value;
    }

    /**
     * Gets the value of the rateofTDS property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getRateofTDS() {
        return rateofTDS;
    }

    /**
     * Sets the value of the rateofTDS property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setRateofTDS(BigDecimal value) {
        this.rateofTDS = value;
    }

    /**
     * Gets the value of the countryRemMadeRecipient property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getCountryRemMadeRecipient() {
        return countryRemMadeRecipient;
    }

    /**
     * Sets the value of the countryRemMadeRecipient property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setCountryRemMadeRecipient(Object value) {
        this.countryRemMadeRecipient = value;
    }

}
