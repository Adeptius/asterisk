package ua.adeptius.asterisk.newmodel;


import ua.adeptius.asterisk.controllers.PhonesController;
import ua.adeptius.asterisk.dao.PhonesDao;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "telephony", schema = "calltrackdb")
public class Telephony {

    @Id
    @Column(name = "login")
    private String login;

    @Column(name = "innerCount")
    private Integer innerCount;

    @Column(name = "outerCount")
    private Integer outerCount;

    @OneToOne
    @PrimaryKeyJoinColumn
    private User user;

    @Transient
    private ArrayList<String> innerPhonesList = new ArrayList<>();

    @Transient
    private ArrayList<String> outerPhonesList = new ArrayList<>();

    public void updateNumbers() throws Exception{
        outerPhonesList = PhonesDao.getCustomersNumbers(login,false);
        innerPhonesList = PhonesDao.getCustomersNumbers(login,true);
        PhonesController.increaseOrDecrease(innerCount, innerPhonesList, login, true);
        PhonesController.increaseOrDecrease(outerCount, outerPhonesList, login, false);
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Telephony telephony = (Telephony) o;

        if (login != null ? !login.equals(telephony.login) : telephony.login != null) return false;
        if (innerCount != null ? !innerCount.equals(telephony.innerCount) : telephony.innerCount != null) return false;
        if (outerCount != null ? !outerCount.equals(telephony.outerCount) : telephony.outerCount != null) return false;
        return user != null ? user.equals(telephony.user) : telephony.user == null;
    }

    @Override
    public int hashCode() {
        int result = login != null ? login.hashCode() : 0;
        result = 31 * result + (innerCount != null ? innerCount.hashCode() : 0);
        result = 31 * result + (outerCount != null ? outerCount.hashCode() : 0);
        result = 31 * result + (user != null ? user.hashCode() : 0);
        return result;
    }
}
