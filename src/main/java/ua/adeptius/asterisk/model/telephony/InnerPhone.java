package ua.adeptius.asterisk.model.telephony;



import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@Entity
@Table(name = "innerphones", schema = "calltrackdb")
@JsonAutoDetect(getterVisibility = NONE, isGetterVisibility = NONE)
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
