package ua.adeptius.asterisk.json;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import ua.adeptius.asterisk.model.User;
import ua.adeptius.asterisk.model.telephony.PhoneGroup;

import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(getterVisibility = NONE, isGetterVisibility = NONE)
public class JsonUser {

    @JsonProperty
    private String login;
    @JsonProperty
    private String email;
    @JsonProperty
    private String userPhoneNumber;
    @JsonProperty
    private String firstName;
    @JsonProperty
    private String lastName;
    @JsonProperty
    private String middleName;
    @JsonProperty
    private Set<PhoneGroup> phoneGroups;


    private String password; // нужен только для моего гуи при создании акка

    public JsonUser() {
    }

    public JsonUser(User user) {
        login = user.getLogin();
        email = user.getEmail();
        userPhoneNumber = user.getUserPhoneNumber();
        firstName = user.getFirstName();
        lastName = user.getLastName();
        middleName = user.getMiddleName();
        phoneGroups = user.getPhoneGroups();
    }


    public Set<PhoneGroup> getPhoneGroups() {
        return phoneGroups;
    }

    public void setPhoneGroups(Set<PhoneGroup> phoneGroups) {
        this.phoneGroups = phoneGroups;
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

    public String getUserPhoneNumber() {
        return userPhoneNumber;
    }

    public void setUserPhoneNumber(String userPhoneNumber) {
        this.userPhoneNumber = userPhoneNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    @Override
    public String toString() {
        return "JsonUser{" +
                "login='" + login + '\'' +
                ", email='" + email + '\'' +
                ", userPhoneNumber='" + userPhoneNumber + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", middleName='" + middleName + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
