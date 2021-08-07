package com.ey.in.tds.returns.bot.domain;

import java.util.List;

public class AckNumResponseMessage {

    private List<Form15CBDetails> formsWithAckNumFound;
    private List<Form15CBDetails> formsWithAckNumNotFound;

    public AckNumResponseMessage(List<Form15CBDetails> formsWithAckNumFound,
                                 List<Form15CBDetails> formsWithAckNumNotFound) {
        this.formsWithAckNumFound = formsWithAckNumFound;
        this.formsWithAckNumNotFound = formsWithAckNumNotFound;
    }

    public List<Form15CBDetails> getFormsWithAckNumFound() {
        return formsWithAckNumFound;
    }

    public List<Form15CBDetails> getFormsWithAckNumNotFound() {
        return formsWithAckNumNotFound;
    }

}
