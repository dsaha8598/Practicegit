package com.ey.in.tds.returns.dto;

import java.io.Serializable;
import java.util.Date;

public class AuthorisedPersonDTO implements Serializable {

    private String authorisedPersonName;

    private String fatherOrHusbandName;

    private String designation;

    private Date dateOfFiling;

    private String place;

    public String getAuthorisedPersonName() {
        return authorisedPersonName;
    }

    public void setAuthorisedPersonName(String authorisedPersonName) {
        this.authorisedPersonName = authorisedPersonName;
    }

    public String getFatherOrHusbandName() {
        return fatherOrHusbandName;
    }

    public void setFatherOrHusbandName(String fatherOrHusbandName) {
        this.fatherOrHusbandName = fatherOrHusbandName;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public Date getDateOfFiling() {
        return dateOfFiling;
    }

    public void setDateOfFiling(Date dateOfFiling) {
        this.dateOfFiling = dateOfFiling;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }
}
