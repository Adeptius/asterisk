package ua.adeptius.asterisk.model;


import org.codehaus.jackson.annotate.JsonIgnore;
import ua.adeptius.asterisk.utils.StringUtils;

import java.util.GregorianCalendar;

public class Phone {

    public Phone(String number) {
        this.number = number;
        googleId = "";
        ip = "";
        busyTime = "";
    }

    private String number;
    private String googleId;
    private String ip;
    @JsonIgnore
    private String busyTime; // время которое телефон занят. Отображается в гуи. Вычисляется наблюдателем относительно времени startedBusy
    @JsonIgnore
    private long updatedTime;   // время аренды. Обновляется при вызове extendTime. Если это значение+12000 больше текущего времени - наблюдатель освобождает телефон
    @JsonIgnore
    private long startedBusy; // время мс когда был занят телефон. устанавливается при установке айди
    private String utmRequest;

    public String getUtmRequest() {
        return utmRequest;
    }

    public void setUtmRequest(String utmRequest) {
        this.utmRequest = utmRequest;
    }

    public void markFree() {
        this.setGoogleId("");
        this.setIp("");
        this.updatedTime = 0;
        this.startedBusy = 0;
        this.busyTime = "";
    }

    public void extendTime(){
        this.updatedTime = new GregorianCalendar().getTimeInMillis();
    }

//    public void setNumber(String number) {
//        this.number = number;
//    }

    public void setGoogleId(String googleId) {// если записывается айди - значит он теперь кем-то занят
        this.startedBusy = new GregorianCalendar().getTimeInMillis();
        setBusyTime(0);
        this.googleId = googleId;
    }

    public long getStartedBusy() {
        return startedBusy;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setBusyTime(long busyTime) {
        this.busyTime = StringUtils.getStringedTime(busyTime);
    }

//    public void setTimeToDie(long updatedTime) {
//        this.updatedTime = updatedTime;
//    }

    public String getNumber() {
        return number;
    }

    public String getGoogleId() {
        return googleId;
    }

    public String getIp() {
        return ip;
    }

    public String getBusyTimeText() {
        return busyTime;
    }

    public boolean isFree() {// так понятно что телефон ничей
        return googleId.equals("");
    }

    public long getUpdatedTime() {
        return updatedTime;
    }


}
