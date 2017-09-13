package ua.adeptius.asterisk.json;

import java.util.HashMap;
import java.util.List;

public class JsonRule {

    private String name;
    private String type;
    private Integer startHour;
    private Integer endHour;
    private boolean[] days = null;
    HashMap<Integer, JsonChainElement> chain;
    private Integer greeting;
    private Integer message;
    private String melody;
    private String amoResponsibleId;

    public String getAmoResponsibleId() {
        return amoResponsibleId;
    }

    public void setAmoResponsibleId(String amoResponsibleId) {
        this.amoResponsibleId = amoResponsibleId;
    }

    public String getMelody() {
        return melody;
    }

    public void setMelody(String melody) {
        this.melody = melody;
    }

    public Integer getGreeting() {
        return greeting;
    }

    public void setGreeting(Integer greeting) {
        this.greeting = greeting;
    }

    public Integer getMessage() {
        return message;
    }

    public void setMessage(Integer message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStartHour() {
        return startHour;
    }

    public void setStartHour(Integer startHour) {
        this.startHour = startHour;
    }

    public Integer getEndHour() {
        return endHour;
    }

    public void setEndHour(Integer endHour) {
        this.endHour = endHour;
    }

    public boolean[] getDays() {
        return days;
    }

    public void setDays(boolean[] days) {
        this.days = days;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public HashMap<Integer, JsonChainElement> getChain() {
        return chain;
    }

    public void setChain(HashMap<Integer, JsonChainElement> chain) {
        this.chain = chain;
    }
}
