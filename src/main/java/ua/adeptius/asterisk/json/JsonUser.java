package ua.adeptius.asterisk.json;


import ua.adeptius.asterisk.model.User;

public class JsonUser {

    private String login;
    private String password;
    private String email;
    private String trackingId;

    public JsonUser() {
    }

    public JsonUser(User user) {
        login = user.getLogin();
        password = user.getPassword();
        email = user.getEmail();
        trackingId = user.getTrackingId();
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
    public String toString() {
        return "JsonUser{" +
                "login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", trackingId='" + trackingId + '\'' +
                '}';
    }
}
