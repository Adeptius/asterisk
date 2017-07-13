package ua.adeptius.asterisk.model;


import org.codehaus.jackson.annotate.JsonIgnore;

import javax.persistence.*;

@Entity
@Table(name = "roistat_accounts", schema = "calltrackdb")
public class RoistatAccount {

    @Id
    @Column(name = "nextelLogin")
    private String nextelLogin;

    @Column(name = "projectNumber")
    private String projectNumber;

    @Column(name = "apiKey")
    private String apiKey;

    @JsonIgnore
    @OneToOne
    @PrimaryKeyJoinColumn
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
