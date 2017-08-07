package ua.adeptius.asterisk.model;


import org.codehaus.jackson.annotate.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.controllers.PhonesController;
import ua.adeptius.asterisk.dao.PhonesDao;
import ua.adeptius.asterisk.monitor.CallProcessor;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;


//@SuppressWarnings("Duplicates")
//@Entity
//@Table(name = "tracking", schema = "calltrackdb")
public class Tracking {
//
//    private static Logger LOGGER =  LoggerFactory.getLogger(Tracking.class.getSimpleName());
//
//    @JsonIgnore
//    @Id
//    @Column(name = "login")
//    private String login;
//
//    @Column(name = "siteNumbersCount")
//    private Integer siteNumbersCount;
//
//    @Column(name = "standartNumber")
//    private String standartNumber;
//
//    @Column(name = "timeToBlock")
//    private Integer timeToBlock;
//
//    @JsonIgnore
//    @Column(name = "blackIps")
//    private String blackIps = "";
//
//    @JsonIgnore
//    @OneToOne
//    @PrimaryKeyJoinColumn
//    private User user;
//
//    @JsonIgnore
//    @Transient
//    private long lastEmailTime;
//
//    @Transient
//    private List<OldPhone> oldPhones = new ArrayList<>();
//
//    public void updateNumbers() throws Exception {
//        LOGGER.debug("{}: обновление списка внешних номеров трекинга", login);
//        ArrayList<String> outerPhones = PhonesDao.getCustomersOuterNumbers("trac_" + login);
//        for (String outerPhone : outerPhones) {
//            if (!oldPhones.stream().map(OldPhone::getNumber).anyMatch(s -> s.equals(outerPhone))){
//                oldPhones.add(new OldPhone(outerPhone));
//            }
//        }
//        if (siteNumbersCount != oldPhones.size()){
//            List<String> current = oldPhones.stream().map(OldPhone::getNumber).collect(Collectors.toList());
//            PhonesController.increaseOrDecreaseOuterList(siteNumbersCount, current, "trac_" + login);
//            oldPhones.clear();
//            for (String s : current) {
//                oldPhones.add(new OldPhone(s));
//            }
//        }
//        CallProcessor.updatePhonesHashMap();
//    }
//
//    @JsonIgnore
//    public List<String> getAvailableNumbers() {
//        List<String> currentPhones = oldPhones.stream().map(OldPhone::getNumber).collect(Collectors.toList());
////        List<String> currentNumbersInRules = user.getOldRules().stream().flatMap(rule -> rule.getFrom().stream()).collect(Collectors.toList());
////        List<String> list = currentPhones.stream().filter(s -> !currentNumbersInRules.contains(s)).collect(Collectors.toList());
//        return currentPhones;
//    }
//
//    @Transient
//    LinkedList<String> blackLinkedList;
//

//
//
//    public long getLastEmailTime() {
//        return lastEmailTime;
//    }
//
//    public void setLastEmailTime(long lastEmailTime) {
//        this.lastEmailTime = lastEmailTime;
//    }
//
//    public List<OldPhone> getOldPhones() {
//        return oldPhones;
//    }
//
//    public void setOldPhones(List<OldPhone> oldPhones) {
//        this.oldPhones = oldPhones;
//    }
//
//    public User getUser() {
//        return user;
//    }
//
//    public void setUser(User user) {
//        if (user != null) {
//            this.login = user.getLogin();
//        }
//        this.user = user;
//    }
//
//    public String getLogin() {
//        return login;
//    }
//
//    public void setLogin(String login) {
//        this.login = login;
//    }
//
//    public Integer getSiteNumbersCount() {
//        return siteNumbersCount;
//    }
//
//    public void setSiteNumbersCount(Integer siteNumbers) {
//        this.siteNumbersCount = siteNumbers;
//    }
//
//    public String getStandardNumber() {
//        return standartNumber;
//    }
//
//    public void setStandartNumber(String standartNumber) {
//        this.standartNumber = standartNumber;
//    }
//
//    public Integer getTimeToBlock() {
//        return timeToBlock;
//    }
//
//    public void setTimeToBlock(Integer timeToBlock) {
//        this.timeToBlock = timeToBlock;
//    }
//
//    public String getBlackIps() {
//        return blackIps;
//    }
//
//    public void setBlackIps(String blackIps) {
//        this.blackIps = blackIps;
//    }
//
//    @Override
//    public String toString() {
//        return "Tracking{" +
//                "login='" + login + '\'' +
//                ", siteNumbersCount=" + siteNumbersCount +
//                ", standartNumber='" + standartNumber + '\'' +
//                ", timeToBlock=" + timeToBlock +
//                ", user=" + (user==null? "null" : user.getLogin()) +
//                ", phones=" + oldPhones +
//                '}';
//    }
//

}
