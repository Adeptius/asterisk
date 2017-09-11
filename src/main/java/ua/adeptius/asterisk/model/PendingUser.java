package ua.adeptius.asterisk.model;


import ua.adeptius.asterisk.utils.MyStringUtils;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "pending_user")
public class PendingUser implements Serializable {

    public PendingUser() {
    }

    public PendingUser(@Nonnull String login, @Nonnull String password, @Nonnull String email) {
        this.login = login;
        this.password = password;
        this.email = email;
        key = MyStringUtils.generateRandomKey();
        date = new Date();
    }

    @Id
    @Column(name = "login")
    private String login;

    @Column(name = "password")
    private String password;

    @Column(name = "email")
    private String email;

    @Column(name = "hash")
    private String key;

    @Column(name = "date")
    private Date date;

    @Override
    public String toString() {
        return "PendingUser{" +
                "login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", key='" + key + '\'' +
                ", date=" + date +
                '}';
    }


    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PendingUser that = (PendingUser) o;

        return login != null ? login.equals(that.login) : that.login == null;
    }

    @Override
    public int hashCode() {
        return login != null ? login.hashCode() : 0;
    }
}
