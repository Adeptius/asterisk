package ua.adeptius.asterisk.model;



import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;

@Entity
@Table(name = "innerphones", schema = "calltrackdb")
@com.fasterxml.jackson.annotation.JsonAutoDetect(
        creatorVisibility = com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE,
        fieldVisibility = com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE,
        getterVisibility = com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE,
        setterVisibility = com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE
)
public class InnerPhone {

    @JsonProperty
    @Id
    @Column(name = "number")
    private String number;

    @Column(name = "busy")
    private String busy;

    @JsonProperty
    @Column(name = "pass")
    private String pass;


    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getBusy() {
        return busy;
    }

    public void setBusy(String busy) {
        this.busy = busy;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String password) {
        this.pass = password;
    }

    @Override
    public String toString() {
        return "InnerPhone{" +
                "number='" + number + '\'' +
                ", busy='" + busy + '\'' +
                ", pass='" + pass + '\'' +
                '}';
    }
}
