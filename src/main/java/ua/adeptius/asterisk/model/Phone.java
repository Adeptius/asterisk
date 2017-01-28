package ua.adeptius.asterisk.model;


public class Phone {

    public Phone(String number) {
        this.number = number;
    }

    private String number;
    private String googleId;
    private String ip;
    private long busyTime;
    private boolean busy;
    private long timeToDie;

    public String getNumber() {
        return number;
    }

    public String getGoogleId() {
        return googleId;
    }

    public String getIp() {
        return ip;
    }

    public long getBusyTime() {
        return busyTime;
    }

    public boolean isBusy() {
        return busy;
    }

    public long getTimeToDie() {
        return timeToDie;
    }

    @Override
    public String toString() {
        return "Phone{" +
                "number='" + number + '\'' +
                ", googleId='" + googleId + '\'' +
                ", ip='" + ip + '\'' +
                ", busyTime=" + busyTime +
                ", busy=" + busy +
                ", timeToDie=" + timeToDie +
                '}';
    }
}
