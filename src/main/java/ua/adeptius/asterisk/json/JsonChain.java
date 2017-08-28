package ua.adeptius.asterisk.json;

import ua.adeptius.asterisk.telephony.DestinationType;
import ua.adeptius.asterisk.telephony.ForwardType;

public class JsonChain {

    private Integer position;
    private String toNumbers;
    private ForwardType forwardType;
    private DestinationType destinationType;
    private int awaitingTime;
    private String melody;

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getToNumbers() {
        return toNumbers;
    }

    public void setToNumbers(String toNumbers) {
        this.toNumbers = toNumbers;
    }

    public ForwardType getForwardType() {
        return forwardType;
    }

    public void setForwardType(ForwardType forwardType) {
        this.forwardType = forwardType;
    }

    public DestinationType getDestinationType() {
        return destinationType;
    }

    public void setDestinationType(DestinationType destinationType) {
        this.destinationType = destinationType;
    }

    public int getAwaitingTime() {
        return awaitingTime;
    }

    public void setAwaitingTime(int awaitingTime) {
        this.awaitingTime = awaitingTime;
    }

    public String getMelody() {
        return melody;
    }

    public void setMelody(String melody) {
        this.melody = melody;
    }
}
