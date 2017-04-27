package ua.adeptius.asterisk.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import ua.adeptius.asterisk.dao.RulesConfigDAO;
import ua.adeptius.asterisk.model.Telephony;
import ua.adeptius.asterisk.model.Tracking;
import ua.adeptius.asterisk.telephony.Rule;
import ua.adeptius.asterisk.utils.logging.LogCategory;
import ua.adeptius.asterisk.utils.logging.MyLogger;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "users", schema = "calltrackdb")
public class User {

    @Id
    @Column(name = "login")
    private String login;

    @JsonIgnore
    @Column(name = "password")
    private String password;

    @Column(name = "email")
    private String email;

    @Column(name = "trackingId")
    private String trackingId;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @PrimaryKeyJoinColumn
    private Tracking tracking;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @PrimaryKeyJoinColumn
    private Telephony telephony;

    @Transient
    private List<Rule> rules = new ArrayList<>();

    public void loadRules(){
        try {
            rules = RulesConfigDAO.readFromFile(login);
        } catch (NoSuchFileException ignored) {
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void removeRulesFile(){
        try{
            RulesConfigDAO.removeFile(login);
            MyLogger.log(LogCategory.DB_OPERATIONS, "Файл конфига " + login + ".conf удалён.");
        }catch (Exception ignored){}
    }

    public void saveRules() throws Exception{
        if (rules.size()==0){
            RulesConfigDAO.removeFile(login);
        }else {
            RulesConfigDAO.writeToFile(login, rules);
        }
    }

    @JsonIgnore
    public List<String> getAvailableNumbers(){
        List<String> numbers = new ArrayList<>();
        if (telephony != null){
            numbers.addAll(telephony.getAvailableNumbers());
        }
        if (tracking != null){
            numbers.addAll(tracking.getAvailableNumbers());
        }
        return numbers;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public Telephony getTelephony() {
        return telephony;
    }

    public void setTelephony(Telephony telephony) {
        this.telephony = telephony;
    }

    public Tracking getTracking() {
        return tracking;
    }

    public void setTracking(Tracking tracking) {
        this.tracking = tracking;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTrackingId() {
        if (trackingId != null){
            return trackingId;
        }else {
            return "";
        }
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }



    @Override
    public String toString() {
        return "User{" +
                "login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", trackingId='" + trackingId + '\'' +
                ", tracking=" + tracking +
                ", telephony=" + telephony +
                '}';
    }
}
