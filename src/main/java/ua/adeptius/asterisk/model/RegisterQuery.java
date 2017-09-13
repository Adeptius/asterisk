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
@Table(name = "register_query")
public class RegisterQuery implements Serializable {

    public RegisterQuery() {
    }

    public RegisterQuery(@Nonnull String login, @Nonnull String password, @Nonnull String email) {
        this.login = login;
        this.password = password;
        this.email = email;
        hash = MyStringUtils.generateRandomKey();
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
    private String hash;

    @Column(name = "date")
    private Date date;

    @Override
    public String toString() {
        return "RegisterQuery{" +
                "login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", hash='" + hash + '\'' +
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

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RegisterQuery that = (RegisterQuery) o;

        return login != null ? login.equals(that.login) : that.login == null;
    }

    @Override
    public int hashCode() {
        return login != null ? login.hashCode() : 0;
    }
}
