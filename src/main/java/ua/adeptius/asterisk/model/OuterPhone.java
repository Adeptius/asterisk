package ua.adeptius.asterisk.model;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "outerphones", schema = "calltrackdb")
public class OuterPhone {

    @Id
    @Column(name = "number")
    private String number;

    @Column(name = "busy")
    private String busy;

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

    @Override
    public String toString() {
        return "OuterPhone{" +
                "number='" + number + '\'' +
                ", busy='" + busy + '\'' +
                '}';
    }
}
