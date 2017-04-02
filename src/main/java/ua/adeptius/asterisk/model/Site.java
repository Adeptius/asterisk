package ua.adeptius.asterisk.model;


import org.codehaus.jackson.annotate.JsonIgnore;
import ua.adeptius.asterisk.dao.ConfigDAO;
import ua.adeptius.asterisk.telephony.Rule;
import ua.adeptius.asterisk.utils.logging.LogCategory;
import ua.adeptius.asterisk.utils.logging.MyLogger;

import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;

public class Site {

    public Site(String name, List<Phone> phones, String standartNumber, String googleAnalyticsTrackingId, String eMail,
                List<String> blackIps, String password, int timeToBlock) {
        this.name = name;
        this.phones = phones;
        this.standartNumber = standartNumber;
        this.googleAnalyticsTrackingId = googleAnalyticsTrackingId;
        this.eMail = eMail;
        this.blackIps = blackIps;
        this.password = password;
        this.timeToBlock = timeToBlock;
        loadRules();
    }

    private List<String> blackIps;
    private String name;
    private List<Phone> phones;
    private String standartNumber;
    private String googleAnalyticsTrackingId;
    private String eMail;
    private String password;
    private int timeToBlock;
    private List<Rule> rules = new ArrayList<>();

    @JsonIgnore
    private long lastEmailTime;

    public void loadRules(){
        try {
            rules = ConfigDAO.readFromFile(name);
        } catch (NoSuchFileException e) {
            MyLogger.log(LogCategory.DB_ERROR_CONNECTING,
                    "Отсутствует конфиг с правилами переадресации для сайта " + name);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void saveRules() throws Exception{
        ConfigDAO.writeToFile(name, rules);
    }

    public void setTimeToBlock(int timeToBlock) {
        this.timeToBlock = timeToBlock;
    }

    public int getTimeToBlock() {
        return timeToBlock;
    }

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
                ", password='" + password + '\'' +
                ", timeToBlock=" + timeToBlock +
                ", rules=" + rules +
                ", lastEmailTime=" + lastEmailTime +
                '}';
    }



}
