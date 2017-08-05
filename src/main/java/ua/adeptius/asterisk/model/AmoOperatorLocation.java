package ua.adeptius.asterisk.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "amo_operator_location", schema = "calltrackdb")
public class AmoOperatorLocation {

    public AmoOperatorLocation() {
    }

    @Id
    @GeneratedValue(generator = "increment") //галка в mysql "AI"
    @GenericGenerator(name = "increment", strategy = "increment")
    @Column(name = "id")
    private int id;

    @JsonIgnore
    @Column(name = "login", insertable = false, updatable = false)
    private String login;

    @Column(name = "name")
    private String name;

    @JsonIgnore
    @Column(name = "binding_string")
    private String bindingString;

//    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "login", referencedColumnName = "login")
    private User user;

    @Transient
    private HashMap<String, String> amoUserIdAndInnerNumber = null;

    @Transient
    private HashMap<String, String> innerNumberAndAmoUserId = null;

    public HashMap<String, String> getAmoUserIdAndInnerNumber() {
        if (amoUserIdAndInnerNumber != null) {
            return amoUserIdAndInnerNumber;
        }
        if (bindingString == null){
            return new HashMap<>();
        }
        amoUserIdAndInnerNumber = new HashMap<>();
        String[] idAndPhone = bindingString.split(" ");
        for (String pair : idAndPhone) {
            String[] splPair = pair.split("=");
            amoUserIdAndInnerNumber.put(splPair[0], splPair[1]);
        }
        return amoUserIdAndInnerNumber;
    }

    public HashMap<String, String> getInnerNumberAndAmoUserId() {
        if (innerNumberAndAmoUserId != null) {
            return innerNumberAndAmoUserId;
        }
        if (bindingString == null){
            return new HashMap<>();
        }
        innerNumberAndAmoUserId = new HashMap<>();
        String[] idAndPhone = bindingString.split(" ");
        for (String pair : idAndPhone) {
            String[] splPair = pair.split("=");
            innerNumberAndAmoUserId.put(splPair[1], splPair[0]);
        }
        return innerNumberAndAmoUserId;
    }

    public void setAmoUserIdAndInnerNumber(HashMap<String, String> amoUserIdAndInnerNumber) {
        amoUserIdAndInnerNumber = null;
        innerNumberAndAmoUserId = null;
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : amoUserIdAndInnerNumber.entrySet()) {
            sb.append(" ").append(entry.getKey()).append("=").append(entry.getValue());
        }
        String result = sb.toString();
        if (result.length()>0){
            bindingString = result.substring(1);
        }else {
            bindingString = null;
        }
    }

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

    public String getBindingString() {
        return bindingString;
    }

    public void setBindingString(String bindingString) {
        this.bindingString = bindingString;
    }

    @Override
    public String toString() {
        return "AmoOperatorLocation{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", name='" + name + '\'' +
                ", bindingString='" + bindingString + '\'' +
//                ", amoAccount=" + amoAccount.getNextelLogin() +
                ", user=" + user.getLogin() +
                '}';
    }
}
