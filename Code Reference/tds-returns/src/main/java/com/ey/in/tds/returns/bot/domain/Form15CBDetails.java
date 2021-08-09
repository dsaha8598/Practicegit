package com.ey.in.tds.returns.bot.domain;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.ey.in.tcs.common.domain.dividend.InvoiceShareholderNonResident;
import com.ey.in.tds.common.dto.onboarding.deductee.AddressDTO;
import com.ey.in.tds.common.dto.onboarding.shareholder.ShareholderMasterDTO;
import com.ey.in.tds.core.util.ApiStatus;
import com.ey.in.tds.feign.client.OnboardingClient;

@Component
public class Form15CBDetails {

    private String remitteeName;
    private String flatDoorBlockNum;
    private String buildingVillagePremises;
    private String roadStreet;
    private String country;
    private String townCityDistrict;
    private String areaLocality;
    private String zipCode;
    private String state;
    private String ackNum;
    private String ackDate;
    // IMPORTANT: Make sure that the date is in this format only: DD/MM/YYYY
    private String proposedDate;
    private String uniqueShareholderIdentificationNumber;
    private String deductorPan;
    private Integer invoiceId;
    private Date invoicePostingDate;
    private OnboardingClient onboardingClient;

    private static final FastDateFormat Date_Input_Format = FastDateFormat.getInstance("E MMM dd HH:mm:ss z yyyy");
    private static final FastDateFormat Date_Output_DB_Format = FastDateFormat.getInstance("dd/MM/yyyy");

    public Form15CBDetails(String remitteeName, String flatDoorBlockNum, String buildingVillagePremises,
                           String townCityDistrict, String areaLocality, String zipCode, String state,
                           String roadStreet, String country, String ackNum, String proposedDate,
                           String uniqueShareholderIdentificationNumber, String ackDate, String deductorPan,
                           Integer invoiceId,Date invoicePostingDate) {
        this.remitteeName = remitteeName;
        this.flatDoorBlockNum = flatDoorBlockNum;
        this.buildingVillagePremises = buildingVillagePremises;
        this.townCityDistrict = townCityDistrict;
        this.areaLocality = areaLocality;
        this.zipCode = zipCode;
        this.state = state;
        this.roadStreet = roadStreet;
        this.country = country;
        this.ackNum = ackNum;
        this.proposedDate = proposedDate;
        this.uniqueShareholderIdentificationNumber = uniqueShareholderIdentificationNumber;
        this.ackDate = ackDate;
        this.deductorPan = deductorPan;
        this.invoiceId = invoiceId;
        this.invoicePostingDate = invoicePostingDate;
    }


    public Form15CBDetails(String ackNum, String ackDate, String deductorPan, Integer invoiceId,Date invoicePostingDate) {
        this.ackNum = ackNum;
        this.ackDate = ackDate;
        this.deductorPan = deductorPan;
        this.invoiceId = invoiceId;
        this.invoicePostingDate = invoicePostingDate;
    }

    @Autowired
    public Form15CBDetails(OnboardingClient onboardingClient) {
        this.onboardingClient = onboardingClient;
    }

    public Form15CBDetails(String remitteeName, String flatDoorBlockNum, String buildingVillagePremises,
                           String townCityDistrict, String areaLocality, String zipCode, String state,
                           String roadStreet, String country, String ackNum, String proposedDate,
                           String uniqueShareholderIdentificationNumber, String ackDate) {
        this.remitteeName = remitteeName;
        this.flatDoorBlockNum = flatDoorBlockNum;
        this.buildingVillagePremises = buildingVillagePremises;
        this.townCityDistrict = townCityDistrict;
        this.areaLocality = areaLocality;
        this.zipCode = zipCode;
        this.state = state;
        this.roadStreet = roadStreet;
        this.country = country;
        this.ackNum = ackNum;
        this.proposedDate = proposedDate;
        this.uniqueShareholderIdentificationNumber = uniqueShareholderIdentificationNumber;
        this.ackDate = ackDate;
    }

    public String getRemitteeName() {
        return remitteeName;
    }

    public String getFlatDoorBlockNum() {
        return flatDoorBlockNum;
    }

    public String getBuildingVillagePremises() {
        return buildingVillagePremises;
    }

    public String getTownCityDistrict() {
        return townCityDistrict;
    }

