package ua.adeptius.asterisk.newmodel;

import ua.adeptius.asterisk.dao.RulesConfigDAO;
import ua.adeptius.asterisk.telephony.Rule;
import ua.adeptius.asterisk.utils.logging.LogCategory;
import ua.adeptius.asterisk.utils.logging.MyLogger;

import javax.persistence.*;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "users", schema = "calltrackdb")
public class User {

    @Id
    @Column(name = "login")
    private String login;

    @Column(name = "password")
    private String password;

    @Column(name = "email")
    private String email;

    @Column(name = "trackingId")
    private String trackingId;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @PrimaryKeyJoinColumn
    private Site site;

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
            MyLogger.log(LogCategory.ELSE, "Файл конфига " + login + ".conf удалён.");
        }catch (Exception ignored){}
    }

    public void saveRules() throws Exception{
        if (rules.size()==0){
            RulesConfigDAO.removeFile(login);
        }else {
            RulesConfigDAO.writeToFile(login, rules);
        }
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

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
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
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User that = (User) o;

        if (login != null ? !login.equals(that.login) : that.login != null) return false;
        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        if (email != null ? !email.equals(that.email) : that.email != null) return false;
        if (trackingId != null ? !trackingId.equals(that.trackingId) : that.trackingId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = login != null ? login.hashCode() : 0;
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (trackingId != null ? trackingId.hashCode() : 0);
        return result;
    }


    @Override
    public String toString() {
        return "User{" +
                "login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", trackingId='" + trackingId + '\'' +
                ", site=" + site +
                ", telephony=" + telephony +
                '}';
    }
}
