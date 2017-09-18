package ua.adeptius.asterisk.model.telephony;


import javax.persistence.*;

@Entity
@Table(name = "melodies", schema = "calltrackdb")
public class Melody {

    @Id
    @Column(name = "name")
    private String name;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Melody{" +
                "name=" + name +
                '}';
    }
}
