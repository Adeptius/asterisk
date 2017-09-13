package ua.adeptius.asterisk.json;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import ua.adeptius.asterisk.model.User;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(getterVisibility = NONE, isGetterVisibility = NONE)
public class JsonUser {

    @JsonProperty
    private String login;
    @JsonProperty
    private String email;
    @JsonProperty
    private String trackingId;
    @JsonProperty
    private boolean amoConnected;

    private String password;

    public JsonUser() {
    }

    public JsonUser(User user) {
        login = user.getLogin();
//        password = user.getPassword();
        email = user.getEmail();
        trackingId = user.getTrackingId();
        amoConnected = user.getAmoAccount() != null;
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

    public boolean isAmoConnected() {
        return amoConnected;
    }

    @Override
    public String toString() {
        return "JsonUser{" +
                "login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", trackingId='" + trackingId + '\'' +
                '}';
    }
}
