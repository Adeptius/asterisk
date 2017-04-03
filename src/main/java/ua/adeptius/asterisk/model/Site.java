package ua.adeptius.asterisk.model;


import org.codehaus.jackson.annotate.JsonIgnore;
import ua.adeptius.asterisk.dao.ConfigDAO;
import ua.adeptius.asterisk.telephony.Rule;
import ua.adeptius.asterisk.utils.logging.LogCategory;
import ua.adeptius.asterisk.utils.logging.MyLogger;

import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;

public class Site extends Customer {

    public final CustomerType type = CustomerType.TRACKING;
    public Site(String name, List<Phone> phones, String standartNumber, String googleAnalyticsTrackingId, String eMail,
                List<String> blackIps, String password, int timeToBlock) {
        super(name, eMail, googleAnalyticsTrackingId, password);
        this.phones = phones;
        this.standartNumber = standartNumber;
        this.blackIps = blackIps;
        this.timeToBlock = timeToBlock;
    }

    private List<String> blackIps;
    private List<Phone> phones;
    private String standartNumber;
    private int timeToBlock;


    @JsonIgnore
    private long lastEmailTime;

    public void setTimeToBlock(int timeToBlock) {
        this.timeToBlock = timeToBlock;
    }

    public int getTimeToBlock() {
        return timeToBlock;
    }

    public List<Phone> getPhones() {
        return phones;
    }

    public String getStandartNumber() {
        return standartNumber;
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
                ", password='" + password + '\'' +
                ", timeToBlock=" + timeToBlock +
                ", rules=" + rules +
                ", lastEmailTime=" + lastEmailTime +
                '}';
    }
}
