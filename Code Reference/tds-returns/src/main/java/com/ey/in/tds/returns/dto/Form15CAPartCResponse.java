package com.ey.in.tds.returns.dto;

public class Form15CAPartCResponse {

    private String jobId;
    private String message;
    private String zippedXMLS;
    private boolean status;
    private String captchaImage;

    public Form15CAPartCResponse(String jobId) {
        this.jobId = jobId;
    }

    public String getJobId() {
        return jobId;
    }

    public String getMessage() {
        return message;
    }

    public String getZippedXMLS() {
        return zippedXMLS;
    }

    public void setZippedXMLS(String zippedXMLS) {
        this.zippedXMLS = zippedXMLS;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }


    public String getCaptchaImage() {
        return captchaImage;
    }

    public void setCaptchaImage(String captchaImage) {
        this.captchaImage = captchaImage;
    }

}
