package com.ey.in.tds.returns.bot.pdf;

import com.ey.in.tds.returns.bot.domain.Form15CBDetails;
import org.springframework.stereotype.Component;

@Component
public class Form15CBMatcher {

    public boolean isEquals(Form15CBDetails obj1, Form15CBDetails obj2) {

        Boolean flag = obj1.getAreaLocality().trim().equalsIgnoreCase(obj2.getAreaLocality().trim())
                && obj1.getCountry().trim().equalsIgnoreCase(obj2.getCountry().trim())
                && obj1.getFlatDoorBlockNum().trim().equalsIgnoreCase(obj2.getFlatDoorBlockNum().trim())
                && obj1.getRemitteeName().trim().equalsIgnoreCase(obj2.getRemitteeName().trim())
                && obj1.getTownCityDistrict().trim().equalsIgnoreCase(obj2.getTownCityDistrict().trim())
                && obj1.getProposedDate().trim().equalsIgnoreCase(obj2.getProposedDate().trim())
                && (obj1.getBuildingVillagePremises() == null || obj1.getBuildingVillagePremises().trim().isEmpty() || obj1.getBuildingVillagePremises().trim().equalsIgnoreCase(obj2.getBuildingVillagePremises().trim()))
                && (obj1.getRoadStreet() == null || obj1.getRoadStreet().trim().isEmpty() || obj1.getRoadStreet().trim().equalsIgnoreCase(obj2.getRoadStreet().trim()));

        return flag;

    }
}
