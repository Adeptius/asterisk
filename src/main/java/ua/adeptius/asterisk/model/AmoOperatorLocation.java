package ua.adeptius.asterisk.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@Entity
@Table(name = "amo_operator_location", schema = "calltrackdb")
@JsonAutoDetect(getterVisibility = NONE, isGetterVisibility = NONE)
public class AmoOperatorLocation {

    public AmoOperatorLocation() {
    }

    @Id
    @GeneratedValue(generator = "increment") //галка в mysql "AI"
    @GenericGenerator(name = "increment", strategy = "increment")
    @Column(name = "id")
    private int id;

    @Column(name = "login")
    private String login;

    @JsonProperty
    @Column(name = "name")
    private String name;

    @Column(name = "binding_string")
    private String bindingString;

    @ManyToOne
    @JoinColumn(name = "login", insertable = false, updatable = false)
    private User user;

//    @ManyToOne
//    @JoinColumn(name = "login")
//    private User user;

    @Transient
    private HashMap<String, String> amoUserIdAndInnerNumber = null;

    @Transient
    private HashMap<String, String> innerNumberAndAmoUserId = null;

    public HashMap<String, String> getAmoUserIdAndInnerNumber() {
        if (amoUserIdAndInnerNumber != null) {
            return amoUserIdAndInnerNumber;
        }
        if (bindingString == null){
            amoUserIdAndInnerNumber = new HashMap<>();
            return amoUserIdAndInnerNumber;
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
            innerNumberAndAmoUserId = new HashMap<>();
            return innerNumberAndAmoUserId;
        }
        innerNumberAndAmoUserId = new HashMap<>();
        String[] idAndPhone = bindingString.split(" ");
        for (String pair : idAndPhone) {
            String[] splPair = pair.split("=");
            innerNumberAndAmoUserId.put(splPair[1], splPair[0]);
        }
        return innerNumberAndAmoUserId;
    }

    public void setAmoUserIdAndInnerNumber(HashMap<String, String> usersAndPhones) {
        amoUserIdAndInnerNumber = null;
        innerNumberAndAmoUserId = null;
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : usersAndPhones.entrySet()) {
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        if (user != null){
            login = user.getLogin();
        }
        this.user = user;
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
