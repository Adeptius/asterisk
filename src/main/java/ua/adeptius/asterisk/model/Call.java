package ua.adeptius.asterisk.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.text.SimpleDateFormat;
import java.util.Date;

@JsonAutoDetect(
        creatorVisibility = com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE,
        fieldVisibility = com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE,
        getterVisibility = com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE,
        setterVisibility = com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE
)
public class Call {

    // Нужно для опеределения кому принадлежит тот номер на который позвонил посетитель
    // а не на кого он на самом деле попал. Для трекинга

    private String firstCall;

    @JsonProperty
    private String calledFrom;

    @JsonProperty
    private String calledTo;

    @JsonProperty
    private CallState callState;

    @JsonProperty
    private Direction direction;

    @JsonProperty
    private String asteriskId;

    @JsonProperty
    private String utm;

    @JsonProperty
    private String googleId;
    private Service service;
    private User user;

    @JsonProperty
    private String calledDate;

    @JsonProperty
    private long calledMillis;

    @JsonProperty
    private int secondsToAnswer = -1; // Значение задаётся только 1 раз, если оно изначально -1. Астериск присылает 2 раза сообщение об ответе. Это просто защита.

    @JsonProperty
    private int secondsFullTime;
    boolean callIsEnded;
    private int amoDealId;
    private int lastOperationTime;
    private OuterPhone outerPhone;

    public int getCalculatedModifiedTime() { // AUTOINCREMENT
        int currentTime = ((int) ((new Date().getTime() / 1000))) + timeDifference;
        if (currentTime <= lastOperationTime) { // если текущее время совпадает с предыдущим
            lastOperationTime++;
            return lastOperationTime;
        } else {
            return currentTime;
        }
    }

    private int timeDifference;


    public void setLastOperationTime(int lastOperationTime) {
        this.lastOperationTime = lastOperationTime;
        timeDifference = lastOperationTime - ((int) new Date().getTime() / 1000);
    }

    public int getAmoDealId() {
        return amoDealId;
    }

    public void setAmoDealId(int amoDealId) {
        this.amoDealId = amoDealId;
    }


    public long getCalledMillis() {
        return calledMillis;
    }

    public String getCalledDate() {
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

    /**
     * Возвращает true callProcessor'у что бы сообщить установилось ли значение впервые впервые
     */
    public boolean setAnsweredDate(Date answeredDate) { // Задаётся в CallProcessor
        if (secondsToAnswer == -1) { // защита, что бы данные вводились только 1 раз.
            secondsToAnswer = (int) ((answeredDate.getTime() - getCalledMillis()) / 1000);
            return true;
        } else {
            return false;
        }
    }

    public void setSecondsToAnswer(int secondsToAnswer) { // Задаётся при чтении с БД
        this.secondsToAnswer = secondsToAnswer;
    }

    @JsonProperty
    public int getSecondsToAnswer() {
        return secondsToAnswer == -1 ? secondsFullTime : secondsToAnswer;
    }

    @JsonProperty
    public int getSecondsTalk() { // Тут высчитывается время разговора
        if (callState == CallState.ANSWER) {
            return getSecondsFullTime() - getSecondsToAnswer();
        } else {
            return 0;
        }
    }

    public void setEndedDate(Date endedDate) { // Задаётся в CallProcessor
        secondsFullTime = (int) ((endedDate.getTime() - getCalledMillis()) / 1000);
        callIsEnded = true;
    }

    public boolean isCallIsEnded() { // Задаётся в CallProcessor
        return callIsEnded;
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
        if (callState == null) {
            return CallState.FAIL; // null если с сип позвонить на 934027182. Номер не может быть вызван.
        }
        return callState;
    }

    public boolean isStateWasAlreadySetted(){
        return stateIsSetted;
    }

    private boolean stateIsSetted;

    public void setCallState(CallState callState) {
        this.callState = callState;
        stateIsSetted = true;
    }

    public OuterPhone getOuterPhone() {
        return outerPhone;
    }

    public void setOuterPhone(OuterPhone outerPhone) {
        this.outerPhone = outerPhone;
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
                "firstCall='" + firstCall + '\'' +
                ", calledFrom='" + calledFrom + '\'' +
                ", calledTo='" + calledTo + '\'' +
                ", callState=" + callState +
                ", direction=" + direction +
                ", asteriskId='" + asteriskId + '\'' +
                ", utm='" + utm + '\'' +
                ", googleId='" + googleId + '\'' +
                ", service=" + service +
                ", user=" + user.getLogin() +
                ", calledDate='" + calledDate + '\'' +
                ", calledMillis=" + calledMillis +
                ", secondsToAnswer=" + secondsToAnswer +
                ", secondsFullTime=" + secondsFullTime +
                ", callIsEnded=" + callIsEnded +
                ", amoDealId=" + amoDealId +
                ", lastOperationTime=" + lastOperationTime +
                ", timeDifference=" + timeDifference +
                ", stateIsSetted=" + stateIsSetted +
                '}';
    }
}
