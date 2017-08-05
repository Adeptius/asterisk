package ua.adeptius.asterisk.model;


import org.codehaus.jackson.annotate.JsonIgnore;
import ua.adeptius.asterisk.utils.StringUtils;

import javax.persistence.*;
import java.util.GregorianCalendar;

@SuppressWarnings("Duplicates")
@Entity
@Table(name = "outerphones", schema = "calltrackdb")
public class OuterPhone {

    @Id
    @Column(name = "number")
    private String number;

    @Column(name = "busy")
    private String busy;

    @Column(name = "site_name")
    private String sitename = "";


    @Transient
    private String googleId = "";

    @Transient
    private String ip;

    @JsonIgnore
    @Transient
    private String busyTime; // время которое телефон занят. Вычисляется наблюдателем относительно времени startedBusy

    @JsonIgnore
    @Transient
    private long updatedTime;   // время аренды. Обновляется при вызове extendTime. Если это значение+12000 больше текущего времени - наблюдатель освобождает телефон

    @JsonIgnore
    @Transient
    private long startedBusy; // время мс когда был занят телефон. устанавливается при установке айди

    @Transient
    private String utmRequest;

    public String getUtmRequest() {
        if (utmRequest == null){
            return "";
        }else {
            return utmRequest;
        }
    }

    public void setUtmRequest(String utmRequest) {
        this.utmRequest = filterUtmMarks(utmRequest);
    }

    private static String filterUtmMarks(String s) {
        if (s == null || "".equals(s)) { // если параметров нет вообще
            return s;
        }
        if (!s.contains("=")) { // если почему-то не пусто но и параметра нет
            return "";
        }
        String[] keys = new String[]{"utm_source", "utm_medium", "utm_campaign", "utm_content", "utm_term", "pm_source", "pm_block", "pm_position"};
        String result = "";
        if (s.contains("&")) { // если параметров несколько
            String[] splitted = s.split("&");
            for (int i = 0; i < splitted.length; i++) {
                if (splitted[i].contains("=")) { // защита, если попадёт хрен знает что вместо ключ=значение
                    String currentKey = splitted[i].substring(0, splitted[i].indexOf("="));
                    for (int j = 0; j < keys.length; j++) {
                        if (currentKey.equals(keys[j])) {
                            String currentValue = splitted[i].substring(splitted[i].indexOf("=") + 1);
                            if (!currentValue.equals("")) { // если значение не пустое.
                                if (!result.equals("")) { // вначале & добавлять не нужно
                                    result += "&";
                                }
                                result += splitted[i];
                            }
                        }
                    }
                }
            }
            return result;
        } else {// если параметр 1
            for (int i = 0; i < keys.length; i++) {
                String key = s.substring(0, s.indexOf("="));
                if (keys[i].equals(key)) {
                    return s;
                }
            }
            return "";
        }
    }

    public void markFree() {
        this.setGoogleId("");
        this.setIp("");
        this.updatedTime = 0;
        this.startedBusy = 0;
        this.busyTime = "";
    }

    public boolean isFree() {// так понятно что телефон ничей
        return googleId.equals("");
    }

    public void setBusyTime(long busyTime) {
        this.busyTime = StringUtils.getStringedTime(busyTime);
    }


    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
//
    public String getBusy() {
        return busy;
    }

    public void setBusy(String busy) {
        this.busy = busy;
    }

    public String getSitename() {
        return sitename;
    }

    public void setSitename(String sitename) {
        this.sitename = sitename;
    }

    public void extendTime(){
        this.updatedTime = new GregorianCalendar().getTimeInMillis();
    }

    public void setGoogleId(String googleId) {// если записывается айди - значит он теперь кем-то занят
        this.startedBusy = new GregorianCalendar().getTimeInMillis();
        setBusyTime(0);
        this.googleId = googleId;
    }


    public String getGoogleId() {
        return googleId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getBusyTime() {
        return busyTime;
    }

    public void setBusyTime(String busyTime) {
        this.busyTime = busyTime;
    }

    public long getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
    }

    public long getStartedBusy() {
        return startedBusy;
    }

    public void setStartedBusy(long startedBusy) {
        this.startedBusy = startedBusy;
    }

    @Override
    public String toString() {
        return "OuterPhone{" +
                "number='" + number + '\'' +
                ", busy='" + busy + '\'' +
                '}';
    }
}
