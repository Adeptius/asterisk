package ua.adeptius.asterisk.json;


import ua.adeptius.asterisk.model.telephony.InnerPhone;
import ua.adeptius.asterisk.model.telephony.OuterPhone;

import java.util.Set;

public class JsonInnerAndOuterPhones {

    public JsonInnerAndOuterPhones(Set<InnerPhone> innerPhones, Set<OuterPhone> outerPhones) {
        this.innerPhones = innerPhones;
        this.outerPhones = outerPhones;
    }

    private Set<InnerPhone> innerPhones;
    private Set<OuterPhone> outerPhones;

    public Set<InnerPhone> getInnerPhones() {
        return innerPhones;
    }

    public void setInnerPhones(Set<InnerPhone> innerPhones) {
        this.innerPhones = innerPhones;
    }

    public Set<OuterPhone> getOuterPhones() {
        return outerPhones;
    }

    public void setOuterPhones(Set<OuterPhone> outerPhones) {
        this.outerPhones = outerPhones;
    }

    @Override
    public String toString() {
        return "JsonInnerAndOuterPhones{" +
                "innerPhones=" + innerPhones +
                ", outerPhones=" + outerPhones +
                '}';
    }
}
