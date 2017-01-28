package ua.adeptius.asterisk.model;


import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.GregorianCalendar;

public class Phone {

    public Phone(String number) {
        this.number = number;
        googleId = new SimpleStringProperty("");
        ip = new SimpleStringProperty("");
    }

    private String number;
    private SimpleStringProperty googleId;
    private SimpleStringProperty ip;
    private SimpleLongProperty busyTime;
    private long timeToDie;

    public void markFree() {
        this.setGoogleId("");
        this.setIp("");
        this.busyTime = new SimpleLongProperty();
        this.timeToDie = 0;
    }

    public void extendTime(){
        this.timeToDie = new GregorianCalendar().getTimeInMillis();
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setGoogleId(String googleId) {
        this.googleId.set(googleId);
    }

    public void setIp(String ip) {
        this.ip.set(ip);
    }

    public void setBusyTime(long busyTime) {
        this.busyTime.set(busyTime);
    }

    public void setTimeToDie(long timeToDie) {
        this.timeToDie = timeToDie;
    }

    public String getNumber() {
        return number;
    }

    public String getGoogleId() {
        return googleId.get();
    }

    public SimpleStringProperty googleIdProperty() {
        return googleId;
    }

    public String getIp() {
        return ip.get();
    }

    public SimpleStringProperty ipProperty() {
        return ip;
    }

    public long getBusyTime() {
        return busyTime.get();
    }

    public SimpleLongProperty busyTimeProperty() {
        return busyTime;
    }

    public boolean isFree() {
        //TODO ip
        return googleId.get().equals("");
    }

    public long getUpdatedTime() {
        return timeToDie;
    }

    @Override
    public String toString() {
        return "Phone{" +
                "number='" + number + '\'' +
                ", googleId='" + getGoogleId() + '\'' +
                ", ip='" + getIp() + '\'' +
                ", busyTime=" + busyTime +
                ", free=" + isFree() +
                ", timeToDie=" + timeToDie +
                '}';
    }


}
