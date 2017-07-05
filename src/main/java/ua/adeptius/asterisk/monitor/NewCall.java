package ua.adeptius.asterisk.monitor;


import org.codehaus.jackson.annotate.JsonIgnore;
import ua.adeptius.asterisk.model.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NewCall {


    // Нужно для опеределения кому принадлежит тот номер на который позвонил посетитель
    // а не на кого он на самом деле попал. Для трекинга
    @JsonIgnore
    private String firstCall;
    private String calledFrom;
    private String calledTo;

    private CallState callState;
    private Direction direction;

    private String asteriskId;
    private String utm = "";
    private String googleId = "";

    @JsonIgnore
    private Service service;

    @JsonIgnore
    private User user;

    private String calledDate;
    private long calledMillis;
    private int secondsToAnswer = -1; // Значение задаётся только 1 раз, если оно изначально -1. Астериск присылает 2 раза сообщение об ответе. Это просто защита.
    //    private int secondsTalk;
    private int secondsFullTime;


    public long getCalledMillis() {
        return calledMillis;
    }

    public String getCalledDate() { // что бы ускорить - создать стринговую переменную в которую будет ложится строка из БД для отдачи на сайт
        return calledDate;
//        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(getCalledMillis()));
    }

    public void setCalledDate(Date calledDate) {
        calledMillis = calledDate.getTime();
        this.calledDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calledMillis);
    }

    public void setCalledDate(String calledDate) throws Exception {
        this.calledDate = calledDate;
        calledMillis = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(calledDate).getTime();
    }

    public void setAnsweredDate(Date answeredDate) { // Задаётся в CallProcessor
        if (secondsToAnswer == -1) { // защита, что бы данные вводились только 1 раз.
            secondsToAnswer = (int) ((answeredDate.getTime() - getCalledMillis()) / 1000);
        }
    }

    public void setSecondsToAnswer(int secondsToAnswer) { // Задаётся при чтении с БД
        this.secondsToAnswer = secondsToAnswer;
    }

    public int getSecondsToAnswer() {
        return secondsToAnswer==-1? secondsFullTime : secondsToAnswer;
    }

    public int getSecondsTalk() { // Тут высчитывается время разговора
        if (callState == CallState.ANSWER) {
            return getSecondsFullTime() - getSecondsToAnswer();
        } else {
            return 0;
        }
    }

    public void setEndedDate(Date endedDate) { // Задаётся в CallProcessor
        secondsFullTime = (int) ((endedDate.getTime() - getCalledMillis()) / 1000);
    }

    public void setSecondsFullTime(int secondsFullTime) { // Задаётся при чтении с БД
        this.secondsFullTime = secondsFullTime;
    }

    public int getSecondsFullTime() {
        return secondsFullTime;
    }

    public enum CallState {
        ANSWER, // звонок был принят и обработан сотрудником
        BUSY, // входящий звонок был, но линия была занята;
        FAIL,
        NOANSWER, // входящий вызов состоялся, но в течение времени ожидания ответа не был принят сотрудником
        CHANUNAVAIL // вызываемый номер был недоступен;
    }

    public enum Direction {
        IN, OUT
    }

    public enum Service {
        TRACKING, TELEPHONY
    }

    public String getUtm() {
        return utm;
    }

    public void setUtm(String utm) {
        this.utm = utm;
    }


    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getCalledFrom() {
        return calledFrom;
    }

    public void setCalledFrom(String calledFrom) {
        this.calledFrom = addZero(calledFrom);
    }

    public String getCalledTo() {
        return calledTo;
    }

    public void setCalledTo(String calledto) {
        this.calledTo = addZero(calledto);
    }

    public String getAsteriskId() {
        return asteriskId;
    }

    public void setAsteriskId(String asteriskId) {
        this.asteriskId = asteriskId;
    }

    public String getFirstCall() {
        return firstCall;
    }

    public void setFirstCall(String firstCall) {
        this.firstCall = firstCall;
    }

    public CallState getCallState() {
        if (callState == null){
            return CallState.FAIL; // null если с сип позвонить на 934027182. Номер не может быть вызван.
        }
        return callState;
    }

    public void setCallState(CallState callState) {
        this.callState = callState;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    @Override
    public String toString() {
        return "NewCall{" +
//                "\n firstCall='" + firstCall + '\'' +
                "\n calledFrom='" + calledFrom + '\'' +
                "\n calledTo='" + calledTo + '\'' +
                "\n callState=" + callState +
                "\n direction=" + direction +
                "\n asteriskId='" + asteriskId + '\'' +
                "\n utm='" + utm + '\'' +
                "\n googleId='" + googleId + '\'' +
                "\n service=" + service +
                "\n user=" + user.getLogin() +
                "\n calledDate='" + calledDate + '\'' +
                "\n calledMillis=" + calledMillis +
                "\n secondsToAnswer=" + secondsToAnswer +
                "\n secondsFullTime=" + secondsFullTime +
                "\n}";
    }
}