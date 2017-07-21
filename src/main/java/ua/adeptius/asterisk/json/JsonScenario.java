package ua.adeptius.asterisk.json;

import ua.adeptius.asterisk.telephony.DestinationType;
import ua.adeptius.asterisk.telephony.ForwardType;

import java.util.Arrays;
import java.util.List;

public class JsonScenario {

    public JsonScenario() {
    }

    private int id;
    private String name;
    private List<String> fromNumbers;
    private List<String> toNumbers;
    private ForwardType forwardType;
    private DestinationType destinationType;
    private int awaitingTime;
    private String melody;
    private int startHour;
    private int endHour;
    private boolean[] days;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public int getEndHour() {
        return endHour;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    public List<String> getFromNumbers() {
        return fromNumbers;
    }

    public void setFromNumbers(List<String> fromNumbers) {
        this.fromNumbers = fromNumbers;
    }

    public List<String> getToNumbers() {
        return toNumbers;
    }

    public void setToNumbers(List<String> toNumbers) {
        this.toNumbers = toNumbers;
    }

    public boolean[] getDays() {
        return days;
    }

    public void setDays(boolean[] days) {
        this.days = days;
    }

    @Override
    public String toString() {
        return "JsonScenario{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", fromNumbers=" + fromNumbers +
                ", toNumbers=" + toNumbers +
                ", forwardType=" + forwardType +
                ", destinationType=" + destinationType +
                ", awaitingTime=" + awaitingTime +
                ", melody='" + melody + '\'' +
                ", startHour=" + startHour +
                ", endHour=" + endHour +
                ", days=" + Arrays.toString(days) +
                '}';
    }
}
