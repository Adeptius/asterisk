package ua.adeptius.asterisk.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "amo_phone_bindings", schema = "calltrackdb")
public class AmoPhoneBinding {

    public AmoPhoneBinding() {
    }

    public AmoPhoneBinding(String worker, String phone, AmoAccount amoAccount) {
        this.worker = worker;
        this.phone = phone;
        this.amoAccount = amoAccount;
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

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "login", referencedColumnName = "nextellogin", insertable = false, updatable = false)
    private AmoAccount amoAccount;

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


    public void setAmoAccount(AmoAccount amoAccount) {
        this.amoAccount = amoAccount;
        login = amoAccount.getNextelLogin();
    }

    @Override
    public String toString() {
        return "Binding{" +
                "worker='" + worker + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
