package ua.adeptius.asterisk.model;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.model.telephony.OuterPhone;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;


@Entity
@Table(name = "sites", schema = "calltrackdb")
@JsonAutoDetect(getterVisibility = NONE, isGetterVisibility = NONE)
public class Site {

    private static Logger LOGGER = LoggerFactory.getLogger(Site.class.getSimpleName());

    public Site() {
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

    @JsonProperty
    @Column(name = "standard_number")
    private String standardNumber = "";

    @JsonProperty
    @Column(name = "time_to_block")
    private int timeToBlock;

    @Column(name = "black_ips")
    private String blackIps = "";

    @JsonProperty
    @Column(name = "google_tracking_id")
    private String googleTrackingId;

    @ManyToOne
    @JoinColumn(name = "login", insertable = false, updatable = false)
    private User user;

    @Transient
    LinkedList<String> blackLinkedList;

    @JsonProperty
    public String getScript(){
        return "<script src=\"https://cstat.nextel.com.ua:8443/tracking/script/"+login+"/"+name+"\"></script>";
    }

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

    // при задании сайта через контроллер, перезаписываем текучий список
    public void setBlackLinkedList(ArrayList<String> blackList) {
        blackIps = ""; // очищаем текущий список
        blackLinkedList = null; // удаляем кеш
        for (String s : blackList) {
            if (s.length() > 15){
                continue;
            }
            addIpToBlackList(s);
        }
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


//    @Transient
//    private List<OuterPhone> outerPhonesCache;
//
//    @JsonProperty
//    public List<OuterPhone> getOuterPhones(){
//        if (outerPhonesCache == null){
//            outerPhonesCache = user.getOuterPhones().stream()
//                    .filter(outerPhone -> name.equals(outerPhone.getSitename()))
//                    .collect(Collectors.toList());
//        }
//        return outerPhonesCache;
//    }
//
//    public void clearOuterPhonesCache(){
//        outerPhonesCache = null;
//    }


    @JsonProperty
    public List<OuterPhone> getOuterPhones(){
        return user.getOuterPhones().stream()
                .filter(outerPhone -> name.equals(outerPhone.getSitename()))
                .collect(Collectors.toList());
    }

    @Transient
    private long lastEmailTime;

    public boolean didEnoughTimePassFromLastEmail(){
        if (lastEmailTime == 0){
            lastEmailTime = System.currentTimeMillis();
            return true;
        }else {
            long past = (System.currentTimeMillis() - lastEmailTime) / 60000; // минут
            int mailAntispam = Main.settings.getMailAntiSpam();
            lastEmailTime = System.currentTimeMillis();
            return past > mailAntispam;
        }
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

    public String getGoogleTrackingId() {
        return googleTrackingId;
    }

    public void setGoogleTrackingId(String googleTrackingId) {
        this.googleTrackingId = googleTrackingId;
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
                ", googleTrackingId='" + googleTrackingId + '\'' +
                ", blackIps length='" + (blackIps == null? "0" : blackIps.length()) + '\'' +
                ", user=" + user.getLogin() +
                '}';
    }
}
