package ua.adeptius.asterisk.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;


@Entity
@Table(name = "sites", schema = "calltrackdb")
@JsonIgnoreProperties("user")
@com.fasterxml.jackson.annotation.JsonAutoDetect(
        creatorVisibility = com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE,
        fieldVisibility = com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE,
        getterVisibility = com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE,
        setterVisibility = com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE
)
public class Site {

    private static Logger LOGGER = LoggerFactory.getLogger(Site.class.getSimpleName());

    public Site() {
    }

    @JsonProperty
    @Id
    @GeneratedValue(generator = "increment") //галка в mysql "AI"
    @GenericGenerator(name = "increment", strategy = "increment")
    @Column(name = "id")
    private int id;

    @Column(name = "login", insertable = false, updatable = false)
    private String login;

    @JsonProperty
    @Column(name = "name")
    private String name;

    @JsonProperty
    @Column(name = "standard_number")
    private String standardNumber = "";

    @JsonProperty
    @Column(name = "time_to_block")
    private int timeToBlock;

    @Column(name = "black_ips")
    private String blackIps = "";

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "login", referencedColumnName = "login")
    private User user;

    @Transient
    LinkedList<String> blackLinkedList;

    @JsonProperty
    public LinkedList<String> getBlackList() {
        if (blackLinkedList == null) {
            blackLinkedList = new LinkedList<>();
            if (blackIps == null) {
                blackIps = "";
            }
            String[] spl = blackIps.split(" ");
            for (int i = 1; i < spl.length; i++) {
                blackLinkedList.add(0, spl[i]);
            }
        }
        return blackLinkedList;
    }

    public void addIpToBlackList(String ip) {
        getBlackList().add(0, ip);
        blackIps = (" " + ip) + blackIps;
        checkBlackListSize();
    }

    public void removeIpFromBlackList(String ip) {
        List<String> currentList = getBlackList();
        currentList.removeIf(s -> s.equals(ip));
        StringBuilder sb = new StringBuilder(200);
        for (String s : currentList) {
            sb.append(" ").append(s);
        }
        blackIps = sb.toString();
    }

    private void checkBlackListSize() {
        if (blackLinkedList.size() > 99) {
            for (int i = 0; i < 10; i++) {
                blackLinkedList.removeLast();
            }
        }
        StringBuilder sb = new StringBuilder(200);
        for (String s : blackLinkedList) {
            sb.append(" ").append(s);
        }
        blackIps = sb.toString();
    }

    @JsonProperty
    public List<OuterPhone> getOuterPhones(){//Todo Оптимизация
        return user.getOuterPhones().stream()
                .filter(outerPhone -> name.equals(outerPhone.getSitename()))
                .collect(Collectors.toList());
    }

    public void releaseAllPhones(){
        getOuterPhones().stream().forEach(phone -> phone.setSitename(null));
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

    public String getStandardNumber() {
        return standardNumber;
    }

    public void setStandardNumber(String standardNumber) {
        this.standardNumber = standardNumber;
    }

    public int getTimeToBlock() {
        return timeToBlock;
    }

    public void setTimeToBlock(int timeToBlock) {
        this.timeToBlock = timeToBlock;
    }

    public String getBlackIps() {
        return blackIps;
    }

    public void setBlackIps(String blackIps) {
        this.blackIps = blackIps;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            login = user.getLogin();
        }
    }


    @Override
    public String toString() {
        return "Site{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", name='" + name + '\'' +
                ", standardNumber='" + standardNumber + '\'' +
                ", timeToBlock=" + timeToBlock +
                ", blackIps='" + blackIps + '\'' +
                ", user=" + user.getLogin() +
                '}';
    }
}
