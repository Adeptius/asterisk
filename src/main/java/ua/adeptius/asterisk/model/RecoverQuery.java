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
@Table(name = "recover_query")
public class RecoverQuery implements Serializable {

    public RecoverQuery() {
    }

    public RecoverQuery(@Nonnull String login, @Nonnull String email) {
        this.login = login;
        hash = MyStringUtils.generateRandomKey();
        this.email = email;
        date = new Date();
    }

    @Id
    @Column(name = "hash")
    private String hash;

    @Column(name = "login")
    private String login;

    @Column(name = "date")
    private Date date;

    @Column(name = "email")
    private String email;

    @Override
    public String toString() {
        return "RecoverQuery{" +
                "login='" + login + '\'' +
                ", hash='" + hash + '\'' +
                ", date=" + date +
                '}';
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
