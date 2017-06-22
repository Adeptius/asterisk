package ua.adeptius.asterisk.model;


import org.codehaus.jackson.annotate.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.controllers.PhonesController;
import ua.adeptius.asterisk.dao.PhonesDao;
import ua.adeptius.asterisk.monitor.CallProcessor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "telephony", schema = "calltrackdb")
public class Telephony {

    private static Logger LOGGER =  LoggerFactory.getLogger(Telephony.class.getSimpleName());

    @JsonIgnore
    @Id
    @Column(name = "login")
    private String login;

    @Column(name = "innerCount")
    private Integer innerCount;

    @Column(name = "outerCount")
    private Integer outerCount;

    @JsonIgnore
    @OneToOne
    @PrimaryKeyJoinColumn
    private User user;

    @Transient
    private ArrayList<String> innerPhonesList = new ArrayList<>();

    @Transient
    private ArrayList<String> outerPhonesList = new ArrayList<>();

    public void updateNumbers() throws Exception{
        LOGGER.debug("{}: обновление списков номеров телефонии", login);
        outerPhonesList = PhonesDao.getCustomersNumbers("tele_" + login,false);
        innerPhonesList = PhonesDao.getCustomersNumbers(login,true);
        PhonesController.increaseOrDecrease(outerCount, outerPhonesList, "tele_" + login, false);
        PhonesController.increaseOrDecrease(innerCount, innerPhonesList, login, true);
        CallProcessor.updatePhonesHashMap();
    }

    @JsonIgnore
    public List<String> getAvailableNumbers() {
        List<String> currentPhones = outerPhonesList;
        List<String> currentNumbersInRules = user.getRules().stream().flatMap(rule -> rule.getFrom().stream()).collect(Collectors.toList());
        List<String> list = currentPhones.stream().filter(s -> !currentNumbersInRules.contains(s)).collect(Collectors.toList());
        return list;
    }


    public ArrayList<String> getInnerPhonesList() {
        return innerPhonesList;
    }

    public void setInnerPhonesList(ArrayList<String> innerPhonesList) {
        this.innerPhonesList = innerPhonesList;
    }

    public ArrayList<String> getOuterPhonesList() {
        return outerPhonesList;
    }

    public void setOuterPhonesList(ArrayList<String> outerPhonesList) {
        this.outerPhonesList = outerPhonesList;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public Integer getInnerCount() {
        return innerCount;
    }

    public void setInnerCount(Integer innerCount) {
        this.innerCount = innerCount;
    }

    public Integer getOuterCount() {
        return outerCount;
    }

    public void setOuterCount(Integer outerCount) {
        this.outerCount = outerCount;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        if (user != null){
            this.login = user.getLogin();
        }
        this.user = user;
    }

    @Override
    public String toString() {
        return "Telephony{" +
                "login='" + login + '\'' +
                ", innerCount=" + innerCount +
                ", outerCount=" + outerCount +
                ", user=" + user.getLogin() +
                ", innerPhonesList=" + innerPhonesList +
                ", outerPhonesList=" + outerPhonesList +
                '}';
    }


}
