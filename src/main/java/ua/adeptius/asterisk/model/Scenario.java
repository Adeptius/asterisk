package ua.adeptius.asterisk.model;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@Entity
@Table(name = "scenarios", schema = "calltrackdb")
@JsonAutoDetect(getterVisibility = NONE, isGetterVisibility = NONE)
public class Scenario {

    @Id
    @GeneratedValue(generator = "increment") //галка в mysql "AI"
    @GenericGenerator(name = "increment", strategy = "increment")
    @Column(name = "id")
    private int id;


    @JsonIgnore
    @Column(name = "login")
    private String login;

    @Column(name = "name")
    private String name;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Scenario{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
