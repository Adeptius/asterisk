package ua.adeptius.asterisk.monitor;


import ua.adeptius.asterisk.newmodel.User;

import java.text.SimpleDateFormat;


public class Call {

    private String id;
    private String from;
    private String to;
    private String called;
    private int answered;
    private int ended;
    private CallState callState;
    private Direction direction;
    private String googleId;
    private String utm;
    private String firstCall;

    private transient User user;
    private transient long calledMillis;

    public void setDateForDb(long millis) {
        called = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(millis);
    }

    public String getGoogleId() {
        return googleId==null?"":googleId;
    }

    public String getFirstCall() {
        return firstCall;
    }

    public void setFirstCall(String firstCall) {
        this.firstCall = firstCall;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getUtm() {
        return utm==null ? "":utm;
    }

    public void setUtm(String utm) {
        this.utm = utm;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public CallState getCallState() {
        return callState;
    }

    public void setCallState(CallState callState) {
        this.callState = callState;
    }

    public String getCalled() {
        return called;
    }

    public void setCalled(String called) {
        this.called = called;
    }

    public long getCalledMillis() {
        return calledMillis;
    }

    public void setCalledMillis(long calledMillis) {
        this.calledMillis = calledMillis;
    }

    public int getAnswered() {
        return answered;
    }

    public void setAnswered(int answered) {
        this.answered = answered;
    }

    public int getEnded() {
        return ended;
    }

    public void setEnded(int ended) {
        this.ended = ended;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = addZero(from);
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = addZero(to);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public static String addZero(String source) {
        try {
            if (source.length() == 9 && !source.startsWith("0")) {
                source = "0" + source;
            }
        } catch (Exception e) {
//            System.out.println("Ошибка добавления нолика. Пришло " + source);
        }
        return source;
    }

    public enum CallState{
        ANSWERED, BUSY, FAIL
    }

    public enum Direction{
        IN, OUT
    }


    @Override
    public String toString() {
        return "Call{" +
                "\nid='" + id + '\'' +
                "\nfrom='" + from + '\'' +
                "\nfirstCall='" + firstCall + '\'' +
                "\nto='" + to + '\'' +
                "\ncustomer=" + user.getLogin() +
                "\ncalled=" + called +
                "\nanswered=" + answered +
                "\nended=" + ended +
                "\ncallState=" + callState +
                "\ndirection=" + direction +
                "\n}";
    }
}
