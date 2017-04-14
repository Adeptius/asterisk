package ua.adeptius.asterisk.model;


import org.codehaus.jackson.annotate.JsonIgnore;
import ua.adeptius.asterisk.controllers.PhonesController;
import ua.adeptius.asterisk.dao.PhonesDao;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Site extends Customer {

    public final CustomerType type = CustomerType.TRACKING;
    public Site(String name,String standartNumber, String googleAnalyticsTrackingId, String eMail,
                List<String> blackIps, String password, int timeToBlock, int outerNumbersCount) throws Exception {
        super(name, eMail, googleAnalyticsTrackingId, password);
        this.standartNumber = standartNumber;
        this.blackIps = blackIps;
        this.timeToBlock = timeToBlock;
        this.outerNumbersCount = outerNumbersCount;
        updateNumbers();
    }

    @Override
    public void updateNumbers() throws Exception {
        ArrayList<String> outerPhones = PhonesDao.getCustomersNumbers(name,false);
        for (String outerPhone : outerPhones) {
            phones.add(new Phone(outerPhone));
        }
        if (outerNumbersCount != phones.size()){
            List<String> current = phones.stream().map(Phone::getNumber).collect(Collectors.toList());
            PhonesController.increaseOrDecrease(outerNumbersCount, current,name,false);
            phones.clear();
            for (String s : current) {
                phones.add(new Phone(s));
            }
        }
    }

    private List<String> blackIps;
    private List<Phone> phones = new ArrayList<>();
    private String standartNumber;
    private int timeToBlock;
    private int outerNumbersCount;

    public int getOuterNumbersCount() {
        return outerNumbersCount;
    }

    public void setOuterNumbersCount(int outerNumbersCount) {
        this.outerNumbersCount = outerNumbersCount;
    }

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
    public List<String> getAvailableNumbers() {
        List<String> currentPhones = phones.stream().map(Phone::getNumber).collect(Collectors.toList());
        List<String> currentNumbersInRules = rules.stream().flatMap(rule -> rule.getFrom().stream()).collect(Collectors.toList());
        List<String> list = currentPhones.stream().filter(s -> !currentNumbersInRules.contains(s)).collect(Collectors.toList());
        return list;
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
