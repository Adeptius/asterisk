package ua.adeptius.asterisk.newmodel;


import org.codehaus.jackson.annotate.JsonIgnore;
import ua.adeptius.asterisk.controllers.PhonesController;
import ua.adeptius.asterisk.dao.PhonesDao;
import ua.adeptius.asterisk.model.Phone;
import ua.adeptius.asterisk.monitor.CallProcessor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@SuppressWarnings("Duplicates")
@Entity
@Table(name = "tracking", schema = "calltrackdb")
public class Tracking {

    @Id
    @Column(name = "login")
    private String login;

    @Column(name = "siteNumbersCount")
    private Integer siteNumbersCount;

    @Column(name = "standartNumber")
    private String standartNumber;

    @Column(name = "timeToBlock")
    private Integer timeToBlock;

    @Column(name = "blackList")
    private String blackList;

    @JsonIgnore
    @OneToOne
    @PrimaryKeyJoinColumn
    private User user;

    @JsonIgnore
    @Transient
    private long lastEmailTime;

    @Transient
    private List<Phone> phones = new ArrayList<>();

    public void updateNumbers() throws Exception {
        ArrayList<String> outerPhones = PhonesDao.getCustomersNumbers("trac_" + login,false);
        for (String outerPhone : outerPhones) {
            phones.add(new Phone(outerPhone));
        }
        if (siteNumbersCount != phones.size()){
            List<String> current = phones.stream().map(Phone::getNumber).collect(Collectors.toList());
            PhonesController.increaseOrDecrease(siteNumbersCount, current, "trac_" + login,false);
            phones.clear();
            for (String s : current) {
                phones.add(new Phone(s));
            }
        }
        CallProcessor.updatePhonesHashMap();
    }

    @JsonIgnore
    public List<String> getAvailableNumbers() {
        List<String> currentPhones = phones.stream().map(Phone::getNumber).collect(Collectors.toList());
        List<String> currentNumbersInRules = user.getRules().stream().flatMap(rule -> rule.getFrom().stream()).collect(Collectors.toList());
        List<String> list = currentPhones.stream().filter(s -> !currentNumbersInRules.contains(s)).collect(Collectors.toList());
        return list;
    }

    @JsonIgnore
    public List<String> getBlackListAsList(){
        if (blackList == null || blackList.isEmpty()){
            return new ArrayList<>();
        }
        return Arrays.asList(blackList.split(","));
    }


    public long getLastEmailTime() {
        return lastEmailTime;
    }

    public void setLastEmailTime(long lastEmailTime) {
        this.lastEmailTime = lastEmailTime;
    }

    public List<Phone> getPhones() {
        return phones;
    }

    public void setPhones(List<Phone> phones) {
        this.phones = phones;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        if (user != null) {
            this.login = user.getLogin();
        }
        this.user = user;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public Integer getSiteNumbersCount() {
        return siteNumbersCount;
    }

    public void setSiteNumbersCount(Integer siteNumbers) {
        this.siteNumbersCount = siteNumbers;
    }

    public String getStandartNumber() {
        return standartNumber;
    }

    public void setStandartNumber(String standartNumber) {
        this.standartNumber = standartNumber;
    }

    public Integer getTimeToBlock() {
        return timeToBlock;
    }

    public void setTimeToBlock(Integer timeToBlock) {
        this.timeToBlock = timeToBlock;
    }

    public String getBlackList() {
        return blackList;
    }

    public void setBlackList(String blackList) {
        this.blackList = blackList;
    }


    @Override
    public String toString() {
        return "Tracking{" +
                "login='" + login + '\'' +
                ", siteNumbersCount=" + siteNumbersCount +
                ", standartNumber='" + standartNumber + '\'' +
                ", timeToBlock=" + timeToBlock +
                ", blackList='" + blackList + '\'' +
                ", user=" + (user==null? "null" : user.getLogin()) +
                ", phones=" + phones +
                '}';
    }


}
