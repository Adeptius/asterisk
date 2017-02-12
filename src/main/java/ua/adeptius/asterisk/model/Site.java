package ua.adeptius.asterisk.model;


import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.List;

public class Site {

    public Site(String name, List<Phone> phones, String standartNumber, String googleAnalyticsTrackingId, String eMail, List<String> blackIps, String password) {
        this.name = name;
        this.phones = phones;
        this.standartNumber = standartNumber;
        this.googleAnalyticsTrackingId = googleAnalyticsTrackingId;
        this.eMail = eMail;
        this.blackIps = blackIps;
        this.password = password;
    }

    private List<String> blackIps;
    private String name;
    private List<Phone> phones;
    private String standartNumber;
    private String googleAnalyticsTrackingId;
    private String eMail;
    private String password;
    @JsonIgnore
    private long lastEmailTime;

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public List<Phone> getPhones() {
        return phones;
    }

    public String getStandartNumber() {
        return standartNumber;
    }

    public String getGoogleAnalyticsTrackingId() {
        return googleAnalyticsTrackingId;
    }

    public String getMail() {
        return eMail;
    }

    public long getLastEmailTime() {
        return lastEmailTime;
    }

    public void setLastEmailTime(long lastEmailTime) {
        this.lastEmailTime = lastEmailTime;
    }

    public List<String> getBlackIps() {
        return blackIps;
    }

    @Override
    public String toString() {
        return "Site{" +
                "blackIps=" + blackIps +
                ", name='" + name + '\'' +
                ", phones=" + phones +
                ", standartNumber='" + standartNumber + '\'' +
                ", googleAnalyticsTrackingId='" + googleAnalyticsTrackingId + '\'' +
                ", eMail='" + eMail + '\'' +
                ", lastEmailTime=" + lastEmailTime +
                '}';
    }
}