    public String getAreaLocality() {
        return areaLocality;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getState() {
        return state;
    }

    public String getRoadStreet() {
        return roadStreet;
    }

    public String getCountry() {
        return country;
    }

    public String getAckNum() {
        return ackNum;
    }

    public String getProposedDate() {
        return proposedDate;
    }

    public String getUniqueShareholderIdentificationNumber() {
        return uniqueShareholderIdentificationNumber;
    }

    public String getAckDate() {
        return ackDate;
    }

    public String getDeductorPan() { return deductorPan; }

    public void setDeductorPan(String deductorPan) { this.deductorPan = deductorPan; }

    public Integer getInvoiceId() { return invoiceId; }

    public void setInvoiceId(Integer invoiceId) { this.invoiceId = invoiceId; }

    public Date getInvoicePostingDate() { return invoicePostingDate; }

    public void setInvoicePostingDate(Date invoicePostingDate) { this.invoicePostingDate = invoicePostingDate; }

    public void setUniqueShareholderIdentificationNumber(String uniqueShareholderIdentificationNumber) { this.uniqueShareholderIdentificationNumber = uniqueShareholderIdentificationNumber; }

    public List<Form15CBDetails> generateFromRecord(List<InvoiceShareholderNonResident> invoiceShareholderNonResidentList, String tenantId) {

        List<Form15CBDetails> form15CBDetails = new ArrayList<>();
        for (InvoiceShareholderNonResident invoiceShareholderNonResident : invoiceShareholderNonResidentList) {
            ResponseEntity<ApiStatus<ShareholderMasterDTO>> shareholderNonResident = onboardingClient
                    .getNonResidentialShareholder(invoiceShareholderNonResident.getDeductorPan(),
                            tenantId, invoiceShareholderNonResident.getShareholderId());
            ShareholderMasterDTO data = Objects.requireNonNull(shareholderNonResident.getBody()).getData();

            String remitteeName = data.getShareholderName();
            AddressDTO shareholderAddress = data.getAddress();
            String flatDoorBlockNum = shareholderAddress.getFlatDoorBlockNo();
            String buildingVillagePremises = shareholderAddress.getNameBuildingVillage();
            String roadStreet = shareholderAddress.getRoadStreetPostoffice();
            String country = shareholderAddress.getCountry();
            String townCityDistrict = shareholderAddress.getTownCityDistrict();
            String areaLocality = shareholderAddress.getAreaLocality();
            String zipCode = shareholderAddress.getPinCode();
            String state = shareholderAddress.getStateName();
            String deductorPan = invoiceShareholderNonResident.getDeductorPan();
            Integer invoiceId = invoiceShareholderNonResident.getId();
            Date invoicePostingDate = invoiceShareholderNonResident.getDateOfPosting();
            // IMPORTANT: Make sure that the date is in this format only: DD/MM/YYYY
            String proposedDate = null;
            try {
                //   proposedDate = DateUtil.convertDateFormat(invoiceShareholderNonResident.getProposedDateOfRemmitence());
                proposedDate = convertDateFormat(invoiceShareholderNonResident.getProposedDateOfRemmitence().toString());
            } catch (Exception e) {
                // Cant do further work with this object if we cannot parse date
                return null;
            }
            String uniqueShareholderIdentificationNumber = invoiceShareholderNonResident.getShareholderId().toString();
            form15CBDetails.add(new Form15CBDetails(remitteeName, flatDoorBlockNum, buildingVillagePremises, townCityDistrict,
                    areaLocality, zipCode, state, roadStreet, country, null, proposedDate,
                    uniqueShareholderIdentificationNumber, null, deductorPan, invoiceId, invoicePostingDate));
        }
        return form15CBDetails;
    }

    public void changeNullFieldsToEmpty() {
        for (Field f : this.getClass().getDeclaredFields()) {
            if (f.getType().equals(String.class)) {
                f.setAccessible(true);
                try {
                    String field = (String) f.get(this);
                    if (field == null) {
                        f.set(this, "");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String convertDateFormat(String inputDate) throws ParseException {
        FastDateFormat inputDateFormat = Date_Input_Format;
        FastDateFormat outputDateFormat = Date_Output_DB_Format;
        Date date = inputDateFormat.parse(inputDate);
        String dateAfterChange = outputDateFormat.format(date);
        return dateAfterChange;
    }
}
