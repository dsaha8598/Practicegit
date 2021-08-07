package com.ey.in.tds.returns.bot.domain;

import java.util.List;

public class AckNumRequestMessage {

    private String userName;
    private String password;
    private String captcha;
    // NOTE: If assesment year is 2020-2021, then value should be 2020
    private String assessmentYear;
    private List<Form15CBDetails> form15CBDetails;

    public AckNumRequestMessage(String userName, String password,
                                String captcha, List<Form15CBDetails> form15CBDetails,
                                String assessmentYear) {
        this.userName = userName;
        this.password = password;
        this.captcha = captcha;
        this.form15CBDetails = form15CBDetails;
        this.assessmentYear = assessmentYear;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getCaptcha() {
        return captcha;
    }

    public List<Form15CBDetails> getForm15CBDetails() {
        return form15CBDetails;
    }

    public String getAssessmentYear() {
        return assessmentYear;
    }

}
