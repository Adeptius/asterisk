package ua.adeptius.asterisk.model;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@Entity
@Table(name = "roistat_accounts", schema = "calltrackdb")
@JsonAutoDetect(getterVisibility = NONE, isGetterVisibility = NONE)
public class RoistatAccount {

    @Id
    @GeneratedValue(generator = "increment")
    @GenericGenerator(name = "increment", strategy = "increment")
    @Column(name = "id")
    private int id;

    @Column(name = "nextel_login")
    private String nextelLogin;

    @JsonProperty
    @Column(name = "project_number")
    private String projectNumber;

    @JsonProperty
    @Column(name = "api_key")
    private String apiKey;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "nextel_login",  insertable = false, updatable = false)
    private User user;

//    public String getNextelLogin() {
//        return nextelLogin;
//    }

//    public void setNextelLogin(String nextelLogin) {
//        this.nextelLogin = nextelLogin;
//    }

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

    void setUser(User user) {
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
