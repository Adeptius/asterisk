package ua.adeptius.asterisk.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "amo_phone_bindings", schema = "calltrackdb")
public class AmoPhoneBinding {

    public AmoPhoneBinding() {
    }

    @Id
    @GeneratedValue(generator = "increment") //галка в mysql "AI"
    @GenericGenerator(name = "increment", strategy = "increment")
    @Column(name = "id")
    private int id;

    @JsonIgnore
    @Column(name = "login")
    private String login;

    @Column(name = "worker")
    private String worker;

    @JsonIgnore
    @Column(name = "phone")
    private String phone;


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

    public String getWorker() {
        return worker;
    }

    public void setWorker(String worker) {
        this.worker = worker;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public AmoPhoneBinding(String login, String worker, String phone) {
        this.login = login;
        this.worker = worker;
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "Binding{" +
                "worker='" + worker + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
