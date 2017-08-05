package ua.adeptius.asterisk.model;


import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "roistat_accounts", schema = "calltrackdb")
public class RoistatAccount {

    @Id
    @GeneratedValue(generator = "increment")
    @GenericGenerator(name = "increment", strategy = "increment")
    @Column(name = "id")
    private int id;

    @Column(name = "nextelLogin",  insertable = false, updatable = false)
    private String nextelLogin;

    @Column(name = "projectNumber")
    private String projectNumber;

    @Column(name = "apiKey")
    private String apiKey;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "nextelLogin", referencedColumnName = "login")
    private User user;

    public String getNextelLogin() {
        return nextelLogin;
    }

    public void setNextelLogin(String nextelLogin) {
        this.nextelLogin = nextelLogin;
    }

    public String getProjectNumber() {
        return projectNumber;
    }

    public void setProjectNumber(String projectNumber) {
        this.projectNumber = projectNumber;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        if (user != null){
            nextelLogin = user.getLogin();
        }
        this.user = user;
    }

    @Override
    public String toString() {
        return "RoistatAccount{" +
                "nextelLogin='" + nextelLogin + '\'' +
                ", projectNumber='" + projectNumber + '\'' +
                ", apiKey='" + apiKey + '\'' +
                ", user=" + user.getLogin() +
                '}';
    }
}
