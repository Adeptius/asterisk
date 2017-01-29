package ua.adeptius.asterisk.model;


import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.GregorianCalendar;

public class Phone {

    public Phone(String number) {
        this.number = number;
        googleId = new SimpleStringProperty("");
        ip = new SimpleStringProperty("");
        busyTime = new SimpleStringProperty("");
    }

    private String number;
    private SimpleStringProperty googleId;
    private SimpleStringProperty ip;
    private SimpleStringProperty busyTime; // время которое телефон занят. Отображается в гуи. Вычисляется наблюдателем относительно времени startedBusy
    private long timeToDie;   // время аренды. Обновляется при вызове extendTime. Если это значение+12000 больше текущего времени - наблюдатель освобождает телефон
    private long startedBusy; // время мс когда был занят телефон. устанавливается при установке айди

    public void markFree() {
        this.setGoogleId("");
        this.setIp("");
//        this.busyTime = new SimpleLongProperty();
        this.timeToDie = 0;
        this.startedBusy = 0;
        this.busyTime = new SimpleStringProperty("");
    }

    public void extendTime(){
        this.timeToDie = new GregorianCalendar().getTimeInMillis();
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setGoogleId(String googleId) {// если записывается айди - значит он теперь кем-то занят
        this.startedBusy = new GregorianCalendar().getTimeInMillis();
        setBusyTime(0);
        this.googleId.set(googleId);
    }

    public long getStartedBusy() {
        return startedBusy;
    }

    public void setIp(String ip) {
        this.ip.set(ip);
    }

    public void setBusyTime(long busyTime) {
        int minutes = (int) (busyTime/60);
        int seconds = (int) (busyTime%60);
        String stringedTime = minutes+"м "+seconds+"с" ;
        this.busyTime.set(stringedTime);
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

    public String getBusyTimeText() {
        return busyTime.get();
    }

    public SimpleStringProperty busyTimeProperty() {
        return busyTime;
    }

    public boolean isFree() {// так понятно что телефон ничей
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
