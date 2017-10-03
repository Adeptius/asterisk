package ua.adeptius.asterisk.json;


import java.util.ArrayList;

public class JsonSite {

    private String name;
    private String standardNumber;
    private String googleTrackingId;
    private Integer timeToBlock;
    private ArrayList<String> blackList;
    private ArrayList<String> connectedPhones;

    public String getStandardNumber() {
        return standardNumber;
    }

    public void setStandardNumber(String standardNumber) {
        this.standardNumber = standardNumber;
    }

    public ArrayList<String> getConnectedPhones() {
        return connectedPhones;
    }

    public void setConnectedPhones(ArrayList<String> connectedPhones) {
        this.connectedPhones = connectedPhones;
    }

    public ArrayList<String> getBlackList() {
        return blackList;
    }

    public void setBlackList(ArrayList<String> blackList) {
        this.blackList = blackList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTimeToBlock() {
        return timeToBlock;
    }

    public void setTimeToBlock(Integer timeToBlock) {
        this.timeToBlock = timeToBlock;
    }

    public String getGoogleTrackingId() {
        return googleTrackingId;
    }

    public void setGoogleTrackingId(String googleTrackingId) {
        this.googleTrackingId = googleTrackingId;
    }

    @Override
    public String toString() {
        return "JsonSite{" +
                "name='" + name + '\'' +
                ", standardNumber='" + standardNumber + '\'' +
                ", timeToBlock=" + timeToBlock +
                ", blackList size=" + (blackList == null? "null" : blackList.size()) +
                ", connectedPhones=" + connectedPhones +
                '}';
    }
}
