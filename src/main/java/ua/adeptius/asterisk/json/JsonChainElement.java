package ua.adeptius.asterisk.json;



import java.util.List;

public class JsonChainElement {

    public JsonChainElement() {
    }

    private List<String> toList;
    private String forwardType;
    private String destinationType;
    private Integer awaitingTime;
    private Integer position;

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getForwardType() {
        return forwardType;
    }

    public void setForwardType(String forwardType) {
        this.forwardType = forwardType;
    }

    public String getDestinationType() {
        return destinationType;
    }

    public void setDestinationType(String destinationType) {
        this.destinationType = destinationType;
    }

    public Integer getAwaitingTime() {
        return awaitingTime;
    }

    public void setAwaitingTime(Integer awaitingTime) {
        this.awaitingTime = awaitingTime;
    }

    public List<String> getToList() {
        return toList;
    }

    public void setToList(List<String> toList) {
        this.toList = toList;
    }
}
