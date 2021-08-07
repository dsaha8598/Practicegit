package com.ey.in.tds.returns.dividend.validator;


public class Errors {

    public enum TYPE{
        ERROR("Error"),
        WARNING("Warning"),
        FATAL_ERROR("Fatal Error");

        private String errorType;

        TYPE(String type){
            this.errorType = type;
        }

        public String getErrorType() {
            return errorType;
        }

        public void setErrorType(String errorType) {
            this.errorType = errorType;
        }
    }
    private String id;
    private String message;
    private TYPE type;

    public String getCompleteMessage() {
        return completeMessage;
    }

    public void setCompleteMessage(String completeMessage) {
        this.completeMessage = completeMessage;
    }

    private String completeMessage;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }
}

