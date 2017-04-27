package ua.adeptius.asterisk.model;


import org.codehaus.jackson.annotate.JsonIgnore;
import ua.adeptius.asterisk.controllers.PhonesController;
import ua.adeptius.asterisk.dao.PhonesDao;
import ua.adeptius.asterisk.monitor.CallProcessor;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;


@SuppressWarnings("Duplicates")
@Entity
@Table(name = "tracking", schema = "calltrackdb")
public class Tracking {

    @JsonIgnore
    @Id
    @Column(name = "login")
    private String login;

    @Column(name = "siteNumbersCount")
    private Integer siteNumbersCount;

    @Column(name = "standartNumber")
    private String standartNumber;

    @Column(name = "timeToBlock")
    private Integer timeToBlock;

    @JsonIgnore
    @Column(name = "blackIps")
    private String blackIps = "";

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

    @Transient
    LinkedList<String> blackLinkedList;

    @JsonIgnore
    public LinkedList<String> getBlackList(){
        if (blackLinkedList == null){
            blackLinkedList = new LinkedList<>();
            if (blackIps==null){
                blackIps = "";
            }
            String[] spl = blackIps.split(" ");
            for (int i = 1; i < spl.length; i++) {
                blackLinkedList.add(0,spl[i]);
            }
        }
        return blackLinkedList;
    }

    public void addIpToBlackList(String ip){
        getBlackList().add(0, ip);
        blackIps = (" " + ip) + blackIps;
        checkBlackListSize();
    }

    public void removeIpFromBlackList(String ip){
        List<String> currentList = getBlackList();
        currentList.removeIf(s -> s.equals(ip));
        StringBuilder sb = new StringBuilder(200);
        for (String s : currentList) {
            sb.append(" ").append(s);
        }
        blackIps = sb.toString();
    }

    private void checkBlackListSize(){
        if (blackLinkedList.size() >99){
            for (int i = 0; i < 10; i++) {
                blackLinkedList.removeLast();
            }
        }
        StringBuilder sb = new StringBuilder(200);
        for (String s : blackLinkedList) {
            sb.append(" ").append(s);
        }
        blackIps = sb.toString();
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

    public String getBlackIps() {
        return blackIps;
    }

    public void setBlackIps(String blackIps) {
        this.blackIps = blackIps;
    }

    @Override
    public String toString() {
        return "Tracking{" +
                "login='" + login + '\'' +
                ", siteNumbersCount=" + siteNumbersCount +
                ", standartNumber='" + standartNumber + '\'' +
                ", timeToBlock=" + timeToBlock +
                ", user=" + (user==null? "null" : user.getLogin()) +
                ", phones=" + phones +
                '}';
    }


}
